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

/**
 *
 * @author serafeim
 */
public class ZenodoFile {
    String fileName;
    String title;
    String keywords;
    String created;
    String modified;
    String checksum;
    String percentage ;
    long filesize;
    String recordId;
    
    public ZenodoFile(String _fileName, String _title, long _size, String _recordId, String _keywords, String _created, String _modified, String _checksum){
        this.fileName = _fileName;
        this.title = _title;
        this.filesize = _size;
        this.recordId = _recordId;
        this.keywords = _keywords;
        this.created = _created;
        this.modified = _modified;
        this.checksum = _checksum;
        this.percentage = null;
    }
    
    public String getDownloadLink(){
        return "https://zenodo.org/record/" + this.recordId + "/files/" + this.fileName;
    }
    
    @Override
    public String toString() {
        return fileName + " (" + title + ")";
    }
    
    public String toTooltip(){
        return "<html>size: " + round(filesize / 1000000.0, 2)  + " MB</html>";
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    //////////////////////////////////////////get///////////////////////////////
    public String getFileName() {
        return fileName;
    }

    public String getTitle() {
        return title;
    }

    public long getFilesize() {
        return filesize;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getCreated() {
        return created;
    }

    public String getModified() {
        return modified;
    }


    public String getPercentage() {
        return percentage;
    }

    public String getChecksum() {
        return checksum;
    }
    
    
    
    /////////////////////////////set////////////////////////////////////////////

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
    
    
    
}
