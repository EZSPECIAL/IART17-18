package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class DebugPrint {

    private static DebugPrint singleton = new DebugPrint();

    /**
     * Private constructor for singleton pattern.
     */
    private DebugPrint() {}

    /**
     * @return the singleton instance of the class
     */
    public static DebugPrint getInstance() {
        return singleton;
    }

    /**
     * Prints a list of coordinates for debugging.
     *
     * @param title the title of this list
     * @param list the list to print
     */
    public void printVectorList(String title, ArrayList<Vector2> list) {

        String print = title + ": ";
        for(Vector2 value : list) {
            print += "(" + value.x + "," + value.y + ")";
        }

        System.out.println(print);
    }

    /**
     * Prints a 2D vector for debugging.
     *
     * @param title the title of this list
     * @param vector the vector to print
     */
    public void printVector(String title, Vector2 vector) {

        System.out.println(title + ": " + vector.x + "," + vector.y);
    }

    // TODO doc
    public void printFlags(String title, ArrayList<Boolean> booleans) {

        String print = title + ": ";
        for(Boolean bool : booleans) {
            print += bool + " ";
        }

        System.out.println(print);
    }
}
