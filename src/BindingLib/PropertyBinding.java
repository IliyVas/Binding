package BindingLib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public class PropertyBinding {
    private Field field;
    private String columnName;

    public PropertyBinding(Method getter, Method setter, String columnName, String fieldName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getFieldValue(Object obj) throws InvocationTargetException, IllegalAccessException {
        return getter.invoke(obj, new Object[]{ null }).toString();
    }

    public void setFieldValue(Object obj, Object value) throws InvocationTargetException, IllegalAccessException {
        setter.invoke(obj, new Object[]{value});
    }

    public String getFieldName() {
        return fieldName;
    }
}
