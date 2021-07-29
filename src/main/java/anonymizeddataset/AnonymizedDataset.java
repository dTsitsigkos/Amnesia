/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package anonymizeddataset;

import exceptions.NotFoundValueException;
import algorithms.flash.LatticeNode;
import anonymizationrules.AnonymizationRules;
import data.Data;
import data.DiskData;
import data.RelSetData;
import data.SETData;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.distinct.HierarchyImplString;
import hierarchy.ranges.HierarchyImplRangesDate;
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
import org.springframework.web.bind.annotation.ExceptionHandler;


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
    
    public void setLength(int l){
        this.length = l;
    }

    /**
     * renders anonymized dataset
     */
    public void renderAnonymizedTable() throws ParseException, NotFoundValueException{
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
//        Map <Integer,DictionaryString> dictionaries = null;
        DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        Map<Integer,Integer> immediateTranslationLevel = new HashMap<Integer,Integer>();
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
        dictionary = dataset.getDictionary();
        
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
        for(int i=0; i<qids.length; i++){
            immediateTranslationLevel.put(qids[i], i);
        }
        Arrays.sort(qids);
//        System.out.println("transormation "+Arrays.toString(transformation) +" qids "+Arrays.toString(qids));
        for(int column=0; column<dataSet[0].length; column++){
            
            columnName = colNamesPosition.get(column);
            boolean anonymizeColumn = false;
            Hierarchy hierarchy = null;
            int level = 0;
            
            if((count < qids.length) && (qids[count] == column)){
                anonymizeColumn = true;
                hierarchy = quasiIdentifiers.get(column);
//                System.out.println("column "+column+" hier "+quasiIdentifiers);
                level = transformation[immediateTranslationLevel.get(column)];
                count++;
                hierarchyLevel [column] = level;
            }

            if(colNamesType.get(column).contains("int")){
                
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn && level > 0){
                        int originalValue = ((Double)columnData[line][column]).intValue();
                        columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                        if(columnData[line][column] == null){
                            columnData[line][column] = "(null)";
                            throw new NotFoundValueException("Value \""+originalValue+"\" is not set in the hierarchy tree");
                        }
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
                        if ((double) columnData[line][column] == 2147483646.0 || columnData[line][column].equals(Double.NaN)) {
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
//                        System.out.println("level double "+level+" hierarchy "+hierarchy.getName()+"value "+columnData[line][column]);
                        double originalValue = ((Double)columnData[line][column]);
                        columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
                        if(columnData[line][column] == null){
                            throw new NotFoundValueException("Value \""+originalValue+"\" is not set in the hierarchy tree");
                        }
                        if ( !columnData[line].equals("(null)")){
                            if ( !hierarchy.getHierarchyType().equals("range")) {
                                Double num = (Double)columnData[line][column];
                                columnData[line][column] = num;
                            }
                            else{
                                columnData[line][column] = columnData[line][column].toString();
                            }
                        }
//                        columnData[line][column] = anonymizeValue(columnData[line][column], hierarchy, level);
//                        if ( hierarchy.getHierarchyType().equals("range")) {
//                            columnData[line][column] = columnData[line][column].toString();
//                        }
                    }
                    else{
//                        if(hierarchy!=null){
//                            System.out.println("level double "+level+" hierarchy "+hierarchy.getName());
//                        }
                        if ((double) columnData[line][column] == 2147483646.0 || columnData[line][column].equals(Double.NaN)) {
                            columnData[line][column] = "(null)";
                        }
                        else {
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = num.intValue();
                        }
                        
//                        if ( columnData[line][column].equals(Double.NaN)){
//                            columnData[line][column] = "(null)";
//                        }
                    }
                }
            }
            else{
                
//                DictionaryString dictionary = dictionaries.get(column);
                for(int line=0; line<columnData.length; line++){
                    
                    //Double d = (Double)columnData[line][column];
                    //columnData[line][column] = dictionary.getIdToString(d.intValue());
                    
                    if(anonymizeColumn && level > 0){
                        if(colNamesType.get(column).contains("date")){
//                            System.out.println("Data date "+columnData[line][column]);
                            Double num = (Double)columnData[line][column];
                            String originalValue = dataset.getDictionary().getIdToString().get(num.intValue());
                            if(originalValue == null){
                                originalValue = HierarchyImplString.getWholeDictionary().getIdToString().get(num.intValue());
                            }
                            columnData[line][column] = anonymizeValue(originalValue, hierarchy, level); 
                            if(columnData[line][column] == null){
                                throw new NotFoundValueException("Value \""+originalValue+"\" is not set in the hierarchy tree");
                            }
                        }
                        else{
                            
                            Object value = anonymizeValue(columnData[line][column], hierarchy, level);
                            if(value == null){
                                Double num = (Double)columnData[line][column];
                                String originalValue = dataset.getDictionary().getIdToString().get(num.intValue());
                                throw new NotFoundValueException("Value \""+originalValue+"\" is not set in the hierarchy tree");
                            }
                            if(value instanceof String && ((String)value).equals("(null)")){
                                columnData[line][column] = "(null)";
                            }
                            else{
                                
                                columnData[line][column] = hierarchy.getDictionary().getIdToString().get(((Double)value).intValue());
                                if(columnData[line][column]==null){
                                    columnData[line][column] = dataset.getDictionary().getIdToString().get(((Double)value).intValue());
                                }
                                
                                if(columnData[line][column].equals("NaN")){
                                    columnData[line][column] = "(null)";
                                }
                            }
                            
                        }
                        
                       
                        
                        //
                    }
                    else{
                        Double num = (Double)columnData[line][column];
                        columnData[line][column] = dataset.getDictionary().getIdToString().get(num.intValue());
                        if(columnData[line][column] == null){
                            
                            columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString().get(num.intValue());
                        }
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
//                    DictionaryString dict = dataset.getDictionary().get(j);
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
    public void renderAnonymizedTable(Map<Double, Double> rules, DictionaryString dictionary) throws NotFoundValueException {
        this.rules = rules;
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
//        Map <Integer,DictionaryString> dictionaries = null;
        //DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        //dictionary = dataset.getDictionary();

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
//        DictionaryString dictionary = dictionaries.get(0);
        dataAnon = new ArrayList<LinkedHashMap>();
        
        
        int line = 0;
        for (int i = start ; i < max ; i++){
            Object []row = new Object[1];
            Set<String> rowset = new HashSet<>();
            for (int j = 0 ;  j < dataSet[i].length ; j ++ ){
                Double value = null;
                if(rules != null && !rules.isEmpty()){
                    value = rules.get(dataSet[i][j]);
                    
                    if(value == null){
                        value = dataSet[i][j];
                        throw new NotFoundValueException("Value \""+dataset.getDictionary().getIdToString(value.intValue())+"\" is not set in the hierarchy tree");

                    }
                }
                else{
                    value = dataSet[i][j];
                }
                rowset.add(dictionary.getIdToString(value.intValue()) == null ? dataset.getDictionary().getIdToString(value.intValue()) : dictionary.getIdToString(value.intValue()));
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
    
    public void renderAnonymizedDiskTable(){
        LinkedHashMap linkedHashTemp = null;
        int max;
        int count = 0;
        DiskData diskData = (DiskData) dataset;
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        Object[][]columnData = null;
        double[][] originalData = null;
        double[][] anonymizedData = null;
        double[][][] originalAnon = null;

        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        System.out.println("max "+max+" start "+start+" length "+length);
        
        try{
            originalAnon = diskData.getOriginalAnonSet(start, max);
            originalData = originalAnon[0];
            anonymizedData = originalAnon[1];
            
            if(originalAnon == null){
                originalData = diskData.getDataSet();
                anonymizedData = originalData;
            }
        }catch(Exception e){
            System.err.println("Error: render disk table "+e.getMessage());
            originalData = diskData.getDataSet();
            anonymizedData = originalData;
        }
        columnData = new Object[length][colNamesType.size()];
        for(int i = 0; i < length; i ++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = anonymizedData[i][j];
            }
        }
        
        
        
        for(int column=0; column<anonymizedData[0].length; column++){
            
            String columnName = colNamesPosition.get(column);
            boolean anonymizeColumn = false;
            Hierarchy hierarchy = null;
            
            if((count < qids.length) && (qids[count] == column)){
                anonymizeColumn = true;
                hierarchy = quasiIdentifiers.get(column);
//                level = transformation[count];
                count++;
            }
            
            if(colNamesType.get(column).equals("int")){
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn && hierarchy!=null){
                        if(hierarchy.getHierarchyType().equals("range")){
                            
                            if (anonymizedData[line][column] == originalData[line][column]){
                                columnData[line][column] = (int) originalData[line][column];
                                if(anonymizedData[line][column] == 2147483646.0){
                                   columnData[line][column] = "(null)" ;
                                }
                            }
                            else{
                                columnData[line][column] = hierarchy.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                if(anonymizedData[line][column] == 2147483646.0){
                                   columnData[line][column] = "(null)" ;
                                }
                                
                                if(!columnData[line][column].equals("(null)") && !((String)columnData[line][column]).contains("-")){
                                    try{
                                       columnData[line][column] = ((Double)Double.parseDouble((String)columnData[line][column])).intValue();
                                    }catch(NumberFormatException e){
//                                        e.printStackTrace();
                                        columnData[line][column] = hierarchy.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                    }
                                }
                            }
                        }
                        else{
                            if(anonymizedData[line][column] == 2147483646.0){
                                columnData[line][column] = "(null)" ;
                            }
                            else{
                                columnData[line][column] = (int)  anonymizedData[line][column];
                            }
                        }
                    }
                    else{
                        if(anonymizedData[line][column] == 2147483646.0){
                            columnData[line][column] = "(null)" ;
                        }
                        else{
                            columnData[line][column] = (int)  anonymizedData[line][column];
                        }
                    }
                }
            }
            else if(colNamesType.get(column).equals("double")){
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn && hierarchy!=null){
                        if(hierarchy.getHierarchyType().equals("range")){
                            if (anonymizedData[line][column] == originalData[line][column]){
                                columnData[line][column] =  originalData[line][column];
                                if(anonymizedData[line][column] == 2147483646.0){
                                   columnData[line][column] = "(null)" ;
                                }
                            }
                            else{
                                columnData[line][column] = hierarchy.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                if(anonymizedData[line][column] == 2147483646.0){
                                   columnData[line][column] = "(null)" ;
                                }
                            }
                        }
                        else{
                            if(anonymizedData[line][column] == 2147483646.0){
                                columnData[line][column] = "(null)" ;
                            }
                            else{
                                columnData[line][column] = anonymizedData[line][column];
                            }
                        }
                    }
                    else{
                        if(anonymizedData[line][column] == 2147483646.0){
                            columnData[line][column] = "(null)" ;
                        }
                        else{
                            columnData[line][column] = anonymizedData[line][column];
                        }
                    }
                }
            }
            else{
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn && hierarchy!=null){
                        if(hierarchy.getNodesType().equals("date")){
                            
                            DictionaryString dictResult = ((HierarchyImplRangesDate) hierarchy).getDictResults();
                            DictionaryString dictOriginal = hierarchy.getDictionaryData();
                            if(originalData[line][column] == anonymizedData[line][column]){
                                columnData[line][column] = hierarchy.getDictionaryData().getIdToString((int) originalData[line][column]);
                                if(anonymizedData[line][column] == 2147483646.0){
                                   columnData[line][column] = "(null)" ;
                                }
                                else{
                                    Date date = new Date((long)anonymizedData[line][column]);
                                    SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                                    columnData[line][column] = df2.format(date);
                                }
                            }
                            else{
                                columnData[line][column] = ((HierarchyImplRangesDate) hierarchy).getDictResults().getIdToString((int) anonymizedData[line][column]);
                                if(anonymizedData[line][column] == 2147483646.0){
                                   columnData[line][column] = "(null)" ;
                                }
                            }
                            
                        }
                        else{
                            if (anonymizedData[line][column] == originalData[line][column]){
                                columnData[line][column] =  dataset.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                if(columnData[line][column] == null){
                                    columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString((int) anonymizedData[line][column]);
                                }
                            }
                            else{
                                columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString((int) anonymizedData[line][column]);
                            }
                            
                            if(anonymizedData[line][column] == 2147483646.0){
                                columnData[line][column] = "(null)" ;
                            }
                        }
                    }
                    else{
                        if(dataset.getColNamesType().get(column).equals("date")){
                            columnData[line][column] = dataset.getDictionary().getIdToString((int) anonymizedData[line][column]);
                            if(columnData[line][column]==null){
                                
                                Date date = new Date((long)anonymizedData[line][column]);
                                SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                                columnData[line][column] = df2.format(date);
                            }

                            if(anonymizedData[line][column] == 2147483646.0){
                                columnData[line][column] = "(null)" ;
                            }
                        }
                        else{
                           columnData[line][column] = dataset.getDictionary().getIdToString((int) anonymizedData[line][column]);
                            if(columnData[line][column] == null){
                               columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString((int) anonymizedData[line][column]);
                            }
                            
                            if(columnData[line][column].equals("NaN")){
                                columnData[line][column] = "(null)" ;
                            }
                        }
                    }
                } 
            }
        }
        
        dataAnon = new ArrayList<LinkedHashMap>();
        for(int i=0; i<columnData.length; i++){
            linkedHashTemp = new LinkedHashMap<>();
            for(int j=0; j<colNamesType.size() ; j ++){
                linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
            }
            dataAnon.add(linkedHashTemp);
        }
        
        
       
    }
    
    
    public void renderAnonymizedTable(Map<Integer, Map<Object,Object>> rules) throws ParseException, Exception{
        LinkedHashMap linkedHashTemp = null;
        int max;
        RelSetData dataRelSet = (RelSetData) dataset;
        double[][] setData = dataRelSet.getSet();
        double[][] relData = dataRelSet.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map<Object,Object> rulesSet =  rules.get(dataRelSet.getSetColumn());
        Map<Object,Object> rulesRelational = null;
        Object columnName = null;
        Object[][]columnData = null;
        
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        
        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        columnName = "line#";
        columnData = new Object[length][colNamesType.size()];
        
        for(int i=0; i< length; i++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = relData[start+i][j];
            }
        }

        int count = 0;
        hierarchyLevel = new int[relData[0].length];
        for (int i = 0 ; i < hierarchyLevel.length ; i++){
            hierarchyLevel[i] = 0;
        }
        dataAnon = new ArrayList<LinkedHashMap>();
        
        for(int column=0; column<relData[0].length; column++){
            
            columnName = colNamesPosition.get(column);
            boolean anonymizeColumn = false;
            Hierarchy hierarchy = null;
            int level = 0;
            
            if((count < qids.length) && (qids[count] == column)){
                anonymizeColumn = true;
                hierarchy = quasiIdentifiers.get(column);
//                level = transformation[count];
                count++;
                hierarchyLevel [column] = level;
            }
            
            
            hierarchy = quasiIdentifiers.get(column);
            // set
            if(colNamesType.get(column).equals("set")){
                DictionaryString dictHierSet = hierarchy.getDictionary();
                for (int i = start ; i < max ; i++){
                    
                    Object []row = new Object[1];
                    Set<String> rowset = new HashSet<>();
                    for (int j = 0 ;  j < setData[i].length ; j ++ ){
                        Double value = null;
                        if(rulesSet != null && !rulesSet.isEmpty()){
                            if(rulesSet.get(setData[i][j]) == null){
                                throw new NotFoundValueException("Value \""+dataset.getDictionary().getIdToString(((Double)setData[i][j]).intValue())+"\" is not set in the hierarchy tree");
                            }
                            
                            value = (Double) rulesSet.get(setData[i][j]);
                            if(value == null){
                                value = setData[i][j];
                            }
                        }
                        else{
                            value = setData[i][j];
                        }
                        rowset.add(dictHierSet.getIdToString(value.intValue()) == null ? dataset.getDictionary().getIdToString(value.intValue()) : dictHierSet.getIdToString(value.intValue()));
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    int countSet=0;
                    for(String str : rowset){
                        sb.append(str);
                        if(countSet != rowset.size()-1)
                            sb.append(dataRelSet.getSetDelimeter());
                        countSet++;
                    }

                    row[0] = sb.toString();
                    linkedHashTemp = new LinkedHashMap<>();
                    String newRow = null;
                    for ( int k = 0 ; k < row.length ; k ++){
                        if ( k == 0){
                            newRow = row[k].toString();
                        }
                        else{
                            newRow = newRow + dataRelSet.getSetDelimeter() + row[k].toString();
                        }
                    }
                    linkedHashTemp.put(dataset.getColumnByPosition(column), newRow);
                    dataAnon.add(linkedHashTemp);
                    row = null;
                }    
            }
            else if(colNamesType.get(column).equals("int")){
               /// Not Supported yet
                rulesRelational = rules.get(column);
//                System.out.println("Rules "+rulesRelational);
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn){
                        if(rulesRelational.get(columnData[line][column]) == null){
                            throw new NotFoundValueException("Value \""+((Double)columnData[line][column]).intValue()+"\" is not set in the hierarchy tree");
                        }
                        
                        if(hierarchy.getHierarchyType().equals("range")){
                            if(rulesRelational.get(columnData[line][column]) instanceof RangeDouble){
                                columnData[line][column] = ((RangeDouble)rulesRelational.get(columnData[line][column])).toString();
                            }
                            else{
//                                System.out.println("nan "+(Double)rulesRelational.get(columnData[line][column])+" boolean "+!Double.isNaN((Double)rulesRelational.get(columnData[line][column])));
                                if(((Double)rulesRelational.get(columnData[line][column])) == 2147483646.0 || ((Double)rulesRelational.get(columnData[line][column])).isNaN()){
                                    Object value = rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN));
                                    if(value instanceof RangeDouble){
                                        columnData[line][column] = ((RangeDouble)rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN))).toString();
                                    }
                                    else{
                                        columnData[line][column] = "(null)";
                                    }
                                }
                                else{
                                   columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
                                }
                                
                            }
                        }
                        else{
//                            System.out.println("Value "+columnData[line][column]);
//                            columnData[line][column] = ((Integer)rulesRelational.get(columnData[line][column])).intValue();
//                            columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
                            try{
                                columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
                            }catch(ClassCastException e){
                                columnData[line][column] = ((Integer)rulesRelational.get(columnData[line][column]));
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
            else if(colNamesType.get(column).equals("double")){
                rulesRelational = rules.get(column);
//                System.out.println("Rules "+rulesRelational);
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn){
                        if(rulesRelational.get(columnData[line][column]) == null){
                            throw new NotFoundValueException("Value \""+((Double)columnData[line][column])+"\" is not set in the hierarchy tree");
                        }
                        if(hierarchy.getHierarchyType().equals("range")){
                            
                            if(rulesRelational.get(columnData[line][column]) instanceof RangeDouble){
                                columnData[line][column] = ((RangeDouble)rulesRelational.get(columnData[line][column])).toString();
                            }
                            else{
                                if(((Double)rulesRelational.get(columnData[line][column])) == 2147483646.0 || ((Double)rulesRelational.get(columnData[line][column])).isNaN()){
                                    Object value = rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN));
                                    if(value instanceof RangeDouble){
                                        columnData[line][column] = ((RangeDouble)rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN))).toString();
                                    }
                                    else{
                                        columnData[line][column] = "(null)";
                                    }
                                }
                                else{
                                    columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column]));
                                }
                            }
                            
                        }
                        else{
                            columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column]));
                        }
                    }
                    else{
                        if (((Double) columnData[line][column]).equals(Double.NaN) || (double)columnData[line][column] == 2147483646.0) {
                            columnData[line][column] = "(null)";
                        }
                        else {
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = num;
                        }
                    }
                }
            }
            else{
                /// Not Supported yet
                rulesRelational = rules.get(column);
//                System.out.println("Date rules "+rulesRelational);
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn){
                        if(rulesRelational.get(columnData[line][column]) == null){
                            throw new NotFoundValueException("Value \""+dataset.getDictionary().getIdToString(((Double)columnData[line][column]).intValue())+"\" is not set in the hierarchy tree");
                        }
                        
                        if(hierarchy.getHierarchyType().equals("range")){
                            if(rulesRelational.get(columnData[line][column]) instanceof RangeDate){
                                RangeDate rd = ((RangeDate)rulesRelational.get(columnData[line][column]));
                                columnData[line][column] = rd.dateToString(hierarchy.translateDateViaLevel(hierarchy.getHeight() - hierarchy.getLevel(rd)));
//                                columnData[line][column] = ((RangeDate)rulesRelational.get(columnData[line][column]))hn;
                            }
                            else{   
                                Double value = (Double)rulesRelational.get(columnData[line][column]);
                                if(value == 2147483646.0){
                                    Object tempVal = rulesRelational.get(new RangeDate(null,null));
                                    if(tempVal instanceof RangeDate){
                                        RangeDate rd = ((RangeDate)rulesRelational.get(new RangeDate(null,null)));
                                        columnData[line][column] = rd.dateToString(hierarchy.translateDateViaLevel(hierarchy.getHeight() - hierarchy.getLevel(rd)));
                                    }
                                    else{
                                        columnData[line][column] = "(null)";
                                    }
                                }
                                else{
//                                System.out.println("column Val"+rulesRelational.get(columnData[line][column]));
                                    columnData[line][column] = dataset.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue()); 
                                    if(columnData[line][column] == null){
                                        columnData[line][column] = hierarchy.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue());
                                    }
                                    
                                    if(columnData[line][column].equals("NaN")){
                                        columnData[line][column] = "(null)";
                                    }
                                }
                            }
                        }
                        else{
//                            System.out.println("dictionary hier \n"+hierarchy.getDictionary().getIdToString()+" dictionary hier data \n"+hierarchy.getDictionaryData().getIdToString()+" dictionary dataset \n"+dataset.getDictionary().getIdToString()+" value "+columnData[line][column]);
                            if(hierarchy.getDictionary().getIdToString().containsKey(((Double)rulesRelational.get(columnData[line][column])).intValue())){
                                columnData[line][column] = hierarchy.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue());
                            }
                            else{
                               columnData[line][column] = dataset.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue()); 
                            }
                            
                            if(columnData[line][column].equals("NaN")){
                                columnData[line][column] = "(null)";
                            }
                        }
                    }
                    else{
                        Double num = (Double)columnData[line][column];
                        columnData[line][column] = dataset.getDictionary().getIdToString().get(num.intValue());
                        if(columnData[line][column] == null){
                            columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString(num.intValue());
                        }
                        if ( ((String)columnData[line][column]).equals("NaN")){
                            columnData[line][column] = "(null)";
                        }
                    }
                } 
            }
        }
        for ( int i = 0 ; i < columnData.length ; i ++){
            linkedHashTemp = new LinkedHashMap<>(dataAnon.get(i));
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                if(!colNamesType.get(j).equals("set")){
                    if (colNamesType.get(j).equals("double")){
                        linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                    }
                    else if (colNamesType.get(j).equals("int")){
                        linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                    }
                    else{
    //                    DictionaryString dict = dataset.getDictionary().get(j);
                        linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);

                    }
                }
            }
            dataAnon.set(i, linkedHashTemp);
