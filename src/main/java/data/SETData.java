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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
    @JsonView(View.GetDataTypes.class)
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
    private Map<Integer,Integer> randomizedMap;
    @JsonView(View.GetDataTypes.class)
    boolean pseudoanonymized = false;
    

    
    
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
                    colNames = strLine.trim().replaceAll("\"", "").replaceAll("[\uFEFF-\uFFFF]", "").replace(".", "").replace("[","(").replace("]", ")");;
                    if (colNames.length() > 128){
                        colNames = colNames.substring(0, 128);
                    }
                    
                    FLAG = false;
                    smallDataSet[0][0] = colNames;
                }
                else if(strLine.trim().isEmpty()){
                    continue;
                }
                else{
//                    System.out.println("strLine = " + strLine);
  
                   
                    temp = strLine.split("\\"+delimeter,-1);
//                    System.out.println("Set Data "+temp[0]);
                    
                    
                    dataSet[counter] = new double[temp.length];
                    if ( counter < 5){
                        smallDataSet[counter+1][0] = strLine.replaceAll("[\uFEFF-\uFFFF]", "");
                    }
                    
                    for (int i = 0; i < temp.length ; i ++ ){
//                        System.out.println("Set Data "+temp[i]);
//                        DictionaryString tempDict = dictionary.get(0);
                        String var = null;
                        temp[i] = temp[i].trim().replaceAll("[\uFEFF-\uFFFF]", "");
                        if ( !temp[i].equals("")  && !temp[i].equals("\"\"")){
                            var = temp[i];
                        }
                        else {
                            var = "NaN";
                        }
                        if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
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
                               int stringId = dictionary.getStringToId(var);
                               dataSet[counter][i] = stringId;
                           }
                           else{
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
                if(!strLine.trim().isEmpty()){
                    counter++;
                }
                if(AppCon.os.equals(online_version) && counter > online_rows){
                    throw new LimitException("Dataset is too large, the limit is "+online_rows+" rows, please download desktop version, the online version is only for simple execution.");
                }
            }
            
            //System.out.println("counter = " + counter);
            sizeOfRows = counter;
            recordsTotal = sizeOfRows;
            recordsFiltered = sizeOfRows;
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
                Random rand = new Random();
                List<Integer> randomNumbers = rand.ints(0, this.sizeOfRows).distinct().limit(this.sizeOfRows).boxed().collect(Collectors.toList());
                this.randomizedMap = new HashMap();
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
                    int randomIndexToSwap = randomNumbers.get(row);
                    if(this.randomizedMap.containsKey(randomIndexToSwap)){
                        System.out.println("Problem with "+randomIndexToSwap);
                    }
                    this.randomizedMap.put(row,randomIndexToSwap);
                    //write row to file
                    for(int column = 0; column < temp[0].length; column++){
                        Object value = temp[randomIndexToSwap][column];
                        
                 
                        if (!value.equals("(null)")){
                            writer.print(value);
                        }
                            
                        
                        
                        if(column != temp[row].length-1){
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
                    strLine = strLine.trim().replaceAll("\"", "").replaceAll("[\uFEFF-\uFFFF]", "").replace(".", "").replace("[","(").replace("]", ")");
                    if (strLine.length() > 128){
                        strLine = strLine.substring(0, 128);
                    }
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
         return this.inputFile.substring(this.inputFile.lastIndexOf(File.separator)+1);
    }

    @Override
    public void exportOriginalData() {
        String path = this.inputFile.substring(0, this.inputFile.lastIndexOf(File.separator));
        System.out.println("Source path "+path);
        String mapFile = path + File.separator + "map.txt";
        try (PrintWriter writer = new PrintWriter(this.inputFile, "UTF-8")) {
            writer.print("Row ID");
                
            for(int i = 0 ; i < columnNames.length ; i ++){
                writer.print(","+columnNames[i]);
            }
            writer.println();
            Random rand = new Random();
            List<Integer> randomNumbers = rand.ints(0, this.sizeOfRows).distinct().limit(this.sizeOfRows).boxed().collect(Collectors.toList());
            this.randomizedMap = new HashMap();
            
            for(int i=0; i<this.sizeOfRows; i++){
                String var;
                int randomIndexToSwap = randomNumbers.get(i);
                if(this.randomizedMap.containsKey(i)){
                    System.out.println("Problem with "+i);
                }
                this.randomizedMap.put(i,randomIndexToSwap);
                writer.print((i+1)+",");
                for (int j = 0 ; j < dataSet[randomIndexToSwap].length ; j ++){
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
                    var = dictionary.getIdToString((int)dataSet[randomIndexToSwap][j]);
                    if(var == null){
                        var = dictHier.getIdToString((int)dataSet[randomIndexToSwap][j]);
                    }
                    writer.print(var);
                    if(j!= dataSet[randomIndexToSwap].length-1){
                        writer.print(",");
                    }
                    
                }
                writer.println();
                //System.out.println("temp = " + linkedHashTemp);
            }
            
        }catch(FileNotFoundException | UnsupportedEncodingException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        File m = new File(mapFile);
        File z = new File(path+File.separator+"anonymized_files.zip");
        try {
            System.out.println("map file "+mapFile);
            m.createNewFile();
            z.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("mexssage create map"+ex.getMessage());
            Logger.getLogger(TXTData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try(PrintWriter writer = new PrintWriter(mapFile, "UTF-8")){
            writer.print("Export row ID -> Original row");
            writer.println();
            for(int i=0; i<this.sizeOfRows; i++){
                writer.print((i+1)+" -> "+(this.randomizedMap.get(i)+1));
                writer.println();
            }
            writer.flush();
            writer.close();
        }catch(FileNotFoundException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
            System.out.println("mexssage map"+ex.getMessage());
        }
        
        try {
            FileInputStream in1 = new FileInputStream(this.inputFile);
            FileInputStream in2 = new FileInputStream(mapFile);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path+File.separator+"anonymized_files.zip"));
            
            out.putNextEntry(new ZipEntry(this.inputFile.substring(this.inputFile.lastIndexOf(File.separator)+1))); 

            byte[] b = new byte[2048];
            int count;

            while ((count = in1.read(b)) > 0) {
                out.write(b, 0, count);
            }
            in1.close();
            out.closeEntry();
            out.putNextEntry(new ZipEntry("map.txt"));
            count=0;
            b = new byte[2048];
            
            while ((count = in2.read(b)) > 0) {
                out.write(b, 0, count);
            }
            in2.close();
            out.closeEntry();
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("mexssage zip"+ex.getMessage());
            Logger.getLogger(TXTData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("mexssage zip io"+ex.getMessage());
            Logger.getLogger(TXTData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public SimpleDateFormat getDateFormat(int column) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setMask(int column, int[] positions, char character) {
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            System.out.println("Both empy load data");
            stringCount = 1;
        }
        else if(!dictionary.isEmpty() && !dictHier.isEmpty()){
            System.out.println("Both have values");
            if(dictionary.getMaxUsedId() > dictHier.getMaxUsedId()){
                stringCount = dictionary.getMaxUsedId()+1;
            }
            else{
                stringCount = dictHier.getMaxUsedId()+1;
            }
        }
        else if(dictionary.isEmpty()){
            System.out.println("Dict data empty");
            stringCount = dictHier.getMaxUsedId()+1;
        }
        else{
            System.out.println("Dict hier empty");
            stringCount = dictionary.getMaxUsedId()+1;
        }
        
        for(int i=0; i<this.sizeOfRows; i++){
            for(int j=0; j<dataSet[i].length; j++){
                String var = dictionary.getIdToString((int)dataSet[i][j]);
                if(var == null){
                    var = this.dictHier.getIdToString((int)dataSet[i][j]);
                }

                if(var.equals("NaN")){
                    continue;
                }

                for(int pos : positions){
                    if(pos<var.length()){
                        var = var.substring(0,pos)+character+var.substring(pos+1);
                    }
                }


                if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                    if(var.equals("NaN")){
                       dictionary.putIdToString(2147483646, var);
                       dictionary.putStringToId(var,2147483646);
        //                                        dictionary.put(counter1, tempDict);
                       dataSet[i][j] = 2147483646.0;
                   }
                   else{
                       dictionary.putIdToString(stringCount, var);
                       dictionary.putStringToId(var,stringCount);
        //                                    dictionary.put(counter1, tempDict);
                       dataSet[i][j] = stringCount;
                       stringCount++;
                   }
               }
               else{
                   //if string is present in the dictionary, get its id
                   if(dictionary.containsString(var)){
                       int stringId = dictionary.getStringToId(var);
                       dataSet[i][j] = stringId;
                   }
                   else{
                       int stringId = this.dictHier.getStringToId(var);
                       dataSet[i][j] = stringId;
                   }
               }
            }
        }
        this.pseudoanonymized = true;
    }

    

}
