package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import Model.Reaction;
import SimulableModel.SimulableReaction;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction) {
        super(reaction);
    }

    public abstract ModelicaCode getRateFormula();

    public abstract ModelicaCode getRateInvFormula();

    public String getRateVariableName() {
        return this.getReactionInstantiate().getId() + "_rate";
    }

    public String getRateInvVariableName(){
        assert this.getReactionInstantiate().isReversible();

        return this.getReactionInstantiate().getId() + "_rateInv";
    }
}
