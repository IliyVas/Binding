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
//TODO: Возможно лучше возвращать StringBuilder, а не List<StringBuilder>
//TODO: Генерировать запросы с незаполнеными параметрами
//TODO: Подумать над порядком параметов
//TODO: Использование map of maps неэффективно
//TODO: createDelete реализовать вариант через IN (array)
//TODO: Реализовать кэш для коллекций
public class QueryGenerator  {
    private UserContext context;
    private Map<EntityBinding, String> selectAllCache;
    private Map<EntityBinding, String> selectByIdCache;
    private Map<EntityBinding, String> updateCache;
    private Map<EntityBinding, String> updateBatchCache;
    private Map<EntityBinding, String> deleteCache;
    private Map<EntityBinding, String> deleteBatchCache;
    private Map<EntityBinding, String> insertCache;
    private Map<EntityBinding, String> insertBatchCache;

    public QueryGenerator(UserContext context) {
        this.context = context;
        this.selectAllCache = new Hashtable<>();
        this.selectByIdCache = new Hashtable<>();
        this.updateCache = new Hashtable<>();
        this.updateBatchCache = new Hashtable<>();
        this.deleteCache = new Hashtable<>();
        this.deleteBatchCache = new Hashtable<>();
        this.insertCache = new Hashtable<>();
        this.insertBatchCache = new Hashtable<>();
    }

    public String createSelectAll(EntityBinding entityBinding) {

        String savedQuery = selectAllCache.get(entityBinding);
        if (savedQuery != null) return savedQuery.toString();

        StringBuilder query = new StringBuilder(40);
        Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();

        while(iterator.hasNext()) query.append(iterator.next().getColumnName()).append(" ");

        switch (entityBinding.getBindingType()){
            case Table:
                query.append("from ").append(entityBinding.getTableName());
                break;

            case StoredProcedure:
                query.append("from table(")
                     .append(
                             generateNameProcedure(QueryType.select, entityBinding.getPackageName())
                     )
                     .append(")");
                break;

            default:
                throw new NotImplementedException();
        }

        selectAllCache.put(entityBinding, query.toString());
        return query.toString();
    }

    public String createSelectById(EntityBinding entityBinding) {

        String savedQuery = selectByIdCache.get(entityBinding);
        if (savedQuery != null) return savedQuery;

        StringBuilder query = new StringBuilder(createSelectAll(entityBinding));
        PropertyBinding identifier = entityBinding.getIdentifier();

        switch (entityBinding.getBindingType()){
            case Table:
                query.append(" where ")
                     .append(identifier.getColumnName())
                     .append("=:")
                     .append(identifier.getFieldName());
                break;

            case StoredProcedure:
                int index = query.indexOf("()");
                if(index != -1) query.insert(index + 1, ":" + identifier.getFieldName());
                else {
                    index = query.indexOf(")");
                    query.insert(index, ":" + identifier.getFieldName());
                }
                break;

            default:
                throw new NotImplementedException();
        }

        selectByIdCache.put(entityBinding, query.toString());
        return  query.toString();
    }

    public String createUpdate(EntityBinding entityBinding) {

        String savedQuery = updateCache.get(entityBinding);
        if (savedQuery != null) return savedQuery;

        StringBuilder query = new StringBuilder(40);

        PropertyBinding property;
        PropertyBinding identifier = entityBinding.getIdentifier();

        switch (entityBinding.getBindingType()){
            case Table:
                Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();

                query.append("update ")
                     .append(entityBinding.getTableName())
                     .append(" set ");

                property = iterator.next();
                if( property != identifier) {
                    query.append(property.getColumnName())
                         .append("=:")
                         .append(property.getFieldName());
                }

                while (iterator.hasNext()){
                    property = iterator.next();
                    if( property != identifier) {
                        query.append(", ")
                                .append(property.getColumnName())
                                .append("=:")
                                .append(property.getFieldName());
                    }
                }

                query.append(" where ")
                     .append(identifier.getColumnName())
                     .append("=:")
                     .append(identifier.getFieldName());
                break;

            case StoredProcedure:

                query.append("{call ")
                     .append(
                             generateNameProcedure(QueryType.update, entityBinding.getPackageName())
                     )
                     .append("}");

                int index = query.indexOf("()") + 1;
                query.insert(index, "?)");

                for(int i=2; i <= entityBinding.getProperties().size(); i++) query.insert(index, "?, ");
                break;

            default:
                throw new NotImplementedException();
        }

        updateCache.put(entityBinding, query.toString());
        return query.toString();
    }

