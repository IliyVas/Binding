package BindingLib;

import Annotations.ManyToOne;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/*
 */
public class Mapper {

    //TODO: Использовать oracleresultset
    static <T> List<T> mapResultSetToEntities(ResultSet resultSet, EntityBinding entityBinding, List sessionEntities)
            throws SQLException {

        List<T> newEntities = new ArrayList<>();

        while (resultSet.next()) {
            try {
                newEntities.add(createOrUpdate(resultSet, entityBinding, sessionEntities));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return newEntities;
    }

    static <T> T mapResultSetToEntity(ResultSet resultSet, EntityBinding entityBinding, List sessionEntities)
            throws SQLException {

        T entity = null;

        try {
            if (resultSet.next()) {
                entity = createOrUpdate(resultSet, entityBinding, sessionEntities);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return entity;
    }

    private static <T> T createOrUpdate(ResultSet resultSet, EntityBinding entityBinding, List sessionEntities)
            throws SQLException, IllegalAccessException, InstantiationException {

        T entity = null;
        PropertyBinding identifier = entityBinding.getIdentifier();
        Object id = getColumnValue(identifier.getColumnName(), identifier.getClass(), resultSet);

        Object columnValue;

        for (Object sessionEntity : sessionEntities) {
            if (identifier.getField().get(sessionEntity) == id) entity = (T) sessionEntity;
        }

        //TODO: хранить entityClass как static field?
        if (entity == null) {
            entity = (T) entityBinding.getEntityClass().newInstance();
            sessionEntities.add(entity);
        }

        //TODO: сделать через интерфейсы
        for (PropertyBinding property : entityBinding.getProperties()) {
            columnValue =
                    getColumnValue(property.getColumnName(), property.getField().getType(), resultSet);
            property.getField().set(entity, columnValue);
        }
        for (Relationship relationship :entityBinding.getRelationships()) {
            if (relationship instanceof ManyToOne) {
                columnValue = getColumnValue(((ManyToOneRelationship)relationship).getColumnName(),
                        relationship.getField().getType(), resultSet);

                relationship.getField().set(entity, columnValue);
            }
        }

        entityBinding.getEntityBindingField().set(entity, entityBinding);

        return entity;
    }

    private static Object getColumnValue(String columnName, Class<?> fieldType, ResultSet resultSet) throws SQLException {
        //TODO: работать не через строки
        switch (fieldType.getName()) {
            case "Integer":
            case "int":
                return resultSet.getInt(columnName);
            case "java.lang.String":
                return resultSet.getString(columnName);
            default:
                return resultSet.getObject(columnName);
        }
    }
}
