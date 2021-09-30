/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.mixedkmanonymity;

import algorithms.Algorithm;
import algorithms.kmanonymity.Apriori;
import algorithms.kmanonymity.Trie;
import anonymizeddataset.AnonymizedDataset;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import data.Pair;

/**
 *
 * @author nikos
 */
public class MixedSolutionCombinations {
    Map<Integer,Hierarchy> hierarchies = null;
    Map<Set<Pair<Integer,Object>>,Integer>  trieRelational = null;
    int k = -1;
    Algorithm alg = null;
    
    MixedSolutionCombinations(Map<Integer,Hierarchy> _hierarchies, Map<Set<Pair<Integer,Object>>,Integer> _trie, int _k, MixedApriori _alg) {
        this.hierarchies = _hierarchies;
        this.trieRelational = _trie;
        this.k = _k;
        this.alg = _alg;
    }
    
    public void createSolutionCombs(Map<Double, List<Pair<Integer,Object>>> results, List<Pair<Integer,Object>> base){
        List<List<Pair<Integer,Object>>> domain = new ArrayList<>();
        List<Pair<Integer,Object>> temp = null;
        
        for(int i=0; i<base.size(); i++){
            temp = new ArrayList<>();
            temp.add(base.get(i));
            getAllGeneralizations(base.get(i), temp);
            domain.add(temp);
        }
        
        List<Pair<Integer,Object>> prefix = new ArrayList<Pair<Integer,Object>>(); 
        combGen(results, prefix, 0, base.size(), domain, base);
    }
    
