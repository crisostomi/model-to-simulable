package SimulableModel;

import java.util.Objects;

public class LinkTypeSimulableSpeciesComprises {
    private SimulableModel simulableModel;
    private SimulableSpecies simulableSpecies;

    public SimulableModel getSimulableModel() {
        return simulableModel;
    }

    public SimulableSpecies getSimulableSpecies() {
        return simulableSpecies;
    }

    public LinkTypeSimulableSpeciesComprises(SimulableModel m, SimulableSpecies r) {
        this.simulableModel = m;
        this.simulableSpecies = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkTypeSimulableSpeciesComprises that = (LinkTypeSimulableSpeciesComprises) o;
        return simulableModel == that.simulableModel &&
                simulableSpecies == that.simulableSpecies;
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulableModel, simulableSpecies);
    }

}
