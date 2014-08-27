package BindingLib;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by tt on 26.08.14.
 */
public class SpManyToOneRelationship extends ManyToOneRelationship{
    Map<QueryType,Integer> order;

    SpManyToOneRelationship(Field field, String columnName, String associatedEntity, Map<QueryType,Integer> order) {
        super(field, columnName, associatedEntity);
        this.order = order;
    }
}