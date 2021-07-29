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
import data.RelSetData;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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
        
//        System.out.println("inputfile = " + inputFile);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),StandardCharsets.UTF_8));
        String line;
        
        if ((line = br.readLine()) != null){
            if (!line.equals("Anonymization rules:")){
                return false;
            }
        }
        while ((line = br.readLine()) != null) {
            if(!line.equals("")){
                columnName = line;
                rules = new HashMap<>();
                while ((line = br.readLine()) != null) {
                    if ( !line.equals("")){
//                        if(line.contains(del)){
//                            temp = line.split(del);
//                            rules.put(temp[0], temp[1]);
//                        }
//                        else{
//                            
//                        }
                        temp = line.split(del);
                        rules.put(temp[0], temp[1]);
                    }
                    else{
                        break;
                    }
                }
                anonymizedRules.put(columnName, rules);
            }
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
//        Map <Integer,DictionaryString> dictionaries = null;
        DictionaryString dictionary = null;
        int []hierarchyLevel = null;
        double [][]dataset = data.getDataSet();
        Object columnName = null;
        int [] transformation = null; 
        Object nonAnonymizedData = null;
        Object anonymizedData = null;
        Object tempData = null;
        colNamesType = data.getColNamesType();
        colNamesPosition = data.getColNamesPosition();
//        dictionaries = data.getDictionary();
        dictionary = data.getDictionary();

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
//            DictionaryString dictionary = dictionaries.get(column);
            for(int line=0; line<dataset.length; line++){
                nonAnonymizedData = new Object(); 
                anonymizedData = new Object();
                tempData = new Object();
                tempData = dataset[line][column];
                Double d = (Double)dataset[line][column];
                nonAnonymizedData = data.getDictionary().getIdToString(d.intValue());
                
                
                //System.out.println("pipaaaaaaaaaaaaaaaaaaaaaaaaaaa = " + nonAnonymizedData.toString());
                if ( ((String)nonAnonymizedData).equals("NaN")){
                    nonAnonymizedData = "";
                    tempData = "NaN";
                }
                else{
                    tempData = dataset[line][column];
                }
                
                Map<Integer, ArrayList<String>> allParents =  hierarchy.getAllParents();
                
                //for (Map.Entry<Integer, ArrayList<String>> entry : allParents.entrySet()) {
                //    System.out.println("key = " + entry.getKey() + " , value = " + entry.getValue().toString());
                //}
                
                //System.out.println("tempData = " + tempData);
                
                //anonymizedData = (Double)dataset[line][column];
                
//                System.out.println("temp data = " + tempData + "\t level = " + level);
                if ( level != 0 ){
                    if(colNamesType.get(column).contains("date")){
                        if(tempData instanceof String){
                            anonymizedData = anonymizeValue(tempData, hierarchy, level);
                        }
                        else{
                            anonymizedData = anonymizeValue(data.getDictionary().getIdToString(((Double)tempData).intValue()), hierarchy, level);
                        }
                    }
                    else{
                        if(tempData instanceof String){
                            anonymizedData = anonymizeValue(data.getDictionary().getStringToId((String)tempData).doubleValue(), hierarchy, level);
                        }
                        else{
                            anonymizedData = anonymizeValue(tempData, hierarchy, level);
                        }
                        Double anon = (double)anonymizedData;
                        anonymizedData = hierarchy.getDictionary().getIdToString().get(anon.intValue());
                        if(anonymizedData==null){
                            anonymizedData = data.getDictionary().getIdToString().get(anon.intValue());
                        }
                    }
                }
                else{
                    Double anon = (double)tempData;
                    anonymizedData = data.getDictionary().getIdToString().get(anon.intValue());
                }
                //System.out.println("anonymized Data = " + anonymizedData);
                
                //Double anon = (double)anonymizedData;
                //anonymizedData = hierarchy.getDictionary().getIdToString().get(anon.intValue());
                Double anon = (double)tempData;
//                System.out.println("double value "+tempData+" original "+data.getDictionary().getIdToString().get(anon.intValue())+" non anonym "+nonAnonymizedData+" anonym "+anonymizedData );
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
                    writer.flush();
                }
                writer.close();
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
//                DictionaryString dict = qis.get(0).getDictionary();
                DictionaryString dictionary = dataset.getDictionary();
                String columnName = dataset.getColumnByPosition(0);
                writer.println(columnName);
                Set<Double> visited = new HashSet<Double>();
                double data[][] = dataset.getDataSet();
                
                String del = "->";
                int counter = 1;
                /*for(Entry<Double, Double> entry : rules.entrySet()){
                    writer.println(dictionary.getIdToString(entry.getKey().intValue()) +
                    del + dictionary.getIdToString(entry.getValue().intValue()));
                    counter ++;
                }*/
                for(int i=0; i<data.length; i++){
                    for(int j=0; j<data[i].length; j++){
                        if(!visited.contains(data[i][j])){
//                            System.out.println(dictionary.getIdToString((int) data[i][j])+" data value "+data[i][j]);
                            Double numAnonymValue = rules.get(data[i][j]);
                            if(numAnonymValue==null){
                                numAnonymValue = data[i][j];
                            }
                            
                            String anonymValue  = qis.get(0).getDictionary().getIdToString(numAnonymValue.intValue()) == null ? dictionary.getIdToString(numAnonymValue.intValue()) : qis.get(0).getDictionary().getIdToString(numAnonymValue.intValue());
                            writer.println(dictionary.getIdToString((int) data[i][j]) + del + anonymValue);
                            visited.add(data[i][j]);
                        }
                    }
                }
                
//                for(Entry<Double, Double> entry : rules.entrySet()){
//                    if(dictionary.getIdToString(entry.getKey().intValue())!=null){
//                        writer.println(dictionary.getIdToString(entry.getKey().intValue()) +
//                        del + qis.get(0).getDictionary().getIdToString(entry.getValue().intValue()));
//                    }
//                    counter ++;
//                }
                
                writer.close();
                
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void exportRelSet(String file,Data dataset, Map<Integer, Map<Object,Object>> rules, Map<Integer, Hierarchy> qis){
        RelSetData data = (RelSetData) dataset;
        Map <Integer,String> colNamesType = dataset.getColNamesType();
        DictionaryString dictionary = dataset.getDictionary();
        Map<Integer,Set<Double>> visited = new HashMap();
        Set<Double> visitedSet = new HashSet<Double>();
        
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("Anonymization rules:");
            
            for(Entry<Integer,Map<Object,Object>> entry : rules.entrySet()){
                String columnName = dataset.getColumnByPosition(entry.getKey());
                writer.println();
                writer.println(columnName);
                String del = "->";
                double[][] relationalValues = data.getDataSet();
//                for(Entry<Object, Object> entryRules : entry.getValue().entrySet()){
                    if(colNamesType.get(entry.getKey()).equals("set")){
                        double[][] setValues = data.getSet();
                        for(int i=0; i<setValues.length; i++){
                            for(int j=0; j<setValues[i].length; j++){
                                if(!visitedSet.contains(setValues[i][j])){
                                    Double numAnonymValue = (Double) entry.getValue().get(setValues[i][j]);
                                    if(numAnonymValue == null){
                                        numAnonymValue = setValues[i][j];
                                    }


                                    String anonymValue = qis.get(entry.getKey()).getDictionary().getIdToString(numAnonymValue.intValue()) == null ? dictionary.getIdToString(numAnonymValue.intValue()) : qis.get(entry.getKey()).getDictionary().getIdToString(numAnonymValue.intValue());
                                    String originalVal = dictionary.getIdToString((int)setValues[i][j]) == null ? qis.get(entry.getKey()).getDictionary().getIdToString((int)setValues[i][j]) : dictionary.getIdToString((int)setValues[i][j]);
                                    writer.println(originalVal + del + anonymValue);
                                    visitedSet.add(setValues[i][j]);
                                }
                                
                            }
                        }
//                        if(dictionary.getIdToString(((Double)entryRules.getKey()).intValue())!=null){
//                            writer.println(dictionary.getIdToString(((Double)entryRules.getKey()).intValue()) +
//                            del + qis.get(entry.getKey()).getDictionary().getIdToString(((Double)entryRules.getValue()).intValue()));
//                        }
                    }
                    else{
                        for(int i=0; i<relationalValues.length; i++){
                            if(colNamesType.get(entry.getKey()).equals("int")){
                                Set<Double> visitedValues = visited.get(entry.getKey());
                                if(visitedValues == null){
                                    visitedValues = new HashSet<Double>();
                                }
                                if(!visitedValues.contains(relationalValues[i][entry.getKey()])){
                                    if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
                                        Object anonymValue =  entry.getValue().get(relationalValues[i][entry.getKey()]);
                                        if(anonymValue == null){
                                            writer.println(((int)relationalValues[i][entry.getKey()]) + del + ((int)relationalValues[i][entry.getKey()]));
                                        }
                                        else if(anonymValue instanceof Double){
                                            Double anonymNumValue = (Double) anonymValue;
                                            if(anonymNumValue == 2147483646.0 || anonymNumValue.isNaN()){
                                                Object value = entry.getValue().get(new RangeDouble(Double.NaN,Double.NaN));
                                                if(value instanceof RangeDouble){
                                                    writer.println("(null)" + del +  ((RangeDouble)entry.getValue().get(new RangeDouble(Double.NaN,Double.NaN))).toString());
                                                }
                                                else{
                                                    writer.println("(null)" + del + "(null)");
                                                }
                                            }
                                            else{
                                                writer.println(((int)relationalValues[i][entry.getKey()]) + del + ((int)relationalValues[i][entry.getKey()]));
                                            }
                                        }
                                        else{
                                            writer.println(((int)relationalValues[i][entry.getKey()]) + del + anonymValue.toString());
                                        }
                                    }
                                    else{
                                        String anonymValue = "";
                                        Object numanonymValue = entry.getValue().get(relationalValues[i][entry.getKey()]);
                                        if(numanonymValue == null){
                                            writer.println(((int) relationalValues[i][entry.getKey()]) + del + ((int) relationalValues[i][entry.getKey()]));
                                        }
                                        try{
                                            anonymValue = Integer.toString(((Double)entry.getValue().get(relationalValues[i][entry.getKey()])).intValue());
                                        }catch(ClassCastException e){
                                            anonymValue = Integer.toString(((Integer)entry.getValue().get(relationalValues[i][entry.getKey()])));
                                        }
                                        
                                        if(relationalValues[i][entry.getKey()] == 2147483646.0 || ((Double)relationalValues[i][entry.getKey()]).isNaN()){
                                            writer.println("(null)" + del + anonymValue);
                                        }
                                        else{
                                            writer.println(((int) relationalValues[i][entry.getKey()]) + del + anonymValue);
                                        }

                                    }
                                    visitedValues.contains(relationalValues[i][entry.getKey()]);
                                    visited.put(entry.getKey(), visitedValues);
                                }
                                
                            }
                            else if(colNamesType.get(entry.getKey()).equals("double")){
                                Set<Double> visitedValues = visited.get(entry.getKey());
                                if(visitedValues == null){
                                    visitedValues = new HashSet<Double>();
                                }
                                if(!visitedValues.contains(relationalValues[i][entry.getKey()])){
                                    if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
                                        Object anonymValue =  entry.getValue().get(relationalValues[i][entry.getKey()]);
                                        if(anonymValue == null){
                                            writer.println((relationalValues[i][entry.getKey()]) + del + (relationalValues[i][entry.getKey()]));
                                        }
                                        else if(anonymValue instanceof Double){
                                            Double anonymNumValue = (Double) anonymValue;
                                            if(anonymNumValue == 2147483646.0 || anonymNumValue.isNaN()){
                                                Object value = entry.getValue().get(new RangeDouble(Double.NaN,Double.NaN));
                                                if(value instanceof RangeDouble){
                                                    writer.println("(null)" + del +  ((RangeDouble)entry.getValue().get(new RangeDouble(Double.NaN,Double.NaN))).toString());
                                                }
                                                else{
                                                    writer.println("(null)" + del + "(null)");
                                                }
                                            }
                                            else{
                                                writer.println(((int)relationalValues[i][entry.getKey()]) + del + ((int)relationalValues[i][entry.getKey()]));
                                            }
                                        }
                                        else{
                                            writer.println((relationalValues[i][entry.getKey()]) + del + ((RangeDouble)anonymValue).toString());
                                        }
                                    }
                                    else{
                                        Object anonymValue = entry.getValue().get(relationalValues[i][entry.getKey()]);
                                        if(anonymValue == null){
                                            writer.println((relationalValues[i][entry.getKey()]) + del + (relationalValues[i][entry.getKey()]));
                                        }
                                        else{
                                            if(relationalValues[i][entry.getKey()] == 2147483646.0 || ((Double)relationalValues[i][entry.getKey()]).isNaN()){
                                                writer.println("(null)" + del + anonymValue);
                                            }
                                            else{
                                                writer.println((relationalValues[i][entry.getKey()]) + del + anonymValue);
                                            }
                                        }  
                                    }
                                    visitedValues.add(relationalValues[i][entry.getKey()]);
                                    visited.put(entry.getKey(), visitedValues);
                                }
                            }
                            else{
                                Set<Double> visitedValues = visited.get(entry.getKey());
                                if(visitedValues == null){
                                    visitedValues = new HashSet<Double>();
                                }
                                if(!visitedValues.contains(relationalValues[i][entry.getKey()])){
                                    if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
                                        Object anonymValue = entry.getValue().get(relationalValues[i][entry.getKey()]);
                                        if(anonymValue == null){
                                            writer.println((dictionary.getIdToString((int)relationalValues[i][entry.getKey()])) + del + dictionary.getIdToString((int)relationalValues[i][entry.getKey()]));
                                        }
                                        else{
                                            if(anonymValue instanceof Double && ((Double)anonymValue) == 2147483646.0){
                                                RangeDate rd = ((RangeDate)entry.getValue().get(new RangeDate(null,null)));
                                                writer.println((dictionary.getIdToString((int)relationalValues[i][entry.getKey()])) + del + rd.dateToString(qis.get(entry.getKey()).translateDateViaLevel(qis.get(entry.getKey()).getHeight() - qis.get(entry.getKey()).getLevel(rd))));  

                                            }
                                            else if(!(anonymValue instanceof RangeDate)){
                                                writer.println((dictionary.getIdToString((int)relationalValues[i][entry.getKey()])) + del + dictionary.getIdToString((int)relationalValues[i][entry.getKey()]));  

                                            }
                                            else{
                                                RangeDate rd = (RangeDate) anonymValue;
                                                writer.println((dictionary.getIdToString((int)relationalValues[i][entry.getKey()])) + del + rd.dateToString(qis.get(entry.getKey()).translateDateViaLevel(qis.get(entry.getKey()).getHeight() - qis.get(entry.getKey()).getLevel(rd))));  
                                            }
                                        }
                                    }
                                    else{
                                        Object anonymValue = entry.getValue().get(relationalValues[i][entry.getKey()]);
                                        if(anonymValue == null){
                                            writer.println((dictionary.getIdToString((int)relationalValues[i][entry.getKey()])) + del + dictionary.getIdToString((int)relationalValues[i][entry.getKey()]));
                                        }
                                        else{
                                            Double numanonymValue  = (Double) anonymValue;
                                            String stranonymValue = qis.get(entry.getKey()).getDictionary().getIdToString(numanonymValue.intValue()) == null ? dictionary.getIdToString(numanonymValue.intValue()) :  qis.get(entry.getKey()).getDictionary().getIdToString(numanonymValue.intValue());
                                            writer.println((dictionary.getIdToString((int)relationalValues[i][entry.getKey()])) + del + stranonymValue);
                                        }
                                    }
                                    visitedValues.add(relationalValues[i][entry.getKey()]);
                                    visited.put(entry.getKey(), visitedValues);
                                }
                                
//                                if(dictionary.getIdToString(((Double)entryRules.getKey()).intValue())!=null){
//                                    if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
//                                        RangeDate rd = (RangeDate) entryRules.getValue();
//                                        writer.println(dictionary.getIdToString(((Double)entryRules.getKey()).intValue()) +
//                                            del + rd.dateToString(qis.get(entry.getKey()).translateDateViaLevel(qis.get(entry.getKey()).getHeight() - qis.get(entry.getKey()).getLevel(rd))));  
//                                    }
//                                    else{
//                                        writer.println(dictionary.getIdToString(((Double)entryRules.getKey()).intValue()) +
//                                            del + qis.get(entry.getKey()).getDictionary().getIdToString(((Double)entryRules.getValue()).intValue()));
//                                    }
//                                }
                            }
                        }
                    }
                    
                    
                    
