package BindingLib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
enum QueryType{
    select,
    insert,
    update,
    delete
}
//TODO: Возможно стоит добавить интерфейс, требующий реализации generateProcedureName
//TODO: Может стоит использовать изменяемые строки?
//TODO: Возможно лучше возвращать String, а не List<String>
//TODO: Возможно лучше генерировать запросы с незаполнеными параметрами или даже prepareStatement
//TODO: Подумать над порядком параметов
//TODO: Нужно возвращать ошибку в случае case default
//TODO: Заменить Dictionary, он устарел
//TODO: Использование map of maps неэффективно
//TODO: Рассмотреть варианты реализации createDelete
//TODO: Реализовать кэш для коллекций
public class QueryGenerator  {
    private UserContext context;
    private Dictionary<EntityBinding, String> selectCache;
    private Map<EntityBinding, Map<Object, String>> selectByIdCache;
    private Map<EntityBinding, Map<Object, String>> updateCache;
    private Dictionary<EntityBinding, List<String>> updateBatchCache;
    private Dictionary<EntityBinding, Map<Object, String>> deleteCache;
    private Dictionary<EntityBinding, List<String>> deleteBatchCache;
    private Dictionary<EntityBinding, Map<Object, String>> insertCache;
    private Dictionary<EntityBinding, List<String>> insertBatchCache;

    public QueryGenerator(UserContext context) {
        this.context = context;
        this.selectCache = new Hashtable<>();
        this.selectByIdCache = new Hashtable<>();
        this.updateCache = new Hashtable<>();
        this.updateBatchCache = new Hashtable<>();
        this.deleteCache = new Hashtable<>();
        this.deleteBatchCache = new Hashtable<>();
        this.insertCache = new Hashtable<>();
        this.insertBatchCache = new Hashtable<>();
    }

    public String createSelect(EntityBinding entityBinding) {

        String query = selectCache.get(entityBinding);
        if (query != null) return query;

        query = "select ";
        Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();

        while(iterator.hasNext()) query += ( iterator.next().getColumnName() + " ");

        switch (entityBinding.getBindingType()){

            case Table:
                query += ("from " + entityBinding.getTableName());
                break;

            case StoredProcedure:
                String fromStatement =
                        "from table(" +
                        generateNameProcedure(QueryType.select, entityBinding.getPackageName()) +
                        ")";
                break;

            default:
                throw new NotImplementedException();
        }
        selectCache.put(entityBinding, query);
        return query;
    }

    public String createSelectById(EntityBinding entityBinding, Object id) {

        String query;
        Map<Object, String> idStringCache = selectByIdCache.get(entityBinding);
        if (idStringCache == null) idStringCache = new Hashtable<>();
        else {
            query = idStringCache.get(id);
            if (query != null) return query;
        }

        query =  createSelect(entityBinding);

        switch (entityBinding.getBindingType()){

            case Table:
                String whereStatement = " where " +
                        entityBinding.getIdentifier().getColumnName() + " = " + id.toString();

                query += whereStatement;
                break;

            case StoredProcedure:
                query = query.replace("()", "(" + id.toString() + ")");
                break;

            default:
                throw new NotImplementedException();
        }

        idStringCache.put(id, query);
        selectByIdCache.put(entityBinding, idStringCache);

        return  query;
    }

    public String createUpdate(EntityBinding entityBinding, Object entity) {

        String query;

        Map<Object, String> idStringCache = updateCache.get(entityBinding);
        if (idStringCache == null) idStringCache = new Hashtable<>();
        else {
            query = idStringCache.get(entity);
            if (query != null) return query;
        }

        PropertyBinding property;
        PropertyBinding identifier = entityBinding.getIdentifier();
        Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();

        switch (entityBinding.getBindingType()){

            case Table:

                query = "update " + entityBinding.getTableName() + " set ";

                while (iterator.hasNext()){
                    property = iterator.next();
                    query += (property.getColumnName() + "=" + property.getFieldValue(entity) + " ");
                }

                query += ("where " + identifier.getColumnName() + "=" + identifier.getFieldValue(entity) );
                break;

            case StoredProcedure:

                String parameters = "(";

                query = "{call " + generateNameProcedure(QueryType.update, entityBinding.getPackageName()) + "}";

                parameters += iterator.next().getFieldValue(entity);
                while (iterator.hasNext()) parameters += (", " + iterator.next().getFieldValue(entity));

                parameters += ")";
                query = query.replace("()", parameters);
                break;

            default:
                throw new NotImplementedException();
        }

        idStringCache.put(entity, query);
        updateCache.put(entityBinding, idStringCache);

        return query;
    }

