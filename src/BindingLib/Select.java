package BindingLib;

import java.util.Iterator;

/*
 */

//TODO: возможно некоторые поля содержат пробелы
//TODO: добавить проверку типа привязки
public class Select {
    private StringBuilder select;
    private EntityBinding entityBinding;
    private boolean isValid;
    private boolean waitingForExpression;

    Select(EntityBinding entityBinding) {
        this.entityBinding = entityBinding;
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
}
