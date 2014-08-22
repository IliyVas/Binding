package BindingLib;

import BindingLib.EntityBinding;
import BindingLib.ResultSetMapper;
import BindingLib.Session;

import java.sql.ResultSet;
import java.util.EventListener;

/**
 * Created by tt on 22.08.14.
 */
public interface AttemptToGetUnloadedFieldListener extends EventListener {
    void loadObject(EntityBinding entityBinding, Object object);
}
