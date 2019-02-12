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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
//import privacytool.gui.ErrorWindow;

/**
 *
 * @author serafeim
 */
public class SaveToZenodoPanel extends JPanel{
    private static final Insets WEST_INSETS = new Insets(5, 0, 5, 5);
    private static final Insets EAST_INSETS = new Insets(5, 5, 5, 0);
    private int row;
    
    
    //text fields
    JTextField titleField;
    JTextField filenameField;
    JTextField authorNameField;
    JTextField affiliationField;
    JTextField descriptionField;
    
    JComboBox typesCombo;
    JComboBox accessRight;
    
    public SaveToZenodoPanel(Map<String, String> args){
        setLayout(new GridBagLayout());
        row = 0;
        initPanel(args);
    }
    
    private void initPanel(Map<String, String> args){
        
        //title
        JLabel label = new JLabel("Title*:", JLabel.LEFT);
        add(label, createGbc(0, row));
        
        titleField = new JTextField();
        titleField.setText((args.get("title") != null) ? args.get("title") : "Untitled");
        add(titleField, createGbc(1, row++));
        
        //filename
        label = new JLabel("File Name*:", JLabel.LEFT);
        add(label, createGbc(0, row));
        
        filenameField = new JTextField();
        filenameField.setText((args.get("filename") != null) ? args.get("filename") : "filename.csv");
        add(filenameField, createGbc(1, row++));
        
        //type
        label = new JLabel("Type*:", JLabel.LEFT);
        add(label, createGbc(0, row));
        
        String[] types = { "dataset", "publication", "poster", "presentation", "software"};
        typesCombo = new JComboBox(types);
        if(args.get("type") != null){
            typesCombo.setSelectedItem(args.get("type"));
        }
        add(typesCombo, createGbc(1, row++));
        
        //access right
        label = new JLabel("Access Right*:", JLabel.LEFT); //name affiliation
        add(label, createGbc(0, row));
        
        String[] accessRights = { "open", "closed"};
        accessRight = new JComboBox(accessRights);
        if(args.get("access") != null){
            accessRight.setSelectedItem(args.get("access"));
        }
        add(accessRight, createGbc(1, row++));
        
        //author
        label = new JLabel("Author*:", JLabel.LEFT);
        add(label, createGbc(0, row));
        
        authorNameField = new JTextField();
        authorNameField.setText((args.get("author") != null) ? args.get("author") : "Name Surname");
        add(authorNameField, createGbc(1, row++));
        
        //affiliation
        label = new JLabel("Affiliation:", JLabel.LEFT); //name affiliation
        add(label, createGbc(0, row));
        
        affiliationField = new JTextField();
        affiliationField.setText((args.get("affiliation") != null) ? args.get("affiliation") : "");
        add(affiliationField, createGbc(1, row++));
        
        //description
        label = new JLabel("Description*:", JLabel.LEFT);
        add(label, createGbc(0, row));
        
        descriptionField = new JTextField();
        descriptionField.setText((args.get("description") != null) ? args.get("description") :"");
//        descriptionField.setPreferredSize(new Dimension(150, 60));
        add(descriptionField, createGbc(1, row++));
        
    }
    
    public boolean getArgs(Map<String, String> args){

        boolean checkPassed = true;
        
        //title
        String title = titleField.getText();
        if(title.trim().isEmpty()){
            //ErrorWindow.showErrorWindow("Field \"Title\" is obligatory!");
            checkPassed = false;
        }
        args.put("title", title);
        
        //filename
        String filename = this.filenameField.getText();
        if(filename.trim().isEmpty()){
            //ErrorWindow.showErrorWindow("Field \"File Name\" is obligatory!");
            checkPassed = false;
        }
        args.put("filename", filename);
        
        //author name
        String authorName = this.authorNameField.getText();
        if(authorName.trim().isEmpty()){
            //ErrorWindow.showErrorWindow("Field \"Author\" is obligatory!");
            checkPassed = false;
        }
        args.put("author", authorName);

        //affiliation
        String affiliation = this.affiliationField.getText();
        args.put("affiliation", affiliation);
        
        //descripiton 
        String description = this.descriptionField.getText();
        if(description.trim().isEmpty()){
            //ErrorWindow.showErrorWindow("Field \"Description\" is obligatory!");
            checkPassed = false;
        }
        args.put("description", description);

        //type
        args.put("type", (String)typesCombo.getSelectedItem());
        
        //access right
        args.put("access", (String)accessRight.getSelectedItem());

        return checkPassed;
    }
    
    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        
        gbc.anchor = (x == 0) ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        gbc.fill = (x == 0) ? GridBagConstraints.BOTH
                : GridBagConstraints.HORIZONTAL;
        
        gbc.insets = (x == 0) ? WEST_INSETS : EAST_INSETS;
        gbc.weightx = (x == 0) ? 0.1 : 1.0;
        gbc.weighty = 1.0;
        return gbc;
    }
}
