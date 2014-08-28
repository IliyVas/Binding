package BindingLib;

import java.lang.reflect.Field;

/**
 * Created by tt on 19.08.14.
 */

//TODO: пересмотреть
class Relationship implements EntityField{
    private Class associatedEntity;
    private Field field;
    private Field isDependenciesLoadedField;

    Relationship(Field field, String associatedEntity) {
        try {
            this.associatedEntity = Class.forName(associatedEntity);
            this.field = field;
            this.field.setAccessible(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Field getField() { return field; }

    public Field getIsDependenciesLoadedField() {
        return isDependenciesLoadedField;
    }

    public void setIsDependenciesLoadedField(Field isDependenciesLoaded) {
        this.isDependenciesLoadedField = isDependenciesLoaded;
    }

    public Class getAssociatedEntity() {
        return associatedEntity;
    }
}
