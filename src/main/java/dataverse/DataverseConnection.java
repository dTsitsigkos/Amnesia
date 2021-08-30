/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataverse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Nikos
 */
public class DataverseConnection {
    private static String boundary;
    private static HttpURLConnection httpConn;
    private static OutputStream outputStream;
    private static final String LINE_FEED = "\r\n";
    private static PrintWriter writer;
    private static String charset = "UTF-8";
    
    public static  List<DataverseFile> getDataverseFiles(String s_url, String token, String persistent_id) throws MalformedURLException, IOException, ParseException{
        List<DataverseFile> files = new ArrayList();
        URL url = new URL(s_url+"/api/datasets/:persistentId/versions?persistentId="+persistent_id);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");
        httpConn.addRequestProperty("X-Dataverse-key",token);
//        outputStream = httpConn.getOutputStream();+

        
        List<String> response = finish(false,null,"get");
        
        System.out.println("SERVER REPLIED:");

        for (String line : response) {
            System.out.println(line);
        }
        
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(response.get(0));
        
        JSONArray jsonData = (JSONArray) jsonResponse.get("data");
        JSONArray jsonfiles = (JSONArray) ((JSONObject) jsonData.get(0)).get("files");
        
        System.out.println("Files "+jsonfiles.toString());
        
        if(jsonfiles.size() > 0){
            for(int i=0; i<jsonfiles.size(); i++){
                JSONObject jsonFile = (JSONObject) ((JSONObject) jsonfiles.get(i)).get("dataFile");
                JSONArray jsonCategories = (JSONArray) ((JSONObject) jsonfiles.get(i)).get("categories");
                files.add(new DataverseFile(jsonFile,jsonCategories));
            }
            return files;
        }
        else{
            return null;
        }
        
    }
    
    public static boolean downloadFile(String s_url, String token, String path,DataverseFile file) throws MalformedURLException, IOException{
        URL url; 
        if(file.getPersistentId() != null && !file.getPersistentId().trim().isEmpty()){
            url = new URL(s_url+"/api/access/datafile/:persistentId/?persistentId="+file.getPersistentId());
        }
        else{
            url = new URL(s_url+"/api/access/datafile/"+file.getDatasetId());
        }
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");
        httpConn.addRequestProperty("X-Dataverse-key",token);
        
        List<String> response = finish(true,path+File.separator+file.getFileName(),"get");
        System.out.println("SERVER REPLIED:");

        for (String line : response) {
            System.out.println(line);
        }
        
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
            return true;
        }
        else{
            return false;
        }
    }
    
    public static boolean uploadFile(String s_url, String token, String persistent_id, String path, String description) throws IOException{
        URL url; 
        url = new URL(s_url+"/api/datasets/:persistentId/add?persistentId="+persistent_id);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data;boundary=" + boundary);
        httpConn.addRequestProperty("X-Dataverse-key",token);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
        
        JSONObject jsondata = new JSONObject();
        JSONArray categories = new JSONArray();
        jsondata.put("description", description);
        jsondata.put("directoryLabel", "");
        
        categories.add("Data");
        jsondata.put("categories",categories);
        jsondata.put("restrict", "false");
        
        
        
        addFormField("jsonData", jsondata.toString(),"text/plain;");
        System.out.println("json data "+jsondata.toString());
        File data = new File(path);
        System.out.println("path "+path);
        addFilePart("file", data);
        
        List<String> response = finish(false,null,"post");
        
        System.out.println("SERVER REPLIED:");

        for (String line : response) {
            System.out.println(line);
        }
        
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
            return true;
        }
        else{
            return false;
        }
        
    }
    
    private static  List<String> finish(boolean getFile, String path, String method) throws IOException { //D:\tests
        List<String> response = new ArrayList<String>();
        
        if(method.toLowerCase().equals("post")){
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
        }
 
        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            if(getFile){
                Files.copy(httpConn.getInputStream(), Paths.get(path),StandardCopyOption.REPLACE_EXISTING);
                response.add("Downloaded file successfully");
            }
            else{
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpConn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
            }
            httpConn.disconnect();
        } else {
            System.out.println("Response "+httpConn.getResponseMessage());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getErrorStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
            System.out.println(response);
            throw new IOException("Server returned non-OK status: " + status);
        }
 
        return response;
    }
    
     private static void addFormField(String name, String value, String contentType) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: "+ contentType+ " charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
 
    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    private static  void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        if(URLConnection.guessContentTypeFromName(fileName)!=null){
            writer.append(
                    "Content-Type: "
                            + URLConnection.guessContentTypeFromName(fileName))
                    .append(LINE_FEED);
        }
        else{
            writer.append(
                    "Content-Type: "
                            + "text/csv")
                    .append(LINE_FEED);
        }
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
 
        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
         
        writer.append(LINE_FEED);
        writer.flush();    
    }
}
