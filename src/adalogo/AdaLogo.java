/*
 * Copyright 2005 Hailang Thai, Minh Cuong Tran, Lesmana Zimmer
 *
 * This file is part of AdaLogo.
 *
 * AdaLogo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * AdaLogo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AdaLogo; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package adalogo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FilePermission;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * this is the main class.
 * it has a main() method to start as application
 * and also init() to run as applet.
 * when running as applet it will only have one button
 * which will then open the frame.
 */
public class AdaLogo extends JApplet {

    /**
     * main method when run as application, this will just start the engine.
     * everything else here is only of interrest if run as applet
     * @param args
     */
    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.mainScreenTurnOn();
    }

    /**
     * this button will open the main frame. it will be disabled
     * when frame is opened and enabled when frame is closed.
     * only relevant if run as applet.
     */
    private JButton startEngineButton;

    private Engine engine;

    /**
     * this will start the engine, which will open the main frame.
     * this method will be called by the button if started as applet.
     */
    public void startEngine() {
        startEngineButton.setEnabled(false);
        engine = new Engine();
        engine.mainScreenTurnOn();
    }

    /**
     * this will be called by engine, if frame was closed,
     * to re-enable button.
     */
    public void stopEngine() {
        startEngineButton.setEnabled(true);
        engine = null;
    }

    /**
     * constructor, used when run as applet.
     * this will create a button which will start the engine.
     */
    public AdaLogo() {

        //engine = new Engine();

        Settings.setApplet(this);

        //check for file access permission
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(new FilePermission("-", "read,write"));
            } else {
                System.out.println("security null?");
            }
        } catch (SecurityException e) {
            //e.printStackTrace();
            Settings.setFilePermission(false);
        }

        //button which will start engine
        startEngineButton = new JButton("show window again");
        startEngineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startEngine();
            }
        });

        JLabel intro = new JLabel("AdaLogo Applet");
        JLabel ver = new JLabel("version: @BUILDTIME@");

        JPanel content = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        content.setLayout(gbl);

        gbc.gridwidth = GridBagConstraints.REMAINDER; //last in row
        content.add(intro, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER; //last in row
        content.add(startEngineButton, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER; //last in row
        content.add(ver, gbc);

        setContentPane(content);

    }

    /**
     * init when run as applet.
     * @see java.applet.Applet#init()
     */
    public void init() {
        showStatus("main screen turn on");
        startEngine();
    }

    //applet method
    public void start() {
    }

    //applet method
    public void stop() {
    }

    //applet method
    public void destroy() {
    }

    //applet information
    public String getAppletInfo() {
        return "you know what you doing";
    }

    //applet information
    public String[][] getParameterInfo() {
        String[][] ret = {
                {"zig","int","move how many zig"},
        };
        return ret;
    }

}
