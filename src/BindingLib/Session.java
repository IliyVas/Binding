package BindingLib;

import Annotations.Entity;
import Annotations.OneToMany;
import oracle.jdbc.OraclePreparedStatement;
import org.reflections.Reflections;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/*
*
*/

public class Session implements AttemptToGetUnloadedFieldListener {
    //TODO: добавить SessionPool
    Connection connection;
    private String url;
    private String dbUser;
    private String password;
    private Map<Class, EntityBinding> entityBindingRepository;
    private Executor executor;
    private QueryGenerator queryGenerator;
    private Cache cache;
    private Map<EntityBinding, List> sessionEntities;
    //TODO: возможно стоит добавить statement cache

    public Session(String url, String dbUser, String password) {
        Locale.setDefault(Locale.ENGLISH);
        this.url = url;
        this.dbUser = dbUser;
        this.password = password;
        this.executor = new Executor();
        this.entityBindingRepository = new Hashtable<>();
        this.queryGenerator = new QueryGenerator();
        this.sessionEntities = new HashMap<>();

        Reflections reflections = new Reflections("Test");
        Set<Class<? extends Object>> classes = reflections.getTypesAnnotatedWith(Entity.class);
        for (Class<? extends  Object> clazz : classes) {
            if (clazz.getAnnotation(Entity.class).bindingType() == "Simple") {}
            else {
                StoredProcedureBinding binding = new StoredProcedureBinding(clazz);
                entityBindingRepository.put(clazz, binding);
            }
        }
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

    public void loadDependencies(Object obj, Relationship relationship) {
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

    public WhereStatementPart get(Class entityClass) { return new WhereStatementPart(entityBindingRepository.get(entityClass)); }

    //TODO: подумать над реализацией в отдельном классе
    public <T> List<T> getAll(Class<T> entityClass) {
        List<T> resultEntities = null;
        EntityBinding entityBinding = entityBindingRepository.get(entityClass);

        ResultSet resultSet = null;
        OraclePreparedStatement statement = null;

        try {
            statement =
                    (OraclePreparedStatement)connection.prepareStatement(queryGenerator.createSelectAll(entityBinding));

            resultSet = statement.executeQuery();



            resultEntities =
                    Mapper.mapResultSetToEntities(resultSet, entityBinding, getSessionEntities(entityBinding));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            //TODO: узнать, что случится в случае null
            try{ resultSet.close(); }
            catch(SQLException e){ e.printStackTrace(); }
            try{ statement.close(); }
            catch(SQLException e){ e.printStackTrace(); }
        }

        return resultEntities;
    }

    public <T> T getById(Class<T> entityClass, Object id) {
        T entity = null;
        EntityBinding entityBinding = entityBindingRepository.get(entityClass);

        ResultSet resultSet = null;
        OraclePreparedStatement statement = null;

        try {
            statement =
                   (OraclePreparedStatement)connection.prepareStatement(queryGenerator.createSelectById(entityBinding));

            statement.setObject(1, id);
            resultSet = statement.executeQuery();

            //TODO: как вывести тип?
            entity = Mapper.mapResultSetToEntity(resultSet, entityBinding, getSessionEntities(entityBinding));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }

    public Map<Class, EntityBinding> getEntityBindingRepository() {
        return entityBindingRepository;
    }

    private List getSessionEntities(EntityBinding entityBinding) {
        List sessionEntities = this.sessionEntities.get(entityBinding);
        if (sessionEntities == null) {
            sessionEntities = new ArrayList<>();
            this.sessionEntities.put(entityBinding, sessionEntities);
        }
        return sessionEntities;
    }

}

