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
import javafx.util.Pair;

/**
 *
 * @author nikos
 */
public class ClusterBasedAlgorithm implements Algorithm {
    DiskData diskData;
    Map<Integer, Hierarchy> hierarchies = null;
    Integer k;
    Integer clusters;
//    Set<Integer> recordsIds;
    Pair<Double[],List<Integer>>[] randomRecords;
    int sizeRandomCluster;
//    List<Double[]> recordsCentroids;
    Double[] recordCentroid;
    Clusters diskClusters;
//    Map<Integer,Centroid> clustersCentroids;
    Centroid[] clustersCentroids;
    static Semaphore[] semaphores;
    Slave[] slaves;
    int numOfSlaves=4;
    final int treeChildClusters = 5;
    int numRecordsMain;
    Map<Integer,RangeDouble> rangeThreads;
    final int  partOfData = 8;
    int numRecsSlave;

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
//        sortDatabase();
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
//        if(removeAnonymizedClusters()){
            long startBuild = System.currentTimeMillis();
//            System.out.println("Build dist tree");
//            this.treeDist = new CentroidTreeDist(this.clustersCentroids,this.diskClusters.getLastClusterId());
//            long endBuild = System.currentTimeMillis();
//            System.out.println("End dist tree "+(endBuild-startBuild)+" ms");
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
           
    //        buildClusteringTree();
            start2 = System.currentTimeMillis(); 
            anonymization();
            end = System.currentTimeMillis();
            System.out.println("Anonymization Time is "+(end - start2)+" ms");
            System.out.println("Anonymization Time is "+(end - start2)/1000.0+" s");
            System.out.println("Anonymization Time is "+(end - start2)/60000.0+" min");
            
        }
