/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import algorithms.Algorithm;
import algorithms.flash.LatticeNode;
import algorithms.mixedkmanonymity.MixedApriori;
import data.Data;
import data.DiskData;
import graph.Graph;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.Pair;

/**
 *
 * @author nikos
 */
public class ClusterBasedAlgorithm implements Algorithm {
    DiskData diskData;
    Map<Integer, Hierarchy> hierarchies = null;
    Integer k;
    Integer clusters;
    Pair<Double[],List<Integer>>[] randomRecords;
    int sizeRandomCluster;
    Double[] recordCentroid;
    Clusters diskClusters;
    Centroid[] clustersCentroids;
    static Semaphore[] semaphores;
    Slave[] slaves;
    int numOfSlaves=4;
    int treeChildClusters = 5;
    int numRecordsMain;
    Map<Integer,RangeDouble> rangeThreads;
    final int  partOfData = 8;
    int numRecsSlave;
    ClusterDistTree cltree = null;
    int initialClusters;
    double clustersProportion = 0.005;

    public ClusterBasedAlgorithm(Data data){
        this.diskData = (DiskData) data;
    }
    
    public ClusterBasedAlgorithm(){
        
    }
    
    @Override
    public void setDataset(Data dataset) {
        this.diskData = (DiskData) dataset;
    }

    @Override
    public void setHierarchies(Map<Integer, Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
    }

    @Override
    public void setArguments(Map<String, Integer> arguments) {
        this.k = arguments.get("k");
        this.clusters = this.diskData.getRecordsTotal()/k;
        System.out.println("Clusters "+this.clusters);
    }
    
    private void clear(){
        this.diskClusters.daleteClusterTable();
        this.diskData.deleteCheckedTable();
    }

    @Override
    public void anonymize() {
        long end ;
        long start2;
        long start = System.currentTimeMillis(); 
        for(Entry<Integer,Hierarchy> entryHier : this.hierarchies.entrySet()){
            Hierarchy hier = entryHier.getValue();
            if(hier.getHierarchyType().equals("range")){
                hier.setDictionaryData(this.diskData.getDictionary());
            }
        }
        if(initialiseClusters()){
            initialiseSlaves();
            fillClusters();
            end = System.currentTimeMillis();
            
            System.out.println("Initialisation and filling Time is "+(end-start)+" ms");
            System.out.println("Initialisation and filling Time is "+(end-start)/1000.0+" s");
            System.out.println("Initialisation and filling Time is "+(end-start)/60000.0+" min");
            start2 = System.currentTimeMillis();
            removeSmallclusters();
            end = System.currentTimeMillis();
            System.out.println("Removing Time is "+(end-start2)+" ms");
            System.out.println("Removing Time is "+(end-start2)/1000.0+" s");
            System.out.println("Removing Time is "+(end-start2)/60000.0+" min");
           
            start2 = System.currentTimeMillis(); 
            anonymization();
            end = System.currentTimeMillis();
            System.out.println("Anonymization Time is "+(end - start2)+" ms");
            System.out.println("Anonymization Time is "+(end - start2)/1000.0+" s");
            System.out.println("Anonymization Time is "+(end - start2)/60000.0+" min");
            
        }
        else{
            anonymizeSmallRecords();
            System.out.println("Exception ");
        }      
        start2 = System.currentTimeMillis(); 
        clear();
        end = System.currentTimeMillis();
        System.out.println("Clear Time is "+(end-start2)+" ms");
        System.out.println("Clear Time is "+(end-start2)/1000.0+" s");
        System.out.println("Clear Time is "+(end-start2)/60000.0+" min");
        System.out.println("Time is "+(end-start)+" ms");
        System.out.println("Time is "+(end-start)/1000.0+" s");
        System.out.println("Time is "+(end-start)/60000.0+" min");
    }
    
