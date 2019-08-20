package ModelicaSimulableModel;

import Model.Reaction;
import SimulableModel.SimulableReaction;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction) {
        super(reaction);
    }

    public abstract String getParameters();

    public abstract String getRateFormula();

    public abstract String getRateInvFormula();

}
