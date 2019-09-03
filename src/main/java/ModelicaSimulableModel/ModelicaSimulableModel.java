package ModelicaSimulableModel;

import DataTypes.DefinedModelicaParameter;
import DataTypes.ModelicaCode;
import DataTypes.ModelicaParameter;
import DataTypes.UndefinedModelicaParameter;
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
        map.put("Parameters", getParametersCode());
        map.put("Monitor", getMonitorCode());
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
                String rateFormula = simReaction.getRateFormula().getCode();
                equation.append("\t\t"+ reactionRateVariable + " = " + rateFormula + ";\n");

                if ( reaction.isComplex() ){
                      String reactionRateKm = ((MichaelisMentenModelicaSimulableReaction) simReaction).getMichaelisConstantName();
                      line = "\tReal " + reactionRateKm+";\n";
                      String reactionRateKcat = ((MichaelisMentenModelicaSimulableReaction) simReaction).getCatalystConstantName();
                      line += "\tReal " + reactionRateKcat;
                      reacDeclarations.append(line + ";"+"\n");
                }
                else {
                    String reactionRateConstantVariable =( (MassActionModelicaSimulableReaction) simReaction).getRateConstantVariableName();
                    parameters.append("\tReal " + reactionRateConstantVariable + ";\n");
                    speciesDeclarations.append(getReactantsNeededForRate(reaction));
                }

                if (reaction.isReversible()){
                    if (reaction.isComplex()){
                        // complex reversible code
                    }
                    else {
                        String reactionRateInvConstantVariable = ((MassActionModelicaSimulableReaction) simReaction).getRateInvConstantVariableName();
                        parameters.append("\tparameter Real " + reactionRateInvConstantVariable + ";\n");
                        speciesDeclarations.append(getProductsNeededForRate(reaction));
                    }

                    String reactionRateInvVariable = simReaction.getRateInvVariableName();
                    String rateInvFormula = simReaction.getRateInvFormula().getCode();
                    equation.append("\t\t"+ reactionRateInvVariable + " = " + rateInvFormula + ";\n");
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

    private String getReactantsNeededForRate(Reaction reaction){
        String line = "";
        for (LinkTypeReactant linkReactant: reaction.getReactants()){
            Species species = linkReactant.getSpecies();
            String speciesVariable =
                    ((ModelicaSimulableSpecies)this.getSimulableSpecies(species.getId())).getVariableName();

            line += "\tReal "+speciesVariable;
            if (species.getName() != null) {
                line = line + " \"" + species.getName() + "\"";
            }
            line += ";\n";
        }
        return line;
    }

    private String getProductsNeededForRate(Reaction reaction){
        String line = "";
        for (LinkTypeProduct linkProduct: reaction.getProducts()){
            Species species = linkProduct.getSpecies();

            String speciesVariable =
                    ((ModelicaSimulableSpecies)this.getSimulableSpecies(species.getId())).getVariableName();

            line += "\tReal "+speciesVariable;
            if (species.getName() != null) {
                line = line + " \"" + species.getName() + "\"";
            }
            line += ";\n";
        }
        return line;
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

        // code for linking reaction variables
        declarations.append("\tReactions reactions;\n");
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
        declarations.append("\tParameters parameters;\n");
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
                    if (reaction.isComplex()){
                        String michaelisMentenConstantName = ((MichaelisMentenModelicaSimulableReaction) simReaction).getMichaelisConstantName();

                        equations.append("\t\t"+
                                "reactions" + "." + michaelisMentenConstantName +
                                " = " +
                                "parameters" + "." + michaelisMentenConstantName +
                                ";\n"
                        );

                        String catalystConstantName = ((MichaelisMentenModelicaSimulableReaction) simReaction).getCatalystConstantName();

                        equations.append("\t\t"+
                                "reactions" + "." + catalystConstantName +
                                " = " +
                                "parameters" + "." + catalystConstantName +
                                ";\n"
                        );
                        if (reaction.isReversible()) {
                            //complex reversible code
                        }

                    }
                    else{
                        String reactionRateConstantVariable = ((MassActionModelicaSimulableReaction) simReaction).getRateConstantVariableName();
                        equations.append("\t\t"+
                                "reactions" + "." + reactionRateConstantVariable +
                                " = " +
                                "parameters" + "." + reactionRateConstantVariable +
                                ";\n"
                        );
                        if (reaction.isReversible()) {
                            String reactionRateInvConstantVariable = ((MassActionModelicaSimulableReaction) simReaction).getRateInvConstantVariableName();
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
        }

        // code for linking monitor
        declarations.append("\tMonitor monitor;\n");
        equations.append(
                "\t\tmonitor" + "." + getSimulationTimeVariableName() +
                        " = " +
                        "parameters" + "." + getSimulationTimeVariableName() + ";\n"
        );

        for (LinkTypeComprises l: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();
            if (be instanceof Compartment) {
                Compartment comp = (Compartment) be;
                for (LinkTypeSpeciesCompartment link: comp.getLinkSpeciesCompartmentSet()) {
                    Species s = link.getSpecies();
                    ModelicaSimulableSpecies ss =
                            (ModelicaSimulableSpecies) getSimulableSpecies(s.getId());

                    if (ss != null) {
                        equations.append("\t\t" +
                                comp.getId() + "." + ss.getVariableName() +
                                " = "+
                                "monitor" + "." + ss.getVariableName() + ";\n"
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

    private ModelicaCode getParametersCode() {
        StringBuilder declarations = new StringBuilder();
        Model m = this.getModelInstantiate();

        for (LinkTypeComprises l: m.getLinkComprisesSet()) {
            BiologicalEntity be = l.getBiologicalEntity();

            if (be instanceof Species) {
                String speciesCode = getSpeciesParametersCode((Species) be);
                declarations.append("\t"+speciesCode + ";\n");
            }

            else if (be instanceof Reaction) {
                String reactionCode = getReactionParametersCode((Reaction) be);
                declarations.append(reactionCode);
            }
        }
        StringBuilder code = new StringBuilder("model Parameters\n");
        code.append(declarations + "\n");
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
        SimulableReaction simulableReaction = this.getSimulableReaction(reaction.getId());
        StringBuilder declarations = new StringBuilder();

        if (reaction.isComplex()) {

            String michaelisConstantName = ((MichaelisMentenModelicaSimulableReaction) simulableReaction).getMichaelisConstantName();
            String line = "parameter Real " + michaelisConstantName+";\n";
            ModelicaParameter Km = ((MichaelisMentenModelicaSimulableReaction) simulableReaction).getMichaelisParameter();
            if (Km instanceof DefinedModelicaParameter) {
                line = line + " = " + ((DefinedModelicaParameter) Km).getValue();
            }

            String catalystConstantName = ((MichaelisMentenModelicaSimulableReaction) simulableReaction).getCatalystConstantName();
            line += "\tparameter Real " + catalystConstantName;
            ModelicaParameter Kcat = ((MichaelisMentenModelicaSimulableReaction) simulableReaction).getCatalystParameter();
            if (Kcat instanceof DefinedModelicaParameter) {
                line = line + " = " + ((DefinedModelicaParameter) Kcat).getValue();
            }

            declarations.append("\t"+line+";\n");
        } else {
            String rateConstantVariable = ((MassActionModelicaSimulableReaction) simulableReaction).getRateConstantVariableName();
            String line = "parameter Real " + rateConstantVariable;
            ModelicaParameter rateModelicaParameter = ((MassActionModelicaSimulableReaction) simulableReaction).getParameter();
            if (rateModelicaParameter instanceof DefinedModelicaParameter) {
                line = line + " = " + ((DefinedModelicaParameter) rateModelicaParameter).getValue();
            }
            declarations.append("\t" + line + ";\n");
        }

        if (reaction.isReversible()) {
            if (reaction.isComplex()) {
                //complex reversible code
            } else {
                String rateInvConstantVariable = ((MassActionModelicaSimulableReaction) simulableReaction).getRateInvVariableName();
                String line = "parameter Real " + rateInvConstantVariable;
                ModelicaParameter rateInvModelicaParameter = ((MassActionModelicaSimulableReaction) simulableReaction).getInvParameter();
                if (rateInvModelicaParameter instanceof DefinedModelicaParameter) {
                    line = line + " = " + ((DefinedModelicaParameter) rateInvModelicaParameter).getValue();
                }

                declarations.append("\t" + line + ";\n");
            }
        }
        return declarations.toString();
    }

    private ModelicaCode getMonitorCode() {
        StringBuilder declarations = new StringBuilder();
        StringBuilder initialEquation = new StringBuilder("\tinitial equation\n");
        StringBuilder equation = new StringBuilder("\tequation\n");

        declarations.append("\tReal " + getSimulationTimeVariableName() + ";\n");

        for (LinkTypeComprises link: this.getModelInstantiate().getLinkComprisesSet()) {
            BiologicalEntity be = link.getBiologicalEntity();
            if (be instanceof Compartment) {
                Compartment comp = (Compartment) be;
                for (LinkTypeSpeciesCompartment linkComp: comp.getLinkSpeciesCompartmentSet()) {
                    Species s = linkComp.getSpecies();
                    ModelicaSimulableSpecies ss =
                            (ModelicaSimulableSpecies) this.getSimulableSpecies(s.getId());

                    if (ss != null) {
                        declarations.append("\tReal " + ss.getVariableName() + ";\n");
                        declarations.append("\tReal " + ss.getAverageVariableName() + ";\n");
                        initialEquation.append("\t\t" + ss.getAverageVariableName() + " = 0;\n");
                        equation.append(
                                "\t\tder(" + ss.getAverageVariableName() + ")" +
                                " = " +
                                "1" + "/" + getSimulationTimeVariableName() + " * " + ss.getVariableName()
                                + ";\n"
                        );
                    }
                }
            }
        }

        StringBuilder code = new StringBuilder("model Monitor\n");
        code.append(declarations + "\n");
        code.append(initialEquation + "\n");
        code.append(equation + "\n");
        code.append("end Monitor;\n");
        return new ModelicaCode(code.toString());
    }

    public Map<String, Double> getProteinConstraints() {
        Map<String, Double> constraints = new HashMap<>();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprises()) {
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies) link.getSimulableSpecies();
            Species s = ss.getSpeciesInstantiate();
            if (s instanceof Protein) {
                Protein p = (Protein) s;
                if (p.getAbundance() != null) {
                    constraints.put(ss.getAverageVariableName(), p.getAbundance());
                }
            }
        }

        return constraints;
    }

    public List<UndefinedModelicaParameter> getUndefinedParameters(){
        List<UndefinedModelicaParameter> params = new ArrayList<>();
        for (LinkTypeSimulableSpeciesComprises link: this.getLinkSimulableSpeciesComprises()){
            ModelicaSimulableSpecies simSpecies =
                    (ModelicaSimulableSpecies) link.getSimulableSpecies();
            ModelicaParameter speciesParam = simSpecies.getParameter();
            if (speciesParam instanceof UndefinedModelicaParameter) {
                params.add((UndefinedModelicaParameter) speciesParam);
            }
        }
        for (LinkTypeSimulableReactionComprises link: this.getLinkSimulableReactionComprises()){
            ModelicaSimulableReaction simReac =
                    (ModelicaSimulableReaction)link.getSimulableReaction();
            if (simReac.getReactionInstantiate().isComplex()){
                ModelicaParameter michaelisParameter = ((MichaelisMentenModelicaSimulableReaction) simReac).getMichaelisParameter();
                if (michaelisParameter instanceof UndefinedModelicaParameter) {
                    params.add((UndefinedModelicaParameter) michaelisParameter);
                }

                ModelicaParameter catalystParameter = ((MichaelisMentenModelicaSimulableReaction) simReac).getCatalystParameter();
                if (catalystParameter instanceof UndefinedModelicaParameter) {
                    params.add((UndefinedModelicaParameter) catalystParameter);
                }

            }
            else {
                ModelicaParameter reactionParam = ((MassActionModelicaSimulableReaction) simReac).getParameter();
                if (reactionParam instanceof UndefinedModelicaParameter) {
                    params.add((UndefinedModelicaParameter) reactionParam);
                }
            }

        }
        return params;
    }

    private String getModuleName(Compartment comp) {
        return comp.getId().substring(0, 1).toUpperCase() + comp.getId().substring(1);
    }

    private String getSimulationTimeVariableName() {
        return "simulation_time";
    }
}
