/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonView;
import dictionary.DictionaryString;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsoninterface.View;
import data.Pair;

/**
 *
 * @author jimakos
 */
public class MyPair {
    private double min;
    private double max; 
    private Data dataset = null; 
    private String vartype = null;
    
    public MyPair(){
    
    }
        
    public MyPair( Data _dataset, String vartype){
        max = 0.0;
        dataset = _dataset;
        this.vartype = vartype;
    }
    
    
    public void findMin(Integer columnIndex) {
        //Integer columnIndex = dataset.getColumnByName(selectedColumn);
        if(dataset instanceof DiskData){
            DiskData diskData = (DiskData) dataset;
            Pair<Double,Double> minMax = diskData.getMinMax(columnIndex, "dataset");
            if (vartype == null){
                this.min = minMax.getKey();
                this.max = minMax.getValue();
            }
            else{
               Calendar cal1 = Calendar.getInstance(), cal2 = Calendar.getInstance();
               Date mindate = new Date(minMax.getKey().longValue());
               Date maxdate = new Date(minMax.getValue().longValue());
               
               cal1.setTime(mindate);
               cal2.setTime(maxdate);
               
               this.min = cal1.get(Calendar.YEAR);
               this.max = cal2.get(Calendar.YEAR);
            }
        }
        else{
            double[][] data = dataset.getDataSet();
            int counter = 0;

            if (vartype == null){
                for ( int i = 0 ; i < data.length; i ++){
                    if ( data[i][columnIndex] != 2147483646.0 &&  !Double.isNaN(data[i][columnIndex]) ){
                        System.out.println("i= "+i+" data "+data[i][columnIndex]);
                        min = data[i][columnIndex];
                        max = data[i][columnIndex];
                        counter ++;
                        break;
                    }
                }

                for( int i=counter; i<data.length; i++){
                    if ( data[i][columnIndex] != 2147483646.0 &&  !Double.isNaN(data[i][columnIndex]) ){
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
                DictionaryString dictionary = dataset.getDictionary();
                SimpleDateFormat df = dataset.getDateFormat(columnIndex);
                Calendar cal1 = Calendar.getInstance();

                for(int i=0; i<data.length; i++){
                    Double d = (Double)data[i][columnIndex];
                    date = dictionary.getIdToString(d.intValue());
                    if ( !date.equals("NaN") ){
                        try {
                            cal1.setTime(df.parse(date));
                            min = cal1.get(Calendar.YEAR);
                            max = cal1.get(Calendar.YEAR);
                            counter ++;
                            break;
                        } catch (ParseException ex) {
                            Logger.getLogger(MyPair.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }

                for( int i=counter; i<data.length; i++){
                    Double d = (Double)data[i][columnIndex];   
                    date = dictionary.getIdToString(d.intValue());
                    cal1 = Calendar.getInstance();
                    if ( !date.equals("NaN") ){
                        try {
                            cal1.setTime(df.parse(date));
                            int tempDate = cal1.get(Calendar.YEAR);
                            if(tempDate > max){
                                max = tempDate;
                            } 
                            else if(tempDate < min ){
                                min = tempDate;
                            }
                        } catch (ParseException ex) {
                            Logger.getLogger(MyPair.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
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

