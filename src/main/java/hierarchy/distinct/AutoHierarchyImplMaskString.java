/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.distinct;

import controller.AppCon;
import data.Data;
import data.RelSetData;
import data.SETData;
import exceptions.LimitException;
import static hierarchy.Hierarchy.online_limit;
import static hierarchy.Hierarchy.online_version;
import hierarchy.NodeStats;
import hierarchy.distinct.HierarchyImplString;
import static hierarchy.distinct.HierarchyImplString.dict;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author nikoc
 */
public class AutoHierarchyImplMaskString extends HierarchyImplString {
    
    Data dataset = null;
    String attribute = null;
    int length;
    
    public AutoHierarchyImplMaskString(String _name, String _nodesType, String _hierarchyType, String _attribute, Data _data, int _length) {
        super(_name, _nodesType,_data.getDictionary());
        attribute = _attribute;
        dataset = _data;
        this.length = _length;
    }
    
    
    @Override
    public void autogenerate() throws LimitException {
        
        int curHeight;
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
        Set<String> strSet = new HashSet();

        //get distinct values from dataset
        if(dataset instanceof SETData){
            for (double[] rowData : data){
                for(double d : rowData){
                    itemsSet.add(d);
                    strSet.add(this.dictData.getIdToString((int) d));
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
                        strSet.add(this.dictData.getIdToString((int) d));
                    }
                }
            }
            else{
                for (double[] rowData : data){
                   itemsSet.add(rowData[column]);
                   strSet.add(this.dictData.getIdToString((int) rowData[column]));
                } 
            }
        }
        else{
            for (double[] rowData : data){
                itemsSet.add(rowData[column]);
                strSet.add(this.dictData.getIdToString((int) rowData[column]));
            }
        }
        
        
        //build leaf level 
        ArrayList<Double> initList = new ArrayList<>(itemsSet);
        
        this.height = this.length+1;
        curHeight = height - 1;
        
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
        
        allParents.put(curHeight, filterIdsByLength(initList,curHeight));
        ArrayList <String> strVal = new ArrayList(strSet);
//        System.out.println("whole words "+strVal);
//        System.out.println("Dict val "+initList);
//        System.out.println("first str value dict "+strVal.get(1)+" "+this.dictData.getStringToId(strVal.get(1)));
//        String maskword = strVal.get(0).substring(0,curHeight-1);
//        System.out.println("mask "+maskword);
        while(curHeight > 0){
            ArrayList<Double> masksIds = new ArrayList();
            
            
            if(curHeight-1==0){
                String strRoot="";
                strRoot = strRoot + StringUtils.repeat("*", this.height-curHeight);
                Double rootId;
                if(dict.containsString(strRoot)){
                    rootId = (double) dict.getStringToId(strRoot);
                }
                else{
                    dict.putIdToString(strCount, strRoot);
                    dict.putStringToId(strRoot, strCount++);
                    rootId = (double) strCount - 1;
                }
                
                
                List<Double> idsLev = new ArrayList<Double>();

                for(String w : strVal){
                    Double id;
                    System.out.println("Str "+w);
                    if(dictData.containsString(w)){
                        id = dictData.getStringToId().get(w).doubleValue();
                    }
                    else{
                        id = dict.getStringToId().get(w).doubleValue();
                    }

                    idsLev.add(id);
                    parents.put(id, rootId);
                    stats.put(id, new NodeStats(curHeight));
                }

                children.put(rootId, idsLev);
                masksIds.add(rootId);
                
            }
            else{
                List<String> levelwords;
                levelwords = this.filterbyLength(strVal, curHeight-1);
//                String tempMask;
                while(!levelwords.isEmpty()){
                    final String maskword = levelwords.get(0).substring(0,curHeight-1);
                    String maskwordAppear = maskword + StringUtils.repeat("*", this.height-curHeight);
                    Double maskId;
                    if(dict.containsString(maskwordAppear)){
                        maskId = (double) dict.getStringToId(maskwordAppear);
                    }
                    else{
                        dict.putIdToString(strCount, maskwordAppear);
                        dict.putStringToId(maskwordAppear, strCount++);
                        maskId = (double) strCount - 1;
                    }


                    List<String> filteredWords = levelwords.stream()
                                                // Filter by any condition
                                                .filter(word -> word.startsWith(maskword))
                                                // Collect your filtered fields
                                                .collect(Collectors.toList());
                    List<Double> idsLev = new ArrayList<Double>();

                    for(String w : filteredWords){
                        Double id;
                        if(dictData.containsString(w)){
                            id = dictData.getStringToId().get(w).doubleValue();
                        }
                        else{
                            id = dict.getStringToId().get(w).doubleValue();
                        }

                        idsLev.add(id);
                        parents.put(id, maskId);
                        stats.put(id, new NodeStats(curHeight));
                    }

                    children.put(maskId, idsLev);
                    levelwords.removeAll(filteredWords);
                    masksIds.add(maskId);
                    strVal.add(maskwordAppear);

                }
            }
            
            curHeight--;
            allParents.put(curHeight, masksIds);
            
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
    
    
    private List<String> filterbyLength(List<String> words, int len){
        Iterator<String> stringIterator = words.iterator();
        List<String> result = new ArrayList();
        while (stringIterator.hasNext()) {
            String string = stringIterator.next();
            if (string.length() > len) {
                result.add(string);
                stringIterator.remove();
            }
        }
        
        return result;
    }
    
    private ArrayList<Double> filterIdsByLength(List<Double> wordsIds, int len){
        Iterator<Double> idIterator = wordsIds.iterator();
        ArrayList<Double> result = new ArrayList();
        while (idIterator.hasNext()) {
            Double id = idIterator.next();
            if (dictData.getIdToString().get(id.intValue()).length() >= len) {
                result.add(id);
                idIterator.remove();
            }
        }
        
        return result;
    }
    
}
