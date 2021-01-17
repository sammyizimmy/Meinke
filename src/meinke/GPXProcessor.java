/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Longitude;
import io.jenetics.jpx.Route;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

/**
 *
 * @author classic
 */
public class GPXProcessor {
    GPX gpx;
    String filepath;
    
    double minLONG = Integer.MAX_VALUE;
    double minLAT = Integer.MAX_VALUE;
    double minELEV = Integer.MAX_VALUE;
    double maxLONG = Integer.MIN_VALUE;
    double maxLAT = Integer.MIN_VALUE;
    double maxELEV = Integer.MIN_VALUE;
    
    ZonedDateTime oldest = ZonedDateTime.of(LocalDateTime.MAX,ZoneId.systemDefault());
    ZonedDateTime newest = ZonedDateTime.of(LocalDateTime.MIN,ZoneId.systemDefault());
    
    double scaleLong;
    double scaleLat;
    double scaleElev;
    
    
    public GPXProcessor(String gpxpath, int trimlen) throws IOException{
        filepath = gpxpath;
        gpx = GPX.read(gpxpath);
        
        for (Route route : gpx.getRoutes()) {
            System.out.println(route.getName().orElse(""));
            for(WayPoint wp : route.getPoints()){
                consumecoords(wp.getTime().orElse(ZonedDateTime.now()),wp.getLatitude(), wp.getLongitude(), wp.getElevation().orElse(Length.of(0, Length.Unit.FOOT)));
            }
        }
        
        for (Track track : gpx.getTracks()) {
            System.out.println(track.getName().orElse(""));
            
            int lastvalidsegment = 0;
//            if (trimlen > 0) {
//                for (int i = 1; i < track.getSegments().size(); i++) {
//                    WayPoint lastpoint = track.getSegments().get(i - 1).getPoints().get(track.getSegments().get(i - 1).getPoints().size() - 1);
//                    WayPoint firstpoint = track.getSegments().get(i).getPoints().get(0);
//                    if (distanceInMiles(lastpoint.getLatitude().doubleValue(), lastpoint.getLongitude().doubleValue()
//                            , firstpoint.getLatitude().doubleValue(), firstpoint.getLongitude().doubleValue()) > trimlen) {
//                        lastvalidsegment = i;
//                    }
//                }
//            }
     
            for (int i = lastvalidsegment; i < track.getSegments().size(); i++) {
                for (WayPoint wp : track.getSegments().get(i).getPoints()) {
                    consumecoords(wp.getTime().orElse(ZonedDateTime.now()), wp.getLatitude(), wp.getLongitude(), wp.getElevation().orElse(Length.of(0, Length.Unit.FOOT)));
                }
            }
        }
        
        for (WayPoint wp : gpx.getWayPoints()) {
            System.out.println(wp.getName().orElse(""));
            consumecoords(wp.getTime().orElse(ZonedDateTime.now()),wp.getLatitude(), wp.getLongitude(), wp.getElevation().orElse(Length.of(0, Length.Unit.FOOT)));
        }
        
        System.out.println("minLONG:" + minLONG);
        System.out.println("minLAT:" + minLAT);
        System.out.println("minELEV:" + minELEV);
        System.out.println("maxLONG:" + maxLONG);
        System.out.println("maxLAT:" + maxLAT);
        System.out.println("maxELEV:" + maxELEV);
    }
 
    private void consumecoords(ZonedDateTime time,Latitude latitude, Longitude longitude, Length elevation) {
        double lat = latitude.toDegrees();
        double lon = longitude.toDegrees();
        double elev = elevation.to(Length.Unit.FOOT);

        //get min/max Latitude
        if (lat < minLAT) {
            minLAT = lat;
        }
        if (lat > maxLAT) {
            maxLAT = lat;
        }
        //get min/max Latitude
        if (lon < minLONG) {
            minLONG = lon;
        }
        if (lon > maxLONG) {
            maxLONG = lon;
        }
        //get min/max elevation
        if (elev < minELEV) {
            minELEV = elev;
        }
        if (elev > maxELEV) {
            maxELEV = elev;
        }
        
        //get min/max time
        if(time.isBefore(oldest)){
            oldest = time;
        }
        if(time.isAfter(newest)){
            newest = time;
        }
    }
    
