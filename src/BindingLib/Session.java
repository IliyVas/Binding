package BindingLib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/*
*
*/

public class Session {
    Connection connection;
    private String url;
    private String dbUser;
    private String password;
    private Map<Class, EntityBinding> entityBindingRepository;
    private Executor executor;

    public Session(String url, String dbUser, String password) {
        this.url = url;
        this.dbUser = dbUser;
        this.password = password;
        this.executor = new Executor();
        this.entityBindingRepository = new Hashtable<>();
    }

    public void open() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(url, dbUser, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void save(Object obj) {
        throw new NotImplementedException();
    }

    public void saveBatch(List objects) {
        throw new NotImplementedException();
    }

    public Select getFrom(Class entity) {
       return new Select(entityBindingRepository.get(entity), connection);
    }

    public void getById(Class entity, int id) { throw new NotImplementedException(); }

    public <T> T[] getAll(Class entity) throws SQLException {
        ResultSet resultSet = executor.executeSelectAll(entityBindingRepository.get(entity));

    }

    public Map<Class, EntityBinding> getEntityBindingRepository() {
        return entityBindingRepository;
    }

}

