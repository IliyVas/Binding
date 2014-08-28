package BindingLib;

import java.lang.reflect.Field;

/**
 * Created by tt on 28.08.14.
 */
public interface EntityFieldAssociatedWithColumn extends EntityField {
    public String getColumnName();
}