    public void drawTracks(JXMapViewer mv){
        List<GeoPosition> allpoints = new ArrayList();
        List<Painter> Painters = new  ArrayList<>();
        for (Track track : gpx.getTracks()) {
            System.out.println(track.getName().orElse(""));
            track.segments().forEach(new Consumer() {
                @Override
                public void accept(Object t) {
                    TrackSegment ts = (TrackSegment) t;
                    
                    List<GeoPosition> trk = new ArrayList((int)ts.points().count());
                    
                    for (WayPoint wp : ts.getPoints()) {
                        GeoPosition pos = new GeoPosition(wp.getLatitude().toDegrees(), wp.getLongitude().toDegrees());
                        trk.add(pos);
                        allpoints.add(pos);
                    }
                    
                    Painters.add(new RoutePainter(trk));
                }
            });
        }
        
        Set<Waypoint> allwaypoints = new HashSet();
        for (WayPoint wp : gpx.getWayPoints()) {
            System.out.println(wp.getName().orElse(""));
            allwaypoints.add(new DefaultWaypoint(wp.getLatitude().toDegrees(), wp.getLongitude().toDegrees()));
            //allpoints.add(new GeoPosition(wp.getLatitude().toDegrees(), wp.getLongitude().toDegrees()));
            
        }
        //org.jxmapviewer.viewer.WaypointPainter painter = new org.jxmapviewer.viewer.WaypointPainter();
        //painter.setWaypoints(allwaypoints);
        //Painters.add(painter);
        Painters.add(new WaypointPainter(gpx.getWayPoints()));
        
        mv.setOverlayPainter(new CompoundPainter(Painters));
        mv.zoomToBestFit(new HashSet<GeoPosition>(allpoints), 0.9);
    }
    
public static class RoutePainter implements Painter<JXMapViewer>
{
    private Color color = Color.RED;
    private boolean antiAlias = true;

    private List<GeoPosition> track;

    public RoutePainter(List<GeoPosition> track,Color color) {
        this(track);
        this.color = color;
    }
    
    /**
     * @param track the track
     */
    public RoutePainter(List<GeoPosition> track)
    {
        // copy the list so that changes in the 
        // original list do not have an effect here
        this.track = new ArrayList<GeoPosition>(track);
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h)
    {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (antiAlias)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // do the drawing
//        g.setColor(Color.BLACK);
//        g.setStroke(new BasicStroke(4));
//
//        drawRoute(g, map);

        // do the drawing again
        g.setColor(color);
        g.setStroke(new BasicStroke(2));

        drawRoute(g, map);

        g.dispose();
    }

    /**
     * @param g the graphics object
     * @param map the map
     */
    private void drawRoute(Graphics2D g, JXMapViewer map)
    {
        int lastX = 0;
        int lastY = 0;

        boolean first = true;

        for (GeoPosition gp : track)
        {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

            if (first)
            {
                first = false;
            }
            else
            {
                g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
            }

            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }
    }
    }
    
    public static class WaypointPainter implements Painter<JXMapViewer> {

        List<WayPoint> waypoints;
        private boolean antiAlias = true;
        private Color color = Color.RED;
        
        public WaypointPainter(List<WayPoint> waypoints,Color color) {
            super();
            this.color = color;
            this.waypoints = waypoints;
        }
        
        public WaypointPainter(List<WayPoint> waypoints) {
            super();
            this.waypoints = waypoints;
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            g = (Graphics2D) g.create();

            // convert from viewport to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            if (antiAlias) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            // do the drawing again
            g.setColor(color);
            g.setStroke(new BasicStroke(2));

            for (WayPoint wp : waypoints) {
                Point2D pt = map.getTileFactory().geoToPixel(new GeoPosition(wp.getLatitude().doubleValue(), wp.getLongitude().doubleValue()), map.getZoom());
                g.fillOval((int) pt.getX(), (int) pt.getY(), 10, 10);
                g.drawString(wp.getName().orElse(""), (int) pt.getX(), (int) pt.getY() - 2);
            }

            g.dispose();
        }

    }

    public static double distanceInMiles(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            return (dist);
        }
    }

}
