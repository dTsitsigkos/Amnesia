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
 * @author serafeim
 */
public class HierarchyImplDouble implements Hierarchy<Double> {
    //@JsonView(View.Hier.class)
    public String inputFile = null;
    public String name = null;
    public String nodesType = null;
    public String hierarchyType = "distinct";
    public int height = -1;
    public BufferedReader br = null;
    
    //@JsonView(View.Hier.class)
    public Double root = null;

    //@JsonView(View.Hier.class)
    Map<Double, List<Double>> children = null;
    Map<Double, NodeStats> stats = null;
    Map<Double, Double> parents = null;
//    Map<Double, List<Double>> siblings = new HashMap<>();
    //@JsonView(View.Hier.class)
    Map<Integer, ArrayList<Double>> allParents = null;
    
    
    public HierarchyImplDouble(String inputFile){
        this.inputFile = inputFile;
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
    //    Map<Double, List<Double>> siblings = new HashMap<>();
        allParents = new HashMap<>();
    }
    
    public HierarchyImplDouble(String _name, String _nodesType){
        this.name = _name;
        this.nodesType = _nodesType;
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
    //    Map<Double, List<Double>> siblings = new HashMap<>();
        allParents = new HashMap<>();
//        this.hierarchyType = "distinct";
    }

    @Override
    public String getInputFile() {
        return inputFile;
    }

    
    public void setNodesType(String nodesType) {
        this.nodesType = nodesType;
    }
   
    
    
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
        //findAllParents();
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
            List<Double> ch = new ArrayList<>();
            for (String token : tokens){
                if(token.equals("has")){ 
                    isChild = true;
                    continue;
                }
                if(isChild){
                    //System.out.println(token);
                    ch.add(Double.parseDouble(token));
                    this.stats.put(Double.parseDouble(token), new NodeStats(curLevel));
                    this.parents.put(Double.parseDouble(token), Double.parseDouble(tokens[0]));  
                }
                else{
                    this.stats.put(Double.parseDouble(token), new NodeStats(curLevel-1));
                    
                    //level 0 and isChild == false then set as root 
                    if(curLevel - 1 == 0){
                        root = new Double(tokens[0]);
                    }
                }
                
                //System.out.println(token + ": " + isChild + " "  + curLevel);
               
            }
            this.children.put(Double.parseDouble(tokens[0]), ch);
            
//            set siblings
//            for(Double child : ch){
//                List<Double> sib = new ArrayList<>(ch);
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
    

    public void findAllParents(){
        List<Double> tempChild = null;
        int i = 0;
        int level = 0;
        ArrayList<Double> tempArr1 = new ArrayList<>();
        ArrayList<Double> tempArr2 = new ArrayList<>();
        
        tempArr1.add(root);
        allParents.put(level, tempArr1);
        tempArr2 = (ArrayList<Double>) tempArr1.clone();
        level ++;
        
        while (level <= height - 1 ){
            tempArr1 = new ArrayList<>();
            for (Double x : tempArr2) {
                tempChild = children.get(x);
                if ( tempChild != null){
                    for ( i = 0 ; i < tempChild.size() ; i ++ ){
                        tempArr1.add(tempChild.get(i));
                    }
                }
            }           
            allParents.put(level, tempArr1);
            tempArr2 = (ArrayList<Double>) tempArr1.clone();
            level ++;  
        }
    }
    

    public List<Double> getChildren(Double parent){
        return this.children.get(parent);
    }
    

    public Integer getLevel(Double node){
        if (this.stats.get(node) != null){
            return this.stats.get(node).getLevel();
        }
        else{
            return null;
        }
    }
    

    public Double getParent(Double node){
        return this.parents.get(node);
    }
    

//    public List<Double> getSiblings(Double node) {
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


    public Double getRoot(){
        return root;
    }
   
    public String getName(){
        return this.name;
    }


