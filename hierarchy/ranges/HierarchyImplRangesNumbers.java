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
package hierarchy.ranges;

import data.Data;
import dictionary.DictionaryString;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.Hierarchy;
import hierarchy.NodeStats;
import hierarchy.distinct.HierarchyImplDouble;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author serafeim
 */
public class HierarchyImplRangesNumbers implements Hierarchy<RangeDouble>{

    String inputFile = null;
    String name = null;
    String nodesType = null;
    String hierarchyType = "range";
    int height = -1;
    BufferedReader br = null;
    RangeDouble root = null;
    
    Map<RangeDouble, List<RangeDouble>> children = new HashMap<>();
    Map<RangeDouble, NodeStats> stats = new HashMap<>();
    Map<RangeDouble, RangeDouble> parents = new HashMap<>();
//    Map<Range, List<Range>> siblings = new HashMap<>();
    Map<Integer,ArrayList<RangeDouble>> allParents = new HashMap<>();

    
    public HierarchyImplRangesNumbers(String inputFile){
        this.inputFile = inputFile;
    }
    
    public HierarchyImplRangesNumbers(String _name, String _nodesType){
        this.name = _name;
        this.nodesType = _nodesType;
//        this.hierarchyType = "range";
    }


    public void load() {
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
    
    private void loadHierarchy() throws IOException{
        String line;
        int curLevel = this.height - 1;
        
        while ((line = br.readLine()) != null) {
            String tokens[] = line.split(" ");
            if(line.trim().isEmpty()){
                curLevel--;
                continue;
            }
            
            
            //split parent
            RangeDouble pRange = new RangeDouble();
            String bounds[] = tokens[0].split(",");
            pRange.lowerBound = Double.parseDouble(bounds[0]);
            pRange.upperBound = Double.parseDouble(bounds[1]);
            pRange.nodesType = nodesType;
                    
            boolean isChild = false;
            List<RangeDouble> ch = new ArrayList<>();
            for (String token : tokens){
                if(token.equals("has")){ 
                    isChild = true;
                    continue;
                }
                RangeDouble newRange = new RangeDouble();
                bounds = token.split(",");
                newRange.lowerBound = Double.parseDouble(bounds[0]);
                newRange.upperBound = Double.parseDouble(bounds[1]);
                newRange.nodesType = nodesType;
                
                if(isChild){
                    
                    ch.add(newRange);             
                    this.stats.put(newRange, new NodeStats(curLevel));
                    
                    
                    this.parents.put(newRange, pRange);  
                }
                else{
                    this.stats.put(newRange, new NodeStats(curLevel-1));
                    
                    if(curLevel - 1 == 0){
                        root = pRange;
                    }
                }
            }
            this.children.put(pRange, ch);
            
//            for (Range child : ch) {
//                List<Range> sib = new ArrayList<>(ch);
//                sib.remove(child);
//                this.siblings.put(child, sib);
//            }
            
        }
        int mb = 1024*1024;
        //System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        //System.out.println(Runtime.getRuntime().totalMemory()/mb);
        //System.out.println(Runtime.getRuntime().totalMemory());
    }
        
    @Override
    public List<RangeDouble> getChildren(RangeDouble parent) {
        return this.children.get(parent);
    }

    @Override
    public Integer getLevel(RangeDouble node) {
        return this.stats.get(node).level;
    }

    @Override
    public RangeDouble getParent(RangeDouble node) {
        //System.out.println("GET PARENT Range = " +node.toString());
        RangeDouble r = new RangeDouble(Double.NaN,Double.NaN);
        //r.nodesType = "int";
        //System.out.println("rrrrrrrrrrrrrrrrrrrrrrrrrrrrr = "+ r.toString());
        for (Map.Entry<RangeDouble, RangeDouble> entry : parents.entrySet()) {
            //System.out.println(entry.getKey()+" : "+entry.getValue());
            RangeDouble r1 = entry.getKey();
           // System.out.println("r1 = " + r1);
            //System.out.println("r = " + r);
            //System.out.println("dfsdfsdfsd :" + parents.get(r1));
            //System.out.println("dfsdfsdfsd :" + parents.get(node));
            if (r1.equals(r)){
                //System.out.println("i am hereeeeeeeeeeeeeeeeeeeeeedsfgsfewfe");
            }
        }
        
        return this.parents.get(node);
    }

//    @Override
//    public List<Range> getSiblings(Range node) {
//        return this.siblings.get(node);
//    }
    
    @Override
    public int[][] getHierarchy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setHierarchy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getHierarchyLength() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RangeDouble getRoot() {
        return this.allParents.get(0).get(0);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<Integer, ArrayList<RangeDouble>> getAllParents() {
        return this.allParents;
    }
    
    public void findAllParents(){
        List<RangeDouble> tempChild = null;
        int i = 0;
        int level = 0;
        ArrayList<RangeDouble> tempArr1 = new ArrayList<>();
        ArrayList<RangeDouble> tempArr2 = new ArrayList<>();
        
        tempArr1.add(root);
        allParents.put(level, tempArr1);
        tempArr2 = (ArrayList<RangeDouble>) tempArr1.clone();
        level ++;
        
        while (level <= height - 1 ){
            tempArr1 = new ArrayList<>();
            for (RangeDouble x : tempArr2) {
                tempChild = children.get(x);
                if ( tempChild != null){
                    for ( i = 0 ; i < tempChild.size() ; i ++ ){
                        tempArr1.add(tempChild.get(i));
                    }
                }
            }           
            allParents.put(level, tempArr1);
            tempArr2 = (ArrayList<RangeDouble>) tempArr1.clone();
            level ++;  
        }
    }
    
    
    public void export(String file) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("range");
            writer.println("name " + this.name);
            writer.println("type " + this.nodesType);
            writer.println("height " + this.height);
            writer.println();
            
            int counter = 1;

            //write parents - childen to file
            for(int curLevel = height - 2; curLevel >= 0; curLevel--){
                for (RangeDouble curParent : this.allParents.get(curLevel)){
                    StringBuilder sb = new StringBuilder();
                    System.out.println("cur parent = " + curParent);
                    if (this.getChildren(curParent) != null){
                        for (RangeDouble child : this.getChildren(curParent)){
                            sb.append(((RangeDouble)child).lowerBound);
                            sb.append(",");
                            sb.append(((RangeDouble)child).upperBound);
                            sb.append(" ");
                        } 
                        writer.println(((RangeDouble)curParent).lowerBound + "," + ((RangeDouble)curParent).upperBound + " has " + sb.toString());
                    }
                    
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
        return this.nodesType;
    }

    @Override
    public void add(RangeDouble newObj, RangeDouble parent) {
        System.out.println("add () newItem: "  + newObj.toString() + " parentItem: " + parent.toString());
       
        
        if(parent != null){
            this.stats.put(newObj, new NodeStats(this.stats.get(parent).getLevel()+1));
            
            this.parents.put(newObj, parent);

            this.children.put(newObj, new ArrayList<RangeDouble>());
            if(this.children.get(parent) != null){
                this.children.get(parent).add(newObj);
            }else{
                ArrayList<RangeDouble> c = new ArrayList<>();
                c.add(newObj);
                this.children.put(parent, c);
            }
            
            
            //parent is a leaf node
            
           
            if (this.stats.get(parent).getLevel() == allParents.size() - 1){
                ArrayList<RangeDouble> p = new ArrayList<>();
                p.add(newObj);
                this.allParents.put(this.stats.get(parent).getLevel() + 1, p);
                this.height++;
                
            }    
            else{
                ArrayList<RangeDouble> childList = allParents.get(this.stats.get(parent).getLevel() + 1);
                childList.add(newObj);
                allParents.put(this.stats.get(parent).getLevel() + 1, childList);
                
            }
            
        }
        else{
            System.out.println("Error: parent is null!");
        }    
    }
    
    private boolean isLeafLevel(ArrayList<RangeDouble> parentsInLevel){
        boolean  leafLevel = true;
        
        for (RangeDouble parent : parentsInLevel){
            List<RangeDouble> ch = this.children.get(parent);
            if(ch != null && ch.size() > 0){
                leafLevel = true;
                break;
            }
        }
        return leafLevel;
    }
    
    @Override
    public void edit(RangeDouble oldValue, RangeDouble newValue){
        //update children map 
        //update children map 
        RangeDouble parent = null;
        ArrayList parentsList = null;
        List<RangeDouble> childrenListNew = null;
        
        System.out.println(this.stats.get(oldValue).getLevel());
        System.out.println("before");
            for (Map.Entry<Integer, ArrayList<RangeDouble>> entry : this.allParents.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        
        List<RangeDouble> childrenList = this.children.get(oldValue);
        if(childrenList != null){//node
            if ( allParents.get(0).get(0).equals(oldValue)){
                System.out.println("roooooooooooooot");
                
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
                 root.setLowerBound(newValue.lowerBound);
                 root.setUpperBound(newValue.upperBound);
            }
            else{
                System.out.println("nodeeeeeeeeeeeeee");
                
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
            System.out.println("leaffffffffffffffffffffffff");
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
        
        System.out.println("after");
            for (Map.Entry<RangeDouble, List<RangeDouble>> entry : this.children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
            
        System.out.println("after");
            for (Map.Entry<RangeDouble, RangeDouble> entry : this.parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("after");
            for (Map.Entry<Integer, ArrayList<RangeDouble>> entry : this.allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("after");
            for (Map.Entry<RangeDouble, NodeStats> entry : this.stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
            
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
    public boolean contains(RangeDouble o){  
        return stats.get(o) != null;
    }

    @Override
    public Map<Integer, Set<RangeDouble>> remove(RangeDouble item) {
        Map<Integer, Set<RangeDouble>> nodesMap = BFS(item,null);
        for(Integer i = nodesMap.keySet().size() ; i > 0 ; i--)
        {
            //System.out.println(i + "-> " + nodesMap.get((i)));
            
            for(RangeDouble itemToDelete : nodesMap.get(i)){
                //System.out.println(itemToDelete.toString());
                if(itemToDelete.equals(root)){
                    System.out.println("Cannot remove root");
                    return null;
                }
                children.remove(itemToDelete);
                children.get(getParent(itemToDelete)).remove(itemToDelete);
                parents.remove(itemToDelete);
//                List<Range> sibs = siblings.get(itemToDelete);
//                if(sibs != null){
//                    for(Range sib : sibs){
//                        if(siblings.get(sib) != null)
//                            siblings.get(sib).remove(itemToDelete);
//                    }
//                }
//                siblings.remove(itemToDelete);
                List<RangeDouble> p = allParents.get(stats.get(itemToDelete).level);
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
        children.put(root, new ArrayList<RangeDouble>());
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
    public Map<Integer,Set<RangeDouble>> dragAndDrop(RangeDouble firstObj, RangeDouble lastObj) {
        RangeDouble parentFirstObj ;
        ArrayList childs1 = null;
        ArrayList childs2 = null;
        ArrayList simb1 = null;
        ArrayList simb2 = null;
        ArrayList parents = null;
        int levelFirstObj;
        int levelLastObj;
        int levelObj;
        int newLevel;
        Set<RangeDouble> s = null;
        NodeStats nodeStat = null;
        
        
        
        System.out.println("Drag and Dropppppppp");
        
        System.out.println("before");
        for (Map.Entry<RangeDouble, NodeStats> entry : this.stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().level +":"+entry.getValue().weight);
        }   
        
        /*System.out.println("before");
            for (Map.Entry<Double, List<Double>> entry : this.children.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
                List <Double> d = entry.getValue();
                for( int i = 0 ; i < d.size() ; i ++  ){
                    System.out.println(d.get(i) + "level:" + this.getLevel(d.get(i)) );
                }
        }*/
            
        System.out.println("old height = " + this.height);
        
        Map<Integer,Set<RangeDouble>> m = this.BFS(firstObj,lastObj);
        
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
                for( RangeDouble node : s ){
                    System.out.println("node = " + node );
                    levelObj = this.getLevel(node);
                    parents = this.allParents.get(levelObj);
                    System.out.println("level = " + levelObj);
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
                for (RangeDouble d : s){
                    nodeStat = this.stats.get(d);
                    nodeStat.level = newLevel;
                }
            }
            
            //height
            this.height = allParents.size();
            System.out.println("new height = " + this.height);
            
        }
        
        System.out.println("after");
        for (Map.Entry<RangeDouble, NodeStats> entry : this.stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().level +":"+entry.getValue().weight);
        }  
        
        
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
    public void computeWeights(Data dataset, String column) {
        
        //all weights set to zero
        for(RangeDouble node : this.stats.keySet()){
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
        
//        System.out.println(c);
        
        //assign values to nodes, starting from root
        
        for (double[] columnData : data) {
            System.out.println("columnData[c] = " + columnData[c]);
            compute(getRoot(), columnData[c],true,true);     
        }               
    }
    
    public void compute(RangeDouble r, Double value, boolean whichContain, boolean isRoot){
        
        //range has been deleted
//        if(this.stats.get(r) == null){
//            System.out.println(r.toString() + " not found");
//            return;
//        }

        if (isRoot == true){
            if(r.contains(value)){
                System.out.println("Compute");
                System.out.println("value = " + value.toString());
                System.out.println(r.toString());
                this.stats.get(r).weight++;
            }
        }
        else{
            if(r.contains2(value,whichContain)){             
                System.out.println("Compute111111");
                System.out.println("value = " + value.toString());
                System.out.println(whichContain);
                System.out.println(r.toString());
                this.stats.get(r).weight++;
            }

        }
        
        
        List<RangeDouble> ch = this.children.get(r);
        /*for ( int i = 0; i < ch.size() ; i ++ ){
            System.out.println("ch = " + ch.get(i).toString() + "\t i = " + i +"\tsize = " + ch.size());
            
        }*/
        if(ch != null){
            //List<Range> ch1 = this.children.get(ch.get(0));
            //if ( ch1 != null ){
                //System.out.println("ch = " + ch.toString());
                int counter = 1;
                for(RangeDouble c : ch){
                    System.out.println("cccccccccccccccc = " + c.toString());
                    if (value == 2147483646.0 || value.equals(Double.NaN)){
                       if (c.toString().equals("NaN - NaN")){
                           this.stats.get(c).weight++;
                           this.stats.get(getRoot()).weight++;
                            System.out.println("value = " + value.toString());
                            System.out.println(c.toString());
                       }
                       else if(c.toString().equals("0 - 0")){
                           this.stats.get(c).weight++;
                           this.stats.get(getRoot()).weight++;
                            System.out.println("value = " + value.toString());
                            System.out.println(c.toString());
                       }
                    }
                    else if (isRoot == true){
                        if(!c.toString().equals("0 - 0") && !c.toString().equals("NaN - NaN") ){
                            if (counter == ch.size()-1){
                                if(c.contains2(value,false)){
                                    System.out.println("Compute22222");
                                    System.out.println("value = " + value.toString());
                                    System.out.println(c.toString());
                                    compute(c, value,false,false);
                                }
                            }
                            else{
                                if(c.contains2(value,true)){
                                    System.out.println("Compute3333");
                                     System.out.println("value = " + value.toString());
                                    System.out.println(c.toString());
                                    compute(c, value,true,false);
                                }
                            }
                        }
                    }
                    else{
                        //if(!c.toString().equals("0 - 0") && !c.toString().equals("NaN - NaN") ){
                       
                            if (counter == ch.size()){
                                if(c.contains2(value,false)){
                                    System.out.println("Compute44444");
                                    System.out.println("value = " + value.toString());
                                    System.out.println(c.toString());
                                    compute(c, value,false,false);
                                }
                            }
                            else{
                                if(c.contains2(value,true)){
                                    System.out.println("Compute555555");
                                     System.out.println("value = " + value.toString());
                                    System.out.println(c.toString());
                                    compute(c, value,true,false);
                                }
                            }
                        //}
                    }
                    counter++;
                }
            //}
           // else{
            
            //}
            
        }
        
        
        /*
        if(r.contains(value)){
            this.stats.get(r).weight++;
        }
        
        System.out.println("Computeeeeee weightssss");
        
        List<Range> ch = this.children.get(r);
        if(ch != null){
            System.out.println("ch = " + ch.toString());
            for(Range c : ch){
                System.out.println(c.toString() +"\t value = " + value.toString());
                if (value == 2147483646.0 || value.equals(Double.NaN)){
                   if (c.toString().equals("NaN - NaN")){
                       this.stats.get(c).weight++;
                       this.stats.get(getRoot()).weight++;
                   }
                   else if(c.toString().equals("0 - 0")){
                       this.stats.get(c).weight++;
                       this.stats.get(getRoot()).weight++;
                   }
                }
                else if(c.contains(value)){
                    System.out.println("i ama heeeeeeeee");
                    compute(c, value);
                }
            }
        }*/
        
    }
  
    @Override
    public Integer getWeight(RangeDouble node){
        return this.stats.get(node).weight;
    }
    
    public void incWeight(RangeDouble node){
        this.stats.get(node).weight++;
    }

    @Override
    public Map<Integer, Set<RangeDouble>> BFS(RangeDouble firstnode, RangeDouble lastNode) {
        Map<Integer,Set<RangeDouble>> bfsMap = new HashMap<Integer,Set<RangeDouble>>();
        LinkedList<RangeDouble> listNodes = new LinkedList<RangeDouble>();
        ArrayList childs1 = null;
        int counter = 1;
        int levelNode1;
        int levelNode2;
        Set s = new HashSet<RangeDouble>();
        
        
        s.add(firstnode);
        bfsMap.put(counter,s);
        listNodes.add(firstnode);
        counter ++;
        
        levelNode1 = this.getLevel(firstnode);
        
        while (!listNodes.isEmpty()){
            childs1 = (ArrayList) this.getChildren(listNodes.getFirst());
            if ( childs1 != null && childs1.size() > 0){// ean exei paidia
                levelNode2 = this.getLevel((RangeDouble) childs1.get(0));
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
                    s = new HashSet<RangeDouble>();
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
    
    public boolean validCheck(String nodeStr){
        boolean valid = true;
        RangeDouble node = new RangeDouble();
        String[] parts = nodeStr.split("-");
        if(parts.length == 2){
            node.lowerBound = Double.parseDouble(parts[0]);
            node.upperBound = Double.parseDouble(parts[1]);
            node.nodesType = nodesType;
        }
        
        List<RangeDouble> chList = children.get(node);
        if(chList != null){
            for(RangeDouble c : chList){
                valid = !node.overlays(c);
            }
        }
        return valid;
    }

    @Override
    public RangeDouble checkColumn(int column, Data dataset) {
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
    public RangeDouble getParent(Double d) {
        
        //System.out.println("parentssize = " + this.allParents.size() + "\t height = " + height);
        List<RangeDouble> leafNodes = this.allParents.get(this.height-1);
       // System.out.println("parentssize = " + this.allParents.size());
        //System.out.println("Arrr get parent = " + leafNodes.toString() +"\t d = " + d);
        RangeDouble r = binarySearch(leafNodes, d);
        //System.out.println("Find parent of " + d + " is " + r.toString());
        return r;
    }

    private RangeDouble binarySearch(List<RangeDouble> list, Double d){
//        System.out.println("binary Search...");
        
        if(list.isEmpty()){
            return null;
        }
        
        //System.out.println("binary Searchhhhhhhhhhhhhhhhhhhhhhh = " + list.get(0));
        
        int mid = (list.size()-1)/2;
        
        //System.out.println("mid = " + mid);
        if((list.size()-1)%2 > 0){
            mid++;
            //System.out.println("Mid of " + list.size() + " is " + mid);
        }
      // System.out.println("Mid of " + list.size() + " is " + mid);
        if(list.size()-1 == 1){
            //System.out.println("Return = " + list.get(0));
            return list.get(0);
        }
        RangeDouble r = list.get(mid);
        
        
        if(d < r.lowerBound){
            for ( int i = 0 ; i < mid ; i ++ ){
                r = list.get(i);
                
                if ( r.contains2(d,true)){
                    return r;
                }
            }
        }
        else if (d > r.upperBound){
            for ( int i = mid ; i < list.size() ; i ++ ){
                r = list.get(i);
                          
                if (list.get(list.size()-1).equals(new RangeDouble(Double.NaN,Double.NaN)) ){
                    if( i == list.size()-2){
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                    else{
                        if ( r.contains2(d,true)){
                            return r;
                        }
                    }
                }
                else{
                    if( i == list.size()-1){
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                    else{
                        if ( r.contains2(d,true)){
                            return r;
                        }
                    }
                }
            }
           //return binarySearch(list.subList(mid+1, list.size()-1), d);
        }
        else{
            return r;
        }
        
        /*if(d < r.lowerBound){
            return binarySearch(list.subList(0, mid-1), d);
        }
        else if (d > r.upperBound){
            
           return binarySearch(list.subList(mid+1, list.size()-1), d);
        }
        else{
            return r;
        }*/
        
        return null;
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
    public void setNodesType(String nodesType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findAllChildren(RangeDouble node,int sum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getInputFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Graph getGraph(String nodeInput, int nodeLevel) {
        Graph graph = new Graph();
        Node n = null;
        Edge e = null;
        ArrayList<String> nodes = null;
        boolean FLAG = false;
        String parent = null;
        List<RangeDouble> nodeChilds = null;
        RangeDouble nodeRange = null;
        String color = null;
        String label = null;
        
        RangeDouble node = null;
        //System.out.println("Get Graphhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        //System.out.println(" nodeInputtttt = " + nodeInput);
        //System.out.println(" nodeLevellllllll = " + nodeLevel);
        
        if (!nodeInput.equals("null") ){
            //System.out.println("nodeInput11111 = " + nodeInput);
            if ( nodeInput.equals("0-0")){
                node  = new RangeDouble(Double.NaN,Double.NaN);
            }
            else{
                String []temp = null;
                temp = nodeInput.split("-");
                node  = new RangeDouble(Double.parseDouble(temp[0]),Double.parseDouble(temp[1]));
            }
            
            if (nodesType.equals("double")){
                node.setNodesType("double");
            }
            else{
                node.setNodesType("int");
            }
        }
        
        int counter = 0;
        
        if (nodesType.equals("double")){
            if ( !nodeInput.equals("null") && !nodeInput.equals("") && nodeLevel != 0 ){

                if (height > nodeLevel + 1){
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                
                                if (nodeChilds.get(j).getLowerBound().isNaN() && nodeChilds.get(j).getUpperBound().isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                        
                    }
                    n = new Node(root+"",root+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }
                else{
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().isNaN() && nodeChilds.get(j).getUpperBound().isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root +"",root+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }

            }
            else{
                nodeRange = root;
                nodeLevel= 0;
                for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().isNaN() && nodeChilds.get(j).getUpperBound().isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                               
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root+"",root+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);


            }
        }
        else{
            if ( !nodeInput.equals("null") && !nodeInput.equals("") && nodeLevel != 0 ){
                //nodeDouble = Double.parseDouble(node);

                if (height > nodeLevel + 1){    
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().intValue() == 0 && nodeChilds.get(j).getUpperBound().intValue() == 0){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue(),label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange.getLowerBound().intValue() + "-" + nodeRange.getUpperBound().intValue(),nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue());
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue(),root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue(),0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }
                else{
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                } 
                                if (nodeChilds.get(j).getLowerBound().intValue() == 0 && nodeChilds.get(j).getUpperBound().intValue() == 0){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue(),label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange.getLowerBound().intValue() + "-" + nodeRange.getUpperBound().intValue(),nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue());
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue(),root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue()+"",0,null, this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);

                }
            }
            else{
                nodeRange = root;
                nodeLevel= 0;
                
                for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().intValue() == 0 && nodeChilds.get(j).getUpperBound().intValue() == 0){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue(),label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange.getLowerBound().intValue() + "-" + nodeRange.getUpperBound().intValue(),nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue());
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        
                        nodeRange = this.parents.get(nodeRange);
                        
                    }
                    n = new Node(root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue() ,root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue()+"",0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);
            }
        }
        
        Collections.reverse(graph.getNodeList());
        Collections.reverse(graph.getEdgeList());

        
        //graph.print();
        
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
        String str = null;
        System.out.println("Check Hierarchies");
        
        
        ArrayList<RangeDouble> firstArr = allParents.get(0);
        ArrayList<RangeDouble> lastArr = allParents.get(allParents.size()-1);
        ArrayList<RangeDouble> preLastArr = allParents.get(allParents.size()-2);
        
        double lowerLimit = firstArr.get(0).lowerBound;
        double upperLimit = firstArr.get(0).upperBound;
        
        double lower;
        double upper;
        
        
        //System.out.println("allParents size = " + allParents.size());
        
        
            //ean uparxei mono to null san paidi
            if (allParents.size() == 2){
                if (lastArr.size() == 1){
                    str = "Ok";
                    return str;
                    //System.out.println("everything ok with null");
                }
            }
        
            if (preLastArr.size() > lastArr.size()){
                str = "Hierarchy:" + this.name + "\nNumber of nodes in the last level must be greater than the previous level.";
                return str;
            }
            
            
            /*System.out.println("before");
            for( int i = 0 ; i < lastArr.size() ; i ++){
                System.out.println("xaxa = " + lastArr.get(i));
            }*/
            
            
            
            Collections.sort(lastArr, new Comparator<RangeDouble>(){
                @Override
                public int compare(RangeDouble o1, RangeDouble o2) {
                    return o1.getLowerBound().compareTo(o2.getLowerBound());
                }
            });
             
            /*System.out.println("after");
            for( int i = 0 ; i < lastArr.size() ; i ++){
                System.out.println("xaxa = " + lastArr.get(i));
            }*/ 

            /*//first node
            lower = lastArr.get(0).lowerBound;
            System.out.println("lower = " + lower +"\tupper = " + lastArr.get(0).upperBound +"\t lower limit = " + lowerLimit);
            if (lower != lowerLimit){
                str = "Hierarchy:" + this.name + "\nFirst node of the last level must have the same lower bound as the root node. ";
                return str;
            }

            //lastnode
            if(allParents.size() == 2){
                upper = lastArr.get(lastArr.size()-2).upperBound;
            }
            else{
                upper = lastArr.get(lastArr.size()-1).upperBound;
            }
            
            if (upper != upperLimit){
                str = "Hierarchy:" + this.name + "\nLast node of the last level must have the same upper bound as the root node.";
                return str;
            }*/

            
            //check all levels for continuous 

            for (Map.Entry<Integer,ArrayList<RangeDouble>> entry : this.allParents.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue().size());
                
                if(entry.getKey() != 0){
                    ArrayList <RangeDouble> tempArr = entry.getValue();
                    Collections.sort(tempArr, new Comparator<RangeDouble>(){
                        @Override
                        public int compare(RangeDouble o1, RangeDouble o2) {
                            return o1.getLowerBound().compareTo(o2.getLowerBound());
                        }
                    });



                    for( int i = 0 ; i < tempArr.size()-1 ; i ++){
                        RangeDouble r = tempArr.get(i);
                        RangeDouble tempR = tempArr.get(i+1);
                        if(!tempR.lowerBound.isNaN() && !tempR.upperBound.isNaN()){
                            if( !r.upperBound.equals(tempR.lowerBound)){
                                str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nNot continuous values between ranges:" + r.toString() + " and " + tempR.toString();
                                return str;
                                //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nNot continuous values between ranges:" + r.toString() + " and " + tempR.toString());
                            }
                        }
                        
                        if ( i == 0){
                            if ( r.lowerBound != lowerLimit){
                                str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nFirst node of the last level must have the same lower bound as the root node. Problem in range:" + r.toString() + ". Root range is :" + (int)lowerLimit + "-" + (int)upperLimit;
                                return str;
                                //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nFirst node of the last level must have the same lower bound as the root node. Problem in range:" + r.toString());
                            }
                        }
                        else if ( i == tempArr.size()-2){
                            if(!tempR.lowerBound.isNaN() && !tempR.upperBound.isNaN()){ 
                                if(!tempR.upperBound.equals(upperLimit)){
                                    str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString() + ". Root range is :" + (int)lowerLimit + "-" + (int)upperLimit;
                                    return str;
                                    //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString());
                                }
                            }
                            else{
                                if(!r.upperBound.equals(upperLimit)){
                                    str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString() + ". Root range is :" + (int)lowerLimit + "-" + (int)upperLimit;
                                    return str;
                                    //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString());
                                }
                            }
                        }
                    }
                }

            }

            //System.out.println("arr size = " + arr.size());

        
        return str;
    }

    @Override
    public RangeDouble getParent(Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int translateDateViaLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
