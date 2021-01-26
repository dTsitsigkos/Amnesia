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
import javax.swing.JTable;
import javax.swing.table.TableModel;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import jsoninterface.View;


/**
 *
 * @author jimakos
 */
public class SETData implements Data,Serializable {
    @JsonView(View.GetColumnNames.class)
    private double dataSet[][] = null;
    @JsonView(View.GetColumnNames.class)
    private String inputFile = null;
    private int sizeOfRows = 0;
    private int sizeOfCol = 0;
    private String delimeter = null;
    @JsonView(View.GetDataTypes.class)
    private Map <Integer,String> colNamesType = null;
    private CheckVariables chVar = null;
    private Map <Integer,String> colNamesPosition = null;
//    private Map <Integer,DictionaryString> dictionary = null;
    private DictionaryString dictionary = null;
    private DictionaryString dictHier = null;
    @JsonView(View.DataSet.class)
    private ArrayList<LinkedHashMap> data;
    @JsonView(View.GetColumnNames.class)
    private String []columnNames = null;
    @JsonView(View.DataSet.class)
    private int recordsTotal;
    @JsonView(View.DataSet.class)
    private int recordsFiltered;
    @JsonView(View.SmallDataSet.class)
    private String[][] smallDataSet;
    

    
    
    public SETData(String inputFile, String del,DictionaryString dict){
        colNamesType = new TreeMap<Integer,String>();
        colNamesPosition = new HashMap<Integer,String>();
        chVar = new CheckVariables();
//        dictionary = new HashMap <Integer,DictionaryString>();
        dictionary = new DictionaryString();
        dictHier = dict;
        
        
        this.inputFile = inputFile;
        System.out.println("DElimeter Set "+del);
        if ( del == null ){
            delimeter = ",";
        }
        else{
            delimeter = del;
        
        }
        
        
        /*try {
        fstream = new FileInputStream(inputFile);
        in = new DataInputStream(fstream);
        br = new BufferedReader(new InputStreamReader(in));
        
        while ((strLine = br.readLine()) != null)   {
        
        //save column names
        if (FLAG == true){
        colNamesType.put(0,null);
        colNamesPosition.put(0,strLine);
        FLAG = false;
        }
        
        //save column types
        else{
        temp = strLine.split(delimeter);
        if (chVar.isInt(temp[0])){
        colNamesType.put(0, "int");
        }
        else if (chVar.isDouble(temp[0])){
        colNamesType.put(0, "double");
        }
        else{
        colNamesType.put(0, "string");
        dictionary.put(0, new Dictionary());
        }
        
        sizeOfCol = temp.length;
        break;
        }
        
        }
        
        in.close();
        
        }catch (Exception e){
        System.err.println("Error: " + e.getMessage());
        }*/
    }
    
    
    @Override
    public double[][] getDataSet() {
        return dataSet;
    }
    
    @Override
    public int getDataColumns() {
        return this.sizeOfCol;
    }
    
    @Override
    public void setData(double[][] _dataSet) {
        this.dataSet = _dataSet;
    }
    
    @Override
    public int getDataLenght() {
        return dataSet.length;
    }
    
    @Override
    public void print() {
        int i,j;
        for (i = 0 ; i < dataSet.length ; i ++){
            // System.out.println("here : " + data.length + "\t" +data[i].length );
            for (j = 0 ; j < dataSet[i].length ; j++){
                System.out.print(dataSet[i][j]+",");
            }
            System.out.println();
        }
    }
    
    @Override
    public String save(boolean[] nothing1) {
        dataSet = new double[sizeOfRows][];
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String colNames = null;
        int counter = 0;
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            System.out.println("Both Empty");
            stringCount = 1;
        }
        else {
            stringCount = dictHier.getMaxUsedId()+1;
        }
        boolean FLAG = true;
        smallDataSet = new String[6][1];
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            System.out.println("del save set "+delimeter);
            
