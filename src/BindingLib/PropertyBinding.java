package BindingLib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
class PropertyBinding implements EntityFieldAssociatedWithColumn {
    private Field field;
    private String columnName;

    PropertyBinding(Field field, String columnName) {
        this.field = field;
        this.field.setAccessible(true);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Field getField() {
        return field;
    }

    public Object getFieldValue(Object obj) throws InvocationTargetException, IllegalAccessException {
        return field.get(obj);
    }

}
