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
package data;

import exceptions.LimitException;
import exceptions.NotFoundValueException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import controller.AppCon;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import dictionary.DictionaryString;
import exceptions.DateParseException;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import javax.persistence.Column;
import jsoninterface.View;
import java.util.Date;




/**
 * A class managing text data
 * @author serafeim
 */
public class TXTData implements Data,Serializable{
    
    @JsonView(View.GetColumnNames.class)
    private String inputFile = null;
    private double dataSet[][] = null;
    private int sizeOfRows = 0;
    private int sizeOfCol = 0;
    private String delimeter = null;
//    private static int counterNamesType=0;
//    private static int counterNamesPosition=0;
    
    @JsonView(View.GetDataTypes.class)
    private Map <Integer,String> colNamesType = null;
    private CheckVariables chVar = null;
    private Map <Integer,String> colNamesPosition = null;
//    private Map <Integer,DictionaryString> dictionary = null;
    private DictionaryString dictionary = null;
    private DictionaryString dictHier = null;
    @JsonView(View.SmallDataSet.class)
    private String[][] smallDataSet;
    @JsonView(View.DataSet.class)
    private ArrayList<LinkedHashMap> data;
    @JsonView(View.SmallDataSet.class)
    private String[][] typeArr;
    @JsonView(View.GetColumnNames.class)
    private String []columnNames = null;
    @JsonView(View.DataSet.class)
    private int recordsTotal;
    @JsonView(View.DataSet.class)
    private int recordsFiltered;
    @JsonView(View.SmallDataSet.class)
    private String errorMessage = null;
    private String[] formatsDate = null;
    
    private static final String[] formats = { 
                "yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss", 
                "MM/dd/yyyy HH:mm:ss",        "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", 
                "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS", 
                "MM/dd/yyyy'T'HH:mm:ssZ",     "MM/dd/yyyy'T'HH:mm:ss", 
                "yyyy:MM:dd HH:mm:ss",        "yyyy/MM/dd", 
                "yyyy:MM:dd HH:mm:ss.SS",      "dd/MM/yyyy",
                "dd MMM yyyy",                "dd-MMM-yyy"};  
                                                
                                                
    
    
    public TXTData(String inputFile, String del,DictionaryString dict){
        recordsTotal = 0;
        this.smallDataSet = null;
        colNamesType = new TreeMap<Integer,String>();
        colNamesPosition = new HashMap<Integer,String>();
        chVar = new CheckVariables();
        dictHier = dict;
        dictionary = new DictionaryString();
        
        this.inputFile = inputFile;
        if ( del == null ){
            delimeter = ",";
        }
        else{
            delimeter = del;
        }
             
    }

    public double[][] getDataSet() {
        return dataSet;
    }

    public String getInputFile() {
        String delimiter = "/";
        String[] temp = inputFile.split(delimiter,-1);
        return temp[temp.length-1];
    }

    
    

    
    /**
     * Gets the array of the loaded dataset
     * @return 2-dimensional array of the loaded dataset
     */

    
    /*@Override
    public double[][] getData() {
        return data;
    }*/

    
    public Map<Integer, String> getColNamesType() {
//        if(counterNamesType==0){
//            System.out.println("colNamesType TXT");
//            for (Map.Entry<Integer, String> entry : colNamesType.entrySet()) {
//                System.out.println(entry.getKey() + ":" + entry.getValue().toString());
//            }
//            counterNamesType++;
//        }
        return colNamesType;
    }
    
    
    @Override
    public void setData(double[][] _dataSet) {
        this.dataSet = _dataSet;
    }
    
    /**
     * Gets the length of the dataset array
     * @return length of the dataset
     */
    
    @JsonIgnore
    @Override
    public int getDataLenght() {
        return dataSet.length;
    }
    
    
    /**
     * Prints the dataset
     */
    @Override
    public void print(){
        int i,j;
        for (i = 0 ; i < dataSet.length ; i ++){
            for (j = 0 ; j < dataSet[i].length ; j++){
                System.out.print(dataSet[i][j]+",");
            }
            System.out.println();
        }
    }
    
