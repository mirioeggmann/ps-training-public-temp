package net.corda.training.webserver;

import net.corda.core.contracts.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.internal.X500UtilsKt;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import net.corda.finance.contracts.asset.Cash;
import net.corda.finance.contracts.asset.Cash.State;
import net.corda.finance.workflows.GetBalances;
import net.corda.training.flow.IOUIssueFlow;
import net.corda.training.flow.IOUSettleFlow;
import net.corda.training.flow.IOUTransferFlow;
import net.corda.training.state.IOUState;
import net.corda.training.flow.SelfIssueCashFlow;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping({"/api/iou/"})
public final class MainController {
    @NotNull
    private static final List<String> SERVICE_NAMES = Arrays.asList("Notary", "Network Map Service");

    @NotNull
    public static List<String> getSERVICE_NAMES() {
        return SERVICE_NAMES;
    }

    private final CordaRPCOps proxy;
    private final CordaX500Name me;
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    public MainController(@NotNull NodeRPCConnection rpc) {
        this.proxy = rpc.getProxy();
        if(this.proxy.nodeInfo().getLegalIdentities().size() < 1) throw new IllegalArgumentException("There should be at least one legal identity.");
        this.me = this.proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    @NotNull
    public final String toDisplayString(@NotNull X500Name $receiver) {
        return BCStyle.INSTANCE.toString($receiver);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        if(this.proxy.notaryIdentities().size() < 1) return false;

        for(Party it : this.proxy.notaryIdentities()){
            if (nodeInfo.isLegalIdentity(it)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMe(NodeInfo nodeInfo) {
        if(nodeInfo.getLegalIdentities().size() < 1) return false;
        return nodeInfo.getLegalIdentities().get(0).getName().toString().equals(me.toString());
    }

    private boolean isNetworkMap(NodeInfo nodeInfo) {
        if(nodeInfo.getLegalIdentities().size() != 1) throw new IllegalArgumentException("There should be exactly one legal identity.");
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().toString().equals("Network Map Service");
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE}, value = {"hello"})
    public final Map<String, String> helloWorld() {
        Map<String, String> map = new LinkedHashMap<>();
        map.putIfAbsent("hello", "world");
        return map;
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(produces = {APPLICATION_JSON_VALUE}, value = {"me"})
    public final Map<String, String> whoami() {
        Map<String, String> map = new LinkedHashMap<>();
        map.putIfAbsent("me", this.me.toString());
        return map;
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GetMapping(produces = {APPLICATION_JSON_VALUE}, value = {"peers"})
    public final Map<String, List<String>> getPeers() {
        List<String> peers = new ArrayList<>();
        for(NodeInfo node : this.proxy.networkMapSnapshot()) {
            if(node.getLegalIdentities().size() < 1) throw new IllegalArgumentException("There should be at least one legal identity.");
            if(!isNotary(node) && !isMe(node) && !isNetworkMap(node)) {
                peers.add(this.toDisplayString(X500UtilsKt.toX500Name(node.getLegalIdentities().get(0).getName())));
            }
        }
        Map<String, List<String>> peersMap = new LinkedHashMap<>();
        peersMap.putIfAbsent("peers", peers);
        return peersMap;
    }

    /**
     * Task 1
     * Displays all IOU states that exist in the node's vault.
     * TODO: Return a list of IOUStates on ledger
     * Hint - Use [rpcOps] to query the vault all unconsumed [IOUState]s
     */
    @GetMapping(produces = {APPLICATION_JSON_VALUE}, value = {"ious"})
    public final List<StateAndRef<IOUState>> getIOUs() {
        return this.proxy.vaultQuery(IOUState.class).getStates();
    }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GetMapping(produces = {APPLICATION_JSON_VALUE}, value = {"cash"})
    public final List<StateAndRef<Cash.State>> getCash() {
        return this.proxy.vaultQuery(Cash.State.class).getStates();
    }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GetMapping(produces = {APPLICATION_JSON_VALUE}, value = {"cash-balances"})
    public final Map<Currency, Amount<Currency>> getCashBalances() {
        return GetBalances.getCashBalances(this.proxy);
    }

    /**
     * Initiates a flow to agree an IOU between two parties.
     * Example request:
     * curl -X PUT 'http://localhost:10007/api/iou/issue-iou?amount=99&currency=GBP&party=O=ParticipantC,L=New%20York,C=US'
     */
    @PostMapping(produces = {TEXT_PLAIN_VALUE}, value = {"issue-iou"})
    public final ResponseEntity<String> issueIOU(@RequestParam("amount") int amount,
                                                 @RequestParam("currency") @NotNull String currency,
                                                 @RequestParam("party") @NotNull String party) {
        if(this.proxy.nodeInfo().getLegalIdentities().size() < 1) throw new IllegalArgumentException("There should be at least one legal identity.");
        Party me = this.proxy.nodeInfo().getLegalIdentities().get(0);
        Party lender = this.proxy.wellKnownPartyFromX500Name(CordaX500Name.Companion.parse(party));
        if (lender != null) {
            ResponseEntity<String> response;
            try {
                // Create a new IOU state using the parameters given.
                IOUState state = new IOUState(new Amount<Currency>((long) amount * (long) 100, Currency.getInstance(currency)), lender, me);
                // Start the IOUIssueFlow. We block and waits for the flow to return.
                SignedTransaction result = this.proxy.startTrackedFlowDynamic(IOUIssueFlow.InitiatorFlow.class, new Object[]{state}).getReturnValue().get();
                if(result.getTx().getOutputs().size() != 1) throw new IllegalArgumentException("There should be at least one output.");
                TransactionState<ContractState> tx = result.getTx().getOutputs().get(0);
                IOUState txIOU = (IOUState)tx.getData();
                // Return the response.
                response = ResponseEntity.status(HttpStatus.CREATED).body("Transaction id " + result.getId() + " committed to ledger.\nLinearId: " + txIOU.getLinearId().toString() + ".\n" + txIOU.toString());
                return response;
            // For the purposes of this demo app, we do not differentiate by exception type.
            } catch (Exception e) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                return response;
            }
        } else {
            throw new IllegalArgumentException("Unknown party name.");
        }
    }

    /**
     * Transfers an IOU specified by [linearId] to a new party.
     * Example request:
     * curl -X GET 'http://localhost:10007/api/iou/transfer-iou?id=705dc5c5-44da-4006-a55b-e29f78955089&party=O=ParticipantC,L=New%20York,C=US'
     */
    @PostMapping(produces = {TEXT_PLAIN_VALUE}, value = {"transfer-iou"})
    public final ResponseEntity<String> transferIOU(@RequestParam("id") @NotNull String id, 
                                                    @RequestParam("party") @NotNull String party) {
        UniqueIdentifier linearId = UniqueIdentifier.Companion.fromString(id);
        Party newLender = this.proxy.wellKnownPartyFromX500Name(CordaX500Name.Companion.parse(party));
        if (newLender != null) {
            ResponseEntity<String> response;
            try {
                this.proxy.startFlowDynamic(IOUTransferFlow.InitiatorFlow.class, new Object[]{linearId, newLender}).getReturnValue().get();
                response = ResponseEntity.status(HttpStatus.CREATED).body("IOU " + id + " transferred to " + party + '.');
            } catch (Exception e) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }

            return response;
        } else {
            throw new IllegalArgumentException("Unknown party name.");
        }
    }

    /**
     * Settles an IOU. Requires cash in the right currency to be able to settle.
     * Example request:
     * curl -X GET 'http://localhost:10007/api/iou/settle-iou?id=705dc5c5-44da-4006-a55b-e29f78955089&amount=98&currency=USD'
     */
    @PostMapping(produces = {TEXT_PLAIN_VALUE}, value = {"settle-iou"})
    public final ResponseEntity<String> settleIOU(@RequestParam("id") @NotNull String id, 
                                                  @RequestParam("amount") int amount, 
                                                  @RequestParam("currency") @NotNull String currency) {
        UniqueIdentifier linearId = UniqueIdentifier.Companion.fromString(id);
        Amount<Currency> settleAmount = new Amount<Currency>((long) amount * (long) 100, Currency.getInstance(currency));

        ResponseEntity<String> response;
        try {
            this.proxy.startFlowDynamic(IOUSettleFlow.InitiatorFlow.class, new Object[]{linearId, settleAmount}).getReturnValue().get();
            response = ResponseEntity.status(HttpStatus.CREATED).body("" + amount + ' ' + currency + " paid off on IOU id " + id + '.');
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return response;
    }

    /**
     * Helper end-point to issue some cash to ourselves.
     * Example request:
     * curl -X GET 'http://localhost:10007/api/iou/self-issue-cash?amount=100&currency=USD'
     */
    @PostMapping(produces = {TEXT_PLAIN_VALUE}, value = {"self-issue-cash"})
    public final ResponseEntity<String> selfIssueCash(@RequestParam("amount") int amount, 
                                                      @RequestParam("currency") @NotNull String currency) {
        Amount<Currency> issueAmount = new Amount<Currency>((long) amount * (long) 100, Currency.getInstance(currency));

        ResponseEntity<String> response;
        try {
            State cashState = this.proxy.startFlowDynamic(SelfIssueCashFlow.class, new Object[]{issueAmount}).getReturnValue().get();
            response = ResponseEntity.status(HttpStatus.CREATED).body(cashState.toString());
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return response;
    }
}
