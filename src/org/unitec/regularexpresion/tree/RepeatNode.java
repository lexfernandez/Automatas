package org.unitec.regularexpresion.tree;

/**
 * Created by furan on 5/12/15.
 */
public class RepeatNode extends Node {
    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public RepeatNode(Node node) {
        this.node = node;
    }

    @Override
    public String inverseExpression() {
        return "("+ node.inverseExpression() +")*";
    }
}
