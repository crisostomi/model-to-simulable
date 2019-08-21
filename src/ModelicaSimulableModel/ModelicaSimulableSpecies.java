package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import Model.*;
import SimulableModel.*;

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

            SimulableReaction r = this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reac.getId());

            ModelicaCode rateFormula = (ModelicaCode)r.getRateFormula();
            sb.append(" -1 * " + stoich + " * " + rateFormula.getCode());

            if (reac.isReversible()) {
                ModelicaCode rateInvFormula = (ModelicaCode)r.getRateInvFormula();
                sb.append(" +1 * " + stoich + " * " + rateInvFormula.getCode());
            }
        }

        for (LinkTypeProduct l: s.getLinkProductSet()) {
            Reaction reac = l.getReaction();
            int stoich = l.getStoichiometry();

            SimulableReaction r = this.getLinkSimulableSpeciesComprises().getSimulableModel().getSimulableReaction(reac.getId());

            ModelicaCode rateFormula = (ModelicaCode)r.getRateFormula();
            sb.append(" +1 * " + stoich + " * " + rateFormula.getCode());

            if (reac.isReversible()) {
                ModelicaCode rateInvFormula = (ModelicaCode)r.getRateInvFormula();
                sb.append("-1 * " + stoich + " * " + rateInvFormula.getCode());
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
}
