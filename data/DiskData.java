/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import exceptions.LimitException;
import algorithms.clusterbased.Clusters;
import anonymizeddataset.AnonymizedDataset;
import com.fasterxml.jackson.annotation.JsonView;
import controller.AppCon;
import static data.Data.online_rows;
import static data.Data.online_version;
import dictionary.DictionaryString;
import exceptions.DateParseException;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.list;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import jsoninterface.View;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author nikos
 */
public class DiskData implements Data,Serializable{
    @JsonView(View.GetColumnNames.class)
    private String inputFile = null;
    @JsonView(View.GetColumnNames.class)
    private String dataType = "disk";
    private String delimeter = null;
    private int sizeOfRows = 0;
    private String urlDatabase = null;
    private Connection conn = null;
    private int sizeOfCol = 0;
    @JsonView(View.SmallDataSet.class)
    private String[][] smallDataSet;
    @JsonView(View.GetDataTypes.class)
    private Map <Integer,String> colNamesType = null;
    private CheckVariables chVar = null;
    private Map <Integer,String> colNamesPosition = null;
    @JsonView(View.GetColumnNames.class)
    private String []columnNames = null;
    @JsonView(View.SmallDataSet.class)
    private String errorMessage = null;
    @JsonView(View.SmallDataSet.class)
    private String[][] typeArr;
    private DictionaryString dictionary;
    private DictionaryString dictHier;
    @JsonView(View.DataSet.class)
    private ArrayList<LinkedHashMap> data;
    @JsonView(View.DataSet.class)
    private int recordsTotal;
    @JsonView(View.DataSet.class)
    private int recordsFiltered;
    private Set<Integer> anonymizedRecords;
    private List<Double[][]> anonymizedRecordsClusters;
    private String anonymizeQuery = null;
    private PreparedStatement anonymizeStatement = null;
    private String urlDbTemp = null;
    public static String Lock = "dblock";
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
    
