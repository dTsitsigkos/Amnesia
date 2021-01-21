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

import java.util.HashMap;
import algorithms.flash.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import data.Data;
import hierarchy.Hierarchy;

/**
 * A frequency set
 * @author serafeim
 */
public class Buffer {
    Data data = null;
    Map<Integer, Hierarchy> hierarchies = null;
    Map<GeneralizedRow, Integer> frequencies = new HashMap<>();
    int counterTimes = -1;
    
    public Buffer(Data _data,  Map<Integer, Hierarchy> _hierarchies){
        hierarchies = _hierarchies;
        data = _data;
    }
    
    /**
     * computes the frequency set for the specified root graph node
     * @param node the node
     * @param qidColumns
     * @param pool
     * @param parallelism
     */
    public void compute(LatticeNode node, int[] qidColumns, ForkJoinPool pool, int parallelism){
        int datasetLength = data.getDataLenght();
        int numberOfThreads = (datasetLength < parallelism) ? datasetLength : parallelism;
        int splitSize = (datasetLength % numberOfThreads == 0) ? datasetLength / numberOfThreads : datasetLength / numberOfThreads + 1;
                
        Worker worker = null;
        for (int i = 0; i < datasetLength; i += splitSize){
            int start = i;
            int end = (splitSize < datasetLength - i) ? i + splitSize : datasetLength;
                        
            worker = new Worker(data,hierarchies,node,null,null,null,qidColumns,start,end,worker);
            pool.execute(worker);
        }
        
        //wait all threads to finish
        for (Worker w = worker; w != null; w = w.nextJoin){       
            mergeMaps(this.frequencies, w.join());
        }
    }
    
    /**
     * Determines if the frequency set is k-Anonymous with respect to k
     * @param k the parameter k of k-Anonymity
     * @return true if the frequency set is k-Anonymous, false otherwise
     */
    public boolean isKAnonymous(int k){
        boolean isAnonymous = true;
        
        for(GeneralizedRow distinctRow : frequencies.keySet()){
            Integer count = frequencies.get(distinctRow);
            if(count < k){
                isAnonymous = false;
                break;
            }
        }
        
        return isAnonymous;
    }
    
    
    GeneralizedRow[] convertKeyset(Buffer parentNodeBuffer){
        //convert parentNodeBuffer's keyset to array for concurrent access
        Set<GeneralizedRow> keyset = parentNodeBuffer.getFrequencies().keySet();
        return keyset.toArray(new GeneralizedRow[keyset.size()]);
    }
    
    /**
     * Computes frequency set from the parent's frequency set (for non-root nodes)
     * @param node a generalization graph node
     * @param parentNode
     * @param parentNodeBuffer
     * @param qidColumns
     * @param pool
     * @param parallelism
     */
    public void compute(LatticeNode node, LatticeNode parentNode, Buffer parentNodeBuffer, int[] qidColumns, ForkJoinPool pool, int parallelism) {
        long computeStartTime = System.currentTimeMillis();
        GeneralizedRow[] keysetArray = convertKeyset(parentNodeBuffer);
        
        //compute parameters for splitting
        int datasetLength = keysetArray.length;
        int numberOfThreads = (datasetLength < parallelism) ? datasetLength : parallelism;
        int splitSize = (datasetLength % numberOfThreads == 0) ? datasetLength / numberOfThreads : datasetLength / numberOfThreads + 1;
        
        Worker worker = null;
        for (int i = 0; i < datasetLength; i += splitSize){
            int start = i;
            int end = (splitSize < datasetLength - i) ? i + splitSize : datasetLength;
            worker = new Worker(data,hierarchies,node,parentNode,parentNodeBuffer,keysetArray,qidColumns,start,end,worker);
            pool.execute(worker);
        }
        
        long waitTime = System.currentTimeMillis();
         counterTimes ++;
        //wait all threads to finish
        for (Worker w = worker; w != null; w = w.nextJoin){
            mergeMaps(this.frequencies, w.join());
        }
    }
    
    public int getSize(){
        return this.frequencies.size();
    }
    
    /**
     * Getter of frequencies map
     * @return frequencies map
     */
    public Map<GeneralizedRow, Integer> getFrequencies() {
        return frequencies;
    }
    
    private void mergeMaps(Map<GeneralizedRow, Integer> mainMap,
        Map<GeneralizedRow, Integer> secondaryMap){

        for(GeneralizedRow gRow : secondaryMap.keySet()){
            Integer mainMapValue = mainMap.get(gRow);
            
            if(mainMapValue == null){
                mainMap.put(gRow, secondaryMap.get(gRow));
            }
            else{
                mainMap.put(gRow, secondaryMap.get(gRow) + mainMapValue);
            }
        }
    }
    
}
