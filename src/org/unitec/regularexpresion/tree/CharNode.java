package org.unitec.regularexpresion.tree;

/**
 * Created by furan on 5/12/15.
 */
public class CharNode extends Node {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CharNode(String value) {
        this.value = value;
    }

    @Override
    public String inverseExpression() {
        return value;
    }
}
