package ModelicaSimulableModel;

import java.util.*;
import DataTypes.*;
import Model.*;
import SimulableModel.*;
import SimulableModel.Link.*;


public class MichaelisMentenModelicaSimulableReaction extends ModelicaSimulableReaction {

    public MichaelisMentenModelicaSimulableReaction(Reaction reaction, ModelicaSimulableModel model) throws PreconditionsException {
        super(reaction, model);
        assert reaction.isComplex();
    }

    @Override
    public ModelicaCode getRateFormula() {
        Reaction reaction = this.getReactionInstantiate();
        Set<Species> modifiers = reaction.getModifiers();
        Set<Species> reactants = reaction.getReactants();
        StringBuilder code = new StringBuilder();

        // prodotto dei substrati
        StringBuilder substrates = new StringBuilder();
        for (Species reactant: reactants){
            ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(reactant.getId());
            String reactantVariable = simulableSpecies.getConcentrationVariableName();
            substrates.append(reactantVariable+"*");
        }
        substrates = substrates.deleteCharAt(substrates.length()-1);

        if (this.usingVmax()){
            code.append("("+this.getSaturationConstantName()+"*"+substrates+ ")/" +
                    "("+this.getMichaelisConstantName()+"+"+substrates+")");
        }
        else{
            // prodotto degli enzimi
            StringBuilder enzymes = new StringBuilder();
            for (Species modifier: modifiers){
                ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(modifier.getId());
                String modifierVariable = simulableSpecies.getConcentrationVariableName();
                enzymes.append(modifierVariable+"*");
            }
            enzymes = enzymes.deleteCharAt(enzymes.length()-1);
            code.append("("+this.getCatalystConstantName()+"*"+enzymes+"*"+substrates+ ")/" +
                    "("+this.getMichaelisConstantName()+"+"+substrates+")");
        }
        return new ModelicaCode(code.toString());
    }

    public String getMichaelisConstantName(){
        return this.getReactionInstantiate().getId() + "_Km";
    }

    public String getCatalystConstantName(){
        return this.getReactionInstantiate().getId() + "_Kcat";
    }

    public String getSaturationConstantName(){
        return this.getReactionInstantiate().getId() + "_Vmax";
    }

