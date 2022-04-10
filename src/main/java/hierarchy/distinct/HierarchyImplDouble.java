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

import exceptions.LimitException;
import com.fasterxml.jackson.annotation.JsonView;
import controller.AppCon;
import data.Data;
import data.DiskData;
import data.RelSetData;
import data.SETData;
import dictionary.DictionaryString;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.Hierarchy;
import static hierarchy.Hierarchy.online_limit;
import static hierarchy.Hierarchy.online_version;
import hierarchy.NodeStats;
import hierarchy.ranges.RangeDouble;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
    int counterNodes = 0;
    
    int levelFlash = -1;
    Map<Integer,Integer> levelpFlash = Collections.synchronizedMap(new HashMap());
    
    //@JsonView(View.Hier.class)
    public Double root = null;

    //@JsonView(View.Hier.class)
    Map<Double, List<Double>> children = null;
    Map<Double, NodeStats> stats = null;
    Map<Double, Double> parents = null;
//    Map<Double, List<Double>> siblings = new HashMap<>();
    //@JsonView(View.Hier.class)
    Map<Integer, ArrayList<Double>> allParents = null;
    Map<Integer, List<Integer>> allParentIds = null;
    
    
    public HierarchyImplDouble(String inputFile){
        this.inputFile = inputFile;
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
    //    Map<Double, List<Double>> siblings = new HashMap<>();
        allParents = new HashMap<>();
        allParentIds = new HashMap<>();
    }
    
    public HierarchyImplDouble(String _name, String _nodesType){
        this.name = _name;
        this.nodesType = _nodesType;
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
    //    Map<Double, List<Double>> siblings = new HashMap<>();
        allParents = new HashMap<>();
        allParentIds = new HashMap<>();
//        this.hierarchyType = "distinct";
    }

    @Override
    public String getInputFile() {
        return inputFile;
    }
    
    
    @Override
    public DictionaryString getDictionaryData(){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void setNodesType(String nodesType) {
        this.nodesType = nodesType;
    }
   
    
    
    public void load() throws LimitException{
        try {
            FileInputStream fstream = new FileInputStream(inputFile);
            DataInputStream in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
            processingMetadata();
            loadHierarchy();
            findAllParents();
            br.close();
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(" i am here");
        //findAllParents();
    }

    private void loadHierarchy() throws IOException, LimitException{
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
                    counterNodes ++;
                    if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                        throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
                    }
                    
                    this.stats.put(Double.parseDouble(token), new NodeStats(curLevel));
                    this.parents.put(Double.parseDouble(token), Double.parseDouble(tokens[0]));  
                }
                else{
                    this.stats.put(Double.parseDouble(token), new NodeStats(curLevel-1));
                    
                    //level 0 and isChild == false then set as root 
                    if(curLevel - 1 == 0){
                        root = new Double(tokens[0]);
                        this.stats.put(root, new NodeStats(0));
                        counterNodes ++;
                        if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                            throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
                        }
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
                this.nodesType = tokens[1].replaceAll("decimal", "double");
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
//            System.out.println("value "+node);
//            System.out.println("Hier  level "+this.stats.get(node).getLevel());
            return this.stats.get(node).getLevel();
        }
        else{
            return null;
        }
    }
    
    @Override
    public Double getParent(Double node, int ti) {
        if(levelpFlash.get(ti) == null){
            return this.parents.get(node);
        }
        else{
            Double anonValue = this.parents.get(node);
//            System.out.println("Node "+node+" anonValue "+anonValue);
//            System.out.println("Flash level "+levelFlash+" level anon node "+this.getLevel((double)anonValue)+" anonNode "+anonValue+" level node "+this.getLevel((double)node));
            if(anonValue == null){
                return null;
            }
            Integer level = this.getLevel((double)node);
            if(level == null){
                return null;
            }
            if(levelpFlash.get(ti) == level){
                return anonValue;
            }
            else{
                return node;
            }
        }
    }
    

    public Double getParent(Double node){
        
        if(levelFlash == -1){
            return this.parents.get(node);
        }
        else{
            Double anonValue = this.parents.get(node);
//            System.out.println("Node "+node+" anonValue "+anonValue);
//            System.out.println("Flash level "+levelFlash+" level anon node "+this.getLevel((double)anonValue)+" anonNode "+anonValue+" level node "+this.getLevel((double)node));
            if(anonValue == null){
                return null;
            }
            Integer level = this.getLevel((double)node);
            if(level == null){
                return null;
            }
            if(levelFlash == level){
                return anonValue;
            }
            else{
                return node;
            }
        }
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
            writer.println("type " + this.nodesType.replace("double", "decimal"));
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
    public void add(Double newObj, Double parent) throws LimitException {
        System.out.println("add () newItem: "  + newObj.toString() + " parentItem: " + parent.toString());
        
        counterNodes ++;
        if(AppCon.os.equals(online_version) && counterNodes > online_limit){
            counterNodes--;
            throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.",false);
        }
       
        
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
            System.out.println(i + "-> " + nodesMap.get((i)));
            
            for(Double itemToDelete : nodesMap.get(i)){
                System.out.println(itemToDelete.toString());
                if(itemToDelete.equals(root)){
                    System.out.println("Cannot remove root");
                    return null;
                }
                this.counterNodes--;
                children.remove(itemToDelete);
                System.out.println("Parent :"+this.parents.get(itemToDelete));
                children.get(this.parents.get(itemToDelete)).remove(itemToDelete);
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
        boolean changeRoot = false;
        
        /*System.out.println(this.stats.get(oldValue).getLevel());
        System.out.println("before");
            for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        
        List<Double> childrenList = this.children.get(oldValue);
        if(childrenList != null){//node
            if ( allParents.get(0).get(0).equals(oldValue)){
                //System.out.println("root");
                
                changeRoot = true;
                
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
                parentsList.remove(oldValue);
                parentsList.add(newValue);
                allParents.put(0,parentsList);
                this.stats.remove(root);
                this.root = newValue;
                this.stats.put(root, new NodeStats(0));
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
            parent = this.parents.get(oldValue);
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
        
        if(!changeRoot){
            this.stats.put(newValue, this.stats.get(oldValue));
            this.stats.remove(oldValue);
        }
        
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
    public void autogenerate() throws LimitException {
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
        Double parentValue = this.parents.get(d);
        if(parentValue == null){
            return -1;
        }
        else{
            return parentValue;
        }
    }

    @Override
    public void buildDictionary(DictionaryString dictionary) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Double> getChildrenIds(double d) {
        List<Double> childrenIds = this.children.get(d);
        Set<Double> chs = null;
        
        if(childrenIds != null){
            chs = new HashSet<>(childrenIds);
//            System.out.println(childrenStrings);
//            for(String child : childrenStrings){
//                
//                chs.add((double)this.dict.getStringToId(child));
//            }
        }
        return chs;
    }

    @Override
    public Integer getLevel(double nodeId) {
        
        try{
            return (this.height - this.getLevel((Double)nodeId) - 1) ;
        }catch(NullPointerException ne){
            return null;    
        }
    }

    @Override
    public List<Integer> getNodeIdsInLevel(int level) {
         int curLevel = this.height - level - 1;
        List<Integer> curLevelIds = this.allParentIds.get(curLevel);
        if(curLevelIds == null){
            
            ArrayList<Double> nodesInLevel = this.getAllParents().get(curLevel);
            List<Integer> nodeIdsInLevel = new ArrayList<>(nodesInLevel.size());
            for(Double id : nodesInLevel){
                nodeIdsInLevel.add(id.intValue());
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
        return getWeight((Double)nodeId);
    }


    @Override
    public int findAllChildren(Double node,int sum, boolean onlyLeaves) {
       int result = 0;
       
       List<Double> child = this.getChildren(node);
       
       if ( child == null){
           return 1;
       }
       
       for (int i =0 ; i < child.size() ; i ++){
           if(onlyLeaves || node.equals(this.root)){
                result = findAllChildren(child.get(i),sum,onlyLeaves) + result;
            }
            else{
                result = findAllChildren(child.get(i),sum,onlyLeaves) + result + 1;
            }
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
            if ( !node.equals("null") && !node.equals("(null)") && !node.equals("") && nodeLevel != 0 ){
            
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
                               
                                if (nodeChilds.get(j).intValue() == 2147483646 || nodeChilds.get(j).isNaN()){
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
                             
                                if (nodeChilds.get(j).intValue() == 2147483646 || nodeChilds.get(j).isNaN()){
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
                              
                                if (nodeChilds.get(j).intValue() == 2147483646 || nodeChilds.get(j).isNaN()){
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
            if ( !node.equals("null") && !node.equals("(null)") && !node.equals("") && nodeLevel != 0 ){
            
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
                                
                                if (nodeChilds.get(j).intValue() == 2147483646 || nodeChilds.get(j).isNaN()){
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
                                
                                if (nodeChilds.get(j).intValue() == 2147483646 || nodeChilds.get(j).isNaN()){
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
                                
                                if (nodeChilds.get(j).intValue() == 2147483646 || nodeChilds.get(j).isNaN()){
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
    public String checkHier(Data d,int col) {
        String str = "Ok";
        System.out.println("Mpainei edw hier double");
        if(d instanceof DiskData){
            DiskData diskData = (DiskData) d;
            Set<Double> values = new HashSet();
            for(int i=0; i<this.height; i++){
                values.addAll(this.allParents.get(i));
            }
            List<Double> missingValues = diskData.checkValues(values, col);
            if(!missingValues.isEmpty()){
                for(int i=0; i<missingValues.size(); i++){
                    if(Double.isNaN(missingValues.get(i)) || missingValues.get(i).equals(2147483646.0)){
                        if(this.getParent(2147483646.0) == null){
                           return "Node (null) for spaces values and non-Numeric values, is not defined in the hierarchy \""+this.name+"\"" ;
                        }
                        missingValues.remove(i);
                    }
                }
                if(missingValues.size() == 1){
                    if(diskData.getColNamesType().get(col).equals("int")){
                        return "Value \""+missingValues.get(0).intValue()+"\" are not defined in hierarchy \""+this.name+"\"";

                    }
                    else{
                        return "Value \""+missingValues.get(0)+"\" are not defined in hierarchy \""+this.name+"\"";
                    }
                }
                else if(!missingValues.isEmpty()){
                    if(diskData.getColNamesType().get(col).equals("int")){
                        return "Values \""+missingValues.toString().replace(".0", "").replace("[", "(").replace("]", ")")+"\" are not defined in hierarchy \""+this.name+"\"";
                    }
                    else{
                        return "Values \""+missingValues.toString().replace("[", "(").replace("]", ")")+"\" are not defined in hierarchy \""+this.name+"\"";
                    }
                }
            }
        }
        else{
            double[][] dataset = d.getDataSet();
            for(int i=0; i<dataset.length; i++){
                Double parent = this.parents.get(dataset[i][col]);
                System.out.println("value "+dataset[i][col]+" parent "+parent);
                if(parent==null){
                    System.out.println("check root "+(dataset[i][col] != root.doubleValue()));
                    if(dataset[i][col] != root.doubleValue()){
                        if(Double.isNaN(dataset[i][col]) || dataset[i][col] == 2147483646.0){
                            if(this.getParent(2147483646.0) == null){
                               return "Node (null) for spaces values and non-Numeric values, is not defined in the hierarchy \""+this.name+"\"" ;
                            }
                        }
                        else{
                            System.out.println("Mapinei edv metas to check");
                            Object value;
                            String type = d.getColNamesType().get(col);
                            if(type.equals("int")){
                                value = (int)dataset[i][col];
                            }
                            else{
                                value = dataset[i][col];
                            }
                            System.out.println("Value \""+value+"\" in "+d.getColumnByPosition(col)+" does not set in the hierarchy "+this.name+" prin to return ");
                            return "Value \""+value+"\" in "+d.getColumnByPosition(col)+" column is not defined in the hierarchy \""+this.name+"\"";
                        }
                    }
                }
            }
        }
        
        return str;
    }

    @Override
    public Double getParent(Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int translateDateViaLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Double> getNodesInLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDictionaryData(DictionaryString dict) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLevel(int l) {
        this.levelFlash = l;
    }
    
    @Override
    public void setpLevel(int ti, int l) {
        if(ti < 0){
            this.levelpFlash = null;
            return;
        }
        
//        if(this.levelpFlash == null){
//            this.levelpFlash = new HashMap();
//        }
        this.levelpFlash.put(ti, l);
    }

    @Override
    public Map<Integer, Set<Double>> getLeafNodesAndParents() {
        Set<Double> leaves = new HashSet<Double>(),parentLeaves = new HashSet<Double>();
        boolean isParentLeaf = true;
        for(Map.Entry<Integer,ArrayList<Double>> entry : this.allParents.entrySet()){
            List<Double> nodesInLevel = entry.getValue();
            if(entry.getKey() != height-1){
                for(Double node : nodesInLevel){
                    if(this.children.get(node) == null){
                        leaves.add(node);
                        List<Double> brothers = this.children.get(this.parents.get(node));
                        isParentLeaf = true;
                        for(Double brother : brothers){
                            if(this.children.containsKey(brother)){
                                isParentLeaf = false;
                                break;
                            }
                        }
                        if(isParentLeaf){
                            parentLeaves.add(this.parents.get(node));
                        }
                    }
                }
            }
            else{
                leaves.addAll(nodesInLevel);
            }
        }
        Map<Integer,Set<Double>> nodesAndParents = new HashMap();
        nodesAndParents.put(0, leaves);
        nodesAndParents.put(1, parentLeaves);
        return nodesAndParents;
    }

    @Override
    public void syncDictionaries(Integer column, Data data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findCommonHeight(Double n1, Double n2) {
        if(n1.equals(root)){
            this.getLevel(root.doubleValue());
        }
        int height1 = this.getLevel(n1.doubleValue());
        int height2 = this.getLevel(n2.doubleValue());
//        System.out.println("Searching common "+n1+" and "+n2);
        if(n1.equals(n2)){
//            System.out.println("Common "+n1+" with height "+height1);
            return height1;
        }
        else{
            while(height1 > height2){
                n2 = this.getParent(n2);
                height2 = this.getLevel(n2.doubleValue());
            }
            
            while(height2 > height1){
                n1 = this.getParent(n1);
                height1 = this.getLevel(n1.doubleValue());
            }
            
            if(n1.equals(n2)){
//                System.out.println("Common "+n1+" with height "+height1);
                return height2;
            }
            
            while(!n1.equals(n2)){
                n1 = this.getParent(n1);
                n2 = this.getParent(n2);
                height2 = this.getLevel(n2.doubleValue());
                height1 = this.getLevel(n1.doubleValue());
            }
            
//            System.out.println("Common "+n1+" with height "+height1);
            return height1;
            
        }
        
    }

    @Override
    public Double findCommon(Double n1, Double n2) {
        if(n1.equals(root) || n2.equals(root)){
            return root;
        }
        int height1 = this.getLevel(n1.doubleValue());
        int height2 = this.getLevel(n2.doubleValue());
//        System.out.println("Searching common anc "+n1+" and "+n2);
        if(n1.equals(n2)){
//            System.out.println("Common anc "+n1+" with height "+height1);
            return n1;
        }
        else{
            while(height1 > height2){
                n2 = this.getParent(n2);
                height2 = this.getLevel(n2.doubleValue());
            }
            
            while(height2 > height1){
                n1 = this.getParent(n1);
                height1 = this.getLevel(n1.doubleValue());
            }
            
            if(n1.equals(n2)){
//                System.out.println("Common anc "+n1+" with height "+height1);
                return n1;
            }
            
            while(!n1.equals(n2)){
                n1 = this.getParent(n1);
                n2 = this.getParent(n2);
                height2 = this.getLevel(n2.doubleValue());
                height1 = this.getLevel(n1.doubleValue());
            }
            
//            System.out.println("Common anc "+n1+" with height "+height1);
            return n1;
            
        }
    }

    @Override
    public boolean checkExistance(Double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearAprioriStructures() {
        allParentIds = new HashMap();
    }

    @Override
    public Double getParent(Double v, Integer k) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getPopulation(Double v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getPopulation(double rd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createLevelsMap(){
        levelpFlash = Collections.synchronizedMap(new HashMap());
    }

    

    

   
    
   
    
}   
