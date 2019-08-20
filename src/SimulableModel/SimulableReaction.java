package SimulableModel;

import DataTypes.Constraint;
import Model.Reaction;

public abstract class SimulableReaction {

    private final Reaction reactionInstantiate;
    private LinkTypeSimulableReactionComprises linkSimulableReactionComprises;

    public SimulableReaction(Reaction reaction){
        this.reactionInstantiate = reaction;
    }

    public abstract Constraint getRateFormula();

    public abstract Constraint getRateInvFormula();

    public Reaction getReactionInstantiate() {
        return reactionInstantiate;
    }

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
