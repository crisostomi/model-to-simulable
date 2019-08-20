package ModelicaSimulableModel;

import Model.Model;
import SimulableModel.SimulableModel;
import Model.*;


public abstract class ModelicaSimulableModel extends SimulableModel {

    public ModelicaSimulableModel(Model model) {
        super(model);
        for (LinkTypeComprises linkModelBioEntity: model.getLinkComprisesSet()){
            BiologicalEntity bioEntity = linkModelBioEntity.getBiologicalEntity();
            if (bioEntity instanceof Compartment){
                Compartment comp = (Compartment) bioEntity;
                for (LinkTypeReactionCompartment linkReacComp: comp.getLinkReactionCompartmentSet()){
                    Reaction reaction = linkReacComp.getReaction();
                    ModelicaSimulableReaction simulableReaction = new ModelicaSimulableMassActionReaction(reaction);
                }
            }
        }
    }


    @Override
    public String getModules() {
        return null;
    }

    public abstract String getReactionsCode();

    public abstract String getModuleCode();

    public String getParameters(){
        return null;
    }
}
