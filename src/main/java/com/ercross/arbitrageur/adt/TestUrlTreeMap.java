package com.ercross.arbitrageur.adt;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;

public class TestUrlTreeMap {

    private UrlTreeMap<String,String> urlTreeMap = new UrlTreeMap<String, String>("Sport", "This is my URL");
    private UrlTreeMapNode<String, String> newSportTypeNode = new UrlTreeMapNode<String, String>("Soccer", "This is Bet9ja's soccer page URL");

    @Test
    public void testIsContains() throws NodeNotFoundException {
        urlTreeMap.add(null, "Sport", newSportTypeNode);
        boolean actualResult = urlTreeMap.contains("Soccer", "Soccer");
        assertTrue(actualResult);
    }

    @Test
    public void testAddSportTypeNode () {
        UrlTreeMapNode<String, String> node = new UrlTreeMapNode<String, String>();
        try {
            urlTreeMap.add(null, "Sport", newSportTypeNode);
            node = urlTreeMap.getNode("Soccer", "Soccer");
        } catch (NodeNotFoundException e) {
            System.out.println("this code should never be called");
        }
        assertEquals(node.getKey(),"Soccer");
        assertEquals(node.getValue(), "This is Bet9ja's soccer page URL");
    }

    @Test
    public void testGetNodeForLevel3Nodes() {
        UrlTreeMapNode<String, String> node = new UrlTreeMapNode<String, String>();
        try {
            urlTreeMap.add(null, "Sport", newSportTypeNode);
            urlTreeMap.add("Soccer", "Soccer", "England", "England's soccer webpage url");
            urlTreeMap.add("Soccer", "England", "Premier League", "Premier League url" );
            node = urlTreeMap.getNode("Soccer", "Premier League");
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        assertEquals(node.getKey(),"Premier League");
        assertEquals(node.getValue(), "Premier League url");
    }

    @Test
    public void testGetNodeThrowsException() {
        Exception exception = assertThrows(NodeNotFoundException.class, () -> {
            urlTreeMap.getNode("Basketball", "NBA");
        });
        String expectedMessage = "Node not found on this UrlTreeMap instance";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(expectedMessage));
    }
}
