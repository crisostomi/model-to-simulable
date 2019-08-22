package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import Model.*;
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

    public Parameter getParameters() {


        Species s = this.getSpeciesInstantiate();
        String s_id = s.getId();

        Parameter p = new Parameter("species", s_id);
        Double min_ia = s.getInitialAmount().getLowerBound();
        Double max_ia = s.getInitialAmount().getUpperBound();

        p.addProperty("minInitialAmount", min_ia.toString());
        p.addProperty("maxInitialAmount", max_ia.toString());

        return p;
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
        return reactions;
    }

    public String getVariableName() {
        return this.getSpeciesInstantiate().getId();
    }

    public String getInitialAmountVariableName() {
        return getVariableName() + "_init";
    }
}
