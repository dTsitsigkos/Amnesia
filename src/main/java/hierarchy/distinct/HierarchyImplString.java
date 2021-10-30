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
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsoninterface.View;


/**
 *
 * @author jimakos
 */
public class HierarchyImplString implements Hierarchy<Double> {
    String inputFile = null;
    String name = null;
    String nodesType = null;
    String hierarchyType = "distinct";
    int height = -1;
    BufferedReader br = null;
    //@JsonView(View.Hier.class)
    Double root = null;
    static DictionaryString dict = null;
    int counterNodes = 0;
    DictionaryString dictData = null;
    
    int levelFlash = -1;
    
    //@JsonView(View.Hier.class)
    Map<Double, List<Double>> children = null;
    Map<Double, NodeStats> stats = null;    
    Map<Double, Double> parents = null;
//    Map<String, List<String>> siblings = new HashMap<>();
    //@JsonView(View.Hier.class)
    Map<Integer, ArrayList<Double>> allParents = null;
    Map<Integer, List<Integer>> allParentIds = null;
    
    Map<Integer, Integer> parentsInteger = null;

    
    
    public HierarchyImplString(String inputFile,DictionaryString dictData){
        this.inputFile = inputFile;
        this.dictData = dictData;
        
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
//    Map<String, List<String>> siblings = new HashMap<>();
        if(dict == null){
            dict = new DictionaryString();
        }
        allParents = new HashMap<>();
        allParentIds = new HashMap<>();
        parentsInteger = null;
        
    }
    
    public HierarchyImplString(String _name, String _nodesType,DictionaryString dictData){
        this.name = _name;
        this.nodesType = _nodesType;
        this.dictData = dictData;
//        this.hierarchyType = "distinct";
        
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
//    Map<String, List<String>> siblings = new HashMap<>();
        if(dict == null){
            dict = new DictionaryString();
        }
        allParents = new HashMap<>();
        allParentIds = new HashMap<>();
        parentsInteger = null;
    }

    public void setNodesType(String nodesType) {
        this.nodesType = nodesType;
    }
    
