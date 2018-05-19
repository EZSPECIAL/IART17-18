package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashSet;

public class MyVertex implements Comparable {

    // Vertex Pukoban state
    private ArrayList<Vector2> boxes;
    private Vector2 player;

    // Parent vertex
    private MyVertex parent;

    private AStar astar;

    // Heuristic properties
    private HashSet<Expansion> currentPass;
    private HashSet<Expansion> nextPass;
    private HashSet<Vector2> expansionCoords;
    private Expansion finalExpansion;
    private int heuristicCost;
    private int gCost;
    private int fCost;

    /**
     * Constructs a MyVertex object which stores the Pukoban board state by
     * copying all the box and player coordinates received.
     *
     * @param boxes the the list of box coordinates
     * @param pCoords the player coordinates
     */
    public MyVertex(AStar astar, ArrayList<Vector2> boxes, Vector2 pCoords) {

        this.astar = astar;

        this.boxes = new ArrayList<Vector2>();

        // Clone box coords
        for(Vector2 box : boxes) {
            this.boxes.add(new Vector2(box.x, box.y));
        }

        // Clone player coords
        this.player = pCoords.cpy();

        this.finalExpansion = new Expansion(new Vector2(0, 0), 0);
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

    /**
     * @return the current heuristic cost
     */
    public int getHeuristicCost() {
        return this.heuristicCost;
    }

    /**
     * @return the total cost for this vertex
     */
    public int getfCost() {
        return this.fCost;
    }

    /**
     * @return the cost to reach this vertex from the starting vertex
     */
    public int getgCost() {
        return this.gCost;
    }

    /**
     * @return the parent vertex of this vertex
     */
    public MyVertex getParent() {
        return this.parent;
    }

    /**
     * @param fCost the cost to set
     */
    public void setfCost(int fCost) {
        this.fCost = fCost;
    }

    /**
     * @param gCost the cost to set
     */
    public void setgCost(int gCost) {
        this.gCost = gCost;
    }

    /**
     * @param parent the parent vertex to set
     */
    public void setParent(MyVertex parent) {
        this.parent = parent;
    }

    // TODO add turns / edit javadoc
    /**
     * Computes the heuristic cost for this vertex and stores it.
     * The heuristic is the sum of the shortest distance to a box for each goal.
     */
    public void computeHeuristic() {

        int result = 0;

        // For each goal find the closest box
        for(Vector2 goal : this.astar.getGoals()) {

            // Reset expansion state variables
            this.expansionCoords = new HashSet<Vector2>();
            this.currentPass = new HashSet<Expansion>();
            this.nextPass = new HashSet<Expansion>();
            this.currentPass.add(new Expansion(goal.cpy(), 0));

            this.doExpansionPasses();

            result += this.finalExpansion.getDist();
            this.finalExpansion = new Expansion(new Vector2(0, 0), 0);
        }

        if(AStar.debugFlag) DebugPrint.getInstance().printInt("h()", result);
        this.heuristicCost = result;
    }

    /**
     * Expands outwards from the current pass expansions and checks for boxes in the path to find
     * the shortest distance to a box from a specific goal.
     */
    private void doExpansionPasses() {

        boolean isFinished = false;
        while(!isFinished) {

            // Cycle through the current expansions and check for a box, if not found add all the possible expansions to the next pass
            for(Expansion exp : this.currentPass) {

                ArrayList<Vector2> goalExpansion = new ArrayList<Vector2>();
                Vector2 goal = exp.getCoords();

                // Add expansion coords to already explored if unique
                this.expansionCoords.add(goal.cpy());

                // If current expansion overlaps a box stop
                if(this.boxes.contains(goal)) {
                    this.finalExpansion = new Expansion(new Vector2(goal.x, goal.y), exp.getDist());
                    isFinished = true;
                    break;
                }

                Vector2 moveRight = new Vector2(goal.x + 1, goal.y);
                Vector2 moveLeft = new Vector2(goal.x - 1, goal.y);
                Vector2 moveUp = new Vector2(goal.x, goal.y + 1);
                Vector2 moveDown = new Vector2(goal.x, goal.y - 1);

                // Add each direction only if they're viable and not already explored
                if(!this.astar.collisionCheck(moveRight)) {
                    if(!this.expansionCoords.contains(moveRight)) goalExpansion.add(moveRight);
                }
                if(!this.astar.collisionCheck(moveLeft)) {
                    if(!this.expansionCoords.contains(moveLeft)) goalExpansion.add(moveLeft);
                }
                if(!this.astar.collisionCheck(moveUp)) {
                    if(!this.expansionCoords.contains(moveUp)) goalExpansion.add(moveUp);
                }
                if(!this.astar.collisionCheck(moveDown)) {
                    if(!this.expansionCoords.contains(moveDown)) goalExpansion.add(moveDown);
                }

                // Add all possible expansions from this cell to the next pass
                for (Vector2 vec : goalExpansion) {
                    this.nextPass.add(new Expansion(vec.cpy(), exp.getDist() + 1));
                }
            }

            if(!isFinished) {

                // Copy next pass to current pass and reset next pass
                this.currentPass = new HashSet<Expansion>();
                for (Expansion exp : this.nextPass) {
                    this.currentPass.add(new Expansion(exp.getCoords(), exp.getDist()));
                }
                this.nextPass = new HashSet<Expansion>();
            }
        }
    }

    /**
     * @return whether all the boxes coincide with all the goals
     */
    public boolean checkGoal() {
        return this.boxes.containsAll(this.astar.getGoals());
    }

    /**
     * Computes f() = g() + h().
     *
     * @return the f() cost
     */
    public int computeFCost() {
        this.fCost = this.gCost + this.heuristicCost;
        return this.fCost;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj.getClass().getPackage().equals(this.getClass().getPackage())
                && obj.getClass().getName().equals(this.getClass().getName())) {

            MyVertex vertex = (MyVertex) obj;

            // Check player coords
            if(!this.player.equals(vertex.player)) return false;

            // Check same number of boxes
            if(this.boxes.size() != vertex.boxes.size()) return false;

            // Check that every box coincides
            if(!this.boxes.containsAll(vertex.boxes)) return false;

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        int result = 0;
        for(Vector2 box : this.boxes) {
            result += box.x + box.y;
        }

        result += this.player.x + this.player.y;

        return result;
    }

    @Override
    public int compareTo(Object o) {

        MyVertex vert = (MyVertex) o;

        if(this.fCost > vert.fCost) {
            return 1;
        } else if(this.fCost < vert.fCost) {
            return -1;
        } else return 0;
    }

    // TODO can use to force g() only or h() only
//    @Override
//    public int compareTo(Object o) {
//
//        MyVertex vert = (MyVertex) o;
//
//        if(this.gCost > vert.gCost) {
//            return 1;
//        } else if(this.gCost < vert.gCost) {
//            return -1;
//        } else return 0;
//    }
}
