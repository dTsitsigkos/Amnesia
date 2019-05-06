/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jsoninterface.View;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author nikos
 */
public class XMLData implements Data {
    
    @JsonView(View.GetColumnNames.class)
    private String inputFile = null;
    private double dataSet[][] = null;
    private int sizeOfRows = 0;
    private int sizeOfCol = 0;
    private Map<String,Integer> namesToColumns;
    
    @JsonView(View.GetDataTypes.class)
    private Map <Integer,String> colNamesType = null;
    private CheckVariables chVar = null;
    private Map <Integer,String> colNamesPosition = null;
    
    private DictionaryString dictionary = null;
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
    
    private static final String[] formats = { 
                "yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss", 
                "MM/dd/yyyy HH:mm:ss",        "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", 
                "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS", 
                "MM/dd/yyyy'T'HH:mm:ssZ",     "MM/dd/yyyy'T'HH:mm:ss", 
                "yyyy:MM:dd HH:mm:ss",        "yyyy/MM/dd", 
                "yyyy:MM:dd HH:mm:ss.SS",      "dd/MM/yyyy",
                "dd MMM yyyy"};
    
    public XMLData(String inputFile,DictionaryString dict){
        recordsTotal = 0;
        this.smallDataSet = null;
        colNamesType = new TreeMap<Integer,String>();
        colNamesPosition = new HashMap<Integer,String>();
        chVar = new CheckVariables();
        dictionary = dict;
        this.inputFile = inputFile;
    }

    @Override
    public double[][] getDataSet() {
        return dataSet;
    }

