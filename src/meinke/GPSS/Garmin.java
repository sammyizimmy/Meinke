/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke.GPSS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import meinke.Meinke;
import static meinke.Meinke.createMeinkeDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author classic
 */
public class Garmin {
    private static final String INFOFILE = "Garmin" + File.separatorChar + "GarminDevice.xml";
    private static final String GPXFOLDER = "Garmin" + File.separatorChar + "GPX" + File.separatorChar;
    private static final String MAPFOLDER = "Garmin" + File.separatorChar + "CustomMaps" + File.separatorChar;
    
    private String RootPath;
    private String Desc;
    private String ID;
    private String Alias;
    
    private boolean Imported = false;
    private boolean Exported = false;
    private boolean Cleared = false;
    
    private boolean isEjecting = false;
    
    public Garmin(String rootpath) {
        RootPath = rootpath;
        File xmlfile = new File(RootPath + File.separatorChar + INFOFILE);
        //load info
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlfile);
            Element rootElement = document.getDocumentElement();

            Desc = getString("Description", rootElement);
            ID = getString("Id", rootElement);
            
            FileInputStream in = null;
            try {
                Properties defaultProps = new Properties();
                in = new FileInputStream(RootPath + File.separatorChar + "Meinke.ini");
                defaultProps.load(in);
                in.close();
                Alias = defaultProps.getProperty("ALIAS");
            } catch (Exception ex) {
                Logger.getLogger(Garmin.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    in.close();
                } catch (Exception ex) {
                    Logger.getLogger(Garmin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    public void eject() {
        if (!isEjecting) {
            String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("windows")) {
                String exepath = Meinke.unpackResourceToTempFile("eject.exe");
                if (exepath == null) {
                    JOptionPane.showMessageDialog(null, "Whoops! Cannot unpack needed files.\nPlease eject manually");
                    return;
                }
                File exefile = new File(exepath);
                new Thread(new Runnable() {
                    public void run() {
                        isEjecting = true;
                        if (exefile.exists()) {
                            System.out.println("Ejecting " + RootPath);
                            try {
                                Process p = Runtime.getRuntime().exec(new String[]{exepath, RootPath, "-L"});
                                p.waitFor();
                                Scanner s = new Scanner(p.getInputStream());
                                while (s.hasNextLine()) {
                                    System.out.println(s.nextLine());
                                }
                                s.close();
                                System.out.println("Ejected " + RootPath);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        isEjecting = false;
                    }
                }).start();
            } else if (os.toLowerCase().contains("linux")) {
                String exepath = Meinke.unpackResourceToTempFile("eject.sh");
                if (exepath == null) {
                    JOptionPane.showMessageDialog(null, "Whoops! Cannot unpack needed files.\nPlease eject manually");
                    return;
                }
                File exefile = new File(exepath);
                exefile.setExecutable(true, false);

                new Thread(new Runnable() {
                    public void run() {
                        isEjecting = true;
                        if (exefile.exists()) {
                            System.out.println("Ejecting " + RootPath);
                            try {
                                Process p = Runtime.getRuntime().exec(new String[]{exepath, RootPath});
                                p.waitFor();
                                Scanner s = new Scanner(p.getInputStream());
                                while (s.hasNextLine()) {
                                    System.out.println(s.nextLine());
                                }
                                s.close();
                                System.out.println("Ejected " + RootPath);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        isEjecting = false;
                    }
                }).start();

            } else {
                JOptionPane.showMessageDialog(null, "Whoops! Eject doesn't work ont his platform.\nPlease do it manually");
            }
        }else{
            JOptionPane.showMessageDialog(null, "Eject is in progress for this device.");
        }
    }

    protected String getString(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();

            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }

    public static boolean isGarmin(String rootpath){
        File file = new File(rootpath + File.separatorChar + INFOFILE);
        return file.exists();
    }

    public String getID() {
        return ID;
    }

    public String getDesc() {
        return Desc;
    }

    public String getRootPath() {
        return RootPath;
    }

    public String getGpxFolder() {
        return RootPath + File.separatorChar + GPXFOLDER;
    }

    public String getMapFolder() {
        return RootPath + File.separatorChar + MAPFOLDER;
    }

    public List<File> listGPXfiles() {
        File fbase = new File(getRootPath() + File.separatorChar + "Garmin" + File.separatorChar + "GPX" + File.separatorChar);
        File fcurrent = new File(getRootPath() + File.separatorChar + "Garmin" + File.separatorChar + "GPX" + File.separatorChar + "Current" + File.separatorChar);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.toLowerCase().endsWith(".gpx");
            }
        };
        
        File[] fbasefiles = fbase.listFiles(filter);
        File[] fcurrentfiles = fcurrent.listFiles(filter);
        
        ArrayList<File> list = new ArrayList<>(fbasefiles.length + fcurrentfiles.length);
        list.addAll(Arrays.asList(fbasefiles));
        list.addAll(Arrays.asList(fcurrentfiles));
        return list;
    }

    public String getAlias() {
        return Alias;
    }

    public void setAlias(String Alias) {
        this.Alias = Alias;
        try {
            Properties defaultProps = new Properties();
            createMeinkeDir();
            FileOutputStream out = new FileOutputStream(RootPath + File.separatorChar + "Meinke.ini");
            defaultProps.setProperty("ALIAS", Alias);
            defaultProps.store(out, "---No Comment---");
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Meinke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isImported() {
        return Imported;
    }

    public boolean isExported() {
        return Exported;
    }

    public boolean isCleared() {
        return Cleared;
    }

    public void setImported(boolean Imported) {
        this.Imported = Imported;
    }

    public void setExported(boolean Exported) {
        this.Exported = Exported;
    }

    public void setCleared(boolean Cleared) {
        this.Cleared = Cleared;
    }
}
