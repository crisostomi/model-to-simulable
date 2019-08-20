package ModelicaSimulableModel;

import DataTypes.ModelicaConstraint;
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
    public ModelicaConstraint getRateFormula() {
        return null;
    }

    @Override
    public ModelicaConstraint getRateInvFormula() {
        return null;
    }
}