    @Override
    public void setData(double[][] _data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDataLenght() {
        return dataSet.length;
    }

    @Override
    public int getDataColumns() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportOriginalData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
    
    public String timestampToDate( String tmstmp){
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
                                    System.out.println("parse = " + parse);
                                    //System.out.println("date111111" );
                                    SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

                                    //Date date = sf.parse(tmstmp); 
                                    //System.out.println("date = " + date );
                                    System.out.println("return = " +sf.format(d1));
                                    //System.out.println(date.);
                                    tmstmp = null;
                                    tmstmp = sf.format(d1);
                                    return tmstmp;
                                }
                            } 
                        }
                        else{
                            System.out.println("parse = " + parse);
                            //System.out.println("date111111" );
                            SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");

                            //Date date = sf.parse(tmstmp); 
                            //System.out.println("date = " + date );
                            System.out.println("return = " +sf.format(d1));
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
    public String save(boolean[] checkColumns) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        File fXmlFile = null;
        DocumentBuilderFactory dbFactory = null;
        DocumentBuilder dBuilder = null;
        Document doc = null;
        ArrayList<String> columns = new ArrayList<String>();
        String []colNames = null;
        int stringCount = dictionary.getMaxUsedId()+1;
        int rec=0;
        int col;
        for(int i=0; i<this.columnNames.length; i++){
            if(checkColumns[i]){
                columns.add(columnNames[i]);
            }
        }
        colNames = new String[columns.size()];
        colNames = columns.toArray(new String[0]);
        this.setColumnNames(colNames);
        dataSet = new double[sizeOfRows][columnNames.length];
        
        try{
            fXmlFile = new File(this.inputFile); 
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = (Document) dBuilder.parse(fXmlFile);

            NodeList nList = doc.getElementsByTagName("MedicalEvent");
            String[] value=null;
            for(int i=0; i<nList.getLength(); i++){ // records
                Node medicalEvent = nList.item(i);
                if (medicalEvent.getNodeType() == Node.ELEMENT_NODE) { 
                    Element mediEventElement = (Element) medicalEvent;
                    NodeList clinicVars = mediEventElement.getElementsByTagName("ns1:ClinicalVariables");    // split
                    value  = new String[this.namesToColumns.size()] ;
                    for(int j=0; j<clinicVars.getLength(); j++){
                        Node clinicVar = clinicVars.item(j);
                        
                        if(clinicVar.getNodeType() == Node.ELEMENT_NODE){
                            Element elementClinicVar = (Element) clinicVar;
                            if(!elementClinicVar.getAttribute("xsi:type").equals("ns1:Observation")){
                                String colName = elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent();
                                String valueTemp = elementClinicVar.getElementsByTagName("ns1:Value").item(0).getTextContent();
                                if(!this.namesToColumns.containsKey(colName)){
                                    continue;
                                }
                                col = this.namesToColumns.get(colName);
                                value[col] = valueTemp;
                                if ( colNamesType.get(col).contains("int") ){
                                    if ( !value[col].equals("")){
                                        try {
                                            dataSet[rec][col] = Integer.parseInt(value[col]);
                                        } catch (java.lang.NumberFormatException exc) {
                                            //ErrorWindow.showErrorWindow("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                            System.out.println("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                            return null;
                                        }   
                                    }
                                    else{
                                        dataSet[rec][col] = 2147483646;
                                    }
                                }
                                else if ( colNamesType.get(col).contains("double") ){
                                    if ( !value[col].equals("")){
                                        dataSet[rec][col] = Double.parseDouble(value[col]);
                                    }
                                    else{
                                        dataSet[rec][col] = Double.NaN;
                                    }
                                }
                                else if ( colNamesType.get(col).contains("date") ){
                                    String var = null;
    //                                System.out.println("date= "+temp[i]+" counter= "+counter+" counter1= "+counter1+" ");
                                    if ( !value[col].equals("")){
                                        var = value[col];
                                        var = this.timestampToDate(var);
                                    }
                                    else {
                                        var = "NaN";
                                    }


                                    //transform timestamp to date
                                    //var = this.timestampToDate(var);

                                    if (var != null) {
                                    //if string is not present in the dictionary
                                        if (dictionary.containsString(var) == false){
                                            dictionary.putIdToString(stringCount, var);
                                            dictionary.putStringToId(var,stringCount);
    //                                        dictionary.put(counter1, tempDict);
                                            dataSet[rec][col] = stringCount;
                                            stringCount++;
                                        }
                                        else{
                                            //if string is present in the dictionary, get its id
                                            int stringId = dictionary.getStringToId(var);
                                            dataSet[rec][col] = stringId;
                                        }
                                    }
                                }
                                else{
                                   String var = null;

                                    if ( !value[col].equals("")){
                                        var = value[col];
                                    }
                                    else {
                                        var = "NaN";
                                    }

                                    //if string is not present in the dictionary
                                    if (dictionary.containsString(var) == false){
                                        dictionary.putIdToString(stringCount, var);
                                        dictionary.putStringToId(var,stringCount);
    //                                    dictionary.put(counter1, tempDict);
                                        dataSet[rec][col] = stringCount;
                                        stringCount++;
                                    }
                                    else{
                                        //if string is present in the dictionary, get its id
                                        int stringId = dictionary.getStringToId(var);
                                        dataSet[rec][col] = stringId;
                                    } 
                                }
                            }
                        }
                        
//                        if(j==clinicVars.getLength()-1){
//                            for(int l=0; l<value.length; l++){
//                                if(value[l]!=null && value[l].equals("97.79")){
//                                    System.out.println("date IGHVI "+value[5]);
//                                }
//                                if(value[l]==null){
//                                    if ( colNamesType.get(l).contains("int") ){
//                                        dataSet[rec][l] = 2147483646;
//                                    }
//                                    else if(colNamesType.get(l).contains("double")){
//                                        dataSet[rec][l] = Double.NaN;
//                                    }
//                                    else {
//                                        String var = "NaN";
//                                        if (dictionary.containsString(var) == false){
//                                            dictionary.putIdToString(stringCount, var);
//                                            dictionary.putStringToId(var,stringCount);
//    //                                        dictionary.put(counter1, tempDict);
//                                            dataSet[rec][l] = stringCount;
//                                            stringCount++;
//                                        }
//                                        else{
//                                            //if string is present in the dictionary, get its id
//                                            int stringId = dictionary.getStringToId(var);
//                                            dataSet[rec][l] = stringId;
//                                        }
//                                    }
//                                }
//                            }
//                        }
                    }
                }
                if(value !=null){
                    for(int l=0; l<value.length; l++){
                        if(value[l]!=null && value[l].equals("97.79")){
                            System.out.println("date IGHVI "+value[5]);
                        }
                        if(value[l]==null){
                            if ( colNamesType.get(l).contains("int") ){
                                dataSet[rec][l] = 2147483646;
                            }
                            else if(colNamesType.get(l).contains("double")){
                                dataSet[rec][l] = Double.NaN;
                            }
                            else {
                                String var = "NaN";
                                if (dictionary.containsString(var) == false){
                                    dictionary.putIdToString(stringCount, var);
                                    dictionary.putStringToId(var,stringCount);
    //                                        dictionary.put(counter1, tempDict);
                                    dataSet[rec][l] = stringCount;
                                    stringCount++;
                                }
                                else{
                                    //if string is present in the dictionary, get its id
                                    int stringId = dictionary.getStringToId(var);
                                    dataSet[rec][l] = stringId;
                                }
                            }
                        }
                    }
                }
                rec++;
            }
            
        }catch(Exception e){
            System.err.println("Error in save: "+e.getMessage());
        }
        return "OK";
    }

    @Override
    public void preprocessing() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String readDataset(String[] columnTypes, boolean[] checkColumns) {
        SaveClmnsAndTypeOfVar(columnTypes,checkColumns);
//        preprocessing();
        String result = save(checkColumns);
        return result;
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
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        File fXmlFile = null;
        DocumentBuilderFactory dbFactory = null;
        DocumentBuilder dBuilder = null;
        Document doc = null;
        int row=0;
        int column;
        
        Object[][] temp = null;
        if ( initialTable != null ){
            System.out.println("initial table");
            temp = initialTable;
        }
        else{
            System.out.println("anonymized table");
            temp = anonymizedTable;
        }
        
        Object[] rowQIs = null;
        if(suppressedValues != null){
            rowQIs = new Object[qids.length];
        }
        
        try{
            fXmlFile = new File(this.inputFile); 
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = (Document) dBuilder.parse(fXmlFile);

            NodeList nList = doc.getElementsByTagName("MedicalEvent");
            for(int i=0; i<nList.getLength(); i++){ // records  
                
                
                //write table data
                    
                    //if suppressed values exist
                if(suppressedValues != null){


                    //get qids of this row
                    for(int l=0; l<qids.length; l++){
                        rowQIs[l] = temp[row][qids[l]];
                    }


                    //check if row is suppressed
                    if(isSuppressed(rowQIs, qids, suppressedValues)){
                        continue;
                    }
                }
                
                Node medicalEvent = nList.item(i);
                if (medicalEvent.getNodeType() == Node.ELEMENT_NODE) {
                    Element mediEventElement = (Element) medicalEvent;
                    NodeList clinicVars = mediEventElement.getElementsByTagName("ns1:ClinicalVariables");    // split
                    for(int j=0; j<clinicVars.getLength(); j++){
                        Node clinicVar = clinicVars.item(j);
                        if(clinicVar.getNodeType() == Node.ELEMENT_NODE){
                            Element elementClinicVar = (Element) clinicVar;
                            if(!elementClinicVar.getAttribute("xsi:type").equals("ns1:Observation")){
                                String colName = elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent();
                                elementClinicVar.getElementsByTagName("ns1:Value").item(0).getTextContent();
                                if(!this.namesToColumns.containsKey(colName)){
                                    continue;
                                }
                                column = this.namesToColumns.get(colName);
                                Object value = temp[row][column];
                                if(value instanceof RangeDouble){
                                    if ( colNamesType.get(column-1).equals("double")){
                                        elementClinicVar.getElementsByTagName("ns1:Value").item(0).setTextContent(((RangeDouble)value).lowerBound + "-"+((RangeDouble)value).upperBound);
                                        
                                    }
                                    else{
                                        elementClinicVar.getElementsByTagName("ns1:Value").item(0).setTextContent((((RangeDouble)value).lowerBound).intValue() + "-"+(((RangeDouble)value).upperBound).intValue());
                                    }
                                }
                                else{
                                    elementClinicVar.getElementsByTagName("ns1:Value").item(0).setTextContent(value.toString());
                                }
                            }
                        }
                    }
                }
                
                row++;
            }
            
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.METHOD, "xml");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            DOMSource domSource = new DOMSource(doc);
            StreamResult sr = new StreamResult(new File(file));
            tf.transform(domSource, sr);
            System.out.println("Done xml");
        }catch(Exception e){
            System.err.println("Error in export: "+e.getMessage());
        }
        
    }

    @Override
    public Map<Integer, String> getColNamesPosition() {
        return colNamesPosition;
    }

    @Override
    public DictionaryString getDictionary() {
       return dictionary;
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
    
    public String[] getColumnNames(){
        return this.columnNames;
    }

    @Override
    @JsonIgnore
    public String getColumnByPosition(Integer columnIndex) {
        return this.colNamesPosition.get(columnIndex);
    }

    @Override
    public void SaveClmnsAndTypeOfVar(String[] columnTypes, boolean[] checkColumns) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int counter =0;
        this.namesToColumns.clear();
        for(int i=0; i<this.columnNames.length; i++){
            if(checkColumns[i]){
                colNamesType.put(counter,null);
                colNamesPosition.put(counter,columnNames[i]);
                this.namesToColumns.put(columnNames[i], counter);
                counter++;
            }
        }
        try{
            counter = 0;
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
//                        dictionary.put(counter, new DictionaryString());
                    }
                    else{
                        colNamesType.put(counter, "string");
//                        dictionary.put(counter, new DictionaryString());
                    }
                    
                    counter++;
                }
            }
            sizeOfCol = columnTypes.length;
        }catch(Exception e){
            System.err.println("Error in SaveClmnsAndTypeOfVar: "+e.getMessage());
        }
    }

    @JsonView(View.SmallDataSet.class)
    @Override
    public String findColumnTypes() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Template
        boolean firstRec = true;
        boolean typeRec = true;
        int smallRows=6;
        int counterSmall=0;
        int columnNum = 0;
        File fXmlFile = null;
        DocumentBuilderFactory dbFactory = null;
        DocumentBuilder dBuilder = null;
        Document doc = null;
        this.namesToColumns = new HashMap<String,Integer>();
        
        try{
            System.out.println("file path: "+this.inputFile);
            fXmlFile = new File(this.inputFile); 
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = (Document) dBuilder.parse(fXmlFile);
            
            NodeList nList = doc.getElementsByTagName("MedicalEvent");
            this.sizeOfRows = nList.getLength();
            if(sizeOfRows < smallRows){
                smallRows = sizeOfRows;
            }
            for (int i = 0; i < nList.getLength(); i++) {
                Node medicalEvent = nList.item(i);
                if (medicalEvent.getNodeType() == Node.ELEMENT_NODE) {
                    Element mediEventElement = (Element) medicalEvent;
                    NodeList clinicVars = mediEventElement.getElementsByTagName("ns1:ClinicalVariables");
                    
                    if(firstRec){
                        for(int j=0; j<clinicVars.getLength(); j++){
                            Node clinicVar = clinicVars.item(j);
                            
                            if(clinicVar.getNodeType() == Node.ELEMENT_NODE){
                                Element elementClinicVar = (Element) clinicVar;
                                if(!elementClinicVar.getAttribute("xsi:type").equals("ns1:Observation")){
                                    namesToColumns.put(elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent(), columnNum++);
                                }
                            }
                        }
                        
                        columnNames = new String[namesToColumns.size()];
                        smallDataSet = new String[smallRows][namesToColumns.size()];
                        firstRec = false;
                    }
                    
                    if(typeRec){
                        for(int j=0; j<clinicVars.getLength(); j++){
                            counterSmall = 0;
                            Node clinicVar = clinicVars.item(j);
                            Element elementClinicVar = (Element) clinicVar;
                            if(clinicVar.getNodeType() == Node.ELEMENT_NODE){
                                if(!elementClinicVar.getAttribute("xsi:type").equals("ns1:Observation")){
                                    String value = elementClinicVar.getElementsByTagName("ns1:Value").item(0).getTextContent();
                                    if (chVar.isInt(value)){
                                        smallDataSet[counterSmall][namesToColumns.get(elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent())] = "int";
                                    }
                                    else if(chVar.isDouble(value)){
                                        smallDataSet[counterSmall][namesToColumns.get(elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent())] = "double";
                                    }
                                    else if(chVar.isDate(value)){
                                        smallDataSet[counterSmall][namesToColumns.get(elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent())] = "date";
                                    }
                                    else{
                                        smallDataSet[counterSmall][namesToColumns.get(elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent())] = "string";
                                    }
                                    smallDataSet[++counterSmall][namesToColumns.get(elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent())] = value;
                                }
                                else{
                                    counterSmall = 1;
                                }
                            }
                        }
                        typeRec = false;
                    }
                    else if(counterSmall < smallRows){
                        for(int j=0; j<clinicVars.getLength(); j++){
                            Node clinicVar = clinicVars.item(j);
                            Element elementClinicVar = (Element) clinicVar;
                            if(clinicVar.getNodeType() == Node.ELEMENT_NODE){
                                
                                if(!elementClinicVar.getAttribute("xsi:type").equals("ns1:Observation")){
                                    
                                    String value = elementClinicVar.getElementsByTagName("ns1:Value").item(0).getTextContent();
                                    String colName = elementClinicVar.getElementsByTagName("ns1:TypeName").item(0).getTextContent();
                                    if(!this.namesToColumns.containsKey(colName)){
                                        continue;
                                    }
                                    int colNum = this.namesToColumns.get(colName);
//                                    System.out.println("column Name: "+colName+" col Num: "+colNum+" Value: "+value+" counterSmall: "+counterSmall+" smallRows: "+smallRows);
                                    smallDataSet[counterSmall][colNum] = value;
                                    if(smallDataSet[0][colNum] != null){
                                        if(smallDataSet[0][colNum].equals("int")){
                                            if (!chVar.isInt(value)){
                                                if (chVar.isDouble(value)){
                                                    smallDataSet[0][colNum] = "double";
                                                }
                                                else {
                                                    smallDataSet[0][colNum] = "string";
                                                }
                                            }
                                        }
                                        else if(smallDataSet[0][colNum].equals("double")){
                                            if (!chVar.isInt(value) && !chVar.isDouble(value)){
                                                smallDataSet[0][colNum] = "string";
                                            }
                                        }
                                    }
                                    else{
                                        if (chVar.isInt(value)){
                                            smallDataSet[0][colNum] = "int";
                                        }
                                        else if (chVar.isDouble(value)){
                                            smallDataSet[0][colNum] = "double";
                                        }
                                        else if(chVar.isDate(value)){
                                            smallDataSet[0][colNum] ="date";
                                        }
                                        else{  
                                            smallDataSet[0][colNum] = "string";
                                        }
                                    }
                                }
                            }
                        }
                        counterSmall++;
                    }
                    else{
                        for (int l = 0 ; l < smallDataSet[0].length ; l ++ ){
                            if ( smallDataSet[0][l] == null){
                                smallDataSet[0][l]= "string";
                            }
                        }
                        
                        break;
                    }
                    
                }
                
            }
            
            
            for(Entry<String,Integer> entry : this.namesToColumns.entrySet()){
                columnNames[entry.getValue()] = entry.getKey();
                
            }
            
        }catch(FileNotFoundException | UnsupportedEncodingException  e){
            System.err.println("Error: "+e.getMessage());
        } catch (SAXException ex) {
            Logger.getLogger(XMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "Ok";
    }

    @Override
    public String[][] getSmallDataSet() {
        return smallDataSet;
    }

    @Override
    public ArrayList<LinkedHashMap> getPage(int start, int length) {
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
                //System.out.println("data = " + dataSet[i][j]);
                if (colNamesType.get(j).equals("double")){
                    if (Double.isNaN(dataSet[i][j])){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
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
                    
                    String str = dictionary.getIdToString((int)dataSet[i][j]);
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
        String[] temp = inputFile.split(delimiter,-1);
        return temp[temp.length-1];
    }
    
}
