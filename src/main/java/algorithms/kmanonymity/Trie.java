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

import hierarchy.Hierarchy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author serafeim
 */
public class Trie {
    
    int maxLevel = -1;
    Hierarchy h = null;
    TrieNode root;
    Stack toVisit = new Stack();
    
    public Trie(Hierarchy h){
        this.h = h;
        root = new TrieNode(-1);
    }
    
    public void insert(double[] word) {
        Map<Double, TrieNode> children = root.children;
        TrieNode curNode = root;
        
        for(int i=0; i<word.length; i++){
            double c = word[i];
            TrieNode t;
            
            if(children.containsKey(c)){
                t = children.get(c);
            }
            else{
                t = new TrieNode(c);
                curNode.addChild(c, t);
                t.setParent(curNode);
            }
            
            children = t.children;
            curNode = t;
            
            //set leaf node
            if(i==word.length-1){
                t.support++;
                if(t.getChildren().isEmpty()){
                    t.setLeaf(true);
                }
            }
            
        }
    }
    
    public TrieNode searchNode(Set<Double> path){
        Map<Double, TrieNode> children = root.children;
        TrieNode t = null;
        
        for(Double c : path){
            if(children.containsKey(c)){
                t = children.get(c);
                children = t.children;
            }else{
                return null;
            }
        }
        
        return t;
    }
    
    public TrieNode searchNode(double[] path){
        Map<Double, TrieNode> children = root.children;
        TrieNode t = null;
        
        for(int i=path.length-1; i>=0; i--){
            if(path[i] == -1)
                return t;
            if(children.containsKey(path[i])){
                t = children.get(path[i]);
                children = t.children;
            }
            else{
                return null;
            }
        }
        
        return t;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    } 
    
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    public void printTree(TrieNode curNode){
        //System.out.println("curNode = " + curNode);
        Map<Double, TrieNode> children = curNode.getChildren();
        for(Double ch : children.keySet()){
            System.out.println(children.get(ch));
            printTree(children.get(ch));
        }
    }
    
    public TrieNode getRoot(){
        return root;
    }
    
    /**
     * returns nodes in pre order traversal
     * @return next node in pre order traversal, null if all nodes are visited
     */
    public TrieNode preorderNext(){
        if(toVisit == null){
            toVisit = new Stack<>();
            return null;
        }
        
        Map<Double, TrieNode> children;
        if(toVisit.isEmpty()){
            children = root.getChildren();
            for(double d : children.keySet()){
                toVisit.push(children.get(d));
            }
        }
        
        TrieNode curNode = (TrieNode)toVisit.pop();
        children = curNode.getChildren();
        
        for(double d : children.keySet()){
            toVisit.push(children.get(d));
        }
        
        if(toVisit.isEmpty()) toVisit = null;
        return curNode;
    }
    
    public Set<TrieNode> getSubTreeOf(TrieNode node){
        
        if(node.getChildren() == null || node.getChildren().isEmpty())
            return null;
        
        Set<TrieNode> subtree = new HashSet<>();
        
        Stack<TrieNode> stack = new Stack<>();
        
        //add initial children to stack
        Map<Double, TrieNode> ch = node.getChildren();
        for(double d : ch.keySet()){
            stack.push(ch.get(d));
        }
        
        while(!stack.isEmpty()){
            
            //add current node to results
            TrieNode curNode = stack.pop();
            subtree.add(curNode);
            
            //add children of current node to stack
            Map<Double, TrieNode> chs = curNode.getChildren();
            for(double d : chs.keySet()){
                stack.push(chs.get(d));
            }
        }
        
        return subtree;
    }
    
    public void combineAndAdd(double[] arr, int r) {
        double[] res = new double[r];       
        doCombine(arr, res, 0, 0, r);   
    }
    
    private void doCombine(double[] arr, double[] res, int currIndex, int level, int r) {
        if(level == r){
            insert(res);
            return;
        }
        for (int i = currIndex; i < arr.length; i++) {
            res[level] = arr[i];
            doCombine(arr, res, i+1, level+1, r);
            //way to avoid printing duplicates
            if(i < arr.length-1 && arr[i] == arr[i+1]){
                i++;
            }
        }
    }
    
}
