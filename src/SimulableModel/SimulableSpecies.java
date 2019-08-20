package SimulableModel;

import DataTypes.Expression;
import Model.Species;

public abstract class SimulableSpecies {

    private LinkTypeSimulableSpeciesComprises linkSimulableSpeciesComprises;
    private final Species speciesInstantiate;
    
    public SimulableSpecies(Species species){
        this.speciesInstantiate = species;
    }

    public abstract Expression getODE_RHS();

    public Species getSpeciesInstantiate() {
        return speciesInstantiate;
    }

    public LinkTypeSimulableSpeciesComprises getLinkSimulableSpeciesComprises() {
        return linkSimulableSpeciesComprises;
    }

    public void insertLinkSimulableSpeciesComprises(LinkSimulableSpeciesComprises pass, LinkTypeSimulableSpeciesComprises l)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableSpeciesComprises = l;
    }

    public void removeLinkSimulableSpeciesComprises(LinkSimulableSpeciesComprises pass)
            throws PreconditionsException {
        if (pass == null)
            throw new PreconditionsException(
                    "It is necessary to show an instance of LinkComprises to invoke this method");
        linkSimulableSpeciesComprises = null;
    }
}
