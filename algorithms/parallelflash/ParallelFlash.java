/* 
 * Copyright (C) 2015 "IMIS-Athena R.C.",
 * Institute for the Management of Information Systems, part of the "Athena" 
 * Research and Innovation Centre in Information, Communication and Knowledge Technologies.
 * [http://www.imis.athena-innovation.gr/]
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 */
package algorithms.parallelflash;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import algorithms.Algorithm;
import algorithms.flash.Lattice;
import algorithms.flash.LatticeBuilder;
import algorithms.flash.LatticeNode;
import data.Data;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.Hierarchy;

/**
 *
 * @author serafeim
 */
public class ParallelFlash implements Algorithm{
    Data dataset = null;
    Map<Integer, Hierarchy> hierarchies = null;
    Integer k = null;
    LatticeBuilder builder = null;
    Lattice lattice = null;
    int hierarchiesNum = -1;
    HistoryBuffers buffers = new HistoryBuffers(10);   
    Set<LatticeNode> resultset = new HashSet<>();
    ForkJoinPool pool = null;
    int parallelism = -1;
    
    public ParallelFlash(int parallelismNum){
        pool = new ForkJoinPool(parallelismNum);
        parallelism = pool.getParallelism();
    }
    
    public ParallelFlash(){
        pool = new ForkJoinPool();
        parallelism = pool.getParallelism();
    }
    
