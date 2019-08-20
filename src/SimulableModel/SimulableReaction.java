package SimulableModel;

import DataTypes.Constraint;
import Model.Reaction;

public abstract class SimulableReaction {

    private LinkTypeSimulableReactionComprises linkSimulableReactionComprises;

    public SimulableReaction(Reaction reaction){

    }

    public abstract Constraint getRateFormula();

    public LinkTypeSimulableReactionComprises getLinkSimulableReactionComprises() {
        return linkSimulableReactionComprises;
    }

    public void insertLinkSimulableReactionComprises(LinkSimulableReactionComprises pass, LinkTypeSimulableReactionComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableReactionComprises = l;
    }

    public void removeLinkSimulableReactionComprises(LinkSimulableReactionComprises pass)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableReactionComprises = null;
    }
}
