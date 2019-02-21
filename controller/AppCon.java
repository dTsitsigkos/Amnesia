package controller;


import algorithms.Algorithm;
import algorithms.flash.Flash;
import algorithms.flash.LatticeNode;
import algorithms.kmanonymity.Apriori;
import algorithms.parallelflash.ParallelFlash;
import anonymizationrules.AnonymizationRules;
import anonymizeddataset.AnonymizedDataset;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Ints;
import data.CheckDatasetForKAnomymous;
import data.Data;
import data.Pair;
import data.SETData;
import data.TXTData;
import dictionary.DictionaryString;
import graph.DatasetsExistence;
import graph.Edge;
import graph.Node;
import graph.Graph;
import hierarchy.HierToJson;
import hierarchy.Hierarchy;
import hierarchy.distinct.AutoHierarchyImplDate;
import hierarchy.distinct.AutoHierarchyImplDouble;
import hierarchy.distinct.AutoHierarchyImplString;
import hierarchy.distinct.HierarchyImplDouble;
import hierarchy.distinct.HierarchyImplString;
import hierarchy.ranges.AutoHierarchyImplRangesDate;
import hierarchy.ranges.AutoHierarchyImplRangesNumbers;
import hierarchy.ranges.AutoHierarchyImplRangesNumbers2;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.MemoryUsage;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import jsoninterface.View;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
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

    public static void main(String[] args) {
        SpringApplication.run(applicationClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    private static Class<AppCon> applicationClass = AppCon.class;
}



@RestController
//@RequestMapping("/greeting")
class AppController {

    
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String welcome(HttpSession session) {
        return "/index.html";
    }
    
    
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
            File dir = null;
            String input = (String)session.getAttribute("inputpath");
            if (input == null){

               //String rootPath = System.getProperty("user.home");//windows
                   
                ////////////////////linux///////////////////////////////////////
                File f = new File(System.getProperty("java.class.path"));//linux
                File dir1 = f.getAbsoluteFile().getParentFile();
                String rootPath = dir1.toString();
                //////////////////////////////////////////////////////////////
                dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
                if (!dir.exists()){
                    dir.mkdirs();
                }
            }
            else{
              
                dir = new File(input);
            }
                    
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                uploadedFile = itr.next();
                file = request.getFile(uploadedFile);
                mimeType = file.getContentType();
                filename = file.getOriginalFilename();
                bytes = file.getBytes();
            }
            
            session.setAttribute("inputpath",dir.toString());
            session.setAttribute("filename",filename);
                    
            try{ // Create the file on server
                File serverFile = new File(dir.getAbsolutePath()
                                + File.separator + filename);
                BufferedOutputStream stream = new BufferedOutputStream(
                                new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();

                //logger.info("Server File Location="
                //		+ serverFile.getAbsolutePath());

                //return "You successfully uploaded file=" + file.getOriginalFilename();
                errMes.setSuccess(true);
                errMes.setProblem("You successfully uploaded file=" + filename);

                return  errMes;
            } catch (Exception e) {
                    //return "You failed to upload " + file.getOriginalFilename() + " => " + e.getMessage();
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
    
    
    
    
    
    /**
     *
     * @param file
     * @param path
     * @param data
     * @param session
     * @return
     */
    @RequestMapping(value = "/action/upload", method = RequestMethod.POST)
    public @ResponseBody ErrorMessage upload(@RequestParam("file") MultipartFile file,@RequestParam("data") boolean data , HttpSession session){
        ErrorMessage errMes = new ErrorMessage();
        //boolean fileCreate = false;
        boolean problem = false;
        
        
        if (!file.isEmpty()) {
            try {
                // Creating the directory to store file
                File dir = null;
                String input = (String)session.getAttribute("inputpath");
                if (input == null){
                    
                    //String rootPath = System.getProperty("user.home");//windows
                   
                    ////////////////////linux///////////////////////////////////////
                    File f = new File(System.getProperty("java.class.path"));//linux
                    File dir1 = f.getAbsoluteFile().getParentFile();
                    String rootPath = dir1.toString();
                    //////////////////////////////////////////////////////////////
                    dir = new File(rootPath + File.separator + "amnesiaResults"+ File.separator + session.getId());  
                    if (!dir.exists()){
                        dir.mkdirs();
                    }

                    if (data == true){
                        session.setAttribute("inputpath",dir.toString());
                        session.setAttribute("filename", file.getOriginalFilename());
                    }
                    else{
                        session.setAttribute("inputpath",dir.toString());
                    }
                }
                else{
                    if (data == true){
                        session.setAttribute("filename", file.getOriginalFilename());
                    }
                    dir = new File(input);
                }
                
                    
                byte[] bytes = file.getBytes();

                // Create the file on server
                File serverFile = new File(dir.getAbsolutePath()
                                + File.separator + file.getOriginalFilename());
                BufferedOutputStream stream = new BufferedOutputStream(
                                new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();

                //logger.info("Server File Location="
                //		+ serverFile.getAbsolutePath());

                //return "You successfully uploaded file=" + file.getOriginalFilename();
                errMes.setSuccess(true);
                errMes.setProblem("You successfully uploaded file=" + file.getOriginalFilename());

                return  errMes;
            } catch (Exception e) {
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
    


 @JsonView(View.SmallDataSet.class)
    @RequestMapping(value="/action/getsmalldataset", method = RequestMethod.POST)//, method = RequestMethod.POST)
    public @ResponseBody Data getSmallDataSet ( @RequestParam("del") String del, @RequestParam("datatype") String datatype ,HttpSession session) throws FileNotFoundException, IOException {

        Data data = null;
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String delimeter = null;
        String result = null;
        
        String rootPath = (String)session.getAttribute("inputpath");
        String filename = (String)session.getAttribute("filename");

	File dir = new File(rootPath);

        
        String fullPath = dir + "/" + filename;
        
        if (datatype.equals("tabular")){

            if ( del == null ){
                delimeter = ",";
            }
            else{
                delimeter = del;
            }

            fstream = new FileInputStream(fullPath);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            while ((strLine = br.readLine()) != null){
                if ( strLine.contains(delimeter)){
                    data = new TXTData(fullPath,delimeter);
                }
                else{
                    if ((strLine = br.readLine()) != null){
                        if ( strLine.contains(delimeter)){
                            //data = new SETData(fullPath,delimeter);
                        }
                        else{
                            data = new TXTData(fullPath,delimeter);
                        }
                    }
                }
                result = data.findColumnTypes();
                break;
                
            }

            br.close();
            //data.findColumnTypes();
            
            if ( result == null){
                return null;
            }
            else if (result == "1"){
                return data;
            }
        
            String [][] smallDataset = data.getSmallDataSet();

            data.getTypesOfVariables(smallDataset);
        
        }
        else if (datatype.equals("set")){
            data = new SETData(fullPath,",");
            data.readDataset(null,null);
            String[][] small = data.getSmallDataSet(); 
        }
        else{
        
        }
        
        session.setAttribute("data", data);

        return data;
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

        File dir = new File(rootPath);


        String fullPath = dir + "/" + filename; 
        fstream = new FileInputStream(fullPath);
        in = new DataInputStream(fstream);
        br = new BufferedReader(new InputStreamReader(in));

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


            
        
         
        return exampleDataSet;
        
    }
    
    
    
    @JsonView(View.DataSet.class)
    @RequestMapping(value="/action/getdataset", method = RequestMethod.POST)//, method = RequestMethod.POST)
    public @ResponseBody Data getDataSet (@RequestParam("start") int start , @RequestParam("length") int length , HttpSession session) throws FileNotFoundException, IOException {
        

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
            System.out.println("data is null");
        }
        

        
        return data;
        
    }
    
    
    
    @RequestMapping(value="/action/loaddataset", method = RequestMethod.POST)
    public @ResponseBody String loadDataset (@RequestParam("vartypes") String [] vartypes, @RequestParam("checkColumns") boolean [] checkColumns, HttpSession session)  {
        String result = null;
        
        
        Data data = (Data) session.getAttribute("data");
        
        if (vartypes != null){
            
            result = data.readDataset(vartypes,checkColumns);
        }


        return result;
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
    
    
    
    //@JsonView(View.Hier.class)
    @RequestMapping(value="/action/loadhierarchy", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loadHierarcy (@RequestParam("filename") String filename, HttpSession session)  {
        Map<String, Hierarchy> hierarchies  = null;
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        String filenamex = (String)session.getAttribute("filename");
        
        
        if ( hierarchies == null ){
            hierarchies = new HashMap<>();
            session.setAttribute("hierarchies", hierarchies);
        }
        
        String rootPath = (String)session.getAttribute("inputpath");
        File dir = new File(rootPath);
        
        
        String fullPath = dir + "/" + filename;
       
        
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
            if(res.equalsIgnoreCase("int") || res.equalsIgnoreCase("double") || res.equalsIgnoreCase("string") || res.equalsIgnoreCase("date")){
                type = res;
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
            
            else if(type.equalsIgnoreCase("string") || type.equalsIgnoreCase("date")){
                h = new HierarchyImplString(fullPath);
//                h.setHierachyType("distinct");
            }
            else{
                h = new HierarchyImplDouble(fullPath);
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

        
        
        h.load();
        session.setAttribute("selectedhier", h.getName());
        hierarchies.put(h.getName(), h);

        
        
        return "xaxaxa";
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
        
        File file = new File(inputPath + "/" +hierName + ".txt");

        
        h.export(file.getAbsolutePath());
        
        
        InputStream myStream = new FileInputStream(file);

	// Set the content type and attachment header.
        if (h.getHierarchyType().equals("distinct")){
            response.addHeader("Content-disposition", "attachment;filename=distinct_hier_"+file.getName());
            response.setContentType("txt/plain");
        }
        else{
            response.addHeader("Content-disposition", "attachment;filename=range_hier_"+file.getName());
            response.setContentType("txt/plain");
        }

	// Copy the stream to the response's output stream.
	IOUtils.copy(myStream, response.getOutputStream());
	response.flushBuffer();
        
        
        
        
        
        


        return null;
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping(value="/action/autogeneratehierarchy", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody String autogeneratehierarchy (@RequestParam("typehier") String typehier, @RequestParam("vartype") String vartype,@RequestParam("onattribute") int onattribute,@RequestParam("step") double step, @RequestParam("sorting") String sorting, @RequestParam("hiername") String hiername, @RequestParam("fanout") int fanout, @RequestParam("limits") String limits, @RequestParam("months") int months, @RequestParam("days") int days, @RequestParam("years") int years, HttpSession session)  {
        Map<String, Hierarchy> hierarchies  = null;
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        Hierarchy h = null;

        
        if ( hierarchies == null ){
            hierarchies = new HashMap<>();
            session.setAttribute("hierarchies", hierarchies);
        }
              
        Data data = (Data) session.getAttribute("data");
        
        String attribute = data.getColumnByPosition(onattribute);

        if (typehier.equals("distinct")){
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
                temp = limits.split("-");
                Double start = Double.parseDouble(temp[0]);
                Double end = Double.parseDouble(temp[1]);

                //h = new AutoHierarchyImplRangesNumbers(hiername, vartype, "range", start, end, step, fanout);
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
            nGraph = h.getGraph(node, level);    
        //}
        //if ( nGraph == null){
         //   System.out.println("i am hereeeeeeeeee");
       // }
        //else{
        //    System.out.println("nGraph = " + nGraph);
        //}
        //System.out.println("node = " + node);
        
        
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
            //System.out.println("null 1111111");
            if ( hierarchies!= null){
               // System.out.println("null 22222");
                hierArray = new HierToJson[hierarchies.size()];
                int i = 0;
                for (Map.Entry<String, Hierarchy> entry : hierarchies.entrySet()) {
                    hierArray[i] = new HierToJson(entry.getKey(),entry.getKey(),entry.getKey(),entry.getValue().getNodesType());
                    i++;
                }
            }
        }
        else{
           // System.out.println("not null 11111");
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
                    
                    k++;
                }
            }
        
        }
        
        //System.out.println("enddddddddddddddddd getHierarchies");
        
        /*for ( int i = 0 ; i < hierArray.length; i++){
            System.out.println("id = " + hierArray[i].getId() + "\ttext = " + hierArray[i].getText());
        }*/
        
        
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
            if (h.getHierarchyType().equals("range")){
                checkHier = h.checkHier();
            }
            
            
            //provlima stin  hierarchia
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
        else if(algorithmSelected.equals("kmAnonymity") || algorithmSelected.equals("apriori") ||
                algorithmSelected.equals("AprioriShort")){
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
                quasiIdentifiers.get(0).buildDictionary(data.getDictionary(0));
            }

            session.setAttribute("algorithm", "apriori");

        }


        algorithm.setDataset(data);
        algorithm.setHierarchies(quasiIdentifiers);

        algorithm.setArguments(args);


        System.out.println("k = " + k + "\t m = " + m );

        //long startTime = System.currentTimeMillis();
//        long startCpuTime = getCpuTime();

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
            System.out.println("result set = " + algorithm.getResultSet() );

            session.setAttribute("results", algorithm.getResultSet());
            if(!algorithmSelected.equals("apriori")){
                Graph graph = algorithm.getLattice();

                session.setAttribute("graph", graph);
            }
        }

        
        

        result = true;

        String solution = this.InformationLoss(session);
        this.setSelectedNode(session, solution);
        

        return "ok";
    }
    
    @RequestMapping(value="/action/informationloss", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody String InformationLoss ( HttpSession session) {
        Set<LatticeNode> infoLossFirstStep = new HashSet<>();
        Set<LatticeNode> infoLossSecondStep = new HashSet<>();
        int minSum = 0;
        int []minHierArray;
        int minHier;
        LatticeNode solution = null;
        String solutionStr = null;
        
        boolean FLAG = false;
        
        Set<LatticeNode> results = (Set<LatticeNode>) session.getAttribute("results");
        /*for ( LatticeNode n : results){
            System.out.println("n = " + n + "\t level = " + n.getLevel() );
        }*/
        
        //first step, sum of levels
        for ( LatticeNode n : results){
            if (FLAG == false){
                minSum = n.getLevel();
                FLAG = true;
            }
            else{
                if ( minSum > n.getLevel()){
                    minSum = n.getLevel();
                }
            }
        }
        
        for ( LatticeNode n : results){
            if ( minSum == n.getLevel()){
                infoLossFirstStep.add(n);
            }         
        }
        
        //second step, min max hierarchy
        minHierArray = new int[infoLossFirstStep.size()];
        int counter = 0;
        for ( LatticeNode n : infoLossFirstStep){
            int []temp = n.getArray();
            minHierArray[counter] = Ints.max(temp);
            counter++;
        }
        
        minHier = Ints.min(minHierArray);

        for ( LatticeNode n : infoLossFirstStep){
            int []temp = n.getArray();
            if (minHier == Ints.max(temp)){
                infoLossSecondStep.add(n);
            }
        }
 
        //third step, choose the first one
        for ( LatticeNode n : infoLossSecondStep){
            solution = n;
            break;
        }
        
        solutionStr = solution.toString();
        solutionStr = solutionStr.replace("[","");
        solutionStr = solutionStr.replace("]","");
        solutionStr = solutionStr.replace(" ", "");
        System.out.println("solution = " + solutionStr);
        
        
        return solutionStr;
        
    }
    
    
    @RequestMapping(value="/action/getsolutiongraph", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody Graph getSolGraph ( HttpSession session) throws FileNotFoundException, IOException {
        Graph graph = null;
       
        
        
        graph = (Graph) session.getAttribute("graph");
        Set<LatticeNode> results = (Set<LatticeNode>) session.getAttribute("results");
        
        
        return graph;
        
    }
    
    
    
    
    
    @RequestMapping(value="/action/getanondataset", method = RequestMethod.GET) //method = RequestMethod.POST
    public @ResponseBody AnonymizedDataset getAnonDataSet (@RequestParam("start") int start , @RequestParam("length") int length, HttpSession session) throws FileNotFoundException, IOException, ParseException {
        AnonymizedDataset anonData = null;
        String selectedNode = null;
        
        
        selectedNode = (String)session.getAttribute("selectednode");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Set<String>> toSuppress = (Map<Integer, Set<String>>)session.getAttribute("tosuppress");
        Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
        
        Map<String, Set<String>> toSuppressJson = null;
        String selectedAttrNames = null;
        boolean FLAG = false;
        String [] temp = null;
        
        
        
        //if (selectedNode!= null){
        if ( allRules == null){
            //System.out.println("get anon dataset to suppress = " + toSuppress);
            if (toSuppress != null){
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
                ArrayList<LinkedHashMap> originalData = data.getPage(start, length);
                //Map<String, Map<String, String>> allRules = (Map<String, Map<String, String>>)session.getAttribute("anonrules");
                anonData = new AnonymizedDataset(data,start,length,selectedNode,quasiIdentifiers,toSuppress,selectedAttrNames,toSuppressJson);
                anonData.setDataOriginal(originalData);
                if (!data.getClass().toString().contains("SET")){
                    if ( allRules == null){
                        anonData.renderAnonymizedTable();
                    }
                    else{
                        anonData.anonymizeWithImportedRules(allRules);
                    }
                }
                else{
                    
                    System.out.println("action/getanondataset ===========");
                    
                    Map<Double, Double> rules = (Map<Double, Double>) session.getAttribute("results");
                    if ( allRules == null){
                        anonData.renderAnonymizedTable(rules);
                    }
                    else{
                        anonData.anonymizeSETWithImportedRules(allRules);
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
    public @ResponseBody void saveDataset ( HttpServletRequest request,HttpSession session , HttpServletResponse response) throws FileNotFoundException, IOException {
        Object [][] exportData = null; 
        
        System.out.println("app export datasettttttt");
        ServletContext context = request.getServletContext();
        String appPath = context.getRealPath("");
        System.out.println("appPath = " + appPath);
                
        Data data = (Data) session.getAttribute("data");
        String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        
        System.out.println("Export Dataset... " + filename);
        System.out.println("Export Dataset...");
        
        File file = new File(inputPath + "/" +filename);
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
	InputStream myStream = new FileInputStream(file);

	// Set the content type and attachment header.
	response.addHeader("Content-disposition", "attachment;filename=" +file.getName());
	response.setContentType("txt/plain");

	// Copy the stream to the response's output stream.
	IOUtils.copy(myStream, response.getOutputStream());
	response.flushBuffer();
        
        
        
        
       

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
    public @ResponseBody void saveAnonymizeDataset ( HttpSession session , HttpServletResponse response) throws FileNotFoundException, IOException, ParseException {
        Object [][] exportData = null; 

        Data data = (Data) session.getAttribute("data");
        String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        
        System.out.println("Export Anonymized Dataset... " + filename);
        AnonymizedDataset anonData = (AnonymizedDataset)session.getAttribute("anondata");
        anonData.setStart(0);
        System.out.println("Export anonymizedDataset...");
        File file = new File(inputPath + "/anonymized_" +filename);
        
        if (data.getClass().toString().contains("SET")){
            Map<Double, Double> results = (Map<Double, Double>) session.getAttribute("results"); 
            exportData = anonData.exportDataset(file.getAbsolutePath(), results);
        }
        else{
            exportData = anonData.exportDataset(file.getAbsolutePath(), true);
        }
        
        /*System.out.println("End ");
        File file = new File(inputPath + "/anonymized_" +filename);
        
        //data.export(file.getAbsolutePath(), null, exportData , null,null, null);*/
        
        InputStream myStream = new FileInputStream(file);

	// Set the content type and attachment header.
	response.addHeader("Content-disposition", "attachment;filename="+file.getName());
	response.setContentType("txt/plain");

	// Copy the stream to the response's output stream.
	IOUtils.copy(myStream, response.getOutputStream());
	response.flushBuffer();
        
        //htre.setHeader(null, null);
        //htre.getOutputStream();
        
        
        
    }
    
    
    @RequestMapping(value="/action/getzenodofiles", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ZenodoFilesToJson getZenodoFiles ( HttpSession session,@RequestParam("usertoken") String usertoken  ) throws FileNotFoundException, IOException {
        System.out.println("Zenodo Files = " + usertoken);
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
    
    
    @RequestMapping(value="/action/loadzenodofile", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loadzenodofile ( HttpSession session,@RequestParam("filename") String fileName, @RequestParam("title") String title, @RequestParam("usertoken") String usertoken  ) throws FileNotFoundException, IOException {
        System.out.println("Zenodo Files");
        ZenodoFilesToJson zenJson = null;
        ZenodoFile zenFile = null;
        File dir = null;
        
        System.out.println("i am hereeeee");
        
        System.out.println("usertoken = " + usertoken);
        
        Map<Integer, ZenodoFile> files = (Map<Integer, ZenodoFile>)session.getAttribute("zenodofiles");
        String inputPath = null;//(String)session.getAttribute("inputpath");
        if ( inputPath == null){
            String rootPath = System.getProperty("catalina.home");
            //String rootPath = "/var/lib/tomcat8";
            dir = new File(rootPath + File.separator + "amnesia"+ File.separator + session.getId());  
            if (!dir.exists()){
                    dir.mkdirs();
            }
            inputPath = rootPath + File.separator + "amnesia"+ File.separator + session.getId();
            session.setAttribute("inputpath",inputPath);
            session.setAttribute("filename",fileName);
            
        }
        
        System.out.println("i am hereeeee22222");

       
        for (Map.Entry<Integer, ZenodoFile> entry : files.entrySet()) {
            zenFile = entry.getValue();
            System.out.println("zen file = " + zenFile.getFileName() + "\timportname = " + fileName);
            System.out.println("zen title = " + zenFile.getTitle() + "\timporttitle = " + title);
            if (zenFile.getFileName().equals(fileName)){
                if (zenFile.getTitle().equals(title)){
                    break;
                }
            }
        }
        
        System.out.println("i am hereeeee3333");

        
        ZenodoConnection.downloadFile(zenFile, inputPath + "/" + zenFile.getFileName(),usertoken );
        
        System.out.println("i am hereeeeeeeeeeeeeeeeeeeeeeeeeee2222222222 =" +zenFile.getFileName() );
        
        return null;
    }
    
    
    
    
    @RequestMapping(value="/action/getsimilarzenodofiles", produces = "application/json", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody ZenodoFilesToJson getSimilarZenodoFiles ( HttpSession session,@RequestParam("usertoken") String usertoken,@RequestParam("filename") String fileName,@RequestParam("title") String title,@RequestParam("keywords") String keywords  ) throws FileNotFoundException, IOException {
        System.out.println("Zenodo Files");
        ZenodoFilesToJson zenJson = null;
        
        
        //String usertoken = "cSQgGzD08dJ11RMyRzLRhU4hi57LK454T8sovlw6Z2STZrQbzg809wUt6ywt";
        String inputPath = (String)session.getAttribute("inputpath");
        String fileNameInput = (String)session.getAttribute("filename");
        inputPath = inputPath +"/" + fileNameInput;
        
        Map<Integer, ZenodoFile> files = ZenodoConnection.getDepositionFiles(usertoken);
        if (files == null){
             return null;
         }
         else{
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
    
    
    @RequestMapping(value="/action/savefiletozenodo", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String saveFileToZenodo ( HttpSession session, @RequestParam("usertoken") String usertoken,@RequestParam("author") String author, @RequestParam("affiliation") String affiliation, @RequestParam("filename") String filename ,@RequestParam("title") String title,@RequestParam("description") String description, @RequestParam("contributors") String contributors, @RequestParam("keywords") String keywords  ) throws FileNotFoundException, IOException, ParseException {
        System.out.println("Save Files to Zenodo");
        
        String url = null;
        url = (String)session.getAttribute("urltoreturn");
        
        System.out.println("urare here = " + url);
        
        String tempName = (String)session.getAttribute("filename");
        String type = "dataset";
        String access = "open";
        String file = null;
        String inputPath = (String)session.getAttribute("inputpath");
        //String filename = null;
        
        Object [][] exportData = null; 

        if (url.equals("mydataset.html")){       
            file = inputPath + "/" +tempName;
            System.out.println("edwwwwwwwwwwwwwwwww");
        }
        else{
            System.out.println("anonymizeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            AnonymizedDataset anonData = (AnonymizedDataset)session.getAttribute("anondata");
            System.out.println("Export anonymizedDataset...");
            exportData = anonData.exportDataset(inputPath + "/anonymize" +tempName, true);
            //filename = "anonymize" +tempName;
            file = inputPath + "/anonymize" +tempName;
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
    public @ResponseBody void getSetData ( HttpSession session   ) throws FileNotFoundException, IOException {
            
        Data data = null;
        String rootPath = (String)session.getAttribute("inputpath");
        String filename = (String)session.getAttribute("filename");
        
       
	File dir = new File(rootPath);
     
        String fullPath = dir + "/" + filename;
        
        data = new SETData(fullPath,",");
        data.readDataset(null, null);
        session.setAttribute("data", data);        
        
    }
    
    
    
    @RequestMapping(value="/action/getpairranges", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody Pair getPairRanges (@RequestParam("columnAttr") int columnAttr,@RequestParam("vartype") String vartype, HttpSession session)  {
        Data data = (Data) session.getAttribute("data");
        Pair p = null;
               
        if (vartype.equals("date")){
            p = new Pair(data,vartype);
        }
        else{
            p = new Pair(data,null);
        }
        p.findMin(columnAttr);

        return p;
    }
    
    
    @RequestMapping(value="/action/addnodehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void addNodeHier (@RequestParam("newnode") String newNode, @RequestParam("parent") String parent, @RequestParam("hiername") String hierName, HttpSession session) throws ParseException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
        
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        h = hierarchies.get(hierName);
        
        
        if ( h.getNodesType().equals("string")){
            h.add(newNode, parent);
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
                    RangeDate newNodeDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempNew[0],true),((HierarchyImplRangesDate) h).getDateFromString(tempNew[1],false));
                    RangeDate parentNodeDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempParent[0],true),((HierarchyImplRangesDate) h).getDateFromString(tempParent[1],false));
                    h.add(newNodeDate, parentNodeDate);
                }
                else{
                    RangeDouble newNodeRange = new RangeDouble(Double.parseDouble(tempNew[0]),Double.parseDouble(tempNew[1]));
                    newNodeRange.setNodesType(h.getNodesType());
                    RangeDouble parentNodeRange = new RangeDouble(Double.parseDouble(tempParent[0]),Double.parseDouble(tempParent[1]));
                    parentNodeRange.setNodesType(h.getNodesType());
                
                    h.add(newNodeRange, parentNodeRange);
                }
            }
            else{
                h.add(Double.parseDouble(newNode), Double.parseDouble(parent));
            }
        }
         
    }
    
    @RequestMapping(value="/action/editnodehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void editNodeHier (@RequestParam("oldnode") String oldNode, @RequestParam("newnode") String newNode, @RequestParam("hiername") String hierName, HttpSession session) throws ParseException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        h = hierarchies.get(hierName);
        
        if ( h.getNodesType().equals("string")){
            h.edit(oldNode, newNode);
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
                    RangeDate newDateNode = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempNew[0],true), ((HierarchyImplRangesDate) h).getDateFromString(tempNew[1],false));
                    RangeDate oldDateNode = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(tempOld[0],true), ((HierarchyImplRangesDate) h).getDateFromString(tempOld[1],false));
                    
                    h.edit(oldDateNode, newDateNode);
                }
                else{ // range double

                    RangeDouble newNodeRange = new RangeDouble(Double.parseDouble(tempNew[0]),Double.parseDouble(tempNew[1]));
                    RangeDouble oldNodeRange = new RangeDouble(Double.parseDouble(tempOld[0]),Double.parseDouble(tempOld[1]));
                    newNodeRange.setNodesType(h.getNodesType());
                    oldNodeRange.setNodesType(h.getNodesType());


                    h.edit(oldNodeRange, newNodeRange);
                }
            }
            else{ // distinct
                h.edit(Double.parseDouble(oldNode), Double.parseDouble(newNode));
            }
        }   
    }
    
    
    @RequestMapping(value="/action/deletenodehier", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody void delNodeHier (@RequestParam("deletenode") String delnode,@RequestParam("hiername") String hierName, HttpSession session) throws ParseException  {
        Map<String, Hierarchy> hierarchies  = null;
        Hierarchy h = null;
       
        
        hierarchies = (Map<String, Hierarchy>) session.getAttribute("hierarchies");
        
        h = hierarchies.get(hierName);
        
        if ( h.getNodesType().equals("string")){
            h.remove(delnode);
        }
        else{
            if(h.getHierarchyType().equals("range")){
                String del = "-";
                
                if (delnode.contains(" ")){
                    delnode = delnode.replaceAll(" ", "");
                }
                
                String []temp = delnode.split(del);
                
                if(h.getNodesType().equals("date")){
                    RangeDate delDate = new RangeDate(((HierarchyImplRangesDate) h).getDateFromString(temp[0], true),((HierarchyImplRangesDate) h).getDateFromString(temp[1], false));
                    h.remove(delDate);
                }
                else{
                
                    RangeDouble delRange = new RangeDouble(Double.parseDouble(temp[0]),Double.parseDouble(temp[1]));
                    
                    h.remove(delRange);
                }
            }
            else{
                h.remove(Double.parseDouble(delnode));
            }
        }

    }
    
    @RequestMapping(value="/action/findsolutionstatistics", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String[] findSolutionStatistics ( HttpSession session) throws ParseException  {
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        String selectedNode = (String) session.getAttribute("selectednode");
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
        Map<Integer, Hierarchy> quasiIdentifiers = new HashMap<Integer, Hierarchy>();
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
                            if(entry.getKey().toString().equals(selectedAttrNames)){
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
                    percentageSuppress = Double.valueOf(df.format(percentageSuppress));
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
        
        System.out.println("SupresssValues//////////////////////////////////////////");
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
        
        System.out.println("//////endddddddddddddddddddd supressssss////////////////");
        
        
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
         
        
        System.out.println("source statistics");
        
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
                    percentageSuppress = Double.valueOf(df.format(percentageSuppress));
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
        System.out.println("Attribute = " + attributes);
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
        
        System.out.println("k = " + k);
       

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
    public @ResponseBody void restart ( HttpSession session)  {
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
        System.out.println("dimitris");
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
        
        
        System.out.println("Identifiers");
        for ( int i = 0 ; i < identifiersArray.length ; i ++){
            System.out.println(identifiersArray[i]);
            if (identifiersArray[i].equals("null")){
                System.out.println("xaxaxaxaxaxaxa");
                identifiersArr[i] = null;
            }
            else{
                identifiersArr[i] = identifiersArray[i];
            }
        }
        
        System.out.println("MinArr");
        for ( int i = 0 ; i < minArray.length ; i ++){
            System.out.println(minArray[i]);
            if ( minArray[i].equals("null")){
                minArr[i] = Double.NaN;
            }
            else{
                minArr[i] = Double.parseDouble(minArray[i]);
            }
        }
        
        System.out.println("MaxArr");
        for ( int i = 0 ; i < maxArray.length ; i ++){
            System.out.println(maxArray[i]);
            if ( maxArray[i].equals("null")){
                maxArr[i] = Double.NaN;
            }
            else{
                maxArr[i] = Double.parseDouble(maxArray[i]);
            }
        }
        
        
        System.out.println("distinct");
        for ( int i = 0 ; i < distinctArr.length ; i ++){
            if (distinctArr[i].equals("null")){
                distinctArr[i] = null;
            }
            System.out.println(distinctArr[i]);
        }
        
        System.out.println("anon" );
        int []level = anonData.getHierarchyLevel();
        for( int  i = 0 ;i < level.length ; i ++){
            System.out.println(level[i]);
        }
        
        Queries queries = new Queries(identifiersArr, minArr, maxArr , distinctArr, hierarchies, data, anonData.getHierarchyLevel(), quasiIdentifiers);
        Results results = queries.executeQueries();
        
        resultArr = new double[4];
        resultArr[0] = Double.parseDouble(results.getNonAnonymizeOccurrences());
        resultArr[1] = Double.parseDouble(results.getAnonymizedOccurrences());
        resultArr[2] = Double.parseDouble(results.getPossibleOccurences());
        resultArr[3] = Double.parseDouble(results.getEstimatedRate());
        
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
        
        if (data.getClass().toString().contains("SET")){
            return "set";
        }
        else{
            return"txt";
        }
        
    }
    
    
    @RequestMapping(value="/action/savenonymizationrules") //method = RequestMethod.POST
    public @ResponseBody String saveAnonynizationRules ( HttpSession session, HttpServletResponse response ) throws FileNotFoundException, IOException, ParseException {
        Data data = (Data) session.getAttribute("data");
        Map<Integer, Hierarchy> quasiIdentifiers = (Map<Integer, Hierarchy>)session.getAttribute("quasiIdentifiers");
        String selectedNode = null;
        String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        String file = inputPath +"/anonymized_rules_"+filename;
        
        AnonymizationRules anonRules = new AnonymizationRules();
        if(data instanceof SETData){
            Map<Double, Double> results = (Map<Double, Double>) session.getAttribute("results");    
            anonRules.export(file, data, results, quasiIdentifiers);
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
        File anonFile = new File(inputPath +"/anonymized_rules_"+filename);
        
        InputStream myStream = new FileInputStream(file);

	// Set the content type and attachment header.
	response.addHeader("Content-disposition", "attachment;filename="+anonFile.getName());
	response.setContentType("txt/plain");

	// Copy the stream to the response's output stream.
	IOUtils.copy(myStream, response.getOutputStream());
	response.flushBuffer();
        
        return null;
    }
    
    
    @RequestMapping(value="/action/loadanonymizationrules", method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String loadAnonynizationRules ( HttpSession session , String filename) throws FileNotFoundException, IOException {
        System.out.println("load anon rules");
        Data data = (Data) session.getAttribute("data");
        System.out.println("fileName = " + filename);
        //String filename = (String)session.getAttribute("filename");
        String inputPath = (String)session.getAttribute("inputpath");
        //String anonRulesFile = inputPath +"/anonymized_rules_"+filename;
        String anonRulesFile = inputPath + "/"+filename;
        AnonymizationRules anonRules = new AnonymizationRules();
        
        System.out.println("inputPath = " + inputPath);
        
        anonRules.importRules(anonRulesFile);
        Map<String, Map<String, String>> rules = anonRules.getAnonymizedRules();
              
        
       
        
        for (Map.Entry<String, Map<String, String>> entry : rules.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        
        session.setAttribute("anonrules",rules);
        
        
        /*if(data instanceof SETData){
            this.anonymizedDatasetPanel2.anonymizeSETWithImportedRules(anonyRules.getAnonymizedRules());
        }
        else{
            this.anonymizedDatasetPanel2.anonymizeWithImportedRules(anonyRules.getAnonymizedRules());
        }*/
        
                
        
        return null;
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
       
        
        String algorithm = (String) session.getAttribute("algorithm");
        System.out.println("algorithmmmmmmmmmmm = " + algorithm);
        
        if ( algorithm.equals("flash")){
             if (data != null ){
                if (graph != null){
                    check.setOriginalExists("true");
                }
                else if (allRules != null){
                    check.setOriginalExists("true");
                }
                else if (toSuppress != null){
                    check.setOriginalExists("true");
                }
                else{
                    check.setOriginalExists("noalgo");
                }
            }
        
            if(selectednode != null || allRules != null){
                check.setAnonExists("true");
            }
        }
        else{
            if (data != null ){
                if (session.getAttribute("results") != null){
                    check.setOriginalExists("true");
                    check.setAnonExists("true");
                }
                //else if (allRules != null){
                //    check.setOriginalExists("true");
                //}
                //else if (toSuppress != null){
                 ///   check.setOriginalExists("true");
                //}
                //else if (results != null){
                 //   check.setOriginalExists("true");
                //}
                else{
                    check.setOriginalExists("noalgo");
                }
            }
        
        //if(selectednode != null || allRules != null){
            //if(selectednode != null || results != null){
                
            //}
        //}
        }
        
        
                
        
        /*if (data != null ){
            if (graph != null){
                check.setOriginalExists("true");
            }
            //else if (allRules != null){
            //    check.setOriginalExists("true");
           // }
            //else if (toSuppress != null){
             ///   check.setOriginalExists("true");
            //}
            else if (results != null){
                check.setOriginalExists("true");
            }
            else{
                check.setOriginalExists("noalgo");
            }
        }
        
        //if(selectednode != null || allRules != null){
        if(selectednode != null || results != null){
            check.setAnonExists("true");
        }*/
        
        
        return check;
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
    
    ///////////////////////////////api my-health my-data/////////////////////////////////////////////////////
    
    @RequestMapping(value="/anonymizedata", produces = MediaType.TEXT_PLAIN, method = RequestMethod.POST)
    public void AnonimizeData(@RequestParam("files") MultipartFile[] files, @RequestParam("del") String del,  MultipartHttpServletRequest request,  HttpSession session, HttpServletResponse response) throws IOException, FileNotFoundException, ParseException{
         
        this.upload(files[0], true, session);
        String errorMessage;
        String path = (String) session.getAttribute("inputpath");
        String filename = (String) session.getAttribute("filename");
        
        
        if(del.equals("s")){
            del = ";";
        }
        System.out.println("Del fiuwtyfuirwfgyrefgweygfy: "+del);
 
        if(files.length==1 || files[1]==null){
            Data dataset = this.getSmallDataSet(del, "tabular", session);
            String[][] types = dataset.getTypesOfVariables(dataset.getSmallDataSet());
            File templateFile = new File(path+File.separator+"template.txt");
            BufferedWriter out = new BufferedWriter(new FileWriter(templateFile.getAbsolutePath(),false));
            
            File dataFile = new File(path+File.separator+filename);
            FileInputStream fileStream = new FileInputStream(dataFile);
            DataInputStream inData = new DataInputStream(fileStream);
            BufferedReader br = new BufferedReader(new  InputStreamReader(inData));

            String firstLine = br.readLine();
            br.close();
            
            String splitLine[] = firstLine.split(del);
            out.write("////////////////////// check columns, vartypes /////////////////////////////");
            out.newLine();
            for(int i=0; i<splitLine.length; i++){
                out.write(splitLine[i]+": true,"+types[i][0]);
                out.newLine();
                
            }
            out.write("//////////////////// END ////////////////////////////////////////////\n\n");
            out.write("\n" +
                      "/////////////////// set k /////////////////////////////////////\n\n"+
                      "k:\n");
            out.close();

            InputStream in = new FileInputStream(templateFile);
            FileCopyUtils.copy(in, response.getOutputStream());
            
           
             
//            HttpHeaders headers = new HttpHeaders();
//            return ResponseEntity
//            .status(HttpStatus.OK)
//            .body("Ola good");
//            response.getOutputStream().flush();
           
        }
        else{
           this.upload(files[1], false, session);
           File templ = new File(path+File.separator+files[1].getOriginalFilename());
           
           for(int i=2; i<files.length; i++){
               this.hierarchy(files[i], false, session);
           }
           
           FileInputStream fileStream = new FileInputStream(templ);
           DataInputStream inData = new DataInputStream(fileStream);
           BufferedReader br = new BufferedReader(new  InputStreamReader(inData));
           
           String strline; 
           List<String> vartypesArr,relationsArr;
           List<Boolean> checkColumnsArr;
           int k=0;
           
           vartypesArr = new ArrayList<String>();
           relationsArr = new ArrayList<String>();
           checkColumnsArr = new ArrayList<Boolean>();
           
           ArrayList<String> possibleTypes = new ArrayList(){{ add("int"); add("double"); add("date"); add("string"); }};
           String error_msg="";
           
           while((strline = br.readLine()) != null){
               if(strline.contains("END") || strline.length()<=1){
                   continue;
               }
               else if(strline.contains("check columns, vartypes")){
                   while(!(strline = br.readLine()).contains("END")){
                       String columnInfo[] = strline.split(":");
                       String attributes[] = columnInfo[1].replaceAll("\n", "").replaceAll(" ", "").split(",");
                       
                       System.out.println("length attr: "+attributes.length);
                       if(attributes.length>1 && attributes.length<=3){
                           if(!attributes[0].equals("true") && !attributes[0].equals("false")){
                               error_msg += "In "+columnInfo[0]+": bollean type must be true or false.\n";
                           }                           
                           checkColumnsArr.add ( attributes[0].equals("true"));
                           
                           if(!possibleTypes.contains(attributes[1])){
                              error_msg += "In "+columnInfo[0]+": not accepted variable type. It must be one of the "+Arrays.toString(possibleTypes.toArray())+"\n"; 
                           }
                           vartypesArr.add(attributes[1]); 
                           
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
            
            System.out.println("Results: k->"+k+" vartypes-> "+Arrays.toString(vartypesArr.toArray(new String[vartypesArr.size()]))+" checkColumns-> "+Arrays.toString(checkColumnsArr.toArray())+" relations-> "+Arrays.toString(relationsArr.toArray()));
            this.getSmallDataSet(del, "tabular", session);
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
//        response.setContentType("text/plain");
//        response.getOutputStream().println("katiksgsf");
//        return ResponseEntity
//            .status(HttpStatus.FORBIDDEN)
//            .body("Error Message");
       
    }
    

    
     /*this.anonymize(k, 0, "pFlash", relationsArr.toArray(new String[relationsArr.size()]), session);
            this.getAnonDataSet(0, 0, session);
            this.saveAnonymizeDataset(session, response);*/
    
    
    //amnesia/dataset
    @RequestMapping(value="/dataset", produces = "application/json",  method = RequestMethod.POST)//, method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String Dataset ( @RequestParam("file") MultipartFile file, @RequestParam("data") boolean data, @RequestParam("del") String del, @RequestParam("datatype") String datatype , @RequestParam("vartypes") String [] vartypes, @RequestParam("checkColumns") boolean [] checkColumns, HttpSession session  ) throws FileNotFoundException, IOException {
        
        this.upload(file, data, session);
        this.getSmallDataSet(del, datatype, session);
        this.loadDataset(vartypes, checkColumns, session);
        
        return null;
    }
            
    
    //amnesia/hierarchy
    @RequestMapping(value="/hierarchy",  method = RequestMethod.POST)//, method = RequestMethod.POST) //method = RequestMethod.POST
    public @ResponseBody String hierarchy ( @RequestParam("file") MultipartFile file, @RequestParam("data") boolean data,  HttpSession session ) throws FileNotFoundException, IOException {
        
        System.out.println("fileeeename = " +file.getName());
        System.out.println("file = " + file);
        
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
            if (h.getHierarchyType().equals("range")){
                checkHier = h.checkHier();
            }
            
            
            //provlima stin  hierarchia
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
        else if(algorithmSelected.equals("kmAnonymity") || algorithmSelected.equals("apriori") ||
                algorithmSelected.equals("AprioriShort")){
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
                quasiIdentifiers.get(0).buildDictionary(data.getDictionary(0));
            }

            session.setAttribute("algorithm", "apriori");

        }


        algorithm.setDataset(data);
        algorithm.setHierarchies(quasiIdentifiers);

        algorithm.setArguments(args);


        System.out.println("k = " + k + "\t m = " + m );

        //long startTime = System.currentTimeMillis();
//        long startCpuTime = getCpuTime();

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
            System.out.println("result set = " + algorithm.getResultSet() );

            session.setAttribute("results", algorithm.getResultSet());
            if(!algorithmSelected.equals("apriori")){
                Graph graph = algorithm.getLattice();

                session.setAttribute("graph", graph);
            }
        }



        //long endTime   = System.currentTimeMillis();
//        long endCpuTime = getCpuTime();

        //long totalTime = endTime - startTime;
//        long totalCpuTime = endCpuTime - startCpuTime;

        //System.out.println("real time: " + totalTime /*+"\ncpu time: " + totalCpuTime);



        result = true;



        return "ok\n";
    }    
      
    
    ///////////////action/anondataexists/////////////////////other functions//////////////////////////////////
    
    
    
    
    private List<String> findHierarchyType(String file){
        List<String> result = new ArrayList<>();
        BufferedReader br;
        String line;
        try {
            br = new BufferedReader(new FileReader(file));
            
            while ((line = br.readLine()) != null) {
                System.out.println(line);
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
        } catch (IOException ex) {
            System.out.println("problem");
            //Logger.getLogger(HierarchyPanel.class.getName()).log(Level.SEVERE, null, ex);
            //error
        }
        
        return result;
    }
    
}


