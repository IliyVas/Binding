package BindingLib;

import java.lang.reflect.Field;

/**
 * Created by tt on 24.08.14.
 */
public class OneToManyRelationship extends Relationship {
    private String associatedField;
    OneToManyRelationship(Field field, String associatedEntity, String associatedField) {
        super(field, associatedEntity);
        this.associatedField = associatedField;
    }

    public String getAssociatedField() {
        return associatedField;
    }

}
