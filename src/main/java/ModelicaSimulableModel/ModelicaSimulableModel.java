package ModelicaSimulableModel;

import java.util.*;
import DataTypes.*;
import SimulableModel.*;
import Model.*;
import SimulableModel.Link.*;
import SimulableModel.LinkType.*;


public class ModelicaSimulableModel extends SimulableModel {

    public ModelicaSimulableModel(Model model) throws PreconditionsException {
        super(model);
        for (Compartment compartment: model.getCompartments()){

            for (Reaction reaction: compartment.getReactions()){

                if (!reaction.getReactants().isEmpty() || !reaction.getProducts().isEmpty()) {
                    ModelicaSimulableReaction simulableReaction;
                    if (reaction.isComplex()){
                        simulableReaction = new MichaelisMentenModelicaSimulableReaction(reaction, this);
                    }
                    else {
                        simulableReaction = new MassActionModelicaSimulableReaction(reaction, this);
                    }
                    LinkSimulableReactionComprises.insertLink(this, simulableReaction);
                }
            }
        }
    }

    @Override
    public Map<String, ModelicaCode> getModules() {
        HashMap<String, ModelicaCode> map = new HashMap<>();

        for (Compartment compartment: this.getModelInstantiate().getCompartments()){
            String fileName = this.getModuleName(compartment);
            map.put(fileName,getModuleCode(compartment));
        }

        map.put("Parameters", getParametersModule());
        map.put("Monitor", getMonitorCode());
        map.put("Reactions", getReactionsCode());
        map.put("System", getLinkingModule());

        return map;
    }

    public Map<String, Double> getProteinConstraints() {
        Map<String, Double> constraints = new HashMap<>();
        for (ModelicaSimulableSpecies simulableSpecies: this.getSimulableSpeciesSet()) {
            Species species = simulableSpecies.getSpeciesInstantiate();
            if (species instanceof Protein) {
                Protein p = (Protein) species;
                if (p.getAbundance() != null) {
                    constraints.put(simulableSpecies.getAverageVariableName(), p.getAbundance());
                }
            }
        }

        return constraints;
    }

    public Set<UndefinedModelicaParameter> getUndefinedParameters(){
        Set<UndefinedModelicaParameter> params = new HashSet<>();
        params.addAll(getUndefinedSpeciesParameters());
        params.addAll(getUndefinedReactionsParameters());
        return params;
    }

    private Set<UndefinedModelicaParameter> getUndefinedSpeciesParameters() {
        Set<UndefinedModelicaParameter> params = new HashSet<>();
        for (ModelicaSimulableSpecies simulableSpecies: this.getSimulableSpeciesSet()) {
            ModelicaParameter speciesParam = simulableSpecies.getParameter();
            if (speciesParam instanceof UndefinedModelicaParameter) {
                params.add((UndefinedModelicaParameter) speciesParam);
            }
        }
        return params;
    }

    private Set<UndefinedModelicaParameter> getUndefinedReactionsParameters() {
        Set<UndefinedModelicaParameter> params = new HashSet<>();

        for (ModelicaSimulableReaction simulableReaction: this.getSimulableReactionSet()){
            params.addAll(simulableReaction.getUndefinedParameters());
        }
        return params;
    }

