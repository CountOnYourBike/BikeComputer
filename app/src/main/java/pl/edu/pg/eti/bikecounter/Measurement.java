package pl.edu.pg.eti.bikecounter;

import java.io.Serializable;

public class Measurement implements Serializable {
    private Integer numberOfRevolutions;
    private Integer wheelEventTime;

    Measurement(Integer numberOfRevolutions, Integer wheelEventTime) {
        this.numberOfRevolutions = numberOfRevolutions;
        this.wheelEventTime = wheelEventTime;
    }

    @Override
    public String toString() {
        return numberOfRevolutions.toString() + " " + wheelEventTime.toString();
    }

    static Measurement fromString(String s) {
        String numberOfRevString, wheelEventTimeString;
        numberOfRevString = s.split(" ")[0];
        wheelEventTimeString = s.split(" ")[1];
        return new Measurement(Integer.parseInt(numberOfRevString), Integer.parseInt(wheelEventTimeString));
    }

    double getSpeed(double circ) {    // wheelCirc w mm
        double speed = circ*numberOfRevolutions / (double)wheelEventTime; // m/s
        speed = speed * 3.6;
        return Math.floor(speed*100)/100;
    }

    Integer getNumberOfRevolutions() {
        return numberOfRevolutions;
    }

    double getDistance(double circ) { // in km
        return circ * numberOfRevolutions / 1000000.;
    }
}