//        new Thread(new Runnable() {
//            public void run(){
//                clear();
//            }
//        }).start();
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
    
    
    private boolean initialiseClusters(){
        Pair<Pair<Double[],List<Integer>>[],Integer> returnedRandom;
        List<Integer> clusterIds = new ArrayList();
//        Map<Integer,Object> recordsInfo = this.diskData.getRandomRecords(this.clusters,this.hierarchies.keySet());
        this.diskData.createAnonymizedTable();
        this.diskData.createChekedTable();
//        this.diskData.removeAnonymizedRec(this.k,this.hierarchies.keySet());
//        this.randomRecords = this.diskData.getRandomRecords(this.clusters,this.hierarchies.keySet());
        returnedRandom = this.diskData.getRandomRecords(this.clusters,this.hierarchies.keySet());
        this.randomRecords = returnedRandom.getKey();
        this.sizeRandomCluster = returnedRandom.getValue();
        this.clustersCentroids = new Centroid[2*this.clusters+1];
//        this.randomRecords = (ArrayList) recordsInfo.get(0);
//        this.recordsIds =  (HashSet) recordsInfo.get(1);
        
        
        System.out.println("Clusters real "+this.sizeRandomCluster);
//        if(this.randomRecords.size() == this.diskData.getRecordsTotal()){
//            this.diskData.cloneOriginalToAnonymize();
//            return false;
//        }
//        else{
            this.diskClusters = new Clusters(this.diskData.getUrlDataBase(),this.clusters,k);
//            int start=0;
            
            int counterCluster=0;
            int sumAnonymized = 0;
            int clusterSmallest = 0;
            List<Integer> mergeSmall = new ArrayList();
            for(int i=0; i<this.sizeRandomCluster; i++){
                
                List<Integer> similarRecords = randomRecords[i].getValue(); 
//                System.out.println("Ran "+counterCluster+" size "+similarRecords.size());
//                counterCluster += similarRecords.size();
                if(similarRecords.size() >= this.k){
                    diskData.fillAnonymizedRecords(similarRecords);
                    randomRecords[i]=null;
                    sumAnonymized++;
                }
//                else{
//                    counterCluster += similarRecords.size();
//                    if(similarRecords.size() == 1){
//                        clusterSmallest++;
//                        mergeSmall.addAll(similarRecords);
//                        if(mergeSmall.size() >= this.k){
//                            randomRecords[i] = new Pair(randomRecords[i].getKey(),mergeSmall);
//                            mergeSmall = new ArrayList();
//                        }
//                        else if(i==sizeRandomCluster-1 && !mergeSmall.isEmpty()){
//                            randomRecords[i] = new Pair(randomRecords[i].getKey(),mergeSmall);
//                        }
//                        else{
//                            randomRecords[i] = null;
//                        }
//                    }
//                }
                
                
//                else{
//                    clusterIds.add(counterCluster);
////                    this.diskClusters.createCluster(similarRecords,counterCluster);
////                    Centroid centroid = new Centroid(counterCluster,this.hierarchies,randomRecords[i].getKey(),true);
////                    this.clustersCentroids[counterCluster] =  centroid; 
//                    counterCluster++;
//                }
            }
            
            
            this.diskClusters.createCluster();
//            this.diskClusters.setLastClusterId(this.sizeRandomCluster - sumAnonymized);
//            return false;
            
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
            
            
            this.randomRecords=null;
            this.diskData.executeAnonymizedBatch();
//            this.diskClusters.setLastClusterId(counterCluster-1);
            this.diskClusters.executeBatch();
            System.out.println("Last"+(this.sizeRandomCluster - sumAnonymized)+"Max "+this.diskClusters.getmaxIdCluster()+" sum recs "+counterCluster+" very small records "+clusterSmallest);
            this.diskClusters.setLastClusterId(this.diskClusters.getmaxIdCluster());
            return this.diskClusters.numOfClusters()==0 ? false : true; 
//            return false;
            
//            for(int i=1; i<=this.clusters; i++){
//                similarRecords = getSimilarRecords(start);
//                start += similarRecords.size();
//
//                this.diskClusters.createCluster(similarRecords);
//                Centroid centroid = new Centroid(this.diskClusters.getLastClusterId(),this.hierarchies,this.recordCentroid,true);
//                this.clustersCentroids.put(this.diskClusters.getLastClusterId(), centroid);
//                // create centroids
//    //            
//    //            System.out.println("Similar "+i);
//    //            System.out.println(Arrays.toString(similarRecords.toArray()));
//
//                if(start == this.randomRecords.size()){
//                    break;
//                }
//            }
//            this.diskClusters.executeBatch();
//            this.randomRecords.clear();
//        }
    }
    
//    private boolean removeAnonymizedClusters(){
//        this.diskData.createAnonymizedTable();
//        Map<Integer,Double[][]> anonymizedRecs = this.diskClusters.removeAnonymized();
//        System.out.println("Anonymized clusters "+anonymizedRecs.isEmpty());
//        if(!anonymizedRecs.isEmpty()){
//            if(this.diskClusters.numOfClusters() == 0){
//                this.diskData.cloneOriginalToAnonymize();
//                return false;
//            }
//            else{
//                System.out.println("REmove anonymized");
//                for(Entry<Integer,Double[][]> recByCluster : anonymizedRecs.entrySet()){
//                    this.clustersCentroids.remove(recByCluster.getKey());
//                    this.diskData.fillAnonymizedRecords(recByCluster.getValue());
//                }   
//            }
//        }
//        
//        return true;
//    }
    
