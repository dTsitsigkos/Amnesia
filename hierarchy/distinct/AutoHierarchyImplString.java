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

import data.Data;
import data.SETData;
import dictionary.DictionaryString;
import hierarchy.NodeStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 *
 * @author serafeim
 */
public class AutoHierarchyImplString extends HierarchyImplString {
    //variables for autogenerating 
    String attribute = null;
    String sorting = null;
    int fanout = 0;
    //boolean exact = false;  
    Data dataset = null;
    int randomNumber = 0;
    
    //generator for random numbers
    Random gen = new Random();
    
    public AutoHierarchyImplString(String _name, String _nodesType, String _hierarchyType, String _attribute, 
                                    String _sorting, int _fanout, Data _data) {
        super(_name, _nodesType);
        attribute = _attribute;
        sorting = _sorting;
        fanout = _fanout;
        dataset = _data;
    }
    
    @Override
    public void autogenerate() {
        int column = dataset.getColumnByName(attribute);
        double[][] data = dataset.getDataSet();
        DictionaryString dict = dataset.getDictionary(column);

        Set<String> itemsSet = new HashSet<>();

        //get distinct values from dataset
        if(dataset instanceof SETData){
            for (double[] rowData : data){
                for(double d : rowData){
                    itemsSet.add(dict.getIdToString((int)d));
                }
            }
        }
        else{
            for (double[] rowData : data){
                itemsSet.add(dict.getIdToString((int)rowData[column]));
            }
        }
             
        height = computeHeight(fanout, itemsSet.size());
        int curHeight = height - 1;
        System.out.println("size: " + itemsSet.size() + " fanout: " + fanout + " height: " + height);

        //build leaf level 
        ArrayList<String> initList = new ArrayList<>(itemsSet);

        System.out.println("sorting = " + sorting);
        
        if(sorting.equals("random")){
            Collections.shuffle(initList);
        } 
        else if(sorting.equals("alphabetical")){
            System.out.println("alphabeticalllll");
            Collections.sort(initList);
        }

        allParents.put(curHeight, initList);
 
        //build inner nodes of hierarchy
        while(curHeight > 0){
            String[] prevLevel = allParents.get(curHeight).toArray(new String[allParents.get(curHeight).size()]);
            int prevLevelIndex = 0;
            
            int curLevelSize = (int)(prevLevel.length / fanout + 1);
            if(fanout > 0){
                curLevelSize = prevLevel.length;
            }
            
            String[] curLevel = new String[curLevelSize];
            int curLevelIndex = 0;
            
            while(prevLevelIndex < prevLevel.length){
                
                String ran = randomNumber();
                
                String[] tempArray = new String[fanout];
                
                //assign a parent every #curFanout children
                int j;
                for(j=0; j<fanout && (prevLevelIndex < prevLevel.length); j++){
                    String ch = prevLevel[prevLevelIndex];
                    prevLevelIndex++;
                    tempArray[j] = ch;
                    parents.put(ch, ran);
                    stats.put(ch, new NodeStats(curHeight));
                }
                
                //array size is not curFanout (elements finished), resize 
                if(j != fanout){
                    tempArray = Arrays.copyOf(tempArray, j);
                }

                children.put(ran, new ArrayList<>(Arrays.asList(tempArray)));
                curLevel[curLevelIndex] = ran;
                curLevelIndex++;
            }

            curHeight--;

            //resize if there are less items in level than initial level max prediction
            if(curLevelIndex != curLevelSize){
                curLevel = Arrays.copyOf(curLevel, curLevelIndex);
            }

            allParents.put(curHeight, new ArrayList<>(Arrays.asList(curLevel)));
        }

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
        
        
        
        
    }
    
    
    private int computeHeight(int fanout, int nodes){// fanout > 1
        int answer =  (int)(Math.log((double)nodes) / Math.log((double)fanout) + 1);
        if((Math.log((double)nodes) % Math.log((double)fanout)) != 0){
            answer++;
        }
        return answer;
    }
    
    private String randomNumber(){
        return "Random" + randomNumber++;
    }
    
}
