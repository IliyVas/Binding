package Test;

import BindingLib.EntityBinding;
import BindingLib.Session;
import BindingLib.StoredProcedureBinding;
import javassist.NotFoundException;

import java.util.List;

/**
 * Created by tt on 26.08.14.
 */
public class TestMain {
    public static void main(String... args) {

        Session session = new Session("jdbc:oracle:thin:@localhost:1521:XE", "SYSTEM", "123");
        session.open();
        List<Box> list = session.getAll(Box.class);
        list.forEach(e -> {
            System.out.println(e.getId()+" "+e.getName());
            e.getThings();
        });

    }
}