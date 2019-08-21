package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import Model.Reaction;
import SimulableModel.SimulableReaction;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction) {
        super(reaction);
    }

    public abstract Parameter getParameters();

    public abstract ModelicaCode getRateFormula();

    public abstract ModelicaCode getRateInvFormula();

}
