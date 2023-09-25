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
package algorithms.flash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import data.Data;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.distinct.HierarchyImplString;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.util.StringUtils;

/**
 * A frequency set
 * @author serafeim
 */
public class Buffer { 
    Data data = null;
    Map<Integer, Hierarchy> hierarchies = null;    
    Map<GeneralizedRow, Integer> frequencies = new HashMap<>();
    
    public Buffer(Data _data,  Map<Integer, Hierarchy> _hierarchies){
        hierarchies = _hierarchies;
        data = _data;
    }
    
    /**
     * computes the frequency set for the specified root graph node
     * @param node the node 
     * @param qidColumns 
     */
    public void compute(GridNode node, int[] qidColumns) throws ParseException{
        double[][] dataset = data.getDataSet();
        
//        System.out.println("node "+node.toString()+" nodeid "+node.id);
        for(int i=0; i<dataset.length; i++){       
            GeneralizedRow generalizedRow = project(node, qidColumns, dataset[i]);
            Integer count;
            if((count = frequencies.get(generalizedRow)) != null){
//                System.out.println("gRow"+generalizedRow.toString()+"Count1="+(count+1));
                frequencies.put(generalizedRow, ++count);
            }
            else {
//                System.out.println("gRow"+generalizedRow.toString()+"Count2="+count);
                frequencies.put(generalizedRow, 1);
            }
        }
    } 
    
    /**
     * projects QI columns of row according to node's elements
     * @param node a generalization graph node
     * @param row the row of the dataset to be generalized
     * @return the generalized QI columns of the row
     */
    private GeneralizedRow project(GridNode node, int[] qidColumns, double[] row) throws ParseException{
        GeneralizedRow gRow = new GeneralizedRow(node.getTransformation().length);
//        System.out.println("level "+node.getLevel());
        int j = 0;
        DictionaryString dict = data.getDictionary();
//        System.out.println("Transformation Array "+Arrays.toString(node.getTransformation()));
        for(int k=0; k<node.getTransformation().length; k++){
            Hierarchy h = hierarchies.get(qidColumns[k]);
//            System.out.println("k = "+k+" "+Arrays.toString(node.getTransformation()));
            //get the value of the specified attribute
//            h.setLevel(node.getLevel());
//            System.out.println("Level "+node.getLevel());
            Object rowValue = null; 
            if(data.getColNamesType().get(qidColumns[k]).equals("string") ){
//                DictionaryString dict = data.getDictionary(qidColumns[k]);
                //rowValue = dict.getIdToString((int)row[qidColumns[k]]);
                rowValue = row[qidColumns[k]];
            }
            else if( data.getColNamesType().get(qidColumns[k]).equals("date")){
                rowValue = dict.getIdToString((int)row[qidColumns[k]]);
                if(rowValue == null){
                    rowValue = HierarchyImplString.getWholeDictionary().getIdToString((int)row[qidColumns[k]]);
                }
            }
            else {
                rowValue = row[qidColumns[k]];
            }
            
            //System.out.println("rowvalueeeeeeee = " + rowValue);
            
            //generalize value
            for(int i=0; i<node.getTransformation()[k]; i++){
                h.setLevel(i);
                if(h.getHierarchyType().equals("range")){
//                    h.setLevel(i+1);
                    System.out.print("project");
                    if(h.getNodesType().equals("double") ||  h.getNodesType().equals("int")){
                        if ( i ==0 ){
                            if (rowValue!=null && ((double) rowValue == 2147483646.0 ||  rowValue.equals(Double.NaN))){
                                Map<Integer, ArrayList<RangeDouble>> x = h.getAllParents();
                                ArrayList<RangeDouble> newList = x.get(x.size()-1);
                                if(newList.size() != 1){
                                    rowValue = newList.get(0);
                                }
                                else{
                                    rowValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
                                }
                            }
                            else{
                                
                                rowValue = h.getParent((Double)rowValue);
                            }    
                        }
                        else{
                            
                            rowValue = h.getParent(rowValue);
                        }
                    }
                    else if (rowValue!=null && h.getNodesType().equals("date") ){
                        Date d = null;
                        RangeDate rd = null;
                        
//                        System.out.println("rowValueeeeeeeee = " + rowValue);
                        if ( !rowValue.toString().contains("-")){
                            if (rowValue.toString().equals("NaN")){
                                System.out.println("Mpainei ");
                                d = null;
                            }
                            else{
                                d = getDateFromString(rowValue.toString());
                            }
                        }
                        else{
                            rd = (RangeDate) rowValue;
                        }
                        if (d != null){
                            if ( i ==0 ){
                                if ( d == null ){
                                    rowValue = h.getParent(new RangeDate(null,null));
                                    System.out.println("Row value null "+rowValue);
                                }
                                else{
                                    rowValue = h.getParent(d);
                                }    
                            }
                            else{
                                
                                rowValue = h.getParent(rowValue);
                            }
                        }
                        else{
                            if ( i ==0 ){
                                if ( rd == null){
                                    Map<Integer, ArrayList<RangeDate>> x = h.getAllParents();
                                    rowValue = h.getParent(new RangeDate(null,null));
                                }
                                
                                else{
                                    rowValue = h.getParent(rd);
                                }    
                            }
                            else{
                                rowValue = h.getParent(rowValue);
                            }
                        }
                        //////////////////////////////////////////////////
                        //System.out.println("row value = " + rowValue);
                        
                    }
                }
                else{
//                    System.out.println("i= "+i);
//                    System.out.println("rowValue original "+rowValue);
//                    System.out.println("rowValue = " + dict.getIdToString(((Double)rowValue).intValue()));
                    rowValue = h.getParent(rowValue);
                }
            }

//            System.out.println("rowvalue = " + h.getDictionary().getIdToString(((Double)rowValue).intValue()));
//            System.out.println("rowValue  "+rowValue);
            if(rowValue != null){
                gRow.generalizedColumns[j] = rowValue.toString();
            }
            else{
                gRow.generalizedColumns[j] = null;
            }
            j++;
        }
        
        return gRow;
    }
    
