package com.mygdx.astar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class AStar extends ApplicationAdapter {

    static final boolean debugFlag = true;

    // Enumerators
    private enum PukoState {LOAD_MAP, RENDER}

    // Level loading constants
    private static final String mapPrefix = "Level";
    private static final String mapFileType = "tmx";

    // Tiled map layers
    static final String baseLayer = "Base";
    static final String boxLayer = "Boxes";
    static final String goalLayer = "Goals";
    static final String playerLayer = "Player";

    // Tiled tile set types
    static final String boxType = "box";
    static final String goalType = "goal";
    static final String playerType = "player";
    static final String wallType = "wall";

    // Level handling constants
    private static final int tileSize = 60;
    private static final float pixelToMeter = 1.0f / tileSize;
    private static float viewportWidth = 40;
    private static float viewportHeight = 22.5f;

    // Render objects
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private AssetManager assetManager;

    // Tiled  properties
    private TiledMap currentMap;
    private String currentMapName = "";
    private TiledHandler tiledHandler;

    // Entity properties
    private ArrayList<Vector2> goals;
    private ArrayList<Vector2> boxes;
    private ArrayList<Vector2> walls;
    private Vector2 player;

    // State properties
    private PukoState state = PukoState.LOAD_MAP;

	@Override
	public void create() {

		this.batch = new SpriteBatch();
		this.assetManager = new AssetManager();
		this.tiledHandler = new TiledHandler(this);

        this.changeMap(1);

        camera = createCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
	}

	// TODO level select
	@Override
	public void render() {

	    switch(this.state) {

            case LOAD_MAP:
                this.goals = this.tiledHandler.getGoals(this.currentMap);
                if(AStar.debugFlag) DebugPrint.getInstance().printVectorList("Goals", this.goals);
                this.state = PukoState.RENDER;
                break;

            case RENDER:

                this.batch.setProjectionMatrix(camera.combined);

                Gdx.gl.glClearColor( 103/255f, 69/255f, 117/255f, 1 );
                Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

                this.tiledHandler.drawTileMap(this.currentMap, this.camera, this.batch);
                break;
        }

//		Gdx.gl.glClearColor(1, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();
	}
	
	@Override
	public void dispose() {

		this.batch.dispose();
		this.assetManager.dispose();
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
        viewportWidth = layer.getWidth();
        viewportHeight = layer.getHeight();

        this.camera = createCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
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
     * @return the current Tiled map
     */
    public TiledMap getCurrentMap() {
	    return this.currentMap;
    }
}
