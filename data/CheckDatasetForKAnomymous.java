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
        
        data = dataset.getDataSet();
        for (int i = 0 ; i < data.length; i++ ){
            String row = null;
            boolean FLAG = false;
            for ( int j = 0 ; j < data[i].length ; j ++ ){
                if (sQids.contains(j)){
                    if ( FLAG == false){
                        row = data[i][j] + "";
                        FLAG = true;
                    }
                    else{
                        row = row + "," + data[i][j];
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
