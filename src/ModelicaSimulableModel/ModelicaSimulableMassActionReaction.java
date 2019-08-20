package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import Model.Reaction;

public class ModelicaSimulableMassActionReaction extends ModelicaSimulableReaction{
    public ModelicaSimulableMassActionReaction(Reaction reaction) {
        super(reaction);
    }

    @Override
    public String getParameters() {
        return null;
    }

    @Override
    public ModelicaCode getRateFormula() {
        return null;
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        return null;
    }
}
