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
     * @param title the title of this print
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
     * @param title the title of this print
     * @param vector the vector to print
     */
    public void printVector(String title, Vector2 vector) {

        System.out.println(title + ": " + "(" + vector.x + "," + vector.y + ")");
    }

    /**
     * Prints an array of boolean values.
     *
     * @param title title the title of this print
     * @param booleans the boolean array to print
     */
    public void printFlags(String title, Boolean[] booleans) {

        String print = title + ": ";
        for(Boolean bool : booleans) {
            print += bool + " ";
        }

        System.out.println(print);
    }

    /**
     * Prints vertex info by printing its box coordinates and player coordinates.
     *
     * @param vert the vertex to print
     */
    public void printVertex(MyVertex vert) {

        System.out.println("Vertex");
        System.out.println("\tplayer: " + "(" + vert.getPlayer().x + "," + vert.getPlayer().y + ")");

        String print = "";
        for(Vector2 box : vert.getBoxes()) {
            print += "(" + box.x + "," + box.y + ")";
        }

        System.out.println("\tboxes: " + print);
    }

    /**
     * Prints a numeric value.
     *
     * @param title the title of this print
     * @param value the value to print
     */
    public void printInt(String title, int value) {
        System.out.println(title + ": " + value);
    }
}