    public DiskData(String inputfile,String del,DictionaryString dict){
        this.recordsTotal = 0;
        this.inputFile = inputfile;
        this.delimeter = del;
        colNamesType = new TreeMap<Integer,String>();
        colNamesPosition = new HashMap<Integer,String>();
        chVar = new CheckVariables();
        this.dictionary = new DictionaryString();
        this.dictHier = dict;
        this.anonymizedRecords = new HashSet();
        this.anonymizedRecordsClusters = new ArrayList();
        
        System.out.println("Input path "+inputFile);
//        String[] folders = inputFile.split(File.separator);
        String name = "anonymization.db";
        
        
        this.urlDatabase = "jdbc:sqlite:"+this.inputFile.substring(0,this.inputFile.lastIndexOf("/"))+File.separator+name;
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
            conn = DriverManager.getConnection(this.urlDatabase);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (Exception ex) {
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    
    public String getUrlDataBase(){
        return this.urlDatabase;
    }
    
    public double[][] getOriginalDataSet(){
        Statement stm = null;
        String sqlQuery = "SELECT * FROM dataset";
        ResultSet rs = null;
        double[][] original_data = null;
        try{
            stm = this.conn.createStatement();
            rs = stm.executeQuery(sqlQuery);
            ResultSetMetaData metaData = rs.getMetaData();
            original_data = new double[this.recordsTotal][metaData.getColumnCount()-1];
            int counterRow = 0;
            while(rs.next()){
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    original_data[counterRow][entry.getKey()] = rs.getDouble(this.columnNames[entry.getKey()]);
                }
                counterRow++;
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: get all disk data "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return original_data;
    }
    
    public double[][] getAnonymizedDataSet(){
        Statement stm = null;
        String sqlQuery = "SELECT * FROM anonymized_dataset";
        ResultSet rs = null;
        double[][] anonymized_data = null;
        try{
            stm = this.conn.createStatement();
            rs = stm.executeQuery(sqlQuery);
            ResultSetMetaData metaData = rs.getMetaData();
            anonymized_data = new double[this.recordsTotal][metaData.getColumnCount()-1];
            int counterRow = 0;
            while(rs.next()){
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    anonymized_data[counterRow][entry.getKey()] = rs.getDouble(this.columnNames[entry.getKey()]);
                }
                counterRow++;
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: get all disk data "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return anonymized_data;
    }

    @Override
    public double[][] getDataSet() {
        return this.getOriginalDataSet();
    }

    @Override
    public void setData(double[][] _data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDataLenght() {
        return this.recordsTotal;
    }

    @Override
    public int getDataColumns() {
        return this.sizeOfCol;
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportOriginalData() {
//        double[][] dataSet = this.getDataSet();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.inputFile, true), StandardCharsets.UTF_8)))) {
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
            
            Double[][] dataSet;
            int start=0;
            int end = this.getRecordsTotal()/4;
            while(end<=this.getRecordsTotal()){
                dataSet = this.getSpecificDataset(start, end, true);
                for(int i=0; i<dataSet.length; i++){
                    Double[] record = dataSet[i];
                    for(int j=0; j<this.sizeOfCol; j++){
                        if (colNamesType.get(j).equals("double")){
                            if (Double.isNaN(record[j+1]) || record[j+1] == 2147483646.0){
                                writer.print("");
                            }
                            else{
                                writer.print( record[j+1]);
                            }
                        }
                        else if(colNamesType.get(j).equals("int")){
                            if (record[j+1] == 2147483646.0){
                                writer.print("");
                            }
                            else{
                                writer.print(record[j+1].intValue()+"");
                            }
                        }
                        else if(colNamesType.get(j).equals("date")){
                            if (record[j+1] == 2147483646.0){
                                writer.print("");
                            }
                            else{
                                Date date = new Date(record[j+1].longValue());
                                SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
                                writer.print( df2.format(date));
                            }
                        }
                        else{
                            String str = dictionary.getIdToString(record[j+1].intValue());
                            if(str == null){
                                str = this.dictHier.getIdToString(record[j+1].intValue());
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
                if(end==this.getRecordsTotal()){
                    break;
                }
                else{
                    start = end;
                    end += end;
                    if(end > this.getRecordsTotal()){
                        end = this.getRecordsTotal();
                    }
                }
            }
            
//            for(int i=0; i<this.sizeOfRows; i++){
//                for(int j=0; j<this.sizeOfCol; j++){
//                    if (colNamesType.get(j).equals("double")){
//                        if (Double.isNaN(dataSet[i][j]) || dataSet[i][j] == 2147483646.0){
//                            writer.print("");
//                        }
//                        else{
//                            Object a = dataSet[i][j];
//                            writer.print( dataSet[i][j]);
//                        }
//                    }
//                    else if(colNamesType.get(j).equals("int")){
//                        if (dataSet[i][j] == 2147483646.0){
//                            writer.print("");
//                        }
//                        else{
//                            writer.print( Integer.toString((int)dataSet[i][j])+"");
//                        }
//                    }
//                    else{
//                        String str = dictionary.getIdToString((int)dataSet[i][j]);
//    //                    DictionaryString dict = dictionary.get(j);
//    //                    String str = dict.getIdToString((int)dataSet[i][j]);
//
//                        if (str.equals("NaN")){
//                            writer.print("");
//                        }
//                        else{
//                            writer.print(str);
//                        }
//                    }
//                    
//                    if(j!=this.sizeOfCol-1){
//                        writer.print(",");
//                    }
//                }
//                writer.println();
//            }
        }catch (FileNotFoundException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String save(boolean[] checkColumns) throws LimitException, DateParseException {
        long start = System.currentTimeMillis();
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        String strLine = null;
        String []temp = null;
        String []colNames = null;
        boolean firstInsert = true;
        SimpleDateFormat sdf[] = new SimpleDateFormat[this.columnNames.length];
        SimpleDateFormat sdfDefault = new SimpleDateFormat("dd/MM/yyyy");
        ArrayList<String> columns = new ArrayList<String>();
        int counter = 0;
        int counterSdf = 0;
//        int stringCount = dictionary.getMaxUsedId()+1;
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            stringCount = 1;
        }
        else {
            stringCount = dictHier.getMaxUsedId()+1;
        }
        this.errorMessage="";
        boolean FLAG = true;
        int counter1 = 0 ;
        Object [][] dataSet = new Object[1][this.sizeOfCol];
        String sqlCreateTable = "CREATE TABLE dataset (\n"
                + " id integer PRIMARY KEY,\n";
        Statement stmnt = null;
        PreparedStatement pstmt = null;
        
        
        try{
            stmnt = conn.createStatement();
            
            stmnt.executeUpdate("DROP TABLE IF EXISTS dataset;");
            for(Entry<Integer,String> entry : this.colNamesPosition.entrySet()){
                counter++;
                sqlCreateTable += " "+entry.getValue();
                String dataType = this.colNamesType.get(entry.getKey());
                if(dataType.equals("int")){
                    dataType = " integer";
                }
                else if(dataType.equals("double")){
                    dataType = " real";
                }
                else if(dataType.equals("date") || dataType.equals("string")){
                    dataType = " real";
                }
                
                if(counter == this.colNamesPosition.size()){
                   sqlCreateTable += dataType+"\n);";
                }
                else{
                    sqlCreateTable += dataType+",\n";
                }
                
            }
            
            
            System.out.println("Create dataset "+sqlCreateTable);
            stmnt.execute(sqlCreateTable);
            conn.setAutoCommit(false);
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));
            
            strLine = br.readLine();
            for(int i=0; i<checkColumns.length; i++){
                if(checkColumns[i]){
                    if(colNamesType.get(counterSdf).contains("date")){
                        sdf[counterSdf] = new SimpleDateFormat(this.formatsDate[counterSdf]);
                    }
                    counterSdf++;
                }
            }
            while ((strLine = br.readLine()) != null){
                
                temp = strLine.split(delimeter,-1);
                counter1 = 0;
                for (int i = 0; i < temp.length ; i ++ ){
                    if (checkColumns[i] == true){
                        if (colNamesType.get(counter1).contains("int") ){
                            if ( !temp[i].equals("")){
                                    try {
                                        dataSet[0][counter1] = Integer.parseInt(temp[i]);
                                    } catch (java.lang.NumberFormatException exc) {
                                        System.out.println("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                        return null;
                                    }   
                                }
                                else{
                                    dataSet[0][counter1] = 2147483646;
                                }
                        }
                        else if (colNamesType.get(counter1).contains("double")){
                            if ( !temp[i].equals("")){
                                temp[i] = temp[i].replaceAll(",", ".");
                                dataSet[0][counter1] = Double.parseDouble(temp[i]);
                            }
                            else{
                                dataSet[0][counter1] = 2147483646.0;
                            }
                        }
                        else if (colNamesType.get(counter1).contains("date")){
                            String var = null;
                            Date d;
                            if (var!=null ||  !temp[i].equals("")){
                                var = temp[i];
//                                var = TXTData.timestampToDate(var);
                                try{
                                    if(this.formatsDate[counter1].equals("dd/MM/yyyy")){
    //                                    var = sdf[counter1].parse(var) == null ? null : var;
                                        d = sdf[counter1].parse(var);
                                        if(d==null){
                                            var = null;
                                        }
                                    }
                                    else{
                                        d = sdf[counter1].parse(var);
                                        var = sdfDefault.format(d);
                                    }
                                }catch(ParseException pe){
                                    throw new DateParseException(pe.getMessage()+"\nDate format must be the same in column "+this.columnNames[counter1]);
                                }

                                if(var == null){
                                    var = "NaN";
                                }
                            }
                            else {
                                var = "NaN";
                                d = null;
                            }


                            //transform timestamp to date
                            //var = this.timestampToDate(var);
                            if(var == null || var.equals("NaN")){
                                var = "NaN";
                                if(!dictionary.containsId(2147483646) && !this.dictHier.containsId(2147483646)){
                                    dictionary.putIdToString(2147483646, var);
                                    dictionary.putStringToId(var,2147483646);
                                    
                                    dictHier.putIdToString(2147483646, var);
                                    dictHier.putStringToId(var,2147483646);
                                }
                                dataSet[0][counter1] = 2147483646.0;
                            }
                            else{
                                dataSet[0][counter1] = new Long(d.getTime()).doubleValue();
                            }

//                            if (var != null) {
//
//                            //if string is not present in the dictionary
//                                if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
//                                    if(var.equals("NaN")){
//                                        dictionary.putIdToString(2147483646, var);
//                                        dictionary.putStringToId(var,2147483646);
////                                        dictionary.put(counter1, tempDict);
//                                        dataSet[0][counter1] = 2147483646.0;
//                                    }
//                                    else{
////                                        dictionary.putIdToString(stringCount, var);
////                                        dictionary.putStringToId(var,stringCount);
////                                        dictionary.put(counter1, tempDict);
//                                        dataSet[0][counter1] = new Long(AnonymizedDataset.getDateFromString(var).getTime()).doubleValue();
//                                        stringCount++;
//                                    }
//                                }
//                                else{
//                                    //if string is present in the dictionary, get its id
//                                    int stringId = dictionary.getStringToId(var);
//                                    dataSet[0][counter1] = (double) stringId;
//                                }
//                            }
                        }
                        else{
                            String var = null;

                            if ( !temp[i].equals("")){
                                var = temp[i];
                            }
                            else {
                                var = "NaN";
                            }

                            //if string is not present in the dictionary
                            if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                 if(var.equals("NaN")){
                                    dictionary.putIdToString(2147483646, var);
                                    dictionary.putStringToId(var,2147483646);
                                    dataSet[0][counter1] = 2147483646.0;
                                }
                                else{
                                    dictionary.putIdToString(stringCount, var);
                                    dictionary.putStringToId(var,stringCount);
                                    dataSet[0][counter1] = (double)stringCount;
                                    stringCount++;
                                }
                            }
                            else{
                                //if string is present in the dictionary, get its id
//                                int stringId = dictionary.getStringToId(var);
//                                dataSet[0][counter1] = (double) stringId;
                                
                                if(dictionary.containsString(var)){
                                    int stringId = dictionary.getStringToId(var);
                                    dataSet[0][counter1] = (double) stringId;
                                }
                                else{
                                    int stringId = this.dictHier.getStringToId(var);
                                    dataSet[0][counter1] = (double) stringId;
                                }
                            }
                        }
                        counter1++;
                    }
                }
//                "INSERT INTO dataset(name,capacity) VALUES(?,?)"
                
                if(firstInsert){
                    String insertSql = "INSERT INTO dataset(";
                    String valuesStr ="";
                    for(int j=0; j<dataSet[0].length; j++){
                        if(j==dataSet[0].length-1){
                            insertSql += this.colNamesPosition.get(j)+") VALUES(";
                            valuesStr += "?)";
                        }
                        else{
                            insertSql += this.colNamesPosition.get(j)+",";
                            valuesStr += "?,";
                        }
                    }
                    
                    pstmt = conn.prepareStatement(insertSql+valuesStr);
                    firstInsert = false;
                }
                
                for(int j=0; j<dataSet[0].length; j++){
                    if(this.colNamesType.get(j).equals("int")){
                        pstmt.setInt(j+1, ((Integer)dataSet[0][j]));
                    }
                    else {
                        pstmt.setDouble(j+1, ((Double)dataSet[0][j]));
                    }
                }
                
                pstmt.executeUpdate();
                
                this.sizeOfRows++;
                if(AppCon.os.equals(online_version) && this.sizeOfRows > online_rows){
                    throw new LimitException("Dataset is too large, the limit is "+online_rows+" rows, please download desktop version, the online version is only for simple execution.");
                }
                
            }
            
            conn.commit();
            long end = System.currentTimeMillis();
            System.out.println("Time for save is "+(end-start)+"ms");
        }catch(IOException e){
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
            this.errorMessage = "2";
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
            return errorMessage;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error: " + ex.getMessage());
            this.errorMessage = "2";
            try {
                conn.rollback();
            } catch (SQLException ex2) {
                ex2.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex2);
            }
            return errorMessage;
        } finally{
            if(stmnt!=null){   
                try {
                    stmnt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(pstmt!=null){
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Table created!");
        return "Ok";
    }

    @Override
    public void preprocessing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String readDataset(String[] columnTypes, boolean[] checkColumns) throws LimitException, DateParseException {
        /// Save data to Database 
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        SaveClmnsAndTypeOfVar(columnTypes,checkColumns);
        return save(checkColumns);
    }
    
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues,boolean printColumnNames) {
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
        
        
            
            
        try (PrintWriter writer = new PrintWriter( new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)))) {
                
                boolean FLAG = false;
                
                if(printColumnNames){
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
                }
                
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
                        if (!value.equals("(null)")){
                            writer.print(value);
                        }
                        
                        if(column != temp[column].length-1){
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("done Disk");
    }

    @Override
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
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
        
        
            
            
        try (PrintWriter writer = new PrintWriter( new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)))) {
                
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
                        if (!value.equals("(null)")){
                            writer.print(value);
                        }
                        
                        if(column != temp[column].length-1){
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(AnonymizedDatasetPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("done Disk");
    }

    @Override
    public Map<Integer, String> getColNamesPosition() {
        return colNamesPosition;
    }

    @Override
    public DictionaryString getDictionary() {
        return this.dictionary;
    }
    
    

    @Override
    public int getColumnByName(String column) {
        for(Integer i : this.colNamesPosition.keySet()){
            if(this.colNamesPosition.get(i).equals(column)){
                return i;
            }
        }
        return -1;
    }

    @Override
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
        boolean FLAG = true;
        String [] newFormatDate = null;
        boolean removedColumn = false;
        
        try {
            fstream = new FileInputStream(inputFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));
            int counter = 0 ;
            
            while ((strLine = br.readLine()) != null)   {
                
                //save column names
                if (FLAG == true){
                    colNames = strLine.split(delimeter,-1);
                    for ( int i = 0 ; i < colNames.length ; i ++){
                        if ( checkColumns[i] == true){
                            colNamesType.put(counter,null);
                            colNamesPosition.put(counter,colNames[i].replaceAll(" ", "_"));
                            counter++;
                        }
                    }
                    
                    if(counter != columnNames.length){
                        newFormatDate = new String[counter];
                        removedColumn=true;
                    }
                    
                    FLAG = false;
                }
            }
            
            counter = 0 ;
            //save column types
            for ( int i = 0 ; i < columnTypes.length ; i ++ ){
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
                    }
                    else{
                        colNamesType.put(counter, "string");
                    }
                    
                    counter++;
                }
            }
            
            this.columnNames =  colNamesPosition.values().toArray(new String[this.colNamesPosition.size()]);
            if(counter!=columnTypes.length){
                this.formatsDate = newFormatDate;
            }
            sizeOfCol = columnNames.length;
            in.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }

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
            br = new BufferedReader(new InputStreamReader(in));
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
        return this.smallDataSet;
    }
    
    public Pair<Double,Double> getMinMax(int column, String table){
        String columnName = this.getColumnByPosition(column);
        String sqlMinMax = "SELECT MIN("+columnName+"), MAX("+columnName+") FROM "+table;
        Statement stm = null;
        ResultSet rs = null;
        Pair<Double,Double> result = null;
        try{
            stm = this.conn.createStatement();
            rs = stm.executeQuery(sqlMinMax);
            result = new Pair(rs.getDouble(1),rs.getDouble(2));
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error getting min max: "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
            
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
        
    }
    
    public List<Double> checkRange(Double max, Double min, int col){
        String sqlQuery = "SELECT DISTINCT "+this.columnNames[col]+" FROM dataset WHERE "+this.columnNames[col]+" NOT BETWEEN "+min+" AND "+max;
        Statement stm = null;
        ResultSet rs = null;
        List<Double> missingValues = new ArrayList<Double>();
        try{ 
            stm = this.conn.createStatement();
            rs = stm.executeQuery(sqlQuery);
            while(rs.next()){
               System.out.println("missing "+rs.getString(1));
               missingValues.add(rs.getDouble(1));
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: check range hierarchy "+e.getMessage());
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return missingValues;
    }
    
    public List<Double> checkValues(Set<Double> vs, int col){
        Statement stm = null;
        String sqlQuery = "SELECT DISTINCT "+this.columnNames[col]+" FROM dataset WHERE "+this.columnNames[col]+" NOT IN "+vs.toString().replace("[", "(").replace("]", ")");
        ResultSet rs = null;
        List<Double> missingValues = new ArrayList();
        System.out.println("SQL CHECK "+sqlQuery);
        try{
           stm = this.conn.createStatement();
           rs = stm.executeQuery(sqlQuery);
           while(rs.next()){
               System.out.println("missing "+rs.getString(1));
               missingValues.add(rs.getDouble(1));
           }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: check values hierarchy "+e.getMessage());
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return missingValues;
    }
    
    public Double[][] getSpecificDataset(int start,int max,boolean withIdenticals){
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Double[][] data = null;
        String sqlQuery;
        if(withIdenticals){
            sqlQuery = "SELECT * FROM dataset WHERE id >= ? AND id <= ? ";
        }
        else{
            sqlQuery = "SELECT * FROM dataset WHERE id >= ? AND id <= ? and id NOT in (SELECT id_ch FROM checked_records)";
        }
         
        try{
            this.conn.setAutoCommit(false);
            pstmt  = this.conn.prepareStatement(sqlQuery,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchSize(50);
            System.out.println("SQecidic data "+(start+1)+" "+max);
            pstmt.setInt(1, start+1);
            pstmt.setInt(2, max);
            rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
//            rs.last();
            data = new Double[max-start][metaData.getColumnCount()];
//            rs.beforeFirst();
            int counterRow = 0;
            while(rs.next()){
//                Double[] record = new Double[metaData.getColumnCount()];
                data[counterRow][0] = rs.getDouble(1);
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    data[counterRow][entry.getKey()+1] = rs.getDouble(this.columnNames[entry.getKey()]);
                }
//                data.add(record);
                counterRow++;
            }
            
        }catch (OutOfMemoryError e) {
            System.err.println("Error outofMemory free"+Runtime.getRuntime().freeMemory()+" Curent "+Runtime.getRuntime().totalMemory()+" max "+Runtime.getRuntime().maxMemory());
            e.printStackTrace();
        }
        
        catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: get disk dataset "+e.getMessage());
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(pstmt!=null){
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return data;
    }
    
    public double[][] getDataset(int start, int max){
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double[][] data = null;
        String sqlQuery = "SELECT * FROM dataset WHERE id >= ? AND id <= ?";
        try{
            pstmt  = this.conn.prepareStatement(sqlQuery);
            
            pstmt.setInt(1, start+1);
            pstmt.setInt(2, max);
            rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            data = new double[max-start][metaData.getColumnCount()-1];
            int counterRow = 0;
            while(rs.next()){
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    data[counterRow][entry.getKey()] = rs.getDouble(this.columnNames[entry.getKey()]);
                }
                counterRow++;
            }
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: get disk dataset "+e.getMessage());
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(pstmt!=null){
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return data;
    }
    
    public double[][][] getOriginalAnonSet(int start, int max){
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double[][][] data = null;
        try{
            
            String sqlQuery = "SELECT * FROM dataset WHERE id >= ? AND id <= ?";
            
            pstmt  = this.conn.prepareStatement(sqlQuery);

            pstmt.setInt(1, start+1);
            pstmt.setInt(2, max);
            rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            data = new double[2][max-start][metaData.getColumnCount()-1];
            int counterRow = 0;
            while(rs.next()){
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    data[0][counterRow][entry.getKey()] = rs.getDouble(this.columnNames[entry.getKey()]);
                }
                counterRow++;
            }

            rs.close();

            sqlQuery = "SELECT * FROM anonymized_dataset WHERE id_an >= ? AND id_an <= ?";
            pstmt  = this.conn.prepareStatement(sqlQuery);
            pstmt.setInt(1, start+1);
            pstmt.setInt(2, max);
            rs = pstmt.executeQuery();
            counterRow = 0;
            while(rs.next()){
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    data[1][counterRow][entry.getKey()] = rs.getDouble(this.columnNames[entry.getKey()]);
                }
                counterRow++;
            }
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: get small dataset "+e.getMessage());
            return null;
        } finally{
            if(rs !=null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(pstmt!=null){
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
           
           
        }
        return data;
    }

    @Override
    public ArrayList<LinkedHashMap> getPage(int start, int length) {
        data = new ArrayList<LinkedHashMap>();
        PreparedStatement pstmt = null;
        int counter = 0 ;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        if ( start + length <= sizeOfRows ){
            max = start + length;
        }
        else{
            max = sizeOfRows;
        }
        
        try{
            String sqlQuery = "SELECT * FROM dataset WHERE id >= ? AND id <= ?";
            pstmt  = this.conn.prepareStatement(sqlQuery);
            
            pstmt.setInt(1, start+1);
            pstmt.setInt(2, max);
            ResultSet rs  = pstmt.executeQuery();
            System.out.println("Query executed "+rs.toString());
            
            while(rs.next()){
                linkedHashTemp = new LinkedHashMap<>();
                for (Entry<Integer,String> entry : this.colNamesType.entrySet()){
                    if (entry.getValue().equals("double")){
                        Double value = rs.getDouble(this.columnNames[entry.getKey()]);
                        if(value.equals(2147483646.0)){
                            linkedHashTemp.put(columnNames[entry.getKey()],"");
                        }
                        else{
                            linkedHashTemp.put(columnNames[entry.getKey()], value);
                        }
                    }
                    else if(entry.getValue().equals("int")){
                        Integer value = rs.getInt(this.columnNames[entry.getKey()]);
                        if(value.equals(2147483646)){
                            linkedHashTemp.put(columnNames[entry.getKey()],"");
                        }
                        else{
                            linkedHashTemp.put(columnNames[entry.getKey()], value);
                        }
                    }
                    else if(entry.getValue().equals("date")){
                        Double value = rs.getDouble(this.columnNames[entry.getKey()]);
                        String strValue;
                        Date date = new Date(value.longValue());
                        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                        strValue = df2.format(date);
                        linkedHashTemp.put(columnNames[entry.getKey()], strValue);
                    }
                    else{
                        Double value = rs.getDouble(this.columnNames[entry.getKey()]);
                        String strValue = this.dictionary.getIdToString(value.intValue());
                        if(strValue == null){
                            strValue = this.dictHier.getIdToString(value.intValue());
                        }
                        
                        if (strValue.equals("NaN")){
                            linkedHashTemp.put(columnNames[entry.getKey()],"");
                        }
                        else{
                            linkedHashTemp.put(columnNames[entry.getKey()], strValue);
                        }
                        
                    }
                }
                data.add(linkedHashTemp);
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if(pstmt!=null){
                    pstmt.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        recordsTotal = sizeOfRows;
        recordsFiltered = sizeOfRows;
        
        return data;
    }

    @JsonView(View.SmallDataSet.class)
    @Override
    public String[][] getTypesOfVariables(String[][] smallDataSet) {
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

    @Override
    public int getRecordsTotal() {
        return recordsTotal;
    }
    
    public static Set<Integer> getRandomNumberBetweenRange(int size, int max){

//        int x = (int) ((Math.random()*((max-min)+1))+min);
//
//        return x;
 
        Set<Integer> set = new HashSet<Integer>(size);

        while(set.size()< size) {
            while (set.add((int)((Math.random() * ( max - 1 ))) + 1) != true)
                ;
        }
        assert set.size() == size;
        
        return set;
    }
    
    public Double[] getNextRecord(int recordId){
        Double[] record = null;
        if(recordId > this.recordsTotal){
            return record;
        }
        
        String sqlSelect = "SELECT * FROM dataset WHERE id="+recordId;
        Statement stm = null;
        
        try{
            stm = this.conn.createStatement();
            ResultSet result = stm.executeQuery(sqlSelect);
            ResultSetMetaData metaData = result.getMetaData();
            int columnNum = metaData.getColumnCount();
            record = new Double[this.sizeOfCol];
            
            for(int i=2; i<=columnNum; i++){
                record[i-2] = result.getDouble(i);
            }
            
        
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: getNextRecord "+e.getMessage());
        }finally{
            try {
                if(stm!=null){
                    stm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        return record;
    }
    
//    public List<Pair<Double[],List<Integer>>> getRandomRecords(int num, Set<Integer> quasiIdentifiers) {
//        int numCounter = num;
//        List<Integer> recordsIds = null;
//        Set<Integer> recordsIdsTemp = null;
//        List<Integer> records = new ArrayList<Integer>();
//        Set<Integer> allRecordsIds = new HashSet<Integer>();
//        int counter=1;
//        Statement stmnt = null, stmnt2 = null;
//        String sqlSelect = "SELECT * FROM dataset WHERE id IN ";
//        ResultSet result = null;
//        ResultSet resultIdentical = null;
//        String orderBy = " ORDER BY ";
//        List<Pair<Double[],List<Integer>>> clusterRecsIds = new ArrayList();
//        Map<String,Set<Double>> identicalMap = new HashMap();
//        
//        this.createChekedTable();
//        
//        try{
//            for(Integer col : quasiIdentifiers){
//                orderBy += this.colNamesPosition.get(col)+", ";
//                identicalMap.put(this.colNamesPosition.get(col), new HashSet());
//            }
//            
//            if(orderBy.endsWith(", ")){
//                orderBy = this.replaceLast(orderBy,", ","");
//            }
//            recordsIdsTemp = this.getRandomNumberBetweenRange(numCounter,this.recordsTotal);
//            
////            while(numCounter != 0){
////                int recordId = this.getRandomNumberBetweenRange(1,this.recordsTotal);
////                if(!recordsIds.contains(recordId)){
////                    recordsIds.add(recordId);
////                    numCounter--;
////                }
////            }
//            
//            System.out.println("End random number "+recordsIdsTemp.size());
//            this.conn.setAutoCommit(false);
//            stmnt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY); 
//            stmnt2 = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY); 
//            stmnt2.setFetchSize(50);
////            recordsIds = recordsIdsTemp.size() >= 1000 ?  recordsIdsTemp.stream().limit(999).collect(Collectors.toList()) : new ArrayList(recordsIdsTemp);
////            int start=0, end=recordsIds.size() >= 1000 ? 999 :fillAnonymizedRecords recordsIds.size();
////            while(!recordsIdsTemp.isEmpty()){
//                
////                for(int i=0; i< end; i++){fillAnonymizedRecords
////                    Integer id = recordsIds.get(i);
////                    recordsIdsTemp.remove(id);
////                    if(i == end-1){
////                        sqlSelect += id;
////                    }
////                    else{
////                        sqlSelect += id +" OR id=";
////                    }
////                    
////                }
////                System.out.println("End of string query "+sqlSelect+Arrays.toString(recordsIds.toArray()).replace("[", "(").replace("]", ")"));
//                stmnt.setFetchSize(50);
//
//                result = stmnt.executeQuery(sqlSelect+recordsIdsTemp.toString().replace("[", "(").replace("]", ")"));
////                recordsIds.clear();
////                recordsIdsTemp.clear();
//                
//                String sqlSelectIdentical = "SELECT * FROM dataset WHERE ";
//                System.out.println("SQL "+counter);
//                
//                
//                ResultSetMetaData metaData = result.getMetaData();
//                while(result.next()){
//                    for(int i=2; i<=metaData.getColumnCount(); i++){
//                        if(quasiIdentifiers.contains(i-2)){
//                            identicalMap.get(metaData.getColumnName(i)).add(result.getDouble(i));
//                        }
//                    }
//                }
//                
//                for(Entry<String,Set<Double>> identicalValues : identicalMap.entrySet()){
//                    sqlSelectIdentical += identicalValues.getKey()+" IN "+identicalValues.getValue().toString().replace("[", "(").replace("]", ")")+" AND ";
//                }
//                
//                if(sqlSelectIdentical.endsWith("AND ")){
//                    sqlSelectIdentical = this.replaceLast(sqlSelectIdentical,"AND ","");
//                }
////                while(result.next()){
////                    ResultSetMetaData metaData = result.getMetaData();
//////                    int idInitialTemp = result.getInt(1);
//////                    recordValues[0] = result.getDouble(1);
////                    for(int i=2; i<=metaData.getColumnCount(); i++){
//////                        recordValues[i-1] = result.getDouble(i);
////                        if(quasiIdentifiers.contains(i-2)){
////                            if(i==metaData.getColumnCount()){
////                                sqlSelectIdentical += metaData.getColumnName(i)+"="+result.getString(i);
////                            }
////                            else{
////                                sqlSelectIdentical += metaData.getColumnName(i)+"="+result.getString(i)+" AND ";
////                            }
////                        }
////                    }
////
////                    if(sqlSelectIdentical.endsWith("AND ")){
////                        sqlSelectIdentical = this.replaceLast(sqlSelectIdentical,"AND ",")");
////                    }
////                    else{
////                       sqlSelectIdentical += ")" ;
////                    }
////                    sqlSelectIdentical += " OR (";
////                }
////    //                System.out.println("Sql select: "+sqlSelectIdentical+"jgfdg");
////                if(sqlSelectIdentical.endsWith(" OR (")){
//////                    sqlSelectIdentical = sqlSelectIdentical.substring(0, sqlSelectIdentical.lastIndexOf(" OR ("));
////                    sqlSelectIdentical = this.replaceLast(sqlSelectIdentical,"OR \\("," ");
////                }
//                
//                stmnt2.setFetchSize(50);
//                System.out.println("Identical "+sqlSelectIdentical+orderBy);
//                resultIdentical = stmnt2.executeQuery(sqlSelectIdentical+orderBy);
//                
//                ResultSetMetaData metaDataIdentical = resultIdentical.getMetaData();
//                Double[] previousRec = null;
//                Double[] recordValues = new Double[this.sizeOfCol+1];
//                
//                if(resultIdentical.next()){
//                    do{
//                        int idTemp = resultIdentical.getInt(1);
//                        boolean samePrevious=true;
//                        recordsIdsTemp.remove(idTemp);
//                        recordValues[0] = resultIdentical.getDouble(1);
//                        for(int i=2; i<=metaDataIdentical.getColumnCount(); i++){
//                            recordValues[i-1] = resultIdentical.getDouble(i);
//                            if(quasiIdentifiers.contains(i-2)){
//                                if(previousRec!=null && !previousRec[i-1].equals(recordValues[i-1])){
//                                    samePrevious=false;
//                                }
//                            }
//                            
//                            
//                        }
//                        if(samePrevious){
//                            records.add(idTemp);
//                        }
//                        else{
//                            clusterRecsIds.add(new Pair (previousRec,records));
//                            records = new ArrayList<Integer>();
//                            records.add(idTemp);
//                        }
//                        
//                        allRecordsIds.add(idTemp);
//                        if(allRecordsIds.size()==8000){
//                            this.insertChekedTable(allRecordsIds);
//                            allRecordsIds.clear();
//                        }
//                        previousRec = recordValues; 
//                        recordValues = new Double[this.sizeOfCol+1];
////                        System.out.println("Previous "+Arrays.toString(previousRec)+" now "+Arrays.toString(recordValues));
//                    }while(resultIdentical.next());
//                }
//                clusterRecsIds.add(new Pair (previousRec,records));
//                counter++;
////                recordsIds = new ArrayList(recordsIdsTemp);
////                end=recordsIds.size() >= 1000 ? 999 : recordsIds.size();
////                recordsIds = recordsIdsTemp.size() >= 1000 ?  recordsIdsTemp.stream().limit(999).collect(Collectors.toList()) : new ArrayList(recordsIdsTemp);
////            }
//        }catch(Exception e){
//            e.printStackTrace();
//            System.out.println("Error: "+e.getMessage());
//        }finally {
//            if(stmnt!=null){
//                try {
//                    stmnt.close();
//                } catch (SQLException ex) {
//                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            
//            if(stmnt2!=null){
//                try {
//                    stmnt2.close();
//                } catch (SQLException ex) {
//                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            
//            if(result!=null){
//                try {
//                    result.close();
//                } catch (SQLException ex) {
//                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            
//            if(resultIdentical!=null){
//                try {
//                    resultIdentical.close();
//                } catch (SQLException ex) {
//                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            
//            try {
//                this.conn.setAutoCommit(true);
//            } catch (SQLException ex) {
//                ex.printStackTrace();
//                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        
//        this.insertChekedTable(allRecordsIds);
//        allRecordsIds.clear();
//        return clusterRecsIds;
//    }
    
    
    /*SELECT
    y.id,y.name,y.email
    FROM @YourTable y
        INNER JOIN (SELECT
                        name,email, COUNT(*) AS CountOf
                        FROM @YourTable
                        GROUP BY name,email
                        HAVING COUNT(*)>1
                    ) dt ON y.name=dt.name AND y.email=dt.email // AND y.id in (1,2,3,....) 
    example query
    
    */
    public void removeAnonymizedRec(int k,Set<Integer> quasiId){
        Set<Integer> recordsIds = new HashSet();
        String selectSql = "SELECT id FROM dataset d INNER JOIN";
        String identifiersSql = "SELECT ";
        String sqlColumnsNames = "";
        String onInnerSql="";
        
        
        for(Integer col : quasiId){
            sqlColumnsNames += this.columnNames[col]+",";
            onInnerSql += "d."+this.columnNames[col]+"="+"dt."+this.columnNames[col]+" AND ";
        }
        
        sqlColumnsNames = replaceLast(sqlColumnsNames,",","");
        onInnerSql = replaceLast(onInnerSql," AND ","");
        identifiersSql += sqlColumnsNames+" FROM dataset GROUP BY "+sqlColumnsNames+" HAVING COUNT(*)>="+k;
        selectSql += "("+identifiersSql+") dt ON "+onInnerSql;
    }
    
    
    /*build function for random 
        /// TODO 
        SELECT * From dataset WHERE (age) In (SELECT age From dataset GROUP by age HAVING Count(*)>=3)
        SELECT * FROM table ORDER BY RANDOM() LIMIT 1;
    
    
    SELECT *
  FROM mytable
 WHERE (group_id, group_type) IN (
                                  VALUES ('1234-567', 2), 
                                         ('4321-765', 3), 
                                         ('1111-222', 5)
                                 );*/
    
    public Pair<Pair<Double[],List<Integer>>[],Integer> getRandomRecords(int num, Set<Integer> quasiIdentifiers) {
        int numCounter = num;
        List<Integer> recordsIds = null;
        Set<Integer> recordsIdsTemp = null;
        List<Integer> records = new ArrayList<Integer>();
        Set<Integer> allRecordsIds = new HashSet<Integer>();
        int counter=1;
        Statement stmnt = null;
        String sqlSelect = "SELECT * FROM dataset WHERE id IN ";
        ResultSet result = null;
        ResultSet resultIdentical = null;
        String orderBy = " ORDER BY ";
        Pair<Double[],List<Integer>>[] clusterRecsIds = new Pair[numCounter];
        int pointer = 0;
        ResultSetMetaData metaDataIdentical;
        
//        this.createChekedTable();
        
        try{
            for(Integer col : quasiIdentifiers){
                orderBy += this.colNamesPosition.get(col)+", ";
            }
            
            if(orderBy.endsWith(", ")){
                orderBy = this.replaceLast(orderBy,", ","");
            }
            recordsIdsTemp = this.getRandomNumberBetweenRange(numCounter,this.recordsTotal);
            this.conn.setAutoCommit(false);
            stmnt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY); 
//            stmnt2 = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY); 
//            stmnt2.setFetchSize(50);
            stmnt.setFetchSize(50);
            result = stmnt.executeQuery(sqlSelect+Arrays.toString(recordsIdsTemp.toArray()).replace("[", "(").replace("]", ")"));
            String sqlSelectIdentical = "SELECT * FROM dataset WHERE ";
            if(quasiIdentifiers.size()==1){
                
//                stmnt.setFetchSize(50);
//                result = stmnt.executeQuery(sqlSelect+Arrays.toString(recordsIdsTemp.toArray()).replace("[", "(").replace("]", ")"));
//                String sqlSelectIdentical = "SELECT * FROM dataset WHERE ";
                int column =quasiIdentifiers.iterator().next();
                String columnName = this.columnNames[column];
                sqlSelectIdentical += columnName +" IN ";
                String[] recordsAttr = new String[recordsIdsTemp.size()];
                System.out.println("Column name "+columnName+" column ");
                int j=0;
                while(result.next()){
                    recordsAttr[j] = result.getString(column+2);
                    j++;
                }
                
//                sqlSelectIdentical = replaceLast(sqlSelectIdentical,",", ")");
                
                System.out.println("Identical "+sqlSelectIdentical+Arrays.toString(recordsAttr).replace("[", "(").replace("]", ")")+orderBy);
                resultIdentical = stmnt.executeQuery(sqlSelectIdentical+Arrays.toString(recordsAttr).replace("[", "(").replace("]", ")")+orderBy);
//                metaDataIdentical = resultIdentical.getMetaData();
//                
//                Double[] previousRec = null;
//                Double[] recordValues = new Double[this.sizeOfCol+1];
//
//                if(resultIdentical.next()){
//                    do{
//                        int idTemp = resultIdentical.getInt(1);
//                        boolean samePrevious=true;
//                        recordValues[0] = resultIdentical.getDouble(1);
//                        for(int i=2; i<=metaDataIdentical.getColumnCount(); i++){
//                            recordValues[i-1] = resultIdentical.getDouble(i);
//                            if(quasiIdentifiers.contains(i-2)){
//                                if(previousRec!=null && !previousRec[i-1].equals(recordValues[i-1])){
//                                    samePrevious=false;
//                                }
//                            }
//
//
//                        }
//                        if(samePrevious){
//                            records.add(idTemp);
//                        }
//                        else{
//                            clusterRecsIds.add(new Pair (previousRec,records));
//                            records = new ArrayList<Integer>();
//                            records.add(idTemp);
//                        }
//
//                        allRecordsIds.add(idTemp);
//                        if(allRecordsIds.size()==8000){
//                            this.insertChekedTable(allRecordsIds);
//                            allRecordsIds.clear();
//                        }
//                        previousRec = recordValues; 
//                        recordValues = new Double[this.sizeOfCol+1];
////                        System.out.println("Previous "+Arrays.toString(previousRec)+" now "+Arrays.toString(recordValues));
//                    }while(resultIdentical.next());
//                }
//                clusterRecsIds.add(new Pair (previousRec,records));
//                counter++;
                
            }
            else{
                
                String columnNamesTuple = "(";
                String[][] recordsAttr = new String[recordsIdsTemp.size()][quasiIdentifiers.size()];
                for(Integer col : quasiIdentifiers){
                    columnNamesTuple += this.columnNames[col]+",";
                }
                
                columnNamesTuple = replaceLast(columnNamesTuple,",",")") +" IN (VALUES ";
                int line=0;
                while(result.next()){
//                    String valuesTuple = "(";
//                    for(Integer col : quasiIdentifiers){
//                        valuesTuple += result.getString(col+2)+",";
//                    }
//                    columnNamesTuple += replaceLast(valuesTuple,",",")")+",";
                    int column=0;
                    for(Integer col : quasiIdentifiers){
                        recordsAttr[line][column] = result.getString(col+2);
                        column++;
                    }
                    line++;
                }
                
//                columnNamesTuple = replaceLast(columnNamesTuple,",",")");
                
                System.out.println("Identical2 "+sqlSelectIdentical+columnNamesTuple+Arrays
                    .stream(recordsAttr)
                    .map(Arrays::toString) 
                    .collect(Collectors.joining(",")).replace("[", "(").replace("]", ")")+")"+orderBy);
//                stmnt2.setFetchSize(50);
                resultIdentical = stmnt.executeQuery(sqlSelectIdentical+columnNamesTuple+Arrays
                    .stream(recordsAttr)
                    .map(Arrays::toString) 
                    .collect(Collectors.joining(",")).replace("[", "(").replace("]", ")")+")"+orderBy);
                
                
                
                
                /*System.out.println("End random number "+recordsIdsTemp.size());
                recordsIds = recordsIdsTemp.size() >= 1000 ?  recordsIdsTemp.stream().limit(990).collect(Collectors.toList()) : new ArrayList(recordsIdsTemp);
   
                while(!recordsIdsTemp.isEmpty()){

    
                    stmnt.setFetchSize(50);

                    result = stmnt.executeQuery(sqlSelect+Arrays.toString(recordsIds.toArray()).replace("[", "(").replace("]", ")"));

                    String sqlSelectIdentical = "SELECT * FROM dataset WHERE (";
                    System.out.println("SQL "+counter);
                    ResultSetMetaData metaData = result.getMetaData();
                    while(result.next()){
                        for(int i=2; i<=metaData.getColumnCount(); i++){
                            if(quasiIdentifiers.contains(i-2)){
                                if(i==metaData.getColumnCount()){
                                    sqlSelectIdentical += metaData.getColumnName(i)+"="+result.getString(i);
                                }
                                else{
                                    sqlSelectIdentical += metaData.getColumnName(i)+"="+result.getString(i)+" AND ";
                                }
                            }
                        }

                        if(sqlSelectIdentical.endsWith("AND ")){
                            sqlSelectIdentical = this.replaceLast(sqlSelectIdentical,"AND ",")");
                        }
                        else{
                           sqlSelectIdentical += ")" ;
                        }
                        sqlSelectIdentical += " OR (";
                    };
                    if(sqlSelectIdentical.endsWith(" OR (")){
                        sqlSelectIdentical = this.replaceLast(sqlSelectIdentical,"OR \\("," ");
                    }

                    stmnt2.setFetchSize(50);
                    System.out.println("Identical "+sqlSelectIdentical+orderBy);
                    resultIdentical = stmnt2.executeQuery(sqlSelectIdentical+orderBy);

                    ResultSetMetaData metaDataIdentical = resultIdentical.getMetaData();
                    Double[] previousRec = null;
                    Double[] recordValues = new Double[this.sizeOfCol+1];

                    if(resultIdentical.next()){
                        do{
                            int idTemp = resultIdentical.getInt(1);
                            boolean samePrevious=true;
                            recordsIdsTemp.remove(idTemp);
                            recordValues[0] = resultIdentical.getDouble(1);
                            for(int i=2; i<=metaDataIdentical.getColumnCount(); i++){
                                recordValues[i-1] = resultIdentical.getDouble(i);
                                if(quasiIdentifiers.contains(i-2)){
                                    if(previousRec!=null && !previousRec[i-1].equals(recordValues[i-1])){
                                        samePrevious=false;
                                    }
                                }


                            }
                            if(samePrevious){
                                records.add(idTemp);
                            }
                            else{
                                clusterRecsIds.add(new Pair (previousRec,records));
                                records = new ArrayList<Integer>();
                                records.add(idTemp);
                            }

                            allRecordsIds.add(idTemp);
                            if(allRecordsIds.size()==8000){
                                this.insertChekedTable(allRecordsIds);
                                allRecordsIds.clear();
                            }
                            previousRec = recordValues; 
                            recordValues = new Double[this.sizeOfCol+1];
                        }while(resultIdentical.next());
                    }
                    clusterRecsIds.add(new Pair (previousRec,records));
                    counter++;
                    recordsIds = recordsIdsTemp.size() >= 1000 ?  recordsIdsTemp.stream().limit(990).collect(Collectors.toList()) : new ArrayList(recordsIdsTemp);
                }*/ 
            }
            
            metaDataIdentical = resultIdentical.getMetaData();
                
            Double[] previousRec = null;
            Double[] recordValues = new Double[this.sizeOfCol+1];

            if(resultIdentical.next()){
                do{
                    int idTemp = resultIdentical.getInt(1);
                    boolean samePrevious=true;
                    recordValues[0] = resultIdentical.getDouble(1);
                    for(int i=2; i<=metaDataIdentical.getColumnCount(); i++){
                        recordValues[i-1] = resultIdentical.getDouble(i);
                        if(quasiIdentifiers.contains(i-2)){
                            if(previousRec!=null && !previousRec[i-1].equals(recordValues[i-1])){
                                samePrevious=false;
                            }
                        }


                    }
                    if(samePrevious){
                        records.add(idTemp);
                    }
                    else{
                        clusterRecsIds[pointer++] = new Pair (previousRec,records);
                        records = new ArrayList<Integer>();
                        records.add(idTemp);
                    }

                    allRecordsIds.add(idTemp);
                    if(allRecordsIds.size()==8000){
                        this.insertChekedTable(allRecordsIds);
                        allRecordsIds.clear();
                    }
                    previousRec = recordValues; 
                    recordValues = new Double[this.sizeOfCol+1];
//                        System.out.println("Previous "+Arrays.toString(previousRec)+" now "+Arrays.toString(recordValues));
                }while(resultIdentical.next());
            }
            clusterRecsIds[pointer++]=new Pair (previousRec,records);
            counter++;
            
            
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error: "+e.getMessage());
        }finally {
            if(stmnt!=null){
                try {
                    stmnt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
//            if(stmnt2!=null){
//                try {
//                    stmnt2.close();
//                } catch (SQLException ex) {
//                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            
            if(result!=null){
                try {
                    result.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(resultIdentical!=null){
                try {
                    resultIdentical.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        this.insertChekedTable(allRecordsIds);
        allRecordsIds.clear();
        return new Pair(clusterRecsIds,pointer);
    }
    
    public void insertChekedTable(Set<Integer> recordsIds){
        String sqlInsert = "INSERT INTO checked_records (id_ch) VALUES ";
        Statement stm = null;
        try{
            if(!recordsIds.isEmpty()){
                stm = this.conn.createStatement();


//                System.out.println("Records ids "+Arrays.toString(recordsIds.toArray()).replace("[", "(").replace("]", ")").replaceAll(",", "),("));
                stm.execute(sqlInsert+Arrays.toString(recordsIds.toArray()).replace("[", "(").replace("]", ")").replaceAll(",", "),("));
            }
        }catch(Exception e){
            System.err.println("Error: create insert Table "+e.getMessage());
            e.printStackTrace();
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void createChekedTable(){
        String sqlCreateTable = "CREATE TABLE checked_records (id_ch integer PRIMARY KEY)";
        Statement stm = null;
        try{
            stm = this.conn.createStatement();
            stm.executeUpdate("drop table if exists checked_records;");
            
            stm.execute(sqlCreateTable);
        }catch(Exception e){
            System.err.println("Error: create checked Table "+e.getMessage());
            e.printStackTrace();
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void sortDatabase(Integer[] columnsQuasi){
//        try{
//            String sqlCreateTable = "CREATE TABLE ordered_dataset (\n"
//                + " id integer PRIMARY KEY,\n";
//            String sqlOrder = "SELECT * FROM dataset ORDER BY ";
//            String insertTable = "INSERT INTO ordered_dataset (id,";
//            String dataType;
//            String StrQuestionMark = "";
//            int counter=0;
//            
//            for(Entry<Integer,String> entry : this.colNamesPosition.entrySet()){
//                counter++;
//                dataType = this.colNamesType.get(entry.getKey());
//                sqlCreateTable += " "+entry.getValue();
//                
//                if(dataType.equals("int")){
//                    dataType = " integer";
//                }
//                else if(dataType.equals("double")){
//                    dataType = " real";
//                }
//                else if(dataType.equals("date") || dataType.equals("string")){
//                    dataType = " real";
//                }
//                
//                if(counter == this.colNamesPosition.size()){
//                    insertTable += ""+entry.getValue()+") VALUES(?,";
//                    sqlCreateTable += dataType+"\n);";
//                    StrQuestionMark += "?)";
//                    
//                }
//                else{
//                    insertTable += ""+entry.getValue()+",";
//                    sqlCreateTable += dataType+",\n";
//                    StrQuestionMark += "?,";
//                }
//                
//            }
//            for(int j=0; j<columnsQuasi.length; j++){
//                if(j==columnsQuasi.length-1){
//                    sqlOrder += ""+this.colNamesPosition.get(columnsQuasi[j]);
//                }
//                else{
//                    sqlOrder += ""+this.colNamesPosition.get(columnsQuasi[j])+",";
//                }
//            }
//            
//            String sql = insertTable+" "+sqlOrder;
//            
//            Statement stmnt = conn.createStatement();
//            stmnt.execute(sqlCreateTable);
//            System.out.println("Ordered Table created!");
//            System.out.println("insert "+insertTable+StrQuestionMark);
//            ResultSet resultSet = stmnt.executeQuery(sqlOrder);
//            ResultSetMetaData rsmd = resultSet.getMetaData();
//            int columnsNumber = rsmd.getColumnCount();
//            while (resultSet.next()) {
//                PreparedStatement pstmt = conn.prepareStatement(insertTable+StrQuestionMark);
//                for (int i = 1; i <= columnsNumber; i++) {
////                    if (i > 1) System.out.print(",  ");
////                    String columnValue = resultSet.getString(i);
////                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
////                    System.out.println("Type"+rsmd.getColumnTypeName(i));
//                    if(rsmd.getColumnTypeName(i).toLowerCase().equals("integer")){
//                        pstmt.setInt(i, resultSet.getInt(i));
//                    }
//                    else {
//                        pstmt.setDouble(i, resultSet.getDouble(i));
//                    }
//                }
//                System.out.println("");
//                pstmt.executeUpdate();
//            }
//        }
//        catch(Exception e){
//            e.printStackTrace();
//            System.err.println("Error: "+e.getMessage());
//        }
        
    }

    @Override
    public Map<Integer, String> getColNamesType() {
        return this.colNamesType;
    }

    @Override
    public String getInputFile() {
        String delimeter = "/";
        String[] temp = inputFile.split(delimeter,-1);
        return temp[temp.length-1];
    }
    
    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
    
    
    
    public void createAnonymizedTable(){
        String sqlCreateTable = "CREATE TABLE anonymized_dataset (id_an integer PRIMARY KEY,";
        Statement stm = null;
        
        
        try{
            stm = this.conn.createStatement();
            stm.executeUpdate("DROP TABLE IF EXISTS anonymized_dataset;");
            int counter = 0;
            for(Entry<Integer,String> entry : this.colNamesPosition.entrySet()){
                counter++;
                sqlCreateTable += " "+entry.getValue();
                String dataType = this.colNamesType.get(entry.getKey());
                if(dataType.equals("int")){
                    dataType = " integer";
                }
                else if(dataType.equals("double")){
                    dataType = " real";
                }
                else if(dataType.equals("date") || dataType.equals("string")){
                    dataType = " real";
                }
                
                if(counter == this.colNamesPosition.size()){
                   sqlCreateTable += dataType+");";
                }
                else{
                    sqlCreateTable += dataType+", ";
                }
                
            }
            
            stm.execute(sqlCreateTable);
            
            
            
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" create anonymized table");
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    
    
    public void cloneOriginalToAnonymize(){
        this.createAnonymizedTable();
        String sqlClone = "INSERT INTO anonymized_dataset SELECT * FROM dataset";
        Statement stm = null;
        try{
            stm = this.conn.createStatement();
            stm.execute(sqlClone);
        }catch(Exception e){
            System.err.println("Error: clone original to anonymize "+e.getMessage());
            e.printStackTrace();
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void executeAnonymizedBatch(){
        Statement stm = null;
        String insertSql = "INSERT INTO anonymized_dataset SELECT * FROM dataset WHERE id IN ";
        try{
//            System.out.println("Intert Anonymize "+insertSql+this.anonymizedRecords.toString().replace("[", "(").replace("]", ")"));
            stm = this.conn.createStatement();
            stm.execute(insertSql+this.anonymizedRecords.toString().replace("[", "(").replace("]", ")"));
//            
            this.anonymizedRecords.clear();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("fill Anonymized records via ids "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
    }
    
    public void fillAnonymizedRecords(List<Integer> records){
        this.anonymizedRecords.addAll(records);
    }
    
    public void createAnonymizedQuery(){
        String insertSql = "INSERT INTO anonymized_dataset(";
        String valuesStr ="";
        for(int j=0; j<this.sizeOfCol; j++){
            if(j==this.sizeOfCol-1){
                insertSql += this.colNamesPosition.get(j)+") VALUES(";
                valuesStr += "?)";
            }
            else{
                if(j==0){
                    insertSql += "id_an,"+this.colNamesPosition.get(j)+",";
                    valuesStr += "?,?,";
                }
                else{
                   insertSql += this.colNamesPosition.get(j)+","; 
                   valuesStr += "?,";
                }
            }
        }
        
        this.anonymizeQuery = insertSql+valuesStr;
    }
    
    public void executeAnonymizedClusterBatch(Double[][] records){
        String sqlUpdate = "";
        PreparedStatement pstm = null;
        Set<Double> ids = new HashSet();
        Double[] errMesg = null;
        System.out.println("Execute Anonymized batch");
        try{
//            String insertSql = "INSERT INTO anonymized_dataset(";
//            String valuesStr ="";
//            for(int j=0; j<this.sizeOfCol; j++){
//                if(j==this.sizeOfCol-1){
//                    insertSql += this.colNamesPosition.get(j)+") VALUES(";
//                    valuesStr += "?)";
//                }
//                else{
//                    if(j==0){
//                        insertSql += "id_an,"+this.colNamesPosition.get(j)+",";
//                        valuesStr += "?,?,";
//                    }
//                    else{
//                       insertSql += this.colNamesPosition.get(j)+","; 
//                       valuesStr += "?,";
//                    }
//                }
//            }
            this.conn.setAutoCommit(false);
            pstm = conn.prepareStatement(this.anonymizeQuery);
            for(int i=0; i<records.length; i++){
//                    System.out.println("Record "+Arrays.toString(records[i]));
                for(int j=0; j<this.sizeOfCol+1; j++){
                    errMesg = records[i];
//                    System.out.println("recRow "+i+" col "+j);
//                    System.out.println("rec "+Arrays.toString(records[i]));
                    if(j==0){
                        pstm.setInt(j+1, records[i][j].intValue());
//                            if(ids.contains(records[i][j])){
//                                System.out.println("Problem with record "+Arrays.toString(records[i]));
//                            }
//                            else{
//                                ids.add(records[i][j]);
//                            }
//                            ids.add(records[i][j]);


                    }
                    else if(this.colNamesType.get(j-1).equals("int")){
                        pstm.setInt(j+1, records[i][j].intValue());
                    }
                    else {
                        pstm.setDouble(j+1, records[i][j]);
                    }
                }
                pstm.executeUpdate();
            }
            conn.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" fillAnonymizedRecords"+Arrays.toString(errMesg));
        }finally{
            if(pstm!=null){
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void initialiseStatement(){
        try {
            this.conn.setAutoCommit(false);
            this.anonymizeStatement = conn.prepareStatement(this.anonymizeQuery);
        } catch (SQLException ex) {
            System.err.println("Error init prepared statement "+ex.getMessage());
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeConnection(){
        try {
            this.conn.commit();
        } catch (SQLException ex) {
            System.err.println("Error close connection commit "+ex.getMessage());
            try {
                this.conn.rollback();
            } catch (SQLException ex1) {
                System.err.println("Error close connection rollback "+ex1.getMessage());
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(this.anonymizeStatement!=null){
            try {
                this.anonymizeStatement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            this.conn.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void copyDB(){
        File source = new File(this.urlDatabase.replace("jdbc:sqlite:", ""));
        File dest = new File(this.urlDatabase.replace("jdbc:sqlite:", "").replace(".db", "2.db"));
        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException ex) {
            Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.urlDbTemp = "jdbc:sqlite:"+dest.getAbsolutePath();
        
    }
    
    public void deleteDB(){
        File source = new File(this.urlDbTemp.replace("jdbc:sqlite:", ""));
        source.delete();
    }
    
    public String getUrlTempDb(){
        return this.urlDbTemp;
    }
    
    public void executeAnonymizedClusterBatch(Double[][][] clusterRecords){
        String sqlUpdate = "";
//        PreparedStatement pstm = null;
        Set<Double> ids = new HashSet();
        Double[] errMesg = null;
        System.out.println("Execute Anonymized batch");
        try{
//            String insertSql = "INSERT INTO anonymized_dataset(";
//            String valuesStr ="";
//            for(int j=0; j<this.sizeOfCol; j++){
//                if(j==this.sizeOfCol-1){
//                    insertSql += this.colNamesPosition.get(j)+") VALUES(";
//                    valuesStr += "?)";
//                }
//                else{
//                    if(j==0){
//                        insertSql += "id_an,"+this.colNamesPosition.get(j)+",";
//                         valuesStr += "?,?,";
//                    }
//                    else{
//                       insertSql += this.colNamesPosition.get(j)+","; 
//                       valuesStr += "?,";
//                    }
//                }
//            }
//            this.conn.setAutoCommit(false);
//            pstm = conn.prepareStatement(this.anonymizeQuery);
//            System.out.println("SQl : "+insertSql+valuesStr);
            
            for(int l=0; l<clusterRecords.length; l++){
                Double[][] records = clusterRecords[l];
                for(int i=0; i<records.length; i++){
//                    System.out.println("Record "+Arrays.toString(records[i]));
                    errMesg = records[i];
                    for(int j=0; j<this.sizeOfCol+1; j++){
    //                    System.out.println("recRow "+i+" col "+j);
    //                    System.out.println("rec "+Arrays.toString(records[i]));
                        if(j==0){
                            this.anonymizeStatement.setInt(j+1, records[i][j].intValue());
//                            if(ids.contains(records[i][j])){
//                                System.out.println("Problem with record "+Arrays.toString(records[i]));
//                            }
//                            else{
//                                ids.add(records[i][j]);
//                            }
//                            ids.add(records[i][j]);


                        }
                        else if(this.colNamesType.get(j-1).equals("int")){
                            this.anonymizeStatement.setInt(j+1, records[i][j].intValue());
                        }
                        else {
                            this.anonymizeStatement.setDouble(j+1, records[i][j]);
                        }
                    }
                    this.anonymizeStatement.executeUpdate();
                }
            }
            
//            this.anonymizedRecordsClusters.clear();
//            conn.commit();
            System.out.println("Done execute Anonymized batch");
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" fillAnonymizedRecords "+Arrays.toString(errMesg));
        }
    }
    
    public void executeAnonymizedClusterBatch(){
        String sqlUpdate = "";
        PreparedStatement pstm = null;
        Set<Double> ids = new HashSet();
        System.out.println("Execute Anonymized batch");
        try{
            String insertSql = "INSERT INTO anonymized_dataset(";
            String valuesStr ="";
            for(int j=0; j<this.sizeOfCol; j++){
                if(j==this.sizeOfCol-1){
                    insertSql += this.colNamesPosition.get(j)+") VALUES(";
                    valuesStr += "?)";
                }
                else{
                    if(j==0){
                        insertSql += "id_an,"+this.colNamesPosition.get(j)+",";
                         valuesStr += "?,?,";
                    }
                    else{
                       insertSql += this.colNamesPosition.get(j)+","; 
                       valuesStr += "?,";
                    }
                }
            }
            this.conn.setAutoCommit(false);
            pstm = conn.prepareStatement(insertSql+valuesStr);
//            System.out.println("SQl : "+insertSql+valuesStr);
            synchronized(anonymizedRecordsClusters){
                for(Double[][] records : this.anonymizedRecordsClusters){
                    for(int i=0; i<records.length; i++){
    //                    System.out.println("Record "+Arrays.toString(records[i]));
                        for(int j=0; j<this.sizeOfCol+1; j++){
        //                    System.out.println("recRow "+i+" col "+j);
        //                    System.out.println("rec "+Arrays.toString(records[i]));
                            if(j==0){
                                pstm.setInt(j+1, records[i][j].intValue());
                                if(ids.contains(records[i][j])){
                                    System.out.println("Problem with record "+Arrays.toString(records[i]));
                                }
                                else{
                                    ids.add(records[i][j]);
                                }

                            }
                            else if(this.colNamesType.get(j-1).equals("int")){
                                pstm.setInt(j+1, records[i][j].intValue());
                            }
                            else {
                                pstm.setDouble(j+1, records[i][j]);
                            }
                        }
                        pstm.executeUpdate();
                    }
                }
            }
            this.anonymizedRecordsClusters.clear();
            conn.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" fillAnonymizedRecords");
        }finally{
            if(pstm!=null){
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void deleteCheckedTable(){
        String sqlDelete = "DROP TABLE IF EXISTS checked_records;";
        Statement stm = null;
        try{
            Connection newConn = DriverManager.getConnection(this.urlDatabase);
            stm = newConn.createStatement();
            stm.executeUpdate(sqlDelete);
            
            
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Delete checked table "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    
    public void fillAnonymizedRecords(Double [][] records){
        this.anonymizedRecordsClusters.add(records);
    }
    
    
    public boolean tableExists(String name){
        ResultSet rs = null;
        try{
            DatabaseMetaData md = conn.getMetaData();
            rs = md.getTables(null, null, name, null);
            boolean result =  rs.next();
            rs.close();
            return result;
        }catch(SQLException ex){
            ex.printStackTrace();
            System.err.println("Error: "+ex.getMessage()+" check table existance disk");
            Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                    Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return false;
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
    
}
