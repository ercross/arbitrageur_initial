package com.ercross.arbitrageur.adt;

import java.util.ArrayList;
import java.util.List;

public class UrlTreeMapNode<Key,Value> {

    private Key key;
    private Value value;
    private List<UrlTreeMapNode<Key, Value>> children = new ArrayList<UrlTreeMapNode<Key, Value>>();

    protected UrlTreeMapNode() {

    }

    protected UrlTreeMapNode(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

    public void addChildren(Key newKey, Value newValue) {
        children.add(new UrlTreeMapNode<Key, Value>(newKey, newValue));
    }

    public Key getKey() {
        return key;
    }
    public void setKey(Key key) {
        this.key = key;
    }
    public Value getValue() {
        return value;
    }
    public void setValue(Value value) {
        this.value = value;
    }
    public List<UrlTreeMapNode<Key, Value>> getChildren() {
        return children;
    }
    public void setChildren(List<UrlTreeMapNode<Key, Value>> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return key + ":" + value + "-> {" + children + "}";
    }
}
