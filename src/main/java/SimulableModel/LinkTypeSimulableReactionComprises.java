package SimulableModel;

import java.util.Objects;

public class LinkTypeSimulableReactionComprises {
    private SimulableModel simulableModel;
    private SimulableReaction simulableReaction;

    public SimulableModel getSimulableModel() {
        return simulableModel;
    }

    public SimulableReaction getSimulableReaction() {
        return simulableReaction;
    }

    public LinkTypeSimulableReactionComprises(SimulableModel m, SimulableReaction r) {
        this.simulableModel = m;
        this.simulableReaction = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkTypeSimulableReactionComprises that = (LinkTypeSimulableReactionComprises) o;
        return simulableModel == that.simulableModel &&
                simulableReaction == that.simulableReaction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulableModel, simulableReaction);
    }
}
