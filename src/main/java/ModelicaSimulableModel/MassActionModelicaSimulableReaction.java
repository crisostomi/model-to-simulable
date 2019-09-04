package ModelicaSimulableModel;

import DataTypes.*;
import Model.LinkType.LinkTypeModifier;
import SimulableModel.*;

import java.util.HashSet;
import java.util.Set;
import Model.LinkType.LinkTypeProduct;
import Model.LinkType.LinkTypeReactant;
import Model.Reaction;
import Model.Species;
import SimulableModel.Link.LinkSimulableSpeciesComprises;


public class MassActionModelicaSimulableReaction extends ModelicaSimulableReaction{
    public MassActionModelicaSimulableReaction(Reaction reaction, SimulableModel model) throws PreconditionsException {
        super(reaction);

        Set<Species> speciesInvolved = new HashSet<>();
        for (LinkTypeReactant l: reaction.getReactants()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (LinkTypeProduct l: reaction.getProducts()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (LinkTypeModifier l: reaction.getModifiers()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (Species s: speciesInvolved) {
            String sId = s.getId();
            SimulableSpecies ss = model.getSimulableSpecies(sId);
            if (ss == null) {
                ss = new ModelicaSimulableSpecies(s);
            }

            LinkSimulableSpeciesComprises.insertLink(model, ss);
        }
    }

    @Override
    public ModelicaCode getRateFormula() {
        Set<LinkTypeReactant> reactantLinks = this.getReactionInstantiate().getReactants();
        StringBuilder code = new StringBuilder(""+this.getRateConstantVariableName());
        for (LinkTypeReactant link:reactantLinks){
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies)this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(s_id);

            int stoich = link.getStoichiometry();
            String speciesVariable = ss.getVariableName();
            code.append("*"+speciesVariable+"^"+stoich);
        }
        return new ModelicaCode(code.toString());
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        Set<LinkTypeProduct> productLinks = this.getReactionInstantiate().getProducts();
        StringBuilder code = new StringBuilder("" + this.getRateInvConstantVariableName());
        for (LinkTypeProduct link : productLinks) {
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(s_id);

            int stoich = link.getStoichiometry();
            String speciesVariable = ss.getVariableName();
            code.append("*" + speciesVariable + "^" + stoich);

        }
        return new ModelicaCode(code.toString());
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
