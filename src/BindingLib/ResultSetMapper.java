package BindingLib;

import java.sql.ResultSet;

/*
 */
public class ResultSetMapper {

    <T> T[] createFromSelect(EntityBinding<T> entityBinding, ResultSet resultSet) {
        Class<T> entity = entityBinding.getEntity();
        T objects[] = new T[resultSet.    }
}
