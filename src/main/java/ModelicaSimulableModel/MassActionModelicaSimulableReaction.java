package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.Parameter;
import Model.*;
import SimulableModel.*;

import java.util.HashSet;
import java.util.Set;
import Model.LinkTypeProduct;
import Model.LinkTypeReactant;
import Model.Reaction;
import Model.Species;


public class MassActionModelicaSimulableReaction extends ModelicaSimulableReaction{
    public MassActionModelicaSimulableReaction(Reaction reaction, SimulableModel model) throws PreconditionsException {
        super(reaction);

        Set<Species> speciesInvolved = new HashSet<>();
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
    public ModelicaCode getRateFormula() {
        Set<LinkTypeReactant> reactantLinks = this.getReactionInstantiate().getReactants();
        StringBuilder code = new StringBuilder(""+this.getRateConstantVariableName());
        for (LinkTypeReactant link:reactantLinks){
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies)this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(s_id);

            int stoich = link.getStoichiometry();
            String speciesVariable = ss.getVariableName();
            code.append("*"+speciesVariable+"^"+stoich);
        }
        return new ModelicaCode(code.toString());
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        Set<LinkTypeProduct> productLinks = this.getReactionInstantiate().getProducts();
        StringBuilder code = new StringBuilder("" + this.getRateInvConstantVariableName());
        for (LinkTypeProduct link : productLinks) {
            Species species = link.getSpecies();
            String s_id = species.getId();
            ModelicaSimulableSpecies ss =
                    (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(s_id);

            int stoich = link.getStoichiometry();
            String speciesVariable = ss.getVariableName();
            code.append("*" + speciesVariable + "^" + stoich);

        }
        return new ModelicaCode(code.toString());
    }
}
