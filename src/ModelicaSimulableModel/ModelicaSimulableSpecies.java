package ModelicaSimulableModel;

import DataTypes.ModelicaCode;
import Model.Species;
import SimulableModel.SimulableSpecies;

public class ModelicaSimulableSpecies extends SimulableSpecies {

    public ModelicaSimulableSpecies(Species s) {
        super(s);
    }

    @Override
    public ModelicaCode getODE_RHS() {
        return null;
    }
}
