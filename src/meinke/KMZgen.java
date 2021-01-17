/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Tile;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 *
 * @author classic
 */
public class KMZgen {
    JXMapViewer mv;
    public KMZgen(JXMapViewer mv) {
        this.mv = mv;
    }

    private String genKmlOverlay(String fname, double North, double South, double East, double West) {
        return "<GroundOverlay>\n"
                + "  <name>" + fname + "</name>\n"
                + "  <Icon>\n"
                + "    <href>" + fname + "</href>\n"
                + "  </Icon>\n"
                + "  <LatLonBox>\n"
                + "    <north>" + North + "</north>\n"
                + "    <south>" + South + "</south>\n"
                + "    <east>" + East + "</east>\n"
                + "    <west>" + West + "</west>\n"
                + "  </LatLonBox>\n"
                + "</GroundOverlay>\n";
    }
    
    private void addKmzFile(File file,ZipOutputStream zipStream) throws IOException{
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipStream.write(bytes, 0, length);
        }

        zipStream.closeEntry();
        fis.close();
    }
    
    private void addKmzFile(byte[] bytes, String name, ZipOutputStream zipStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(name);
        zipStream.putNextEntry(zipEntry);
        zipStream.write(bytes);
        zipStream.closeEntry();
    }

    public void genKMZ(Dialog parent, String kmzfilepath) {
        int zoom = mv.getZoom();
        int size = mv.getTileFactory().getTileSize(0);
        Dimension mapSize = mv.getTileFactory().getMapSize(0);

        // calculate the "visible" viewport area in tiles
        int numWide = mv.getViewportBounds().width / size + 2;
        int numHigh = mv.getViewportBounds().width / size + 2;

        // TilePoint topLeftTile = getTileFactory().getTileCoordinate(
        // new Point2D.Double(viewportBounds.x, viewportBounds.y));
        TileFactoryInfo info = mv.getTileFactory().getInfo();

        // number of tiles in x direction
        int tpx = (int) Math.floor(mv.getViewportBounds().getX() / info.getTileSize(0));
        // number of tiles in y direction
        int tpy = (int) Math.floor(mv.getViewportBounds().getY() / info.getTileSize(0));
        // TilePoint topLeftTile = new TilePoint(tpx, tpy);
        
        
        List<Tile> tlist = new ArrayList<>(numWide*numHigh);
        for (int x = 0; x < numWide; x++) {
            for (int y = 0; y < numHigh; y++) {
                int itpx = x + tpx;// topLeftTile.getX();
                int itpy = y + tpy;// topLeftTile.getY();
                tlist.add(mv.getTileFactory().getTile(itpx, itpy, zoom));
            }
        }
        
        final JDialog dlg = new JDialog(parent, "Progress Dialog", true);
        JProgressBar dpb = new JProgressBar(0, 100);
        dlg.add(BorderLayout.CENTER, dpb);
        dlg.add(BorderLayout.NORTH, new JLabel("Loading tiles."));
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(300, 75);
        dlg.setLocationRelativeTo(mv);
        
        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                //setup kml
                StringBuilder kml = new StringBuilder();
                kml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                        + "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n"
                        + "<Document>\n"
                        + "<name>Meinke</name>\n");
                
                //setup cache
                String cachedir = Meinke.getMeinkeDir() + "cache" + File.separatorChar;
                if (!new File(cachedir).exists()) {
                    try {
                        Files.createDirectories(new File(cachedir).toPath());
                    } catch (IOException ex) {
                        System.out.println("doInBackground() cannot create directory.");
                        return null;
                    }
                }

                //setup zip file
                FileOutputStream fos = new FileOutputStream(kmzfilepath);
                ZipOutputStream zipOS = new ZipOutputStream(fos);

                //dowload files
                for (int i = 0; i < tlist.size(); i++) {
                    Tile tile = tlist.get(i);
                    File dlfile = new File(cachedir + tile.getURL().hashCode() + "-" + tile.getX() + "-" + tile.getY() + "-" + tile.getZoom() + ".jpg");
                    if (!dlfile.exists()) {
                        try {
                            URL lnk = new URL(tile.getURL());
                            
                            BufferedImage pngin = ImageIO.read(lnk);
                            BufferedImage jpegout = new BufferedImage(pngin.getWidth(), pngin.getHeight(), BufferedImage.TYPE_INT_RGB);
                            Graphics2D g2d = jpegout.createGraphics();
                            g2d.setColor(Color.WHITE); // Or what ever fill color you want...
                            g2d.fillRect(0, 0, jpegout.getWidth(), jpegout.getHeight());
                            g2d.drawImage(pngin, 0, 0, null);
                            g2d.dispose();

                            ImageIO.write(jpegout, "jpg", dlfile);
                            System.out.println(lnk.toString() + "->" + dlfile.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
         
                    if (dlfile.exists()) {//add to kml
                        int lx = tile.getX() * mv.getTileFactory().getTileSize(tile.getZoom());
                        int ly = tile.getY() * mv.getTileFactory().getTileSize(tile.getZoom());
                        GeoPosition nw = mv.getTileFactory().pixelToGeo(new Point2D.Double(lx, ly), tile.getZoom());
                        GeoPosition se = mv.getTileFactory().pixelToGeo(new Point2D.Double(lx + mv.getTileFactory().getTileSize(tile.getZoom()), ly + mv.getTileFactory().getTileSize(tile.getZoom())), tile.getZoom());
                        kml.append(genKmlOverlay(dlfile.getName(), nw.getLatitude(), se.getLatitude(), se.getLongitude(), nw.getLongitude()));
                        addKmzFile(dlfile, zipOS);
                    }
                    
                    dlg.setTitle("Downloading " + i + " of " + tlist.size());
                    dpb.setValue((int)(((double)i/(double)tlist.size())*100));
                }
                
                kml.append("</Document>\n"
                        + "</kml>");
                addKmzFile(kml.toString().getBytes(), "doc.kml", zipOS);
 
                zipOS.close();
                fos.close();
                
                return null;
            }
            @Override
            protected void done() {
                dlg.setVisible(false);
            }

        };
        sw.execute();
        
        dlg.setVisible(true);
        System.out.println("done");
    }
}
