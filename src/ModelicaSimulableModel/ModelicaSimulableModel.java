package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import SimulableModel.*;
import Model.*;

import java.util.*;


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
                Compartment comp = (Compartment) bioEntity;
                String fileName = comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
                map.put(fileName,getModuleCode(comp));
            }
        }
        map.put("Reactions", getReactionsCode());
        map.put("System", getLinkingModule());
        return map;
    }

    private ModelicaCode getReactionsCode() {
        StringBuilder reacParameters = new StringBuilder();
        StringBuilder reacDeclarations = new StringBuilder();
        StringBuilder reacEquation = new StringBuilder("\tequation\n");
        for (LinkTypeComprises link:this.getModelInstantiate().getLinkComprisesSet()){
            BiologicalEntity bioEntity = link.getBiologicalEntity();
            if (bioEntity instanceof Reaction){
                Reaction reaction = (Reaction) bioEntity;
                String r_id = reaction.getId();
                SimulableReaction simReaction = this.getSimulableReaction(r_id);

                reacDeclarations.append("\tReal " + r_id + "_rate;\n");
                reacParameters.append("\tparameter Real " + r_id + "_rateConstant;\n");
                String rateFormula = ((ModelicaCode) simReaction.getRateFormula()).getCode();
                reacEquation.append("\t\t"+r_id + "_rate = " + rateFormula + ";\n");

                for (LinkTypeReactant linkReactant: reaction.getReactants()){
                    Species species = linkReactant.getSpecies();
                    reacDeclarations.append("\tReal "+species.getId()+";\n");
                }

                if (reaction.isReversible()){
                    reacParameters.append("\tparameter Real " + r_id + "_rateInvConstant;\n");
                    String rateInvFormula = ((ModelicaCode) simReaction.getRateInvFormula()).getCode();
                    reacEquation.append("\t\t"+r_id + "_rateInv = " + rateInvFormula + ";\n");

                    for (LinkTypeProduct linkProduct: reaction.getProducts()){
                        Species species = linkProduct.getSpecies();
                        reacDeclarations.append("\tReal "+species.getId()+";\n");
                    }
                }
            }
        }


        StringBuilder code = new StringBuilder("model Reactions\n");
        code.append(reacParameters + "\n");
        code.append(reacDeclarations + "\n\n");
        code.append(reacEquation + "\n\n");
        code.append("end Reactions;\n");
        return new ModelicaCode(code.toString());
    }

    private ModelicaCode getModuleCode(Compartment comp) {

        StringBuilder parameters = new StringBuilder();
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("\tinitial equation \n");
        StringBuilder equation = new StringBuilder("\tequation \n");

        Set<Reaction> reactionsInvolvedInComp = new HashSet<>();

        for(LinkTypeSpeciesCompartment link: comp.getLinkSpeciesCompartmentSet()){
            Species species = link.getSpecies();
            String s_id = species.getId();
            SimulableSpecies simSpecies = this.getSimulableSpecies(s_id);
            if (simSpecies != null) {

                Set<Reaction> reactions = simSpecies.getInvolvedReactions();
                reactionsInvolvedInComp.addAll(reactions);


                declarations.append("\tReal "+s_id+";\n");
                parameters.append("\tparameter Real "+s_id+"_init;\n");
                String rhs = ((ModelicaCode)simSpecies.getODE_RHS()).getCode();
                if (rhs.isEmpty()) {
                    rhs = "0";
                }
                equation.append("\t\tder("+s_id+") = "+ rhs + ";\n");
                initialEquation.append("\t\t"+s_id+" = "+s_id+"_init;\n");
            }
        }

        for (Reaction reaction: reactionsInvolvedInComp) {
            declarations.append("\tReal "+reaction.getId()+"_rate;\n");
        }

        StringBuilder code = new StringBuilder();
        String modelName = comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
        code.append("model "+modelName+"\n\n");
        code.append(parameters + "\n");
        code.append(declarations + "\n\n");
        code.append(initialEquation + "\n\n");
        code.append(equation + "\n\n");
        code.append("end " + modelName + ";\n\n");
        return new ModelicaCode(code.toString());
    }

    private ModelicaCode getLinkingModule() {
        StringBuilder code = new StringBuilder("model System\n");

        StringBuilder declarations = new StringBuilder();
        StringBuilder equations = new StringBuilder("\tequation\n");
        declarations.append("\tReactions reactions;\n");

        for (LinkTypeComprises link: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = link.getBiologicalEntity();
            if (be instanceof Compartment) {
                Compartment comp = (Compartment) be;
                Set<Reaction> reactionsInvolvedInComp = new HashSet<>();
                String className = comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
                declarations.append("\t" + className + " " + comp.getId() + ";\n");

                for (LinkTypeSpeciesCompartment linkSpecies: comp.getLinkSpeciesCompartmentSet()) {
                    Species species = linkSpecies.getSpecies();
                    String s_id = species.getId();
                    SimulableSpecies ss = this.getSimulableSpecies(s_id);

                    if (ss != null) {
                        Set<Reaction> reactions = ss.getInvolvedReactions();
                        reactionsInvolvedInComp.addAll(reactions);
                    }
                }

                for (Reaction r: reactionsInvolvedInComp) {
                    equations.append(
                            "\t\t"+
                            comp.getId() + "." + r.getId() + "_rate" +
                            " = " +
                            "reactions" + "." + r.getId() + "_rate" +
                            ";\n"
                    );
                }
            }
        }

        Set<Species> speciesInvolved = new HashSet<>();

        for (LinkTypeComprises l: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();
            if (be instanceof Reaction) {
                Reaction reaction = (Reaction) be;
                String r_id = reaction.getId();

                for (LinkTypeReactant linkReactant: reaction.getReactants()) {
                    Species s = linkReactant.getSpecies();
                    speciesInvolved.add(s);
                }

                if (reaction.isReversible()) {
                    for (LinkTypeProduct linkProduct: reaction.getProducts()) {
                        Species s = linkProduct.getSpecies();
                        speciesInvolved.add(s);
                    }
                }
            }
        }

        for (Species s: speciesInvolved) {
            Compartment comp = s.getLinkSpeciesCompartment().getCompartment();
            equations.append(
                    "\t\t" +
                    comp.getId() + "." + s.getId() +
                    " = " +
                    "reactions" + "." + s.getId() +
                    ";\n"
            );
        }

        code.append(declarations + "\n\n");
        code.append(equations + "\n\n");
        code.append("end System;\n");
        return new ModelicaCode(code.toString());
    }

    public List<Parameter> getParameters(){
        List<Parameter> params = new ArrayList<>();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprises()){
            SimulableSpecies simSpecies = link.getSimulableSpecies();
            params.add(simSpecies.getParameters());
        }
        for (LinkTypeSimulableReactionComprises link: this.getLinkSimulableReactionComprises()){
            SimulableReaction simReac = link.getSimulableReaction();
            params.add(simReac.getParameters());
        }
        return params;
    }
}
