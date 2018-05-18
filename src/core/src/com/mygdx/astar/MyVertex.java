package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MyVertex {

    private ArrayList<Vector2> boxes;
    private Vector2 player;
    private int heuristicCost;

    public MyVertex(ArrayList<Vector2> boxes, Vector2 player) {

        this.boxes = new ArrayList<Vector2>();

        // Clone box coords
        for(Vector2 box : boxes) {
            this.boxes.add(new Vector2(box.x, box.y));
        }

        // Clone player coords
        this.player = player.cpy();
    }
}
