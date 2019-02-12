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
        super(_name, _nodesType);
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
        HashMap<String,Set> mapLevel1 = null;
        HashMap<String,Set> mapLevel2 = null;
        Set tempSet = null;
        String tempSplit[] = null;
        String del = "/";
        List<String> childrenList = null;
        
        int column = dataset.getColumnByName(attribute);
        double[][] data = dataset.getDataSet();
        DictionaryString dict = dataset.getDictionary(column);

        Set<String> itemsSet = new HashSet<>();

        //get distinct values from dataset
        for (double[] rowData : data){
            itemsSet.add(dict.getIdToString((int)rowData[column]));
        }
        
             
        height = 4;
        

        //build leaf level 
        ArrayList<String> initList = new ArrayList<>(itemsSet);

        Collections.sort(initList);

        mapLevel1 = new HashMap<String,Set>();

        //allParents.put(curHeight, initList);
 
        ArrayList<String> parentsLevel2 = new ArrayList<String>();
        ArrayList<String> parentsLevel1 = new ArrayList<String>();
        ArrayList<String> parentsLevel = new ArrayList<String>();
        ArrayList<String> parentsLevel3 = new ArrayList<String>();
         
        for(int i =  0 ; i < initList.size() ; i ++ ){
            
            String postCode = initList.get(i).toString();
            tempSplit = postCode.split(del);
            String subStr = tempSplit[1] + "/" + tempSplit[2];
            
            if (!mapLevel1.containsKey(subStr)){
                tempSet = new HashSet();
                tempSet.add(postCode);
                mapLevel1.put(subStr,tempSet);
                parentsLevel3.add(postCode);
                parents.put(postCode,subStr);
                stats.put(postCode, new NodeStats(3));
                stats.put(subStr, new NodeStats(2));
                System.out.println("level3 = " + postCode);
            }
            else{
                parentsLevel3.add(postCode);
                stats.put(postCode, new NodeStats(3));
                parents.put(postCode,subStr);
                tempSet = mapLevel1.get(subStr);
                tempSet.add(postCode);
            }
            
            tempSet = null;
        }
        
        
        System.out.println("edwwww");
        for (Map.Entry<String,Set> entry : mapLevel1.entrySet()) {
            System.out.println("key = " + entry.getKey() + "value= " + entry.getValue());
        }
        System.out.println("endddddddddddddddddd");
        
        
        System.out.println("first");
        for (Map.Entry<String,Set> entry : mapLevel1.entrySet()) {
            System.out.println("xa= " + entry.getValue());
            childrenList = new ArrayList<String>();
            childrenList.addAll(entry.getValue());
            //System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
            children.put(entry.getKey(), childrenList);
            childrenList = null;
        }
        System.out.println("end");
        
        
        childrenList = null;
        childrenList = new ArrayList<String>();
        mapLevel2 = new HashMap<String,Set>();
        for (Map.Entry<String, Set> entry : mapLevel1.entrySet()) {
            String postCode = entry.getKey(); 
            tempSplit = postCode.split(del);
            String subStr = tempSplit[1];
            
            if (!mapLevel2.containsKey(subStr)){
                tempSet = new HashSet();
                tempSet.add(postCode);
                mapLevel2.put(subStr,tempSet);
                   
                parentsLevel2.add(postCode);
                parentsLevel1.add(subStr);
                parents.put(postCode,subStr);
                parents.put(subStr, "*");
                
                stats.put(subStr, new NodeStats(1));
                
                System.out.println("level 2 = " + postCode);
                System.out.println("level 1 = " + subStr);
            }
            else{
                parentsLevel2.add(postCode);
                parents.put(postCode,subStr);
                tempSet = mapLevel2.get(subStr);
                tempSet.add(postCode);
            }
            
            tempSet = null;
            
        }
        
        
       
        ArrayList <String> childListLevel1 =new ArrayList<String>();
        
        for (Map.Entry<String,Set> entry : mapLevel2.entrySet()) {
            childrenList = new ArrayList<String>();
            childrenList.addAll(entry.getValue());
            //System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
            children.put(entry.getKey(), childrenList);
            childrenList = null;
            childListLevel1.add(entry.getKey());
        }
        
        children.put("*", childListLevel1);
        
        parentsLevel.add("*");
        
        allParents.put(0, parentsLevel);
        allParents.put(1, parentsLevel1);
        allParents.put(2, parentsLevel2);
        allParents.put(3, parentsLevel3);
        
        root = allParents.get(0).get(0);
        stats.put(root, new NodeStats(0));
        
        System.out.println("allParents");
        for (Map.Entry<Integer, ArrayList<String>> entry : allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
        }
        
        System.out.println("parents");
        for (Map.Entry<String, String> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
        }
        
        System.out.println("children");
        for (Map.Entry<String, List<String>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
        }
    }
    
    
}
