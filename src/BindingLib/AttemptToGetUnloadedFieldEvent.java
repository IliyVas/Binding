package BindingLib;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Created by tt on 22.08.14.
 */
class AttemptToGetUnloadedFieldEvent extends EventObject {
    List<AttemptToGetUnloadedFieldListener> listeners = new ArrayList<>();

    void addListener(AttemptToGetUnloadedFieldListener listener) {
        listeners.add(listener);
    }
}
