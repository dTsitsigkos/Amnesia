/*
 * Copyright (C) 2015 serafeim
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package solutions;

import com.fasterxml.jackson.annotation.JsonView;
import hierarchy.Hierarchy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jsoninterface.View;

/**
 *
 * @author serafeim
 */

public class SolutionStatistics {
        Map<SolutionAnonValues, Integer> details = new HashMap<>();
        int dataLength;
        Map <Integer,String> colNamesType = null;
        int pageLength= 0;
        
        public SolutionStatistics(int dataLength, Map <Integer,String> _colNamesType){
            this.dataLength = dataLength;
            this.colNamesType = _colNamesType;
        }

        public Map<SolutionAnonValues, Integer> getDetails() {
            return details;
        }

        public int getDataLength() {
            return dataLength;
        }

        public Map<Integer, String> getColNamesType() {
            return colNamesType;
        }
        
        public void add(Object[] values){
            SolutionAnonValues record = new SolutionAnonValues(values);
            Integer count = this.details.get(record);
            if(count != null){
                this.details.put(record, ++count);
            }
            else{
                this.details.put(record, 1);
            }
        }
        
        public void print(){

            for(SolutionAnonValues anonValues : this.details.keySet()){
                System.out.println(anonValues + " -> " + this.details.get(anonValues));
            }
        }
        
        public int size(){
            return this.details.size();
        }

        public int getPageLength() {
            return pageLength;
        }
        
        
        public int getSupport(SolutionAnonValues v){
            return this.details.get(v);
        }
        
        public double getPercentage(SolutionAnonValues v){
            return (getSupport(v) * 100.0 / dataLength);
        }
        
        public Set<SolutionAnonValues> getKeyset(){
            return this.details.keySet();
        }

        public void setDetails(Map<SolutionAnonValues, Integer> details) {
            this.details = details;
        }

        public void setDataLength(int dataLength) {
            this.dataLength = dataLength;
        }

        public void setColNamesType(Map<Integer, String> colNamesType) {
            this.colNamesType = colNamesType;
        }

        public void setPageLength(int pageLength) {
            this.pageLength = pageLength;
        }
        
        
        
