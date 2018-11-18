/* 
  Copyright (c) 2018, Kollins Gabriel Lima <kollins.lima@gmail.com>

  This file is part of SOFIA project - https://project-sofia.gitbook.io/project/.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,Inc
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package android_usb;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class DeviceSelector extends JFrame implements ListSelectionListener, ActionListener, WindowListener {

    private USBHandler handler;
    private String defaultDevice = null;
    private String selectedDevice = null;
    private String lastDevice = null;
    private JList<String> deviceList;
    private JPanel bottomPanel, buttonPanel;
    private JButton okButton, cancelButton;
    private JCheckBox setDefaultDevice;

    private boolean serialSection;

    private DeviceWatcher watcher;
    private AndroidSerialMonitor androidSerialMonitor;

    public DeviceSelector(Path buildPath) {
        super("Select Device");

        this.handler = new USBManager(buildPath);
        watcher = new DeviceWatcher(this, handler);
        serialSection = false;
        
        setUpFrame();

    }
    
    public DeviceSelector(AndroidSerialMonitor androidSerialMonitor, USBHandler handler){
        super("Select Device");

        this.handler = handler;
        this.androidSerialMonitor = androidSerialMonitor;
        watcher = new DeviceWatcher(this, handler);
        serialSection = true;

        setUpFrame();
    }
    

    public void choose() {

        if(defaultDevice != null){
            handler.copyHexToDevice(defaultDevice);
        }
        else{
            selectedDevice = null;

            this.setVisible(true);

            loadList();

            new Thread(watcher).start();
        }
    }

    public USBHandler getUSBHandler(){
        return handler;
    }

    public String getLastDevice(){
        return lastDevice;
    }

    public String getSelectedDevice(){
        return selectedDevice;
    }

    public synchronized void loadList(){

        ArrayList<String> devices = handler.getDevices();       

        //Fill List
        DefaultListModel<String> listModel = (DefaultListModel<String>) deviceList.getModel();
        listModel.removeAllElements();

        for (String device : devices) {
            listModel.addElement(device);
        }
        deviceList.setModel(listModel);

    }

    //Set up JFrame and its components
    private void setUpFrame(){

        ////////////////////Set up JList///////////////////////////////
        deviceList = new JList<String>(new DefaultListModel<String>());
        deviceList.addListSelectionListener(this);
//        text.setSize(new Dimension(250,250));
//        text.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /////////////////////Set Up Buttons///////////////////////////////
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        okButton = new JButton();
        okButton.setText("Ok");
        okButton.setActionCommand("OK");

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setActionCommand("CANCEL");

        if (serialSection){
            okButton.addActionListener(androidSerialMonitor);
            cancelButton.addActionListener(androidSerialMonitor);
        } else {
            okButton.addActionListener(this);
            cancelButton.addActionListener(this);
        }

        buttonPanel.add(okButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPanel.add(cancelButton);

        ////////////////////Set Up Bottom Panel////////////////////////////////
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        setDefaultDevice = new JCheckBox();
        setDefaultDevice.setText("Set as default device");
        setDefaultDevice.setVisible(!serialSection);

        bottomPanel.add(setDefaultDevice, BorderLayout.WEST);
        bottomPanel.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        ///////////////////////Add all to JFrame/////////////////////////////
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);    //Close only this frame
        this.add(new JScrollPane(deviceList),BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.AFTER_LAST_LINE);

        this.pack();

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(dim.width/2, dim.height/2));
        this.setExtendedState(JFrame.NORMAL);
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        this.addWindowListener(this);
    }

    //JList listener
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (selectedDevice == null || (!selectedDevice.equals(deviceList.getSelectedValue()))){
            selectedDevice = deviceList.getSelectedValue();
            //System.out.println("Device selected: " + deviceList.getSelectedValue());
        }

    }

    //JButton Listener
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        //System.out.println(command);

        if (command.equals("CANCEL")){
            selectedDevice = null;
            //System.out.println("Nothing Choosed");

            //Close JFrame
            closeFrame();
        }
        else if (command.equals("OK")){

            if (selectedDevice != null) {
                if (setDefaultDevice.isSelected()) {
                    defaultDevice = selectedDevice;
                }

                handler.copyHexToDevice(selectedDevice);               

                lastDevice = new String(selectedDevice);
        
                //Close JFrame
                closeFrame();
            }
            else {
                System.out.println("Select a device");
            }
        }
    }

    public void closeFrameFromOutside(){
        closeFrame();
    }

    private void closeFrame(){
        watcher.setFinish(true);
        this.setVisible(false);
        this.dispose();
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        watcher.setFinish(true);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
