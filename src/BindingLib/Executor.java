package BindingLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 */
//TODO: добавить кеширование
//TODO: connection или DataSource
//TODO: Возможно стоит убрать QueryBank
public class Executor {
    private Connection connection;
    private Map<Class, EntityBinding> entityBindingRepository;
    private QueryGenerator queryGenerator;
    private PropertyMapper propertyMapper;

    ResultSet executeSelectAll(EntityBinding entityBinding) throws SQLException {
        PreparedStatement query;
        query = connection.prepareStatement( queryGenerator.createSelectAll(entityBinding) );
        return query.executeQuery();
    }

    ResultSet executeSelect(String sql, List bindingParameters) throws SQLException {
        PreparedStatement query = connection.prepareStatement(sql);
        Iterator iterator = bindingParameters.iterator();
        int index = 1;

        while (iterator.hasNext()) {
            query.setObject(index, iterator.next());
            index++;
        }

        return query.executeQuery();
    }
}
