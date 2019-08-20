package SimulableModel;

import Model.*;

import java.util.Set;

public abstract class SimulableModel<T extends SimulableReaction> {

    public SimulableModel(Model model){
    }

    public abstract Set<Module> getModules();

    public SimulableSpecies getSimulableSpecies(String id){
        return null;
    }

    public SimulableReaction getSimulableReaction(String id){
        return null;
    }
}
