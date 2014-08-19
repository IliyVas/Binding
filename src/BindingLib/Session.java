package BindingLib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void getById(Class entity, int id) { throw new NotImplementedException(); }

    public WhereStatementPart get(Class entity) { return new WhereStatementPart(entityBindingRepository.get(entity)); }

    public <T> Set<T> getAll(Class<T> entity) {
        EntityBinding entityBinding = entityBindingRepository.get(entity);
        connection.prepareStatement("sss").execute();
        throw new NotImplementedException();
    }

    public Map<Class, EntityBinding> getEntityBindingRepository() {
        return entityBindingRepository;
    }

}

