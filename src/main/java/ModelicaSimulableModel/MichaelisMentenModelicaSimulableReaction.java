package ModelicaSimulableModel;

import DataTypes.*;
import Model.*;
import SimulableModel.*;
import SimulableModel.PreconditionsException;

import java.util.HashSet;
import java.util.Set;

public class MichaelisMentenModelicaSimulableReaction extends ModelicaSimulableReaction {


    public MichaelisMentenModelicaSimulableReaction(Reaction reaction, SimulableModel model) throws PreconditionsException {
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
        Set<LinkTypeModifier> modifierLinks = this.getReactionInstantiate().getModifiers();
        Set<LinkTypeReactant> reactantLinks = this.getReactionInstantiate().getReactants();
        StringBuilder code = new StringBuilder();

        StringBuilder enzymes = new StringBuilder();
        for (LinkTypeModifier modifierLink: modifierLinks){
            Species modifier = modifierLink.getSpecies();
            ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(modifier.getId());
            String modifierVariable = simulableSpecies.getVariableName();
            enzymes.append(modifierVariable+"*");
        }
        enzymes = enzymes.deleteCharAt(enzymes.length()-1);

        StringBuilder substrates = new StringBuilder();
        for (LinkTypeReactant reactantLink: reactantLinks){
            Species reactant = reactantLink.getSpecies();
            ModelicaSimulableSpecies simulableSpecies = (ModelicaSimulableSpecies) this.getLinkSimulableReactionComprises().getSimulableModel().getSimulableSpecies(reactant.getId());
            String reactantVariable = simulableSpecies.getVariableName();
            substrates.append(reactantVariable+"*");
        }
        substrates = substrates.deleteCharAt(substrates.length()-1);


        code.append("("+this.getCatalystConstantName()+"*"+enzymes+"*"+substrates+ ")/" +
                "("+this.getMichaelisConstantName()+"+"+substrates+")");

        return new ModelicaCode(code.toString());
    }

    public String getMichaelisConstantName(){
        return this.getReactionInstantiate().getId() + "_Km";
    }

    public String getCatalystConstantName(){
        return this.getReactionInstantiate().getId() + "_Kcat";
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
