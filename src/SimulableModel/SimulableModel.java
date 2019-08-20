package SimulableModel;

import Model.*;

public class SimulableModel {

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
}
