/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke.maps;

import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 *
 * @author classic
 */
public class SarTopoTileFactoryInfo extends TileFactoryInfo {

    public final static map SARTOPO_MapBuilderHybrid = new map("Map Builder Hybrid", "mbh");
    public final static map SARTOPO_MapBuilderTopo = new map("Map Builder Topo", "mbt");
    public final static map SARTOPO_ScannedTopos = new map("Scanned Topos", "t");
    public final static map SARTOPO_NAIP = new map("NAIP", "n");
    public final static map SARTOPO_GlobalImagery = new map("Global Imagery", "imagery");
    public final static map SARTOPO_ShadedRelief = new map("Shaded Relief", "r");

    private final static int TOP_ZOOM_LEVEL = 21;

    private final static int MAX_ZOOM_LEVEL = 17;

    private final static int MIN_ZOOM_LEVEL = 2;

    private final static int TILE_SIZE = 256;

    public static class map {

        private String type;
        private String name;

        private map(final String name, final String type) {
            this.type = type;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    private map maptype;

    /**
     * @param mode the mode
     */
    public SarTopoTileFactoryInfo(map maptype) {
        super(maptype.getName(), MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL, TOP_ZOOM_LEVEL, TILE_SIZE, false, false, "", "", "", "");
        this.maptype = maptype;
    }

    @Override
    public String getTileUrl(final int x, final int y, final int zoom) {
        return "https://sartopo.com/tile/" + maptype.type + "/" + (TOP_ZOOM_LEVEL - 0 - zoom) + "/" + x + "/" + y + ".png";
    }
}
