package SimulableModel;

import Model.*;

import java.util.HashSet;
import java.util.Set;

public abstract class SimulableModel<T extends SimulableReaction> {

    private final Model modelInstantiate;
    private Set<LinkTypeSimulableReactionComprises> linkSimulableReactionComprisesSet = new HashSet<LinkTypeSimulableReactionComprises>();
    private Set<LinkTypeSimulableSpeciesComprises> linkSimulableSpeciesComprisesSet = new HashSet<LinkTypeSimulableSpeciesComprises>();


    public SimulableModel(Model model){
        this.modelInstantiate = model;
    }

    public abstract Set<Module> getModules();

    public void insertLinkSimulableReactionComprises(LinkSimulableReactionComprises pass, LinkTypeSimulableReactionComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableReactionComprisesSet.add(l);
    }

    public void removeLinkSimulableReactionComprises(LinkSimulableReactionComprises pass, LinkTypeSimulableReactionComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableReactionComprisesSet.remove(l);
    }

    public void insertLinkSimulableSpeciesComprises(LinkSimulableSpeciesComprises pass, LinkTypeSimulableSpeciesComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableSpeciesComprisesSet.add(l);
    }

    public void removeLinkSimulableSpeciesComprises(LinkSimulableSpeciesComprises pass, LinkTypeSimulableSpeciesComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableSpeciesComprisesSet.remove(l);
    }

    public Set<LinkTypeSimulableReactionComprises> getLinkSimulableReactionComprisesSet() {
        return linkSimulableReactionComprisesSet;
    }

    public Set<LinkTypeSimulableSpeciesComprises> getLinkSimulableSpeciesComprisesSet() {
        return linkSimulableSpeciesComprisesSet;
    }

    public SimulableSpecies getSimulableSpecies(String id){
        return null;
    }

    public SimulableReaction getSimulableReaction(String id){
        return null;
    }
}
