package ModelicaSimulableModel;

import Model.Species;
import SimulableModel.SimulableSpecies;

public class ModelicaSimulableSpecies extends SimulableSpecies {

    public ModelicaSimulableSpecies(Species s) {
        super(s);
    }


    @Override
    public ModelicaExpression getODE_RHS() {
        return null;
    }
}