//            dataAnon.add(linkedHashTemp);
        }
        
//        for(int i=0; i<dataAnon.size(); i++){
//            System.out.println("i= "+i+" hashTample : "+dataAnon.get(i));
//        }
        //relational
    }
    
    public Object[][] exportDiskDataset(String file, Map<Integer, Hierarchy> quasiIdentifiers){
        DiskData data = (DiskData) this.dataset; 
        boolean printColumns = true;
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        int start=0;
        int end = dataset.getRecordsTotal()/4;
        while(end<=dataset.getRecordsTotal()){
            double[][][] originalAnon = data.getOriginalAnonSet(start, end);
            Object[][]columnData = null;
            double[][] anonymizedData = originalAnon[1];
            double[][] originalData = originalAnon[0];
            columnData = new Object[anonymizedData.length][anonymizedData[0].length];
            int count = 0;



            for(int i=0; i< anonymizedData.length; i++){
                for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                    columnData[i][j] = anonymizedData[i][j];
                }
            }

            for(int column=0; column<anonymizedData[0].length; column++){
            
                String columnName = colNamesPosition.get(column);
                boolean anonymizeColumn = false;
                Hierarchy hierarchy = null;

                if((count < qids.length) && (qids[count] == column)){
                    anonymizeColumn = true;
                    hierarchy = quasiIdentifiers.get(column);
    //                level = transformation[count];
                    count++;
                }

                if(colNamesType.get(column).equals("int")){
                    for(int line=0; line<columnData.length; line++){
                        if(anonymizeColumn && hierarchy!=null){
                            if(hierarchy.getHierarchyType().equals("range")){

                                if (anonymizedData[line][column] == originalData[line][column]){
                                    columnData[line][column] = (int) originalData[line][column];
                                    if(anonymizedData[line][column] == 2147483646.0){
                                       columnData[line][column] = "(null)" ;
                                    }
                                }
                                else{
                                    columnData[line][column] = hierarchy.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                    if(anonymizedData[line][column] == 2147483646.0){
                                       columnData[line][column] = "(null)" ;
                                    }

                                    if(!columnData[line][column].equals("(null)") && !((String)columnData[line][column]).contains("-")){
                                        try{
                                           columnData[line][column] = ((Double)Double.parseDouble((String)columnData[line][column])).intValue();
                                        }catch(NumberFormatException e){
    //                                        e.printStackTrace();
                                            columnData[line][column] = hierarchy.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                        }
                                    }
                                }
                            }
                            else{
                                if(anonymizedData[line][column] == 2147483646.0){
                                    columnData[line][column] = "(null)" ;
                                }
                                else{
                                    columnData[line][column] = (int)  anonymizedData[line][column];
                                }
                            }
                        }
                        else{
                            if(anonymizedData[line][column] == 2147483646.0){
                                columnData[line][column] = "(null)" ;
                            }
                            else{
                                columnData[line][column] = (int)  anonymizedData[line][column];
                            }
                        }
                    }
                }
                else if(colNamesType.get(column).equals("double")){
                    for(int line=0; line<columnData.length; line++){
                        if(anonymizeColumn && hierarchy!=null){
                            if(hierarchy.getHierarchyType().equals("range")){
                                if (anonymizedData[line][column] == originalData[line][column]){
                                    columnData[line][column] =  originalData[line][column];
                                    if(anonymizedData[line][column] == 2147483646.0){
                                       columnData[line][column] = "(null)" ;
                                    }
                                }
                                else{
                                    columnData[line][column] = hierarchy.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                    if(anonymizedData[line][column] == 2147483646.0){
                                       columnData[line][column] = "(null)" ;
                                    }
                                }
                            }
                            else{
                                if(anonymizedData[line][column] == 2147483646.0){
                                    columnData[line][column] = "(null)" ;
                                }
                                else{
                                    columnData[line][column] = anonymizedData[line][column];
                                }
                            }
                        }
                        else{
                            if(anonymizedData[line][column] == 2147483646.0){
                                columnData[line][column] = "(null)" ;
                            }
                            else{
                                columnData[line][column] = anonymizedData[line][column];
                            }
                        }
                    }
                }
                else{
                    for(int line=0; line<columnData.length; line++){
                        if(anonymizeColumn && hierarchy!=null){
                            if(hierarchy.getNodesType().equals("date")){

                                DictionaryString dictResult = ((HierarchyImplRangesDate) hierarchy).getDictResults();
                                DictionaryString dictOriginal = hierarchy.getDictionaryData();
                                if(originalData[line][column] == anonymizedData[line][column]){
                                    columnData[line][column] = hierarchy.getDictionaryData().getIdToString((int) originalData[line][column]);
                                    if(anonymizedData[line][column] == 2147483646.0){
                                       columnData[line][column] = "(null)" ;
                                    }
                                    else{
                                        Date date = new Date((long)anonymizedData[line][column]);
                                        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                                        columnData[line][column] = df2.format(date);
                                    }
                                }
                                else{
                                    columnData[line][column] = ((HierarchyImplRangesDate) hierarchy).getDictResults().getIdToString((int) anonymizedData[line][column]);
                                    if(anonymizedData[line][column] == 2147483646.0){
                                       columnData[line][column] = "(null)" ;
                                    }
                                }

                            }
                            else{
                                if (anonymizedData[line][column] == originalData[line][column]){
                                    columnData[line][column] =  dataset.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                    if(columnData[line][column] == null){
                                        columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString((int) anonymizedData[line][column]);
                                    }
                                }
                                else{
                                    columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString((int) anonymizedData[line][column]);
                                }

                                if(anonymizedData[line][column] == 2147483646.0){
                                    columnData[line][column] = "(null)" ;
                                }
                            }
                        }
                        else{
                            if(dataset.getColNamesType().get(column).equals("date")){
                                columnData[line][column] = dataset.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                if(columnData[line][column]==null){

                                    Date date = new Date((long)anonymizedData[line][column]);
                                    SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                                    columnData[line][column] = df2.format(date);
                                }

                                if(anonymizedData[line][column] == 2147483646.0){
                                    columnData[line][column] = "(null)" ;
                                }
                            }
                            else{
                               columnData[line][column] = dataset.getDictionary().getIdToString((int) anonymizedData[line][column]);
                                if(columnData[line][column] == null){
                                   columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString((int) anonymizedData[line][column]);
                                }

                                if(columnData[line][column].equals("NaN")){
                                    columnData[line][column] = "(null)" ;
                                }
                            }
                        }
                    } 
                }
            }

            data.export(file, null, columnData, qids, quasiIdentifiers, suppressedValues,printColumns);
            printColumns = false;
            if(end==data.getRecordsTotal()){
                break;
            }
            else{
                start = end;
                end += end;
                if(end > data.getRecordsTotal()){
                    end = data.getRecordsTotal();
                }
            }
            
        }
        
        
        return null;
    }
    
    
    public Object[][] exportRelSetDataset(String file,Map<Integer,Map<Object,Object>> rules,Map<Integer, Hierarchy> quasiIdentifiers){
        /// Not supported yet
        RelSetData  data  = (RelSetData) this.dataset;
        double[][] setData = data.getSet();
        double[][] relData = data.getDataSet();
        Map<Object,Object> rulesSet = rules.get(data.getSetColumn());
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        Map<Object,Object> rulesRelational = null;
        int count = 0 ;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionary = dataset.getDictionary();
        columnName = "line#";
        columnData = new Object[dataset.getRecordsTotal()][colNamesType.size()];
        
        for(int i=0; i< relData.length; i++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = relData[start+i][j];
            }
        }
        
        for(int column=0; column<relData[0].length; column++){
            columnName = colNamesPosition.get(column);
            boolean anonymizeColumn = false;
            Hierarchy hierarchy = null;
            int level = 0;
            
            if((count < qids.length) && (qids[count] == column)){
                anonymizeColumn = true;
                hierarchy = quasiIdentifiers.get(column);
                count++;
            }
            
            if(colNamesType.get(column).contains("set")){
                
                for (int i = 0 ; i < dataset.getRecordsTotal() ; i++){
                    Object []row = new Object[1];
                    //row[0] = line++;
                    Set<String> rowset = new HashSet<>();
                    for (int j = 0 ;  j < setData[i].length ; j ++ ){
                        Double value = null;
                        if(rulesSet != null && !rulesSet.isEmpty()){
                            value = (Double) rulesSet.get(setData[i][j]);
                            if(value == null){
                                value = setData[i][j];
                            }
                        }
                        else{
                            value = setData[i][j];
                        }
                        
                        String anonymizeValue = hierarchy.getDictionary().getIdToString(value.intValue()) == null ? data.getDictionary().getIdToString(value.intValue()) : hierarchy.getDictionary().getIdToString(value.intValue());
                        rowset.add(anonymizeValue);
                    }

                    StringBuilder sb = new StringBuilder();
                    int countSet=0;
                    for(String str : rowset){
                        sb.append(str);
                        if(countSet != rowset.size()-1)
                            sb.append(data.getSetDelimeter());
                        countSet++;
                    }

                    row[0] = sb.toString();
                    
                    String newRow = null;

                    for ( int k = 0 ; k < row.length ; k ++){
                        if ( k == 0){
                            newRow = row[k].toString();
                        }
                        else{
                            newRow = newRow + data.getSetDelimeter() + row[k].toString();
                        }
                    }
                    columnData[i][column] = newRow;
                    row = null;
                }
            }
            else if(colNamesType.get(column).contains("int")){
                /// Not supperted yet
                rulesRelational = rules.get(column);
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn){
                        if(hierarchy.getHierarchyType().equals("range")){
                            if(rulesRelational.get(columnData[line][column]) instanceof RangeDouble){
                                columnData[line][column] = ((RangeDouble)rulesRelational.get(columnData[line][column])).toString();
                            }
                            else{
                                if(((Double)rulesRelational.get(columnData[line][column])) == 2147483646.0 || ((Double)rulesRelational.get(columnData[line][column])).isNaN()){
                                    Object value = rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN));
                                    if(value instanceof RangeDouble){
                                        columnData[line][column] = ((RangeDouble)rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN))).toString();
                                    }
                                    else{
                                        columnData[line][column] = "(null)";
                                    }
                                }
                                else{
                                    columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
                                }
                                
                                
                                
