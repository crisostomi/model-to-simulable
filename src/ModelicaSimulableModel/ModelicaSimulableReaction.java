package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import Model.Reaction;
import SimulableModel.PreconditionsException;
import SimulableModel.SimulableReaction;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction) {
        super(reaction);
    }

    public abstract Parameter getParameters();

    public abstract ModelicaCode getRateFormula();

    public abstract ModelicaCode getRateInvFormula();

    public String getRateVariableName() {
        return this.getReactionInstantiate().getId() + "_rate";
    }

    public String getRateConstantVariableName() {
        return this.getReactionInstantiate().getId() + "_rateConstant";
    }

    public String getRateInvVariableName(){
        assert this.getReactionInstantiate().isReversible();

        return this.getReactionInstantiate().getId() + "_rateInv";
    }

    public String getRateInvConstantVariableName() {
        assert this.getReactionInstantiate().isReversible();

        return this.getReactionInstantiate().getId() + "_rateInvConstant";
    }

}
