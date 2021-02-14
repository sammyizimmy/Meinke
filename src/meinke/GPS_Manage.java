/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Route;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import meinke.GPSS.Garmin;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author classic
 */
public class GPS_Manage extends javax.swing.JDialog {

    private List<GPXProcessor> gpstracks = new ArrayList<>();
    private List<GPXProcessor> sartopotracks = new ArrayList<>();
    
    private Garmin gps;
    private TrackView trackview;
    /**
     * Creates new form GPS_Manage
     */
    public GPS_Manage(Garmin gps) {
        this.gps = gps;
        
        initComponents();
        
        this.setLocationRelativeTo(null);
        if (Meinke.GPS_WINDOW_WIDTH > 0 && Meinke.GPS_WINDOW_HEIGHT > 0) {
            this.setBounds(Meinke.GPS_WINDOW_X, Meinke.GPS_WINDOW_Y, Meinke.GPS_WINDOW_WIDTH, Meinke.GPS_WINDOW_HEIGHT);
        }
        
        jLabelName.setText(gps.getAlias());
        loadgpx();
        
        trackCheckBoxListGPS.generateContents(gpstracks);
        trackCheckBoxListGPS.setUpdateaction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                updatemap();
            }
        });
        
        trackCheckBoxListSARTOPO.generateContents(sartopotracks);
        trackCheckBoxListSARTOPO.setUpdateaction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                updatemap();
            }
        });
        
        
        trackview = new TrackView();
        
        jPanelMap.add(trackview);
        String[] tflist = new String[Meinke.TileFactoryInfoList.size()];
        for (int i = 0; i < tflist.length; i++) {
            tflist[i] = Meinke.TileFactoryInfoList.get(i).getName();
        }
        jComboBoxMapType.setModel(new DefaultComboBoxModel(tflist));
        jComboBoxMapType.setSelectedIndex(Meinke.TILE_SELECTION);
        
        updatemap();
        
        //start file watcher
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(Meinke.TO_GPS_PATH);

            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.OVERFLOW,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            Timer watchtimer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    boolean hadkey = false;
                 
                    WatchKey key = watchService.poll();
                    while (key != null) {
                        hadkey = true;
                        key.pollEvents();
                        key.reset();
                        key = watchService.poll();
                    }

                    if (hadkey) {
                        loadgpx();
                        trackCheckBoxListGPS.generateContents(gpstracks);
                        trackCheckBoxListSARTOPO.generateContents(sartopotracks);
                        updatemap();
                        formWindowOpened(null);
                    }
                }
            });
            watchtimer.setRepeats(true);
            watchtimer.start();
        } catch (IOException ex) {
            Logger.getLogger(GPS_Manage.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private List<Painter> createOverlayPainters(TrackCheckBoxList cbl, List<GPXProcessor> tracks,Color normalcolor,Color selectedcolor){
        List<Painter> Painters = new ArrayList<>();
        Painter selectedpainter = null;
        
        for (int i = 0; i < tracks.size(); i++) {
            JCheckBox titlecheckbox = cbl.getTitleCheckbox(i);
            if (titlecheckbox.isSelected()) {
                boolean entiretarckselected = (titlecheckbox == cbl.getMouseover());
                GPXProcessor gpxtrack = tracks.get(i);
                for (int j = 0; j < gpxtrack.gpx.getRoutes().size(); j++) {
                    if (cbl.getCheckbox(i, j, 0, 0).isSelected()) {
                        Route route = gpxtrack.gpx.getRoutes().get(j);
                        List<GeoPosition> points = new ArrayList();
                        for (WayPoint wp : route.getPoints()) {
                            GeoPosition pos = new GeoPosition(wp.getLatitude().toDegrees(), wp.getLongitude().toDegrees());
                            points.add(pos);
                        }
                        if(entiretarckselected){
                            Painters.add(new GPXProcessor.RoutePainter(points,selectedcolor));
                        }else if(cbl.getCheckbox(i, j, 0, 0) == cbl.getMouseover()){
                            selectedpainter = new GPXProcessor.RoutePainter(points,selectedcolor);
                        }else{
                            Painters.add(new GPXProcessor.RoutePainter(points,normalcolor));
                        }
                    }
                }
                for (int j = 0; j < gpxtrack.gpx.getTracks().size(); j++) {
                    if (cbl.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), j, 0).isSelected()) {
                        Track track = gpxtrack.gpx.getTracks().get(j);
                        List<GeoPosition> points = new ArrayList();
                        track.segments().forEach(new Consumer() {
                            @Override
                            public void accept(Object t) {
                                for (WayPoint wp : ((TrackSegment)t).getPoints()) {
                                    GeoPosition pos = new GeoPosition(wp.getLatitude().toDegrees(), wp.getLongitude().toDegrees());
                                    points.add(pos);
                                }
                            }
                        });
                        if(entiretarckselected){
                            Painters.add(new GPXProcessor.RoutePainter(points,selectedcolor));
                        }else if(cbl.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), j, 0) == cbl.getMouseover()){
                            selectedpainter = new GPXProcessor.RoutePainter(points,selectedcolor);
                        }else{
                            Painters.add(new GPXProcessor.RoutePainter(points,normalcolor));
                        }
                    }
                }
                for (int j = 0; j < gpxtrack.gpx.getWayPoints().size(); j++) {
                    if (cbl.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), gpxtrack.gpx.getTracks().size(), j).isSelected()) {
                        WayPoint waypoint = gpxtrack.gpx.getWayPoints().get(j);
                        
                        if(entiretarckselected){
                            Painters.add(new GPXProcessor.WaypointPainter(Collections.singletonList(waypoint), selectedcolor));
                        } else if (cbl.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), gpxtrack.gpx.getTracks().size(), j) == cbl.getMouseover()) {
                            selectedpainter = new GPXProcessor.WaypointPainter(Collections.singletonList(waypoint), selectedcolor);
                        } else {
                            Painters.add(new GPXProcessor.WaypointPainter(Collections.singletonList(waypoint), normalcolor));
                        }
                    }
                }
            }
        }
        if (selectedpainter != null) {
            Painters.add(selectedpainter);
        }
        return Painters;
    }
    
    private void updatemap() {
        List<Painter> Painters = createOverlayPainters(trackCheckBoxListGPS, gpstracks, Color.RED, Color.ORANGE);
        Painters.addAll(createOverlayPainters(trackCheckBoxListSARTOPO, sartopotracks, Color.BLUE, Color.ORANGE));
        trackview.mv.setOverlayPainter(new CompoundPainter(Painters));
    }

    private void loadgpx() {
        gpstracks.clear();
        sartopotracks.clear();
        
        List<File> files = gps.listGPXfiles();
        ZonedDateTime now = ZonedDateTime.now();
        for (File file : files) {
            try {
                GPXProcessor gpx = new GPXProcessor(file.getAbsolutePath(), Meinke.TRIM_TRACKS_AFTER);
                if (gpx.newest.isAfter(now.minusDays(Meinke.IGNORE_TRACKS_AFTER))) {
                    gpstracks.add(gpx);
                }
            } catch (IOException ex) {
                Logger.getLogger(TrackView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        File folder = new File(Meinke.TO_GPS_PATH);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.toLowerCase().endsWith(".gpx");
            }
        };
        
        File[] fbasefiles = folder.listFiles(filter);
        for (File fbasefile : fbasefiles) {
            try {
                GPXProcessor gpx = new GPXProcessor(fbasefile.getAbsolutePath(), 0);
                sartopotracks.add(gpx);
            } catch (IOException ex) {
                Logger.getLogger(TrackView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelName = new javax.swing.JLabel();
        jButtonExportToGPS = new javax.swing.JButton();
        jPanelMap = new javax.swing.JPanel();
        jButtonImportFromGPS = new javax.swing.JButton();
        jButtonClearGPS = new javax.swing.JButton();
        jComboBoxMapType = new javax.swing.JComboBox<>();
        jButtonEject = new javax.swing.JButton();
        trackCheckBoxListGPS = new meinke.TrackCheckBoxList();
        trackCheckBoxListSARTOPO = new meinke.TrackCheckBoxList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabelName.setText("GPS Name");

        jButtonExportToGPS.setText("Export to GPS");
        jButtonExportToGPS.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonExportToGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportToGPSActionPerformed(evt);
            }
        });

        jPanelMap.setLayout(new javax.swing.BoxLayout(jPanelMap, javax.swing.BoxLayout.LINE_AXIS));

        jButtonImportFromGPS.setText("Import from GPS");
        jButtonImportFromGPS.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonImportFromGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportFromGPSActionPerformed(evt);
            }
        });

        jButtonClearGPS.setText("Clear GPS");
        jButtonClearGPS.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonClearGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearGPSActionPerformed(evt);
            }
        });

        jComboBoxMapType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMapType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMapTypeActionPerformed(evt);
            }
        });

        jButtonEject.setText("Eject GPS");
        jButtonEject.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonEject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEjectActionPerformed(evt);
            }
        });

        trackCheckBoxListGPS.setBorder(javax.swing.BorderFactory.createTitledBorder("on GPS"));

        trackCheckBoxListSARTOPO.setBorder(javax.swing.BorderFactory.createTitledBorder("from SARTOPO"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonImportFromGPS, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(trackCheckBoxListGPS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelName, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(trackCheckBoxListSARTOPO, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonClearGPS, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonEject, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonExportToGPS, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBoxMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelMap, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelName)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(trackCheckBoxListGPS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(trackCheckBoxListSARTOPO, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonExportToGPS)
                    .addComponent(jButtonImportFromGPS)
                    .addComponent(jButtonClearGPS)
                    .addComponent(jButtonEject))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonImportFromGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportFromGPSActionPerformed
        GPX.Builder gpxbuilder = GPX.builder();
        
        for (int i = 0; i < gpstracks.size(); i++) {
            JCheckBox titlecheckbox = trackCheckBoxListGPS.getTitleCheckbox(i);
            if (titlecheckbox.isSelected()) {
                GPXProcessor gpxtrack = gpstracks.get(i);
                for (int j = 0; j < gpxtrack.gpx.getRoutes().size(); j++) {
                    if (trackCheckBoxListGPS.getCheckbox(i, j, 0, 0).isSelected()) {
                        Route route = gpxtrack.gpx.getRoutes().get(j);
                        gpxbuilder.addRoute(route);
                    }
                }
                for (int j = 0; j < gpxtrack.gpx.getTracks().size(); j++) {
                    if (trackCheckBoxListGPS.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), j, 0).isSelected()) {
                        Track track = gpxtrack.gpx.getTracks().get(j);
                        gpxbuilder.addTrack(track);
                    }
                }
                for (int j = 0; j < gpxtrack.gpx.getWayPoints().size(); j++) {
                    if (trackCheckBoxListGPS.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), gpxtrack.gpx.getTracks().size(), j).isSelected()) {
                        WayPoint waypoint = gpxtrack.gpx.getWayPoints().get(j);
                        gpxbuilder.addWayPoint(waypoint);
                    }
                }
            }
        }
        
        GPX gpx = gpxbuilder.build();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save GPS track");
        fileChooser.setCurrentDirectory(new File(Meinke.FROM_GPS_PATH));
        fileChooser.setSelectedFile(new File(Meinke.FROM_GPS_PATH + File.separatorChar + "FromGPS.gpx"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                GPX.write(gpx, fileToSave.toPath());
                gps.setImported(true);
                JOptionPane.showMessageDialog(this, "File saved to: " + fileToSave.getCanonicalPath(),"File wrote", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(GPS_Manage.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Error saving file:\n" + ex.getLocalizedMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButtonImportFromGPSActionPerformed

    private void jButtonClearGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearGPSActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all tracks from GPS?\nArchived tracks will remain untouched", "Clear gps", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            List<File> files = gps.listGPXfiles();
            for (File file : files) {
                file.delete();
            }
            gps.setCleared(true);
            
            loadgpx();
            trackCheckBoxListGPS.generateContents(gpstracks);
            trackCheckBoxListSARTOPO.generateContents(sartopotracks);
            updatemap();
        }
    }//GEN-LAST:event_jButtonClearGPSActionPerformed

    private void jComboBoxMapTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMapTypeActionPerformed
        if(jComboBoxMapType.getSelectedIndex() != -1){
            GeoPosition center = trackview.mv.getCenterPosition();
            
            int totalmapzoom = trackview.mv.getTileFactory().getInfo().getTotalMapZoom();
            
            trackview.setTileFactoryInfo(Meinke.TileFactoryInfoList.get(jComboBoxMapType.getSelectedIndex()));
            trackview.mv.setZoom(trackview.mv.getZoom() + (trackview.mv.getTileFactory().getInfo().getTotalMapZoom() - totalmapzoom));
            trackview.mv.setCenterPosition(center);
            
            Meinke.TILE_SELECTION = jComboBoxMapType.getSelectedIndex();
            Meinke.saveSettings();
        }
    }//GEN-LAST:event_jComboBoxMapTypeActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        List<GPXProcessor> tlist = new ArrayList<>();
        tlist.addAll(gpstracks);
        tlist.addAll(sartopotracks);
        trackview.bestfit(tlist);
    }//GEN-LAST:event_formWindowOpened

    private void jButtonEjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEjectActionPerformed
        gps.eject();
        JOptionPane.showMessageDialog(this, "Ejecting, Please wait.");
    }//GEN-LAST:event_jButtonEjectActionPerformed

    private void jButtonExportToGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportToGPSActionPerformed
        GPX.Builder gpxbuilder = GPX.builder();
        
        for (int i = 0; i < sartopotracks.size(); i++) {
            JCheckBox titlecheckbox = trackCheckBoxListSARTOPO.getTitleCheckbox(i);
            if (titlecheckbox.isSelected()) {
                GPXProcessor gpxtrack = sartopotracks.get(i);
                for (int j = 0; j < gpxtrack.gpx.getRoutes().size(); j++) {
                    if (trackCheckBoxListSARTOPO.getCheckbox(i, j, 0, 0).isSelected()) {
                        Route route = gpxtrack.gpx.getRoutes().get(j);
                        gpxbuilder.addRoute(route);
                    }
                }
                for (int j = 0; j < gpxtrack.gpx.getTracks().size(); j++) {
                    if (trackCheckBoxListSARTOPO.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), j, 0).isSelected()) {
                        Track track = gpxtrack.gpx.getTracks().get(j);
                        gpxbuilder.addTrack(track);
                    }
                }
                for (int j = 0; j < gpxtrack.gpx.getWayPoints().size(); j++) {
                    if (trackCheckBoxListSARTOPO.getCheckbox(i, gpxtrack.gpx.getRoutes().size(), gpxtrack.gpx.getTracks().size(), j).isSelected()) {
                        WayPoint waypoint = gpxtrack.gpx.getWayPoints().get(j);
                        gpxbuilder.addWayPoint(waypoint);
                    }
                }
            }
        }
        
        GPX gpx = gpxbuilder.build();
        
        if (JOptionPane.showConfirmDialog(this, "Export map tiles to GPS?","Export map", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            KMZgen kmzgen = new KMZgen(trackview.mv);
            kmzgen.genKMZ(this, gps.getMapFolder() + "meinke.kmz");
        }
        File fileToSave = new File(gps.getGpxFolder() + "meinke-"+ java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")) +".gpx");
        try {
            GPX.write(gpx, fileToSave.toPath());
            gps.setExported(true);
            JOptionPane.showMessageDialog(this, "Files saved to: " + gps.getAlias(), "File wrote", JOptionPane.PLAIN_MESSAGE);
            
            if (JOptionPane.showConfirmDialog(this, "Eject GPS?", "Eject", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                gps.eject();
                JOptionPane.showMessageDialog(this, "Ejecting, Please wait.");
            }
            this.dispose();
        } catch (IOException ex) {
            Logger.getLogger(GPS_Manage.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error saving file:\n" + ex.getLocalizedMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonExportToGPSActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Meinke.GPS_WINDOW_X = this.getX();
        Meinke.GPS_WINDOW_Y = this.getY();
        Meinke.GPS_WINDOW_WIDTH = this.getWidth();
        Meinke.GPS_WINDOW_HEIGHT = this.getHeight();
        Meinke.saveSettings();
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClearGPS;
    private javax.swing.JButton jButtonEject;
    private javax.swing.JButton jButtonExportToGPS;
    private javax.swing.JButton jButtonImportFromGPS;
    private javax.swing.JComboBox<String> jComboBoxMapType;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JPanel jPanelMap;
    private meinke.TrackCheckBoxList trackCheckBoxListGPS;
    private meinke.TrackCheckBoxList trackCheckBoxListSARTOPO;
    // End of variables declaration//GEN-END:variables
}
