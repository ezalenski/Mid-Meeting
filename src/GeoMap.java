import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * interface to allow different costfunctions to be passed to the neighbor function
 */
interface CostFunction {
    double calculate(double originalWeight);
}

/**
 * Created by ezalenski on 8/30/16.
 */
class GeoMap {
    private final ArrayList<City> cities;
    private final ArrayList<City> populatedCities;

    /**
     * Default GeoMap with empty cities and populated cities
     */
    public GeoMap() {
        cities = new ArrayList<>();
        populatedCities = new ArrayList<>();
    }

    /**
     * copy constructor
     * @param other
     */
    public GeoMap( GeoMap other ) {
        this.cities = new ArrayList<>( other.cities );
        this.populatedCities = new ArrayList<>( other.populatedCities );
    }

    public HashMap<String, Integer> getMapCityNamesToVertex() {
        HashMap<String, Integer> ret = new HashMap<>();
        for(City c : cities) {
            ret.put(c.name.substring(0, c.name.indexOf(',')), c.vertex);
        }
        return ret;
    }

    /**
     * populates map with cities from inputStreamReader
     * @param inputStreamReader
     */
    public void citiesFromStream(InputStreamReader inputStreamReader) {
        try {
            BufferedReader br = new BufferedReader(inputStreamReader);
            String input;
            int count = 0;
            br.readLine();
            while((input = br.readLine()) != null) {
                if(!input.isEmpty()) {
                    City n = new City(count, input);
                    cities.add(count, n);
                    count++;
                }
            }
        } catch(IOException e) {
            System.err.println( e.getMessage());
        }
    }

    /**
     * populates neighbors/edges of cities from inputStreamReader
     * @param inputStreamReader
     */
    public void neighborsFromStream(InputStreamReader inputStreamReader, CostFunction cost) {
        try {
            BufferedReader br = new BufferedReader(inputStreamReader);
            br.readLine();
            String input;
            while((input = br.readLine()) != null) {
                if(!input.isEmpty()) {
                    StringTokenizer results = new StringTokenizer(input, " ");

                    int v1 = Integer.parseInt(results.nextToken());
                    int v2 = Integer.parseInt(results.nextToken());
                    double weight = Double.parseDouble(results.nextToken());
                    City c1 = cities.get(v1 - 1);
                    City c2 = cities.get(v2 - 1);
                    c1.addNeighbor(c2, cost.calculate(weight));
                    c2.addNeighbor(c1, cost.calculate(weight));
                }
            }
        }catch(IOException e) {
            System.err.println( e.getMessage());
        }
    }

    /**
     * populates cities with people from inputStreamReader
     * @param inputStreamReader
     */
    public void populateFromStream(InputStreamReader inputStreamReader) {
        try {
            BufferedReader br = new BufferedReader(inputStreamReader);
            br.readLine();
            String input;
            while((input = br.readLine()) != null) {
                if(!input.isEmpty()) {
                    StringTokenizer results = new StringTokenizer(input, " ");
                    String name = results.nextToken();
                    int id = Integer.parseInt(results.nextToken());
                    City city = cities.get(id - 1);
                    city.addPerson(name);
                    if (!populatedCities.contains(city)) {
                        populatedCities.add(city);
                    }
                }
            }
        }catch(IOException e) {
            System.err.println( e.getMessage());
        }
    }

    /**
     * Displays all the information in the format of
     * "{city id}: {city name} - {city population} -> {neighbors name} Weight: {neighbor weight} -> ..."
     */
    public void displayMap() {
        for (City city : cities ) {
            System.out.print(city.vertex + ": " + city.name + " - " + city.getSize());
            ArrayList<Neighbor> neighbors = city.getNeighbors();
            for(Neighbor neighbor : neighbors) {
                System.out.print(" -> " + neighbor.adj.name + " Weight: " + neighbor.weight);
            }
            System.out.print('\n');
        }
    }

    /**
     * Calculates the minimum distances from a city specified by the origin(vertex/id)
     * @param origin
     * @return an arraylist of costs where the index of each cost is the corresponding
     * city's vertex
     */
    private ArrayList<Cost> calculateMinDistances(int origin) {
        PriorityQueue<Cost> costQueue = new PriorityQueue<>();
        ArrayList<Cost> costList = new ArrayList<>();
        for(City city : cities) {
            Cost cityCost = new Cost(city, Double.MAX_VALUE);
            costList.add(city.vertex, cityCost);
        }

        City start = cities.get(origin);
        Cost startCost = costList.get(origin);
        startCost.setDone();
        startCost.cost = 0;

        ArrayList<Neighbor> neighbors = start.getNeighbors();
        for(Neighbor neighbor : neighbors) {
            Cost neighborCost = costList.get(neighbor.adj.vertex);
            neighborCost.cost = neighbor.weight;
            costQueue.add(neighborCost);
        }

        while(!costQueue.isEmpty()) {
            Cost smallestCost = costQueue.poll();
            smallestCost.setDone();
            City dest = smallestCost.getDest();
            neighbors = dest.getNeighbors();

            for(Neighbor neighbor : neighbors) {
                double newCost = smallestCost.cost + neighbor.weight;
                Cost neighborCost = costList.get(neighbor.adj.vertex);
                if(!neighborCost.isDone() && (Double.compare(newCost, costList.get(neighbor.adj.vertex).cost) == -1)) {
                    if (costQueue.contains(neighborCost)) {
                        costQueue.remove(neighborCost);
                    }
                    neighborCost.cost = newCost;
                    costQueue.add(neighborCost);
                }
            }
        }
        return costList;
    }

