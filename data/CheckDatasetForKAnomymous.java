/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jimakos
 */
public class CheckDatasetForKAnomymous {
    private Data dataset = null;
    
    public CheckDatasetForKAnomymous( Data _dataset){
        dataset = _dataset;
    }
    
    public int compute(Set<Integer> sQids){
        int k = -1;
        Map<String,Integer> freq = new HashMap<String,Integer>();
        double [][] data = null;
        double[][] setData = null;
        String setDelimeter="";
        
        if(dataset instanceof RelSetData){
            setData = ((RelSetData)dataset).getSet();
            setDelimeter = ((RelSetData)dataset).getSetDelimeter();
        }
        
        data = dataset.getDataSet();
        for (int i = 0 ; i < data.length; i++ ){
            String row = null;
            boolean FLAG = false;
            for ( int j = 0 ; j < data[i].length ; j ++ ){
                if (sQids.contains(j)){
                    if(data[i][j]==-1 && setData!=null){
                        String setRow = "";
                        for(int n=0; n< setData[i].length; n++){
                            setRow += setData[i][n];
                            if(n!=setData[i].length-1){
                                setRow += setDelimeter;
                            }
                        }
                        
                        if(FLAG == false){
                            row = setRow + "";
                            FLAG = true;
                        }
                        else{
                            row = row + "," + setRow;
                        }
                    }
                    else{
                        if ( FLAG == false){
                            row = data[i][j] + "";
                            FLAG = true;
                        }
                        else{
                            row = row + "," + data[i][j];
                        }
                    }
                }
            }
           
            //String row = data[i].toString();
            if (!freq.containsKey(row)){
                freq.put(row, 1);
            }
            else{
                freq.put(row, freq.get(row) + 1);
            }
        }
        
        Integer min = Collections.min(freq.values());
        
        return min;
    }
}
