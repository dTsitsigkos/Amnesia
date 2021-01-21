/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.mixedkmanonymity;

import algorithms.Algorithm;
import algorithms.flash.LatticeNode;
import algorithms.kmanonymity.Apriori;
import algorithms.kmanonymity.Combinations;
import algorithms.kmanonymity.SolutionCombinations;
import algorithms.kmanonymity.Trie;
import anonymizeddataset.AnonymizedDataset;
import data.Data;
import data.RelSetData;
import dictionary.DictionaryString;
import graph.Graph;
import hierarchy.Hierarchy;
import hierarchy.ranges.HierarchyImplRangesDate;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.util.Pair;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.scene.input.KeyCode.T;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author nikos
 */
public class MixedApriori implements Algorithm {
    RelSetData dataset = null;
    double[][] relationalData = null;
    double[][] setData = null;
    Map<Integer, Hierarchy> hierarchies = null;
    Hierarchy hierarchySet = null;
    Map<Set<Pair<Integer,Object>>,Integer> trieRelational = null;
    Map<Integer,Set<Double>> domainLeavesRelational = null;
    Map<Set<Double>,Integer> trieSet = null;
    int nextIndex = 0;
    Map<Integer,Integer> testGensSet = null;
    Map<Double,Integer[]> pointMapSet = null;
    Set<Double> domainLeavesSet = null;
    Map<Double,Double> costsSet = null;
    Set<Double> visitedSet = null;
    Set<Double>[] keysSet = null;
    
    Map<Pair<Integer,Object>,Object> testGensRelational = null;
    Map<Pair<Integer,Object>,Double> costsRelational = null;
    Map<Pair<Integer,Object>,Object[]> pointMapRelational;
    Map<Integer,Set<Double>> rangeLastLevel;
    Set<Pair<Integer,Object>> vistitedRelational = null;
    Set<Pair<Integer,Object>>[] keysRelational;
    int domainSize = -1;
    int k = -1;
    int m = -1;
    String anonymize_property = null; // set,relational,mixed\
    int setColumn;
    int numOfVisits=0;
    
    double[][] anonymize_set;
    Object[][] anonymize_relational;
    Map<Integer,Map<Object,Object>> rulesSetRealtioanl;
    
    List<Integer> columns;
    DictionaryString dict = null;