//                    else if(colNamesType.get(entry.getKey()).equals("int")){
//                        visited.clear();
////                        System.out.println("key "+entryRules.getKey()+" value "+entryRules.getValue());
//                        for(int i=0; i<relationalValues.length; i++){
//                            if(!visited.contains(relationalValues[i][entry.getKey()])){
//                                if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
//                                    RangeDouble anonymValue = (RangeDouble) entry.getValue().get(relationalValues[i][entry.getKey()]);
//                                    if(anonymValue == null){
//                                        writer.println(((int)relationalValues[i][entry.getKey()]) + del + ((int)relationalValues[i][entry.getKey()]));
//                                    }
//                                    else{
//                                        writer.println(((int)relationalValues[i][entry.getKey()]) + del + anonymValue.toString());
//                                    }
//                                }
//                                else{
//                                    String anonymValue = "";
//                                    Object numanonymValue = entry.getValue().get(relationalValues[i][entry.getKey()]);
//                                    if(numanonymValue == null){
//                                        writer.println(((int) relationalValues[i][entry.getKey()]) + del + ((int) relationalValues[i][entry.getKey()]));
//                                    }
//                                    try{
//                                        anonymValue = Integer.toString(((Double)entry.getValue().get(relationalValues[i][entry.getKey()])).intValue());
//                                    }catch(ClassCastException e){
//                                       anonymValue = Integer.toString(((Integer)entry.getValue().get(relationalValues[i][entry.getKey()])));
//                                    }
//                                    writer.println(((int) relationalValues[i][entry.getKey()]) + del + anonymValue);
//
//                                }
//                                visited.contains(relationalValues[i][entry.getKey()]);
//                            }
//                        }
//                        
////                        if(entryRules.getKey()!=null){
////                            if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
////                                writer.println(((Double)entryRules.getKey()).intValue() +
////                                del + ((RangeDouble)entryRules.getValue()).toString());
////                            }
////                            else{
////                                String value = "";
////                                try{
////                                    value = Integer.toString(((Double)entryRules.getValue()).intValue());
////                                }catch(ClassCastException e){
////                                    value =  Integer.toString(((Integer)entryRules.getValue()));
////                                }
////                                writer.println(((Double)entryRules.getKey()).intValue() +
////                                del + value);
////                            }
////                        }
//                    }
//                    else if(colNamesType.get(entry.getKey()).equals("double")){
//                        if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
//                            writer.println(entryRules.getKey() +
//                                del + ((RangeDouble)entryRules.getValue()).toString());
//                        }
//                        else{
//                          writer.println(entryRules.getKey() +
//                            del + entryRules.getValue());  
//                        }
//                        
//                    }
//                    else{
//                        if(dictionary.getIdToString(((Double)entryRules.getKey()).intValue())!=null){
//                            if(qis.get(entry.getKey()).getHierarchyType().equals("range")){
//                                RangeDate rd = (RangeDate) entryRules.getValue();
//                                writer.println(dictionary.getIdToString(((Double)entryRules.getKey()).intValue()) +
//                                    del + rd.dateToString(qis.get(entry.getKey()).translateDateViaLevel(qis.get(entry.getKey()).getHeight() - qis.get(entry.getKey()).getLevel(rd))));  
//                            }
//                            else{
//                                writer.println(dictionary.getIdToString(((Double)entryRules.getKey()).intValue()) +
//                                    del + qis.get(entry.getKey()).getDictionary().getIdToString(((Double)entryRules.getValue()).intValue()));
//                            }
//                        }
//                    }
//                }
                writer.flush();
            }
            writer.flush();
            writer.close();
            
        }catch(Exception e){
            System.out.println("Rules export exception "+e.getMessage());
        }
    }
    
    private Object anonymizeValue(Object value, Hierarchy h, int level) throws ParseException{
        Object anonymizedValue = value;
        
       // System.out.println("anonymizedValueeeeeee = " + anonymizedValue + ", value = " + value);
        
        for(int i=0; i<level; i++){
            h.setLevel(i);
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
//                        System.out.println("anonymizedValueeeeeeeDouble = " + anonymizedValue + ", value = " + value+" level="+level);
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
                        System.out.println("anonymizedValueeeeeee = " + anonymizedValue + ", value = " + value+" level="+level);
                        
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

