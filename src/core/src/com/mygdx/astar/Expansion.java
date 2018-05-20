package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

public class Expansion {

    private Vector2 coords;
    private int dist;

    /**
     * Constructs an Expansion object which stores coordinates and distance
     * from goal for the current Pukoban board.
     *
     * @param coords the coordinates to use
     * @param dist the distance from the goal for this expansion
     */
    public Expansion(Vector2 coords, int dist) {
        this.coords = coords;
        this.dist = dist;
    }

    /**
     * @return the coordinates of this expansion
     */
    public Vector2 getCoords() {
        return this.coords;
    }

    /**
     * @return the distance from the goal for this expansion
     */
    public int getDist() {
        return this.dist;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj.getClass().getPackage().equals(this.getClass().getPackage())
                && obj.getClass().getName().equals(this.getClass().getName())) {

            Expansion exp = (Expansion) obj;

            if(this.coords.equals(exp.coords)) return true;
            else return false;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (int) (this.coords.x + this.coords.y);
    }
}