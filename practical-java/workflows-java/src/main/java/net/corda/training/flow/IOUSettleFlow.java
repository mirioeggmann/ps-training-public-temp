package net.corda.training.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.*;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.finance.workflows.asset.CashUtils;
import net.corda.training.contract.IOUContract;
import net.corda.training.state.IOUState;

import java.util.*;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;
import static net.corda.finance.workflows.GetBalances.getCashBalance;

public class IOUSettleFlow {

    /**
     * This is the flow which handles the settlement (partial or complete) of existing IOUs on the ledger.
     * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
     * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
     * The flow returns the [SignedTransaction] that was committed to the ledger.
     */
    @InitiatingFlow
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {

        private final UniqueIdentifier stateLinearId;
        private final Amount<Currency> amount;

        public InitiatorFlow(UniqueIdentifier stateLinearId, Amount<Currency> amount) {
            this.stateLinearId = stateLinearId;
            this.amount = amount;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // 1. Retrieve the IOU State from the vault using LinearStateQueryCriteria

            // 2. Get a reference to the inputState data that we are going to settle.

            // 3. Check the party running this flow is the borrower.

            // 4. We should now get some of the components required for to execute the transaction
            // Here we get a reference to the default notary and instantiate a transaction builder.

            // 5. Check we have enough cash to settle the requested amount


            // 6. Get some cash from the vault and add a spend to our transaction builder.

            // 7. Create a command. you will need to provide the Command constructor with a reference to the Settle Command as well as a list of required signers.

            // 8. Add the command and the input state to the transaction using the TransactionBuilder.

            // 9. Add an IOU output state if the IOU in question that has not been fully settled.

            // 10. Verify and sign the transaction

            // 11. Collect all of the required signatures from other Corda nodes using the CollectSignaturesFlow

            /* 12. Return the output of the FinalityFlow which sends the transaction to the notary for verification
             *     and the causes it to be persisted to the vault of appropriate nodes.
             */
            return null;

        }

    }

    /**
     * This is the flow which signs IOU settlements.
     * The signing is handled by the [SignTransactionFlow].
     */
    @InitiatedBy(IOUSettleFlow.InitiatorFlow.class)
    public static class Responder extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartyFlow;
        private SecureHash txWeJustSignedId;

        public Responder(FlowSession otherPartyFlow) {
            this.otherPartyFlow = otherPartyFlow;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    // Once the transaction has verified, initialize txWeJustSignedID variable.
                    txWeJustSignedId = stx.getId();
                }
            }

            // Create a sign transaction flow

            // Run the sign transaction flow to sign the transaction

            // Run the ReceiveFinalityFlow to finalize the transaction and persist it to the vault.

            return null;
        }
    }

}