    private ModelicaCode getReactionsCode() {
        StringBuilder parameters = new StringBuilder();
        StringBuilder reacDeclarations = new StringBuilder();
        StringBuilder equation = new StringBuilder("\tequation\n");
        Set<Species> speciesNeededForRate = new HashSet<>();

        for (Reaction reaction:this.getModelInstantiate().getReactions()){

            ModelicaSimulableReaction simReaction = (ModelicaSimulableReaction) this.getSimulableReaction(reaction.getId());

            // Real reaction_x_rate "reaction name as a comment";
            String reactionRateVariable = simReaction.getRateVariableName();
            String line = "\tReal " + reactionRateVariable;
            if (reaction.getName() != null) {
                line = line + " \"" + reaction.getName() + "\"";
            }
            reacDeclarations.append(line + ";\n");

            // reaction_x_rate = species_y * species_z ..;
            String rateFormula = simReaction.getRateFormula().getCode();
            equation.append("\t\t"+ reactionRateVariable + " = " + rateFormula + ";\n");

            // Real reaction_x_constants;
            // Real species_needed_for_rate;
            parameters.append(simReaction.getConstantsDeclarationsNeededForRate());
            speciesNeededForRate.addAll(simReaction.getSpeciesNeededForRate());

            if (reaction.isReversible()){
                if (reaction.isComplex()){
                    // complex reversible code
                    System.out.println("WARNING: complex reversible reaction found!");
                }
                else {
                    parameters.append(simReaction.getConstantsDeclarationsNeededForInverseRate());
                    speciesNeededForRate.addAll(simReaction.getSpeciesNeededForInverseRate());
                }

                String reactionRateInvVariable = simReaction.getRateInvVariableName();
                String rateInvFormula = simReaction.getRateInvFormula().getCode();
                equation.append("\t\t"+ reactionRateInvVariable + " = " + rateInvFormula + ";\n");
            }
        }

        StringBuilder code = new StringBuilder("model Reactions\n\n");
        code.append(parameters + "\n");
        code.append(declareSpeciesNeededForRate(speciesNeededForRate) + "\n");
        code.append(reacDeclarations + "\n");
        code.append(equation + "\n\n");
        code.append("end Reactions;\n");
        return new ModelicaCode(code.toString());
    }

    private StringBuilder declareSpeciesNeededForRate(Set<Species> speciesNeededForRate) {
        StringBuilder result = new StringBuilder();
        for (Species species: speciesNeededForRate){
            String speciesVariable =
                    ((ModelicaSimulableSpecies)this.getSimulableSpecies(species.getId())).getVariableName();

            String speciesDeclaration = "\tReal " + speciesVariable;
            if (species.getName() != null) {
                speciesDeclaration = speciesDeclaration + " \"" + species.getName() + "\"";
            }
            result.append(speciesDeclaration+";\n");
        }
        return result;
    }

