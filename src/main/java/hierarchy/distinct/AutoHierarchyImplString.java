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
import controller.AppCon;
import data.Data;
import data.RelSetData;
import data.SETData;
import dictionary.DictionaryString;
import graph.Edge;
import hierarchy.NodeStats;
import static hierarchy.distinct.HierarchyImplString.dict;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
        super(_name, _nodesType,_data.getDictionary());
        attribute = _attribute;
        sorting = _sorting;
        fanout = _fanout;
        dataset = _data;
    }
    
    @Override
    public void autogenerate() throws LimitException {
        int column = dataset.getColumnByName(attribute);
        double[][] data = dataset.getDataSet();
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

//        DictionaryString dict = dataset.getDictionary();

        Set<Double> itemsSet = new HashSet<>();

        //get distinct values from dataset
        if(dataset instanceof SETData){
            for (double[] rowData : data){
                for(double d : rowData){
                    itemsSet.add(d);
                }
            }
        }
        else if(dataset instanceof RelSetData){
//            int i;
//            for(i=0; i<6; i++){
//                if(data[i].length != dataset.getDataColumns()){
//                    for (double[] rowData : data){
//                        for(double d : rowData){
//                            itemsSet.add(d);
//                        }
//                    }
//                    break;
//                }
//            }
//            if(i==6){
//                for (double[] rowData : data){
//                    itemsSet.add(rowData[column]);
//                }
//            }
            if(data[0][column] == -1){
                data = ((RelSetData) dataset).getSet();
                for (double[] rowData : data){
                    for(double d : rowData){
                        itemsSet.add(d);
                    }
                }
            }
            else{
                for (double[] rowData : data){
                   itemsSet.add(rowData[column]);
                }
            }
        }
        else{
            for (double[] rowData : data){
                itemsSet.add(rowData[column]);
            }
        }
             
        height = computeHeight(fanout, itemsSet.size());
        int curHeight = height - 1;
        System.out.println("size: " + itemsSet.size() + " fanout: " + fanout + " height: " + height);

        //build leaf level
        ArrayList<Double> initList = new ArrayList<>(itemsSet);
        
//        if ( initList.get(initList.size()-1) == 2147483646.0 ||  initList.get(initList.size()-1).isNaN() ){
//            randomNumber = initList.get(initList.size()-2).intValue();
//        }
//        else{
//            randomNumber = initList.get(initList.size()-1).intValue();
//        }

        System.out.println("sorting = " + sorting);
        
        if(sorting.equals("random")){
            Collections.shuffle(initList);
        }
        else if(sorting.equals("alphabetical")){
            System.out.println("alphabeticalllll");
//            Collections.sort(initList);
            Collections.sort(initList, new Comparator<Double>() {
                @Override
                public int compare(Double d1, Double d2) {
//                    return s1.getTo().compareToIgnoreCase(s2.getTo());
                    String s1 = dictData.getIdToString(d1.intValue());
                    if(s1 == null){
                        s1 = dict.getIdToString(d1.intValue());
                    }
                    String s2 = dictData.getIdToString(d2.intValue());
                    if(s2 == null){
                        s2 = dict.getIdToString(d2.intValue());
                    }
                    return s1.compareToIgnoreCase(s2);
                }
            });
        }

        allParents.put(curHeight, initList);
 
        //build inner nodes of hierarchy
        while(curHeight > 0){
            Double[] prevLevel = allParents.get(curHeight).toArray(new Double[allParents.get(curHeight).size()]);
            int prevLevelIndex = 0;
            
            int curLevelSize = (int)(prevLevel.length / fanout + 1);
            if(fanout > 0){
                curLevelSize = prevLevel.length;
            }
            
            counterNodes += curLevelSize;
            if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
            }
            
            
            Double[] curLevel = new Double[curLevelSize];
            int curLevelIndex = 0;
            
            while(prevLevelIndex < prevLevel.length){
                
                String ran = randomNumber();
                Double ranId;
                if(dict.containsString(ran)){
                    ranId = (double) dict.getStringToId(ran);
                }
                else{
                    dict.putIdToString(strCount, ran);
                    dict.putStringToId(ran, strCount++);
                    ranId = (double) strCount - 1;
                }
                
                Double[] tempArray = new Double[fanout];
                
                //assign a parent every #curFanout children
                int j;
                for(j=0; j<fanout && (prevLevelIndex < prevLevel.length); j++){
                    Double chId = prevLevel[prevLevelIndex];
                    prevLevelIndex++;
                    tempArray[j] = chId;
                    parents.put(chId, ranId);
                    stats.put(chId, new NodeStats(curHeight));
                    
                    
                    if(dictData.containsId(chId.intValue()) && curHeight==height-1){
                        dict.putIdToString(chId.intValue(), dictData.getIdToString(chId.intValue()));
                        dict.putStringToId(dictData.getIdToString(chId.intValue()), chId.intValue());
                    }
                }
                
                //array size is not curFanout (elements finished), resize
                if(j != fanout){
                    tempArray = Arrays.copyOf(tempArray, j);
                }

                children.put(ranId, new ArrayList<>(Arrays.asList(tempArray)));
                curLevel[curLevelIndex] = ranId;
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
        
        if(this.getParent(2147483646.0) == null){
            Double nan = 2147483646.0;
            allParents.get(1).add(nan);
            parents.put(nan, root);
            List<Double> childsTemp = (ArrayList<Double>) children.get(root);
            childsTemp.add(nan);
            children.put(nan,null);
            stats.put(nan,new NodeStats(1));
            dict.putIdToString(2147483646, "NaN");
            dict.putStringToId("NaN", 2147483646);
        }
        
        
//        System.out.println("allParents");
//        for (Map.Entry<Integer, ArrayList<Double>> entry : allParents.entrySet()) {
//            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
//        }
//        
//        System.out.println("parents");
//        for (Map.Entry<Double, Double> entry : parents.entrySet()) {
//            System.out.println(entry.getKey()+" : "+ entry.getValue().toString());
//        }

    
        
        
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
