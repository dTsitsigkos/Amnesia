/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package anonymizeddataset;

import algorithms.flash.LatticeNode;
import anonymizationrules.AnonymizationRules;
import data.Data;
import data.SETData;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;


/**
 *
 * @author jimakos
 */
public class AnonymizedDataset {
    private Data dataset = null;
    private String solutionNode = null;
    private Map<Integer, Hierarchy> quasiIdentifiers = null;
    private int[] qids = null;
    private boolean anonymizedTableRendered = false;
    private Map<Double, Double> rules = null;
    private Map<Integer, Set<String>> suppressedValues;
    private int []hierarchyLevel;
    private int start;
    private int length;
    private int []transformation;
    private ArrayList<LinkedHashMap> dataAnon;
    private String selectedAttrName = null;
    private Map<String, Set<String>> toSuppressJson = null;
    private int recordsTotal;
    private int recordsFiltered;
    private ArrayList<LinkedHashMap> dataOriginal;

    public AnonymizedDataset( Data _dataset, int _start, int _length, String _selectedNode,Map<Integer, Hierarchy> _quasiIdentifiers, Map<Integer, Set<String>> _toSuppress, String _selectedAttrNames, Map<String, Set<String>> _toSuppressJson) {      
        this.dataset = _dataset;
        this.start = _start;
        this.length = _length;
        this.solutionNode = _selectedNode;
        this.quasiIdentifiers = _quasiIdentifiers;
        this.suppressedValues = _toSuppress;
        this.selectedAttrName = _selectedAttrNames;
        this.toSuppressJson = _toSuppressJson;
        recordsTotal = dataset.getDataLenght();
        recordsFiltered = dataset.getDataLenght();
       
        int i = 0;
        if (quasiIdentifiers != null){
            this.qids = new int[quasiIdentifiers.size()];
            for (Map.Entry<Integer, Hierarchy> entry : quasiIdentifiers.entrySet()) {
                qids[i] = entry.getKey();
                i++;
            }
        }

        if ( solutionNode != null){
            if (solutionNode.contains(",")){
                String[]temp = solutionNode.split(",");
                transformation = new int[temp.length];
                for ( i = 0 ; i < temp.length ; i ++){
                    transformation[i] = Integer.parseInt(temp[i]);
                }
            }
            else{
                transformation = new int[1];
                transformation[0] = Integer.parseInt(solutionNode);
            }
        }
    }

    public void setStart(int start) {
        this.start = start;
    }

    /**
     * renders anonymized dataset
     */
    public void renderAnonymizedTable() throws ParseException{
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
            
        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();
        
        //compute data of first column with line numbers
        columnName = "line#";
        columnData = new Object[length][colNamesType.size()];
        
        for(int i=0; i< length; i++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = dataSet[start+i][j];
            }
        }

        int count = 0;
        hierarchyLevel = new int[dataSet[0].length];
        for (int i = 0 ; i < hierarchyLevel.length ; i++){
            hierarchyLevel[i] = 0;
        }

