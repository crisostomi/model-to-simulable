package ModelicaSimulableModel;

import java.util.*;
import DataTypes.*;
import Model.*;
import SimulableModel.*;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction) {
        super(reaction);
    }

    public abstract ModelicaCode getRateFormula();

    public abstract ModelicaCode getRateInvFormula();

    public abstract Set<Species> getSpeciesNeededForRate();

    public abstract Set<Species> getSpeciesNeededForInverseRate();

    public abstract StringBuilder getConstantsDeclarationsNeededForRate();

    public abstract StringBuilder getConstantsDeclarationsNeededForInverseRate();

    public abstract StringBuilder getParametersDeclarations();

    public abstract Set<UndefinedModelicaParameter> getUndefinedParameters();

    public abstract StringBuilder getLinkingReactionParameterCode(String reactionModule, String parameterModule);

    public String getRateVariableName() {
        return this.getReactionInstantiate().getId() + "_rate";
    }

    public String getRateInvVariableName(){
        assert this.getReactionInstantiate().isReversible();

        return this.getReactionInstantiate().getId() + "_rateInv";
    }
}
