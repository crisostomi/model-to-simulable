package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import SimulableModel.*;
import Model.*;

import java.util.HashMap;
import java.util.Map;


public abstract class ModelicaSimulableModel extends SimulableModel {

    public ModelicaSimulableModel(Model model) throws PreconditionsException{
        super(model);
        for (LinkTypeComprises linkModelBioEntity: model.getLinkComprisesSet()){
            BiologicalEntity bioEntity = linkModelBioEntity.getBiologicalEntity();
            if (bioEntity instanceof Compartment){
                Compartment comp = (Compartment) bioEntity;
                for (LinkTypeReactionCompartment linkReacComp: comp.getLinkReactionCompartmentSet()){
                    Reaction reaction = linkReacComp.getReaction();
                    ModelicaSimulableReaction simulableReaction = new ModelicaSimulableMassActionReaction(reaction);
                    LinkSimulableReactionComprises.insertLink(this, simulableReaction);
                }
            }
        }
    }


    @Override
    public Map<String, Module> getModules() {
        HashMap<String, ModelicaCode> map = new HashMap<>();
        StringBuilder reactionCode = new StringBuilder();

        for (LinkTypeComprises link: this.getModelInstantiate().getLinkComprisesSet()){
            BiologicalEntity bioEntity = link.getBiologicalEntity();
            if (bioEntity instanceof Compartment){
                map.put(bioEntity.getId(),getModuleCode((Compartment)bioEntity));
            }
        }
        map.put("Reactions",getReactionsCode());
    }

    public ModelicaCode getReactionsCode() {
        StringBuilder reacDeclarations = new StringBuilder();
        StringBuilder reacEquation = new StringBuilder("equation\n");
        for (LinkTypeComprises link:this.getModelInstantiate().getLinkComprisesSet()){
            BiologicalEntity bioEntity = link.getBiologicalEntity();
            if (bioEntity instanceof Reaction){
                Reaction reaction = (Reaction) bioEntity;
                String r_id = reaction.getId();
                SimulableReaction simReaction = this.getSimulableReaction(r_id);

                reacDeclarations.append("Real " + r_id + "_rate;\n");
                reacDeclarations.append("parameter Real " + r_id + "_rateConstant;\n");
                String rateFormula = ((ModelicaCode) simReaction.getRateFormula()).getCode();
                reacEquation.append(r_id + "_rate = " + rateFormula);

                if (reaction.isReversible()){
                    reacDeclarations.append("parameter Real " + r_id + "_rateInvConstant;\n");
                    String rateFormula = ((ModelicaCode) simReaction.getRa()).getCode();

                    reacDeclarations.append()
                }
            }
        }
    }

    public ModelicaCode getModuleCode(Compartment comp) {return null;}

    public ModelicaCode getParameters(){
        return null;
    }
}
