package SimulableModel;

import DataTypes.Constraint;
import Model.Reaction;

public abstract class SimulableReaction {

    public SimulableReaction(Reaction reaction){

    }

    public abstract Constraint getRateFormula();
}
