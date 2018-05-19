package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MyVertex {

    private ArrayList<Vector2> boxes;
    private Vector2 player;
    private int heuristicCost;
    private int depth; // TODO remove?

    /**
     * Constructs a MyVertex object which stores the Pukoban board state by
     * copying all the box and player coordinates received.
     *
     * @param boxes the the list of box coordinates
     * @param pCoords the player coordinates
     */
    public MyVertex(ArrayList<Vector2> boxes, Vector2 pCoords) {

        this.boxes = new ArrayList<Vector2>();

        // Clone box coords
        for(Vector2 box : boxes) {
            this.boxes.add(new Vector2(box.x, box.y));
        }

        // Clone player coords
        this.player = pCoords.cpy();
    }

    /**
     * @return the list of boxes for this Pukoban board state
     */
    public ArrayList<Vector2> getBoxes() {
        return this.boxes;
    }

    /**
     * @return the player coordinates for this Pukoban board state
     */
    public Vector2 getPlayer() {
        return this.player;
    }
}