    private void getAllGeneralizations(Pair<Integer,Object> current, List<Pair<Integer,Object>> result) {
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
               result.add(((MixedApriori)alg).convertToPair(current.getKey(), temp));
               temp = hierarchy.getParent(temp);
            }
        }
        else{
            temp =  hierarchy.getParentId((Double)current.getValue());
            while((Double)temp!=-1){
               result.add(((MixedApriori)alg).convertToPair(current.getKey(), temp));
               temp = hierarchy.getParentId((Double)temp);
            } 
        }
    }
    
    private void combGen(Map<Double, List<Pair<Integer,Object>>> results, List<Pair<Integer,Object>> prefix,
            int depth, int size, List<List<Pair<Integer,Object>>> domain, List<Pair<Integer,Object>> base) {
        
        boolean notThis;
        if(depth == size-1){
            Map<Integer,Pair<Integer,Object>> temp = null;
            for(int j=0; j<domain.get(depth).size(); j++){
                notThis = false;
                temp = new HashMap<Integer,Pair<Integer,Object>>();
                
//                for(int l=0; l<size; l++){
//                    temp.add(new Pair<Integer,Double>(0,0.0));
//                }
                
//                System.out.println("depth "+depth+" j "+j+" domain size "+domain.size()+" size list "+domain.get(depth).size());
                temp.put(depth,domain.get(depth).get(j));
                for(int i=0; i<depth; i++){
                    if(domain.get(depth).get(j).equals(prefix.get(i))){
                        notThis = true;
                        break;
                    }
                    if(prefix.get(i).getValue() instanceof Double){
                        if((Double)prefix.get(i).getValue() != -1.0){
                            if(MixedCombinations.sameBranch(prefix.get(i), domain.get(depth).get(j),this.hierarchies)){
                                if((Double)domain.get(depth).get(j).getValue() > (Double)prefix.get(i).getValue()){
                                    temp.put(i,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                                else{
                                    temp.put(depth,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                    temp.put(i,prefix.get(i));
                                }
                            }
                            else{
                                temp.put(i,prefix.get(i));
                            }
                        }
                        else{
                           temp.put(i,prefix.get(i)); 
                        }
                    }
                    else{
                       if(MixedCombinations.sameBranch(prefix.get(i), domain.get(depth).get(j),this.hierarchies)){
                           if(prefix.get(i).getValue() instanceof RangeDouble){
                                if(((RangeDouble)domain.get(depth).get(j).getValue()).compareTo((RangeDouble)prefix.get(i).getValue()) > 0){
                                    temp.put(i,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                                else{
                                    temp.put(depth,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                    temp.put(i,prefix.get(i));
                                }
                           }
                           else{
                               if(((RangeDate)domain.get(depth).get(j).getValue()).compareTo((RangeDate)prefix.get(i).getValue()) > 0){
                                    temp.put(i,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                                else{
                                    temp.put(depth,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                    temp.put(i,prefix.get(i));
                                }
                           }
                       }
                       else{
                           temp.put(i,prefix.get(i));
                        }
                    }
                    
                }
                if(notThis){
                    temp = null;
                    continue;
                }
                List<Pair<Integer,Object>> tempComb = new ArrayList<Pair<Integer,Object>>( temp.values());
                Collections.sort(tempComb,new Comparator<Pair<Integer,Object>>(){

                    @Override
                    public int compare(Pair<Integer, Object> o1, Pair<Integer, Object> o2) {
//                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                        return o1.getValue().compareTo(o2.getValue());
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
                                String valueDate = hierarchies.get(o1.getKey()).getDictionaryData().getIdToString(((Double)o1.getValue()).intValue());

                                return ((RangeDate)o2.getValue()).compareTo(AnonymizedDataset.getDateFromString(valueDate));

                            }
                            else{
                                if(o2.getValue() instanceof Double)  {
                                    String valueDate = hierarchies.get(o1.getKey()).getDictionaryData().getIdToString(((Double)o2.getValue()).intValue());

                                    return ((RangeDate)o1.getValue()).compareTo(AnonymizedDataset.getDateFromString(valueDate));

                                }
                                else{
                                    return ((RangeDate)o1.getValue()).compareTo((RangeDate)o2.getValue());
                                }
                            }
                        }catch(Exception e){
                            System.out.println("PRoble with parsing "+e.getMessage());
                            return 0;
                        }
                    }
                    
                });
                
                Set<Pair<Integer,Object>> tempRelSet = this.pureSet(new HashSet(tempComb));
//                System.out.println("Potential comb "+Arrays.toString(tempComb.toArray())+" k="+trieRelational.get(tempRelSet) +" pure comb "+tempRelSet +" size m "+size);
                if((trieRelational.containsKey(tempRelSet) && trieRelational.get(tempRelSet) >= k) || tempRelSet.size()<size){
//                    double score = trieRelational.get(tempRelSet);
                    double score = ((MixedApriori)alg).getAddedCost(tempComb, base);
                    if(!results.containsKey(score)){
                        results.put(score, tempComb);
//                        System.out.println("Result solution comb comb "+Arrays.toString(tempComb.toArray())+" score "+score);
                    }
                }
            }
        }
        else{
           for(int j=0; j<domain.get(depth).size(); j++){
               List<Pair<Integer,Object>> temp = (List)((ArrayList)prefix).clone();
               temp.add(depth,domain.get(depth).get(j));
               notThis = false;
               for(int i=0; i<=(depth-1); i++){
                    if(domain.get(depth).get(j).equals(temp.get(i))){
                        notThis = true;
                        break;
                    }
                    if(temp.get(i).getValue() instanceof Double){
                        if((Double)temp.get(i).getValue() != -1.0){
                            if(MixedCombinations.sameBranch(temp.get(i), domain.get(depth).get(j),this.hierarchies)){
                                if((Double) domain.get(depth).get(j).getValue() > (Double) temp.get(i).getValue()){
                                    temp.add(i,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                                else{
                                    temp.add(depth,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                            }
                        }
                    }
                    else{
                        if(temp.get(i).getValue() instanceof RangeDouble){
                            if(MixedCombinations.sameBranch(temp.get(i), domain.get(depth).get(j),this.hierarchies)){
                                if(((RangeDouble) domain.get(depth).get(j).getValue()).compareTo((RangeDouble) temp.get(i).getValue()) > 0){
                                    temp.add(i,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                                else{
                                    temp.add(depth,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                            }  
                        }
                        else{
                            if(MixedCombinations.sameBranch(temp.get(i), domain.get(depth).get(j),this.hierarchies)){
                                if(((RangeDate) domain.get(depth).get(j).getValue()).compareTo((RangeDate) temp.get(i).getValue()) > 0){
                                    temp.add(i,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                                else{
                                    temp.add(depth,((MixedApriori)alg).convertToPair(prefix.get(i).getKey(), -1.0));
                                }
                            }  
                        }
                        
                    }
               }
               if(notThis){
                   temp = null;
                   continue;
               }
               combGen(results, temp, depth+1, size, domain, base);
           } 
        }
    }
    
    Set<Pair<Integer,Object>> pureSet(Set<Pair<Integer,Object>> setComb){
        Set<Pair<Integer,Object>> pureCombination = new HashSet();
        for(Pair<Integer,Object> val : setComb){
            if(val.getValue() instanceof Double){
                if(!val.getValue().equals(-1.0))
                    pureCombination.add(val);
            }
            else{
                pureCombination.add(val);
            }
        }
        
        return pureCombination;
    }
}
