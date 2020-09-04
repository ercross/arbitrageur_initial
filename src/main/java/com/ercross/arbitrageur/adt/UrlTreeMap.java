package com.ercross.arbitrageur.adt;
//adt is short for abstract data structure

import java.util.ArrayDeque;
import java.util.Queue;

import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;

/**
 * @author ercross
 *
 * A N-ary Tree ADT with two elements field, a key-value pair where each key is mapped to a value.
 * Neither the key nor the value can be null. Tree can contain duplicate keys or value, but a parent node can't contain duplicate children or grandchildren
 * Since this tree is custom for this arbitrageur app, its content is often
 * rootNode = Sport (unique key and value, duplicates of which cannot be found elsewhere on the tree)
 * level one Nodes = sport types (unique key and value, duplicates of which cannot be found elsewhere on the tree)
 * level two nodes = event countries (non-unique values and keys, could be found under another sport type)
 * level three nodes = league names (non-unique values and keys, could be found under another sport type)
 */
public class UrlTreeMap<Key, Value> {

    private UrlTreeMapNode<Key, Value> rootNode;

    public UrlTreeMap() {
        rootNode = null;
    }

    public UrlTreeMap(Key rootNodeKey, Value rootNodeValue) {
        this.rootNode = new UrlTreeMapNode<>(rootNodeKey, rootNodeValue);
    }

    /**
     * clears out the tree by setting rootNode equals null
     * Since there is no further reference to such tree instance, it gets garbage collected
     */
    public void clear() {
        rootNode = null;
    }

    public UrlTreeMapNode<Key, Value> getRootNode (){
        return rootNode;
    }

    public Value getValue(Key sportTypeKey, Key requiredNodeKey) throws NodeNotFoundException {
        return getNode(sportTypeKey, requiredNodeKey).getValue();
    }

    public void replaceValue(Key sportTypeKey, Key requiredNodeKey, Value newValue) throws NodeNotFoundException {
        getNode(sportTypeKey, requiredNodeKey).setValue(newValue);
    }

    public void add(Key sportTypeKey, Key parentKey, UrlTreeMapNode<Key, Value> newNode) throws NodeNotFoundException {
        this.add(sportTypeKey, parentKey, newNode.getKey(), newNode.getValue());
    }

    /**
     * The first two params are to specify the path to which the new node is to be added since nodes on level three, countries, are duplicates
     *
     * @param newNodeKey
     * @param newNodeValue
     * @throws NodeNotFoundException
     */
    public void add(Key sportTypeKey, Key parentKey, Key newNodeKey, Value newNodeValue) throws NodeNotFoundException {
        if (rootNode == null) {
            this.rootNode.setKey(newNodeKey);
            this.rootNode.setValue(newNodeValue);
            return;
        }

        //adds the new node as a direct child of the rootNode if the supplied key is the same as the rootNode's key
        if (sportTypeKey == null & parentKey == rootNode.getKey()) {
            rootNode.addChildren(newNodeKey, newNodeValue);
            return;
        }

        //gets the parentNode and adds a new node which contains newNodeKey and newNodeValue
        getNode(sportTypeKey, parentKey).addChildren(newNodeKey, newNodeValue);
    }

    /**
     * Uses breath-first search algorithm to check if this instance of UrlTreeMap contains a node having the requiredKey under the sportTypeKey
     * @return true if the node is contained in this instance of UrlTreeMap
     */
    public boolean contains(Key sportTypeKey, Key requiredNodeKey) {

        //Gets the sportType to in order to minimize the requiredKey search runtime. Returns false if the sportTypeKey is not found on tree
        UrlTreeMapNode<Key, Value> sportTypeNode;
        try {
            sportTypeNode = getSportTypeNode(sportTypeKey);
        }
        catch (NodeNotFoundException e) {
            return false;
        }

        //the breadth-first algorithm
        Queue <UrlTreeMapNode<Key, Value>> queue = new ArrayDeque<>();
        queue.add(sportTypeNode);
        UrlTreeMapNode<Key, Value> currentNode;
        while (!(queue.isEmpty())) {
            currentNode = queue.remove();
            if (currentNode.getKey() == requiredNodeKey) {
                return true;
            }else {
                if (currentNode.getChildren() != null) {
                    queue.addAll(currentNode.getChildren());
                }
            }
        }
        return false;
    }

    protected UrlTreeMapNode<Key, Value> getNode (Key sportTypeKey, Key requiredNodeKey) throws NodeNotFoundException {
        //returns rootNode if the requiredNodeKey matches the key of the rootNode
        if (sportTypeKey==null & requiredNodeKey == rootNode.getKey()) {
            return rootNode;
        }

        //if the condition is true, then the required node is a direct childNode of rootNode, i.e., a level one node
        if (sportTypeKey == requiredNodeKey) {
            return getSportTypeNode(requiredNodeKey);
        }
        return getByBreadthFirstSearch(sportTypeKey, requiredNodeKey);
    }

    private UrlTreeMapNode<Key, Value> getByBreadthFirstSearch(Key startSearchFromThisNode, Key requiredNodeKey) throws NodeNotFoundException {
        Queue <UrlTreeMapNode<Key, Value>> queue = new ArrayDeque<>();
        queue.add(getSportTypeNode(startSearchFromThisNode));
        UrlTreeMapNode<Key, Value> currentNode;
        while(!(queue.isEmpty())) {
            currentNode = queue.remove();
            if(currentNode.getKey() == requiredNodeKey) {
                return currentNode;
            }
            else if (currentNode.getChildren() != null) {
                queue.addAll(currentNode.getChildren());
            }
        }
        throw new NodeNotFoundException();
    }

    private UrlTreeMapNode<Key, Value> getSportTypeNode (Key requiredSportTypeKey) throws NodeNotFoundException {
        for(UrlTreeMapNode<Key, Value> sportTypeNode: rootNode.getChildren()) {
            if (sportTypeNode.getKey() == requiredSportTypeKey) {
                return sportTypeNode;
            }
        }
        throw new NodeNotFoundException();
    }
}
