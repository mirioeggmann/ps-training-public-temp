package net.corda.training.flow;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.training.contract.IOUContract;
import net.corda.training.state.IOUState;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;


/**
 * This is the flow which handles transfers of existing IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
public class IOUTransferFlow{

    @InitiatingFlow
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {
        private final UniqueIdentifier stateLinearId;
        private final Party newLender;

        public InitiatorFlow(UniqueIdentifier stateLinearId, Party newLender) {
            this.stateLinearId = stateLinearId;
            this.newLender = newLender;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // 1. Retrieve the IOU State from the vault using LinearStateQueryCriteria

            // 2. Get a reference to the inputState data that we are going to settle.

            // 3. We should now get some of the components required for to execute the transaction
            // Here we get a reference to the default notary and instantiate a transaction builder.

            // 4. Construct a transfer command to be added to the transaction.


            // 5. Add the command to the transaction using the TransactionBuilder.

            // 6. Add input and output states to flow using the TransactionBuilder.

            // 7. Ensure that this flow is being executed by the current lender.

            // 8. Verify and sign the transaction

            // 9. Collect all of the required signatures from other Corda nodes using the CollectSignaturesFlow

            /* 10. Return the output of the FinalityFlow which sends the transaction to the notary for verification
             *     and the causes it to be persisted to the vault of appropriate nodes.
             */

            // temporary return null Needs to be return a transaction built by the transaction builder.
            return null;
        }
    }


    /**
     * This is the flow which signs IOU settlements.
     * The signing is handled by the [SignTransactionFlow].
     */
    @InitiatedBy(IOUTransferFlow.InitiatorFlow.class)
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
                    requireThat(require -> {
                        return null;
                    });
                    // Once the transaction has verified, initialize txWeJustSignedID variable.
                }
            }

            // Create a sign transaction flow

            // Run the sign transaction flow to sign the transaction

            // Run the ReceiveFinalityFlow to finalize the transaction and persist it to the vault.

            return null;
        }

    }

}