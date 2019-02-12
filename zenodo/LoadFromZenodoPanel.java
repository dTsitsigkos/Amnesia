/* 
 * Copyright (C) 2015 "IMIS-Athena R.C.",
 * Institute for the Management of Information Systems, part of the "Athena" 
 * Research and Innovation Centre in Information, Communication and Knowledge Technologies.
 * [http://www.imis.athena-innovation.gr/]
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 */
package zenodo;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import static javax.swing.SwingWorker.StateValue.DONE;
//import privacytool.framework.data.rest.ZenodoFile;
//import privacytool.gui.MainGUI;
//import privacytool.gui.loadingWorkers.ZenodoDownloadFileWorker;
//import privacytool.gui.loadingWorkers.ZenodoGetFilesWorker;
//import privacytool.gui.loadingWorkers.LoadingFrame;

/**
 * Panel to load data from Zenodo
 * @author serafeim
 */
public class LoadFromZenodoPanel {
    String accessToken;
    boolean verified = false;       //if access token is verified
    Semaphore mutex = new Semaphore(0);
    //MainGUI mainGui;
    private final String baseDownloadPath;
    
    public LoadFromZenodoPanel( String baseDownloadPath){
        //this.mainGui = mainGui;
        this.baseDownloadPath = baseDownloadPath;
    }
    
    /**
     * Shows access token input panel
     * @return the access token given
     */
    public String showAccessTokenPanel() {
            AccessTokenPanel accessTokenPanel = new AccessTokenPanel(accessToken);
            int result = JOptionPane.showConfirmDialog(null, accessTokenPanel,
                    "Zenodo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                this.accessToken = accessTokenPanel.getAccessToken();
            }
            else{
                return null;
            }

        return accessToken;
    }
    
    /**
     * Load panel showing available files for loading
     */
    public void loadZenodoFilePanel(){
        
        LoadingFrame loadingFrame = new LoadingFrame();
        loadingFrame.showLoading(LoadingFrame.CONTINUOUS);
        
        final ZenodoGetFilesWorker filesWorker = new ZenodoGetFilesWorker(accessToken, loadingFrame);
        filesWorker.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getPropertyName().equals("state") &&
                        ((StateValue)event.getNewValue()).equals(DONE)){
                    try {
                        
                        //get files found for this access token
                        Map<Integer, ZenodoFile> files = filesWorker.get();
                        if(files == null)
                            return;
                        
                        verified = true;
                        
                        //show dialog to choose file to load
                        ChooseFilePanel chooseFilePanel = new ChooseFilePanel(files);
                        int result = JOptionPane.showConfirmDialog(null, chooseFilePanel,
                                "Zenodo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (result == JOptionPane.OK_OPTION) {
                            //get selected file
                            ZenodoFile selectedFile = files.get(chooseFilePanel.getSelectedIndex());
                            
                            //download file from repository
                            downloadFileFromZenodo(selectedFile);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(LoadFromZenodoPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
        });
        filesWorker.execute();
    }
    
    /**
     * Downloads a file from Zenodo
     * @param file the file to be downloaded
     */
    private void downloadFileFromZenodo(ZenodoFile file){
        LoadingFrame loadingFrame = new LoadingFrame();
        loadingFrame.showLoading(LoadingFrame.CONTINUOUS);
        final ZenodoDownloadFileWorker downloadWorker = new ZenodoDownloadFileWorker(file, baseDownloadPath, accessToken, loadingFrame);
        downloadWorker.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getPropertyName().equals("state") &&
                        ((StateValue)event.getNewValue()).equals(DONE)){
                    try {
                        //get path of downloaded file
                        String localFile = downloadWorker.get();
                        
                        //load file
                        File file = new File(localFile);
                        //mainGui.loadAndDeleteFile(file);
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(LoadFromZenodoPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        downloadWorker.execute();
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public boolean isAccessTokenVerified() {
        return verified;
    }
    
    
    /**
     * Panel to choose file for download
     */
    private class ChooseFilePanel extends JPanel{
        JLabel label;
        JList filesList;
        
        ChooseFilePanel(Map<Integer, ZenodoFile> files){
            setLayout(new BorderLayout());
            label = new JLabel("Please choose file to load:");
            
            //convert list to array
            ZenodoFile[] filesArr = convertToArray(files);
            
            //show jlist with available files
            filesList = new JList(filesArr);
            filesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            filesList.setLayoutOrientation(JList.VERTICAL);
            
            //add mouse listener for tooltips
            filesList.addMouseMotionListener(new MouseMotionListener() {
                
                @Override
                public void mouseDragged(MouseEvent e) {
                }
                
                @Override
                public void mouseMoved(MouseEvent e) {
                    JList l = (JList) e.getSource();
                    ListModel m = l.getModel();
                    int index = l.locationToIndex(e.getPoint());
                    if (index > -1) {
                        l.setToolTipText(((ZenodoFile)m.getElementAt(index)).toTooltip());
                    }
                }
            });
            
            JScrollPane listScroller = new JScrollPane(filesList);
            
            //add to panel
            add(label, BorderLayout.NORTH);
            add(listScroller, BorderLayout.CENTER);
        }
        
        private ZenodoFile[] convertToArray(Map<Integer, ZenodoFile> map){
            ZenodoFile[] arr = new ZenodoFile[map.size()];
            for(Integer fileNum : map.keySet()){
                arr[fileNum] = map.get(fileNum);
            }
            
            return arr;
        }
        
        public int getSelectedIndex(){
            return this.filesList.getSelectedIndex();
        }
    }
    
    /**
     * Panel to insert access token
     */
    private class AccessTokenPanel extends JPanel{
        JLabel label;
        JPasswordField pwdField;
        
        AccessTokenPanel(String prevAccessToken){
            label = new JLabel("Please enter Zenodo Access Token:");
            pwdField = new JPasswordField(60);
            if(prevAccessToken != null)
                pwdField.setText(prevAccessToken);
            
            add(label);
            add(pwdField);
            JCheckBox hidePasswordCheckbox = new JCheckBox("Hide");
            hidePasswordCheckbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        pwdField.setEchoChar('*');
                    } else {
                        pwdField.setEchoChar((char) 0);
                    }
                }
            });
            hidePasswordCheckbox.setSelected(true);
            add(hidePasswordCheckbox);
        }
        
        String getAccessToken(){
            return new String(this.pwdField.getPassword());
        }
    }
}
