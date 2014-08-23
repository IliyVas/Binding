package BindingLib;

import Annotations.*;
import Exceptions.BadOrderValueException;
import Exceptions.MultipleOrderAnnotationsException;
import Exceptions.NoOrderAnnotationException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tt on 21.08.14.
 */
class StoredProcedureBinding extends EntityBinding {
    SpPropertyBinding identifier;
    List<SpPropertyBinding> propertyBindings;

    StoredProcedureBinding(Class entity) throws NotFoundException{
        this(
                entity,
                ClassPool.getDefault().getCtClass(entity.getCanonicalName())
        );
    }
    StoredProcedureBinding(Class entity, CtClass entityCtClass) {
        super(entity, entityCtClass);
        this.propertyBindings = new ArrayList<>();

        CtField field;
        String columnName;
        Map<QueryType, Integer> order = new HashMap<>();

        CtField[] fields = entityCtClass.getDeclaredFields();
        boolean[] usedInsertPositions = new boolean[fields.length];
        boolean[] usedUpdatePositions = new boolean[fields.length];
        byte length = 0; //Число полей, помеченных @Column
        OrderingType orderingType = OrderingType.unknown;


        for (int i = 0; i < fields.length; i++ ) {

            field = fields[i];
            order.clear();

            if (field.hasAnnotation(Order.class)) {
                try {
                    Order orderAnnotation = (Order)field.getAnnotation(Order.class);
                    byte value = orderAnnotation.value();

                    if (
                            value > fields.length ||
                            value <= 0 ||
                            usedUpdatePositions[value - 1] == true ||
                            usedInsertPositions[value - 1] == true ||
                            field.hasAnnotation(Id.class) ||
                            (orderingType != OrderingType.general && orderingType != OrderingType.unknown)
                    )
                    throw new BadOrderValueException();

                    if (field.hasAnnotation(OrderInInsert.class) || field.hasAnnotation(OrderInUpdate.class))
                        throw new MultipleOrderAnnotationsException();

                    usedUpdatePositions[value - 1 ] = true;
                    usedInsertPositions[value - 1 ] = true;
                    order.put(QueryType.update, (byte)(value + 1));
                    order.put(QueryType.insert, value);
                    propertyBindings.add(new SpPropertyBinding(field, columnName, order));
                    if (orderingType == OrderingType.unknown) orderingType = OrderingType.general;


                }
                catch (ClassNotFoundException e){}
            }
            else if (field.hasAnnotation(OrderInInsert.class) || field.hasAnnotation(OrderInUpdate.class)) {
                try {
                    byte orderInInsert = ((OrderInInsert)field.getAnnotation(OrderInInsert.class)).value();
                    byte orderInUpdate = ((OrderInUpdate)field.getAnnotation(OrderInUpdate.class)).value();

                    if (
                            orderInInsert > fields.length ||
                            orderInUpdate > fields.length ||
                            orderInInsert <= 0 ||
                            orderInUpdate <= 0 ||
                            usedInsertPositions[orderInInsert - 1] == true ||
                            usedUpdatePositions[orderInUpdate - 1] == true ||
                            (orderingType != OrderingType.custom && i != 0)
                    )
                    throw new BadOrderValueException();

                    usedInsertPositions[orderInInsert - 1] = true;
                    usedUpdatePositions[orderInUpdate - 1] = true;
                    order.put(QueryType.update, orderInUpdate);
                    order.put(QueryType.insert, orderInInsert);
                    propertyBindings.add(new SpPropertyBinding(field, columnName, order));
                }
                catch (ClassNotFoundException e) {
                    throw new BadOrderValueException();
                }
            }
            else {
                if (
                        orderingType != OrderingType.defaultOrder &&
                        (length > 1 ||  orderingType != OrderingType.unknown)
                )
                throw new NoOrderAnnotationException();
                ё
                if (!field.hasAnnotation(Id.class)) {
                    order.put(QueryType.insert, length + 1);
                    order.put(QueryType.update, length + 2);
                    if (orderingType != OrderingType.defaultOrder) orderingType = OrderingType.defaultOrder;

                    propertyBindings.add(field, columnName, order);
                    length++;

                }
            }

            if (field.hasAnnotation(Column.class)) {
                columnName = ((Column)field.getAnnotation(Column.class)).name();

                extendingGetterAndSetterMethods(field, entityCtClass);

                SpPropertyBinding property = new SpPropertyBinding(field, columnName);
                this.properties.add(property);

                if (field.isAnnotationPresent(Id.class)) {
                    this.identifier = property;
                }
                //TODO: переопределить hashCode
            }
        }
    }

    SpPropertyBinding getIdentifier() { return identifier; }

    enum OrderingType {
        //В insert и update одинаковый порядок параметров за исключением того, что в update первый параметр - id, а в
        //insert он отсутствует. Указывается с помощью аннотации @order для всех полей кроме id
        general,
        //Порядок параметров указывается отдельно для insert и update с помощью аннотаций @OrderInInsert и
        //@OrderInUpdate соответственно
        custom,
        //Порядок равен порядку определения полей в классе
        defaultOrder,
        unknown
    }

}
