package BindingLib;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class StoredProcedureParametersBinding implements Iterable<SpParameterBinding> {
    private List<SpParameterBinding> parameters = new ArrayList<>();

    public List<SpParameterBinding> getParameters() {
        return this.parameters;
    }

    public SpParameterBinding get(String name) {
        throw new NotImplementedException();
    }

    @Override
    public Iterator<SpParameterBinding> iterator() {
        return this.parameters.iterator();
    }
}
