package ModelicaSimulableModel;

import DataTypes.ModelicaConstraint;
import Model.Reaction;
import SimulableModel.SimulableReaction;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction) {
        super(reaction);
    }

    public abstract String getParameters();

    public abstract ModelicaConstraint getRateFormula();

    public abstract ModelicaConstraint getRateInvFormula();

}
