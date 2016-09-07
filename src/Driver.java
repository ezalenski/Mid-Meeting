
/* ------------------------------------------------
 * Mid Meeting
 *
 * Class: CS 342, Fall 2016
 * System: OS X, IntelliJ IDE
 * Author Code Number: 928
 *
 * -------------------------------------------------
 */
import com.sun.tools.javac.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * Created by ezalenski on 8/30/16.
 */
class Driver {

    public static void main(String[] args) {
        displayAuthorInfo();

        GeoMap map = new GeoMap();

        map.citiesFromStream(new InputStreamReader(Driver.class.getResourceAsStream("Resources/CityNames.txt")));
        map.neighborsFromStream(new InputStreamReader(Driver.class.getResourceAsStream("Resources/CityDistances.txt")), new NoWeight());
        map.populateFromStream(new InputStreamReader(Driver.class.getResourceAsStream("Resources/Participants.txt")));
        map.displayMap();

        System.out.println();
        map.printMinDistances(58);
        System.out.println();

        Pair<String, Double> minAvgDistance = map.findMinAvgDistance();
        System.out.println(minAvgDistance.fst + " has the minimum average distance with " + minAvgDistance.snd + " units traveled.");
        System.out.println();

        GeoMap costMap = new GeoMap();
        SkyScannerAPI skyScannerAPI = new SkyScannerAPI();

        costMap.citiesFromStream(new InputStreamReader(Driver.class.getResourceAsStream("Resources/CityNames.txt")));
        costMap.neighborsFromStream(new InputStreamReader(Driver.class.getResourceAsStream("Resources/CityDistances.txt")), new CalculateCostFromDistance());

        try {
            InputStream input = skyScannerAPI.getQuotes(costMap.getMapCityNamesToVertex());
            if(input != null) {
                costMap.neighborsFromStream(new InputStreamReader(input), new NoWeight());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        costMap.populateFromStream(new InputStreamReader(Driver.class.getResourceAsStream("Resources/Participants.txt")));
        minAvgDistance = costMap.findMinAvgDistance();
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println(minAvgDistance.fst + " has the minimum average cost of $" + df.format(minAvgDistance.snd) + ".");

        costMap.getMapCityNamesToVertex();
    }

    private static void displayAuthorInfo() {
        System.out.println("Author Code Number: 928\n" +
                "Class: CS 342, Fall 2016\n" +
                "Program: #1, Mid Meeting\n");
    }
}

/**
 * CostFunction that doesn't do anything
 */
class NoWeight implements CostFunction {
    @Override
    public double calculate(double originalWeight) {
        return originalWeight;
    }
}

/**
 * CostFunction that converts miles to USD
 */
class CalculateCostFromDistance implements CostFunction {
    public double calculate(double originalWeight) {
        return (.6 * originalWeight);
    }
}