    @Override
    public void load() throws LimitException{
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(this.inputFile),StandardCharsets.UTF_8));
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
    
    public static DictionaryString getWholeDictionary(){
        return dict;
    }
    
    public static void setWholeDictionary(DictionaryString d){
        dict = d;
    }
    
    private void loadHierarchy() throws IOException, LimitException{
        String line;
        int curLevel = this.height - 1;
        int strCount;
        if(dictData.isEmpty() && dict.isEmpty()){
            System.out.println("Both empty");
            strCount = 1;
        }
        else if(!dictData.isEmpty() && !dict.isEmpty()){
            System.out.println("Both have values");
            if(dictData.getMaxUsedId() > dict.getMaxUsedId()){
                strCount = dictData.getMaxUsedId()+1;
            }
            else{
                strCount = dict.getMaxUsedId()+1;
            }
        }
        else if(dictData.isEmpty()){
            System.out.println("Dict data empty");
            strCount = dict.getMaxUsedId()+1;
        }
        else{
            System.out.println("Dict hier empty");
            strCount = dictData.getMaxUsedId()+1;
        }
//        int strCount = dictData.getMaxUsedId()+1;
        System.out.println("strCount starts: "+strCount);
        List<String> lastParents = new ArrayList<String>();
        Map<String,List<Double>> childrenLast = new HashMap<String,List<Double>>();
        List<Double> lastLevelChilds;
        Double strId= -1.0;

        
        while ((line = br.readLine()) != null) {
            String tokens[] = line.split(" ");
            if(line.trim().isEmpty()){
//                System.out.println("Line "+line);
                if(curLevel==this.height -1 ){
                    for(String tkn : lastParents){
                        
                        
                        if(dictData.containsString(tkn)){
                            strId = dictData.getStringToId(tkn).doubleValue();
                            
                            dict.putIdToString(strId.intValue(), tkn);
                            dict.putStringToId(tkn, strId.intValue());
                        }
                        else{
                           
                            if(!dict.containsString(tkn)){
                                if(tkn.equals("NaN")){
                                    dict.putIdToString(2147483646, tkn);
                                    dict.putStringToId(tkn,2147483646);
                                }
                                else{
                                    dict.putIdToString(strCount, tkn);
                                    dict.putStringToId(tkn, strCount++);
                                }
                                
                            }
                            strId = dict.getStringToId(tkn).doubleValue();
                        }
                        
                        this.stats.put(strId, new NodeStats(curLevel-1));
                        lastLevelChilds = childrenLast.get(tkn);
                        this.children.put(strId, lastLevelChilds);
                        
                        counterNodes += lastLevelChilds.size();
                        if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                            throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
                        }
                        
                        for(Double chId : lastLevelChilds){
                            this.parents.put(chId, strId);
                            this.stats.put(chId, new NodeStats(curLevel));

                        }
                        
                        if(curLevel - 1 == 0 && lastParents.size()==1){
                            root = strId;
                            
                        }
                        
                    }
                }
                
                curLevel--;
                continue;
            }
            boolean isChild = false;
            List<Double> ch = new ArrayList<>();
            double idParent=-1;
            for (String token : tokens){
                if(token.equals("has")){
                    isChild = true;
//                    idParent = strId;
                    continue;
                }
                if(curLevel!= this.height-1){
                    
                    
                    
                    
                    if(dictData.containsString(token)){
                        strId = dictData.getStringToId(token).doubleValue();
                        
                        dict.putIdToString(strId.intValue(), token);
                        dict.putStringToId(token, strId.intValue());
                    }
                    else{
                        if(!dict.containsString(token)){
                            if(token.equals("NaN")){
                                dict.putIdToString(2147483646, token);
                                dict.putStringToId(token,2147483646);
                            }
                            else{
                                dict.putIdToString(strCount, token);
                                dict.putStringToId(token, strCount++);
                            }
                        }
                        strId = dict.getStringToId(token).doubleValue();
                    }
                    if(!isChild){
                        idParent = strId;
                    }
                    
                    if(isChild){
                        //System.out.println(token);
                        ch.add(strId);
                        counterNodes ++;
                        if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                            throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
                        }
                        this.stats.put(strId, new NodeStats(curLevel));
                        this.parents.put(strId, idParent);
//                        if(!this.children.containsKey(strId) && !this.dictData.containsString(token) && !this.dictData.containsId((int)strId)){
//                            this.dict.putIdToString((int)strId, token);
//                            dict.putStringToId(token, (int)strId);
//                        }
                    }
                    else{
                        this.stats.put(strId, new NodeStats(curLevel-1));

                        //level 0 and isChild == false then set as root
                        if(curLevel - 1 == 0){
                            root = idParent;
                            this.stats.put(root, new NodeStats(0));
                            counterNodes ++;
                            if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                                throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
                            }
                        }
                    }
                }
                else{
                    if(isChild && !dictData.containsString(token) && !dict.containsString(token)){
                        
                        if(token.equals("NaN")){
                            dict.putIdToString(2147483646, token);
                            dict.putStringToId(token,2147483646);
                            ch.add(2147483646.0);
                        }
                        else{
                            dict.putIdToString(strCount, token);
                            dict.putStringToId(token, strCount++);
                            ch.add((double)strCount-1);
                        }
                        
                    }
                    else if(isChild){
                        if(dictData.containsString(token)){
                            strId = dictData.getStringToId(token).doubleValue();
                            dict.putIdToString(strId.intValue(), token);
                            dict.putStringToId(token, strId.intValue());
                        }
                        else {
                            strId = dict.getStringToId(token).doubleValue();
                        }
                        
                        ch.add(strId);
                    }
                    else if(token.equals(tokens[0])){
                       lastParents.add(token);
                    }
                    
                }
                
                
                
                //System.out.println(token + ": " + isChild + " "  + curLevel);
                
            }
            
            if(curLevel != this.height -1){
                this.children.put(idParent, ch);
            }
            else{
                childrenLast.put(tokens[0], ch);
            }
            //set siblings
//            for(String child : ch){
//                List<String> sib = new ArrayList<>(ch);
//                sib.remove(child);
//                this.siblings.put(child, sib);
//            }
        }
        
