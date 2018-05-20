package com.mygdx.astar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
     * @return whether the map exists
     */
    public boolean loadMap(String filepath) {

        astar.getAssetManager().setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));

        // Check map exists
        FileHandle handle = Gdx.files.internal(filepath);
        if(!handle.exists()) return false;

        this.astar.getAssetManager().load(filepath, TiledMap.class);
        this.astar.getAssetManager().finishLoading();
        return true;
    }

    /**
     * Cycles through a Tiled map and finds all the entities asked for to build a list of their coordinates.
     *
     * @param layer the Tiled map layer to use for the search
     * @param type the entity to look for
     * @return the list of coordinates for this entity
     */
    public ArrayList<Vector2> getEntities(TiledMapTileLayer layer, String type) {

        ArrayList<Vector2> list = new ArrayList<Vector2>();

        int height = layer.getHeight();
        int width = layer.getWidth();

        // Cycle through tile map and add objects that have goal type
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {

                // Tile might not exist since layers have null tiles
                try {
                    if(layer.getCell(x, y).getTile().getProperties().get("name", String.class).equals(type)) {
                        list.add(new Vector2(x, y));
                    }
                } catch (NullPointerException e) {
                    continue;
                }
            }
        }

        return list;
    }

    /**
     * Draws a Tiled map's base layer to screen.
     *
     * @param map the Tiled map to use
     * @param camera the camera to use
     * @param batch the sprite batch to use
     */
    public void drawTileMap(TiledMap map, OrthographicCamera camera, SpriteBatch batch) {

        OrthogonalTiledMapRenderer renderer = new OrthogonalTiledMapRenderer(map, batch);
        int[] baseLayer = {AStar.baseLayerI};

        renderer.setView(camera);
        renderer.render(baseLayer);
    }

    /**
     * Draws the specified texture to each of the 2D vector positions specified
     * in the coords ArrayList.
     *
     * @param coords the coordinates to use
     * @param tex the texture to use
     */
    public void drawEntities(ArrayList<Vector2> coords, Texture tex) {

        SpriteBatch batch = astar.getBatch();

        for(Vector2 vec : coords) {
            batch.draw(tex, vec.x * AStar.tileSize, vec.y * AStar.tileSize);
        }
    }
}
