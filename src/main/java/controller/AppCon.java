package controller;


import algorithms.Algorithm;
import algorithms.clusterbased.ClusterBasedAlgorithm;
import algorithms.demographics.DemographicAlgorithm;
import algorithms.differentialprivacy.DifferentialPrivacyAlgorithm;
import algorithms.flash.Flash;
import algorithms.flash.GridNode;
import algorithms.kmanonymity.Apriori;
import algorithms.mixedkmanonymity.MixedApriori;
import algorithms.parallelflash.ParallelFlash;
import anonymizationrules.AnonymizationRules;
import anonymizeddataset.AnonymizedDataset;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import static controller.AppCon.os;
import static controller.AppCon.rootPath;
import data.CheckDatasetForKAnomymous;
import data.DICOMData;
import data.Data;
import data.DiskData;
import data.MyPair;
import data.RelSetData;
import data.SETData;
import data.TXTData;
import data.XMLData;
import dataverse.DataverseConnection;
import dataverse.DataverseFile;
import dictionary.DictionaryString;
import exceptions.DateParseException;
import exceptions.LimitException;
import exceptions.NotFoundValueException;
import graph.DatasetsExistence;
import graph.Edge;
import graph.Node;
import graph.Graph;
import hierarchy.DemographicInfo;
import hierarchy.HierToJson;
import hierarchy.Hierarchy;
import hierarchy.distinct.AutoHierarchyImplDate;
import hierarchy.distinct.AutoHierarchyImplDouble;
import hierarchy.distinct.AutoHierarchyImplMaskString;
import hierarchy.distinct.AutoHierarchyImplString;
import hierarchy.distinct.HierarchyImplDemographicZipCode;
import hierarchy.distinct.HierarchyImplDouble;
import hierarchy.distinct.HierarchyImplString;
import hierarchy.ranges.AutoHierarchyImplRangesDate;
import hierarchy.ranges.AutoHierarchyImplRangesNumbers;
import hierarchy.ranges.AutoHierarchyImplRangesNumbers2;
import hierarchy.ranges.HierarchyImplRangeDemographicAge;
import hierarchy.ranges.HierarchyImplRangesDate;
import hierarchy.ranges.HierarchyImplRangesNumbers;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.management.MemoryUsage;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import jsoninterface.View;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import solutions.FindSolutions;
import solutions.SolutionHeader;
import solutions.SolutionStatistics;
import solutions.SolutionStatistics.SolutionAnonValues;
import solutions.Solutions;
import solutions.SolutionsArrayList;
import statistics.ColumnsNamesAndTypes;
import statistics.HierarchiesAndLevels;
import statistics.Queries;
import statistics.Results;
import statistics.ResultsToJson;
import zenodo.LoadFromZenodoPanel;
import zenodo.ZenodoConnection;
import zenodo.ZenodoFile;
import zenodo.ZenodoFilesToJson;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jimakos
 */

/*session variables


*/

@SpringBootApplication
public class AppCon extends SpringBootServletInitializer {
    private static Class<AppCon> applicationClass = AppCon.class;
    public static String os = "linux";
    public static String rootPath = System.getProperty("catalina.home");
    public static String parentDir; 

    public static void main(String[] args) throws URISyntaxException {
        SpringApplication.run(applicationClass, args);
        ApplicationHome home = new ApplicationHome(AppCon.class);
        parentDir = home.getDir().getPath();    // returns the folder where the jar is. This is what I wanted.
        home.getSource(); // returns the jar absolute path.
        System.out.println("PAth "+home.getDir().getPath());

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }
}

@Configuration
class WebConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${cors.mapping}")
    private String mapping;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {        
        registry.addMapping(mapping)
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","));
    }
}

@RestController
//@RequestMapping("/greeting")
class AppController {
    private static String os = AppCon.os;
    
    private static String rootPath = AppCon.rootPath;

    
    /*
    @RequestMapping(value = "/")
    public String welcome(HttpSession session) {
        return "/index.html";
    }
    */
    
    /*@RequestMapping(value = "/action/getsessionid")
    public String getSessionId(HttpSession session) {
        return session.getId();
    }*/
    
    /*@RequestMapping(value = "/action/hello")
    public String hello(HttpSession session) {
        return "xaxaxaxa";
    }*/
    