        public void sort(){
            List<Map.Entry<SolutionAnonValues, Integer>> list = new LinkedList<>(this.details.entrySet());
            // Sorting the list based on values
            Collections.sort(list, new Comparator<Map.Entry<SolutionAnonValues, Integer>>()
            {
                @Override
                public int compare(Map.Entry<SolutionAnonValues, Integer> o1,
                        Map.Entry<SolutionAnonValues, Integer> o2)
                {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            
            // Maintaining insertion order with the help of LinkedList
            Map<SolutionAnonValues, Integer> sortedMap = new LinkedHashMap<>();
            for (Map.Entry<SolutionAnonValues, Integer> entry : list)
            {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            
            this.details = sortedMap;
            
        }
        
        
        public SolutionsArrayList getPage( int pageNum, int limit, int k){
            limit = 50;
            int pageCounter = 0;//pageNum;// * limit;
            int endCounter = dataLength;//(pageNum + 1);// *limit;
            int startCounter = 0;
            SolutionsArrayList solutions = new SolutionsArrayList();
            Solutions sol = null;
            pageLength= 0;
            solutions.k = k;

            //System.out.println("dataLength = " + dataLength);
            //System.out.println("details size = " + details.size());
            
            int problemRecords = 0 ;
            int restRecords = 0;
            
            /*for (Map.Entry<SolutionAnonValues, Integer> entry : details.entrySet()) {
    
                System.out.println("entry.getKey().toString() = " + entry.getKey().toString() +"\tentry.getValue().toString() =" + entry.getValue().toString());
                
                if ( startCounter >= pageCounter && endCounter > startCounter){
                    if ( entry.getValue() < k ){
                        sol = new Solutions(entry.getKey().toString(),entry.getValue().toString(),"#ff0000");
                    }
                    else{
                        sol = new Solutions(entry.getKey().toString(),entry.getValue().toString(),null);
                    }
                    pageLength += entry.getValue();
                    solutions.addNewSolution(sol);
                }
                else if( endCounter <= startCounter){
                    if (pageLength != dataLength ){ 
                        double percentage = ((double)pageLength/dataLength) * 100;
                        DecimalFormat df = new DecimalFormat("#.##");      
                        percentage = Double.valueOf(df.format(percentage));
                        solutions.setPagePercentange(percentage);
                        solutions.setEndPage(0);
                    }
                    else{
                        solutions.setPagePercentange(100);
                        solutions.setEndPage(1);
                    }
                    
                    return solutions;
                }               
                startCounter ++;
            }
            
            System.out.println("ENDdddddddgetPageeeeeeeeeeeeeeeeeeeeee");
            
            double percentage = ((double)pageLength/dataLength) * 100;
            DecimalFormat df = new DecimalFormat("#.##");      
            percentage = Double.valueOf(df.format(percentage));
            solutions.setPagePercentange(percentage);
            solutions.setEndPage(1);*/
            int counter = 0;
            int lastRecord = 0;

            
            for (Map.Entry<SolutionAnonValues, Integer> entry : details.entrySet()) {

                
                
                //if ( startCounter >= pageCounter && endCounter > startCounter){
                    if ( entry.getValue() < k ){
                        //sol = new Solutions(entry.getKey().toString(),entry.getValue().toString(),"#ff0000");
                        problemRecords += entry.getValue();
                    }
                    else{
                        if ( counter > limit ){
                            restRecords += entry.getValue();
                            
                        }
                        else{
                            sol = new Solutions(entry.getKey().toString(),entry.getValue().toString(),null);
                            solutions.addNewSolution(sol);
                            lastRecord = entry.getValue();
                        }
                    }
                    pageLength += entry.getValue();
                    
                    counter ++;
                
            }
            
           
            sol = new Solutions("problematic records",problemRecords+"","#ff0000");
            solutions.addNewSolution(sol);
            
            double percentageRest = ((double)lastRecord/dataLength)*100;
               
            
            sol = new Solutions("Records with support less than " + Math.round(percentageRest) + "%", restRecords+"","#00FFFF");
            solutions.addNewSolution(sol);
            
            //solutions.setPagePercentange(100);
            //System.out.println("pageLength = " + pageLength + "\tdataLength = " + dataLength);
            
            double percentage = ((double)pageLength/dataLength) * 100;
            DecimalFormat df = new DecimalFormat("#.##");      
            percentage = Double.valueOf(df.format(percentage));
            solutions.setPagePercentange(percentage);
            //solutions.setEndPage(1);
            
            return solutions;
            
        }
        
        
        public class SolutionAnonValues implements Comparable<SolutionAnonValues>{
            Object[] anonValues;
            
            public SolutionAnonValues(Object[] _anonValues){
                this.anonValues = _anonValues;
            }
            
            @Override
            public int hashCode() {
                int hash = 5;
                hash = 59 * hash + Arrays.deepHashCode(this.anonValues);
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

                final SolutionAnonValues other = (SolutionAnonValues) obj;
                if (!Arrays.deepEquals(this.anonValues, other.anonValues)) {
                    return false;
                }
                return true;
            }
            
            @Override
            public String toString() {
                return Arrays.toString(anonValues);
            }
            
            public Object[] getAnonValues() {
                return anonValues;
            }
            
            @Override
            public int compareTo(SolutionAnonValues o) {
                return this.toString().compareTo(o.toString());
            }
            
            public SolutionAnonValues putNUllValues(){
                Object[] anonValueWithNULLValues = new Object[anonValues.length];
                
                for (int i = 0 ; i < anonValues.length ; i++ ){
                    if (anonValues[i].equals(2147483646) || anonValues[i].equals(Double.NaN) || anonValues[i].equals("NaN")){
                        anonValueWithNULLValues[i] = "(null)";
                    }
                    else{
                        anonValueWithNULLValues[i] = anonValues[i];
                    }
                
                }
                
                return  new SolutionAnonValues(anonValueWithNULLValues);
            }
            
            public SolutionAnonValues removeNUllValues(Map<Integer, Hierarchy> quasiIdentifiers, int[] qids){
                Object[] anonValuenoNULLValues = new Object[anonValues.length];
                Hierarchy h ;
                
                for (int i = 0 ; i < anonValues.length ; i++ ){
                    if (anonValues[i].equals("(null)")){

                            if ( colNamesType.get(i).equals("int")){
                                anonValuenoNULLValues[i] = 2147483646;
                            }
                            else if ( colNamesType.get(i).equals("double")){
                                anonValuenoNULLValues[i] = Double.NaN;
                            }
                            else{
                               anonValuenoNULLValues[i] = "NaN";
                            }
                        
                    }
                    else{
                        anonValuenoNULLValues[i] = anonValues[i];
                    }
                }
                
                return  new SolutionAnonValues(anonValuenoNULLValues);
            }
            
        }
    }
