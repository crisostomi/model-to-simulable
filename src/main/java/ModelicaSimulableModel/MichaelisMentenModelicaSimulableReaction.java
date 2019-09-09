package ModelicaSimulableModel;

import DataTypes.*;
import Model.*;
import Model.LinkType.LinkTypeModifier;
import Model.LinkType.LinkTypeProduct;
import Model.LinkType.LinkTypeReactant;
import SimulableModel.*;
import SimulableModel.Link.LinkSimulableSpeciesComprises;

import java.util.HashSet;
import java.util.Set;

public class MichaelisMentenModelicaSimulableReaction extends ModelicaSimulableReaction {

    public MichaelisMentenModelicaSimulableReaction(Reaction reaction, SimulableModel model) throws PreconditionsException {
        super(reaction);

        Set<Species> speciesInvolved = new HashSet<>();
        for (LinkTypeReactant l: reaction.getLinkReactantSet()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (LinkTypeProduct l: reaction.getLinkProductSet()) {
            Species s = l.getSpecies();
            speciesInvolved.add(s);
        }

        for (LinkTypeModifier l: reaction.getLinkModifierSet()) {
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
        Reaction reaction = this.getReactionInstantiate();
        Set<Species> modifiers = reaction.getModifiers();
        Set<Species> reactants = reaction.getReactants();
        StringBuilder code = new StringBuilder();

        // prodotto dei substrati
        StringBuilder substrates = new StringBuilder();
        for (Species reactant: reactants){
            ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(reactant.getId());
            String reactantVariable = simulableSpecies.getVariableName();
            substrates.append(reactantVariable+"*");
        }
        substrates = substrates.deleteCharAt(substrates.length()-1);

        if ( reaction.getRateParameters().get(RateParameter.Vmax).getLowerBound() != 0 ){
            code.append("("+this.getSaturationConstantName()+"*"+substrates+ ")/" +
                    "("+this.getMichaelisConstantName()+"+"+substrates+")");
        }
        else{
            // prodotto degli enzimi
            StringBuilder enzymes = new StringBuilder();
            for (Species modifier: modifiers){
                ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(modifier.getId());
                String modifierVariable = simulableSpecies.getVariableName();
                enzymes.append(modifierVariable+"*");
            }
            enzymes = enzymes.deleteCharAt(enzymes.length()-1);
            code.append("("+this.getCatalystConstantName()+"*"+enzymes+"*"+substrates+ ")/" +
                    "("+this.getMichaelisConstantName()+"+"+substrates+")");
        }
        return new ModelicaCode(code.toString());
    }

    public String getMichaelisConstantName(){
        return this.getReactionInstantiate().getId() + "_Km";
    }

    public String getCatalystConstantName(){
        return this.getReactionInstantiate().getId() + "_Kcat";
    }

    public String getSaturationConstantName(){
        return this.getReactionInstantiate().getId() + "_Vmax";
    }

    public ModelicaParameter getMichaelisParameter() {
        String michaelisName = this.getMichaelisConstantName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate(RateParameter.Km).getLowerBound() == r.getRate(RateParameter.Km).getUpperBound()) {
            return new DefinedModelicaParameter(michaelisName, r.getRate(RateParameter.Km).getLowerBound());
        } else {
            return new UndefinedModelicaParameter(michaelisName, r.getRate(RateParameter.Km).getLowerBound(), r.getRate(RateParameter.Km).getUpperBound());
        }
    }

    public ModelicaParameter getCatalystParameter(){
        String catalystName = this.getCatalystConstantName();
        Reaction r = this.getReactionInstantiate();

        if (r.getRate(RateParameter.Kcat).getLowerBound() == r.getRate(RateParameter.Kcat).getUpperBound()) {
            return new DefinedModelicaParameter(catalystName, r.getRate(RateParameter.Kcat).getLowerBound());
        } else {
            return new UndefinedModelicaParameter(catalystName, r.getRate(RateParameter.Kcat).getLowerBound(), r.getRate(RateParameter.Kcat).getUpperBound());
        }


    }

    @Override
    public ModelicaCode getRateInvFormula() {
        return null;
    }
}