    public List<String> createUpdate(EntityBinding entityBinding, List<Object> entities) {

        List<String> queries = new ArrayList<>();
        Iterator iterator = entities.iterator();

        while (iterator.hasNext()) queries.add(createUpdate(entityBinding, iterator.next()));

        return queries;
    }

    public String createDelete(EntityBinding entityBinding, Object entity) {

        String query;
        PropertyBinding identifier = entityBinding.getIdentifier();
        Object id = identifier.getFieldValue(entity);

        Map<Object, String> idStringCache = deleteCache.get(entityBinding);
        if (idStringCache == null) idStringCache = new Hashtable<>();
        else {
            query = idStringCache.get(id);
            if (query != null) return query;
        }

        switch (entityBinding.getBindingType()){

            case Table:
                query = "delete from " +
                        entityBinding.getTableName() + " where " +
                        entityBinding.getIdentifier().getColumnName() + "=" +
                        entityBinding.getIdentifier().getFieldValue(entity);
                break;

            case StoredProcedure:
                query = "{call " + generateNameProcedure(QueryType.delete, entityBinding.getPackageName()) + "}";
                query = query.replace("()", "(" + entityBinding.getIdentifier().getFieldValue(entity) + ")");
                break;

            default:
                throw new NotImplementedException();
        }

        idStringCache.put(id, query);
        deleteCache.put(entityBinding, idStringCache);

        return query;
    }

    public List<String> createDelete(EntityBinding entityBinding, List<Object> entities) {

        List<String> queries = new ArrayList<>();
        Iterator iterator = entities.iterator();

        while(iterator.hasNext()) queries.add(createDelete(entityBinding, iterator.next()));

        return queries;
    }

    public List<String> createInsert(EntityBinding entityBinding, List<Object> entities) {

        List<String> queries = new ArrayList<>();
        Iterator iterator = entities.iterator();

        while (iterator.hasNext()) queries.add(createUpdate(entityBinding, iterator.next()));

        return queries;
    }

    public String createInsert(EntityBinding entityBinding, Object entity) {

        String query;

        Map<Object, String> idStringCache = insertCache.get(entityBinding);
        if (idStringCache == null) idStringCache = new Hashtable<>();
        else {
            query = idStringCache.get(entity);
            if (query != null) return query;
        }

        String values;
        Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();
        PropertyBinding property;

        switch (entityBinding.getBindingType()) {

            case Table:

                query = "insert into  " + entityBinding.getTableName() + " (";

                values = "(";
                property = iterator.next();

                query += property.getColumnName();
                values += property.getFieldValue(entity);

                while (iterator.hasNext()) {
                    property = iterator.next();
                    query += (", " + property.getColumnName());
                    values += (", " + property.getFieldValue(entity));
                }

                query += (") values " + values + ")");
                break;

            case StoredProcedure:

                query = "{ call " + generateNameProcedure(QueryType.insert, entityBinding.getPackageName()) + "}";
                values = "(";

                property = iterator.next();
                values += property.getFieldValue(entity);

                while (iterator.hasNext()) values += (", " + iterator.next().getFieldValue(entity));

                values += ")";
                query = query.replace("()", values);
                break;

            default:
                throw new NotImplementedException();
        }

        idStringCache.put(entity, query);
        insertCache.put(entityBinding, idStringCache);

        return query;
    }

    private String generateNameProcedure(QueryType queryType, String procedurePackage) {
        String procedureFullName = procedurePackage;
        switch (queryType){
            case select:
                procedureFullName += ".GETOBJ()";
                break;
            case insert:
                procedureFullName += ".INSERTOBJ()";
                break;
            case update:
                procedureFullName += ".UPDATEOBJ()";
                break;
            case delete:
                procedureFullName += ".DELETEOBJ()";
                break;
        }
        return procedureFullName;
    }
}
