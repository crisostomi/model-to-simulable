package DataTypes;

public class ModelicaCode implements Constraint, Expression, Module {

    private String code;

    public ModelicaCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isEmpty() {
        return this.code.isEmpty();
    }
}
