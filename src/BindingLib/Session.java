package BindingLib;

import oracle.jdbc.OracleCallableStatement;
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
    Connection connection;
    private String url;
    private String dbUser;
    private String password;
    private Map<Class, EntityBinding> entityBindingRepository;
    private Executor executor;
    private QueryGenerator queryGenerator;
    private Cache cache;

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

    public Connection getConnection() {
        return connection;
    }

    public void save(Object obj) {
        throw new NotImplementedException();
    }

    public void saveBatch(List objects) {
        throw new NotImplementedException();
    }

    private OraclePreparedStatement loadById(EntityBinding entityBinding, Object id) {
        if (entityBinding instanceof StoredProcedureBinding) {
            StoredProcedureBinding binding = (StoredProcedureBinding)entityBinding;
            OraclePreparedStatement statement = queryGenerator.createSelectById(binding, id);
            return statement;
        }
    }

    @Override
    public void loadObject(EntityBinding entityBinding, Object object) {
        Object id = entityBinding.getIdentifier().getFieldValue(object);
        OraclePreparedStatement statement = loadById(entityBinding, id);
        OracleResultSet resultSet = statement.executeQuery();
        ResultSetMapper.map(resultSet.next(), object);
    }

    public void getById(Class entity, Object id) {
        OracleResultSet resultSet = loadById(entityBindingRepository.get(entity), id);

    }

    public WhereStatementPart get(Class entity) { return new WhereStatementPart(entityBindingRepository.get(entity)); }

    //TODO: подумать над реализацией в отдельном классе
    public <T> Set<T> getAll(Class<T> entity) {
        Set<T> resultEntities;
        EntityBinding entityBinding = entityBindingRepository.get(entity);
        if (entityBinding instanceof StoredProcedureBinding) {
            StoredProcedureBinding binding = (StoredProcedureBinding)entityBinding;
            OraclePreparedStatement statement = queryGenerator.createSelectAll(binding);
            if (cache.isOutOfDate(binding, statement)) {
                ResultSet resultSet = statement.executeQuery();
                resultEntities = ResultSetMapper.createFrom(resultSet, binding);
                cache.put(binding, statement, resultEntities);
            }
            else resultEntities = cache.get(binding,statement);
            statement.close();


            binding.setAsConnected(resultEntities);
            return resultEntities;
        }
    }

    public Map<Class, EntityBinding> getEntityBindingRepository() {
        return entityBindingRepository;
    }

}

