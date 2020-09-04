package com.ercross.arbitrageur.util;

import com.ercross.arbitrageur.model.Event;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ercross
 *
 * Often times, the team names, event names, league names fetched from Flashscore is spelt differently than that found on bookmakers' site
 * This utility class will enable bookmaker navigator classes to check for the availability of such variance in a mapper (implemented using a json file)
 * peculiar to the probing bookmaker.
 * If not found, a close match to the name is found on the site using the WordMatchFinder.java algorithm
 * and the best match is recorded in the json file peculiar to that bookmaker
 *
 * The files containing the mappings are for persistence purpose only as using a database would be an overkill
 * Hence, all bookmakers mapper files content are loaded into their respective maps at startup and managed centrally from here.
 * New expectedName, actualName pair discoveries are written to mapper file as well as to the mapper objects in memory
 * This abstracts the mapping functionality from clients
 *
 * NameReplacer.writeToJSONfile() is implemented to overwrite the existing file. Therefore each mapper file will be loaded into memory (Map) at
 * application startup where it will be modified and accessed at runtime. In the case of any abrupt disruption of runtime, maybe due to a crash,
 * persistence of the maps to the respective .json files is delegated to main().
 */
public class NameReplacer {

    private static final Logger LOG = LogManager.getLogger(NameReplacer.class);

    private NameReplacer() {
        throw new IllegalStateException();
    }

    //***********************************Map content****************************************
    //*******HashMap<BookmakerName, HashMap<expectedName, actualName>***********************
    //**************************************************************************************
    private static final HashMap<String, HashMap<String,String>> mappers = new HashMap<>();

    //must be invoked in main() at startup
    public static void loadAllMapperFilesIntoMemory() {
        try {
            mappers.put("Bet9ja", loadMapperIntoMemory("Bet9ja"));
            mappers.put("Nairabet", loadMapperIntoMemory("Nairabet"));
        } catch (IOException e) {
            LOG.error("Unable to fetch one or more mapper files");
        }
    }

    /**
     * @param bookmakerName
     * @param expectedName is name found flashscore.com
     * @param actualNameFound is the name found on the bookmakerName's website
     */
    public static void saveMatchToInMemoryMapper (String bookmakerName, String expectedName, String actualNameFound) {
        mappers.get(bookmakerName).put(expectedName, actualNameFound);
    }

    //implementation based on Gson.get(key) returning null if the key is not found
    public static boolean isContainedInMapper(String expectedName, String filePath) throws IOException {
        return getActualNameFor(expectedName, filePath) != null;
    }

    /**
     * invoke isContainedInMapper before invoking this method
     * Ensure file reside in resources folder and precede file name with /
     *
     * @return actual name found on the invoking bookmaker's website
     */
    public static String getActualNameFor (String key, String filePath) throws IOException {
        String value;

        try(InputStream resourceStream = NameReplacer.class.getResourceAsStream(filePath);
            InputStreamReader iReader = new InputStreamReader(resourceStream);
            Reader reader = new BufferedReader(iReader)) {
                JsonObject parser = JsonParser.parseReader(reader).getAsJsonObject();
                value = parser.get(key).getAsString();
        }
        return value;
    }

    /**
     * A method to persist the key,value pair of expected team names, i.e., team names as found on flashscore.com, and actual names, i.e.,
     * team names often found on bookmakerName's site
     *
     * @param bookmakerName, name of the bookmaker whose mapper is to be written to
     * @param mapper, a map of key=expected_team_names and value=actual_team_names as found on bookmaker's website
     * @throws IOException if write operation is not successful
     */
    public static void persistMapperToFile(String bookmakerName, Map<String, String> mapper) throws IOException {
        writeToJSONfile(bookmakerName, mapper);
    }

    /**
     * Checks the mapper pertaining to bookmakerName for occurrence of event.homeTeamName and event.awayTeamName
     * and replace the name found on flashscore with the name often found on the bookmaker's website
     *
     * @param event
     * @param bookmakerName bookmaker whose mapper would be checked
     * @return Event object containing the modified team names as found in the bookmaker's mapper
     */
    public static Event replaceActualNamesIn (Event event, String bookmakerName) {
        Map<String, String> bookmakerMapper = mappers.get("bookmakerName");
        String homeTeamName = bookmakerMapper.get(event.getHomeTeamName());
        String awayTeamName = bookmakerMapper.get(event.getAwayTeamName());
        String countryName  = bookmakerMapper.get(event.getEventCountry());
        if ( homeTeamName != null)
            event.setHomeTeamName(homeTeamName);
        if ( awayTeamName != null)
            event.setAwayTeamName(awayTeamName);
        if ( countryName != null)
            event.setEventCountry(countryName);
        return event;
    }

    public static HashMap<String, String> loadMapperIntoMemory(String bookmakerName) throws IOException {
        //HashMap<String expectedName, String actualNameFound>**********************
        HashMap<String, String> mapper;
        String filePath = "/" + bookmakerName + ".json";
        try(InputStream resourceStream = NameReplacer.class.getResourceAsStream(filePath);
            InputStreamReader iReader = new InputStreamReader(resourceStream);
            Reader reader = new BufferedReader(iReader)) {
                Gson gson = new Gson();
                mapper = gson.fromJson(reader, HashMap.class);
        }
        return mapper;
    }

    // BookmakerName is preferred to filepath to abstract off the kind of technology used by mapper
    private static void writeToJSONfile(String bookmakerName, Map<String, String> mapper)  throws IOException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Writer writer = new PrintWriter("src/main/resources/" + bookmakerName + "NameReplacer.json")) {
                writer.append(gson.toJson(mapper));
        }
    }
}