    public Date getDateFromString(String tmstmp) throws ParseException{
        Date d = null;
        SimpleDateFormat sf = null;
        
        sf = new SimpleDateFormat("dd/MM/yyyy");
        d = sf.parse(tmstmp);
                   
        return d;
    }
    
    /**
     * Determines if the frequency set is k-Anonymous with respect to k
     * @param k the parameter k of k-Anonymity
     * @return true if the frequency set is k-Anonymous, false otherwise
     */
    public boolean isKAnonymous(int k){
        boolean isAnonymous = true;
       
        for(GeneralizedRow distinctRow : frequencies.keySet()){
            Integer count = frequencies.get(distinctRow);
//            System.out.println("count="+count+" gRow="+distinctRow.toString());
            if(count < k){
                isAnonymous = false;
                break;
            }
        }
        return isAnonymous;
    }
    
    /**
     * Computes frequency set from the parent's frequency set (for non-root nodes)
     * @param node a generalization graph node
     * @param parentNode
     * @param parentNodeBuffer
     * @param qidColumns
     */
    public void compute(GridNode node, GridNode parentNode, Buffer parentNodeBuffer, int[] qidColumns) throws ParseException {
        
        int[] nodeTransf = node.getTransformation();
        int[] parentNodeTransf = parentNode.getTransformation();
//        System.out.println("node tranf "+Arrays.toString(nodeTransf)+" parent transf "+Arrays.toString(parentNodeTransf));
        for(GeneralizedRow pRow : parentNodeBuffer.getFrequencies().keySet()){  
            GeneralizedRow gRow = new GeneralizedRow(nodeTransf.length);
            
            for(int i=0; i<pRow.generalizedColumns.length; i++){
                
                if(nodeTransf[i] == parentNodeTransf[i]){
                    gRow.generalizedColumns[i] = pRow.generalizedColumns[i];
                }
                //if this is the element to be further generalized
                else{
                    int k = nodeTransf[i] - parentNodeTransf[i];
                    Hierarchy h = hierarchies.get(qidColumns[i]);
                    Object value = pRow.generalizedColumns[i];
                    Object parent = null;
//                    h.setLevel(nodeTransf[i]);
//                    System.out.println("node other "+k);
                    if(h.getHierarchyType().equals("range")){
                        System.out.print("compute ");
                        if(h.getNodesType().equals("double") ||  h.getNodesType().equals("int")){
                            if(parentNodeTransf[i] == 0 && nodeTransf[i] > 0){
                                Double doubleValue = Double.parseDouble(value.toString());
                                int parentLevel = 0;
                                h.setLevel(parentLevel);
                                parent = h.getParent(doubleValue);
                                parentLevel++;
                                for(int j=0; j<k-1; j++){
                                    h.setLevel(parentLevel);
                                    parent = h.getParent(parent);
                                    parentLevel++;
                                }
                            }
                            else{
                                RangeDouble rangeValue = RangeDouble.parseRange(value.toString());
                                parent = rangeValue;
                                int parentLevel = parentNodeTransf[i];
//                                System.out.println("parentLevel "+parentNodeTransf[i]);
                                for(int j=0; j<k; j++){
                                    h.setLevel(parentLevel);
                                    parent = h.getParent(parent);
                                    parentLevel++;
                                }
                            }
                        }
                        else if (h.getNodesType().equals("date") ){
                            System.out.println("Value "+value);
                            if(parentNodeTransf[i] == 0 && nodeTransf[i] > 0){
                                SimpleDateFormat sf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy" );
                                Date doubleValue =  sf.parse(value.toString());
//                                h.setLevel(1);
                                parent = h.getParent(doubleValue);

                                for(int j=0; j<k-1; j++){
                                    h.setLevel(j+1);
                                    parent = h.getParent(parent);
                                }
                            }
                            else{
                                RangeDate rangeValue = RangeDate.parseRange(value.toString());
                                parent = rangeValue;
                                int parentLevel = parentNodeTransf[i];
                                for(int j=0; j<k; j++){
                                    h.setLevel(parentLevel);
                                    parent = h.getParent(parent);
                                    parentLevel++;
                                }
                            }
                        }
                    }
                    else{
                       /* if(data.getColNamesType().get(qidColumns[i]).equals("string") || data.getColNamesType().get(qidColumns[i]).equals("date") ){
                            parent = value;
                            for(int j=0; j<k; j++)
                                parent =  h.getParent(parent);
                        }
                        else{*/
                            Double doubleValue = Double.parseDouble(value.toString());
                            parent = doubleValue;
                            int parentLevel = parentNodeTransf[i];
                            for(int j=0; j<k; j++){
                                h.setLevel(parentLevel);
                                parent = h.getParent(parent);  
                                parentLevel++;
                            }
                        //}  
                    }
                    
                    gRow.generalizedColumns[i] = parent.toString();
                }  
            }
            
            //compute frequencies based on parent's respective frequencies
            Integer count;
            Integer curCount = parentNodeBuffer.getFrequencies().get(pRow);   
            if((count = frequencies.get(gRow)) != null){
                
                frequencies.put(gRow, count + curCount);
            }
            else{
                
                frequencies.put(gRow, curCount);
            }
        }
    }
    
    public int getSize(){
        return this.frequencies.size();
    }
    
    /**
     * Getter of frequencies map
     * @return frequencies map
     */
    public Map<GeneralizedRow, Integer> getFrequencies() {
        return frequencies;
    }
    
    private class GeneralizedRow{
        public String[] generalizedColumns = null;

        public GeneralizedRow (int size){
            generalizedColumns = new String[size];
        }
        
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Arrays.deepHashCode(this.generalizedColumns);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GeneralizedRow other = (GeneralizedRow) obj;
            if (!Arrays.deepEquals(this.generalizedColumns, other.generalizedColumns)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return  Arrays.toString(generalizedColumns);
        }
    } 
    
}
