import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Created by ezalenski on 8/30/16.
 */
public class Driver {

    public static void main(String[] args) {
        GeoMap map = new GeoMap();
        map.citiesFromFile("CityNames.txt");
        map.neighborsFromFile("CityDistances.txt");
        map.populateFromFile("Participants.txt");
        map.displayMap();
        System.out.println();
        ArrayList<Double> results = map.calculateMinDistances(58);
        for(int i = 0; i < results.size(); i++) {
            System.out.println((i+1) + ": " + map.getCityName(i+1) + " is " + results.get(i) + " units from " + map.getCityName(58));
        }
        System.out.println();
        Pair<String, Double> minAvgDistance = map.findMinAvgDistance();
        System.out.println(minAvgDistance.fst + " has the minimum average distance with " + minAvgDistance.snd + " units traveled.");
    }
}
