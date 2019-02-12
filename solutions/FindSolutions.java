/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solutions;

import algorithms.flash.LatticeNode;
import com.fasterxml.jackson.annotation.JsonView;
import data.Data;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jsoninterface.View;
import solutions.SolutionStatistics.SolutionAnonValues;

/**
 *
 * @author jimakos
 */
public class FindSolutions {
    private Data dataset = null;
    @JsonView(View.Solutions.class)
    private final Map<SolutionHeader, SolutionStatistics> solutionStatistics = new LinkedHashMap<>();
    Map<SolutionHeader, Integer> nonAnonymizedCount = new LinkedHashMap<>();
    Map<SolutionHeader, Set<String>> nonAnonymousValues = new HashMap<>();
    Map<Integer, Hierarchy> quasiIdentifiers = null;
    String node = null;
    int[] qids = null;
    Map<Integer, Set<String>> toSuppress;
    
    
    public FindSolutions(Data _dataset, Map<Integer, Hierarchy> _quasiIdentifiers, String _node, int[] _qids, Map<Integer, Set<String>> _toSuppress) throws ParseException{
        dataset = _dataset;
        quasiIdentifiers = _quasiIdentifiers;
        node = _node;
        qids = _qids;
        toSuppress = _toSuppress;
        
        int[] transformation = null;

        
        if ( node != null){
            if (node.contains(",")){
                String[]temp = node.split(",");
                transformation = new int[temp.length];
                for ( int i = 0 ; i < temp.length ; i ++){
                    transformation[i] = Integer.parseInt(temp[i]);
                }
            }
            else{
                transformation = new int[1];
                transformation[0] = Integer.parseInt(node);
            }
        }
        
        //System.out.println("nodeeeeeeeeeeeeeeeeee = " + node);
         
       //transf = ((LatticeNode)node).getTransformation();
        
        //find statistics for eveery single QI

        
        if(qids.length > 1){ 
            for(int i=0; i<qids.length; i++){
                int[] curQids = new int[1];
                curQids[0] = qids[i];
                //anonymizeTuples(transformation, curQids, i);
                anonymizeTuplesOneQuasi(transformation, qids, i,curQids);
            }
        }
        
        
        
        

        anonymizeTuples(transformation, qids, 0);
        
        /*System.out.println("suppressssssssssssssssssssssssssss");
        if (toSuppress != null){
        for (Map.Entry<Integer, Set<String>> entry : toSuppress.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().toString());
        }}*/
        
        
        //this.print();
        
    }

