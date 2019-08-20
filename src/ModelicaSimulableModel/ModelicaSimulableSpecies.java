package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
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

    public String getParameters() {
        StringBuilder sb = new StringBuilder();
        Species s = this.getSpeciesInstantiate();

        sb.append("<species ");
        String sId = s.getId();
        sb.append("id=\"" + sId + "\" ");

        Double minIa = s.getInitialAmount().getLowerBound();
        Double maxIa = s.getInitialAmount().getUpperBound();

        sb.append("minInitialAmount=\"" + minIa + "\" ");
        sb.append("maxInitialAmount=\"" + maxIa + "\"");

        sb.append(">");

        return sb.toString();
    }
}
