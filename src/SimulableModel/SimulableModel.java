package SimulableModel;

import Model.*;

public abstract class SimulableModel<T extends SimulableReaction> {

    public SimulableModel(Model model){
    }

    public abstract String getModules();

    public SimulableSpecies getSimulableSpecies(String id){
        return null;
    }

    public SimulableReaction getSimulableReaction(String id){
        return null;
    }
}
