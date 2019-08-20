package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import SimulableModel.*;
import Model.*;

import java.util.HashMap;
import java.util.Map;


public class ModelicaSimulableModel extends SimulableModel {

    public ModelicaSimulableModel(Model model) throws PreconditionsException{
        super(model);
        for (LinkTypeComprises linkModelBioEntity: model.getLinkComprisesSet()){
            BiologicalEntity bioEntity = linkModelBioEntity.getBiologicalEntity();
            if (bioEntity instanceof Compartment){
                Compartment comp = (Compartment) bioEntity;
                for (LinkTypeReactionCompartment linkReacComp: comp.getLinkReactionCompartmentSet()){
                    Reaction reaction = linkReacComp.getReaction();
                    ModelicaSimulableReaction simulableReaction = new ModelicaSimulableMassActionReaction(reaction, this);
                    LinkSimulableReactionComprises.insertLink(this, simulableReaction);
                }
            }
        }
    }


    @Override
    public Map<String, ModelicaCode> getModules() {
        HashMap<String, ModelicaCode> map = new HashMap<>();

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
        StringBuilder reacEquation = new StringBuilder("\tequation\n");
        for (LinkTypeComprises link:this.getModelInstantiate().getLinkComprisesSet()){
            BiologicalEntity bioEntity = link.getBiologicalEntity();
            if (bioEntity instanceof Reaction){
                Reaction reaction = (Reaction) bioEntity;
                String r_id = reaction.getId();
                SimulableReaction simReaction = this.getSimulableReaction(r_id);

                reacDeclarations.append("\tReal " + r_id + "_rate;\n");
                reacDeclarations.append("\tparameter Real " + r_id + "_rateConstant;\n");
                String rateFormula = ((ModelicaCode) simReaction.getRateFormula()).getCode();
                reacEquation.append("\t\t"+r_id + "_rate = " + rateFormula + ";\n");

                if (reaction.isReversible()){
                    reacDeclarations.append("\tparameter Real " + r_id + "_rateInvConstant;\n");
                    String rateInvFormula = ((ModelicaCode) simReaction.getRateInvFormula()).getCode();
                    reacEquation.append("\t\t"+r_id + "_rateInv = " + rateInvFormula + ";\n");
                }
            }
        }


        StringBuilder code = new StringBuilder("model Reaction\n");

        code.append(reacDeclarations + "\n\n");
        code.append(reacEquation + "\n\n");
        code.append("end Reactions;\n");
        return new ModelicaCode(code.toString());
    }

    public ModelicaCode getModuleCode(Compartment comp) {
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("\tinitial equation \n");
        StringBuilder equation = new StringBuilder("\tequation \n");
        for(LinkTypeSpeciesCompartment link: comp.getLinkSpeciesCompartmentSet()){
            Species species = link.getSpecies();
            String s_id = species.getId();
            SimulableSpecies simSpecies = this.getSimulableSpecies(s_id);
            if (simSpecies != null) {
                declarations.append("\tReal "+s_id+";\n");
                declarations.append("\tparameter Real "+s_id+"_init;\n");
                String rhs = ((ModelicaCode)simSpecies.getODE_RHS()).getCode();
                if (!rhs.isEmpty()) {
                    equation.append("\t\tder("+s_id+") = "+ rhs + ";\n");
                }
                initialEquation.append("\t\t"+s_id+" = "+s_id+"_init;\n");
            }

        }
        StringBuilder code = new StringBuilder();
        code.append("model "+comp.getId()+"\n\n");
        code.append(declarations + "\n\n");
        code.append(initialEquation + "\n\n");
        code.append(equation + "\n\n");
        code.append("end " + comp.getId() + ";\n\n");
        return new ModelicaCode(code.toString());
    }

    public ModelicaCode getParameters(){
        StringBuilder parameters = new StringBuilder();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprisesSet()){
            SimulableSpecies simSpecies = link.getSimulableSpecies();
            parameters.append(simSpecies.getParameters() + "\n");
        }
        for (LinkTypeSimulableReactionComprises link: this.getLinkSimulableReactionComprisesSet()){
            SimulableReaction simReac = link.getSimulableReaction();
            parameters.append(simReac.getParameters() + "\n");
        }
        return new ModelicaCode(parameters.toString());
    }
}