    //@RequestMapping(value="/hello")
    /*@RequestMapping(value="/action/greeting")//http://localhost:8084/mavenproject1/greeting?name=fd
    public @ResponseBody Test greeting(@RequestParam(value="name", defaultValue="World") String name) {
        Test t = new Test();
        t.setStr(name);
        return t;
    }*/
    
    
    // ean to kanw kai gia hierarchies tha prepei na allaksw ta paths
    @RequestMapping(value = "/action/uploadf", method = RequestMethod.POST)
    public  @ResponseBody ErrorMessage uploadf(MultipartHttpServletRequest request, HttpSession session){
        try {
            ErrorMessage errMes = new ErrorMessage();
            String uploadedFile = null;
            MultipartFile file = null;
            String mimeType = null;
            String filename = null;
            byte[] bytes = null;
            File dir = null,dirErr = null;
            String input = (String)session.getAttribute("inputpath");
            if(os.equals("online")){
//            if (input == null){

//                String rootPath = System.getProperty("catalina.home");
                //String rootPath = "/usr/local/apache-tomcat-8.0.15";
//                String rootPath = "/var/lib/tomcat8";
                dir = new File(rootPath + File.separator + "amnesia"+ File.separator + session.getId());  
                if (!dir.exists()){
                    dir.mkdirs();
                }
                
//                dirErr = new File(rootPath + File.separator+ "errorLog");
//                if(!dirErr.exists()){
//                    dirErr.mkdirs();
//                }
                
    //            }
    //            else{
    //              
    //                dir = new File(input);
    //            }
            }
            else{
               if (input == null){
                    File f;
                    File dir1;
                    String rootPath;

                    if(this.os.equals("linux")){
                        f = new File(System.getProperty("java.class.path"));//linux
                        dir1 = f.getAbsoluteFile().getParentFile();
                        rootPath = dir1.toString();
                    }
                    else{
                        rootPath = System.getProperty("user.home");//windows
                    }

    //               String rootPath = System.getProperty("user.home");//windows

                    ////////////////////linux///////////////////////////////////////
    //                File f = new File(System.getProperty("java.class.path"));//linux
    //                File dir1 = f.getAbsoluteFile().getParentFile();
    //                String rootPath = dir1.toString();
                    //////////////////////////////////////////////////////////////
                    dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
                    if (!dir.exists()){
                        dir.mkdirs();
                    }
                }
                else{

                    dir = new File(input);
                } 
            }
            
                    
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                uploadedFile = itr.next();
                file = request.getFile(uploadedFile);
                mimeType = file.getContentType();
                filename = file.getOriginalFilename();
//                bytes = file.getBytes();
                

//                System.out.println("uploadedFile = " + uploadedFile +"\tfile =" + file +"\t mimeType =" + mimeType + "\tilename = "+ filename +"\tbytes = " + bytes);

            }
            
            session.setAttribute("inputpath",dir.toString());
            session.setAttribute("filename",filename);
                    
            try{ // Create the file on server
                File serverFile = new File(dir.getAbsolutePath()
                                + File.separator + filename);
//                BufferedOutputStream stream = new BufferedOutputStream(
//                                new FileOutputStream(serverFile));
//                stream.write(bytes);
//                stream.close();
                
                InputStream fis = file.getInputStream();
                DataOutputStream dout = new DataOutputStream(new FileOutputStream(serverFile));
                byte[] buffer = new byte[1024*1000];
                
                int b;
                while ((b = fis.read(buffer)) >= 0) {
                    dout.write(buffer,0,b);
                }

                //logger.info("Server File Location="
                //		+ serverFile.getAbsolutePath());

                //return "You successfully uploaded file=" + file.getOriginalFilename();
                errMes.setSuccess(true);
                errMes.setProblem("You successfully uploaded file=" + filename);

                return  errMes;
            } catch (Exception e) {
                    errMes.setSuccess(false);
                    errMes.setProblem("You failed to upload " + file.getOriginalFilename() + " => " + e.getMessage());
                    return  errMes;
            }
                    
                    
        }
        catch (Exception e) {
            System.out.println("problem");
        }
        return null;
    }
    
    
    @RequestMapping(value = "/action/errorhandle", method = RequestMethod.POST)
    public @ResponseBody void errorHandling(@RequestParam("error") String error, HttpSession session) throws IOException{
        if(os.equals("online")){
            System.out.println("Error handling");
//            String rootPath = System.getProperty("catalina.home");
//            String rootPath = "/var/lib/tomcat8";
            File dir = new File(rootPath+File.separator+"amnesia"+File.separator+"errorLog");
            if(!dir.exists()){
                dir.mkdirs();
            }
            File errorFile = new File(dir.getAbsolutePath()+File.separator+"error_"+session.getId()+".txt");
//            if(!errorFile.exists()){
//                errorFile.createNewFile();
//            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorFile.getAbsolutePath(), true), StandardCharsets.UTF_8));
            out.write(error+"\n\n\n\n\n");
            out.close();
        }
    }
    
    
    
    /**
     *
     * @param file
     * @param path
     * @param data
     * @param session
     * @return
     */
    @RequestMapping(value = "/action/upload", method = RequestMethod.POST)
    public @ResponseBody ErrorMessage upload(@RequestParam("file") MultipartFile file,@RequestParam("data") boolean data , HttpSession session) throws Exception{
        
        ErrorMessage errMes = new ErrorMessage();
        String message = "memory problem";
        //boolean fileCreate = false;
        boolean problem = false;
        
        /*
        // Get name of uploaded file.
        String fileName = file.getOriginalFilename();

        System.out.println("filename = " + fileName);
        
        // Path where the uploaded file will be stored.
        String path = "/media/disk/mavenproject1/src/main/webapp/inputs/" + fileName;

        // This buffer will store the data read from 'uploadedFileRef'
        byte[] buffer = new byte[1000];

        // Now create the output file on the server.
        File outputFile = new File(path);

        FileInputStream reader = null;
        FileOutputStream writer = null;
        int totalBytes = 0;
        try {
            fileCreate = outputFile.createNewFile();
            if (fileCreate == false){
                errMes.setSuccess(false);
                errMes.setProblem("file name exists. Change the name of the file.");
                problem = true;
            }           
            else{
                // Create the input stream to uploaded file to read data from it.
                reader = (FileInputStream) file.getInputStream();

                // Create writer for 'outputFile' to write data read from
                // 'uploadedFileRef'
                writer = new FileOutputStream(outputFile);

                // Iteratively read data from 'uploadedFileRef' and write to
                // 'outputFile';            
                int bytesRead = 0;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    writer.write(buffer);
                    totalBytes += bytesRead;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (reader != null){
                    reader.close();
                }
                if (writer != null){
                    writer.close();
                }
              
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if ( problem != true ){
            errMes.setSuccess(true);
            errMes.setProblem(null);
        }*/
        
//        DecimalFormat decFormat = new DecimalFormat();
//        DecimalFormatSymbols decSymbols = decFormat.getDecimalFormatSymbols();           
//        System.out.println("Decimal separator is : " + decSymbols.getDecimalSeparator());
//        System.out.println("Thousands separator is : " + decSymbols.getGroupingSeparator());
        
        if (!file.isEmpty()) {
            try {
                // Creating the directory to store file
                File dir = null,dirErr = null;
                String input = (String)session.getAttribute("inputpath");
//                System.out.println(" i am here");
//                System.out.println("session id  = " + session.getId());
//                System.out.println("input = " + input);
                if(os.equals("online")){
//                if (input == null){
                    //Desktop
//                    File f = new File(System.getProperty("java.class.path"));
//                    File dir1 = f.getAbsoluteFile().getParentFile();
//                    String rootPath = dir1.toString();
//                    dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
//                    if (!dir.exists()){
//                        dir.mkdirs();
//                    }
                    
//                    String rootPath = "/usr/local/apache-tomcat-8.0.15";
                    //System.out.println("session id  = " + session.getId());
//                    String rootPath = System.getProperty("catalina.home");
//                    String rootPath = "/var/lib/tomcat8";
                    dir = new File(rootPath + File.separator + "amnesia"+ File.separator + session.getId());  
                    
//                    System.out.println("dir name = " + dir.getAbsolutePath() + "\t root path = " + rootPath);
                    if (!dir.exists()){
                        dir.mkdirs();
                    }
                    
//                    dirErr = new File(rootPath + File.separator+ "errorLog");
//                    if(!dirErr.exists()){
//                        dirErr.mkdirs();
//                    }

                    if (data == true){
                        session.setAttribute("inputpath",dir.toString());
                        session.setAttribute("filename", file.getOriginalFilename());
                    }
                    else{
                        session.setAttribute("inputpath",dir.toString());
                    }
                }
                else{
                    if (input == null){
                    
                        File f ;
                        File dir1 ;
                        String rootPath;

                        if(this.os.equals("linux")){
                            f = new File(System.getProperty("java.class.path"));//linux
                            dir1 = f.getAbsoluteFile().getParentFile();
                            rootPath = dir1.toString();
                        }
                        else{
                            rootPath = System.getProperty("user.home");//windows
                        }

    //                    String rootPath = System.getProperty("user.home");//windows

                        ////////////////////linux///////////////////////////////////////
    //                    File f = new File(System.getProperty("java.class.path"));//linux
    //                    File dir1 = f.getAbsoluteFile().getParentFile();
    //                    String rootPath = dir1.toString();
                        //////////////////////////////////////////////////////////////
                        
                        dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
                        if (!dir.exists()){
                            dir.mkdirs();
                        }
                        else{
                            String filename = file.getOriginalFilename().replace("\\", "/");
                            session.setAttribute("inputpath",dir.toString());
                            if(file.getOriginalFilename().contains("/")){
                                System.out.println("Delete all");
                                this.deleteFiles(session);
                            }
                            dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
                            if (!dir.exists()){
                                dir.mkdirs();
                            }
                        }

                        if (data == true){
                            session.setAttribute("inputpath",dir.toString());
                            String filename = file.getOriginalFilename().replace("\\", "/");
                            if(file.getOriginalFilename().contains("/")){
                                session.setAttribute("filename", file.getOriginalFilename().split("/")[1]);
                            }
                            else{
                                session.setAttribute("filename", file.getOriginalFilename());
                            }
                        }
                        else{
                            session.setAttribute("inputpath",dir.toString());
                        }
                    }
                    else{
                        
                        if (data == true){
                            String filename = file.getOriginalFilename().replace("\\", "/");
                            if(file.getOriginalFilename().contains("/")){
                                session.setAttribute("filename", file.getOriginalFilename().split("/")[1]);
                            }
                            else{
                                session.setAttribute("filename", file.getOriginalFilename());
                            }
                            session.setAttribute("filename", file.getOriginalFilename());
                        }
                        dir = new File(input);
                    }   
                }
//                }
//                else{
//                    if (data == true){
//                        session.setAttribute("filename", file.getOriginalFilename());
//                    }
//                    dir = new File(input);
//                }
                
                
                /*String input = (String)session.getAttribute("inputpath");
                if (input == null){
                    session.setAttribute("inputpath",dir.toString());
                }*/
                    
//                byte[] bytes = file.getBytes();

                // Create the file on server
                File serverFile;
//                String pattern = Pattern.quote(System.getProperty("java.io.tmpdir"));
                String filename = file.getOriginalFilename().replace("\\", "/");
                if(filename.contains("/")){
                    
                    serverFile = new File(dir.getAbsolutePath()
                                + File.separator + file.getOriginalFilename().split("/")[1]);
                   
                }
                else{
                    
                    serverFile = new File(dir.getAbsolutePath()
                                + File.separator + file.getOriginalFilename());
                }
                 
//                BufferedOutputStream stream = new BufferedOutputStream(
//                                new FileOutputStream(serverFile));
//                stream.write(bytes);
//                stream.close();
                
                InputStream fis = file.getInputStream();
                DataOutputStream dout = new DataOutputStream(new FileOutputStream(serverFile));
                byte[] buffer = new byte[1024*1000];
                
                int b;
                while ((b = fis.read(buffer)) >= 0) {
                    dout.write(buffer,0,b);
                }
                
                dout.close();
                fis.close();
                //logger.info("Server File Location="
                //		+ serverFile.getAbsolutePath());

                //return "You successfully uploaded file=" + file.getOriginalFilename();
                errMes.setSuccess(true);
                errMes.setProblem("You successfully uploaded file=" + file.getOriginalFilename());

                return  errMes;
            }catch (OutOfMemoryError e) {
                
                errMes.setSuccess(false);
                errMes.setProblem("Memory problem");
                return errMes;
            }
            catch (Exception e) {
                    //return "You failed to upload " + file.getOriginalFilename() + " => " + e.getMessage();
                    errMes.setSuccess(false);
                    errMes.setProblem("You failed to upload " + file.getOriginalFilename() + " => " + e.getMessage());
                    return  errMes;
            }
        } else {
                //return "You failed to upload " + file.getOriginalFilename()
                        //	+ " because the file was empty.";
                errMes.setSuccess(false);
                errMes.setProblem("You failed to upload " + file.getOriginalFilename() +"because the file was empty.");
                problem = true;     
                return  errMes;
        }
        
        
        //return errMes;
    }
    
    @RequestMapping(value="/action/getdatatype", method = RequestMethod.POST)
    public @ResponseBody String getDataType(HttpSession session){
        Data data = (Data)session.getAttribute("data");
        
        if(data == null){
            return "";
        }
        else if(data instanceof DICOMData || data instanceof TXTData || data instanceof DiskData){
            return "tabular";
        }
        else{
            return "other";
        }
        
    }

    @JsonView(View.SmallDataSet.class)
    @RequestMapping(value="/action/getsmalldataset", method = RequestMethod.POST)//, method = RequestMethod.POST)
    public @ResponseBody Data getSmallDataSet ( @RequestParam("del") String del, @RequestParam("datatype") String datatype, @RequestParam("delset") String delset ,HttpSession session) throws FileNotFoundException, IOException, LimitException, DateParseException,NotFoundValueException {

        Data data = null;
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String delimeter = null;
        String result = null;
        Map<String,Hierarchy> hierarchies = null;
        DictionaryString dict = null;
        
        String rootPath = (String)session.getAttribute("inputpath");
        String filename = (String)session.getAttribute("filename");
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        

	File dir = new File(rootPath);

        
        String fullPath = dir + File.separator + filename;
        
        dict = HierarchyImplString.getWholeDictionary();
        if(dict == null){
            System.out.println("Whole Dictionary is null");
            dict = new DictionaryString();
        }
//        if(hierarchies!=null){
//            for(Map.Entry<String,Hierarchy> entry : hierarchies.entrySet()){
//                Hierarchy h = entry.getValue();
//                if(h instanceof HierarchyImplString){
//                    dict = h.getDictionary();
//                    break;
//                }
//                
//            }
//        }
        
        if(datatype.equals("dicomfile")){
            data = new DICOMData(rootPath,dict);
            result = data.findColumnTypes();
            
            if(result.equals("1")){
                return data;
            }
            
            String[][] smallDataset = data.getSmallDataSet();
            data.getTypesOfVariables(smallDataset);
        }
        else if (datatype.equals("tabular")){

            if ( del == null ){
                delimeter = ",";
            }
            else{
                delimeter = del;
            }
            
            System.out.println("del "+del);

            fstream = new FileInputStream(fullPath);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            
            if(!fullPath.toLowerCase().endsWith(".xml")){
                while ((strLine = br.readLine()) != null){
                    if ( strLine.contains(delimeter)){
                        data = new TXTData(fullPath,delimeter,dict);
                    }
                    else{
                        if ((strLine = br.readLine()) != null){
                            if ( strLine.contains(delimeter)){
                                //data = new SETData(fullPath,delimeter);
                            }
                            else{
                                data = new TXTData(fullPath,delimeter,dict);
                            }
                        }
                    }
                    result = data.findColumnTypes();
                    break;

                }

                br.close();
            }
            else{
                data = new XMLData(fullPath,dict);
                result = data.findColumnTypes();
            }
            //data.findColumnTypes();
            
            if ( result == null){
                return null;
            }
            else if (result.equals("1")){
                return data;
            }
        
            String [][] smallDataset = data.getSmallDataSet();

            data.getTypesOfVariables(smallDataset);
        
        }
        else if (datatype.equals("set")){
            data = new SETData(fullPath,del,dict);
            data.readDataset(null,null);
            String[][] small = data.getSmallDataSet(); 
        }
        else if(datatype.equals("RelSet")){
            data = new RelSetData(fullPath,del,delset,dict);
            
            result = data.findColumnTypes();
            
            String [][] smallDataset = data.getSmallDataSet();
            
            data.getTypesOfVariables(smallDataset);
        }
        else if( datatype.equals("Disk")){
            data = new DiskData(fullPath,del,dict);
            
            result = data.findColumnTypes();
            
            if(result.equals("1")){
                return data;
            }
            
            String[][] smallDataset = data.getSmallDataSet();
            data.getTypesOfVariables(smallDataset);
        }
        
        
        session.setAttribute("data", data);

        return data;
    }
    
    
    @RequestMapping(value="/action/deletefiles", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void deleteFiles (HttpSession session) throws FileNotFoundException, IOException {
        
        String inputPath = (String) session.getAttribute("inputpath");
        if(inputPath == null){
            return;
        }
        String filename = (String)session.getAttribute("filename");
        long daysBack = 1;
        long purgeTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000);
//        System.out.println("inputPAth delete "+inputPath);
        int index=inputPath.lastIndexOf('/');
        if(index==-1){
            index = inputPath.lastIndexOf('\\');
        }
        
        
//        File dir = new File(inputPath);
        
        
        
        File  dir = new File(inputPath.substring(0,index));
        
//        File dir2 = new File(inputPath);
//        System.out.println("Dir1 "+dir+" dir2 "+dir2);
//        FileUtils.cleanDirectory(dir2);
        for (File file: dir.listFiles()) {
            System.out.println("Filename to delette "+file.getName()+" session "+session.toString());
            long diff = new Date().getTime() - file.lastModified();
            System.out.println("diff "+diff+" thresehold "+(24 * 60 * 60 * 1000)+ " last mod "+file.lastModified()+" date "+new Date(file.lastModified()).getDay());
            if(file.getName().equals(session.getId())){
                boolean deleteDir = true;
//                FileUtils.forceDelete(file);
                for(File sessionFile : file.listFiles()){
//                    System.out.println("Directory session file "+sessionFile.getName());
                    if(!sessionFile.getName().endsWith(".xml") && !sessionFile.getName().endsWith(".db")){
                        
                        sessionFile.delete();
                    }
                    else{
                        deleteDir = false;
                    }
                }
                if(deleteDir){
                    try{
                        FileUtils.forceDelete(file);
                    }catch(Exception e){
                        this.errorHandling(e.getMessage(), session);
                    }
                }
            }
            else if(!file.getName().equals("errorLog") && !file.getName().equals("ObjectFiles") && diff > 2 * 60 * 60 * 1000){
                FileUtils.forceDelete(file);
            }
        }
        
        
        
//        if(filename!=null && !filename.endsWith("xml")){
//            System.out.println("Delete dir "+dir);
//            FileUtils.cleanDirectory(dir);
//        }
//        else{
//            for (File file: dir.listFiles()) {
//                System.out.println("Filename to delette "+file.getName()+" session "+session.toString());
//                if(!file.getName().equals(session.getId()))
//                    FileUtils.forceDelete(file);
//            }
//        }
        
        
//        FileUtils.cleanDirectory(dir);
//        for (File file: dir.listFiles()) {
//            if(filename!=null && !filename.endsWith("xml")) {
//                file.delete();
//            }
//            else if(!file.getName().equals(session.toString())){
//                file.delete();
//            }
//
//        }
    }
    
     @RequestMapping(value="/action/createinputpath", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void createInputPath (@RequestParam("path") String path, HttpSession session) throws FileNotFoundException, IOException {
        File tempFile = new File(path);
        if(!tempFile.exists()){
            tempFile.mkdirs();
        }
    }
    
    @RequestMapping(value="/action/getexampledataset", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String[] getExampleDataSet ( HttpSession session) throws FileNotFoundException, IOException {
        String[] exampleDataSet = new String[4];
        
        

        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String rootPath = (String)session.getAttribute("inputpath");
        String filename = (String)session.getAttribute("filename");
        int counter = 0;
        
        System.out.println("inputpath: "+ session.getAttribute("inputpath"));
        System.out.println("filename: "+ session.getAttribute("filename"));

        

        if(filename != null && filename.endsWith(".dcm")){
            return new String[] {"DICOM"};
        }
        else{

            File dir = new File(rootPath);

            String fullPath = dir + File.separator + filename; 
            fstream = new FileInputStream(fullPath);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));

            while ((strLine = br.readLine()) != null){
                if ( counter < 4){
                    exampleDataSet[counter] = strLine;
                    counter++;
                }
                else{
                    break;
                }
            }

            br.close();
        }


            
        
         
        return exampleDataSet;
        
    }
    
    
    
    @JsonView(View.DataSet.class)
    @RequestMapping(value="/action/getdataset", method = RequestMethod.POST)//, method = RequestMethod.POST)
    public @ResponseBody Data getDataSet (@RequestParam("start") int start , @RequestParam("length") int length , @RequestParam(value = "onlyStrings",required = false,defaultValue = "false") boolean onlyStrings, HttpSession session) throws FileNotFoundException, IOException {
        

        Data data = (Data) session.getAttribute("data");
        if (data == null){
            System.out.println("data is null");
        }
        else{
            
            data.getPage(start, length);
            //this.getAnonDataSet (start ,length,session);
        }
        
        /*DataToJson[] json = null;
        //for( int i = 0 ; i < data.getColumns().length ; i ++ ){
            json = data.get;
            if ( json != null){
                for ( int i = 0 ; i < json.length; i ++){
                    System.out.println("malakia : " + json[i].getData());
                }
            }
            else{
                System.out.println("kenoooooooooooooo");
            }*/
        //}
        
        
        return data;
        
    }
    
    
    
    @JsonView(View.GetColumnNames.class)
    @RequestMapping(value="/action/getcolumnnames", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody Data getColumnNames ( HttpSession session ) throws FileNotFoundException, IOException {
        
        Data data = (Data) session.getAttribute("data");
        if (data == null){
            System.out.println("data is null col");
        }
        

        
        return data;
        
    }
    
    
    
    @RequestMapping(value="/action/loaddataset", method = RequestMethod.POST)
    public @ResponseBody String loadDataset (@RequestParam("vartypes") String [] vartypes, @RequestParam("checkColumns") boolean [] checkColumns, HttpSession session) throws IOException, LimitException, DateParseException,NotFoundValueException   {
        String result = null;
        
        
        Data data = (Data) session.getAttribute("data");
        
        if (vartypes != null){
            
            result = data.readDataset(vartypes,checkColumns);
        }
        
        if(os.equals("online")){
            this.deleteFiles(session);
        }
        return result;
    }
    
    @RequestMapping(value="/action/saveregex", method = RequestMethod.POST)
    public @ResponseBody void saveRegex (@RequestParam("column") int column, @RequestParam("char") String character, @RequestParam("regex") String regex, HttpSession session)  {
        
        
        Data data = (Data) session.getAttribute("data");
        
        data.setRegex(column, character.charAt(0), regex.trim());
    }
    
    
    @RequestMapping(value="/action/savemask", method = RequestMethod.POST)
    public @ResponseBody void saveMask (@RequestParam("column") int column, @RequestParam("positions") String positions, @RequestParam("char") String character, @RequestParam("option") String option, HttpSession session)  {
        int[] pos_arr = Arrays.stream(positions.substring(1, positions.length()-1).split(","))
            .map(String::trim).mapToInt(Integer::parseInt).toArray();
        
        Data data = (Data) session.getAttribute("data");
        
        data.setMask(column, pos_arr, character.charAt(0), option);
    }
    
    @RequestMapping(value="/action/saveselectedhier", method = RequestMethod.POST)
    public @ResponseBody void saveSelectedHier (@RequestParam("hiername") String hiername, HttpSession session)  {
        session.setAttribute("selectedhier", hiername);
        
    }
    
    
    @RequestMapping(value="/action/getselectedhier", method = RequestMethod.POST)
    public @ResponseBody String getSelectedHier ( HttpSession session)  {
        
        String hiername = null;
        hiername =(String)session.getAttribute("selectedhier");
        
        return hiername;
    }
    
    @RequestMapping(value="/action/removehierarchy", method = RequestMethod.POST)
    public @ResponseBody String removeHierarchy ( HttpSession session)  {
        
        String hiername = null;
        hiername =(String)session.getAttribute("selectedhier");
        Map<String,Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        if(!hierarchies.containsKey(hiername)){
            return "no";
        }
        if(hierarchies.get(hiername).getHierarchyType().contains("demographic")){
            hierarchies.get(hiername).clear();
        }
        hierarchies.remove(hiername);
        if(hierarchies.size()>0){
            session.setAttribute("selectedhier", hierarchies.entrySet().iterator().next().getKey());
        }
        else{
            session.setAttribute("selectedhier", "");
        }
        
        return "OK";
    }
    
    @JsonView(View.Demographic.class)
    @RequestMapping(value="/action/getdemographicinfo", method = RequestMethod.POST)
    public  @ResponseBody Map<String,ArrayList<String>> getDemographicInfo(HttpSession session){
        Map<String,ArrayList<String>> attributeCountries = new HashMap();
        attributeCountries.put("Age", HierarchyImplRangeDemographicAge.getCountries());
        attributeCountries.put("ZipCode", HierarchyImplDemographicZipCode.getCountries());
        return attributeCountries;
    }
    
    
    @RequestMapping(value="/action/generatedemographichierarchy", method = RequestMethod.POST)
    public @ResponseBody String generateDemographicHierarchy(@RequestParam("hier") String hier,@RequestParam("country") String country, @RequestParam("nodeType") String nodeType, HttpSession session)throws LimitException, IOException{
        Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        if ( hierarchies == null ){
            hierarchies = new HashMap<>();
            session.setAttribute("hierarchies", hierarchies);
        }
        
        Hierarchy h;
        
        if(hier.equals("age")){
            h = new HierarchyImplRangeDemographicAge("demographic_age_"+country,nodeType, country);
        }
        else{
            Data data = (Data) session.getAttribute("data");
            h = new HierarchyImplDemographicZipCode("demographic_zip_"+country,"string", country,data.getDictionary());
        }
        h.load();
        session.setAttribute("selectedhier", h.getName());
        hierarchies.put(h.getName(), h);

        if(os.equals("online")){
            this.deleteFiles(session);
        }

        return "OK";
    }
    
//    @JsonView(View.Hier.class)
    @RequestMapping(value="/action/loadhierarchy", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loadHierarcy (@RequestParam("filename") String filename, HttpSession session) throws IOException, LimitException  {
//        try{
            Map<String, Hierarchy> hierarchies  = null;
            hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");

            String filenamex = (String)session.getAttribute("filename");


            if ( hierarchies == null ){
                hierarchies = new HashMap<>();
                session.setAttribute("hierarchies", hierarchies);
//                throw new Exception("Kati kati kati");
            }

            

            String rootPath = (String)session.getAttribute("inputpath");
            this.createInputPath(rootPath, session);
            File dir = new File(rootPath);


            String fullPath = dir + File.separator + filename;


            Hierarchy h = null;

            //read metadata of file to determine which type of hierarchy to use
            List<String> results = findHierarchyType(fullPath);


    //        System.out.println(results);
            if(results.size() != 2){
                //error
                //ErrorWindow.showErrorWindow("Error reading metadata in hierarchy file");

            }

            boolean distinct = false;
            String type = null;

            for(String res : results){
                if(res.equalsIgnoreCase("distinct")){
                    distinct = true;
                }
                if(res.equalsIgnoreCase("int") || res.equalsIgnoreCase("decimal") || res.equalsIgnoreCase("string") || res.equalsIgnoreCase("date") || res.equalsIgnoreCase("double")){
                    type = res.replace("double", "decimal");
                }
            }

            if (results.isEmpty()){
                System.out.println("results = empty");
            }



            //create distinct hierarchy according to type
            if(distinct){
                if(type == null){
                    //error
                    //ErrorWindow.showErrorWindow("Error reading metadata: no valid type found");

                }

                else if(type.equalsIgnoreCase("string")){
                    Data data = (Data) session.getAttribute("data");
                    if(data!=null){
                        h = new HierarchyImplString(fullPath,data.getDictionary());
                    }
                    else{
                        DictionaryString dict = new DictionaryString();
                        if(hierarchies!=null){
                            for(Map.Entry<String,Hierarchy> entry : hierarchies.entrySet()){
                                Hierarchy hTemp = entry.getValue();
                                if(hTemp instanceof HierarchyImplString){
                                    if(hTemp.getDictionaryData().getMaxUsedId() > dict.getMaxUsedId()){
                                        dict = hTemp.getDictionaryData();
                                    }
                                }

                            }
                        }
    //                    System.out.println("Mpainei string");
                        h = new HierarchyImplString(fullPath,dict);
                    }
    //                h = new HierarchyImplString(fullPath);
    //                h.setHierachyType("distinct");
                }
                else if(type.equalsIgnoreCase("date")){
                    return "error";
                }
                else{
                    h = new HierarchyImplDouble(fullPath);
    //                System.out.println("Mpaineiii");
    //                h.setHierachyType("distinct");
                }
            }else{      //create range hierarchy
                if (type.equalsIgnoreCase("date")){
                    h = new HierarchyImplRangesDate(fullPath);
                }
                else{
                    h = new HierarchyImplRangesNumbers(fullPath);
                }
    //            h.setHierachyType("range");
            }


            if(fullPath.endsWith(".txt")){
                h.load();
            }
            else{
                h.loadJson();
            }
            session.setAttribute("selectedhier", h.getName());
            hierarchies.put(h.getName(), h);

            if(os.equals("online")){
                this.deleteFiles(session);
            }

            return "OK";
//        }catch(Exception e){
//            System.out.println("Edwww exception");
//            return "Problem with loading anonymization rules "+e.getMessage();
//        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping(value="/action/savehierarchy") //method = RequestMethod.POST
    public @ResponseBody String saveHierarchy ( HttpServletResponse response, HttpSession session) throws FileNotFoundException, IOException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
        String hierName = (String)session.getAttribute("selectedhier");
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        h = hierarchies.get(hierName);
        
          
        String inputPath = (String)session.getAttribute("inputpath");
        this.createInputPath(inputPath, session);
        
//        File file = new File(inputPath + File.separator +hierName + ".txt");
        File file = new File(inputPath + File.separator + hierName+".json");

        
        h.exportJson(file.getAbsolutePath());
        
        
        InputStream myStream = new FileInputStream(file);

	// Set the content type and attachment header.
        if (h.getHierarchyType().equals("distinct")){
            response.addHeader("Content-disposition", "attachment;filename=distinct_hier_"+file.getName());
//            response.setContentType("txt/plain");
            response.setContentType("application/json");
        }
        else{
            response.addHeader("Content-disposition", "attachment;filename=range_hier_"+file.getName());
            response.setContentType("txt/plain");
        }

	// Copy the stream to the response's output stream.
	IOUtils.copy(myStream, response.getOutputStream());
	response.flushBuffer();
        
        
        if(os.equals("online")){
            this.deleteFiles(session);
        }
        
        
        


        return null;
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping(value="/action/autogeneratehierarchy", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String autogeneratehierarchy (@RequestParam("typehier") String typehier, @RequestParam("vartype") String vartype,@RequestParam("onattribute") int onattribute,@RequestParam("step") double step, @RequestParam("sorting") String sorting, @RequestParam("hiername") String hiername, @RequestParam("fanout") int fanout, @RequestParam("limits") String limits, @RequestParam("months") int months, @RequestParam("days") int days, @RequestParam("years") int years,  @RequestParam("length") int length, HttpSession session) throws LimitException  {
        Map<String, Hierarchy> hierarchies  = null;
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        Hierarchy h = null;

        
        if ( hierarchies == null ){
            hierarchies = new HashMap<>();
            session.setAttribute("hierarchies", hierarchies);
        }
              
        Data data = (Data) session.getAttribute("data");
        
        String attribute = data.getColumnByPosition(onattribute);
        
        if(typehier.equals("mask")){
            System.out.println("autogenerate backend "+step+" "+typehier+" "+vartype+" "+onattribute+" "+sorting+" "+hiername+" "+fanout+" "+limits+" "+length);
            h = new AutoHierarchyImplMaskString(hiername, vartype, "distinct",attribute, data,length);
        }
        else if (typehier.equals("distinct")){
            if(vartype.equals("int") ||vartype.equals("double")){
                h = new AutoHierarchyImplDouble(hiername, vartype, "distinct", attribute, sorting, fanout, data);

            }
            if(vartype.equals("date")){
                h = new AutoHierarchyImplDate(hiername, vartype, "distinct", attribute, data);
            }
            else if(vartype.equals("string")){
                h = new AutoHierarchyImplString(hiername, vartype, "distinct", attribute, sorting, fanout, data);
            }
        }
        else{
            if(vartype.equals("date")){
                String []temp = null;
                temp = limits.split("-");
                int start = Integer.parseInt(temp[0]);
                int end = Integer.parseInt(temp[1]);
                h = new AutoHierarchyImplRangesDate(hiername, vartype, "range", start, end, fanout, months, days, years);
            }
            
            else{
                String []temp = null;
                Double start=null,end=null;
                temp = limits.split("-");
                int count = StringUtils.countMatches(limits, "-");
                if(count==1){
                    start = Double.parseDouble(temp[0]);
                    end = Double.parseDouble(temp[1]);
                }
                else if(count==2){
                    try{
                        start = Double.parseDouble("-"+temp[1]);
                        end = Double.parseDouble(temp[2]);
                        System.out.println("Count "+count+" start "+start+" end "+end);
                    }catch(Exception e){
                        e.printStackTrace();
                        
                        // TODO exception 
                    }
                }
                else if(count==3){
                    try{
                        start = Double.parseDouble("-"+temp[1]);
                        end = Double.parseDouble("-"+temp[3]);
                        System.out.println("Count "+count+" start "+start+" end "+end);
                    }catch(Exception e){
                         e.printStackTrace();
                        
                        // TODO exception 
                    }
                }
                else{
                    /// TODO exception 
                    
                    System.out.println("Count "+count);
                }

                //h = new AutoHierarchyImplRangesNumbers(hiername, vartype, "range", start, end, step, fanout);
                System.out.println("info "+hiername+" "+vartype+" start "+start+" end "+end+" step "+step+" "+fanout);
                h = new AutoHierarchyImplRangesNumbers2(hiername, vartype, "range", start, end, step, fanout);
            }
        }
       
       h.autogenerate();
        
       
       hierarchies.put(h.getName(), h);
       session.setAttribute("selectedhier", h.getName());
        
        

        return "OK";
    }
    
    
    
    /* @RequestMapping(value="/action/gethierarchy") //method = RequestMethod.POST
    public @ResponseBody Hierarchy gethierarchy (@RequestParam("filename") String filename, HttpServletRequest request)  {
        String result = null;
        Hierarchy h = (Hierarchy) request.getSession().getAttribute("hierarchy");
       

        h.load();
        //this.hierarchies.put(h.getName(), h);
        
        return h;
    }*/
//    @JsonView(View.Graph.class)
    @RequestMapping(value="/action/gethiergraph", /*method = RequestMethod.GET)*/ method = RequestMethod.POST)
    public @ResponseBody Graph getHierGraph (@RequestParam("hiername") String hierName,@RequestParam("node") String node,@RequestParam("level") int level,HttpSession session) throws FileNotFoundException, IOException {
        Graph nGraph = null;
       
        Node n = null;
        Edge e = null;
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;

        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");        

        if ( hierarchies!= null){
            
                h = hierarchies.get(hierName);
          
        }
        ///System.out.println("node = " + node);
        //if ( !node.equals("(null)")){
            //System.out.println("Node="+node);
            nGraph = h.getGraph(node, level);    
        //}
        //if ( nGraph == null){
         //   System.out.println("i am hereeeeeeeeee");
       // }
        //else{
        //    System.out.println("nGraph = " + nGraph);
        //}
        //System.out.println("node = " + node);
        
        session.setAttribute("selectedhier", hierName);
        return nGraph;
        
    }
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping(value="/action/gethierarchies", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody HierToJson[] getHierarchies (@RequestParam("selectedhier") String selectedHier, HttpSession session)  {
        Map<String, Hierarchy> hierarchies  = null;
        HierToJson[] hierArray = null;
        
        //System.out.println("startttttttttt getHierarchies");
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        if (selectedHier.equals("null")){
//            System.out.println("null 1111111");
            if ( hierarchies!= null){
               // System.out.println("null 22222");
                hierArray = new HierToJson[hierarchies.size()];
                int i = 0;
                for (Map.Entry<String, Hierarchy> entry : hierarchies.entrySet()) {
                    hierArray[i] = new HierToJson(entry.getKey(),entry.getKey(),entry.getKey(),entry.getValue().getNodesType(),entry.getValue().getHierarchyType());
                    i++;
                }
            }
        }
        else{
//            System.out.println("not null 11111");
            if ( hierarchies!= null){
             //   System.out.println("not null 22222");
                hierArray = new HierToJson[hierarchies.size()];
                
                hierArray[0] = new HierToJson(selectedHier,selectedHier);
                int k = 0;
                int j = 1;
                
                for (Map.Entry<String, Hierarchy> entry : hierarchies.entrySet()) {
               //     System.out.println("not null 33333");
                    if ( !entry.getKey().equals(selectedHier)){
                 //        System.out.println("not null 44444");
                        hierArray[j] = new HierToJson(entry.getKey(),entry.getKey());
                        j++;
                    }
                    
                    hierArray[k].setSort(entry.getKey());
                    hierArray[k].setType(entry.getValue().getNodesType());
                    hierArray[k].setHierType(entry.getValue().getHierarchyType());
                    
                    k++;
                }
            }
        
        }
        
//        System.out.println("enddddddddddddddddd getHierarchies");
//        
//        for ( int i = 0 ; i < hierArray.length; i++){
//            System.out.println("id = " + hierArray[i].getId() + "\ttext = " + hierArray[i].getText());
//        }
        
        
        return hierArray;
    }
    
    
    
    
    
    
     /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @JsonView(View.GetDataTypes.class)
    @RequestMapping(value="/action/getattrtypes", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody Data getAttrTypes (HttpSession session)  {
        Data data = (Data) session.getAttribute("data");
        
        /*String [][]varTypes = null;
        
        varTypes = data.getTypeArr();*/
        
        
        return data;
        
    }
    
    
    @RequestMapping(value="/action/gethiertypes", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody HierToJson[] getHierTypes (HttpSession session)  {
        Map<String, Hierarchy> hierarchies  = null;
        HierToJson[] hierArray = null;
        Hierarchy h = null;
        
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        hierArray = new HierToJson[hierarchies.size()];

        if ( hierarchies!= null){
            int i = 0;
            for (Map.Entry<String, Hierarchy> entry : hierarchies.entrySet()) {
                h = entry.getValue();
                hierArray[i] = new HierToJson(entry.getKey(),h.getNodesType());
                i++;
            }
        }
        
        
        return hierArray;
    }
    
    
    /*@RequestMapping(value="/action/getattrnames") //method = RequestMethod.POST
    public @ResponseBody DataToJson[] getAttrNames (HttpSession session)  {
        DataToJson[] namesArr = null;
        Data data = (Data) session.getAttribute("data");
        
        
        Map <Integer,String> names = data.getColNamesPosition();
        
        namesArr = new DataToJson[names.size()];
        for ( int i = 0 ; i < names.size() ; i ++){
            System.out.println("xaxa= " + names.get(i));
            namesArr[i] = new DataToJson(names.get(i));
        }
        
        
        /*String [][]varTypes = null;
        
        varTypes = data.getTypeArr();
        
        
        
        return namesArr;
        
    }*/
                                
    @RequestMapping(value="/action/algorithmexecution", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String algorithmExecution (@RequestParam("k") int k, @RequestParam("m") int m, @RequestParam("algo") String algo , @RequestParam("relations") String[] relations , HttpSession session) throws IOException  {
        
        
        Algorithm algorithm = null;
        Map<Integer, Hierarchy> quasiIdentifiers = new HashMap<Integer, Hierarchy>();
        boolean result = false;
        Map<String, Hierarchy> hierarchies  = null;

        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        Data data = (Data) session.getAttribute("data");
        
        String algorithmSelected = algo;
        
        /*System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        for ( int  i = 0 ; i < relations.length ; i ++){
            System.out.println(" relation = " + relations[i]);
        }*/
        
        
        for ( int  i = 0 ; i < relations.length ; i ++){
            if (!relations[i].equals("")){
                quasiIdentifiers.put(i, hierarchies.get(relations[i]));
            }
        }
        
        
        /*for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        
        
        ///////////////////////new feature///////////////////////
        String checkHier = null;
        for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
            Hierarchy h = entry.getValue();
            System.out.println("h "+h+" htype "+h.getHierarchyType());
//            if (h.getHierarchyType().equals("range")){
//                checkHier = h.checkHier(data,entry.getKey());
//            }
//            else if(h instanceof HierarchyImplString){
//                h.syncDictionaries(entry.getKey(),data);
//            }
            
//            if(h instanceof HierarchyImplString){
//                h.syncDictionaries(entry.getKey(),data);
//            }
            
            //provlima stin  hierarchia
            checkHier = h.checkHier(data,entry.getKey());
            if(checkHier != null && !checkHier.endsWith("Ok")){
                return checkHier;
            }
        }

        ////////////////////////////////////////////
        

        
        Map<String, Integer> args = new HashMap<>();

        /*if(algorithmSelected.equals("Incognito")){
            args.put("k", k);
            //algorithm = new Incognito();
        }
        else*/ if(algorithmSelected.equals("Flash")){
            args.put("k", k);
            algorithm = new Flash();
            session.setAttribute("algorithm", "flash");
            
        }
        else if(algorithmSelected.equals("pFlash")){
            args.put("k", k);
            algorithm = new ParallelFlash();
            session.setAttribute("algorithm", "flash");
        }
        else if(algorithmSelected.equals("clustering")){
            args.put("k", k);
            algorithm = new ClusterBasedAlgorithm();
            session.setAttribute("algorithm", "clustering");
        }
        else if(algorithmSelected.equals("demographic")){
            args.put("k", k);
            algorithm = new DemographicAlgorithm();
            session.setAttribute("algorithm", "demographic");
        }
        else if(algorithmSelected.equals("dp")){
            args.put("k", k);
            algorithm = new DifferentialPrivacyAlgorithm();
            session.setAttribute("algorithm", "dp");
        }
        else if(algorithmSelected.equals("kmAnonymity") || algorithmSelected.equals("apriori") ||
                algorithmSelected.equals("AprioriShort") || algorithmSelected.equals("mixedapriori")){
                args.put("k", k);

            //check if m is an integer

            if(k > data.getDataLenght()){
                //ErrorWindow.showErrorWindow("Parameter k should be less or equal to the dataset length");
                //return;
            }
            args.put("m", m);

            /*if(algorithmSelected.equals("kmAnonymity")){
                algorithm = new KmAnonymity();
            }
            else if (algorithmSelected.equals("AprioriShort")){
                if(!(this.dataset instanceof SETData)){
                    ErrorWindow.showErrorWindow("No set-valued dataset loaded!");
                    return;
                }
                algorithm = new AprioriShort();
            }
            else*/ 
            if(algorithmSelected.equals("apriori")){
                if(!(data instanceof SETData)){
                    //ErrorWindow.showErrorWindow("No set-valued dataset loaded!");
                    //return;
                }
                algorithm = new Apriori();
                quasiIdentifiers.get(0).buildDictionary(quasiIdentifiers.get(0).getDictionary());
            }
            else if(algorithmSelected.equals("mixedapriori")){
                algorithm = new MixedApriori();
            }
            
            

            session.setAttribute("algorithm", "apriori");

        }


        algorithm.setDataset(data);
        algorithm.setHierarchies(quasiIdentifiers);
        algorithm.setArguments(args);
     


//        System.out.println("k = " + k + "\t m = " + m );

        //long startTime = System.currentTimeMillis();
//        long startCpuTime = getCpuTime();

        final String message = "memory problem";
        String resultAlgo="";
        Future<String> future = null;
        System.out.println("Algorithm starts");
        try {
            if(os.equals("online")){
                ExecutorService executor = Executors.newCachedThreadPool();
                final Algorithm temp = algorithm;
                future = executor.submit( new Callable<String>() {
                public String call() throws OutOfMemoryError {
                    try{
                    temp.anonymize();
                    }catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        return message;
                    }
                    
                    
                    return "Ok";
                }});
                resultAlgo = future.get(3, TimeUnit.MINUTES);
            }
            else{
                algorithm.anonymize();
            }

        }catch (TimeoutException e) {
        // Too long time
            if(future == null){
                e.printStackTrace();
            }
            else{
                e.printStackTrace();
                future.cancel(true);
                restart(session);
                return "outoftime";
            }
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return message;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
            restart(session);
            return "wrong";
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
            restart(session);
            return "wrong";
        }
        
        if(resultAlgo.equals(message)){
            return message;
        }
        

        hierarchies = new HashMap<>();
        session.setAttribute("quasiIdentifiers", quasiIdentifiers);
        session.setAttribute("k", k);

        if(algorithm.getResultSet() == null){
            if(!algorithmSelected.equals("clustering")){
                result = false;
                return "noresults";
            }
        }
        else{
//            System.out.println("result set = " + algorithm.getResultSet() );
        
            session.setAttribute("results", algorithm.getResultSet());
//            String sol = this.InformationLoss(session);
//            System.out.println("Solutions pFlash "+algorithm.getResultSet());
//            System.out.println("Sol pFlash "+sol);
//            System.out.println("algorithm : "+algorithmSelected);
            if(!algorithmSelected.equals("dp") && !algorithmSelected.equals("apriori") && !algorithmSelected.equals("mixedapriori") && !algorithmSelected.equals("clustering") && !algorithmSelected.equals("demographic")){
                Graph graph = algorithm.getLattice();

                session.setAttribute("graph", graph);
            }
        }
        
        


        result = true;


//        String solution = this.InformationLoss(session);
//        this.setSelectedNode(session, solution);
        

        //////information loss not needed in interface///////
        //String solution = this.InformationLoss(session);
        //this.setSelectedNode(session, solution);
        ////////////////////////////////////////////////////


        return "ok";
    }
    
    @RequestMapping(value="/action/informationloss", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody String InformationLoss ( HttpSession session) {
        Set<GridNode> infoLossFirstStep = new HashSet<>();
        Set<GridNode> infoLossSecondStep = new HashSet<>();
        int minSum = 0;
        int []minHierArray;
        int minHier;
        GridNode solution = null;
        String solutionStr = null;
        
        boolean FLAG = false;
        
        Set<GridNode> results = (Set<GridNode>) session.getAttribute("results");
        /*for ( LatticeNode n : results){
            System.out.println("n = " + n + "\t level = " + n.getLevel() );
        }*/
        
        //first step, sum of levels
        for ( GridNode n : results){
            if (FLAG == false){
                minSum = n.getLayer();
                FLAG = true;
            }
            else{
                if ( minSum > n.getLayer()){
                    minSum = n.getLayer();
                }
            }
        }
        
        for ( GridNode n : results){
            if ( minSum == n.getLayer()){
                infoLossFirstStep.add(n);
            }         
        }
        
        //second step, min max hierarchy
        minHierArray = new int[infoLossFirstStep.size()];
        int counter = 0;
        for ( GridNode n : infoLossFirstStep){
            int []temp = n.getArray();
            minHierArray[counter] = Ints.max(temp);
            counter++;
        }
//        System.out.println("Info loass "+Arrays.toString(minHierArray));
        minHier = Ints.min(minHierArray);

        for ( GridNode n : infoLossFirstStep){
            int []temp = n.getArray();
            if (minHier == Ints.max(temp)){
                infoLossSecondStep.add(n);
            }
        }
 
        //third step, choose the first one
        for ( GridNode n : infoLossSecondStep){
            solution = n;
            break;
        }
        
        solutionStr = solution.toString();
        solutionStr = solutionStr.replace("[","");
        solutionStr = solutionStr.replace("]","");
        solutionStr = solutionStr.replace(" ", "");
//        System.out.println("solution = " + solutionStr);
        
        
        return solutionStr;
        
    }
    
    @RequestMapping(value="/action/dataquality", method = RequestMethod.POST)
    public @ResponseBody Map<String,Double> lossMetrics ( HttpSession session) throws FileNotFoundException, IOException, ParseException, Exception {
        Data data = (Data) session.getAttribute("data");
        AnonymizedDataset anonData = (AnonymizedDataset) session.getAttribute("anondata");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
        anonData = this.getAnonDataSet(0,10,session);
//        if(anonData == null){
//            anonData = this.getAnonDataSet(0,10,session);
//        }
//        else{
//            anonData.setStart(0);
//        }
        
        
        if(data instanceof TXTData || data instanceof DICOMData){
            String algorithm = (String) session.getAttribute("algorithm");
            /// algorithm for demographic
            if(algorithm!=null && algorithm.equals("demographic")){
                Map<Integer, Map<Integer,Object>> rules = (Map<Integer, Map<Integer,Object>>) session.getAttribute("results");
                anonData.exportDataset(null,rules);
            }
            else{
                anonData.exportDataset(null, true);
                
            }
        }
        else if(data instanceof DiskData){
            
            anonData.exportDiskDataset(null,quasiIdentifiers);
            
        }
        else if(data instanceof RelSetData){
            Map<Integer, Map<Object,Object>> results = (Map<Integer, Map<Object,Object>>) session.getAttribute("results");
                anonData.exportRelSetDataset(null, results, quasiIdentifiers);
        }
        else{
            Map<Double, Double> results = (Map<Double, Double>) session.getAttribute("results");
            anonData.exportDataset(null, results, quasiIdentifiers);
        }
        
        Map<String,Double> inLoss = data.getInformationLoss();
        for(Map.Entry<String,Double> loss : inLoss.entrySet()){
            System.out.println("Metric "+loss.getKey()+" value "+loss.getValue());
        }
        return data.getInformationLoss();
    }
    
    @RequestMapping(value="/action/checksuppress", method = RequestMethod.POST)
    public @ResponseBody boolean checkSuppress ( HttpSession session) throws FileNotFoundException, IOException {
        
        return (session.getAttribute("suppressSolution")!=null && (boolean)session.getAttribute("suppressSolution")) || session.getAttribute("tosuppress")==null;
    }
    
    @RequestMapping(value="/action/getsolutiongraph", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody Graph getSolGraph ( HttpSession session) throws FileNotFoundException, IOException {
        Graph graph = null;
        
        if(session.getAttribute("suppressSource")!=null && ((boolean) session.getAttribute("suppressSource"))){
            return null;
        }
        
        graph = (Graph) session.getAttribute("graph");
        Set<GridNode> results = (Set<GridNode>) session.getAttribute("results");
        
        
        
        return graph;
        
    }
    
    
    
    
    @RequestMapping(value="/action/getanondataset", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody AnonymizedDataset getAnonDataSet (@RequestParam("start") int start , @RequestParam("length") int length, HttpSession session) throws FileNotFoundException, IOException, ParseException, Exception{
        AnonymizedDataset anonData = null;
        String selectedNode = null;
        
        
        selectedNode = (String)session.getAttribute("selectednode");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Set<String>> toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
        String algorithm = (String) session.getAttribute("algorithm");
        Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
        
        Map<String, Set<String>> toSuppressJson = null;
        String selectedAttrNames = null;
        boolean FLAG = false;
        String [] temp = null;
        
        
        
        //if (selectedNode!= null){
        if ( allRules == null){
            //System.out.println("get anon dataset to suppress = " + toSuppress);
            if (toSuppress != null){
                System.out.println("Suppression is cuclulate");
                toSuppressJson = new HashMap<String, Set<String>>();
                for (Map.Entry<Integer, Set<String>> entry : toSuppress.entrySet()) {
                    if ( entry.getKey().toString().equals("-1")){
                        for (Map.Entry<Integer, Hierarchy> entry1 : quasiIdentifiers.entrySet()) {             
                            if ( FLAG == false){
                                FLAG = true;
                                selectedAttrNames = data.getColumnByPosition((Integer)entry1.getKey());
                            }
                            else{
                                selectedAttrNames = selectedAttrNames + "," + data.getColumnByPosition((Integer)entry1.getKey());

                            }
                        }
                        toSuppressJson.put(selectedAttrNames,entry.getValue());
                    }
                    else{
                        selectedAttrNames = data.getColumnByPosition((Integer)entry.getKey());
                        toSuppressJson.put(selectedAttrNames,entry.getValue());
                    }               
                }
            }
            else{
                    for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
                        if ( FLAG == false){
                            FLAG = true;
                            selectedAttrNames = data.getColumnByPosition((Integer)entry.getKey());
                        }
                        else{
                            selectedAttrNames = selectedAttrNames + "," + data.getColumnByPosition((Integer)entry.getKey());

                        }
                    }
               
            }

        }

            if (data == null){
                System.out.println("data is null");
            }
            else{
//                ArrayList<LinkedHashMap> originalData = data.getPage(start, length);
                //Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
                anonData = new AnonymizedDataset(data,start,length,selectedNode,quasiIdentifiers,toSuppress,selectedAttrNames,toSuppressJson,false);
//                anonData.setDataOriginal(originalData);
                if(algorithm!=null && algorithm.equals("dp")){
                    if(data instanceof TXTData){
                        Object[][] anonymdata = (Object[][]) session.getAttribute("results");
                        anonData.renderAnonymizeDifferential(anonymdata);
                    }
                    else{
                        anonData.renderAnonymizeDifferentialDisk();
                    }
                }
                else if (!data.getClass().toString().contains("SET") && !data.getClass().toString().contains("RelSet") && !data.getClass().toString().contains("Disk")){
                    System.out.println("action/getanondataset TXT ===========");
                    if ( allRules == null){
                        if(algorithm!=null && algorithm.equals("demographic")){
                            Map<Integer, Map<Integer,Object>> rules = (Map<Integer, Map<Integer,Object>>) session.getAttribute("results");
                            anonData.renderAnonymizedTableDemographic(rules);
                        }
                        else{
                            anonData.renderAnonymizedTable();
                        }
                    }
                    else{
                        anonData.anonymizeWithImportedRules(allRules,null);
                    }
                    
                }
                else if(data.getClass().toString().contains("SET")){
                    
//                    System.out.println("action/getanondataset ===========");
                    
                    Map<Double, Double> rules = (Map<Double, Double>) session.getAttribute("results");
                    if ( allRules == null){
                        anonData.renderAnonymizedTable(rules, quasiIdentifiers.get(0).getDictionary());
                    }
                    else{
                        anonData.anonymizeSETWithImportedRules(allRules,null);
                    }
                }
                else if(data.getClass().toString().contains("Disk")){
                    System.out.println("action/getanondataset Disk ===========");
                    if(allRules == null){
                        anonData.renderAnonymizedDiskTable();
                    }
                    else{
                        anonData.anonymizeWithImportedRulesForDisk(allRules,null);
                    }
                }
                else{
                    System.out.println("action/getanondataset Mixed ===========");
                    
                    Map<Integer,Map<Object,Object>> rules = (Map<Integer,Map<Object,Object>>) session.getAttribute("results");
                    if ( allRules == null){
                       anonData.renderAnonymizedTable(rules);
                    }
                    else{
                        anonData.anonymizeRelSetWithImportedRules(allRules,null);
                    }
                    
                }
                session.setAttribute("anondata", anonData);
            }
        
        
        //}
        //else{
          //  anonData = new AnonymizedDataset(data,0,0,selectedNode,quasiIdentifiers,toSuppress,selectedAttrNames,toSuppressJson);
            
            
            
        //}

        return anonData;
        
    }
     
    
    @RequestMapping(value="/action/savedataset") //method = RequestMethod.POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public @ResponseBody void saveDataset (HttpServletRequest request,HttpSession session , HttpServletResponse response) throws FileNotFoundException, IOException {
        Object [][] exportData = null; 
        
        if(request !=null){
            System.out.println("app export datasettttttt");
            ServletContext context = request.getServletContext();
            String appPath = context.getRealPath("");
            System.out.println("appPath = " + appPath);
        }
                
        Data data = (Data) session.getAttribute("data");
        String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        
        
        
        System.out.println("Export Dataset... " + filename);
        System.out.println("Export Dataset...");
        this.createInputPath(inputPath, session);
        if(!(data instanceof DICOMData)){
            File file = new File(inputPath + File.separator +filename);
            file.createNewFile(); 
        }
        /*System.out.println(file.getAbsolutePath());
        try {
            // get your file as InputStream
            InputStream inputStream = new FileInputStream(file);
            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            System.out.println("done");
          } catch (IOException ex) {
            System.out.println("Error writing file to output stream. Filename was '{}'"+ file.getName() +"\t" + ex);
            throw new RuntimeException("IOError writing file to output stream");
          }*/
        
        
        /* try {
            String filePathToBeServed = inputPath + "/" +filename;//complete file name with path;
            File file = new File(filePathToBeServed);
            InputStream inputStream = new FileInputStream(file);
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment; filename="+file.getName()+".txt"); 
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            inputStream.close();
        } catch (Exception e){
            System.out.println("Request could not be completed at this moment. Please try again.");
            e.printStackTrace();
        }*/

        // Get your file stream from wherever.
        data.exportOriginalData();
        if(response != null){
//            ZipInputStream inputStream = new ZipInputStream(new FileInputStream(new File(inputPath + File.separator+"anonymized_files.zip")));
            InputStream myStream = new FileInputStream(new File(inputPath + File.separator+"anonymized_files.zip"));

            // Set the content type and attachment header.
            response.addHeader("Content-disposition", "attachment;filename=anonymized_files.zip");
            response.setContentType("application/zip");

            // Copy the stream to the response's output stream.
            IOUtils.copy(myStream, response.getOutputStream());
            response.flushBuffer();
            myStream.close();
            if(os.equals("online")){
                this.deleteFiles(session);
            }
        }
        
        
       

        //response.setContentType(APPLICATION_PDF);
        //response.setContentType("application/octet-stream");
        //response.addHeader("Content-Disposition", "attachment; filename=" + file.getName());
        
        /*InputStream inputStream = new FileInputStream(file);
        response.addHeader("Content-Disposition", "attachment; filename="+file.getName()); 
        response.setContentType("text/csv");
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        inputStream.close();*/

        //response.addHeader("Content-Length", String.valueOf(file.length()));
        /*ServletOutputStream out;
        
        try (FileInputStream fileIn = new FileInputStream(file)) {
            out = response.getOutputStream();
            

            byte[] outputByte = new byte[50000];
            //copy binary contect to output stream
            while(fileIn.read(outputByte, 0, 50000) != -1)
            {
                out.write(outputByte, 0, 50000);
            }
        }
        out.flush();
        out.close();*/
        
        
        
        /*response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment;filename="+"content.pdf");*/
        //htre.setHeader(null, null);
        //htre.getOutputStream();
        
        
    }
    
    @RequestMapping(value="/action/saveanonymizedataset") //method = RequestMethod.POST
    public @ResponseBody void saveAnonymizeDataset ( HttpSession session , HttpServletResponse response) throws FileNotFoundException, IOException, ParseException, InterruptedException {
        Object [][] exportData = null; 

        Data data = (Data) session.getAttribute("data");
        String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
        
        this.createInputPath(inputPath, session);
        
        
//        System.out.println("Export Anonymized Dataset... " + filename);
        AnonymizedDataset anonData = (AnonymizedDataset)session.getAttribute("anondata");
        anonData.setStart(0);
//        System.out.println("Export anonymizedDataset...");
        File file = new File(inputPath + File.separator + "anonymized_" +filename);
        
        if (data.getClass().toString().contains("SET")){
            Map<Double, Double> results = (Map<Double, Double>) session.getAttribute("results");
            try{
                exportData = anonData.exportDataset(file.getAbsolutePath(), results, quasiIdentifiers);
            }catch(Exception e){
                anonData.setLength(data.getRecordsTotal());
                anonData.anonymizeSETWithImportedRules(allRules,file.getAbsolutePath());
            }
        }
        else if(data.getClass().toString().contains("RelSet")){
            Map<Integer, Map<Object,Object>> results = (Map<Integer, Map<Object,Object>>) session.getAttribute("results");
            try{
                exportData = anonData.exportRelSetDataset(file.getAbsolutePath(), results, quasiIdentifiers);
            }catch(Exception e){
                anonData.setLength(data.getRecordsTotal());
                anonData.anonymizeRelSetWithImportedRules(allRules,file.getAbsolutePath());
            }
        }
        else if(data.getClass().toGenericString().contains("Disk")){
            String algorithm = (String) session.getAttribute("algorithm");
            System.out.println("Mpainei gia disk export");
            try{
                if(algorithm!=null && algorithm.equals("dp")){
                    exportData = anonData.exportDiskDatasetDifferential(file.getAbsolutePath(),quasiIdentifiers);
                }
                else{
                    exportData = anonData.exportDiskDataset(file.getAbsolutePath(),quasiIdentifiers);
                }
            }catch(Exception e){
                e.printStackTrace();
                anonData.setLength(data.getRecordsTotal());
                anonData.anonymizeWithImportedRulesForDisk(allRules,file.getAbsolutePath());
            }
        }
        else{
            String algorithm = (String) session.getAttribute("algorithm");
            if(algorithm!=null && algorithm.equals("demographic")){
                Map<Integer, Map<Integer,Object>> rules = (Map<Integer, Map<Integer,Object>>) session.getAttribute("results");
                anonData.exportDataset(file.getAbsolutePath(),rules);
            }
            else if(algorithm!=null && algorithm.equals("dp")){
                Object[][] anonymdata = (Object[][]) session.getAttribute("results");
                anonData.exportDatasetDifferential(file.getAbsolutePath(),anonymdata);
            }
            else{
                try{
                    exportData = anonData.exportDataset(file.getAbsolutePath(), true);
                }catch(Exception e){
                    try{
                        exportData = anonData.exportDataset(file.getAbsolutePath(), false);
                    }catch(Exception e1){
                        System.out.println("DAta length "+data.getRecordsTotal());
                        anonData.setLength(data.getRecordsTotal());
                        anonData.anonymizeWithImportedRules(allRules,file.getAbsolutePath());
                    }               
                }
            }
        }
        
        /*System.out.println("End ");
        File file = new File(inputPath + "/anonymized_" +filename);
        
        //data.export(file.getAbsolutePath(), null, exportData , null,null, null);*/
        
        if(response!=null){
            
            if(data instanceof DICOMData){
                file = new File(inputPath + File.separator + "anonymized_dicom_files.zip");
                response.setContentType("application/zip");
            }
            else{
                response.setContentType("txt/plain");
            }
            
            InputStream myStream = new FileInputStream(file);

            // Set the content type and attachment header.
            response.addHeader("Content-disposition", "attachment;filename="+file.getName());

            // Copy the stream to the response's output stream.
            IOUtils.copy(myStream, response.getOutputStream());
            response.flushBuffer();
            myStream.close();
            
            
            if(os.equals("online")){
                this.deleteFiles(session);
            }
        }
        
        //htre.setHeader(null, null);
        //htre.getOutputStream();
        
        
        
        
    }
    
    @RequestMapping(value="/action/getdataversefiles", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody JSONObject getDataverseFiles ( HttpSession session,@RequestParam("usertoken") String usertoken, @RequestParam("server_url") String server_url, @RequestParam("dataset_id") String dataset_id ) throws FileNotFoundException, IOException, MalformedURLException, Exception {
        List<DataverseFile> files = null;
        if(server_url.endsWith("/")){
            server_url = server_url.substring(0, server_url.length() - 1); 
        }
        files = DataverseConnection.getDataverseFiles(server_url, usertoken, dataset_id);
        if(files==null){
            return null;
        }
        else{
            JSONObject jdata = new JSONObject();
            session.setAttribute("dataversefiles",files);
            jdata.put("data", files);
            return jdata;
        }
    }
    
    @RequestMapping(value="/action/getzenodofiles", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ZenodoFilesToJson getZenodoFiles ( HttpSession session,@RequestParam("usertoken") String usertoken  ) throws FileNotFoundException, IOException {
//        System.out.println("Zenodo Files = " + usertoken);
        ZenodoFilesToJson zenJson = null;
        Map<Integer, ZenodoFile> files = null;
        
        //System.out.println("edwwwwwww");
        //String usertoken1 = "cSQgGzD08dJ11RMyRzLRhU4hi57LK454T8sovlw6Z2STZrQbzg809wUt6ywt";
         files = ZenodoConnection.getDepositionFiles(usertoken);
         if (files == null){
             return null;
         }
         else{
            zenJson = new ZenodoFilesToJson(files,false,null,null,null,null);
            session.setAttribute("zenodofiles",files);
         }
        
        
        
        return zenJson;
        
    }
    
    
    @RequestMapping(value="/action/loaddataversefile", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loaddataversefile ( HttpSession session,@RequestParam("filename") String fileName, @RequestParam("type") String type,
            @RequestParam("size") String filesize, @RequestParam("usertoken") String usertoken, @RequestParam("server_url") String server_url
            ) throws FileNotFoundException, IOException {
        
        if(server_url.endsWith("/")){
            server_url = server_url.substring(0, server_url.length() - 1); 
        }
        
        List<DataverseFile> files = (List<DataverseFile>) session.getAttribute("dataversefiles");
        String inputPath = (String)session.getAttribute("inputpath");
        File dir1,f1,dir = null;
        String rootPath;

        if(this.os.equals("linux")){
            f1 = new File(System.getProperty("java.class.path"));//linux
            dir1 = f1.getAbsoluteFile().getParentFile();
            rootPath = dir1.toString();
        }
        else{
            rootPath = System.getProperty("user.home");//windows
        }
        
        
        if(os.equals("online")){
            dir = new File(this.rootPath + File.separator + "amnesia"+ File.separator + session.getId());  
            if (!dir.exists()){
                dir.mkdirs();
            }
            inputPath = this.rootPath + File.separator + "amnesia"+ File.separator + session.getId();
        }
        else{
            dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
            if (!dir.exists()){
                dir.mkdirs();
            }
            inputPath = rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId();
        }
        session.setAttribute("inputpath",inputPath);
        session.setAttribute("filename",fileName);
        
        for(DataverseFile f : files){
            if (f.getFileName().equals(fileName) && f.getType().equals(type) && f.getFilesize().equals(filesize)){
                DataverseConnection.downloadFile(server_url, usertoken, inputPath, f);
                break;
            }
        }
        return null;
    }
    
    
    @RequestMapping(value="/action/loadzenodofile", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loadzenodofile ( HttpSession session,@RequestParam("filename") String fileName, @RequestParam("title") String title, @RequestParam("usertoken") String usertoken  ) throws FileNotFoundException, IOException {
//        System.out.println("Zenodo Files");
        ZenodoFilesToJson zenJson = null;
        ZenodoFile zenFile = null;
        
//        System.out.println("i am hereeeee");
//        
//        System.out.println("usertoken = " + usertoken);
        
        Map<Integer, ZenodoFile> files = (Map<Integer, ZenodoFile>)session.getAttribute("zenodofiles");
        String inputPath = (String)session.getAttribute("inputpath");
        File dir1,f1,dir = null;
        String rootPath;

        if(this.os.equals("linux")){
            f1 = new File(System.getProperty("java.class.path"));//linux
            dir1 = f1.getAbsoluteFile().getParentFile();
            rootPath = dir1.toString();
        }
        else{
            rootPath = System.getProperty("user.home");//windows
        }
        if ( inputPath == null){
            if(os.equals("online")){
                dir = new File(this.rootPath + File.separator + "amnesia"+ File.separator + session.getId());  
                if (!dir.exists()){
                    dir.mkdirs();
                }
                inputPath = this.rootPath + File.separator + "amnesia"+ File.separator + session.getId();
            }
            else{
                dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
                if (!dir.exists()){
                    dir.mkdirs();
                }
                inputPath = rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId();
            }
            session.setAttribute("inputpath",inputPath);
            session.setAttribute("filename",fileName);
            
        }
        
//        System.out.println("i am hereeeee22222");

       
        for (Map.Entry<Integer, ZenodoFile> entry : files.entrySet()) {
            zenFile = entry.getValue();
//            System.out.println("zen file = " + zenFile.getFileName() + "\timportname = " + fileName);
//            System.out.println("zen title = " + zenFile.getTitle() + "\timporttitle = " + title);
            if (zenFile.getFileName().equals(fileName)){
                if (zenFile.getTitle().equals(title)){
                    break;
                }
            }
        }
        
        System.out.println("i am hereeeee3333");

        
        ZenodoConnection.downloadFile(zenFile, inputPath + File.separator + zenFile.getFileName(),usertoken );
        
        System.out.println("i am hereeeeeeeeeeeeeeeeeeeeeeeeeee2222222222 =" +zenFile.getFileName() );
//        if(os.equals("online")){
//            this.deleteFiles(session);
//        }
        
        return null;
    }
    
    
    
    
    @RequestMapping(value="/action/getsimilarzenodofiles", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ZenodoFilesToJson getSimilarZenodoFiles ( HttpSession session,@RequestParam("usertoken") String usertoken,@RequestParam("filename") String fileName,@RequestParam("title") String title,@RequestParam("keywords") String keywords  ) throws FileNotFoundException, IOException {
        System.out.println("Zenodo Files");
        ZenodoFilesToJson zenJson = null;
        
        
        //String usertoken = "cSQgGzD08dJ11RMyRzLRhU4hi57LK454T8sovlw6Z2STZrQbzg809wUt6ywt";
        String inputPath = (String)session.getAttribute("inputpath");
        String fileNameInput = (String)session.getAttribute("filename");
        File dir = new File(inputPath);  
        if (!dir.exists()){
                dir.mkdirs();
        }
        inputPath = inputPath +File.separator + fileNameInput;
        
        
        Map<Integer, ZenodoFile> files = ZenodoConnection.getDepositionFiles(usertoken);
        if (files == null){
             return null;
         }
         else{
            Data data = (Data) session.getAttribute("data");
            Double[][] inverse = Arrays.stream(data.getDataSet()).map(d -> Arrays.stream(d).boxed().toArray(Double[]::new)).toArray(Double[][]::new);
            data.exportOriginalData();
            zenJson = new ZenodoFilesToJson(files,true,fileName,title,keywords,inputPath);
            session.setAttribute("zenodofiles",files);
         }
        
        
        return zenJson;
    }
    
    
    @RequestMapping(value="/action/saveurltoreturn", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST //only for zenodo
    public @ResponseBody void saveUrlToReturn( HttpSession session,@RequestParam("url") String url ) throws FileNotFoundException, IOException {
        session.setAttribute("urltoreturn",url);
        System.out.println("urlllllllllll = " + url);
        
    }
    
    @RequestMapping(value="/action/savefiletodataverse", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String saveFileToDataverse ( HttpSession session, @RequestParam("usertoken") String usertoken, @RequestParam("descr") String descr,
            @RequestParam("server_url") String server_url, @RequestParam("dataset_id") String dataset_id) throws FileNotFoundException, 
            IOException, ParseException, InterruptedException{
        
        if(server_url.endsWith("/")){
            server_url = server_url.substring(0, server_url.length() - 1); 
        }
        
        String url = null;
        url = (String)session.getAttribute("urltoreturn");
        
        String tempName = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        this.createInputPath(inputPath, session);
        
        String file = null;
        
        if (url.equals("mydataset.html")){
            this.saveDataset(null,session, null);
            file = inputPath + File.separator +"anonymized_files.zip";
        }
        else{
           this.saveAnonymizeDataset(session, null);
           if(tempName.endsWith(".dcm")){
               tempName = "dicom_files.zip";
           }
           file = inputPath  + File.separator + "anonymized_" +tempName;
        }
        
        DataverseConnection.uploadFile(server_url, usertoken, dataset_id, file, descr);
        
//        if(os.equals("online")){
//            this.deleteFiles(session);
//        }
        
        return url;
    }
    
    @RequestMapping(value="/action/savefiletozenodo", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String saveFileToZenodo ( HttpSession session, @RequestParam("usertoken") String usertoken,@RequestParam("author") String author, @RequestParam("affiliation") String affiliation, @RequestParam("filename") String filename ,@RequestParam("title") String title,@RequestParam("description") String description, @RequestParam("contributors") String contributors, @RequestParam("keywords") String keywords  ) throws FileNotFoundException, IOException, ParseException, InterruptedException {
        System.out.println("Save Files to Zenodo");
        
        String url = null;
        url = (String)session.getAttribute("urltoreturn");
        
        System.out.println("urare here = " + url);
        
        String tempName = (String)session.getAttribute("filename");
        String type = "dataset";
        String access = "open";
        String file = null;
        String inputPath = (String)session.getAttribute("inputpath");
        this.createInputPath(inputPath, session);
        //String filename = null;
        
        Object [][] exportData = null; 

        if (url.equals("mydataset.html")){
            this.saveDataset(null,session, null);
            file = inputPath + File.separator +"anonymized_files.zip";
            System.out.println("edwwwwwwwwwwwwwwwww");
        }
        else{
            System.out.println("anonymizeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            this.saveAnonymizeDataset(session, null);
            //filename = "anonymize" +tempName;
            file = inputPath  + File.separator + "anonymized_" +tempName;
        }
         
        
        System.out.println("url = " + url);
        
        //crete deposition 
       Long depositionId = ZenodoConnection.createDeposition(usertoken, 
                title, 
                type, 
                description, 
                author,
                affiliation,
                access, 
                keywords);
       
        System.out.println("depositionId2222 = " + depositionId);
        
        if(depositionId == null){
            //Show error
            ZenodoConnection.getErrorMessage();
            return null;
        }
        
        System.out.println("depositionId = " + depositionId);
        
        //upload file to deposition
        if(!ZenodoConnection.uploadFileToDeposition(depositionId, 
                file, 
                filename, 
                usertoken)){
            //Show error
            ZenodoConnection.getErrorMessage();
            return null;
        }
    
        //publish deposition
        if(!ZenodoConnection.publishDeposition(depositionId, usertoken)){
            //Show error
            ZenodoConnection.getErrorMessage();
            return null;
        }
        
        System.out.println("url = " + url);
       
        if(os.equals("online")){
            this.deleteFiles(session);
        }
        
        return url;
    }
    
    
    
    @RequestMapping(value="/action/getselectednode", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String getSelectedNode ( HttpSession session  ) throws FileNotFoundException, IOException {
        String selectedNode = null;
       
        
        selectedNode = (String) session.getAttribute("selectednode");
        
        
        
        return selectedNode;
        
    }
    
    
    @RequestMapping(value="/action/setselectednode", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void setSelectedNode ( HttpSession session, @RequestParam("selectednode") String selectedNode   ) throws FileNotFoundException, IOException {
            
       session.setAttribute("selectednode",selectedNode);
           
    }
    
    
    
    @RequestMapping(value="/action/getsetdata", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ErrorMessage getSetData ( HttpSession session   ) throws FileNotFoundException, IOException {
        ErrorMessage errMes = new ErrorMessage();
        try{ 
            Data data = null;
            String rootPath = (String)session.getAttribute("inputpath");
            String filename = (String)session.getAttribute("filename");
            DictionaryString dict = null;
            Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");


            File dir = new File(rootPath);

            String fullPath = dir + File.separator + filename;

            dict = new DictionaryString();
            if(hierarchies!=null){
                for(Map.Entry<String,Hierarchy> entry : hierarchies.entrySet()){
                    Hierarchy h = entry.getValue();
                    if(h instanceof HierarchyImplString){
                        if(h.getDictionary().getMaxUsedId() > dict.getMaxUsedId()){
                            dict = h.getDictionary();
                        }
                    }

                }
            }

            data = new SETData(fullPath,",",dict);
            data.readDataset(null, null);
            session.setAttribute("data", data);   
            errMes.setSuccess(true);
            errMes.setProblem("Set-valued dataset was successfully saved on server");
            return errMes;
        }catch(Exception e){
            errMes.setSuccess(false);
            errMes.setProblem("Problem with set-valued dataset");
            return errMes;
        }
        
    }
    
    
    
    @RequestMapping(value="/action/getpairranges", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody MyPair getPairRanges (@RequestParam("columnAttr") int columnAttr,@RequestParam("vartype") String vartype, HttpSession session)  {
        Data data = (Data) session.getAttribute("data");
        MyPair p = null;
               
        if (vartype.equals("date")){
            p = new MyPair(data,vartype);
        }
        else{
            p = new MyPair(data,null);
        }
        p.findMin(columnAttr);

        return p;
    }
    
    
    @RequestMapping(value="/action/addnodehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String addNodeHier (@RequestParam("newnode") String newNode, @RequestParam("parent") String parent, @RequestParam("hiername") String hierName, HttpSession session) throws ParseException, LimitException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
        
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        Data data = (Data) session.getAttribute("data");
        
        
        h = hierarchies.get(hierName);
        
        
        if ( h.getNodesType().equals("string")){
            int newStrId,parentId;
            
            if(newNode.equals("") || newNode.equals("(null)")){
                newNode = "NaN";
            }
            DictionaryString dictData = h.getDictionaryData();
            DictionaryString dictHier = h.getDictionary();
            
            
            if(dictData.containsString(newNode)){
                newStrId = dictData.getStringToId(newNode);
                if(h.getParent((double)newStrId)!=null){
                    return "The node exists in hierarchy";
                }
            }
            else if(dictHier.containsString(newNode)){
                newStrId = dictHier.getStringToId(newNode);
                if(h.getParent((double)newStrId)!=null){
                    return "The node exists in hierarchy";
                }
            }
            else{
                if(dictData.isEmpty() && dictHier.isEmpty()){
                    System.out.println("Both empty");
                    newStrId = 1;
                }
                else if(!dictData.isEmpty() && !dictHier.isEmpty()){
                    System.out.println("Both have values");
                    if(dictData.getMaxUsedId() > dictHier.getMaxUsedId()){
                        newStrId = dictData.getMaxUsedId()+1;
                    }
                    else{
                        newStrId = dictHier.getMaxUsedId()+1;
                    }
                }
                else if(dictData.isEmpty()){
                    System.out.println("Dict data empty");
                    newStrId = dictHier.getMaxUsedId()+1;
                }
                else{
                    System.out.println("Dict hier empty");
                    newStrId = dictData.getMaxUsedId()+1;
                }
                
                h.getDictionary().putIdToString(newStrId, newNode);
                h.getDictionary().putStringToId(newNode, newStrId);
            }
            
            if(dictData.containsString(parent)){
                parentId = dictData.getStringToId(parent);
            }
            else{
               parentId = dictHier.getStringToId(parent); 
            }
//            Double parentVal=null;
////            System.out.println("Parent "+parent);
//            parentVal = dictHier.getStringToId(parent) == null ? (double) dictData.getStringToId(parent) : (double) dictHier.getStringToId(parent);
//            
//            if(h.getHeight()-1==h.getLevel(parentVal)){
//                return;
//            }
//            else if(h.getDictionaryData().containsString(parent)){
//                
//                strCount = dictData.getMaxUsedId() > h.getDictionary().getMaxUsedId() ? dictData.getMaxUsedId()+1 : dictHier.getMaxUsedId()+1;
//                dictData.putIdToString(strCount, newNode);
//                dictData.putStringToId(newNode, strCount);
//            }
//            else{
//                
//                
//                if(dictHier.containsString(newNode) && h.checkExistance(dictHier.getStringToId(newNode).doubleValue())){
//                        return;
//                }
//                
//                if(dictData.containsString(newNode)){
////                    System.out.println("Edvvvvv gia nan add");
//                    strCount = dictData.getStringToId(newNode);
//                }
//                else{
//                
//                    parentVal = (double) dictHier.getStringToId(parent);
//                    if(parentVal == null){
//                        parentVal = (double) dictData.getStringToId(parent);
//                    }
//                    strCount = dictData.getMaxUsedId() > h.getDictionary().getMaxUsedId() ? dictData.getMaxUsedId()+1 : dictHier.getMaxUsedId()+1;
//                    dictHier.putIdToString(strCount, newNode);
//                    dictHier.putStringToId(newNode, strCount);
//                }
////                System.out.println("child "+strCount+" parent "+parentVal)
//            }
            h.add((double)newStrId, (double)parentId);
            
        }
        else{
            if(h.getHierarchyType().equals("range")){
                String del = "-";
                
                if (newNode.contains(" ")){
                    newNode = newNode.replaceAll(" ", "");
                }
                
                if (parent.contains(" ")){
                    parent = parent.replaceAll(" ", "");
                }
                
                String []tempNew = newNode.split(del);
                String []tempParent = parent.split(del);
                
                
                if(h.getNodesType().equals("date")){
                    RangeDate newNodeDate,parentNodeDate;
                    
                    if(newNode.equals("(null)"))
                        newNodeDate = new RangeDate(null,null);
                    else if(tempNew.length == 2)
                        newNodeDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempNew[0],true),((HierarchyImplRangesDate) h).getDateFromString(tempNew[1],false));
                    else
                        newNodeDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(newNode,true),((HierarchyImplRangesDate) h).getDateFromString(newNode,false));
                    
                    if(tempParent.length == 2)
                        parentNodeDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempParent[0],true),((HierarchyImplRangesDate) h).getDateFromString(tempParent[1],false));
                    else
                        parentNodeDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(parent,true),((HierarchyImplRangesDate) h).getDateFromString(parent,false));
                    
                    if(h.getParent(newNodeDate)!=null){
                        return "The node exists in hierarchy";
                    }
                    
                    h.add(newNodeDate, parentNodeDate);
                }
                else{
                    
                    RangeDouble newNodeRange = RangeDouble.parseRange(newNode);
//                    RangeDouble newNodeRange = new RangeDouble(Double.parseDouble(tempNew[0]),Double.parseDouble(tempNew[1]));
                    newNodeRange.setNodesType(h.getNodesType());
                    RangeDouble parentNodeRange = RangeDouble.parseRange(parent);
//                    RangeDouble parentNodeRange = new RangeDouble(Double.parseDouble(tempParent[0]),Double.parseDouble(tempParent[1]));
                    parentNodeRange.setNodesType(h.getNodesType());
                    
                    if(h.getParent(newNodeRange)!=null){
                        return "The node exists in hierarchy";
                    }
                
                    h.add(newNodeRange, parentNodeRange);
                }
            }
            else{
                //// TODO add check intdouble existance
                if(newNode.equals("(null)")){
                    if(h.getParent(Double.NaN)!=null || h.getParent(2147483646.0)!=null){
                        return "The node exists in hierarchy";
                    }
                    h.add(2147483646.0, Double.parseDouble(parent));
                }
                else{
                    if(h.getParent(Double.parseDouble(newNode))!=null){
                        return "The node exists in hierarchy";
                    }
                    h.add(Double.parseDouble(newNode), Double.parseDouble(parent));
                }
            }
        }
        
        return "ok";
         
    }
    
    @RequestMapping(value="/action/editnodehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String editNodeHier (@RequestParam("oldnode") String oldNode, @RequestParam("newnode") String newNode, @RequestParam("hiername") String hierName, HttpSession session) throws ParseException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        h = hierarchies.get(hierName);
        int newStrId,oldStrId;
        
        if ( h.getNodesType().equals("string")){
            
            if(newNode.trim().isEmpty()){
                newNode = "NaN";
            }
            
            if(oldNode.trim().equals("(null)")){
                oldNode = "NaN";
            }
//            if(h.getDictionary().containsString(oldNode)){
//                strId = h.getDictionary().getStringToId(oldNode);
//                h.getDictionary().update(strId, newNode);
//            }
//            else{
//                strId = h.getDictionaryData().getStringToId(oldNode);
//                h.getDictionaryData().update(strId, newNode);
//            }
            
            if(h.getDictionaryData().containsString(newNode)){
                newStrId = h.getDictionaryData().getStringToId(newNode);
                if(h.getParent((double)newStrId)!=null){
                    return "Value "+newNode+" is already exists in hierarchy";
                }
            }
            else if(h.getDictionary().containsString(newNode)){
                newStrId = h.getDictionary().getStringToId(newNode);
                if(h.getParent((double)newStrId)!=null){
                    return "Value "+newNode+" is already exists in hierarchy";
                }
            }
            else{
                if(h.getDictionaryData().isEmpty() && h.getDictionary().isEmpty()){
                    System.out.println("Edit Both empty edit");
                    newStrId = 1;
                }
                else if(!h.getDictionaryData().isEmpty() && !h.getDictionary().isEmpty()){
                    System.out.println("Both have values edit");
                    if(h.getDictionaryData().getMaxUsedId() > h.getDictionary().getMaxUsedId()){
                        newStrId = h.getDictionaryData().getMaxUsedId()+1;
                    }
                    else{
                        newStrId = h.getDictionary().getMaxUsedId()+1;
                    }
                }
                else if(h.getDictionaryData().isEmpty()){
                    System.out.println("Dict data empty edit");
                    newStrId = h.getDictionaryData().getMaxUsedId()+1;
                }
                else{
                    System.out.println("Dict hier empty edit");
                    newStrId = h.getDictionaryData().getMaxUsedId()+1;
                }
                
                h.getDictionary().putIdToString(newStrId, newNode);
                h.getDictionary().putStringToId(newNode, newStrId);
            }
            
            if(h.getDictionaryData().containsString(oldNode)){
                oldStrId = h.getDictionaryData().getStringToId(oldNode);
            }
            else{
                oldStrId = h.getDictionary().getStringToId(oldNode);
            }
            
            h.edit((double)oldStrId, (double)newStrId);
        }
        else{
            if(h.getHierarchyType().equals("range")){
                
                String del = "-";
                
                if (newNode.contains(" ")){
                    newNode = newNode.replaceAll(" ", "");
                }

                if (oldNode.contains(" ")){
                    oldNode = oldNode.replaceAll(" ", "");
                }

                String []tempNew = newNode.split(del);
                String []tempOld = oldNode.split(del);
                
                if(h.getNodesType().equals("date")){
//                    if(tempNew[0] || tempNew[1]){
//                        
//                    }
                    RangeDate newDateNode,oldDateNode;
                    
                    if(tempNew.length == 2)
                        newDateNode = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempNew[0],true), ((HierarchyImplRangesDate) h).getDateFromString(tempNew[1],false));
                    else
                        newDateNode = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(newNode,true), ((HierarchyImplRangesDate) h).getDateFromString(newNode,false));
                    
                    if(tempOld.length == 2)
                        oldDateNode = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempOld[0],true), ((HierarchyImplRangesDate) h).getDateFromString(tempOld[1],false));
                    else
                        oldDateNode = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(oldNode,true), ((HierarchyImplRangesDate) h).getDateFromString(oldNode,false));
                    
                    h.edit(oldDateNode, newDateNode);
                }
                else{ // range double
                    RangeDouble newNodeRange = RangeDouble.parseRange(newNode);
                    RangeDouble oldNodeRange = RangeDouble.parseRange(oldNode);
//                    RangeDouble newNodeRange = new RangeDouble(Double.parseDouble(tempNew[0]),Double.parseDouble(tempNew[1]));
//                    RangeDouble oldNodeRange = new RangeDouble(Double.parseDouble(tempOld[0]),Double.parseDouble(tempOld[1]));
                    newNodeRange.setNodesType(h.getNodesType());
                    oldNodeRange.setNodesType(h.getNodesType());


                    h.edit(oldNodeRange, newNodeRange);
                }
            }
            else{ // distinct
                //// TODO edit check intdouble existance 
                h.edit(Double.parseDouble(oldNode), Double.parseDouble(newNode));
            }
        }
        
        return "ok";
    }
    
    
    @RequestMapping(value="/action/deletenodehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String delNodeHier (@RequestParam("deletenode") String delnode,@RequestParam("hiername") String hierName, HttpSession session) throws ParseException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
       
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        h = hierarchies.get(hierName);
        
        if ( h.getNodesType().equals("string")){
            if(delnode.equals("(null)")){
                delnode = "NaN";
            }
            DictionaryString dict;
            if(h.getDictionary().containsString(delnode)){
                dict = h.getDictionary();
            }
            else{
                dict = h.getDictionaryData();
                
            }
            
            double nodeId = dict.getStringToId(delnode);
            Double root = (Double) h.getRoot();
            if(root == nodeId){
                return "You can not delete root";
            }
            h.remove(nodeId);
//            dict.remove((int) nodeId);
        }
        else{
            if(h.getHierarchyType().equals("range")){
                String del = "-";
                
                if (delnode.contains(" ")){
                    delnode = delnode.replaceAll(" ", "");
                }
                
                String []temp = delnode.split(del);
                
                if(h.getNodesType().equals("date")){
                    RangeDate delDate;
                    if(delnode.equals("(null)")){
                        delDate = new RangeDate(null,null);
                        
                    }
                    else if(temp.length == 2){
                        delDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(temp[0], true),((HierarchyImplRangesDate) h).getDateFromString(temp[1], false));
                    }
                    else{
                        delDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(delnode, true),((HierarchyImplRangesDate) h).getDateFromString(delnode, false));
                    }
                    
                    RangeDate root = (RangeDate) h.getRoot();
                    if(root.equals(delDate)){
                        return "You can not delete root";
                    }
                    h.remove(delDate);
                }
                else{
                    RangeDouble delRange = RangeDouble.parseRange(delnode);
//                    RangeDouble delRange = new RangeDouble(Double.parseDouble(temp[0]),Double.parseDouble(temp[1]));
                    RangeDouble root = (RangeDouble) h.getRoot();
                    if(root.equals(delRange)){
                        return "You can not delete root";
                    }
                    h.remove(delRange);
                }
            }
            else{
                Double delValue;
                if(delnode.equals("(null)")){
                    delValue = Double.NaN;
                    try{
                       h.remove(delValue); 
                    }catch(Exception e){
                        h.remove(2147483646.0);
                    }
                }
                else{
                    delValue = Double.parseDouble(delnode);
                    Double root = (Double) h.getRoot();
                    if(root.equals(delValue)){
                        return "You can not delete root";
                    }
                    h.remove(delValue);
                }
                
            }
        }
        
        return "OK";

    }
    
    @RequestMapping(value="/action/findsolutionstatistics", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String[] findSolutionStatistics ( HttpSession session) throws ParseException  {
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        String selectedNode = (String) session.getAttribute("selectednode");
        System.out.println("Selected Node "+selectedNode);
         Map<Integer, Set<String>> toSuppress = new HashMap<>();
        //Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
        String []attr = null;
        
        //System.out.println("selectedNode = " + selectedNode);
        int[] qids = new int[quasiIdentifiers.keySet().size()];
        int i = 0;
        for(Integer column : quasiIdentifiers.keySet()){
            qids[i] = column;
            i++;
        }

       
        //System.out.println("findSolutionStatistics////////////////////////////////////////" );
        

        FindSolutions sol = new FindSolutions(data, quasiIdentifiers, selectedNode,qids,toSuppress);

        Map<SolutionHeader, SolutionStatistics> solMap = sol.getSolutionStatistics();

        session.setAttribute("solutionstatistics",solMap);
        
        /*System.out.println("//////////////////////////////findSolutionStatistics///////////////////////////");
        System.out.println("solMap");
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
            Map<SolutionAnonValues, Integer> tempppp = entry.getValue().getDetails();
            for (Map.Entry<SolutionAnonValues, Integer> entry2 : tempppp.entrySet()){
                System.out.println(entry2.getKey() + "\t" + entry2.getValue());
            }
        }
        System.out.println("\n");
        System.out.println("//////////////////////////////////////////////////////////////////////////////");*/
        
        
        attr = new String[solMap.size()];
        i = 0;
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            SolutionStatistics s = entry.getValue();
            //s.print();
            attr[i] = (String)entry.getKey().toString();
            i++;
        }

        return attr;
        
    }
    
    
    @JsonView(View.Solutions.class)
    @RequestMapping(value="/action/getsolutionstatistics", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody SolutionsArrayList getSolutionStatistics (@RequestParam("selectedattributenames") String selectedAttrNames, HttpSession session)  {  
        
        Map<SolutionHeader, SolutionStatistics> solMap = null;
        Map<SolutionHeader, SolutionStatistics> solMapSuppress = (Map<SolutionHeader, SolutionStatistics>)session.getAttribute("solmapsuppress");
        SolutionStatistics solStat = null;
        Solutions sol = null;
        SolutionsArrayList solutions = null;
        Map<SolutionHeader, Set<String>> nonAnonymousValues = new HashMap<>();
        Map<SolutionHeader, Integer> nonAnonymizedCount = new LinkedHashMap<>();
        boolean suppress = false;
        Data dataset = (Data)session.getAttribute("data");
        String selectedAttr = null;
        String []temp = null;
        String del = ",";        
        int k = (int)session.getAttribute("k");
        boolean FLAG = false;
        Map<Integer, Set<String>> toSuppress;
        int []qids;
        Map<Integer, Hierarchy> quasiIdentifiers =  (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        int dataSizeSuppress = 0;
        
        
        String node = null;
        
        if (solMapSuppress == null){
            solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
        }
        else{
            solMap = solMapSuppress;
        }
        
        
        if (selectedAttrNames.contains(" ")){
            temp = selectedAttrNames.split(" ");
            qids = new int[temp.length];
            for( int i = 0 ; i < temp.length ; i ++){
                if (FLAG == false){
                    for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
                        if (entry.getKey().toString().equals(temp[i])){
                            SolutionHeader sol1 = entry.getKey();
                            node = sol1.getLevelsToString();
                        }
                    }
                    
                    selectedAttr = "" + dataset.getColumnByName(temp[i]);
                    FLAG  = true;
                }
                else{
                    
                    selectedAttr = selectedAttr + "," + dataset.getColumnByName(temp[i]);
                }
            }
        }
        else{
            for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
                if (entry.getKey().toString().equals(selectedAttrNames)){
                    SolutionHeader sol1 = entry.getKey();
                    node = sol1.getLevelsToString();
                }
            }
            selectedAttr = "" + dataset.getColumnByName(selectedAttrNames);
            qids = new int[1];
            qids[0] = Integer.parseInt(selectedAttr);
            //quasiIdentifiers.put(Integer.parseInt(selectedAttr), null);
        }
        
    
        session.setAttribute("pagenumsolution",0);
        session.setAttribute("selectedattributenames",selectedAttrNames);
        session.setAttribute("selectedattributes",selectedAttr);
        session.setAttribute("suppressSource",false);
        session.setAttribute("suppressSolution",true);
        toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
        
        
       // if ( toSuppress == null){
       
   
        dataSizeSuppress = 0;
        
        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
                solStat = entry.getValue();
                solStat.sort();         
                Set<String> setAnon = new HashSet<>();
                int count = 0;


                    for(SolutionAnonValues values : solStat.getKeyset()){


                        if ( selectedAttrNames.equals(entry.getKey().toString())){
                            dataSizeSuppress += solStat.getSupport(values);
                        }

                        if(solStat.getSupport(values) < k){
                            count += solStat.getSupport(values);
                            setAnon.add(values.toString());
                            if(entry.getKey().toString().equals(selectedAttrNames) && checkQuasi(dataset, quasiIdentifiers,selectedAttrNames)){
                                suppress = true;
                            }
                        }
                    }

                   

                    if(count != 0){
                        nonAnonymizedCount.put(entry.getKey(), count);
                        nonAnonymousValues.put(entry.getKey(), setAnon);
                        session.setAttribute("nonanonymousvalues",nonAnonymousValues);
                        session.setAttribute("nonanonymizedcount",nonAnonymizedCount);


                    }


        }           
        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
           
            if(entry.getKey().toString().equals(selectedAttrNames)){
                double percentageSuppress = 0.0;
                //System.out.println("hereeeeeeeeeee = " + nonAnonymizedCount.get(entry.getKey()));
                if (nonAnonymizedCount.get(entry.getKey()) != null){
                    percentageSuppress = ((double)nonAnonymizedCount.get(entry.getKey())/dataSizeSuppress) * 100;
 
                    DecimalFormat df = new DecimalFormat("#.##");      
                    percentageSuppress = Double.valueOf((df.format(percentageSuppress)).replaceAll(",", "."));
                }
                solStat = entry.getValue();
                
                //solStat.getSuppressPercentage();
                
                solStat.sort();
                
                solutions = solStat.getPage(0, 20, k);
                solutions.setPercentangeSuppress(percentageSuppress);
                solutions.setSuppress(suppress);
                
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////////////
        /*System.out.println("////////////////////////////////my print///////////////////////////////////////");
        
        SolutionStatistics s = null;
        
        System.out.println("toSuppress");
        //toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
        if (toSuppress != null){
            for (Map.Entry<Integer, Set<String>> entry : toSuppress.entrySet()) {
                System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
            }
        }
        System.out.println("\n");
        
        System.out.println("solMap");
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
            s = entry.getValue();
            for (Map.Entry<SolutionAnonValues, Integer> entry2 : s.getDetails().entrySet()){
                System.out.println(" key = " + entry2.getKey() + "\t value = " + entry2.getValue());
            }
        }
        System.out.println("\n");
        
        System.out.println("solMapSuppress");
        if ( solMapSuppress != null){
            for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMapSuppress.entrySet()) {
                System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
                s = entry.getValue();
                for (Map.Entry<SolutionAnonValues, Integer> entry2 : s.getDetails().entrySet()){
                    System.out.println(" key = " + entry2.getKey() + "\t value = " + entry2.getValue());
                }
            }
        }
        else{
            System.out.println("null");
        }
        System.out.println("\n");
        
        System.out.println("nonAnonymousValues");
        if (nonAnonymousValues != null){
            for (Map.Entry<SolutionHeader, Set<String>> entry : nonAnonymousValues.entrySet()) {
                System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
            }
        }
        else{
            System.out.println("null");
        }
        System.out.println("\n");
        
        System.out.println("nonAnonymizedCount");
        if(nonAnonymizedCount != null){
            for (Map.Entry<SolutionHeader, Integer> entry : nonAnonymizedCount.entrySet()) {
                System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
            }
        }
        else{
            System.out.println("null");
        }
        System.out.println("\n");
        
        
        System.out.println("//////////////////////////////////////////////////////////////////////////////");

        
        //System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww22222222222222222222222222222222222");*/

        return solutions;
        
    }
    
    
    
    
    /*@JsonView(View.Solutions.class)
    @RequestMapping(value="/action/suppress", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody SolutionsArrayList suppressValues ( HttpSession session)  {
        Map<SolutionHeader, Set<String>> nonAnonymousValues = (Map<SolutionHeader, Set<String>>)session.getAttribute("nonanonymousvalues");
        Map<Integer, Set<String>> toSuppress = new HashMap<>();
        Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>)session.getAttribute("solutionstatistics");
        SolutionHeader selectedHeader = null;
        String[] temp = null;
        String del = ",";
        int[] qids;
        String node = null;
        boolean FLAG = false;
        Map<Integer, Hierarchy> quasiIdentifiers = null;
        Data dataset = (Data) session.getAttribute("data");
        SolutionsArrayList solutions = null;
        
        System.out.println("SupresssValues//////////////////////////////////////////");
        String selectedAttrNames = (String)session.getAttribute("selectedattributenames");
        String selectedAttr = (String)session.getAttribute("selectedattributes");
        int k = (int)session.getAttribute("k");
        Graph graph = (Graph)session.getAttribute("graph");

        System.out.println("selected attr = " + selectedAttr);
        System.out.println("selected attr Names= " + selectedAttrNames);
        
        quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        
        
        
        //if ( quasiIdentifiers != null){
            qids = new int[quasiIdentifiers.keySet().size()];
            int i = 0;
            for(Integer column : quasiIdentifiers.keySet()){
                qids[i] = column;
                i++;
            }
            
        //}
        
        node = (String) session.getAttribute("selectednode");

        
        
        ////////////////////////////////////////////////////////////////////
        System.out.println("input Stringsssssssssss");
        System.out.println("selecteAttr = " + selectedAttr);
        System.out.println("node = " + node);
        System.out.println("quasiidentifiers = " + quasiIdentifiers);
        
        ///////////////////////////////////////////////////////////////////
        
        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {

            if(entry.getKey().toString().equals(selectedAttrNames)){
                selectedHeader = entry.getKey();
            }
        }
        
        System.out.println("node = " + node);
        
        int curQid;
        
        //if one column is selected, add non-anonymous values of this column
        if(selectedHeader.qids.length == 1){
            curQid = selectedHeader.qids[0];
            Set<String> s = nonAnonymousValues.get(selectedHeader);
            toSuppress.put(curQid, nonAnonymousValues.get(selectedHeader));
        }
        //else add non-anonymous values of all columns to be suppressed
        else{

            for(SolutionHeader header : nonAnonymousValues.keySet()){
               
                if(header.qids.length == 1)
                    curQid = header.qids[0];
                else
                    curQid = -1;

               
                toSuppress.put(curQid, nonAnonymousValues.get(header));
            }
        }
        
        
        
        FindSolutions solution = new FindSolutions(dataset, quasiIdentifiers, node,qids,toSuppress);
        solMap = solution.getSolutionStatistics();
        session.setAttribute("tosuppress",toSuppress);
        session.setAttribute("solmapsuppress",solMap);
        
        
        SolutionStatistics solStat = null;
        
        
        //////////////////////////////auto to thelw////////////////////////////////////////
        //for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
         //   System.out.println("solMap suppress header = " + entry.getKey());
          //  if(entry.getKey().toString().equals(selectedAttrNames)){
                
           //     solStat = entry.getValue();
                
                
           //     solStat.sort();
                //solStat.print();
            //    solutions = solStat.getPage(0, 100, k);
           // }
        //}
        ///////////////////////////////////////////////////////////////////////////////////
        //String strNames = null;
        //boolean FLAG2 = false;
        //for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
         //   if (FLAG2 == false){
       //         strNames = entry.getValue().getName();
       //         FLAG2 = true;
        //    }
        //    else{
        //        strNames += strNames + " " + entry.getValue().getName();
         //   }
            
        //}
        
        
       // solStat = solMap.get(strNames);
        //solStat.sort();
               
        //solutions = solStat.getPage(0, 100, k);
        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            System.out.println("solMap suppress header = " + entry.getKey());
            
             System.out.println("len = " + entry.getKey().qids.length);
            
            if(entry.getKey().toString().equals(selectedAttrNames)){
                
                solStat = entry.getValue();
               
                
                solStat.sort();
                //solStat.print();
                solutions = solStat.getPage(0, 100, k);
            }
        }
        
        
        System.out.println("/////////////////////////Supresss////////////////////////////////////////");

        System.out.println("toSuppress");
        for (Map.Entry<Integer, Set<String>> entry : toSuppress.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        
        System.out.println("solMap");
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            System.out.println(entry.getKey() + "\t dataLength = " + entry.getValue().getDataLength()+"\tpageLength = " + entry.getValue().getPageLength());
            Map<SolutionAnonValues, Integer> tempppp = entry.getValue().getDetails();
            for (Map.Entry<SolutionAnonValues, Integer> entry2 : tempppp.entrySet()){
                System.out.println(entry2.getKey() + "\t" + entry2.getValue());
            }
        }
        
        System.out.println("//////////////////////////////////////////////////////////////////////////////");
        
        
        return solutions;
        
    }*/
    
    
    @JsonView(View.Solutions.class)
    @RequestMapping(value="/action/suppress", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody SolutionsArrayList suppressValues ( HttpSession session) throws ParseException  {
        Map<SolutionHeader, Set<String>> nonAnonymousValues = (Map<SolutionHeader, Set<String>>)session.getAttribute("nonanonymousvalues");
        //Map<Integer, Set<String>> toSuppress = new HashMap<>();
        
        Map<Integer, Set<String>> toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
        if ( toSuppress == null ){
            toSuppress = new HashMap<>();
        }
        
        Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>)session.getAttribute("solutionstatistics");
        SolutionHeader selectedHeader = null;
        String[] temp = null;
        String del = ",";
        int[] qids = null;
        String node = null;
        boolean FLAG = false;
        Map<Integer, Hierarchy> quasiIdentifiers = null;
        Data dataset = (Data) session.getAttribute("data");
        SolutionsArrayList solutions = null;
        
//        System.out.println("SupresssValues//////////////////////////////////////////");
        String selectedAttrNames = (String)session.getAttribute("selectedattributenames");
        String selectedAttr = (String)session.getAttribute("selectedattributes");
        int k = (int)session.getAttribute("k");
        Graph graph = (Graph)session.getAttribute("graph");
 
        quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        
        
        
        if ( quasiIdentifiers != null){
            qids = new int[quasiIdentifiers.keySet().size()];
            int i = 0;
            for(Integer column : quasiIdentifiers.keySet()){
                qids[i] = column;
                i++;
            }
            
        }
        
        
        node = (String) session.getAttribute("selectednode");


        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {

            if(entry.getKey().toString().equals(selectedAttrNames)){
                selectedHeader = entry.getKey();
            }
        }

        
        int curQid = -1;
        
        //if one column is selected, add non-anonymous values of this column
        if(selectedHeader.qids.length == 1){
            curQid = selectedHeader.qids[0];
            Set<String> s = nonAnonymousValues.get(selectedHeader);
            toSuppress.put(curQid, nonAnonymousValues.get(selectedHeader));
        }
        //else add non-anonymous values of all columns to be suppressed
        else{

            for(SolutionHeader header : nonAnonymousValues.keySet()){
               
                if(header.qids.length == 1)
                    curQid = header.qids[0];
                else
                    curQid = -1;

               
                toSuppress.put(curQid, nonAnonymousValues.get(header));
            }
        }
        
        Set<String> suppressValues =  toSuppress.get(curQid);
        
        
        FindSolutions solution = new FindSolutions(dataset, quasiIdentifiers, node,qids,toSuppress);
        solMap = solution.getSolutionStatistics();
        System.out.println("create solMap "+solMap);
        session.setAttribute("tosuppress",toSuppress);
        
        session.setAttribute("solmapsuppress",solMap);
       
        
        
        /*SolutionStatistics s;
        System.out.println("solMap");
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            System.out.println(" key = " + entry.getKey() + "\t value = " + entry.getValue());
            s = entry.getValue();
            for (Map.Entry<SolutionAnonValues, Integer> entry2 : s.getDetails().entrySet()){
                System.out.println(" key = " + entry2.getKey() + "\t value = " + entry2.getValue());
            }
        }
        System.out.println("\n");*/
        
        
        
        SolutionStatistics solStat = null;
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
          
            if(entry.getKey().toString().equals(selectedAttrNames)){
                
                solStat = entry.getValue();
               
                
                solStat.sort();
                //solStat.print();
                solutions = solStat.getPage(0, 50, k);
            }
        }
        
//        System.out.println("//////endddddddddddddddddddd supressssss////////////////");
        
        
       /* System.out.println("/////////////////////////Supresss////////////////////////////////////////");

        System.out.println("toSuppress");
        for (Map.Entry<Integer, Set<String>> entry : toSuppress.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        
        System.out.println("solMap");
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            System.out.println(entry.getKey() + "\t dataLength = " + entry.getValue().getDataLength()+"\tpageLength = " + entry.getValue().getPageLength());
            Map<SolutionAnonValues, Integer> tempppp = entry.getValue().getDetails();
            for (Map.Entry<SolutionAnonValues, Integer> entry2 : tempppp.entrySet()){
                System.out.println(entry2.getKey() + "\t" + entry2.getValue());
            }
        }
        
        System.out.println("//////////////////////////////////////////////////////////////////////////////");*/
        
        
        return solutions;
        
    }
    
   
    @JsonView(View.Solutions.class)
    @RequestMapping(value="/action/getsourcestatistics", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody SolutionsArrayList getSourceStatistics (@RequestParam("kcheck") String k, @RequestParam("selectedattribute") String selectedAttr, @RequestParam("selectedattributenames") String selectedAttrNames, HttpSession session) throws ParseException  {
        Data dataset = (Data) session.getAttribute("data");
         Map<Integer, Set<String>> toSuppress = new HashMap<>();
         boolean suppress = false;
         
        
//        System.out.println("source statistics");
        
        String[] temp = null;
        String del = ",";
        int[] qids;
        String node = null;
        boolean FLAG = false;
        Map<Integer, Hierarchy> quasiIdentifiers = new HashMap<Integer, Hierarchy>();
        SolutionsArrayList solutionsArr = null;
        Map<SolutionHeader, Set<String>> nonAnonymousValues = new HashMap<>();
        Map<SolutionHeader, Integer> nonAnonymizedCount = new LinkedHashMap<>();
        int dataSizeSuppress = 0;
        
        
        if (selectedAttr.contains(",")){
            temp = selectedAttr.split(del);
            qids = new int[temp.length];
            for(int i = 0 ; i < temp.length ; i++){
                if( FLAG == false ){
                    node = "0";
                    FLAG = true;
                }
                else{
                    node = node + ",0";
                }
                qids[i] = Integer.parseInt(temp[i]);
                quasiIdentifiers.put(Integer.parseInt(temp[i]), null);
                selectedAttrNames = selectedAttrNames.replaceAll(",", " ");
            }
        }
        else{
            qids = new int[1];
            qids[0] = Integer.parseInt(selectedAttr);
            node = "0";
            quasiIdentifiers.put(Integer.parseInt(selectedAttr), null);
            
        }

        FindSolutions solution = new FindSolutions(dataset, quasiIdentifiers, node,qids,toSuppress);
        Map<SolutionHeader, SolutionStatistics> solMap = solution.getSolutionStatistics();
        session.setAttribute("pagenumsolution",0);
        SolutionStatistics solStat = null;
        Solutions sol = null;
        session.setAttribute("selectedattributenames",selectedAttrNames);
        // session.setAttribute("quasiIdentifiers",quasiIdentifiers);
        session.setAttribute("quasiIdentifiers", quasiIdentifiers);
        session.setAttribute("selectedattributes",selectedAttr);
        session.setAttribute("suppressSource",true);
        session.setAttribute("suppressSolution",false);
        int intK = Integer.parseInt(k);
        session.setAttribute("k",intK);
        
        session.setAttribute("solutionstatistics",solMap);
        session.setAttribute("selectednode",node);
       
        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
                solStat = entry.getValue();
                solStat.sort();
                //solutionsArr = solStat.getPage(0, 100);          
                Set<String> setAnon = new HashSet<>();
                int count = 0;       
                for(SolutionAnonValues values : solStat.getKeyset()){         
                    //System.out.println("sol = " + values.toString() + "\t support = " + solStat.getSupport(values)+ "\tk =" + k );
                    
                   if ( selectedAttrNames.equals(entry.getKey().toString())){
                        dataSizeSuppress += solStat.getSupport(values);
                    }
                    
                    if(solStat.getSupport(values) < Integer.parseInt(k)){
                        count += solStat.getSupport(values);
                        setAnon.add(values.toString());
                        suppress = true;
                        //dataSizeSuppress += solStat.getSupport(values);
                    }
                }
                
                
                
                if(count != 0){
                    nonAnonymizedCount.put(entry.getKey(), count);
                    nonAnonymousValues.put(entry.getKey(), setAnon);
                    session.setAttribute("nonanonymousvalues",nonAnonymousValues);
                    session.setAttribute("nonanonymizedcount",nonAnonymizedCount);
                    
                }
        }
        
        
        for (Map.Entry<SolutionHeader, SolutionStatistics> entry : solMap.entrySet()) {
            if(entry.getKey().toString().equals(selectedAttrNames)){
                double percentageSuppress = 0.0;
                //System.out.println("hereeeeeeeeeee = " + nonAnonymizedCount.get(entry.getKey()));
                if (nonAnonymizedCount.get(entry.getKey()) != null){
                    percentageSuppress = ((double)nonAnonymizedCount.get(entry.getKey())/dataSizeSuppress) * 100;
 
                    DecimalFormat df = new DecimalFormat("#.##");  
                    percentageSuppress = Double.valueOf((df.format(percentageSuppress)).replaceAll(",", "."));
                }
                solStat = entry.getValue();
                
                //solStat.getSuppressPercentage();
                
                solStat.sort();
                
                solutionsArr = solStat.getPage(0, 20, intK);
                solutionsArr.setPercentangeSuppress(percentageSuppress);
                solutionsArr.setSuppress(suppress);
                
            }
        }
    

        return solutionsArr;
        
    }
    
    
    @RequestMapping(value="/action/getdashboard", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String getdashboard ( HttpSession session)  {
       String str = null;
       Data data = (Data) session.getAttribute("data");
       Map<String, Hierarchy> hierarchies  = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
       Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
       String selectedNode = (String)session.getAttribute("selectednode");
       
       if (data != null){
           if (hierarchies != null){
               if ( quasiIdentifiers != null){
                   if ( selectedNode != null){
                       str ="data_hier_algo_solution";
                   }
                   else{
                    str = "data_hier_algo";
                   }
               }
               else{
                str = "data_hier";
               }
               
           }
           else{
               str = "data";
           }
       }
       else {
           str = "null";
       }
        
       return str;
    }
    
    @RequestMapping(value="/action/checkdataset", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody int checkDataset ( @RequestParam("attributes") String attributes ,HttpSession session)  {
        Data data = (Data) session.getAttribute("data");
        Set<Integer> sQids = new HashSet<Integer>();
//        System.out.println("Attribute = " + attributes);
        if (!attributes.equals("")){
            if (attributes.contains(",")){
                String []temp = null;
                temp = attributes.split(",");
                for ( int i = 0 ; i < temp.length ; i++){
                    sQids.add(Integer.parseInt(temp[i]));
                }
            }
            else{
                 sQids.add(Integer.parseInt(attributes));
            }
        }
        
        
        CheckDatasetForKAnomymous check = new CheckDatasetForKAnomymous(data);
        int k = check.compute(sQids);
        
//        System.out.println("k = " + k);
       

        return k;
        
    }
    
    
    @RequestMapping(value="/action/deletesuppress", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void deleteSuppress ( HttpSession session)  {  
        if (session.getAttribute("tosuppress") != null){
            session.removeAttribute("tosuppress");
        }
        if (session.getAttribute("solmapsuppress") != null){
            session.removeAttribute("solmapsuppress");
        }
        if (session.getAttribute("anondata") != null){
            session.removeAttribute("anondata");
        }
        
        if (session.getAttribute("selectednode")!= null){
            session.removeAttribute("selectednode");
        }
    }
    
    
    
    @RequestMapping(value="/action/restart", method = RequestMethod.POST) //method = RequestMethod.POST
    public static @ResponseBody void restart ( HttpSession session) {
        try{
            String inputPath = (String) session.getAttribute("inputpath");
            int index = inputPath.lastIndexOf(File.separator);

            if(os.equals("online")){
                File  dir = new File(inputPath.substring(0,index));
                File dir2 = new File(inputPath);

                FileUtils.cleanDirectory(dir2);
                for (File file: dir.listFiles()) {
                    if(file.getName().equals(session.getId())){
                        FileUtils.forceDelete(file);
                        break;
                    }
                }

            }
            
            HierarchyImplString.setWholeDictionary(null);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Enumeration<String> allAttributes = session.getAttributeNames();
        while ( allAttributes.hasMoreElements()){
            String attrName = (String)  allAttributes.nextElement();
            session.removeAttribute(attrName);
        }
    
        System.gc();
        
        
        
    }
    
    @RequestMapping(value="/action/deletedataset", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void deleteDataset ( HttpSession session)  {
        session.removeAttribute("filename");
        session.removeAttribute("data");
        session.removeAttribute("inputpath");
        session.removeAttribute("anondata");
        session.removeAttribute("selectednode");
    }
    

    @RequestMapping(value="/action/deletehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void deleteHier ( HttpSession session)  {
        session.removeAttribute("hierarchies");
        session.removeAttribute("selectedhier");
    }
    
    
    
    
     
    @RequestMapping(value="/action/getcolumnnamesandtypes", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ArrayList<ColumnsNamesAndTypes> getColumnNamesAndTypes ( HttpSession session ) throws FileNotFoundException, IOException {
        Data data = (Data) session.getAttribute("data");
        //Map<String, Hierarchy> hierarchies  = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        ArrayList<ColumnsNamesAndTypes> colTypeList = new ArrayList<ColumnsNamesAndTypes>();
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Hierarchy h = null;
        ColumnsNamesAndTypes colNamesTypes = null;
//        System.out.println("dimitris");
        for (Map.Entry<Integer,String> entry : data.getColNamesPosition().entrySet()) {
            String columnName = entry.getValue();
            h = quasiIdentifiers.get(entry.getKey());
            if ( h != null){
                colNamesTypes = new ColumnsNamesAndTypes(columnName,h.getHierarchyType());
            }
            else{
                colNamesTypes = new ColumnsNamesAndTypes(columnName,"distinct");
            }
            colTypeList.add(colNamesTypes);
        }

        
        return colTypeList;
        
    }
    
    
    @RequestMapping(value="/action/gethiernamesandlevels", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ArrayList<HierarchiesAndLevels> getHierNamesAndlevels ( HttpSession session ) throws FileNotFoundException, IOException {
        ArrayList<HierarchiesAndLevels> hiersAndLevels = new ArrayList<HierarchiesAndLevels>();
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Hierarchy h = null;
        HierarchiesAndLevels hiersAndlevels = null;
        
        for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
            h = (Hierarchy)entry.getValue();
            hiersAndlevels = new HierarchiesAndLevels(h.getName(),h.getHeight() +"");
            hiersAndLevels.add(hiersAndlevels);
        }

        
        return hiersAndLevels;
        
    }
    
    @RequestMapping(value="/action/createdatabase", method = RequestMethod.GET)
    public @ResponseBody String createDatabase(@RequestParam("name") String name, HttpSession session) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        Class.forName("org.sqlite.JDBC").newInstance();
//        String rootPath = System.getProperty("catalina.home");
//        String rootPath = "/usr/local/apache-tomcat-8.0.15";
//                String rootPath = "/var/lib/tomcat8";
        String url = "jdbc:sqlite:"+rootPath+File.separator+name;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        return "Database created";
    }
    
    @RequestMapping(value="/action/checkanonymity", method = RequestMethod.GET)
    public @ResponseBody String checkAnonymity( @RequestParam("k") Integer k, @RequestParam("m") Integer m, @RequestParam(value = "cols", required=false) String columns, HttpSession session){
        Data data = (Data) session.getAttribute("data");
        MixedApriori alg = new MixedApriori();
        alg.setDictionary(data.getDictionary());
        if(!(columns==null || columns.equals(""))){
            String[] strcols  = columns.split(",");
            List<Integer> columnsCheck = new ArrayList<Integer>();
            for(String col : strcols){
                columnsCheck.add(Integer.parseInt(col));
            }
            
            
            alg.setDataTable(data.getDataSet());
//            System.out.println("Oxi set "+Arrays.toString(((RelSetData) data).getSet()));
            if(data instanceof RelSetData){
                alg.setSetData(((RelSetData) data).getSet());
                return alg.checkAnonymity(k, m, columnsCheck,((RelSetData) data).getSetColumn());
            }
            else{
                return alg.checkAnonymity(k, m, columnsCheck,-1);
            }
        }
        else{
           alg.setSetData(data.getDataSet());
           return alg.checkAnonymitySet(k, m);
        }
    }
    
    @RequestMapping(value="/action/getresultsstatisticsqueries", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody double[] getResultsStatisticsQueries ( @RequestParam("identifiers[]") String []identifiersArray, @RequestParam("min[]") String []minArray, @RequestParam("max[]") String []maxArray, @RequestParam("distinct[]") String []distinctArr , HttpSession session ) throws FileNotFoundException, IOException {
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        String [] identifiersArr = new String[identifiersArray.length];
        double []minArr = new double[identifiersArray.length];
        double []maxArr = new double[identifiersArray.length];
        AnonymizedDataset anonData = (AnonymizedDataset)session.getAttribute("anondata");
        ArrayList<ResultsToJson> resultList = new ArrayList<ResultsToJson>(); 
        ResultsToJson resultsToJson = null;
        double[] resultArr = null;
        
        
//        System.out.println("Identifiers");
        for ( int i = 0 ; i < identifiersArray.length ; i ++){
//            System.out.println(identifiersArray[i]);
            if (identifiersArray[i].equals("null")){
//                System.out.println("xaxaxaxaxaxaxa");
                identifiersArr[i] = null;
            }
            else{
                identifiersArr[i] = identifiersArray[i];
            }
        }
        
//        System.out.println("MinArr");
        for ( int i = 0 ; i < minArray.length ; i ++){
//            System.out.println(minArray[i]);
            if ( minArray[i].equals("null")){
                minArr[i] = Double.NaN;
            }
            else{
                minArr[i] = Double.parseDouble(minArray[i]);
            }
        }
        
//        System.out.println("MaxArr");
        for ( int i = 0 ; i < maxArray.length ; i ++){
//            System.out.println(maxArray[i]);
            if ( maxArray[i].equals("null")){
                maxArr[i] = Double.NaN;
            }
            else{
                maxArr[i] = Double.parseDouble(maxArray[i]);
            }
        }
        
        
//        System.out.println("distinct");
        for ( int i = 0 ; i < distinctArr.length ; i ++){
            if (distinctArr[i].equals("null")){
                distinctArr[i] = null;
            }
//            System.out.println(distinctArr[i]);
        }
        
//        System.out.println("anon" );
        int []level = anonData.getHierarchyLevel();
//        for( int  i = 0 ;i < level.length ; i ++){
//            System.out.println(level[i]);
//        }
        
        Queries queries = new Queries(identifiersArr, minArr, maxArr , distinctArr, hierarchies, data, anonData.getHierarchyLevel(), quasiIdentifiers);
        Results results = queries.executeQueries();
        
        resultArr = new double[4];
        resultArr[0] = Double.parseDouble(results.getNonAnonymizeOccurrences());
        resultArr[1] = Double.parseDouble(results.getAnonymizedOccurrences());
        resultArr[2] = Double.parseDouble(results.getPossibleOccurences());
        try{
            resultArr[3] = Double.parseDouble(results.getEstimatedRate());
        }catch(Exception e){
            resultArr[3] = Double.parseDouble(results.getEstimatedRate().split(",")[0]);
        }
        
        /*resultsToJson = new ResultsToJson("nonAnonymizeOccurrences",results.getNonAnonymizeOccurrences());
        resultList.add(resultsToJson);
        resultsToJson = new ResultsToJson("anonymizedOccurrences",results.getAnonymizedOccurrences());
        resultList.add(resultsToJson);
        resultsToJson = new ResultsToJson("possibleOccurences",results.getPossibleOccurences());
        resultList.add(resultsToJson);
        resultsToJson = new ResultsToJson("estimatedRate",results.getEstimatedRate());
        resultList.add(resultsToJson);*/
        
        return resultArr;
        
    }
    
    
    
    @RequestMapping(value="/action/getproperalgorithm", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String getProperAlgorithm ( HttpSession session ) throws FileNotFoundException, IOException {
        Data data = (Data) session.getAttribute("data");
        Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        if(hierarchies != null){
            for(Hierarchy h : hierarchies.values()){
                if(h.getHierarchyType().contains("demographic")){
                    return "demographic";
                }
            }
        }
        
        
        if (data.getClass().toString().contains("SET")){
            return "set";
        }
        else if(data instanceof RelSetData){
            return "relset";
        }
        else if(data instanceof DiskData){
            return "disk";
        }
        else{
            return"txt";
        }
        
    }
    
    @RequestMapping(value="/action/getsetvaluedcolumn", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String getSetValuedColumn ( HttpSession session ) throws FileNotFoundException, IOException {
        Data data = (Data) session.getAttribute("data");
        
        if(data instanceof RelSetData){
            RelSetData relsetdata = (RelSetData) data;
            return data.getColNamesPosition().get(relsetdata.getSetColumn())+"_"+relsetdata.getSetColumn();
        }
        else{
            return "none";
        }
        
    }
    
    
    @RequestMapping(value="/action/savenonymizationrules") //method = RequestMethod.POST
    public @ResponseBody String saveAnonynizationRules ( HttpSession session, HttpServletResponse response ) throws FileNotFoundException, IOException, ParseException {
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        String selectedNode = null;
        String filename = (String)session.getAttribute("filename");
        
        if(filename.endsWith(".dcm")){
            filename = "anonymized_rules_dicom_files.txt";
        }
        String inputPath = (String)session.getAttribute("inputpath");
//        System.out.println("Filename :"+filename+" "+filename.endsWith("xml") +" "+(filename.endsWith("xml") ? filename.replace("xml", "txt") : filename));
        String file = inputPath +File.separator +"anonymized_rules_"+(filename.endsWith("xml") ? filename.replace("xml", "txt") : filename);
        
        this.createInputPath(inputPath, session);
        
        AnonymizationRules anonRules = new AnonymizationRules();
        if(data instanceof SETData){
            Map<Double, Double> results = (Map<Double, Double>) session.getAttribute("results");    
            anonRules.export(file, data, results, quasiIdentifiers);
        }
        else if(data instanceof RelSetData ){
            Map<Integer, Map<Object,Object>> results = (Map<Integer,Map<Object,Object>>) session.getAttribute("results"); 
            anonRules.exportRelSet(file, data, results, quasiIdentifiers);
        }
        else{
            int []qids = new int[quasiIdentifiers.keySet().size()];
            int i = 0;
            for(Integer column : quasiIdentifiers.keySet()){
                qids[i] = column;
                i++;
            }
            
            selectedNode = (String)session.getAttribute("selectednode");
            Map<Integer, Set<String>> toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
            anonRules.export(file, data, qids, toSuppress,quasiIdentifiers,selectedNode);
        }
        File anonFile = new File(file);
//        if(!anonFile.exists()){
//            anonFile.mkdirs();
            anonFile.createNewFile();
//        }
        
        InputStream myStream = new FileInputStream(file);

	// Set the content type and attachment header.
	response.addHeader("Content-disposition", "attachment;filename="+anonFile.getName());
	response.setContentType("txt/plain");

	// Copy the stream to the response's output stream.
	IOUtils.copy(myStream, response.getOutputStream());
	response.flushBuffer();
        
        if(os.equals("online")){
            this.deleteFiles(session);
        }
        
        return null;
    }
    
    
    @RequestMapping(value="/action/loadanonymizationrules", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loadAnonynizationRules ( HttpSession session , String filename) throws FileNotFoundException, IOException {
//        System.out.println("load anon rules");
        try{
        Data data = (Data) session.getAttribute("data");
//        System.out.println("fileName = " + filename);
        //String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        this.createInputPath(inputPath, session);
        //String anonRulesFile = inputPath +"/anonymized_rules_"+filename;
        String anonRulesFile = inputPath + File.separator +filename;
        AnonymizationRules anonRules = new AnonymizationRules();
        
//        System.out.println("inputPath = " + inputPath);
        
        if(!anonRules.importRules(anonRulesFile)){
            return "File structure not supported for anonymization rules!";
        }
        Map<String, Map<String, String>> rules = anonRules.getAnonymizedRules();
              
        
       
        
//        for (Map.Entry<String, Map<String, String>> entry : rules.entrySet()) {
//            System.out.println(entry.getKey()+" : "+entry.getValue());
//        }
        
        session.setAttribute("anonrules",rules);
        
        if(os.equals("online")){
            this.deleteFiles(session);
        }
        
        
        /*if(data instanceof SETData){
            this.anonymizedDatasetPanel2.anonymizeSETWithImportedRules(anonyRules.getAnonymizedRules());
        }
        else{
            this.anonymizedDatasetPanel2.anonymizeWithImportedRules(anonyRules.getAnonymizedRules());
        }*/
        
                
        
            return null;
        }catch(Exception e){
            return "Problem with loading anonymization rules "+e.getMessage();
        }
    }
    
    //@JsonView(View.DatasetsExists.class)
    @RequestMapping(value="/action/checkdatasetsexistence", produces = "application/json")//, method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody DatasetsExistence CheckDatasetsExistence ( HttpSession session  ) throws FileNotFoundException, IOException {
        
        DatasetsExistence check =  new DatasetsExistence();
        Data data = (Data) session.getAttribute("data");
        String selectednode = (String) session.getAttribute("selectednode");
        Graph graph = (Graph) session.getAttribute("graph");
        Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
        Map<Integer, Set<String>> toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
        Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
       
        
        String algorithm = (String) session.getAttribute("algorithm");
//        System.out.println("algorithmmmmmmmmmmm = " + algorithm);
        
//        System.out.println("checkdatasetsexistence");
        check.setAlgorithm(algorithm);
        check.setDiskData(data);
        if(data!=null && allRules!=null){
            check.setOriginalExists("true");
            check.setAnonExists("true");
        }
        else if(data!=null && algorithm==null){
            check.setOriginalExists("true");
            check.setAnonExists("noalgo");
        }
        else if(data!=null && hierarchies!=null && algorithm!=null && (algorithm.equals("kmAnonymity") || algorithm.equals("apriori") || algorithm.equals("AprioriShort") || algorithm.equals("clustering")) || algorithm.equals("demographic") || algorithm.equals("dp") && selectednode==null){
            check.setOriginalExists("true");
            check.setAnonExists("true");
        }
        else if(data!=null && algorithm!=null && selectednode==null){
            check.setOriginalExists("true");
        }
        else if(data!=null && algorithm!=null && selectednode!=null){
            check.setOriginalExists("true");
            check.setAnonExists("true");
        }

        
        return check;
    }
    
    @RequestMapping(value="/action/highlightsteps", method = RequestMethod.POST, produces = "application/json")//, method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody JSONObject checkGeneralExistance ( HttpSession session  ) throws FileNotFoundException, IOException {
        JSONObject jdata = new JSONObject();
        try{
            Data data = (Data) session.getAttribute("data");
            Graph graph = (Graph) session.getAttribute("graph");
            Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
            String algorithm = (String) session.getAttribute("algorithm");
            AnonymizedDataset anonData = (AnonymizedDataset)session.getAttribute("anondata");
            
            jdata.put("Status", "SUCCESS");
            jdata.put("data", data!=null?1:0);
            jdata.put("graph", graph!=null?1:0);
            jdata.put("hier", hierarchies!=null?1:0);
            jdata.put("algo", algorithm!=null?1:0);
            jdata.put("anonData", anonData!=null?1:0);
        }catch(Exception e){
            e.printStackTrace();
            jdata.put("Status", "FAIL");
            jdata.put("Response", "Problem with highlight steps' service");
            
        }
        

        
        return jdata;
    }
    
    
    
    
    /*@RequestMapping(value="/action/cleanusedata", produces = "application/json")//, method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void CleanUserData ( HttpSession session ) throws FileNotFoundException, IOException {
        String rootPath = (String)session.getAttribute("inputpath");

        if ( rootPath != null){
            File file = new File(rootPath);
            delete(file);
        }
        System.out.println("all cleannnnnnnn root = " + rootPath);
    }*/
    
    ///////////////////////////////api with template   /////////////////////////////////////////////////////
    
    @RequestMapping(value="/anonymizedata", produces = MediaType.TEXT_PLAIN, method = RequestMethod.POST)
    public void AnonimizeData(@RequestParam("files") MultipartFile[] files, @RequestParam("del") String del,  MultipartHttpServletRequest request,  HttpSession session, HttpServletResponse response) throws IOException, FileNotFoundException, ParseException, Exception{
        try{
        this.upload(files[0], true, session);
        String errorMessage;
        String path = (String) session.getAttribute("inputpath");
        String filename = (String) session.getAttribute("filename");
        File templateFile=null;
        
        
        if(del.equals("s")){
            del = ";";
        }
//        System.out.println("Del fiuwtyfuirwfgyrefgweygfy: "+del);
 
        if(files.length==1 || files[1]==null){
            
           
                Data dataset = this.getSmallDataSet(del, "tabular","", session);
                String[][] types = dataset.getTypesOfVariables(dataset.getSmallDataSet());
                templateFile = new File(path+File.separator+"template.txt");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(templateFile.getAbsolutePath(), true), StandardCharsets.UTF_8));

                File dataFile = new File(path+File.separator+filename);
                FileInputStream fileStream = new FileInputStream(dataFile);
                DataInputStream inData = new DataInputStream(fileStream);
                BufferedReader br = new BufferedReader(new  InputStreamReader(inData,StandardCharsets.UTF_8));
                
                if(!filename.endsWith(".xml")){
                    String firstLine = br.readLine();
                    br.close();

                    String splitLine[] = firstLine.split(del);
                    out.write("////////////////////// check columns, vartypes /////////////////////////////");
                    out.newLine();
                    for(int i=0; i<splitLine.length; i++){
                        out.write(splitLine[i]+": true,"+types[i][0].replace("double", "decimal"));
                        out.newLine();

                    }
                    out.write("//////////////////// END ////////////////////////////////////////////\n\n");
                    out.write("\n" +
                              "/////////////////// set k /////////////////////////////////////\n\n"+
                              "k:\n");
                    out.close();
                }
                else{
                    XMLData xmldata = (XMLData) dataset;
                    String[] names = xmldata.getColumnNames();
                    out.write("////////////////////// check columns, vartypes /////////////////////////////");
                    out.newLine();
                    for(int i=0; i<names.length; i++){
                       out.write(names[i]+": true,"+types[i][0].replace("double", "decimal"));
                       out.newLine();
                    }
                    out.write("//////////////////// END ////////////////////////////////////////////\n\n");
                    out.write("\n" +
                              "/////////////////// set k /////////////////////////////////////\n\n"+
                              "k:\n");
                    out.close();
                }

            InputStream in = new FileInputStream(templateFile);
            FileCopyUtils.copy(in, response.getOutputStream());
            
          
           
        }
        else{
           
           
           for(int i=2; i<files.length; i++){
               this.hierarchy(files[i], false, session);
           }
           
           this.upload(files[1], false, session);
           this.upload(files[0], false, session);
           File templ = new File(path+File.separator+files[1].getOriginalFilename());
           FileInputStream fileStream = new FileInputStream(templ);
           DataInputStream inData = new DataInputStream(fileStream);
           BufferedReader br = new BufferedReader(new  InputStreamReader(inData,StandardCharsets.UTF_8));
           
           String strline; 
           List<String> vartypesArr,relationsArr;
           List<Boolean> checkColumnsArr;
           int k=0;
           
           vartypesArr = new ArrayList<String>();
           relationsArr = new ArrayList<String>();
           checkColumnsArr = new ArrayList<Boolean>();
           
           ArrayList<String> possibleTypes = new ArrayList(){{ add("int"); add("decimal"); add("date"); add("string"); }};
           String error_msg="";
           
           while((strline = br.readLine()) != null){
               if(strline.contains("END") || strline.length()<=1){
                   continue;
               }
               else if(strline.contains("check columns, vartypes")){
                   while(!(strline = br.readLine()).contains("END")){
                       String columnInfo[] = strline.split(":");
                       String attributes[] = columnInfo[1].replaceAll("\n", "").replaceAll(" ", "").split(",");
                       
//                       System.out.println("length attr: "+attributes.length);
                       if(attributes.length>1 && attributes.length<=3){
                           if(!attributes[0].equals("true") && !attributes[0].equals("false")){
                               error_msg += "In "+columnInfo[0]+": bollean type must be true or false.\n";
                           }                           
                           checkColumnsArr.add ( attributes[0].equals("true"));
                           
                           if(!possibleTypes.contains(attributes[1])){
                              error_msg += "In "+columnInfo[0]+": not accepted variable type. It must be one of the "+Arrays.toString(possibleTypes.toArray())+"\n"; 
                           }
                           vartypesArr.add(attributes[1].replace("decimal", "double")); 
                           
                           if(attributes.length==3){
                               relationsArr.add( attributes[2] );
                           }
                           else{
                               relationsArr.add("");
                           }
                       }
                       else{
                           error_msg += "In "+columnInfo[0]+":  missing boolean type or the variable type of the column.\n";
                        }
                   }
               }
               else if(strline.contains("set k")){
                   while(!(strline = br.readLine()).contains("k:"));
                   String splits[] = strline.split(":");
                   try
                   {
                       k = Integer.parseInt(splits[1].replaceAll(" ", ""));
                   }catch(NumberFormatException | NullPointerException nfe){
                       error_msg += "k is not set or its not a number.\n";
//                       response.getOutputStream().println("k is not set or its not a number.");
//                       return;
                   }
               }
               
               
            }
            
            if(!error_msg.isEmpty()){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().println(error_msg);
                return;
            }
            
//            System.out.println("Results: k->"+k+" vartypes-> "+Arrays.toString(vartypesArr.toArray(new String[vartypesArr.size()]))+" checkColumns-> "+Arrays.toString(checkColumnsArr.toArray())+" relations-> "+Arrays.toString(relationsArr.toArray()));
            this.getSmallDataSet(del, "tabular","", session);
            boolean [] checkColumns = new boolean[checkColumnsArr.size()];
            for(int i=0; i<checkColumns.length; i++){
                checkColumns[i] = checkColumnsArr.get(i);
            }
            this.loadDataset(vartypesArr.toArray(new String[vartypesArr.size()]), checkColumns, session);

           
            this.anonymize(k, 0, "pFlash", relationsArr.toArray(new String[relationsArr.size()]), session);
            String solution = this.InformationLoss(session);
            this.setSelectedNode(session, solution);
            this.getAnonDataSet(0, 0, session);
            this.saveAnonymizeDataset(session, response);
            
        }

        response.flushBuffer();
        if(os.equals("online")){
            this.deleteFiles(session);
        }
        
        }catch(Exception e){
            e.printStackTrace();
            this.errorHandling(e.getLocalizedMessage(), session);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().println("Failed anonymization procedure!");
        }
       
    }
    
/////////////////////////////////////Expose additional ReST API//////////////////////////////////////////////////////////////////////
    @RequestMapping(value="/getSession",  method = RequestMethod.POST)
    public void getSession(HttpSession session, HttpServletResponse response) throws IOException{
        try{
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("Session_Id",session.getId());
            response.getOutputStream().print(jsonAnswer.toString());
        }catch(Exception e){
            e.printStackTrace();
            this.errorHandling(e.getLocalizedMessage(), session);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Failed to send session id, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/loadData",  method = RequestMethod.POST)
    public void loadData(@RequestParam("file") MultipartFile file, @RequestParam("datasetType") String datasetType, @RequestParam("del") String del,
            @RequestParam(value = "columnsType") String dataTypes,  @RequestParam(value = "delSet",required = false) String delset, HttpSession session, HttpServletResponse response) throws IOException {
        
        try{
            this.upload(file, true, session);
            this.getSmallDataSet(del, datasetType,delset, session);
            Data data = (Data) session.getAttribute("data");
            System.out.println("length "+data.getSmallDataSet()[0].length);
            boolean[] checkColumns = new boolean[data.getSmallDataSet()[0].length];
            String[] vartypes = new String[data.getSmallDataSet()[0].length];
            Map<String,String> datatypesMap = jsonToMap(dataTypes);
            for(int i=0; i<checkColumns.length; i++){
                if(datatypesMap.containsKey(data.getColumnNames()[i])){
                    checkColumns[i] = true;
                    String type = datatypesMap.get(data.getColumnNames()[i]);
                    if(type.equals("int") || type.equals("double") || type.equals("decimal") || type.equals("set") || type.equals("string") || type.equals("date")){
                        vartypes[i] = datatypesMap.get(data.getColumnNames()[i]).replace("decimal", "double");
                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","Unsupported data type "+type);
                        response.getOutputStream().print(jsonAnswer.toString());
                        return;
                    }
                }
                else{
                    checkColumns[i] = false;
                    vartypes[i] = null;
                }
                
            }
            System.out.println("checkColumns "+Arrays.toString(checkColumns));
            this.loadDataset(vartypes, checkColumns, session);
        }catch(Exception e){
            e.printStackTrace();
            this.errorHandling(e.getLocalizedMessage(), session);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Failed load dataset, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        JSONObject jsonAnswer = new JSONObject();
        jsonAnswer.put("Status","Success");
        jsonAnswer.put("Message","Dataset is  successfully loaded!");
        response.getOutputStream().print(jsonAnswer.toString());
    }
    
    @RequestMapping(value="/generateHierarchy",  method = RequestMethod.POST)
    public void generateHierarchy(@RequestParam("hierType") String hierType, @RequestParam("varType") String varType, @RequestParam("attribute") String colName,
            @RequestParam("hierName") String name,@RequestParam(value = "startLimit",required = false,defaultValue = "0") int startLimit,
            @RequestParam(value = "endLimit",required = false,defaultValue = "0") int endLimit, @RequestParam(value = "startYear",required = false,defaultValue = "0") int startYear,
            @RequestParam(value = "endYear",required = false,defaultValue = "0") int endYear,@RequestParam(value = "fanout",required = false,defaultValue = "0") int fanout,
            @RequestParam(value = "step",required = false,defaultValue = "0") int step, @RequestParam(value = "years",required = false,defaultValue = "0") int years,
            @RequestParam(value = "months",required = false,defaultValue = "0") int months, @RequestParam(value = "days",required = false,defaultValue = "0") int days,
            @RequestParam(value = "sorting",required = false,defaultValue = "0") String sorting,@RequestParam(value = "length",required = false,defaultValue = "0") int length,
            HttpSession session, HttpServletResponse response) throws IOException{
        
        //@RequestParam("typehier") String typehier, @RequestParam("vartype") String vartype,@RequestParam("onattribute") int onattribute,
        //@RequestParam("step") double step, @RequestParam("sorting") String sorting, @RequestParam("hiername") String hiername, 
        //@RequestParam("fanout") int fanout, @RequestParam("limits") String limits, @RequestParam("months") int months, 
        //@RequestParam("days") int days, @RequestParam("years") int years,  @RequestParam("length") int length
        
        
        try{
            Data data = (Data) session.getAttribute("data");
            String limits = "";
            if(hierType.equals("range") && varType.equals("date")){
                limits += startYear+"-"+endYear;
            }
            else if(hierType.equals("range")){ // vartype int or double
                limits += startLimit+"-"+endLimit;
            }
            int attributeCol = data.getColumnByName(colName);
            this.autogeneratehierarchy(hierType, varType, attributeCol, (double)step, sorting, name, fanout, limits, months, days, years, length, session);
            response.setStatus(HttpServletResponse.SC_OK);
            this.saveHierarchy(response,session);
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Failed to autogenerate an hierarchy, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
        
    }
    
    @RequestMapping(value="/loadHierarchies",  method = RequestMethod.POST)
    public void loadHierarchies(@RequestParam("hierarchies") MultipartFile[] hierarchies, HttpSession session, HttpServletResponse response) throws IOException{
        try{
            Map<String, Hierarchy> hierarchiesLoaded = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
            if(hierarchiesLoaded != null){
                for(Map.Entry<String,Hierarchy> hierLoaded : hierarchiesLoaded.entrySet()){
                    if(hierLoaded.getValue().getHierarchyType().contains("demographic")){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","Failed to load custom hierarchies due to demographic hierarchies have already loaded, please remove demographic hierarchies and try again!");
                        response.getOutputStream().print(jsonAnswer.toString());
                        return;
                    }
                }
            }
            for(int i=0; i<hierarchies.length; i++){
                this.hierarchy(hierarchies[i], false, session);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("Message","Hierarchies have been successfully loaded!");
            response.getOutputStream().print(jsonAnswer.toString());
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Failed to load hierarchies, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/anonymization",  method = RequestMethod.POST)
    public void anonymization (@RequestParam("bind") String bind, @RequestParam("k") int k, @RequestParam(value = "m",required = false,defaultValue = "-1") int m,
            HttpSession session, HttpServletResponse response) throws IOException{
        
        System.out.println("session anonymization "+session.getId());
        try{
            Data data = (Data) session.getAttribute("data");
            String hierType = "other";
            Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
            for(Map.Entry<String,Hierarchy> hier : hierarchies.entrySet()){
                if(hier.getValue().getHierarchyType().contains("demographic")){
                    hierType = "demographic";
                }
            }
            Map<String,String> colNamesHier = this.jsonToMap(bind);
            Map<Integer, Hierarchy> quasiIdentifiers = new HashMap();
           
            for(Map.Entry<String,String> colHier : colNamesHier.entrySet()){
               quasiIdentifiers.put(data.getColumnByName(colHier.getKey()), hierarchies.get(colHier.getValue()));
               System.out.println("colName "+data.getColumnByName(colHier.getKey())+" Hier "+hierarchies.get(colHier.getValue()));
            }
            
            String checkHier = null;
            for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
                Hierarchy h = entry.getValue();
                checkHier = h.checkHier(data,entry.getKey());
                if(checkHier != null && !checkHier.endsWith("Ok")){
                     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message",checkHier);
                    response.getOutputStream().print(jsonAnswer.toString());
                    return;
                }
            }
            
            session.setAttribute("quasiIdentifiers", quasiIdentifiers);
            session.setAttribute("k", k);
            
            Map<String, Integer> args = new HashMap<>();
            Algorithm algorithm = null;
            if(hierType.equals("demographic")){
                args.put("k", k);
                algorithm = new DemographicAlgorithm();
                session.setAttribute("algorithm", "demographic");
            }
            else if(data instanceof TXTData || data instanceof DICOMData){
                args.put("k", k);
                algorithm = new ParallelFlash();
                session.setAttribute("algorithm", "pFlash");
            }
            else if(data instanceof SETData){
                args.put("k", k);
                if(m<0){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","m parameter is required for km-anonymity, please try again!");
                    response.getOutputStream().print(jsonAnswer.toString());
                    return;
                }
                args.put("m", m);
                algorithm = new Apriori();
                session.setAttribute("algorithm", "apriori");
            }
            else if(data instanceof RelSetData){
                args.put("k", k);
                if(m<0){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","m parameter is required for km-anonymity, please try again!");
                    response.getOutputStream().print(jsonAnswer.toString());
                    return;
                }
                args.put("m", m);
                algorithm = new MixedApriori();
                session.setAttribute("algorithm", "apriori");
            }
            else if(data instanceof DiskData){
                args.put("k", k);
                algorithm = new ClusterBasedAlgorithm();
                session.setAttribute("algorithm", "clustering");
            }
            else{
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","Wrong dataset type, please try again!");
                response.getOutputStream().print(jsonAnswer.toString());
            }
            
            algorithm.setDataset(data);
            algorithm.setHierarchies(quasiIdentifiers);

            algorithm.setArguments(args);


            final String message = "memory problem";
            String resultAlgo="";
            Future<String> future = null;
            System.out.println("Algorithm starts");
            try {
                if(os.equals("online")){
                    ExecutorService executor = Executors.newCachedThreadPool();
                    final Algorithm temp = algorithm;
                    future = executor.submit( new Callable<String>() {
                    public String call() throws OutOfMemoryError {
                        try{
                        temp.anonymize();
                        }catch (OutOfMemoryError e) {
                            e.printStackTrace();
                            return message;
                        }


                        return "Ok";
                    }});
                    resultAlgo = future.get(3, TimeUnit.MINUTES);
                }
                else{
                    algorithm.anonymize();
                }

            }catch (TimeoutException e) {
            // Too long time
                if(future == null){
                    e.printStackTrace();
                }
                else{
                    e.printStackTrace();
                    future.cancel(true);
                    restart(session);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","Failed to anonymize the dataset, online version is out of time please try again!");
                    response.getOutputStream().print(jsonAnswer.toString());
                }
            }
            catch (OutOfMemoryError e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message",message);
                response.getOutputStream().print(jsonAnswer.toString());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
                restart(session);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","Failed to anonymize the dataset, please try again!");
                response.getOutputStream().print(jsonAnswer.toString());
            } catch (ExecutionException ex) {
                ex.printStackTrace();
                Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
                restart(session);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","Failed to anonymize the dataset, please try again!");
                response.getOutputStream().print(jsonAnswer.toString());
            }
            

            if(resultAlgo.equals(message)){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message",message);
                response.getOutputStream().print(jsonAnswer.toString());
            }
            
            if(algorithm.getResultSet() == null){
                if(!(data instanceof DiskData)){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","Anonymization procedure has no results");
                    response.getOutputStream().print(jsonAnswer.toString());
                }
                this.getAnonDataSet(0, 10, session);
                response.setStatus(HttpServletResponse.SC_OK);
                this.saveAnonymizeDataset(session, response);
            }
            else{

                session.setAttribute("results", algorithm.getResultSet());
    //            System.out.println("algorithm : "+algorithmSelected);
                if((data instanceof TXTData || data instanceof DICOMData) && !(hierType.equals("demographic"))){
                    Graph graph = algorithm.getLattice();
                    session.setAttribute("graph", graph);
                    JSONObject jsonAnswer = new JSONObject();
                    ArrayList<Node> nodesSol = graph.getNodeList();
                    String idSol = "sol";
                    for(int i=0; i<nodesSol.size(); i++){
                        JSONObject levelRes = new JSONObject();
                        levelRes.put("levels", nodesSol.get(i).getLabel().replace(" ", ""));
                        levelRes.put("result", nodesSol.get(i).getColor().toLowerCase().contains("red") ? "unsafe" : "safe");
                        jsonAnswer.put(idSol+i,levelRes);
                    }
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    JSONObject solutions = new JSONObject();
                    solutions.put("Solutions",jsonAnswer);
                    response.getOutputStream().print(solutions.toString());
                }
                else {
                    this.getAnonDataSet(0, 0, session);
                    response.setStatus(HttpServletResponse.SC_OK);
                    this.saveAnonymizeDataset(session, response);
                }
            }
        
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Failed to anonymize the dataset, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/getSolution",  method = RequestMethod.POST)
    public void getSolution (@RequestParam("sol") String sol, HttpSession session, HttpServletResponse response) throws IOException{
        try{
            this.deleteSuppress(session);
            sol = sol.trim().replace("[", "").replace("]","");
            this.setSelectedNode(session, sol);
            this.getAnonDataSet(0, 0, session);
            response.setStatus(HttpServletResponse.SC_OK);
            this.saveAnonymizeDataset(session, response);
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Failed to return solution file, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/getSuppressPercentage",  method = RequestMethod.POST)
    public void getSuppressPercentage (@RequestParam("sol") String sol, HttpSession session, HttpServletResponse response) throws IOException{
        
        try{
            this.deleteSuppress(session);
            sol = sol.trim().replace("[", "").replace("]","");
            this.setSelectedNode(session, sol);
            Data data = (Data) session.getAttribute("data");
            int  k = (int) session.getAttribute("k");
            Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>) session.getAttribute("quasiIdentifiers");
            String attributes = "";
            for(Map.Entry<Integer,Hierarchy> quasi : quasiIdentifiers.entrySet()){
                attributes += data.getColumnByPosition(quasi.getKey())+" ";
            }
            attributes = attributes.substring(0, attributes.length() - 1);
            Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
            this.findSolutionStatistics(session);
            SolutionsArrayList stats = this.getSolutionStatistics(attributes, session);
            response.setStatus(HttpServletResponse.SC_OK);
            double suppress = stats.getPercentangeSuppress();
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("percentageSuppress", suppress);
            jsonAnswer.put("k",k);
            if(suppress!=0.0){
                jsonAnswer.put("Message","To produce a k="+k+" anonymity solution, it must be suppressed by "+suppress+"%");
            }
            else{
                jsonAnswer.put("Message","The solution: ["+sol+"] statisfies k="+k+" anonymity");
            }
            response.getOutputStream().print(jsonAnswer.toString());
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to return the percentage of suppression, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
        
        
    }
    
    @RequestMapping(value="/getSuppressedSolution",  method = RequestMethod.POST)
    public void suppressSolution (@RequestParam("sol") String sol, HttpSession session, HttpServletResponse response) throws IOException{
        try{
            this.deleteSuppress(session);
            String originalSol = sol;
            sol = sol.trim().replace("[", "").replace("]","");
            this.setSelectedNode(session, sol);
            Data data = (Data) session.getAttribute("data");
            int  k = (int) session.getAttribute("k");
            Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>) session.getAttribute("quasiIdentifiers");
            String attributes = "";
            for(Map.Entry<Integer,Hierarchy> quasi : quasiIdentifiers.entrySet()){
                attributes += data.getColumnByPosition(quasi.getKey())+" ";
            }
            attributes = attributes.substring(0, attributes.length() - 1); 
            Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
            this.findSolutionStatistics(session);
            SolutionsArrayList stats = this.getSolutionStatistics(attributes, session);
            response.setStatus(HttpServletResponse.SC_OK);
            double suppress = stats.getPercentangeSuppress();

            if(suppress!=0.0){
                this.suppressValues(session);
                this.getAnonDataSet(0, 0, session);
                this.saveAnonymizeDataset(session, response);
            }
            else{
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","The solution "+originalSol+" satisfies "+k+"-anonymity so it can not be suppressed!");
                response.getOutputStream().print(jsonAnswer.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to suppress solution, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/getStatistics",  method = RequestMethod.POST)
    public void getStatistics (@RequestParam("sol") String sol, @RequestParam("quasi_ids") String[] columns, @RequestParam(value="suppressed",required = false,defaultValue = "false")
            boolean suppressed, HttpSession session, HttpServletResponse response) throws IOException{
        try{
            String[] sortedCols;
            Data data = (Data) session.getAttribute("data");
            Set<String> setCols = null;
            if(columns.length > 1){
                sortedCols = new String[columns.length];
                
                setCols = new HashSet();
                for(String col : columns){
                    setCols.add(col.trim());
                }
                String[] colNames = data.getColumnNames();
                int i=0;
                for(String dcol : colNames){
                    if(setCols.contains(dcol)){
                        sortedCols[i] = dcol;
                        i++;
                    }
                }
            }
            else{
                sortedCols = columns;
            }
            
            if(suppressed){
                this.deleteSuppress(session);
                String originalSol = sol;
                sol = sol.trim().replace("[", "").replace("]","");
                this.setSelectedNode(session, sol);
                int  k = (int) session.getAttribute("k");
                Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>) session.getAttribute("quasiIdentifiers");
                String attributes = "";
                if(setCols == null){
                    setCols = new HashSet(Arrays.asList(sortedCols));
                }
                if(setCols.size() != quasiIdentifiers.entrySet().size()){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","\"quasi_ids\" attribute does not contain all quisi identifiers!");
                    response.getOutputStream().print(jsonAnswer.toString());
                    return;
                }
                for(Map.Entry<Integer,Hierarchy> quasi : quasiIdentifiers.entrySet()){
                    attributes += data.getColumnByPosition(quasi.getKey())+" ";
                    if(!setCols.contains(data.getColumnByPosition(quasi.getKey()))){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","The quisi identifier: "+data.getColumnByPosition(quasi.getKey())+" is not provided in \"quasi_ids\"");
                        response.getOutputStream().print(jsonAnswer.toString());
                        return;
                    }
                }
                attributes = attributes.substring(0, attributes.length() - 1); 
                Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
                this.findSolutionStatistics(session);
                SolutionsArrayList stats = this.getSolutionStatistics(attributes, session);
                response.setStatus(HttpServletResponse.SC_OK);
                double suppress = stats.getPercentangeSuppress();
                if(suppress!=0.0){
                    SolutionsArrayList stats_suppressed = this.suppressValues(session);
                    JSONObject jsonAnswer = new JSONObject();
                    JSONArray jsonarray = new JSONArray();
                    jsonAnswer.put("Status","Success");
                    int totalRecs = 0;
                    for(Solutions msol : stats_suppressed.getSolutions()){
                        JSONObject jsonSol = new JSONObject();
                        jsonSol.put("value", msol.getLabel());
                        jsonSol.put("numberOfValues",msol.getData());
                        totalRecs += Integer.parseInt(msol.getData());
                        jsonarray.add(jsonSol);
                    }
                    jsonAnswer.put("AnonymizedStats", jsonarray);
                    jsonAnswer.put("TotalRecords", totalRecs);
                    jsonAnswer.put("k",k);
                    response.getOutputStream().print(jsonAnswer.toString());
                    }
                else{
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","The solution "+originalSol+" satisfies "+k+"-anonymity so it can not be suppressed!");
                    response.getOutputStream().print(jsonAnswer.toString());
                }
            }
            else{
                this.deleteSuppress(session);
                sol = sol.trim().replace("[", "").replace("]","");
                System.out.println("sol "+sol);
                this.setSelectedNode(session, sol);
                int  k = (int) session.getAttribute("k");
                Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>) session.getAttribute("quasiIdentifiers");
                String attributes = "";
                for(String col : sortedCols){
                    if(quasiIdentifiers.containsKey(data.getColumnByName(col.trim()))){
                        attributes += col.trim()+" ";
                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","The column: "+col.trim()+" is not a quisi identifier!");
                        response.getOutputStream().print(jsonAnswer.toString());
                        return;
                    }
                }
                attributes = attributes.substring(0, attributes.length() - 1); 
                this.findSolutionStatistics(session);
                System.out.println("Attributes "+attributes);
                SolutionsArrayList stats = this.getSolutionStatistics(attributes, session);
                response.setStatus(HttpServletResponse.SC_OK);
                double suppress = stats.getPercentangeSuppress();
                JSONObject jsonAnswer = new JSONObject();
                JSONArray jsonarray = new JSONArray();
                jsonAnswer.put("Status","Success");
                int totalRecs = 0;
                for(Solutions msol : stats.getSolutions()){
                    JSONObject jsonSol = new JSONObject();
                    jsonSol.put("value", msol.getLabel());
                    jsonSol.put("numberOfValues",msol.getData());
                    totalRecs += Integer.parseInt(msol.getData());
                    jsonarray.add(jsonSol);
                }
                jsonAnswer.put("AnonymizedStats", jsonarray);
                jsonAnswer.put("TotalRecords", totalRecs);
                jsonAnswer.put("k",k);
                response.getOutputStream().print(jsonAnswer.toString());
            }
            
            
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to return statistics, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/getAnonRules",  method = RequestMethod.POST)
    public void getAnonRules (@RequestParam(value="sol",required = false) String sol, @RequestParam(value="suppressed",required = false,defaultValue = "false")
            boolean suppressed, HttpSession session, HttpServletResponse response) throws IOException{
        
        try{
            Data data = (Data) session.getAttribute("data");
            if(data instanceof DiskData){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","Anonymization rules are not available for Disk based clustering algorithm!");
                response.getOutputStream().print(jsonAnswer.toString());
            }
            
            if(suppressed){
                if(!(data instanceof TXTData || data instanceof DICOMData)){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","Suppression is available only for simple table data!");
                    response.getOutputStream().print(jsonAnswer.toString());
                }
                else if(sol == null){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","Need to provide specific solution");
                    response.getOutputStream().print(jsonAnswer.toString());
                }
                else{
                    this.deleteSuppress(session);
                    String originalSol = sol;
                    sol = sol.trim().replace("[", "").replace("]","");
                    this.setSelectedNode(session, sol);
                    int  k = (int) session.getAttribute("k");
                    Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>) session.getAttribute("quasiIdentifiers");
                    String attributes = "";
                    for(Map.Entry<Integer,Hierarchy> quasi : quasiIdentifiers.entrySet()){
                        attributes += data.getColumnByPosition(quasi.getKey())+" ";
                    }
                    attributes = attributes.substring(0, attributes.length() - 1); 
                    Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
                    this.findSolutionStatistics(session);
                    SolutionsArrayList stats = this.getSolutionStatistics(attributes, session);
                    response.setStatus(HttpServletResponse.SC_OK);
                    double suppress = stats.getPercentangeSuppress();

                    if(suppress!=0.0){
                        this.suppressValues(session);
                        this.saveAnonynizationRules(session, response);
                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","The solution "+originalSol+" satisfies "+k+"-anonymity so it can not be suppressed!");
                        response.getOutputStream().print(jsonAnswer.toString());
                    }
                }
            }
            else{
                if(sol!=null && !(data instanceof TXTData || data instanceof DICOMData)){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","The \"sol\" field needs only fot simple table data!");
                    response.getOutputStream().print(jsonAnswer.toString());
                }
                else if(sol!=null){
                    this.deleteSuppress(session);
                    sol = sol.trim().replace("[", "").replace("]","");
                    this.setSelectedNode(session, sol);
                    this.saveAnonynizationRules(session, response);
                }
                else{
                    this.saveAnonynizationRules(session, response);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to produce anonymization rules, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    
    @RequestMapping(value="/clearSession",  method = RequestMethod.POST)
    public void clearSession ( HttpSession session, HttpServletResponse response) throws IOException{
        try{
            this.restart(session);
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("Message","Session is cleared!");
            response.getOutputStream().print(jsonAnswer.toString());
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to clear session, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    
    
    @RequestMapping(value="/loadAnonRules",  method = RequestMethod.POST)
    public void loadAnonRules(@RequestParam("rules") MultipartFile file, HttpSession session, HttpServletResponse response) throws IOException {
        try{
            this.upload(file, false, session);
            String check = this.loadAnonynizationRules(session, file.getOriginalFilename());
            if(check!=null){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message",check);
                response.getOutputStream().print(jsonAnswer.toString());
            }
            else{
                this.getAnonDataSet(0, 0, session);
//                session.removeAttribute("results");
                response.setStatus(HttpServletResponse.SC_OK);
                this.saveAnonymizeDataset(session, response);
            }
            
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to load anonymization rules, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    
    @RequestMapping(value="/setMask",  method = RequestMethod.POST)
    public void setMask(@RequestParam("col_idx") int  col_idx,@RequestParam("char") String character, @RequestParam("option") String maskOption, @RequestParam(value="positions",required = false) String positions,
            @RequestParam(value="regexVal",required = false) String regex,HttpSession session, HttpServletResponse response) throws IOException {
        try{
            Data data = (Data) session.getAttribute("data");
            if(!data.getColNamesType().get(col_idx).equals("string")){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","Column "+data.getColNamesPosition().get(col_idx)+" is not a string data type");
                response.getOutputStream().print(jsonAnswer.toString());
                return;
            }
            
            if(maskOption.trim().equals("suffix") || maskOption.trim().equals("prefix")){
                this.saveMask(col_idx, positions, character, maskOption, session);
            }
            else if(maskOption.trim().equals("regex")){
                this.saveRegex(col_idx, character, regex, session);
            }
            else{
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","No acceptable option, option's value: suffix, prefix, regex");
                response.getOutputStream().print(jsonAnswer.toString());
                return;
            }
            
            this.saveDataset(null, session, response);
            
            
            
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to set masking rule, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/loadDICOM",  method = RequestMethod.POST)
    public void loadDicoms(@RequestParam("dicoms") MultipartFile[] files, @RequestParam("datasetType") String datasetType, HttpSession session, HttpServletResponse response) throws IOException {
        try{
            for(MultipartFile file : files){
                this.upload(file, true, session);
            }
            this.getSmallDataSet(null, datasetType,null, session);
            Data data = (Data) session.getAttribute("data");
            String[][] smallDataset = data.getSmallDataSet();
            
            String [][] types = data.getTypesOfVariables(smallDataset);
            JSONObject jsonAnswer = new JSONObject();
            JSONObject datatypes = new JSONObject();
            String[] colnames = data.getColumnNames();
            for(int i=0; i<colnames.length; i++){
                JSONArray jsontypes = new JSONArray();
                for(int j=0; j<types[i].length; j++){
                    jsontypes.add(types[i][j]);
                }
                datatypes.put(colnames[i], jsontypes);
            }
            jsonAnswer.put("columnTypes", datatypes);
            response.getOutputStream().print(jsonAnswer.toString());
            return;
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to DICOM files, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/bindDICOMDataTypes",  method = RequestMethod.POST)
    public void bindData(@RequestParam("columnsType") String dataTypes, HttpSession session, HttpServletResponse response) throws IOException {
        try{
            Data data = (Data) session.getAttribute("data");
            boolean[] checkColumns = new boolean[data.getSmallDataSet()[0].length];
            String[] vartypes = new String[data.getSmallDataSet()[0].length];
            Map<String,String> datatypesMap = jsonToMap(dataTypes);
            for(int i=0; i<checkColumns.length; i++){
                if(datatypesMap.containsKey(data.getColumnNames()[i])){
                    checkColumns[i] = true;
                    String type = datatypesMap.get(data.getColumnNames()[i]);
                    if(type.equals("int") || type.equals("double") || type.equals("decimal") || type.equals("set") || type.equals("string") || type.equals("date")){
                        vartypes[i] = datatypesMap.get(data.getColumnNames()[i]).replace("decimal", "double");
                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","Unsupported data type "+type);
                        response.getOutputStream().print(jsonAnswer.toString());
                        return;
                    }
                }
                else{
                    checkColumns[i] = false;
                    vartypes[i] = null;
                }
                
            }
            this.loadDataset(vartypes, checkColumns, session);
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to bind columns' types in DICOM dataset, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        JSONObject jsonAnswer = new JSONObject();
        jsonAnswer.put("Status","Success");
        jsonAnswer.put("Message","DICOM Dataset is successfully loaded!");
        response.getOutputStream().print(jsonAnswer.toString());
    }
    
    @RequestMapping(value="/getDemographicDistributions",  method = RequestMethod.POST)
    public void getDemographicDistribution(HttpSession session, HttpServletResponse response) throws IOException {
        try{
            Map<String,ArrayList<String>> info = this.getDemographicInfo(session);

            Map<String,Map<String,ArrayList<String>>> demographicInfo = new HashMap<>();
            Set<String> distributions = info.keySet();
            for(String distr : distributions){
                Map<String,ArrayList<String>> datatypesCountries = new HashMap();
                ArrayList<String> types = new ArrayList();
                if(distr.toLowerCase().equals("age")){
    //                h = new HierarchyImplRangeDemographicAge("demographic_age_"+country,nodeType, country);

                    types.add("int");
                    types.add("double");


                }
                else{
    //                Data data = (Data) session.getAttribute("data");
    //                h = new HierarchyImplDemographicZipCode("demographic_zip_"+country,"string", country,data.getDictionary());
                    types.add("string");

                }
                datatypesCountries.put("dataTypes", types);
                datatypesCountries.put("countries", info.get(distr));
                demographicInfo.put(distr, datatypesCountries);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("demographic",demographicInfo);
            response.getOutputStream().print(jsonAnswer.toString());
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to return demographic information, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/generateAndLoadDemographicHierarchy",  method = RequestMethod.POST)
    public void getDemographicDistribution(@RequestParam("hier_attr") String hier,@RequestParam("country") String country, @RequestParam("varType") String nodeType,HttpSession session, HttpServletResponse response) throws IOException {
        try{
            Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
            Data data = (Data) session.getAttribute("data");
            
            if(data == null){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","You must firstly load the dataset!");
                response.getOutputStream().print(jsonAnswer.toString());
                return;
            }
            
            
            if ( hierarchies == null ){
                hierarchies = new HashMap<>();
                session.setAttribute("hierarchies", hierarchies);
            }
            else{
                for(Map.Entry<String,Hierarchy> hierLoad : hierarchies.entrySet()){
                    if(!hierLoad.getValue().getHierarchyType().contains("demographic")){
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        JSONObject jsonAnswer = new JSONObject();
                        jsonAnswer.put("Status","Fail");
                        jsonAnswer.put("Message","Unable to generate demographic hierarchy due to they have already loaded custom hierarchies, please remove custom hierarchies and try again!");
                        response.getOutputStream().print(jsonAnswer.toString());
                        return;
                    }
                }
            }

            Hierarchy h;
            if(hier.toLowerCase().equals("age")){
                
                h = new HierarchyImplRangeDemographicAge("demographic_age_"+country,nodeType, country);
            }
            else{
                
                h = new HierarchyImplDemographicZipCode("demographic_zip_"+country,"string", country,data.getDictionary());
            }
            h.load();
            hierarchies.put(h.getName(), h);

            if(os.equals("online")){
                this.deleteFiles(session);
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("Message","The demographic \""+h.getName()+"\" hierarchy has been successfully generated and loaded!");
            jsonAnswer.put("hierName",h.getName());
            response.getOutputStream().print(jsonAnswer.toString());
        
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to generate demographic hierarchy, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
        
        
    }
    
    @RequestMapping(value="/removeHierarchy",  method = RequestMethod.POST)
    public void removeHier(@RequestParam("hierName") String hiername,HttpSession session, HttpServletResponse response) throws IOException {
        try{
            Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
            if(hierarchies.get(hiername).getHierarchyType().contains("demographic")){
                hierarchies.get(hiername).clear();
            }
            hierarchies.remove(hiername);
            
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("Message","The  \""+hiername+"\" hierarchy has been successfully removed!");
            response.getOutputStream().print(jsonAnswer.toString());
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to remove \" \""+hiername+" please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/removeHierarchies",  method = RequestMethod.POST)
    public void removeHiers(HttpSession session, HttpServletResponse response) throws IOException {
        try{
            Map<String, Hierarchy> hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
            for(Map.Entry<String,Hierarchy> hier : hierarchies.entrySet()){
                if(hier.getValue().getHierarchyType().contains("demographic")){
                    hier.getValue().clear();
                }
                hierarchies.remove(hier.getKey());
            }
            session.setAttribute("hierarchies", null);
            
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("Message","All hierarchies have been successfully removed!");
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to remove hierarchies please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
    }
    
    @RequestMapping(value="/informationLoss",  method = RequestMethod.POST)
    public void informationLoss(@RequestParam(value="sol",required = false) String sol,@RequestParam(value="suppressed",required = false,defaultValue = "false")
            boolean suppressed,HttpSession session, HttpServletResponse response) throws IOException {
        
        try{
            Data data = (Data) session.getAttribute("data");
            AnonymizedDataset anonData = (AnonymizedDataset) session.getAttribute("anondata");
            String algo = (String) session.getAttribute("algorithm");


            


            if(data == null){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject jsonAnswer = new JSONObject();
                jsonAnswer.put("Status","Fail");
                jsonAnswer.put("Message","Dataset was not loaded!");
                response.getOutputStream().print(jsonAnswer.toString());
                return;
            }


            if((data instanceof TXTData || data instanceof DICOMData) && !algo.equals("demographic")){
                if(sol==null){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","Need to provide specific solution");
                    response.getOutputStream().print(jsonAnswer.toString());
                    return;
                }
                else{
                    if(suppressed){
                        this.deleteSuppress(session);
                        String originalSol = sol;
                        sol = sol.trim().replace("[", "").replace("]","");
                        this.setSelectedNode(session, sol);
                        int  k = (int) session.getAttribute("k");
                        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>) session.getAttribute("quasiIdentifiers");
                        String attributes = "";
                        for(Map.Entry<Integer,Hierarchy> quasi : quasiIdentifiers.entrySet()){
                            attributes += data.getColumnByPosition(quasi.getKey())+" ";
                        }
                        attributes = attributes.substring(0, attributes.length() - 1); 
                        Map<SolutionHeader, SolutionStatistics> solMap = (Map<SolutionHeader, SolutionStatistics>) session.getAttribute("solutionstatistics");
                        this.findSolutionStatistics(session);
                        SolutionsArrayList stats = this.getSolutionStatistics(attributes, session);
                        response.setStatus(HttpServletResponse.SC_OK);
                        double suppress = stats.getPercentangeSuppress();

                        if(suppress!=0.0){
                            this.setSelectedNode(session, sol);
                            this.suppressValues(session);
                            this.getAnonDataSet(0, 0, session);
                        }
                        else{
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            JSONObject jsonAnswer = new JSONObject();
                            jsonAnswer.put("Status","Fail");
                            jsonAnswer.put("Message","The solution "+originalSol+" satisfies "+k+"-anonymity so it can not be suppressed!");
                            response.getOutputStream().print(jsonAnswer.toString());
                        }
                    }
                    else{
                        this.deleteSuppress(session);
                        sol = sol.trim().replace("[", "").replace("]","");
                        this.setSelectedNode(session, sol);
                        this.getAnonDataSet(0, 0, session);
                    }
                }
            }
            else{
                if(anonData == null){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JSONObject jsonAnswer = new JSONObject();
                    jsonAnswer.put("Status","Fail");
                    jsonAnswer.put("Message","There is no anonymized dataset!");
                    response.getOutputStream().print(jsonAnswer.toString());
                    return;
                } 
            }
            
            Map<String,Double> inLoss = this.lossMetrics(session);
            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Success");
            jsonAnswer.put("InLoss",inLoss);
            response.getOutputStream().print(jsonAnswer.toString());
        
        }catch(Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject jsonAnswer = new JSONObject();
            jsonAnswer.put("Status","Fail");
            jsonAnswer.put("Message","Unable to produce information loss metrics, please try again!");
            response.getOutputStream().print(jsonAnswer.toString());
        }
        
        
    }
    
    
    
    
    
    
    
    
    
    

    
    
////////////////////////////////////////////// old API /////////////////////////////////////////////////////////////////////////
    
    //amnesia/dataset
    @RequestMapping(value="/dataset", produces = "application/json",  method = RequestMethod.POST)
    public @ResponseBody String Dataset ( @RequestParam("file") MultipartFile file, @RequestParam("data") boolean data, @RequestParam("del") String del, @RequestParam("datatype") String datatype , @RequestParam("vartypes") String [] vartypes, @RequestParam("checkColumns") boolean [] checkColumns, HttpSession session  ) throws FileNotFoundException, IOException, Exception {
        
        this.upload(file, data, session);
        this.getSmallDataSet(del, datatype,"", session);
        this.loadDataset(vartypes, checkColumns, session);
        
        return null;
    }
            
    
    //amnesia/hierarchy
    @RequestMapping(value="/hierarchy",  method = RequestMethod.POST)//, method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String hierarchy ( @RequestParam("file") MultipartFile file, @RequestParam("data") boolean data,  HttpSession session ) throws FileNotFoundException, IOException, Exception {
        
        
        this.upload(file, data, session);
        this.loadHierarcy(file.getOriginalFilename(), session);
        
        return null;
    }
    
    
    //amnesia/anonymize
    @RequestMapping(value="/anonymize",  method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String anonymize (@RequestParam("k") int k, @RequestParam("m") int m, @RequestParam("algo") String algo , @RequestParam("relations") String[] relations , HttpSession session)  {
        
        
        Algorithm algorithm = null;
        Map<Integer, Hierarchy> quasiIdentifiers = new HashMap<Integer, Hierarchy>();
        boolean result = false;
        Map<String, Hierarchy> hierarchies  = null;

        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        Data data = (Data) session.getAttribute("data");
        
        String algorithmSelected = algo;
        
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        for ( int  i = 0 ; i < relations.length ; i ++){
            System.out.println(" relation = " + relations[i]);
        }
        
        
        
        
        for ( int  i = 0 ; i < relations.length ; i ++){
            if (!relations[i].equals("") && hierarchies.get(relations[i])!=null){
                quasiIdentifiers.put(i, hierarchies.get(relations[i]));
            }
        }
        
       
        
        
        ///////////////////////new feature///////////////////////
        String checkHier = null;
        for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
            Hierarchy h = entry.getValue();
            if(h !=null){
                if(h instanceof HierarchyImplString){
                    h.syncDictionaries(entry.getKey(),data);
                }

                //problem in hierarchy
                checkHier = h.checkHier(data,entry.getKey());
                if(checkHier != null && !checkHier.endsWith("Ok")){
                    return checkHier;
                }
                System.out.println("Not Null");
            }
        }

        ////////////////////////////////////////////
        

        
        Map<String, Integer> args = new HashMap<>();

        if(algorithmSelected.equals("Flash")){
            args.put("k", k);
            algorithm = new Flash();
            session.setAttribute("algorithm", "flash");
        }
        else if(algorithmSelected.equals("pFlash")){
            args.put("k", k);
            algorithm = new ParallelFlash();
            session.setAttribute("algorithm", "flash");
        }
        else if(algorithmSelected.equals("kmAnonymity") || algorithmSelected.equals("apriori") ||
                algorithmSelected.equals("AprioriShort")){
                args.put("k", k);

            //check if m is an integer

            if(k > data.getDataLenght()){
                System.out.println("k must be at least as long as the number of records");
            }
            args.put("m", m);

            if(algorithmSelected.equals("apriori")){
                if(!(data instanceof SETData)){
                     System.out.println("No set-valued dataset loaded!");
                }
                algorithm = new Apriori();
                quasiIdentifiers.get(0).buildDictionary(data.getDictionary());
            }

            session.setAttribute("algorithm", "apriori");

        }


        algorithm.setDataset(data);
        algorithm.setHierarchies(quasiIdentifiers);

        algorithm.setArguments(args);


        String message = "memory problem";
        try {

            algorithm.anonymize();

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {

            return message;
        }

        hierarchies = new HashMap<>();
        session.setAttribute("quasiIdentifiers", quasiIdentifiers);
        session.setAttribute("k", k);

        if(algorithm.getResultSet() == null){
            result = false;
            message = "noresults";
            return null;
        }
        else{

            session.setAttribute("results", algorithm.getResultSet());
            if(!algorithmSelected.equals("apriori")){
                Graph graph = algorithm.getLattice();

                session.setAttribute("graph", graph);
            }
        }



        result = true;



        return "ok\n";
    }    
      
    
    ///////////////action/anondataexists/////////////////////other functions//////////////////////////////////
    
    
    
    
    private List<String> findHierarchyType(String file){
        List<String> result = new ArrayList<>();
        BufferedReader br;
        String line;
        if(file.endsWith(".txt")){
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file),StandardCharsets.UTF_8));

                while ((line = br.readLine()) != null) {
    //                System.out.println(line);
                    if(line.trim().isEmpty())
                        break;

                    //find if distinct or range hierarchy
                    if(line.trim().equalsIgnoreCase("distinct")){
                        result.add("distinct");
                        continue;
                    } else if (line.trim().equalsIgnoreCase("range")){
                        result.add("range");
                        continue;
                    }

                    //find if int, double or string
                    String[] tokens = line.split(" ");
                    if(tokens[0].equalsIgnoreCase("type")){
                        result.add(tokens[1]);
                    }
                }
                br.close();
            } catch (IOException ex) {
                System.out.println("problem");
            }
        }
        else if(file.endsWith(".json")){
            JSONParser parser = new JSONParser();
            try {
                JSONObject  obj = (JSONObject) parser.parse(new FileReader(file));
                result.add(((String) obj.get("hierType")).toLowerCase());
                result.add(((String) obj.get("type")).toLowerCase());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
            } catch (org.json.simple.parser.ParseException ex) {
                Logger.getLogger(AppCon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }
    
    private boolean checkQuasi(Data dataset,Map<Integer, Hierarchy> quasi,String attrNames){
        String quasiNames="";
        Map<Integer,String> namesCol = dataset.getColNamesPosition();
        for(Map.Entry<Integer, Hierarchy> entry : quasi.entrySet()){
            quasiNames += namesCol.get(entry.getKey());
        }
        System.out.println("AttrNames: "+attrNames+" quasiNames: "+quasiNames);
        return attrNames.replaceAll(" ", "").equals(quasiNames.replaceAll(" ", ""));
    }
    
    private  Map<String, String> jsonToMap(String t)  {

       ObjectMapper mapper = new ObjectMapper();
       Map<String, String> map = null;
       if(t==null){
           return null;
       }
        try {

            // convert JSON string to Map
            map = mapper.readValue(t, Map.class);

            // it works
            //Map<String, String> map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});

            System.out.println(map);
            
            

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return map;
    }
    
}


