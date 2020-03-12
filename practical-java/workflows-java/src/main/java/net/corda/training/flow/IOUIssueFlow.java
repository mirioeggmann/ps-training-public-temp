package net.corda.training.flow;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.training.contract.IOUContract;
import net.corda.training.state.IOUState;

import java.util.List;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;
import static net.corda.training.contract.IOUContract.Commands.Issue;

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
public class IOUIssueFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {
        private final IOUState state;
        public InitiatorFlow(IOUState state) {
            this.state = state;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Step 1. Get a reference to the notary service on our network and our key pair.
            // Note: ongoing work to support multiple notary identities is still in progress.

            // Step 2. Create a new issue command.
            // Remember that a command is a CommandData object and a list of CompositeKeys

            // Step 3. Create a new TransactionBuilder object.

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.

            // Step 5. Verify and sign it with our KeyPair.

            // Step 6. Collect the other party's signature using the SignTransactionFlow.

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return null;
        }
    }

    /**
     * This is the flow which signs IOU issuances.
     * The signing is handled by the [SignTransactionFlow].
     */
    @InitiatedBy(IOUIssueFlow.InitiatorFlow.class)
    public static class ResponderFlow extends FlowLogic<SignedTransaction> {

        private final FlowSession flowSession;
        private SecureHash txWeJustSigned;

        public ResponderFlow(FlowSession flowSession){
            this.flowSession = flowSession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            class SignTxFlow extends SignTransactionFlow {

                private SignTxFlow(FlowSession flowSession, ProgressTracker progressTracker) {
                    super(flowSession, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    // Put in any checks that you might have into a requireThat method
                    requireThat(req -> {
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