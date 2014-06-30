package BindingLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

/*
 */
//TODO: добавить кеширование
//TODO: connection или DataSource
//TODO: Возможно стоит убрать QueryBank
public class Executor {
    private Connection connection;
    private Map<Class, EntityBinding> entityBindingRepository;
    private QueryGenerator queryGenerator;
    private PropertyMapper propertyMapper;

    <T> T[] executeSelectAll(Class<T> entity) {
        String query;
        EntityBinding entityBinding = entityBindingRepository.get(entity);
        QueryBank queryBank = entityBinding.getQueryBank();

        query = queryBank.getSelectAll();

        if ( query == "" ) {

        }
    }

    <T> T[] executeSelect(Class<T> entity, String... parametersAndValues) {
        PreparedStatement query;
        EntityBinding entityBinding = entityBindingRepository.get(entity);
        QueryBank queryBank = entityBinding.getQueryBank();

        if (parametersAndValues.length % 2 == 0) {
            int propertyNumber = parametersAndValues.length / 2;
            PropertyBinding[] propertyBindings = new PropertyBinding[propertyNumber];
            Object[] propertyValues = new Object[propertyNumber];

            for (int i = 0; i < propertyNumber; i++) {
                propertyBindings[i] = entityBinding.getPropertyBinding( parametersAndValues[2*i] );
                propertyValues[i] = parametersAndValues[2*i + 1];
            }

            query = connection.prepareStatement( queryBank.getSelect(propertyBindings) );

            if (query == null) {
                String sql = queryGenerator.createSelect(entity, propertyBindings);
                queryBank.addSelect(propertyBindings, sql);
                query = connection.prepareStatement(sql);
            }

            query = propertyMapper.setQueryProperties(query, propertyValues);
            query.executeQuery();

        }
    }
}
