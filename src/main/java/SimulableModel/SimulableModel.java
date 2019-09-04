package SimulableModel;

import DataTypes.PreconditionsException;
import Model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import DataTypes.Module;
import SimulableModel.Link.LinkSimulableReactionComprises;
import SimulableModel.Link.LinkSimulableSpeciesComprises;
import SimulableModel.LinkType.LinkTypeSimulableReactionComprises;
import SimulableModel.LinkType.LinkTypeSimulableSpeciesComprises;

public abstract class SimulableModel {

    private final Model modelInstantiate;
    private Map<String, LinkTypeSimulableReactionComprises> linkSimulableReactionComprisesMap = new HashMap<String, LinkTypeSimulableReactionComprises>();
    private Map<String, LinkTypeSimulableSpeciesComprises> linkSimulableSpeciesComprisesMap = new HashMap<String, LinkTypeSimulableSpeciesComprises>();


    public SimulableModel(Model model) {
        this.modelInstantiate = model;
    }

    public abstract Map<String, ? extends Module> getModules();

    public void insertLinkSimulableReactionComprises(LinkSimulableReactionComprises pass, LinkTypeSimulableReactionComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableReactionComprisesMap.put(l.getSimulableReaction().getReactionInstantiate().getId(), l);
    }

    public void removeLinkSimulableReactionComprises(LinkSimulableReactionComprises pass, LinkTypeSimulableReactionComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableReactionComprisesMap.remove(l.getSimulableReaction().getReactionInstantiate().getId());
    }

    public void insertLinkSimulableSpeciesComprises(LinkSimulableSpeciesComprises pass, LinkTypeSimulableSpeciesComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableSpeciesComprisesMap.put(l.getSimulableSpecies().getSpeciesInstantiate().getId(), l);
    }

    public void removeLinkSimulableSpeciesComprises(LinkSimulableSpeciesComprises pass, LinkTypeSimulableSpeciesComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableSpeciesComprisesMap.remove(l.getSimulableSpecies().getSpeciesInstantiate().getId());
    }

    public Model getModelInstantiate() {
        return modelInstantiate;
    }

    public Collection<LinkTypeSimulableReactionComprises> getLinkSimulableReactionComprises() {
        return linkSimulableReactionComprisesMap.values();
    }

    public Collection<LinkTypeSimulableSpeciesComprises> getLinkSimulableSpeciesComprises() {
        return linkSimulableSpeciesComprisesMap.values();
    }

    public SimulableSpecies getSimulableSpecies(String id) {
        LinkTypeSimulableSpeciesComprises link = linkSimulableSpeciesComprisesMap.get(id);
        if (link != null) {
            return link.getSimulableSpecies();
        }
        return null;
    }

    public SimulableReaction getSimulableReaction(String id) {
        LinkTypeSimulableReactionComprises link = linkSimulableReactionComprisesMap.get(id);
        if (link != null) {
            return link.getSimulableReaction();
        }
        return null;
    }
}