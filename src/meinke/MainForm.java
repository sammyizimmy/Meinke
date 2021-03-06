/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import meinke.GPSS.Garmin;
import net.samuelcampos.usbdrivedetector.USBDeviceDetectorManager;
import net.samuelcampos.usbdrivedetector.events.DeviceEventType;
import net.samuelcampos.usbdrivedetector.events.IUSBDriveListener;
import net.samuelcampos.usbdrivedetector.events.USBStorageEvent;

/**
 *
 * @author classic
 */
public class MainForm extends javax.swing.JFrame {
//System.getProperty("java.io.tmpdir") 
    
    private List<GPSButton> gpsbuttons = new ArrayList<>();
    GPS_Manage gpsmanager = null;
    /**
     * Creates new form MainForm
     */
    public MainForm() {
        initComponents();
        
        initGpsManager();
        
        this.setLocationRelativeTo(null);
        if(Meinke.MAIN_WINDOW_WIDTH > 0 && Meinke.MAIN_WINDOW_HEIGHT > 0){
            this.setBounds(Meinke.MAIN_WINDOW_X, Meinke.MAIN_WINDOW_Y, Meinke.MAIN_WINDOW_WIDTH, Meinke.MAIN_WINDOW_HEIGHT);
        }
    }

    private void initGpsManager() {
        USBDeviceDetectorManager driveDetector = new USBDeviceDetectorManager();
        driveDetector.getRemovableDevices().forEach(System.out::println);

        driveDetector.addDriveListener(new IUSBDriveListener() {
            @Override
            public void usbDriveEvent(USBStorageEvent event) {
                if (event.getEventType() == DeviceEventType.REMOVED) {
                    removeByRootPath(event.getStorageDevice().getRootDirectory().getAbsolutePath());
                } else {
                    if (Garmin.isGarmin(event.getStorageDevice().getRootDirectory().getAbsolutePath())) {
                        Garmin garmin = new Garmin(event.getStorageDevice().getRootDirectory().getAbsolutePath());
                        
                        GPSButton gpsbutton = new GPSButton(garmin);
                        gpsbutton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                gpsmanager = new GPS_Manage(garmin);
                                gpsmanager.setModal(true);
                                gpsmanager.show();
                                gpsmanager = null;
                                gpsbutton.setText(gpsbutton.getext(garmin));
                            }
                        });
                        blockPanel1.addComponent(gpsbutton);
                        
                        gpsbuttons.add(gpsbutton);
                        
                        System.out.println("Found Garmin " + garmin.getDesc() + " ID:" + garmin.getID());
                    }
                }
            }
        });
    }

    private void removeByRootPath(String rootpath) {
        int toremove = -1;
        //find item to remove
        for (int i = 0; i < gpsbuttons.size(); i++) {
            GPSButton btn = gpsbuttons.get(i);
            if (btn.getGps().getRootPath().equals(rootpath)) {
                toremove = i;
                break;
            }
        }
        
        if(toremove != -1) {
            if (gpsmanager != null) {
                gpsmanager.setVisible(false);
                gpsmanager = null;
            }
            blockPanel1.removeComponent(gpsbuttons.get(toremove));
            gpsbuttons.remove(toremove);
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

        blockPanel1 = new meinke.BlockPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemSettings = new javax.swing.JMenuItem();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuManage = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Meinke");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jMenuFile.setText("File");

        jMenuItemSettings.setText("Settings");
        jMenuItemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSettingsActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSettings);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar1.add(jMenuFile);

        jMenuManage.setText("Manage");
        jMenuBar1.add(jMenuManage);

        jMenuHelp.setText("Help");

        jMenuItemHelp.setText("Help");
        jMenuHelp.add(jMenuItemHelp);

        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(blockPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(blockPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        JOptionPane.showMessageDialog(this, "Meinke 1.0\n\n© 2021, Samuel Zimmerman (szimmerman484@gmail.com)\n\n"
                + "Copying and distribution of this file, with or without modification, are permitted in any medium\n"
                + "without royalty, provided the copyright notice and this notice are preserved. This\n"
                + "file is offered as-is, without any warranty.","About",JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSettingsActionPerformed
        SettingsForm sf = new SettingsForm();
        sf.setModal(true);
        sf.setLocationRelativeTo(this);
        sf.show();
    }//GEN-LAST:event_jMenuItemSettingsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Meinke.MAIN_WINDOW_X = this.getX();
        Meinke.MAIN_WINDOW_Y = this.getY();
        Meinke.MAIN_WINDOW_WIDTH = this.getWidth();
        Meinke.MAIN_WINDOW_HEIGHT = this.getHeight();
        Meinke.saveSettings();
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private meinke.BlockPanel blockPanel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemHelp;
    private javax.swing.JMenuItem jMenuItemSettings;
    private javax.swing.JMenu jMenuManage;
    // End of variables declaration//GEN-END:variables
}
