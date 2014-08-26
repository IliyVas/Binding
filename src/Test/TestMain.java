package Test;

import BindingLib.EntityBinding;
import BindingLib.StoredProcedureBinding;
import javassist.NotFoundException;

/**
 * Created by tt on 26.08.14.
 */
public class TestMain {
    public static void main(String... args) {

            EntityBinding eb = new StoredProcedureBinding(Box.class);

    }
}
