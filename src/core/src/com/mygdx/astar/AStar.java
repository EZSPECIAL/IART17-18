package com.mygdx.astar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AStar extends ApplicationAdapter {

    static final boolean debugFlag = false;
    private static final String gameTitle = "Pukoban";
    final DecimalFormat df = new DecimalFormat("#0.00");
    private static final double keyDelay = 1.0;
    private static final double stepIncrement = 0.05;
    private static final double minStepRate = 0.05;
    private static final double maxStepRate = 2.0;
    private static final int minLevel = 1;
    private static final int maxLevel = 20;

    // Enumerators
    private enum PukoState {LOAD_MAP, RUN_ALGO, RENDER}
    private enum PlayerBoxCol {NO_MOVE, PUSH, PULL, FREE_SPACE}

    // Level loading constants
    private static final String mapPrefix = "Level";
    private static final String mapFileType = "tmx";

    // Texture names
    private static final String playerTexName = "player.png";
    private static final String boxTexName = "box.png";
    private static final String goalTexName = "goal.png";

    // Tiled map layers
    static final String baseLayer = "Base";
    static final String boxLayer = "Boxes";
    static final String goalLayer = "Goals";
    static final String playerLayer = "Player";
    static final int baseLayerI = 0;

    // Tiled tile set types
    static final String boxType = "box";
    static final String goalType = "goal";
    static final String playerType = "player";
    static final String wallType = "wall";

    // Level handling constants
    public static final int tileSize = 60;
    private static final float pixelToMeter = 1.0f / tileSize;
    private static float viewportWidth = 40;
    private static float viewportHeight = 22.5f;

    // Render objects
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private AssetManager assetManager;
    private Texture goalTex;
    private Texture boxTex;
    private Texture playerTex;

    // Tiled  properties
    private TiledMap currentMap;
    private String currentMapName = "";
    private Vector2 tileMapSize;
    private TiledHandler tiledHandler;

    // Entity properties
    private ArrayList<Vector2> goals;
    private ArrayList<Vector2> boxes;
    private ArrayList<Vector2> walls;
    private Vector2 player;

    // State properties
    private PukoState state = PukoState.LOAD_MAP;
    private double keyTimeout = 0;
    private int currentMapI = 1;

    // A* properties
    private ArrayList<MyVertex> solution = new ArrayList<MyVertex>();
    private boolean runAlgo = false;
    private boolean useTurns = false;
    private int turnCost = 0;
    private MyVertex.FCostMethod algoMethod = MyVertex.FCostMethod.BOTH;

    // Render properties
    private int iteration = 0;
    private double accumulator = 0;
    private static double stepRate = 0.5; // seconds

	@Override
	public void create() {

	    // Initial game title
        String num = df.format(this.stepRate);
        Gdx.graphics.setTitle(AStar.gameTitle + " " + num);

		this.batch = new SpriteBatch();
		this.assetManager = new AssetManager();
		this.tiledHandler = new TiledHandler(this);

		this.loadTextures();
        this.changeMap(currentMapI);

        camera = createCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
	}

	@Override
	public void render() {

	    // Get user input
        this.processKeyboard();

	    switch(this.state) {

	        // Loading map
            case LOAD_MAP:

                this.goals = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.goalLayer), AStar.goalType);
                this.boxes = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.boxLayer), AStar.boxType);
                this.walls = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.baseLayer), AStar.wallType);
                this.player = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.playerLayer), AStar.playerType).get(0).cpy();

                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Goals", this.goals);
                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Boxes", this.boxes);
                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Walls", this.walls);
                if(AStar.debugFlag) DebugPrint.getInstance().printVector("Player", this.player);
                this.state = PukoState.RUN_ALGO;
                break;

            // Running A*
            case RUN_ALGO:

                if(runAlgo) {

                    // Inform which algorithm is running
                    if(this.algoMethod.equals(MyVertex.FCostMethod.BOTH)) {
                        if (this.useTurns)
                            System.out.println("Starting A* with heuristic addition: box direction changes with cost: " + this.turnCost);
                        else
                            System.out.println("Starting A* with manhattan distance heuristic with obstacle consideration");
                    } else if(this.algoMethod.equals(MyVertex.FCostMethod.G_ONLY)) {
                        System.out.println("Starting uniform cost search");
                    } else if(this.algoMethod.equals(MyVertex.FCostMethod.H_ONLY)) {
                        System.out.println("Starting greedy search with manhattan distance with obstacle consideration");
                    }

                    // Start timer
                    long startTime = System.currentTimeMillis();

                    // Run A*
                    AStarAlgo astar = new AStarAlgo(this);
                    this.solution = astar.runAlgorithm(this.algoMethod, this.useTurns, this.turnCost);

                    // Stop timer
                    long stopTime = System.currentTimeMillis();

                    long elapsedTime = stopTime - startTime;
                    System.out.println("Execution time: " + elapsedTime / 1000.0f + "s");

                    this.runAlgo = false;
                    this.iteration = 0;
                    this.accumulator = 0;
                    this.state = PukoState.RENDER;
                }
                break;

            // Rendering solution
            case RENDER:

                // Update accumulator to step through the solution
                if(this.iteration != 0) {
                    this.accumulator += Gdx.graphics.getDeltaTime();
                    if(this.accumulator >= AStar.stepRate) {
                        this.accumulator = this.accumulator - AStar.stepRate;
                        this.iteration++;
                    }
                }

                this.batch.setProjectionMatrix(camera.combined);

                Gdx.gl.glClearColor( 103/255f, 69/255f, 117/255f, 1 );
                Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

                this.tiledHandler.drawTileMap(this.currentMap, this.camera, this.batch);

                // Draw dynamic entities
                this.batch.begin();

                this.tiledHandler.drawEntities(this.goals, this.goalTex);

                // Playback solution
                int index = this.solution.size() - 1 - this.iteration;
                if(this.iteration == 0) this.iteration++;

                // Stop at goal state
                if(index < 0) {
                    this.iteration = this.solution.size() + 1;
                    index = 0;
                }

                MyVertex currState = this.solution.get(index);

                ArrayList<Vector2> player = new ArrayList<Vector2>();
                player.add(currState.getPlayer());
                this.tiledHandler.drawEntities(player, this.playerTex);
                this.tiledHandler.drawEntities(currState.getBoxes(), this.boxTex);
                this.batch.end();

                break;
        }

        // Render static map while waiting for user input or A* termination
        if(!this.state.equals(PukoState.RENDER)) {

            this.batch.setProjectionMatrix(camera.combined);

            Gdx.gl.glClearColor( 103/255f, 69/255f, 117/255f, 1 );
            Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

            this.tiledHandler.drawTileMap(this.currentMap, this.camera, this.batch);

            // Draw dynamic entities
            this.batch.begin();

            this.tiledHandler.drawEntities(this.goals, this.goalTex);

            ArrayList<Vector2> player = new ArrayList<Vector2>();
            player.add(this.player);
            this.tiledHandler.drawEntities(player, this.playerTex);
            this.tiledHandler.drawEntities(this.boxes, this.boxTex);
            this.batch.end();
        }
	}
	
	@Override
	public void dispose() {

		this.batch.dispose();
		this.assetManager.dispose();
		this.playerTex.dispose();
		this.boxTex.dispose();
		this.goalTex.dispose();
		if(this.currentMap != null) this.currentMap.dispose();
	}

    /**
     * Processes keyboard events and sets related flags.
     */
    private void processKeyboard() {

        if(this.state.equals(PukoState.RUN_ALGO) || this.state.equals(PukoState.RENDER) && !runAlgo) {

            // Increase level
            if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {

                int previousMapI = this.currentMapI;

                this.currentMapI++;
                this.currentMapI = MathUtils.clamp(this.currentMapI, AStar.minLevel, AStar.maxLevel);

                if(previousMapI != this.currentMapI) {

                    if(this.changeMap(this.currentMapI)) this.state = PukoState.LOAD_MAP;
                    else this.currentMapI = previousMapI;
                }
            }

            // Decrease level
            if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {

                int previousMapI = this.currentMapI;

                this.currentMapI--;
                this.currentMapI = MathUtils.clamp(this.currentMapI, AStar.minLevel, AStar.maxLevel);

                if(previousMapI != this.currentMapI) {
                    if(this.changeMap(this.currentMapI)) this.state = PukoState.LOAD_MAP;
                    else this.currentMapI = previousMapI;
                }
            }
        }

        // Waiting for input to run algorithm
	    if(!runAlgo) {

            // Run A* with manhattan distance + obstacle consideration
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                this.useTurns = false;
                this.runAlgo = true;
                this.turnCost = 0;
                this.algoMethod = MyVertex.FCostMethod.BOTH;
                this.state = PukoState.RUN_ALGO;
            }

            // Run A* with manhattan distance + obstacle consideration + number of box turns
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                this.useTurns = true;
                this.runAlgo = true;
                this.turnCost = 1;
                this.algoMethod = MyVertex.FCostMethod.BOTH;
                this.state = PukoState.RUN_ALGO;
            }

            // Run A* with manhattan distance + obstacle consideration + number of box turns * 2 (player has to make minimum of 2 moves to change box direction)
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                this.useTurns = true;
                this.runAlgo = true;
                this.turnCost = 2;
                this.algoMethod = MyVertex.FCostMethod.BOTH;
                this.state = PukoState.RUN_ALGO;
            }

            // Run uniform cost search
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                this.useTurns = false;
                this.runAlgo = true;
                this.turnCost = 0;
                this.algoMethod = MyVertex.FCostMethod.G_ONLY;
                this.state = PukoState.RUN_ALGO;
            }

            // Run greedy search
            if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
                this.useTurns = false;
                this.runAlgo = true;
                this.turnCost = 0;
                this.algoMethod = MyVertex.FCostMethod.H_ONLY;
                this.state = PukoState.RUN_ALGO;
            }
        }

        // Increase solution stepping speed
        if(Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            this.stepRate += AStar.stepIncrement;
            this.stepRate = MathUtils.clamp(this.stepRate, AStar.minStepRate, AStar.maxStepRate);
            String num = df.format(this.stepRate);
            Gdx.graphics.setTitle(AStar.gameTitle + " " + num);
        }

        // Decrease solution stepping speed
        if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            this.stepRate -= AStar.stepIncrement;
            this.stepRate = MathUtils.clamp(this.stepRate, AStar.minStepRate, AStar.maxStepRate);
            String num = df.format(this.stepRate);
            Gdx.graphics.setTitle(AStar.gameTitle + " " + num);
        }
    }

    /**
     * Changes the current map by loading a new Tiled map. Sets the map size properties,
     * the camera and viewport according to the new map.
     *
     * @param mapID the numeric map ID to use
     */
    private boolean changeMap(int mapID) {

        System.out.println("Changing to map ID: " + mapID);

        String filepath = mapPrefix + mapID + "." + mapFileType;
        if(!this.tiledHandler.loadMap(filepath)) {
            System.out.println(filepath + " doesn't exist!");
            return false;
        }

	    if(!this.currentMapName.equals("")) this.assetManager.unload(this.currentMapName);

        this.currentMap = this.assetManager.get(filepath);
        TiledMapTileLayer layer = (TiledMapTileLayer) this.currentMap.getLayers().get(baseLayer);

        Gdx.graphics.setWindowedMode(layer.getWidth() * tileSize, layer.getHeight() * tileSize);

        this.tileMapSize = new Vector2(layer.getWidth(), layer.getHeight());
        if(AStar.debugFlag) DebugPrint.getInstance().printVector("Map size", this.tileMapSize);

        viewportWidth = layer.getWidth();
        viewportHeight = layer.getHeight();

        this.camera = createCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);

        return true;
    }

    /**
     * Computes the possible vertices to reach from the given starting vertex using Pukoban rules.
     *
     * @param vert the vertex to use as starting point
     * @return the list of vertices possible from the given starting vertex
     */
    public ArrayList<MyVertex> calcPossibleMoves(MyVertex vert) {

	    ArrayList<MyVertex> moves = new ArrayList<MyVertex>();
	    Vector2 pCoords = vert.getPlayer();

	    Vector2 moveRight = new Vector2(pCoords.x + 1, pCoords.y);
        Vector2 moveLeft = new Vector2(pCoords.x - 1, pCoords.y);
        Vector2 moveUp = new Vector2(pCoords.x, pCoords.y + 1);
        Vector2 moveDown = new Vector2(pCoords.x, pCoords.y - 1);

        // Check possible directions for Player, disregards boxes
        boolean validRight = !this.collisionCheck(moveRight);
        boolean validLeft = !this.collisionCheck(moveLeft);
        boolean validUp = !this.collisionCheck(moveUp);
        boolean validDown = !this.collisionCheck(moveDown);

        Boolean[] bools = {validRight, validLeft, validUp, validDown};
        if(AStar.debugFlag) DebugPrint.getInstance().printFlags("MoveFlags", bools);

        // Simulate all board states for every player direction possible from starting state
        if(validRight) {
            moves.addAll(this.simulatePukobanState(vert, moveRight, new Vector2(1, 0)));
        }

        if(validLeft) {
            moves.addAll(this.simulatePukobanState(vert, moveLeft, new Vector2(-1, 0)));
        }

        if(validUp) {
            moves.addAll(this.simulatePukobanState(vert, moveUp, new Vector2(0, 1)));
        }

        if(validDown) {
            moves.addAll(this.simulatePukobanState(vert, moveDown, new Vector2(0, -1)));
        }

        return moves;
    }

    /**
     * Computes the new box positions using the player coordinates and the player movement direction.
     * Takes in consideration whether the box movement is a pull or a push.
     *
     * @param vert the vertex to use as starting point
     * @param pCoords the player coordinates after simulating movement
     * @param direction the direction used to simulate movement
     * @param isPull whether the move is a pull or a push
     * @return the list with the new box positions
     */
    private ArrayList<Vector2> moveBoxes(MyVertex vert, Vector2 pCoords, Vector2 direction, boolean isPull) {

        ArrayList<Vector2> boxes = vert.getBoxes();
        ArrayList<Vector2> tempBoxes = new ArrayList<Vector2>();

        for (Vector2 box : boxes) {
            tempBoxes.add(new Vector2(box.x, box.y));
        }

        // Pulling box
        if(isPull) {

            Vector2 oppositeDir = new Vector2(direction.x * -1, direction.y * -1);
            Vector2 pullBox = pCoords.cpy();
            pullBox = pullBox.add(oppositeDir).add(oppositeDir);

            int index = tempBoxes.indexOf(pullBox);
            Vector2 newBox = tempBoxes.get(index);

            newBox = newBox.add(direction);
            tempBoxes.set(index, newBox);

        // Pushing box
        } else {

            int index = tempBoxes.indexOf(pCoords);
            Vector2 newBox = tempBoxes.get(index);

            newBox = newBox.add(direction);
            tempBoxes.set(index, newBox);
        }

        return tempBoxes;
    }

    /**
     * Simulates the effect of player movement on the map boxes. Returns the new board state
     * created by that movement, if any.
     *
     * @param vert the vertex to use as starting point
     * @param pCoords the player coordinates after simulating movement
     * @param direction the direction used to simulate movement
     * @return the possible board states created by this movement
     */
    private ArrayList<MyVertex> simulatePukobanState(MyVertex vert, Vector2 pCoords, Vector2 direction) {

	    ArrayList<MyVertex> verts = new ArrayList<MyVertex>();

	    switch(this.simulatePlayerMove(vert, pCoords, direction)) {

            case PUSH:
                verts.add(new MyVertex(this, this.moveBoxes(vert, pCoords, direction, false), pCoords));
                break;

            case PULL:
                verts.add(new MyVertex(this, this.moveBoxes(vert, pCoords, direction, true), pCoords));
                verts.add(new MyVertex(this, vert.getBoxes(), pCoords));
                break;

            case FREE_SPACE:
                verts.add(new MyVertex(this, vert.getBoxes(), pCoords));
                break;

            default:
                break;
        }

        return verts;
    }

    /**
     * Simulates the effects of player movement on the map boxes. Checks whether the movement
     * can be a push or pull, and also if the movement isn't possible due to box collision.
     *
     * @param vert the vertex to use as starting point
     * @param pCoords the player coordinates after simulating movement
     * @param direction the direction used to simulate movement
     * @return the enumerator describing the type of movement that occurred
     */
    private PlayerBoxCol simulatePlayerMove(MyVertex vert, Vector2 pCoords, Vector2 direction) {

	    ArrayList<Vector2> boxes = vert.getBoxes();

        // Player/Box collision
        int index = boxes.indexOf(pCoords);

        // Box is pushed
        if(index != -1) {

            Vector2 boxCoords = boxes.get(index).cpy();
            boxCoords = boxCoords.add(direction);

            // Box can move
            if(!this.collisionCheck(boxCoords) && !this.boxCollision(boxes, boxCoords)) {
                return PlayerBoxCol.PUSH;
            } else return PlayerBoxCol.NO_MOVE;

        // Free space, might pull or not
        } else {

            Vector2 oppositeDir = new Vector2(direction.x * -1, direction.y * -1);
            Vector2 checkForBox = pCoords.cpy();
            checkForBox = checkForBox.add(oppositeDir).add(oppositeDir);

            // Box to the opposite side of player
            if(boxes.contains(checkForBox)) {
                return PlayerBoxCol.PULL;
            // No boxes for pushing/pulling
            } else return PlayerBoxCol.FREE_SPACE;
        }
    }

    /**
     * Checks whether the given coordinates collide with the existing box coordinates.
     *
     * @param boxes the list of box coordinates to check
     * @param coords the coordinates to use
     * @return whether collision happened
     */
    private boolean boxCollision(ArrayList<Vector2> boxes, Vector2 coords) {
	    return boxes.contains(coords);
    }

    /**
     * Checks for map boundary collision and wall collisions using the given coordinates.
     *
     * @param coords the coordinates to use
     * @return whether collision happened
     */
    public boolean collisionCheck(Vector2 coords) {

	    if(coords.x < 0 || coords.y < 0) {
	        return true;
        } else if(coords.x > this.tileMapSize.x - 1 || coords.y > this.tileMapSize.y - 1) {
	        return true;
        } else if(this.walls.contains(coords)) {
	        return true;
        }

        return false;
    }

    /**
     * Loads the textures needed for drawing the game.
     */
    private void loadTextures() {

	    this.assetManager.load(AStar.goalTexName, Texture.class);
	    this.assetManager.load(AStar.boxTexName, Texture.class);
	    this.assetManager.load(AStar.playerTexName, Texture.class);
	    this.assetManager.finishLoading();

	    this.goalTex = this.assetManager.get(AStar.goalTexName);
	    this.boxTex = this.assetManager.get(AStar.boxTexName);
	    this.playerTex = this.assetManager.get(AStar.playerTexName);
    }

    /**
     * Creates an orthographic camera for displaying the screen.
     *
     * @return the orthographic camera
     */
    private OrthographicCamera createCamera() {

        OrthographicCamera camera = new OrthographicCamera(viewportWidth / pixelToMeter, viewportHeight / pixelToMeter);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        return camera;
    }

    /**
     * @return the asset manager for this game
     */
	public AssetManager getAssetManager() {
	    return this.assetManager;
    }

    /**
     * @return the sprite batch for this game
     */
    public SpriteBatch getBatch() {
	    return this.batch;
    }

    /**
     * @return the list of coordinates of each goal for this game
     */
    public ArrayList<Vector2> getGoals() {
        return this.goals;
    }

    /**
     * @return the list of coordinates of each box for the current map state
     */
    public ArrayList<Vector2> getBoxes() {
        return this.boxes;
    }

    /**
     * @return the coordinates of the player for the current map state
     */
    public Vector2 getPlayer() {
        return this.player;
    }
}
