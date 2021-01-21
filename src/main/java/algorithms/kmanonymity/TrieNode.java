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
package algorithms.kmanonymity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author serafeim
 */
public class TrieNode {
    
    double value;
    int support = 0;
    Map<Double, TrieNode> children = new TreeMap<>(Collections.reverseOrder());
    boolean isLeaf = false;
    TrieNode parent = null;
    
    public TrieNode() {}
    
    public TrieNode(double value){
        this.value = value;
    }
    
    public int getSupport(){
        return support;
    }

    public double getValue() {
        return value;
    }

    public Map<Double, TrieNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return isLeaf;
    }
    
    @Override
    public String toString(){
        return "[" + value  + " ( " + support + " )]";
    }
    
    public void setLeaf(boolean value){
        isLeaf = value;
    }
    
    public void addChild(Double value, TrieNode node){
        children.put(value, node);
        isLeaf = false;
    }
    
    public void setParent(TrieNode _parent){
        parent = _parent;
    }

    public TrieNode getParent(){
        return parent;
    }
    
    public List<Double> getItemset(){
        List<Double> path = new ArrayList<>();
        path.add(this.value);
        TrieNode _parent = parent;
        
        while(_parent.getValue() != -1){
            path.add(_parent.value);
            _parent = _parent.getParent();
        }
        
        return path;
    }
    
}
