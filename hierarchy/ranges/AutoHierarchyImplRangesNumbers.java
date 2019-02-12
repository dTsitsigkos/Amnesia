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

import hierarchy.NodeStats;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Class for autogenerating range hierarchies
 * @author serafeim
 */
public class AutoHierarchyImplRangesNumbers extends HierarchyImplRangesNumbers{
    
    //variables for autogenerating 
    Double start = null;
    Double end = null;
    Double step = null;
    int fanout = 0;  
    //String nodesType = null;
    
    //generator for random numbers
    Random gen = new Random();
    
    /**
     * Class constructor
     * @param _name name of the hierarchy
     * @param _nodesType type of hierarchy's nodes
     * @param _hierarchyType type of hierarchy (distinct/range)
     * @param _start start of ranges domain
     * @param _end end of ranges domain
     * @param _step length of each range
     * @param _fanout to be used
     * @param _plusMinusFanout window of fanout
     */
    public AutoHierarchyImplRangesNumbers(String _name, String _nodesType, String _hierarchyType,
                Double _start, Double _end, Double _step, int _fanout) {
        super(_name, _nodesType);
        start = _start;
        end = _end;
        step = _step;
        fanout = _fanout;
        nodesType = _nodesType;
        System.out.println("nodesTypes = " + nodesType);
        
    }

