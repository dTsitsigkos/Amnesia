/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import data.Data;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;
import java.math.BigDecimal;
import java.util.Map;

/**
 *
 * @author jimakos
 */
public class Queries {
    private String[] identifiersArr;
    private double[] minArr;
    private double[] maxArr;
    private String[] distinctArr;
    private String[] newDistinctArr;
    private double[][] nonAnonymized;
    private String queryCategory = null;
    Map<String, Hierarchy> hierarchies;
    private int nonAnonymizeOccurrences = 0;
    private int anonymizedOccurrences = 0;
    private int possibleOccurencences = 0;
    private double estimatedRate = 0.0;
    private int []hierarchyLevel;
    private DictionaryString dict = null;
    private Map <Integer,String> columnTypes;
    private Data data;
    private Map<Integer, Hierarchy> quasiIdentifiers;
    double allEstimatedRate = 0.0;
    double [][]dataset = null;
    Results results;
    
    public Queries(String[] _identifiersArr, double[] _minArr, double[] _maxArr , String[] _distinctArr, Map<String, Hierarchy> _hierarchies,Data  _data, int []_hierarchyLevel, Map<Integer, Hierarchy> _quasiIdentifiers){
        this.identifiersArr = _identifiersArr;
        this.minArr = _minArr;
        this.maxArr = _maxArr;
        this.distinctArr = _distinctArr;
        this.hierarchies = _hierarchies;
        this.nonAnonymized = _data.getDataSet();
        this.hierarchyLevel = _hierarchyLevel;
        this.columnTypes = _data.getColNamesType();
        this.data = _data;
        this.quasiIdentifiers = _quasiIdentifiers;
        results = new Results();
        //preprocessing();

    }
    
    /*public void preprocessing(){//find queryCategory
        boolean FLAG1 = false;
        boolean FLAG2 = false;
        
        for( int i = 0 ; i < identifiersArr.length ; i ++){
            if (identifiersArr[i] != null ){
                if (distinctArr[i] != null ){
                    if ( FLAG1 == false ){
                        if (queryCategory == null){
                            queryCategory = "distinct";
                        }
                        else{
                            queryCategory = "mixed";
                            return;
                        }
                        FLAG1 = true;
                    }
                }
                else{
                    if ( FLAG2 == false ){
                        if (queryCategory == null){
                            queryCategory = "ranges";
                        }
                        else{
                            queryCategory = "mixed";
                            return;
                        }
                        FLAG2 = true;
                    }
                }
            }
        }
    }*/
    
    public Results executeQueries(){
        //System.out.println("Query Category = " + queryCategory);
        calculateNonAnonymized();
        calculateAnonymized();
        
        
        //System.out.println("i am hereeeeee");
        return results;
    }
    
