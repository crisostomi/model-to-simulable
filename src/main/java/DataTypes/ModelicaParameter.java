package DataTypes;

public abstract class ModelicaParameter {
    private final String parameterName;

    public ModelicaParameter(String name) {
        this.parameterName = name;
    }

    public String getParameterName() {
        return parameterName;
    }
}
