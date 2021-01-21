/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.util.Pair;
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
        Centroid[] queneCentroids = new Centroid[maxChilds];
        this.numParents = (int) Math.ceil(((double)clusters.numOfClusters())/maxChilds);
        parents = new Centroid[numParents];
        int remainder = clusters.numOfClusters() % maxChilds;
        System.out.println("Clusters "+clusters.numOfClusters()+" Parents "+numParents+" remainder "+remainder);
        this.centroidClusters = new Centroid[numParents][];
        
        int parentId = 0;
        List<Integer> clustersIds = new LinkedList(Arrays.asList(clusters.getClustersIds()));
        while(!clustersIds.isEmpty()){
            Centroid firstCentroid = centroidsCl[clustersIds.get(0)];
            clustersIds.remove(0);
            queneCentroids[0] = firstCentroid;
            Centroid parent = findShortCentroids(queneCentroids,firstCentroid,centroidsCl,clustersIds,clusters);
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
    }
    

    
    private Centroid findShortCentroids(Centroid[] quene,Centroid initialCentr, Centroid[] consumer,List<Integer> clusterIds,Clusters cl){
        Centroid parentCentr = new Centroid(initialCentr);

        List<Pair<Integer,Double>> compareList = new ArrayList();
        for(Integer cluster : clusterIds){
            double distance = consumer[cluster].computeDistance(parentCentr);

            compareList.add(new Pair(cluster,distance));

        }

        Collections.sort(compareList, new Comparator<Pair<Integer,Double>>() {
            @Override
            public int compare(Pair<Integer,Double> lhs, Pair<Integer,Double> rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getValue().compareTo(rhs.getValue());
            }
        });

//        System.out.println("Sorted List "+Arrays.toString(compareList.toArray()));

        for(int i=0; i<quene.length-1; i++){
            Pair<Integer,Double> element = compareList.get(i);
            quene[i+1] = consumer[element.getKey()];
            clusterIds.remove(element.getKey());
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
    
    public void printStatistics(){
        System.out.println("Parents size "+numParents);
        System.out.println("Children "+children);
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
