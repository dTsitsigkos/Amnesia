/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import hierarchy.ranges.RangeDouble;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author nikos
 */
public class ClusterDistTree {
    
    Centroid parents[][]; /// level,parents
    int depth;
//    Centroid[][][] centroids;  // last level 
    int children;
    int pointer_to_last;
    Map<Integer,Integer> levelsToSize;
    Centroid[] centroids;
    
    
    ClusterDistTree(Centroid[] c, int childs, int clusters){
        this.centroids = c;
        this.pointer_to_last = clusters-1;
        this.children = childs;
        int depth = 0;
        int numParents = (int) Math.ceil(((double)clusters)/childs);
        int remainder;
        this.levelsToSize = new HashMap();
        System.out.println("childs "+childs+" clusters "+clusters+" parents "+numParents);
        while(numParents > 1){
            depth++;
            numParents = (int) Math.ceil(((double)numParents)/childs);
        }
        this.depth = depth;
        this.parents = new Centroid[depth][];
        System.out.println("parents "+numParents);
        
        while(depth!=0){
            if(depth == this.depth){
                numParents = (int) Math.ceil(((double)clusters)/childs);
                remainder = clusters % childs;
            }
            else{
                numParents = (int) Math.ceil(((double)numParents)/childs);
                remainder = clusters % numParents;
            }
            this.parents[depth-1] = new Centroid[numParents];
            levelsToSize.put(depth, numParents);
            depth--;
        }
        System.out.println("Start lower level");
        for(int i=0; i<this.depth; i++){
            int counter=0;
            if(i==0){
                System.out.println("Start lower level");
                RangeDouble range = new RangeDouble(1.0,(double)childs);
                if(range.getUpperBound().intValue() > clusters){
                    range.setUpperBound((double)clusters);
                }
                while(range.getLowerBound().intValue()<=clusters){
//                    System.out.println("First Range "+range);
                    int start=0;
                    boolean goToNextRange = false;
                    while(c[range.getLowerBound().intValue()+start]==null){
                        if((range.getLowerBound().intValue()+start) == range.getUpperBound().intValue()){
                            range = new RangeDouble(range.getUpperBound()+1,range.getUpperBound()+childs);

                            if(range.getUpperBound().intValue() > clusters){
                                range.setUpperBound((double)clusters);
                            }
                            goToNextRange=true;
                            break;
                            
                        }
                        start++;
                    }
                    
                    if(goToNextRange){
                        counter++;
                        continue;
                    }
                    Centroid parentCentr = new Centroid(c[range.getLowerBound().intValue()+start]);
                    for(int j=range.getLowerBound().intValue()+start+1; j<=range.getUpperBound().intValue(); j++){
                        if(c[j]!=null){
                            parentCentr.update(c[j]);
                        }
                    }
                    this.parents[this.depth-1][counter++] = parentCentr;
                    range = new RangeDouble(range.getUpperBound()+1,range.getUpperBound()+childs);

                    if(range.getUpperBound().intValue() > clusters){
                        range.setUpperBound((double)clusters);
                    }
                    
                }
            }
            else{
                System.out.println("Start other level "+i);
                int upperBound = this.parents[this.depth-i].length-1;
                RangeDouble range = new RangeDouble(0.0,(double)this.children-1);
                
                if(range.getUpperBound().intValue() > upperBound){
                    range.setUpperBound((double)upperBound);
                }
                
                while(range.getLowerBound().intValue()<=upperBound){
                    int start = 0;
                    boolean goToNextRange = false;
                    while(this.parents[this.depth-i][range.getLowerBound().intValue()+start]==null){
                        if((range.getLowerBound().intValue()+start) == range.getUpperBound().intValue()){
                            range = new RangeDouble(range.getUpperBound()+1,range.getUpperBound()+childs);

                            if(range.getUpperBound().intValue() > upperBound){
                                range.setUpperBound((double)upperBound);
                            }
                            goToNextRange=true;
                            break;
                            
                        }
                        start++;
                    }
                    
                    if(goToNextRange){
                        counter++;
                        continue;
                    }
                    Centroid parentCentr = new Centroid(this.parents[this.depth-i][range.getLowerBound().intValue()+start]);
                    for(int j=range.getLowerBound().intValue()+start+1; j<=range.getUpperBound().intValue(); j++){
                        if(this.parents[this.depth-i][j]!=null)
                            parentCentr.update(this.parents[this.depth-i][j]);
                    }
                    this.parents[this.depth-(i+1)][counter++] = parentCentr;
                    range = new RangeDouble(range.getUpperBound()+1,range.getUpperBound()+children);
                    
                    
                    if(range.getUpperBound().intValue() > upperBound){
                        range.setUpperBound((double)upperBound);
                    }
                }
            }
        }
    }
    
    public RangeDouble findRange(Double[] record){
        RangeDouble range = null;
        for(int i=0; i<this.depth; i++){
            int parentPos = -1;
            double minDistance = 10000000.0;
            double distance;
            if(i==0){
                for(int j=0; j<this.parents[i].length; j++){
                    if(this.parents[i][j]!=null){
                        distance = this.parents[i][j].computeDistance(record,true);
                        if(distance < minDistance){
                            parentPos = j;
                            minDistance = distance;
                        }
                    }
                }
                range = new RangeDouble((double)parentPos*this.children,(double)parentPos*this.children+this.children-1);
                
                if(this.levelsToSize.get(2) != null && range.getUpperBound().intValue() >= this.levelsToSize.get(2)){
                    range.setUpperBound((double)this.levelsToSize.get(2)-1);
                }
                
            }
            else{
                for(int j=range.getLowerBound().intValue(); j<=range.getUpperBound().intValue(); j++){
                    if(this.parents[i][j]!=null){
                        distance = this.parents[i][j].computeDistance(record,true);
                        if(distance < minDistance){
                            parentPos = j;
                            minDistance = distance;
                        }
                    }
                }
                range = new RangeDouble((double)parentPos*this.children,(double)parentPos*children+children-1);
                if(i!=this.depth-1){
                    if(range.getUpperBound().intValue() >= this.levelsToSize.get(i+2)){
                        range.setUpperBound((double)this.levelsToSize.get(i+2)-1);
                    }
                }
            }
        }
        return range;
    }
    
    public void print(){
        System.out.println("Levels "+this.depth);
        for(Entry<Integer,Integer> entry : this.levelsToSize.entrySet()){
            System.out.println("Level "+entry.getKey()+" size "+entry.getValue());
        }
        System.out.println("Pointer last "+this.pointer_to_last);
        System.out.println("Children "+this.children);
    }
    
    public int get_pointer_last(){
        return this.pointer_to_last;
    }
}
