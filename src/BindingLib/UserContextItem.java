package BindingLib;

/**
 *
 */
public class UserContextItem {
    private final String name;
    private final Object value;

    public UserContextItem(String name, Object value) {

        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
