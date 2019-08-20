package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import DataTypes.PreconditionsException;
import Model.LinkTypeProduct;
import Model.LinkTypeReactant;
import Model.Reaction;
import Model.Species;

import java.util.Set;

public class ModelicaSimulableMassActionReaction extends ModelicaSimulableReaction{
    public ModelicaSimulableMassActionReaction(Reaction reaction) {
        super(reaction);
    }

    @Override
    public String getParameters() {
        StringBuilder parameters = new StringBuilder();
        parameters.append("<reaction ");
        Reaction reaction = this.getReactionInstantiate();
        String r_id = reaction.getId();
        double minRateConstant = reaction.getRate().getLowerBound();
        double maxRateConstant = reaction.getRate().getUpperBound();

        parameters.append("id = "+r_id);
        parameters.append("minRateConstant = " + minRateConstant);
        parameters.append("maxRateConstant = " + maxRateConstant);

        if (reaction.isReversible()){
            try{
                double minRateInvConstant = reaction.getRateInv().getLowerBound();
                double maxRateInvConstant = reaction.getRateInv().getUpperBound();
                parameters.append("minRateInvConstant = "+minRateInvConstant);
                parameters.append("maxRateInvConstant = "+maxRateInvConstant);
            }
            catch (PreconditionsException e) {}

        }
        parameters.append(">");
        return parameters.toString();
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
