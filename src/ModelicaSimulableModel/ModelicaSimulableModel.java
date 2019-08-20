package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import Model.Model;
import SimulableModel.SimulableModel;
import Model.*;

import java.util.Set;


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
    public Set<ModelicaCode> getModules() {
        return null;
    }

    public ModelicaCode getReactionsCode() {return null;}

    public ModelicaCode getModuleCode() {return null;}

    public ModelicaCode getParameters(){
        return null;
    }
}
