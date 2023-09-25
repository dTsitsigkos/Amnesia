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
import java.util.Set;

/**
 *
 * @author serafeim
 */

public class Grid {
    private  int[] qidColumns;
    private  int size;
    private  GridNode[][] layers;
    
    
   
    public Grid(final int[] qiC,  GridNode[][] l,  int nN) {
        this.qidColumns = qiC;
        this.size = nN;
        this.layers = l;
        
    }
    
       
    
    public GridNode getLowest() {
        int i=0;
        while(i<layers.length){
            if (layers[i].length==1){
                return layers[i][0];
            } 
            else if (layers[i].length > 1) {
                throw new RuntimeException("A lot of lowest nodes!");
            }
            i++;
        }
        throw new RuntimeException("Empty Grid!");
    }
    
    
    public int getSize() {
        return size;
    }
    
    public GridNode[][] getLayers() {
        return layers;
    }
    
    
    
    
    
    public GridNode getHighest() {
        int i=0;
        
        while(i>=0){
            if (layers[i].length==1){
                return layers[i][0];
            } else if (layers[i].length > 1) {
                throw new RuntimeException("A lot of highest nodes!");
            }
            i--;
        }
        throw new RuntimeException("Empty Grid!");
        
    }

    
    public void setTagDownwards(GridNode node) {
        node.tag();
        for(GridNode predecessor : node.getBottomNodes()){
            setTagDownwards(predecessor);
        }
    }
      
    
    public void setTagUpwards(GridNode node, Set<GridNode> resultset) {
        
        node.tag();
        resultset.add(node);
        for(GridNode successor : node.getTopNodes()){
            setTagUpwards(successor, resultset);
        }
    }
    
    public void print(){
        System.out.println("Lattice for columns : " + Arrays.toString(this.qidColumns));
        for(int i = 0; i<layers.length; i++){
            System.out.println("level " + i);
            for(int j = 0; j<layers[i].length; j++){
                System.out.println(layers[i][j] + " " + layers[i][j].isTagged());
            }
            System.out.println();
        }
    }
    
    public int getHeight(){
        return layers.length;
    }

    public int[] getQidColumns() {
        return qidColumns;
    }
    
}