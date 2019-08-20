package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import SimulableModel.*;
import Model.*;

import java.util.HashMap;
import java.util.Map;
import DataTypes.Module;


public class ModelicaSimulableModel extends SimulableModel {

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
    public Map<String, ModelicaCode> getModules() {
        HashMap<String, ModelicaCode> map = new HashMap<>();
        StringBuilder reactionCode = new StringBuilder();

        for (LinkTypeComprises link: this.getModelInstantiate().getLinkComprisesSet()){
            BiologicalEntity bioEntity = link.getBiologicalEntity();
            if (bioEntity instanceof Compartment){
                map.put(bioEntity.getId(),getModuleCode((Compartment)bioEntity));
            }
        }
        map.put("Reactions",getReactionsCode());
        return map;
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
                    String rateInvFormula = ((ModelicaCode) simReaction.getRateInvFormula()).getCode();
                    reacEquation.append(r_id + "_rateInv = " + rateInvFormula);
                }
            }
        }
        String result = reacDeclarations.toString() + "\n\n\n" + reacEquation.toString();
        return new ModelicaCode(result);
    }

    public ModelicaCode getModuleCode(Compartment comp) {
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("initial equation \n");
        StringBuilder equation = new StringBuilder("equation \n");
        for(LinkTypeSpeciesCompartment link: comp.getLinkSpeciesCompartmentSet()){
            Species species = link.getSpecies();
            String s_id = species.getId();
            SimulableSpecies simSpecies = this.getSimulableSpecies(s_id);
            if (simSpecies != null) {
                declarations.append("Real "+s_id+";\n");
                equation.append("der(+"+s_id+") = "+simSpecies.getODE_RHS());
                initialEquation.append(s_id+" = "+s_id+"_init;\n");
            }

        }
        StringBuilder code = new StringBuilder();
        code.append("model "+comp.getId()+";\n");
        code.append(initialEquation);
        code.append(equation);
        return new ModelicaCode(code.toString());
    }

    public ModelicaCode getParameters(){
        StringBuilder parameters = new StringBuilder();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprisesSet()){
            SimulableSpecies simSpecies = link.getSimulableSpecies();
            parameters.append(simSpecies.getParameters());
        }
        parameters.append("\n");
        for (LinkTypeSimulableReactionComprises link: this.getLinkSimulableReactionComprisesSet()){
            SimulableReaction simReac = link.getSimulableReaction();
            parameters.append(simReac.getParameters());
        }
        return new ModelicaCode(parameters.toString());
    }
}
