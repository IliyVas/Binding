package BindingLib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Field;
import java.util.*;

/**
 *
 */
class SpPropertyBinding extends PropertyBinding implements SpEntityFieldAssociatedWithColumn {
    Map<QueryType, Integer> order;

    SpPropertyBinding(Field field, String columnName, Map<QueryType, Integer> order) {
        super(field, columnName);
        this.order = new HashMap<>();
        this.order.put(QueryType.insert, order.get(QueryType.insert));
        this.order.put(QueryType.update, order.get(QueryType.update));
    }

    @Override
    public int getOrder(QueryType queryType) {
        return order.get(queryType);
    }

}
