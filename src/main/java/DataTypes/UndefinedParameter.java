package DataTypes;

public class UndefinedParameter extends Parameter {
    private final double lowerBound;
    private final double upperBound;

    public UndefinedParameter(String name, double lb, double ub) {
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
