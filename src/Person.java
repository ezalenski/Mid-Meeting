/**
 * Created by ezalenski on 8/30/16.
 */
class Person {
    private String name;
    private int origin;

    /**
     * copy constructor: copies name and origin
     * @param other
     */
    public Person( Person other) {
        this.name = other.name;
        this.origin = other.origin;
    }

    /**
     * creates a new Person with the name and vertex of the city they live in
     * @param name
     * @param origin
     */
    public Person( String name, int origin) {
        this.name = name;
        this.origin = origin;
    }

    /**
     * returns the name of the Person
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * changes the name of the Person
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * changes the city that the person lives in, takes the vertex of the new city
     * @param home
     */
    public void setOrigin(int home) {
        this.origin = home;
    }

    /**
     * returns the city that the person currently lives in.
     * @return
     */
    public int getOrigin() {
        return origin;
    }
}
