/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import meinke.GPSS.Garmin;

/**
 *
 * @author classic
 */
public class GPSButton extends JButton{

    private Garmin gps;
    JPopupMenu menu = new JPopupMenu("Popup");
    
    public GPSButton(Garmin gps) {
        super();
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setHorizontalTextPosition(SwingConstants.LEFT);
        this.setVerticalAlignment(SwingConstants.TOP);
        this.setVerticalTextPosition(SwingConstants.TOP);
        this.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        this.setText(getext(gps));
        
        this.gps = gps;
        
        JMenuItem item = new JMenuItem("Import Tracks");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Menu item Test1");
            }
        });
        //menu.add(item);

        item = new JMenuItem("Export Tracks");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Menu item Test2");
            }
        });
        //menu.add(item);

        item = new JMenuItem("Clear Tracks");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Are you sure you want to clear all tracks from GPS?\nArchived tracks will remain untouched", "Clear gps", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    List<File> files = gps.listGPXfiles();
                    for (File file : files) {
                        file.delete();
                    }
                    gps.setCleared(true);
                    setText(getext(gps));
                }
            }
        });
        menu.add(item);
        
        item = new JMenuItem("Eject");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gps.eject();
            }
        });
        menu.add(item);
        
        item = new JMenuItem("Alias");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String retval = null;
                if(gps.getAlias() != null){
                    retval = JOptionPane.showInputDialog(Meinke.mf, "Enter Alias", gps.getAlias());
                }else{
                    retval = JOptionPane.showInputDialog(Meinke.mf, "Enter Alias", gps.getID());
                }
                
                if(retval != null && !retval.isEmpty()){
                    gps.setAlias(retval);
                    setText(getext(gps));
                }
            }
        });
        menu.add(item);
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    menu.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    menu.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent ev) {
            }
        });
    }

    public String getext(Garmin gps){
        String results = "<html><center>";
        results += gps.getDesc();
        results += "<br></br>";
        if(gps.getAlias() != null){
            results += gps.getAlias();
        }else{
            results += gps.getID();
        }
        results += "<br></br><hr>";
        
        results += "<table cellspacing=\"0\" cellpadding=\"0\">";
        
        if(gps.isImported()){
            results += "<tr><td>Imported</td><td><font color = green>&#10003;</font></td></tr>";
        }else{
            results += "<tr><td>Imported</td><td><font color = red>&#10005;</font></td></tr>";
        }
        
        if (gps.isExported()) {
            results += "<tr><td>Exported</td><td><font color = green>&#10003;</font></td></tr>";
        } else {
            results += "<tr><td>Exported</td><td><font color = red>&#10005;</font></td></tr>";
        }

        if (gps.isCleared()) {
            results += "<tr><td>Cleared</td><td><font color = green>&#10003;</font></td></tr>";
        } else {
            results += "<tr><td>Cleared</td><td><font color = red>&#10005;</font></td></tr>";
        }
        
        results += "</table></center></html>";
        
        return results;
    }
    
    public Garmin getGps() {
        return gps;
    }
}
