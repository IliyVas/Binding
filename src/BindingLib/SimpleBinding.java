package BindingLib;

import javassist.CtClass;
import javassist.CtField;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by tt on 21.08.14.
 */
abstract class SimpleBinding extends EntityBinding {
    private String tableName;
    public SimpleBinding(Class entity, CtClass CtEntityClass) {
        super(entity);
        throw new NotImplementedException();
    }
    public String getTableName() {
        return tableName;
    }

}
