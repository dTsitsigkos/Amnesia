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
//    private static Map<Integer,String> localDatesDict;
    
    
    public Centroid(int cluster, Map<Integer,Hierarchy> qh,Double[] values, boolean recWithId){
        this.clusterId = cluster;
        this.quasiHiers = qh;
        this.centroidValues = new HashMap();
        this.dateValues = new HashMap();
//        if(localDatesDict==null){
//            localDatesDict = new TreeMap();
//        }
        for(Entry<Integer,Hierarchy> entry : this.quasiHiers.entrySet()){
            Pair<String,Object> centroidValue = new Pair(entry.getValue().getNodesType(),values[recWithId ? entry.getKey()+1 : entry.getKey()]);
            this.centroidValues.put(entry.getKey(), centroidValue);
            if(entry.getValue().getNodesType().equals("date")){
                
//                    String date=entry.getValue().getDictionary().getIdToString(((Double)centroidValue.getValue()).intValue());
//                    localDatesDict.put(((Double)centroidValue.getValue()).intValue(),date);
                    dateValues.put(entry.getKey(), ((Double)centroidValue.getValue()).longValue());
               
            }
            
        }
    }
    
    public Centroid(Centroid c){
        this.clusterId = Integer.parseInt("333"+c.clusterId);
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
        
        int datesVals=0;
        try{
        Map<Integer,Pair<String,Object>> updated = new HashMap();
        for(Entry<Integer,Pair<String,Object>> entry : this.centroidValues.entrySet()){
            int column = entry.getKey();
            Object centroidValue = null;
            Object newValue = null ; 
            Hierarchy hier = this.quasiHiers.get(column);
            column = withRecId ? column+1 : column;
            if(record[column] != 2147483646.0){
                if(hier.getHierarchyType().equals("distinct")){
                    centroidValue = (Double)entry.getValue().getValue();
                    newValue = hier.findCommon(record[column], (Double)centroidValue);
                }
                else{
                    if(hier.getNodesType().equals("date")){
                        centroidValue = entry.getValue().getValue();
//                        BigInteger total = BigInteger.ZERO;
//                        String strDateCentroid = hier.getDictionary().getIdToString(centroidValue.intValue());
//                        String strDateRec=null;
//                        if(originalDates!=null){
//                            strDateRec = originalDates[datesVals];
//                        }
//                        datesVals++;
//                        if(strDateRec==null){
//                            strDateRec = hier.getDictionary().getIdToString(record[column].intValue());
//                        }
    //                    System.out.println("Centroid "+strDateCentroid+" "+strDateRec+" column "+column+" record size "+record.length+" recId "+withRecId);

                        Date dateCentroid=null,dateRec=null;
                        dateCentroid = new Date(this.dateValues.get(entry.getKey()));
                        dateRec = new Date(record[column].longValue());
//                        try {
//                            dateCentroid = this.dateValues.get(entry.getKey());
//                            dateRec = AnonymizedDataset.getDateFromString(strDateRec==null ? hier.getDictionary().getIdToString().get(record[column].intValue()) : strDateRec);
//                        } catch (ParseException ex) {
//                            System.err.println("Error: parseDate "+strDateRec+" "+ex.getMessage());
//                            ex.printStackTrace();
//                            Logger.getLogger(Centroid.class.getName()).log(Level.SEVERE, null, ex);
//                        } 

                        if(dateCentroid!=null && dateRec!=null){
                            long timeCentroid = this.dateValues.get(entry.getKey());
                            long timeRec = dateRec.getTime();

//                            total = total.add(BigInteger.valueOf(timeCentroid));
//                            total = total.add(BigInteger.valueOf(timeRec));
//
//                            BigInteger averageMillis = total.divide(BigInteger.valueOf(2));
//                            Date averageDate = new Date((timeCentroid+timeRec)/2);
                            
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
                            
//                            synchronized(localDatesDict){
//                                localDatesDict.remove(record[column].intValue());
//                            }
                            
    //                        System.out.println("milisec "+averageMillis);
    //                        System.out.println("New avg pure date "+averageDate.toString());
//                            String strDate = this.dateToString(averageDate);
    //                        System.out.println("New avg date "+strDate);
//                               System.out.println("ClusterID "+this.clusterId+" newValue "+newValue);
//                            if(s!=null){
//                                s.acquire();
//                            }
//                            DictionaryString dictHier = hier.getDictionary();
//                            int newDateId = dictHier.getMaxUsedId()+1;
//
//                            if(!dictHier.containsString(strDate)){
//                                dictHier.putIdToString(newDateId, strDate);
//                                dictHier.putStringToId(strDate, newDateId);
//                            }
//                            else{
//                                newDateId = dictHier.getStringToId(strDate);
//                            }
//
//                            if(s!=null){
//                                s.release();
//                            }
                            
                            this.dateValues.put(entry.getKey(), (timeCentroid+timeRec)/2);

                           
                        }
                        else{
                            newValue = centroidValue;
                        }
                    }
                    else{
                        centroidValue = (Double)entry.getValue().getValue();
                        newValue = (record[column] + ((Double)centroidValue))/2;
                    }
                }
            }
            else{
               newValue = centroidValue; 
            }
            
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
//            Double centroidValue = entry.getValue().getValue();
//            Double centroidValue2 = c.centroidValues.get(entry.getKey()).getValue();
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
//                    BigInteger total = BigInteger.ZERO;
//                    String strDateCentroid = hier.getDictionary().getIdToString(centroidValue.intValue());
//                    String strDateRec = hier.getDictionary().getIdToString(centroidValue2.intValue());
//                    System.out.println("Centroid "+strDateCentroid+" "+strDateRec+" column "+column+" record size "+record.length+" recId "+withRecId);
                    
                    Date dateCentroid=null,dateRec=null;
                    dateCentroid = new Date(this.dateValues.get(column));
                    dateRec = new Date(c.dateValues.get(column));
                    
                    if(dateCentroid!=null && dateRec!=null){
                        long timeCentroid = this.dateValues.get(column);
                        long timeRec = c.dateValues.get(column);
                        
//                        total = total.add(BigInteger.valueOf(timeCentroid));
//                        total = total.add(BigInteger.valueOf(timeRec));
//                        
//                        BigInteger averageMillis = total.divide(BigInteger.valueOf(2));
//                        Date averageDate = new Date((timeCentroid+timeRec)/2);
//                        System.out.println("milisec "+averageMillis);
//                        System.out.println("New avg pure date "+averageDate.toString());
                        this.dateValues.put(column, (timeCentroid+timeRec)/2);
//                        String strDate = this.dateToString(averageDate);
//                        System.out.println("New avg date "+strDate);
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
                            
//                        DictionaryString dictHier = hier.getDictionary();
//                        int newDateId = dictHier.getMaxUsedId()+1;
//                        System.out.println("ClusterID "+this.clusterId+" newValue Centr "+newValue);
//                        if(!dictHier.containsString(strDate)){
//                            dictHier.putIdToString(newDateId, strDate);
//                            dictHier.putStringToId(strDate, newDateId);
//                        }
//                        else{
//                            newDateId = dictHier.getStringToId(strDate);
//                        }
//                        
//                        newValue = (double) newDateId;
                    }
                    else{
                        newValue = centroidValue;
                    }
                }
                else{
                    
                     Double centroidValue = (Double) entry.getValue().getValue();
                     Double centroidValue2 =(Double) c.centroidValues.get(entry.getKey()).getValue();
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
            if(!record[column].equals(2147483646.0)){
                if(hier.getHierarchyType().equals("distinct")){

                    distance += (double)hier.findCommonHeight((Double)centroidValue,record[column])/(hier.getHeight()-1);
                }
                else {
                    if(hier.getNodesType().equals("date")){
    //                    System.out.println("date centroid "+centroidValue);
    //                    System.out.print("Range Date ");
    //                    ((RangeDate)hier.getRoot()).print();
//                        String strDateCentroid = hier.getDictionary().getIdToString(centroidValue.intValue());
//                        String strDateRec=null;
//                        if(originalDates!=null){
//                            strDateRec = originalDates[datesVals];
//                        }
//                        datesVals++;
//                        if(strDateRec==null){
//                            strDateRec = hier.getDictionary().getIdToString(record[column].intValue());
//                            synchronized(localDatesDict){
//                                localDatesDict.put(record[column].intValue(),strDateRec);
//                            }
//                        }

    //                    System.out.println("Dist Centroid "+strDateCentroid+" "+strDateRec+" column "+column+" record size "+record.length+" recId "+recordWithId);

                        Date dateCentroid=null,dateRec=null;
                        dateCentroid = new Date(this.dateValues.get(entry.getKey()));
                        dateRec = new Date(record[column].longValue());
                       
//                        System.out.println("DAte centroid "+dateCentroid.toString());
                        
                        long timeCentroid =this.dateValues.get(entry.getKey());
                        long timeRec = record[column].longValue();

//                        System.out.println("Time centroid "+timeCentroid+" time rec "+timeRec);
                        long diff  = Math.abs(timeRec - timeCentroid);



//                        System.out.println("Diff "+diff);
//                        if(timeCentroid > timeRec){
//                            diff = timeCentroid - timeRec;
//                        }
//                        else{
//                            diff = timeRec - timeCentroid;
//                        }

                        RangeDate root = (RangeDate) hier.getRoot();

                        long upperTime = root.getUpperBound().getTime();
                        long lowerTime = root.getLowerBound().getTime();
                        distance += (double)diff /(Math.abs(upperTime - lowerTime));
                        

                    }
                    else{
                        RangeDouble root = (RangeDouble) hier.getRoot();
                        Double diff;

                        if((Double)centroidValue > record[column]){
                           diff = ((Double)centroidValue) - record[column];
                        }
                        else{
                           diff = record[column] - ((Double)centroidValue);
                        }

                        distance += (double)diff / (root.upperBound-root.lowerBound);
                    }
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
//                    System.out.println("date centroid "+centroidValue);
//                    System.out.print("Range Date ");
//                    ((RangeDate)hier.getRoot()).print();
//                    String strDateCentroid = hier.getDictionary().getIdToString(centroidValue.intValue());
//                    String strDateRec = hier.getDictionary().getIdToString(centroidValue2.intValue());
                    
//                    System.out.println("Dist Centroid "+strDateCentroid+" "+strDateRec+" column "+column+" record size "+record.length+" recId "+recordWithId);
                    
                    Date dateCentroid=null,dateRec=null;
                    dateCentroid = new Date(this.dateValues.get(entry.getKey()));
                    dateRec = new Date(c.dateValues.get(entry.getKey()));
                    
                    
//                    if(dateCentroid!=null && dateRec!=null){
                    long timeCentroid = this.dateValues.get(entry.getKey());
                    long timeRec = c.dateValues.get(entry.getKey());

//                        System.out.println("Time centroid "+timeCentroid+" time rec "+timeRec);
                    long diff = Math.abs(timeCentroid - timeRec);;

//                        if(dateCentroid.after(dateRec)){
//                            diff  = Math.abs(timeCentroid - timeRec);
//                        }
//                        else{
//                            diff = Math.abs(timeRec - timeCentroid);
//                        }

//                        System.out.println("Diff "+diff);
//                        if(timeCentroid > timeRec){
//                            diff = timeCentroid - timeRec;
//                        }
//                        else{
//                            diff = timeRec - timeCentroid;
//                        }

                    RangeDate root = (RangeDate) hier.getRoot();

                    long upperTime = root.getUpperBound().getTime();
                    long lowerTime = root.getLowerBound().getTime();
                    distance += (double)diff /(Math.abs(upperTime - lowerTime));
//                    }
                    
                }
                else{
                    RangeDouble root = (RangeDouble) hier.getRoot();
                    Double diff;

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
//                System.out.println("Distinct value "+entry.getValue().getValue());
                for(int i=0; i<records.length; i++){
                   records[i][entry.getKey()+1] = (Double)entry.getValue().getValue();
                } 
            }
            else{
                if(hier.getNodesType().equals("date")){
                    HierarchyImplRangesDate h = (HierarchyImplRangesDate)  hier;
//                    RangeDate commonParent = null;
////                    System.out.println("Print first records "+records[0][entry.getKey()+1]+" "+records[1][entry.getKey()+1]);
//                    commonParent = h.findCommonRange(localDatesDict.get(records[0][entry.getKey()+1].intValue()), localDatesDict.get(records[1][entry.getKey()+1].intValue()));
//                    int recCount = 2;
//                    while(recCount < records.length && commonParent== null){
//                        commonParent = h.findCommonRange(localDatesDict.get(records[recCount-1][entry.getKey()+1].intValue()), localDatesDict.get(records[recCount][entry.getKey()+1].intValue()));
//                        if(commonParent.equals(h.getRoot())){
//                            break;
//                        }
//                        recCount++;
//                    }
//                    
//                    
//                    if(commonParent != null){
//                        while(recCount < records.length){
//                            if(commonParent.equals(h.getRoot())){
//                                break;
//                            }
//                            try {
//                                if(commonParent.contains(localDatesDict.get(records[recCount][entry.getKey()+1].intValue()))){
//                                    recCount++;
//                                    continue;
//                                }
//                                else{
//                                    commonParent = h.findCommonRange(commonParent, localDatesDict.get(records[recCount][entry.getKey()+1].intValue()));
//                                }
//                            } catch (ParseException ex) {
//                                Logger.getLogger(Centroid.class.getName()).log(Level.SEVERE, null, ex);
//                                commonParent = h.findCommonRange(commonParent, localDatesDict.get(records[recCount][entry.getKey()+1].intValue()));
//                            }
//                            
//                            
//                            recCount++;
//                        }
//                        String strCommon = commonParent.dateToString(h.translateDateViaLevel(h.getHeight() - h.getLevel(commonParent)));
                        String strCommon;
                        strCommon = ((RangeDate)entry.getValue().getValue()).dateToString();
                        
//                        System.out.println("Cluster "+clusterId+" column RangeDate "+entry.getKey()+" value "+strCommon);
                        
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
//                    }
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
//                        System.out.println("Cluster "+clusterId+" column RangeDouble "+entry.getKey()+" value "+commonParent);
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
