package com.mygdx.astar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import java.util.ArrayList;

public class AStar extends ApplicationAdapter {

    static final boolean debugFlag = true;

    // Enumerators
    private enum PukoState {LOAD_MAP, RUN_ALGO, RENDER}

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

    // A* properties
    private DefaultUndirectedWeightedGraph<MyVertex, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<MyVertex, DefaultEdge>(DefaultEdge.class);

	@Override
	public void create() {

		this.batch = new SpriteBatch();
		this.assetManager = new AssetManager();
		this.tiledHandler = new TiledHandler(this);

		this.loadTextures();
        this.changeMap(1);

        camera = createCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
	}

	// TODO level select
	@Override
	public void render() {

	    switch(this.state) {

	        // Loading map
            case LOAD_MAP:

                this.goals = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.goalLayer), AStar.goalType);
                this.boxes = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.boxLayer), AStar.boxType);
                this.walls = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.baseLayer), AStar.wallType);
                this.player = this.tiledHandler.getEntities((TiledMapTileLayer) this.currentMap.getLayers().get(AStar.playerLayer), AStar.playerType).get(0).cpy(); // TODO static final

                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Goals", this.goals);
                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Boxes", this.boxes);
                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Walls", this.walls);
                if(AStar.debugFlag) DebugPrint.getInstance().printVector("Player", this.player);
                this.state = PukoState.RUN_ALGO;
                break;

            // Running A*
            case RUN_ALGO:

                MyVertex testVert = new MyVertex(this.boxes, this.player);
                this.graph.addVertex(testVert);


                this.calcPossibleMoves(testVert);
                this.state = PukoState.RENDER;
                break;

            // Rendering solution
            case RENDER:

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

                break;
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

	// TODO doc
    private void changeMap(int mapID) {

	    if(!this.currentMapName.equals("")) this.assetManager.unload(this.currentMapName);

	    String filepath = mapPrefix + mapID + "." + mapFileType;
        this.tiledHandler.loadMap(filepath);
        this.currentMap = this.assetManager.get(filepath);
        TiledMapTileLayer layer = (TiledMapTileLayer) this.currentMap.getLayers().get(baseLayer);

        Gdx.graphics.setWindowedMode(layer.getWidth() * tileSize, layer.getHeight() * tileSize);

        this.tileMapSize = new Vector2(layer.getWidth(), layer.getHeight());
        viewportWidth = layer.getWidth();
        viewportHeight = layer.getHeight();

        this.camera = createCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    }

    // TODO doc
    private void calcPossibleMoves(MyVertex start) {

	    Vector2 moveRight = new Vector2(this.player.x + 1, this.player.y);
        Vector2 moveLeft = new Vector2(this.player.x - 1, this.player.y);
        Vector2 moveUp = new Vector2(this.player.x, this.player.y + 1);
        Vector2 moveDown = new Vector2(this.player.x, this.player.y - 1);

        boolean validRight = this.boundsCheck(moveRight);
        boolean validLeft = this.boundsCheck(moveLeft);
        boolean validUp = this.boundsCheck(moveUp);
        boolean validDown = this.boundsCheck(moveDown);

        ArrayList<Boolean> bools = new ArrayList<Boolean>();
        bools.add(validRight);
        bools.add(validLeft);
        bools.add(validUp);
        bools.add(validDown);
        DebugPrint.getInstance().printFlags("MoveFlags", bools);


    }

    // TODO doc
    private boolean boundsCheck(Vector2 coords) {

	    if(coords.x < 0 || coords.y < 0) {
	        return false;
        } else if(coords.x > this.tileMapSize.x - 1 || coords.y > this.tileMapSize.y - 1) {
	        return false;
        } else if(this.walls.contains(coords)) {
	        return false;
        }

        return true;
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
}
