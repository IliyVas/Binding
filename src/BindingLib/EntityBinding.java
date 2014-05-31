package BindingLib;

import Annotations.Column;
import Annotations.Id;
import Annotations.Table;
import com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
//TODO: решить где создавать подключение
//TODO: разобраться с generics
public class EntityBinding<T> {
    private Class<T> entity;
    private String tableName;
    private String packageName;
    private List<PropertyBinding> properties;
    private EntityBindingType bindingType;
    private PropertyBinding identifier;

    public EntityBinding(Class entity, EntityBindingType bindingType) {
        this.entity = entity;
        this.bindingType = bindingType;
        this.properties = new ArrayList<>();
        try {
            Method getter;
            String getterName;
            Method setter;
            String setterName;
            String columnName;
            for (Field field : entity.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    getterName = "get" +
                            field.getName().substring(0, 1).toUpperCase() +
                            field.getName().substring(1);
                    getter = entity.getMethod(getterName);
                    if (getter.getReturnType() != field.getType()) throw new NoSuchMethodException();


                    setterName = "set" +
                            field.getName().substring(0, 1).toUpperCase() +
                            field.getName().substring(1);
                    setter = entity.getMethod(setterName);
                    if (setter.getParameterTypes().length != 1 &&
                            setter.getParameterTypes()[0] != field.getType())
                        throw new NoSuchMethodException();

                    columnName = field.getAnnotation(Column.class).name();

                    PropertyBinding property = new PropertyBinding(getter, setter, columnName, field.getName());
                    this.properties.add(property);

                    if (field.isAnnotationPresent(Id.class)) {
                        this.identifier = property;
                    }
                }
            }
        }
        catch (NoSuchMethodException ex) {
            System.out.println(ex);
        }

        this.properties.add(this.identifier);

        if(this.entity.isAnnotationPresent(Table.class)){
            switch (bindingType) {
                case Table:
                    this.tableName = this.entity.getAnnotation(Table.class).name();
                    break;
                case StoredProcedure:
                    this.packageName = this.entity.getAnnotation(Table.class).name();
                    break;
            }
        }
        //TODO: дописать исключение
        else {}


    }

    public Class getEntity() {
        return entity;
    }

    public String getTableName() {
        return tableName;
    }

    public List<PropertyBinding> getProperties() {
        return properties;
    }

    public EntityBindingType getBindingType() {
        return bindingType;
    }

    public String getPackageName() {
        return packageName;
    }

    public PropertyBinding getIdentifier(){ return this.identifier; }

    public void setEntity(Class entity) {
        this.entity = entity;
    }

    //Не слишком ли много исключений?
    public T getAll() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Class c = Class.forName(entity.getName());
        T entity = (T)c.newInstance();
        this.identifier.setFieldValue(entity, 1);
        return entity;
    }

}