    public Map<Integer, ArrayList<Double>> getAllParents() {
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
                for (Double curParent : this.allParents.get(curLevel)){
                    if(this.getChildren(curParent) == null){
                        continue;
                    }
                    if(this.getChildren(curParent).isEmpty())
                        continue;
                    StringBuilder sb = new StringBuilder();
                    for (Double child : this.getChildren(curParent)){
                        //System.out.println("child = " + child);
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
    public void add(Double newObj, Double parent) {
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
            
            this.children.put(newObj, new ArrayList<Double>());
            if(this.children.get(parent) != null){
                this.children.get(parent).add(newObj);
            }else{
                ArrayList<Double> c = new ArrayList<>();
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
                ArrayList<Double> p = new ArrayList<>();
                p.add(newObj);
                this.allParents.put(this.stats.get(parent).getLevel() + 1, p);
                this.height++;
                
            }    
            else{
                ArrayList<Double> childList = allParents.get(this.stats.get(parent).getLevel() + 1);
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


    @Override
    public Map<Integer, Set<Double>> remove(Double item)
    {
        Map<Integer, Set<Double>> nodesMap = BFS(item,null);
        for(Integer i = nodesMap.keySet().size() ; i > 0 ; i--)
        {
            //System.out.println(i + "-> " + nodesMap.get((i)));
            
            for(Double itemToDelete : nodesMap.get(i)){
                //System.out.println(itemToDelete.toString());
                if(itemToDelete.equals(root)){
                    System.out.println("Cannot remove root");
                    return null;
                }
                children.remove(itemToDelete);
                children.get(getParent(itemToDelete)).remove(itemToDelete);
                parents.remove(itemToDelete);
//                List<Double> sibs = siblings.get(itemToDelete);
//                if(sibs != null){
//                    for(Double sib : sibs){
//                        if(siblings.get(sib) != null){
//                            siblings.get(sib).remove(itemToDelete);
//                        }
//                    }
//                }
//                siblings.remove(itemToDelete);
                
                         
                List<Double> p = allParents.get(stats.get(itemToDelete).level);
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
        children.put(root, new ArrayList<Double>());
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
    public void edit(Double oldValue, Double newValue){
        //update children map 
        Double parent = null;
        ArrayList parentsList = null;
        List<Double> childrenListNew = null;
        
        /*System.out.println(this.stats.get(oldValue).getLevel());
        System.out.println("before");
            for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        
        List<Double> childrenList = this.children.get(oldValue);
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
                //System.out.println("node");
                
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
    public boolean contains(Double o){  
        return stats.get(o) != null;
    }

    @Override
    public Map<Integer,Set<Double>> dragAndDrop(Double firstObj, Double lastObj) {
        Double parentFirstObj ;
        ArrayList childs1 = null;
        ArrayList childs2 = null;
        ArrayList simb1 = null;
        ArrayList simb2 = null;
        ArrayList parents = null;
        int levelFirstObj;
        int levelLastObj;
        int levelObj;
        int newLevel;
        Set<Double> s = null;
        NodeStats nodeStat = null;
        
        
        
       // System.out.println("Drag and Dropppppppp");
        
        /*System.out.println("before");
        for (Map.Entry<Double, NodeStats> entry : this.stats.entrySet()) {
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
        
        Map<Integer,Set<Double>> m = this.BFS(firstObj,lastObj);
        
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
                for( Double node : s ){
                   // System.out.println("node = " + node );
                    levelObj = this.getLevel(node);
                    parents = this.allParents.get(levelObj);
                    //System.out.println("level = " + levelObj);
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
                for (Double d : s){
                    nodeStat = this.stats.get(d);
                    nodeStat.level = newLevel;
                }
            }
            
            //height
            this.height = allParents.size();
           // System.out.println("new height = " + this.height);
            
        }
        
        /*System.out.println("after");
        for (Map.Entry<Double, NodeStats> entry : this.stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().level +":"+entry.getValue().weight);
        } */ 
        
        
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
    
    
    @Override
    public Map<Integer,Set<Double>> BFS(Double firstnode,Double lastNode){
        Map<Integer,Set<Double>> bfsMap = new HashMap<Integer,Set<Double>>();
        LinkedList<Double> listNodes = new LinkedList<Double>();
        ArrayList childs1 = null;
        int counter = 1;
        int levelNode1;
        int levelNode2;
        Set s = new HashSet<Double>();
        
        
        s.add(firstnode);
        bfsMap.put(counter,s);
        listNodes.add(firstnode);
        counter ++;
        levelNode1 = this.getLevel(firstnode);
        
        while (!listNodes.isEmpty()){
            childs1 = (ArrayList) this.getChildren(listNodes.getFirst());
            if ( childs1 != null && !childs1.isEmpty()){// ean exei paidia
                levelNode2 = this.getLevel((Double) childs1.get(0));
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
                    s = new HashSet<Double>();
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
        for(Double node : this.stats.keySet()){
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
        
        //System.out.println(c);
       
        for (double[] columnData : data) {
            NodeStats s = this.stats.get(columnData[c]);
            
            if(s != null){      //find weights of leaf level
                List<Double> cList = this.children.get(columnData[c]);
                if(cList == null || cList.isEmpty()){

                    //System.out.println(columnData[c]);
                    s.weight++;                         
                }
            }
        }

        //find weights for inner nodes
        for(int j = this.allParents.keySet().size()-1 ; j>=0 ; j--){
            for(Double node : this.allParents.get(j)){
                Integer totalWeight = 0;
                List<Double> cList = this.children.get(node);
                //System.out.println(node + " has children: " + cList);
                if(cList != null && !cList.isEmpty()){
                    for(Double child : cList){
                        totalWeight += this.stats.get(child).weight;
                    }
                    this.stats.get(node).weight = totalWeight;
                    //System.out.println(node + " weights " + totalWeight);
                }
                
            }
        }
    }
    
    
    @Override
    public Integer getWeight(Double node){
        //System.out.println("geteWeight " + node);
        return this.stats.get(node).weight;
    }
    
    
    public void incWeight(Double node){
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
    public Double checkColumn(int column, Data dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DictionaryString getDictionary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void transformParents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Integer> getParentsInteger() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLevelSize(int level) {
        return this.allParents.get(this.height - level - 1).size();
    }

    @Override
    public double getParentId(double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildDictionary(DictionaryString dictionary) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Double> getChildrenIds(double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getLevel(double nodeId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Integer> getNodeIdsInLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getWeight(double nodeId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public int findAllChildren(Double node,int sum) {
       int result = 0;
       
       List<Double> child = this.getChildren(node);
       
       if ( child == null){
           return 1;
       }
       
       for (int i =0 ; i < child.size() ; i ++){
           //System.out.println(child.get(i));
           result = findAllChildren(child.get(i),sum) + result;
       }
       
       return result;
    }

    @Override
    public Graph getGraph(String node, int nodeLevel) {
        Graph graph = new Graph();
        
        Node n = null;
        Edge e = null;
        ArrayList<String> nodes = null;
        boolean FLAG = false;
        String parent = null;
        List<Double> nodeChilds = null;
        Double nodeDouble = null;
        String color = null;
        String label = null;
        
        //System.out.println("roottttttttttttttttttttttttttt = " + root);
        
        //System.out.println("node =" + node + "\t level = "+ nodeLevel );
        
       
        
        int counter = 0;
        
        if (nodesType.equals("double")){
            if ( !node.equals("null") && !node.equals("") && nodeLevel != 0 ){
            
                //System.out.println("i am here");
            
            
                if (height > nodeLevel + 1){
                    nodeDouble = Double.parseDouble(node);
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeDouble);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                               
                                if (nodeChilds.get(j).isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeDouble+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                           // System.out.println("noChildren");
                        }
                        nodeDouble = this.parents.get(nodeDouble);
                        //System.out.println("node = " + node);
                    }
                    n = new Node(root+"",root+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }
                else{
                    //System.out.println("i am here222");
                    nodeDouble = Double.parseDouble(node);
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeDouble);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                             
                                if (nodeChilds.get(j).isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color, this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeDouble+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            //System.out.println("noChildren");
                        }
                        nodeDouble = this.parents.get(nodeDouble);
                        //System.out.println("node = " + node);
                    }
                    n = new Node(root +"",root+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }

            }
            else{
                nodeDouble = root;
                nodeLevel= 0;
                for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeDouble);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                              
                                if (nodeChilds.get(j).isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeDouble+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            //System.out.println("noChildren");
                        }
                        nodeDouble = this.parents.get(nodeDouble);
                        //System.out.println("node = " + node);
                    }
                    n = new Node(root+"",root+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);


            }
        }
        else{
            if ( !node.equals("null") && !node.equals("") && nodeLevel != 0 ){
            
                //nodeDouble = Double.parseDouble(node);
            
                if (height > nodeLevel + 1){
                    
                    nodeDouble = Double.parseDouble(node);
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeDouble);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                
                                if (nodeChilds.get(j).intValue() == 2147483646){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j).intValue()+"";
                                }
                                n = new Node(nodeChilds.get(j).intValue()+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeDouble.intValue()+"",nodeChilds.get(j).intValue()+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            //System.out.println("noChildren");
                        }
                        nodeDouble = this.parents.get(nodeDouble);
                    }
                    n = new Node(root.intValue()+"",root.intValue()+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }
                else{
                    

                    //System.out.println("i am here222");
                    nodeDouble = Double.parseDouble(node);
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeDouble);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                
                                if (nodeChilds.get(j).intValue() == 2147483646){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j).intValue()+"";
                                }
                                n = new Node(nodeChilds.get(j).intValue()+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeDouble.intValue()+"",nodeChilds.get(j).intValue()+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                           // System.out.println("noChildren");
                        }
                        nodeDouble = this.parents.get(nodeDouble);
                    }
                    n = new Node(root.intValue()+"",root.intValue()+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }
            }
            else{
                
                nodeDouble = root;
                nodeLevel= 0;
                
                for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeDouble);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                
                                if (nodeChilds.get(j).intValue() == 2147483646){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j).intValue()+"";
                                }
                                n = new Node(nodeChilds.get(j).intValue()+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeDouble.intValue()+"",nodeChilds.get(j).intValue()+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            //System.out.println("noChildren");
                        }
                        
                        nodeDouble = this.parents.get(nodeDouble);
                        
                    }
                    n = new Node(root.intValue()+"",root.intValue()+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);
            }
        }
        
        //System.out.println("endddd");
        
        
        Collections.reverse(graph.getNodeList());
        Collections.reverse(graph.getEdgeList());

        /*for ( int i = 0 ; i < graph.getNodeList().size() ; i ++){
            System.out.println("\tid = " +graph.getNodeList().get(i).getLabel() );
        }
        
        
        for ( int i = 0 ; i < graph.getEdgeList().size() ; i ++){
            System.out.println("From = " + graph.getEdgeList().get(i).getFrom() + "\t to = " + graph.getEdgeList().get(i).getTo() +"\t id = " + graph.getEdgeList().get(i).getId() );
            
        }*/
        
        
        return graph;
    }

    @Override
    public String checkHier() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getParent(Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int translateDateViaLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
   
    
}   
