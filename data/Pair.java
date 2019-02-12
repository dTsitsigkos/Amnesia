/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonView;
import dictionary.DictionaryString;
import jsoninterface.View;

/**
 *
 * @author jimakos
 */
public class Pair {
    private double min;
    private double max; 
    private Data dataset = null; 
    private String vartype = null;
    
    public Pair(){
    
    }
        
    public Pair( Data _dataset, String vartype){
        max = 0.0;
        dataset = _dataset;
        this.vartype = vartype;
    }
    
    
    public void findMin(Integer columnIndex) {
        //Integer columnIndex = dataset.getColumnByName(selectedColumn);
        double[][] data = dataset.getDataSet();
        int counter = 0;
        
        if (vartype == null){
            for ( int i = 0 ; i < data.length; i ++){
                if ( data[i][columnIndex] != 2147483646.0 &&  data[i][columnIndex] != Double.NaN ){
                    min = data[i][columnIndex];
                    max = data[i][columnIndex];
                    counter ++;
                    break;
                }
            }

            for( int i=counter; i<data.length; i++){
                if ( data[i][columnIndex] != 2147483646.0 &&  data[i][columnIndex] != Double.NaN ){
                    if(data[i][columnIndex] > max){
                        max = data[i][columnIndex];
                    } 
                    else if(data[i][columnIndex] < min ){
                        min = data[i][columnIndex];
                    }
                }
            }
        }
        else{
            String []temp;
            String date;
            DictionaryString dictionary = dataset.getDictionary(columnIndex);
            
            for(int i=0; i<data.length; i++){
                Double d = (Double)data[i][columnIndex];
                date = dictionary.getIdToString(d.intValue());
                if ( !date.equals("NaN") ){
                    temp = date.split("/");
                    min = Integer.parseInt(temp[2]);
                    max = Integer.parseInt(temp[2]);
                    counter ++;
                    break;
                }
                
            }
            
            for( int i=counter; i<data.length; i++){
                Double d = (Double)data[i][columnIndex];   
                date = dictionary.getIdToString(d.intValue());
                if ( !date.equals("NaN") ){
                    temp = date.split("/");
                    int tempDate = Integer.parseInt(temp[2]);
                    if(tempDate > max){
                        max = tempDate;
                    } 
                    else if(tempDate < min ){
                        min = tempDate;
                    }
                }
            }
            
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
    

}

