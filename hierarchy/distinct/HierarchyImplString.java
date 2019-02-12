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
package hierarchy.distinct;

import com.fasterxml.jackson.annotation.JsonView;
import data.Data;
import data.SETData;
import dictionary.DictionaryString;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.Hierarchy;
import hierarchy.NodeStats;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsoninterface.View;


/**
 *
 * @author jimakos
 */
public class HierarchyImplString implements Hierarchy<String> {
    String inputFile = null;
    String name = null;
    String nodesType = null;
    String hierarchyType = "distinct";
    int height = -1;
    BufferedReader br = null;
    //@JsonView(View.Hier.class)
    String root = null;
    DictionaryString dict = null;
    
    //@JsonView(View.Hier.class)
    Map<String, List<String>> children = null;
    Map<String, NodeStats> stats = null;    
    Map<String, String> parents = null;
//    Map<String, List<String>> siblings = new HashMap<>();
    //@JsonView(View.Hier.class)
    Map<Integer, ArrayList<String>> allParents = null;
    Map<Integer, List<Integer>> allParentIds = null;
    
    Map<Integer, Integer> parentsInteger = null;

    
    
    public HierarchyImplString(String inputFile){
        this.inputFile = inputFile;
        
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
//    Map<String, List<String>> siblings = new HashMap<>();
        allParents = new HashMap<>();
        allParentIds = new HashMap<>();
        parentsInteger = null;
    }
    
    public HierarchyImplString(String _name, String _nodesType){
        this.name = _name;
        this.nodesType = _nodesType;
//        this.hierarchyType = "distinct";
        
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
//    Map<String, List<String>> siblings = new HashMap<>();
        allParents = new HashMap<>();
        allParentIds = new HashMap<>();
        parentsInteger = null;
    }

    public void setNodesType(String nodesType) {
        this.nodesType = nodesType;
    }
    
    @Override
    public void load(){
        try {
            br = new BufferedReader(new FileReader(this.inputFile));
            processingMetadata();
            loadHierarchy();
            findAllParents();
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(" i am here");
        findAllParents();
    }
    
    private void loadHierarchy() throws IOException{
        String line;
        int curLevel = this.height - 1;
        
        while ((line = br.readLine()) != null) {
            String tokens[] = line.split(" ");
            if(line.trim().isEmpty()){
                curLevel--;
                continue;
            }
            boolean isChild = false;
            List<String> ch = new ArrayList<>();
            for (String token : tokens){
                if(token.equals("has")){
                    isChild = true;
                    continue;
                }
                if(isChild){
                    //System.out.println(token);
                    ch.add(token);
                    this.stats.put(token, new NodeStats(curLevel));
                    this.parents.put(token, tokens[0]);
                }
                else{
                    this.stats.put(token, new NodeStats(curLevel-1));
                    
                    //level 0 and isChild == false then set as root
                    if(curLevel - 1 == 0){
                        root = tokens[0];
                    }
                }
                
                //System.out.println(token + ": " + isChild + " "  + curLevel);
                
            }
            this.children.put(tokens[0], ch);
            
            //set siblings
//            for(String child : ch){
//                List<String> sib = new ArrayList<>(ch);
//                sib.remove(child);
//                this.siblings.put(child, sib);
//            }
        }
    }
    
    private void processingMetadata() throws IOException{
        
        String line;
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            if(line.trim().isEmpty())
                break;
            
            //System.out.println("Metadata: " + line);
            String[] tokens = line.split(" ");
            if(tokens[0].equalsIgnoreCase("name")){
                this.name = tokens[1];
            }
            else if(tokens[0].equalsIgnoreCase("type")){
                this.nodesType = tokens[1];
            }
            else if(tokens[0].equalsIgnoreCase("height")){
                this.height = Integer.parseInt(tokens[1]);
            }
        }
    }
    
    
    @Override
    public void findAllParents(){
        List<String> tempChild = null;
        int i = 0;
        int level = 0;
        ArrayList<String> tempArr1 = new ArrayList<>();
        ArrayList<String> tempArr2 = new ArrayList<>();
        
        tempArr1.add(root);
        allParents.put(level, tempArr1);
        tempArr2 = (ArrayList<String>) tempArr1.clone();
        level ++;
        
        while (level <= height - 1 ){
            tempArr1 = new ArrayList<>();
            for (String x : tempArr2) {
                tempChild = children.get(x);
                if ( tempChild != null){
                    for ( i = 0 ; i < tempChild.size() ; i ++ ){
                        tempArr1.add(tempChild.get(i));
                    }
                }
            }
            allParents.put(level, tempArr1);
            tempArr2 = (ArrayList<String>) tempArr1.clone();
            level ++;
        }
    }
    
    
    @Override
    public List<String> getChildren(String parent){
        return this.children.get(parent);
    }
    
    
    @Override
    public Integer getLevel(String node){
        NodeStats nodeStats = this.stats.get(node);
        if(nodeStats == null){
            System.out.println("Error: stats for node " + node + " cannot be found!");
            return null;
        }
        
        return nodeStats.getLevel();
    }
    
    
    @Override
    public String getParent(String node){
        return this.parents.get(node);
    }
    
    
//    @Override
//    public List<String> getSiblings(String node) {
//        return this.siblings.get(node);
//    }
    
    
    public int[][] getHierarchy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void setHierarchy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public int getHierarchyLength() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @Override
    public String getRoot(){
        return root;
    }
    