    public Map<SolutionHeader, SolutionStatistics> getSolutionStatistics() {
        return solutionStatistics;
    }
    
    
    private void anonymizeTuplesOneQuasi(int[] transformation, int[] qids, int whichQid, int []specificQid) throws ParseException{
//        System.out.println("\ntransf : " + Arrays.toString(transformation) + " qids : " + Arrays.toString(qids));
        double [][]dataSet = dataset.getDataSet();
        int length = dataSet.length;
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        String columnName = null;
        SolutionHeader header;
        SolutionStatistics statistics;
        
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();
        
        //System.out.println("whichQiddddddddddddddddddddd = " + whichQid);
        

//        Map<String, Double> tempFreq = new HashMap<>();
        header = new SolutionHeader(specificQid, transformation, this.dataset);
        statistics = new SolutionStatistics(this.dataset.getDataLenght(),colNamesType);

        
        for(int line=0; line<length; line++){
            int count = 0;
            int j = 0;
            
            Object data[] = new Object[qids.length];
            
            for(int column=0; column<dataSet[0].length; column++){
                
                //System.out.println("dataSet[0].length = " + dataSet[0].length + "\tcolumn = " +column);
                //System.out.println(" dataSet[line][column] = " +  dataSet[line][column]);
                        
                columnName = colNamesPosition.get(column);
                boolean anonymizeColumn = false;
                Hierarchy hierarchy = null;
                int level = 0;
                
                if((count < qids.length) && (qids[count] == column)){
                    anonymizeColumn = true;
                    hierarchy = quasiIdentifiers.get(column);
                    level = (qids.length > 1) ? transformation[count] : transformation[whichQid];
                    
                    count++;
                }
                else{
                    continue;
                }
                
                if(colNamesType.get(column).contains("int")){
                    
                    data[j] = dataSet[line][column];
                    if(anonymizeColumn && level > 0){
                        data[j] = anonymizeValue(data[j], hierarchy, level);
                    }
                    
                    if(data[j] instanceof Double){
                        data[j] = ((Double)data[j]).intValue();
                    }
                    j++;
                }
                else if(colNamesType.get(column).contains("double")){
                    
                    data[j] = (double)dataSet[line][column];
                    if(anonymizeColumn && level > 0){
                        data[j] = anonymizeValue(data[j], hierarchy, level);
                    }
                    j++;
                }
                else{
                    DictionaryString dictionary = dictionaries.get(column);
                    
                    data[j] = dictionary.getIdToString((int)dataSet[line][column]);
                    if(anonymizeColumn && level > 0){
                        data[j] = anonymizeValue(data[j], hierarchy, level);
                    }
                    j++;
                }
            }
            //////////////////////////////////important
            //if values are not suppressed add to statistics
            if(!isSuppressed(data, whichQid)){
                //statistics.add(data);
                Object data1[] = new Object[1];
                data1[0] = data[whichQid];
                statistics.add(data1);
            }
            
        }
        
        
        /*System.out.println("header = " + header  + "\t statistics = " + statistics);
        for (Map.Entry<SolutionAnonValues, Integer> entry : statistics.details.entrySet()) {
            System.out.println("key = " + entry.getKey() +"\tvalue = " + entry.getValue());
        }*/
        
        this.solutionStatistics.put(header, statistics);
    }
    
    
    
    
    private void anonymizeTuples(int[] transformation, int[] qids, int whichQid) throws ParseException{
//        System.out.println("\ntransf : " + Arrays.toString(transformation) + " qids : " + Arrays.toString(qids));
        double [][]dataSet = dataset.getDataSet();
        int length = dataSet.length;
        Map <Integer,String> colNamesType = null;
        Map <Integer,String> colNamesPosition = null;
        Map <Integer,DictionaryString> dictionaries = null;
        String columnName = null;
        SolutionHeader header;
        SolutionStatistics statistics;
        
        
        colNamesType = dataset.getColNamesType();
        colNamesPosition = dataset.getColNamesPosition();
        dictionaries = dataset.getDictionary();
        
        //System.out.println("whichQiddddddddddddddddddddd = " + whichQid);
        

//        Map<String, Double> tempFreq = new HashMap<>();
        header = new SolutionHeader(qids, transformation, this.dataset);
        statistics = new SolutionStatistics(this.dataset.getDataLenght(),colNamesType);

        
        for(int line=0; line<length; line++){
            int count = 0;
            int j = 0;
            
            Object data[] = new Object[qids.length];
            
            for(int column=0; column<dataSet[0].length; column++){
                
                //System.out.println("dataSet[0].length = " + dataSet[0].length + "\tcolumn = " +column);
                //System.out.println(" dataSet[line][column] = " +  dataSet[line][column]);
                        
                columnName = colNamesPosition.get(column);
                boolean anonymizeColumn = false;
                Hierarchy hierarchy = null;
                int level = 0;
                
                if((count < qids.length) && (qids[count] == column)){
                    anonymizeColumn = true;
                    hierarchy = quasiIdentifiers.get(column);
                    level = (qids.length > 1) ? transformation[count] : transformation[whichQid];
                    
                    count++;
                }
                else{
                    continue;
                }
                
                if(colNamesType.get(column).contains("int")){
                    
                    data[j] = dataSet[line][column];
                    if(anonymizeColumn && level > 0){
                        data[j] = anonymizeValue(data[j], hierarchy, level);
                    }
                    
                    if(data[j] instanceof Double){
                        data[j] = ((Double)data[j]).intValue();
                    }
                    j++;
                }
                else if(colNamesType.get(column).contains("double")){
                    
                    data[j] = (double)dataSet[line][column];
                    if(anonymizeColumn && level > 0){
                        data[j] = anonymizeValue(data[j], hierarchy, level);
                    }
                    j++;
                }
                else{
                    DictionaryString dictionary = dictionaries.get(column);
                    
                    data[j] = dictionary.getIdToString((int)dataSet[line][column]);
                    if(anonymizeColumn && level > 0){
                        data[j] = anonymizeValue(data[j], hierarchy, level);
                    }
                    j++;
                }
            }
            //////////////////////////////////important
            //if values are not suppressed add to statistics
            if(!isSuppressed(data, whichQid)){
                statistics.add(data);
            }
            
        }
        
        // System.out.println("header = " + header  + "\t statistics = " + statistics);
        /*System.out.println("header = " + header  + "\t statistics = " + statistics);
        for (Map.Entry<SolutionAnonValues, Integer> entry : statistics.details.entrySet()) {
            System.out.println("key = " + entry.getKey() +"\tvalue = " + entry.getValue());
        }*/
        
        this.solutionStatistics.put(header, statistics);
    }
    
    
    
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
                                //System.out.println(" i am hereeeeeeeee anonymized value");
                                anonymizedValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
                            }
                        }
                        else{
                             //System.out.println("valueeeeeeeeeeeeeeeee = " +  anonymizedValue);
                            anonymizedValue = h.getParent((Double)anonymizedValue);
                            //System.out.println("afterrrrrrrrrrrrrrrrrrrrrr = " +  anonymizedValue);
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
                                if ( d.equals(Double.NaN)){
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
                    //System.out.println("value = " + value + "\t i = " + i + "\tlevel = " + level);
                    if ( level == 0 ){
                        if ( (double) value == 2147483646.0 ||  value.equals(Double.NaN)){
                            return "(null)";
                        }
                        else{
                            anonymizedValue = h.getParent(anonymizedValue);
                        }
                    }
                    else{
                        anonymizedValue = h.getParent(anonymizedValue);
                    }
                }
                else if (value instanceof String){
                    if ( level == 0 ){
                        if ( ((String)value).equals("NaN")){
                            return "(null)";
                        }
                        else{
                            anonymizedValue = h.getParent(anonymizedValue);
                        }
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
                
                int translateLevel = h.translateDateViaLevel(h.getHeight()-level);
                anonymizedValue = rd.dateToString(translateLevel);

            }
        }
        
        
        return anonymizedValue;
    }
    
    public Date getDateFromString(String tmstmp) throws ParseException{
        Date d = null;
        SimpleDateFormat sf = null;
        
        sf = new SimpleDateFormat("dd/MM/yyyy");
        d = sf.parse(tmstmp);
                   
        return d;
    }
    
    
    private void print() {
        
        System.out.println("printttttttttttttttt");
        
        for(SolutionHeader header : this.solutionStatistics.keySet()){

            SolutionStatistics stats = this.solutionStatistics.get(header);
            
            //if there are too many distinct values, do not plot this chart


            stats.sort();
            
            //System.out.println("stats = " + stats.getKeyset());
            
            
            Set<String> setAnon = new HashSet<>();
            
            int count = 0;
            //System.out.println("printtttttttttttttt2222222");
            for(SolutionAnonValues values : stats.getKeyset()){
                
               // System.out.println("printtttttttttttttt33333");
                
                //if support is less than k, values can be suppressed
                if(stats.getSupport(values) < 5){/////////////////////////////// edw anti gia 5 to k
                    count += stats.getSupport(values);
                    setAnon.add(values.toString());
                }
                
            
                    SolutionAnonValues anonValues = values.putNUllValues();
                    //data.addValue(stats.getPercentage(values), values, header);
                    //data.addValue(stats.getPercentage(values), anonValues, header);
                    //System.out.println("Statts = " + stats.getPercentage(values) + "/t values = " + anonValues +  "\t header = " +header );

            }
            
            //System.out.println("printtttttttttttttt44444");
            
            //store counter and set of non-anonymized values
            if(count != 0){
                this.nonAnonymizedCount.put(header, count);
                nonAnonymousValues.put(header, setAnon);
            }
        }
        //return data;
    }
    
    /*public void updateChart(Map<SolutionHeader, SolutionStatistics> updatededStatistics){
        this.statistics = updatededStatistics;
        this.nonAnonymizedCount.clear();
        this.nonAnonymousValues.clear();
        remove(suppressPanel);
        initializeSuppressPanel();
        this.messageLabel.setText(null);
        this.messageLabel.repaint();
        this.drawCharts();
    }*/
    
    
    
    /*private Object anonymizeValue(Object value, Hierarchy h, int level){
        
        Object anonymizedValue = value;
        
        for(int i=0; i<level; i++){
            if(h.getHierarchyType().equals("range") && i==0){
                anonymizedValue = h.getParent((Double)anonymizedValue);
            }
            else{
                anonymizedValue = h.getParent(anonymizedValue);
            }
        }
        
        return anonymizedValue;
    }*/

    class ValueComparator implements Comparator {
        
        Map map;
        
        public ValueComparator(Map map) {
            this.map = map;
        }
        
        @Override
        public int compare(Object keyA, Object keyB) {
            Comparable valueA = (Comparable) map.get(keyA);
            Comparable valueB = (Comparable) map.get(keyB);
            return valueB.compareTo(valueA);
        }
    }
    
    private boolean isSuppressed(Object[] data, int whichQid){
        
        if(data.length == 1){
            Set<String> suppressed = this.toSuppress.get(qids[whichQid]);
            if(suppressed != null && suppressed.contains(Arrays.toString(data))){
                return true;
            }
        }
        else{
            Object[] checkArr = new Object[1];
            
            //check for each and every qid if is suppressed
            for(int i=0; i<qids.length; i++){
                Set<String> suppressed = this.toSuppress.get(qids[i]);
                if(suppressed != null){
                    checkArr[0] = data[i];
                    if(suppressed.contains(Arrays.toString(checkArr)))
                        return true;
                }
            }
            
            //check for all qids combined
            Set<String> suppressed = this.toSuppress.get(-1);
            if(suppressed != null){
                if(suppressed.contains(Arrays.toString(data))){
                    return true;
                }
            }
        }
        return false;
    }
    
}