    private void anonymizeSmallRecords(){
        List<Pair<Double[],List<Integer>>> smallClusters = this.diskData.getSmallRecordsClusters(k, this.hierarchies.keySet(), true);
        if(!smallClusters.isEmpty()){
            this.diskClusters.createCluster();
            this.numOfSlaves = new ForkJoinPool().getParallelism();
            Pair<Double[],List<Integer>>[] smallClustersArr = new Pair[smallClusters.size()];
            
            if(smallClusters.size() < numOfSlaves){
                this.numOfSlaves = smallClusters.size();
            }
            
            int numSmallRecSl = (smallClusters.size() + numOfSlaves -1)/numOfSlaves;
            RangeDouble range = new RangeDouble(0.0,(double)numSmallRecSl-1);
            Slave.setCase(2);
            smallClusters.toArray(smallClustersArr);
            Slave.setRandom(clustersCentroids, smallClustersArr ,this.diskClusters);
            
            slaves = new Slave[this.numOfSlaves];
            for(int i=0; i<this.numOfSlaves; i++){
                slaves[i] = new Slave(i+1,range,this.hierarchies);
                range = new RangeDouble(range.upperBound+1,range.upperBound+numSmallRecSl >= smallClusters.size() ? smallClusters.size()-1  : range.upperBound+numSmallRecSl);
            }

            for(int i=0; i< this.numOfSlaves; i++){
                slaves[i].start();
            }


            for(int i=0; i< this.numOfSlaves; i++){
                try {
                    slaves[i].join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.diskClusters.executeBatch();
            this.diskClusters.setLastClusterId(this.diskClusters.getmaxIdCluster());
            Double [][] records = this.diskData.getSpecificDataset(0, this.diskData.getRecordsTotal(), false);
            this.initialClusters = smallClusters.size();
            this.numOfSlaves = new ForkJoinPool().getParallelism();
            slaves = new Slave[numOfSlaves];
            if(this.diskData.getRows() != 0){
                
                if(this.diskData.getRows() < this.numOfSlaves){
                    this.numOfSlaves = this.diskData.getRows();
                    slaves = new Slave[numOfSlaves];
                }
                
                int newNumRec = (this.diskData.getRows()  + numOfSlaves -1)/numOfSlaves;
                RangeDouble nRange = new RangeDouble(0.0,(double)newNumRec-1);
                Slave.setRecords(records, null);
                for(int i=0; i< this.numOfSlaves; i++){
                    slaves[i] = new Slave(i+1,this.diskClusters,this.clustersCentroids,this.diskData,this.hierarchies,nRange);
                    if(i==0){
                        Slave.setCase(0);
                    }
                    slaves[i].start();
                    nRange = new RangeDouble(nRange.upperBound+1,nRange.upperBound+newNumRec >= this.diskData.getRows() ? this.diskData.getRows()-1  : nRange.upperBound+newNumRec);
                }

                for(int i=0; i< this.numOfSlaves; i++){
                    try {
                        slaves[i].join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                this.diskClusters.executeBatch();
            }

            removeSmallclusters();
            anonymization();
        }
        else{
            this.diskData.cloneOriginalToAnonymize();
        }
    }
    
    
    private boolean initialiseClusters(){
        Pair<Pair<Double[],List<Integer>>[],Integer> returnedRandom;
        this.diskData.createAnonymizedTable();
        this.diskData.createChekedTable();
        returnedRandom = this.diskData.getRandomRecords(this.clusters,this.hierarchies.keySet());
        this.randomRecords = returnedRandom.getKey();
        this.sizeRandomCluster = returnedRandom.getValue();
        this.clustersCentroids = new Centroid[2*this.clusters+1];
        
        
        System.out.println("Clusters real "+this.sizeRandomCluster);
        this.diskClusters = new Clusters(this.diskData.getUrlDataBase(),this.clusters,k);

        int counterCluster=0;
        int sumAnonymized = 0;
        int clusterSmallest = 0;
        List<Integer> mergeSmall = new ArrayList();
        for(int i=0; i<this.sizeRandomCluster; i++){

            List<Integer> similarRecords = randomRecords[i].getValue(); 
            if(similarRecords.size() >= this.k){
                diskData.fillAnonymizedRecords(similarRecords);
                randomRecords[i]=null;
                sumAnonymized++;
            }
        }

        this.diskClusters.createCluster();

        this.numOfSlaves = new ForkJoinPool().getParallelism();
        int numRandomRecSl = (sizeRandomCluster + numOfSlaves -1)/numOfSlaves;
        RangeDouble range = new RangeDouble(0.0,(double)numRandomRecSl-1);
        Slave.setCase(2);
        Slave.setRandom(clustersCentroids, randomRecords,this.diskClusters);
        slaves = new Slave[this.numOfSlaves];
        for(int i=0; i<this.numOfSlaves; i++){
            slaves[i] = new Slave(i+1,range,this.hierarchies);
            range = new RangeDouble(range.upperBound+1,range.upperBound+numRandomRecSl >= sizeRandomCluster ? sizeRandomCluster-1  : range.upperBound+numRandomRecSl);
        }

        for(int i=0; i< this.numOfSlaves; i++){
            slaves[i].start();
        }


        for(int i=0; i< this.numOfSlaves; i++){
            try {
                slaves[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }



        this.diskData.executeAnonymizedBatch();
        this.diskClusters.executeBatch();
        System.out.println("Last "+(this.sizeRandomCluster - sumAnonymized)+" Max "+this.diskClusters.getmaxIdCluster()+" sum recs "+counterCluster+" very small records "+clusterSmallest);
        this.diskClusters.setLastClusterId(this.diskClusters.getmaxIdCluster());
        this.initialClusters = this.diskClusters.getLastClusterId();
        if(very_small_k()){
            int i=0; 
            
            while(this.randomRecords[i]==null){
                i++;
            }
            Double[] simple_rec = this.randomRecords[i].getKey();
            this.randomRecords=null;
            this.diskClusters.setSplit(!very_small_k());
            int childsTree;
            if(((int)(this.diskClusters.getLastClusterId() * this.clustersProportion)) >1 ){
                childsTree = ((int)(this.diskClusters.getLastClusterId() * this.clustersProportion));
            }
            else{
               childsTree = 30; 
            }
            this.cltree = new ClusterDistTree(this.clustersCentroids,childsTree,this.diskClusters.getmaxIdCluster());
            System.out.println("Tree dist build");
            cltree.print();
            RangeDouble simpleRange = cltree.findRange(simple_rec);
            System.out.println("Range search "+simpleRange);
        }
        return this.diskClusters.numOfClusters()==0 ? false : true; 
    }
    
    private boolean very_small_k(){
        return ((double)this.k / this.diskData.getRecordsTotal()) < 0.0001 && (this.initialClusters * 0.01) > 1 /*&& ((int)(this.initialClusters * this.clustersProportion)) > 1*/;
    }
     
    private void initialiseSlaves(){
         
        this.rangeThreads = new HashMap();
        System.out.println("Threads : "+this.numOfSlaves);
        numRecordsMain = (this.diskData.getRecordsTotal() + numOfSlaves -1)/numOfSlaves;
        int numRecords = (numRecordsMain + numOfSlaves -1)/numOfSlaves;
        System.out.println("Main "+numRecordsMain+" slaves "+numRecords);
        RangeDouble range = new RangeDouble(0.0,(double)numRecords-1);
        for(int i=0; i< this.numOfSlaves; i++){
            System.out.println("Thread "+(i+1)+" range "+range.toString() );
            slaves[i] = new Slave(i+1,this.diskClusters,this.clustersCentroids,this.diskData,this.hierarchies,range);
            this.rangeThreads.put(i, range);
            range = new RangeDouble(range.upperBound+1,range.upperBound+numRecords >= numRecordsMain ? numRecordsMain-1  : range.upperBound+numRecords);
        }
        
    }
    
   
    
    private void recomputeSizeRanges(long heapRemainsSize){
        this.numRecordsMain = ((Long)(heapRemainsSize / (this.diskData.getDataColumns() * Double.BYTES))).intValue();
        this.numRecordsMain = ((Double)(this.numRecordsMain - (this.numRecordsMain* 0.05))).intValue();
        int numRecords = (numRecordsMain + numOfSlaves -1)/numOfSlaves;
        System.out.println("New Main "+numRecordsMain+" slaves "+numRecords);
        RangeDouble range = new RangeDouble(0.0,(double)numRecords-1);
        for(int i=0; i< this.numOfSlaves; i++){
            System.out.println("Thread "+(i+1)+" new range "+range.toString() );
            slaves[i] = new Slave(i+1,this.diskClusters,this.clustersCentroids,this.diskData,this.hierarchies,range);
            this.rangeThreads.put(i, range);
            range = new RangeDouble(range.upperBound+1,range.upperBound+numRecords >= numRecordsMain ? numRecordsMain-1  : range.upperBound+numRecords);
        }
    }
    
    private void fillClusters(){
        Slave.setCase(0);
        RangeDouble rangeMain;
        long heapFreeSize = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        long recordsSize = this.numRecordsMain * this.diskData.getDataColumns() * Double.BYTES;
        System.out.println("Heap "+heapFreeSize+" Records "+recordsSize);
        if(recordsSize > heapFreeSize){
            this.recomputeSizeRanges(heapFreeSize);
        }
      
        rangeMain = new RangeDouble(0.0,(double)this.numRecordsMain) ;
        
        
        Double[][] records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue(),false);
        Slave.setRecords(records,this.cltree);
        System.out.println("Reputaton range "+rangeMain);
        while(rangeMain.upperBound <= this.diskData.getRecordsTotal()){
            
            RangeDouble nRange = null;
            if(this.diskData.getRows() < this.numRecordsMain){
                int newNumRec = (this.diskData.getRows()  + numOfSlaves -1)/numOfSlaves;
                nRange = new RangeDouble(0.0,(double)newNumRec-1);
                for(int i=0; i< this.numOfSlaves; i++){
                    slaves[i].setRange(nRange);
                    nRange = new RangeDouble(nRange.upperBound+1,nRange.upperBound+newNumRec >= this.diskData.getRows() ? this.diskData.getRows()-1  : nRange.upperBound+newNumRec);
                }
            }
            
            for(int i=0; i< this.numOfSlaves; i++){
                slaves[i].start();
            }
            
            
            
            for(int i=0; i< this.numOfSlaves; i++){
                try {
                    slaves[i].join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println("Ends fill");
            
            records = null;
            Slave.setRecords(null,null);
            System.gc();
            Runtime.getRuntime().gc();
            
            if(very_small_k()){
                this.diskClusters.executeBatch();
                System.out.println("Start split");
                int numSlavesBig = this.numOfSlaves;
                List<Integer> bigClusters = this.diskClusters.getBigclusters();
                if(numSlavesBig > bigClusters.size()){
                    numSlavesBig = bigClusters.size();
                }
                
                if(numSlavesBig > 1){
                    int numClusters = (bigClusters.size() + numSlavesBig -1)/numSlavesBig;

                    RangeDouble rangeBigCl = new RangeDouble(0.0,(double)numClusters);
                    Slave.setCase(3);
                    Slave.setSplit(this.diskClusters);
                    Slave.setCentroids(this.clustersCentroids);
                    for(int i=0; i< numSlavesBig; i++){
                        System.out.println("Sublist range"+rangeBigCl);
                        slaves[i] =  new Slave(i+1,this.hierarchies,bigClusters.subList(rangeBigCl.lowerBound.intValue(), rangeBigCl.upperBound.intValue()));
                        rangeBigCl = new RangeDouble((double)rangeBigCl.lowerBound.intValue()+numClusters,(double)rangeBigCl.upperBound.intValue()+numClusters);
                        slaves[i].start();
                        if(rangeBigCl.getUpperBound().intValue() > bigClusters.size()){
                            rangeBigCl.setUpperBound((double)bigClusters.size());
                        }
                        
                        if(rangeBigCl.getUpperBound().intValue() < rangeBigCl.getLowerBound()){
                            break;
                        }
                    }

                    for(int i=0; i< numSlavesBig; i++){
                        try {
                            slaves[i].join();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    this.diskClusters.executeBatch();
                    this.diskClusters.executeUpdateBatch();
                    this.diskClusters.setLastClusterId(this.diskClusters.getmaxIdCluster());
                    System.out.println("End split");
                }

                int childsTree;
                if(((int)(this.diskClusters.getLastClusterId() * this.clustersProportion)) > 1){
                    childsTree = ((int)(this.diskClusters.getLastClusterId() * this.clustersProportion));
                }
                else{
                   childsTree = 30; 
                }
                this.cltree = new ClusterDistTree(this.clustersCentroids,childsTree,this.diskClusters.getmaxIdCluster());
            }
            
            if(rangeMain.upperBound>=this.diskData.getRecordsTotal()){
                break;
            }
            
            
            
            heapFreeSize = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
            recordsSize = this.numRecordsMain * this.diskData.getDataColumns() * Double.BYTES;
            System.out.println("2 Heap "+heapFreeSize+" Records "+recordsSize);
            if(recordsSize > heapFreeSize){
                this.recomputeSizeRanges(heapFreeSize);
            }
            
            for(int i=0; i< this.numOfSlaves; i++){
               
                if(rangeMain.upperBound!=this.diskData.getRecordsTotal() && i==0){
                    rangeMain = new RangeDouble(rangeMain.upperBound,rangeMain.upperBound+this.numRecordsMain>this.diskData.getRecordsTotal() ? (double)this.diskData.getRecordsTotal() : rangeMain.upperBound+this.numRecordsMain) ;
                    records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue(),false);
                    Slave.setRecords(records,this.cltree);
                    Slave.setCase(0);
                    System.out.println("Reputaton range "+rangeMain);
                }
                slaves[i] = new Slave(i+1,this.diskData,this.hierarchies,this.rangeThreads.get(i));
            }
            
            
        }
        Slave.setRecords(null,null);
        this.diskClusters.executeBatch();
        System.out.println("Total Size "+this.diskClusters.totalSize());
    }

    private void removeSmallclusters(){
        List<Integer> smallClustersId = this.diskClusters.getSmallClusters();
        System.out.println("Max ID "+this.diskClusters.getmaxIdCluster());
        if(very_small_k()){
            if(!smallClustersId.isEmpty()){
                
                Double[][] smallClusterRecords = this.diskClusters.removeSmallClusters(this.clustersCentroids,smallClustersId);
                
                System.out.println("Start tree");
                System.out.println("Cluster info "+this.diskClusters.numOfClusters()+" "+this.diskClusters.getmaxIdCluster());
//                int childsTree;
//                if(((int)(this.diskClusters.getmaxIdCluster() * this.clustersProportion)) == 1){
//                    childsTree = ((int)(this.diskClusters.getLastClusterId() * this.clustersProportion));
//                }
//                else{
//                   childsTree = 30; 
//                }
                if(this.diskClusters.getmaxIdCluster() > 100){
                    this.cltree = new ClusterDistTree(this.clustersCentroids,((int)(this.diskClusters.getmaxIdCluster()*this.clustersProportion)),this.diskClusters.getmaxIdCluster());
                    this.cltree.print();
                }   
                else{
                    this.cltree = null;
                }
                System.out.println("End tree");
                System.out.println("Unique "+smallClusterRecords.length);
                int numRecordsSlave = (smallClusterRecords.length + numOfSlaves -1)/numOfSlaves;
                System.out.println("Unique "+smallClusterRecords.length+" Rec per slave "+numRecordsSlave);
                RangeDouble rangeRec = new RangeDouble(0.0,(double)numRecordsSlave-1);
                
                for(int i=0; i<this.numOfSlaves; i++){
                    slaves[i] = new Slave(i+1,this.diskClusters,this.clustersCentroids,this.diskData,this.hierarchies,rangeRec);
                    if(i==0){
                        Slave.setCase(0);
                        Slave.setRecords(smallClusterRecords,this.cltree);
                    }
                    slaves[i].start();
                    rangeRec = new RangeDouble(rangeRec.upperBound+1,rangeRec.upperBound+numRecordsSlave >= smallClusterRecords.length ? smallClusterRecords.length-1  : rangeRec.upperBound+numRecordsSlave);
                }
                
                for(int i=0; i< this.numOfSlaves; i++){
                    try {
                        slaves[i].join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                this.diskClusters.executeBatch();
                
                System.out.println("Start split");
                int slaveBig = this.numOfSlaves;
                List<Integer> bigClusters = this.diskClusters.getBigclusters();
                if(slaveBig > bigClusters.size()){
                    slaveBig = bigClusters.size();
                }
                if(slaveBig > 0){
                    int numClusters = (bigClusters.size() + slaveBig -1)/slaveBig;
                    RangeDouble rangeBigCl = new RangeDouble(0.0,(double)numClusters);
                    Slave.setCase(3);
                    Slave.setSplit(this.diskClusters);
                    Slave.setCentroids(this.clustersCentroids);
                    for(int i=0; i< slaveBig; i++){
                        System.out.println("Sublist range"+rangeBigCl);
                        if(rangeBigCl.getLowerBound() > rangeBigCl.getUpperBound()){
                            break;
                        }
                        slaves[i] =  new Slave(i+1,this.hierarchies,bigClusters.subList(rangeBigCl.lowerBound.intValue(), rangeBigCl.upperBound.intValue()));
                        rangeBigCl = new RangeDouble((double)rangeBigCl.lowerBound.intValue()+numClusters,(double)rangeBigCl.upperBound.intValue()+numClusters);
                        slaves[i].start();
                        if(rangeBigCl.getUpperBound().intValue() > bigClusters.size()){
                            rangeBigCl.setUpperBound((double)bigClusters.size());
                        }
                    }

                    for(int i=0; i< slaveBig; i++){
                        try {
                            slaves[i].join();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    this.diskClusters.executeBatch();
                    this.diskClusters.executeUpdateBatch();
                    this.diskClusters.setLastClusterId(this.diskClusters.getmaxIdCluster());
                    System.out.println("End split");
                }
                
                System.out.println("Total Size "+this.diskClusters.totalSize());
                smallClustersId = this.diskClusters.getSmallClusters();
            }
        }
        else{
            
            Slave.setCase(1);
            Slave.setCentroids(clustersCentroids);
            this.numOfSlaves = new ForkJoinPool().getParallelism();
            this.slaves = new Slave[numOfSlaves];
            boolean exit=false;
            while(!smallClustersId.isEmpty()){
                switch(smallClustersId.size()){
                    case 1:
                        System.out.println("Case 1 "+smallClustersId.get(0));
                        shiftToClosest(smallClustersId.get(0));
                        exit=true;
                        break;
                    default:
                        System.out.println("Switch case "+smallClustersId.size());
                        Slave.setCentroids(clustersCentroids);
                        Slave.setConsumer(smallClustersId);
                        if(smallClustersId.size() < this.numOfSlaves){
                            this.numOfSlaves = smallClustersId.size();
                            slaves = new Slave[this.numOfSlaves];
                        }

                        for(int i=0; i<slaves.length; i++){
                            slaves[i] = new Slave(i+1,this.diskData,this.hierarchies);
                            slaves[i].start();
                        }
                        
                        for(int i=0; i<slaves.length; i++){
                            try {
                                slaves[i].join();
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                                Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        this.diskClusters.executeBatch();
                        List<Integer> emptyClusters = this.diskClusters.removeEmptyClusters();
                        if(!emptyClusters.isEmpty()){
                            for(Integer cluster : emptyClusters){
                                this.clustersCentroids[cluster]=null;
                            }
                        }
                        break;

                }
                if(exit){
                    break;
                }
                smallClustersId = this.diskClusters.getSmallClusters();
            }
        }
        System.out.println("Total size "+this.diskClusters.totalSize());
        System.out.println("Done!");
    }
    
    private void shiftToClosest(int clusterId){
        int newCluster;
        Centroid cOld=null,cNew=null;
        if(this.diskClusters.numOfClusters() == 1){
            int sizeCluster = this.diskClusters.getSize(clusterId);
            Double[][] recordsTofill = this.diskData.getRandomAnonymizedRecords(this.k,sizeCluster,this.hierarchies.keySet());
            for(int i=0; i<recordsTofill.length; i++){
                this.diskClusters.put(clusterId,recordsTofill[i][0].intValue(),0,null);
                this.clustersCentroids[clusterId].update(recordsTofill[i],true);
            }
            this.diskClusters.executeBatch();
        }
        else{
            this.clustersCentroids[clusterId] = null;
            Double[][] records = this.diskClusters.getRecords(clusterId,false);
            for(int i=0; i<records.length; i++){
                double minDistance = 100000000.0;
                int centroidCluster = -1;

                for(int j=0; j<=this.diskClusters.getLastClusterId(); j++){
                    Centroid c = this.clustersCentroids[j];
                    if(c!=null){
                        double distance = c.computeDistance(records[i],true);
                        if(distance < minDistance){
                            minDistance = distance; 
                            centroidCluster = j;
                        }
                    }

                }


                this.diskClusters.remove(clusterId, records[i][0].intValue());
                newCluster = this.diskClusters.put(centroidCluster,records[i][0].intValue(),minDistance,null);
                if(newCluster > 0){
                    cOld = this.diskClusters.getCentroid(centroidCluster,this.hierarchies);
                    cNew = this.diskClusters.getCentroid(newCluster,this.hierarchies);
                }

                if(newCluster < 0){
                    this.clustersCentroids[centroidCluster].update(records[i],true);
                }
                else{
                    this.clustersCentroids[centroidCluster] = cOld;
                    this.clustersCentroids[newCluster] = cNew;
                }

            }
            this.diskClusters.executeBatch();
            this.diskClusters.removeSize(clusterId);
        }
    }
    
    private void anonymization(){
        if(this.very_small_k()){
            this.treeChildClusters = (int) (this.diskClusters.numOfClusters()*0.01);
        }
        else{
            this.treeChildClusters = (int) (this.diskClusters.numOfClusters()*0.2);
        }
        long recordSize = this.treeChildClusters * 2*(this.k-1) * Double.BYTES;    //// records from clusters which will come in the main memory 
        long heapFreeSize = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        if(recordSize > heapFreeSize){
            long availableSize = ((Long)(heapFreeSize*10/100));
            this.treeChildClusters = ((Long)(availableSize/(2*(this.k-1) * Double.BYTES))).intValue();
        }
        if(this.treeChildClusters == 0){
            this.diskData.createAnonymizedQuery();
            this.diskData.initialiseStatement();
            System.out.println("Very small num of clusters");
            Integer[] clusterIds = this.diskClusters.getClustersIds();
            Map<Integer,Double[][]> recordsClusters = this.diskClusters.getClusterDatasetRecs(Arrays.asList(clusterIds),false);
            Double[][][] clusterRecords = new Double[clusterIds.length][][];
            int i=0;
            for(Integer clusterId : clusterIds){
                diskClusters.anonymizeCluster(clusterId,clustersCentroids[clusterId],diskData,clusterRecords,recordsClusters.get(clusterId),i);
            }
            this.diskData.executeAnonymizedClusterBatch(clusterRecords);
            this.diskData.closeConnection();
            
        }
        else{
            ClusterTree tree = new ClusterTree(this.clustersCentroids,this.treeChildClusters,this.diskClusters);
            this.diskData.createAnonymizedQuery();
            this.diskData.initialiseStatement();
            this.diskData.copyDB();
            this.diskClusters.setTempDb(this.diskData.getUrlTempDb());
            Integer[] clusters;
            int totalClusters=0;
            
            while((clusters=tree.getNextClusters())!=null){
                totalClusters += clusters.length;
                System.out.println("Clusters "+clusters.length+" total "+totalClusters);

                Double[][][] clusterRecords = new Double[clusters.length][][];
                Map<Integer,Double[][]> recordsClusters = this.diskClusters.getClusterDatasetRecs(Arrays.asList(clusters),true);
                if(clusters.length > numOfSlaves){
                    int numClusters = (clusters.length + numOfSlaves -1)/numOfSlaves;
                    RangeDouble rangeSlaveClusters = new RangeDouble(0.0,(double)numClusters-1);
                    Slave.setCase(4);
                    Slave.setAnonymize(recordsClusters,clusterRecords,clusters);
                    for(int i=0; i<this.numOfSlaves; i++){
                        slaves[i] = new Slave(i+1,clustersCentroids,diskData,diskClusters,rangeSlaveClusters);
                        slaves[i].start();
                        rangeSlaveClusters = new RangeDouble(rangeSlaveClusters.getUpperBound()+1,rangeSlaveClusters.getUpperBound()+numClusters > clusters.length-1 ? clusters.length-1 : rangeSlaveClusters.getUpperBound()+numClusters);
                    }

                    for(int i=0; i<slaves.length; i++){
                        try {
                            slaves[i].join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    this.diskData.executeAnonymizedClusterBatch(clusterRecords);
                }
                else{
                    RangeDouble rangeSlaveClusters = new RangeDouble(0.0,0.0);
                    Slave.setCase(4);
                    Slave.setAnonymize(recordsClusters,clusterRecords,clusters);
                    for(int i=0; i<clusters.length; i++){
                        slaves[i] = new Slave(i+1,clustersCentroids,diskData,diskClusters,rangeSlaveClusters);
                        slaves[i].start();
                        rangeSlaveClusters = new RangeDouble(rangeSlaveClusters.getUpperBound()+1,rangeSlaveClusters.getUpperBound()+1);
                    }

                    for(int i=0; i<clusters.length; i++){
                        try {
                            slaves[i].join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                this.diskData.executeAnonymizedClusterBatch(clusterRecords);
            }
            this.diskData.deleteDB();
            this.diskData.closeConnection();
            System.out.println("Total clusters tree "+totalClusters);
        }
        System.out.println("Total clusters database "+this.diskClusters.numOfClusters());
    }
    

    @Override
    public Object getResultSet() {
        return null;
    }

    @Override
    public Graph getLattice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAnonymousResult(LatticeNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
