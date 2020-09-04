package com.ercross.arbitrageur.test.util;

import com.ercross.arbitrageur.util.MarketExtraction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMarketExtraction {

    private static Map<String,String> regexes = new HashMap<>();

    @Test
    public void testFetchResource() {
        final String resourceFile = "/Bet9jaRegex.txt";
        InputStream resourceStream = TestMarketExtraction.class.getResourceAsStream(resourceFile);
        assertTrue(resourceStream != null);
    }

    @Test
    public void testPutRegularExpressionToMapFrom() throws IOException {
        final String resourceFile = "/Bet9jaRegex.txt";
        InputStream resourceStream = TestMarketExtraction.class.getResourceAsStream(resourceFile);
        Map<String,String> regexes = MarketExtraction.putRegularExpressionsToMapFrom(resourceStream);
        assertTrue(regexes.isEmpty() == false);
    }
}
