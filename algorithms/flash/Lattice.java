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

//import cern.colt.Arrays;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author serafeim
 */

public class Lattice {
    private final int[] qidColumns;
    /** The levels. */
    private final LatticeNode[][] levels;
    /** The size. */
    private final int size;
    
    /**
     * Initializes a lattice.
     *
     * @param qidColumns
     * @param levels the levels
     * @param numNodes the max levels
     */
    public Lattice(final int[] qidColumns, final LatticeNode[][] levels, final int numNodes) {
        this.qidColumns = qidColumns;
        this.levels = levels;
        this.size = numNodes;
    }
    
    /**
     * Returns the bottom node.
     *
     * @return
     */
    public LatticeNode getBottom() {
        for (int i = 0; i<levels.length; i++) {
            if (levels[i].length==1){
                return levels[i][0];
            } else if (levels[i].length > 1) {
                throw new RuntimeException("Multiple bottom nodes!");
            }
        }
        throw new RuntimeException("Empty lattice!");
    }
    
    /**
     * Returns all levels in the lattice.
     *
     * @return
     */
    public LatticeNode[][] getLevels() {
        return levels;
    }
    
    /**
     * Returns the number of nodes in the lattice.
     *
     * @return
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Returns the top node.
     *
     * @return
     */
    public LatticeNode getTop() {
        for (int i = levels.length - 1; i>=0; i--) {
            if (levels[i].length==1){
                return levels[i][0];
            } else if (levels[i].length > 1) {
                throw new RuntimeException("Multiple top nodes!");
            }
        }
        throw new RuntimeException("Empty lattice!");
    }

    /**
     * Sets the property to all predecessors of the given node.
     *
     * @param node the node
     */
    public void setTagDownwards(LatticeNode node) {
        node.tag();
        for(LatticeNode predecessor : node.getPredecessors()){
            setTagDownwards(predecessor);
        }
    }
      
    /**
     * Sets the property to all successors of the given node.
     *
     * @param node the node
     * @param resultset
     */
    public void setTagUpwards(LatticeNode node, Set<LatticeNode> resultset) {
        
        node.tag();
        resultset.add(node);
        for(LatticeNode successor : node.getSuccessors()){
            setTagUpwards(successor, resultset);
        }
    }
    
    public void print(){
        System.out.println("Lattice for columns : " + Arrays.toString(this.qidColumns));
        for(int i = 0; i<levels.length; i++){
            System.out.println("level " + i);
            for(int j = 0; j<levels[i].length; j++){
                System.out.println(levels[i][j] + " " + levels[i][j].isTagged());
            }
            System.out.println();
        }
    }
    
    public int getHeight(){
        return levels.length;
    }

    public int[] getQidColumns() {
        return qidColumns;
    }
    
}