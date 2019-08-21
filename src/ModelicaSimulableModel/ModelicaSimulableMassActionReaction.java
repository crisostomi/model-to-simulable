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


public class ModelicaSimulableMassActionReaction extends ModelicaSimulableReaction{
    public ModelicaSimulableMassActionReaction(Reaction reaction, SimulableModel model) throws PreconditionsException {
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
    public Parameter getParameters() {
        Reaction reaction = this.getReactionInstantiate();
        String r_id = reaction.getId();

        Parameter p = new Parameter("reaction", r_id);
        Double minRateConstant = reaction.getRate().getLowerBound();
        Double maxRateConstant = reaction.getRate().getUpperBound();

        p.addProperty("minRateConstant", minRateConstant.toString());
        p.addProperty("maxRateConstant", maxRateConstant.toString());

        if (reaction.isReversible()){
            try{
                Double minRateInvConstant = reaction.getRateInv().getLowerBound();
                Double maxRateInvConstant = reaction.getRateInv().getUpperBound();
                p.addProperty("minRateInvConstant", minRateInvConstant.toString());
                p.addProperty("maxRateInvConstant", maxRateInvConstant.toString());
            }
            catch (DataTypes.PreconditionsException e) {}
        }

        return p;
    }

    @Override
    public ModelicaCode getRateFormula() {
        Set<LinkTypeReactant> reactantLinks = this.getReactionInstantiate().getReactants();
        String r_id = this.getReactionInstantiate().getId();
        StringBuilder code = new StringBuilder(r_id+"_rateConstant");
        for (LinkTypeReactant link:reactantLinks){
            Species species = link.getSpecies();
            String s_id = species.getId();
            int stoich = link.getStoichiometry();
            code.append("*"+s_id+"^"+stoich);
        }
        return new ModelicaCode(code.toString());
    }

    @Override
    public ModelicaCode getRateInvFormula() {
        Set<LinkTypeProduct> productLinks = this.getReactionInstantiate().getProducts();
        String r_id = this.getReactionInstantiate().getId();
        StringBuilder code = new StringBuilder(r_id+"_rateInvConstant");
        for (LinkTypeProduct link:productLinks){
            Species species = link.getSpecies();
            String s_id = species.getId();
            int stoich = link.getStoichiometry();
            code.append("*"+s_id+"^"+stoich);
        }
        return new ModelicaCode(code.toString());
    }
}
