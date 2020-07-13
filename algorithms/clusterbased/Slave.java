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
    private static Centroid[] centroids;
    private DiskData data;
    private static int caseRun = -1; // 0 fill clusters, 1 remove small clusters
    private static ArrayList<Integer> consumerClustersIds;
    private static removeValues[] producerMoves;
    private static List<Integer> empty,fill;
    private Map<Integer,Hierarchy> quasi;
    private RangeDouble range;
    private static Double[][] records;
    private List<Integer> bigClusters;
    
    
    private static Pair<Double[],List<Integer>>[] randomRecs;
    private Map<Integer,Hierarchy> hierarchies;
    private static AtomicInteger clusterIdCounter;
    private static ClusterDistTree distTree; 
    
    private static Map<Integer,Double[][]> clusterRecs;
    private static Double[][][] anonymizedRecs;
    private static Integer[] clustersIds;
    
    private static int nextRemove;
    
    public Slave(int id,Centroid[] c, DiskData d,Clusters cl,RangeDouble r){
        this.slaveId = id;
        this.data = d;
        this.range = r;
        
        if(this.slaveId == 1){
            this.clusters = cl;
            this.centroids = c;
        }
    }
    
    public Slave(int id, RangeDouble r,Map<Integer,Hierarchy> h){
        this.slaveId = id;
        this.range = r;
        this.hierarchies = h;
        if(this.slaveId == 1){
            semaphoreSplit = new Semaphore(1);
        }
    }
    
    public Slave(int id,Map<Integer,Hierarchy> h,List<Integer> bigCl){
        this.slaveId = id;
        this.hierarchies = h;
        this.bigClusters = bigCl;
        if(this.slaveId == 1){
            semaphoreSplit = new Semaphore(1);
        }
    }
    
    public Slave(int id, Clusters cl,  Centroid[] c, DiskData d, Map<Integer,Hierarchy> q,RangeDouble r){
        this.slaveId = id;
        this.data = d;
        this.range = r;
        this.quasi = q;
        
        
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
    
    
    
    public static void setCase(int cr){
        caseRun = cr;
        
    }
    
    public void setRange(RangeDouble r){
        this.range = r;
    }
    
    public static void setAnonymize(Map<Integer,Double[][]> rCl, Double[][][] anoRec, Integer[] clIds){
        clusterRecs = rCl;
        anonymizedRecs = anoRec;
        clustersIds = clIds;
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
    
    public static void setSplit(Clusters cl){
        clusters = cl;
        clusterIdCounter = new AtomicInteger(clusters.getLastClusterId());
    }
    
    public static void setRecords(Double[][] r, ClusterDistTree cl){
        records = r;
        distTree = cl;
        if(r==null){
            System.gc();
            Runtime.getRuntime().gc();
        }
    }
    
    public static void setConsumer(List<Integer> c){
        consumerClustersIds = new ArrayList<>(c);
        empty = new ArrayList<Integer>();
        fill = new ArrayList<Integer>();
        producerMoves = new removeValues[c.size()*(clusters.getK()-1)];
        nextRemove = 0;
    }
    
    
    public static void setCentroids(Centroid[] clCen){
        centroids = clCen;
    }
    
    
    
    public void run(){
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
            case 3:
                splitClusters();
                break;
            case 4:
                anonymize();
        }
    }
    
    public void anonymize(){
        System.out.println("Thread "+this.slaveId+" range "+range);
        for(int i=range.getLowerBound().intValue(); i <= range.getUpperBound().intValue(); i++){
            clusters.anonymizeCluster(clustersIds[i],centroids[clustersIds[i]],data,anonymizedRecs,clusterRecs.get(clustersIds[i]),i);
        }
    }
    
    public void splitClusters(){
        
        Map<Integer,Double[][]> records;
        synchronized(clusters){
            records = clusters.getClusterDatasetRecs(this.bigClusters,false);
        }
        
        for(Integer cluster : this.bigClusters){
            Double[][] clusterRecords = records.get(cluster);
            
            Centroid newCentroid = new Centroid(cluster,this.hierarchies,clusterRecords[0],true);
            for(int i=1; i<clusters.getK(); i++){
                double distance = newCentroid.computeDistance(clusterRecords[i],true);
                clusters.update(cluster, clusterRecords[i][0].intValue(), distance);
                newCentroid.update(clusterRecords[i],true);
            }
            
            centroids[cluster] = newCentroid;
            
            int remainder = clusterRecords.length % clusters.getK();
            int i=clusters.getK();
            int size_cluster=0;
            int newClusterId = clusterIdCounter.incrementAndGet();
            clusters.setSize(newClusterId, 0);
            while(remainder!=0){
                synchronized(clusters){
                    clusters.remove(cluster, clusterRecords[i][0].intValue());
                    clusters.put(newClusterId, clusterRecords[i][0].intValue(), 0.0, semaphoreSplit);
                }
                size_cluster++;
                if(size_cluster == 1){
                    newCentroid = new Centroid(newClusterId,this.hierarchies,clusterRecords[i],true);
                }
                else{
                    double distance = newCentroid.computeDistance(clusterRecords[i],true);
                    synchronized(clusters){
                        clusters.update(newClusterId, clusterRecords[i][0].intValue(), distance);
                    }
                    newCentroid.update(clusterRecords[i],true); 
                }
                remainder--;
                i++;
            }
            
            int counter=0;
            while(i<clusterRecords.length){
                if(counter % clusters.getK() == 0 && counter != 0){
                    centroids[newClusterId] = newCentroid;
                    newClusterId = clusterIdCounter.incrementAndGet();
                    clusters.setSize(newClusterId, 0);
                    size_cluster=0;
                    counter = 0;
                }
                
                synchronized(clusters){
                    clusters.remove(cluster, clusterRecords[i][0].intValue());
                    clusters.put(newClusterId, clusterRecords[i][0].intValue(), 0.0, semaphoreSplit);
                }
                counter++;
                size_cluster++;
                
                if(size_cluster == 1){
                    newCentroid = new Centroid(newClusterId,this.hierarchies,clusterRecords[i],true);
                }
                else{
                    double distance = newCentroid.computeDistance(clusterRecords[i],true);
                    synchronized(clusters){
                        clusters.update(newClusterId, clusterRecords[i][0].intValue(), distance);
                    }
                    newCentroid.update(clusterRecords[i],true); 
                }
                i++;
            }
            centroids[newClusterId] = newCentroid;
        }
        
    }
    
    public void fillClusters(){
        int clusterId;
        System.out.println("Slave "+this.slaveId+" range "+range);
        for(int i=this.range.lowerBound.intValue(); i<=this.range.upperBound.intValue(); i++){
            if(randomRecs[i]!=null){
                List<Integer> recordsIds = randomRecs[i].getValue();
                clusterId = clusterIdCounter.incrementAndGet();
                for(Integer recordId : recordsIds){
                    synchronized(clusters){
                        this.clusters.put(clusterId,recordId,0.0,semaphoreSplit);
                    }
                }
                Centroid centroid = new Centroid(clusterId,this.hierarchies,randomRecs[i].getKey(),true);
                centroids[clusterId] =  centroid;
                clusters.setSize(clusterId,recordsIds.size());
            }
        }
    }
    
    public void findDistance(){
        System.out.println("Thread "+this.slaveId+"distance");
        int recordId;
        int newCluster;
        Centroid cOld=null,cNew=null;       
        try{
            int start = this.range.lowerBound.intValue();
            int end = this.range.upperBound.intValue() >= records.length ? records.length-1 : this.range.upperBound.intValue();
            
            
            System.out.println("Thread "+this.slaveId+" start "+start+" end "+end);
            for(int i=start; i<=end; i++){
                
                Double[] record = records[i];
                if(record[0]!=null){
                    recordId = record[0].intValue();
                    double minDistance = 10000000.0;
                    int centroidCluster = -1;
                    
                    
                    if(distTree == null){
                        for(int j=0; j<=clusters.getLastClusterId(); j++){
                            Centroid c = centroids[j];
                            if(c!=null){
                                double distance = c.computeDistance(record,true);
                                if(distance < minDistance){
                                    minDistance = distance;
                                    centroidCluster = j;
                                }    
                            }
                        }
                        
                        
                        synchronized(clusters){
                            newCluster = this.clusters.put(centroidCluster,recordId,minDistance,semaphoreSplit);
                            if(newCluster > 0){
                                    cOld = this.clusters.getCentroid(centroidCluster,this.quasi);
                                    cNew = this.clusters.getCentroid(newCluster,this.quasi);
                            }
                        }



                        if(newCluster < 0){
                            this.centroids[centroidCluster].update(record,true); 
                        }
                        else{
                            this.centroids[centroidCluster] = cOld;
                            this.centroids[newCluster] = cNew;
                        }
                    }
                    else{
                        RangeDouble rangeCentroids = distTree.findRange(record);
                        for(int j=rangeCentroids.getLowerBound().intValue(); j<=rangeCentroids.getUpperBound().intValue(); j++){
                            Centroid c = centroids[j];
                            if(c!=null){
                                double distance = c.computeDistance(record,true);
                                if(distance < minDistance){
                                    minDistance = distance;
                                    centroidCluster = j;
                                }    
                            }
                        }
                        
                        
                        synchronized(clusters){
                            newCluster = this.clusters.put(centroidCluster,recordId,minDistance,null);
                        }
                        this.centroids[centroidCluster].update(record,true); 
                    }
                    
                }else{
                    break;
                }
            }
            System.out.println("End find distance thread "+this.slaveId);
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error slave "+this.slaveId+" find distance "+e.getMessage());
        }
    }
    
    public void removeClusters(){
        System.out.println("Thread id "+this.slaveId+" "+"remove clusters");
        int clusterId = -1;
        
        
        try {
            synchronized(consumerClustersIds){
                while(!consumerClustersIds.isEmpty()){
                    clusterId = consumerClustersIds.get(0);
                    consumerClustersIds.remove(0);
                    empty.add(clusterId);
                    if(!fill.contains(clusterId)){
                        Double[][] clusterRecords;
                        synchronized(clusters){
                            clusterRecords = clusters.getRecords(clusterId,false);
                        }
                        for(int i=0; i<clusterRecords.length; i++){
                            double minDistance = 10000000.0;
                            int centroidCluster = -1;
                            for(int j=0; j<=clusters.getLastClusterId(); j++){
                                Centroid c = centroids[j];

                                if(c!=null && j != clusterId &&  !empty.contains(j)){
                                    double distance = c.computeDistance(clusterRecords[i],true);
                                    if(distance < minDistance){
                                        minDistance = distance;
                                        centroidCluster = j;
                                    }    
                                }
                            }
                            semaphore.acquire();
                            producerMoves[nextRemove++] = new removeValues(clusterId,clusterRecords[i],centroidCluster,minDistance);
                            semaphore.release();
                            fill.add(centroidCluster);
                        }
                    }
                }
            }
            int splitCluster,removeSize=0;
            Centroid cOld = null, cNew = null;
            while(true && this.slaveId == 1){
                removeValues rValues = producerMoves[removeSize];
                Double[] record = rValues.record;
                clusters.remove(rValues.removeCluster, record[0].intValue());
                splitCluster = clusters.put(rValues.newCluster, record[0].intValue(), rValues.distance,null);
                if(splitCluster > 0){
                    cOld = this.clusters.getCentroid(rValues.newCluster,this.quasi);
                    cNew = this.clusters.getCentroid(splitCluster,this.quasi);
                }
                if(splitCluster < 0){
                    centroids[rValues.newCluster].update(record,true);
                }
                else{
                    this.centroids[rValues.newCluster] = cOld;
                    this.centroids[splitCluster] = cNew;
                }

                if(removeSize==nextRemove-1){
                    break;
                }
                else{
                    removeSize++;
                }
            }
            if(this.slaveId == 1){
                clusters.executeBatch();
            }
            System.out.println("Thread "+this.slaveId+" Done!");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error slave small clusters "+ex.getMessage()+" clusterId "+clusterId);
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