//    private void initialiseSlaves(){
//         
////        this.numOfSlaves = new ForkJoinPool().getParallelism();
////        this.numOfSlaves =1;
////        System.out.println("Threads : "+this.numOfSlaves);
//        slaves = new Slave[this.numOfSlaves];
//        numRecsSlave = (this.diskData.getRecordsTotal() + this.partOfData -1)/this.partOfData;
//        RangeDouble range = new RangeDouble(1.0,(double)numRecsSlave);
////        semaphores = new Semaphore[3];
////        semaphores[0] = new Semaphore(1);
////        semaphores[1] = new Semaphore(1);
////        semaphores[2] = new Semaphore(1);
//        for(int i=0; i< this.numOfSlaves; i++){
//            slaves[i] = new Slave(i+1,this.diskClusters,this.clustersCentroids,this.diskData,this.hierarchies,range);
//            range = new RangeDouble(range.upperBound+1,range.upperBound+numRecsSlave);
//        }
//        
//    }
//    
    private void initialiseSlaves(){
         
        
        this.rangeThreads = new HashMap();
//        this.numOfSlaves =1;
        System.out.println("Threads : "+this.numOfSlaves);
//        slaves = new Slave[this.numOfSlaves];
        numRecordsMain = (this.diskData.getRecordsTotal() + numOfSlaves -1)/numOfSlaves;
        int numRecords = (numRecordsMain + numOfSlaves -1)/numOfSlaves;
        System.out.println("Main "+numRecordsMain+" slaves "+numRecords);
        RangeDouble range = new RangeDouble(0.0,(double)numRecords-1);
//        semaphores = new Semaphore[3];
//        semaphores[0] = new Semaphore(1);
//        semaphores[1] = new Semaphore(1);
//        semaphores[2] = new Semaphore(1);
        for(int i=0; i< this.numOfSlaves; i++){
            System.out.println("Thread "+(i+1)+" range "+range.toString() );
            slaves[i] = new Slave(i+1,this.diskClusters,this.clustersCentroids,this.diskData,this.hierarchies,range);
            this.rangeThreads.put(i, range);
            range = new RangeDouble(range.upperBound+1,range.upperBound+numRecords >= numRecordsMain ? numRecordsMain-1  : range.upperBound+numRecords);
            
            
            
        }
        
    }
    
    
//    private List<Integer> getSimilarRecords(int start){
//        List<Integer> records = new ArrayList<Integer>();
//        Set<Integer> quasiIdentifiers = this.hierarchies.keySet();
//        records.add(this.randomRecords.get(start)[0].intValue());
//        this.recordCentroid = this.randomRecords.get(start);
//        
//        for(int i=start+1; i<this.randomRecords.size(); i++){
//            Double[] record = this.randomRecords.get(i);
//            Double[] previousRecord = this.randomRecords.get(i-1);
//            int j;
//            for(j=1; j<record.length; j++){
//                if(quasiIdentifiers.contains(j-1)){
//                    if(!record[j].equals(previousRecord[j])){
//                       break; 
//                    }
//                }
//            }
//            
//            if(j==record.length){
//                records.add(record[0].intValue());
//            }
//            else{
//                break;
//            }
//        }
//        
//        return records;
//    }
    
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
//        long heapFreeSize = Runtime.getRuntime().freeMemory(); 
//        long heapFreeSize = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
        long heapFreeSize = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        long recordsSize = this.numRecordsMain * this.diskData.getDataColumns() * Double.BYTES;
        System.out.println("Heap "+heapFreeSize+" Records "+recordsSize);
        if(recordsSize > heapFreeSize){
            this.recomputeSizeRanges(heapFreeSize);
        }
      
        rangeMain = new RangeDouble(0.0,(double)this.numRecordsMain) ;
        
        
        Double[][] records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue(),false);
        Slave.setRecords(records);
        System.out.println("Reputaton range "+rangeMain);
        while(rangeMain.upperBound<= this.diskData.getRecordsTotal()){
            
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
            
            if(rangeMain.upperBound>=this.diskData.getRecordsTotal()){
                break;
            }
            
            records = null;
            Slave.setRecords(null);
            System.gc();
            Runtime.getRuntime().gc();
            
            heapFreeSize = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
            recordsSize = this.numRecordsMain * this.diskData.getDataColumns() * Double.BYTES;
            System.out.println("2 Heap "+heapFreeSize+" Records "+recordsSize);
            if(recordsSize > heapFreeSize){
                this.recomputeSizeRanges(heapFreeSize);
//                rangeMain = new RangeDouble(rangeMain.upperBound,rangeMain.upperBound+this.numRecordsMain>this.diskData.getRecordsTotal() ? (double)this.diskData.getRecordsTotal() : rangeMain.upperBound+this.numRecordsMain) ;
//                records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue(),false);
//                Slave.setRecords(records);
            }
//            else{
//               for(int i=0; i< this.numOfSlaves; i++){
//               
//                    if(rangeMain.upperBound!=this.diskData.getRecordsTotal() && i==0){
//                        rangeMain = new RangeDouble(rangeMain.upperBound,rangeMain.upperBound+this.numRecordsMain>this.diskData.getRecordsTotal() ? (double)this.diskData.getRecordsTotal() : rangeMain.upperBound+this.numRecordsMain) ;
//                        records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue(),false);
//                        Slave.setRecords(records);
//                        System.out.println("Reputaton range "+rangeMain);
//                    }
//                    slaves[i] = new Slave(i+1,this.diskData,this.hierarchies,this.rangeThreads.get(i));
//               
//                } 
//            }
            
            for(int i=0; i< this.numOfSlaves; i++){
               
                    if(rangeMain.upperBound!=this.diskData.getRecordsTotal() && i==0){
                        rangeMain = new RangeDouble(rangeMain.upperBound,rangeMain.upperBound+this.numRecordsMain>this.diskData.getRecordsTotal() ? (double)this.diskData.getRecordsTotal() : rangeMain.upperBound+this.numRecordsMain) ;
                        records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue(),false);
                        Slave.setRecords(records);
                        System.out.println("Reputaton range "+rangeMain);
                    }
                    slaves[i] = new Slave(i+1,this.diskData,this.hierarchies,this.rangeThreads.get(i));
               
                }
            
            
        }
