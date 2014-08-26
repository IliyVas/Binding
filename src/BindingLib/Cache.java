package BindingLib;

import java.util.List;
import java.util.Map;

/**
 * Created by tt on 21.08.14.
 */
public class Cache {
    //TODO: возможно стоит связать кеш с Class
    private Map<EntityBinding, List> cachedObjectLists;
    private Map<EntityBinding,Boolean> isSelectAllExecuted;
}
