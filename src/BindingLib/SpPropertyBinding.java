package BindingLib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
class SpPropertyBinding extends PropertyBinding {
    Map<QueryType, Integer> order;

    SpPropertyBinding(Field field, String columnName, Map<QueryType, Integer> order) {
        super(field, columnName);
        this.order = order;
    }

}
