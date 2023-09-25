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
package algorithms.parallelflash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import algorithms.flash.GridNode;
import data.Data;
import data.DiskData;
import data.TXTData;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.distinct.HierarchyImplString;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author serafeim
 */
public class Worker extends RecursiveTask<Map<GeneralizedRow, Integer>>{
    Data data = null;
    Map<Integer, Hierarchy> hierarchies = null;
    GridNode node = null;
    GridNode parentNode = null;
    Buffer parentNodeBuffer = null;
    GeneralizedRow[] keysetArray = null;
    int[] qidColumns = null;
    Map<GeneralizedRow, Integer> frequencies = new HashMap<>();
    int start = -1;
    int end = -1;
    int worder_id = -1;
    public Worker nextJoin = null;
    
    public Worker(Data data,Map<Integer, Hierarchy> hierarchies,GridNode node,GridNode parentNode,
            Buffer parentNodeBuffer,GeneralizedRow[] keysetArray,int[] qidColumns,int start,
            int end,Worker nextJoin, int wi){
        this.data = data;
        this.hierarchies = hierarchies;
        this.node = node;
        this.parentNode = parentNode;
        this.parentNodeBuffer = parentNodeBuffer;
        this.keysetArray = keysetArray;
        this.qidColumns = qidColumns;
        this.start = start;
        this.end = end;
        this.nextJoin = nextJoin;
        this.worder_id = wi;
        System.out.println("worker id: "+this.worder_id);
    }
    
