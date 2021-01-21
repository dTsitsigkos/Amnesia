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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

/**
 *
 * @author serafeim
 */
public class LoadingFrame extends JFrame{
    JProgressBar progressBar;
    
    public static boolean PROGRESS = false;
    public static boolean CONTINUOUS = true;
    
    public void showLoading(boolean mode){
        
        Container content = this.getContentPane();
        progressBar = new JProgressBar(0, 100);
        if(mode == CONTINUOUS)
            progressBar.setIndeterminate(true);
        
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Loading");
        progressBar.setBorder(border);
        content.add(progressBar, BorderLayout.CENTER);
        this.setSize(300, 90);

        //center align frame to screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        
//        this.setUndecorated(true);
//        this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        this.setVisible(true);
    }
    
    public void stopLoading(){
        this.setVisible(false);
    }
    
    public void setOnTop(){
        setAlwaysOnTop(true);
    }
    
    public void updateText(String textMessage){
        this.progressBar.setString(textMessage);
    }
    
    public void updateValue(int value){
        this.progressBar.setValue(value);
    }
    
   
}
