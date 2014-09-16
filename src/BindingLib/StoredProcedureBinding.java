package BindingLib;

import Annotations.*;
import Exceptions.*;
import javassist.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 */
//TODO: удалить лишние методы и поля из класса
public class StoredProcedureBinding extends EntityBinding {
    private Map<QueryType, String> proceduresNames;
    private List<SpEntityFieldAssociatedWithColumn> attributeFields;

    public StoredProcedureBinding(Class entity) {

        super(entity);
        this.attributeFields = new ArrayList<>();
        this.proceduresNames = new HashMap<>();

        try {

            CtClass ctWrapperClass = createWrapperClass();
            ctWrapperClass.setSuperclass(ClassPool.getDefault().getCtClass(entity.getName()));

            if (entity.isAnnotationPresent(ProceduresNamesPattern.class)) {

                String pattern = ((ProceduresNamesPattern) entity.getAnnotation(ProceduresNamesPattern.class)).value();

                if (pattern.equals("")) throw new BadAnnotationValueException();

                if (pattern.contains("<prefix>")) {

                    String selectPrefix = "get";
                    String updatePrefix = "update";
                    String insertPrefix = "insert";
                    String deletePrefix = "delete";

                    if (entity.isAnnotationPresent(ProceduresPrefixes.class)) {

                        ProceduresPrefixes prefixes =
                                (ProceduresPrefixes) entity.getAnnotation(ProceduresPrefixes.class);
                        selectPrefix = prefixes.select();
                        updatePrefix = prefixes.update();
                        insertPrefix = prefixes.insert();
                        deletePrefix = prefixes.delete();

                    }

                    proceduresNames.put(QueryType.select, pattern.replace("<prefix>", selectPrefix));
                    proceduresNames.put(QueryType.update, pattern.replace("<prefix>", updatePrefix));
                    proceduresNames.put(QueryType.insert, pattern.replace("<prefix>", insertPrefix));
                    proceduresNames.put(QueryType.delete, pattern.replace("<prefix>", deletePrefix));
                }
            }

            if (entity.isAnnotationPresent(SelectProcedureName.class)) proceduresNames.put(QueryType.select,
                    ((SelectProcedureName) entity.getAnnotation(SelectProcedureName.class)).value());
            if (entity.isAnnotationPresent(InsertProcedureName.class)) proceduresNames.put(QueryType.insert,
                    ((InsertProcedureName) entity.getAnnotation(InsertProcedureName.class)).value());
            if (entity.isAnnotationPresent(UpdateProcedureName.class)) proceduresNames.put(QueryType.update,
                    ((UpdateProcedureName) entity.getAnnotation(UpdateProcedureName.class)).value());
            if (entity.isAnnotationPresent(DeleteProcedureName.class)) proceduresNames.put(QueryType.delete,
                    ((DeleteProcedureName) entity.getAnnotation(DeleteProcedureName.class)).value());

            Field field;
            Map<QueryType, Integer> order = new HashMap<>();
            Map<Relationship, String> stateFieldsNames = new HashMap<>();

            Field[] fields = entity.getDeclaredFields();
            Method[] methods = entity.getMethods();
            boolean[] usedInsertPositions = new boolean[fields.length];
            boolean[] usedUpdatePositions = new boolean[fields.length];
            int length = 0; //Число полей, помеченных @Column или @ManyToOne
            OrderingType orderingType = OrderingType.unknown;

            for (int i = 0; i < fields.length; i++) {

                field = fields[i];
                order.clear();

                if (field.isAnnotationPresent(Order.class)) {

                    Order orderAnnotation = field.getAnnotation(Order.class);
                    int value = orderAnnotation.value();

                    if (
                            value > fields.length ||
                            value <= 0 ||
                            usedUpdatePositions[value - 1] == true ||
                            usedInsertPositions[value - 1] == true ||
                            field.isAnnotationPresent(Id.class) ||
                            field.isAnnotationPresent(OneToMany.class) ||
                            (orderingType != OrderingType.general && orderingType != OrderingType.unknown)
                    )
                    throw new BadOrderValueException();

                    if (field.isAnnotationPresent(OrderInInsert.class) ||
                        field.isAnnotationPresent(OrderInUpdate.class))
                            throw new MultipleOrderAnnotationsException();

                    usedUpdatePositions[value - 1] = true;
                    usedInsertPositions[value - 1] = true;
                    order.put(QueryType.update, value);
                    order.put(QueryType.insert, value);
                    if (orderingType == OrderingType.unknown) orderingType = OrderingType.general;

                } else if (field.isAnnotationPresent(OrderInUpdate.class) || field.isAnnotationPresent(OrderInInsert.class)) {

                    //TODO: проверить, что существуют обе аннотации
                    int orderInInsert = field.getAnnotation(OrderInInsert.class).value();
                    int orderInUpdate = field.getAnnotation(OrderInUpdate.class).value();

                    if (
                            orderInInsert > fields.length ||
                            orderInUpdate > fields.length ||
                            orderInInsert <= 0 ||
                            orderInUpdate <= 0 ||
                            usedInsertPositions[orderInInsert - 1] == true ||
                            usedUpdatePositions[orderInUpdate - 1] == true ||
                            field.isAnnotationPresent(OneToMany.class) ||
                            (orderingType != OrderingType.custom && length != 0)
                    )
                    throw new BadOrderValueException();

                    usedInsertPositions[orderInInsert - 1] = true;
                    usedUpdatePositions[orderInUpdate - 1] = true;
                    order.put(QueryType.update, orderInUpdate);
                    order.put(QueryType.insert, orderInInsert);
    
                    if (orderingType != OrderingType.custom) orderingType = OrderingType.custom;

                } else {

                    if (orderingType != OrderingType.defaultOrder &&
                       (length > 1 || orderingType != OrderingType.unknown))
                        throw new BadOrderValueException();


                    if (orderingType != OrderingType.defaultOrder) orderingType = OrderingType.defaultOrder;

                    order.put(QueryType.update, length + 1);
                    order.put(QueryType.insert, length + 1);
                }

                if (field.isAnnotationPresent(Column.class)) {

                    if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class))
                        throw new MultipleColumnTypeAnnotationsException();

                    Column columnAnnotation = field.getAnnotation(Column.class);
                    SpPropertyBinding property = new SpPropertyBinding(field, columnAnnotation.name(), order);
                    attributeFields.add(property);
                    addField(property);

                    if (field.isAnnotationPresent(Id.class)) setIdentifier(property);

                } else if (field.isAnnotationPresent(ManyToOne.class)) {
                    if (field.isAnnotationPresent(OneToMany.class))
                        throw new MultipleColumnTypeAnnotationsException();

                    ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);

                    associatedEntity = manyToOneAnnotation.associatedEntity();

                    SpManyToOneRelationship newRelationship =
                            new SpManyToOneRelationship(field,
                                    manyToOneAnnotation.fkColumnName(), associatedEntity, order);

                    addRelationship(newRelationship);
                    attributeFields.add(newRelationship);
                    addField(newRelationship);

                    stateFieldsNames.put(newRelationship,
                            extendingGetterAndSetterMethods(field,ctWrapperClass,entityBindingFieldName,newRelationship));

                } else if (field.isAnnotationPresent(OneToMany.class)) {

                    OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);

                    OneToManyRelationship newRelationship = new OneToManyRelationship(field,
                            oneToManyAnnotation.associatedEntity(),
                            oneToManyAnnotation.associatedField());

                    addRelationship(newRelationship);
                    addField(newRelationship);

                    stateFieldsNames.put(newRelationship,
                            extendingGetterAndSetterMethods(field,ctWrapperClass,entityBindingFieldName,newRelationship));

                } else length--;

                length++;
            }


            setEntity(ctWrapperClass.toClass());
            setEntityBindingField(getEntityClass().getDeclaredField(entityBindingFieldName));

            for (Relationship relationship : getRelationships()) {
                relationship.setIsDependenciesLoadedField(
                        getEntityClass().getDeclaredField(stateFieldsNames.get(relationship)));
            }

        } catch (Exception e) {

                e.printStackTrace();
        }

    }

    protected String getProcedureName(QueryType queryType) {
        return proceduresNames.get(queryType);
    }

    List<SpEntityFieldAssociatedWithColumn> getAttributeFields() { return attributeFields; }

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
