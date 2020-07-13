/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.ranges;

import anonymizeddataset.AnonymizedDataset;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author jimakos
 */
public class RangeDate {
    public Date lowerBound = null;
    public Date upperBound = null;
    
    public RangeDate(){
    
    }
    
    public RangeDate(Date _lowerBound, Date _upperBound){
        this.lowerBound = _lowerBound;
        this.upperBound = _upperBound;
    }

    public Date getLowerBound() {
        return lowerBound;
    }

    public Date getUpperBound() {
        return upperBound;
    }

    public void setLowerBound(Date lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(Date upperBound) {
        this.upperBound = upperBound;
    }
    
    @Override 
    public String toString(){
       StringBuilder sb = new StringBuilder();

       sb.append(this.lowerBound);
       sb.append("-");
       sb.append(this.upperBound);

       return sb.toString();
    }     
    
    @Override
    public boolean equals(Object obj){
        
        if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
        RangeDate r = (RangeDate) obj;
        
        if (r.lowerBound == null && this.lowerBound == null && r.upperBound == null && this.upperBound == null){
            return true;
        }
        else if((r.lowerBound == null && r.upperBound == null) && (this.upperBound !=null && this.lowerBound !=null)){
            return false;
        }
        else if((this.lowerBound == null && this.upperBound == null) && (r.upperBound !=null && r.lowerBound !=null)){
            return false;
        }
        
        return (this.upperBound.equals(r.upperBound)) && (this.lowerBound.equals(r.lowerBound));
        
        
        
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    public int compareTo(RangeDate d){
        if(d==null && this.upperBound==null && this.lowerBound==null){
            return 0;
        }
        else if(this.upperBound==null && this.lowerBound==null && d.upperBound==null && d.lowerBound==null){
            return 0;
        }
        else if(this.upperBound==null && this.lowerBound==null){
           return -1; 
        }
        else if(d.upperBound==null && d.lowerBound==null){
            return 1;
        }
        else if(upperBound.equals(d.upperBound) && lowerBound.equals(d.lowerBound)){
            return 0;
        }
        else if(this.lowerBound.before(d.lowerBound)  && upperBound.equals(d.upperBound)){
            return 1;
        }
        else if(this.upperBound.after(d.upperBound)){
            return 1;
        }
        else{
            return -1;
        }
    }
    
    
    public int compareTo(Date d){
        if(d==null && this.upperBound==null && this.lowerBound==null){
            return 0;
        }
        else if(d==null){
            return 1;
        }
        else if(this.upperBound==null && this.lowerBound==null){
            return -1;
        }
        else if(this.upperBound.equals(d)){
            return 1;
        }
        else if(this.lowerBound.equals(d)){
            return 1;
        }
        else if(this.upperBound.after(d) && this.lowerBound.before(d)){
            return 1;
        }
        else if(this.lowerBound.after(d)){
            return 1;
        }
        else{
            return -1;
        }
    }
    
    public boolean contains(String v) throws ParseException{
        Date d = AnonymizedDataset.getDateFromString(v);
        return this.contains(d);
    }
    
    public void print(){
        BigInteger total = BigInteger.ZERO;
        System.out.print(this.toString());
        System.out.println("Time: "+BigInteger.valueOf(this.lowerBound.getTime())+"    "+BigInteger.valueOf(this.lowerBound.getTime()));
    }
    
    public boolean contains(Date v){
        //return v >= this.lowerBound && v <= this.upperBound;
//        System.out.println("v = "+v);
        if(v==null && this.lowerBound==v && this.upperBound==v){
//            RangeDate ranNull = new RangeDate(null,null);
            return true;
        }
        else if(v==null){
            return false;
        }
        else if(this.lowerBound==null && this.upperBound==null){
            return false;
        }
        else{
            if (v.equals(this.lowerBound)){
                return true;
            }
            else if (v.equals(this.upperBound)){
                return true;
            }
            else if ( v.after(this.lowerBound) && v.before(this.upperBound)){
                return true;
            }
        }
            
        return false;
        
        //return v.after(this.lowerBound) && v.before(this.upperBound);
    }
    
    public boolean contains2(String v, boolean FLAG) throws ParseException{
        return contains2(AnonymizedDataset.getDateFromString(v),FLAG);
    }
    
    public boolean contains2(Date v, boolean FLAG){
        if(v!=null && this.lowerBound==null &&  this.upperBound==null){
            return false;
        }
        if ( FLAG == true){
            //return v >= this.lowerBound && v < this.upperBound;
            if(v==null && this.lowerBound==v &&  this.upperBound==v){
                return true;
            }
            else if(v==null){
                return false;
            }
            else if(this.lowerBound==null && this.upperBound==null){
                return false;
            }
            else if (v.equals(this.lowerBound)){
                return true;
            }
            else if ( v.after(this.lowerBound) && v.before(this.upperBound)){
                return true;
            }
            
            
            return false;
        }
        else{
            if (v.equals(this.lowerBound)){
                return true;
            }
            else if (v.equals(this.upperBound)){
                return true;
            }
            else if ( v.after(this.lowerBound) && v.before(this.upperBound)){
                return true;
            }
            return false;
            //System.out.println(" v = " + v + "\tmin = " + this.lowerBound + "\tmax = " + this.upperBound);
            //return v >= this.lowerBound && v <= this.upperBound;
        }
    }
    
    private boolean lastDayOfMonth(int year, int month, int day){
        if(month == 2){
            if(year % 4 == 0){
                if(day == 29){
                    return true;
                }
                else{
                    return false;
                }
            }
            else if(day == 28){
                return true;
            }
            else{
                return false;
            }
        }
        else if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12){
            if(day == 31){
                return true;
            }
            else{
                return false;
            }
        }
        else{
          if(day == 30){
                return true;
            }
            else{
                return false;
            }  
        }
    }
    
    public String dateToString(){
        String str = null;
        Calendar calendar = Calendar.getInstance() ;
        if(lowerBound!=null){
            calendar.setTime(lowerBound);

            Calendar calendar2 = Calendar.getInstance() ;

            calendar2.setTime(upperBound);
            if(calendar.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)){
                str = Integer.toString(calendar.get(Calendar.YEAR));
            }
            else{
                str = calendar.get(Calendar.YEAR) + "-" +calendar2.get(Calendar.YEAR);
            }
            if((calendar.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR)) && ((calendar.get(Calendar.MONTH) + 1) == 1 && (calendar2.get(Calendar.MONTH) + 1) == 12) &&  (calendar.get(Calendar.DAY_OF_MONTH) == 1 && calendar2.get(Calendar.DAY_OF_MONTH) == 31)){
                return str; 
            }
            else{
                if(calendar.get(Calendar.DAY_OF_MONTH) == 1 && lastDayOfMonth(calendar2.get(Calendar.YEAR),(calendar2.get(Calendar.MONTH) + 1),calendar2.get(Calendar.DAY_OF_MONTH)) && calendar.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH)){
                    if(((calendar.get(Calendar.MONTH) + 1) == 1 && (calendar2.get(Calendar.MONTH) + 1) == 12) && calendar.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)){
                        str = Integer.toString(calendar.get(Calendar.YEAR));
                    }
                    else{
                        str = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "-" + (calendar2.get(Calendar.MONTH)  + 1) + "/" + calendar2.get(Calendar.YEAR);
                    }
                }
                else{
                    str = calendar.get(Calendar.DAY_OF_MONTH) + "/" + ( calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "-" + calendar2.get(Calendar.DAY_OF_MONTH) + "/" + (calendar2.get(Calendar.MONTH) + 1) + "/" + calendar2.get(Calendar.YEAR);
                }
            }
            return str;
        }
        else{
            return "(null)";
        }
    }
    
    public String dateToString(int level){
        String str = null;
        Calendar calendar = Calendar.getInstance() ;
        if (lowerBound != null){

        
            calendar.setTime(lowerBound);

            Calendar calendar2 = Calendar.getInstance() ;

            calendar2.setTime(upperBound);

            switch (level) {
                case 0:
                    if(calendar.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)){
                        str = Integer.toString(calendar.get(Calendar.YEAR));
                    }
                    else{
                        str = calendar.get(Calendar.YEAR) + "-" +calendar2.get(Calendar.YEAR);
                    }
                    break;
                case 1:
                    str = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "-" + (calendar2.get(Calendar.MONTH)  + 1) + "/" + calendar2.get(Calendar.YEAR);
                    break;
                default:
                    str = calendar.get(Calendar.DAY_OF_MONTH) + "/" + ( calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "-" + calendar2.get(Calendar.DAY_OF_MONTH) + "/" + (calendar2.get(Calendar.MONTH) + 1) + "/" + calendar2.get(Calendar.YEAR);
                    break;
            }
        }
        else{
            str = "(null)";
        }
        
        return str;
    }
    
    public static String dateToString(int level,Date d){
        String str = null;
        Calendar calendar = Calendar.getInstance() ;
        if(d!=null){
            calendar.setTime(d);
            switch (level) {
                case 0:
                    str = calendar.get(Calendar.YEAR)+"";
                    break;
                case 1:
                    str = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
                    break;
                default:
                    str = calendar.get(Calendar.DAY_OF_MONTH) + "/" + ( calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) ;
                    break;
            }
        }
        else{
            str = "(null)";
        }
        
        
        return str;
    }
    
    
    
    public String dateToExportHierString(int level){
        String str = null;
        Calendar calendar = Calendar.getInstance() ;
        if (lowerBound != null){

        
            calendar.setTime(lowerBound);

            Calendar calendar2 = Calendar.getInstance() ;

            calendar2.setTime(upperBound);

            switch (level) {
                case 0:
                    if(calendar.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)){
                        str = Integer.toString(calendar.get(Calendar.YEAR));
                    }
                    else{
                        str = calendar.get(Calendar.YEAR) + "," +calendar2.get(Calendar.YEAR);
                    }					
                    break;
                case 1:
                    str = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "," + (calendar2.get(Calendar.MONTH)  + 1) + "/" + calendar2.get(Calendar.YEAR);
                    break;
                default:
                    str = calendar.get(Calendar.DAY_OF_MONTH) + "/" + ( calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "," + calendar2.get(Calendar.DAY_OF_MONTH) + "/" + (calendar2.get(Calendar.MONTH) + 1) + "/" + calendar2.get(Calendar.YEAR);
                    break;
            }
        }
        else{
            str = "null,null";
        }
        
        return str;
    }
    
    public static RangeDate parseRange(String str) throws ParseException{
        if(str.contains("null")){
            return new RangeDate(null,null);
        }
        SimpleDateFormat sf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy",Locale.US );
        String[] arr = str.split("-");
        
        if(arr.length != 2){
            Date lowBound = sf.parse(str.trim());
            Date highBound = sf.parse(str.trim());
            return new RangeDate(lowBound, highBound);
        }
       
        Date lowBound = sf.parse(arr[0].trim());
        Date highBound = sf.parse(arr[1].trim());
        
        return new RangeDate(lowBound, highBound);
    }
    
}
