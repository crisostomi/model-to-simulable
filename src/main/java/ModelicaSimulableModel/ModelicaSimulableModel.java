package ModelicaSimulableModel;

import DataTypes.DefinedParameter;
import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import DataTypes.UndefinedParameter;
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

                    if (!reaction.getReactants().isEmpty() || !reaction.getProducts().isEmpty()) {
                        ModelicaSimulableReaction simulableReaction = new MassActionModelicaSimulableReaction(reaction, this);
                        LinkSimulableReactionComprises.insertLink(this, simulableReaction);
                    }
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
                String fileName = this.getModuleName(comp);
                map.put(fileName,getModuleCode(comp));
            }
        }
        map.put("Parameters", getParamersCode());
        map.put("Reactions", getReactionsCode());
        map.put("System", getLinkingModule());
        return map;
    }

    private ModelicaCode getReactionsCode() {
        StringBuilder parameters = new StringBuilder();
        StringBuilder reacDeclarations = new StringBuilder();
        StringBuilder speciesDeclarations = new StringBuilder();
        StringBuilder equation = new StringBuilder("\tequation\n");

        for (LinkTypeComprises link:this.getModelInstantiate().getLinkComprisesSet()){
            BiologicalEntity bioEntity = link.getBiologicalEntity();
            if (bioEntity instanceof Reaction){
                Reaction reaction = (Reaction) bioEntity;
                String r_id = reaction.getId();
                ModelicaSimulableReaction simReaction =
                        (ModelicaSimulableReaction) this.getSimulableReaction(r_id);

                String reactionRateVariable = simReaction.getRateVariableName();
                String line = "\tReal " + reactionRateVariable;
                if (reaction.getName() != null) {
                    line = line + " \"" + reaction.getName() + "\"";
                }
                reacDeclarations.append(line + ";\n");

                String reactionRateConstantVariable = simReaction.getRateConstantVariableName();
                parameters.append("\tReal " + reactionRateConstantVariable + ";\n");

                String rateFormula = simReaction.getRateFormula().getCode();
                equation.append("\t\t"+ reactionRateConstantVariable + " = " + rateFormula + ";\n");

                for (LinkTypeReactant linkReactant: reaction.getReactants()){
                    Species species = linkReactant.getSpecies();

                    String speciesVariable =
                            ((ModelicaSimulableSpecies)this.getSimulableSpecies(species.getId())).getVariableName();

                    line = "\tReal "+speciesVariable;
                    if (species.getName() != null) {
                        line = line + " \"" + species.getName() + "\"";
                    }
                    speciesDeclarations.append(line+";\n");
                }

                if (reaction.isReversible()){
                    String reactionRateInvConstantVariable = simReaction.getRateInvConstantVariableName();
                    parameters.append("\tparameter Real " + reactionRateInvConstantVariable + ";\n");

                    String reactionRateInvVariable = simReaction.getRateInvVariableName();
                    String rateInvFormula = simReaction.getRateInvFormula().getCode();

                    equation.append("\t\t"+ reactionRateInvVariable + " = " + rateInvFormula + ";\n");

                    for (LinkTypeProduct linkProduct: reaction.getProducts()){
                        Species species = linkProduct.getSpecies();

                        String speciesVariable =
                                ((ModelicaSimulableSpecies)this.getSimulableSpecies(species.getId())).getVariableName();

                        line = "\tReal "+speciesVariable;
                        if (species.getName() != null) {
                            line = line + " \"" + species.getName() + "\"";
                        }
                        speciesDeclarations.append(line+";\n");
                    }
                }
            }
        }


        StringBuilder code = new StringBuilder("model Reactions\n");
        code.append(parameters + "\n");
        code.append(speciesDeclarations + "\n");
        code.append(reacDeclarations + "\n");
        code.append(equation + "\n\n");
        code.append("end Reactions;\n");
        return new ModelicaCode(code.toString());
    }

    private ModelicaCode getModuleCode(Compartment comp) {

        StringBuilder parameters = new StringBuilder();
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("\tinitial equation \n");
        StringBuilder equation = new StringBuilder("\tequation \n");

        Set<ModelicaSimulableReaction> reactionsInvolvedInComp = new HashSet<>();

        for(LinkTypeSpeciesCompartment link: comp.getLinkSpeciesCompartmentSet()){
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies simSpecies = (ModelicaSimulableSpecies)this.getSimulableSpecies(s_id);
            if (simSpecies != null) {

                String speciesVariable = simSpecies.getVariableName();
                String line = "\tReal " + speciesVariable;
                if (species.getName() != null){
                    line = line + " \"" + species.getName() + "\"";
                }

                Set<ModelicaSimulableReaction> reactions = simSpecies.getInvolvedReactions();
                reactionsInvolvedInComp.addAll(reactions);

                declarations.append(line + ";\n");
                String speciesIAVariable = simSpecies.getInitialAmountVariableName();
                parameters.append("\tReal "+speciesIAVariable+";\n");

                String rhs = simSpecies.getODE_RHS().getCode();
                if (rhs.isEmpty()) {
                    rhs = "0";
                }
                equation.append("\t\tder("+speciesVariable+") = "+ rhs + ";\n");
                initialEquation.append("\t\t"+speciesVariable+" = "+speciesIAVariable+";\n");
            }
        }

        for (ModelicaSimulableReaction reaction: reactionsInvolvedInComp) {
            Reaction r = reaction.getReactionInstantiate();
            String reactionRateVariable = reaction.getRateVariableName();
            String line = "\tReal " + reactionRateVariable;
            if (r.getName() != null) {
                line = line + " \"" + r.getName() + "\"";
            }

            declarations.append(line+";\n");
        }

        StringBuilder code = new StringBuilder();
        String modelName = this.getModuleName(comp);
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
        declarations.append("\tParameters parameters;\n");

        // code for linking species variables
        for (LinkTypeComprises link: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = link.getBiologicalEntity();
            if (be instanceof Compartment) {
                Compartment comp = (Compartment) be;
                Set<ModelicaSimulableReaction> reactionsInvolvedInComp = new HashSet<>();
                String className = comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
                declarations.append("\t" + className + " " + comp.getId() + ";\n");

                for (LinkTypeSpeciesCompartment linkSpecies: comp.getLinkSpeciesCompartmentSet()) {
                    Species species = linkSpecies.getSpecies();
                    String s_id = species.getId();
                    ModelicaSimulableSpecies ss =
                            (ModelicaSimulableSpecies)this.getSimulableSpecies(s_id);

                    if (ss != null) {
                        Set<ModelicaSimulableReaction> reactions = ss.getInvolvedReactions();
                        reactionsInvolvedInComp.addAll(reactions);
                    }
                }

                for (ModelicaSimulableReaction r: reactionsInvolvedInComp) {
                    equations.append(
                            "\t\t"+
                            comp.getId() + "." + r.getRateVariableName() +
                            " = " +
                            "reactions" + "." + r.getRateVariableName() +
                            ";\n"
                    );
                }
            }
        }

        // code for linking reaction variable
        Set<ModelicaSimulableSpecies> speciesInvolved = new HashSet<>();
        for (LinkTypeComprises l: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();
            if (be instanceof Reaction) {
                Reaction reaction = (Reaction) be;

                for (LinkTypeReactant linkReactant: reaction.getReactants()) {
                    Species s = linkReactant.getSpecies();
                    ModelicaSimulableSpecies ss =
                            (ModelicaSimulableSpecies) this.getSimulableSpecies(s.getId());

                    speciesInvolved.add(ss);
                }

                if (reaction.isReversible()) {
                    for (LinkTypeProduct linkProduct: reaction.getProducts()) {
                        Species s = linkProduct.getSpecies();
                        ModelicaSimulableSpecies ss =
                                (ModelicaSimulableSpecies) this.getSimulableSpecies(s.getId());

                        speciesInvolved.add(ss);
                    }
                }
            }
        }

        for (ModelicaSimulableSpecies s: speciesInvolved) {
            Compartment comp = s.getSpeciesInstantiate().getLinkSpeciesCompartment().getCompartment();
            equations.append(
                    "\t\t" +
                    comp.getId() + "." + s.getVariableName() +
                    " = " +
                    "reactions" + "." + s.getVariableName() +
                    ";\n"
            );
        }

        // code for linking parameters variables
        for (LinkTypeComprises l: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();
            if (be instanceof Compartment) {
                Compartment comp = (Compartment) be;
                for (LinkTypeSpeciesCompartment link: comp.getLinkSpeciesCompartmentSet()) {
                    Species species = link.getSpecies();
                    ModelicaSimulableSpecies simSpecies =
                            (ModelicaSimulableSpecies) this.getSimulableSpecies(species.getId());

                    if (simSpecies != null) {
                        String speciesIAVariable = simSpecies.getInitialAmountVariableName();
                        equations.append("\t\t"+
                                comp.getId() + "." + speciesIAVariable +
                                        " = " +
                                        "parameters" + "." + speciesIAVariable +
                                        ";\n"
                        );
                    }
                }
            }
        }

        for (LinkTypeComprises l: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();
            if (be instanceof Reaction) {
                Reaction reaction = (Reaction) be;
                ModelicaSimulableReaction simReaction =
                        (ModelicaSimulableReaction) this.getSimulableReaction(reaction.getId());

                if (simReaction != null) {
                    String reactionRateConsantVariable = simReaction.getRateConstantVariableName();
                    equations.append("\t\t"+
                            "reactions" + "." + reactionRateConsantVariable +
                                    " = " +
                                    "parameters" + "." + reactionRateConsantVariable +
                                    ";\n"
                    );
                    if (reaction.isReversible()) {
                        String reactionRateInvConstantVariable = simReaction.getRateInvConstantVariableName();
                        equations.append("\t\t"+
                                "reactions" + "." + reactionRateInvConstantVariable +
                                        " = " +
                                        "parameters" + "." + reactionRateInvConstantVariable +
                                        ";\n"
                        );
                    }
                }
            }
        }

        code.append(declarations + "\n\n");
        code.append(equations + "\n\n");
        code.append("end System;\n");
        return new ModelicaCode(code.toString());
    }

    private ModelicaCode getParamersCode() {
        StringBuilder declarations = new StringBuilder();
        Model m = this.getModelInstantiate();

        for (LinkTypeComprises l: m.getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();
            if (be instanceof Species) {
                Species s = (Species) be;
                ModelicaSimulableSpecies species =
                        (ModelicaSimulableSpecies) this.getSimulableSpecies(s.getId());

                String speciesIAVariable = species.getInitialAmountVariableName();
                String line = "parameter Real " + speciesIAVariable;
                Parameter speciesParameter = species.getParameter();
                if (speciesParameter instanceof DefinedParameter) {
                    line = line + " = " + ((DefinedParameter) speciesParameter).getValue();
                }
                declarations.append("\t"+line + ";\n");
            } else if (be instanceof Reaction) {
                Reaction r = (Reaction) be;
                ModelicaSimulableReaction reaction =
                        (ModelicaSimulableReaction) this.getSimulableReaction(r.getId());

                String rateConsantVariable = reaction.getRateConstantVariableName();
                String line = "parameter Real " + rateConsantVariable;
                Parameter rateParameter = reaction.getParameter();
                if (rateParameter instanceof DefinedParameter) {
                    line = line + " = " + ((DefinedParameter) rateParameter).getValue();
                }
                declarations.append("\t"+line + ";\n");

                if (r.isReversible()) {
                    String rateInvConstantVariable = reaction.getRateInvVariableName();
                    line = "parameter Real " + rateInvConstantVariable;
                    Parameter rateInvParameter = reaction.getInvParameter();
                    if (rateInvParameter instanceof DefinedParameter) {
                        line = line + " = " + ((DefinedParameter) rateInvParameter).getValue();
                    }

                    declarations.append("\t"+line + ";\n");
                }
            }
        }

        StringBuilder code = new StringBuilder("model Parameters\n");
        code.append(declarations + "\n");
        code.append("end Parameters;\n");

        return new ModelicaCode(code.toString());
    }

    public List<UndefinedParameter> getUndefinedParameters(){
        List<UndefinedParameter> params = new ArrayList<>();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprises()){
            ModelicaSimulableSpecies simSpecies =
                    (ModelicaSimulableSpecies) link.getSimulableSpecies();
            Parameter speciesParam = simSpecies.getParameter();
            if (speciesParam instanceof UndefinedParameter) {
                params.add((UndefinedParameter) speciesParam);
            }
        }
        for (LinkTypeSimulableReactionComprises link: this.getLinkSimulableReactionComprises()){
            ModelicaSimulableReaction simReac =
                    (ModelicaSimulableReaction)link.getSimulableReaction();
            Parameter reactionParam = simReac.getParameter();
            if (reactionParam instanceof UndefinedParameter) {
                params.add((UndefinedParameter) reactionParam);
            }
        }
        return params;
    }

    private String getModuleName(Compartment comp) {
        return comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
    }
}