            while ((strLine = br.readLine()) != null){
                
                //do not read the fist line
                if (FLAG == true){
                    colNames = strLine;
                    FLAG = false;
                    smallDataSet[0][0] = strLine;
                }
                else{
//                    System.out.println("strLine = " + strLine);
  
                   
                    temp = strLine.split("\\"+delimeter,-1);
//                    System.out.println("Set Data "+temp[0]);
                    
                    
                    dataSet[counter] = new double[temp.length];
                    if ( counter < 5){
                        smallDataSet[counter+1][0] = strLine;
                    }
                    
                    for (int i = 0; i < temp.length ; i ++ ){
//                        System.out.println("Set Data "+temp[i]);
//                        DictionaryString tempDict = dictionary.get(0);
                        String var = null;

                        if ( !temp[i].equals("")){
                            var = temp[i];
                        }
                        else {
                            var = "NaN";
                        }
                        if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                            if(var.equals("79902")){
                                System.out.println("No in dictionaries");
                            }
                            if(var.equals("NaN")){
                               dictionary.putIdToString(2147483646, var);
                               dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                               dataSet[counter][i] = 2147483646.0;
                           }
                           else{
                               dictionary.putIdToString(stringCount, var);
                               dictionary.putStringToId(var,stringCount);
//                                    dictionary.put(counter1, tempDict);
                               dataSet[counter][i] = stringCount;
                               stringCount++;
                           }
                       }
                       else{
                           //if string is present in the dictionary, get its id
                           if(dictionary.containsString(var)){
                               if(var.equals("79902")){
                                    System.out.println("In dictionary data");
                                }
                               int stringId = dictionary.getStringToId(var);
                               dataSet[counter][i] = stringId;
                           }
                           else{
                               if(var.equals("79902")){
                                    System.out.println("In dictionary hier");
                                }
                               int stringId = this.dictHier.getStringToId(var);
                               dataSet[counter][i] = stringId;
                           }
                       }
                        
                        //if string is not present in the dictionary
//                        if (dictionary.containsString(temp[i]) == false){
//                            dictionary.putIdToString(stringCount, temp[i]);
//                            dictionary.putStringToId(temp[i],stringCount);
////                            dictionary.put(i, tempDict);
//                            dataSet[counter][i] = stringCount;
//                            stringCount++;
//                        }
//                        else{
//                            //if string is present in the dictionary, get its id
//                            int stringId = dictionary.getStringToId(temp[i]);
//                            dataSet[counter][i] = stringId;
//                        }
                    }
                    counter++;
                }
            }
            
            in.close();
            
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return "OK";
    }
    
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
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    @Override
    public String readDataset(String []nothing, boolean[] nothing1) throws LimitException {
        SaveClmnsAndTypeOfVar(nothing,nothing1);
        preprocessing();
        String result = save(nothing1);
        
        return result;
    }
    
    
    @Override
    public Map<Integer, String> getColNamesPosition() {
        return colNamesPosition;
    }
    
//    @Override
//    public Map<Integer, DictionaryString> getDictionary() {
//        return this.dictionary;
//    }
    
    @Override
    public DictionaryString getDictionary() {
        return this.dictionary;
    }
    
//    @Override
//    public void setDictionary(Integer column, DictionaryString dict) {
//        this.dictionary.put(column, dict);
//    }
    
    @Override
    public int getColumnByName(String column) {
        for(Integer i : this.colNamesPosition.keySet()){
            if(this.colNamesPosition.get(i).equals(column)){
                return i;
            }
        }
        return -1;
    }
    
