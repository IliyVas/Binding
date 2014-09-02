package Test;

import Annotations.*;

/**
 * Created by tt on 26.08.14.
 */
@Entity(bindingType = "StoredProsedure")
@SelectProcedureName("TEST.getthobj")
@InsertProcedureName("TEST.insertthobj")
public class Thing {
    @Id
    @Column(name="id")
    private int id;
    @Column(name= "name")
    private String name;
    @ManyToOne(associatedEntity = "Test.Box", fkColumnName = "box_id")
    private Box box;

    public Box getBox() {
        return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }
}
