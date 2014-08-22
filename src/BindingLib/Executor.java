package BindingLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private ResultSetMapper propertyMapper;

    ResultSet executeSelectAll(EntityBinding entityBinding) throws SQLException {
        PreparedStatement query;
        query = connection.prepareStatement( queryGenerator.createSelectAll(entityBinding) );
        return query.executeQuery();
    }
}
