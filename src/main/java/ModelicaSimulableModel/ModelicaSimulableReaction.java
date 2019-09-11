package ModelicaSimulableModel;

import java.util.*;
import DataTypes.*;
import Model.*;
import SimulableModel.*;
import SimulableModel.Link.LinkSimulableSpeciesComprises;

public abstract class ModelicaSimulableReaction extends SimulableReaction {

    public ModelicaSimulableReaction(Reaction reaction, ModelicaSimulableModel model) throws PreconditionsException {
        super(reaction);
        Set<Species> speciesInvolved = new HashSet<>();
        speciesInvolved.addAll(reaction.getReactants());
        speciesInvolved.addAll(reaction.getProducts());
        speciesInvolved.addAll(reaction.getModifiers());


        for (Species species: speciesInvolved) {
            SimulableSpecies simulableSpecies = model.getSimulableSpecies(species.getId());
            if (simulableSpecies == null) {
                simulableSpecies = new ModelicaSimulableSpecies(species);
            }

            LinkSimulableSpeciesComprises.insertLink(model, simulableSpecies);
        }
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