    private ModelicaCode getModuleCode(Compartment comp) {

        StringBuilder parameters = new StringBuilder();
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("\tinitial equation \n");
        StringBuilder equation = new StringBuilder("\tequation \n");

        Set<ModelicaSimulableReaction> reactionsInvolvedInComp = new HashSet<>();

        for(Species species: comp.getSpecies()){
            String s_id = species.getId();
            ModelicaSimulableSpecies simSpecies = (ModelicaSimulableSpecies)this.getSimulableSpecies(s_id);
            if (simSpecies != null) {
                // Real species_X "X";
                String speciesVariable = simSpecies.getVariableName();
                String line = "\tReal " + speciesVariable;
                if (species.getName() != null){
                    line = line + " \"" + species.getName() + "\"";
                }
                declarations.append(line + ";\n");

                Set<ModelicaSimulableReaction> reactions = simSpecies.getInvolvedReactions();
                reactionsInvolvedInComp.addAll(reactions);

                // Real species_X_init;
                String speciesIAVariable = simSpecies.getInitialAmountVariableName();
                parameters.append("\tReal "+speciesIAVariable+";\n");

                // species_X = species_X_init
                initialEquation.append("\t\t"+speciesVariable+" = "+speciesIAVariable+";\n");

                // der(species_X) = +1 * reaction_X_rate * ...;
                String rhs = simSpecies.getODE_RHS().getCode();
                if (rhs.isEmpty()) {
                    rhs = "0";
                }
                equation.append("\t\tder("+speciesVariable+") = "+ rhs + ";\n");
            }
        }

        declarations.append("\n");
        for (ModelicaSimulableReaction simulableReaction: reactionsInvolvedInComp) {
            // Real reaction_X_rate "reaction name";
            Reaction reaction = simulableReaction.getReactionInstantiate();
            String reactionRateVariable = simulableReaction.getRateVariableName();
            String line = "\tReal " + reactionRateVariable;
            if (reaction.getName() != null) {
                line = line + " \"" + reaction.getName() + "\"";
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
        StringBuilder code = new StringBuilder("model System\n\n");

        StringBuilder declarations = new StringBuilder();
        StringBuilder equations = new StringBuilder("\tequation\n");
        declarations.append("\tReactions reactions;\n");
        declarations.append("\tParameters parameters;\n");
        declarations.append("\tMonitor monitor;\n");

        // code for linking reaction rate variables from compartment to reactions
        for (Compartment compartment: this.getModelInstantiate().getCompartments()) {
            String className = getModuleName(compartment);
            declarations.append("\t" + className + " " + compartment.getId() + ";\n");

            equations.append(getLinkingReactionsCode(compartment));
        }

        // code for linking compartment species to reaction species
        equations.append(getLinkingSpeciesCode());

        // code for linking parameters variables
        equations.append(getLinkingSpeciesParametersCode());
        equations.append(getLinkingReactionParametersCode());

        // code for linking monitor
        equations.append(getLinkingMonitorCode());

        code.append(declarations + "\n\n");
        code.append(equations + "\n\n");
        code.append("end System;\n");
        return new ModelicaCode(code.toString());
    }

    private StringBuilder getLinkingSpeciesCode() {
        StringBuilder equations = new StringBuilder();
        Set<Species> speciesInvolved = new HashSet<>();
        for (Reaction reaction: this.getModelInstantiate().getReactions()) {
            speciesInvolved.addAll(reaction.getInvolvedSpecies());
        }

        for (Species species: speciesInvolved) {
            // compartment_X.species_Y = reactions.species_Y
            ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getSimulableSpecies(species.getId());
            Compartment comp = simulableSpecies.getSpeciesInstantiate().getLinkSpeciesCompartment().getCompartment();
            equations.append(
                    "\t\t" +
                            comp.getId() + "." + simulableSpecies.getVariableName() +
                            " = " +
                            "reactions" + "." + simulableSpecies.getVariableName() +
                            ";\n"
            );
        }
        return equations;
    }

    private StringBuilder getLinkingReactionsCode(Compartment compartment) {
        StringBuilder equations = new StringBuilder();
        Set<ModelicaSimulableReaction> reactionsInvolvedInComp = new HashSet<>();

        for (Species species: compartment.getSpecies()) {
            String s_id = species.getId();
            ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies)this.getSimulableSpecies(s_id);

            if (simulableSpecies != null) {
                Set<ModelicaSimulableReaction> reactions = simulableSpecies.getInvolvedReactions();
                reactionsInvolvedInComp.addAll(reactions);
            }
        }

        for (ModelicaSimulableReaction reaction: reactionsInvolvedInComp) {
            // compartment_X.reaction_X_rate = reactions.reaction_X_rate;
            equations.append(
                    "\t\t"+
                            compartment.getId() + "." + reaction.getRateVariableName() +
                            " = " +
                            "reactions" + "." + reaction.getRateVariableName() +
                            ";\n"
            );
        }
        return equations;
    }

    private StringBuilder getLinkingSpeciesParametersCode() {
        StringBuilder equations = new StringBuilder();
        for (Compartment comp: this.getModelInstantiate().getCompartments()) {
            for (Species species: comp.getSpecies()) {
                ModelicaSimulableSpecies simSpecies =
                        (ModelicaSimulableSpecies) this.getSimulableSpecies(species.getId());

                if (simSpecies != null) {
                    // compartment_X.species_Y_init = parameters.species_Y_init;
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
        return equations;
    }

    private StringBuilder getLinkingReactionParametersCode() {
        StringBuilder equations = new StringBuilder();
        for (Reaction reaction: this.getModelInstantiate().getReactions()) {
            ModelicaSimulableReaction simReaction =
                    (ModelicaSimulableReaction) this.getSimulableReaction(reaction.getId());
            if (simReaction != null) {
                equations.append(
                        simReaction.getLinkingReactionParameterCode(
                                "reactions", "parameters"
                        ));
            }
        }

        return equations;
    }

    private StringBuilder getLinkingMonitorCode() {
        StringBuilder equations = new StringBuilder();
        equations.append(
                "\t\tmonitor" + "." + getSimulationTimeVariableName() +
                        " = " +
                        "parameters" + "." + getSimulationTimeVariableName() + ";\n"
        );

        for (Compartment compartment: this.getModelInstantiate().getCompartments()) {
            for (Species species: compartment.getSpecies()) {
                ModelicaSimulableSpecies simulableSpecies =
                        (ModelicaSimulableSpecies) getSimulableSpecies(species.getId());

                if (simulableSpecies != null) {
                    // compartment_X.species_Y = monitor.species_Y;
                    equations.append("\t\t" +
                            compartment.getId() + "." + simulableSpecies.getVariableName() +
                            " = "+
                            "monitor" + "." + simulableSpecies.getVariableName() + ";\n"
                    );
                }
            }
        }

        return equations;
    }

    private ModelicaCode getParametersModule() {
        StringBuilder declarations = new StringBuilder();
        Model m = this.getModelInstantiate();

        for (Species species: m.getSpecies()) {
            String speciesCode = getSpeciesParametersCode(species);
            declarations.append("\t"+speciesCode + ";\n");
        }

        for (Reaction reaction: m.getReactions()){
            String reactionCode = getReactionParametersCode(reaction);
            declarations.append(reactionCode);
        }

        StringBuilder code = new StringBuilder("model Parameters\n\n");
        code.append(declarations);
        code.append("\tparameter Real simulation_time;\n\n");
        code.append("end Parameters;\n");
        return new ModelicaCode(code.toString());
    }

    private String getSpeciesParametersCode(Species species){
        ModelicaSimulableSpecies simulableSpecies =
                (ModelicaSimulableSpecies) this.getSimulableSpecies(species.getId());

        String speciesIAVariable = simulableSpecies.getInitialAmountVariableName();
        String line = "parameter Real " + speciesIAVariable;
        ModelicaParameter speciesModelicaParameter = simulableSpecies.getParameter();
        if (speciesModelicaParameter instanceof DefinedModelicaParameter) {
            line = line + " = " + ((DefinedModelicaParameter) speciesModelicaParameter).getValue();
        }
        return line;
    }

    private String getReactionParametersCode(Reaction reaction) {
        ModelicaSimulableReaction simulableReaction =
                (ModelicaSimulableReaction) this.getSimulableReaction(reaction.getId());
        StringBuilder declarations = simulableReaction.getParametersDeclarations();

        return declarations.toString();
    }

    private ModelicaCode getMonitorCode() {
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("\tinitial equation\n");
        StringBuilder equation = new StringBuilder("\tequation\n");

        declarations.append("\tReal " + getSimulationTimeVariableName() + ";\n");

        for (BiologicalEntity be: this.getModelInstantiate().getBiologicalEntities()) {
            if (be instanceof Compartment) {
                Compartment comp = (Compartment) be;
                for (Species species: comp.getSpecies()) {
                    ModelicaSimulableSpecies simulableSpecies =
                            (ModelicaSimulableSpecies) this.getSimulableSpecies(species.getId());

                    if (simulableSpecies != null) {
                        declarations.append("\tReal " + simulableSpecies.getVariableName() + ";\n");
                        declarations.append("\tReal " + simulableSpecies.getAverageVariableName() + ";\n");
                        initialEquation.append("\t\t" + simulableSpecies.getAverageVariableName() + " = 0;\n");
                        equation.append(
                                "\t\tder(" + simulableSpecies.getAverageVariableName() + ")" +
                                " = " +
                                "1" + "/" + getSimulationTimeVariableName() + " * " + simulableSpecies.getVariableName()
                                + ";\n"
                        );
                    }
                }
            }
        }

        StringBuilder code = new StringBuilder("model Monitor\n\n");
        code.append(declarations + "\n");
        code.append(initialEquation + "\n");
        code.append(equation + "\n");
        code.append("end Monitor;\n");
        return new ModelicaCode(code.toString());
    }

    private String getModuleName(Compartment comp) {
        return comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
    }

    private String getSimulationTimeVariableName() {
        return "simulation_time";
    }

    public Set<ModelicaSimulableSpecies> getSimulableSpeciesSet() {
        Set<ModelicaSimulableSpecies> simulableSpeciesSet = new HashSet<>();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprises()){
            ModelicaSimulableSpecies simSpecies =
                    (ModelicaSimulableSpecies) link.getSimulableSpecies();
            simulableSpeciesSet.add(simSpecies);
        }

        return simulableSpeciesSet;
    }

    public Set<ModelicaSimulableReaction> getSimulableReactionSet() {
        Set<ModelicaSimulableReaction> simulableReactionSet = new HashSet<>();
        for (LinkTypeSimulableReactionComprises link: this.getLinkSimulableReactionComprises()){
            ModelicaSimulableReaction simReaction =
                    (ModelicaSimulableReaction) link.getSimulableReaction();
            simulableReactionSet.add(simReaction);
        }

        return simulableReactionSet;
    }
}