    public void calculateNonAnonymized(){
        boolean FLAG = true;  
        String valueStr;
        Hierarchy h = null;
        int []diff = new int[identifiersArr.length];
        
        
        //non-Anonymized Statistics
        for(int row = 0 ; row < nonAnonymized.length; row++){
            FLAG = true;
            for( int column = 0 ; column < identifiersArr.length ; column ++){
                if (identifiersArr[column] != null){
                    if (distinctArr[column] == null ){
                        if (nonAnonymized[row][column] != 2147483646.0 && !Double.isNaN(nonAnonymized[row][column])){
                            if (minArr[column] <= nonAnonymized[row][column] && maxArr[column] >= nonAnonymized[row][column] ){
                                if (FLAG != true ){
                                    nonAnonymizeOccurrences = 0;
                                    FLAG = false;
                                    break;
                                }
                            }
                            else{
                                FLAG = false;
                                break;
                            }    
                        }
                        else{
                            FLAG = false;
                            break;
                        }
                    }
                    else{
                        if (columnTypes.get(column).equals("string")){
                            dict = data.getDictionary(column);
                            valueStr = dict.getIdToString((int)nonAnonymized[row][column]);
                            if(valueStr.equals(distinctArr[column])){
                                if (FLAG != true ){
                                    nonAnonymizeOccurrences = 0;
                                    FLAG = false;
                                    break;
                                }
                            }
                            else{
                                FLAG = false;
                                break;
                            }  
                        }

                        else{
                            if (nonAnonymized[row][column] != 2147483646.0 && !Double.isNaN(nonAnonymized[row][column])){
                                if (Double.parseDouble(distinctArr[column]) == nonAnonymized[row][column]){
                                    if (FLAG != true ){
                                        nonAnonymizeOccurrences = 0;
                                        FLAG = false;
                                        break;
                                    }
                                }
                                else{
                                    FLAG = false;
                                    break;
                                }    
                            }
                            else{
                                FLAG = false;
                                break;
                            }
                        }               
                    }
                }
            }
            if (FLAG == true){
                nonAnonymizeOccurrences++;
            }
        } 

        System.out.println("nonAnonymizeOccurrences = " + nonAnonymizeOccurrences);
        results.setNonAnonymizeOccurrences(nonAnonymizeOccurrences +"");
    }
    
    
    public void calculateAnonymized(){
        boolean FLAG = true;
        boolean FLAG2= true;
        String valueStr = null;
        Hierarchy h = null;
        double []sumOfChildren = new double[identifiersArr.length];
        int []diff = new int[identifiersArr.length];
        boolean FLAGQueryGreaterHier = false;
        //Object dataObjAnon = null;
        String dataAnon = null;
        String []temp = null;
        String del = " - ";
        Map <Integer,DictionaryString> dictionaries = null;
        boolean EqualFLAG = false;
        
        //System.out.println("calculate Anonymize");
        
        Object value = null;//preprocessing about distinct values
        newDistinctArr = new String[distinctArr.length];
        for ( int i = 0 ; i < identifiersArr.length ; i++ ){
            if ( identifiersArr[i] != null ){
                if ( distinctArr[i] != null){
                    h = quasiIdentifiers.get(i);
                    if ( h != null ){
                        if (columnTypes.get(i).equals("string")){
                            value = distinctArr[i];
                        }
                        else{
                            value = Double.parseDouble(distinctArr[i]);    
                        }
                        
                        if ( h.getLevel(value) != null){

                            if(h.getLevel(value) == (h.getHeight()-1-hierarchyLevel[i]) ){//erwisi idio epipedo me tin hierarchia...to tupos deksia einai epeidi i ierarchies arxikopoipountai apo panw pros ta katw enw efarmozontai anapoda
                                //out.println("11111111111111111");
                                //estimatedRate = 0.0; 
                                newDistinctArr[i] = value +"";
                            }
                            else if (h.getLevel(value) > (h.getHeight()-1-hierarchyLevel[i]) ){//i erwtisi pio xamila apo tin hierarchia
                                //out.println("22222222222222222");
                                diff[i] =  h.getLevel(value) - (h.getHeight()-1-hierarchyLevel[i]) ;
                                int sumOfChildrenValue = h.findAllChildren(value, 0);

                                for (int j = 0 ; j < diff[i] ; j ++ ){
                                    value = h.getParent(value);
                                }
                                newDistinctArr[i] = value +"";

                            }
                            else if (h.getLevel(value) < (h.getHeight()-1-hierarchyLevel[i])){// i erwtisi pio psila apo tin hierarchia
                               // System.out.println("333333333333333");
                                //System.out.println("root = " + h.getLevel(value) );
                                if (h.getLevel(value) != 0){//i erwtisi sto root
                                    FLAGQueryGreaterHier = true;
                                    diff[i] = (h.getHeight()-1-hierarchyLevel[i])- h.getLevel(value) ;
                                }
                                else{
                                    FLAGQueryGreaterHier = true;
                                    diff[i] = (h.getHeight()-1-hierarchyLevel[i])- h.getLevel(value) ;                             
                                }
                                 //newDistinctArr[i] = value +"";
                            }
                        }
                        else{
                            if (columnTypes.get(i).equals("string")){
                            value = distinctArr[i];
                            }
                            else{
                                value = Double.parseDouble(distinctArr[i]);    
                            }    
                            newDistinctArr[i] = value +"";
                        }
                    }
                    else{
                        if (columnTypes.get(i).equals("string")){
                            value = distinctArr[i];
                        }
                        else{
                            value = Double.parseDouble(distinctArr[i]);    
                        }    
                        newDistinctArr[i] = value +"";
                    }
                }
            }
        }
        
        /*System.out.println("newDistinctArr");
        for( int i = 0 ; i < newDistinctArr.length ; i ++){
            System.out.println(newDistinctArr[i]);
        }
        System.out.println("end");*/
        
        dataset = data.getDataSet();
        String recordStr= null;
        for (int row = 0; row < dataset.length ; row++){
            FLAG  = false;
            recordStr= null;
            for ( int column = 0 ; column < identifiersArr.length ; column++){
                if (identifiersArr[column] != null){ // exei erwtisei gia auto
                    if (quasiIdentifiers.containsKey(column)){
                        if (hierarchyLevel[column] != 0){
                        
                            if (distinctArr[column] != null){//distinct
                                if (columnTypes.get(column).equals("string")){//distinct string
                                    
                                    dictionaries = data.getDictionary();
                                    DictionaryString dictionary = dictionaries.get(column);
                                    Double d = (Double)dataset[row][column];
                                    String newValue = dictionary.getIdToString(d.intValue());
                                    h = quasiIdentifiers.get(column);
                                    for ( int i = 0 ; i < hierarchyLevel[column] ; i ++){
                                        newValue = (String) h.getParent(newValue);
                                    }
                                    
                                    if (FLAG == false){
                                        recordStr = newValue +"";
                                        FLAG = true;
                                    }
                                    else{
                                        recordStr = recordStr + "," + newValue; 
                                    
                                    }
                                }
                                else{//distinct double
                                    Double newValue = dataset[row][column];
                                    if (nonAnonymized[row][column] != 2147483646.0 && !Double.isNaN(nonAnonymized[row][column])){
                                        h = quasiIdentifiers.get(column);
                                        for ( int i = 0 ; i < hierarchyLevel[column] ; i ++){
                                            newValue = (Double) h.getParent(newValue);
                                        }
                                    }
                                    
                                    if (FLAG == false){
                                        recordStr = newValue +"";
                                        FLAG = true;
                                    }
                                    else{
                                        recordStr = recordStr + "," + newValue; 
                                    
                                    }
                                }
                                //System.out.println("recordStr = " + recordStr);
                            }
                            else{//ranges
                                //System.out.println("rangeeeeeeeeees");
                                Double newValue = dataset[row][column];
                                RangeDouble r = null;
                                if (nonAnonymized[row][column] != 2147483646.0 && !Double.isNaN(nonAnonymized[row][column])){
                                    h = quasiIdentifiers.get(column);
                                    for ( int i = 0 ; i < hierarchyLevel[column] ; i ++){
                                        if ( i == 0 ){
                                            r = (RangeDouble) h.getParent(newValue);
                                        }
                                        else{
                                            r = (RangeDouble) h.getParent(r);
                                        }
                                        //System.out.println("rrrr = " + r.toString());
                                    }
                                }

                                if (FLAG == false){
                                    recordStr = r.toString() +"";
                                    FLAG = true;
                                }
                                else{
                                    recordStr = recordStr + "," + r.toString(); 

                                }
                            }
                        }
                    }
                    else{
                        if (distinctArr[column] != null){
                            if (columnTypes.get(column).equals("string")){
                                dictionaries = data.getDictionary();
                                DictionaryString dictionary = dictionaries.get(column);
                                Double d = (Double)dataset[row][column];
                                String newValue = dictionary.getIdToString(d.intValue());
                                
                                if (FLAG == false){
                                    recordStr = newValue +"";
                                    FLAG = true;
                                }
                                else{
                                    recordStr = recordStr + "," + newValue; 

                                }
                            }
                            else{
                                if (FLAG == false){
                                    recordStr = dataset[row][column] +"";
                                    FLAG = true;
                                }
                                else{
                                    recordStr = recordStr + "," + dataset[row][column]; 

                                }
                            }
                        }
                        /*else{//range
                            System.out.println("range");
                            if (FLAG == false){
                                recordStr = minArr[column] +"-" +maxArr[column] +"";
                                FLAG = true;
                            }
                            else{
                                recordStr = recordStr + "," + minArr[column] +"-" +maxArr[column]; 

                            }
                        }*/
                    }
                }
                else{
                    if (FLAG == false){
                        recordStr = "null" +"";
                        FLAG = true;
                    }
                    else{
                        recordStr = recordStr + "," + "null"; 

                    }
                }
            }
            //edw tha mpoun oi sugkriseis
            
            
            
            
            
            /*temp = null;
            del = ",";
            temp = recordStr.split(del);
            EqualFLAG = false;
            for ( int i = 0 ; i < newDistinctArr.length ; i++){
                if (newDistinctArr[i] != null ){ // einai distinct
                    if (columnTypes.get(i).equals("string")){//einai distinct string
                        
                    }
                    else{ // einai distinct double
                        if ( Double.parseDouble(newDistinctArr[i]) != Double.parseDouble(temp[i]) ){
                            EqualFLAG = false;
                            break;
                        }
                        else{
                            EqualFLAG = true;
                        }
                    }
                    
                }
                else if ( minArr[i] != Double.NaN){ // einai range
                    
                }
            }
            
            
            //moli vgei tsekareis flag kai arxizeis kai metras tous counters
            if (EqualFLAG == true){
                anonymizedOccurrences ++;
            }
        }*/
            //System.out.println("record str = " + recordStr);
            
            
            temp = null;
            del = ",";
            temp = recordStr.split(del);
            FLAG = true;
            FLAG2 = true;
            estimatedRate = 0.0;
            int counter = 0;
            for( int column = 0 ; column < identifiersArr.length ; column ++){
                if (identifiersArr[column] != null){
                    if (distinctArr[column] != null){//distinct value
                        //dataObjAnon = anonymized.getValueAt(row, column +1);
                        dataAnon = temp[column];

                        if (columnTypes.get(column).equals("string")){
                            if ( FLAGQueryGreaterHier == false){

                                h = quasiIdentifiers.get(column);
                                if(!dataAnon.equals(distinctArr[column])){
                                    FLAG = false;
                                }

                                if (newDistinctArr[column] != null){
                                   // System.out.println("NewDistinct = " + newDistinctArr[column] + "\t dataAnnon = " + dataAnon );
                                    if (newDistinctArr[column].equals(dataAnon)){

                                        if ( h != null){
                                            //System.out.println(h.getLevel(distinctArr[column]) + "\t" + (h.getHeight()-1-hierarchyLevel[column]));
                                            if (h.getLevel(distinctArr[column]) > (h.getHeight()-1-hierarchyLevel[column])) {
                                                //System.out.println(" i aam hereeeeeeeeeeeeeeee");
                                                if (counter == 0){
                                                    int sumOfChildrenValue = h.findAllChildren(distinctArr[column], 0);
                                                    //System.out.println("sumOfChildren11111111111111111 = " + sumOfChildrenValue);
                                                    double prob = h.findAllChildren(newDistinctArr[column], 0);
                                                    //System.out.println("prob = " + prob);        
                                                    prob = sumOfChildrenValue/prob;
                                                    //System.out.println("prob22222222222 = " + prob);
                                                    estimatedRate = prob;
                                                }
                                                else{
                                                    int sumOfChildrenValue = h.findAllChildren(distinctArr[column], 0);
                                                   // System.out.println("sumOfChildren11111111111111111 = " + sumOfChildrenValue);
                                                    double prob = h.findAllChildren(newDistinctArr[column], 0);
                                                    //System.out.println("prob = " + prob);        
                                                    //System.out.println("sumOfChildren = " + sumOfChildrenValue + "\tprob = " + prob);
                                                    prob = sumOfChildrenValue/prob;
                                                    estimatedRate = estimatedRate*prob;
                                                   // System.out.println("prob22222222222 = " + prob);
                                                    //System.out.println("estimated = " + estimatedRate);
                                                }
                                            }
                                            else{
                                                if (counter == 0){
                                                    estimatedRate = 1.0;
                                                }
                                                else{
                                                    estimatedRate = estimatedRate*1.0;
                                                }
                                            }
                                        }
                                        else{
                                            if (counter == 0){
                                                estimatedRate = 1.0;
                                            }
                                            else{
                                                estimatedRate = estimatedRate*1.0;
                                            }
                                        } 
                                    }
                                    else{
                                        estimatedRate = 0.0;
                                        FLAG2 = false;
                                            //break;
                                    }   
                                }
                                else{
                                    estimatedRate = 0.0;
                                    FLAG2 = false;
                                    //break;
                                } 

                            }
                            else{
                                h = quasiIdentifiers.get(column);
                                String dataInTable = dataAnon;
                                for ( int i = 0 ; i < diff[column]; i++ ){
                                    dataInTable = (String)h.getParent(dataInTable);
                                }

                                if (!distinctArr[column].equals(dataInTable)){
                                    FLAG = false;
                                    //break;
                                }


                                if (distinctArr[column].equals(dataInTable)){
                                    if (counter == 0){
                                        estimatedRate = 1.0;
                                    }
                                    else{
                                        estimatedRate = estimatedRate*1.0;
                                    }

                                }
                                else{
                                    FLAG2 = false;
                                    estimatedRate = 0.0;
                                }  

                            }
                        }

                        else{
                            //dataObjAnon = anonymized.getValueAt(row, column+1);
                            dataAnon = temp[column];
                            if (!dataAnon.equals("(null)")){


                                //System.out.println(distinctArr[column] + "\t" +Double.parseDouble(dataAnon) + "");

                                if ( FLAGQueryGreaterHier == false){
                                    h = quasiIdentifiers.get(column);
                                    if (Double.parseDouble(distinctArr[column]) != Double.parseDouble(dataAnon)){

                                        FLAG = false;
                                        //break;
                                    } 

                                    if (newDistinctArr[column] != null){
                                        //System.out.println("value = " +Double.parseDouble(newDistinctArr[column]) +"\t" +Double.parseDouble(dataAnon) );
                                        if (Double.parseDouble(newDistinctArr[column]) == Double.parseDouble(dataAnon)){

                                               // System.out.println("i am hereeeeeeeeeeeeeeeeeeeeeeeee");
                                                //System.out.println("new = " + newDistinctArr[column]+"\told = " + distinctArr[column]);
                                                if ( h != null){
                                                    if (h.getLevel(Double.valueOf(distinctArr[column])) > (h.getHeight()-1-hierarchyLevel[column])) {
                                                        //System.out.println("distinct = " + distinctArr[column]+"\tnewdistinct = " + newDistinctArr[column] );
                                                        if (counter == 0){
                                                            int sumOfChildrenValue = h.findAllChildren(Double.parseDouble(distinctArr[column]), 0);
                                                            double prob = h.findAllChildren(Double.parseDouble(newDistinctArr[column]), 0);
                                                            //System.out.println("sumOfChildren = " + sumOfChildrenValue + "\tprob = " + prob);

                                                            prob = sumOfChildrenValue/prob;
                                                           //System.out.println("sumOfChildren = " + sumOfChildrenValue + "\tprob = " + prob +"\testimmated = " + estimatedRate);
                                                            estimatedRate = prob;
                                                        }
                                                        else{
                                                            int sumOfChildrenValue = h.findAllChildren(Double.parseDouble(distinctArr[column]), 0);
                                                            double prob = h.findAllChildren(Double.parseDouble(newDistinctArr[column]), 0);
                                                             //System.out.println("sumOfChildren = " + sumOfChildrenValue + "\tprob = " + prob);
                                                            prob = sumOfChildrenValue/prob;
                                                            estimatedRate = estimatedRate*prob;
                                                            //System.out.println("summmmmmmOfChildren = " + sumOfChildrenValue + "\tprob = " + prob +"\testimmated = " + estimatedRate);
                                                        } 
                                                    }
                                                    else{
                                                        if (counter == 0){
                                                        estimatedRate = 1.0;

                                                        }
                                                        else{
                                                            estimatedRate = estimatedRate*1.0;
                                                        }
                                                    }
                                                }
                                                else{
                                                    if (counter == 0){
                                                        estimatedRate = 1.0;

                                                    }
                                                    else{
                                                        estimatedRate = estimatedRate*1.0;
                                                    }
                                                } 
                                        }
                                        else{
                                            FLAG2 = false;
                                            estimatedRate = 0.0;
                                            //break;
                                        }   
                                    }
                                    else{
                                        FLAG2 = false;
                                        estimatedRate = 0.0;
                                         //break;
                                    }   

                                }
                                else{
                                    //System.out.println(distinctArr[column] + "\t" +Double.parseDouble(dataAnon));

                                    h = quasiIdentifiers.get(column);
                                    double dataInTable = Double.parseDouble(dataAnon);
                                    for ( int i = 0 ; i < diff[column]; i++ ){
                                        dataInTable = (double)h.getParent(dataInTable);
                                    }

                                    if (Double.parseDouble(distinctArr[column]) != dataInTable){
                                        FLAG = false;
                                        //break;
                                    }


                                    if (Double.parseDouble(distinctArr[column]) == dataInTable){

                                            if ( counter == 0){
                                                estimatedRate = 1.0;

                                            }
                                            else{
                                                estimatedRate = estimatedRate*1.0;
                                            }

                                    }
                                    else{
                                        FLAG2 = false;
                                        estimatedRate = 0.0;
                                        //break;
                                    }   
                                }

                            }
                            else{
                                FLAG = false;
                                FLAG2 = false;
                                estimatedRate = 0.0;
                                break;
                            }
                        }
                    }
                    else{//range value
                        //dataObjAnon = anonymized.getValueAt(row, column +1);
                        dataAnon = temp[column];
                        //System.out.println(dataAnon + ",");
                        temp = dataAnon.split("-");
                    
                        if (minArr[column] == Double.parseDouble(temp[0]) && maxArr[column] == Double.parseDouble(temp[1])){

                                if (counter==0 ){
                                    estimatedRate = 1.0;
                                    //estimatedRate = estimatedRate*1.0;
                                }
                                else {
                                     estimatedRate = estimatedRate*1.0;
                                }
                            //}
                            
                            //System.out.println("0000000 : " +  minArr[column] + ">= " + Double.parseDouble(temp[0]) +"\t" + maxArr[column] + " <= " + Double.parseDouble(temp[1])) ;
                            //propabilities[column] = propabilities[column] + 1.0;
                        }

                        else if (minArr[column] >= Double.parseDouble(temp[0]) && maxArr[column] >= Double.parseDouble(temp[1]) && Double.parseDouble(temp[1]) >= minArr[column] ){

                                FLAG = false;
                                double prob = (Double.parseDouble(temp[1]) - minArr[column])/(Double.parseDouble(temp[1]) - Double.parseDouble(temp[0]));
                                if ( prob != 0.0){
                                    //propabilities[column] = propabilities[column] + prob;
                                    //propabilities[column] = prob;
                                    
                                     if (minArr[column] <= Double.parseDouble(temp[0])){
                                        FLAG = true;
                                    }
                                    else{
                                        FLAG = false;
                                    }
                                     
                                    if ( counter ==0 ){
                                        estimatedRate = prob;
                                        //estimatedRate = estimatedRate*prob;
                                    }
                                    else{
                                        estimatedRate = estimatedRate*prob;
                                    }
                                }
                                else{
                                    FLAG2 = false;
                                    FLAG = false;
                                    //propabilities[column] = 0;
                                    estimatedRate = 0.0;
                                }

                            
                            System.out.println("11111111 : " +  minArr[column] + ">= " + Double.parseDouble(temp[0]) +"\t" + maxArr[column] + " >= " + Double.parseDouble(temp[1])) ;
                            //System.out.println(row + "," +column);
                            //propabilities[column] = propabilities[column] + 1.0;
                        }
                        else if (minArr[column] <= Double.parseDouble(temp[0]) && maxArr[column] >= Double.parseDouble(temp[1])){

                                if (counter ==0 ){
                                    estimatedRate = 1.0;
                                    //estimatedRate = estimatedRate*1.0;
                                }
                                else {
                                     estimatedRate = estimatedRate*1.0;
                                }

                            System.out.println( "2222222 :" + minArr[column] + "<= " + Double.parseDouble(temp[0]) +"\t" + maxArr[column] + " >= " + Double.parseDouble(temp[1])) ;

                        }

                        else if (minArr[column] <= Double.parseDouble(temp[0]) && maxArr[column] <= Double.parseDouble(temp[1]) && Double.parseDouble(temp[0]) <= maxArr[column]){
                            FLAG = false;
                                double prob = (maxArr[column] - Double.parseDouble(temp[0]))/(Double.parseDouble(temp[1]) - Double.parseDouble(temp[0]));
                                if ( prob != 0.0){

                                    if ( counter ==0 ){
                                        estimatedRate = prob;
                                        //estimatedRate = estimatedRate*prob;
                                    }
                                    else{
                                        estimatedRate = estimatedRate*prob;
                                    }
                                }
                                else{
                                    FLAG2 = false;
                                    FLAG = false;
                                    estimatedRate = 0.0;
                                }

                            
                            System.out.println( "333333 :" + minArr[column] + "<= " + Double.parseDouble(temp[0]) +"\t" + maxArr[column] + " <= " + Double.parseDouble(temp[1]) + "\tpossibility = " +(maxArr[column] - Double.parseDouble(temp[0]))/(Double.parseDouble(temp[1]) - Double.parseDouble(temp[0]))) ;

                        }
                        else if (minArr[column] >= Double.parseDouble(temp[0]) && maxArr[column] <= Double.parseDouble(temp[1])){
                            FLAG = false;
                            double prob = (maxArr[column] - Double.parseDouble(temp[0]))/(Double.parseDouble(temp[1]) - Double.parseDouble(temp[0]));
                            if ( prob != 0.0){
                                        //propabilities[column] = propabilities[column] + prob;
                                        //propabilities[column] = prob;
                                if ( counter ==0 ){
                                    estimatedRate = prob;
                                            //estimatedRate = estimatedRate*prob;
                                }
                                else{
                                    estimatedRate = estimatedRate*prob;
                                }
                            }
                            else{
                                FLAG2 = false;
                                FLAG = false;
                                        //propabilities[column] = 0;
                                estimatedRate = 0.0;
                            }
                        }
                        else{
                            FLAG = false;
                            FLAG2 =false;
                            estimatedRate = 0.0;
                        }
                    }
                    counter++;
                } 
            }
            if (FLAG == true){
                anonymizedOccurrences++;
            }
            if (FLAG2 == true){
                possibleOccurencences++;
            }
            System.out.println("estimated rate = " + estimatedRate);
            
            //if ( estimatedRate != 1.0){
                allEstimatedRate = allEstimatedRate +estimatedRate;
                System.out.println("allEstimated rate =" + allEstimatedRate);
                
            //}
            
            
        }
        
        System.out.println("aanonymizedOccurrences = " + anonymizedOccurrences);
        System.out.println("possibleOccurencences = " + possibleOccurencences);
        System.out.println("allEstimatedRate = " + allEstimatedRate);
        results.setAnonymizedOccurrences(anonymizedOccurrences+"");
        results.setPossibleOccurences(possibleOccurencences +"");
        
        results.setEstimatedRate(String.format ("%.3f", allEstimatedRate) +"");
    }
    
    
}

