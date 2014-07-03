package BindingLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 */

//TODO: возможно некоторые поля содержат пробелы
//TODO: добавить проверку типа привязки
class Select {
    private StringBuilder select;
    private EntityBinding entityBinding;
    private Executor executor;
    private List propertiesValues;
    private boolean isValid;
    private boolean waitingForExpression;

    Select(EntityBinding entityBinding, Executor executor) {
        this.executor = executor;
        this.entityBinding = entityBinding;
        this.propertiesValues = new ArrayList();
        this.isValid = true;
        this.waitingForExpression = false;
        select = new StringBuilder("SELECT ");

        Iterator<PropertyBinding> iterator = entityBinding.getProperties().iterator();

        select.append(iterator.next().getColumnName());

        while (iterator.hasNext()) {
            select.append(", ")
                  .append(iterator.next().getColumnName());
        }

        select.append(" FROM ")
              .append(entityBinding.getTableName());
    }

    public Select where(String fieldName) {
        addWhere();
        PropertyBinding property = entityBinding.getPropertyBinding(fieldName);
        if (property == null) {
            isValid = false;
        }
        else {
            select.append(" ")
                  .append(property.getColumnName());
            waitingForExpression = true;
        }
        return this;
    }

    private void addWhere() {
        if (select.indexOf("where") == -1) {
            select.append(" where");
        }
    }

    public Select eq(Object value) {
        if (waitingForExpression) {
            select.append(" = ?");
            propertiesValues.add(value);
            waitingForExpression = false;
        }
        else {
            isValid = false;
        }
        return this;
    }

    public <T> T[] execute() throws SQLException {
        if (isValid && !waitingForExpression) {
            ResultSet resultSet = executor.executeSelect(select.toString(), propertiesValues);
        }
    }

}
