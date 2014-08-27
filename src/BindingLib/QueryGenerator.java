package BindingLib;

import Annotations.ManyToOne;
import oracle.jdbc.OraclePreparedStatement;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

//TODO: Возможно лучше возвращать StringBuilder, а не List<StringBuilder>
//TODO: Генерировать запросы с незаполнеными параметрами
//TODO: Подумать над порядком параметов
//TODO: Использование map of maps неэффективно
//TODO: createDelete реализовать вариант через IN (array)
//TODO: Реализовать кэш для коллекций
public class QueryGenerator  {
    private UserContext context;
    private Map<EntityBinding, String> selectAllCache;
    private Map<EntityBinding, OraclePreparedStatement> selectAllStatementCache;
    private Map<EntityBinding, String> selectByIdCache;
    private Map<EntityBinding, String> updateCache;
    private Map<EntityBinding, String> updateBatchCache;
    private Map<EntityBinding, String> deleteCache;
    private Map<EntityBinding, String> deleteBatchCache;
    private Map<EntityBinding, String> insertCache;
    private Map<EntityBinding, String> insertBatchCache;

    public QueryGenerator() {
        this.selectAllCache = new Hashtable<>();
        this.selectAllStatementCache = new Hashtable<>();
        this.selectByIdCache = new Hashtable<>();
        this.updateCache = new Hashtable<>();
        this.updateBatchCache = new Hashtable<>();
        this.deleteCache = new Hashtable<>();
        this.deleteBatchCache = new Hashtable<>();
        this.insertCache = new Hashtable<>();
        this.insertBatchCache = new Hashtable<>();
    }

    String createSelectAll(EntityBinding binding) {

        String query = selectAllCache.get(binding);

        if (query == null) {

            StringBuilder newQuery = new StringBuilder("select ");

            newQuery.append(binding.getProperties().stream().map(p -> p.getColumnName()).collect(Collectors.joining(", ")))
                    .append(' ')
                    .append(binding.getRelationships().stream().filter(r -> r instanceof ManyToOneRelationship)
                            .map(r -> ((ManyToOneRelationship) r).getColumnName()).collect(Collectors.joining(", ")))
                    .append(" from ");

            if (binding instanceof SimpleBinding)
                newQuery.append(((SimpleBinding) binding).getTableName()).append(" where id = ?");

            else newQuery.append("table(")
                    .append(((StoredProcedureBinding) binding).getProcedureName(QueryType.selectAll))
                    .append("())");

            query = newQuery.toString();
            selectAllCache.put(binding, query);
        }

        return query;
    }

    String createSelectById(EntityBinding binding) {

        String query = selectByIdCache.get(binding);

        if (query == null) {

            StringBuilder newQuery = new StringBuilder("select ");

            newQuery.append(attributesColumns(binding))
                    .append(' ')
                    .append(associationsColumns(binding))
                    .append(" from ");

            if (binding instanceof SimpleBinding) newQuery.append(((SimpleBinding) binding).getTableName());
            else newQuery.append("table(")
                    .append(((StoredProcedureBinding) binding).getProcedureName(QueryType.selectById))
                    .append("(?))");

            query = newQuery.toString();
            selectByIdCache.put(binding, query);
        }

        return query;
    }

    private String attributesColumns(EntityBinding binding) {
        return binding.getProperties().stream().map(p -> p.getColumnName()).collect(Collectors.joining(", "));
    }
    private String associationsColumns(EntityBinding binding) {
        return binding.getRelationships().stream().filter(r -> r instanceof ManyToOneRelationship)
                .map(r -> ((ManyToOneRelationship) r).getColumnName()).collect(Collectors.joining(", "));
    }
/*
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
    } */
}
