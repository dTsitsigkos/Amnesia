/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.differentialprivacy;

import algorithms.parallelflash.Worker;
import hierarchy.Hierarchy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author dimak
 */
public class Tool extends RecursiveTask<Object> {
    
    private int toolId = -1;
    private int start,end;
    public Tool nextJoin = null;
    private Map<List,Integer> histogram;
    private Object[][] anonymData;
    private Integer[] qidCols;
    private static int caseFunc;
    private Map<List,Integer> noiseHist;
    private LaplacianNoiseGenerator laplaceNoise;
    private List<Map.Entry<List,Integer>> toNoise;
    private Object[][] noiseDataset;
    
    public Tool(Object[][] anomData, Integer[] qc, int _start, int _end,Tool nj, int tid){
        this.toolId = tid;
        this.nextJoin = nj;
        this.anonymData = anomData;
        this.start = _start;
        this.end = _end;
        this.histogram = new HashMap();
        this.qidCols = qc;
        caseFunc = 0;
    }
    
    public Tool(List<Map.Entry<List,Integer>> originalHist, LaplacianNoiseGenerator lg,Tool nj, int tid){
        this.toolId = tid;
        this.nextJoin = nj;
        this.laplaceNoise = lg;
        this.toNoise = originalHist;
        this.histogram = new HashMap();
        caseFunc = 1;
        
    }
    
    public Tool(List<Map.Entry<List,Integer>> noiseHist, Object[][] anomData, int _start, int _end, Integer[] qc, Tool nj, int tid){
        this.toolId = tid;
        this.nextJoin = nj;
        this.qidCols = qc;
        this.start = _start;
        this.end = _end;
        this.toNoise = noiseHist;
        this.anonymData = anomData;
        caseFunc = 2;
    }
    
    public Tool(List<Map.Entry<List,Integer>> noiseHist, Integer[] qc, Tool nj, int tid){
        this.toolId = tid;
        this.nextJoin = nj;
        this.qidCols = qc;
        this.toNoise = noiseHist;
        this.anonymData = null;
        caseFunc = 2;
    }

    @Override
    protected Object compute() {
        switch(caseFunc){
            case 0:
                computeHistogram();
                break;
                
            case 1:
                addNoise();
                break;
                
            case 2:
                if(this.anonymData!=null){
                    reconstructDataset();
                }
                else{
                   reconstructDiskDataset();
                }
                return this.noiseDataset;
                
        }
        return this.histogram;
    }
    
    private void reconstructDiskDataset(){
        int rows = this.toNoise.stream().mapToInt(entry -> entry.getValue()).sum();
        this.noiseDataset = new Object[rows][this.qidCols.length];
        Random rand= new Random();
        List<Integer> randomNumbers = rand.ints(0, rows).distinct().limit(rows).boxed().collect(Collectors.toList());
        int i=0;
        for(Entry<List,Integer> noisePart : this.toNoise){
            for(int l=0; l<noisePart.getValue(); l++){
                for(int j=0; j<this.noiseDataset[0].length; j++){
                    this.noiseDataset[randomNumbers.get(i)][j] = noisePart.getKey().get(j);
                }
                i++;
            }
        }
    }
    
    private void reconstructDataset(){
        int rows = this.toNoise.stream().mapToInt(entry -> entry.getValue()).sum();
        this.noiseDataset = new Object[rows][this.anonymData[0].length];
        
        Random rand= new Random();
        int i=0,qi;
        List<Integer> randomNumbers = rand.ints(0, rows).distinct().limit(rows).boxed().collect(Collectors.toList());
        int iter = this.start;
        for(Entry<List,Integer> noisePart : this.toNoise){
            for(int l=0; l<noisePart.getValue(); l++){
                qi=0;
                for(int j=0; j<this.noiseDataset[0].length; j++){
                    if(ArrayUtils.contains(this.qidCols,j)){
                        this.noiseDataset[randomNumbers.get(i)][j] = noisePart.getKey().get(qi++);
                    }
                    else{
                        if(iter < end){
                            this.noiseDataset[randomNumbers.get(i)][j] = this.anonymData[iter][j];
                        }
                        else{
                            int randomRow = rand.nextInt((this.anonymData.length - 1) + 1) + 0;
                            this.noiseDataset[randomNumbers.get(i)][j] = this.anonymData[randomRow][j];
                        }
                    }
                }
                i++;
                iter++;
            }
        }
        
    }
    
    private void addNoise(){
        for(Entry<List,Integer> combSupport :  this.toNoise){
            double noise = this.laplaceNoise.nextLaplacian();
            int newCount = combSupport.getValue()+((int)Math.rint(noise));
            if(newCount < 0){
                this.histogram.put(combSupport.getKey(),0);
            }
            else{
                this.histogram.put(combSupport.getKey(),newCount);
            }
        }
    }
    
    
    private void computeHistogram(){
        for(int i=start; i<end; i++){
            List<Object> row = new ArrayList();
            for(int j=0; j<qidCols.length; j++){
                row.add(this.anonymData[i][qidCols[j]]);
            }
            
            if(this.histogram.containsKey(row)){
                this.histogram.put(row,this.histogram.get(row)+1);
            }
            else{
                this.histogram.put(row,1);
            }
        }
    }
    
}
