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
package algorithms.kmanonymity;

import algorithms.Algorithm;
import algorithms.mixedkmanonymity.MixedApriori;
import hierarchy.Hierarchy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author serafeim
 */
public class SolutionCombinations {
    Hierarchy hierarchy = null;
    Trie trie = null;
    int k = -1;
    Map<Set<Double>,Integer> trieSet=null;
    Algorithm alg = null;
    
    SolutionCombinations(Hierarchy _hierarchy, Trie _trie, int _k, Apriori _alg) {
        this.hierarchy = _hierarchy;
        this.trie = _trie;
        this.k = _k;
        this.alg = _alg;
    }
    
    public SolutionCombinations(Hierarchy _hierarchy, Map<Set<Double>,Integer> _trieSet, int _k, MixedApriori _alg){
        this.hierarchy = _hierarchy;
        this.trieSet = _trieSet;
        this.k = _k;
        this.alg = _alg;
    }
    
    public void createSolutionCombs(Map<Double, double[]> results, List<Double> base){
        List<List<Double>> domain = new ArrayList<>();
        List<Double> temp = null;
        
        for(int i=0; i<base.size(); i++){
            temp = new ArrayList<>();
            temp.add(base.get(i));
            getAllGeneralizations(base.get(i), temp);
            domain.add(temp);
        }
        
        double[] prefix = new double[base.size()];
        combGen(results, prefix, 0, base.size(), domain, base);
    }
    
    private void getAllGeneralizations(double current, List<Double> result) {
        double temp = hierarchy.getParentId((int)current);
        while(temp != -1){
            result.add(temp);
            temp = hierarchy.getParentId((int)temp);
        }
    }
    
    private void combGen(Map<Double, double[]> results, double[] prefix,
            int depth, int size, List<List<Double>> domain, List<Double> base) {
        boolean notThis;
        
        if(depth == size-1){
            double[] temp = null;
            for(int j=0; j<domain.get(depth).size(); j++){
                notThis = false;
                temp = new double[size];
                temp[depth] = domain.get(depth).get(j);
                for(int i=0; i<depth; i++){
                    if(domain.get(depth).get(j) == prefix[i]){
                        notThis = true;
                        break;
                    }
                    if(prefix[i] != -1){
                        if(sameBranch(prefix[i], domain.get(depth).get(j))){
                            if(domain.get(depth).get(j) > prefix[i]){
                                temp[i] = -1;
                            }
                            else{
                                temp[depth] = -1;
                                temp[i] = prefix[i];
                            }
                        }
                        else{
                            temp[i] = prefix[i];
                        }
                    }
                    else{
                        temp[i] = prefix[i];
                    }
                }
                if(notThis){
                    temp = null;
                    continue;
                }
                
                Arrays.sort(temp);
                if(trie!=null){
                    TrieNode node = trie.searchNode(temp);
//                    System.out.println("Potential comb "+temp);
                    if(node.getSupport() >= k){
                        double score = ((Apriori)alg).getAddedCost(temp, base);
                        if(!results.containsKey(score)){
                            results.put(score, temp);
//                            System.out.println("Results comb Set2 "+Arrays.toString(temp)+" score "+score);

                        }
                    }
                }
                else{
                    Set<Double> tempSet = new HashSet<Double>();
                    for(int l=0; l<temp.length; l++){
                        tempSet.add(temp[l]);
                    }
//                    System.out.println("Sol Comb: "+Arrays.toString(tempSet.toArray())+" score "+score);
                    if(tempSet.contains(-1.0)){
                        tempSet.remove(-1.0);
                    }
                    if(trieSet.containsKey(tempSet) && trieSet.get(tempSet) >= k){
                        double score = ((MixedApriori)alg).getAddedCost(temp, base);
//                        double score = trieSet.get(tempSet);
                        if(!results.containsKey(score)){
                            results.put(score, temp);
//                            System.out.println("Results comb Set "+Arrays.toString(tempSet.toArray())+" score "+score);
                        }
                    }
                }
                
            }
        }
        else{
            for(int j=0; j<domain.get(depth).size(); j++){
                double[] temp = prefix.clone();
                temp[depth] = domain.get(depth).get(j);
                notThis = false;
                for(int i=0; i<=(depth-1); i++){
                    if(domain.get(depth).get(j) == temp[i]){
                        notThis = true;
                        break;
                    }
                    if(temp[i] != -1){
                        if(sameBranch(temp[i], domain.get(depth).get(j))){
                            if(domain.get(depth).get(j) > temp[i]){
                                temp[i] = -1;
                            }
                            else{
                                temp[depth] = -1;
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
    
    public boolean sameBranch(double first, double second){
        if(first == second)
            return true;
        
        double tmp = first;
        if (first < second){
            while (tmp != -1){
                tmp = hierarchy.getParentId(tmp);
                if (tmp == second)
                    return true;
                if (tmp > second)
                    return false;
            }
            return false;
        }
        tmp = second;
        while (tmp != -1){
            tmp = hierarchy.getParentId(tmp);
            if (tmp == first)
                return true;
            if (tmp > first)
                return false;
        }
        return false;
    }
    
    private double getAddedCost(double[] comb, Hierarchy h) {
        double sum = 0;
        for(double d : comb){
            if(d != -1)
                sum += getCost(d, h);
        }
        return sum;
    }
    
    private double getCost(double value, Hierarchy h){
        int hbase = 10;
        return Math.pow(hbase, h.getLevel(value));
    }
    
}
