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
package anonymizationrules;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import data.Data;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import org.springframework.util.StringUtils;


/**
 * Class to keep anonymization rules
 * @author jimakos
 */
public class AnonymizationRules {
    Map<String, Map<String,String>> anonymizedRules = null;

    public AnonymizationRules (){
        this.anonymizedRules = new HashMap<String,Map<String,String>>();
    }
    
    
    /**
     * Imports anonymization rules from file
     * @param inputFile
     * @return returns false in case of an error
     * @throws IOException
     */
    public boolean importRules(String inputFile) throws IOException{
        String del = "->";
        String []temp = null;
        Map<String,String> rules = null;
        String columnName = null;
        
        System.out.println("inputfile = " + inputFile);
        
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;
        
        if ((line = br.readLine()) != null){
            if (!line.equals("Anonymization rules:")){
                return false;
            }
        }
        while ((line = br.readLine()) != null) {
            columnName = line;
            rules = new HashMap<>();
            while ((line = br.readLine()) != null) {
                if ( !line.equals("")){
                    temp = line.split(del);
                    rules.put(temp[0], temp[1]);
                }
                else{
                    break;
                }
            }
            anonymizedRules.put(columnName, rules);
        }
        
        br.close();
        return true;
    }
    
    /**
     * Creates rules for a specified column
     * @param column column to create rules for
     * @param initTable initial table
     * @param anonymizedTable anonymized table
     * @param qids
     * @param suppressedValues
     * @return a map with keys the initial values and value the anonymized values
     */
    public  Map<String, String> createRules(Integer column, Data data, int[] qids, Map<Integer, Set<String>> suppressedValues,Map<Integer, Hierarchy> quasiIdentifiers, int level) throws ParseException{
        Map<String,String> map = new HashMap<>();
        Object[] qidsRow = new Object[qids.length];
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        int []hierarchyLevel = null;
        double [][]dataset = data.getDataSet();
        Object columnName = null;
        int [] transformation = null; 
        Object nonAnonymizedData = null;
        Object anonymizedData = null;
        Object tempData = null;
        colNamesType = data.getColNamesType();
        colNamesPosition = data.getColNamesPosition();
        dictionaries = data.getDictionary();

        hierarchyLevel = new int[dataset[0].length];
        for (int i = 0 ; i < hierarchyLevel.length ; i++){
            hierarchyLevel[i] = 0;
        }

        columnName = colNamesPosition.get(column);
        Hierarchy hierarchy = quasiIdentifiers.get(column);

        if(colNamesType.get(column).contains("int")){   
            for(int line=0; line<dataset.length; line++){
                nonAnonymizedData = new Object(); 
                anonymizedData = new Object();
                tempData = new Object();
                tempData = dataset[line][column];

                if ((double) tempData == 2147483646.0) {
                    nonAnonymizedData = "";
                }
                else {
                    Double num = (Double)tempData;
                    nonAnonymizedData = num.intValue();
                }
               
                anonymizedData = anonymizeValue(tempData, hierarchy, level);
                if ( !hierarchy.getHierarchyType().equals("range")) {
                    Double num = (Double)anonymizedData;
                    anonymizedData = num.intValue();
                }
                else{
                    anonymizedData = anonymizedData.toString();
                }
                
                map.put(nonAnonymizedData.toString(), anonymizedData.toString());
            }
        }
        else if(colNamesType.get(column).contains("double")){
            for(int line=0; line<dataset.length; line++){
                nonAnonymizedData = new Object(); 
                anonymizedData = new Object();
                tempData = new Object();
                tempData = dataset[line][column];
                nonAnonymizedData = dataset[line][column];
                
                if ( tempData.equals(Double.NaN)){
                    nonAnonymizedData = "";
                }
                
                anonymizedData = anonymizeValue(tempData, hierarchy, level);
                if ( hierarchy.getHierarchyType().equals("range")) {
                    anonymizedData = anonymizedData.toString();
                }
                                
                map.put(nonAnonymizedData.toString(), anonymizedData.toString());
            }
        }
        else{
            DictionaryString dictionary = dictionaries.get(column);
            for(int line=0; line<dataset.length; line++){
                nonAnonymizedData = new Object(); 
                anonymizedData = new Object();
                tempData = new Object();
                tempData = dataset[line][column];
                Double d = (Double)dataset[line][column];
                nonAnonymizedData = dictionary.getIdToString(d.intValue());
                
                
                //System.out.println("pipaaaaaaaaaaaaaaaaaaaaaaaaaaa = " + nonAnonymizedData.toString());
                if ( ((String)nonAnonymizedData).equals("NaN")){
                    nonAnonymizedData = "";
                    tempData = "NaN";
                }
                else{
                    tempData = nonAnonymizedData;
                }
                
                Map<Integer, ArrayList<String>> allParents =  hierarchy.getAllParents();
                
                //for (Map.Entry<Integer, ArrayList<String>> entry : allParents.entrySet()) {
                //    System.out.println("key = " + entry.getKey() + " , value = " + entry.getValue().toString());
                //}
                
                //System.out.println("tempData = " + tempData);
                
                
                anonymizedData = anonymizeValue(tempData, hierarchy, level);
               
                
                
                
                map.put(nonAnonymizedData.toString(), anonymizedData.toString());
            }
        }
        
        return map;
    }
    
