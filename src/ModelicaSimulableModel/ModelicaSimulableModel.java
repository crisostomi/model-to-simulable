package ModelicaSimulableModel;

import Model.Model;
import SimulableModel.SimulableModel;

public abstract class ModelicaSimulableModel extends SimulableModel {

    public ModelicaSimulableModel(Model model) {
        super(model);
    }

    @Override
    public String getModules() {
        return null;
    }

    public abstract String getReactionsCode();

    public abstract String getModuleCode();

    public String getParameters(){
        return null;
    }
}
