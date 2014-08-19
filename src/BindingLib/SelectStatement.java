package BindingLib;

import Exceptions.BadQueryDepthValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by tt on 18.08.14.
 */
public class SelectStatement {
    private EntityBinding entityBinding;
    private StringBuilder statement;

    public SelectStatement(EntityBinding entityBinding, int queryDeep) throws BadQueryDepthValueException {
        if (queryDeep < 0) throw new BadQueryDepthValueException();

        this.entityBinding = entityBinding;
        this.statement = new StringBuilder();

        List<Relationship> relationships = entityBinding.getRelationships();
        RelationTree tree = null;

        if (relationships != null && !relationships.isEmpty()) {
            tree = new RelationTree(entityBinding, queryDeep);
        }

        switch (entityBinding.getBindingType()) {
            case Table:
                Stream<PropertyBinding> stream = entityBinding.getProperties().stream();
                statement
                  .append("select ")
                  .append(stream.map(p -> p.getColumnName()).collect(Collectors.joining(", ")))
                  .append(" ");

                while (tree.)
                break;
            case StoredProcedure:
                break;
        }
    }

    class RelationTree {
        int relationDepth;
        Node root;

        RelationTree(EntityBinding entityBinding, int deep) {
            this.relationDepth = deep;
            this.root = new Node(entityBinding, null, 0);
        }

        class Node {
            EntityBinding entityBinding;
            List<Node> children;
            Node parent;
            int deep;

            Node(EntityBinding entityBinding, Node parent, int deep) {
                this.deep = deep;
                this.children = new ArrayList<Node>();
                this.parent = parent;

                if (deep != relationDepth) {
                    List<Relationship> relationships = entityBinding.getRelationships();
                    for (Relationship relationship : relationships) {
                        
                    }
                }



            }
        }
    }
}
