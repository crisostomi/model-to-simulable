package DataTypes;

public abstract class Parameter {
    private final String parameterName;

    public Parameter(String name) {
        this.parameterName = name;
    }

    public String getParameterName() {
        return parameterName;
    }
}