    private static boolean isSuppressed(Object[] data, int[] qids, Map<Integer, Set<String>> suppressedValues){
        
        if (suppressedValues == null)
            return false;
        
        Object[] checkArr = new Object[1];
        
        //check for each and every qid if is suppressed
        for(int i=0; i<qids.length; i++){
            Set<String> suppressed = suppressedValues.get(qids[i]);
            if(suppressed != null){
                checkArr[0] = data[i];
                if(suppressed.contains(java.util.Arrays.toString(checkArr)))
                    return true;
            }
        }
        
        //check for all qids combined
        Set<String> suppressed = suppressedValues.get(-1);
        if(suppressed != null){
            if(suppressed.contains(java.util.Arrays.toString(data))){
                return true;
            }
        }
        
        return false;
    }
    
    public Map<String, Map<String, String>> getAnonymizedRules() {
        return anonymizedRules;
    }
    
    
    public void setAnonymizedRules(Map<String, Map<String, String>> anonymizeRules) {
        this.anonymizedRules = anonymizeRules;
    }
    
    public void setColumnRules(String columnName, Map<String, String> rules){
        this.anonymizedRules.put(columnName, rules);
    }
    
    public void print(){
        for(String s1 : this.anonymizedRules.keySet()){
            System.out.println(s1);
            Map<String, String> map = this.anonymizedRules.get(s1);
            for(String s2 : map.keySet()){
                System.out.println(s2 + " -> " + map.get(s2));
            }
            System.out.println();
        }
    }
    
    public Set<String> getKeyset(){
        return this.anonymizedRules.keySet();
    }
    
    /**
     * Gets the anonymized value for a specific value in a column
     * @param column the column of the value
     * @param key the value for which to return the anonymized value
     * @return the anonymized value of key
     */
    public String get(String column, String key){
        Map<String, String> map = this.anonymizedRules.get(column);
        if(map == null){
            return null;
        }
        
        return map.get(key);
    }
    
