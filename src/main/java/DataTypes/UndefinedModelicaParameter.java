package DataTypes;

public class UndefinedModelicaParameter extends ModelicaParameter {
    private final double lowerBound;
    private final double upperBound;

    public UndefinedModelicaParameter(String name, double lb, double ub) {
        super(name);
        this.lowerBound = lb;
        this.upperBound = ub;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }
}
