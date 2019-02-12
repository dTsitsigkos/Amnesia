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

import javax.swing.SwingWorker;


/**
 *
 * @author serafeim
 */
public class ZenodoDownloadFileWorker extends SwingWorker<String, Void>{
    private final ZenodoFile file;
    private final LoadingFrame loadingFrame;
    private final String accessToken;
    private final String baseDownloadPath;

    public ZenodoDownloadFileWorker(ZenodoFile file, String baseDownloadPath, String accessToken, LoadingFrame loadingFrame){
        this.file = file;
        this.loadingFrame = loadingFrame;
        this.accessToken = accessToken;
        this.baseDownloadPath = baseDownloadPath;
    }
    
    @Override
    protected String doInBackground() throws Exception {
        
        String localFile = baseDownloadPath + accessToken + "_" + file.getFileName();
        //ZenodoConnection.downloadFile(file, localFile);
        
        return localFile;
    }
    
    @Override
    public void done(){
        loadingFrame.stopLoading();
    }
}
