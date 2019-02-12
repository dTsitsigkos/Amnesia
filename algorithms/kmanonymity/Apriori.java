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

import algorithms.Algorithm;
import algorithms.flash.LatticeNode;
import data.Data;
import dictionary.DictionaryString;
import graph.Graph;
import hierarchy.Hierarchy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author serafeim
 */
public class Apriori implements Algorithm {
    Data dataset = null;
    double[][] data = null;
    Map<Integer, Hierarchy> hierarchies = null;
    Hierarchy hierarchy = null;
    Trie trie = null;
    int nextIndex = 0;
    int[] testGens = null;
    int[][] pointMap = null;
    double[] costs = null;
    int domainSize = -1;
    int k = -1;
    int m = -1;
    
    @Override
    public void setDataset(Data dataset) {
        this.dataset = dataset;
        this.data = this.dataset.getDataSet();
    }
    
    @Override
    public void setHierarchies(Map<Integer, Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
        this.hierarchy = hierarchies.get(0);
    }
    
    @Override
    public void setArguments(Map<String, Integer> arguments) {
        this.k = arguments.get("k");
        this.m = arguments.get("m");
    }
    
    @Override
    public void anonymize() {
        System.out.println("New Apriori");
        createInternalStructures();
        trie = new Trie(hierarchy);
        
        for(int i=1; i<=m; i++){
            System.out.println("m = " + i);
            populateTree(i);
            fixAll();           
        }
    }
    
    public void populateTree(int size){
        double[] transaction = null;
        Set <double[]> combinations;
        
        /*DictionaryString dict = dataset.getDictionary(0);
        
        for (Map.Entry<Integer,String> entry : dict.idToString.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        
        while((transaction = getNextTransaction()) != null){
            /*System.out.println("transaction = ");
            for (int i = 0 ; i < transaction.length ; i ++ ){
                System.out.println(transaction[i]+",");
            }*/
            
            //expand transaction
            Set<Double> expandedTransaction = expandTransaction(transaction);
            
            //generate combinations
            combinations = Combinations.getCombinations(expandedTransaction, size, hierarchy);
            
            /*System.out.println("combinations = ");
            for (double[] s : combinations) {
                for ( int i = 0 ; i < s.length ; i++){
                    System.out.print(s[i] + ",");
                }
                System.out.println("end");
            }*/

            
            //insert combinations to count-tree
            if (combinations != null && !combinations.isEmpty()){
                for(double[] comb : combinations ){
                    trie.insert(comb);
                }
            }
            
            //System.out.println("i am hereeeeeee");
        }
        
