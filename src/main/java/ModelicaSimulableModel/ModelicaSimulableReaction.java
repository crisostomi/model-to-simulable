package ModelicaSimulableModel;

import DataTypes.DefinedParameter;
import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import DataTypes.UndefinedParameter;
import Model.Reaction;
import SimulableModel.PreconditionsException;
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

    public Parameter getParameter() {
        String parameterName = this.getRateConstantVariableName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate().getLowerBound() == r.getRate().getUpperBound()) {
            return new DefinedParameter(parameterName, r.getRate().getLowerBound());
        } else {
            return new UndefinedParameter(parameterName, r.getRate().getLowerBound(), r.getRate().getUpperBound());
        }
    }

    public Parameter getInvParameter() {
        Reaction r = this.getReactionInstantiate();
        assert r.isReversible();

        String parameterName = this.getRateInvConstantVariableName();
        try {
            if (r.getRateInv().getLowerBound() == r.getRateInv().getUpperBound()) {
                return new DefinedParameter(parameterName, r.getRateInv().getLowerBound());
            } else {
                return new UndefinedParameter(parameterName, r.getRateInv().getLowerBound(), r.getRateInv().getUpperBound());
            }
        } catch (DataTypes.PreconditionsException e) {return null;}
    }

}
