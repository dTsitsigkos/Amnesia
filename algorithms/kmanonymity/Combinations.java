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

import static com.google.common.collect.Collections2.orderedPermutations;
import hierarchy.Hierarchy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author serafeim
 */
public class Combinations {
    Trie tree = null;
    
    public Combinations(Trie _tree){
        tree = _tree;
    }
    
    public void combine(double[] arr, int r) {
        double[] res = new double[r];
        
        doCombine(arr, res, 0, 0, r);
        
    }
    
    private void doCombine(double[] arr, double[] res, int currIndex, int level, int r) {
        if(level == r){
            tree.insert(res);
            return;
        }
        for (int i = currIndex; i < arr.length; i++) {
            res[level] = arr[i];
            doCombine(arr, res, i+1, level+1, r);
            //way to avoid printing duplicates
            if(i < arr.length-1 && arr[i] == arr[i+1]){
                i++;
            }
        }
    }
    
    public static Set <double[]> getCombinations(Set<Double> domain,int size, Hierarchy h){
        ArrayList<Integer> index = null;
        int limit = -1;
        Set <double[]> distinct;
        
        //System.out.println("size = " + size);
        if ( size > domain.size() ){
            return null;
        }
        
        index = new ArrayList<Integer>();
        distinct = new HashSet<double[]>();
        limit = domain.size() - size;
        
        for ( int i = 0 ; i < limit ; i ++){
            index.add(0);
        }
        for ( int i = limit ; i < domain.size() ; i++ ){
            index.add(1);
        }
        
        double[] temp ;//= new Double[];
        boolean overlaps = false;
        
        int ii = 0;
        
        for (List<Integer> perm : orderedPermutations(index)) {
            temp = new double[size];
            int countTemp = 0;
            ii = 0;
            //System.out.println(perm.toString());
            for (double d : domain){
                //System.out.println("ii = " + ii);
                if ( perm.get(ii).equals(1)){
                    //System.out.println("i am here");
                    overlaps = false;
                    for (int j = 0 ; j < countTemp ; j ++ ){
                        if (sameBranch(d, temp[j], h)){
                            overlaps = true;
                            break;
                        }
                    }
                    if (!overlaps){
                        temp[countTemp] = (int)d;
                        countTemp ++;
                    }
                    else{
                        break;
                    }
                }
                ii++;
            }
            if ( !overlaps ){
                distinct.add(temp);
            }
        }
        
        return distinct;
    }
    
    private static boolean sameBranch(double first, double second, Hierarchy h){
        if(first == second)
            return true;
        double tmp = first;
        if (first < second){
            while (tmp != -1){
                tmp = h.getParentId(tmp);
                if (tmp == second)
                    return true;
                if (tmp > second)
                    return false;
            }
            return false;
        }
        tmp = second;
        while (tmp != -1){
            tmp = h.getParentId(tmp);
            if (tmp == first)
                return true;
            if (tmp > first)
                return false;
        }
        return false;
    }
    
}
