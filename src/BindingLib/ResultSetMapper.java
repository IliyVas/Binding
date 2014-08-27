package BindingLib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/*
 */
public class ResultSetMapper {

    //TODO: Использовать oracleresultset
    protected static <T> List<T> createEntities(ResultSet resultSet, EntityBinding entityBinding) throws SQLException{

        Class entityClass = entityBinding.getEntityClass();
        List<T> newEntities = new ArrayList<>();

        while (resultSet.next()) {
            try {
                //TODO: проверить можно ли вообще так писать
                T entity = (T)entityClass.newInstance();

                for (PropertyBinding property : entityBinding.getProperties()) {
                    Object columnValue = null;
                    switch (property.getField().getType().getName()) {
                        case "Integer":
                        case "int":
                            columnValue = resultSet.getInt(property.getColumnName());
                            break;
                        case "java.lang.String":
                            columnValue = resultSet.getString(property.getColumnName());
                            break;
                    }
                    property.getField().set(entity, columnValue);
                }



                newEntities.add(entity);

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return newEntities;
    }
}