    @Override
    protected Map<GeneralizedRow, Integer> compute() {
        if(parentNode == null && parentNodeBuffer == null){
            try {
                System.out.println("Node from Root"+node);
                computeFromRoot();
            } catch (ParseException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                System.out.println("Node from Buffer"+node);
                computeFromBuffer();
            } catch (ParseException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return this.frequencies;
    }
    
    /**
     * projects QI columns of row according to node's elements
     * @param node a generalization graph node
     * @param row the row of the dataset to be generalized
     * @return the generalized QI columns of the row
     */
    private GeneralizedRow project(GridNode node, int[] qidColumns, double[] row) throws ParseException{
        DictionaryString dict = data.getDictionary();
        GeneralizedRow gRow = new GeneralizedRow(node.getTransformation().length);
        
        int j = 0;
        for(int k=0; k<node.getTransformation().length; k++){
            Hierarchy h = hierarchies.get(qidColumns[k]);
            
            //get the value of the specified attribute
            Object rowValue = null;
            //if(data.getColNamesType().get(qidColumns[k]).equals("string") || data.getColNamesType().get(qidColumns[k]).equals("date")){
//                DictionaryString dict = data.getDictionary(qidColumns[k]);
            //    rowValue = dict.getIdToString((int)row[qidColumns[k]]);
            //}
            //else {
            if(data.getColNamesType().get(qidColumns[k]).equals("string") ){
//                DictionaryString dict = data.getDictionary(qidColumns[k]);
                //rowValue = dict.getIdToString((int)row[qidColumns[k]]);
                rowValue = row[qidColumns[k]];
            }
            else if( data.getColNamesType().get(qidColumns[k]).equals("date")){
                if(data instanceof TXTData){
                    rowValue = dict.getIdToString((int)row[qidColumns[k]]);
                    if(rowValue == null){
                        rowValue = HierarchyImplString.getWholeDictionary().getIdToString((int)row[qidColumns[k]]);
                    }
                    System.out.println("Row value "+rowValue);
                }
                else{
                    SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
                    rowValue = df2.format(new Date((long)row[qidColumns[k]]));
                    System.out.println("Row value "+rowValue);
                }
            }
            else {
                rowValue = row[qidColumns[k]];
            }
            //}
            
            //generalize value
            for(int i=0; i<node.getTransformation()[k]; i++){
                h.setpLevel(this.worder_id,i);
//                System.out.println("Row value before if range: "+rowValue+" worder id: "+worder_id);
                if(h.getHierarchyType().equals("range")){
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
//                                System.out.println("Row value range: "+rowValue+" worder id: "+worder_id);
                                rowValue = h.getParent((Double)rowValue);
                            }    
                        }
                        else{
                            rowValue = h.getParent(rowValue, this.worder_id);
                        }
                    }
                    else if (rowValue!=null && h.getNodesType().equals("date")){
                        Date d = null;
                        RangeDate rd = null;
                        
                        //System.out.println("rowValueeeeeeeee = " + rowValue);
                        if (!rowValue.toString().contains("-")){
                            if (rowValue.toString().equals("NaN")){
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
//                                    Map<Integer, ArrayList<RangeDate>> x = h.getAllParents();
//                                    ArrayList<RangeDate> newList = x.get(x.size()-1);
//                                    if(newList.size() != 1){
//                                        rowValue = newList.get(0);
//                                    }
//                                    else{
//                                        rowValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
//                                    }
                                    
                                    rowValue = h.getParent(new RangeDate(null,null),this.worder_id);
                                    System.out.println("Row value null "+rowValue);
                                }
                                else{
                                    //System.out.println("mpika3333");
                                    System.out.println("edwwwwwwwwwwwwwwwww = " + d.toString());
                                    rowValue = h.getParent(d);
                                }    
                            }
                            else{
                                //System.out.println("mpika222222222222");
                                rowValue = h.getParent(rowValue,this.worder_id);
                            }
                        }
                        else{
                            if ( i ==0 ){
                                
                                if ( rd == null){
//                                    Map<Integer, ArrayList<RangeDate>> x = h.getAllParents();
//                                    ArrayList<RangeDate> newList = x.get(x.size()-1);
//                                    if(newList.size() != 1){
//                                        rowValue = newList.get(0);
//                                    }
//                                    else{
//                                        rowValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
//                                    }

                                    Map<Integer, ArrayList<RangeDate>> x = h.getAllParents();
                                    rowValue = h.getParent(new RangeDate(null,null),this.worder_id);
                                }
                                
                                else{
                                    rowValue = h.getParent(rd,this.worder_id);
                                }    
                            }
                            else{
                                System.out.println("mpika222222222222 "+rowValue);
                                rowValue = h.getParent(rowValue,this.worder_id);
                            }
                        }
                        //////////////////////////////////////////////////
                        //System.out.println("row value = " + rowValue);
                        
                    }
                }
                else{
                    rowValue = h.getParent(rowValue,this.worder_id);
                }
            }
            /*for(int i=0; i<node.getTransformation()[k]; i++){
                if(h.getHierarchyType().equals("range")){
                    if ( i ==0 ){
                        if ( (double) rowValue == 2147483646.0 ||  rowValue.equals(Double.NaN)){
                            Map<Integer, ArrayList<RangeDouble>> x = h.getAllParents();
                            ArrayList<RangeDouble> newList = x.get(x.size()-1);
                            if(newList.size() != 1){
                                rowValue = newList.get(0);
                            }
                            else{
                                System.out.println(" i am hereeeeeeeee anonymized value");
                                rowValue = x.get(0).get(0);//h.getParent((Double)anonymizedValue);
                            }
                        }
                        else{
                            System.out.println("valueeeeeeeeeeeeeeeee = " +  rowValue);
                            rowValue = h.getParent((Double)rowValue);
                            System.out.println("afterrrrrrrrrrrrrrrrrrrrrr = " +  rowValue);
                        }    
                    }
                    else{
                        rowValue = h.getParent(rowValue);
                    }
                }
                else{
                    rowValue = h.getParent(rowValue);
                }
            }*/
            
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
    
    private void computeFromRoot() throws ParseException{
        double[][] dataset;
        dataset = data.getDataSet();
//        if(data instanceof TXTData){
//            dataset = data.getDataSet();
//        }
//        else{
//            dataset = ((DiskData) data).getDataset(start, end-1);
//        }
        
        
        
        for(int i=start; i<end; i++){
            GeneralizedRow generalizedRow = project(node, qidColumns, dataset[i]);
//            System.out.println("Generalized Row "+generalizedRow);
//            if(node.toString().equals("[0, 2]")){
//                
//            }
            Integer count;
            
            if((count = frequencies.get(generalizedRow)) != null){
                frequencies.put(generalizedRow, ++count);
            }
            else {
                frequencies.put(generalizedRow, 1);
            }
        }
    }
    
