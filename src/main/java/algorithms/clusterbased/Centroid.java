/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import anonymizeddataset.AnonymizedDataset;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.HierarchyImplRangesDate;
import hierarchy.ranges.HierarchyImplRangesNumbers;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author nikos
 */
public class Centroid {
    private int clusterId;
    private Map<Integer,Hierarchy> quasiHiers = null;
    private Map<Integer,Pair<String,Object>> centroidValues;
    private Map<Integer,Long> dateValues;
    
    
    public Centroid(int cluster, Map<Integer,Hierarchy> qh,Double[] values, boolean recWithId){
        this.clusterId = cluster;
        this.quasiHiers = qh;
        this.centroidValues = new HashMap();
        this.dateValues = new HashMap();
        for(Entry<Integer,Hierarchy> entry : this.quasiHiers.entrySet()){
            Pair<String,Object> centroidValue = new Pair(entry.getValue().getNodesType(),values[recWithId ? entry.getKey()+1 : entry.getKey()]);
            this.centroidValues.put(entry.getKey(), centroidValue);
            
            if(entry.getValue().getNodesType().equals("date")){
                if(((Double)centroidValue.getValue()).equals(2147483646.0)){
                    System.out.println("Initial "+Arrays.toString(values));
                    System.out.println("Edw mpainei giati etsi");
                    dateValues.put(entry.getKey(), 0L);
                    
                }
                else{
                    dateValues.put(entry.getKey(), ((Double)centroidValue.getValue()).longValue());
                }
            }
            
        }
    }
    
    public Centroid(Centroid c){
        this.quasiHiers = c.quasiHiers;
        this.centroidValues = new HashMap(c.centroidValues);
        this.dateValues = new HashMap(c.dateValues);
    }
    
