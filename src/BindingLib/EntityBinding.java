package BindingLib;

import org.intellij.lang.annotations.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EntityBinding {
    private Class entity;
    //TODO: Есть возможность использовать только одно поле
    private String tableName;
    private String packageName;
    private List<PropertyBinding> properties;
    private EntityBindingType bindingType;
    private PropertyBinding identifier;

    public EntityBinding(Class entity, String tableOrPackageName, String idColumn, EntityBindingType bindingType) {
        this.entity = entity;
        this.bindingType = bindingType;
        this.properties = new ArrayList<>();
        try {
            this.identifier = new PropertyBinding(entity.getMethod("get" + idColumn), idColumn);
        }
        catch (NoSuchMethodException ex) {
            System.out.println(ex);
        }
        this.properties.add(this.identifier);
        switch (bindingType) {
            case Table:
                this.tableName = tableOrPackageName;
                break;
            case StoredProcedure:
                this.packageName = tableOrPackageName;
                break;
        }
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
}