    /**
     *  returns the String name of the city at the given vertex
     * @param vertex
     * @return
     */
    public String getCityName(int vertex) {
        return cities.get(vertex).name;
    }

    /**
     * finds the midpoint and the minimum average distance of the current map given the cities and population
     * returns a pair which contains the name of the midpoint and the total distance travelled.
     * @return
     */
    public Pair<String, Double> findMinAvgDistance() {
        double minTotal = Double.MAX_VALUE;
        int minVertex = -1;
        for(City city : cities) {
            double avgTotal = 0.0;
            int totalSize = 0;
            ArrayList<Cost> results = calculateMinDistances(city.vertex);
            for(City populated : populatedCities) {
                avgTotal += (populated.getSize() * results.get(populated.vertex).cost);
                totalSize += populated.getSize();
            }
            avgTotal = avgTotal / totalSize;
            if(Double.compare(avgTotal, minTotal) == -1) {
                minTotal = avgTotal;
                minVertex = city.vertex;
            }
        }
        return new Pair<>(cities.get(minVertex).name, minTotal);
    }

    /**
     * prints all the minimum distances from target in alphabetic order
     * @param target
     */
    public void printMinDistances(int target) {
        ArrayList<Cost> results = calculateMinDistances(target-1);
        Collections.sort(results, (Cost c1, Cost c2) -> c1.getDest().name.compareToIgnoreCase(c2.getDest().name));
        for(Cost c : results) {
            System.out.println(c.getDest().name + " is " + c.cost + " units from " + getCityName(target-1));
        }
    }

    private class Cost implements Comparator<Cost>, Comparable<Cost>{
        private final City dest;
        double cost;
        private boolean done;

        /**
         * creates a Cost with the cost for going to the dest city
         * @param dest
         * @param cost
         */
        Cost(City dest, double cost) {
            this.dest = dest;
            this.cost = cost;
            done = false;
        }

        /**
         * Set the Cost as done/visited
         */
        public void setDone() {
            this.done = true;
        }

        /**
         * returns if the Cost is done/visited
         * @return
         */
        public boolean isDone() {
            return done;
        }

        /**
         * returns the City that is the destination of the Cost
         * @return
         */
        public City getDest() {
            return dest;
        }

        /**
         * Compares based doubles
         * @param other
         * @return
         */
        @Override
        public int compareTo(Cost other) {
            return Double.compare(this.cost, other.cost);
        }

        /**
         * compares based on double value
         * @param lhs
         * @param rhs
         * @return
         */
        @Override
        public int compare(Cost lhs, Cost rhs) {
            return Double.compare(lhs.cost, rhs.cost);
        }
    }
}

class City {
    public final int vertex;
    public final String name;
    private ArrayList<Neighbor> neighbors = new ArrayList<>();
    private ArrayList<Person> population = new ArrayList<>();

    /**
     * copy constructor, copies vertex, name, neighbors and population
     * @param other
     */
    public City( City other ) {
        this.vertex = other.vertex;
        this.name = other.name;
        this.neighbors = new ArrayList<>(other.neighbors);
        this.population = new ArrayList<>(other.population);
    }

    /**
     * creates a new city with vertex and name
     * @param vertex
     * @param name
     */
    public City( int vertex, String name ) {
        this.vertex = vertex;
        this.name = name;
    }

    /**
     * adds a city as a neighbor with a given weight
     * @param other
     * @param weight
     * @return
     */
    public boolean addNeighbor(City other, double weight) {
        Neighbor adj = new Neighbor(other, weight);
        return neighbors.add(adj);
    }

    /**
     * adds a person to the cities population with the String name
     * @param name
     * @return
     */
    public boolean addPerson(String name) {
        Person inhabitant = new Person(name, this.vertex);
        return population.add(inhabitant);
    }

    /**
     * returns the population of the city
     * @return
     */
    public int getSize() {
        return population.size();
    }

    /**
     * returns an arraylist of neighbors
     * @return
     */
    ArrayList<Neighbor> getNeighbors() {
        return neighbors;
    }
}

class Neighbor {
    public final City adj;
    public final double weight;

    /**
     * Copy constructor: copies the vertex and weight of the other neighbor
     * @param other
     */
    Neighbor( Neighbor other ) {
        this.adj = other.adj;
        this.weight = other.weight;
    }

    /**
     * creates a neighbor containing the corresponding City and the weight for that neighbor
     * @param adj
     * @param weight
     */
    Neighbor(City adj, double weight) {
        this.adj = adj;
        this.weight = weight;
    }
}