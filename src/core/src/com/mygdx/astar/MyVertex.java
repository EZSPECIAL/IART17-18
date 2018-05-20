package com.mygdx.astar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashSet;

public class MyVertex implements Comparable {

    public enum FCostMethod {BOTH, G_ONLY, H_ONLY};

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
    private HashSet<Expansion> expansionList;
    private Expansion finalExpansion;
    private int heuristicCost;
    private int gCost;
    private int fCost;

    /**
     * Constructs a MyVertex object which stores the Pukoban board state by
     * copying all the box and player coordinates received.
     *
     * @param astar the main class instance
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

    /**
     * Expands outwards from the current pass expansions and checks for boxes in the path to find
     * the shortest distance to a box from a specific goal. Can specify a box to find the distance
     * to it, instead of finding the closest box to a goal.
     *
     * @param box the box to use, can be null if only interested in knowing the closest one
     * @return the distance to the closest box or to the specified box
     */
    private Expansion doExpansionPasses(Vector2 box) {

        Expansion expansion = new Expansion(new Vector2(0, 0), 0);

        boolean isFinished = false;
        while(!isFinished) {

            // Cycle through the current expansions and check for a box, if not found add all the possible expansions to the next pass
            for(Expansion exp : this.currentPass) {

                ArrayList<Vector2> goalExpansion = new ArrayList<Vector2>();
                Vector2 goal = exp.getCoords();

                // Add expansion coords to already explored if unique
                this.expansionCoords.add(goal.cpy());
                if(box != null) this.expansionList.add(exp);

                // If current expansion overlaps a box, stop
                if(box == null) {
                    if(this.boxes.contains(goal)) {
                        expansion = new Expansion(new Vector2(goal.x, goal.y), exp.getDist());
                        isFinished = true;
                        break;
                    }
                // If current expansion found the specified box, stop
                } else {
                    if (box.equals(goal)) {
                        expansion = new Expansion(new Vector2(goal.x, goal.y), exp.getDist());
                        isFinished = true;
                        break;
                    }
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

        return expansion;
    }

    /**
     * Backtracks from a box to a goal by using all the expanded states
     * from the previous round.
     *
     * @param initExp the expansion where the box was found
     * @return the path from the box to the goal
     */
    private ArrayList<Expansion> backtrackPath(Expansion initExp) {

        ArrayList<Expansion> path = new ArrayList<Expansion>();
        path.add(initExp);

        int distCheck = initExp.getDist() - 1;
        if(distCheck < 0) return path;
        Vector2 adjCoords = new Vector2(initExp.getCoords().x, initExp.getCoords().y);

        boolean isFinished = false;

        while(!isFinished) {
            for (Expansion exp : this.expansionList) {
                if(this.isAdj(exp.getCoords(), adjCoords) && exp.getDist() == distCheck) {

                    path.add(exp);
                    adjCoords = exp.getCoords().cpy();

                    distCheck--;
                    if(distCheck < 0) {
                        isFinished = true;
                    }
                    break;
                }
            }
        }

        return path;
    }

    /**
     * Counts the number of turns needed to go from a specific goal
     * to a specific box.
     *
     * @param path the path from a specific goal to a specific box
     * @return the number of turns to get from the goal to the box
     */
    private int countTurns(ArrayList<Expansion> path) {

        int firstDx, firstDy, secondDx, secondDy;
        int turnCount = 0;

        for(int i = 0; i < path.size() - 2; i++) {

            // Reset deltas
            firstDx = 0;
            firstDy = 0;
            secondDx = 0;
            secondDy = 0;

            Vector2 firstCoords = path.get(i).getCoords();
            Vector2 secondCoords = path.get(i + 1).getCoords();
            Vector2 thirdCoords = path.get(i + 2).getCoords();

            firstDx = (int) (firstCoords.x - secondCoords.x);
            firstDy = (int) (firstCoords.y - secondCoords.y);

            secondDx = (int) (secondCoords.x - thirdCoords.x);
            secondDy = (int) (secondCoords.y - thirdCoords.y);

            if(firstDx != 0 && secondDy != 0) turnCount++;
            else if(firstDy != 0 && secondDx != 0) turnCount++;
        }

        return turnCount;
    }

    /**
     * Checks if 2 vectors are adjacent using 4 directions.
     *
     * @param vec1 the first vector
     * @param vec2 the second vector
     * @return whether the vectors are adjacent using 4 directions
     */
    private boolean isAdj(Vector2 vec1, Vector2 vec2) {

        Vector2 rightVec = new Vector2(vec2.x + 1, vec2.y);
        Vector2 leftVec = new Vector2(vec2.x - 1, vec2.y);
        Vector2 upVec = new Vector2(vec2.x, vec2.y + 1);
        Vector2 downVec = new Vector2(vec2.x, vec2.y - 1);

        return (vec1.equals(rightVec) || vec1.equals(leftVec) || vec1.equals(upVec) || vec1.equals(downVec));
    }

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

            this.doExpansionPasses(null);

            result += this.finalExpansion.getDist();
            this.finalExpansion = new Expansion(new Vector2(0, 0), 0);
        }

        if(AStar.debugFlag) DebugPrint.getInstance().printInt("h()", result);
        this.heuristicCost = result;
    }

    /**
     * Computes the heuristic cost for this vertex and stores it.
     * The heuristic is the sum of the shortest distance to a box for each goal considering
     * the number of turns the box has to make to reach the goal. The shortest path
     * over all the box/goal combinations is the one used.
     *
     * @param turnValue how much each turn should weigh in the heuristic cost
     */
    public void computeHeuristicWithTurns(int turnValue) {

        int result = 0;

        // For each goal find the distance to each box
        for(Vector2 goal : this.astar.getGoals()) {

            int bestResult = -1;

            // Find the distance to each box for the current goal
            for(Vector2 box : this.boxes) {

                this.expansionCoords = new HashSet<Vector2>();
                this.expansionList = new HashSet<Expansion>();
                this.currentPass = new HashSet<Expansion>();
                this.nextPass = new HashSet<Expansion>();
                this.currentPass.add(new Expansion(goal.cpy(), 0));

                // Find the path to this box
                Expansion exp = this.doExpansionPasses(box);
                ArrayList<Expansion> path = this.backtrackPath(exp);

                // Count the number of direction changes
                int turns = this.countTurns(path);

                // Update best path
                if(bestResult == -1) bestResult = exp.getDist() + turns * turnValue;
                else if(exp.getDist() + turns * 2 < bestResult) bestResult = exp.getDist() + turns * turnValue;
            }

            result += bestResult;
        }

        if(AStar.debugFlag) DebugPrint.getInstance().printInt("h()", result);
        this.heuristicCost = result;
    }

    /**
     * @return whether all the boxes coincide with all the goals
     */
    public boolean checkGoal() {
        return this.boxes.containsAll(this.astar.getGoals());
    }

    /**
     * Computes f() using the specified method.
     *
     * @param method the method to use for calculating f()
     * @return the f() cost
     */
    public int computeFCost(FCostMethod method) {

        switch(method) {

            case BOTH:
                this.fCost = this.gCost + this.heuristicCost;
                return this.fCost;

            case G_ONLY:
                this.fCost = this.gCost;
                return this.gCost;

            case H_ONLY:
                this.fCost = this.heuristicCost;
                return this.fCost;
        }

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
}
