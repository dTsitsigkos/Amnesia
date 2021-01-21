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

import java.util.Map;
import javax.swing.SwingWorker;


/**
 *
 * @author serafeim
 */
public class ZenodoUploadFileWorker extends SwingWorker<Void, Void>{
    Map<String, String> args;
    String accessToken;
    String file;
    LoadingFrame loadingFrame;
    String errorMessage = null;
    
    public ZenodoUploadFileWorker(Map<String, String> args, String file, String accessToken, LoadingFrame loadingFrame){
        this.args = args;
        this.accessToken = accessToken;
        this.file = file;
        this.loadingFrame = loadingFrame;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        
        //crete deposition
        /*Long depositionId = ZenodoConnection.createDeposition(accessToken, 
                args.get("title"), 
                args.get("type"), 
                args.get("description"), 
                args.get("author"), 
                args.get("affiliation"), 
                args.get("access"));
        if(depositionId == null){
            errorMessage = ZenodoConnection.getErrorMessage();
            return null;
        }
        
        //upload file to deposition
        if(!ZenodoConnection.uploadFileToDeposition(depositionId, 
                file, 
                args.get("filename"), 
                accessToken)){
            errorMessage = ZenodoConnection.getErrorMessage();
            return null;
        }
        
        //publish deposition
        if(!ZenodoConnection.publishDeposition(depositionId, accessToken)){
            errorMessage = ZenodoConnection.getErrorMessage();
            return null;
        }*/
        
        return null;
    }
    
    @Override
    public void done(){
        loadingFrame.stopLoading();
         if (errorMessage != null){
            //ErrorWindow.showErrorWindow(ZenodoConnection.getErrorMessage());
        }
    }
}