    public void simpleChange(){
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            if(entry.getValue().getKey().equals("int") || entry.getValue().getKey().equals("double")){
                entry.setValue(new Pair(entry.getValue().getKey(),((Double)entry.getValue().getValue())+1));
            }
        }
    }
    
    
    public void print(){
        System.out.println("ClusterID "+this.clusterId);
        
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            System.out.println("Column "+entry.getKey()+" Type "+entry.getValue().getKey()+" Value "+entry.getValue().getValue());
        }
    }
    
    public void update(Double[] record, boolean withRecId){
        
        try{
            Map<Integer,Pair<String,Object>> updated = new HashMap();
            for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
                int column = entry.getKey();
                Object centroidValue = null;
                Object newValue = null ; 
                Hierarchy hier = this.quasiHiers.get(column);
                column = withRecId ? column+1 : column;
//                if(record[column] != 2147483646.0){
                if(hier.getHierarchyType().equals("distinct")){
                    centroidValue = (Double)entry.getValue().getValue();
                    newValue = hier.findCommon(record[column], (Double)centroidValue);
                }
                else{
                    if(hier.getNodesType().equals("date")){
                        centroidValue = entry.getValue().getValue();
                        Date dateCentroid=null,dateRec=null;
                        if(this.dateValues.get(entry.getKey()) == 0){
                            dateCentroid = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound;
                            System.out.println("Rec update centr "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound+" date rec "+new Date(record[column].longValue()));
                        }
                        else{
                            dateCentroid = new Date(this.dateValues.get(entry.getKey()));
                        }
                        if(record[column].equals(2147483646.0)){
                            dateRec = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound;
                            System.out.println("Rec update record "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound+" date Cent "+dateCentroid);

                        }
                        else{
                            dateRec = new Date(record[column].longValue());
                        }
                        if(dateCentroid!=null && dateRec!=null){
                            long timeCentroid = this.dateValues.get(entry.getKey());
                            if(timeCentroid == 0){
                                timeCentroid = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound.getTime();

                            }
                            long timeRec = dateRec.getTime();
                            if(timeRec == 0){
                                timeRec = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound.getTime();

                            }
                            if(centroidValue instanceof Double){
                               newValue = ((HierarchyImplRangesDate)hier).findCommonRange(dateCentroid, dateRec);
                            }
                            else{
                                if(((RangeDate)centroidValue).contains(dateRec)){
                                    newValue = centroidValue;
                                }
                                else{
                                    newValue = ((HierarchyImplRangesDate)hier).findCommonRange((RangeDate)centroidValue, dateRec);
                                }

                            }
                            this.dateValues.put(entry.getKey(), (timeCentroid+timeRec)/2);
                        }
                        else{
                            newValue = centroidValue;
                        }
                    }
                    else{
                        centroidValue = (Double)entry.getValue().getValue();
                        Double rec;
                        if(((Double)centroidValue).equals(2147483646.0)){
                            centroidValue = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                        }

                        if(record[column].equals(2147483646.0)){
                            rec = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                        }
                        else{
                            rec = record[column];
                        }
                        newValue = (rec + ((Double)centroidValue))/2;
                    }
                }
//                }
//                else{
//                   newValue = centroidValue; 
//                }

                if(newValue!=null)
                    updated.put(withRecId ? column-1 : column, new Pair(entry.getValue().getKey(),newValue));
                else
                    updated.put(withRecId ? column-1 : column, entry.getValue());
            }

            if(!updated.isEmpty())
                this.centroidValues = updated;
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error centr update "+e.getMessage());
        }
    }
    
    public void update(Centroid c){
        Map<Integer,Pair<String,Object>> updated = new HashMap();
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            int column = entry.getKey();
            Object newValue =null; 
            Hierarchy hier = this.quasiHiers.get(column);
            if(hier.getHierarchyType().equals("distinct")){
                Double centroidValue = (Double)entry.getValue().getValue();
                Double centroidValue2 =(Double) c.centroidValues.get(entry.getKey()).getValue();
                newValue = hier.findCommon(centroidValue2, centroidValue);
            }
            else{
                if(hier.getNodesType().equals("date")){
                    Object centroidValue = entry.getValue().getValue();
                    Object centroidValue2 = c.centroidValues.get(entry.getKey()).getValue();                  
                    Date dateCentroid=null,dateRec=null;
                    dateCentroid = new Date(this.dateValues.get(column));
                    dateRec = new Date(c.dateValues.get(column));
                    
                    if(this.dateValues.get(column) == 0){
                        dateCentroid = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound;
                        System.out.println("Centr update centroid "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound+" date rec "+new Date(c.dateValues.get(column)));
                    }
                    else{
                        dateCentroid = new Date(this.dateValues.get(column));
                    }
                    if(c.dateValues.get(column) == 0){
                        dateRec = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound;
                         System.out.println("Centr update record "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound+" date centr "+dateCentroid);
                    }
                    else{
                        dateRec = new Date(c.dateValues.get(column));
                    }
                    
                    if(dateCentroid!=null && dateRec!=null){
                        long timeCentroid = this.dateValues.get(column);
                        if(timeCentroid == 0){
                            timeCentroid = dateCentroid.getTime();
                            
                        }
                        long timeRec = c.dateValues.get(column); 
                        if(timeRec == 0){
                            timeRec = dateRec.getTime();
                           
                            
                        }
                        this.dateValues.put(column, (timeCentroid+timeRec)/2);
                        if(centroidValue instanceof Double && centroidValue2 instanceof Double){
                           newValue = ((HierarchyImplRangesDate)hier).findCommonRange(dateCentroid,dateRec);
                        }
                        else if(centroidValue instanceof Double){
                            if(((RangeDate)centroidValue2).contains(dateCentroid)){
                                newValue = centroidValue2;
                            }
                            else{
                                newValue = ((HierarchyImplRangesDate)hier).findCommonRange(dateCentroid, (RangeDate)centroidValue2);
                            }
                        }
                        else if(centroidValue2 instanceof Double){
                            if(((RangeDate)centroidValue).contains(dateRec)){
                                newValue = centroidValue;
                            }
                            else{
                                newValue = ((HierarchyImplRangesDate)hier).findCommonRange(dateRec, (RangeDate)centroidValue);
                            }
                        }
                        else{
                            newValue = ((HierarchyImplRangesDate)hier).findCommonRange((RangeDate)centroidValue,(RangeDate)centroidValue2);
                        }                        
                    }
                    else{
                        newValue = centroidValue;
                    }
                }
                else{
                    
                    Double centroidValue = (Double) entry.getValue().getValue();
                    Double centroidValue2 =(Double) c.centroidValues.get(entry.getKey()).getValue();
                    if(((Double)centroidValue).equals(2147483646.0)){
                        centroidValue = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                    }
                    
                    if(((Double)centroidValue2).equals(2147483646.0)){
                        centroidValue2 = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                    }
                        
                       
                    newValue = (centroidValue2 + centroidValue)/2;
                }
            }
            
            if(newValue!=null)
                updated.put(column, new Pair(entry.getValue().getKey(),newValue));
            else
                updated.put(column, entry.getValue());
        }
        
        if(!updated.isEmpty())
            this.centroidValues = updated;
    }
    
    public double computeDistance(Double record[], boolean recordWithId){
        double distance = 0;
        int datesVals = 0;
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            int column = recordWithId ? entry.getKey()+1 : entry.getKey();
            Hierarchy hier = this.quasiHiers.get(entry.getKey());
            Object centroidValue = entry.getValue().getValue();
            
            if(hier.getHierarchyType().equals("distinct")){

                distance += (double)hier.findCommonHeight((Double)centroidValue,record[column])/(hier.getHeight()-1);
            }
            else {
                if(hier.getNodesType().equals("date")){
                    long timeCentroid =this.dateValues.get(entry.getKey());
                    if(0 == timeCentroid){
                        timeCentroid = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound.getTime();
                        System.out.println("Rec distance centroid "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound);
                    }
                    long timeRec = record[column].longValue();
                    if(record[column].equals(2147483646.0)){
                        timeRec = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound.getTime();
                        System.out.println("Rec distance record "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound);
                    }
//                        System.out.println("REcord "+Arrays.toString(record));
                    long diff  = Math.abs(timeRec - timeCentroid);

                    RangeDate root = (RangeDate) hier.getRoot();

                    long upperTime = root.getUpperBound().getTime();
                    long lowerTime = root.getLowerBound().getTime();
                    distance += (double)diff /(Math.abs(upperTime - lowerTime));


                }
                else{
                    RangeDouble root = (RangeDouble) hier.getRoot();
                    Double diff,rec;
                    if(((Double)centroidValue).equals(2147483646.0)){
                        centroidValue = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                    }

                    if(record[column].equals(2147483646.0)){
                        rec = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                    }
                    else{
                        rec = record[column];
                    }

                    if((Double)centroidValue > rec){
                       diff = ((Double)centroidValue) - rec;
                    }
                    else{
                       diff = rec - ((Double)centroidValue);
                    }

                    distance += (double)diff / (root.upperBound-root.lowerBound);
                }
            }
            
        }
        
        return distance;
    }
    
    public double computeDistance(Centroid c){
        double distance = 0;
        
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            Hierarchy hier = this.quasiHiers.get(entry.getKey());
            Object centroidValue = entry.getValue().getValue();
            Object centroidValue2 = c.centroidValues.get(entry.getKey()).getValue();
            if(hier.getHierarchyType().equals("distinct")){
                
                distance += (double)hier.findCommonHeight((Double)centroidValue,(Double)centroidValue2)/(hier.getHeight()-1);
            }
            else {
                if(hier.getNodesType().equals("date")){

                    long timeCentroid = this.dateValues.get(entry.getKey());
                    if(0 == timeCentroid){
                        timeCentroid = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound.getTime();
                        System.out.println("Centr distance centrroid "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound);
                    }
                    long timeRec = c.dateValues.get(entry.getKey());
                    if(0 == timeRec){
                        timeCentroid = ((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound.getTime();
                        System.out.println("Centr distance record "+((HierarchyImplRangesDate)hier).getParent(new RangeDate(null,null)).lowerBound);
                    }
                    long diff = Math.abs(timeCentroid - timeRec);;

                    RangeDate root = (RangeDate) hier.getRoot();

                    long upperTime = root.getUpperBound().getTime();
                    long lowerTime = root.getLowerBound().getTime();
                    distance += (double)diff /(Math.abs(upperTime - lowerTime));
                    
                }
                else{
                    RangeDouble root = (RangeDouble) hier.getRoot();
                    Double diff;
                    
                    if(((Double)centroidValue).equals(2147483646.0)){
                        centroidValue = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                    }

                    if(((Double)centroidValue2).equals(2147483646.0)){
                        centroidValue2 = ((HierarchyImplRangesNumbers)hier).getParent(2147483646.0).lowerBound;
                    }

                    if((Double)centroidValue > (Double)centroidValue2){
                       diff = ((Double)centroidValue) - ((Double)centroidValue2);
                    }
                    else{
                       diff = ((Double)centroidValue2) - ((Double)centroidValue);
                    }
                    
                    distance += (double)diff / (root.upperBound-root.lowerBound);
                }
            }
        }
        
        return distance;
    }
    
    
    public static String dateToString(Date d){
        
        Calendar calendar = Calendar.getInstance() ;
        calendar.setTime(d);
        
        return calendar.get(Calendar.DAY_OF_MONTH) + "/" + ( calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
        
    }
    
    
    public int getClusterId(){
        return this.clusterId;
    }
    
    public Double[][] anonymize(Double[][] records){
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            Hierarchy hier = this.quasiHiers.get(entry.getKey());
            
            if(hier.getHierarchyType().equals("distinct")){
                for(int i=0; i<records.length; i++){
                   records[i][entry.getKey()+1] = (Double)entry.getValue().getValue();
                } 
            }
            else{
                if(hier.getNodesType().equals("date")){
                    HierarchyImplRangesDate h = (HierarchyImplRangesDate)  hier;
                        String strCommon;
                        strCommon = ((RangeDate)entry.getValue().getValue()).dateToString();
                        DictionaryString dict = h.getDictResults();
                        int idRange;
                        if(dict == null){
                            dict = new DictionaryString();
                            h.setDictionaryResults(dict);
                        }
                        synchronized(dict){
                            
                            
                            if(dict.containsString(strCommon)){
                                idRange = dict.getStringToId(strCommon);
                            }
                            else{
                                idRange = dict.getMaxUsedId()+1;
                                dict.putIdToString(idRange, strCommon);
                                dict.putStringToId(strCommon, idRange);
                            }
                        }
                        for(int i=0; i<records.length; i++){
                            records[i][entry.getKey()+1] = (double)idRange;
                        }
                }
                else{
                   HierarchyImplRangesNumbers h =  (HierarchyImplRangesNumbers) hier;
                   RangeDouble commonParent = null;
                   commonParent = h.findCommonRange(records[0][entry.getKey()+1], records[1][entry.getKey()+1]);
                   int recCount = 2;
                   
                    while(recCount < records.length && commonParent == null){
                       commonParent = h.findCommonRange(records[recCount-1][entry.getKey()+1], records[recCount][entry.getKey()+1]);
                       recCount++;
                    }
                    
                    if(commonParent != null){
                        while(recCount < records.length){
                            commonParent = h.findCommonRange(commonParent, records[recCount][entry.getKey()+1]);
                            recCount++;
                        }
                        DictionaryString dict = h.getDictionary();
                        int idRange;
                        if(dict == null){
                            dict = new DictionaryString();
                            h.setDictionaryData(dict);
                        }
                        synchronized(dict){
                        
                            String strCommon = commonParent.toString();

                            if(dict.containsString(strCommon)){
                                idRange = dict.getStringToId(strCommon);
                            }
                            else{
                                idRange = dict.getMaxUsedId() + 1;
                                dict.putIdToString(idRange, strCommon);
                                dict.putStringToId(strCommon, idRange);
                            }
                        }
                        for(int i=0; i<records.length; i++){
                            records[i][entry.getKey()+1] = (double)idRange;
                        }
                    }
                    else{
                        DictionaryString dict = h.getDictionary();
                        int idRange;
                        if(dict == null){
                            dict = new DictionaryString();
                            h.setDictionaryData(dict);
                        }
                        synchronized(dict){
                            String strCommon = ""+(entry.getValue().getKey().equals("int") ? records[0][entry.getKey()+1].intValue() : records[0][entry.getKey()+1]);


                            if(dict.containsString(strCommon)){
                                idRange = dict.getStringToId(strCommon);
                            }
                            else{
                                idRange = dict.getMaxUsedId() + 1;
                                dict.putIdToString(idRange, strCommon);
                                dict.putStringToId(strCommon, idRange);
                            }
                        }
                        
                        for(int i=0; i<records.length; i++){
                            records[i][entry.getKey()+1] = (double)idRange;
                        }
                    }
                }
            }
        }
        
        return records;
    }
    
    
}
