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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Class executing Zenodo Rest calls
 * @author serafeim
 */
public class ZenodoConnection {
    static String errorMessage = "";
    
    
    
    /**
     * Verifies access token given
     * @param accessToken access token to be verified
     * @return 
     */
    public static boolean verifyAccessToken(String accessToken){
        String query = "https://zenodo.org/api/deposit/depositions?access_token=" + accessToken;
       System.out.println("verify token = " + query);

        Client client = Client.create();
        
        WebResource webResource = client.resource(query);
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);
        
        if (response.getStatus() != 200) {
            //ErrorWindow.showErrorWindow("Connection to Zenodo failed\n(Verifivation of access token failed!)");
            
            return false;
            
        }
        return true;
    }
    
    /**
     * Lists deposition files
     * @param accessToken
     * @return a map containing an increasing id for key and a file in value
     */
    public static Map<Integer, ZenodoFile> getDepositionFiles(String accessToken){
        Map<Integer, ZenodoFile> files = new HashMap<>();
        String query = "https://zenodo.org/api/deposit/depositions?access_token=" + accessToken;
        System.out.println("query = " + query);
        try {
            
            Client client = Client.create();
            
            System.out.println("hereeerere222222");
            WebResource webResource = client.resource(query);
            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);
            
            System.out.println("hereeerere");
            if (response.getStatus() != 200) {
                errorMessage = ("Connection to Zenodo failed\n(error code: "
                        + response.getStatus() + ")");
                System.out.println("hereeerere1111");
                return null;
                
            }
            
            //parsing response from zenodo
            JSONParser parser = new JSONParser();
            
            
            JSONArray objArray = (JSONArray)parser.parse(response.getEntity(String.class));
            
            int fileNumb = 0;
            
            //parsing files
           // System.out.println("zenodoooooooooooo");
            for(Object obj : objArray){
                JSONObject depositionJson = (JSONObject)obj;
                //System.out.println("new fileeeeeeeeeeeeeeeeeeeeeeeeeee");
                //deposition details
                
                Long id = (Long)depositionJson.get("id");
                //Long fileId = (Long)depositionJson.get("record_id");
                //System.out.println( "record_id = " + record_id);
                
                //bypass Unsubmitted files
                if(id == null)
                    continue;
                
                String query2 = "https://zenodo.org/api/deposit/depositions/" + id + "?access_token=" + accessToken;
                System.out.println("Quwry 2 = " + query2);
                Client client2 = Client.create();
            
                WebResource webResource2 = client2.resource(query2);
                ClientResponse response2 = webResource2.accept("application/json")
                        .get(ClientResponse.class);

                if (response2.getStatus() != 200) {
                    errorMessage = ("Connection to Zenodo failed\n(error code: "
                            + response2.getStatus() + ")");

                    return null;

                }
                //System.out.println("hereeeeee" +response2 );
                
                
                JSONObject depositionJson2 = (JSONObject) parser.parse(response2.getEntity(String.class));

                 //System.out.println("hereeeeee");
                
                /*//parsing response from zenodo
                JSONParser parser = new JSONParser();

                JSONArray objArray = (JSONArray)parser.parse(response.getEntity(String.class));
                */
                
                
                String title = (String)depositionJson2.get("title");
                //System.out.println("title = " + title);
                
                JSONArray filesArray = (JSONArray)depositionJson2.get("files");
               //System.out.println("zenodoooooooooooo22222");
                for(Object fileObj : filesArray){
                    //System.out.println("zenodoooooooooooo33333");
                    JSONObject fileJson = (JSONObject)fileObj;
                    String filename = (String)fileJson.get("filename");
                    //long size = Long.parseLong((String)fileJson.get("filesize"));
                    long size = (long)fileJson.get("filesize");
                    String checksum = (String)fileJson.get("checksum");
                    
                   
                   System.out.println(fileJson.get("links"));
                   JSONObject fileObj2 = (JSONObject)fileJson.get("links");
                   //JSONObject fileJson2 = (JSONObject)fileObj2;
                   String fileId = (String) fileObj2.get("download");
                   //String fileId = (String)fileJson2.get("donwload");
                 
                  //  System.out.println("title = " + title);
                   // System.out.println("filename = " + filename);
                   // System.out.println("filesize = " + size);
                   // System.out.println("checksum = " + checksum);
                   System.out.println("id = " + fileId);
                   
                    
                    JSONObject metadata = (JSONObject)depositionJson2.get("metadata");
                  
                    
                    //System.out.println("zenodoooooooooooo22222");
                    
                    JSONArray keywordsJson = (JSONArray)metadata.get("keywords");
                    String keywords = null;
                    boolean FLAG = false;
                    
                    if ( keywordsJson != null){
                        for ( int i = 0 ; i < keywordsJson.size() ; i++ ){
                            //System.out.println("zenodoooooooooooo3333");
                            if ( FLAG == false){
                                keywords = keywordsJson.get(i).toString();
                                FLAG = true;
                            }
                            else{
                                keywords = keywords + "," + keywordsJson.get(i).toString();
                            }

                        }
                    }
                    
                    if ( keywords == null){
                        keywords = "";
                    }
                    //System.out.println("keywordssssssss= " + keywords );
                    
                    String created =  (String)depositionJson.get("created");
                    String modified = (String)depositionJson.get("modified");
                    
                   
                    
                    ZenodoFile file = new ZenodoFile(filename, title, size, fileId,keywords,created,modified,checksum);
//                    System.out.println(file);
                    files.put(fileNumb, file);
                    fileNumb++;
                    //System.out.println("zenodoooooooooooo222222");
                }
                
               
                
                
                //deposition files details
                /*JSONArray filesArray = (JSONArray)depositionJson.get("files");
                System.out.println("zenodoooooooooooo22222");
                for(Object fileObj : filesArray){
                    System.out.println("zenodoooooooooooo33333");
                    JSONObject fileJson = (JSONObject)fileObj;
                    String filename = (String)fileJson.get("filename");
                    long size = Long.parseLong((String)fileJson.get("filesize"));
                    String checksum = (String)fileJson.get("checksum");
                    
                    
                 
                    JSONObject metadata = (JSONObject)depositionJson.get("metadata");
                  
                    
                    System.out.println("zenodoooooooooooo22222");
                    
                    JSONArray keywordsJson = (JSONArray)metadata.get("keywords");
                    String keywords = null;
                    boolean FLAG = false;
                    for ( int i = 0 ; i < keywordsJson.size() ; i++ ){
                        System.out.println("zenodoooooooooooo3333");
                        if ( FLAG == false){
                            keywords = keywordsJson.get(i).toString();
                            FLAG = true;
                        }
                        else{
                            keywords = keywords + "," + keywordsJson.get(i).toString();
                        }
                        
                    }
                    
                    if ( keywords == null){
                        keywords = "";
                    }
                    System.out.println("keywordssssssss= " + keywords );
                    
                    String created =  (String)depositionJson.get("created");
                    String modified = (String)depositionJson.get("modified");
                    String title = (String)depositionJson.get("title");
                   
                    
                    ZenodoFile file = new ZenodoFile(filename, title, size, recordId,keywords,created,modified,checksum);
//                    System.out.println(file);
                    files.put(fileNumb, file);
                    fileNumb++;
                    System.out.println("zenodoooooooooooo222222");
                }*/
            }
            
            System.out.println("files");
            for (Map.Entry<Integer, ZenodoFile> entry : files.entrySet()) {
                System.out.println(entry.getKey()+" id : "+entry.getValue().recordId + "\t file : " + entry.getValue().getFileName() + "title:" + entry.getValue().getTitle());
            }
            
        } catch (UniformInterfaceException | ClientHandlerException e) {
            errorMessage = "An error occurred connecting of Zenodo";
            return null;
        } catch (ParseException ex) {
            errorMessage = "An error occurred parsing data from Zenodo";
            return null;
        }
        
        
        return files;
    }
    
    
    /*
    
    
    */
    
    
   
    
    public static String getErrorMessage(){
        return errorMessage;
    }
    
    /**
     * Donwloads a file from Zenodo to the filesystem
     * @param file file from Zenodo to download
     * @param filename path to store downloaded file
     * @throws MalformedURLException
     * @throws IOException 
     */ 
        //"https://zenodo.org/api/files/cff3fe69-7f8e-4e94-8d22-f4553e424f11/gsdg?access_token=cSQgGzD08dJ11RMyRzLRhU4hi57LK454T8sovlw6Z2STZrQbzg809wUt6ywtD
     
    public static void downloadFile(ZenodoFile file, final String filename,String accessToken)
            throws MalformedURLException, IOException {
        //String urlString = "https://zenodo.org/api/files/cff3fe69-7f8e-4e94-8d22-f4553e424f11/gsdg?access_token=cSQgGzD08dJ11RMyRzLRhU4hi57LK454T8sovlw6Z2STZrQbzg809wUt6ywt"; //file.getDownloadLink();
        //System.out.println("everythiong oj");
        String urlString = file.getRecordId() + "?access_token=" + accessToken;
        //System.out.println("download file = " + urlString);
        Client client = Client.create();
            
            WebResource webResource = client.resource(urlString);
            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);
            
            if (response.getStatus() != 200) {
                errorMessage = ("Connection to Zenodo failed\n(error code: "
                        + response.getStatus() + ")");
                
                
                
            }
        
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {

            
            in = new BufferedInputStream(response.getEntityInputStream());
            //System.out.println("everythiong oj1111111");
             //System.out.println("filename = " + filename);
            fout = new FileOutputStream(filename);
            
           
            //System.out.println("everythiong oj222222");
            final byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
               // System.out.println("data = " + data.toString());
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }
      
    
    /*public static void downloadFile(ZenodoFile file, final String filename)
            throws MalformedURLException, IOException {
        String urlString = "https://zenodo.org/api/files/cff3fe69-7f8e-4e94-8d22-f4553e424f11/gsdg"; //file.getDownloadLink();
        //System.out.println("everythiong oj");
        
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            URL a ;
            
            in = new BufferedInputStream(new URL(urlString).openStream());
            System.out.println("everythiong oj1111111");
             System.out.println("filename = " + filename);
            fout = new FileOutputStream(filename);
            
           
            System.out.println("everythiong oj222222");
            final byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
                System.out.println("data = " + data.toString());
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }*/
    
    /**
     * Creates a Zenodo deposition
     * @param accessToken 
     * @param title 
     * @param type 
     * @param description 
     * @param author
     * @param affiliation
     * @param access
     * @return newly created deposition's id
     */
    public static Long createDeposition(String accessToken,
            String title, String type, String description,
            String author, String affiliation, String access, String keywords){
        

        
        //create json object with arguments
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("title", title);
        metadataJson.put("upload_type", type);
        metadataJson.put("description", description);
        
        
        JSONObject creator = new JSONObject();
        creator.put("name", author);
        creator.put("affiliation", affiliation);
        JSONArray creators = new JSONArray();
        creators.add(creator);
        
        //JSONObject keywordsObject = new JSONObject();
        //keywordsObject.put("keywords", keywords);
        JSONArray keywordsArray = new JSONArray();
        keywordsArray.add(keywords);
        
        
        metadataJson.put("creators", creators);
        metadataJson.put("access_right", access);
        metadataJson.put("keywords", keywordsArray);
        
        
        
        JSONObject obj = new JSONObject();
        obj.put("metadata", metadataJson);
        
//        System.out.println(obj.toJSONString());
        
        Client client = Client.create();
        
        
        System.out.println("create deposition11111");
        
        //execute post call
        WebResource webResource = client
                .resource("https://zenodo.org/api/deposit/depositions?access_token=" + accessToken);
        
        System.out.println("access token = " + accessToken + "\n\n\n " + obj.toJSONString());
        
        ClientResponse response = webResource.type("application/json")
                .post(ClientResponse.class, obj.toJSONString());
        
        System.out.println("create deposition333");
        
        if (response.getStatus() != 201) {
            System.out.println(errorMessage = ("Connection to Zenodo failed\n(error code: "
                    + response.getStatus() +"\n\n" + response +")"));
            return null;
        }
        
        System.out.println("create deposition5555");
        
        //parse response to get deposition id
        JSONParser parser = new JSONParser();
        JSONObject responseJson;
        try {
            
       System.out.println("create deposition41111");
            responseJson = (JSONObject)parser.parse(response.getEntity(String.class));
        } catch (ParseException ex) {
             System.out.println("create deposition4222222");
            errorMessage = "An error occurred parsing data from Zenodo";
            return null;
        }
        
        
        Long depositionId = (Long)responseJson.get(("id"));
//        System.out.println("Created deposition with id : " + depositionId);
 System.out.println("create deposition4444");
        return depositionId;
    }
    
    /**
     * Adds a new file to a Zenodo deposition
     * @param depositionId
     * @param file
     * @param filename
     * @param accessToken
     * @return true if file added successfully, false otherwise
     */
    public static boolean uploadFileToDeposition(Long depositionId, String file, String filename, String accessToken){
        Client client = Client.create();
        
        System.out.println("uploadddddddddddddd111111");
        
        WebResource resource = client
                .resource("https://zenodo.org/api/deposit/depositions/" + depositionId + "/files?access_token=" + accessToken);
        
        System.out.println("uploadddddddddddd2222222");
        
        final FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        
        formDataMultiPart.field("filename", filename);
        FileDataBodyPart fdp = new  FileDataBodyPart("file",
                new File(file),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        formDataMultiPart.bodyPart(fdp);
        
        ClientResponse response = resource.type(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.TEXT_HTML)
                .post(ClientResponse.class, formDataMultiPart);
        
        if (response.getStatus() != 201 && response.getStatus() != 406 ) {
            errorMessage = "Error uploading file to Zenodo";
            return false;
        }
        
//        System.out.println("File uploaded");
        return true;
    }
    
    /**
     * Publishes a Zenodo deposition
     * @param depositionId
     * @param accessToken
     * @return true if deposition was published successfully, false otherwise
     */
    public static boolean publishDeposition(Long depositionId, String accessToken){
        Client client = Client.create();
        
        WebResource resource = client
                .resource("https://zenodo.org/api/deposit/depositions/" + depositionId + "/actions/publish?access_token=" + accessToken);
        ClientResponse response = resource.post(ClientResponse.class);
        if (response.getStatus() != 202) {
            errorMessage = "Error publishing file to Zenodo";
            return false;
        }
//        System.out.println("deposition published");
        return true;
    }
    
}
