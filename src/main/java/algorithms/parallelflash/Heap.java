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

import algorithms.flash.*;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author serafeim
 */
public class Heap {
    private final int[] maxLevels;
    private final int[][] distinctValues;
    PriorityQueue<GridNode> queue = new PriorityQueue<>(10, new LatticeNodeComparator());
    
    Heap(int[] maxLevels, int[][] distinctValues) {
        this.maxLevels = maxLevels;
        this.distinctValues = distinctValues;
    }
    
    public boolean isEmpty(){
        return this.queue.isEmpty();
    }

    public GridNode extractMin(){
        return this.queue.poll();
    }
    
    public void add(GridNode node){
        this.queue.add(node);
    }
    
    class LatticeNodeComparator implements Comparator<GridNode>{

        @Override
        public int compare(GridNode n1, GridNode n2) {
            if(n1.getLayer() < n2.getLayer()){
                return -1;
            }
            else if(n1.getLayer() > n2.getLayer()){
                return 1;
            }
            else{
                if(n1.getAvgGeneralization(maxLevels) < n2.getAvgGeneralization(maxLevels)){
                    return -1;
                }
                else if(n1.getAvgGeneralization(maxLevels) > n2.getAvgGeneralization(maxLevels)){
                    return 1;
                }
                else{
                    if(n1.getDistinctValuesAvgGen(distinctValues) < n2.getDistinctValuesAvgGen(distinctValues)){
                        return -1;
                    }
                    else if(n1.getDistinctValuesAvgGen(distinctValues) > n2.getDistinctValuesAvgGen(distinctValues)){
                        return 1;
                    }
                }
            }
            
            return 0;
        }
    }
    
}
