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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author serafeim
 */
public class RangeDouble {
    public Double lowerBound; 
    public Double upperBound;
    public String nodesType;
    public DecimalFormat newFormat = new DecimalFormat("#.##");
    
    
    public RangeDouble(){
        
    }
    
    public RangeDouble(Double _lowerBound, Double _upperBound){
        this.lowerBound = _lowerBound;
        this.upperBound = _upperBound;
        this.nodesType = null;
    }
    
    public void print(){
        System.out.println(lowerBound + "-" + upperBound);
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
        RangeDouble r = (RangeDouble) obj;
        return (this.upperBound.equals(r.upperBound)) && (this.lowerBound.equals(r.lowerBound));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        //if ( this.lowerBound != null && this.upperBound != null ){
            //System.out.println("lower= " + this.lowerBound);
            //System.out.println("upper= " + this.upperBound);
            if(this.lowerBound !=null && this.upperBound!=null){
                hash = 59 * hash + (int) (Double.doubleToLongBits(this.lowerBound) ^ (Double.doubleToLongBits(this.lowerBound) >>> 32));
                hash = 59 * hash + (int) (Double.doubleToLongBits(this.upperBound) ^ (Double.doubleToLongBits(this.upperBound) >>> 32));
            }
            else{
                return 0;
            }
        //}
        return hash;
    }
    
     @Override 
     public String toString(){
        StringBuilder sb = new StringBuilder();
        
        if(this.lowerBound.equals(Double.NaN)){
            return "(null)";
        }
        if (this.nodesType!=null && this.nodesType.equals("double")){
            sb.append(this.lowerBound);
            sb.append("-");
            sb.append(this.upperBound);
            //sb.append(" - ");
            //sb.append(this.nodesType);
        }
        else{
            sb.append(this.lowerBound.intValue());
            sb.append("-");
            sb.append(this.upperBound.intValue());
            //sb.append(" - ");
            //sb.append(this.hierType);
        }
        
        /*if ( this.lowerBound != null && this.upperBound != null ){
            if(isInt(this.lowerBound)){
                sb.append(this.lowerBound.intValue());
            }
            else{
               sb.append(this.lowerBound);
            }
            sb.append(" - ");

            if(isInt(this.lowerBound)){
                System.out.println("to string  = " + this.upperBound + "i am here");
               sb.append(this.upperBound.intValue());
            }
            else{
                System.out.println("to string  = " + this.upperBound + "i am here22222");
                sb.append(this.upperBound);
            }
        }*/
         
        return sb.toString();
     }

    private boolean isInt(Double d){
        return ((d == Math.floor(d) && !Double.isInfinite(d)));
    }
    
    public void setLowerBound(Double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(Double upperBound) {
        this.upperBound = upperBound;
    }

    public Double getLowerBound() {
        return lowerBound;
    }

    public Double getUpperBound() {
        return upperBound;
    }
    
    public boolean contains(Double v){
        if((Double.isNaN(v) || v==2147483646.0) && ((this.lowerBound==0 && this.upperBound==0) || (Double.isNaN(this.lowerBound) && Double.isNaN(this.upperBound)))){
            return true;
        }
        else if(Double.isNaN(v)){
            return false;
        }
        else if(v==2147483646.0){
            return false;
        }
        else{
            return v >= this.lowerBound && v <= this.upperBound;
        }
    }
    
    public boolean contains2(Double v, boolean FLAG){
//        System.out.println(" v = " + v + "\tmin = " + this.lowerBound + "\tmax = " + this.upperBound+" boolean "+((!Double.isNaN(v) && v!=2147483646.0) && ((this.lowerBound==0 && this.upperBound==0) || (Double.isNaN(this.lowerBound) && Double.isNaN(this.upperBound))))+" boolean2 "+this.equals(new RangeDouble(Double.NaN,Double.NaN)));
        if((!Double.isNaN(v) && v!=2147483646.0) && ((this.lowerBound==0 && this.upperBound==0) || (Double.isNaN(this.lowerBound) && Double.isNaN(this.upperBound)))){
            return false;
        }
        if ( FLAG == true){
            if((Double.isNaN(v) || v==2147483646.0) && ((this.lowerBound==0 && this.upperBound==0) || (Double.isNaN(this.lowerBound) && Double.isNaN(this.upperBound)))){
                return true;
            }
            else if(Double.isNaN(v)){
                return false;
            }
            else if(v==2147483646.0){
                return false;
            }
            else{
                return v >= this.lowerBound && v < this.upperBound;
            }
        }
        else{
//            System.out.println(" v = " + v + "\tmin = " + this.lowerBound + "\tmax = " + this.upperBound);
            return v >= this.lowerBound && v <= this.upperBound;
        }
    }
    
    public int compareTo(RangeDouble d){
        if(this.upperBound.equals(d.upperBound) && this.lowerBound.equals(d.lowerBound)){
            return 0;
        }
        else if(this.lowerBound < d.lowerBound &&  this.upperBound.equals(d.upperBound)){
            return 1;
        }
        else if(this.upperBound > d.upperBound){
            return 1;
        }
        else{
            return -1;
        }
    }
    
    public int compareTo(Double d){
        if(this.upperBound.equals(d)){
            return 1;
        }
        else if(this.lowerBound.equals(d)){
            return 1;
        }
        else if(this.lowerBound < d && this.upperBound > d){
            return 1;
        }
        else if(this.lowerBound > d){
            return 1;
        }
        else{
            return -1;
        }
    }
     
    public boolean overlays(RangeDouble r){
        return (r.lowerBound < this.lowerBound && r.upperBound < this.lowerBound)
                || (r.lowerBound >= this.upperBound && r.upperBound >= this.upperBound);
    }
    
    public static RangeDouble parseRange(String str){
        String[] arr = str.split("-");
        Double start=null,end=null;
        int count = StringUtils.countMatches(str, "-");
        if(count==1){
            start = Double.parseDouble(arr[0]);
            end = Double.parseDouble(arr[1]);
        }
        else if(count==2){
            try{
                start = Double.parseDouble("-"+arr[1]);
                end = Double.parseDouble(arr[2]);
                System.out.println("Count "+count+" start "+start+" end "+end);
            }catch(Exception e1){
                e1.printStackTrace();

                // TODO exception 
            }
        }
        else if(count==3){
            try{
                start = Double.parseDouble("-"+arr[1]);
                end = Double.parseDouble("-"+arr[3]);
                System.out.println("Count "+count+" start "+start+" end "+end);
            }catch(Exception e2){
                 e2.printStackTrace();

                // TODO exception 
            }
        }
        else{
            /// TODO exception 
            if(str.equals("(null)")){
                return new RangeDouble(Double.NaN,Double.NaN);
            }
            System.out.println("Count "+count);
        }
//        double lowBound = Double.parseDouble(arr[0].trim());
//        double highBound = Double.parseDouble(arr[1].trim());
        
        return new RangeDouble(start, end);
    }

    public void setNodesType(String nodesType) {
        this.nodesType = nodesType;
    }
    
//    public void toFixed(Double start, Double end){
//        if(!start.equals(this.lowerBound)){
//           this.lowerBound =  BigDecimal.valueOf(lowerBound).setScale(7, RoundingMode.HALF_UP)
//            .doubleValue();
//        }
//        
//        if(!end.equals(this.upperBound)){
//            this.upperBound =  BigDecimal.valueOf(upperBound).setScale(7, RoundingMode.HALF_UP)
//            .doubleValue();
//        }
//    }

    
}