    public void export(String file, Data data, int[] qids,Map<Integer, Set<String>> suppressedValues, Map<Integer, Hierarchy> quasiIdentifiers, String solutionNode) throws ParseException{
        int[] transformation = null;
        
        if ( solutionNode != null){
            if (solutionNode.contains(",")){
                String[]temp = solutionNode.split(",");
                transformation = new int[temp.length];
                for ( int i = 0 ; i < temp.length ; i ++){
                    transformation[i] = Integer.parseInt(temp[i]);
                }
            }
            else{
                transformation = new int[1];
                transformation[0] = Integer.parseInt(solutionNode);
            }
        }
        
        try {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("Anonymization rules:");
                int counter = 0;
                for(Integer qid : qids){
                    
                    //create rules for the column
                    String columnName = data.getColumnByPosition(qid);
                    
                    Map<String, String> map = createRules( qid, data, qids, suppressedValues, quasiIdentifiers,transformation[counter]);
                    
                    //write rules to file
                    writer.println(columnName);
                    String del = "->";
                    
                    for (Map.Entry<String, String> entry : map.entrySet()){
                        writer.print(entry.getKey()  + del + entry.getValue() + "\n");
                    }
                    
                    counter++;
                    writer.println();
                }
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
           // Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * export anonymization rules for set-valued data
     * @param file
     * @param dataset
     * @param rules
     * @param qis
     */
    public void export(String file, Data dataset, Map<Double, Double> rules, Map<Integer, Hierarchy> qis){
        try {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("Anonymization rules:");
                DictionaryString dict = qis.get(0).getDictionary();
                String columnName = dataset.getColumnByPosition(0);
                writer.println(columnName);
                
                String del = "->";
                int counter = 1;
                for(Entry<Double, Double> entry : rules.entrySet()){
                    writer.println(dict.getIdToString(entry.getKey().intValue()) +
                    del + dict.getIdToString(entry.getValue().intValue()));
                    counter ++;
                }
                writer.close();
                
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Object anonymizeValue(Object value, Hierarchy h, int level) throws ParseException{
        Object anonymizedValue = value;
        
       // System.out.println("anonymizedValueeeeeee = " + anonymizedValue + ", value = " + value);
        
        for(int i=0; i<level; i++){
            if(h.getHierarchyType().equals("range")){
                if(h.getNodesType().equals("double") ||  h.getNodesType().equals("int")){
                    if ( i ==0 ){
                        if ( (double) value == 2147483646.0 ||  value.equals(Double.NaN)){
                            Map<Integer, ArrayList<RangeDouble>> x = h.getAllParents();
                            ArrayList<RangeDouble> newList = x.get(x.size()-1);
                            if(newList.size() != 1){
                                anonymizedValue = newList.get(0);
                            }
                            else{
                                anonymizedValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
                            }
                        }
                        else{
                            anonymizedValue = h.getParent((Double)anonymizedValue);
                        }    
                    }
                    else{
                        anonymizedValue = h.getParent(anonymizedValue);
                    }
                }
                else if (h.getNodesType().equals("date") ){
                    Date d = null;
                    RangeDate rd = null;
                    
                    if (!value.toString().contains("-")){
                         if (value.toString().equals("NaN") || value.toString().equals("(null)")){
                            d = null;
                        }
                        else{
                            d = getDateFromString(value.toString());
                        }    
                    }
                    else{
                        rd = (RangeDate) value;
                    }
                    //System.out.println("rowValue = " + rowValue +"\tdate = " + d.toString());
                    ////////////////////////////////////////////////
                    if (d != null){
                        if ( i ==0 ){
                            //System.out.println("mpika");
                            if ( d == null){
                                //System.out.println("mpika2222");
                                Map<Integer, ArrayList<RangeDate>> x = h.getAllParents();
                                ArrayList<RangeDate> newList = x.get(x.size()-1);
                                if(newList.size() != 1){
                                    anonymizedValue = newList.get(0);
                                }
                                else{
                                    anonymizedValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
                                }
                            }
                            else{
                                //System.out.println("mpika3333");
                                //System.out.println("edwwwwwwwwwwwwwwwww = " + d.toString());
                                anonymizedValue = h.getParent(d);
                            }    
                        }
                        else{
                            //System.out.println("mpika222222222222");
                            anonymizedValue = h.getParent(anonymizedValue);
                        }
                    }
                    else{
                        if ( i ==0 ){
                            //System.out.println("mpika");
                            if ( rd == null){
                                //System.out.println("mpika2222");
                                Map<Integer, ArrayList<RangeDate>> x = h.getAllParents();
                                ArrayList<RangeDate> newList = x.get(x.size()-1);
                                if(newList.size() != 1){
                                    anonymizedValue = newList.get(0);
                                }
                                else{
                                    anonymizedValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
                                }
                            }
                            else{
                                //System.out.println("mpika3333");
                               // System.out.println("edwwwwwwwwwwwwwwwww = " + rd.toString());
                                anonymizedValue = h.getParent(rd);
                            }    
                        }
                        else{
                            //System.out.println("mpika222222222222");
                            anonymizedValue = h.getParent(anonymizedValue);
                        }
                    }
                }
            }
            else{
                if ( value instanceof Double){ //|| value.toString().equals("NaN") ){
                    if ( ((double) value == 2147483646.0 ||  value.equals(Double.NaN)) && level == 0){
                        return "(null)";
                    }
                    else{
                        anonymizedValue = h.getParent(anonymizedValue);
                    }
                }
                else if (value instanceof String){
                   // System.out.println("valueeeeeeeeeeeee = " + value);
                    if ( ((String)value).equals("NaN") && level == 0 ){
                        //System.out.println("11111111111111111");
                        return "(null)";
                    }
                    else{
                        //System.out.println("22222222222222222");
                       // System.out.println("anonymizedValueeeeeee = " + anonymizedValue + ", value = " + value);
                        anonymizedValue = h.getParent(anonymizedValue);
                        //System.out.println("anonymizedValue = " + anonymizedValue);
                    }
                }
                
            }
        }  
        
        if(h.getHierarchyType().equals("range")){
            if(h.getNodesType().equals("date")) {
                RangeDate rd = (RangeDate)anonymizedValue;
                /*if (level == 3){
                    anonymizedValue = rd.dateToString(0);
                }
                else if (level == 2){
                    anonymizedValue = rd.dateToString(1);
                }
                else if (level == 1){
                    anonymizedValue = rd.dateToString(2);
                }*/

                
                int translateLevel = h.translateDateViaLevel(h.getHeight()-level);
                anonymizedValue = rd.dateToString(translateLevel);

            }
        }
        
        return anonymizedValue;
    }
    
    public Date getDateFromString(String tmstmp) throws ParseException{
        Date d = null;
        SimpleDateFormat sf = null;
        
        if ( !tmstmp.equals("NaN")){
        
            if (tmstmp.contains("/")){
                int num = StringUtils.countOccurrencesOf(tmstmp, "/");;
                if ( num == 1){
                    sf = new SimpleDateFormat("MM/yyyy");
                    d = sf.parse(tmstmp);
                }
                else{
                    sf = new SimpleDateFormat("dd/MM/yyyy");
                    d = sf.parse(tmstmp);
                }
            }
            else{
                sf = new SimpleDateFormat("yyyy");
                d = sf.parse(tmstmp);
            }
        }
        else{
            d = null;
        }
        
       /* if (curLevel == 2){
            sf = new SimpleDateFormat("dd/MM/yyyy");
            d = sf.parse(tmstmp);
            
            sf = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
            tmstmp = sf.format( d );
            
            d = sf.parse(tmstmp);
            
            System.out.println("edwwwwwwwwwwwwww = " + d);
            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
            //System.out.println("ddddddddddddddd = " + d);
            //d = formatter.parse(d.toString());
            
            //d = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
        }*/
        
        return d;
    }
    
}

