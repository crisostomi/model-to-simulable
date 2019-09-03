package DataTypes;

public class DefinedModelicaParameter extends ModelicaParameter {
    private final Double value;

    public DefinedModelicaParameter(String name, Double value) {
        super(name);
        this.value = value;
    }

    public Double getValue() {
        return value;
    }
}
