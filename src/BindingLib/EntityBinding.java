package BindingLib;

import Annotations.Column;
import Annotations.Id;
import Annotations.Table;
import javassist.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public abstract class EntityBinding {

    private Class entity;
    private PropertyBinding identifier;
    private List<Relationship> relationships;
    private List<EntityField> fields;
    //TODO: Возможно нужно оставить только одного "слушателя"
    private List<AttemptToGetUnloadedFieldListener> listeners;

    private Field entityBindingField;

    public EntityBinding(Class entity) {

        this.listeners = new ArrayList<>();
        this.relationships = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.entity = entity;
    }

    protected String createEntityBindingField(CtClass entityChild) {
        String entityBindingFieldName = null;
        try {
            CtClass CtEntityBinding = ClassPool.getDefault().getCtClass(EntityBinding.class.getName());
            CtField CtEntityBindingField = new CtField(CtEntityBinding, "eb" + postfix(), entityChild);

            entityChild.addField(CtEntityBindingField);

            entityBindingFieldName = CtEntityBindingField.getName();

        } catch (CannotCompileException | NotFoundException e) {
            e.printStackTrace();
        }

        return entityBindingFieldName;
    }

    protected String extendingGetterAndSetterMethods(Field field,
                                                     CtClass entityChild,
                                                     String entityBindingFieldName,
                                                     Relationship relationship) {

        CtMethod getter, setter;
        CtField stateField;
        String getterName, setterName, returnType, parameterType;
        String stateFieldName = null;

        try {

            CtClass entityCtClass = ClassPool.getDefault().getCtClass(entity.getName());
            //TODO: добавить is для boolean
            getterName = "get" +
                    field.getName().substring(0, 1).toUpperCase() +
                    field.getName().substring(1);

            getter = entityCtClass.getDeclaredMethod(getterName);

            returnType = getter.getReturnType().getName();
            //TODO: рассмотреть другие подходы
            if (!returnType.equals(field.getType().getName()))
                throw new NotFoundException("Method not found");

            stateField = CtField.make("boolean isL" + postfix() + "=false;", entityChild);
            entityChild.addField(stateField);

            getter = CtMethod.make(
                    returnType + " " + getterName + "() {" +
                        "if(" + stateField.getName() + "== false){" +
                                entityBindingFieldName + ".fireGettingUnloadedFieldEvent(this,\"" +
                                relationship.getField().getName() + "\");" +
                                stateField.getName() + "=true; }" +
                        "return super." + getterName + "(); }",
                    entityChild);

            entityChild.addMethod(getter);

            setterName = "set" +
                    field.getName().substring(0, 1).toUpperCase() +
                    field.getName().substring(1);

            setter = entityCtClass.getDeclaredMethod(setterName);
            parameterType = setter.getParameterTypes()[0].getName();

            if (setter.getParameterTypes().length != 1 ||
                    !parameterType.equals(field.getType().getName()))
                throw new NotFoundException("Method not found!!");


            //TODO: Добавить проверку на изменение параметра
            setter = CtMethod.make(
                    "void " + setterName + "(" + parameterType +  " setterParameter) {" +
                        stateField.getName() + "=true;" +
                        "super." + setterName + "(setterParameter); }",
                    entityChild);

            entityChild.addMethod(setter);

            stateFieldName = stateField.getName();

        } catch (NotFoundException | CannotCompileException ex) {
            ex.printStackTrace();
        }

        return stateFieldName;
    }

    public void addGettingUnloadedFieldLListener(AttemptToGetUnloadedFieldListener listener) {
        this.listeners.add(listener);
    }

    public void fireGettingUnloadedFieldEvent(Object obj, String fieldName) {
        Relationship relationship = null;

        for (Relationship rs : relationships) {
            if (rs.getField().getName().equals(fieldName)) {
                relationship = rs;
                break;
            }
        }

        for (AttemptToGetUnloadedFieldListener listener : listeners) {
            listener.loadDependencies(obj, relationship);
        }
    }

    Class getEntityClass() { return this.entity; }


    abstract <T extends EntityFieldAssociatedWithColumn> List<T> getAttributeFields();

    List<Relationship> getRelationships() {
        return relationships;
    }

    List<EntityField> getFieldsProperties() { return fields; }

    PropertyBinding getIdentifier() { return identifier; }




    void addRelationship(Relationship relationship) {
        relationships.add(relationship);
    }

    void addField(EntityField field) { fields.add(field);}

    protected String postfix() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("mmnnnnnnnnnss"));
    }

    void setIdentifier(PropertyBinding identifier) {
        this.identifier = identifier;
    }

    public void setEntity(Class entity) {
        this.entity = entity;
    }

    public void setEntityBindingField(Field entityBindingField) {
        this.entityBindingField = entityBindingField;
        entityBindingField.setAccessible(true);
    }

    public Field getEntityBindingField() {
        return entityBindingField;
    }
}

