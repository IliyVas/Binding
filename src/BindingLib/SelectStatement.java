package BindingLib;

import Exceptions.BadQueryDepthValueException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public SelectStatement(EntityBinding entityBinding, JoiningStrategy joiningStrategy, int queryDepth) throws BadQueryDepthValueException {
        if (queryDepth < 0) throw new BadQueryDepthValueException();

        this.entityBinding = entityBinding;
        this.statement = new StringBuilder();

        List<Relationship> relationships = entityBinding.getRelationships();
        RelationTree tree = null;

        if (relationships != null && !relationships.isEmpty()) {
            tree = new RelationTree(entityBinding, queryDepth);
        }

        switch (entityBinding.getBindingType()) {
            case Table:
                Stream<PropertyBinding> stream = entityBinding.getProperties().stream();
                //TODO: проверить что быстрее joining или append через итерацию
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
        JoiningStrategy joiningStrategy;
        Node root;
        StringBuilder columns;
        StringBuilder joins;
        List<String> usedJoiningTables;
        List<Relationship> usedRelashinships;
        List<String> usedTableNames;


        RelationTree(EntityBinding entityBinding, JoiningStrategy joiningStrategy, int deep) {
            this.relationDepth = deep;
            this.joiningStrategy = joiningStrategy;
            this.root = new Node(entityBinding, null, 0);
            this.usedJoiningTables = new ArrayList<>();
            this.usedRelashinships = new ArrayList<>();
            this.usedTableNames = new ArrayList<>();
            this.columns = new StringBuilder();
            this.joins = new StringBuilder();
        }

        class Node {
            EntityBinding entityBinding;
            String tableNameOrAllias;
            List<Node> children;
            Node parent;
            int deep;

            Node(EntityBinding entityBinding, Node parent, String tableName, int deep) {
                this.deep = deep;
                this.children = new ArrayList<Node>();
                this.parent = parent;
                this.entityBinding = entityBinding;
                this.tableNameOrAllias = tableName;

                List<Relationship> relationships = entityBinding.getRelationships();

                switch (joiningStrategy) {
                    case full:
                        //TODO: проверить left join == left outer join && a as b == a b
                        for (Relationship relationship : relationships) {

                            Stream<PropertyBinding> stream;
                            EntityBinding associatedEntity = relationship.getAssociatedEntity(entityBinding);

                            switch (relationship.getType(entityBinding)) {
                                case OneToMany:

                                    stream = associatedEntity.getProperties().stream();
                                    String associatedTableName = associatedEntity.getTableName();
                                    String associatedTableNameOrAlias = tableNameOrAlias(tableName);

                                    columns.append(
                                        stream.map(p -> associatedTableNameOrAlias + '.' + p.getColumnName())
                                        .collect(Collectors.joining(", "))
                                    );

                                    String id = entityBinding.getIdentifier().getColumnName();

                                    //TODO:(JFF) проверить что быстрее: .append.append || .append(+) && ' ' || " "
                                    joins.append("left join ").append(associatedTableName).append(' ')
                                    .append(associatedTableName == associatedTableNameOrAlias ? "" : associatedTableNameOrAlias)
                                    .append(" on ").append(tableNameOrAllias).append('.').append(id).append(" = ")
                                    .append(associatedTableNameOrAlias).append('.')
                                    .append(relationship.getColumnName(associatedEntity))
                                    .append(' ');

                                    usedRelashinships.add(relationship);
                                    this.children.add(new Node(associatedEntity, this, associatedTableNameOrAlias, 0));

                                    break;

                                case ManyToOne:
                                    stream = associatedEntity.getProperties().stream();
                                    //TODO: дописать или переписать
                                    break;

                            }
                        }

                }



            }

            String tableNameOrAlias(String name) {
                if (usedTableNames.contains(name))
                    return name + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("mmssnnnnnnnnn"));
                else {
                    usedTableNames.add(name);
                    return name;
                }
            }
        }
    }
}
