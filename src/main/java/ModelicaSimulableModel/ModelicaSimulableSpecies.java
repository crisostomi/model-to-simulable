package ModelicaSimulableModel;

import DataTypes.DefinedModelicaParameter;
import DataTypes.ModelicaCode;
import DataTypes.ModelicaParameter;
import DataTypes.UndefinedModelicaParameter;
import Model.*;
import Model.LinkType.LinkTypeModifier;
import Model.LinkType.LinkTypeProduct;
import Model.LinkType.LinkTypeReactant;
import SimulableModel.*;

import java.util.HashSet;
import java.util.Set;

public class ModelicaSimulableSpecies extends SimulableSpecies {

    public ModelicaSimulableSpecies(Species s) {
        super(s);

    }

    @Override
    public ModelicaCode getODE_RHS() {
        Species s = this.getSpeciesInstantiate();
        StringBuilder sb = new StringBuilder();

        for (LinkTypeReactant l: s.getLinkReactantSet()) {
            Reaction reac = l.getReaction();
            int stoich = l.getStoichiometry();
            ModelicaSimulableReaction sr =
                    (ModelicaSimulableReaction) this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reac.getId());

            sb.append(" -1 * " + stoich + " * " + sr.getRateVariableName());

            if (reac.isReversible()) {
                sb.append(" +1 * " + stoich + " * " + sr.getRateInvVariableName());
            }
        }

        for (LinkTypeProduct l: s.getLinkProductSet()) {
            Reaction reac = l.getReaction();
            int stoich = l.getStoichiometry();
            ModelicaSimulableReaction sr =
                    (ModelicaSimulableReaction) this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reac.getId());


            sb.append(" +1 * " + stoich + " * " + sr.getRateVariableName());

            if (reac.isReversible()) {
                sb.append("-1 * " + stoich + " * " + sr.getRateInvVariableName());
            }
        }

        ModelicaCode rhs = new ModelicaCode(sb.toString());

        return rhs;
    }

    public ModelicaParameter getParameter() {
        Species s = this.getSpeciesInstantiate();
        String parameterName = this.getInitialConcentrationVariableName();

        if (s.getInitialAmount().getLowerBound() == s.getInitialAmount().getUpperBound()) {
            return new DefinedModelicaParameter(parameterName, s.getInitialAmount().getLowerBound());
        } else {
            return new UndefinedModelicaParameter(parameterName, s.getInitialAmount().getLowerBound(), s.getInitialAmount().getUpperBound());
        }
    }

    public Set<ModelicaSimulableReaction> getInvolvedReactions(){
        Set<ModelicaSimulableReaction> reactions = new HashSet<>();
        for (LinkTypeReactant linkReactant:this.getSpeciesInstantiate().getLinkReactantSet()){
            Reaction reaction = linkReactant.getReaction();

            ModelicaSimulableReaction sr =
                    (ModelicaSimulableReaction) this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reaction.getId());

            reactions.add(sr);
        }
        for (LinkTypeProduct linkProduct:this.getSpeciesInstantiate().getLinkProductSet()){
            Reaction reaction = linkProduct.getReaction();

            ModelicaSimulableReaction sr =
                    (ModelicaSimulableReaction) this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reaction.getId());

            reactions.add(sr);
        }
        for (LinkTypeModifier linkModifier:this.getSpeciesInstantiate().getLinkModifierSet()){
            Reaction reaction = linkModifier.getReaction();

            ModelicaSimulableReaction sr =
                    (ModelicaSimulableReaction) this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reaction.getId());

            reactions.add(sr);
        }
        return reactions;

    }

    public String getConcentrationVariableName() {
        return this.getSpeciesInstantiate().getId() + "_conc";
    }

    public String getInitialConcentrationVariableName() {
        return getConcentrationVariableName() + "_init";
    }

    public String getAverageConcentrationVariableName() {
        return getConcentrationVariableName() + "_avg";
    }
}
