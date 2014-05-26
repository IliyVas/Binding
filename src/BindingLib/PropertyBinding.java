package BindingLib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public class PropertyBinding {
    private Method getter;
    private String columnName;

    public PropertyBinding(Method getter, String columnName) {

        this.getter = getter;
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getFieldValue(Object obj) {
        try {
            return getter.invoke(obj, new Object[]{ null }).toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //TODO: Или лучше бросать исключение
        return null;
    }
}