    @Override
    public String getName(){
        return this.name;
    }
    
    
    @Override
    public Map<Integer, ArrayList<String>> getAllParents() {
        return allParents;
    }
    
    
    public void export(String file) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("distinct");
            writer.println("name " + this.name);
            writer.println("type " + this.nodesType);
            writer.println("height " + this.height);
            writer.println();
            int counter = 1;
            
            //write parents - childen to file
            for(int curLevel = height - 2; curLevel >= 0; curLevel--){
                //System.out.println("i = " + curLevel + "\t children = " + this.allParents.get(curLevel).toString() );
                //List<String> p = this.allParents.get(curLevel);
                for (String curParent : this.allParents.get(curLevel)){
                    if(this.getChildren(curParent) == null){
                        continue;
                    }
                    if(this.getChildren(curParent).isEmpty())
                        continue;
                    StringBuilder sb = new StringBuilder();
                    for (String child : this.getChildren(curParent)){
                        sb.append(child);
                        sb.append(" ");
                    }
                    writer.println(curParent + " has " + sb.toString());
                }
                
                counter ++;
                writer.println();
                
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    public String getNodesType() {
        return nodesType;
    }
    
    
    @Override
    public void add(String newObj, String parent) {
        System.out.println("add () newItem: "  + newObj.toString() + " parentItem: " + parent.toString());
        
        
        
        
        if(parent != null){
            this.stats.put(newObj, new NodeStats(this.stats.get(parent).getLevel()+1));
            
            
            /* System.out.println("before");
            for (Map.Entry<Double, Double> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            }*/
            
            this.parents.put(newObj, parent);
            
            /*System.out.println("after");
            for (Map.Entry<Double, Double> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            }*/
            
            
            //add siblings
//            List<Double> cList = this.children.get(parent);
//            List<Double> sibs = new ArrayList<>();
//            if(cList != null)
//                sibs.addAll(cList);
//            this.siblings.put(newObj, sibs);
//            for(Double sib : sibs){
//                this.siblings.get(sib).add(newObj);
//            }
            
            
            /*System.out.println("before");
            for (Map.Entry<Double, List<Double>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            }*/
            
            this.children.put(newObj, new ArrayList<String>());
            if(this.children.get(parent) != null){
                this.children.get(parent).add(newObj);
            }else{
                ArrayList<String> c = new ArrayList<>();
                c.add(newObj);
                this.children.put(parent, c);
            }
            
            /*System.out.println("after");
            for (Map.Entry<Double, List<Double>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            }*/
            
            
            //System.out.println(height + " " + this.levels.get(parent));
            
            
            //parent is a leaf node
            
            
            if (this.stats.get(parent).getLevel() == allParents.size() - 1){
                ArrayList<String> p = new ArrayList<>();
                p.add(newObj);
                this.allParents.put(this.stats.get(parent).getLevel() + 1, p);
                this.height++;
                
            }
            else{
                ArrayList<String> childList = allParents.get(this.stats.get(parent).getLevel() + 1);
                childList.add(newObj);
                allParents.put(this.stats.get(parent).getLevel() + 1, childList);
                
            }
            
            /*System.out.println("after");
            for (Map.Entry<Integer, ArrayList<Double>> entry : allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
            }*/
            
        }
        else{
            System.out.println("Error: parent is null!");
        }
    }
    
    private boolean isLeafLevel(ArrayList<String> parentsInLevel){
        boolean  leafLevel = true;
        
        for (String parent : parentsInLevel){
            List<String> ch = this.children.get(parent);
            if(ch != null && ch.size() > 0){
                leafLevel = true;
                break;
            }
        }
        return leafLevel;
    }
    
    @Override
    public Map<Integer, Set<String>> remove(String item)
    {
        Map<Integer, Set<String>> nodesMap = BFS(item,null);
        for(Integer i = nodesMap.keySet().size() ; i > 0 ; i--)
        {
            //System.out.println(i + "-> " + nodesMap.get((i)));
            
            for(String itemToDelete : nodesMap.get(i)){
                //System.out.println(itemToDelete.toString());
                if(itemToDelete.equals(root)){
                    System.out.println("Cannot remove root");
                    return null;
                }
                children.remove(itemToDelete);
                children.get(getParent(itemToDelete)).remove(itemToDelete);
                parents.remove(itemToDelete);
//                List<String> sibs = siblings.get(itemToDelete);
//                if(sibs != null){
//                    for(String sib : sibs){
//                        if(siblings.get(sib) != null)
//                            siblings.get(sib).remove(itemToDelete);
//                    }
//                }
//                siblings.remove(itemToDelete);
                List<String> p = allParents.get(stats.get(itemToDelete).level);
                p.remove(itemToDelete);
                if(p.isEmpty()){
                    allParents.remove(stats.get(itemToDelete).level);
                    height--;
                }
                stats.remove(itemToDelete);
            }
            
        }
        return nodesMap;
    }
    
    
    
    
    @Override
    public void clear() {
        //System.out.println("model clear");
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
//        siblings = new HashMap<>();
        allParents = new HashMap<>();
        children.put(root, new ArrayList<String>());
        stats.put(root, new NodeStats(0));
        //System.out.println("model : " + root.toString());
        /*
        ArrayList<Object> tList = new ArrayList<>();
        tList.add(root);
        allParents.put(0, tList);
        */
        height = 1;
    }
    
    
    
    @Override
    public void edit(String oldValue, String newValue){
        //update children map
        String parent = null;
        ArrayList parentsList = null;
        List<String> childrenListNew = null;
        
        /*System.out.println(this.stats.get(oldValue).getLevel());
        System.out.println("before");
        for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        
        List<String> childrenList = this.children.get(oldValue);
        if(childrenList != null){//node
            if ( allParents.get(0).get(0).equals(oldValue)){
                //System.out.println("root");
                
                //children
                this.children.put(newValue, childrenList);
                this.children.remove(oldValue);
                
                //parents
                for ( int i = 0; i < childrenList.size() ; i ++ ){
                    this.parents.remove(childrenList.get(i));
                    this.parents.put(childrenList.get(i), newValue);
                }
                
                //allParents
                parentsList = allParents.get(0);
                parentsList.remove(0);
                parentsList.add(newValue);
                allParents.put(0,parentsList);
            }
            else{
                // System.out.println("node");
                
                //children structure
                //its children
                this.children.put(newValue, childrenList);
                this.children.remove(oldValue);
                
                //his father children
                parent = this.parents.get(oldValue);
                childrenListNew = this.children.get(parent);
                for ( int i = 0 ; i < childrenListNew.size() ; i ++ ){
                    if ( childrenListNew.get(i).equals(oldValue)){
                        childrenListNew.remove(i);
                        childrenListNew.add(newValue);
                        this.children.put(parent, childrenListNew);
                        break;
                    }
                }
                
                //parent structure
                //its parent
                this.parents.put(newValue, parent);
                this.parents.remove(oldValue);
                
                //its children's father
                for ( int i = 0; i < childrenList.size() ; i ++ ){
                    this.parents.remove(childrenList.get(i));
                    this.parents.put(childrenList.get(i), newValue);
                }
                
                //allParents
                parentsList = allParents.get(this.stats.get(oldValue).getLevel());
                for ( int i = 0 ; i < parentsList.size() ; i ++ ) {
                    if (parentsList.get(i).equals(oldValue)){
                        parentsList.remove(i);
                        parentsList.add(newValue);
                        allParents.put(this.stats.get(oldValue).getLevel(), parentsList);
                        break;
                    }
                }
                
            }
            
        }
        else{//leaf
            
            //children
            parent = this.getParent(oldValue);
            childrenListNew = this.children.get(parent);
            for ( int i = 0 ; i < childrenListNew.size() ; i++ ){
                if ( childrenListNew.get(i).equals(oldValue)){
                    childrenListNew.remove(i);
                    childrenListNew.add(newValue);
                    this.children.put(parent, childrenListNew);
                    break;
                }
            }
            
            //parents
            this.parents.put(newValue,parent);
            this.parents.remove(oldValue);
            
            
            //allParents
            parentsList = allParents.get(allParents.size()-1);
            for( int i = 0 ; i < parentsList.size() ; i ++ ){
                if (parentsList.get(i).equals(oldValue)){
                    parentsList.remove(i);
                    parentsList.add(newValue);
                    allParents.put(allParents.size()-1, parentsList);
                    break;
                }
            }
            
        }
        
        this.stats.put(newValue, this.stats.get(oldValue));
        this.stats.remove(oldValue);
        
        /*System.out.println("after");
        for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        
        System.out.println("after");
        for (Map.Entry<Double, Double> entry : this.parents.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("after");
        for (Map.Entry<Integer, ArrayList<Double>> entry : this.allParents.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("after");
        for (Map.Entry<Double, NodeStats> entry : this.stats.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        
        /*
        if(parent != null){
        int index = this.children.get(this.getParent(oldValue)).indexOf(oldValue);
        this.children.get(this.getParent(oldValue)).set(index, newValue);
        
        //update parents map
        Double parent = this.parents.get(oldValue);
        
        this.parents.remove(oldValue);
        this.parents.put(newValue, parent);
        
        
        }
        
        if(childrenList != null){
        for(Double child : childrenList){
        this.parents.put(child, newValue);
        }
        }
        
        //        if(this.getSiblings(oldValue) != null){
        //            List<Double> mySiblings = this.siblings.get(oldValue);
        //            this.siblings.remove(oldValue);
        //            this.siblings.put(newValue, mySiblings);
        //            for (Double sib : mySiblings){
        //                System.out.println("sibling : " + sib);
        //                int i = this.siblings.get(sib).indexOf(oldValue);
        //                System.out.println(i);
        //                if(i != -1){        //TODO: fix this!
        //                    this.siblings.get(sib).set(i, newValue);
        //                }
        //
        //            }
        //        }
        
        //update allParents
        ArrayList<Double> parentsInLevel = this.allParents.get(this.stats.get(oldValue).getLevel());
        
        if(parentsInLevel != null){
        int i = parentsInLevel.indexOf(oldValue);
        if(i != -1){         //parent not found
        System.out.println("to i : " + i + " oldvalue : " + oldValue.toString());
        parentsInLevel.set(i, newValue);
        }
        }
        
        //update levels
        this.stats.put(newValue, this.stats.get(oldValue));
        this.stats.remove(oldValue);
        */
    }
    
    
    @Override
    public boolean contains(String o){
        return stats.get(o) != null;
    }
    
    
    @Override
    public Map<Integer,Set<String>> dragAndDrop(String firstObj, String lastObj) {
        String parentFirstObj ;
        ArrayList childs1 = null;
        ArrayList childs2 = null;
        ArrayList simb1 = null;
        ArrayList simb2 = null;
        ArrayList parents = null;
        int levelFirstObj;
        int levelLastObj;
        int levelObj;
        int newLevel;
        Set<String> s = null;
        NodeStats nodeStat = null;
        
        
        
        //System.out.println("Drag and Dropppppppp");
        
        /*System.out.println("before");
        for (Map.Entry<String, NodeStats> entry : this.stats.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue().level +":"+entry.getValue().weight);
        } */
        
        /*System.out.println("before");
        for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue());
        List <Double> d = entry.getValue();
        for( int i = 0 ; i < d.size() ; i ++  ){
        System.out.println(d.get(i) + "level:" + this.getLevel(d.get(i)) );
        }
        }*/
        
        //System.out.println("old height = " + this.height);
        
        Map<Integer,Set<String>> m = this.BFS(firstObj,lastObj);
        
        if ( m != null ){
            
            //afairw to prwto komvo apo ta paidia(children) tou patera tou
            parentFirstObj = this.getParent(firstObj);
            childs1 = (ArrayList) this.getChildren(parentFirstObj);
            for( int i = 0 ; i < childs1.size() ; i ++ ){
                if ( childs1.get(i).equals(firstObj)){
                    childs1.remove(i);
                    break;
                }
            }
            
            
            //allazw ton parent tou prwtou paidiou
            this.parents.put(firstObj,lastObj);
            
            
            //afairw ta siblings tou prwtou komvou
//            this.siblings.remove(firstObj);
            
            
            //topothetw to prwto komvo sta swsta siblings
//            childs1 = (ArrayList) this.getChildren(lastObj);
//            simb1 = null;
//            if (childs1 != null ){
//                simb1 = new ArrayList<Double>();
//                for ( int i = 0 ; i < childs1.size() ; i++ ){
//                    simb1.add(childs1.get(i));
//                    simb2 = (ArrayList)this.getSiblings((Double) childs1.get(i));
//                    if ( simb2 != null){
//                        simb2.add(firstObj);
//                    }
//                    else{
//                        simb2 = new ArrayList<Double>();
//                    }
//                }
//            }
//            this.siblings.put(firstObj, simb1);
            
            
            //vazw ton prwto komvo sta paidia(children) tou deuterou komvou
            if ( this.getChildren(lastObj) != null ){
                this.getChildren(lastObj).add(firstObj);
            }
            else{
                childs1 = new ArrayList<Double>();
                childs1.add(firstObj);
                this.children.put(lastObj, childs1);
            }
            
            
            //diagrafw to allparents tou prwtou kai tou dentrou tou
            for ( int i = 1 ; i <= m.size() ; i ++ ){
                s = m.get(i);
                for( String node : s ){
                    //System.out.println("node = " + node );
                    levelObj = this.getLevel(node);
                    parents = this.allParents.get(levelObj);
                    // System.out.println("level = " + levelObj);
                    for( int j = 0 ; j < parents.size() ; j++ ){
                        if ( parents.get(j).equals(node)){
                            parents.remove(j);
                        }
                    }
                }
            }
            
            //topothetw ton prwto komvo kai to dentro tou sto allparents
            levelLastObj = this.getLevel(lastObj);
            
            //simb2 = this.allParents.get(levelLastObj);
            //simb2.add(lastObj);
            newLevel = levelLastObj;
            for ( int i = 1 ; i <= m.size() ; i ++ ){
                s = m.get(i);
                newLevel = newLevel + 1;
                if ( newLevel > this.allParents.size() - 1){// giati to allParents arxizei apo to miden
                    parents = new ArrayList<Double>();
                    parents.addAll(s);
                    this.allParents.put(newLevel,parents);
                    parents = null;
                }
                else{
                    parents = this.allParents.get(newLevel);
                    parents.addAll(s);
                    parents = null;
                }
            }
            
            
            
            //allazw ta level tou dentrou tou prwtou komvou
            levelFirstObj =  this.getLevel(firstObj);
            levelLastObj = this.getLevel(lastObj);
            newLevel = levelLastObj + 1;
            nodeStat = this.stats.get(firstObj);
            nodeStat.level = newLevel;
            
            for ( int i = 2 ; i <= m.size() ; i ++  ){
                newLevel = newLevel + 1;
                s = m.get(i);
                for (String d : s){
                    nodeStat = this.stats.get(d);
                    nodeStat.level = newLevel;
                }
            }
            
            //height
            this.height = allParents.size();
            //System.out.println("new height = " + this.height);
            
        }
        
        /* System.out.println("after");
        for (Map.Entry<String, NodeStats> entry : this.stats.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue().level +":"+entry.getValue().weight);
        }*/
        
        
        /*System.out.println("after");
        for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
        System.out.println(entry.getKey()+" : "+entry.getValue().toString());
        List <Double> d = entry.getValue();
        for( int i = 0 ; i < d.size() ; i ++  ){
        System.out.println(d.get(i) + "level:" + this.getLevel(d.get(i)) );
        }
        }*/
        
        return m;
    }
    
    
    @Override
    public Integer getHeight() {
        return this.height;
    }
    
    
    public Map<Integer,Set<String>> BFS(String firstnode,String lastNode){
        Map<Integer,Set<String>> bfsMap = new HashMap<>();
        LinkedList<String> listNodes = new LinkedList<String>();
        ArrayList childs1 = null;
        int counter = 1;
        int levelNode1;
        int levelNode2;
        Set s = new HashSet<String>();
        
        
        s.add(firstnode);
        bfsMap.put(counter,s);
        listNodes.add(firstnode);
        counter ++;
        levelNode1 = this.getLevel(firstnode);
        
        while (!listNodes.isEmpty()){
            childs1 = (ArrayList) this.getChildren(listNodes.getFirst());
            if ( childs1 != null && !childs1.isEmpty()){// ean exei paidia
                levelNode2 = this.getLevel((String) childs1.get(0));
                System.out.println("edw lala " + levelNode2 + " " + childs1.get(0));
                if (levelNode2 == levelNode1){// ean einai sto idio epipedo tote valta sto proigoumeno set
                    s.addAll(childs1);
                    if ( lastNode != null){
                        if (s.contains(lastNode)){
                            bfsMap = null;
                            break;
                        }
                    }
                    bfsMap.put(counter, s);
                }
                else{// ean den einai sto idio epipedo dimiourgise kainourgio set
                    s = new HashSet<String>();
                    levelNode1 = levelNode2;
                    s.addAll(childs1);
                    if ( lastNode != null ){
                        if (s.contains(lastNode)){
                            bfsMap = null;
                            break;
                        }
                    }
                    bfsMap.put(counter, s);
                    
                }
                listNodes.addAll(childs1);//add ola stin linked list
                
                if (listNodes.size() > 1){
                    if ( this.stats.get(listNodes.getFirst()).level != this.stats.get(listNodes.get(1)).level ){//ean to epomeno stoixeio tis listas exei allo level tote auksise ton counter
                        counter ++;
                    }
                }
                else{//ean uparxei mono ena stoixeio stin lista auksise ton counter
                    counter ++;
                }
            }
            
            listNodes.removeFirst();//remove to prwto stoixeio tis linkedlist giati to exoume tsekarei
        }
        
        
        return bfsMap;
        
    }
    
    
    @Override
    public void computeWeights(Data dataset, String column) {
        for(String node : this.stats.keySet()){
            NodeStats s = this.stats.get(node);
            s.weight = 0;
        }
        
        //find index of column
        Integer c;
        double[][] data = dataset.getDataSet();
        for(c  = 0 ; c < dataset.getColNamesPosition().keySet().size() ; c++){
            if(dataset.getColNamesPosition().get(c).equals(column)){
                break;
            }
        }
        
        DictionaryString dict = dataset.getDictionary(c);
        if(dict == null){
            System.out.println("No dictionary found for column " + c);
            return;
        }
        //System.out.println(c);
        
        if(dataset instanceof SETData){
            for (double[] rowData : data) {
                for(double d : rowData){
                    String fromDict = dict.getIdToString((int)d);
                    NodeStats s = this.stats.get(fromDict);
                    
                    if(s != null){      //find weights of leaf level
                        List<String> cList = this.children.get(fromDict);
                        if(cList == null || cList.isEmpty()){
                            
                            //System.out.println(rowData[c]);
                            s.weight++;
                        }
                    }
                }
            }
        }
        else{
            for (double[] columnData : data) {
                String fromDict = dict.getIdToString((int)columnData[c]);
                NodeStats s = this.stats.get(fromDict);
                
                if(s != null){      //find weights of leaf level
                    List<String> cList = this.children.get(fromDict);
                    if(cList == null || cList.isEmpty()){
                        
                        //System.out.println(rowData[c]);
                        s.weight++;
                    }
                }
            }
        }
        
        //find weights for inner nodes
        for(int j = this.allParents.keySet().size()-2 ; j>=0 ; j--){
            for(String node : this.allParents.get(j)){
                Integer totalWeight = 0;
                List<String> cList = this.children.get(node);
                if(cList != null && !cList.isEmpty()){
                    for(String child : cList){
                        totalWeight += this.stats.get(child).weight;
                    }
                    this.stats.get(node).weight = totalWeight;
                }
                
            }
        }
    }
    
    @Override
    public String checkColumn(int column, Data dataset){
        
        DictionaryString colDict = dataset.getDictionary(column);
        
        double[][] data = dataset.getDataSet();
        int dataLength = dataset.getDataLenght();
        
        //check if every value in the column is present in this hierarchy
        for(int i=0; i<dataLength; i++){
            
            //if not, return value missing
            if(!this.stats.containsKey(colDict.getIdToString((int)data[i][column]))){
                return colDict.getIdToString((int)data[i][column]);
            }
        }
        
        
        return null;
    }
    
    @Override
    public Integer getWeight(String node){
        NodeStats stats = this.stats.get(node);
        if(stats == null){
            return null;
        }
        return stats.weight;
    }
    
    
    public void incWeight(String node){
        this.stats.get(node).weight++;
    }
    
//    @Override
//    public void setHierachyType(String type) {
//        this.hierarchyType = type;
//    }
    
    @Override
    public String getHierarchyType() {
        return this.hierarchyType;
    }
    
    @Override
    public void autogenerate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean validCheck(String parsePoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public DictionaryString getDictionary() {
        return this.dict;
    }
    
    @Override
    public void transformParents(){
        parentsInteger = null;
        parentsInteger = new HashMap<>();
        for (String key : parents.keySet()) {
            parentsInteger.put(dict.getStringToId(key), dict.getStringToId(parents.get(key)));
        }
        
        
    }
    
    @Override
    public  Map<Integer,Integer> getParentsInteger(){
        return parentsInteger;
    }
    
    @Override
    public String getParent(Double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public int getLevelSize(int level) {
        return this.allParents.get(this.height - level - 1).size();
    }
    
    @Override
    public double getParentId(double d) {
        String stringValue = this.dict.getIdToString((int)d);
        String parentStringValue = this.parents.get(stringValue);
        if(parentStringValue == null){
            return -1;
        }
        else{
            return this.dict.getStringToId(parentStringValue);
        }
    }
    
    @Override
    public void buildDictionary(DictionaryString dictionary) {
        int id = dictionary.getMaxUsedId();
        
        //add values to dictionnary bottom-up
        for(int curLevel = Collections.max(this.allParents.keySet()); curLevel != -1; curLevel--){
            for(String value : this.allParents.get(curLevel)){
                if(!dictionary.containsString(value)){
                    dictionary.putIdToString(++id, value);
                    dictionary.putStringToId(value, id);
                }
            }
        }
        
        //set column's dictionary the same as hierarchy's
        this.dict = dictionary;
    }

    @Override
    public Set<Double> getChildrenIds(double d) {
//        System.out.println(this.stats.keySet().size());
        String stringValue = this.dict.getIdToString((int)d);
        List<String> childrenStrings = this.children.get(stringValue);
        Set<Double> chs = null;
        
        if(childrenStrings != null){
            chs = new HashSet<>();
//            System.out.println(childrenStrings);
            for(String child : childrenStrings){
                
                chs.add((double)this.dict.getStringToId(child));
            }
        }
        return chs;
    }

    @Override
    public Integer getLevel(double nodeId) {
        String value = this.dict.getIdToString((int)nodeId);
        return (this.height - this.getLevel(value) - 1) ;
    }

    @Override
    public List<Integer> getNodeIdsInLevel(int level) {
        int curLevel = this.height - level - 1;
        List<Integer> curLevelIds = this.allParentIds.get(curLevel);
        if(curLevelIds == null){
            
            ArrayList<String> nodesInLevel = this.getAllParents().get(curLevel);
            List<Integer> nodeIdsInLevel = new ArrayList<>(nodesInLevel.size());
            for(String str : nodesInLevel){
                nodeIdsInLevel.add(this.dict.getStringToId(str));
            }
            
            this.allParentIds.put(curLevel, nodeIdsInLevel);
            return nodeIdsInLevel;
        }
        else{
            return curLevelIds;
        }
    }

    @Override
    public Integer getWeight(double nodeId) {
        String value = this.dict.getIdToString((int)nodeId);
        return getWeight(value);
    }
    

    @Override
    public int findAllChildren(String node,int sum) {
        int result = 0;
       
        List<String> child = this.getChildren(node);
       
        if ( child == null){
            return 1;
        }

        for (int i =0 ; i < child.size() ; i ++){
            result = findAllChildren(child.get(i),sum) + result;
        }

        return result;    
    }

    @Override
    public String getInputFile() {
        return inputFile;
    }

    @Override
     public Graph getGraph(String node, int nodeLevel) {
        Graph graph = new Graph();
        Node n = null;
        Edge e = null;
        ArrayList<String> nodes = null;
        boolean FLAG = false;
        String parent = null;
        List<String> nodeChilds = null;
        int counter = 0;
        int counterNode = 0;
        String color = null;
        String label = null;
        
        System.out.println("roottttttttttttttttttttttttttt = " + root);
        
        
      
        
        if ( !node.equals("null") && !node.equals("") && nodeLevel != 0 ){
            
           
            System.out.println("i am here");
            counter = 1;
            if (height > nodeLevel + 1){
                for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(node);
                    //System.out.println(node);
                    counterNode = counter;
                    counter ++;                
                    //Collections.sort(nodeChilds);
                    if ( nodeChilds != null){
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){
                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                 color = "red";
                            }
                            else{
                                color = null;
                            }
                            
                            if (nodeChilds.get(j).equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChilds.get(j)+"";
                            }

                            n = new Node(nodeChilds.get(j),label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                            graph.setNode(n);
                            e = new Edge(node + "",nodeChilds.get(j)+"");
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    node = this.parents.get(node);
                    
                    //System.out.println("node = " + node);
                }
                n = new Node(root,root,0,null,null);
                graph.setNode(n);
                Collections.reverse(graph.getNodeList());
                Collections.reverse(graph.getEdgeList());
                
            }
            else{
                System.out.println("i am here222");
                for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(node);
                   
                    System.out.println("ola kala");
                    //Collections.sort(nodeChilds);
                    if ( nodeChilds != null){
                        counterNode = counter;
                        counter ++;
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){
                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                            }
                            else{
                                color = null;
                            }
                            
                            if (nodeChilds.get(j).equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChilds.get(j)+"";
                            }
                            n = new Node(nodeChilds.get(j),label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                            graph.setNode(n);
                            e = new Edge(node + "",nodeChilds.get(j)+"");
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    node = this.parents.get(node);
                    //System.out.println("node = " + node);
                }
                n = new Node(root,root,0,null,this.hierarchyType + "," +this.nodesType);
                graph.setNode(n);
            
            }
            
        }
        else{
            node = root;
            nodeLevel= 0;
            for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(node);
                    //Collections.sort(nodeChilds);
                    counterNode = counter;
                    counter ++;
                    if ( nodeChilds != null){
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){

                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                            }
                            else{
                                color = null;
                            }
                            
                            if (nodeChilds.get(j).equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChilds.get(j)+"";
                            }
                            n = new Node(nodeChilds.get(j),label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                            graph.setNode(n);
                            e = new Edge(node + "",nodeChilds.get(j)+"");
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    node = this.parents.get(node);
                    //System.out.println("nodeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee = " + node);
                }
                n = new Node(root,root,0,null,this.hierarchyType + "," +this.nodesType);
                graph.setNode(n);
            
            
        }
        
        //Collections.reverse(graph.getNodeList());
        //Collections.reverse(graph.getEdgeList());

        /*for ( int i = 0 ; i < graph.getNodeList().size() ; i ++){
            System.out.println(graph.getNodeList().get(i).getId() + "\t level = " + graph.getNodeList().get(i).getLevel() + "\tlabel = "+ graph.getNodeList().get(i).getLabel() );
        }
        
        
        for ( int i = 0 ; i < graph.getEdgeList().size() ; i ++){
            System.out.println("From = " + graph.getEdgeList().get(i).getFrom() + "\t to = " + graph.getEdgeList().get(i).getTo() );
            
        }*/
      
        
        
        return graph;
    }

    @Override
    public String checkHier() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getParent(Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int translateDateViaLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}   
