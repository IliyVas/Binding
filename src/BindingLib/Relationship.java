package BindingLib;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by tt on 19.08.14.
 */
public class Relationship {
    private RelationType type;
    private String joiningTable;
    private Map<EntityBinding, String> columnName;
    private Map<EntityBinding, Field> field;
    private Map<EntityBinding, EntityBinding> associatedEntity;

    public EntityBinding getAssociatedEntity(EntityBinding entityBinding) {
        return associatedEntity.get(entityBinding);
    }
}
