package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

public class AStarAlgo {

    private AStar astar;

    // A* lists
    private LinkedList<MyVertex> openList = new LinkedList<MyVertex>();
    private HashSet<MyVertex> closedList = new HashSet<MyVertex>();

    /**
     * Constructs an AStarAlgo object responsible for running
     * the A* algorithm on the current map.
     *
     * @param astar the main class instance
     */
    public AStarAlgo(AStar astar) {
        this.astar = astar;
    }

    /**
     * Runs the A* algorithm using the current loaded map.
     *
     * @param useTurns whether to consider turns in the heuristic cost
     * @param turnCost the cost of boxes changing direction
     */
    public ArrayList<MyVertex> runAlgorithm(boolean useTurns, int turnCost) {

        ArrayList<Vector2> boxes = astar.getBoxes();
        Vector2 pCoords = astar.getPlayer();

        // Compute f() for the starting vertex
        MyVertex rootVertex = new MyVertex(this.astar, boxes, pCoords);

        if(useTurns) rootVertex.computeHeuristicWithTurns(turnCost);
        else rootVertex.computeHeuristic();

        rootVertex.setgCost(0);
        rootVertex.computeFCost();

        // Add root vertex and run the iterations
        this.openList.add(rootVertex);

        // Find solution
        MyVertex runVert;
        while(true) {
            if(this.openList.size() <= 0) System.out.println("Error: no open list remains!");
            runVert = this.openList.get(0);
            if(runVert.checkGoal()) {
                this.closedList.add(runVert);
                this.openList.remove(runVert);
                break;
            }
            this.doIterations(runVert, useTurns, turnCost);
        }

        System.out.println("Found path");

        return this.backtrack(runVert);
    }

    /**
     * Finds the path from the goal state to the initial state by backtracking.
     *
     * @param goalState the solution map state
     * @return the list of map states to get from the start to the finish of the Pukoban puzzle
     */
    private ArrayList<MyVertex> backtrack(MyVertex goalState) {

        ArrayList<MyVertex> verts = new ArrayList<MyVertex>();
        verts.add(goalState);

        MyVertex currVert = goalState;

        while(true) {

            // Add parents to list
            MyVertex parent = currVert.getParent();
            if(parent != null) {
                if(AStar.debugFlag) DebugPrint.getInstance().printVertex(parent);
                verts.add(parent);
                currVert = parent;
            } else break;
        }

        return verts;
    }

    /**
     * Computes the possible moves from the given vertex and for each possible move computes
     * the f() cost and sets the parent vertex. If the possible move is already in the closed
     * list it is ignored. If the possible move is already in the open list but not in the
     * closed list, f() gets updated if the current g() is better through this path, also updating
     * the parent.
     *
     * @param currVert the vertex to consider for this iteration
     * @param useTurns whether to consider turns in the heuristic cost
     * @param turnCost the cost of boxes changing direction
     */
    private void doIterations(MyVertex currVert, boolean useTurns, int turnCost) {

        // Update lists
        this.closedList.add(currVert);
        this.openList.remove(currVert);

        ArrayList<MyVertex> nextVerts = this.astar.calcPossibleMoves(currVert);

        // For each possible move compute f() and add it to the open list
        for(MyVertex vert : nextVerts) {

            // Ignore if already in closed list
            if(this.closedList.contains(vert)) continue;

            // Compute f() if vertex is not in open list
            if(!this.openList.contains(vert)) {

                if(AStar.debugFlag) DebugPrint.getInstance().printVertex(vert);

                if(useTurns) vert.computeHeuristicWithTurns(turnCost);
                else vert.computeHeuristic();

                vert.setgCost(currVert.getgCost() + 1);
                vert.computeFCost();
                vert.setParent(currVert);

                this.openList.add(vert);

            // If already on open list, update f() cost if lower through this path and update with new parent
            } else {

                int newFCost = currVert.getgCost() + 1 + currVert.getHeuristicCost();
                if(newFCost < vert.getfCost()) {
                    vert.setParent(currVert);
                    vert.setfCost(newFCost);
                }
            }
        }

        // Sort open list
        Collections.sort(this.openList);
        if(AStar.debugFlag && this.openList.size() > 0) DebugPrint.getInstance().printInt("Best cost", this.openList.get(0).getfCost());
    }
}
