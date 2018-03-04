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
    private JList<String> deviceList;
    private JPanel bottomPanel, buttonPanel;
    private JButton okButton, cancelButton;
    private JCheckBox setDefaultDevice;

    private DeviceWatcher watcher;

    public DeviceSelector(Path buildPath) {
        super("Select Device");

        this.handler = new USBManager(buildPath);
        watcher = new DeviceWatcher(this, handler);

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
        okButton.addActionListener(this);

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPanel.add(cancelButton);

        ////////////////////Set Up Bottom Panel////////////////////////////////
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        setDefaultDevice = new JCheckBox();
        setDefaultDevice.setText("Set as default device");

        bottomPanel.add(setDefaultDevice, BorderLayout.WEST);
        bottomPanel.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        ///////////////////////Add all to JFrame/////////////////////////////
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);    //Close only this frame
        this.add(new JScrollPane(deviceList),BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.AFTER_LAST_LINE);

        this.pack();
        this.setMinimumSize(new Dimension(250, 250));
        this.setExtendedState(JFrame.NORMAL);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
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

                //Close JFrame
                closeFrame();
            }
            else {
                System.out.println("Select a device");
            }

        }
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
