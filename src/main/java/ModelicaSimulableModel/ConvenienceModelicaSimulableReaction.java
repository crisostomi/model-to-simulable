package ModelicaSimulableModel;

import DataTypes.*;
import Model.Reaction;
import Model.Species;

import java.util.HashSet;
import java.util.Set;

public class ConvenienceModelicaSimulableReaction extends ModelicaSimulableReaction {
    public ConvenienceModelicaSimulableReaction(Reaction reaction, ModelicaSimulableModel model) throws PreconditionsException {
        super(reaction, model);
        assert reaction.isComplex();
        assert reaction.getReactants().size() > 1 && reaction.getProducts().size() > 1;
    }

    @Override
    public ModelicaCode getRateFormula() {
        Reaction reaction = this.getReactionInstantiate();
        Set<Species> enzymes = reaction.getModifiers();
        Set<Species> substrates = reaction.getReactants();
        StringBuilder code = new StringBuilder();

        // prodotto di enzimi * (k_cat * prodotto di substrato_i / kM) / (prodotto di 1 + rapporto di substrato_i / kM)
        String k_cat = this.getCatalystConstantName();
        String k_m = this.getMichaelisConstantName();
        for (Species enzyme: enzymes) {
            ModelicaSimulableSpecies simulableSpecies =
                    (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(enzyme.getId());
            code.append(simulableSpecies.getConcentrationVariableName() + "*");
        }

        code.append("(" + k_cat);
        for (Species substrate: substrates) {
            ModelicaSimulableSpecies simulableSpecies =
                    (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(substrate.getId());
            code.append(
                    "*(" + simulableSpecies.getConcentrationVariableName() + " / " + k_m + ")"
            );
        }
        code.append(") / (");

        for (Species substrate: substrates) {
            ModelicaSimulableSpecies simulableSpecies =
                    (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(substrate.getId());
            code.append(
                    "(1 + " + simulableSpecies.getConcentrationVariableName() + " / " + k_m + ")*"
            );
        }
        code.replace(code.length()-1, code.length(), "");
        code.append(")");

        return new ModelicaCode(code.toString());
    }

    public String getMichaelisConstantName(){
        return this.getReactionInstantiate().getId() + "_Km";
    }

    public String getCatalystConstantName(){
        return this.getReactionInstantiate().getId() + "_Kcat";
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

        String reactionRateKcat = this.getCatalystConstantName();
        declarations.append("\tReal " + reactionRateKcat + ";\n");

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

        String catalystConstantName = this.getCatalystConstantName();
        line = "\tparameter Real " + catalystConstantName;
        ModelicaParameter Kcat = this.getCatalystParameter();
        if (Kcat instanceof DefinedModelicaParameter) {
            line += " = " + ((DefinedModelicaParameter) Kcat).getValue();
        }
        line += ";\n";

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

        ModelicaParameter catalystParameter = this.getCatalystParameter();
        if (catalystParameter instanceof UndefinedModelicaParameter) {
            params.add((UndefinedModelicaParameter) catalystParameter);
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


        String catalystConstantName = this.getCatalystConstantName();

        equations.append("\t\t"+
                reactionModule + "." + catalystConstantName +
                " = " +
                parameterModule + "." + catalystConstantName +
                ";\n"
        );
        if (reaction.isReversible()) {
            //complex reversible code
            System.out.println("WARNING! Complex reversible reaction found");
        }

        return equations;
    }
}
