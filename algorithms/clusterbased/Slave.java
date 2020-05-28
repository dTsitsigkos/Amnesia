/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import data.DiskData;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author nikos
 */
public class Slave extends Thread{
    private int slaveId;
    private static Semaphore semaphore,semaphoreSplit,semaphoreUpdate;
    private static Clusters clusters;
//    private static Map<Integer,Centroid> centroids; /// cluster -> centroid
    private static Centroid[] centroids;
    private DiskData data;
//    private Set<Integer> randomRecIds;
//    private static int nextRecord = 0;
    private static int caseRun = -1; // 0 fill clusters, 1 remove small clusters
    private static ArrayList<Integer> consumerClustersIds;
//    private static Map<Pair<Integer,Double[]>,Pair<Integer,Double>> producerMoves;
    private static removeValues[] producerMoves;
//    private static Map<Integer,String[]> datesValsProducer;
//    private static ArrayList<Integer> cloneSmallClusters;
    private static List<Integer> empty,fill;
    private Map<Integer,Hierarchy> quasi;
    private RangeDouble range;
    private static Double[][] records;
    
    
    private static Pair<Double[],List<Integer>>[] randomRecs;
    private Map<Integer,Hierarchy> hierarchies;
    private static AtomicInteger clusterIdCounter;
    
    private static int nextRemove;
    
    public Slave(int id, RangeDouble r,Map<Integer,Hierarchy> h){
        this.slaveId = id;
        this.range = r;
        this.hierarchies = h;
        if(this.slaveId == 1){
            semaphoreSplit = new Semaphore(1);
        }
    }
    
    public Slave(int id, Clusters cl,  Centroid[] c, DiskData d, Map<Integer,Hierarchy> q,RangeDouble r){
        this.slaveId = id;
        this.data = d;
//        this.randomRecIds = rri;
        this.range = r;
        
//        nextRecord = 0;
        this.quasi = q;
//        this.semaphores = s;
        
        
        if(this.slaveId == 1){
            this.clusters = cl;
            this.centroids = c;
            caseRun = -1;
            semaphore = new Semaphore(1);
            semaphoreSplit = new Semaphore(1);
            semaphoreUpdate = new Semaphore(1);
        }
        
        
        System.out.println("Thread "+this.slaveId+" constructed");
    }
    
    public Slave(int id, DiskData d,Map<Integer,Hierarchy> q,RangeDouble r){
        this.slaveId = id;
        this.data = d;
        this.quasi = q;
        this.range = r;

    }
    
    public Slave(int id, DiskData d,Map<Integer,Hierarchy> q){
        this.slaveId = id;
        this.data = d;
        this.quasi = q;

    }
    
//    public Slave(int id,DiskData d, Map<Integer,Hierarchy> q, int rep,RangeDouble range){
//        this.slaveId = id;
//        this.data = d;
//        this.quasi = q;
//        this.reputation = rep;
//        this.range = range;
//    }
    
    
    
    public static void setCase(int cr){
        caseRun = cr;
        
    }
    
    public RangeDouble getRange(){
        return this.range;
    }
    
    public static void setRandom(Centroid[] c, Pair<Double[],List<Integer>>[]  rRecs, Clusters cl){
        randomRecs = rRecs;
        centroids = c;
        clusters = cl;
        clusterIdCounter = new AtomicInteger(0);
    }
    
    public static void setRecords(Double[][] r){
        records = r;
        if(r==null){
            System.gc();
            Runtime.getRuntime().gc();
        }
    }
    
