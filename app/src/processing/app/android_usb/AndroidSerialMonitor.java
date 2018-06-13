package android_usb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.nio.file.*;

public class AndroidSerialMonitor extends JFrame implements ActionListener, WindowListener {

    private String lastDevice;
    private USBHandler handler;
    private JTextArea messageList;
    private JTextField messageField;
    private JPanel topPanel, bottomPanel;
    private JButton sendButton;
    private JCheckBox autoscrollBox;

    private boolean stopObserver;

    public AndroidSerialMonitor(String lastDevice, USBHandler usbHandler) {
        this.handler = usbHandler;
        this.lastDevice = lastDevice;
        stopObserver = false;

        launchMonitor();

        Path devicePath = handler.getDevicePathForDevice(lastDevice);

        //Find target directory
        File directories = new File(devicePath.toString());
        File[] listDirectories = directories.listFiles();

        for (File file : listDirectories) {
            if (file.isDirectory()) {

                File targetDir = new File(file.toString() + "/" + USBManager.ANDROID_PATH_SERIAL);
                targetDir.mkdirs();
                File targetFileTransmitter = new File(targetDir.toString().concat("/sofia_transmitter.txt"));
                File targetFileReceiver = new File(targetDir.toString().concat("/sofia_receiver.txt"));
                try {
                    targetFileTransmitter.createNewFile();
                    targetFileReceiver.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Thread(new ObserverDevice(targetFileTransmitter)).start();
            }
        }
    }


    private void launchMonitor() {
        ////////////////////Set up TextField///////////////////////////////
        messageField = new JTextField();
        messageField.setEditable(false);
        ////////////////////Set up Display///////////////////////////////
        messageList = new JTextArea();

        /////////////////////Set Up Top Pannel///////////////////////////////
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        sendButton = new JButton();
        sendButton.setText("Send");
        sendButton.setActionCommand("SEND");
        sendButton.addActionListener(this);

        topPanel.add(messageField, BorderLayout.CENTER);
        topPanel.add(sendButton, BorderLayout.EAST);

        ////////////////////Set Up Bottom Panel////////////////////////////////
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        autoscrollBox = new JCheckBox();
        autoscrollBox.setText("Autoscroll");

        bottomPanel.add(autoscrollBox, BorderLayout.WEST);

        ///////////////////////Add all to JFrame/////////////////////////////
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);    //Close only this frame
        this.add(topPanel, BorderLayout.NORTH);
        this.add(new JScrollPane(messageList), BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.AFTER_LAST_LINE);

        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(dim.width / 2, dim.height / 2));
        this.setExtendedState(JFrame.NORMAL);
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        this.setVisible(true);

        this.addWindowListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("SEND")) {

            handler.sendSerialMessage(lastDevice, messageField.getText());
            messageField.setText("");

        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        stopObserver = true;
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

    private class ObserverDevice implements Runnable {

        private File observeFile;
        private long fileSize;
        private BufferedReader reader;

        public ObserverDevice(File observeFile) {
            this.observeFile = observeFile;
            fileSize = 0;
        }

        @Override
        public void run() {

            try {
                reader = new BufferedReader(new FileReader(observeFile));
                while (!stopObserver) {
                    if (observeFile.length() != fileSize) {
                        fileSize = observeFile.length();

                        try {
                            //for (int i = 0; i < fileSize; i++) {
                                messageList.append(String.valueOf((char) reader.read()));
                            //}
                            //messageList.append("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Finish Thread.");
        }
    }
}

