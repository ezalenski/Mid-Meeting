/**
 * Created by ezalenski on 8/30/16.
 */
public class Person {
    private String name;
    private int origin;

    public Person() {
        name = "";
        origin = -1;
    }

    public Person( Person other) {
        this.name = other.name;
        this.origin = other.origin;
    }

    public Person( String name, int origin) {
        this.name = name;
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrigin(int home) {
        this.origin = home;
    }

    public int getOrigin() {
        return origin;
    }
}
