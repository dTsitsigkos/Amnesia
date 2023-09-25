/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.differentialprivacy;

import algorithms.Algorithm;
import algorithms.flash.GridNode;
import algorithms.parallelflash.GeneralizedRow;
import algorithms.parallelflash.ParallelFlash;
import algorithms.parallelflash.Worker;
import anonymizeddataset.AnonymizedDataset;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import data.Data;
import data.DiskData;
import data.TXTData;
import dictionary.DictionaryString;
import exceptions.NotFoundValueException;
import graph.Graph;
import hierarchy.Hierarchy;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimak
 */
public class DifferentialPrivacyAlgorithm implements Algorithm {
    private Data dataset = null;
    private Object[][] anonymizeDataset;
    private Object[][] noiseDataset = null;
    private Map<Integer, Hierarchy> hierarchies = null;
    private int []transformation;
    private ForkJoinPool pool = null;
    private Map<List,Integer> histogram;
    private int parallelism = -1;
    private int k;
    
    
    
    public DifferentialPrivacyAlgorithm()  {
        pool = new ForkJoinPool();
        parallelism = pool.getParallelism();
    }
    
    
    @Override
    public void setDataset(Data dataset) {
        this.dataset = dataset;
    }

    @Override
    public void setHierarchies(Map<Integer, Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
    }

    @Override
    public void setArguments(Map<String, Integer> arguments) {
        this.k = arguments.get("k");
    }

