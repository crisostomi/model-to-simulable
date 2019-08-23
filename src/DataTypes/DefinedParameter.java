package DataTypes;

public class DefinedParameter extends Parameter {
    private final Double value;

    public DefinedParameter(String name, Double value) {
        super(name);
        this.value = value;
    }

    public Double getValue() {
        return value;
    }
}
