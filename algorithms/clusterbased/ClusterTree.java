/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.SerializationUtils;

/**
 *
 * @author nikos
 */
public class ClusterTree {
    Centroid[] parents;
    Centroid[][] centroidClusters;
    int children;
    int numParents;
    int currentParentExtract;
    
    
    public ClusterTree(Centroid[] centroidsCl, int maxChilds, Clusters clusters){
        this.children = maxChilds;
        this.currentParentExtract = 0;
//        Map<Integer,Centroid> tempCentroidsCl = convertToMap(centroidsCl,clusters);
        Centroid[] tempCentroidsCl = centroidsCl.clone();
        Centroid[] queneCentroids = new Centroid[maxChilds];
        this.numParents = (int) Math.ceil(((double)clusters.numOfClusters())/maxChilds);
        parents = new Centroid[numParents];
        int remainder = clusters.numOfClusters() % maxChilds;
        System.out.println("Clusters "+clusters.numOfClusters()+" Parents "+numParents+" remainder "+remainder);
        this.centroidClusters = new Centroid[numParents][];
//        if(remainder == 0){
//            clusters = new Centroid[numParents][maxChilds];
//        }
//        else{
//            clusters = new Centroid[numParents][];
//            for(int i=0; i<clusters.length; i++){
//                if(i != clusters.length-1){
//                    clusters[i] = new Centroid[remainder];
//                }
//                else{
//                    clusters[i] = new Centroid[maxChilds];
//                }
//            }
//        }
        
        int parentId = 0;
        int centroidcounter=0;
        int clusterSize = clusters.numOfClusters();
        List<Integer> clustersIds = new LinkedList(Arrays.asList(clusters.getClustersIds()));
        while(!clustersIds.isEmpty()){
            Centroid firstCentroid = centroidsCl[clustersIds.get(0)];
//            tempCentroidsCl.remove(firstCentroid.getKey());
            clustersIds.remove(0);
            queneCentroids[0] = firstCentroid;
            Centroid parent = findShortCentroids(queneCentroids,firstCentroid,centroidsCl,clustersIds);
            System.out.println("Consumer size "+clustersIds.size()+" parent id"+parentId+" "+queneCentroids.length);
            this.parents[parentId] = parent;
            this.centroidClusters[parentId] = queneCentroids.clone();
            if(!clustersIds.isEmpty()){
                if(clustersIds.size() == remainder){
                    queneCentroids = new Centroid[remainder];
                }
                else{
                    queneCentroids = new Centroid[maxChilds];
                }
            }
            parentId++;
            
        }
//        while(!tempCentroidsCl.isEmpty()){
//            Map.Entry<Integer,Centroid> firstCentroid = tempCentroidsCl.entrySet().iterator().next();
//            tempCentroidsCl.remove(firstCentroid.getKey());
//            queneCentroids[0] = firstCentroid.getValue();
//            Centroid parent = findShortCentroids(queneCentroids,firstCentroid.getValue(),tempCentroidsCl);
//            System.out.println("Consumer size "+tempCentroidsCl.size()+" parent id"+parentId+" "+queneCentroids.length);
//            this.parents[parentId] = parent;
//            this.centroidClusters[parentId] = queneCentroids.clone();
//            if(tempCentroidsCl.size() != 0){
//                if(tempCentroidsCl.size() == remainder){
//                    queneCentroids = new Centroid[remainder];
//                }
//                else{
//                    queneCentroids = new Centroid[maxChilds];
//                }
//            }
//            parentId++;
//            
//        }
    }
    
//    private Map<Integer,Centroid> convertToMap(Centroid[] centrs, Clusters clusters){
//        
//    }
    
    private Centroid findShortCentroids(Centroid[] quene,Centroid initialCentr, Centroid[] consumer,List<Integer> clusterIds){
        Centroid parentCentr = new Centroid(initialCentr);
        int counter = 1;
        while(counter < quene.length){
            double minDistance = 1000000.0;
            Integer centroidCluster = -1;
            for(Integer cluster : clusterIds){
                double distance = consumer[cluster].computeDistance(parentCentr);
                if(minDistance > distance){
                    minDistance = distance;
                    centroidCluster = cluster;
                }
                
            }
            
            parentCentr.update(consumer[centroidCluster]);
            quene[counter] = consumer[centroidCluster];
            counter++;
            clusterIds.remove(centroidCluster);
        }
        
        return parentCentr;
    }
    
    public void print(){
        for(int i=0; i<centroidClusters.length; i++){
            System.out.print("Parent ");
            parents[i].print();
            for(int j=0; j<centroidClusters[i].length; j++){
                this.centroidClusters[i][j].print();
            }
        }
    }
    
    public Integer[] getNextClusters(){
        if(this.currentParentExtract < this.numParents){
            Centroid[] extract = this.centroidClusters[this.currentParentExtract];
            Integer[] clustersId = new Integer[extract.length];
            for(int i=0; i<extract.length; i++){
                clustersId[i] = extract[i].getClusterId();
            }
            this.currentParentExtract++;
            return clustersId;
        }
        return null;
    }
}