    @Override
    public void anonymize() {
        
        try {
            kAnonymization();
            createHistogram();
            addNoise();
            constructNoiseDataset();
        } catch (ParseException ex) {
            Logger.getLogger(DifferentialPrivacyAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotFoundValueException ex) {
            Logger.getLogger(DifferentialPrivacyAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    private void constructNoiseDataset(){
        Tool tool = null;
//        int datasetLength = this.histogram.entrySet().stream().mapToInt(entry -> entry.getValue()).sum();
        int histLength = this.histogram.entrySet().size();
        int numberOfThreads = (histLength < parallelism) ? histLength : parallelism;
        int splitSize = (histLength % numberOfThreads == 0) ? histLength / numberOfThreads : histLength / numberOfThreads + 1;
        int i=0;
        
        int seedValue = 10;
        Set<Map.Entry<List, Integer>> setHist = this.histogram.entrySet();
        List<Map.Entry<List, Integer>> arrHist = new ArrayList(setHist);
        Collections.shuffle(arrHist, new Random(seedValue));
        setHist = new HashSet(arrHist);
        
         
//        Integer[] qidCols = (Integer[])this.hierarchies.keySet().toArray();
        Integer[] qidCols = Arrays.copyOf(this.hierarchies.keySet().toArray(),this.hierarchies.keySet().size(),Integer[].class);
        int start = 0;
        if(this.dataset instanceof TXTData){
            int anonymLength = this.anonymizeDataset.length;
            int numberOfThreadsAnonym = (anonymLength < parallelism) ? anonymLength : parallelism;
            int splitSizeAnonym = (anonymLength % numberOfThreadsAnonym == 0) ? anonymLength / numberOfThreadsAnonym : anonymLength / numberOfThreadsAnonym + 1;
            for(List<Entry<List,Integer>> part : Iterables.partition(setHist, splitSize)){
                int end = (splitSizeAnonym < anonymLength - start) ? start + splitSizeAnonym : anonymLength;
                System.out.println("Thread "+i+" Start: "+start+" End: "+end);
                System.out.println("part noise Hist "+i+": "+part);
                tool = new Tool(part,this.anonymizeDataset,start,end,qidCols,tool,i++);
                start += splitSizeAnonym;
                pool.execute(tool);
            }
        }
        else{
            for(List<Entry<List,Integer>> part : Iterables.partition(this.histogram.entrySet(), splitSize)){
//                System.out.println("Thread "+i+" Start: "+start+" End: "+end);
                System.out.println("part noise Hist "+i+": "+part);
                tool = new Tool(part,qidCols,tool,i++);
                pool.execute(tool);
            }
        }
        
        for (Tool t = tool; t != null; t = t.nextJoin){       
            noiseDataset = mergeArrays(noiseDataset, (Object[][])t.join());
        }
        
        if(this.dataset instanceof DiskData){
            ((DiskData)this.dataset).setDifferentialData(noiseDataset,this.hierarchies);
        }
        
    }
    
    private void addNoise(){
        Tool tool = null;
        Random ran = new Random();
        LaplacianNoiseGenerator lg = new LaplacianNoiseGenerator(ran.nextLong(),0,1);
        int i=0;
        int datasetLength = this.histogram.entrySet().size();
        int numberOfThreads = (datasetLength < parallelism) ? datasetLength : parallelism;
        int splitSize = (datasetLength % numberOfThreads == 0) ? datasetLength / numberOfThreads : datasetLength / numberOfThreads + 1;
        
        int seedValue = 10;
        Set<Map.Entry<List, Integer>> setHist = this.histogram.entrySet();
        List<Map.Entry<List, Integer>> arrHist = new ArrayList(setHist);
        Collections.shuffle(arrHist, new Random(seedValue));
        setHist = new HashSet(arrHist);
        for(List<Entry<List,Integer>> part : Iterables.partition(setHist, splitSize)){
            System.out.println("part Hist "+i+": "+part);
            tool = new Tool(part,lg,tool,i++);
            pool.execute(tool);
        }
        
        this.histogram = new HashMap();
        for (Tool t = tool; t != null; t = t.nextJoin){       
            mergeMaps(this.histogram, (Map<List,Integer>)t.join());
        }
    }
    
    
    private void createHistogram(){
        if(this.dataset instanceof TXTData){
            int datasetLength = this.anonymizeDataset.length;
            System.out.println("Dataset Diff Len: "+datasetLength);
            int numberOfThreads = (datasetLength < parallelism) ? datasetLength : parallelism;
            int splitSize = (datasetLength % numberOfThreads == 0) ? datasetLength / numberOfThreads : datasetLength / numberOfThreads + 1;

    //        Integer[] qidCols = this.hierarchies.keySet().toArray();
            Integer[] qidCols = Arrays.copyOf(this.hierarchies.keySet().toArray(),this.hierarchies.keySet().size(),Integer[].class);

            for(Integer col : qidCols){
                System.out.println("QID: "+col);
            }

            System.out.println("SplitSize "+splitSize);
            System.out.println("Threads "+numberOfThreads);


            Tool tool = null;
            for (int i = 0; i < datasetLength; i += splitSize){
                int start = i;
                int end = (splitSize < datasetLength - i) ? i + splitSize : datasetLength;
    //            System.out.println("Thread "+i+": start: "+start+" end: "+end);
                tool = new Tool(this.anonymizeDataset,qidCols,start,end,tool,i);
                pool.execute(tool);
            }

            this.histogram = new HashMap();
            for (Tool t = tool; t != null; t = t.nextJoin){       
                mergeMaps(this.histogram, (Map<List,Integer>)t.join());
            }
        }
        else{
            this.histogram = ((DiskData) this.dataset).createAnonymHist(getAttrNames(), hierarchies);
        }
    }
    
    private void kAnonymization() throws ParseException, NotFoundValueException{
        Map<String, Integer> args = new HashMap<>();
        args.put("k", this.k);
        Algorithm algorithm = new ParallelFlash();
        

        algorithm.setDataset(this.dataset);
        algorithm.setHierarchies(this.hierarchies);

        algorithm.setArguments(args);

        algorithm.anonymize();

        Set<GridNode> results = (Set<GridNode>) algorithm.getResultSet();

        String solution = this.getSolution(results);
        buildTransforamtion(solution);
        System.out.println("Solution "+solution);
//        anonymizeTable();
        AnonymizedDataset anonData = new AnonymizedDataset(this.dataset,0,this.dataset.getDataLenght(),solution,this.hierarchies,null,getAttrNames(),null,true);
        anonData.renderAnonymizedTable();
        this.anonymizeDataset = anonData.getAnonymTable();
        
        for(int i=0; i<5; i++){
            for(int j=0; j<this.anonymizeDataset[0].length; j++){
                System.out.print(this.anonymizeDataset[i][j]+",");
            }
            System.out.println();
        }
        
        if(this.dataset instanceof DiskData){
            DiskData ddata = (DiskData) this.dataset;
            ddata.createAnonymizedTableDiff();
            ddata.createAnonymizedQuery();
            ddata.fillAnonymizedTable(anonymizeDataset);
            this.anonymizeDataset = null;
        }
        
        
//        for(int j=0; j<anonymizeDataset[0].length; j++){
//            System.out.println("Column "+j+": "+anonymizeDataset[0][j]);
//        }
//        this.setSelectedNode(session, solution);
//        this.getAnonDataSet(0, 0, session);
    }
    
    
    private String getAttrNames(){
        boolean FLAG = false;
        String selectedAttrNames = null;
        for (Map.Entry<Integer, Hierarchy> entry : this.hierarchies.entrySet()) {
            if ( FLAG == false){
                FLAG = true;
                selectedAttrNames = this.dataset.getColumnByPosition((Integer)entry.getKey());
            }
            else{
                selectedAttrNames = selectedAttrNames + "," + this.dataset.getColumnByPosition((Integer)entry.getKey());

            }
        }
        
        return selectedAttrNames;
    }
    
    
    
    
    
    private void buildTransforamtion(String solutionNode){
        if ( solutionNode != null){
            if (solutionNode.contains(",")){
                String[]temp = solutionNode.split(",");
                transformation = new int[temp.length];
                for (int  i = 0 ; i < temp.length ; i ++){
                    transformation[i] = Integer.parseInt(temp[i]);
                }
            }
            else{
                transformation = new int[1];
                transformation[0] = Integer.parseInt(solutionNode);
            }
        }
    }
    
    
    private String getSolution(Set<GridNode> results){
        
        Set<GridNode> infoLossFirstStep = new HashSet<>();
        Set<GridNode> infoLossSecondStep = new HashSet<>();
        int minSum = 0;
        int []minHierArray;
        int minHier;
        GridNode solution = null;
        String solutionStr = null;
        
        boolean FLAG = false;
        System.out.println("All results "+results);
        
        
        //first step, sum of levels
        for ( GridNode n : results){
            if (FLAG == false){
                minSum = n.getLayer();
                FLAG = true;
            }
            else{
                if ( minSum > n.getLayer()){
                    minSum = n.getLayer();
                }
            }
        }
        
        for ( GridNode n : results){
            if ( minSum == n.getLayer()){
                infoLossFirstStep.add(n);
            }         
        }
        
        //second step, min max hierarchy
        minHierArray = new int[infoLossFirstStep.size()];
        int counter = 0;
        for ( GridNode n : infoLossFirstStep){
            int []temp = n.getArray();
            minHierArray[counter] = Ints.max(temp);
            counter++;
        }
//        System.out.println("Info loass "+Arrays.toString(minHierArray));
        minHier = Ints.min(minHierArray);

        for ( GridNode n : infoLossFirstStep){
            int []temp = n.getArray();
            if (minHier == Ints.max(temp)){
                infoLossSecondStep.add(n);
            }
        }
 
        //third step, choose the first one
        for ( GridNode n : infoLossSecondStep){
            solution = n;
            break;
        }
        
        System.out.println("Solutions accepted "+infoLossSecondStep);
        
        solutionStr = solution.toString();
        solutionStr = solutionStr.replace("[","");
        solutionStr = solutionStr.replace("]","");
        solutionStr = solutionStr.replace(" ", "");
//        System.out.println("solution = " + solutionStr);
        
        
        return solutionStr;
    }
    
    private void mergeMaps(Map<List, Integer> mainMap,
        Map<List, Integer> secondaryMap){
//        System.out.println("Mpainei sto merge");
        for(List gRow : secondaryMap.keySet()){
            Integer mainMapValue = mainMap.get(gRow);
            
            if(mainMapValue == null){
//                System.out.println("No Value: "+gRow+" "+secondaryMap.get(gRow));
                mainMap.put(gRow, secondaryMap.get(gRow));
            }
            else{
//                System.out.println("Contain Value: "+gRow+" "+ secondaryMap.get(gRow) + mainMapValue);
                mainMap.put(gRow, secondaryMap.get(gRow) + mainMapValue);
            }
        }
    }
    
    private Object[][] mergeArrays(Object[][] mainArr, Object[][] secondaryArr){
        List<Object[]> toShuffle;
        if(mainArr == null){
            toShuffle = Arrays.asList(secondaryArr);
            Collections.shuffle(toShuffle);
            return toShuffle.toArray(new Object[0][0]);
        }
        else{
            Object[][] result = new Object[mainArr.length + secondaryArr.length][];

            System.arraycopy(mainArr, 0, result, 0, mainArr.length);
            System.arraycopy(secondaryArr, 0, result, mainArr.length, secondaryArr.length);
            
            toShuffle = Arrays.asList(result);
            Collections.shuffle(toShuffle);
            
            
        }
        
        return toShuffle.toArray(new Object[0][0]);
    }

    @Override
    public Object getResultSet() {
        return this.noiseDataset;
    }

    @Override
    public Graph getLattice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAnonymousResult(GridNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
