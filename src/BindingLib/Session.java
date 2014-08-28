package BindingLib;

import Annotations.Entity;
import Annotations.OneToMany;
import Exceptions.NoSuchObjectInSessionException;
import oracle.jdbc.OracleCallableStatement;
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

        Reflections.log = null;
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

    public <T> T create(Class<T> entityClass, Object... attributes ) {

        boolean attributesContainsId;
        T entity = null;
        OracleCallableStatement statement;
        EntityField entityField;
        EntityBinding entityBinding = entityBindingRepository.get(entityClass);

        try {
            if (attributes.length == entityBinding.getFieldsProperties().size()) attributesContainsId = true;
            else if (attributes.length == entityBinding.getFieldsProperties().size() - 1) attributesContainsId = false;
            else throw new IllegalArgumentException("wrong number of parameters");

            entity = (T) entityBinding.getEntityClass().newInstance();
            ListIterator<EntityField> iterator = entityBinding.getFieldsProperties().listIterator();

            for (int i = 0; i < attributes.length; i++) {

                entityField = iterator.next();
                if (entityField == entityBinding.getIdentifier()) {
                    if (attributesContainsId == true) {

                        Field id = entityBinding.getIdentifier().getField();

                        for (Object sessionEntity : getSessionEntities(entityBinding)) {
                            if (id.get(sessionEntity) == attributes[i])
                                throw new IllegalArgumentException("Object with such id already exists");
                        }
                    }
                    else {
                        entityField = iterator.next();
                    }
                }

                //TODO: нужна ли проверка на совместимость типов?
                entityField.getField().set(entity, attributes[i]);
            }

            statement = (OracleCallableStatement) connection.prepareCall(queryGenerator.createInsert(entityBinding));

            if (entityBinding instanceof SimpleBinding) {}
            else {

                for (EntityField fieldProperty : entityBinding.getFieldsProperties()) {

                    if (fieldProperty instanceof SpPropertyBinding) {

                        statement.setObject(((SpPropertyBinding) fieldProperty).getOrder(QueryType.insert),
                                fieldProperty.getField().get(entity));

                    } else if (fieldProperty instanceof SpManyToOneRelationship) {

                        SpManyToOneRelationship relationship = (SpManyToOneRelationship) fieldProperty;
                        EntityBinding parentBinding = entityBindingRepository.get(relationship.getAssociatedEntity());
                        Field parentPK = parentBinding.getIdentifier().getField();
                        Object parent = relationship.getField().get(entity);
                        boolean isParentExists = false;

                        if (parent == null) {
                            statement.setNull(relationship.getOrder(QueryType.insert), Types.NUMERIC);
                        } else {

                            for (Object sessionEntity : getSessionEntities(parentBinding)) {
                                if (sessionEntity == parent) {
                                    isParentExists = true;
                                    break;
                                }
                            }

                            if (isParentExists == true) {
                                statement.setObject(relationship.getOrder(QueryType.insert),
                                        parentPK.get(parent));

                                for (Relationship parentRelationship : parentBinding.getRelationships()) {
                                    if (parentRelationship instanceof OneToManyRelationship) {
                                        if (((OneToManyRelationship) parentRelationship).getAssociatedField() ==
                                                relationship.getField().getName()) {
                                            ((List) parentRelationship.getField().get(parent)).add(entity);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                throw new NoSuchObjectInSessionException();
                            }
                        }
                    } else if (fieldProperty instanceof OneToManyRelationship) {
                        List fieldObjects = (List) fieldProperty.getField().get(entity);
                        Field fkField =
                                ((OneToManyRelationship) fieldProperty).getAssociatedEntity()
                                        .getDeclaredField(((OneToManyRelationship) fieldProperty).getAssociatedField());
                        if (fieldObjects != null) {
                            for (Object fieldObject : fieldObjects) {
                                boolean isExists = false;
                                for (Object sessionEntity : getSessionEntities(entityBinding)) {
                                    if (fieldObject == sessionEntity) {
                                        isExists = true;
                                        break;
                                    }
                                }
                                if (!isExists) throw new NoSuchObjectInSessionException();
                            }
                        }
                    }
                }
            }

            statement.execute();

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SQLException e) {
            e.printStackTrace();
        } catch (NoSuchObjectInSessionException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return entity;
    }

    public void updateAll(Class entityClass) {

        EntityBinding binding = entityBindingRepository.get(entityClass);
        List sessionEntities = getSessionEntities(binding);

        try {
            if (binding instanceof SimpleBinding) {
            } else {
                OracleCallableStatement cStatement =
                        (OracleCallableStatement) connection.prepareCall(queryGenerator.createUpdate(binding));

                for (Object entity : sessionEntities) {

                    for (EntityField fieldProperty : binding.getFieldsProperties()) {

                        if (fieldProperty instanceof SpPropertyBinding) {

                            cStatement.setObject(((SpPropertyBinding) fieldProperty).getOrder(QueryType.update),
                                    fieldProperty.getField().get(entity));

                        } else if (fieldProperty instanceof SpManyToOneRelationship) {

                            SpManyToOneRelationship relationship = (SpManyToOneRelationship) fieldProperty;
                            EntityBinding parentBinding = entityBindingRepository.get(relationship.getAssociatedEntity());
                            Field parentPK = parentBinding.getIdentifier().getField();
                            Object parent = relationship.getField().get(entity);
                            boolean isParentExists = false;

                            if (parent == null) {
                                cStatement.setNull(relationship.getOrder(QueryType.insert), Types.NUMERIC);
                            } else {

                                for (Object sessionEntity : getSessionEntities(parentBinding)) {
                                    if (sessionEntity == parent) {
                                        isParentExists = true;
                                        break;
                                    }
                                }

                                if (isParentExists) {
                                    cStatement.setObject(relationship.getOrder(QueryType.insert),
                                            parentPK.get(parent));

                                    for (Relationship parentRelationship : parentBinding.getRelationships()) {
                                        if (parentRelationship instanceof OneToManyRelationship) {
                                            if (((OneToManyRelationship) parentRelationship).getAssociatedField() ==
                                                    relationship.getField().getName()) {
                                                ((List) parentRelationship.getField().get(parent)).add(entity);
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    throw new NoSuchObjectInSessionException();
                                }
                            }
                        } else if (fieldProperty instanceof OneToManyRelationship) {
                            List fieldObjects = (List) fieldProperty.getField().get(entity);
                            Field fkField =
                                    ((OneToManyRelationship) fieldProperty).getAssociatedEntity()
                                            .getDeclaredField(((OneToManyRelationship) fieldProperty).getAssociatedField());
                            if (fieldObjects != null) {
                                for (Object fieldObject : fieldObjects) {
                                    boolean isExists = false;
                                    for (Object sessionEntity : getSessionEntities(binding)) {
                                        if (fieldObject == sessionEntity) {
                                            isExists = true;
                                            break;
                                        }
                                    }
                                    if (!isExists) throw new NoSuchObjectInSessionException();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchObjectInSessionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

