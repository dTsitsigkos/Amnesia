/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.ranges;

import hierarchy.NodeStats;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jimakos
 */
public class AutoHierarchyImplRangesDate extends HierarchyImplRangesDate{
    private int start = -1;
    private int end = -1;
    private int years = -1;
    private int fanout = -1;
    private int[] daysOfmonth;
    
    
    public AutoHierarchyImplRangesDate(String _name, String _nodesType, String _hierarchyType, int _start, int _end, int _fanout, int _months, int _days, int _years) {
        super(_name, _nodesType);
        this.start = _start;
        this.end = _end;
        this.months = _months -1;

        this.days = _days;
        this.years = _years - 1;
        this.fanout = _fanout;
        this.height = 0;
    }
    
    /*Map<RangeDate, List<RangeDate>> children = new HashMap<>();
    Map<RangeDate, NodeStats> stats = new HashMap<>();
    Map<RangeDate, RangeDate> parents = new HashMap<>();
    Map<Integer,ArrayList<RangeDate>> allParents = new HashMap<>();*/
    
    
    /////////////////////////////////////////////
    //allParents
    //root = allParents.get(0).get(0);
    //stats.put(root, new NodeStats(0));
    //children
    
    @Override
    public void autogenerate() {
        
        ArrayList<RangeDate> initList = new ArrayList<>();
        
        ArrayList<Date> monthsArr = new ArrayList<>();
        ArrayList<Date> daysArr = new ArrayList<>();
        
        Map <Integer, ArrayList<Date>> yearsMap = new HashMap<Integer,ArrayList<Date>>();

        
        String dateStartStr = "1/1/"+ start;
        String dateEndStr = "31/12/" + end;
        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance() ;
        Date dateStart = null;
        Date dateEnd  = null;
        
        try {
            dateStart =  sf.parse(dateStartStr);
            dateEnd  =  sf.parse(dateEndStr);
        } catch (ParseException ex) {
            Logger.getLogger(AutoHierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        Date currentDate = dateStart;
        Date currentDate2 = null;
        
        
        
        
        
        
       if ( fanout == 0){
        
           //fill in the height, because it is a standard number
           if (days == 0){
                height = 2;
            }
            else{
                this.height = 3;
            }
        
        /////////////////////////////////previous Code/////////////////////////////////////////////
        
            //months contruction
            while ( currentDate.before(dateEnd)){
                monthsArr.add(currentDate);

                calendar.setTime(currentDate);
                if ( months == 0){
                    calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH)));

                }
                else{

                    calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) + months));

                }
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));


                currentDate2 = calendar.getTime();

                if (currentDate2.after(dateEnd)){

                    monthsArr.add(dateEnd);
                    break;
                }


                monthsArr.add(currentDate2);

                calendar.setTime(currentDate2);
                if ( months == 0){
                    calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) + 1));
                }
                else{
                    calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) + months));
                }
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR, 0);

                currentDate = calendar.getTime();
            }
            Date d = null;

            monthsArr.add(d);
            monthsArr.add(d);

            //System.out.println("monthssssssssssssssss");
            //for ( int  i = 0 ; i < monthsArr.size() ; i ++){
            //    System.out.println("date = " + monthsArr.get(i));
            //}
           // System.out.println("heightttttt = " + height);

            //days construction
            if (days != 0 ){
                
                Date currentDate3 = null;
                //-2 gia na min valw tsekarw to null
                for ( int i = 1 ; i < monthsArr.size() - 2 ; i = i + 2 ){
                    currentDate = monthsArr.get(i-1);
                    currentDate3 = monthsArr.get(i);
                    //System.out.println("currentDate = " + currentDate + "\tcurrentDate3 = " + currentDate3);
                    while ( currentDate.before(currentDate3)){
                        daysArr.add(currentDate);

                        calendar.setTime(currentDate);
                        if (days == 0){
                            calendar.set(Calendar.DAY_OF_MONTH, (calendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
                        }
                        else{
                            calendar.set(Calendar.DAY_OF_MONTH, (calendar.get(Calendar.DAY_OF_MONTH) + days));    
                        }
                        calendar.set(Calendar.HOUR, 0);
                        currentDate2 = calendar.getTime();

                        //fix days of the last range
                        if (!currentDate2.before(currentDate3) || currentDate2.equals(currentDate3) ){                   

                            //daysArr.add(currentDate3);

                            currentDate2 = currentDate3;


                            //break;
                        }


                        daysArr.add(currentDate2);

                        //calendar.setTime(currentDate2);
                        //if ( days == 0){

                        //}
                        //else{
                            calendar.set(Calendar.DAY_OF_MONTH, (calendar.get(Calendar.DAY_OF_MONTH) + 1));
                        //}
                        calendar.set(Calendar.HOUR, 0);

                        //System.out.println("currentDate = " + currentDate + "\tcurrentDate2 = " + currentDate2);

                        currentDate = calendar.getTime();
                    }

                    if  ( currentDate.equals(currentDate3)){
                        daysArr.remove(daysArr.size()-1);
                        daysArr.add(currentDate);
                    }

                }
            }

            //System.out.println("dayssssssssssssssssss");
            //for ( int  i = 1 ; i < daysArr.size() ; i = i + 2){
            //    System.out.println("date = " + daysArr.get(i-1) + "\t date2 = " + daysArr.get(i));
            //}

            //update all data structures
            root = new RangeDate(dateStart,dateEnd);
            ArrayList<RangeDate> allP = new ArrayList<RangeDate>();
            ArrayList<RangeDate> allP2 = new ArrayList<RangeDate>();
            allP.add(root);
            allParents.put(0, allP);
            allP = new ArrayList<RangeDate>();

            ArrayList<RangeDate> allC = new ArrayList<RangeDate>();
            ArrayList<RangeDate> allC2 = new ArrayList<RangeDate>();

            stats.put(root, new NodeStats(0));

            int j = 1 ;
            for ( int i = 1; i < monthsArr.size() ; i = i + 2 ){
                currentDate = monthsArr.get(i);
                allP.add(new RangeDate(monthsArr.get(i-1),monthsArr.get(i)));
                allC.add(new RangeDate(monthsArr.get(i-1),monthsArr.get(i)));
                parents.put( new RangeDate(monthsArr.get(i-1),monthsArr.get(i)), root);
                stats.put(new RangeDate(monthsArr.get(i-1),monthsArr.get(i)), new NodeStats(1));
                while( j < daysArr.size() && (daysArr.get(j).before(currentDate)|| daysArr.get(j).equals(currentDate))){
                    allP2.add(new RangeDate(daysArr.get(j-1),daysArr.get(j)));
                    allC2.add(new RangeDate(daysArr.get(j-1),daysArr.get(j)));
                    parents.put(new RangeDate(daysArr.get(j-1),daysArr.get(j)), new RangeDate(monthsArr.get(i-1),monthsArr.get(i)));
                    stats.put(new RangeDate(daysArr.get(j-1),daysArr.get(j)),new NodeStats(2));
                    j = j + 2;
                }
                if ( monthsArr.get(i-1) != null ){
                    children.put(new RangeDate(monthsArr.get(i-1),monthsArr.get(i)), allC2);
                    //System.out.println("father = " + monthsArr.get(i-1) + "-" +monthsArr.get(i) + " childs = " + allC2.toString());
                    allC2 = new ArrayList<RangeDate>();
                }


            }
            allParents.put(1, allP);
            allParents.put(2, allP2);
            children.put(root, allC);     
            
            ////////////////////////////////////////////////////////////////////////////////////////
       }
       else{
       
           //System.out.println("autogenerateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
           
           ArrayList<Date> yearsArr = new ArrayList<Date>();  
           
           //years bottom level contruction
            while ( currentDate.before(dateEnd) ){
                yearsArr.add(currentDate);

                calendar.setTime(currentDate);
                
                
                calendar.set(Calendar.YEAR, (calendar.get(Calendar.YEAR) + years));
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));


                currentDate2 = calendar.getTime();

                if (currentDate2.after(dateEnd) || currentDate2.equals(dateEnd)){
                    
                    Calendar calendarTemp = Calendar.getInstance() ;
                    Calendar calendarTemp2 = Calendar.getInstance() ;
                    
                    calendarTemp.setTime(currentDate);
                    calendarTemp2.setTime(dateEnd);
                    
                    if (calendarTemp2.get(Calendar.YEAR) - calendarTemp.get(Calendar.YEAR) < years ){
                        //System.out.println("calendarTemp1 = " + calendarTemp.get(Calendar.YEAR) + "\tcalendarTemp2 = " + calendarTemp2.get(Calendar.YEAR) + "\tyears = " +years );

                        //System.out.println("current Date = " + currentDate + "\t currentDate2 = " + currentDate2 + "\t dateEnd = " + dateEnd);   
                        
                        yearsArr.remove(yearsArr.size()-1);
                        yearsArr.remove(yearsArr.size()-1); 
                        
                    }
                    
                    yearsArr.add(dateEnd);
                    
                    break;
                }


                yearsArr.add(currentDate2);

                calendar.setTime(currentDate2);

                calendar.set(Calendar.YEAR, (calendar.get(Calendar.YEAR) + 1));
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MONTH, calendar.getActualMinimum(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                

                currentDate = calendar.getTime();

            }
//            yearsArr.add(null);
//            yearsArr.add(null);
            
            yearsMap.put(0, yearsArr);
            
            //System.out.println("autogenerateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee2222222222222222222");
            
            /*System.out.println("mappppppppppppppppppppppppppppp");
            for (Map.Entry<Integer, ArrayList<Date>> entry : yearsMap.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
            }*/
             
            //create other levels of year using fanout
            int numOfRanges = yearsArr.size()/2;
            int modOfRanges = numOfRanges%fanout;
            int level = 1;
            ArrayList<Date> yearsArrTemp = new ArrayList<Date>();

            System.out.println("i am hereeeeeeeeeeee numOfranges = " + numOfRanges + ", fanout = " + fanout);
            while ( numOfRanges > fanout || numOfRanges > 2 ){
                System.out.println("i am hereeeeeeeeeeee2222222");
                numOfRanges = yearsArr.size()/2;
                modOfRanges = numOfRanges%fanout;
                int i = 0;
                while( i <  yearsArr.size()){
                    yearsArrTemp.add(yearsArr.get(i));
                    i = i + 2*fanout -1;
                    
                    if ( i + modOfRanges >= yearsArr.size()){
                        //if (modOfRanges == 1 || modOfRanges == 0){/////////edw exei ginei malakia 2,5
                        
                        if( modOfRanges == 1){
                            yearsArrTemp.remove(yearsArrTemp.size()-1);
                            yearsArrTemp.remove(yearsArrTemp.size()-1);
                        }
                        //else{
                            yearsArrTemp.add(yearsArr.get(yearsArr.size()-1));
                        //}
                        /*}
                        else{
                            i = i - 2*fanout +1;
                            System.out.println("iiiiiiiiiiiiii = " + i );
                            yearsArrTemp.add(yearsArr.get(i));
                            i++;
                            yearsArrTemp.add(yearsArr.get(i));
                            yearsArrTemp.add(yearsArr.get(yearsArr.size()-1));
                        }*/
                        break;
                    }
                    
                    yearsArrTemp.add(yearsArr.get(i));
                    
                    i++;
                    
                }
                
                
                /*System.out.println("level = " + level);
                for (int j = 0 ; j < yearsArrTemp.size() ; j ++){
                    System.out.println(yearsArrTemp.get(j) + " + " +  yearsArrTemp.get(++j));

                }*/
                yearsMap.put(level, yearsArrTemp);
                ArrayList<Date> prevLevelYear = yearsMap.get(level - 1);
                
                int p = 1;
                for ( int k = 1 ; k < yearsArrTemp.size() ; k = k + 2 ){
                    RangeDate d = new RangeDate(yearsArrTemp.get(k-1), yearsArrTemp.get(k));
                    ArrayList<RangeDate> childsTemp = new ArrayList<RangeDate>(); 
                    
                    
                   
                    
                        while( p < prevLevelYear.size() &&  (prevLevelYear.get(p).before(yearsArrTemp.get(k)) || prevLevelYear.get(p).equals(yearsArrTemp.get(k)) )){
                            childsTemp.add(new RangeDate (prevLevelYear.get(p-1),prevLevelYear.get(p)));
                            parents.put(new RangeDate (prevLevelYear.get(p-1),prevLevelYear.get(p)), new RangeDate(yearsArrTemp.get(k-1), yearsArrTemp.get(k)));
                            p = p + 2;        
                        }
                    
                    
                    
                        children.put(d, childsTemp);
                        childsTemp = new ArrayList<RangeDate>();
                    
                }
                
                
                level++;
                
                if ( yearsArrTemp.size() == 2){
                    break;
                }
                
                yearsArr = new ArrayList<Date>();
                yearsArr.addAll(yearsArrTemp);
                yearsArrTemp = new ArrayList<Date>(); 
                //numOfRanges = yearsArr.size()/2;
                
            }
            
            //System.out.println("autogenerateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee3333333333333333333333333333");
            
            //create the first level of year
           
            /*yearsArrTemp.add(dateStart);
            yearsArrTemp.add(dateEnd);
            
            RangeDate d = new RangeDate(dateStart,dateEnd);
            ArrayList<Date> tempPrevLevel = yearsMap.get(level - 1);
            ArrayList<RangeDate> childsTemp = new ArrayList<RangeDate>();
            for( int i = 1 ; i < tempPrevLevel.size() ; i = i + 2){
                parents.put(new RangeDate(tempPrevLevel.get(i-1),tempPrevLevel.get(i)), new RangeDate(dateStart,dateEnd));
                childsTemp.add(new RangeDate(tempPrevLevel.get(i-1),tempPrevLevel.get(i)));
            }
            children.put(d, childsTemp);
                    
            yearsMap.put(level, yearsArrTemp);
            */
            
            System.out.println("mappppppppppppppppppppppppppppp222222222222222222222");
            for (Map.Entry<Integer, ArrayList<Date>> entry : yearsMap.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
            }
            
            
            height = yearsMap.size();
            
            ArrayList<Date> tempPrevLevel = null;
            ArrayList<RangeDate> childsTemp = null;
            RangeDate d = null;
            
            //System.out.println("monthsssssssssssss = " + months + "/t daysssssssssssssss = " + days);
            
            //create months
            if ( months != -1 ){
                height ++;
                yearsArr = new ArrayList<Date>();
                yearsArr = yearsMap.get(0);
                
                
                int firstlimit = 1;
                
                for ( int i = 1 ; i < yearsArr.size(); i = i + 2){
                    
                    int j = i;
                    currentDate = yearsArr.get(j-1);
                    dateEnd = yearsArr.get(j);
                    
                    d = new RangeDate(yearsArr.get(j-1), yearsArr.get(j));
                    
                    
                    System.out.println("current = " + currentDate + "/t end = " + dateEnd);
                    
                    while ( currentDate.before(dateEnd)){
                        //System.out.println("currentDate2222 = " + currentDate );
                        monthsArr.add(currentDate);

                        calendar.setTime(currentDate);
                        calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) + months));

                        
                        calendar.set(Calendar.HOUR, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));


                        currentDate2 = calendar.getTime();
                       // System.out.println("currentDate3333 = " + currentDate2 );
                        
                        
                        if (currentDate2.after(dateEnd)){
                            //System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                            //System.out.println("Months");
                            //System.out.println(monthsArr.toString());
                            monthsArr.add(dateEnd);
                            break;
                        }


                        monthsArr.add(currentDate2);

                        calendar.setTime(currentDate2);

                        calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH) + 1));
                        
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.HOUR, 0);

                        currentDate = calendar.getTime();
                    }
                    
                    
                    System.out.println("months = " + monthsArr.toString());
                    
                    childsTemp = new ArrayList<RangeDate>(); 
                    for( int k = firstlimit ; k < monthsArr.size() ; k = k + 2){
                        childsTemp.add(new RangeDate(monthsArr.get(k-1), monthsArr.get(k) ));
                        parents.put(new RangeDate(monthsArr.get(k-1), monthsArr.get(k) ),  new RangeDate(yearsArr.get(j-1), yearsArr.get(j)));
                    }
                    children.put(d, childsTemp);
                    
                    System.out.println("childsTemp = " + childsTemp );
                    
                    
                    //firstlimit = childsTemp.size() - 1 ;
                    childsTemp = new ArrayList<RangeDate>();
                    
                    firstlimit = monthsArr.size() + 1 ;
                }
                
                
                //logika tha prepei na valw kai to teleutaio sosososososos//
                
                //System.out.println("Months");
               // System.out.println(monthsArr.toString());
                
                
                firstlimit = 1;
                //create days
                if ( days != 0 ){
                    height ++;
                    
                    
                    
                    Date currentDate3 = null;
                    //-2 gia na min valw tsekarw to null
                    for ( int i = 1 ; i < monthsArr.size() ; i = i + 2 ){
                        currentDate = monthsArr.get(i-1);
                        currentDate3 = monthsArr.get(i);
                        //System.out.println("currentDate = " + currentDate + "\tcurrentDate3 = " + currentDate3);
                        
                        d = new RangeDate(monthsArr.get(i-1), monthsArr.get(i));
                        
                        while ( currentDate.before(currentDate3)){
                            
                            daysArr.add(currentDate);

                            calendar.setTime(currentDate);

                            calendar.set(Calendar.DAY_OF_MONTH, (calendar.get(Calendar.DAY_OF_MONTH) + days));    
                            
                            calendar.set(Calendar.HOUR, 0);
                            currentDate2 = calendar.getTime();

                            //fix days of the last range
                            if (!currentDate2.before(currentDate3) || currentDate2.equals(currentDate3) ){                   

                                //daysArr.add(currentDate3);

                                currentDate2 = currentDate3;


                                //break;
                            }


                            daysArr.add(currentDate2);

                            //calendar.setTime(currentDate2);
                            //if ( days == 0){

                            //}
                            //else{
                                calendar.set(Calendar.DAY_OF_MONTH, (calendar.get(Calendar.DAY_OF_MONTH) + 1));
                            //}
                            calendar.set(Calendar.HOUR, 0);

                            //System.out.println("currentDate = " + currentDate + "\tcurrentDate2 = " + currentDate2);

                            currentDate = calendar.getTime();
                        }
                        
                        if  ( currentDate.equals(currentDate3)){
                            daysArr.remove(daysArr.size()-1);
                            daysArr.add(currentDate);
                        }
                        
                        childsTemp = new ArrayList<RangeDate>(); 
                        for( int k = firstlimit ; k < daysArr.size() ; k = k + 2){
                            childsTemp.add(new RangeDate(daysArr.get(k-1), daysArr.get(k) ));
                            parents.put(new RangeDate(daysArr.get(k-1), daysArr.get(k) ),  new RangeDate(monthsArr.get(i-1), monthsArr.get(i)));
                        }
                        children.put(d, childsTemp);

                        childsTemp = new ArrayList<RangeDate>();
                        firstlimit = daysArr.size() + 1 ;


                    }
                    //System.out.println("Days");
                    //System.out.println(daysArr.toString());
                
                    //provlima 2,7,14,5  giati menei mono mia mera kai mallon den tin deixnei
                    
                }

            }
            
            
            //System.out.println("autogenerateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee44444444444444444444444444444444");
            RangeDate tempRoot = new RangeDate();
            tempRoot.setLowerBound(dateStart);
            tempRoot.setUpperBound(dateEnd);
            ArrayList<RangeDate> allP = new ArrayList<RangeDate>();
            ArrayList<RangeDate> allP2 = new ArrayList<RangeDate>();
            allP.add(tempRoot);
            allParents.put(0, allP);
            stats.put(tempRoot, new NodeStats(0));
            root = tempRoot;