//        System.out.println("Data dict");
//        for (Object objectName : dictData.idToString.keySet()) {
//            System.out.print(objectName+" : ");
//            System.out.println(dictData.idToString.get(objectName));
//        }
//        
//        System.out.println("Hier dict");
//         for (Object objectName : dict.idToString.keySet()) {
//            System.out.println(objectName);
//            System.out.println(dict.idToString.get(objectName));
//        }
        System.out.println("Num Nodes "+this.counterNodes);
        
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
    
    
    @Override
    public List<Double> getChildren(Double parent){
        return this.children.get(parent);
    }
    
    
    @Override
    public Integer getLevel(Double node){
        NodeStats nodeStats = this.stats.get(node);
        if(nodeStats == null){
//            System.out.println("Error: stats for node " + node + "value dictionary data "+dictData.getIdToString(node.intValue())+" value dictionary hierarvhy "+dict.getIdToString(node.intValue())+ " cannot be found!");
            return null;
        }
        
        return nodeStats.getLevel();
    }
    
    @Override
    public boolean checkExistance(Double d){
        if(d.doubleValue() != root.doubleValue()){
            return this.parents.get(d) != null;
        }
        else{
            return true;
        }
    }
    
    
    @Override
    public Double getParent(Double node){
        /*System.out.println("Parentssssssss");
        for (Map.Entry<Double, Double> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("childrens");
        for (Map.Entry<Double, List<Double>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        
        
        System.out.println("i am here!!!");*/
        if(levelFlash == -1){
            return this.parents.get(node);
        }
        else{
            Double anonValue = this.parents.get(node);
//            System.out.println("Flash level "+levelFlash+" node "+node+" "+this.dictData.getIdToString(node.intValue()));
//            System.out.println("Flash level "+levelFlash+"  anon node "+anonValue+" anonNode "/*+this.dictData.getIdToString(anonValue.intValue())+" level node "+this.getLevel((double)node)*/);
//            System.out.println("Flash level "+levelFlash+" level anon node "+this.getLevel((double)anonValue)+" anonNode "+this.dict.getIdToString(anonValue.intValue())+" level node "+this.getLevel((double)node));
            if(this.getLevel((double)node) == null){
                return null;
            }
            if(levelFlash == this.getLevel((double)node)){
                return anonValue;
            }
            else{
                return node;
            }
        }
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
    public Double getRoot(){
        return root;
    }
    
    @Override
    public String getName(){
        return this.name;
    }
    
    
    @Override
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
                //System.out.println("i = " + curLevel + "\t children = " + this.allParents.get(curLevel).toString() );
                //List<String> p = this.allParents.get(curLevel);
                for (Double curParent : this.allParents.get(curLevel)){
                    if(this.getChildren(curParent) == null){
                        continue;
                    }
                    if(this.getChildren(curParent).isEmpty())
                        continue;
                    StringBuilder sb = new StringBuilder();
                    for (Double child : this.getChildren(curParent)){
                        String childToken;
                        if(curLevel == height-2){
                            childToken = dictData.getIdToString(child.intValue());
                            if(childToken == null){
                                childToken = dict.getIdToString(child.intValue());
                            }
                        }
                        else{
                            childToken = dict.getIdToString(child.intValue());
                            if(childToken == null){
                                childToken = dictData.getIdToString(child.intValue());
                            }
                        }
                        
                        sb.append(childToken);
                        sb.append(" ");
                    }
                    
                    String parentVal = dict.getIdToString(curParent.intValue());
                    if(parentVal == null){
                        parentVal = dictData.getIdToString(curParent.intValue());
                    }
                    writer.println(parentVal + " has " + sb.toString());
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
    
    private boolean isLeafLevel(ArrayList<Double> parentsInLevel){
        boolean  leafLevel = true;
        
        for (Double parent : parentsInLevel){
            List<Double> ch = this.children.get(parent);
            if(ch != null && ch.size() > 0){
                leafLevel = true;
                break;
            }
        }
        return leafLevel;
    }
    
    @Override
    public Map<Integer, Set<Double>> remove(Double item)
    {
        Map<Integer, Set<Double>> nodesMap = BFS(item,null);
        for(Integer i = nodesMap.keySet().size() ; i > 0 ; i--)
        {
            //System.out.println(i + "-> " + nodesMap.get((i)));
            
            for(Double itemToDelete : nodesMap.get(i)){
                
//                if(this.getLevel(itemToDelete)!=this.height-1){
//                    dict.remove(itemToDelete.intValue());
//                }
                //System.out.println(itemToDelete.toString());
                if(itemToDelete.equals(root)){
                    System.out.println("Cannot remove root");
                    return null;
                }
                
                this.counterNodes--;
                children.remove(itemToDelete);
                children.get(this.parents.get(itemToDelete)).remove(itemToDelete);
                parents.remove(itemToDelete);
//                List<String> sibs = siblings.get(itemToDelete);
//                if(sibs != null){
//                    for(String sib : sibs){
//                        if(siblings.get(sib) != null)
//                            siblings.get(sib).remove(itemToDelete);
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
                
                //children
                this.children.put(newValue, childrenList);
                this.children.remove(oldValue);
                
                changeRoot = true;
                
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
                this.stats.remove(root);
                this.root = newValue;
                this.stats.put(root, new NodeStats(0));
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
                for (Double d : s){
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
    
    
    public Map<Integer,Set<Double>> BFS(Double firstnode,Double lastNode){
        Map<Integer,Set<Double>> bfsMap = new HashMap<>();
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
        
//        DictionaryString dict = dataset.getDictionary();
        if(dict == null){
            System.out.println("No dictionary found  " );
            return;
        }
        //System.out.println(c);
        
        if(dataset instanceof SETData){
            for (double[] rowData : data) {
                for(double d : rowData){
//                    String fromDict = dict.getIdToString((int)d);
                    NodeStats s = this.stats.get(d);
                    
                    if(s != null){      //find weights of leaf level
                        List<Double> cList = this.children.get(d);
                        if(cList == null || cList.isEmpty()){
                            
                            //System.out.println(rowData[c]);
                            s.weight++;
                        }
                    }
                }
            }
        }
        else if(dataset instanceof RelSetData){
            RelSetData datasetRelSet = (RelSetData) dataset;
            if(c == datasetRelSet.getSetColumn() ){
                data = datasetRelSet.getSet();
                for (double[] rowData : data) {
//                    System.out.println("rowData "+Arrays.toString(rowData));
                    for(double d : rowData){
    //                    String fromDict = dict.getIdToString((int)d);
                        NodeStats s = this.stats.get(d);

                        if(s != null){      //find weights of leaf level
                            List<Double> cList = this.children.get(d);
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
//                String fromDict = dict.getIdToString((int)columnData[c]);
                    NodeStats s = this.stats.get(columnData[c]);

                    if(s != null){      //find weights of leaf level
                        List<Double> cList = this.children.get(columnData[c]);
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
//                String fromDict = dict.getIdToString((int)columnData[c]);
                NodeStats s = this.stats.get(columnData[c]);
                
                if(s != null){      //find weights of leaf level
                    List<Double> cList = this.children.get(columnData[c]);
                    if(cList == null || cList.isEmpty()){
                        
                        //System.out.println(rowData[c]);
                        s.weight++;
                    }
                }
            }
        }
        
        //find weights for inner nodes
        for(int j = this.allParents.keySet().size()-2 ; j>=0 ; j--){
            for(Double node : this.allParents.get(j)){
                Integer totalWeight = 0;
                List<Double> cList = this.children.get(node);
                if(cList != null && !cList.isEmpty()){
                    for(Double child : cList){
                        totalWeight += this.stats.get(child).weight;
                    }
                    this.stats.get(node).weight = totalWeight;
                }
                
            }
        }
    }
    
    @Override
    public Double checkColumn(int column, Data dataset){
        
//        DictionaryString colDict = dataset.getDictionary();
        
        double[][] data = dataset.getDataSet();
        int dataLength = dataset.getDataLenght();
        
        //check if every value in the column is present in this hierarchy
        for(int i=0; i<dataLength; i++){
            
            //if not, return value missing
            if(!this.stats.containsKey(dictData.getIdToString((int)data[i][column]))){
                return data[i][column];
            }
        }
        
        
        return null;
    }
    
    @Override
    public Integer getWeight(Double node){
        NodeStats stats = this.stats.get(node);
        if(stats == null){
            return null;
        }
        return stats.weight;
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
    public DictionaryString getDictionary() {
        return this.dict;
    }
    
    @Override
    public DictionaryString getDictionaryData(){
        return this.dictData;
    }
    
    @Override
    public void transformParents(){
        parentsInteger = null;
        parentsInteger = new HashMap<>();
        for (Double key : parents.keySet()) {
            parentsInteger.put(key.intValue(), parents.get(key).intValue());
        }
        
        
    }
    
    @Override
    public  Map<Integer,Integer> getParentsInteger(){
        return parentsInteger;
    }
    
//    @Override
//    public Double getParent(Double d) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
    @Override
    public int getLevelSize(int level) {
        return this.allParents.get(this.height - level - 1).size();
    }
    
    @Override
    public double getParentId(double d) {
//        String stringValue = this.dict.getIdToString((int)d);
        Double parentStringValue = this.parents.get(d);
        if(parentStringValue == null){
            return -1;
        }
        else{
            return parentStringValue;
        }
    }
    
    @Override
    public void buildDictionary(DictionaryString dictionary) {
//        int id = dictionary.getMaxUsedId();
//        
//        //add values to dictionnary bottom-up
//        for(int curLevel = Collections.max(this.allParents.keySet()); curLevel != -1; curLevel--){
//            for(String value : this.allParents.get(curLevel)){
//                if(!dictionary.containsString(value)){
//                    dictionary.putIdToString(++id, value);
//                    dictionary.putStringToId(value, id);
//                }
//            }
//        }
//        
//        //set column's dictionary the same as hierarchy's
//        this.dict = dictionary;
    }

    @Override
    public Set<Double> getChildrenIds(double d) {
//        System.out.println(this.stats.keySet().size());
//        String stringValue = this.dict.getIdToString((int)d);
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
//        System.out.println("getLevel hierarchy = " + nodeId + "\t height = " + this.height);
//        String value = this.dict.getIdToString((int)nodeId);
        if(this.getLevel((Double)nodeId) == null){
            return null;
        }
        return (this.height - this.getLevel((Double)nodeId) - 1) ;
    }

    @Override
    public List<Integer> getNodeIdsInLevel(int level) {
        /*int curLevel = this.height - level - 1;
        System.out.println("currentLevel = " + curLevel);
        List<Integer> curLevelIds = this.allParentIds.get(curLevel);
        if(curLevelIds == null){
            
            ArrayList<Double> nodesInLevel = this.getAllParents().get(curLevel);
            
            List<Integer> nodeIdsInLevel = new ArrayList<>(nodesInLevel.size());
            for(Double id : nodesInLevel){
                if ( curLevel == this.height - 1){
                    nodeIdsInLevel.add(id.intValue());
                    //nodeIdsInLevel.add(this.dictData.g);
                }
                else{
                    nodeIdsInLevel.add(id.intValue());
                }
            }
            
            this.allParentIds.put(curLevel, nodeIdsInLevel);
            return nodeIdsInLevel;
        }
        else{
            return curLevelIds;
        }*/
        
        int curLevel = this.height - level - 1;
//        System.out.println("currentLevel = " + curLevel);
        List<Integer> curLevelIds = this.allParentIds.get(curLevel);
        if(curLevelIds == null){
            
            ArrayList<Double> nodesInLevel = this.getAllParents().get(curLevel);          
            List<Integer> nodeIdsInLevel = new ArrayList<>(nodesInLevel.size());
            for(Double id : nodesInLevel){
                if ( curLevel == this.height - 1){
                    nodeIdsInLevel.add(id.intValue());
                    //nodeIdsInLevel.add(this.dictData.g);
                }
                else{
                    nodeIdsInLevel.add(id.intValue());
                }
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
//        String value = this.dict.getIdToString((int)nodeId);
        return getWeight((Double)nodeId);
    }
    

    @Override
    public int findAllChildren(Double node,int sum) {
        int result = 0;
       
        List<Double> child = this.getChildren(node);
       
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
        List<Double> nodeChilds = null;
        int counter = 0;
        int counterNode = 0;
        String color = null;
        String label = null;
        Double nodeId;
        String nodeChild;
        
        System.out.println("roottttttttttttttttttttttttttt = " + root);
        System.out.println("nodeeeeeeeeeeeeeeeeeeeeee = " + node);
        
      
        
        if ( !node.equals("(null)") && !node.equals("") && nodeLevel != 0 ){
            
            if(dict.containsString(node)){
                nodeId = (double) dict.getStringToId(node);
            }
            else{
                nodeId = (double) dictData.getStringToId(node);
            }
            
            System.out.println("i am here"+nodeId);
            counter = 1;
            if (height > nodeLevel + 1){
                for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(nodeId);
                    //System.out.println(node);
                    counterNode = counter;
                    counter ++;                
                    //Collections.sort(nodeChilds);
                    if ( nodeChilds != null){
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){
                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                                nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                    nodeChild = dict.getIdToString(nodeChilds.get(j).intValue()); 
                                }
                            }
                            else{
                                color = null;
                                nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                  nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());  
                                }
                            }
                            
                            if (nodeChild.equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChild;
                            }

//                            n = new Node(nodeChilds.get(j),label,i+1,color,this.hierarchyType + "," +this.nodesType); 
                            
                            n = new Node(nodeChild,label,i+1,color,this.hierarchyType + "," +this.nodesType);
                            graph.setNode(n);
                            e = new Edge(node,nodeChild);
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    nodeId = this.parents.get(nodeId);
                    System.out.println("ParentId: "+nodeId);
                    if(nodeId!=null){
                        node = dict.getIdToString(nodeId.intValue());
                        if(node == null){
                            node = dictData.getIdToString(nodeId.intValue());
                        }
                    }
                    //System.out.println("node = " + node);
                }
                n = new Node(dict.getIdToString(root.intValue()),dict.getIdToString(root.intValue()),0,null,null);
                graph.setNode(n);
                Collections.reverse(graph.getNodeList());
                Collections.reverse(graph.getEdgeList());
                
            }
            else{
                System.out.println("i am here222");
                for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(nodeId);
                   
                    System.out.println("ola kala");
                    //Collections.sort(nodeChilds);
                    if ( nodeChilds != null){
                        counterNode = counter;
                        counter ++;
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){
                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                                nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                    nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                }
                            }
                            else{
                                color = null;
                                nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                  nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());  
                                }
                            }
                            
                            if (nodeChild.equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChild;
                            }
                            n = new Node(nodeChild,label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                            graph.setNode(n);
                            System.out.println("Node "+node+" child "+nodeChild);
                            e = new Edge(node ,nodeChild);
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    nodeId = this.parents.get(nodeId);
                    if(nodeId!=null){
                        node = dict.getIdToString(nodeId.intValue());
                        if(node == null){
                            node = dictData.getIdToString(nodeId.intValue());
                        }
                    }
                    //System.out.println("node = " + node);
                }
                n = new Node(dict.getIdToString(root.intValue()),dict.getIdToString(root.intValue()),0,null,this.hierarchyType + "," +this.nodesType);
                graph.setNode(n);
            
            }
            
        }
        else{
            nodeId = root;
            nodeLevel= 0;
            for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(nodeId);
                    //Collections.sort(nodeChilds);
                    counterNode = counter;
                    counter ++;
                    if ( nodeChilds != null){
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){

                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                                nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                    nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                }
                            }
                            else{
                                color = null;
                                nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                  nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());  
                                }
                            }
                            
                            if (nodeChild.equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChild;
                            }
                            n = new Node(nodeChild,label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                            graph.setNode(n);
                            e = new Edge(dict.getIdToString(root.intValue()),nodeChild);
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    nodeId = this.parents.get(nodeId);
                    
                    //System.out.println("nodeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee = " + node);
                }
                System.out.println("Root "+root+" dictionary ");
                n = new Node(dict.getIdToString(root.intValue()),dict.getIdToString(root.intValue()),0,null,this.hierarchyType + "," +this.nodesType);
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
        ArrayList<Edge> list = graph.getEdgeList();
        Collections.sort(list, new Comparator<Edge>() {
            @Override
            public int compare(Edge s1, Edge s2) {
                return s1.getTo().compareToIgnoreCase(s2.getTo());
            }
        });
        
        graph.setEdgeList(list);
        
        
        return graph;
    }

    @Override
    public String checkHier(Data d,int col) {
        String str = "Ok";
        if(d instanceof DiskData){
            DiskData diskData = (DiskData) d;
            Set<Double> values = new HashSet();
            for(int i=0; i<this.height; i++){
                values.addAll(this.allParents.get(i));
            }
            List<Double> missingValues = diskData.checkValues(values, col);
            if(!missingValues.isEmpty()){
                String originalValues = "";
                String realValue = "";
                for(int i=0; i<missingValues.size(); i++){
                    if(Double.isNaN(missingValues.get(i)) || missingValues.get(i).equals(2147483646.0)){
                        if(this.getParent(2147483646.0) == null){
                           return "Node (null) for spaces values, is not defined in the hierarchy \""+this.name+"\"" ;
                        }
                        missingValues.remove(i);
                    }
                }
                if(missingValues.size() == 1){
                    realValue = this.dictData.getIdToString(missingValues.get(0).intValue());
                    if(realValue == null){
                        realValue = this.dict.getIdToString(missingValues.get(0).intValue());
                    }
                    return "Value \""+realValue+"\" are not defined in hierarchy \""+this.name+"\"";
                }
                else if(!missingValues.isEmpty()){
                    
                    for(Double missingValue : missingValues) {
                        realValue = this.dictData.getIdToString(missingValue.intValue());
                        if(realValue == null){
                            realValue = this.dict.getIdToString(missingValue.intValue());
                        }
                        originalValues += realValue +", ";
                    }
                    return "Values \""+originalValues.substring(0, originalValues.length() - 1)+"\" are not defined in hierarchy \""+this.name+"\"";
                }
            }
        }
        else if(d instanceof SETData){
            double[][] dataset = d.getDataSet();
            for(int i=0; i<dataset.length; i++){
                for(int j=0; j<dataset[i].length; j++){
                    Double parent = this.parents.get(dataset[i][j]);
                    if(parent == null && root.doubleValue()!=dataset[i][j]){
                        String value = d.getDictionary().getIdToString((int)dataset[i][j]);
                        if(value == null){
                            System.out.println("Null in dictionary");
                            value = dict.getIdToString((int)dataset[i][j]);
                        }
                        if(value.equals("NaN")){
                            if(this.parents.get(2147483646.0)==null){
                                return "Node (null) for spaces values and non-Date values, is not defined in the hierarchy \""+this.name+"\""; 
                            }
                        }
                        else{
                            
                            return "Value \""+value+"\" is not defined in the hierarchy \""+this.name+"\"";
                        }
                    }
                }
            }
            
        }
        else if(d instanceof RelSetData){
            RelSetData relsetData = (RelSetData) d;
            double[][] dataset;
            if(relsetData.getSetColumn() == col){
                dataset = relsetData.getSet();
                for(int i=0; i<dataset.length; i++){
                    for(int j=0; j<dataset[i].length; j++){
                        Double parent = this.parents.get(dataset[i][j]);
                        if(parent == null && root.doubleValue()!=dataset[i][j]){
                            String value = relsetData.getDictionary().getIdToString((int)dataset[i][j]);
                            if(value == null){
                                value = dict.getIdToString((int)dataset[i][j]);
                            }
                            if(value.equals("NaN")){
                                if(this.parents.get(2147483646.0)==null){
                                    return "Node (null) for spaces values and non-Date values, is not defined in the hierarchy \""+this.name+"\""; 
                                }
                            }
                            else{
                                
                                return "Value \""+value+"\" in "+d.getColumnByPosition(col)+" column is not defined in the hierarchy \""+this.name+"\"";
                            }
                        }
                    }
                }
            }
            else{
                dataset = relsetData.getDataSet();
                for(int i=0; i<dataset.length; i++){
                    Double parent = this.parents.get(dataset[i][col]);
                    if(parent == null && root.doubleValue()!=dataset[i][col]){
                        String value = relsetData.getDictionary().getIdToString((int)dataset[i][col]);
                        if(value == null){
                            value = dict.getIdToString((int)dataset[i][col]);
                        }
                        return "Value \""+value+"\"  in "+d.getColumnByPosition(col)+" column is not defined in the hierarchy \""+this.name+"\"";
                    }
                }
            }
        }
        else{
            double[][] dataset = d.getDataSet();
            for(int i=0; i<dataset.length; i++){
                Double parent = this.parents.get(dataset[i][col]);
                if(parent==null){
                    String value = d.getDictionary().getIdToString((int)dataset[i][col]);
                    if(value == null){
                        value = dict.getIdToString((int)dataset[i][col]);
                    }
                    if(dataset[i][col]!= root.doubleValue()){
                        return "Value "+value+" in "+d.getColumnByPosition(col)+" column is not defined in the hierarchy \""+this.name+"\"";
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
    public Map<Integer, Set<Double>> getLeafNodesAndParents() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Set<Double> leaves = new HashSet<Double>(),parentLeaves = new HashSet<Double>();
        boolean isParentLeaf = true;
        for(Entry<Integer,ArrayList<Double>> entry : this.allParents.entrySet()){
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
            
            if(entry.getKey() == height-2){
                parentLeaves.addAll(nodesInLevel); 
            }
            else if(entry.getKey() == height-1){
                leaves.addAll(nodesInLevel);
            }
        }
        Map<Integer,Set<Double>> nodesAndParents = new HashMap();
        nodesAndParents.put(0, leaves);
        nodesAndParents.put(1, parentLeaves);
        return nodesAndParents;
    }
    
    public void replaceHierValues(Integer id, Integer oldId){
        /// children stats parents allParents
//        System.out.println("Edv mpainei gia sync");
        if(this.children.containsKey(oldId.doubleValue())){
            List<Double> childrenList = this.children.get(oldId.doubleValue());
            this.children.remove(oldId.doubleValue());
            this.children.put(id.doubleValue(), childrenList);
        }
        
        for(Entry<Double,List<Double>> entryChildren: this.children.entrySet()){
            if(entryChildren.getValue().contains(oldId.doubleValue())){
                entryChildren.getValue().remove(oldId.doubleValue());
                entryChildren.getValue().add(id.doubleValue());
                break;
            }
        }
        
        if(this.parents.containsKey(oldId.doubleValue())){
            Double parent = this.parents.get(oldId.doubleValue());
            this.parents.remove(oldId.doubleValue());
            this.parents.put(id.doubleValue(), parent);
        }
        
        if(this.parents.containsValue(oldId.doubleValue())){
            for(Entry<Double,Double> entryParents : this.parents.entrySet()){
                if(entryParents.getValue() == oldId.doubleValue()){
                    this.parents.put(entryParents.getKey(), id.doubleValue());
                }
            }
        }
        
        for(Entry<Integer,ArrayList<Double>> entryLevels : this.allParents.entrySet()){
            ArrayList<Double> levelList = entryLevels.getValue();
            if(levelList.contains(oldId.doubleValue())){
                levelList.remove(oldId.doubleValue());
                levelList.add(id.doubleValue());
            }
        }
        
        if(this.stats.containsKey(oldId.doubleValue())){
            NodeStats tempStats = this.stats.get(oldId.doubleValue());
            this.stats.remove(oldId.doubleValue());
            this.stats.put(id.doubleValue(), tempStats);
        }
        
//        System.out.println("Edw vgainei gia sync");
        
    }
    
    public void findValue(Integer id, String value){ /// dict hier id=value
        Integer newId = this.dictData.getMaxUsedId() > this.dict.getMaxUsedId() ? this.dictData.getMaxUsedId()+1 : this.dict.getMaxUsedId()+1;
        if(dictData.containsString(value)){
            this.dictData.replace(value, newId, id);    
        }
        this.dict.replace(value, newId, id);
        replaceHierValues(newId,id);
    }
    
    public void setId(Integer id,String value){
        if(this.dict.containsId(id) && !this.dict.getIdToString().get(id).equals(value)){
            findValue(id,this.dict.getIdToString().get(id));
            this.dict.replace(value, id, this.dict.getStringToId(value));
        }
        else if(!this.dict.containsId(id) && this.dict.containsString(value)){
            this.dict.replace(value, id, this.dict.getStringToId(value));
            
        }
        replaceHierValues(id,this.dict.getStringToId(value));
//        else if(!this.dict.containsId(id)){
//            this.dict.putIdToString(id, value);
//            this.dict.putStringToId(value, id);
//        }
    }

    @Override
    public void syncDictionaries(Integer column, Data data) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        Map<String,Integer> stringToIdData = new HashMap(this.dictData.getStringToId());
        Map<Integer,String> idToStringData = new HashMap(this.dictData.getIdToString());
        Map<String,Integer> newValues = new HashMap();
        double[][] dataset;
        boolean set=false;
        if(data instanceof RelSetData){
            RelSetData relData = (RelSetData) data;
            if(column == relData.getSetColumn()){
               dataset = relData.getSet();
               set = true;
            }
            else{
                dataset = relData.getDataSet();
            }
        }
        else if(data instanceof SETData){
            dataset = data.getDataSet();
            set = true;
        }
        else if(data instanceof DiskData){
            return;
        }
        else{
            dataset = data.getDataSet();
            
        }
        
        if(!set){
            for(int i=0; i<dataset.length; i++){
                double value = dataset[i][column];
                String strValue = idToStringData.get((int)value);
                if(!newValues.containsKey(strValue)){
                    if(dict.containsString(strValue) && value != dict.getStringToId(strValue).doubleValue()){
                        Integer newId = this.dictData.getMaxUsedId() > this.dict.getMaxUsedId() ? this.dictData.getMaxUsedId()+1 : dict.getMaxUsedId()+1;
                        this.dictData.replace(strValue, newId, (int)value); 
                        replaceHierValues(newId,this.dict.getStringToId(strValue));
                        dict.replace(strValue, newId, this.dict.getStringToId(strValue));
                        dataset[i][column] = newId;
                        newValues.put(strValue, newId);
                    }
                }
                else{
                    dataset[i][column] = newValues.get(strValue);
                }
            }
        }
        else{
            for(int i=0; i<dataset.length; i++){
                for(int j=0; j<dataset[i].length; j++){
                    double value = dataset[i][j];
                    String strValue = idToStringData.get((int)value);
                    if(!newValues.containsKey(strValue)){
//                        System.out.println("Mpainbei set");
                        if(this.dict.containsString(strValue) && value != this.dict.getStringToId(strValue).doubleValue()){
                            Integer newId = this.dictData.getMaxUsedId() > this.dict.getMaxUsedId() ? this.dictData.getMaxUsedId()+1 : this.dict.getMaxUsedId()+1;
                            this.dictData.replace(strValue, newId, (int)value); 
                            replaceHierValues(newId,this.dict.getStringToId(strValue));
                            this.dict.replace(strValue, newId, this.dict.getStringToId(strValue));
                            dataset[i][j] = newId;
                            newValues.put(strValue, newId);
                        }
                    }
                    else{
                        dataset[i][j] = newValues.get(strValue);
                    }
                }
            }
        }
        
        
//        for(Entry<String,Integer> entryData : stringToIdData.entrySet()){
//            if(this.dict.containsString(entryData.getKey())){
//                if(!this.dict.getStringToId(entryData.getKey()).equals(entryData.getValue())){
////                    setId(entryData.getValue(),entryData.getKey());
//                    
////                    System.out.println("hier "+this.dict.getStringToId(entryData.getKey())+" data "+entryData.getValue()+" string "+entryData.getKey());
//                    
////                    Integer newId = this.dictData.getMaxUsedId() > this.dict.getMaxUsedId() ? this.dictData.getMaxUsedId()+1 : this.dict.getMaxUsedId()+1;
////                    this.dictData.replace(entryData.getKey(), newId, entryData.getValue()); 
////                    replaceHierValues(newId,this.dict.getStringToId(entryData.getKey()));
////                    this.dict.replace(entryData.getKey(), newId, this.dict.getStringToId(entryData.getKey()));
//                    
//                }
//            }
//        }
    }

    @Override
    public int findCommonHeight(Double n1, Double n2) {
        if(n1.equals(root)){
            return this.getLevel(root.doubleValue());
        }
        int height1 = this.getLevel(n1.doubleValue());
        int height2 = this.getLevel(n2.doubleValue());
//        System.out.println("Searching common str "+n1+" and "+n2);
        if(n1.equals(n2)){
//            System.out.println("Common str "+n1+" with height "+height1);
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
//                System.out.println("Common str "+n1+" with height "+height1);
                return height2;
            }
            
            while(!n1.equals(n2)){
                n1 = this.getParent(n1);
                n2 = this.getParent(n2);
                height2 = this.getLevel(n2.doubleValue());
                height1 = this.getLevel(n1.doubleValue());
            }
            
//            System.out.println("Common str "+n1+" with height "+height1);
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
        
//        System.out.println("Searching common anc str "+n1+" and "+n2);
        if(n1.equals(n2)){
//            System.out.println("Common anc str "+n1+" with height "+height1);
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
//                System.out.println("Common anc str "+n1+" with height "+height1);
                return n1;
            }
            
            while(!n1.equals(n2)){
                n1 = this.getParent(n1);
                n2 = this.getParent(n2);
                height2 = this.getLevel(n2.doubleValue());
                height1 = this.getLevel(n1.doubleValue());
            }
            
//            System.out.println("Common anc str "+n1+" with height "+height1);
            return n1;
            
        }
    }

    @Override
    public void clearAprioriStructures() {
        allParentIds = new HashMap();
        parentsInteger = null;
    }

    @Override
    public Double getParent(Double v, Integer k) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getPopulation(double v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getPopulation(Double rd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}   
