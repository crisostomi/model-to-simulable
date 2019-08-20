package ModelicaSimulableModel;

import DataTypes.ModelicaExpression;
import Model.Species;
import DataTypes.Expression;
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