    public static void setConsumer(List<Integer> c){
        consumerClustersIds = new ArrayList<>(c);
//        cloneSmallClusters = (ArrayList<Integer>) consumerClustersIds.clone();
        empty = new ArrayList<Integer>();
        fill = new ArrayList<Integer>();
        producerMoves = new removeValues[c.size()*(clusters.getK()-1)];
//        datesValsProducer = new HashMap();
        nextRemove = 0;
    }
    
    
    public static void setCentroids(Centroid[] clCen){
        centroids = clCen;
    }
    
    
    public void test(){
        try{
            try {
//                this.semaphores[0].acquire();
                Thread.sleep(1000);
                System.out.println("Test "+this.slaveId);
                Centroid c = this.centroids[1];
                System.out.print("Before ");
                c.print();
                if(this.slaveId % 2 != 0){
                    c.simpleChange();
                }
                System.out.print("After ");
                c.print();
                
            } finally {
//               this.semaphores[0].release();
            }
        }catch (InterruptedException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void run(){
//        test();
        switch(caseRun){
            case 0:
                findDistance();
                break;
            case 1:
                removeClusters();
                break;
            case 2:
                fillClusters();
                break;
        }
    }
    
    public void fillClusters(){
//        int start = this.range.lowerBound.intValue();
//            
//        int end = this.range.upperBound.intValue() >= records.length ? records.length-1 : this.range.upperBound.intValue();
        int clusterId;
        System.out.println("Slave "+this.slaveId+" range "+range);
        for(int i=this.range.lowerBound.intValue(); i<=this.range.upperBound.intValue(); i++){
            if(randomRecs[i]!=null){
                List<Integer> recordsIds = randomRecs[i].getValue();
                
                clusterId = clusterIdCounter.incrementAndGet();
                for(Integer recordId : recordsIds)
                    this.clusters.put(clusterId,recordId,0.0,semaphoreSplit);
                Centroid centroid = new Centroid(clusterId,this.hierarchies,randomRecs[i].getKey(),true);
                centroids[clusterId] =  centroid;
                clusters.setSize(clusterId,recordsIds.size());
            }
        }
    }
    
    public void findDistance(){
//        Double[][] records;
        int recordId;
        int newCluster;
        Centroid cOld=null,cNew=null;
//        String[] datesVals = null;
//        int countDatesVal = 0;
        
        try{
//            semaphore.acquire();
//            this.nextRecord++;
//            while(this.randomRecIds.contains(this.nextRecord)){
//                this.nextRecord++;
//            }
//            records = this.data.getDataset(this.range.lowerBound.intValue()-1, this.range.upperBound.intValue());
//            records = this.data.getSpecificDataset(this.range.lowerBound.intValue()-1, this.range.upperBound.intValue());
//            records = recordsSplit.get(this.reputation);
            int start = this.range.lowerBound.intValue();
            
            int end = this.range.upperBound.intValue() >= records.length ? records.length-1 : this.range.upperBound.intValue();
//            for(Entry<Integer,Hierarchy> entryH : this.quasi.entrySet()){
//                if(entryH.getValue().getNodesType().equals("date")){
//                    countDatesVal++;
//                }
//            }

//            if(countDatesVal!=0){
//                datesVals = new String[countDatesVal];
//                countDatesVal=0;
//            }
            
            
//            System.out.println("Thread "+this.slaveId+" start "+start+" end "+end);
            for(int i=start; i<=end; i++){
//            for(Double[] record : records){
                
                Double[] record = records[i];
                if(record[0]!=null){
                    recordId = record[0].intValue();
//                    System.out.println("Thread "+this.slaveId+" record "+Arrays.toString(record)+" i="+i);
    //                if(this.randomRecIds.contains(recordId)){
    //                    continue;
    //                }
    //            while((record = this.data.getNextRecord(this.nextRecord)) != null){

    //                System.out.println("Record ID "+recordId+" threadId "+this.slaveId+" record "+Arrays.toString(record));
    //                semaphore.release();
                    double minDistance = 10000000.0;
                    int centroidCluster = -1;

    //                for(Entry<Integer,Hierarchy> entryH : this.quasi.entrySet()){
    //                    if(entryH.getValue().getNodesType().equals("date")){
    //                        datesVals[countDatesVal] = entryH.getValue().getDictionary().getIdToString(record[entryH.getKey()+1].intValue());
    //                        countDatesVal++;
    //                    }
    //                }
    //                countDatesVal=0;
    //                synchronized(this.centroids){
                    
                    
//                    
                        for(int j=0; j<=clusters.getLastClusterId(); j++){
                            Centroid c = centroids[j];
    //                        int cluster = entryCentroid.getKey();
    //                        System.out.println("Before Distance  threadId "+this.slaveId);
                            if(c!=null){
                                double distance = c.computeDistance(record,true);
        //                        System.out.println("Distance "+distance+" threadId "+this.slaveId);
                                if(distance < minDistance){
                                    minDistance = distance;
                                    centroidCluster = j;
                                }    
                            }
                        }
                    
                    
                    
                    
//                    Pair<Integer,Double> clusterDist = treeDist.findShortest(record, minDistance, centroidCluster);
//                    centroidCluster = clusterDist.getKey();
//                    minDistance = clusterDist.getValue();
    //                }

    //                semaphoresClusters.get(centroidCluster).acquire();
    //                System.out.println("Thread "+this.slaveId+" is going to write in database");
                    synchronized(clusters){
    //                    System.out.println("Before put  threadId "+this.slaveId);
                        newCluster = this.clusters.put(centroidCluster,recordId,minDistance,semaphoreSplit);
    //                    System.out.println("After put threadId "+this.slaveId);
                        if(newCluster > 0){

                                cOld = this.clusters.getCentroid(centroidCluster,this.quasi);
                                cNew = this.clusters.getCentroid(newCluster,this.quasi);
//                                treeDist.insert(cNew);
        //                    semaphoresClusters.put(newCluster,new Semaphore(1));
        //                    semaphoresCentr.put(newCluster,new Semaphore(1));
                        }
                    }
    //                System.out.println("Thread "+this.slaveId+" wrote in database successfully!");
    //                semaphoresClusters.get(centroidCluster).release();



                    if(newCluster < 0){
    //                    semaphores[1].acquire();
    //                    semaphoresCentr.get(centroidCluster).acquire();
    //                    synchronized(centroids){
    //                        System.out.println("Before update threadId "+this.slaveId);
                            this.centroids[centroidCluster].update(record,true); 
    //                        System.out.println("After update threadId "+this.slaveId);
    //                    }
    //                    int tempclusterId = clusters.getNumOfClusters();
    //                    Centroid temp = centroids.get(tempclusterId);
    //                    if(temp!=null){
    //                        System.out.println("Thread "+this.slaveId+" cluster "+tempclusterId+" ok ");
    //                        temp.print();
    //                    }
    //                    else{
    //                        System.out.println("Thread "+this.slaveId+" cluster "+tempclusterId+" is null");
    //                    }
    //                    semaphores[1].release();
    //                    semaphoresCentr.get(centroidCluster).release();
                    }
                    else{
    //                    semaphores[1].acquire();
    //                    semaphoresCentr.get(centroidCluster).acquire();
    //                    semaphoresCentr.get(newCluster).acquire();

    //                    synchronized(centroids){
                            this.centroids[centroidCluster] = cOld;
                            this.centroids[newCluster] = cNew;
    //                    }

    //                    semaphoresCentr.get(centroidCluster).release();
    //                    semaphoresCentr.get(newCluster).release();
    //                    System.out.println("Thread "+this.slaveId+" put "+newCluster);
    //                    semaphores[1].release();
                    }

    //                semaphore.acquire();
    //                this.nextRecord++;
    //                while(this.randomRecIds.contains(this.nextRecord)){
    //                    this.nextRecord++;
    //                }
                }else{
                    break;
                }
            }
//            semaphore.release();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error slave "+this.slaveId+" find distance "+e.getMessage());
        }
    }
    
    public void removeClusters(){
//        String[] datesVals = null;
//        int countDatesVal = 0;
        System.out.println("Thread id "+this.slaveId+" "+"remove clusters");
//        for(Entry<Integer,Hierarchy> entryH : this.quasi.entrySet()){
//            if(entryH.getValue().getNodesType().equals("date")){
//                countDatesVal++;
//            }
//        }

//        if(countDatesVal!=0){
//            datesVals = new String[countDatesVal];
//            countDatesVal=0;
//        }
        
        
        try {
//            semaphore.acquire();
            synchronized(consumerClustersIds){
                while(!consumerClustersIds.isEmpty()){
                    int clusterId = consumerClustersIds.get(0);
                    consumerClustersIds.remove(0);
                    empty.add(clusterId);
    //                semaphore.release();


                        if(!fill.contains(clusterId)){
                            Double[][] clusterRecords = clusters.getRecords(clusterId,false);
                            for(int i=0; i<clusterRecords.length; i++){
                                double minDistance = 10000000.0;
                                int centroidCluster = -1;
//                                for(Entry<Integer,Hierarchy> entryH : this.quasi.entrySet()){
//                                    if(entryH.getValue().getNodesType().equals("date")){
//                                        datesVals[countDatesVal] = entryH.getValue().getDictionary().getIdToString(clusterRecords[i][entryH.getKey()+1].intValue());
//                                        countDatesVal++;
//                                    }
//                                }
//                                countDatesVal=0;
                                for(int j=0; j<=clusters.getLastClusterId(); j++){
                                    Centroid c = centroids[j];
    //                                int possibleCluster = entryCentroid.getKey();

                                    if(c!=null && j != clusterId && /*!cloneSmallClusters.contains(possibleCluster)*/  !empty.contains(j)){
                                        double distance = c.computeDistance(clusterRecords[i],true);
        //                                System.out.println("Distance remove "+distance);
                                        if(distance < minDistance){
                                            minDistance = distance;
                                            centroidCluster = j;
                                        }    
                                    }
                                }


//                                synchronized(producerMoves){
//                                    producerMoves.put(new Pair(clusterId,clusterRecords[i]), new Pair(centroidCluster,minDistance));
//
//                                }
                                semaphore.acquire();
                                producerMoves[nextRemove++] = new removeValues(clusterId,clusterRecords[i],centroidCluster,minDistance);
                                semaphore.release();

//                                if(datesVals!=null){
//    //                                synchronized(datesValsProducer){
//                                        datesValsProducer.put(clusterRecords[i][0].intValue(), datesVals);
//    //                                }
//                                    datesVals = new String[datesVals.length];
//                                }
    //                            semaphores[1].acquire();
                                fill.add(centroidCluster);
    //                            semaphores[1].release();
                            }
                        }

    //                semaphore.acquire();
                }
            }
//            semaphore.release();
            
//            semaphores[1].acquire();
            int splitCluster,removeSize=0;
            Centroid cOld = null, cNew = null;
//            synchronized(producerMoves){
                while(true && this.slaveId == 1){
                    removeValues rValues = producerMoves[removeSize];
//                    Map.Entry<Pair<Integer,Double[]>,Pair<Integer,Double>> entry =  producerMoves.entrySet().iterator().next();
//                    Pair<Integer,Double[]> clusterRec = entry.getKey();
//                    Pair<Integer,Double> newClusterInfo = entry.getValue();
//                    producerMoves.remove(clusterRec);
    //                semaphores[1].release();
                    Double[] record = rValues.record;

    //                semaphores[3].acquire();
                    clusters.remove(rValues.removeCluster, record[0].intValue());
    //                semaphores[2].acquire();
    //                centroids.get(clusterRec.getKey()).updateSubtraction(record);
    //                semaphores[2].release();

                    splitCluster = clusters.put(rValues.newCluster, record[0].intValue(), rValues.distance,null);
                    if(splitCluster > 0){
                        cOld = this.clusters.getCentroid(rValues.newCluster,this.quasi);
                        cNew = this.clusters.getCentroid(splitCluster,this.quasi);
                    }
    //                semaphores[3].release();

                    if(splitCluster < 0){
    //                    semaphores[2].acquire();
//                        synchronized(datesValsProducer){
                            centroids[rValues.newCluster].update(record,true);
//                        }
    //                    semaphores[2].release();
                    }
                    else{
    //                    semaphores[2].acquire();
                        this.centroids[rValues.newCluster] = cOld;
                        this.centroids[splitCluster] = cNew;
    //                    System.out.println("Thread "+this.slaveId+" put "+newCluster);
    //                    semaphores[2].release();
                    }

    //                semaphores[1].acquire();
                    if(removeSize==nextRemove-1){
                        break;
                    }
                    else{
                        removeSize++;
                    }
                }
//            }
//            semaphores[1].release();
            if(this.slaveId == 1){
//                clusters.executeDeleteBatch();
                clusters.executeBatch();
            }
            System.out.println("Thread "+this.slaveId+" Done!");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error slave small clusters "+ex.getMessage());
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    class removeValues{
        int removeCluster;
        int newCluster;
        double distance;
        Double[] record;
        
        removeValues(int rC, Double[] r, int nC, double d){
            this.removeCluster = rC;
            this.record = r;
            this.distance = d;
            this.newCluster = nC;
        }
    }
}
