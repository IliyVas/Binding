package BindingLib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/*
 */
public class ResultSetMapper {

    //TODO: Использовать oracleresultset
    protected static <T> Set<T> createEntities(ResultSet resultSet, EntityBinding entityBinding) throws SQLException{

        Class entityClass = entityBinding.getEntityClass();
        Set<T> newEntities = new HashSet<>();

        while (resultSet.next()) {
            try {
                //TODO: проверить можно ли вообще так писать
                T entity = (T)entityClass.newInstance();

                for (PropertyBinding property : entityBinding.getProperties()) {
                    property.getField().set(entity, resultSet.getObject(property.getColumnName()));
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
