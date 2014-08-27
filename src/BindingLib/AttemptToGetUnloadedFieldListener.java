package BindingLib;

import java.util.EventListener;

/**
 * Created by tt on 22.08.14.
 */
public interface AttemptToGetUnloadedFieldListener extends EventListener {
    void loadDependencies(Object obj, Relationship relationship);
}