    /**
     * Executes a preprocessing of the dataset
     */
    @Override
    public void preprocessing() throws LimitException {
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        int i = 0;
        int counter = -1;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            
            //counts lines of the dataset
            while ((strLine = br.readLine()) != null)   {
                counter++;
                if(AppCon.os.equals(online_version) && counter > online_rows){
                    throw new LimitException("Dataset is too large, the limit is "+online_rows+" rows, please download desktop version, the online version is only for simple execution.");
                }
            }
            
            //System.out.println("counter = " + counter);
            
            sizeOfRows = counter;
            in.close();
            
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Loads dataset from file to memory
     */
    
    @Override
    public String save(boolean[] checkColumns) throws DateParseException{
        
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String []colNames = null;
        SimpleDateFormat sdf[] = new SimpleDateFormat[this.columnNames.length];
        SimpleDateFormat sdfDefault = new SimpleDateFormat("dd/MM/yyyy");
        ArrayList<String> columns = new ArrayList<String>();
        int counterSdf = 0;
        
        int counter = 0;
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            System.out.println("Both empy load data");
            stringCount = 1;
        }
        else {
            System.out.println("Hier not empty load data");
            stringCount = dictHier.getMaxUsedId()+1;
        }
        boolean FLAG = true;
        int counter1 = 0 ;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            
            while ((strLine = br.readLine()) != null){
//                System.out.println("Edw mpainei");
                //do not read the fist line
                if (FLAG == true){
                    temp = strLine.split(delimeter,-1);
                    for ( int i = 0 ; i < temp.length ; i ++){
                        if (checkColumns[i] == true){
                            columns.add(temp[i]);
                            if(colNamesType.get(counterSdf).contains("date")){
                                sdf[counterSdf] = new SimpleDateFormat(this.formatsDate[counterSdf]);
                            }
                            counterSdf++;
                        }
                    }
                    colNames = new String[columns.size()];
                    colNames = columns.toArray(new String[0]);
                    this.setColumnNames(colNames);
                    FLAG = false;
                    System.out.println("Size "+sizeOfRows+ " and "+columnNames.length);
                    dataSet = new double[sizeOfRows][columnNames.length];
                }
                else{
                    temp = strLine.split(delimeter,-1);
                    counter1 = 0;
                    for (int i = 0; i < temp.length ; i ++ ){
                        if (checkColumns[i] == true){
                        

                            if ( colNamesType.get(counter1).contains("int") ){
                                if ( !temp[i].equals("")){
                                    try {
                                        dataSet[counter][counter1] = Integer.parseInt(temp[i]);
                                    } catch (java.lang.NumberFormatException exc) {
                                        //ErrorWindow.showErrorWindow("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                        exc.printStackTrace();
                                        try {
                                            dataSet[counter][counter1] = new Double(temp[i]).intValue();
                                        } catch (Exception exc1) {
                                            exc1.printStackTrace();
    //                                        System.out.println("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                            return null;
                                        }
                                    }   
                                }
                                else{
                                    dataSet[counter][counter1] = 2147483646;
                                }
                            }
                            else if (colNamesType.get(counter1).contains("double")){
                                if ( !temp[i].equals("")){
//                                    System.out.println("double "+Double.parseDouble(temp[i]));
                                    temp[i] = temp[i].replaceAll(",", ".");
                                    dataSet[counter][counter1] = Double.parseDouble(temp[i]);
                                }
                                else{
                                    dataSet[counter][counter1] = Double.NaN;
                                }

                            }
                            else if (colNamesType.get(counter1).contains("date")){
//                                DictionaryString tempDict = dictionary.get(counter1);
                                String var = null;
//                                System.out.println("date= "+temp[i]+" counter= "+counter+" counter1= "+counter1+" ");
                                if ( !temp[i].equals("")){
                                    var = temp[i];
//                                    var = this.timestampToDate(var);
//                                    sdf.parse(var);
//                                    System.out.println("Format Date "+this.columnNames[counter1]+" format "+this.formatsDates[counter1]);
                                    try{
                                        if(this.formatsDate[counter1].equals("dd/MM/yyyy")){
                                            var = sdf[counter1].parse(var) == null ? null : var;
                                        }
                                        else{
                                            Date d = sdf[counter1].parse(var);
                                            var = sdfDefault.format(d);
                                        }

                                        if(var == null){
                                            var = "NaN";
                                        }
                                    }catch(ParseException ep){
                                        throw new DateParseException(ep.getMessage()+"\nDate format must be the same in column "+this.columnNames[counter1]);
                                    }
                                    
                                }
                                else {
                                    var = "NaN";
                                }

                                
                                //transform timestamp to date
                                //var = this.timestampToDate(var);
                                
                                if (var != null) {
                                    
                                //if string is not present in the dictionary
                                    if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                        if(var.equals("NaN")){
                                            dictionary.putIdToString(2147483646, var);
                                            dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                                            dataSet[counter][counter1] = 2147483646.0;
                                        }
                                        else{
                                            dictionary.putIdToString(stringCount, var);
                                            dictionary.putStringToId(var,stringCount);
    //                                        dictionary.put(counter1, tempDict);
                                            dataSet[counter][counter1] = stringCount;
                                            stringCount++;
                                        }
                                    }
                                    else{
                                        //if string is present in the dictionary, get its id
                                        if(dictionary.containsString(var)){
                                            int stringId = dictionary.getStringToId(var);
                                            dataSet[counter][counter1] = stringId;
                                        }
                                        else{
                                            int stringId = this.dictHier.getStringToId(var);
                                            dataSet[counter][counter1] = stringId;
                                        }
                                    }
                                }
                            }
                            else{
//                                DictionaryString tempDict = dictionary.get(counter1);
                                String var = null;

                                if ( !temp[i].equals("")){
                                    var = temp[i];
                                }
                                else {
                                    var = "NaN";
                                }
                                
                                
//                                if(var.equals("A0")){
//                                    System.out.println("Yparxei sto dataset "+var+"sth grammh "+counter);
//                                }
//                                if(var.equals("V402XXD")){
//                                    System.out.println("V402XXD "+dictionary.getStringToId(var)+dictionary.getStringToId("V402XXD")+dictionary.containsString(var)+dictionary.containsString("V402XXD"));
//                                    System.out.println("Data dict");
//                                    for (Object objectName : dictionary.idToString.keySet()) {
//                                        System.out.print(objectName+" : ");
//                                        System.out.println(dictionary.idToString.get(objectName));
//                                    }
//                                }
                                //if string is not present in the dictionary
                                if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                     if(var.equals("NaN")){
                                        dictionary.putIdToString(2147483646, var);
                                        dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                                        dataSet[counter][counter1] = 2147483646.0;
                                    }
                                    else{
                                        dictionary.putIdToString(stringCount, var);
                                        dictionary.putStringToId(var,stringCount);
    //                                    dictionary.put(counter1, tempDict);
                                        dataSet[counter][counter1] = stringCount;
                                        stringCount++;
                                    }
                                }
                                else{
                                    //if string is present in the dictionary, get its id
                                    if(dictionary.containsString(var)){
                                        int stringId = dictionary.getStringToId(var);
                                        dataSet[counter][counter1] = stringId;
                                    }
                                    else{
                                        int stringId = this.dictHier.getStringToId(var);
                                        dataSet[counter][counter1] = stringId;
                                    }
                                }
//                                System.out.println("String value "+var+" id "+dataSet[counter][counter1]+" row "+counter+"col "+counter1);
                            }
                            counter1++;
                        }
                    }
                    counter++;
                }
            }
            
            in.close();
            
            System.out.println("size row = " + dataSet.length + "\tsize column = " + dataSet[0].length);
            
            /*for ( int  i = 0; i < dataSet.length ; i ++){
                for ( int j=0; j < dataSet[i].length ; j ++ ){
                    System.out.print("data = " + dataSet[i][j]);
                }
                System.out.println();
            }*/
            
            
        }catch(DateParseException de){
            throw new DateParseException(de);
        }
        catch(Exception e){//Catch exception if any
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        return "OK";
    }
    
    
    /**
     * Reads dataset from file (preprocessing and load)
     */
    @Override
    public String readDataset(String[] columnTypes, boolean[] checkColumns) throws LimitException, DateParseException {
        SaveClmnsAndTypeOfVar(columnTypes,checkColumns);
        preprocessing();
        String result = save(checkColumns);
        return result;
        
    }
    
    /**
     * Gets dictionary for the specified column
     * @param column the number of the column
     * @return the dictionary for the column
     */
