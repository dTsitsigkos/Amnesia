/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataverse;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Nikos
 */
public class DataverseFile {
    String fileName;
    String description;
    String filesize;
    String datasetId;
    String persistentId;
    String categories;
    String created;
    String type;
    
    public DataverseFile(JSONObject jsonfile, JSONArray jsonCategories){
        this.filesize = humanReadableByteCountSI((long)jsonfile.get("filesize"));
        this.fileName = (String) jsonfile.get("filename");
        this.description = (String) jsonfile.get("description");
        this.persistentId = (String) jsonfile.get("persistentId");
        this.datasetId = ""+(long) jsonfile.get("id");
        this.created = (String) jsonfile.get("creationDate");
        this.type = (String) jsonfile.get("contentType");
        categories = "";
        if(jsonCategories!=null){
            for(int i=0; i<jsonCategories.size(); i++){
                if(i!=jsonCategories.size()-1){
                    this.categories += jsonCategories.get(i)+", ";
                }
                else{
                    this.categories += jsonCategories.get(i);
                }
            }
        }
        
    }
    
    private static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
    
    public String getFileName() {
        return fileName;
    }

    public String getDescription() {
        return description;
    }
    
    public String getCategories(){
        return categories;
    }

    public String getFilesize() {
        return filesize;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getType() {
        return type;
    }

    public String getCreated() {
        return created;
    }
    
    public String getPersistentId(){
        return  persistentId;
    }
}
