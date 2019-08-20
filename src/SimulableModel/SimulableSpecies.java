package SimulableModel;

import DataTypes.Expression;
import Model.Species;

public abstract class SimulableSpecies {

    public SimulableSpecies(Species species){

    }

    public abstract Expression getODE_RHS();
}