//    @Override
//    public DictionaryString getDictionary(Integer column){
//        return this.dictionary.get(column);
//    }
    
    
    /**
     * Gets column names
     * @return a map with the column names by position
     */
    @Override
    public Map<Integer, String> getColNamesPosition() {
//        if(counterNamesPosition==0){
//            System.out.println("colNamesPosition TXT");
//            for (Map.Entry<Integer, String> entry : colNamesPosition.entrySet()) {
//                System.out.println(entry.getKey() + ":" + entry.getValue().toString());
//            }
//            counterNamesPosition++;
//        }
        return colNamesPosition;
    }
    
    /**
     * Gets all dictionaries
     * @return a map with with the column dictionaries by position
     */
    @Override
    public DictionaryString getDictionary() {
        return dictionary;
    }
    
    /**
     * Finds the column number of the column name specified
     * @param column the column name to search for
     * @return the column number of the given column
     */
    @Override
    @JsonIgnore
    public int getColumnByName(String column){
        for(Integer i : this.colNamesPosition.keySet()){
            if(this.colNamesPosition.get(i).equals(column)){
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Sets a new dictionary for a specific column
     * @param column the column number
     * @param dict the new dictionary
     */
//    @Override
//    public void setDictionary(Integer column, DictionaryString dict) {
//        this.dictionary.put(column, dict);
//    }
    
    /**
     * Replaces the dictionary of a column with a new one. Updates values in this
     * column with those taken from the new dictionary
     * @param column the column number
     * @param dict the new dictionary
     */
//    @Override
//    public void replaceColumnDictionary(Integer column, DictionaryString dict) {
//        DictionaryString curDict = this.dictionary.get(column);
//        
//        for (double[] row : dataSet) {
//            
//            //retrieve actual value from dictionary
//            String columnValue = curDict.getIdToString((int)row[column]);
//            
//            //replace with value from new dictionary
//            row[column] = dict.getStringToId(columnValue);
//            
//        }
//        
//        //set given dictionary as the new one
//        setDictionary(column, dict);
//    }
    
    @Override
    public void export(String file, Object [][] initialTable, Object[][] anonymizedTable,
            int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
        
        System.out.println("Export in data...");
        
        Object[][] temp = null;
        if ( initialTable != null ){
            System.out.println("initial table");
            temp = initialTable;
        }
        else{
            System.out.println("anonymized table");
            temp = anonymizedTable;
        }
        
        try {
            
            
            try (PrintWriter writer = new PrintWriter( file, "UTF-8")) {
                //TableModel model =  anonymizedTable.getModel();
                //int columnCount = model.getColumnCount();
                //int rowCount = model.getRowCount();
                
                //write column names
                /*for(int column = 0; column < temp[0].length; column++){
                    
                    //if column contains ranges, split it in min/max

                    Hierarchy h = hierarchies.get(column);
                    if(h != null && h.getHierarchyType().equals("range")){
                        Object[] possibleValues = { "min-max value", "average with tolerance" };
                        Object selectedValue = JOptionPane.showInputDialog(null,
                        "Choose one", "Output file format",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        possibleValues, possibleValues[0]);
                        System.out.println("object = " + selectedValue);
                        if ( selectedValue.equals("min-max value")){
                            //writer.print(model.getColumnName(column) + "_min,");
                            //writer.print(model.getColumnName(column) + "_max");
                        }
                        else{
                            
                        }
                        
                    }
                    else{
                        writer.print(temp[column]);
                    }
                    
                    if(column != temp[column].length-1){
                        writer.print(",");
                    }
                    
                   
                    Hierarchy h = hierarchies.get(column-1);
                    if(h != null && h.getHierarchyType().equals("range")){
                        writer.print(model.getColumnName(column) + "_min,");
                        writer.print(model.getColumnName(column) + "_max");
                    }
                    else{
                        writer.print(model.getColumnName(column));
                    }
                    
                    if(column != columnCount-1){
                        writer.print(",");
                    }
                }
                writer.println();*/
                
                boolean FLAG = false;
                
                for(int i = 0 ; i < temp[0].length ; i ++){
                    if (FLAG == false){
                        writer.print(columnNames[i]);
                        FLAG = true;
                    }
                    else{
                        writer.print(","+columnNames[i]);
                    }
                    
                }
                writer.println();
                
                Object[] rowQIs = null;
                if(suppressedValues != null){
                    rowQIs = new Object[qids.length];
                }
                //write table data
                for (int row = 0; row < temp.length; row++){
                    
                    //if suppressed values exist
                    if(suppressedValues != null){
                        
                        
                        //get qids of this row
                        for(int i=0; i<qids.length; i++){
                            rowQIs[i] = temp[row][qids[i]];
                        }
                        
                        
                        //check if row is suppressed
                        if(isSuppressed(rowQIs, qids, suppressedValues)){
                            continue;
                        }
                    }
                    //write row to file
                    for(int column = 0; column < temp[0].length; column++){
                        Object value = temp[row][column];
                        if(value instanceof RangeDouble){
                            if ( colNamesType.get(column-1).equals("double")){
                                writer.print(((RangeDouble)value).lowerBound + ",");
                                writer.print(((RangeDouble)value).upperBound);
                            }
                            else{
                                writer.print((((RangeDouble)value).lowerBound).intValue() + ",");
                                writer.print((((RangeDouble)value).upperBound).intValue());
                            }
                        }
                        else{
                            if (!value.equals("(null)")){
                                writer.print(value);
                            }
                            
                        }
                        
                        if(column != temp[column].length-1){
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("done");
        
    }
    
    private boolean isSuppressed(Object[] data, int[] qids, Map<Integer, Set<String>> suppressedValues){
        
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
    
    @Override
    @JsonIgnore
    public String getColumnByPosition(Integer columnIndex) {
        return this.colNamesPosition.get(columnIndex);
    }
    
    @Override
    public void SaveClmnsAndTypeOfVar(String[] columnTypes, boolean[] checkColumns) {
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String []colNames = null;
        String [] newFormatDate = null;
        
        boolean FLAG = true;
        boolean removedColumn = false;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            int counter = 0 ;
            
            while ((strLine = br.readLine()) != null)   {
                
                //save column names
                if (FLAG == true){
                    colNames = strLine.split(delimeter,-1);
                    for ( int i = 0 ; i < colNames.length ; i ++){
                        if ( checkColumns[i] == true){
                            colNamesType.put(counter,null);
                            colNamesPosition.put(counter,colNames[i]);
                            counter++;
                        }
                    }
                    
                    if(counter != columnNames.length){
                        newFormatDate = new String[counter];
                        removedColumn = true;
                    }
                    FLAG = false;
                }
            }
            
            counter = 0 ;
            //save column types
//            System.out.println("Column Types from user "+Arrays.toString(columnTypes));
            for ( int i = 0 ; i < columnTypes.length ; i ++ ){
                //System.out.println("columnnnnnnnnnnnnnn = " + colNamesType.size());
                if ( checkColumns[i] == true){
                    if (columnTypes[i].equals("int")){
                        colNamesType.put(counter, "int");
                    }
                    else if (columnTypes[i].equals("double")){
                        colNamesType.put(counter, "double");
                    }
                    else if (columnTypes[i].equals("date")){
                        colNamesType.put(counter, "date");
                        if(removedColumn){
                            newFormatDate[counter] = this.formatsDate[i]; 
                        }
//                        dictionary.put(counter, new DictionaryString());
                    }
                    else{
                        colNamesType.put(counter, "string");
//                        dictionary.put(counter, new DictionaryString());
                    }
                    
                    counter++;
                }
            }
            
            if(counter!=columnTypes.length){
                this.columnNames =  colNamesPosition.values().toArray(new String[this.colNamesPosition.size()]);
                this.formatsDate = newFormatDate;
            }
            sizeOfCol = columnNames.length;
            System.out.println("new Names "+Arrays.toString(this.columnNames));
            in.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }
    

    @JsonView(View.SmallDataSet.class)
    @Override
    public String findColumnTypes() {
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        boolean FLAG = true;
        boolean FLAG2 = true;
        int counter = 0;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            while ((strLine = br.readLine()) != null)   {
                //escape first row
                if (FLAG == true){
                    temp = strLine.split(delimeter,-1);
                    columnNames = new String[temp.length];
                    smallDataSet = new String[6][temp.length];
                    this.formatsDate = new String[temp.length];
                    for ( int i = 0 ; i < temp.length ; i ++){
                        columnNames[i] = temp[i];
                    }
                    FLAG = false;
                    
                    
                }
                //save column types
                else{
                    temp = strLine.split(delimeter,-1);
                    //System.out.println("strLine = " + strLine);
                    /*for ( int i = 0 ; i < temp.length ; i ++){
                        System.out.println("tempp =" + temp[i]);
                    }*/
                    
                    //System.out.println("temp len = " + temp.length + " \tcolumnNames len =" + columnNames.length);
                    
                    if( temp.length != columnNames.length){
                        System.out.println("columnNames = " + columnNames.length +"\t temp = " + temp.length );
                        //ErrorWindow.showErrorWindow("Parse problem.Different size between title row and data row");
                        this.errorMessage = "1";//"Parse problem.Different size between title row and data row"
                        System.out.println("Parse problem.Different size between title row and data row");
                        return errorMessage;
                    }
                    
                    if (FLAG2 == true){
                        for ( int i = 0 ; i < temp.length ; i ++ ){
                            counter = 0;
                            if ( !temp[i].equals("")){
                                if (chVar.isInt(temp[i])){
                                    smallDataSet[counter][i] = "int";
                                }
                                else if (chVar.isDouble(temp[i])){
                                    smallDataSet[counter][i] = "double";
                                }
                                else if(chVar.isDate(temp[i])){
                                    smallDataSet[counter][i] = "date";
                                    this.formatsDate[i] = chVar.lastFormat;
                                }
                                else{  
                                    smallDataSet[counter][i] = "string";
                                }
                            }
                            
                            smallDataSet[++counter][i] = temp[i];
                        }
                        FLAG2 = false;
                    }
                    //check for the next 6 records for the types
                    else if ( counter < 6 ){
                        
                        for ( int i = 0 ; i < temp.length ; i ++ ){
                            smallDataSet[counter][i] = temp[i];

                            
                            if ( !temp[i].equals("")){
                                if ( smallDataSet[0][i] != null ){
                                    if (smallDataSet[0][i].equals("int")){
                                        if (!chVar.isInt(temp[i])){
                                            if (chVar.isDouble(temp[i])){
                                                smallDataSet[0][i] = "double";
                                            }
                                            else {
                                                smallDataSet[0][i] = "string";
                                            }
                                        }
                                    }
                                    else if(smallDataSet[0][i].equals("double")){
                                        if (!chVar.isInt(temp[i]) && !chVar.isDouble(temp[i])){
                                            smallDataSet[0][i] = "string";
                                        }
                                    }
                                }
                                else{
                                    if (chVar.isInt(temp[i])){
                                        smallDataSet[0][i] = "int";
                                    }
                                    else if (chVar.isDouble(temp[i])){
                                        smallDataSet[0][i] = "double";
                                    }
                                    else if(chVar.isDate(temp[i])){
                                        smallDataSet[0][i] ="date";
                                        this.formatsDate[i] = chVar.lastFormat;
                                    }
                                    else{  
                                        smallDataSet[0][i] = "string";
                                    }
                                }
                            }
                        }
                        counter ++;
                    }
                    //if record has no type, then put string
                    else{
                       
                        for (int i = 0 ; i < smallDataSet[0].length ; i ++ ){
                            if ( smallDataSet[0][i] == null){
                                smallDataSet[0][i]= "string";
                            }
                        }
                        
                        break;
                    }
                }
            }
            in.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
        
        
        return "Ok";
    }
    
    @Override
    public String[][] getSmallDataSet() {
        return smallDataSet;
    }   
    
    
    
    @Override
    public ArrayList<LinkedHashMap> getPage(int start,int length){
        data = new ArrayList<LinkedHashMap>();
        int counter = 0 ;
        
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        if ( start + length <= sizeOfRows ){
            max = start + length;
        }
        else{
            max = sizeOfRows;
        }
        
        
        /*for ( int i = 0 ; i < dataSet.length ; i ++){
            for ( int j = 0 ; j < dataSet[i].length ; j ++){
                System.out.print(dataSet[i][j] +"," );
            }
            System.out.println();
        }
        System.out.println("Dictionary");
        Dictionary d;
        for (Map.Entry<Integer,Dictionary> entry : dictionary.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            d = entry.getValue();
            System.out.println("\\\\\\");
            for (Map.Entry<Integer,String> entry1 : d.getIdToString().entrySet()) {
                System.out.println(entry1.getKey()+" : "+entry1.getValue());
                
            }
            System.out.println("End");
        }*/
                

        for ( int i = start ; i < max ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
//                System.out.println(" row "+i+" col "+j+" type "+colNamesType.get(j));
                if (colNamesType.get(j).equals("double")){
                    if (Double.isNaN(dataSet[i][j])){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
//                        System.out.println("double "+dataSet[i][j]);
                        Object a = dataSet[i][j];
                        linkedHashTemp.put(columnNames[j], dataSet[i][j]);
                    }
                }
                else if (colNamesType.get(j).equals("int")){
                    if (dataSet[i][j] == 2147483646.0){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        linkedHashTemp.put(columnNames[j], Integer.toString((int)dataSet[i][j])+"");
                    }
                }
                else{
//                    System.out.println("Value "+dataSet[i][j]+" row "+i+" col "+j+" type "+colNamesType.get(j));
                    String str = dictionary.getIdToString((int)dataSet[i][j]);
                    if(str == null){
                        str = this.dictHier.getIdToString((int)dataSet[i][j]);
                    }
//                    DictionaryString dict = dictionary.get(j);
//                    String str = dict.getIdToString((int)dataSet[i][j]);

                    if (str.equals("NaN")){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        linkedHashTemp.put(columnNames[j], str);
                    }
                }
            }
            data.add(linkedHashTemp);
            counter ++;
            
        }
        
        
        
        
        recordsTotal = sizeOfRows;
        recordsFiltered = sizeOfRows;
        

        
        
        return data;
        
    }
    
    @JsonView(View.SmallDataSet.class)
    @Override
    public String[][] getTypesOfVariables(String [][]smallDataSet) {
        this.smallDataSet = smallDataSet;
        String []str = null;
        String []columnTypes = null;
        typeArr = new String[smallDataSet[0].length][];
    
        for ( int i = 0 ; i < 1 ; i ++){
            columnTypes = new String[smallDataSet[i].length];
            for ( int j = 0 ; j < smallDataSet[i].length ; j ++ ){
                if ( smallDataSet[i][j] != null ){
                    if (smallDataSet[i][j].equals("string")){
                        str = new String[1];
                        str[0] = "string";
                        columnTypes[j] = "string";
                    }
                    else if (smallDataSet[i][j].equals("int")){
                        str = new String[3];
                        str[0] = "int";
                        str[1] = "string";
                        str[2] = "double";
                        columnTypes[j] = "int";
                    }
                    else if (smallDataSet[i][j].equals("date")){
                        str = new String[2];
                        str[0] = "date";
                        str[1] = "string";
                        columnTypes[j] = "date";
                    }
                    else {
                        str = new String[2];
                        str[0] = "double";
                        str[1] = "string";
                        columnTypes[j] = "double";

                    }
                }
                else{
                    str = new String[4];
                    str[0] = "string";
                    str[1] = "int";
                    str[2] = "double";
                    str[3] = "date";
                    columnTypes[j] = "string";
                }
                typeArr[j] = str;
            }
            
        
        }

        return typeArr;
    }

    public String[][] getTypeArr() {
        return typeArr;
    }


    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    
    
    public int getRecordsTotal() {
        return recordsTotal;
    }
    
    
    public static String timestampToDate( String tmstmp){
      String [] temp = null;
        if ( tmstmp != null ) {
           for (String parse : formats) {
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                //System.out.println("edwwwwwww = ");
                try {
                    Date d1 = sdf.parse(tmstmp);
                    if (sdf.parse(tmstmp) != null){
                        if ( parse.equals("yyyy/MM/dd")){
                            if ( tmstmp.contains("/")){// den mporei na dei to dd/MM/yyyy giati prwta vlepei to yyyy//MM/dd
                                temp = tmstmp.split("/");
                                if (temp[0].length() <= 2){
                                    return tmstmp;
                                }
                                else{
//                                    System.out.println("parse = " + parse);
                                    //System.out.println("date111111" );
                                    SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

                                    //Date date = sf.parse(tmstmp); 
                                    //System.out.println("date = " + date );
//                                    System.out.println("return = " +sf.format(d1));
                                    //System.out.println(date.);
                                    tmstmp = null;
                                    tmstmp = sf.format(d1);
                                    return tmstmp;
                                }
                            } 
                        }
                        else{
//                            System.out.println("parse = " + parse);
                            //System.out.println("date111111" );
                            SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

                            //Date date = sf.parse(tmstmp); 
                            //System.out.println("date = " + date );
//                            System.out.println("return = " +sf.format(d1));
                            //System.out.println(date.);
                            tmstmp = null;
                            tmstmp = sf.format(d1);
                            return tmstmp;
                        }                        
                    }
 
                    //System.out.println("Printing the value of " + parse);
                } catch (ParseException e) {

                }
            }
        }
        
        return null;
    }

    @Override
    public void exportOriginalData() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        try (PrintWriter writer = new PrintWriter(this.inputFile, "UTF-8")) {
            boolean FLAG = false;
                
            for(int i = 0 ; i < columnNames.length ; i ++){
                if (FLAG == false){
                    writer.print(columnNames[i]);
                    FLAG = true;
                }
                else{
                    writer.print(","+columnNames[i]);
                }

            }
            writer.println();
            
            for(int i=0; i<this.sizeOfRows; i++){
                for(int j=0; j<this.sizeOfCol; j++){
                    if (colNamesType.get(j).equals("double")){
                        if (Double.isNaN(dataSet[i][j])){
                            writer.print("");
                        }
                        else{
                            Object a = dataSet[i][j];
                            writer.print( dataSet[i][j]);
                        }
                    }
                    else if (colNamesType.get(j).equals("int")){
                        if (dataSet[i][j] == 2147483646.0){
                            writer.print("");
                        }
                        else{
                            writer.print( Integer.toString((int)dataSet[i][j])+"");
                        }
                    }
                    else{
                        String str = dictionary.getIdToString((int)dataSet[i][j]);
                        if(str == null){
                            str = this.dictHier.getIdToString((int)dataSet[i][j]);
                        }
    //                    DictionaryString dict = dictionary.get(j);
    //                    String str = dict.getIdToString((int)dataSet[i][j]);

                        if (str.equals("NaN")){
                            writer.print("");
                        }
                        else{
                            writer.print(str);
                        }
                    }
                    
                    if(j!=this.sizeOfCol-1){
                        writer.print(",");
                    }
                }
                writer.println();
            }
            
        }catch(FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("done orgiginal data");
    }

    @Override
    public int getDataColumns() {
        return this.sizeOfCol;
    }

}
