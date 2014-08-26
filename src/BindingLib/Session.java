package BindingLib;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    //TODO: возможно стоит добавить statement cache

    public Session(String url, String dbUser, String password) {
        this.url = url;
        this.dbUser = dbUser;
        this.password = password;
        this.executor = new Executor();
        this.entityBindingRepository = new Hashtable<>();
        this.queryGenerator = new QueryGenerator();
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





    public WhereStatementPart get(Class entity) { return new WhereStatementPart(entityBindingRepository.get(entity)); }

    //TODO: подумать над реализацией в отдельном классе
    public <T> Set<T> getAll(Class<T> entity) {
        Set<T> resultEntities = null;
        EntityBinding entityBinding = entityBindingRepository.get(entity);

        ResultSet resultSet = null;
        OraclePreparedStatement statement = null;

        try {
            statement =
                    (OraclePreparedStatement)connection.prepareStatement(queryGenerator.createSelectAll(entityBinding));

            resultSet = statement.executeQuery();
                resultEntities = ResultSetMapper.createEntities(resultSet, entityBinding);

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally{
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