//            root = new RangeDate(dateStart,dateEnd);
//            ArrayList<RangeDate> allP = new ArrayList<RangeDate>();
//            ArrayList<RangeDate> allP2 = new ArrayList<RangeDate>();
//            allP.add(root);
//            allParents.put(0, allP);
//            stats.put(root, new NodeStats(0));
            
            allP = new ArrayList<RangeDate>();

            ArrayList<RangeDate> allC = new ArrayList<RangeDate>();
            ArrayList<RangeDate> allC2 = new ArrayList<RangeDate>();
            
            ArrayList<Date> tempDate;
            
            int counter = 1;
            for ( int i = yearsMap.size() - 2 ; i >= 0 ; i --  ){
                tempDate = yearsMap.get(i);
                for ( int j = 1 ; j < tempDate.size() ; j = j + 2 ){
                    allP.add(new RangeDate(tempDate.get(j-1), tempDate.get(j)));
                    stats.put(new RangeDate(tempDate.get(j-1), tempDate.get(j)), new NodeStats(counter));
                }
                
                allParents.put(counter, allP);
                allP = new ArrayList<RangeDate>();
                counter++;
                
            }
            
            if( months != -1 ){
                for ( int i = 1; i < monthsArr.size() ; i = i + 2 ){
                    allP.add(new RangeDate(monthsArr.get(i-1), monthsArr.get(i)));
                    stats.put(new RangeDate(monthsArr.get(i-1), monthsArr.get(i)), new NodeStats(counter));
                }


                allParents.put(counter, allP);
                allP = new ArrayList<RangeDate>();
                counter ++;

                if (days != 0 ){
                    for ( int i = 1; i < daysArr.size() ; i = i + 2 ){
                        allP.add(new RangeDate(daysArr.get(i-1), daysArr.get(i)));
                        stats.put(new RangeDate(daysArr.get(i-1), daysArr.get(i)), new NodeStats(counter));
                    }

                    allParents.put(counter, allP);
                }
            }
            
            ///////null//////
            RangeDate ranNull = new RangeDate(null,null);
            allP = allParents.get(1);
            allP.add(ranNull);
            parents.put(ranNull, root);
            childsTemp = (ArrayList<RangeDate>) children.get(root);
            childsTemp.add(ranNull);
            children.put(ranNull,null);
            stats.put(ranNull,new NodeStats(1));
            
       } 
       
       
        
       //System.out.println("autogenerateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee5555555555555555555555555");
       
       
       
        /*System.out.println("all Parents");
        for (Map.Entry<Integer,ArrayList<RangeDate>> entry : allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        //System.out.println("/////////////////////////////////////");
        
        /*System.out.println("Stats");
        for (Map.Entry<RangeDate, NodeStats> entry : stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }*/
        /*System.out.println("/////////////////////////////////////");
        
        System.out.println("parents");
        for (Map.Entry<RangeDate, RangeDate> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("children");
        for (Map.Entry<RangeDate, List<RangeDate>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");*/
        
        //////////////////////////////////////end Previous Code//////////////////////////////////
        
        
        
        
        
        //.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
        
        /*for ( int  i = 0 ; i < daysArr.size() ; i ++){
            System.out.println("date = " + daysArr.get(i));
        }
        
        System.out.println("months size = " + monthsArr.size() + "\t daysSize = " + daysArr.size());*/
        
        /*int startDay = 1;
        int endDay = 31;
        String dateStart = "1/1/"+ start;
        String dateEnd = "1/1/" + end;
        Date curDate = null;
        
        Date d = null;
        
        SimpleDateFormat sf = null;
         
        sf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            d = sf.parse(dateStart);
            
            System.out.println("date = " + d);
        
        } catch (ParseException ex) {
            Logger.getLogger(AutoHierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Calendar calendar = Calendar.getInstance() ;
        
        calendar.setTime(d);
        System.out.println("Calendar month = " + (calendar.get(Calendar.MONTH) + 1));
        
        System.out.println("Calendar.DAY_OF_MONTH before = " + calendar.get(Calendar.DAY_OF_MONTH));
        
        calendar.set(Calendar.DAY_OF_MONTH, (calendar.get(Calendar.DAY_OF_MONTH) + 150));
        
        System.out.println("Calendar.DAY_OF_MONTH after = " + calendar.get(Calendar.DAY_OF_MONTH));
        
        
        System.out.println("Calendar month = " + (calendar.get(Calendar.MONTH) + 1));
        
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy" );
        
        System.out.println("date = " + sdf.format(calendar.getTime()));
        
        Date d2 = calendar.getTime();
        System.out.println("d222 = " + d2);*/
        
            
            //split domain to ranges using BigDecimal for accuracy
            /*ArrayList<RangeDouble> initList = new ArrayList<>();
            BigDecimal bdStart = new BigDecimal(start.toString());
            BigDecimal bdEnd = new BigDecimal(start.toString());
            BigDecimal bdFixEnd = new BigDecimal(end.toString());
            BigDecimal bdStep = new BigDecimal(step.toString());
            int showNULL = 0;
            boolean notFit= false;
            boolean FLAG = false;
            
            while(bdEnd.compareTo(bdFixEnd) < 0){
            bdEnd = bdStart.add(bdStep);
            if(bdEnd.compareTo(bdFixEnd) > 0 || bdEnd.compareTo(bdFixEnd) == 0){
            bdEnd = bdFixEnd;
            }   
            RangeDouble r = new RangeDouble();
            r.lowerBound = bdStart.doubleValue();
            r.upperBound = bdEnd.doubleValue();
            System.out.println("lower = " + r.lowerBound + "\tupper = " + r.upperBound +"\t find = " + (r.upperBound + step) +"\t end = " + end);
            r.nodesType = nodesType;          
            if ( r.upperBound + step > end ){
            double diff = end - r.upperBound;
            if ( diff > step/2 ){
            //System.out.println("bigger = " );
            r.upperBound = end;
            initList.add(r);
            
            }
            else{
            r = initList.get(initList.size()-1);
            r.upperBound = end;
            }
            
            notFit = true;
            break;
            }
            
            initList.add(r);
            bdStart = bdStart.add(bdStep);
            }
            
            
            
            /*System.out.println(" init list");
            for( int i = 0 ; i < initList.size() ; i ++){
            System.out.println("Low = " + initList.get(i).lowerBound + "\t up = " + initList.get(i).upperBound ) ;
            }
            height = computeHeight(fanout, initList.size());
            int curHeight = height - 1;
            if ( height == 1 ){//ean exei mono mia timi kai tha prepei na prosthesoume kai to null
            ArrayList<RangeDouble> arr = new ArrayList<RangeDouble>();
            RangeDouble ran = new RangeDouble(Double.NaN,Double.NaN);
            ran.nodesType = nodesType;
            arr.add(ran);
            allParents.put(height, arr);
            stats.put(allParents.get(1).get(0), new NodeStats(0));
            allParents.put(curHeight, initList);
            parents.put(ran, allParents.get(0).get(0));
            children.put(ran,null);
            children.put(allParents.get(0).get(0), arr);
            }
            else{
            if ( height == 2) {
            showNULL = 0;
            }
            else {
            showNULL = 1;
            }
            if (curHeight > 1 ){
            allParents.put(curHeight, initList);
            }
            else{
            RangeDouble ran = new RangeDouble(Double.NaN,Double.NaN);
            ran.nodesType = nodesType;
            initList.add(ran);
            allParents.put(curHeight, initList);
            }
            }
            while(curHeight > 0){
            RangeDouble[] prevLevel = allParents.get(curHeight).toArray(new RangeDouble[allParents.get(curHeight).size()]);
            for ( int i = 0; i < prevLevel.length ; i ++){
            System.out.println(prevLevel[i]);
            }
            int prevLevelIndex = 0;
            int curLevelSize = (int)(prevLevel.length / fanout + 1);
            if(fanout > 0){
            curLevelSize = prevLevel.length;
            }
            RangeDouble[] curLevel = null;
            curLevel = new RangeDouble[curLevelSize];
            int curLevelIndex = 0;
            while(prevLevelIndex < prevLevel.length){
            RangeDouble ran = new RangeDouble();
            ran.nodesType = nodesType;
            RangeDouble firstChild = null;
            RangeDouble lastChild = null;
            if(prevLevel.length - prevLevelIndex == 1){
            ran = prevLevel[prevLevelIndex];
            allParents.get(curHeight).remove(ran);
            prevLevelIndex++;
            }
            else{
            int j;
            RangeDouble[] tempArray = null;
            tempArray = new RangeDouble[fanout+1];
            for(j=0; j<fanout+1 && (prevLevelIndex < prevLevel.length); j++){//////////////////////////////////
            RangeDouble ch = prevLevel[prevLevelIndex];
            prevLevelIndex++;
            tempArray[j] = ch;
            parents.put(ch, ran);
            stats.put(ch, new NodeStats(curHeight));
            ch.nodesType = nodesType;
            if(j == 0){
            firstChild = ch;
            }
            else {
            if(!ch.lowerBound.equals(Double.NaN)){//gia na valoume null values
            lastChild = ch;
            }
            else{
            ch = prevLevel[prevLevelIndex-2];//pairnw to proteleutaio range giati to telutaio einai null
            lastChild = ch;
            }
            }
            }
            ran.lowerBound = firstChild.lowerBound;
            ran.upperBound = lastChild.upperBound;
            ran.nodesType = nodesType;
            if(j != fanout){
            tempArray = Arrays.copyOf(tempArray, j);
            }
            if ( tempArray[tempArray.length-1] == null){
            tempArray = Arrays.copyOf(tempArray, tempArray.length-1);
            }
            children.put(ran, new ArrayList<>(Arrays.asList(tempArray)));
            }
            curLevel[curLevelIndex] = ran;
            curLevelIndex++;
            }
            curHeight--;
            if(curLevelIndex != curLevelSize){
            curLevel = Arrays.copyOf(curLevel, curLevelIndex);
            }
            ArrayList arrList = null;
            if (curHeight == showNULL && showNULL > 0){
            arrList = new ArrayList<>(Arrays.asList(curLevel));
            RangeDouble ran = new RangeDouble(Double.NaN,Double.NaN);
            ran.nodesType = nodesType;
            arrList.add(ran);
            allParents.put(curHeight,arrList);
            }
            else{
            allParents.put(curHeight, new ArrayList<>(Arrays.asList(curLevel)));
            }
            }
            root = allParents.get(0).get(0);
            stats.put(root, new NodeStats(0));*/
        
    }
}
