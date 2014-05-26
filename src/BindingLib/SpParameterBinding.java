package BindingLib;

/**
 *
 */
public class SpParameterBinding {
    private String parameterName;
    private Object value;

    public SpParameterBinding(String parameterName, Object value) {
        this.parameterName = parameterName;
        this.value = value;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getValue() {
        return value;
    }
}