    public ModelicaParameter getMichaelisParameter() {
        String michaelisName = this.getMichaelisConstantName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate(RateParameter.Km).getLowerBound() == r.getRate(RateParameter.Km).getUpperBound()) {
            return new DefinedModelicaParameter(michaelisName, r.getRate(RateParameter.Km).getLowerBound());
        } else {
            return new UndefinedModelicaParameter(michaelisName, r.getRate(RateParameter.Km).getLowerBound(), r.getRate(RateParameter.Km).getUpperBound());
        }
    }

    public ModelicaParameter getCatalystParameter(){
        String catalystName = this.getCatalystConstantName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate(RateParameter.Kcat).getLowerBound() == r.getRate(RateParameter.Kcat).getUpperBound()) {
            return new DefinedModelicaParameter(catalystName, r.getRate(RateParameter.Kcat).getLowerBound());
        } else {
            return new UndefinedModelicaParameter(catalystName, r.getRate(RateParameter.Kcat).getLowerBound(), r.getRate(RateParameter.Kcat).getUpperBound());
        }
    }

    public ModelicaParameter getSaturationParameter(){
        String saturationName = this.getSaturationConstantName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate(RateParameter.Vmax).getLowerBound() == r.getRate(RateParameter.Vmax).getUpperBound()) {
            return new DefinedModelicaParameter(saturationName, r.getRate(RateParameter.Vmax).getLowerBound());
        } else {
            return new UndefinedModelicaParameter(saturationName, r.getRate(RateParameter.Vmax).getLowerBound(), r.getRate(RateParameter.Vmax).getUpperBound());
        }
    }

    private boolean usingVmax() {
        Reaction reaction = this.getReactionInstantiate();
        return reaction.getRateParameters().get(RateParameter.Vmax).getLowerBound() != 0;
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        return null;
    }

    @Override
    public Set<Species> getSpeciesNeededForRate() {
        Reaction reaction = this.getReactionInstantiate();
        Set<Species> speciesNeededForRate = new HashSet<>();
        speciesNeededForRate.addAll(reaction.getReactants());
        speciesNeededForRate.addAll(reaction.getModifiers());

        return speciesNeededForRate;
    }

    @Override
    public Set<Species> getSpeciesNeededForInverseRate() {
        return null;
    }

    @Override
    public StringBuilder getConstantsDeclarationsNeededForRate() {
        Reaction reaction = this.getReactionInstantiate();
        StringBuilder declarations = new StringBuilder();

        String reactionRateKm = this.getMichaelisConstantName();
        declarations.append("\tReal " + reactionRateKm + ";\n");

        if (this.usingVmax()) {
            String reactionRateVmax = this.getSaturationConstantName();
            declarations.append("\tReal " + reactionRateVmax + ";\n");
        } else {
            String reactionRateKcat = this.getCatalystConstantName();
            declarations.append("\tReal " + reactionRateKcat + ";\n");
        }

        return declarations;
    }

    @Override
    public StringBuilder getConstantsDeclarationsNeededForInverseRate() {
        return null;
    }

    @Override
    public StringBuilder getParametersDeclarations() {
        StringBuilder declarations = new StringBuilder();
        Reaction reaction = this.getReactionInstantiate();

        String michaelisConstantName = this.getMichaelisConstantName();
        String line = "\tparameter Real " + michaelisConstantName;
        ModelicaParameter Km = this.getMichaelisParameter();
        if (Km instanceof DefinedModelicaParameter) {
            line += " = " + ((DefinedModelicaParameter) Km).getValue();
        }
        line += ";\n";
        declarations.append(line);

        if (this.usingVmax()) {
            String saturationConstantName = this.getSaturationConstantName();
            line = "\tparameter Real " + saturationConstantName;
            ModelicaParameter Vmax = this.getSaturationParameter();
            if (Vmax instanceof DefinedModelicaParameter) {
                line += " = " + ((DefinedModelicaParameter) Vmax).getValue();
            }
            line += ";\n";
        } else {
            String catalystConstantName = this.getCatalystConstantName();
            line = "\tparameter Real " + catalystConstantName;
            ModelicaParameter Kcat = this.getCatalystParameter();
            if (Kcat instanceof DefinedModelicaParameter) {
                line += " = " + ((DefinedModelicaParameter) Kcat).getValue();
            }
            line += ";\n";
        }

        declarations.append(line);

        if (reaction.isReversible()) {
            System.out.println("WARNING! Complex reversible reaction found");
        }

        return declarations;
    }

    @Override
    public Set<UndefinedModelicaParameter> getUndefinedParameters() {
        Set<UndefinedModelicaParameter> params = new HashSet<>();
        ModelicaParameter michaelisParameter = this.getMichaelisParameter();
        if (michaelisParameter instanceof UndefinedModelicaParameter) {
            params.add((UndefinedModelicaParameter) michaelisParameter);
        }

        if (this.usingVmax()) {
            ModelicaParameter saturationParameter = this.getSaturationParameter();
            if (saturationParameter instanceof UndefinedModelicaParameter) {
                params.add((UndefinedModelicaParameter) saturationParameter);
            }
        } else {
            ModelicaParameter catalystParameter = this.getCatalystParameter();
            if (catalystParameter instanceof UndefinedModelicaParameter) {
                params.add((UndefinedModelicaParameter) catalystParameter);
            }
        }

        return params;
    }

    @Override
    public StringBuilder getLinkingReactionParameterCode(String reactionModule, String parameterModule) {
        StringBuilder equations = new StringBuilder();
        Reaction reaction = this.getReactionInstantiate();

        // reactions.reaction_X_Km = parameters.reaction_X_Km
        // reactions.reaction_X_KCat = parameters.reaction_X_Kcat
        String michaelisMentenConstantName = this.getMichaelisConstantName();

        equations.append("\t\t"+
                reactionModule + "." + michaelisMentenConstantName +
                " = " +
                parameterModule + "." + michaelisMentenConstantName +
                ";\n"
        );

        if (this.usingVmax()) {
            String saturationConstantName = this.getSaturationConstantName();

            equations.append("\t\t"+
                    reactionModule + "." + saturationConstantName +
                    " = " +
                    parameterModule + "." + saturationConstantName +
                    ";\n"
            );

        } else {
            String catalystConstantName = this.getCatalystConstantName();

            equations.append("\t\t"+
                    reactionModule + "." + catalystConstantName +
                    " = " +
                    parameterModule + "." + catalystConstantName +
                    ";\n"
            );
        }
        if (reaction.isReversible()) {
            //complex reversible code
            System.out.println("WARNING! Complex reversible reaction found");
        }

        return equations;
    }
}
