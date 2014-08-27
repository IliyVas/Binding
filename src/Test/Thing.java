package Test;

import Annotations.*;

import java.util.List;

/**
 * Created by tt on 26.08.14.
 */
@Entity(bindingType = "StoredProsedure")
@SelectAllProcedureName("TEST.getthobj")
public class Thing {
    @Id
    @Column(name="id")
    private int id;
    @Column(name= "name")
    private String name;
    @ManyToOne(associatedEntity = "Test.Box", fkColumnName = "box_id")
    private List<Thing> things;

    public List<Thing> getThings() {
        return things;
    }

    public void setThings(List<Thing> things) {
        this.things = things;
    }
}
