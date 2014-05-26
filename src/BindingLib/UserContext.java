package BindingLib;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 */
public class UserContext implements Iterable<UserContextItem> {
    private Dictionary<String,UserContextItem> items = new Hashtable<>();

    public Object get(String name) {
        return this.items.get(name);
    }

    public void set(String name, Object value) {
        this.items.put(name, new UserContextItem(name, value));
    }

    @Override
    public Iterator<UserContextItem> iterator() {
        return null;
    }
}
