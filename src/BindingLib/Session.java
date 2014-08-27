package BindingLib;

import Annotations.Entity;
import javassist.ClassPool;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
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
    private Map<EntityBinding, List> connectedEntities;
    //TODO: возможно стоит добавить statement cache

    public Session(String url, String dbUser, String password) {
        Locale.setDefault(Locale.ENGLISH);
        this.url = url;
        this.dbUser = dbUser;
        this.password = password;
        this.executor = new Executor();
        this.entityBindingRepository = new Hashtable<>();
        this.queryGenerator = new QueryGenerator();
        this.connectedEntities = new HashMap<>();

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

    public void loadDependencies(EntityBinding entityBinding, Object obj) {

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
        PreparedStatement statement = null;

        try {
            statement =
                    /*(OraclePreparedStatement) unimplemented feature*/
                    connection.prepareStatement(queryGenerator.createSelectAll(entityBinding));

            resultSet = statement.executeQuery();
            resultEntities = ResultSetMapper.createEntities(resultSet, entityBinding);

            List entities = connectedEntities.get(entityBinding);
            if (entities == null) connectedEntities.put(entityBinding, resultEntities);
            else {
                Field identifier = entityBinding.getIdentifier().getField();
                for (Object entityInResult : resultEntities) {
                    boolean isAlreadyConnected = false;
                    for (Object entity : entities) {
                        if (identifier.get(entity) == identifier.get(entityInResult)) {
                            entity = entityInResult;
                            isAlreadyConnected = true;
                            break;
                        }
                    }
                    if (!isAlreadyConnected) entities.add(entityInResult);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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

    public Map<Class, EntityBinding> getEntityBindingRepository() {
        return entityBindingRepository;
    }

}

