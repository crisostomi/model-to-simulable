package DataTypes;

public class ModelicaCode implements Constraint, Expression, Module {
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String code;

    public ModelicaCode(String code) {
        this.code = code;
    }
}
