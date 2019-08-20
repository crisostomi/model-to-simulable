package DataTypes;

public class ModelicaCode implements Constraint, Expression, Module {
    private String code;

    public ModelicaCode(String code) {
        this.code = code;
    }
}