//    @Override
//    public void replaceColumnDictionary(Integer column, DictionaryString dict) {
//        DictionaryString curDict = this.dictionary.get(column);
//        
//        for (double[] row : dataSet) {
//            
//            for(int j=0; j<row.length; j++){
//                
//                //retrieve actual value from dictionary
//                String columnValue = curDict.getIdToString((int)row[j]);
//                
//                //replace with value from new dictionary
//                row[j] = dict.getStringToId(columnValue);
//            }
//            
//        }
//        
//        //set given dictionary as the new one
//        setDictionary(column, dict);
//    }
    
    @Override
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, 
            int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
        
        Object[][] temp = null;
        if ( initialTable != null ){
            //System.out.println("initial table");
            temp = initialTable;
        }
        else{
            //System.out.println("anonymized table");
            temp = anonymizedTable;
        }
        
        try {
            
            try (PrintWriter writer = new PrintWriter( file, "UTF-8")) {

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
                
                //Object[] rowQIs = new Object[qids.length];
                
                //write table data
                for (int row = 0; row < temp.length; row++){
                    
                    //if suppressed values exist
                    /*if(suppressedValues != null){
                        
                        //get qids of this row
                        for(int i=0; i<qids.length; i++){
                            rowQIs[i] = model.getValueAt(row, qids[i]+1);
                        }
                        
                        //check if row is suppressed
                        if(isSuppressed(rowQIs, qids, suppressedValues)){
                            continue;
                        }
                    }*/
                    
                    //write row to file
                    for(int column = 0; column < temp[0].length; column++){
                        Object value = temp[row][column];
                        
                 
                        if (!value.equals("(null)")){
                            writer.print(value);
                        }
                            
                        
                        
                        if(column != temp[column].length-1){
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
                writer.flush();
                writer.close();
            }
            
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println("done");
        /*try {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                TableModel model =  anonymizedTable.getModel();
                int columnCount = model.getColumnCount();
                
                //write column names
                for(int column = 1; column < columnCount; column++){
                    writer.print(model.getColumnName(column));
                    if(column != columnCount-1){
                        writer.print(",");
                    }
                }
                writer.println();
                
                //write table data
                for (int row = 0; row < model.getRowCount(); row++){
                    for(int column = 1; column < columnCount; column++){
                        writer.print(model.getValueAt(row, column));
                        if(column != columnCount-1){
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
           // Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    @Override
    public String getColumnByPosition(Integer columnIndex) {
        return this.colNamesPosition.get(columnIndex);
    }
    
    @Override
    public void SaveClmnsAndTypeOfVar(String []nothing, boolean[] nothing1) {
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String []colNames = null;
        boolean FLAG = true;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            
            while ((strLine = br.readLine()) != null)   {
                
                //save column names
                if (FLAG == true){
                    colNamesType.put(0,null);
                    colNamesPosition.put(0,strLine);
                    columnNames = new String[1];
                    columnNames[0] = strLine;
                    FLAG = false;
                }
                
                //save column types
                else{
                    /*temp = strLine.split(delimeter);
                    if (chVar.isInt(temp[0])){
                    colNamesType.put(0, "int");
                    }
                    else if (chVar.isDouble(temp[0])){
                    colNamesType.put(0, "double");
                    }
                    else{*/
                    colNamesType.put(0, "string");
//                    dictionary.put(0, new DictionaryString());
                    //}
                    
                    sizeOfCol = 1;
                    break;
                }
                
            }
            
            in.close();
            
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
    @Override
    public String findColumnTypes() {//den kaleitai pouthena akoma
        /*FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        boolean FLAG = true;
        String []typeOfVar = new String[1];
        
        try {
        fstream = new FileInputStream(inputFile);
        in = new DataInputStream(fstream);
        br = new BufferedReader(new InputStreamReader(in));
        
        while ((strLine = br.readLine()) != null)   {
        
        //escape first row
        if (FLAG == true){
        FLAG = false;
        }
        
        //save column types
        else{
        temp = strLine.split(delimeter);
        if (chVar.isInt(temp[0])){
        typeOfVar[0] = "int";
        }
        else if (chVar.isDouble(temp[0])){
        typeOfVar[0] = "double";
        }
        else{
        typeOfVar[0] = "String";
        }
        break;
        }
        }
        in.close();
        
        }catch (Exception e){
        System.err.println("Error: " + e.getMessage());
        }
        
        return typeOfVar;*/
        
        return null;
    }
    
     public String[] getColumnNames() {
        return columnNames;
    }
    
    
    @Override
    public String[][] getSmallDataSet() {
        return smallDataSet;
    }
    

    @Override
    public String[][] getTypesOfVariables(String[][] smallDataSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<LinkedHashMap> getPage(int start,int length) {
        boolean FLAG = false;
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
        
        for ( int i = start ; i < max ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            FLAG = false;
            String var;
            for (int j = 0 ; j < dataSet[i].length ; j ++){
//                DictionaryString dict = dictionary.get(0);
                //System.out.println()
                if (FLAG == false){
                    var = dictionary.getIdToString((int)dataSet[i][j]);
                    if(var == null){
                        var = dictHier.getIdToString((int)dataSet[i][j]);
                    }
                    linkedHashTemp.put(columnNames[0], var );
                //System.out.println( dict.getIdToString((int)dataSet[i][j]));
                //linkedHashTemp.put(columnNames[0], null);
                    FLAG = true;
                }
                else{
                    var = dictionary.getIdToString((int)dataSet[i][j]);
                    if(var == null){
                        var = dictHier.getIdToString((int)dataSet[i][j]);
                    }
                    linkedHashTemp.put(columnNames[0], linkedHashTemp.get(columnNames[0]) +delimeter+var);
                }
                
            }
            //System.out.println("temp = " + linkedHashTemp);
            data.add(linkedHashTemp);
            counter ++;
            
        }
        
        recordsTotal = sizeOfRows;
        recordsFiltered = sizeOfRows;
        
        //System.out.println("number ofPages = " + this.recordsTotal);
        
        return data;
        /*for ( int i = 0 ; i < dataSet.length; i ++ ){
            for ( int j = 0 ; j < dataSet[i].length ; j++) {
                System.out.print(dict.getIdToString((int)dataSet[i][j]) +",");
            }
            System.out.println();
        }
        
        return null;*/
    }

    @Override
    public int getRecordsTotal() {
        return recordsTotal;
    }

    @Override
    public Map<Integer, String> getColNamesType() {
        return colNamesType;
    }

    @Override
    public String getInputFile() {
        String delimiter = "/";
        String[] temp = inputFile.split(delimiter);
        return temp[temp.length-1];
    }

    @Override
    public void exportOriginalData() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        LinkedHashMap linkedHashTemp = null;
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
                linkedHashTemp = new LinkedHashMap<>();
                FLAG = false;
                String var;
                for (int j = 0 ; j < dataSet[i].length ; j ++){
    //                DictionaryString dict = dictionary.get(0);
                    //System.out.println()
//                    if (FLAG == false){
//                        linkedHashTemp.put(columnNames[0], dictionary.getIdToString((int)dataSet[i][j]));
//                        writer.print(dictionary.getIdToString((int)dataSet[i][j]));
//                    //System.out.println( dict.getIdToString((int)dataSet[i][j]));
//                    //linkedHashTemp.put(columnNames[0], null);
//                        FLAG = true;
//                    }
//                    else{
//                        writer.print(linkedHashTemp.get(columnNames[0]) +","+dictionary.getIdToString((int)dataSet[i][j]));
//                        linkedHashTemp.put(columnNames[0], linkedHashTemp.get(columnNames[0]) +","+dictionary.getIdToString((int)dataSet[i][j]));
//                        
//                    }
                    var = dictionary.getIdToString((int)dataSet[i][j]);
                    if(var == null){
                        var = dictHier.getIdToString((int)dataSet[i][j]);
                    }
                    writer.print(var);
                    if(j!= dataSet[i].length-1){
                        writer.print(",");
                    }
                    
                }
                writer.println();
                //System.out.println("temp = " + linkedHashTemp);
            }
            
        }catch(FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public SimpleDateFormat getDateFormat(int column) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    

}
