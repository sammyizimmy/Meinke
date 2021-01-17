/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meinke;

import io.jenetics.jpx.Route;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.WayPoint;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author classic
 */
public class TrackCheckBoxList extends JScrollPane{

    private JPanel cbpanel = null;
    private Object mouseover = null;
    private ActionListener updateaction = null;

    public TrackCheckBoxList() {
        super();
    }
    
    public TrackCheckBoxList(List<GPXProcessor> tracks,ActionListener updateaction) {
        super();
        setUpdateaction(updateaction);
        generateContents(tracks);
    }

    public void setUpdateaction(ActionListener updateaction) {
        this.updateaction = updateaction;
    }
 
    public void generateContents(List<GPXProcessor> tracks) {
        cbpanel = new JPanel();
        cbpanel.setLayout(new BoxLayout(cbpanel, BoxLayout.Y_AXIS));
        for (GPXProcessor gpxtrack : tracks) {
            JPanel gpxpanel = new JPanel();
            gpxpanel.setLayout(new BoxLayout(gpxpanel, BoxLayout.Y_AXIS));

            String datetime = "From:" + gpxtrack.newest.format(DateTimeFormatter.ISO_LOCAL_DATE) + " To:" + gpxtrack.oldest.format(DateTimeFormatter.ISO_LOCAL_DATE);

            gpxpanel.add(createTitlecheckbox(new File(gpxtrack.filepath).getName(), datetime));

            for (Route route : gpxtrack.gpx.getRoutes()) {
                gpxpanel.add(createItemcheckbox(route.getName().orElse("noname"), datetime));
            }
            for (Track track : gpxtrack.gpx.getTracks()) {
                gpxpanel.add(createItemcheckbox(track.getName().orElse("noname"), datetime));
            }
            for (WayPoint waypoint : gpxtrack.gpx.getWayPoints()) {
                gpxpanel.add(createItemcheckbox(waypoint.getName().orElse("noname"), datetime));
            }
            gpxpanel.add(Box.createRigidArea(new Dimension(10, 10)));
            cbpanel.add(gpxpanel);
        }
        this.setViewportView(cbpanel);
    }
    
    public JCheckBox getCheckbox(int gpxnum, int routenum, int tracknum, int waypointnum) {
        return ((JCheckBox) ((JPanel) ((JPanel) cbpanel.getComponent(gpxnum)).getComponent(1 + routenum + tracknum + waypointnum)).getComponent(1));
    }
    
    public JCheckBox getTitleCheckbox(int num) {
        return ((JCheckBox) ((JPanel) cbpanel.getComponent(num)).getComponent(0));
    }

    public Object getMouseover() {
        return mouseover;
    }
    
    
    private JComponent createItemcheckbox(String name, String tooltip) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JCheckBox jcb = new JCheckBox(name);
        jcb.setSelected(true);
        jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JCheckBox source = (JCheckBox) evt.getSource();
                JCheckBox titlecheckbox = (JCheckBox) source.getParent().getParent().getComponent(0);
                if (!titlecheckbox.isSelected() && source.isSelected()) {
                    titlecheckbox.setSelected(true);
                }
                if (updateaction != null) {
                    updateaction.actionPerformed(evt);
                }
            }
        });
        jcb.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                mouseover = arg0.getComponent();
                if (updateaction != null) {
                    updateaction.actionPerformed(null);
                }
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                if (arg0.getComponent() == mouseover) {
                    mouseover = null;
                    if (updateaction != null) {
                        updateaction.actionPerformed(null);
                    }
                }
            }
        });
        if (tooltip != null) {
            jcb.setToolTipText(tooltip);
        }
        panel.add(Box.createRigidArea(new Dimension(20, 1)));
        panel.add(jcb);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JComponent createTitlecheckbox(String name, String tooltip) {
        JCheckBox title = new JCheckBox(name);
        title.setSelected(true);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JCheckBox source = (JCheckBox) evt.getSource();
                for (Component child : source.getParent().getComponents()) {
                    if (child.getClass() == JPanel.class) {
                        JCheckBox target = (JCheckBox) ((JPanel) child).getComponent(1);
                        target.setSelected(source.isSelected());
                    }
                }
                if (updateaction != null) {
                    updateaction.actionPerformed(evt);
                }
            }
        });
        title.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                mouseover = arg0.getComponent();
                if (updateaction != null) {
                    updateaction.actionPerformed(null);
                }
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                if (arg0.getComponent() == mouseover) {
                    mouseover = null;
                    if (updateaction != null) {
                        updateaction.actionPerformed(null);
                    }
                }
            }
        });
        if (tooltip != null) {
            title.setToolTipText(tooltip);
        }
        return title;
    }

}