//                                Double value = (Double)rulesRelational.get(columnData[line][column]);
//                                if(value == 2147483646.0){
//                                    columnData[line][column] = ((Double)rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN))).intValue();
//                                }
//                                else{
//                                    columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
//                                }
                            }
                        }
                        else{
//                            System.out.println("Value "+columnData[line][column]);
                            try{
                                columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
                            }catch(ClassCastException e){
                                columnData[line][column] = ((Integer)rulesRelational.get(columnData[line][column]));
                            }
//                            columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column])).intValue();
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
                rulesRelational = rules.get(column);
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn){
                        if(hierarchy.getHierarchyType().equals("range")){
                            if(rulesRelational.get(columnData[line][column]) instanceof RangeDouble){
                                columnData[line][column] = ((RangeDouble)rulesRelational.get(columnData[line][column])).toString();
                            }
                            else{
                                if(((Double)rulesRelational.get(columnData[line][column])) == 2147483646.0 || ((Double)rulesRelational.get(columnData[line][column])).isNaN()){
                                    Object value = rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN));
                                    if(value instanceof RangeDouble){
                                        columnData[line][column] = ((RangeDouble)rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN))).toString();
                                    }
                                    else{
                                        columnData[line][column] = "(null)";
                                    }
                                }
                                else{
                                    columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column]));
                                }