    /*
    public List<String> createUpdate(EntityBinding entityBinding, List<Object> entities) {

        List<String> queries = new ArrayList<>();
        Iterator iterator = entities.iterator();

        while (iterator.hasNext()) queries.add(createUpdate(entityBinding));

        return queries;
    }
    */

    public String createDelete(EntityBinding entityBinding) {

        String savedQuery = selectByIdCache.get(entityBinding);
        if (savedQuery != null) return savedQuery;

        StringBuilder query = new StringBuilder(40);

        switch (entityBinding.getBindingType()){
            case Table:
                query.append("delete from ")
                     .append(entityBinding.getTableName())
                     .append(" where ")
                     .append(entityBinding.getIdentifier().getColumnName())
                     .append("in (")
                     .append(entityBinding.getIdentifier().getFieldName())
                     .append(")");
                break;

            case StoredProcedure:
                query.append("{call ")
                     .append(
                             generateNameProcedure(QueryType.delete, entityBinding.getPackageName())
                             )
                     .append("}");

                int index = query.indexOf("()");
                if( index != -1) {
                    query.insert(index + 1, ":" + entityBinding.getIdentifier().getFieldName());
                }
                else {
                    index = query.indexOf(")");
                    query.insert(index, ":" + entityBinding.getIdentifier().getFieldName());
                }
                break;

            default:
                throw new NotImplementedException();
        }

        deleteCache.put(entityBinding, query.toString());
        return query.toString();
    }

    /*
    public List<StringBuilder> createDelete(EntityBinding entityBinding, List<Object> entities) {

        List<StringBuilder> queries = new ArrayList<>();
        Iterator iterator = entities.iterator();

        while(iterator.hasNext()) queries.add(createDelete(entityBinding, iterator.next()));

        return queries;
    }
    */

    /*
    public List<StringBuilder> createInsert(EntityBinding entityBinding, List<Object> entities) {

        List<StringBuilder> queries = new ArrayList<>();
        Iterator iterator = entities.iterator();

        while (iterator.hasNext()) queries.add(createUpdate(entityBinding, iterator.next()));

        return queries;
    }
    */

    public String createInsert(EntityBinding entityBinding) {

        String savedQuery = updateCache.get(entityBinding);
        if (savedQuery != null) return savedQuery;

        StringBuilder query = new StringBuilder(40);
        StringBuilder values;

        switch (entityBinding.getBindingType()) {
            case Table:
                Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();
                PropertyBinding property;

                query.append("insert into  ")
                     .append(entityBinding.getTableName())
                     .append(" (");

                values = new StringBuilder("(");
                property = iterator.next();

                query.append(property.getColumnName());
                values.append(":" + property.getFieldName());

                while (iterator.hasNext()) {
                    property = iterator.next();
                    if(property != entityBinding.getIdentifier()) {
                        query.append(", ")
                             .append(":" + property.getColumnName());
                        values.append(", ")
                              .append(":" + property.getFieldName());
                    }
                }

                query.append(") values(")
                     .append(values)
                     .append(")");
                break;

            case StoredProcedure:
                query.append("{ call ")
                     .append(
                             generateNameProcedure(QueryType.insert, entityBinding.getPackageName())
                             )
                     .append("}");

                values = new StringBuilder("(");

                int index = query.indexOf("()") +1;
                if (index == 0) index = query.indexOf(")");
                query.insert(index, "?");
                for( int i = 3; i <= entityBinding.getProperties().size(); i++) query.insert(index, ", ?");

                break;

            default:
                throw new NotImplementedException();
        }

        insertCache.put(entityBinding, query.toString());
        return query.toString();
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
