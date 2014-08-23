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
    private List<Relationship> relationships;
    private Set connectedEntities;
    //TODO: Возможно нужно оставить только одного "слушателя"
    private List<AttemptToGetUnloadedFieldListener> listeners;

    //TODO: Если возможно, то сделать перечислением
    // stateField values:
    //   0 - new
    //   1 - upToDate
    //   2 - changed
    //   3 - lazyLoaded

    private CtField stateField;
    private CtField entityBindingField;

    public EntityBinding(Class entity, CtClass entityCtClass) {

        //TODO: возможно entity нужно определять после вызова toClass
        this.entity = entity;
        this.listeners = new ArrayList<>();

        try {
            stateField = new CtField(
                    CtClass.byteType,
                    "state" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("mmnnnnnnnnnss")),
                    entityCtClass);
            entityCtClass.addField(stateField);

            entityBindingField = CtField.make(
                    "EntityBinding eb" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("mmnnnnnnnnnss;")),
                    entityCtClass);
            entityCtClass.addField(entityBindingField);


        }
        catch (NotFoundException | CannotCompileException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        this.relationships.l

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

    protected void extendingGetterAndSetterMethods(CtField field, CtClass entityCtClass) {

        CtMethod getter, setter;
        String getterName, setterName, columnName;

        try {

            //TODO: добавить is для boolean
            getterName = "get" +
                    field.getName().substring(0, 1).toUpperCase() +
                    field.getName().substring(1);

            getter = entityCtClass.getDeclaredMethod(getterName);

            //TODO: рассмотреть другие подходы
            if (getter.getReturnType().getName() != field.getType().getName())
                throw new NotFoundException("Method not found");

            getter.insertBefore(
                    "if(" + stateField.getName() + "==3){" +
                            entityBindingField.getName() + ".fireGettingUloadedFieldEvent(this);" +
                            stateField.getName() + "=1; }"
            );


            setterName = "set" +
                    field.getName().substring(0, 1).toUpperCase() +
                    field.getName().substring(1);

            setter = entityCtClass.getDeclaredMethod(setterName);

            if (setter.getParameterTypes().length != 1 ||
                    setter.getParameterTypes()[0].getName() != field.getType().getName())
                throw new NotFoundException("Method not found");

            //TODO: Добавить проверку на изменение параметра
            setter.insertBefore(
                    stateField.getName() + "=\"changed\";"
            );
        }
        catch (NotFoundException | CannotCompileException ex) {
            ex.printStackTrace();
        }
    }

    public void addGettingUnloadedFieldLListener(AttemptToGetUnloadedFieldListener listener) {
        this.listeners.add(listener);
    }

    public void fireGettingUnloadedFieldEvent(Object obj) {
        for (AttemptToGetUnloadedFieldListener listener : listeners) {
            listener.loadObject(this, obj);
        }
    }

    CtClass getEntityCtClass() {
        return this.entityClass;
    }

    Class getEntityClass() { return this.entityClass.toClass(); }

    CtField getIdCtField() { return this.i}

    public String getTableName() {
        return tableName;
    }

    public List<PropertyBinding> getProperties() {
        return properties;
    }

    public List<Relationship> getRelationships() { return relationships; }

    public EntityBindingType getBindingType() {
        return bindingType;
    }

    public String getPackageName() {
        return packageName;
    }

    abstract PropertyBinding getIdentifier();

    public void setEntity(Class entity) {
        this.entity = entity;
    }

    public PropertyBinding getPropertyBinding(String fieldName) {
        return propertyBindingMap.get(fieldName);
    }

    private boolean isMethodExist(String methodName) {
        for (CtMethod method : entityClass.getDeclaredMethods())  {
            System.out.println(method.getName());
            if (method.getName() == methodName) return true;
        }

        return false;
    }
}