    /**
     * Automatically generates hierarchy's structures
     */
    @Override
    public void autogenerate() {
        
        System.out.println("nodesTypesssssssssssssssssss = " + nodesType);
        
        //split domain to ranges using BigDecimal for accuracy
        ArrayList<RangeDouble> initList = new ArrayList<>();
        BigDecimal bdStart = new BigDecimal(start.toString());
        BigDecimal bdEnd = new BigDecimal(start.toString());
        BigDecimal bdFixEnd = new BigDecimal(end.toString());
        BigDecimal bdStep = new BigDecimal(step.toString());
        int showNULL = 0;
        boolean notFit= false;
        boolean FLAG = false;


        //generate ranges of leaf level
        while(bdEnd.compareTo(bdFixEnd) < 0){            
            bdEnd = bdStart.add(bdStep);
            if(bdEnd.compareTo(bdFixEnd) > 0 || bdEnd.compareTo(bdFixEnd) == 0){
                bdEnd = bdFixEnd;
            }
            
            
            RangeDouble r = new RangeDouble();
            r.lowerBound = bdStart.doubleValue();
            r.upperBound = bdEnd.doubleValue();
            System.out.println("lower = " + r.lowerBound + "\tupper = " + r.upperBound +"\t find = " + (r.upperBound + step) +"\t end = " + end);
            r.nodesType = nodesType;
            
            if ( r.upperBound + step > end ){
                double diff = end - r.upperBound;
                //System.out.println("Upper = " + (r.upperBound + step) );
                //System.out.println("End = " + end);
                //System.out.println("diff = " + diff);
                //System.out.println("step = " + step);
                
                if ( diff > step/2 ){
                    //System.out.println("bigger = " );
                    r.upperBound = end;
                    initList.add(r);
                    
                }
                else{
                    
                    r = initList.get(initList.size()-1);
                    r.upperBound = end;
                }
                
                
                //System.out.println("lower = " + r.lowerBound + "\tupper = " + r.upperBound +"\t find = " + (r.upperBound + step) +"\t end = " + end);
                notFit = true;
                break;
            }
            //System.out.println("lower = " + r.lowerBound + "\tupper = " + r.upperBound +"\t find = " + (r.upperBound + step) +"\t end = " + end);

            /////////////////////////////////////
            initList.add(r);
            bdStart = bdStart.add(bdStep);
        }
        
        
        System.out.println(" init list");
        for( int i = 0 ; i < initList.size() ; i ++){
            System.out.println("Low = " + initList.get(i).lowerBound + "\t up = " + initList.get(i).upperBound ) ;
        }
      
        height = computeHeight(fanout, initList.size());
        int curHeight = height - 1;
            
        //System.out.println("heighttttttttttttttttttttttttt = " + height);
        
        if ( height == 1 ){//ean exei mono mia timi kai tha prepei na prosthesoume kai to null
            ArrayList<RangeDouble> arr = new ArrayList<RangeDouble>();
            RangeDouble ran = new RangeDouble(Double.NaN,Double.NaN);
            ran.nodesType = nodesType;
            arr.add(ran);
            allParents.put(height, arr);
            stats.put(allParents.get(1).get(0), new NodeStats(0));
            allParents.put(curHeight, initList);
            parents.put(ran, allParents.get(0).get(0));
            children.put(ran,null);
            children.put(allParents.get(0).get(0), arr);
        }
        else{
        
            if ( height == 2) {
                showNULL = 0;
            }
            else {
                showNULL = 1;
            }

            if (curHeight > 1 ){ 
                allParents.put(curHeight, initList);
            }
            else{
                RangeDouble ran = new RangeDouble(Double.NaN,Double.NaN);
                ran.nodesType = nodesType;
                initList.add(ran);
                allParents.put(curHeight, initList);
                //System.out.println("gfgfdgfd = " + initList.toString());
            }       
        }

        //build inner nodes of hierarchy
        while(curHeight > 0){
            //System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
            RangeDouble[] prevLevel = allParents.get(curHeight).toArray(new RangeDouble[allParents.get(curHeight).size()]);
            System.out.println("prelevel = " + prevLevel.length);
            for ( int i = 0; i < prevLevel.length ; i ++){
                System.out.println(prevLevel[i]);
            }
            
            int prevLevelIndex = 0;
            
            int curLevelSize = (int)(prevLevel.length / fanout + 1);
            if(fanout > 0){
                curLevelSize = prevLevel.length;
            }
            
            RangeDouble[] curLevel = null;
            
            curLevel = new RangeDouble[curLevelSize];
         
            int curLevelIndex = 0;
            
            
            while(prevLevelIndex < prevLevel.length){
                
                
                RangeDouble ran = new RangeDouble();
                ran.nodesType = nodesType;
                            
                RangeDouble firstChild = null;
                RangeDouble lastChild = null;
                //System.out.println("height = " + height + "\t curLevel = " + curLevel + "\t curLevelSize = " + curLevelSize +"\t curHeight = " +curHeight);
                
                //check if is one child then move it one level up
                if(prevLevel.length - prevLevelIndex == 1){
                    ran = prevLevel[prevLevelIndex];
                    allParents.get(curHeight).remove(ran);
                    prevLevelIndex++;
                    //System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww2222222");
                }
                else{
                    //System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww3333333333");
                    //assign a parent every #curFanout children
                    int j;
                    
                    
                    RangeDouble[] tempArray = null;
                    
                    tempArray = new RangeDouble[fanout+1];

                    for(j=0; j<fanout+1 && (prevLevelIndex < prevLevel.length); j++){//////////////////////////////////

                        RangeDouble ch = prevLevel[prevLevelIndex];
                        prevLevelIndex++;
                        tempArray[j] = ch;
                        
                        parents.put(ch, ran);
                        stats.put(ch, new NodeStats(curHeight));
                        ch.nodesType = nodesType;
                  
                        if(j == 0){
                            firstChild = ch;                            
                        }
                        else {
                           // System.out.println("lower = " + ch.lowerBound);
                            if(!ch.lowerBound.equals(Double.NaN)){//gia na valoume null values
                                lastChild = ch;
                            }
                            else{
                                //System.out.println("yessssssssssssssssssssssss");
                                ch = prevLevel[prevLevelIndex-2];//pairnw to proteleutaio range giati to telutaio einai null
                                lastChild = ch;
                            }
                        }
                    }

                    ran.lowerBound = firstChild.lowerBound;
                    ran.upperBound = lastChild.upperBound;
                    ran.nodesType = nodesType;
                    
                    //array is not curFanout (elements finished), resize 
                    if(j != fanout){
                        tempArray = Arrays.copyOf(tempArray, j);
                    }
                    
                    if ( tempArray[tempArray.length-1] == null){
                        tempArray = Arrays.copyOf(tempArray, tempArray.length-1);
                    }

                    children.put(ran, new ArrayList<>(Arrays.asList(tempArray)));
                }
                curLevel[curLevelIndex] = ran;
                curLevelIndex++;
            }

            curHeight--;

            //resize if there are less items in level than initial level max prediction
            if(curLevelIndex != curLevelSize){
                curLevel = Arrays.copyOf(curLevel, curLevelIndex);
            }

            
            ArrayList arrList = null;
            if (curHeight == showNULL && showNULL > 0){
                System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww55555555");
                arrList = new ArrayList<>(Arrays.asList(curLevel));
                RangeDouble ran = new RangeDouble(Double.NaN,Double.NaN);
                ran.nodesType = nodesType;
                arrList.add(ran);
                allParents.put(curHeight,arrList);
            }
            else{             
                System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww6666");
                allParents.put(curHeight, new ArrayList<>(Arrays.asList(curLevel)));
            }   
        }   
                           
        root = allParents.get(0).get(0);
        stats.put(root, new NodeStats(0));
    }

    /**
     * Computes height of the autogenerated hierarchy
     * @param fanout fanout to be used
     * @param nodes total nodes of leaf level
     * @return height of the autogenerated hierarchy
     */
    private int computeHeight(int fanout, int nodes){// fanout > 1
        int answer =  (int)(Math.log((double)nodes) / Math.log((double)fanout) + 1);
        if((Math.log((double)nodes) % Math.log((double)fanout)) != 0){
            answer++;
        }
        
        System.out.println("answer = " + answer);
        return answer;
    }
    
//    private void assignSiblings(List<Range> list){
//        for (Range l : list){
//            List<Range> sibs = new ArrayList<>(list);
//            sibs.remove(l);
//            siblings.put(l, sibs);
//        }
//    }
    
}
