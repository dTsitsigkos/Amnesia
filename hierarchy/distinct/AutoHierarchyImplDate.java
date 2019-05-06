/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.distinct;

import data.Data;
import data.SETData;
import dictionary.DictionaryString;
import hierarchy.NodeStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jimakos
 */
public class AutoHierarchyImplDate extends HierarchyImplString {
    String attribute = null;
    Data dataset = null;
    String hierarchyType = null;
    
    
    public AutoHierarchyImplDate(String _name, String _nodesType, String _hierarchyType, String _attribute, Data _data) {
        super(_name, _nodesType,_data.getDictionary());
        attribute = _attribute;
        dataset = _data;
        this.hierarchyType = _hierarchyType;
    }
    
    //height
    //allParents
    //parents
    //stats
    //children
    //root
    
    
    @Override
    public void autogenerate() {
        HashMap<Double,Set> mapLevel1 = null;
        HashMap<Double,Set> mapLevel2 = null;
        Set tempSet = null;
        String tempSplit[] = null;
        String del = "/";
        List<Double> childrenList = null;
        int strCount=dictData.getMaxUsedId()+1;
        
        int column = dataset.getColumnByName(attribute);
        double[][] data = dataset.getDataSet();
//        DictionaryString dict = dataset.getDictionary();

        Set<Double> itemsSet = new HashSet<>();

        //get distinct values from dataset
        for (double[] rowData : data){
            itemsSet.add(rowData[column]);
        }
        
          
        height = 4;
        

        //build leaf level 
        ArrayList<Double> initList = new ArrayList<>(itemsSet);

        Collections.sort(initList);

        mapLevel1 = new HashMap<Double,Set>();

        //allParents.put(curHeight, initList);
 
        ArrayList<Double> parentsLevel2 = new ArrayList<Double>();
        ArrayList<Double> parentsLevel1 = new ArrayList<Double>();
        ArrayList<Double> parentsLevel = new ArrayList<Double>();
        ArrayList<Double> parentsLevel3 = new ArrayList<Double>();
         
        for(int i =  0 ; i < initList.size() ; i ++ ){
            
            String postCode = dictData.getIdToString(initList.get(i).intValue());
            tempSplit = postCode.split(del);
            System.out.println(postCode);
            String subStr = tempSplit[1] + "/" + tempSplit[2];
            Double postCodeId = (double) dictData.getStringToId(postCode);
            Double subStrId;
            
            if(!dict.containsString(subStr)){
               dict.putIdToString(strCount, subStr);
               dict.putStringToId(subStr, strCount++);
            }
            
            subStrId = (double) dict.getStringToId(subStr);
            
            if (!mapLevel1.containsKey(subStrId)){
                tempSet = new HashSet();
                tempSet.add(postCodeId);
                mapLevel1.put(subStrId,tempSet);
                parentsLevel3.add(postCodeId);
                parents.put(postCodeId,subStrId);
                stats.put(postCodeId, new NodeStats(3));
                stats.put(subStrId, new NodeStats(2));
                System.out.println("level3 = " + postCode);
            }
            else{
                parentsLevel3.add(postCodeId);
                stats.put(postCodeId, new NodeStats(3));
                parents.put(postCodeId,subStrId);
                tempSet = mapLevel1.get(subStrId);
                tempSet.add(postCodeId);
            }
            
            tempSet = null;
        }
        
        
        System.out.println("edwwww");
        for (Map.Entry<Double,Set> entry : mapLevel1.entrySet()) {
            System.out.println("key = " + entry.getKey() + "value= " + entry.getValue());
        }
        System.out.println("endddddddddddddddddd");
        
        
        System.out.println("first");
        for (Map.Entry<Double,Set> entry : mapLevel1.entrySet()) {
            System.out.println("xa= " + entry.getValue());
            childrenList = new ArrayList<Double>();
            childrenList.addAll(entry.getValue());
            //System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
            children.put(entry.getKey(), childrenList);
            childrenList = null;
        }
        System.out.println("end");
        
        
        childrenList = null;
        childrenList = new ArrayList<Double>();
        mapLevel2 = new HashMap<Double,Set>();
        for (Map.Entry<Double, Set> entry : mapLevel1.entrySet()) {
            String postCode = dict.getIdToString(entry.getKey().intValue()); 
            tempSplit = postCode.split(del);
            String subStr = tempSplit[1];
            Double postCodeId = entry.getKey();
            Double subStrId;
            
            if(!dict.containsString(subStr)){
                dict.putIdToString(strCount, subStr);
                dict.putStringToId(subStr, strCount++);
            }
            
            subStrId = (double) dict.getStringToId(subStr);
            
            if (!mapLevel2.containsKey(subStrId)){
                tempSet = new HashSet();
                tempSet.add(postCodeId);
                mapLevel2.put(subStrId,tempSet);
                   
                parentsLevel2.add(postCodeId);
                parentsLevel1.add(subStrId);
                parents.put(postCodeId,subStrId);
                
                if(!dict.containsString("*")){
                    dict.putIdToString(strCount, "*");
                    dict.putStringToId("*", strCount++);
                } 
                
                parents.put(subStrId,(double)dict.getStringToId("*"));
                
                stats.put(subStrId, new NodeStats(1));
                
                System.out.println("level 2 = " + postCode);
                System.out.println("level 1 = " + subStr);
            }
            else{
                parentsLevel2.add(postCodeId);
                parents.put(postCodeId,subStrId);
                tempSet = mapLevel2.get(subStrId);
                tempSet.add(postCodeId);
            }
            
            tempSet = null;
            
        }
        
        
       
        ArrayList <Double> childListLevel1 =new ArrayList<Double>();
        
        for (Map.Entry<Double,Set> entry : mapLevel2.entrySet()) {
            childrenList = new ArrayList<Double>();
            childrenList.addAll(entry.getValue());
            //System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
            children.put(entry.getKey(), childrenList);
            childrenList = null;
            childListLevel1.add(entry.getKey());
        }
        
        if(!dict.containsString("*")){
           dict.putIdToString(strCount, "*");
           dict.putStringToId("*", strCount++);
        }
        children.put((double)dict.getStringToId("*"), childListLevel1);
        
        parentsLevel.add((double)dict.getStringToId("*"));
        
        allParents.put(0, parentsLevel);
        allParents.put(1, parentsLevel1);
        allParents.put(2, parentsLevel2);
        allParents.put(3, parentsLevel3);
        
        root = allParents.get(0).get(0);
        stats.put(root, new NodeStats(0));
        
        System.out.println("allParents");
        for (Map.Entry<Integer, ArrayList<Double>> entry : allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
        }
        
        System.out.println("parents");
        for (Map.Entry<Double, Double> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
        }
        
        System.out.println("children");
        for (Map.Entry<Double, List<Double>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
        }
    }
    
    
}