        for(int column=0; column<dataSet[0].length; column++){
            
            columnName = colNamesPosition.get(column);
            boolean anonymizeColumn = false;
            Hierarchy hierarchy = null;
            int level = 0;
            
            if((count < qids.length) && (qids[count] == column)){
                anonymizeColumn = true;
                hierarchy = quasiIdentifiers.get(column);
                level = transformation[count];
                count++;
                hierarchyLevel [column] = level;
            }

            if(colNamesType.get(column).contains("int")){
                
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn && level > 0){
                        columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                        if ( !columnData[line].equals("(null)")){
                            if ( !hierarchy.getHierarchyType().equals("range")) {
                                Double num = (Double)columnData[line][column];
                                columnData[line][column] = num.intValue();
                            }
                            else{
                                columnData[line][column] = columnData[line][column].toString();
                            }
                        }
                    }
                    else{      
                        if ((double) columnData[line][column] == 2147483646.0) {
                            columnData[line][column] = "(null)";
                        }
                        else {
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = num.intValue();
                        }
                    }
                }
            }
            else if(colNamesType.get(column).contains("double")){
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn && level > 0){
                        columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                        if ( hierarchy.getHierarchyType().equals("range")) {
                            columnData[line][column] = columnData[line][column].toString();
                        }
                    }
                    else{
                        if ( columnData[line][column].equals(Double.NaN)){
                            columnData[line][column] = "(null)";
                        }
                    }
                }
            }
            else{
                DictionaryString dictionary = dictionaries.get(column);
                for(int line=0; line<columnData.length; line++){
                    Double d = (Double)columnData[line][column];
                    columnData[line][column] = dictionary.getIdToString(d.intValue());
                    if(anonymizeColumn && level > 0){
                        columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                    }
                    else{
                        if ( ((String)columnData[line][column]).equals("NaN")){
                            columnData[line][column] = "(null)";
                        }
                    }
                }
            }
        }

        dataAnon = new ArrayList<LinkedHashMap>();
        
        for ( int i = 0 ; i < columnData.length ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                if (colNamesType.get(j).equals("double")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else if (colNamesType.get(j).equals("int")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else{
                    DictionaryString dict = dataset.getDictionary().get(j);
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                    
                }
            }
            
            dataAnon.add(linkedHashTemp);
        }
    }
    
    private boolean isSuppressed(Object[] data){
        
        if (this.suppressedValues == null)
            return false;

        Object[] checkArr = new Object[1];
        
        //check for each and every qid if is suppressed
        for(int i=0; i<qids.length; i++){
            Set<String> suppressed = this.suppressedValues.get(qids[i]);
            if(suppressed != null){
                checkArr[0] = data[i];
                if(suppressed.contains(Arrays.toString(checkArr)))
                    return true;
            }
        }
        
        //check for all qids combined
        Set<String> suppressed = this.suppressedValues.get(-1);
        if(suppressed != null){
            if(suppressed.contains(Arrays.toString(data))){
                return true;
            }
        }

        return false;
    }
    
    /**
     * render anonymized table when dataset is set-valued
     * @param rules
     */
    public void renderAnonymizedTable(Map<Double, Double> rules) {
        this.rules = rules;
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();

        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }

        //compute data of first column with line numbers
        columnName = "line#";
        columnData = new Object[length][colNamesType.size()];       
        DictionaryString dictionary = dictionaries.get(0);
        dataAnon = new ArrayList<LinkedHashMap>();
        
        int line = 0;
        for (int i = 0 ; i < length ; i++){
            Object []row = new Object[1];
            Set<String> rowset = new HashSet<>();
            for (int j = 0 ;  j < dataSet[i].length ; j ++ ){
                Double value = null;
                if(rules != null && !rules.isEmpty()){
                    value = rules.get(dataSet[i][j]);
                    if(value == null){
                        value = dataSet[i][j];
                    }
                }
                else{
                    value = dataSet[i][j];
                }
                rowset.add(dictionary.getIdToString(value.intValue()));
            }
            
            StringBuilder sb = new StringBuilder();
            int count=0;
            for(String str : rowset){
                sb.append(str);
                if(count != rowset.size()-1)
                    sb.append(",");
                count++;
            }
            
            row[0] = sb.toString();
            linkedHashTemp = new LinkedHashMap<>();
            String newRow = null;
            for ( int k = 0 ; k < row.length ; k ++){
                if ( k == 0){
                    newRow = row[k].toString();
                }
                else{
                    newRow = newRow + "," + row[k].toString();
                }
            }
            linkedHashTemp.put(colNamesPosition.get(0), newRow);
            dataAnon.add(linkedHashTemp);
            row = null;
        }
    }
    
    public Object[][] exportDataset(String file,Map<Double, Double> rules){
        this.rules = rules;
        this.rules = rules;
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();

        //compute data of first column with line numbers
        columnName = "line#";
        columnData = new Object[dataset.getRecordsTotal()][colNamesType.size()];     
        DictionaryString dictionary = dictionaries.get(0);

        int line = 0;
        for (int i = 0 ; i < dataset.getRecordsTotal() ; i++){
            Object []row = new Object[1];
            //row[0] = line++;
            Set<String> rowset = new HashSet<>();
            for (int j = 0 ;  j < dataSet[i].length ; j ++ ){
                Double value = null;
                if(rules != null && !rules.isEmpty()){
                    value = rules.get(dataSet[i][j]);
                    if(value == null){
                        value = dataSet[i][j];
                    }
                }
                else{
                    value = dataSet[i][j];
                }
                rowset.add(dictionary.getIdToString(value.intValue()));
            }
            
            StringBuilder sb = new StringBuilder();
            int count=0;
            for(String str : rowset){
                sb.append(str);
                if(count != rowset.size()-1)
                    sb.append(",");
                count++;
            }

            row[0] = sb.toString();
            linkedHashTemp = new LinkedHashMap<>();
            String newRow = null;
            
            for ( int k = 0 ; k < row.length ; k ++){
                if ( k == 0){
                    newRow = row[k].toString();
                }
                else{
                    newRow = newRow + "," + row[k].toString();
                }
            }
            linkedHashTemp.put(colNamesPosition.get(0), newRow);
            columnData[i][0] = newRow;
            row = null;
        }
        
        this.dataset.export(file, null, columnData, null, null, suppressedValues);
        return null;
    }
    
    
    /**
     * export anonymized dataset to file
     * @param file the filename
     */
    public Object[][] exportDataset(String file, boolean anonymized) throws ParseException{
        double [][]dataSet = this.dataset.getDataSet();
        Object[][]columnData = null;
        Map <Integer,String> colNamesType = dataset.getColNamesType();
        Map <Integer,DictionaryString> dictionary = dataset.getDictionary();
        
        columnData = new Object[dataSet.length][dataSet[0].length];
        if (anonymized == false ){
            //write data
            for ( int i = 0 ; i < dataSet.length ; i ++){
                for (int j = 0 ; j <dataSet[i].length  ; j ++){
                    if (colNamesType.get(j).equals("double")){
                        columnData[i][j] =  Double.toString(dataSet[i][j]);
                    }
                    else if (colNamesType.get(j).equals("int")){
                        columnData[i][j] = Integer.toString((int)dataSet[i][j]);
                    }
                    else{
                        DictionaryString dict = dictionary.get(j);
                        columnData[i][j] = dict.getIdToString((int)dataSet[i][j]);
                    }
                }
            }
            this.dataset.export(file, columnData, null ,this.qids, this.quasiIdentifiers , suppressedValues);
        
        }
        else{
            Map <Integer,String> colNamesPosition = null;
            Map <Integer,DictionaryString> dictionaries = null;
            Object columnName = null;
            colNamesType = dataset.getColNamesType();
            colNamesPosition = dataset.getColNamesPosition();
            dictionaries = dataset.getDictionary();

            //compute data of first column with line numbers
            for(int i=0; i< dataSet.length; i++){
                for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                    columnData[i][j] = dataSet[start+i][j];
                }
            }

            int count = 0;

            //compute data of columns
            hierarchyLevel = new int[dataSet[0].length];
            for (int i = 0 ; i < hierarchyLevel.length ; i++){
                hierarchyLevel[i] = 0;
            }

            for(int column=0; column<dataSet[0].length; column++){
                columnName = colNamesPosition.get(column);
                boolean anonymizeColumn = false;
                Hierarchy hierarchy = null;
                int level = 0;

                if((count < qids.length) && (qids[count] == column)){
                    anonymizeColumn = true;
                    hierarchy = quasiIdentifiers.get(column);
                    level = transformation[count];
                    count++;
                    hierarchyLevel [column] = level;
                }

                if(colNamesType.get(column).contains("int")){
                    for(int line=0; line<columnData.length; line++){
                        if(anonymizeColumn && level > 0){
                            columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                            if ( !columnData[line].equals("(null)")){
                                if ( !hierarchy.getHierarchyType().equals("range")) {
                                    Double num = (Double)columnData[line][column];
                                    columnData[line][column] = num.intValue();
                                }
                                else{
                                    columnData[line][column] = columnData[line][column].toString();
                                }
                            }
                        }
                        else{                       
                            if ((double) columnData[line][column] == 2147483646.0) {
                                columnData[line][column] = "(null)";
                            }
                            else {
                                Double num = (Double)columnData[line][column];
                                columnData[line][column] = num.intValue();
                            }
                        }
                    }
                }
                else if(colNamesType.get(column).contains("double")){
                    for(int line=0; line<columnData.length; line++){
                        if(anonymizeColumn && level > 0){
                            columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                            if ( hierarchy.getHierarchyType().equals("range")) {
                                columnData[line][column] = columnData[line][column].toString();
                            }
                        }
                        else{
                            if ( columnData[line][column].equals(Double.NaN)){
                                columnData[line][column] = "(null)";
                            }
                        }
                    }
                }
                else{
                   DictionaryString dict = dictionaries.get(column);
                    for(int line=0; line<columnData.length; line++){
                        Double d = (Double)columnData[line][column];
                        columnData[line][column] = dict.getIdToString(d.intValue());
                        if(anonymizeColumn && level > 0){
                            columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                        }
                        else{
                            if ( ((String)columnData[line][column]).equals("NaN")){
                                columnData[line][column] = "(null)";
                            }
                        }
                    }
                }
            }

            this.dataset.export(file, null, columnData,qids, this.quasiIdentifiers, suppressedValues);
        }
        
        return columnData;
    }
    
    /**
     * Anonymizes value based on an hierarchy to a specific level
     * @param value the value to be anonymized
     * @param h the hierarchy to be used
     * @param level the level of the hierarchy of the anonymized value
     * @return the anonymized value
     */
    private Object anonymizeValue(Object value, Hierarchy h, int level) throws ParseException{
        Object anonymizedValue = value;

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
                         if (value.toString().equals("NaN")){
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
                    if ( ((String)value).equals("NaN") && level == 0 ){
                        return "(null)";
                    }
                    else{
                        anonymizedValue = h.getParent(anonymizedValue);
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
                
               
                int translateLevel =  h.translateDateViaLevel(h.getHeight()-level);
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
    
    /**
     * anonymize dataset with imported rules
     * @param rules the imported rules
     */
    public void anonymizeWithImportedRules(Map<String, Map<String, String>> rules) {
        
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;

        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();
        
        //compute data of first column with line numbers
        columnName = "line#";
        columnData = new Object[length][colNamesType.size()];

        for(int i=0; i< length; i++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = dataSet[start+i][j];
            }
        }
        
        int count = 0;
        
        
        
        //compute data of columns
        for(int column=0; column<dataSet[0].length; column++){ 
            columnName = colNamesPosition.get(column);
            if(rules.containsKey(columnName)){
                Map<String,String> columnRules = rules.get(columnName);
                if(colNamesType.get(column).contains("int")){
                    for(int line=0; line<columnData.length; line++){
                        if ((double) columnData[line][column] == 2147483646.0) {
                            columnData[line][column] = columnRules.get("");
                        }
                        else {

                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = columnRules.get(String.valueOf(num.intValue()));
                        }
                    }
                }
                else if(colNamesType.get(column).contains("double")){
                    for(int line=0; line<columnData.length; line++){
                        if ( columnData[line][column].equals(Double.NaN)){
                            columnData[line][column] = columnRules.get("");
                        }
                        else{
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = columnRules.get(String.valueOf(num));
                        }
                    }
                }
                else{
                    DictionaryString dictionary = dictionaries.get(column);
                    for(int line=0; line<columnData.length; line++){
                        Double d = (Double)columnData[line][column];
                        columnData[line][column] = dictionary.getIdToString(d.intValue());

                        if ( ((String)columnData[line][column]).equals("NaN")){
                            columnData[line][column] = columnRules.get("");
                        }
                        else{
                            columnData[line][column] = columnRules.get(columnData[line][column]);
                        }

                    }
                }
            }
            else{
                if(colNamesType.get(column).contains("int")){

                    for(int line=0; line<columnData.length; line++){
                        if ((double) columnData[line][column] == 2147483646.0) {
                            columnData[line][column] = "(null)";
                        }
                        else {
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = num.intValue();
                        }
                    }
                }
                else if(colNamesType.get(column).contains("double")){
                    for(int line=0; line<columnData.length; line++){
                        if ( columnData[line][column].equals(Double.NaN)){
                            columnData[line][column] = "(null)";
                        }
                    }
                }
                else{
                    DictionaryString dictionary = dictionaries.get(column);
                    for(int line=0; line<columnData.length; line++){
                        Double d = (Double)columnData[line][column];
                        columnData[line][column] = dictionary.getIdToString(d.intValue());

                        if ( ((String)columnData[line][column]).equals("NaN")){
                            columnData[line][column] = "(null)";
                        }
                    }
                }
            }
        }
       
        //create json array
        dataAnon = new ArrayList<LinkedHashMap>();

        for ( int i = 0 ; i < columnData.length ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                if (colNamesType.get(j).equals("double")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else if (colNamesType.get(j).equals("int")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else{
                    DictionaryString dict = dataset.getDictionary().get(j);
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
            }
            dataAnon.add(linkedHashTemp);
        }
    }
    
    public void anonymizeSETWithImportedRules(Map<String, Map<String, String>> allRules) {
        Map<String,String> rules = null;
        
        for (Map.Entry<String,  Map<String,String>> entry : allRules.entrySet()) {
            rules = entry.getValue();
        }
        
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();
        
        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        //compute data of first column with line numbers
        columnName = "line#";
        columnData = new Object[length][colNamesType.size()];
        
        DictionaryString dictionary = dictionaries.get(0);
        dataAnon = new ArrayList<LinkedHashMap>();
        
        int line = 0;
        for (int i = 0 ; i < length ; i++){
            Object []row = new Object[1];
            Set<String> rowset = new HashSet<>();
            for (int j = 0 ;  j < dataSet[i].length ; j ++ ){
                Double value = null;
                value = dataSet[i][j];
                String data = dictionary.getIdToString(value.intValue());
                if ( rules.containsKey(data)){
                    data  = rules.get(data);
                }
                rowset.add(data);
            }
            
            StringBuilder sb = new StringBuilder();
            int count=0;
            for(String str : rowset){
                sb.append(str);
                if(count != rowset.size()-1)
                    sb.append(",");
                count++;
            }
            row[0] = sb.toString();
            linkedHashTemp = new LinkedHashMap<>();
            String newRow = null;
            for ( int k = 0 ; k < row.length ; k ++){
                if ( k == 0){
                    newRow = row[k].toString();
                }
                else{
                    newRow = newRow + "," + row[k].toString();
                }
                System.out.print(row[k].toString() + ",");
            }
            linkedHashTemp.put(colNamesPosition.get(0), newRow);
            dataAnon.add(linkedHashTemp);
            System.out.println();
            row = null;
        }
    }
    
    /**
     * adjust scrolling to both tables
     */
    public void setQuasiIdentifiers(Map<Integer, Hierarchy> quasiIdentifiers) {
        this.quasiIdentifiers = quasiIdentifiers;
        qids = new int[this.quasiIdentifiers.keySet().size()];
        int i = 0;
        
        for(Integer column : this.quasiIdentifiers.keySet()){
            qids[i] = column;
            i++;
        }
    }
    
    public boolean isAnonymizedTableRendered() {
        return anonymizedTableRendered;
    }
    
    
    public void setSuppressedValues(Map<Integer, Set<String>> _suppressedValues) {
        this.suppressedValues = _suppressedValues;
    }

    public int[] getHierarchyLevel() {
        return hierarchyLevel;
    }

    public ArrayList<LinkedHashMap> getDataAnon() {
        return dataAnon;
    }

    
    public String getSelectedAttrName() {
        return selectedAttrName;
    }

    public Map<String, Set<String>> getToSuppressJson() {
        return toSuppressJson;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public ArrayList<LinkedHashMap> getDataOriginal() {
        return dataOriginal;
    }

    public void setDataOriginal(ArrayList<LinkedHashMap> dataOriginal) {
        this.dataOriginal = dataOriginal;
    }
    
}