//                                Double value = (Double)rulesRelational.get(columnData[line][column]);
//                                if(value == 2147483646.0){
//                                    columnData[line][column] = ((Double)rulesRelational.get(new RangeDouble(Double.NaN,Double.NaN)));
//                                }
//                                columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column]));
                            }
                        }
                        else{
                            columnData[line][column] = ((Double)rulesRelational.get(columnData[line][column]));
                        }
                    }
                    else{
                        if (((Double) columnData[line][column]).equals(Double.NaN) || (double) columnData[line][column] == 2147483646.0) {
                            columnData[line][column] = "(null)";
                        }
                        else {
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = num;
                        }
                    }
                }
            }
            else{
               rulesRelational = rules.get(column);
//                System.out.println("Date rules "+rulesRelational);
                for(int line=0; line<columnData.length; line++){
                    if(anonymizeColumn){
                        if(hierarchy.getHierarchyType().equals("range")){
                            if(rulesRelational.get(columnData[line][column]) instanceof RangeDate){
                                RangeDate rd = ((RangeDate)rulesRelational.get(columnData[line][column]));
                                columnData[line][column] = rd.dateToString(hierarchy.translateDateViaLevel(hierarchy.getHeight() - hierarchy.getLevel(rd)));
//                                columnData[line][column] = ((RangeDate)rulesRelational.get(columnData[line][column]))hn;
                            }
                            else{
                                Double value = (Double)rulesRelational.get(columnData[line][column]);
                                if(value == 2147483646.0){
                                    RangeDate rd = ((RangeDate)rulesRelational.get(new RangeDate(null,null)));
                                    columnData[line][column] = rd.dateToString(hierarchy.translateDateViaLevel(hierarchy.getHeight() - hierarchy.getLevel(rd)));
                                }
                                else{
//                                    System.out.println("column Val"+rulesRelational.get(columnData[line][column]));
                                    columnData[line][column] = dataset.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue()); 
                                }
                            }
                        }
                        else{
//                            System.out.println("dictionary hier \n"+hierarchy.getDictionary().getIdToString()+" dictionary hier data \n"+hierarchy.getDictionaryData().getIdToString()+" dictionary dataset \n"+dataset.getDictionary().getIdToString()+" value "+columnData[line][column]);
                            if(hierarchy.getDictionary().getIdToString().containsKey(((Double)rulesRelational.get(columnData[line][column])).intValue())){
                                columnData[line][column] = hierarchy.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue());
                            }
                            else{
                               columnData[line][column] = dataset.getDictionary().getIdToString().get(((Double)rulesRelational.get(columnData[line][column])).intValue()); 
                            }
                        }
                    }
                    else{
                        Double num = (Double)columnData[line][column];
                        columnData[line][column] = dataset.getDictionary().getIdToString().get(num.intValue());
                        if(columnData[line][column] == null){
                            columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString().get(num.intValue());
                        }
                        if ( ((String)columnData[line][column]).equals("NaN")){
                            columnData[line][column] = "(null)";
                        }
                    }
                } 
            }
        }
        
        data.export(file, null, columnData, qids, quasiIdentifiers, suppressedValues);
        
        
        return null;
    }
    
    public Object[][] exportDataset(String file,Map<Double, Double> rules, Map<Integer, Hierarchy> quasiIdentifiers){
        this.rules = rules;
        this.rules = rules;
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
//        Map <Integer,DictionaryString> dictionaries = null;
        DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionary = dataset.getDictionary();

        //compute data of first column with line numbers
        columnName = "line#";
        columnData = new Object[dataset.getRecordsTotal()][colNamesType.size()];     
//        DictionaryString dictionary = dictionaries.get(0);

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
                String anonymized = quasiIdentifiers.get(0).getDictionary().getIdToString(value.intValue()) == null ? dataset.getDictionary().getIdToString(value.intValue()) :  quasiIdentifiers.get(0).getDictionary().getIdToString(value.intValue());
                rowset.add(anonymized);
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
//        Map <Integer,DictionaryString> dictionary = dataset.getDictionary();
        DictionaryString dictionary = dataset.getDictionary();
        
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
//                        DictionaryString dict = dictionary.get(j);
                        columnData[i][j] = dictionary.getIdToString((int)dataSet[i][j]);
                    }
                }
            }
            this.dataset.export(file, columnData, null ,this.qids, this.quasiIdentifiers , suppressedValues);
        
        }
        else{
            Map <Integer,String> colNamesPosition = null;
//            Map <Integer,DictionaryString> dictionaries = null;
            Object columnName = null;
            colNamesType = dataset.getColNamesType();
            colNamesPosition = dataset.getColNamesPosition();
//            dictionaries = dataset.getDictionary();

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
                    hierarchy.setLevel(level);
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
//                   DictionaryString dict = dictionaries.get(column);
                    for(int line=0; line<columnData.length; line++){
                        //Double d = (Double)columnData[line][column];
                        //columnData[line][column] = dictionary.getIdToString(d.intValue());
                        if(anonymizeColumn && level > 0){
                            if(colNamesType.get(column).contains("date")){
                               columnData[line][column] = anonymizeValue(dataset.getDictionary().getIdToString(((Double)columnData[line][column]).intValue()), hierarchy, level);
                            }
                            else{
                                Object value = anonymizeValue(columnData[line][column], hierarchy, level);
                                if(value instanceof String && ((String)value).equals("(null)")){
                                    columnData[line][column] = "(null)";
                                }
                                else{

                                    columnData[line][column] = hierarchy.getDictionary().getIdToString().get(((Double)value).intValue());
                                    if(columnData[line][column]==null){
                                        columnData[line][column] = dataset.getDictionary().getIdToString().get(((Double)value).intValue());
                                    }
                                }
                            }
                        }
                        else{
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = dataset.getDictionary().getIdToString().get(num.intValue());
                            if(columnData[line][column] == null){
                                columnData[line][column] = HierarchyImplString.getWholeDictionary().getIdToString().get(num.intValue());
                            }
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
//        System.out.println("level "+level+" heirarchy "+h.getName()+"value "+value);
        for(int i=0; i<level; i++){
            h.setLevel(i);
            if(h.getHierarchyType().equals("range")){
                if(h.getNodesType().equals("double") ||  h.getNodesType().equals("int")){
                    if ( i ==0 ){
                        System.out.println("Value "+value+" i="+i);
                        if ( (double) value == 2147483646.0 ||  value.equals(Double.NaN)){
                            anonymizedValue = h.getParent(new RangeDouble(Double.NaN,Double.NaN));
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
                                anonymizedValue = h.getParent(new RangeDate(null,null));
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
                                anonymizedValue = h.getParent(new RangeDate(null,null));
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
//                System.out.println("AnoValue="+anonymizedValue);
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
    
     public static Date getDateFromString(String tmstmp) throws ParseException{
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
     
    public void anonymizeWithImportedRulesForDisk(Map<String,Map<String,String>> rules, String file){
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
//        Map <Integer,DictionaryString> dictionaries = null;
        DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        double [][] dataSet = null;
        
        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        DiskData diskData = (DiskData) this.dataset;
        colNamesType = diskData.getColNamesType();
        colNamesPosition = diskData.getColNamesPosition();
//        dictionaries = dataset.getDictionary();
        dictionary = diskData.getDictionary();
        dataSet = diskData.getDataset(start, max);
        columnData = new Object[length][colNamesType.size()];

        for(int i=0; i< length; i++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = dataSet[i][j];
            }
        }
        
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
        
        dataAnon = new ArrayList<LinkedHashMap>();

        for ( int i = 0 ; i < columnData.length ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
            }
            dataAnon.add(linkedHashTemp);
        }
        
        if(file!=null){
            DiskData data = (DiskData) this.dataset;
            data.export(file, null, columnData, qids, quasiIdentifiers, suppressedValues,true);
        }
    }
    
    /**
     * anonymize dataset with imported rules
     * @param rules the imported rules
     */
    public void anonymizeWithImportedRules(Map<String, Map<String, String>> rules, String file) {
        
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
//        Map <Integer,DictionaryString> dictionaries = null;
        DictionaryString dictionary = null;
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
//        dictionaries = dataset.getDictionary();
        dictionary = dataset.getDictionary();
        
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
//                    DictionaryString dictionary = dictionaries.get(column);
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
//                    DictionaryString dictionary = dictionaries.get(column);
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
//                    DictionaryString dict = dataset.getDictionary().get(j);
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
            }
            dataAnon.add(linkedHashTemp);
        }
        
        if(file!=null){
            
            this.dataset.export(file, null, columnData,qids, this.quasiIdentifiers, suppressedValues);
        }
    }
    
    public void anonymizeRelSetWithImportedRules(Map<String, Map<String, String>> allRules, String file){
        RelSetData dataSet = (RelSetData) this.dataset;
        double[][] setData = dataSet.getSet();
        double[][] relData = dataSet.getDataSet();
        
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        int count = 0;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();

        dictionary = dataset.getDictionary();
        
        if ( start + length <= dataset.getRecordsTotal() ){
            max = start + length;
        }
        else{
            max = dataset.getRecordsTotal();
            length = dataset.getRecordsTotal()-start;
        }
        
        columnName = "line#";
        columnData = new Object[length][colNamesType.size()];
        
        dataAnon = new ArrayList<LinkedHashMap>();
        
        for(int i=0; i< length; i++){
            for( int j = 0 ; j < colNamesType.size() ; j ++ ){
                columnData[i][j] = relData[start+i][j];
            }
        }
        
        for(int column=0; column<relData[0].length; column++){ 
            columnName = colNamesPosition.get(column);
            Map<String,String> columnRules = allRules.get(columnName);
            if(allRules.containsKey(columnName)){
                if(colNamesType.get(column).contains("set")){
                    for (int i = start ; i < max ; i++){
                        Object []row = new Object[1];
                        Set<String> rowset = new HashSet<>();
                        for (int j = 0 ;  j < setData[i].length ; j ++ ){
                            Double value = null;
                            value = setData[i][j];
                            String data = dictionary.getIdToString(value.intValue());
                            if ( columnRules.containsKey(data)){
                                data  = columnRules.get(data);
                            }
                            rowset.add(data);
                        }

                        StringBuilder sb = new StringBuilder();
                        int countSet=0;
                        for(String str : rowset){
                            sb.append(str);
                            if(countSet != rowset.size()-1)
                                sb.append(dataSet.getSetDelimeter());
                            countSet++;
                        }
                        row[0] = sb.toString();
                        String newRow = null;
                        for ( int k = 0 ; k < row.length ; k ++){
                            if ( k == 0){
                                newRow = row[k].toString();
                            }
                            else{
                                newRow = newRow + dataSet.getSetDelimeter() + row[k].toString();
                            }
                            //System.out.print(row[k].toString() + ",");
                        }
                        columnData[i-start][column] = newRow;
                        row = null;
                    }
                }
                else if(colNamesType.get(column).contains("int")){
                    for(int line=0; line<columnData.length; line++){
                        if ((double) columnData[line][column] == 2147483646.0) {
                            if(columnRules.containsKey("")){
                                columnData[line][column] = columnRules.get("");
                            }
                            else if(columnRules.containsKey("(null)")){
                                columnData[line][column] = columnRules.get("(null)");
                            }
                            else{
                                columnData[line][column] = columnRules.get("NaN");
                            }
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
                            if(columnRules.containsKey("")){
                                columnData[line][column] = columnRules.get("");
                            }
                            else if(columnRules.containsKey("(null)")){
                                columnData[line][column] = columnRules.get("(null)");
                            }
                            else{
                                columnData[line][column] = columnRules.get("NaN");
                            }
                            
                        }
                        else{
                            Double num = (Double)columnData[line][column];
                            columnData[line][column] = columnRules.get(String.valueOf(num));
                        }
                    }
                }
                else{
                   for(int line=0; line<columnData.length; line++){
                        Double d = (Double)columnData[line][column];
                        columnData[line][column] = dictionary.getIdToString(d.intValue());

                        if ( ((String)columnData[line][column]).equals("NaN")){
                            if(columnRules.containsKey("")){
                                columnData[line][column] = columnRules.get("");
                            }
                            else if(columnRules.containsKey("(null)")){
                                columnData[line][column] = columnRules.get("(null)");
                            }
                            else{
                                columnData[line][column] = columnRules.get("NaN");
                            }
                        }
                        else{
                            columnData[line][column] = columnRules.get(columnData[line][column]);
                        }

                    }
                }
            }
            else{
                if(colNamesType.get(column).contains("set")){
                    for (int i = start ; i < max ; i++){
                        Object []row = new Object[1];
                        Set<String> rowset = new HashSet<>();
                        for (int j = 0 ;  j < setData[i].length ; j ++ ){
                            Double value = null;
                            value = setData[i][j];
                            String data = dictionary.getIdToString(value.intValue());
                            rowset.add(data);
                        }
                        StringBuilder sb = new StringBuilder();
                        int countSet=0;
                        for(String str : rowset){
                            sb.append(str);
                            if(countSet != rowset.size()-1)
                                sb.append(dataSet.getSetDelimeter());
                            countSet++;
                        }
                        row[0] = sb.toString();
                        String newRow = null;
                        for ( int k = 0 ; k < row.length ; k ++){
                            if ( k == 0){
                                newRow = row[k].toString();
                            }
                            else{
                                newRow = newRow + dataSet.getSetDelimeter() + row[k].toString();
                            }
                            //System.out.print(row[k].toString() + ",");
                        }
                        columnData[i-start][column] = newRow;
                        row = null;
                        
                    }
                }
                else if(colNamesType.get(column).contains("int")){
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
        

        for ( int i = 0 ; i < columnData.length ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                if (colNamesType.get(j).equals("set")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else if (colNamesType.get(j).equals("double")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else if (colNamesType.get(j).equals("int")){
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
                else{
//                    DictionaryString dict = dataset.getDictionary().get(j);
                    linkedHashTemp.put(dataset.getColumnByPosition(j), columnData[i][j]);
                }
            }
            dataAnon.add(linkedHashTemp);
        }
        RelSetData  data  = (RelSetData) this.dataset;
        if(file!=null)
            data.export(file, null, columnData, qids, quasiIdentifiers, suppressedValues);
    }
    
    

    public void anonymizeSETWithImportedRules(Map<String, Map<String, String>> allRules, String file) {
        Map<String,String> rules = null;
        
        for (Map.Entry<String,  Map<String,String>> entry : allRules.entrySet()) {
            rules = entry.getValue();
        }
        
        double [][]dataSet = this.dataset.getDataSet();
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
//        Map <Integer,DictionaryString> dictionaries = null;
        DictionaryString dictionary = null;
        Object columnName = null;
        Object[][]columnData = null;
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
//        dictionaries = dataset.getDictionary();
        dictionary = dataset.getDictionary();
        
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
        
//        DictionaryString dictionary = dictionaries.get(0);
        dataAnon = new ArrayList<LinkedHashMap>();
        
        int line = 0;
        for (int i = start ; i < max ; i++){
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
                //System.out.print(row[k].toString() + ",");
            }
            linkedHashTemp.put(colNamesPosition.get(0), newRow);
            dataAnon.add(linkedHashTemp);
            columnData[i][0] = newRow;
//            System.out.println();
            row = null;
        }
        
        if(file!=null)
            this.dataset.export(file, null, columnData, null, null, suppressedValues);
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
        if(this.dataOriginal!=null){
            this.dataOriginal.clear();
        }
        this.dataOriginal = new ArrayList(dataOriginal);
    }
    
}
