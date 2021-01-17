/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import meinke.maps.SarTopoTileFactoryInfo;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 *
 * @author classic
 */
public class Meinke{
    public static String FROM_GPS_PATH = System.getProperty("user.home");
    public static String TO_GPS_PATH = System.getProperty("user.home");
    public static int SARCASM_LEVEL = 50;
    public static int TILE_SELECTION = 0;
    public static int IGNORE_TRACKS_AFTER = 2;
    public static int TRIM_TRACKS_AFTER = 1;
    
    public static int MAIN_WINDOW_X = 0;
    public static int MAIN_WINDOW_Y = 0;
    public static int MAIN_WINDOW_WIDTH = 0;
    public static int MAIN_WINDOW_HEIGHT = 0;
    
    public static int GPS_WINDOW_X = 0;
    public static int GPS_WINDOW_Y = 0;
    public static int GPS_WINDOW_WIDTH = 0;
    public static int GPS_WINDOW_HEIGHT = 0;
    
    public static ArrayList<TileFactoryInfo> TileFactoryInfoList= new ArrayList<TileFactoryInfo>();
    
    public static MainForm mf;
    public static void main(String[] args){
        createMeinkeDir();
        loadSettings();
        
        createMeinkeDir(FROM_GPS_PATH);
        createMeinkeDir(TO_GPS_PATH);
        
        //load tile factories
        TileFactoryInfoList.add(new SarTopoTileFactoryInfo(SarTopoTileFactoryInfo.SARTOPO_GlobalImagery));
        TileFactoryInfoList.add(new SarTopoTileFactoryInfo(SarTopoTileFactoryInfo.SARTOPO_MapBuilderHybrid));
        TileFactoryInfoList.add(new SarTopoTileFactoryInfo(SarTopoTileFactoryInfo.SARTOPO_MapBuilderTopo));
        TileFactoryInfoList.add(new SarTopoTileFactoryInfo(SarTopoTileFactoryInfo.SARTOPO_NAIP));
        TileFactoryInfoList.add(new SarTopoTileFactoryInfo(SarTopoTileFactoryInfo.SARTOPO_ScannedTopos));
        TileFactoryInfoList.add(new SarTopoTileFactoryInfo(SarTopoTileFactoryInfo.SARTOPO_ShadedRelief));
        TileFactoryInfoList.add(new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID));
        TileFactoryInfoList.add(new OSMTileFactoryInfo());
        
        mf = new MainForm();
        mf.show();
    }
    
    public static String getMeinkeDir() {
        String path = System.getProperty("user.home");
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            path += "\\AppData\\Roaming\\Meinke\\";
        } else if (os.toLowerCase().contains("linux")) {
            path += "/.Meinke/";
        }else{
            path = "";
        }
        return path;
    }

    public static void createMeinkeDir() {
        createMeinkeDir(getMeinkeDir());
    }
    
    public static void createMeinkeDir(String path) {
        if (!new File(path).exists()) {
            try {
                Files.createDirectories(new File(path).toPath());
            } catch (IOException ex) {
                System.out.println("meinke.Meinke.getMeinkeDir() cannot create directory.");
            }
        }
    }
    
    public static void loadSettings() {
        FileInputStream in = null;
        try {
            Properties defaultProps = new Properties();
            in = new FileInputStream(getMeinkeDir() + "Settings.ini");
            defaultProps.load(in);
            in.close();
            FROM_GPS_PATH = defaultProps.getProperty("FROM_GPS_PATH");
            TO_GPS_PATH = defaultProps.getProperty("TO_GPS_PATH");
            SARCASM_LEVEL = Integer.parseInt(defaultProps.getProperty("SARCASM_LEVEL"));
            TILE_SELECTION = Integer.parseInt(defaultProps.getProperty("TILE_SELECTION"));
            IGNORE_TRACKS_AFTER = Integer.parseInt(defaultProps.getProperty("IGNORE_TRACKS_AFTER"));
            TRIM_TRACKS_AFTER = Integer.parseInt(defaultProps.getProperty("TRIM_TRACKS_AFTER"));
            
            MAIN_WINDOW_X = Integer.parseInt(defaultProps.getProperty("MAIN_WINDOW_X"));
            MAIN_WINDOW_Y = Integer.parseInt(defaultProps.getProperty("MAIN_WINDOW_Y"));
            MAIN_WINDOW_WIDTH = Integer.parseInt(defaultProps.getProperty("MAIN_WINDOW_WIDTH"));
            MAIN_WINDOW_HEIGHT = Integer.parseInt(defaultProps.getProperty("MAIN_WINDOW_HEIGHT"));
            
            GPS_WINDOW_X = Integer.parseInt(defaultProps.getProperty("GPS_WINDOW_X"));
            GPS_WINDOW_Y = Integer.parseInt(defaultProps.getProperty("GPS_WINDOW_Y"));
            GPS_WINDOW_WIDTH = Integer.parseInt(defaultProps.getProperty("GPS_WINDOW_WIDTH"));
            GPS_WINDOW_HEIGHT = Integer.parseInt(defaultProps.getProperty("GPS_WINDOW_HEIGHT"));
            
        } catch (Exception ex) {
            Logger.getLogger(Meinke.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (Exception ex) {
                Logger.getLogger(Meinke.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void saveSettings() {
        try {
            Properties defaultProps = new Properties();
            createMeinkeDir();
            FileOutputStream out = new FileOutputStream(getMeinkeDir() + "Settings.ini");
            defaultProps.setProperty("FROM_GPS_PATH", FROM_GPS_PATH);
            defaultProps.setProperty("TO_GPS_PATH", TO_GPS_PATH);
            defaultProps.setProperty("SARCASM_LEVEL", String.valueOf(SARCASM_LEVEL));
            defaultProps.setProperty("TILE_SELECTION", String.valueOf(TILE_SELECTION));
            defaultProps.setProperty("IGNORE_TRACKS_AFTER", String.valueOf(IGNORE_TRACKS_AFTER));
            defaultProps.setProperty("TRIM_TRACKS_AFTER", String.valueOf(TRIM_TRACKS_AFTER));
            
            defaultProps.setProperty("MAIN_WINDOW_X", String.valueOf(MAIN_WINDOW_X));
            defaultProps.setProperty("MAIN_WINDOW_Y", String.valueOf(MAIN_WINDOW_Y));
            defaultProps.setProperty("MAIN_WINDOW_WIDTH", String.valueOf(MAIN_WINDOW_WIDTH));
            defaultProps.setProperty("MAIN_WINDOW_HEIGHT", String.valueOf(MAIN_WINDOW_HEIGHT));
            
            defaultProps.setProperty("GPS_WINDOW_X", String.valueOf(GPS_WINDOW_X));
            defaultProps.setProperty("GPS_WINDOW_Y", String.valueOf(GPS_WINDOW_Y));
            defaultProps.setProperty("GPS_WINDOW_WIDTH", String.valueOf(GPS_WINDOW_WIDTH));
            defaultProps.setProperty("GPS_WINDOW_HEIGHT", String.valueOf(GPS_WINDOW_HEIGHT));
            
            defaultProps.store(out, "---No Comment---");
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Meinke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String unpackResourceToTempFile(String name) {
        String filepath = System.getProperty("java.io.tmpdir") + File.separatorChar + name;
        if (!new File(name).exists()) {
            try {
                InputStream is = Meinke.class.getResourceAsStream("/resources/" + name);
                OutputStream os = new FileOutputStream(filepath);
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                os.close();
                return filepath;
            } catch (IOException ex) {
                Logger.getLogger(Meinke.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            return filepath;
        }
        return null;
    }
}
