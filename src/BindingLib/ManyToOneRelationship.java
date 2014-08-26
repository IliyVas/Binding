package BindingLib;

import java.lang.reflect.Field;

/**
 * Created by tt on 24.08.14.
 */
class ManyToOneRelationship extends Relationship implements EntityField {
    private String fkColumnName;

    ManyToOneRelationship(Field field, String fkColumnName, String associatedEntity) {
        super(field, associatedEntity);
        this.fkColumnName = fkColumnName;
    }

    public String getColumnName() {
        return fkColumnName;
    }
    public Field getField() { return super.getField(); }
}