    private void computeFromBuffer() throws ParseException{
        int[] nodeTransf = node.getTransformation();
        int[] parentNodeTransf = parentNode.getTransformation();
        Map<GeneralizedRow, Integer> parentFrequencies = parentNodeBuffer.getFrequencies();
        
        for(int row=start; row<end; row++){
            
            GeneralizedRow pRow = this.keysetArray[row];
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
                    
                    if(h.getHierarchyType().equals("range")){
                        if(h.getNodesType().equals("double") ||  h.getNodesType().equals("int")){
                            if(parentNodeTransf[i] == 0 && nodeTransf[i] > 0){
                                Double doubleValue = Double.parseDouble(value.toString());
                                int parentLevel = 0;
                                h.setpLevel(this.worder_id,parentLevel);
                                parent = h.getParent(doubleValue);
                                parentLevel++;
                                for(int j=0; j<k-1; j++){
                                    h.setpLevel(this.worder_id,parentLevel);
                                    parent = h.getParent(parent,this.worder_id);
                                    parentLevel++;
                                }
                            }
                            else{
                                RangeDouble rangeValue = RangeDouble.parseRange(value.toString());
                                parent = rangeValue;
                                int parentLevel = parentNodeTransf[i];
                                for(int j=0; j<k; j++){
                                    h.setpLevel(this.worder_id,parentLevel);
                                    parent = h.getParent(parent,this.worder_id);
                                    parentLevel++;
                                }
                            }
                        }
                        else if (h.getNodesType().equals("date") ){
                            if(parentNodeTransf[i] == 0 && nodeTransf[i] > 0){
                                SimpleDateFormat sf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy" );
                                if(data instanceof TXTData){
                                    Date doubleValue =  sf.parse(value.toString());
                                    parent = h.getParent(doubleValue);
                                }
                                else{
                                    parent = h.getParent(new Date((long)value));
                                }

                                for(int j=0; j<k-1; j++){
                                    h.setpLevel(this.worder_id,j+1);
                                    parent = h.getParent(parent,this.worder_id);
                                }
                            }
                            else{
                                RangeDate rangeValue = RangeDate.parseRange(value.toString());
                                parent = rangeValue;
                                int parentLevel = parentNodeTransf[i];
                                for(int j=0; j<k; j++){
                                    h.setpLevel(this.worder_id,parentLevel);
                                    parent = h.getParent(parent,this.worder_id);
                                    parentLevel++;
                                }
                            }
                        }
                    }
                    else{
                        Double doubleValue = Double.parseDouble(value.toString());
                        parent = doubleValue;
                        int parentLevel = parentNodeTransf[i];
                        for(int j=0; j<k; j++){
                            h.setpLevel(this.worder_id,parentLevel);
                            parent = h.getParent(parent,this.worder_id);  
                            parentLevel++;
                        }  
                    }
                    gRow.generalizedColumns[i] = parent.toString();
                }  
            }
            
            /*for(int i=0; i<pRow.generalizedColumns.length; i++){
                if(nodeTransf[i] == parentNodeTransf[i]){
                    gRow.generalizedColumns[i] = pRow.generalizedColumns[i];
                }
                //if this is the element to be further generalized
                else{
                    int k = nodeTransf[i] - parentNodeTransf[i];
                    Hierarchy h = hierarchies.get(qidColumns[i]);
                    Object value = pRow.generalizedColumns[i];
                    Object parent = null;
                    
                    if(h.getHierarchyType().equals("range")){
                        if(parentNodeTransf[i] == 0 && nodeTransf[i] > 0){
                            Double doubleValue = Double.parseDouble(value.toString());
                            parent = h.getParent(doubleValue);
                            
                            for(int j=0; j<k-1; j++)
                                parent = h.getParent(parent);
                        }
                        else{
                            RangeDouble rangeValue = RangeDouble.parseRange(value.toString());
                            parent = rangeValue;
                            for(int j=0; j<k; j++)
                                parent = h.getParent(parent);
                        }
                    }
                    else{
                        if(data.getColNamesType().get(qidColumns[i]).equals("string") || data.getColNamesType().get(qidColumns[i]).equals("date")){
                            parent = value;
                            for(int j=0; j<k; j++)
                                parent =  h.getParent(parent);
                        }
                        else{
                            Double doubleValue = Double.parseDouble(value.toString());
                            parent = doubleValue;
                            for(int j=0; j<k; j++)
                                parent = h.getParent(parent);
                        }
                    }
                    gRow.generalizedColumns[i] = parent.toString();
                }
            }*/
            
            //compute frequencies based on parent's respective frequencies
            Integer count;
            Integer curCount = parentFrequencies.get(pRow);
            if((count = frequencies.get(gRow)) != null){
                frequencies.put(gRow, count + curCount);
            }
            else{
                frequencies.put(gRow, curCount);
            }
        }
    }
    
}
