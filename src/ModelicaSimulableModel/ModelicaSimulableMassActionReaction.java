package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import Model.*;
import SimulableModel.*;

import java.util.HashSet;
import java.util.Set;

public class ModelicaSimulableMassActionReaction extends ModelicaSimulableReaction{
    public ModelicaSimulableMassActionReaction(Reaction reaction) throws PreconditionsException {
        super(reaction);

        Set<Species> speciesInvolved = new HashSet<>();
        SimulableModel model = this.getLinkSimulableReactionComprises().getSimulableModel();
        for (LinkTypeReactant l: reaction.getReactants()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (LinkTypeProduct l: reaction.getProducts()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (LinkTypeModifier l: reaction.getModifiers()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (Species s: speciesInvolved) {
            String sId = s.getId();
            SimulableSpecies ss = model.getSimulableSpecies(sId);
            if (ss == null) {
                ss = new ModelicaSimulableSpecies(s);
            }

            LinkSimulableSpeciesComprises.insertLink(model, ss);
        }
    }

    @Override
    public String getParameters() {
        return null;
    }

    @Override
    public ModelicaCode getRateFormula() {
        return null;
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        return null;
    }
}
