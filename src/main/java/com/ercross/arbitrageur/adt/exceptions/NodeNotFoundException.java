package com.ercross.arbitrageur.adt.exceptions;

public class NodeNotFoundException extends Exception{

    private static final long serialVersionUID = -7288672589763594075L;

    public String getMessage() {
        return "Node not found on this UrlTreeMap instance";
    }

}