    @Override
    public void setDataset(Data dataset) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        this.dataset = (RelSetData) dataset;
        relationalData = this.dataset.getDataSet();
        setData = this.dataset.getSet();
        setColumn = this.dataset.getSetColumn();
    }

    @Override
    public void setHierarchies(Map<Integer, Hierarchy> hierarchies) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        this.hierarchies = hierarchies;
        
    }
    
    public void setSetData(double[][] setData){
        this.setData = setData;
    }
    
    public void setDictionary(DictionaryString d){
        this.dict = d;
    }

    @Override
    public void setArguments(Map<String, Integer> arguments) {
        this.k = arguments.get("k");
        this.m = arguments.get("m");
    }
    
    public void setDataTable(double[][] table){
        this.relationalData = table;
    }
    
    public String checkAnonymitySet(int k, int m){
        String warning_msg="";
        this.k = k;
        this.m = m;
        Set<Double> combTrie;
        
        this.anonymize_property="set";
        for(int i=1; i<=m; i++){
            List<Double> transaction = null;
            Set<double[]> combinations;
            this.trieSet = new HashMap();
            while((transaction = this.getNextTransactionSetTest())!=null){
                combinations = Combinations.getSimpleCombinations(new HashSet(transaction), i);
//                System.out.println("Transaction "+transaction);
                if(combinations!=null){
                    for(double[] combArr : combinations ){
    //                        System.out.println("Combination "+Arrays.toString(comb.toArray()));
                        Set<Double> comb = new HashSet();
                        for(double combElm : combArr){
                            comb.add(combElm);
                        }

                        if(trieSet.containsKey(comb)){
                            trieSet.put(comb, trieSet.get(comb)+1);
                        }
                        else{
                            trieSet.put(comb, 1);
                        }
                    }
                }
            }
            this.numOfVisits =0;
            this.sortTrie();
            while((combTrie=this.preorderNextSet())!=null && this.trieSet.get(combTrie)<k){
                String combs ="";
                if(dict==null){
                    combs += ""+combTrie;
                }
                else{
                    for(Double comb : combTrie){
                        if(dict.getIdToString().containsKey(comb.intValue())){
                            combs += dict.getIdToString(comb.intValue())+", ";
                        }
                        else{
                            combs += comb+" ";
                        }
                    }
                }
                warning_msg += "Combination "+combs+" has support "+this.trieSet.get(combTrie)+"\n\"<br>\"";
            }
            trieSet.clear();
            this.keysSet = null;
        }
        
        if(warning_msg.equals("")){
            warning_msg = "Dataset is anonymmized";
        }
        return warning_msg;
    }
    
    public String checkAnonymity(int k, int m, List<Integer> cols,int setCol){
        this.k = k;
        this.m = m;
        this.columns = cols;
        System.out.println("Columns "+cols);
        this.setColumn = cols.contains(setCol) ? setCol : -1;
        String warning_msg="";
        
        for(int i=1; i<=m; i++){
            List<Pair<Integer,Object>> transaction = null;
            Set <Set<Pair<Integer,Object>>> combinations;
            if(setCol==-1){
                this.anonymize_property = "relational";
            }
            else{
                this.anonymize_property = "mixed";
            }
            
            trieRelational = new HashMap();
            System.out.println("test m= "+i);
            while((transaction =  this.getNextTransactionTest())!=null){
//                System.out.println("Transaction "+transaction);
                combinations = MixedCombinations.getSimpleCombinations(new HashSet(transaction), i);
                if(combinations!=null){
                    for(Set<Pair<Integer,Object>> comb : combinations ){
                        System.out.println("Combination Test"+Arrays.toString(comb.toArray()));
                        if(trieRelational.containsKey(comb)){
                            trieRelational.put(comb, trieRelational.get(comb)+1);
                        }
                        else{
                            trieRelational.put(comb, 1);
                        }
                    }
                }
            }
            this.numOfVisits =0;
            this.sortTrie();
            this.keysRelational = (Set<Pair<Integer,Object>>[]) trieRelational.keySet().toArray(new HashSet[trieRelational.size()]);
            Set<Pair<Integer,Object>> combTrie;
            while((combTrie=this.preorderNextRelational())!=null && this.trieRelational.get(combTrie)<this.k){
                String combs="";
                
                if(dict==null){
                    combs += combTrie;
                }
                else{
                    for(Pair<Integer,Object> comb : combTrie){
                        if(comb.getValue() instanceof Double){
                            if(dict.getIdToString().containsKey(((Double)comb.getValue()).intValue())){
                                combs += comb.getKey()+"="+dict.getIdToString().get(((Double)comb.getValue()).intValue())+", ";
                            }
                            else{
                                combs += ""+comb+", ";
                            }
                        }
                        else{
                            combs += ""+comb+", ";
                        }
                    }
                }
                
                warning_msg += "Combination "+combs+" has support "+this.trieRelational.get(combTrie)+"\n\"<br>\"";
            }
            
            this.trieRelational.clear();
           
        }
        
        System.out.println("Returned "+warning_msg);
        if(warning_msg.equals("")){
            warning_msg = "Dataset is anonymmized";
        }
        return warning_msg;
    }
    
    private List<Pair<Integer,Object>>  getNextTransactionTest(){
        List<Pair<Integer,Object>> transaction = null;
        double[] originalTransaction = null;
        if(nextIndex == relationalData.length){
            nextIndex = 0;
            return null;
        }
        
        transaction = new ArrayList<Pair<Integer,Object>>();
        
        if(this.setColumn==-1){
            originalTransaction = relationalData[nextIndex];

            for(int i=0; i<originalTransaction.length; i++){
                if(this.columns.contains(i)){
                    Pair<Integer,Object> value = this.convertToPair(i, originalTransaction[i]);
                    transaction.add(value); 

                }
            }
        }
        else{
            originalTransaction = new double[relationalData[nextIndex].length+setData[nextIndex].length];
            System.arraycopy(relationalData[nextIndex], 0, originalTransaction, 0, relationalData[nextIndex].length);
            System.arraycopy(setData[nextIndex], 0, originalTransaction, relationalData[nextIndex].length, setData[nextIndex].length);
            
            for(int i=0; i<originalTransaction.length; i++){
                Pair<Integer,Object> value=null;
                if(i>=relationalData[nextIndex].length){
                    value = this.convertToPair(this.setColumn, originalTransaction[i]);
                }
                else{
                    if(i!=this.setColumn && this.columns.contains(i))
                        value = this.convertToPair(i, originalTransaction[i]); 
                }
                if(value!=null)
                    transaction.add(value);
            }
            
        }
        nextIndex++;
        return transaction;
    }
    
    private List<Double> getNextTransactionSetTest(){
        List<Double> transaction = null;
        double[] originalTransaction = null;
        if(nextIndex == setData.length){
            nextIndex = 0;
            return null;
        }
        
        transaction = new ArrayList<Double>();
        
        
        originalTransaction = setData[nextIndex];
        
        for(int i=0; i<originalTransaction.length; i++){
           
            Pair<Integer,Object> value = this.convertToPair(i, originalTransaction[i]);
            transaction.add(originalTransaction[i]);    
        }
        nextIndex++;
        return transaction;
    }

    @Override
    public void anonymize() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        for(int i=0; i<3; i++){
            switch(i){
                case 0:                             /// set
                    anonymize_property = "set";
                    break;
                case 1:                            //// relational
                    anonymize_property = "relational";
                    break;
                case 2:                            //// mixed
                    anonymize_property = "mixed";
                    break;
            }
            createInternalStructures();
            for(int j=1; j<=m; j++){
                System.out.println(this.anonymize_property+" m = " + j);
                populateTree(j);
                System.out.println("ok with tree "+this.anonymize_property+" m = "+j);
                sortTrie();
                System.out.println("ok with sort "+this.anonymize_property+" m = "+j);
                if(this.anonymize_property.equals("set")){
//                    for(Entry<Set<Double>,Integer> entry : this.trieSet.entrySet()){
//                        System.out.println(this.anonymize_property+Arrays.toString(entry.getKey().toArray())+" support "+entry.getValue());
//                    }
                }
                else{
//                    for(Entry<Set<Pair<Integer,Object>>,Integer> entry : this.trieRelational.entrySet()){
//                        System.out.println(this.anonymize_property+Arrays.toString(entry.getKey().toArray())+" support "+entry.getValue());
//                    }
                }
                fixAll();  
                System.out.println("ok with fix "+this.anonymize_property+" m = "+j);
            }
            
            
        }
        
        for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
            entry.getValue().clearAprioriStructures();
        }
    }
    
    /////////////// SET /////////////////////////////////
    
    private Set<Double> getTranslateTransactionSet(Set<Double> transaction){
        Set<Double> translated_transaction = new HashSet();
        for(Double value : transaction){
            translated_transaction.add(this.getTranslation(value));
        }
        
        return translated_transaction;
    }
    public double getAddedCost(double[] comb, List<Double> base){
        double existingCost = 0;
        double newCost = 0;
        
        resetTestGens();
        
        for(int i = 0; i<comb.length; i++){
            double anonItem = comb[i];
            if(anonItem != -1 && (anonItem != base.get(i))){
                generalizeTest(anonItem);
            }
        }
        
        existingCost = getTotalCost();
        newCost = getTestCost();
//        System.out.println("exist cost Mixed Set"+existingCost+" test Cost ");
        return (newCost - existingCost);
    }
    
    private void generalizeTest(double generalized) {
        
        int l = getLevel(generalized);

        if(l == 0 || this.domainLeavesSet.contains(generalized)){
            return;
        }

        Set<Double> children = this.hierarchySet.getChildrenIds(generalized);
        if(children != null){
            if (l == 1){
                for(Double child : children){
                    testGensSet.put(child.intValue(),(int)generalized);
                }
            }
            else{
                for(Double child : children){
                    genTest(child, generalized);
                }
            }
        }
        
    }
    
    
    private void genTest(double o, double g){
//        System.out.println("genTest gen Test gen Test");
        
        if(getLevel(o) == 0 || domainLeavesSet.contains(o)){
            testGensSet.put((int)o, (int)g);
        }
        else{
            Set<Double> children = this.hierarchySet.getChildrenIds(o);
            if(children != null){
                for(Double child : children){
                    genTest(child, g);
                }
            }
        }
    }
    
    private Double getHighestLevelSet(Set<Double> comb){
        int level=0;
        Double value=null;
        for(Double val : comb){
            if(level < this.hierarchySet.getLevel(val)){
                value = val;
                level = this.hierarchySet.getLevel(val);
            }
        }
        if(value==null){
            value = (Double) comb.toArray()[0];
        }
        return value;
    }
    
    private void fixSet(List<Double> prefix){
        Set<Double> domain = expandTransactionListSet(prefix);
        
        if(domain.size() > 1){
           Map<Double, double[]> results = new TreeMap<>();
           SolutionCombinations solCombs = new SolutionCombinations(hierarchySet, trieSet, k, this);
           solCombs.createSolutionCombs(results, prefix);
            
            for(Entry<Double, double[]> entry : results.entrySet()){
                double[] anonPath = entry.getValue();
//                System.out.println("AnonPath : "+Arrays.toString(anonPath));
                Set<Double>  temp = new HashSet();
                for(int i=0; i<anonPath.length; i++){
                    temp.add(anonPath[i]);
                }
//                System.out.println("AnonPath support "+trieSet.get(temp));
                for(int i=0; i<anonPath.length; i++){
                    double anonItem = anonPath[i];
                    if((anonItem != -1) && (anonItem != prefix.get(i)) && anonItem == this.getTranslation(anonItem) && !this.visitedSet.contains(anonItem)){
//                        System.out.println("General anon : "+this.hierarchySet.getDictionary().getIdToString((int)anonItem)+" prefix "+this.hierarchySet.getDictionary().getIdToString(prefix.get(i).intValue()) /*== null ? this.dataset.getDictionary().getIdToString(prefix.get(i).intValue()) : this.hierarchySet.getDictionary().getIdToString(prefix.get(i).intValue())*/);
//                        System.out.println("General anon : "+this.hierarchySet.getDictionary().getIdToString((int)anonItem)+" prefix "+this.hierarchySet.getDictionary().getIdToString(prefix.get(i).intValue()));
//                        if(this.hierarchySet.getDictionary().getIdToString((int)anonItem)==null){
//                            System.out.println("General anon null previous : "+this.dataset.getDictionary().getIdToString((int)anonItem)+" prefix "+this.dataset.getDictionary().getIdToString(prefix.get(i).intValue()));
//                        }
//                        if(this.hierarchySet.getDictionary().getIdToString(prefix.get(i).intValue())==null){
//                            System.out.println("General anon null previous prefix: "+this.hierarchySet.getDictionary().getIdToString((int)anonItem)+" prefix "+this.dataset.getDictionary().getIdToString(prefix.get(i).intValue()));
//                        }
                        generalize(anonItem);
                        visitedSet.add(anonItem);
                    }
                }
                break;
            }
        }
    }
    
    private void generalize(double generalized){
        int l = getLevel(generalized);
        if(l == 0)
            return;
        
        Set<Double> children = this.hierarchySet.getChildrenIds(generalized);
        if(children != null){
            if(l == 1){
                for(Double child : children){
                    Integer[] pointMapArr = pointMapSet.get(child);
                    pointMapArr[1] = (int)generalized;
//                    pointMapArr[2] = getLevel(generalized);
                    pointMapSet.put(child,pointMapArr);
                }
            }else{
                for(Double child : children){
                    Integer[] pointMapArr = pointMapSet.get(child);
                    pointMapArr[1] = (int)generalized;
//                    pointMapArr[2] = getLevel(generalized);
                    pointMapSet.put(child,pointMapArr);
                    gen(child, generalized);
                }
            }
        }
    }
    
    private void gen(double o, double g){
        if(getLevel(o) == 0){
            Integer[] pointMapArr = pointMapSet.get(o);
            pointMapArr[1] = (int)g;
//            pointMapArr[2] = getLevel(g);
            pointMapSet.put(o,pointMapArr);
        }
        else{
            Set<Double> children = this.hierarchySet.getChildrenIds(o);
            if(children != null){
                for(Double child : children){
                    Integer[] pointMapArr = pointMapSet.get(child);
                    pointMapArr[1] = (int)g;
//                    pointMapArr[2] = getLevel(g);
                    pointMapSet.put(child,pointMapArr);
                    gen(child, g);
                }
            }
        }
    }
    
    private int getLevel(double point){
        return this.hierarchySet.getLevel(point);
    }
    
    private Set<Double> preorderNextSet(){
        
        if(numOfVisits != trieSet.size()){
            Set<Double> return_comb;
            if(this.keysSet==null){
              this.keysSet =  (Set<Double>[]) this.trieSet.keySet().toArray(new HashSet[trieSet.size()]);  
            }
//            
            return_comb =  this.keysSet[numOfVisits];
            if(trieSet.get(return_comb) >= k){
                return null;
            }
            else{
                numOfVisits++;
                return return_comb;
            }
        }
        else {
            return null;
        }
    }
    
    private double[] getNextTransactionSet(){
        double[] anonTransaction = null;
        double[] originalTransaction = null;
        if(nextIndex == setData.length){
            nextIndex = 0;
            return null;
        }
        originalTransaction = setData[nextIndex];
        anonTransaction = new double[originalTransaction.length];
        for(int i=0; i<originalTransaction.length; i++){
            anonTransaction[i] = getTranslation(originalTransaction[i]);
        }
        nextIndex++;
        
        return anonTransaction;
    }
    
    
    private double getCost(Double n){
        if(costsSet.containsKey(n)){
            return costsSet.get(n);
        }
        else{
            return 0.0;
        }
    }
    
    private Set<Double> expandTransaction(double[] transaction){
        Set<Double> expanded = new TreeSet<>(Collections.reverseOrder());
        for(double item : transaction){
            expanded.add(item);
            getAllGeneralizations(item, expanded);
        }
        
        return expanded;
    }
    
    private Set<Double> expandTransactionListSet(List<Double> transaction){
        Set<Double> expanded = new TreeSet<>(Collections.reverseOrder());
        for(double item : transaction){
            expanded.add(item);
            getAllGeneralizations(item, expanded);
        }
        
        return expanded;
    }
    
    private void getAllGeneralizations(double current, Set<Double> result){
        double temp = hierarchySet.getParentId((int)current);
        while(temp != -1){
            result.add(temp);
            temp = hierarchySet.getParentId((int)temp);
        }
    }
    
    private double getTranslation(double point){
//        System.out.println("point "+point+" "+dataset.getDictionary().getIdToString((int)point)+" Hier "+hierarchySet.getDictionary().getIdToString((int)point));
        if(pointMapSet.get(point) != null)
            return pointMapSet.get(point)[1].doubleValue();
        else{
            return Double.NaN;
        }
    }
     
     
    
    //////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    ///////////////////// SET RELATIONAL MIXED //////////////////////////////////////////
    
    public void populateTree(int size){
        this.numOfVisits = 0;
        if(this.anonymize_property.equals("set")){
            double[] transaction = null;
            Set <double[]> combinations;
            trieSet.clear();
            while((transaction = getNextTransactionSet()) != null){
//                if(size==2){
//                    System.out.println("Transaction "+Arrays.toString(transaction));
//                }
                Set<Double> expandedTransaction = expandTransaction(transaction);
//                if(size==2){
//                    System.out.println("expanded Transaction "+expandedTransaction);
//                }
                combinations = Combinations.getCombinations(expandedTransaction, size, this.hierarchySet);
                
//                if(size==2)
//                {
//                    System.out.println("Combinations "+combinations);
//                }
                if(combinations!=null && !combinations.isEmpty()){
                    for(double[] comb : combinations){
//                        Set<Double> setComb = new HashSet<Double>();
//                        for(int i=0; i<comb.length; i++){
//                            setComb.add(comb[i]);
//                        }
                        Double[] doubleArray = ArrayUtils.toObject(comb);
//                        Set<Double> setComb = Arrays.stream(doubleArray).collect(Collectors.toCollection(HashSet::new));
                        Set<Double> setComb = Arrays.stream(doubleArray).collect(Collectors.toSet());
//                        Set<Double> setComb = new HashSet<Double>(Arrays.asList(doubleArray));
                        if(trieSet.containsKey(setComb)){
                            trieSet.put(setComb, trieSet.get(setComb)+1);
                        }
                        else{
                            trieSet.put(setComb,1);
                        }
                    }
                }
            }
        }
        else if(this.anonymize_property.equals("relational") || this.anonymize_property.equals("mixed")){
            List<Pair<Integer,Object>> transaction = null;
            Set <Set<Pair<Integer,Object>>> combinations;
            trieRelational.clear();
            while((transaction =(this.anonymize_property.equals("relational") ? getNextTransactionRelational() : getNextTransactionMixed())) != null){
//                System.out.println("TRansaction "+Arrays.toString(transaction.toArray()));
                Set<Pair<Integer,Object>> expandedTransaction = expandTransaction(transaction);
//                System.out.println("Expaned TRansaction "+Arrays.toString(expandedTransaction.toArray()));
                combinations = MixedCombinations.getCombinations(expandedTransaction, size, hierarchies);

                 if (combinations != null && !combinations.isEmpty()){
                    for(Set<Pair<Integer,Object>> comb : combinations ){
//                        System.out.println("Combination "+Arrays.toString(comb.toArray()));
                        if(this.anonymize_property.equals("relational")){
                            putTrie(comb);
                        }
                        else if(!containsOnlySet(comb)){
    //                      System.out.println("Combination "+Arrays.toString(comb.toArray()));
                            putTrie(comb);
                            
                        }
                    }
                }
            }
        }
//        else{
//            List<Pair<Integer,Object>> transaction = null;
//            Set <Set<Pair<Integer,Object>>> combinations;
////            if(size==1)
//            trieRelational.clear();
//            while((transaction = getNextTransactionMixed()) != null){
////                System.out.println("Transaction "+transaction);
//                Set<Pair<Integer,Object>> expandedTransaction = expandTransaction(transaction);
////                System.out.println("Expanded mixed "+expandedTransaction);
//                combinations = MixedCombinations.getCombinations(expandedTransaction, size, hierarchies);
//                
//
//                 if (combinations != null && !combinations.isEmpty()){
//                    for(Set<Pair<Integer,Object>> comb : combinations ){
//                        if(!containsOnlySet(comb)){
////                            System.out.println("Combination "+Arrays.toString(comb.toArray()));
//                            if(trieRelational.containsKey(comb)){
//                                trieRelational.put(comb, trieRelational.get(comb)+1);
//                            }
//                            else{
//                                trieRelational.put(comb, 1);
//                            }
//                        }
//                    }
//                }
//            }
//        }
       
    }
    
    private void fixAll(){
        if(this.anonymize_property.equals("set")){
            Set<Double> combNode;
            System.out.println("Size "+trieSet.size());
            this.keysSet = (Set<Double>[]) trieSet.keySet().toArray(new HashSet[trieSet.size()]);
            visitedSet.clear();
            while((combNode = preorderNextSet()) != null){
//                Double nodeValue = getHighestLevelSet(combNode);
//                if(nodeValue != this.getTranslation(nodeValue)){
//                    continue;
//                }
                
                Set<Double> translated_comb = this.getTranslateTransactionSet(combNode);
                if(!trieSet.containsKey(translated_comb) || trieSet.get(translated_comb) >= k)
                        continue;
                
                if(trieSet.get(combNode) < k){
//                    System.out.println("Problematic combination "+combNode+" support "+trieSet.get(combNode));
//                    System.out.println("Translated Comb "+translated_comb+" support "+trieSet.get(translated_comb));
                    List<Double> itemset = new ArrayList<Double>(combNode);
                    fixSet(itemset);
                    
                }
            }
        }
        else if(this.anonymize_property.equals("relational") || this.anonymize_property.equals("mixed")){
            Set<Pair<Integer,Object>> combNode;
            this.vistitedRelational.clear();
            this.keysRelational = (Set<Pair<Integer,Object>>[]) trieRelational.keySet().toArray(new HashSet[trieRelational.size()]);
            while((combNode = preorderNextRelational()) != null){
//                for(Pair<Integer,Double> nodeValue : combNode){
                
//                Pair<Integer,Double> nodeValue = getHighestLevelRelational(combNode);
                Set<Pair<Integer,Object>> translated_comb = this.getTranslateTransactionRelational(combNode);
//                System.out.println("General fix rel"+Arrays.toString(translated_comb.toArray())+" and comb "+combNode+" k = "+trieRelational.get(translated_comb));
                    if(!trieRelational.containsKey(translated_comb) || trieRelational.get(translated_comb) >= k)
                        continue;

                    if(trieRelational.get(translated_comb) < k){
//                        System.out.println("Run fix rel"+Arrays.toString(combNode.toArray()));
                        List<Pair<Integer,Object>> itemset = new ArrayList<Pair<Integer,Object>>(translated_comb);
                        fixRelational(itemset);
                    }
//                }
            }
        }
//        else{
//           Set<Pair<Integer,Object>> combNode; 
//           while((combNode = preorderNextRelational()) != null){
//               
//           }
//        }
    }
    
    
     private void resetTestGens(){ /// if set relational 
        if(this.anonymize_property.equals("set")){ 
//            List<Integer> leavesIds = this.hierarchySet.getNodeIdsInLevel(0);
            
            for(Double leafId : domainLeavesSet){
                testGensSet.put(leafId.intValue(), pointMapSet.get(leafId.doubleValue())[1]);
            }
        }
        else if(this.anonymize_property.equals("relational")|| this.anonymize_property.equals("mixed")){
            for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
                if(entry.getKey()!=this.setColumn || this.anonymize_property.equals("mixed")){
                    List<Object> leavesIds;
                    Hierarchy hierarchy = this.hierarchies.get(entry.getKey());
                    if(hierarchy.getHierarchyType().equals("range")){
                        leavesIds = new ArrayList(this.rangeLastLevel.get(entry.getKey()));
                    }
                    else{
                        leavesIds = new ArrayList(this.domainLeavesRelational.get(entry.getKey()));
                    }
                    
                    for(Object leafId : leavesIds){
                        Pair<Integer,Object> leaf  = this.convertToPair(entry.getKey(), leafId);
                        testGensRelational.put(leaf, pointMapRelational.get(leaf)[1]);
                    }
                }
            }
        }
    }
     
     
    private void putTrie(Set<Pair<Integer,Object>> comb){
        if(trieRelational.containsKey(comb)){
            trieRelational.put(comb, trieRelational.get(comb)+1);
        }
        else{
            trieRelational.put(comb, 1);
        }
    }
    
    private double getTotalCost() { /// if set relational 
        double res = 0;
        int count=0;
        if(this.anonymize_property.equals("set")){
//            List<Integer> domainLeaves = this.hierarchySet.getNodeIdsInLevel(0);
            for(Double leafId : domainLeavesSet){
                int gen_val = pointMapSet.get(leafId.doubleValue())[1];
                res += costsSet.get((double)gen_val)*this.hierarchySet.getWeight(leafId.doubleValue());
            }
        }
        else if(this.anonymize_property.equals("relational") || this.anonymize_property.equals("mixed")){
            for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
                if(entry.getKey()!=this.setColumn || this.anonymize_property.equals("mixed")){
                    List<Object> domainLeaves;
                    Hierarchy hierarchy = this.hierarchies.get(entry.getKey());
                    if(hierarchy.getHierarchyType().equals("range")){
                        domainLeaves = new ArrayList<Object>(this.rangeLastLevel.get(entry.getKey())) ;
                    }
                    else{
                        domainLeaves = new ArrayList(domainLeavesRelational.get(entry.getKey()));
                    }
                    for(Object leafId: domainLeaves){
                        Pair<Integer,Object> leaf = this.convertToPair(entry.getKey(), leafId);
                        Object gen_val = pointMapRelational.get(leaf)[1];
//                        System.out.println("Original gen Val "+gen_val );
                        if(leafId instanceof Integer)
                            res += costsRelational.get(this.convertToPair(entry.getKey(),gen_val))*hierarchy.getWeight(((Integer)leafId).doubleValue());
                        else{
                            if(leafId instanceof Double && !((Double)leafId).equals(Double.NaN)){
//                                System.out.println("Leaf "+leafId);
                                res += costsRelational.get(this.convertToPair(entry.getKey(),gen_val))*hierarchy.getWeight(((Double)leafId).doubleValue());
//                                System.out.println("Cost val"+gen_val+"cost "+costsRelational.get(this.convertToPair(entry.getKey(),gen_val))*hierarchy.getWeight(((Double)leafId).doubleValue()));
                            }
                            else if(leafId instanceof RangeDouble && !((RangeDouble)leafId).getLowerBound().equals(Double.NaN)){
                                res += costsRelational.get(this.convertToPair(entry.getKey(),gen_val))*hierarchy.getWeight(leafId);
//                                System.out.println("Cost val"+gen_val+"cost "+ costsRelational.get(this.convertToPair(entry.getKey(),gen_val))*hierarchy.getWeight(leafId));
                            }
                            else if(leafId instanceof RangeDouble && !((RangeDate)leafId).getLowerBound().equals(new RangeDate(null,null))){
                                res += costsRelational.get(this.convertToPair(entry.getKey(),gen_val))*hierarchy.getWeight(leafId);
                            }
                            
                        }
                    }
                }
            }
        }
        
        return res;
    }
    
    private double getTestCost() {  /// if set relational 
        double res = 0;
        int count =0;
        if(this.anonymize_property.equals("set")){
//            List<Integer> domainLeaves = this.hierarchySet.getNodeIdsInLevel(0);
            for(Double leafId : domainLeavesSet){
               int testCost = testGensSet.get(leafId.intValue());
//               System.out.println("cost set test cost "+costsSet.get((double)testCost)+" testCost "+testCost);
               res += costsSet.get((double)testCost)*this.hierarchySet.getWeight(leafId.doubleValue());
            }
        }
        else if(this.anonymize_property.equals("relational") || this.anonymize_property.equals("mixed")){
            for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
                if(entry.getKey()!=this.setColumn || this.anonymize_property.equals("mixed")){
                    List<Object> domainLeaves;
                    Hierarchy hierarchy = this.hierarchies.get(entry.getKey());
                    if(hierarchy.getHierarchyType().equals("range")){
                        domainLeaves = new ArrayList<Object>(this.rangeLastLevel.get(entry.getKey())) ;
                    }
                    else{
                        domainLeaves = new ArrayList(domainLeavesRelational.get(entry.getKey()));
                    }
                    for(Object leafId: domainLeaves){
                        Pair<Integer,Object> leaf = this.convertToPair(entry.getKey(), leafId);
                        Object testCost = testGensRelational.get(leaf);
//                        if(testCost instanceof RangeDate && ((RangeDate)testCost).getUpperBound().toString().contains("1991")){
//                            System.out.println("testCost "+testCost+" child "+leaf+" cost "+costsRelational.get(this.convertToPair(entry.getKey(),testCost))+" weight "+(leafId instanceof RangeDate ? hierarchy.getWeight(leafId) : hierarchy.getWeight(((Double)leafId).doubleValue())));
//                        }
//                        System.out.println("Test val"+testCost);
                        if(leafId instanceof Integer){
//                            System.out.println("Test val"+this.convertToPair(entry.getKey(),testCost));
                            res += costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(((Integer)leafId).doubleValue());
//                            if(entry.getKey()==this.setColumn){
//                                int tempTest;
//                                if(testCost instanceof Double){
//                                    tempTest = ((Double)testCost).intValue();
//                                }
//                                else{
//                                    tempTest = (Integer) testCost;
//                                }
//                                System.out.println("Cost Integer Test val set"+hierarchy.getDictionary().getIdToString(tempTest)+" original val "+hierarchy.getDictionaryData().getIdToString((Integer)leafId)+" cost "+costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(((Integer)leafId).doubleValue())+" res "+res);
//
//                            }
//                            else{
//                                System.out.println("Cost Integer Test val "+testCost+" original val "+leafId+" cost "+costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(((Integer)leafId).doubleValue())+" res "+res);
//                            }
                            
                        }
                        else{
                            if(leafId instanceof Double){
                                res += costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(((Double)leafId).doubleValue());
//                                System.out.println("Cost Double Test val "+testCost+" original val "+leafId+" cost "+costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(((Double)leafId).doubleValue()));
                            }
                            else{
                                res += costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(leafId); 
//                                System.out.println("Cost Test val"+testCost+" cost "+costsRelational.get(this.convertToPair(entry.getKey(),testCost))*hierarchy.getWeight(leafId));
                            }
                        }
                    }
                }
            }
        }
        
        return res;
    }
    
    
    
    
    
    
    
    private void createInternalStructures() { // if set relational 
        // domainSize it is complicated for relational data
        // pointMap
        // testGens
        // costs
        if(this.anonymize_property.equals("set")){
            pointMapSet = new HashMap<Double,Integer[]>();
            testGensSet = new HashMap<Integer,Integer>();
            costsSet = new HashMap<Double,Double>();
            hierarchySet = this.hierarchies.get(this.setColumn);
            trieSet = new HashMap<Set<Double>,Integer>();
            this.visitedSet = new HashSet<Double>();
            this.hierarchySet.computeWeights(dataset, dataset.getColumnByPosition(this.setColumn));
            for(int height=0; height<this.hierarchySet.getHeight(); height++){
                
                
                if(height == 0){
                    Map<Integer,Set<Double>> leavesAndParents = this.hierarchySet.getLeafNodesAndParents();
                    domainLeavesSet  = leavesAndParents.get(0);
                    domainSize = domainLeavesSet.size();
                    for(Entry<Integer,Set<Double>> entry : leavesAndParents.entrySet()){
                        Set<Double> values = entry.getValue();
                        for(Double nodeId : values){
                            Integer[] temp = new Integer[2];
                            temp[0] = nodeId.intValue();
                            temp[1] = nodeId.intValue();
                            pointMapSet.put(nodeId, temp);
                            costsSet.put(nodeId, 0.0);
                            if(entry.getKey() == 0){
    //                            System.out.println("Leaves ");
                                testGensSet.put(nodeId.intValue(), -1);
                            }
                            else{
    //                            System.out.println("Parents ");
                                Set<Double> children = this.hierarchySet.getChildrenIds(nodeId);
                                if(children == null){
                                    testGensSet.put(nodeId.intValue(), -1);
                                }
                                else{
                                    costsSet.put(nodeId, (double)children.size() / (double)domainSize);
                                }

                            }
                        }
                    }
                }
                else{
                    List<Integer> nodeIdsInLevel = this.hierarchySet.getNodeIdsInLevel(height);
                    for(Integer nodeId : nodeIdsInLevel){
                        if(pointMapSet.containsKey(nodeId.doubleValue())){
                            continue;
                        }
                        else{
                            Integer[] temp  = new Integer[2];
                            temp[0] = nodeId;       //original value
                            temp[1] = nodeId;       //generalized value
                            pointMapSet.put(nodeId.doubleValue(), temp);
                            costsSet.put(nodeId.doubleValue(), 0.0);
                            Set<Double> children = this.hierarchySet.getChildrenIds(nodeId);
                            if(children!=null){
                                for(Double child : children){
                                    costsSet.put(nodeId.doubleValue(), costsSet.get(nodeId.doubleValue())+costsSet.get(child));
                                }
                            }
                        }
                    }
                }
                
//                List<Integer> nodeIdsInLevel = this.hierarchySet.getNodeIdsInLevel(height);
//                if(height==0){
//                    domainSize = nodeIdsInLevel.size();
//                }
//                for(Integer nodeId : nodeIdsInLevel){
//                    Integer tempPoint[] = new Integer[2];
//                    tempPoint[0] = nodeId;  //original value
//                    tempPoint[1] = nodeId;  //generalized value
////                    tempPoint[2] = height;  //level
//                    
//                    pointMapSet.put(nodeId.doubleValue(), tempPoint);
//                    
//                    costsSet.put(nodeId.doubleValue(), 0.0);
//                    if(height > 0){
//                        Set<Double> children = this.hierarchySet.getChildrenIds(nodeId);
//                        if(children!=null){
//                            if(height == 1){
//                               costsSet.put(nodeId.doubleValue(), (double)children.size() / (double)domainSize); 
//                            }
//                            else{
//                                for(Double child : children){
//                                    costsSet.put(nodeId.doubleValue(), costsSet.get(nodeId.doubleValue()) + costsSet.get(child));
//                                }
//    //                            if(height == this.hierarchySet.getHeight()-1){
//    //                                testGensSet.put(nodeId, -1);
//    //                            }
//                            }
//                        }
//                    }
//                    else{
//                        testGensSet.put(nodeId, -1);
//                    }
//                }
            }
        }
        else if(this.anonymize_property.equals("relational")){
            domainLeavesRelational = new HashMap();
            pointMapRelational = new HashMap<Pair<Integer,Object>,Object[]>();
            testGensRelational = new HashMap<Pair<Integer,Object>,Object>();
            costsRelational = new HashMap<Pair<Integer,Object>,Double>();
            trieRelational = new HashMap<Set<Pair<Integer,Object>>,Integer>();
            this.vistitedRelational = new HashSet();
            Map<Integer,Set<Double>> leavesAndParents;
            Set<Integer> hierarchiesRange = new HashSet();
//            this.numOfVisits = 0;
            
            for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
                if(entry.getValue().getHierarchyType().equals("range")){
                    hierarchiesRange.add(entry.getKey());
                }
            }
            getChildrenFromDataSet(hierarchiesRange);
            for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
                if(entry.getKey()!=this.setColumn){
                    Hierarchy hierarchy = this.hierarchies.get(entry.getKey());
                    hierarchy.computeWeights(dataset, dataset.getColumnByPosition(entry.getKey()));
                    for(int height=0; height<hierarchy.getHeight(); height++){
                        List<Object> nodeIdsInLevel=null;
                        
                        
                        if(hierarchy instanceof HierarchyImplRangesDate){
                            hierarchy.setDictionaryData(this.dataset.getDictionary());
                        }
                        
                        if(hierarchy.getHierarchyType().equals("range")){
                            nodeIdsInLevel = hierarchy.getNodesInLevel(height);
                        }
                        else if(height!=0){
                            nodeIdsInLevel = hierarchy.getNodeIdsInLevel(height);
                        }
                        
                        if(height==0){
                            domainSize = 0;
//                            System.out.println("Domain size "+nodeIdsInLevel.size()+" domain "+nodeIdsInLevel);
                            if(hierarchy.getHierarchyType().equals("range")){
//                                getChildrenFromDataSet(entry.getKey());
                                domainSize = this.rangeLastLevel.get(entry.getKey()).size();
                            }
                            else{
                                leavesAndParents = hierarchy.getLeafNodesAndParents();
                                domainLeavesRelational.put(entry.getKey(), leavesAndParents.get(0));
//                                System.out.println("Children distinct "+nodeIdsInLevel);
                                domainSize = domainLeavesRelational.get(entry.getKey()).size();
                                for(Entry<Integer,Set<Double>> entryLeaves : leavesAndParents.entrySet()){
                                    Set<Double> values = entryLeaves.getValue();
                                    for(Double nodeId : values){
                                        Object[] temp = new Object[2];
                                        temp[0] = nodeId.intValue();
                                        temp[1] = nodeId.intValue();
                                        Pair<Integer,Object> node = this.convertToPair(entry.getKey(), nodeId);
                                        pointMapRelational.put(node, temp);
                                        costsRelational.put(node, 0.0);
                                        if(entryLeaves.getKey() == 0){
                //                            System.out.println("Leaves ");
                                            testGensRelational.put(node, -1);
                                        }
                                        else{
                //                            System.out.println("Parents ");
                                            Set<Double> children = hierarchy.getChildrenIds(nodeId);
//                                            System.out.println("Parents children "+children+ " parent "+nodeId+" hierarchy "+hierarchy.getName());
                                            costsRelational.put(node, (double)children.size() / (double)domainSize);

                                        }
                                    }
                                }
                                continue;
                            }
                        }
                        for(Object nodeId : nodeIdsInLevel){
                            Pair<Integer,Object> node = this.convertToPair(entry.getKey(), nodeId);
                            if(pointMapRelational.containsKey(node)){
                                continue;
                            }
                            Object tempPoint[] = new Object[2];
                            tempPoint[0] = nodeId;  //original value
                            tempPoint[1] = nodeId;  //generalized value
//                            Pair<Integer,Object> node = this.convertToPair(entry.getKey(), nodeId);
//                            tempPoint[2] = this.getLevel(node);  //level
                            pointMapRelational.put(node, tempPoint);
                            costsRelational.put(node, 0.0);
                            if(this.getLevel(node) > 0){
                                Set<Object> children = null;
                                if(hierarchy.getHierarchyType().equals("range")){
                                    if(this.getLevel(node)!= 1){
//                                        System.out.println("children "+nodeId);
                                        List<Object> childrenList = hierarchy.getChildren(nodeId);
                                        if(childrenList != null){
                                            children = new HashSet(childrenList) ;
                                        }
//                                        else{
//                                            continue;
//                                        }
                                    }
                                }
                                else{
                                    children = hierarchy.getChildrenIds(((Integer) nodeId).doubleValue());
                                }
                                if(this.getLevel(node) == 1){
                                    if(children!=null){
                                        costsRelational.put(node, (double)children.size() / (double)domainSize); 
                                    }
                                    else{
                                        costsRelational.put(node, (double)findNumOfChildren(node) / (double)domainSize); 
                                    }
//                                    System.out.println("node "+node+" with cost "+costsRelational.get(node) + " and height "+height);
                                }
                                else if(children!=null){
                                    for(Object child : children){
                                        Pair<Integer,Object> nodechild = this.convertToPair(entry.getKey(), child);
//                                        System.out.println("node "+nodechild+" and height "+height);
                                        costsRelational.put(node, costsRelational.get(node) + costsRelational.get(nodechild));
                                        
//                                        System.out.println("node "+node+" with cost "+costsRelational.get(node) + " and height "+height);
                                    }
//                                    if(node.getValue() instanceof RangeDate && ((RangeDate)node.getValue()).getUpperBound().toString().contains("1991")){
//                                        System.out.println("node not leaf"+node+" with cost "+costsRelational.get(node) + " and height "+height);
//                                    }
//                                    System.out.println("node "+" with cost "+costsRelational.get(node)+" and height "+height);
//                                    if(height == hierarchy.getHeight()-1){
//                                        testGensRelational.put(node, -1);
//                                    } 
                                }
                            }
                            else{
                                testGensRelational.put(node, -1);
                            }
                            
                        }
                    }
                }
            }
        }
        else{ /// mixed
//            pointMapRelational = new HashMap<Pair<Integer,Object>,Object[]>();
//            testGensRelational = new HashMap<Pair<Integer,Object>,Object>();
//            costsRelational = new HashMap<Pair<Integer,Object>,Double>();
            trieRelational = new HashMap<Set<Pair<Integer,Object>>,Integer>();
//            pointMapSet = new HashMap<Double,Integer[]>();
//            this.numOfVisits = 0;
//            for(Entry<Double,Integer[]> entry : this.pointMapSet.entrySet()){
//                System.out.print("original value "+entry.getValue()[0]+" anon value "+entry.getValue()[1]);
//            }
            this.domainLeavesRelational.put(this.setColumn, domainLeavesSet);
            for(int height=0; height<this.hierarchySet.getHeight(); height++){
                if(height == 0){
                    for(Double nodeId : domainLeavesSet){
                        Object tempPoint[] = new Object[3];
                        Integer[] tempArr = this.pointMapSet.get(nodeId);
                        tempPoint[0] = tempArr[0].doubleValue();
                        tempPoint[1] = tempArr[1].doubleValue();
                        Pair<Integer,Object> node = this.convertToPair(this.setColumn, nodeId);
                        this.pointMapRelational.put(node, tempPoint);
                        this.costsRelational.put(node, this.costsSet.get(nodeId));
                        this.testGensRelational.put(node,this.testGensSet.get(nodeId));
                    }
                }
                else{
                    List<Integer> nodeIdsInLevel = this.hierarchySet.getNodeIdsInLevel(height);
                    for(Integer nodeId : nodeIdsInLevel){
                        Pair<Integer,Object> node = this.convertToPair(this.setColumn, nodeId);
                        if(!this.pointMapRelational.containsKey(node)){
                            Object tempPoint[] = new Object[3];
                            Integer[] tempArr = this.pointMapSet.get(nodeId.doubleValue());
                            tempPoint[0] = tempArr[0].doubleValue();
                            tempPoint[1] = tempArr[1].doubleValue();
        //                    System.out.println("original value "+nodeId+" anonValue "+tempPoint[1]+" the other original "+tempPoint[0]);
        //                    tempPoint[2] = tempArr[2];

                            this.pointMapRelational.put(node, tempPoint);
                            this.costsRelational.put(node, this.costsSet.get(nodeId.doubleValue()));
                        }
                    }
                }
            }     
            
        }
        
    }
    
    private void sortTrie(){
        if(this.anonymize_property.equals("set")){
            Map<Set<Double>, Integer> sorted = trieSet
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                    toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                        LinkedHashMap::new));
            trieSet = sorted;
        }
        else if(this.anonymize_property.equals("relational") || this.anonymize_property.equals("mixed")){
            Map<Set<Pair<Integer,Object>>, Integer> sorted = trieRelational
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                    toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                        LinkedHashMap::new));
            trieRelational = sorted;
        }
    }
    
    private Set<Pair<Integer,Object>> expandTransaction(List<Pair<Integer,Object>> transaction){
        Set<Pair<Integer,Object>> expanded = new TreeSet<>(new Comparator<Pair<Integer,Object>>()
        {
            public int compare(Pair<Integer,Object> o1, Pair<Integer,Object> o2)
            {
                try{
                    int retVal = o1.getKey().compareTo(o2.getKey());
                    if (retVal != 0) {
                        return retVal;
                    }
                    if(o1.getValue() instanceof Double && o2.getValue() instanceof Double){
                        return ((Double)o1.getValue()).compareTo((Double)o2.getValue());
                    }
                    else if(o1.getValue() instanceof RangeDouble){
                        if(o2.getValue() instanceof Double){
                            return ((RangeDouble)o1.getValue()).compareTo((Double)o2.getValue());
                        }
                        else {
                            return ((RangeDouble)o1.getValue()).compareTo((RangeDouble)o2.getValue());
                        }
                    }
                    else if(o1.getValue() instanceof Double && o2.getValue() instanceof RangeDouble){
                        return ((RangeDouble)o2.getValue()).compareTo((Double)o1.getValue());
                    }
                    else if(o1.getValue() instanceof Double && o2.getValue() instanceof RangeDate){
                        String valueDate = dataset.getDictionary().getIdToString(((Double)o1.getValue()).intValue());

                        return ((RangeDate)o2.getValue()).compareTo(AnonymizedDataset.getDateFromString(valueDate));

                    }
                    else{
                        if(o2.getValue() instanceof Double)  {
                            String valueDate = dataset.getDictionary().getIdToString(((Double)o2.getValue()).intValue());

                            return ((RangeDate)o1.getValue()).compareTo(AnonymizedDataset.getDateFromString(valueDate));

                        }
                        else{
                            return ((RangeDate)o1.getValue()).compareTo((RangeDate)o2.getValue());
                        }
                    }
                }catch(Exception e){
                    System.out.println("Problem with parsing "+e.getMessage());
                    return 0;
                }
                
            } 
        });
        for(Pair<Integer,Object> item : transaction){
            expanded.add(item);
            getAllGeneralizations(item, expanded);
        }
        
        return expanded;
    }
    
    private void getAllGeneralizations(Pair<Integer,Object> current, Set<Pair<Integer,Object>> result) {
        Hierarchy hierarchy = this.hierarchies.get(current.getKey());
        Object temp;
        if(hierarchy.getHierarchyType().equals("range")){
            if(current.getValue() instanceof Double){
                temp = hierarchy.getParent((Double) current.getValue());
            }
            else{
                temp = hierarchy.getParent(current.getValue());
            }
            
            
            while(temp!=null){
               result.add(this.convertToPair(current.getKey(), temp));
//               System.out.println("Returned "+temp);
               temp = hierarchy.getParent(temp);
            }
            
        }
        else{
            temp =  hierarchy.getParentId((Double)current.getValue());
            while((Double)temp!=-1){
               result.add(this.convertToPair(current.getKey(), temp));
               temp = hierarchy.getParentId((Double)temp);
            } 
        }
        
    }
    
    private Pair<Integer,Object> getTranslation(Pair<Integer,Object> point) {
        if(pointMapRelational.get(point)!=null){
            return this.convertToPair(point.getKey(), pointMapRelational.get(point)[1]) ;
        }
        else{
//            if(this.hierarchies.containsKey(point.getKey()) && this.hierarchies.get(point.getKey()).getHierarchyType().equals("range")){
////                    Predicate<Pair<Integer, Object>> pairContainsValue = pair -> ((RangeDouble) pair.getValue()).contains((Double)point.getValue());
////                    Pair<Integer,Object> keyWithValue =  (Pair<Integer,Object>) pointMapRelational.keySet().stream()
////                                    .filter(pairContainsValue::test).toArray()[1];
////                    System.out.println("Point "+point+" -> "+keyWithValue+" and level "+pointMapRelational.get(keyWithValue)[2]+" other level "+this.getLevel(keyWithValue));
////                    
////                    return this.convertToPair(point.getKey(), pointMapRelational.get(keyWithValue)[1]) ;
////                System.out.println("Point translation ragne "+point);
////                Object[] tempArrPoint = new Object[3];
////                tempArrPoint[0] = point.getValue();
////                tempArrPoint[1] = point.getValue();
////                tempArrPoint[2] = 0;
////                pointMapRelational.put(point, tempArrPoint);
////                return point;
//                    
//            }
//            else{
                return null;
//            }
        }
    } 
    
    
    ////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////// RELATIONAL /////////////////////////////////////////
    
    private Set<Object> getChildrenFromAllLevels(Hierarchy h, Pair<Integer,Object> genVal,boolean onlylast){
        Set<Double> childrenLastLevel = this.rangeLastLevel.get(genVal.getKey());
        Set<Object> children = new HashSet();
        
        for(Object child : childrenLastLevel){
            if(genVal.getValue() instanceof RangeDouble){
                if(((RangeDouble) genVal.getValue()).contains((Double)child)){
                    children.add(child);
                }
            }
            else{
                try {
                    if(((RangeDate) genVal.getValue()).contains(this.dataset.getDictionary().getIdToString((((Double)child).intValue())))){
                        children.add(child); 
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(MixedApriori.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Problem with parsing "+ex.getMessage());
                }
            }
        }
        
        if(!onlylast)
            children.addAll(getAllchildren(h,genVal.getValue()));
        return children;
    }
    
    
    private List<Object> getAllchildren(Hierarchy h, Object g){
        List<Object> children;
        if(h.getChildren(g)==null){
            return new ArrayList<Object>();
        }
        else{
            children = (List<Object>)((ArrayList<Object>) h.getChildren(g)).clone();
            List<Object> grandChildren = new ArrayList<>();
            for(Object child : children){
                grandChildren.addAll(getAllchildren(h,child));
            }
            children.addAll(grandChildren);
            return children;
        }
    }
    
    private void getChildrenFromDataSet(Set<Integer> hierarchiesCol){
        if(hierarchiesCol.isEmpty()){
            return;
        }
        Set<Double> childrenLastLevel;
        if(this.rangeLastLevel==null){
            this.rangeLastLevel = new HashMap<Integer,Set<Double>>();
        }
        
        for(int i=0; i<this.relationalData.length; i++){
            for(Integer hierarchyCol : hierarchiesCol){
                childrenLastLevel = this.rangeLastLevel.get(hierarchyCol);
                if(childrenLastLevel==null){
                    childrenLastLevel = new HashSet();
                }
                if(!childrenLastLevel.contains(relationalData[i][hierarchyCol])){
                    childrenLastLevel.add(relationalData[i][hierarchyCol]);
                    Object[] mapArr = new Object[2];
                    mapArr[0] = relationalData[i][hierarchyCol];
                    mapArr[1] = relationalData[i][hierarchyCol];
    //                mapArr[2] = 0;
                    Pair<Integer,Object> nodeLastLevel = this.convertToPair(hierarchyCol, relationalData[i][hierarchyCol]);
                    this.pointMapRelational.put(nodeLastLevel, mapArr);
                    this.testGensRelational.put(nodeLastLevel, -1);
                    this.costsRelational.put(nodeLastLevel, 0.0);
                }
                this.rangeLastLevel.put(hierarchyCol, childrenLastLevel);
            }
        }
        
        
    }
    
//    private void getChildrenFromDataSet(int  column){
//        Set<Double> childrenLastLevel = new HashSet<Double>();
//        
//        if(this.rangeLastLevel==null){
//            this.rangeLastLevel = new HashMap<Integer,Set<Double>>();
//        }
//        
//        for(int i=0; i<this.relationalData.length; i++){
//            if(!childrenLastLevel.contains(relationalData[i][column])){
//                childrenLastLevel.add(relationalData[i][column]);
//                Object[] mapArr = new Object[2];
//                mapArr[0] = relationalData[i][column];
//                mapArr[1] = relationalData[i][column];
////                mapArr[2] = 0;
//                Pair<Integer,Object> nodeLastLevel = this.convertToPair(column, relationalData[i][column]);
//                this.pointMapRelational.put(nodeLastLevel, mapArr);
//                this.testGensRelational.put(nodeLastLevel, -1);
//                this.costsRelational.put(nodeLastLevel, 0.0);
//            }
//        }
//        
//        this.rangeLastLevel.put(column, childrenLastLevel);
//    }
    
    private int findNumOfChildren(Pair<Integer,Object> node){
        Set<Double> children = this.rangeLastLevel.get(node.getKey());
        int count=0;
        for(Double child : children){
            if(node.getValue() instanceof RangeDouble){
                if(((RangeDouble)node.getValue()).contains(child)){
                   count++; 
                }
                
            }
            else{
                try { 
                    if(((RangeDate)node.getValue()).contains(this.dataset.getDictionary().getIdToString(child.intValue()))){
                        count++;
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(MixedApriori.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Problem with parsing "+ex.getMessage());
                }
               
            }    
        }
        
        return count;
    }
    
    private Set<Pair<Integer,Object>> preorderNextRelational(){
        if(this.numOfVisits!=trieRelational.size()){
            Set<Pair<Integer,Object>> returned_val;
//            returned_val = (Set<Pair<Integer,Object>>) trieRelational.keySet().toArray()[numOfVisits];
            returned_val = this.keysRelational[numOfVisits];
            if(trieRelational.get(returned_val)>=k){
                return null;
            }
            numOfVisits++;
            return returned_val;
        }
        else{
            return null;
        }
    }
    
    private Set<Pair<Integer,Object>>  getTranslateTransactionRelational(Set<Pair<Integer,Object>> transaction){
        Set<Pair<Integer,Object>> translated_transaction = new HashSet<Pair<Integer,Object>>();
        
        for(Pair<Integer,Object> value : transaction){
            translated_transaction.add(this.getTranslation(value));
        }
        
        return translated_transaction;
    }
    
    private List<Pair<Integer,Double>> findBestSolution(Map<Double,List<Pair<Integer,Double>>> results){
        Iterator<Map.Entry<Double,List<Pair<Integer,Double>>>> iterator = results.entrySet().iterator();
        int count=0;
        int level = 1000000;
        List<Pair<Integer,Double>> returned_solution=null;
        while(iterator.hasNext() && count<4){
            int current_level=0;
            Entry<Double,List<Pair<Integer,Double>>> entry = (Entry<Double,List<Pair<Integer,Double>>>) iterator.next();
            List<Pair<Integer,Double>> solution_list = entry.getValue();
            for(Pair<Integer,Double> solution : solution_list){
                current_level += this.hierarchies.get(solution.getKey()).getLevel((double)solution.getValue());
            }
            if(current_level<level){
                returned_solution = solution_list;
                level=current_level;
            }
            count++;
        }
        return returned_solution;
    }
    
    private void fixRelational(List<Pair<Integer,Object>> prefix){
        Set<Pair<Integer,Object>> domain = expandTransaction(prefix);
        
//        System.out.println("Domain size"+domain.size());
        if(domain.size() > 1){
            Map<Double, List<Pair<Integer,Object>>> results = new TreeMap<Double, List<Pair<Integer,Object>>>();
//            System.out.println("Transaction "+Arrays.toString(prefix.toArray()));
//            System.out.println("Expand transaction solutions "+Arrays.toString(domain.toArray()));
            MixedSolutionCombinations solCombs = new MixedSolutionCombinations(this.hierarchies, trieRelational, k, this);
            solCombs.createSolutionCombs(results, prefix);
            
//            System.out.println("results size "+results.size());
            
            for(Entry<Double, List<Pair<Integer,Object>>> entry : results.entrySet()){
                List<Pair<Integer,Object>> anonPath = /*findBestSolution(results);*/entry.getValue();
//                System.out.println("Gonna generalized "+Arrays.toString(anonPath.toArray()) +" score "+entry.getKey());
                for(int i=0; i<anonPath.size(); i++){
                   
//                    System.out.println("anaonPAth i="+i+" "+anonPath.get(i));
                    
                    Pair<Integer,Object> anonItem = anonPath.get(i);
                    if((anonItem.getValue() instanceof Double && ((Double)anonItem.getValue() == -1.0))){
                        continue;
                    }
                    
                    if((anonItem.getValue() instanceof Integer && ((Integer)anonItem.getValue() == -1))){
                        continue;
                    }
                    if((!anonItem.equals(prefix.get(i))) && anonItem.equals(this.getTranslation(anonItem)) && !this.vistitedRelational.contains(anonItem)){
//                        System.out.println("Gonna generalized "+Arrays.toString(anonPath.toArray()) +" score "+entry.getKey());
//                        System.out.println("General anon : "+anonItem);
                        generalize(anonItem);  //// TODO
                        this.vistitedRelational.add(anonItem);
                        
                    }
                }
                break;
            }
        }
        
        
        
    }
    
    private void generalize(Pair<Integer,Object> generalized){
        int l = getLevel(generalized);
//        System.out.println("Level "+l+" gen val "+generalized);
        if(l == 0)
            return;
        
        Hierarchy hierarchy = this.hierarchies.get(generalized.getKey());
        Set<Object>  children;
        if(hierarchy.getHierarchyType().equals("range")){
            children = this.getChildrenFromAllLevels(hierarchy, generalized,false);
//            System.out.println("All children range "+Arrays.toString(children.toArray()));
        }
        else{
            children = hierarchy.getChildrenIds((Double)generalized.getValue());
        }
        
        if(children != null){
            for(Object child : children){
                Pair<Integer,Object> childNode = this.convertToPair(generalized.getKey(), child);
                Object[] mapArr = pointMapRelational.get(childNode);
//                System.out.println("childNode "+childNode+" gen node "+generalized+" arr "+Arrays.toString(mapArr));
                mapArr[1] = generalized.getValue();
//                mapArr[2] = getLevel(generalized);
                pointMapRelational.put(childNode, mapArr);
                if(l > 1 && !hierarchy.getHierarchyType().equals("range")){
                    gen(childNode, generalized);
                }
            }
//            if(l == 1){
//                for(Object child : children){
//                    Pair<Integer,Object> childNode = this.convertToPair(generalized.getKey(), child);
//                    Object[] mapArr = pointMapRelational.get(childNode);
//                    mapArr[1] = generalized.getValue();
//                    mapArr[2] = getLevel(generalized);
//                    pointMapRelational.put(childNode, mapArr);
//                }
//            }else{
//                for(Object child : children){
//                    Pair<Integer,Object> childNode = this.convertToPair(generalized.getKey(), child);
//                    Object[] mapArr = pointMapRelational.get(childNode);
////                    System.out.println("exist "+mapArr[1]+" gen "+generalized.getValue());
//                    mapArr[1] = generalized.getValue();
//                    mapArr[2] = getLevel(generalized);
//                    pointMapRelational.put(childNode, mapArr);
//                    if(!hierarchy.getHierarchyType().equals("range")){
//                        gen(childNode, generalized);
//                    }
//                }
//            }
        }
    }
    
    private void gen(Pair<Integer,Object> o, Pair<Integer,Object> g){
        if(getLevel(o) == 0){
            Object[] mapArr = pointMapRelational.get(o);
            mapArr[1] = g.getValue();
//            mapArr[2] = getLevel(g);
            pointMapRelational.put(o, mapArr);
            
        }
        else{
            Hierarchy hierarchy = this.hierarchies.get(o.getKey());
            Set<Object> children;
            if(hierarchy.getHierarchyType().equals("range")){
                children = new HashSet(hierarchy.getChildren(o.getValue()));
            }
            else{
                children = hierarchy.getChildrenIds((Double)o.getValue());
            }
             
            if(children != null){
                for(Object child : children){
                    Pair<Integer,Object> childNode = this.convertToPair(o.getKey(), child);
                    Object[] mapArr = pointMapRelational.get(childNode);
                    mapArr[1] = g.getValue();
//                    mapArr[2] = getLevel(g);
                    pointMapRelational.put(childNode, mapArr);
                    gen(childNode, g);
                }
            }
        }
    }
    
    ///// generalize
   /////  gen
    
    private int getLevel(Pair<Integer,Object> point){
        if(point.getValue() instanceof Double){
            return this.hierarchies.get(point.getKey()).getLevel((double)point.getValue());
        }
        else{
            return this.hierarchies.get(point.getKey()).getHeight() - this.hierarchies.get(point.getKey()).getLevel(point.getValue()) ;
        }
        
    }
    
    private void generalizeTest(Pair<Integer,Object> generalized){
        int l = this.getLevel(generalized);
        
        if(l==0){
            return;
        }
        Hierarchy hierarchy = this.hierarchies.get(generalized.getKey());
        Set<Object> children;
        if(hierarchy.getHierarchyType().equals("range")){
//            System.out.println("Gen Value Test"+generalized.getValue());
            children = getChildrenFromAllLevels(hierarchy,generalized,true);
        }
        else{
            children = hierarchy.getChildrenIds((Double)generalized.getValue());
        }
        if(children != null){
            if(hierarchy.getHierarchyType().equals("range")){
                for(Object child : children){
                    Pair<Integer,Object> childNode = this.convertToPair(generalized.getKey(), child);
                    if(this.getLevel(childNode)==0 ){
                        testGensRelational.put(childNode, generalized.getValue());
                    }
                }
            }
            else if (l == 1 || this.domainLeavesRelational.get(generalized.getKey()).contains(generalized.getValue())){
               for(Object child : children){
                    testGensRelational.put(this.convertToPair(generalized.getKey(),child), generalized.getValue());
                }
            }
            else{
                for(Object child : children){
                    
                    genTest(this.convertToPair(generalized.getKey(),child), generalized);
                } 
            }
        }
    }
    
    private void genTest(Pair<Integer,Object> o, Pair<Integer,Object> g){
        if(getLevel(o) == 0 || this.domainLeavesRelational.get(o.getKey()).contains(o.getValue())){
            testGensRelational.put(o, g.getValue());
        }
        else{
            Set<Object> children; 
            Hierarchy hierarchy =  this.hierarchies.get(o.getKey());
            children = this.hierarchies.get(o.getKey()).getChildrenIds((Double)o.getValue());
            if(children != null){
                for(Object child : children){
                    genTest(this.convertToPair(o.getKey(), child), g);
                }
            }
        }
    }
    
    
    public double getAddedCost(List<Pair<Integer,Object>> comb, List<Pair<Integer,Object>> base){
        double existingCost = 0;
        double newCost = 0;
        
        this.resetTestGens();
        
        for(int i = 0; i<comb.size(); i++){
            Pair<Integer,Object> anonItem = comb.get(i);
            if(anonItem.getValue() instanceof Double && (Double)anonItem.getValue() == -1.0){
                continue;
            }
            if(anonItem.getValue() instanceof Integer && (Integer)anonItem.getValue() == -1){
                continue;
            }
            if( (!anonItem.equals(base.get(i)))){
//                System.out.println("AnonItem "+anonItem+" comb "+comb);
                generalizeTest(anonItem);
            }
        }
        
        existingCost = getTotalCost();  //// TODO change relational condition 
        newCost = getTestCost();
        
//        System.out.println("exist cost "+existingCost+" test cost "+newCost+" for "+comb+" sum "+(newCost - existingCost));
        return (newCost - existingCost);
    }
    
    public Pair<Integer,Object> convertToPair(Integer key, Object value){
        if(value instanceof Integer){
            return new Pair<Integer,Object>(key,((Integer)value).doubleValue());
        }
        else{
            return new Pair<Integer,Object>(key,value);
        }
    }
    
    private double getCost(Pair<Integer,Double> n){
        if(costsRelational.containsKey(n))
            return costsRelational.get(n);
        else
            return 0.0;
    }
    
    
    private List<Pair<Integer,Object>> getNextTransactionRelational(){
        List<Pair<Integer,Object>> anonTransaction = null;
        double[] originalTransaction = null;
        if(nextIndex == relationalData.length){
            nextIndex = 0;
            return null;
        }
        
        anonTransaction = new ArrayList<Pair<Integer,Object>>();
        
        
        originalTransaction = relationalData[nextIndex];
        
        for(int i=0; i<originalTransaction.length; i++){
            if(i!=this.setColumn){
                Pair<Integer,Object> value = getTranslation(this.convertToPair(i, originalTransaction[i]));
                if(value!=null){
                    anonTransaction.add(value); 
                }
            }
        }
            // do not forget -1 represent the set column
//        for ( int i = 0 ; i < originalTransaction.length ; i ++){
//            System.out.print(originalTransaction[i] + ",");
//        }
//        System.out.println();
        
        
        
        
        nextIndex++;
        
        //System.out.println("originalTransaction = ");
        /*for (int i = 0 ; i < originalTransaction.length ; i ++ ){
            System.out.println(originalTransaction[i]+",");
        }
        
        //System.out.println("anonTransaction = ");
        for (int i = 0 ; i < anonTransaction.length ; i ++ ){
            System.out.println(anonTransaction[i]+",");
        }*/
        
        
        return anonTransaction;
    }
    
    private Pair<Integer,Double> getHighestLevelRelational(Set<Pair<Integer,Double>> comb){
        int level=0;
        Pair<Integer,Double> value=null;
        for(Pair<Integer,Double> val : comb){
            if(level < this.hierarchies.get(val.getKey()).getLevel(val.getValue())){
                value = val;
                level = this.hierarchies.get(val.getKey()).getLevel(val.getValue());
            }
        }
        if(value==null){
            value = (Pair<Integer,Double>) comb.toArray()[0];
        }
        return value;
    }
    
    ///////////////////////////// Mixed ///////////////////////////////////////////////////////
    
    private void convertToAnonymize(){
        Map<Object,Object> rulesSet =  this.rulesSetRealtioanl.get(this.setColumn);
        Map<Object,Object> rulesRelationalColumn;
        this.anonymize_set = new double[this.setData.length][];
        for(int i=0; i<this.setData.length; i++){
            this.anonymize_set[i] = new double[this.setData[i].length];
            for(int j=0; j<this.setData[i].length; j++){
                this.anonymize_set[i][j] = (Double) rulesSet.get(((Double)setData[i][j]));
            }
        }
        
        this.anonymize_relational = new Object[this.relationalData.length][this.relationalData[0].length];
        for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
            if(entry.getKey()!=this.setColumn){
                rulesRelationalColumn = this.rulesSetRealtioanl.get(entry.getKey());
                for(int i=0; i<this.relationalData.length; i++){
                    this.anonymize_relational[i][entry.getKey()] = rulesRelationalColumn.get(this.relationalData[i][entry.getKey()]);
                }
            }
        }   
    }
    
    private List<Pair<Integer,Object>> getNextTransactionMixed(){
        List<Pair<Integer,Object>> anonTransaction = null;
        double[] originalTransactionRelational = null;
        double[] originalTransactionSet = null;
        double[] originalTransaction = null;
        if(nextIndex == relationalData.length){
            nextIndex = 0;
            return null;
        }
        
        originalTransaction = new double[relationalData[nextIndex].length + setData[nextIndex].length];
        System.arraycopy(relationalData[nextIndex], 0, originalTransaction, 0, relationalData[nextIndex].length);
        System.arraycopy(setData[nextIndex], 0, originalTransaction, relationalData[nextIndex].length, setData[nextIndex].length);
        anonTransaction = new ArrayList<Pair<Integer,Object>>();
//        System.out.println("mixed original transaction "+Arrays.toString(originalTransaction));
        
        for(int i=0; i<originalTransaction.length; i++){
            
            Pair<Integer,Object> value=null;
            if(i>=relationalData[nextIndex].length){
                value = getTranslation(this.convertToPair(this.setColumn, originalTransaction[i]));
            }
            else{
                if(i!=this.setColumn){
                    value = getTranslation(this.convertToPair(i, originalTransaction[i]));
                }
            }
            if(value!=null){
                anonTransaction.add(value); 
            }
        }
        
//        originalTransactionRelational = relationalData[nextIndex];
//        originalTransactionSet = setData[nextIndex];
//        
//        for(int i=0; i<originalTransactionRelational.length; i++){
//            if(i!=this.setColumn){
//                Pair<Integer,Object> value = getTranslation(this.convertToPair(i, originalTransactionRelational[i]));
//                if(value!=null){
//                    anonTransaction.add(value); 
//                }
//            }
//        }
//        
//        for(int i=0; i<originalTransactionSet.length; i++){
//            Pair<Integer,Object> value = getTranslation(this.convertToPair(this.setColumn, originalTransactionSet[i]));
//            if(value!=null){
//                anonTransaction.add(value); 
//            }
//        }
            // do not forget -1 represent the set column
//        for ( int i = 0 ; i < originalTransaction.length ; i ++){
//            System.out.print(originalTransaction[i] + ",");
//        }
//        System.out.println();
        
        
        
        
        nextIndex++;
        
        //System.out.println("originalTransaction = ");
        /*for (int i = 0 ; i < originalTransaction.length ; i ++ ){
            System.out.println(originalTransaction[i]+",");
        }
        
        //System.out.println("anonTransaction = ");
        for (int i = 0 ; i < anonTransaction.length ; i ++ ){
            System.out.println(anonTransaction[i]+",");
        }*/
        
        
        return anonTransaction;
    }
    
    
    private boolean containsOnlySet(Set<Pair<Integer,Object>> combination){
        if(combination.size()>1){
            for(Pair<Integer,Object> element : combination){
                if(element.getKey()!=this.setColumn){
                    return false;
                }
            }
        }
        else{
            return false;
        }
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getResultSet() {
        
        Map<Integer,Map<Object,Object>> rulesMixed = new HashMap<Integer,Map<Object,Object>>();
//        if(!pointMapSet.isEmpty()){
//            Map<Object, Object> rulesSet = new HashMap<>();
//            List<Integer> leavesValues = this.hierarchySet.getNodeIdsInLevel(0);
//            for(Integer leaf : leavesValues){
//                rulesSet.put(leaf.doubleValue(),pointMapSet.get(leaf.doubleValue())[1].doubleValue());
//            }
////            for(Entry<Double,Integer[]> entry : pointMapSet.entrySet()){
////                rulesSet.put( entry.getValue()[0].doubleValue(), entry.getValue()[1].doubleValue());
////            }
//            rulesMixed.put(this.setColumn,rulesSet);
//        }
        
        if(!pointMapRelational.isEmpty()){
//            Map<Integer,Map<Double,Double>> rulesRelational =  new HashMap<>();
//            for(Entry<Integer,Hierarchy> entry : this.hierarchies.entrySet()){
////                if(entry.getKey()!=this.setColumn){
//                    Map<Object,Object> rules = new HashMap();
//                
//                    List<Object> nodeIdsInLevel;
//                    if(entry.getValue().getHierarchyType().equals("range")){
//                        nodeIdsInLevel = new ArrayList(this.rangeLastLevel.get(entry.getKey()));
//                    }
//                    else{
//                        nodeIdsInLevel = entry.getValue().getNodeIdsInLevel(0);
//                    }
//
//                    for(Object value : nodeIdsInLevel){
//                        Double doubleValue;
//                        if(value instanceof Integer){
//                            value = ((Integer) value).doubleValue();
//                        }
//                        else{
//                           value = (Double) value; 
//                        }
//                        rules.put(value, pointMapRelational.get(this.convertToPair(entry.getKey(), value))[1]);
//                    }
//                    rulesMixed.put(entry.getKey(), rules);
////                }
//                
//            }
            
            for(Entry<Integer,Hierarchy> entryHier : hierarchies.entrySet()){
                Map<Object,Object> rules = new HashMap();
                rulesMixed.put(entryHier.getKey(), rules);
            }
            
            for(Entry<Pair<Integer,Object>,Object[]> entryValue : this.pointMapRelational.entrySet()){
                Map<Object,Object> rulesColumn = rulesMixed.get(entryValue.getKey().getKey());
                Object value = entryValue.getKey().getValue();
                Object anonymizedValue = entryValue.getValue()[1];
                if(!hierarchies.get(entryValue.getKey().getKey()).getHierarchyType().equals("range")){
                
                    if(value instanceof Integer){
                        value = ((Integer) value).doubleValue();
                    }
                    else{
                       value = (Double) value; 
                    }
                    
                    if(anonymizedValue instanceof Integer){
                        anonymizedValue = ((Integer) entryValue.getValue()[1]).doubleValue();
                    }
                }
                else{
                    if(!(value instanceof RangeDate) && !(value instanceof RangeDouble)){
                        if(value instanceof Integer){
                            value = ((Integer) value).doubleValue();
                        }
                        else{
                           value = (Double) value; 
                        }
                        
                        if(anonymizedValue instanceof Integer){
                           anonymizedValue = ((Integer) entryValue.getValue()[1]).doubleValue();
                        }
                    }
                }
                rulesColumn.put(value, anonymizedValue);
                rulesMixed.put(entryValue.getKey().getKey(), rulesColumn);
            }
//            for(Entry<Pair<Integer,Object>,Object[]> entry : pointMapRelational.entrySet()){
//                Map<Object,Object> tempRules = rulesMixed.get(entry.getKey().getKey());
//                tempRules.put(entry.getValue()[0] instanceof Integer ? ((Integer)entry.getValue()[0]).doubleValue() : entry.getValue()[0] , entry.getValue()[1] instanceof Integer ? ((Integer)entry.getValue()[1]).doubleValue() : entry.getValue()[1]);
//                rulesMixed.put(entry.getKey().getKey(),tempRules);
//            }
        }
        
//        System.out.println("Result set "+rulesMixed);
        
        
        return rulesMixed;
    }

    @Override
    public Graph getLattice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAnonymousResult(LatticeNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
