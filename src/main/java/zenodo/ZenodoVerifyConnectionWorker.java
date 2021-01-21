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
public class ZenodoVerifyConnectionWorker extends SwingWorker<Boolean, Void>{
    private final String accessToken;
    private final LoadingFrame loadingFrame;

    public ZenodoVerifyConnectionWorker(String accessToken, LoadingFrame loadingFrame){
        this.accessToken = accessToken;
        this.loadingFrame = loadingFrame;
    }
    
    @Override
    protected Boolean doInBackground() throws Exception {
        return ZenodoConnection.verifyAccessToken(accessToken);
    }
    
    @Override
    public void done(){
        loadingFrame.stopLoading();
    }
}