        //System.out.println("Trieeeeeeeeeeeee "  );
        //trie.printTree(trie.getRoot());
        //System.out.println("trie = " + trie.getRoot());
    }
    
    private double[] getNextTransaction(){
        if(nextIndex == data.length){
            nextIndex = 0;
            return null;
        }
        
        double[] originalTransaction = data[nextIndex];
        double[] anonTransaction = new double[originalTransaction.length];
        for(int i=0; i<originalTransaction.length; i++){
            anonTransaction[i] = getTranslation(originalTransaction[i]);
        }
        nextIndex++;
        
        //System.out.println("originalTransaction = ");
        /*for (int i = 0 ; i < originalTransaction.length ; i ++ ){
            System.out.println(originalTransaction[i]+",");
        }
        
        //System.out.println("anonTransaction = ");
        for (int i = 0 ; i < anonTransaction.length ; i ++ ){
            System.out.println(anonTransaction[i]+",");
        }*/
        
        
        return anonTransaction;
    }
    
    private double getTranslation(double point) {
        return pointMap[(int)point][1];
    } 
    
    private void fixAll(){
        
        TrieNode curNode;
        
        //in preorder count tree traversal
        while((curNode = trie.preorderNext()) != null){
            Double nodeValue = curNode.getValue();
            //System.out.println("curNode = " + curNode +"/t nodeValue = " + nodeValue );
            
            //bypass if already been generalized
            if(nodeValue != getTranslation(nodeValue))
                continue;
            
            //fix if leaf with support < k
            if(curNode.isLeaf() && (curNode.getSupport() < k)){
                List<Double> itemset = curNode.getItemset();
                /*if(itemset.size() == 4){
                    System.out.println(itemset);
                }
                System.out.println("pre " + itemset + " " + curNode.getSupport());*/
                fix(itemset);
            }
        }
        
        /*for ( int i = 0 ; i < pointMap.length; i++){
            //for ( int j = 0 ; j < pointMap[i].length; j++){
                System.out.println("pointMap[0] = " + pointMap[i][0] + "/t pointMap[1] = " + pointMap[i][1]);
           // }
        }*/
        
    }
     
    public List<List<Double>> getAllCombinations(List<Double> itemset,int numGen){   
        List<List<Double>> comb = null;
        Double[] arrGen = null;
        Double item = null;
        List<TempContainer<Double>> ancestor = new ArrayList<>();
        List<Double> temp = null;
        TempContainer<Double> temp2;
        int counter = 0 ;

        for ( int i = 0 ; i < itemset.size() ; i ++ ){
            counter = 0;
            temp = new ArrayList<Double>();
            
            if ( itemset.size() != 1 ){
                temp.add(itemset.get(i));
            }
            
            item = hierarchy.getParentId(itemset.get(i));
            
            if ( item != -1 ){
                temp.add(item);
                counter ++;
            }
            
            while(item != -1 && counter < numGen){
                item = hierarchy.getParentId(item);
                if ( item != -1 ){
                    temp.add(item);
                    counter ++;
                }
                
            }
            temp2 = new TempContainer<Double>();
            temp2.setItems(temp);
            ancestor.add(temp2);
        }
        
        for ( int i = 0 ; i < ancestor.size() ; i ++ ){
            temp2 = ancestor.get(i);
        }
        
        comb = getCombination(0, ancestor);
        
        for ( int i = 0 ; i < comb.size() ; i ++){
            List<Double> arr = null;
            arr = comb.get(i);
        }
        
        return comb;
    }
    
    private List<List<Double>> getCombination(int currentIndex, List<TempContainer<Double>> containers) {
        if (currentIndex == containers.size()) {
            // Skip the items for the last container
            List<List<Double>> combinations = new ArrayList<List<Double>>();
            combinations.add(new ArrayList<Double>());
            return combinations;
        }
        
        List<List<Double>> combinations = new ArrayList<List<Double>>();
        TempContainer<Double> container = containers.get(currentIndex);
        List<Double> containerItemList = container.getItems();
        // Get combination from next index
        List<List<Double>> suffixList = getCombination(currentIndex + 1, containers);
        int size = containerItemList.size();
        
        for (int ii = 0; ii < size; ii++) {
            Double containerItem = containerItemList.get(ii);
            if (suffixList != null) {
                for (List<Double> suffix : suffixList) {
                    List<Double> nextCombination = new ArrayList<Double>();
                    nextCombination.add(containerItem);
                    nextCombination.addAll(suffix);
                    combinations.add(nextCombination);
                }
            }
        }
        
        return combinations;
    }
    
    @Override
    public Object getResultSet() {
        Map<Double, Double> rules = new HashMap<>();
        for(int i=0; i<pointMap.length; i++){
            if(pointMap[i][0] != pointMap[i][1]){
                rules.put((double)pointMap[i][0], (double)pointMap[i][1]);
            }
        }
        
        return rules;
    }
    
    @Override
    public Graph getLattice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean isAnonymousResult(LatticeNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void fix(List<Double> prefix) {
        Set<Double> domain = expandTransaction(prefix);
        
        //System.out.println("prefix = " + prefix.toString() + "\t domain = " + domain.toString());
        
        if(domain.size() > 1){
            Map<Double, double[]> results = new TreeMap<>();
            SolutionCombinations solCombs = new SolutionCombinations(this.hierarchy, trie, k, this);
            solCombs.createSolutionCombs(results, prefix);
            
            for(Entry<Double, double[]> entry : results.entrySet()){
                double[] anonPath = entry.getValue();
                for(int i=0; i<anonPath.length; i++){
                    double anonItem = anonPath[i];
                    if((anonItem != -1) && (anonItem != prefix.get(i))){
                        generalize(anonItem);
                    }
                }
                break;
            }
        }
    }
    
    private void generalize(double generalized){
        int l = getLevel(generalized);
        if(l == 0)
            return;
        
        Set<Double> children = this.hierarchy.getChildrenIds(generalized);
        if(children != null){
            if(l == 1){
                for(Double child : children){
                    pointMap[child.intValue()][1] = (int)generalized;
                    pointMap[child.intValue()][2] = getLevel(generalized);
                }
            }else{
                for(Double child : children){
                    pointMap[child.intValue()][1] = (int)generalized;
                    pointMap[child.intValue()][2] = getLevel(generalized);
                    gen(child, generalized);
                }
            }
        }
    }
    
    private void gen(double o, double g){
        if(getLevel(o) == 0){
            pointMap[(int)o][1] = (int)g;
            pointMap[(int)o][2] = getLevel(g);
        }
        else{
            Set<Double> children = this.hierarchy.getChildrenIds(o);
            if(children != null){
                for(Double child : children){
                    pointMap[child.intValue()][1] = (int)g;
                    pointMap[child.intValue()][2] = getLevel(g);
                    gen(child, g);
                }
            }
        }
    }
    
    private Set<Double> expandTransaction(double[] transaction){
        Set<Double> expanded = new TreeSet<>(Collections.reverseOrder());
        for(double item : transaction){
            expanded.add(item);
            getAllGeneralizations(item, expanded);
        }
        
        return expanded;
    }
    
    private Set<Double> expandTransaction(List<Double> transaction){
        Set<Double> expanded = new TreeSet<>(Collections.reverseOrder());
        for(double item : transaction){
            expanded.add(item);
            getAllGeneralizations(item, expanded);
        }
        
        return expanded;
    }
    
    private void getAllGeneralizations(double current, Set<Double> result) {
        double temp = hierarchy.getParentId((int)current);
        while(temp != -1){
            result.add(temp);
            temp = hierarchy.getParentId((int)temp);
        }
    }
    
    private void createInternalStructures() {
        List<Integer> nodeIds = this.hierarchy.getNodeIdsInLevel(0);
        domainSize = nodeIds.size();
        
        //allocate and init testgens
        this.testGens = new int[domainSize];
        for (int i=0;i<domainSize;i++) {
            testGens[i] = -1;
        }
        
        //get root id
        String strValue = (String)this.hierarchy.getRoot();
        int maxId = this.hierarchy.getDictionary().getStringToId(strValue);
        
        //allocate and init pointMap
        pointMap = new int[maxId + 1][];
        costs = new double[maxId + 1];
        
        for(int height=0; height<this.hierarchy.getHeight(); height++){
            List<Integer> nodeIdsInLevel = this.hierarchy.getNodeIdsInLevel(height);
            for(Integer nodeId : nodeIdsInLevel){
                pointMap[nodeId] = new int[3];
                pointMap[nodeId][0] = nodeId;       //original value
                pointMap[nodeId][1] = nodeId;       //generalized value
                pointMap[nodeId][2] = height;       //level
                
                //calculate costs
                costs[nodeId] = 0;
                if(height > 0){
                    Set<Double> children = this.hierarchy.getChildrenIds(nodeId);
                    if (height == 1){
                        costs[nodeId] = (double)children.size() / (double)domainSize;
                    }
                    else{
                        for(Double child : children){
                            
                            costs[nodeId] = costs[nodeId]+costs[child.intValue()];
                        }
                    }
                }
            }
        }
    }
    
    public double getAddedCost(double[] comb, List<Double> base){
        double existingCost = 0;
        double newCost = 0;
        
        resetTestGens();
        
        for(int i = 0; i<comb.length; i++){
            double anonItem = comb[i];
            if(anonItem != -1 && (anonItem != base.get(i))){
                generalizeTest(anonItem);
            }
        }
        
        existingCost = getTotalCost();
        newCost = getTestCost();
        
        return (newCost - existingCost);
    }
    
    private void resetTestGens(){
        for(int i=0; i<domainSize; i++){
            testGens[i] = pointMap[i][1];
        }
    }
    
    private void generalizeTest(double generalized) {
        int l = getLevel(generalized);
        
        if(l == 0)
            return;
        
        Set<Double> children = this.hierarchy.getChildrenIds(generalized);
        if(children != null){
            if (l == 1){
                for(Double child : children){
                    testGens[child.intValue()] = (int)generalized;
                }
            }
            else{
                for(Double child : children){
                    genTest(child, generalized);
                }
            }
        }
        
    }
    
    private void genTest(double o, double g){
        if(getLevel(o) == 0){
            testGens[(int)o] = (int)g;
        }
        else{
            Set<Double> children = this.hierarchy.getChildrenIds(o);
            if(children != null){
                for(Double child : children){
                    genTest(child, g);
                }
            }
        }
    }
    
    private int getLevel(double point){
        return this.hierarchy.getLevel(point);
    }
    
    private double getTotalCost() {
        double res = 0;
        
        for(int i=0; i<domainSize; i++){
            res += (costs[pointMap[i][1]]*this.hierarchy.getWeight(i));
        }
        
        return res;
    }
    
    private double getTestCost() {
        double res = 0;
        
        for(int i=0; i<domainSize; i++){
            if (testGens[i] == 49 ||
                testGens[i] == 45 ||
                testGens[i] == 46 ||
                testGens[i] == 47 || 
                testGens[i] == 48){
            }
            res += (costs[testGens[i]]*this.hierarchy.getWeight(i));
        }
        
        return res;
    }
    
    private String print(List<Double> list){
        String str = "";
        for(Double d : list){
            str += this.hierarchy.getDictionary().getIdToString(d.intValue()) + " ,";
        }
        return "[" + str + " ]";
    }
    
    private String print(double[] list){
        String str = "";
        for(double d : list){
            str += this.hierarchy.getDictionary().getIdToString((int)d) + " ,";
        }
        return "[" + str + " ]";
    }
    
    private String print(double d){
        return this.hierarchy.getDictionary().getIdToString((int)d);
    }
    
}
