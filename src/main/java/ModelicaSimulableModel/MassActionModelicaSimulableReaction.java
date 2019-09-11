package ModelicaSimulableModel;

import java.util.*;
import DataTypes.*;
import Model.*;
import Model.LinkType.*;
import SimulableModel.*;
import SimulableModel.Link.*;


public class MassActionModelicaSimulableReaction extends ModelicaSimulableReaction{
    public MassActionModelicaSimulableReaction(Reaction reaction, ModelicaSimulableModel model) throws PreconditionsException {
        super(reaction, model);
        assert !reaction.isComplex();
    }

    @Override
    public ModelicaCode getRateFormula() {
        Set<LinkTypeReactant> reactantLinks = this.getReactionInstantiate().getLinkReactantSet();
        StringBuilder code = new StringBuilder(""+this.getRateConstantVariableName());
        for (LinkTypeReactant link:reactantLinks){
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies)this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(s_id);

            int stoich = link.getStoichiometry();
            String speciesVariable = ss.getConcentrationVariableName();
            code.append("*"+speciesVariable+"^"+stoich);
        }
        return new ModelicaCode(code.toString());
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        Set<LinkTypeProduct> productLinks = this.getReactionInstantiate().getLinkProductSet();
        StringBuilder code = new StringBuilder("" + this.getRateInvConstantVariableName());
        for (LinkTypeProduct link : productLinks) {
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(s_id);

            int stoich = link.getStoichiometry();
            String speciesVariable = ss.getConcentrationVariableName();
            code.append("*" + speciesVariable + "^" + stoich);

        }
        return new ModelicaCode(code.toString());
    }

    @Override
    public Set<Species> getSpeciesNeededForRate() {
        Reaction reaction = this.getReactionInstantiate();
        return reaction.getReactants();
    }

    @Override
    public Set<Species> getSpeciesNeededForInverseRate() {
        Reaction reaction = this.getReactionInstantiate();
        return reaction.getProducts();
    }

    @Override
    public StringBuilder getConstantsDeclarationsNeededForRate() {
        String reactionRateConstantVariable = this.getRateConstantVariableName();
        return new StringBuilder("\tReal " + reactionRateConstantVariable + ";\n");
    }

    @Override
    public StringBuilder getConstantsDeclarationsNeededForInverseRate() {
        String reactionRateInvConstantVariable = this.getRateInvConstantVariableName();
        return new StringBuilder("\tparameter Real " + reactionRateInvConstantVariable + ";\n");
    }

    @Override
    public StringBuilder getParametersDeclarations() {
        StringBuilder declarations = new StringBuilder();
        Reaction reaction = this.getReactionInstantiate();

        String rateConstantVariable = this.getRateConstantVariableName();
        String line = "parameter Real " + rateConstantVariable;
        ModelicaParameter rateModelicaParameter = this.getParameter();
        if (rateModelicaParameter instanceof DefinedModelicaParameter) {
            line = line + " = " + ((DefinedModelicaParameter) rateModelicaParameter).getValue();
        }
        declarations.append("\t" + line + ";\n");


        if (reaction.isReversible()) {
            String rateInvConstantVariable = this.getRateInvVariableName();
            line = "parameter Real " + rateInvConstantVariable;
            ModelicaParameter rateInvModelicaParameter = this.getInvParameter();
            if (rateInvModelicaParameter instanceof DefinedModelicaParameter) {
                line = line + " = " + ((DefinedModelicaParameter) rateInvModelicaParameter).getValue();
            }

            declarations.append("\t" + line + ";\n");
        }
        return declarations;
    }

    @Override
    public Set<UndefinedModelicaParameter> getUndefinedParameters() {
        Set<UndefinedModelicaParameter> params = new HashSet<>();
        ModelicaParameter reactionParam = this.getParameter();
        if (reactionParam instanceof UndefinedModelicaParameter) {
            params.add((UndefinedModelicaParameter) reactionParam);
        }

        return params;
    }

    @Override
    public StringBuilder getLinkingReactionParameterCode(String reactionModule, String parameterModule) {
        StringBuilder equations = new StringBuilder();
        Reaction reaction = this.getReactionInstantiate();

        String reactionRateConstantVariable = this.getRateConstantVariableName();
        equations.append("\t\t"+
                reactionModule + "." + reactionRateConstantVariable +
                " = " +
                parameterModule + "." + reactionRateConstantVariable +
                ";\n"
        );
        if (reaction.isReversible()) {
            String reactionRateInvConstantVariable = this.getRateInvConstantVariableName();
            equations.append("\t\t"+
                    reactionModule + "." + reactionRateInvConstantVariable +
                    " = " +
                    parameterModule + "." + reactionRateInvConstantVariable +
                    ";\n"
            );
        }


        return equations;
    }

    public String getRateVariableName() {
        return this.getReactionInstantiate().getId() + "_rate";
    }

    public String getRateConstantVariableName() {
        return this.getReactionInstantiate().getId() + "_K";
    }

    public String getRateInvConstantVariableName() {
        assert this.getReactionInstantiate().isReversible();

        return this.getReactionInstantiate().getId() + "_K_inv";
    }

    public ModelicaParameter getParameter() {
        String parameterName = this.getRateConstantVariableName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate(RateParameter.K).getLowerBound() == r.getRate(RateParameter.K).getUpperBound()) {
            return new DefinedModelicaParameter(parameterName, r.getRate(RateParameter.K).getLowerBound());
        } else {
            return new UndefinedModelicaParameter(parameterName, r.getRate(RateParameter.K).getLowerBound(), r.getRate(RateParameter.K).getUpperBound());
        }
    }

    public ModelicaParameter getInvParameter() {
        Reaction r = this.getReactionInstantiate();
        assert r.isReversible();

        String parameterName = this.getRateInvConstantVariableName();
        try {
            if (r.getRateInv(RateParameter.K).getLowerBound() == r.getRateInv(RateParameter.K).getUpperBound()) {
                return new DefinedModelicaParameter(parameterName, r.getRateInv(RateParameter.K).getLowerBound());
            } else {
                return new UndefinedModelicaParameter(parameterName, r.getRateInv(RateParameter.K).getLowerBound(), r.getRateInv(RateParameter.K).getUpperBound());
            }
        } catch (DataTypes.PreconditionsException e) {return null;}
    }
}