//        for(int i=0; i< this.numOfSlaves; i++){
//            try {
//                slaves[i].join();
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//                Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        Slave.setRecords(null);
        this.diskClusters.executeBatch();
        System.out.println("Total Size "+this.diskClusters.totalSize());
        
//        for(Entry<Integer,Centroid> entry : this.clustersCentroids.entrySet()){
//            entry.getValue().print();
//        }
//        for(int i=0; i<=this.diskClusters.getLastClusterId(); i++){
//            if(this.clustersCentroids[i]!=null)
//                this.clustersCentroids[i].print();
//        }
    }

//    private void fillClusters(){
//        Slave.setCase(0);
//        int reputation = 1;
//        int counterRecs=0;
//        RangeDouble rangeMain = new RangeDouble(0.0,(double)this.numRecordsMain) ;
//        RangeDouble range;
////        List<Double[]> records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue());
////        Slave.setRecords(reputation, records);
////        System.out.println("Reputaton "+reputation+"range "+rangeMain);
//        
////        while(counterRecs<= this.diskData.getRecordsTotal()){
//            
//            for(int i=0; i< this.numOfSlaves; i++){
//                slaves[i].start();
//            }
//            
//            
//            
//            while(true){
//                for(int i=0; i< this.numOfSlaves; i++){
//
//                    if(slaves[i]!=null && slaves[i].getState()==Thread.State.TERMINATED ){ 
//                        int numRecs = slaves[i].getRange().upperBound.intValue() - slaves[i].getRange().lowerBound.intValue()+1;
//                        counterRecs += numRecs;
//                        if(slaves[i].getReputation() < 2){
//                            range= new RangeDouble(((this.numRecsSlave*(i+1+this.numOfSlaves))-((double)this.numRecsSlave))+1,(this.numRecsSlave*(i+1+(double)this.numOfSlaves)));
//                            slaves[i] = new Slave(i+1,this.diskData,this.hierarchies,slaves[i].getReputation()+1,range);
//                            slaves[i].start();
//                        }
//                        else{
//                           slaves[i] = null; 
//                        }
//                    }
//    //                    if(rangeMain.upperBound!=this.diskData.getRecordsTotal() && i==this.numOfSlaves-1){
//    //                        rangeMain = new RangeDouble(rangeMain.upperBound,rangeMain.upperBound+this.numRecordsMain>this.diskData.getRecordsTotal() ? (double)this.diskData.getRecordsTotal() : rangeMain.upperBound+this.numRecordsMain) ;
//    //                        records = this.diskData.getSpecificDataset(rangeMain.lowerBound.intValue(), rangeMain.upperBound.intValue());
//    //                        Slave.setRecords(++reputation, records);
//    //                        Slave.removePreviousRecs(reputation-1);
//    //                        System.out.println("Reputaton "+reputation+"range "+rangeMain);
//    //                    }
//    //                    slaves[i] = new Slave(i+1,this.diskData,this.hierarchies,reputation,this.rangeThreads.get(i));
//    //                    slaves[i].start();
//
//                }
//                if(counterRecs >= this.diskData.getRecordsTotal()){
//                    break;
//                }
//            }
//            
//            
//            
////            if(rangeMain.upperBound==this.diskData.getRecordsTotal()){
////                break;
////            }
////        }
////        for(int i=0; i< this.numOfSlaves; i++){
////            try {
////                slaves[i].join();
////            } catch (InterruptedException ex) {
////                ex.printStackTrace();
////                Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
////            }
////        }
////        Slave.removePreviousRecs(reputation-1);
//        this.diskClusters.executeBatch();
//        System.out.println("Total Size "+this.diskClusters.totalSize());
//        
//        for(Integer clusterId : this.diskClusters.getClustersIds()){
//            this.clustersCentroids[clusterId].print();
//        }
//    }
    
    private void removeSmallclusters(){
        List<Integer> smallClustersId = this.diskClusters.getSmallClusters();
        Slave.setCase(1);
//        this.diskData.getDictionary().print();
        Slave.setCentroids(clustersCentroids);
        System.out.println("small clusters "+Arrays.toString(smallClustersId.toArray()));
//        semaphores = new Semaphore[4];
//        for(int i=0; i<semaphores.length; i++){
//            semaphores[i] = new Semaphore(1);
//        }
//        Slave.setSemaphores();
        boolean exit=false;
        while(!smallClustersId.isEmpty()){
            switch(smallClustersId.size()){
                case 1:
                    System.out.println("Case 1 "+smallClustersId.get(0));
                    shiftToClosest(smallClustersId.get(0));
//                    this.diskClusters.deleteTable(smallClustersId.get(0));
                    this.diskClusters.removeSize(smallClustersId.get(0));
                    exit=true;
                    break;
//                case 2:
//                    System.out.println("Case 2 "+smallClustersId.get(0)+" "+smallClustersId.get(1));
//                    merge(smallClustersId.get(0),smallClustersId.get(1));
//                    exit = true;
//                    break;
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
//                    for(int i=0; i<slaves.length; i++){
//                        slaves[i].start();
//                    }
                    for(int i=0; i<slaves.length; i++){
                        try {
                            slaves[i].join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
//                    this.diskClusters.executeDeleteBatch();
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
        System.out.println("Total size "+this.diskClusters.totalSize());
        System.out.println("Done!");
    }
    
    private void shiftToClosest(int clusterId){
        this.clustersCentroids[clusterId] = null;
        Double[][] records = this.diskClusters.getRecords(clusterId,false);
        int newCluster;
        Centroid cOld=null,cNew=null;
        int countDatesVal=0;
//        String[] datesVals=null;
//        for(Entry<Integer,Hierarchy> entryH : this.hierarchies.entrySet()){
//            if(entryH.getValue().getNodesType().equals("date")){
//                countDatesVal++;
//            }
//        }
//        
//        if(countDatesVal!=0){
//            datesVals = new String[countDatesVal];
//        }
        for(int i=0; i<records.length; i++){
            countDatesVal=0;
            double minDistance = 100000000.0;
            int centroidCluster = -1;
//            for(Entry<Integer,Hierarchy> entryH : this.hierarchies.entrySet()){
//                if(entryH.getValue().getNodesType().equals("date")){
//                    datesVals[countDatesVal] = entryH.getValue().getDictionary().getIdToString(records[i][entryH.getKey()+1].intValue());
//                    countDatesVal++;
//                }
//            }
            
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
    }
    
    private void merge(int clusterIdFrom, int clusterIdTo){
        
    }
    
//    private void buildClusteringTree(){
//        
//        
//    }
    
    private void anonymization(){
        ClusterTree tree = new ClusterTree(this.clustersCentroids,this.treeChildClusters,this.diskClusters);
        this.diskData.createAnonymizedQuery();
        this.diskData.initialiseStatement();
        this.diskData.copyDB();
        this.diskClusters.setTempDb(this.diskData.getUrlTempDb());
//        tree.print();
        Integer[] clusters;
        int totalClusters=0;
        List<Thread> listThreads = new ArrayList();
        Algorithm mixedApriori = new MixedApriori(); /// this for km anonymity 
        Map<String,List<Double[][]>> threadAnonymizedRecs = new HashMap();
        while((clusters=tree.getNextClusters())!=null){
            totalClusters += clusters.length;
//            List<Double[][]> clusterRecords = new ArrayList();
            System.out.println("Clusters "+Arrays.toString(clusters));
//            Double[][][] clusterRecords = this.diskClusters.getRecords(clusters);
//            for(int i=0; i<clusters.length; i++){
//                clusterRecords[i] = clustersCentroids[clusters[i]].anonymize(clusterRecords[i]);
//            }
            Double[][][] clusterRecords = new Double[clusters.length][][];
            for(int i=0; i<clusters.length; i++){
                diskClusters.anonymizeCluster(clusters[i],clustersCentroids[clusters[i]],diskData,clusterRecords,i);
            }
            this.diskData.executeAnonymizedClusterBatch(clusterRecords);
//            this.diskClusters.setAnonymizedPointer(0);
        }
        this.diskData.deleteDB();
        this.diskData.closeConnection();
        System.out.println("Total clusters tree "+totalClusters);
        System.out.println("Total clusters database "+this.diskClusters.numOfClusters());
    }
//    private void anonymization(){
//        ClusterTree tree = new ClusterTree(this.clustersCentroids,this.treeChildClusters,this.diskClusters);
//        this.diskData.createAnonymizedQuery();
//        tree.print();
//        Integer[] clusters;
//        int totalClusters=0;
//        List<Thread> listThreads = new ArrayList();
//        Algorithm mixedApriori = new MixedApriori(); /// this for km anonymity 
//        Map<String,Integer> threadAnonymizedRecs = new HashMap();
//        while((clusters=tree.getNextClusters())!=null){
//            totalClusters += clusters.length;
//            System.out.println("Clusters "+Arrays.toString(clusters));
////            if(clusters.length != 1){
////                int end = this.numOfSlaves <= clusters.length ? this.numOfSlaves : clusters.length-1;
//                Double[][][] clusterRecords = this.diskClusters.getRecords(clusters);
//                for(int i=0; i<clusters.length-1; i++){
//                    final int clusterId = clusters[i];
////                    final int clustersLength = clusters.length;
//                    final int pointer = i;
//                    Thread t = new Thread(new Runnable() {
//                        public void run() {
//                            
////                            diskClusters.anonymizeCluster(clusterId,clustersCentroids[clusterId],diskData,clusterRecords,pointer);
////                            synchronized(diskData){
////                                diskData.executeAnonymizedClusterBatch(clusterRecords[pointer]);
////                            }
//                            clusterRecords[pointer] = clustersCentroids[clusterId].anonymize(clusterRecords[pointer]);
//                            synchronized(diskData){
//                                diskData.executeAnonymizedClusterBatch(clusterRecords[pointer]);
//                            }
////                            threadAnonymizedRecs.put(Thread.currentThread().getName(), pointer);
//                        }
//                    });
//                    t.start();
//                    listThreads.add(t);
//                }
////                diskClusters.anonymizeCluster(clusters[clusters.length-1],clustersCentroids[clusters[clusters.length-1]],diskData,clusterRecords,clusters.length-1);
//                clusterRecords[clusters.length-1] = clustersCentroids[clusters[clusters.length-1]].anonymize(clusterRecords[clusters.length-1]);
//                synchronized(diskData){
//                    this.diskData.executeAnonymizedClusterBatch(clusterRecords[clusters.length-1]);
//                }
////                int count =0;
////                List<Thread> listThreadsTemp = new ArrayList(listThreads);
////                while(!listThreads.isEmpty()){
////                    List<Thread> listThreadsTemp = new ArrayList(listThreads);
////                    for(Thread t : listThreadsTemp) {
////    //                    try {
////    //                        t.join();
////    //                        this.diskData.executeAnonymizedClusterBatch(threadAnonymizedRecs.get(t.getName()));
////    //                    } catch (InterruptedException ex) {
////    //                        Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
////    //                    }
////                        if(t.getState()==Thread.State.TERMINATED){ 
////                            count++;
////                            this.diskData.executeAnonymizedClusterBatch(clusterRecords[threadAnonymizedRecs.get(t.getName())]);
////                            listThreads.remove(t);
////                            if(listThreads.isEmpty()){
////                                break;
////                            }
////                        }
////                    }
////                }
//                
////                listThreads.clear();
//                
////                for(int i=end; i<clusters.length; i++){
////                    List<Double[][]> clusterRecords = new ArrayList();
////                    diskClusters.anonymizeCluster(clusters[i],clustersCentroids.get(clusters[i]),diskData,clusterRecords);
//////                    synchronized(this.diskData){
////                    this.diskData.executeAnonymizedClusterBatch(clusterRecords);
//////                    }
////                }
//                
//                
//                
////                listThreads.clear();
////                threadAnonymizedRecs.clear();
//                
////            }
////            else{
////                
////                List<Double[][]> clusterRecords = new ArrayList();
////                diskClusters.anonymizeCluster(clusters[0],clustersCentroids.get(clusters[0]),diskData,clusterRecords);
////                this.diskData.executeAnonymizedClusterBatch(clusterRecords);
////                
////                
////            }
////            final Integer[] clusterThread = clusters;
////            System.out.println("Clusters "+Arrays.toString(clusters));
////            totalClusters += clusters.length;
////            Thread t = new Thread(new Runnable() {
////                public void run() {
////                    List<Double[][]> clusterRecords = new ArrayList();
////                    for(Integer cluster : clusterThread){
////                        diskClusters.anonymizeCluster(cluster,clustersCentroids.get(cluster),diskData,clusterRecords);
////                    }
////                    threadAnonymizedRecs.put(Thread.currentThread().getName(), clusterRecords);
////                }
////            });
////            t.start();
////            listThreads.add(t);
//                for(Thread t : listThreads){
//                    try {
//                        t.join();
////                        this.diskData.executeAnonymizedClusterBatch(clusterRecords[threadAnonymizedRecs.get(t.getName())]);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(ClusterBasedAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
////                this.diskData.executeAnonymizedClusterBatch(clusterRecords);
////                clusterRecords = null;
//                listThreads.clear();
//        }
//        
//       
////        this.diskData.executeAnonymizedClusterBatch();
//        
//        System.out.println("Total clusters tree "+totalClusters);
//        System.out.println("Total clusters database "+this.diskClusters.numOfClusters());
//        
//        
//    }
    

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
