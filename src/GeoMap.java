import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by ezalenski on 8/30/16.
 */
public class GeoMap {
    private ArrayList<City> cities;
    private PriorityQueue<Cost> costQueue;
    private ArrayList<City> populatedCities;

    public GeoMap() {
        cities = new ArrayList<City>();
        populatedCities = new ArrayList<City>();
    }

    public GeoMap( GeoMap other ) {
        this.cities = new ArrayList<City>( other.cities );
        this.populatedCities = new ArrayList<City>( other.populatedCities );
    }

    public void citiesFromFile(String filename) {
        try {
            Scanner s = new Scanner( new File(filename));
            int numCities = s.nextInt();
            s.nextLine();
            int count = 0;
            while( numCities > 0 ) {
                String name = s.nextLine();
                City n = new City(count, name);
                cities.add(count, n);
                count++;
                numCities--;
            }
        } catch(IOException e) {
            System.err.println( e.getMessage());
        }
    }

    public void neighborsFromFile(String filename) {
        try {
            Scanner s = new Scanner( new File(filename));
            int numEdges = s.nextInt();
            s.nextLine();
            while( numEdges > 0 ) {
                int v1 = s.nextInt();
                int v2 = s.nextInt();
                double weight = s.nextDouble();
                City c1 = cities.get(v1-1);
                City c2 = cities.get(v2-1);
                c1.addNeighbor(c2, weight);
                c2.addNeighbor(c1, weight);
                numEdges--;
            }
        }catch(IOException e) {
            System.err.println( e.getMessage());
        }
    }

    public void populateFromFile(String filename) {
        try {
            Scanner s = new Scanner( new File(filename));
            int numPeople = s.nextInt();
            while( numPeople > 0 ) {
                String name = s.next();
                int id = s.nextInt();
                City city = cities.get(id-1);
                city.addPerson(name);
                if(!populatedCities.contains(city)) {
                    populatedCities.add(city);
                }
                numPeople--;
            }
        }catch(IOException e) {
            System.err.println( e.getMessage());
        }
    }

    public void displayMap() {
        for (City city : cities ) {
            System.out.print(city.vertex + ": " + city.name + " - " + city.getSize());
            ArrayList<Neighbor> neighbors = city.getNeighbors();
            for(Neighbor neighbor : neighbors) {
                System.out.print(" -> " + cities.get(neighbor.vertex).name + " Weight: " + neighbor.weight);
            }
            System.out.print('\n');
        }
    }

    public ArrayList<Double> calculateMinDistances(int origin) {
        costQueue = new PriorityQueue<Cost>();
        ArrayList<Cost> costList = new ArrayList<Cost>();
        for(City city : cities) {
            Cost cityCost = new Cost(city, Double.MAX_VALUE);
            costList.add(city.vertex, cityCost);
        }

        City start = cities.get(origin-1);
        Cost startCost = costList.get(origin-1);
        startCost.setDone(true);
        startCost.cost = 0;

        ArrayList<Neighbor> neighbors = start.getNeighbors();
        for(Neighbor neighbor : neighbors) {
            Cost neighborCost = costList.get(neighbor.vertex);
            neighborCost.cost = neighbor.weight;
            costQueue.add(neighborCost);
        }

        while(!costQueue.isEmpty()) {
            Cost smallestCost = costQueue.poll();
            smallestCost.setDone(true);
            City dest = smallestCost.getDest();
            neighbors = dest.getNeighbors();

            for(Neighbor neighbor : neighbors) {
                double newCost = smallestCost.cost + neighbor.weight;
                Cost neighborCost = costList.get(neighbor.vertex);
                if(!neighborCost.isDone() && (Double.compare(newCost, costList.get(neighbor.vertex).cost) == -1)) {
                    if (costQueue.contains(neighborCost)) {
                        costQueue.remove(neighborCost);
                    }
                    neighborCost.cost = newCost;
                    costQueue.add(neighborCost);
                }
            }
        }
        ArrayList<Double> ret = new ArrayList<Double>();
        for(Cost cost : costList) {
            ret.add(cost.cost);
        }
        return ret;
    }

    public String getCityName(int i) {
        return cities.get(i-1).name;
    }

    public Pair<String, Double> findMinAvgDistance() {
        double minTotal = Double.MAX_VALUE;
        int minVertex = -1;
        for(City city : cities) {
            double avgTotal = 0.0;
            int totalSize = 0;
            ArrayList<Double> results = calculateMinDistances(city.vertex+1);
            for(City populated : populatedCities) {
                avgTotal += (populated.getSize() * results.get(populated.vertex));
                totalSize += populated.getSize();
            }
            avgTotal = avgTotal / totalSize;
            if(Double.compare(avgTotal, minTotal) == -1) {
                minTotal = avgTotal;
                minVertex = city.vertex;
            }
        }
        return new Pair<String, Double>(cities.get(minVertex).name, minTotal);
    }

    private class Cost implements Comparator<Cost>, Comparable<Cost>{
        private City dest;
        double cost;
        private boolean done;

        Cost() {
            dest = null;
            cost = 1;
            done = false;
        }

        Cost(City dest, double cost) {
            this.dest = dest;
            this.cost = cost;
            done = false;
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        public boolean isDone() {
            return done;
        }

        public City getDest() {
            return dest;
        }

        @Override
        public int compareTo(Cost other) {
            return Double.compare(this.cost, other.cost);
        }

        @Override
        public int compare(Cost lhs, Cost rhs) {
            return Double.compare(lhs.cost, rhs.cost);
        }
    }
}

class City {
    public int vertex;
    public String name;
    private ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
    private ArrayList<Person> population = new ArrayList<Person>();

    public City() {
        vertex = -1;
        name = "";
    }

    public City( City other ) {
        this.vertex = other.vertex;
        this.name = other.name;
        this.neighbors = new ArrayList<Neighbor>(other.neighbors);
        this.population = new ArrayList<Person>(other.population);
    }

    public City( int vertex, String name ) {
        this.vertex = vertex;
        this.name = name;
    }

    public boolean addNeighbor(City other, double weight) {
        Neighbor adj = new Neighbor(other.vertex, weight);
        return neighbors.add(adj);
    }

    public boolean addPerson(String name) {
        Person inhabitant = new Person(name, this.vertex);
        return population.add(inhabitant);
    }

    public int getSize() {
        return population.size();
    }

    public ArrayList<Neighbor> getNeighbors() {
        return neighbors;
    }
}

class Neighbor {
    public int vertex;
    public double weight;

    Neighbor() {
        vertex = -1;
        weight = 1;
    }

    Neighbor( Neighbor other ) {
        this.vertex = other.vertex;
        this.weight = other.weight;
    }

    Neighbor(int vertex, double weight) {
        this.vertex = vertex;
        this.weight = weight;
    }
}