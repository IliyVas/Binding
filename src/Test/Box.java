package Test;

import Annotations.*;

import java.util.List;

/**
 * Created by tt on 26.08.14.
 */
@Entity(bindingType = "StoredProsedure")
@SelectAllProcedureName("TEST.getobj")
@InsertProcedureName("TEST.insertobj")
public class Box {
    @Id
    @Column(name="id")
    private int id;
    @Column(name= "name")
    private String name;
    @OneToMany(associatedField = "box", associatedEntity = "Test.Thing")
    private List<Thing> things;


    public Box() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Thing> getThings() {
        return things;
    }

    public void setThings(List<Thing> things) {
        this.things = things;
    }
}
