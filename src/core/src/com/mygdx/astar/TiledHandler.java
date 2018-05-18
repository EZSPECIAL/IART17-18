package com.mygdx.astar;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class TiledHandler {

    private AStar astar;

    /**
     * Constructs a TiledHandler object who's responsible for constructing lists of the
     * various tile entities in Tiled maps and rendering them.
     *
     * @param astar the main class instance
     */
    public TiledHandler(AStar astar) {
        this.astar = astar;
    }

    /**
     * Loads a Tiled (TMX) map by using the built-in loader.
     *
     * @param filepath the file path to use
     */
    public void loadMap(String filepath) {

        astar.getAssetManager().setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));

        this.astar.getAssetManager().load(filepath, TiledMap.class);
        this.astar.getAssetManager().finishLoading();
    }

    /**
     * Cycles through a Tiled map and finds all the goals to build a list of their coordinates.
     *
     * @param map the Tiled map to use
     * @return the list of coordinates of each goal in the map
     */
    public ArrayList<Vector2> getGoals(TiledMap map) {

        ArrayList<Vector2> goals = new ArrayList<Vector2>();
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(AStar.goalLayer);

        int height = layer.getHeight();
        int width = layer.getWidth();

        // Cycle through tile map and add objects that have goal type
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {

                // Tile might not exist since layers have null tiles
                try {
                    if(layer.getCell(x, y).getTile().getProperties().get("name", String.class).equals(AStar.goalType)) {
                        goals.add(new Vector2(x, y));
                    }
                } catch (NullPointerException e) {
                    continue;
                }
            }
        }

        return goals;
    }

    /**
     * Draws a Tiled map to screen.
     *
     * @param map the Tiled map to use
     * @param camera the camera to use
     * @param batch the sprite batch to use
     */
    public void drawTileMap(TiledMap map, OrthographicCamera camera, SpriteBatch batch) {

        OrthogonalTiledMapRenderer renderer = new OrthogonalTiledMapRenderer(map, batch);
        renderer.setView(camera);
        renderer.render();
    }
}