    @Override
    public void setDataset(Data dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public void setHierarchies(Map<Integer, Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
    }
    
    @Override
    public void setArguments(Map<String, Integer> arguments) {
        this.k = arguments.get("k");
    }
    
    @Override
    public void anonymize() {
        System.out.println("Parallel Flash running");
        hierarchiesNum = this.hierarchies.keySet().size();
        int qidColumns[] = new int[hierarchiesNum];
        int minLevels[] = new int[hierarchiesNum];
        int maxLevels[] = new int[hierarchiesNum];
        int distinctValues[][] = new int[hierarchiesNum][];
        int count = 0;
        
        for(Integer column : this.hierarchies.keySet()){
            Hierarchy h = this.hierarchies.get(column);
            
            //store QI columns and min-max levels
            qidColumns[count] = column;
            minLevels[count] = 0;
            maxLevels[count] = getHierarchyHeight(h)-1;
            
            //compute distinct values in hierarchy
            distinctValues[count] = new int[getHierarchyHeight(h)];
            findHierarchyDistinctValues(h, distinctValues[count]);
            count++;
        }
                
        //build lattice
        builder = new LatticeBuilder(qidColumns, minLevels, maxLevels);
        lattice = builder.build();
        Heap heap = new Heap(maxLevels, distinctValues);
        Sorting sorter = new Sorting(maxLevels, distinctValues);
        
        //outer loop of Flash algorithm
        for(int level = 0; level <= lattice.getHeight()-1; level++){
            for(LatticeNode node : sorter.sort(lattice.getLevels()[level])){
                if(!node.isTagged()){
                    LatticeNode[] path = findPath(node, maxLevels, distinctValues);
                    checkPath(path, heap);
                    while(!heap.isEmpty()){
                        node = heap.extractMin();
                        for(LatticeNode successor : sorter.sort(node.getSuccessors())){
                            if(!successor.isTagged()){
                                path = findPath(successor, maxLevels,distinctValues);
                                checkPath(path, heap);
                            }
                        }
                    }
               }
            }
        }
        System.out.println("Results : " + this.resultset);
    }
    
    public void checkPath(LatticeNode[] path, Heap heap){
        int low = 0;
        int high = path.length-1;
        
        while(low <= high){
            int mid = (low + high) / 2;
            if((low + high) % 2 > 0)
                mid++;
            
            LatticeNode midNode = path[mid];
            if(checkAndTag(midNode)){
                high = mid - 1;
            }
            else{
                heap.add(midNode);
                low = mid + 1;
            }
        }
    }
    
    public LatticeNode[] findPath(LatticeNode node, int[] maxLevels, int[][] distinctValues){
        List<LatticeNode> path = new ArrayList<>();
        Sorting sorter = new Sorting(maxLevels, distinctValues);
        
        while(true){
             LatticeNode headNode = head(path);
             if(headNode != null && headNode == node)
                 break;

            path.add(node);

            for(LatticeNode upNode : sorter.sort(node.getSuccessors())){
                if(!upNode.isTagged()){
                    node = upNode;
                    break;
                }
            }
        }
        
        return path.toArray(new LatticeNode[path.size()]);
    }
    
    private LatticeNode head(List<LatticeNode> path){
        if(path.size() > 0)
            return path.get(path.size()-1);
        return null;
    }
    
    public boolean checkAndTag(LatticeNode node){
        Buffer curBuffer = null;

        LatticeNode bestNode = this.buffers.findClosestNode(node);
        
        if (bestNode != null){
            Buffer bestNodeBuffer = this.buffers.get(bestNode);
            curBuffer = new Buffer(this.dataset, this.hierarchies);
            curBuffer.compute(node, bestNode, bestNodeBuffer, this.lattice.getQidColumns(), this.pool, parallelism);
        }
        else{
            //compute frequency set from dataset
            curBuffer = new Buffer(this.dataset, this.hierarchies);
            curBuffer.compute(node, this.lattice.getQidColumns(), this.pool, parallelism);
            
        }
        
        //check if node is k-anonymous
        if(curBuffer.isKAnonymous(this.k)){
            lattice.setTagUpwards(node, this.resultset);
            return true;
        }
        else{
            //put current buffer node to history
            this.buffers.put(node, curBuffer);
            lattice.setTagDownwards(node);
            return false;
        }
    }
        
    private int getHierarchyHeight(Hierarchy h){   
        if(h.getHierarchyType().equals("range")){
            return h.getHeight() + 1;   //as range's leaf level is not present in the hierarchy
        }
        
        return h.getHeight();
    }
    
    private void findHierarchyDistinctValues(Hierarchy h, int[] distinctValues){
        if(h.getHierarchyType().equals("distinct")){
            for(int level=0; level < distinctValues.length; level++){
                distinctValues[level] = h.getLevelSize(level);
            }
        }
        else{
            for(int level=1; level < distinctValues.length; level++){
                distinctValues[level] = h.getLevelSize(level-1);
            }
            
            //for the leaf nodes set the count of the rows
            distinctValues[0] = this.dataset.getDataLenght();
        }
    }
    
    @Override
    public Set<LatticeNode> getResultSet() {
        return this.resultset;
    }

    @Override
    public boolean isAnonymousResult(LatticeNode node) {
        return this.resultset.contains(node);
    }

    @Override
    public Graph getLattice() {
        LatticeNode[][] nodesArray = this.lattice.getLevels();
        int k = 0;
        String[] attrNames = new String[hierarchies.size()];
        for (Map.Entry<Integer, Hierarchy> entry : hierarchies.entrySet()) {
            System.out.println(entry.getKey()+" : "+dataset.getColumnByPosition(entry.getKey()));
            attrNames[k] = dataset.getColumnByPosition(entry.getKey());
            k++;
        }
        
        Graph graph = new Graph();
        int edgeNum = 0;
        for(int i=0; i<nodesArray.length; i++){
        
            //sort nodes of level
            Arrays.sort(nodesArray[i], new Comparator<LatticeNode>() {
                @Override
                public int compare(LatticeNode node1, LatticeNode node2) {
                    int[] transformation1 = node1.getTransformation();
                    int[] transformation2 = node2.getTransformation();
                    
                    for(int i=0; i<transformation1.length; i++){
                        if(transformation1[i] < transformation2[i]){
                            return 1;
                        }
                        else if(transformation1[i] > transformation2[i]){
                            return -1;
                        }
                    }
                    return 0;
                }
            });
            
            
            for(int j=0; j<nodesArray[i].length; j++){
                
                LatticeNode curNode = nodesArray[i][j];
                char []nodesArr = nodesArray[i][j].toString().toCharArray();
                ArrayList<String> arrLevel = new ArrayList<String>();
                for ( k = 1; k < nodesArr.length ; k = k + 3){
                    arrLevel.add(String.valueOf(nodesArr[k]));
                }
                
                if (isAnonymousResult(nodesArray[i][j])){
                    if (nodesArray[i][j].toString().contains(",")){
                        String str = null;
                        for ( k = 0 ; k < attrNames.length; k ++ ){
                            if ( k == 0){
                                str = "\""+attrNames[k] +"\" generalized to level " + arrLevel.get(k)+"\n";
                            }
                            else{
                                str = str + ", \""+attrNames[k] +"\" generalized to level " + arrLevel.get(k) +"\n";
                            }
                            
                        }
                       graph.setNode(new Node(nodesArray[i][j].toString(),nodesArray[i][j].toString(),i,"lightblue",str));

                    }
                    else{
                        graph.setNode(new Node(nodesArray[i][j].toString(),nodesArray[i][j].toString(),i,"lightblue","Quasi Identifier : \""+attrNames[0] +"\" generalized to level " + arrLevel.get(0)));
                    }
                }
                else{
                    if (nodesArray[i][j].toString().contains(",")){
                        String str = null;
                        for ( k = 0 ; k < attrNames.length; k ++ ){
                            if ( k == 0){
                                str = "\""+attrNames[k] +"\" generalized to level " + arrLevel.get(k)+"\n";
                            }
                            else{
                                str = str + ", \""+attrNames[k] +"\" generalized to level " + arrLevel.get(k)+"\n";
                            }
                            
                        }
                        graph.setNode(new Node(nodesArray[i][j].toString(),nodesArray[i][j].toString(),i,"red",str));
                    }
                    else{
                        graph.setNode(new Node(nodesArray[i][j].toString(),nodesArray[i][j].toString(),i,"red","Quasi Identifier : \""+attrNames[0] +"\" generalized to level " + arrLevel.get(0)));
                    }
                }
                
                LatticeNode[] successors = curNode.getSuccessors();
                Arrays.sort(successors, new Comparator<LatticeNode>() {
                    @Override
                    public int compare(LatticeNode node1, LatticeNode node2) {
                        int[] transformation1 = node1.getTransformation();
                        int[] transformation2 = node2.getTransformation();

                        for(int i=0; i<transformation1.length; i++){
                            if(transformation1[i] < transformation2[i]){
                                return 1;
                            }
                            else if(transformation1[i] > transformation2[i]){
                                return -1;
                            }
                        }
                        return 0;
                    }

                });
                for(LatticeNode suc : successors){
                    graph.setEdge(new Edge(curNode.toString(), suc.toString()));
                    edgeNum++;
                }
            }
        }
        return graph;
    }

}
