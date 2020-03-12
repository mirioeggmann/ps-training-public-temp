package net.corda.training.state;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.training.contract.IOUContract;

import java.util.Currency;
import java.util.List;

/**
 * This is where you'll add the definition of your state object. Look at the unit tests in [IOUStateTests] for
 * instructions on how to complete the [IOUState] class.
 *
 * Remove the data property before starting the [IOUState] tasks.
 */
@BelongsToContract(IOUContract.class)
public class IOUState implements ContractState {

    public final String data;

    // Private constructor used only for copying a State object
    // the data attribute is a temporary variable, remove this value before starting the IOUState tasks.
    @ConstructorForDeserialization
    private IOUState(String data){
        this.data = data;
    }

    public IOUState() {
        this("");
    }

    /**
     *  This method will return a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  lender or the borrower.
     *  TODO: This will be filled out in Task 5 and 3
     */
    // @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of();
    }

    /**
     * Helper methods for when building transactions for settling and transferring IOUs.
     * - [pay] adds an amount to the paid property. It does no validation.
     * - [withNewLender] creates a copy of the current state with a newly specified lender. For use when transferring.
     * - [copy] creates a copy of the state using the internal copy constructor ensuring the LinearId is preserved.
     */
    public IOUState pay(Amount<Currency> amountToPay) {
        return this;
    }
}