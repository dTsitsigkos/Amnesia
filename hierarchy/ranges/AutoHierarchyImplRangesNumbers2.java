/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.ranges;

import hierarchy.NodeStats;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jimakos
 */
public class AutoHierarchyImplRangesNumbers2 extends HierarchyImplRangesNumbers{
     //variables for autogenerating 
    Double start = null;
    Double end = null;
    Double step = null;
    int fanout = 0;  

    
    public AutoHierarchyImplRangesNumbers2(String _name, String _nodesType, String _hierarchyType, Double _start, Double _end, Double _step, int _fanout) {
        super(_name, _nodesType);
        start = _start;
        end = _end;
        step = _step;
        fanout = _fanout;
        nodesType = _nodesType;
        this.height = 0;
        
    }

    /**
     * Automatically generates hierarchy's structures
     */
    @Override
    public void autogenerate() {
        Map <Integer, ArrayList<RangeDouble>> numbersMap = new HashMap<Integer,ArrayList<RangeDouble>>();
        
        ArrayList<RangeDouble> initList = new ArrayList<>();
        double currentNumber = start;
        RangeDouble rangeD = null;
        
        while (currentNumber < end){
            
            rangeD = new RangeDouble();
            rangeD.lowerBound = currentNumber;
            rangeD.nodesType = nodesType;
            
            currentNumber = currentNumber + step;
            if ( currentNumber >= end){
                if (end-rangeD.lowerBound >= 1){
                     rangeD.setUpperBound(end);
                     initList.add(rangeD);
                }

                break;
            }
            
            rangeD.setUpperBound(currentNumber);
            initList.add(rangeD);
            
        }
        System.out.println("Last level init list = " + initList.toString());
        
        numbersMap.put(0, initList);
        
        
        
        
        int numOfRanges = initList.size();
        int modOfRanges = initList.size()%fanout;
        int level = 1;
        
        //initList = new ArrayList<>();
        ArrayList<RangeDouble> initListTemp = new ArrayList<>();
        rangeD = null;

        while ( initList.size() > fanout  || initList.size() > 1){
            numOfRanges = initList.size();
            modOfRanges = numOfRanges%fanout;
            int i = 0;
            while( i <  initList.size()){
                //initListTemp.add(initList.get(i));
                rangeD = new RangeDouble();
                rangeD.setLowerBound(initList.get(i).lowerBound);
                rangeD.nodesType = nodesType;
                //System.out.println("i = " + i );
                i = i + fanout -1;

                //System.out.println("modOfRanges = " + modOfRanges);
                
                if ( i + modOfRanges >= initList.size()){
                    //if (modOfRanges == 1 || modOfRanges == 0){/////////edw exei ginei malakia 2,5

                    if( modOfRanges == 1){
                        rangeD.setLowerBound(initListTemp.get(initListTemp.size()-1).lowerBound);
                        initListTemp.remove(initListTemp.size()-1);
                        //initListTemp.remove(initListTemp.size()-1);
                    }
                    //else{
                    rangeD.setUpperBound(initList.get(initList.size()-1).upperBound);
                    initListTemp.add(rangeD);
                        //initList.add(initList.get(initList.size()-1));
                    //}
                   
                    break;
                }

                
                rangeD.setUpperBound(initList.get(i).upperBound);
                initListTemp.add(rangeD);
                
                i++;

            }


            /*System.out.println("level = " + level);
            for (int j = 0 ; j < yearsArrTemp.size() ; j ++){
                System.out.println(yearsArrTemp.get(j) + " + " +  yearsArrTemp.get(++j));

            }*/
            
            //System.out.println("level = " + level + ", initList = " + initListTemp.toString());
            
            numbersMap.put(level, initListTemp);
            
            ArrayList<RangeDouble> prevLevelYear = numbersMap.get(level - 1);
            
            int p = 0;
            for ( int k = 0 ; k < initListTemp.size() ; k = k + 1 ){
                //RangeDouble d = new RangeDouble(initListTemp.get(k-1), initListTemp.get(k));
                ArrayList<RangeDouble> childsTemp = new ArrayList<RangeDouble>(); 


                while( p < prevLevelYear.size() &&  ((prevLevelYear.get(p).upperBound < initListTemp.get(k).upperBound ) || (prevLevelYear.get(p).upperBound == initListTemp.get(k).upperBound ))){
                    childsTemp.add(prevLevelYear.get(p));
                    parents.put(prevLevelYear.get(p), initListTemp.get(k));
                    p = p + 1;        
                }

                children.put(initListTemp.get(k), childsTemp);
                childsTemp = new ArrayList<RangeDouble>();
            }
            
            
            level++;
            
            
            
            initList = new ArrayList<RangeDouble>();
            initList.addAll(initListTemp);
            initListTemp = new ArrayList<RangeDouble>(); 
        }
        
        
        for (Map.Entry<Integer, ArrayList<RangeDouble>> entry : numbersMap.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        
        height = numbersMap.size();
        
        root = new RangeDouble(start,end);
        root.nodesType = nodesType;
        ArrayList<RangeDouble> allP = new ArrayList<RangeDouble>();
        allP.add(root);
        allParents.put(0, allP);
        stats.put(root, new NodeStats(0));
        
        //System.out.println("rootttttttttt = " + root);
        //System.out.println("all parents =  " + allParents.get(0));
        

        allP = new ArrayList<RangeDouble>();
        rangeD = null;
        
        
        ArrayList<RangeDouble> tempRange;

        int counter = 1;
        for ( int i = numbersMap.size() -2 ; i >= 0 ; i -- ){
            //System.out.println("i = " + i);
            tempRange = numbersMap.get(i);
            for ( int j = 0 ; j < tempRange.size() ; j = j + 1 ){
                rangeD = new RangeDouble(tempRange.get(j).lowerBound, tempRange.get(j).upperBound);
                rangeD.nodesType = nodesType;
                allP.add(rangeD);
                stats.put(tempRange.get(j), new NodeStats(counter));
            }
            //System.out.println("allP  = " + allP.toString() );

            allParents.put(counter, allP);
            allP = new ArrayList<RangeDouble>();
            counter++;
        }
       
        
           /////null values ////////////////////////////
        //allParents
        RangeDouble ranNull = new RangeDouble(Double.NaN,Double.NaN);
        ranNull.setNodesType(nodesType);
        allP = null;
        allP = allParents.get(1);
        allP.add(ranNull);
        //allParents.put(1, allP);
        //parents
        ranNull = new RangeDouble(Double.NaN,Double.NaN);
        ranNull.setNodesType(nodesType);
        parents.put(root, ranNull);
        //children
        List<RangeDouble> childsTemp = children.get(root);
        ranNull = new RangeDouble(Double.NaN,Double.NaN);
        ranNull.setNodesType(nodesType);
        childsTemp.add(ranNull);
        //children.put(root, childsTemp);
        
        ranNull = new RangeDouble(Double.NaN,Double.NaN);
        ranNull.setNodesType(nodesType);
        children.put(ranNull, null);
        //stats
        ranNull = new RangeDouble(Double.NaN,Double.NaN);
        ranNull.setNodesType(nodesType);
        stats.put(ranNull,new NodeStats(1));
        
        ///////////////////////////////////////////// 
        
        
        /*System.out.println("all Parents");
        for (Map.Entry<Integer,ArrayList<RangeDouble>> entry : allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("Stats");
        for (Map.Entry<RangeDouble, NodeStats> entry : stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("parents");
        for (Map.Entry<RangeDouble, RangeDouble> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("children");
        for (Map.Entry<RangeDouble, List<RangeDouble>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");*/
        
    }

}
