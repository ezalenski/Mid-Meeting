import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SkyScannerAPI {
    private final String url = "http://partners.api.skyscanner.net";
    private final String path = "/apiservices/browsequotes/v1.0/";
    private ArrayList<Quote> quotes = new ArrayList<>();
    HashMap<Integer, String> apiIdToName = new HashMap<>();

    /**
     * default constructor
     */
    public SkyScannerAPI() { }

    /**
     * Takes a json inputstream and parses it for quotes and places
     * @param source
     * @throws IOException
     */
    private void parseJsonBody(InputStream source) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(source);

        while (!parser.isClosed()) {
            JsonToken token = parser.nextToken();
            boolean quotesParsed = false, placesParsed = false;
            while (token != null && !(quotesParsed && placesParsed)) {
                token = parser.nextToken();
                if(JsonToken.FIELD_NAME.equals(token) && "Quotes".equals(parser.getCurrentName())) {
                    parseQuotes(parser);
                    quotesParsed = true;
                }
                if(JsonToken.FIELD_NAME.equals(token) && "Places".equals(parser.getCurrentName())) {
                    parsePlaces(parser);
                    placesParsed = true;
                }
            }
            parser.close();
        }
    }

    /**
     * parses the places section of the response JSON
     * @param parser
     * @throws IOException
     */
    private void parsePlaces(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        if (!JsonToken.START_ARRAY.equals(token)) {
            return;
        }
        int id = -1;
        String name;
        while(!JsonToken.END_ARRAY.equals(token)) {
            token = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(token) && "PlaceId".equals(parser.getCurrentName())) {
                token = parser.nextToken();
                id = parser.getIntValue();
            }
            if (JsonToken.FIELD_NAME.equals(token) && "CityName".equals(parser.getCurrentName())) {
                token = parser.nextToken();
                name = parser.getText();
                if(id > 0 && !name.isEmpty()) {
                    apiIdToName.put(id, name);
                    id = -1;
                }
            }
        }
    }

    /**
     * parses the quotes from the response json
     * @param parser
     * @throws IOException
     */
    private void parseQuotes(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        double cost = -1;
        int origin = -1, dest;
        if (!JsonToken.START_ARRAY.equals(token)) {
            return;
        }
        while(!JsonToken.END_ARRAY.equals(token)) {
            token = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(token) && "MinPrice".equals(parser.getCurrentName())) {
                token = parser.nextToken();
                cost = parser.getDoubleValue();
            }
            if (JsonToken.FIELD_NAME.equals(token) && "CarrierIds".equals(parser.getCurrentName())) {
                while(!JsonToken.END_ARRAY.equals(token)) {
                    token = parser.nextToken();
                }
                token = parser.nextToken();
            }
            if (JsonToken.FIELD_NAME.equals(token) && "OriginId".equals(parser.getCurrentName())) {
                token = parser.nextToken();
                origin = parser.getIntValue();
            }
            if (JsonToken.FIELD_NAME.equals(token) && "DestinationId".equals(parser.getCurrentName())) {
                token = parser.nextToken();
                dest = parser.getIntValue();
                if (origin > 0 && dest > 0 && cost > 0) {
                    Quote q = new Quote(origin, dest, cost);
                    quotes.add(q);
                    origin = dest = -1;
                    cost = -1;
                }
            }
        }
    }

    /**
     * takes a map of City Names to vertices so the function can return an
     * InputStream in the valid format for GeoMap.neighborsFromStream
     * @param cityToVertex
     * @return
     * @throws IOException
     */
    public InputStream getQuotes(HashMap<String, Integer> cityToVertex) throws IOException {
        Date today = new Date();
        SimpleDateFormat datelocale = new SimpleDateFormat("yyyy-MM");
        String params = "US/USD/en-US/US/US/" + datelocale.format(today) + "/?apiKey=<API-KEY-HERE>";
        HttpURLConnection connection = (HttpURLConnection) new URL(url + path + params).openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if(responseCode != 200) {
            System.err.println("Get request failed with error code " + responseCode);
            return null;
        }

        parseJsonBody(connection.getInputStream());
        String output = "";
        for(Quote q : quotes) {
            Integer c1 = cityToVertex.get(apiIdToName.get(q.getOrigin()));
            Integer c2 = cityToVertex.get(apiIdToName.get(q.getDestination()));
            if(c1 != null && c2 != null) {
                output += (c1+1) + " " + (c2+1) + " " + q.getCost() + '\n';
            }
        }
        return new ByteArrayInputStream(output.getBytes("UTF-8"));
    }
}

class Quote {
    private final int origin;
    private final int destination;
    private final double cost;

    /**
     * creates a quote with the PlaceIDs from the skyscanner response for both
     * origin and destination and it holds the cost for the flight
     * @param origin
     * @param destination
     * @param cost
     */
    Quote(int origin, int destination, double cost) {
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
    }

    /**
     * returns the origin placeID
     * @return
     */
    public int getOrigin() {
        return origin;
    }

    /**
     * returns the destination PlaceID
     * @return
     */
    public int getDestination() {
        return destination;
    }

    /**
     * returns the cost to travel between origin and destination
     * @return
     */
    public double getCost() {
        return cost;
    }
}

