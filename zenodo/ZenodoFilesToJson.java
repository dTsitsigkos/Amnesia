/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zenodo;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author jimakos
 */
public class ZenodoFilesToJson {
    private ArrayList<ZenodoFile> data;
    private Map<Integer, ZenodoFile> files;
    private boolean COMPARE;
    private int recordsTotal;
    private int recordsFiltered;
    private ZenodoFile zenFile = null;
    private String fileName = null;
    private String title = null;
    private String []keywords = null;
    private String inputPath = null;
    

    public ZenodoFilesToJson(Map<Integer, ZenodoFile> _files, boolean _COMPARE, String _fileName, String _title, String _keywords, String _inputPath) throws IOException{
        this.files = _files;
        this.COMPARE = _COMPARE;
        this.fileName = _fileName;
        this.title = _title;
        if( _keywords != null ){
            if ( _keywords.contains(",")){
                this.keywords = _keywords.split(",");
            }
            else{
                keywords = new String[1];
                this.keywords[0] = _keywords;
            }
        }
        this.inputPath = _inputPath;
        this.compareFiles();
        
    }
    
    public void compareFiles() throws IOException{
        data = new ArrayList<ZenodoFile>();
        if (COMPARE == false){//all files
            for (Map.Entry<Integer, ZenodoFile> entry : files.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
                zenFile=entry.getValue();
                zenFile.setPercentage(null);
                data.add(entry.getValue());
            }

        }
        else{//common files
            double filenamePercentage = 0.5;
            double titlePercentange = 0.25;
            double keywordsPercentage = 0.25;
            double fileNameSimilarity = 0.0;
            double titleSimilarity = 0.0;
            double keywordsSimilarity = 0.0;
            double similarityPercentage = 0.0;
            
            File file = new File(inputPath);
            HashCode md5 = Files.hash(file, Hashing.md5());
            byte[] md5Bytes = md5.asBytes();
            String checksum = md5.toString();
            
            
            for (Map.Entry<Integer, ZenodoFile> entry : files.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
                zenFile=entry.getValue();

                //System.out.println("fileName = " + zenFile.getFileName() + "\tchecksum = " + zenFile.getChecksum());
                
                System.out.println("my checksum = " + checksum + "\t checksum = " + zenFile.getChecksum());

                
                if( !checksum.equals(zenFile.getChecksum())){
                    fileNameSimilarity = this.wordSimilarity(fileName, zenFile.getFileName(),true);
                    titleSimilarity = this.wordSimilarity(title, zenFile.getTitle(),false);
                    keywordsSimilarity = this.jaccardDistance(keywords, zenFile.getKeywords());
                    similarityPercentage = filenamePercentage*fileNameSimilarity + titlePercentange*titleSimilarity + keywordsPercentage*keywordsSimilarity ;
                }
                else{
                    similarityPercentage = 1.0;
                }
                
                //zenFile.setPercentage(null);
                //System.out.println("similarity = " + similarityPercentage);
                
                if (similarityPercentage >= 0.0){
                    zenFile.setPercentage((int)(similarityPercentage*100) + "%");
                    data.add(entry.getValue());
                }
            
            }
        
        }
        recordsTotal = data.size();
        recordsFiltered = data.size();
    }
    
    
    public ArrayList<ZenodoFile> getData() {
        return data;
    }

    
    public double jaccardDistance(String[] keywordsFile, String keywordsZenodoFile){
        double jaccDist = 0.0;
        double jaccSim = 0.0;
        double union = 0.0;
        double intersection = 0.0;
        String []tempKeywords = null;
       // System.out.println("jaccccarrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrdddddddddddddddd");
       
        if (keywordsZenodoFile.equals("")){
            return jaccDist;
        }
        else {
            if ( keywordsZenodoFile.contains(",")){
                tempKeywords = keywordsZenodoFile.split(",");
                union = keywordsFile.length + tempKeywords.length ;
            }
            else{
                union = keywordsFile.length + 1 ;
            }
            for ( int i = 0 ; i < keywordsFile.length ; i ++ ){
                if (keywordsZenodoFile.contains(keywordsFile[i])){
                    intersection ++;
                }
            }

            union = union - intersection;
            jaccSim = (union - intersection)/union;
            jaccDist = 1 - jaccSim; 

            //System.out.println("union = " + union);
            //System.out.println("intersection = " + intersection);
            //System.out.println("jaccSim = " + jaccSim);
            //System.out.println("jaccDist = " + jaccDist);
        }    
            

        return jaccDist;
    }
    
    public double wordSimilarity(String strFile, String strZenodoFile, boolean FILENAME){
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        double wordSim = 0.0;
        
        if ( FILENAME == true){
            String []temp = null;
            if (strFile.contains(".")){
                temp = strFile.split("\\.");
                strFile = temp[0];
            }
            
            if (strZenodoFile.contains(".")){
                temp = strZenodoFile.split("\\.");
                strZenodoFile = temp[0];
            }
            
        }
        
        strFile = strFile.replaceAll(" ", "");
        strZenodoFile = strZenodoFile.replaceAll(" ", "");
        
        strFile = strFile.toLowerCase();
        strZenodoFile = strZenodoFile.toLowerCase();
        
        wordSim = 1-l.distance(strFile, strZenodoFile);
        //System.out.println("strFile = " + strFile + "\tstrZenodo = " + strZenodoFile + "\tsimilarity = " + wordSim);
        return wordSim;
    }
   

  
    
    
}
