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
package algorithms.flash;

import java.util.Arrays;

/**
 *
 * @author serafeim
 */

public class GridNode {
    public  int id;
    private int layer;
    private Integer[] transf;
    private GridNode[] bottomNodes;
    private GridNode[] topNodes;
    private int downIdx;
    private int upIdx;
    
    
    private boolean tagged = false;
    
    public GridNode(int id) {
        this.id = id;
        this.downIdx = 0;
        this.upIdx = 0;
    }
    
    public int getLayer() {
        return this.layer;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) { 
            return true; 
        }
        else if (obj == null) { 
            return false; 
        }
        else if (this.getClass() != obj.getClass()) {
            return false; 
        }
        
        return Arrays.equals(transf, ((GridNode) obj).transf);
    }

    
    
    
    public double getAvgGeneralization(int[] maxLevels){
        double sum = 0.0;
        
        for(int i=0; i < this.transf.length; i++){
            sum += this.transf[i] / (double) maxLevels[i];
        }
        return sum / (double)this.transf.length;
    }
    
    public double getDistinctValuesAvgGen(int[][] distinctValues) {
        double sum = 0.0;
        
        for(int i=0; i < this.transf.length; i++){
            sum += distinctValues[i][this.transf[i]] / (double)distinctValues[i][0];
        }  
        
        sum /= this.transf.length;
       
        return 1-sum;
    }
    
    
    public GridNode[] getTopNodes() {
        return this.topNodes;
    }
       
    
    public GridNode[] getBottomNodes() {
        return this.bottomNodes;
    }
       
    
    
       
    public int[] getTransformation() {
        return Arrays.stream(transf).mapToInt(Integer::intValue).toArray();
    }
       
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(transf);
    }
    
    protected void setTopNodes(GridNode[] tn) {
        this.topNodes = tn;
    }
   
    
    public void setTransformation(Integer[] t, int layer) {
        this.transf = t;
        this.layer = layer;
    }
   
    
    protected void setBottomNodes(GridNode[] bn) {
        this.bottomNodes = bn;
    }
    
    
    
    void addTopNode(GridNode topN) {
        topNodes[upIdx] = topN;
        upIdx++;
    }  
    
    void addBottomNode(GridNode bottomN) {
        bottomNodes[downIdx] = bottomN;
        downIdx++;
    }
       
    
    
    
    @Override
    public String toString() {
        return Arrays.toString(this.transf);
    }
    
    public int[] getArray(){
       return Arrays.stream(transf).mapToInt(Integer::intValue).toArray();
    }

    public boolean isTagged() {
        return tagged;
    }

    public void tag() {
        this.tagged = true;
    }
    
}
