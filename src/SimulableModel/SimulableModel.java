package SimulableModel;

import Model.*;

public abstract class SimulableModel {

    public SimulableModel(Model model){
        for (LinkTypeComprises linkModelBioEntity: model.getLinkComprisesSet()){
            BiologicalEntity bioEntity = linkModelBioEntity.getBiologicalEntity();
            if (bioEntity instanceof Compartment){
                Compartment comp = (Compartment) bioEntity;
                for (LinkTypeReactionCompartment linkReacComp: comp.getLinkReactionCompartmentSet()){
                    Reaction reaction = linkReacComp.getReaction();
                    SimulableReaction simulableReaction = new SimulableReaction(reaction);
                }
            }
        }
    }

    public abstract String getModules();

    public SimulableSpecies getSimulableSpecies(String id){
        return null;
    }

    public SimulableReaction getSimulableReaction(String id){
        return null;
    }
}
