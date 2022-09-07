/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.mixedkmanonymity;

import static com.google.common.collect.Collections2.orderedPermutations;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.Pair;

/**
 *
 * @author nikos
 */
public class MixedCombinations {
    
    public static Set <Set<Pair<Integer,Object>>> getCombinations(Set<Pair<Integer,Object>> domain,int size, Map<Integer, Hierarchy> hierarchies){
        ArrayList<Integer> index = null;
        int limit = -1;
        Set <Set<Pair<Integer,Object>>> distinct;
        
        if ( size > domain.size() ){
            return null;
        }
        
        index = new ArrayList<Integer>();
        distinct = new HashSet<Set<Pair<Integer,Object>>>();
        limit = domain.size() - size;
        
        for ( int i = 0 ; i < limit ; i ++){
            index.add(0);
        }
        for ( int i = limit ; i < domain.size() ; i++ ){
            index.add(1);
        }
        
        List<Pair<Integer,Object>> temp;//= new Double[];
        boolean overlaps = false;
        
        int ii = 0;
        
        for (List<Integer> perm : orderedPermutations(index)) {
            temp = new ArrayList<Pair<Integer,Object>>();
            int countTemp = 0;
            ii = 0;
            for (Pair<Integer,Object> d : domain){
                
                if ( perm.get(ii).equals(1)){
                    
                    overlaps = false;
                    for (int j = 0 ; j < temp.size() ; j ++ ){
                        if (sameBranch(d, temp.get(j), hierarchies)){
                            overlaps = true;
                            break;
                        }
                    }
                    if (!overlaps){
                        
                        temp.add(d);
                        countTemp ++;
                    }
                    else{
                        break;
                    }
                }
                ii++;
            }
            if ( !overlaps ){
                distinct.add(new HashSet<Pair<Integer,Object>>(temp));
            }
        }
        return distinct;
    }
    
    public static Set <Set<Pair<Integer,Object>>> getSimpleCombinations(Set<Pair<Integer,Object>> domain,int size){
        ArrayList<Integer> index = null;
        int limit = -1;
        Set <Set<Pair<Integer,Object>>> distinct;
        
        if ( size > domain.size() ){
            return null;
        }
        
        index = new ArrayList<Integer>();
        distinct = new HashSet<Set<Pair<Integer,Object>>>();
        limit = domain.size() - size;
        
        for ( int i = 0 ; i < limit ; i ++){
            index.add(0);
        }
        for ( int i = limit ; i < domain.size() ; i++ ){
            index.add(1);
        }
        
        List<Pair<Integer,Object>> temp;//= new Double[];
        boolean overlaps = false;
        
        int ii = 0;
        
        for (List<Integer> perm : orderedPermutations(index)) {
            temp = new ArrayList<Pair<Integer,Object>>();
            int countTemp = 0;
            ii = 0;
            for (Pair<Integer,Object> d : domain){
                if ( perm.get(ii).equals(1)){
                    
                    overlaps = false;
                    for (int j = 0 ; j < temp.size() ; j ++ ){
                        if (d.getKey() == temp.get(j).getKey()){
                            overlaps = true;
                            break;
                        }
                    }
                    if (!overlaps){
                        
                        temp.add(d);
                        countTemp ++;
                    }
                    else{
                        break;
                    }
                }
                ii++;
            }
            if ( !overlaps ){
                distinct.add(new HashSet<Pair<Integer,Object>>(temp));
            }
        }
        return distinct;
    }
    
    public static boolean sameBranch(Pair<Integer,Object> first, Pair<Integer,Object> second, Map<Integer, Hierarchy> hierarchies){
        if(first.getKey() == second.getKey()){
            Hierarchy h = hierarchies.get(first.getKey());
            if(first.getValue() instanceof Double && second.getValue() instanceof Double){
                Double firstDouble=(Double) first.getValue();
                Double secondDouble = (Double) second.getValue();
                
                if(firstDouble==secondDouble){
                    return true;
                }
                Double tmp = firstDouble;
                if(firstDouble < secondDouble){
                    while(tmp != -1){
                        tmp = h.getParentId(tmp);
                        if (tmp.equals(secondDouble))
                            return true;
                        if (tmp > secondDouble)
                            return false;
                    }
                    return false;
                }
                tmp = secondDouble;
                while(tmp != -1){
                    tmp = h.getParentId(tmp);
                    if (tmp.equals(firstDouble))
                        return true;
                    if (tmp > firstDouble)
                        return false;
                }
                return false;
            }
            else if(first.getValue() instanceof RangeDouble && second.getValue() instanceof RangeDouble){
                RangeDouble firstRange= (RangeDouble) first.getValue();
                RangeDouble secondRange = (RangeDouble) second.getValue();
                
                if(firstRange.equals(secondRange)){
                    return true;
                }
                RangeDouble tmp = firstRange;
                if(firstRange.compareTo(secondRange) < 0){
                    while(tmp != null){
                        tmp = (RangeDouble) h.getParent(tmp);
                        if (tmp.equals(secondRange))
                            return true;
                        if (tmp.compareTo(secondRange) > 0)
                            return false;
                    }
                    return false;
                }
                tmp = secondRange;
                while(tmp != null){
                    tmp = (RangeDouble) h.getParent(tmp);
                    if (tmp.equals(firstRange))
                        return true;
                    if (tmp.compareTo(firstRange) > 0)
                        return false;
                }
                return false;
            }
            else if(first.getValue() instanceof RangeDate && second.getValue() instanceof RangeDate){
                RangeDate firstDate= (RangeDate) first.getValue();
                RangeDate secondDate = (RangeDate) second.getValue();
                
                if(firstDate.equals(secondDate)){
                    return true;
                }
                RangeDate tmp = firstDate;
                if(firstDate.compareTo(secondDate) < 0){
                    while(tmp != null){
                        tmp = (RangeDate) h.getParent(tmp);
                        if (tmp.equals(secondDate))
                            return true;
                        if (tmp.compareTo(secondDate) > 0)
                            return false;
                    }
                    return false;
                }
                tmp = secondDate;
                while(tmp != null){
                    tmp = (RangeDate) h.getParent(tmp);
                    if (tmp.equals(firstDate))
                        return true;
                    if (tmp.compareTo(firstDate) > 0)
                        return false;
                }
                return false;
            }
            else if(first.getValue() instanceof RangeDouble || second.getValue() instanceof RangeDouble){
                RangeDouble firstDouble;
                RangeDouble secondDouble;
                if(first.getValue() instanceof RangeDouble){
                    firstDouble = (RangeDouble) first.getValue();
                    secondDouble = (RangeDouble) h.getParent((Double) second.getValue());
                }
                else{
                    firstDouble = (RangeDouble) h.getParent((Double) first.getValue());
                    secondDouble = (RangeDouble) second.getValue();
                }

                    
                if(firstDouble.equals(secondDouble)){
                    return true;
                }
                RangeDouble tmp = firstDouble;
                if(firstDouble.compareTo(secondDouble) < 0){
                    while(tmp != null){
                        tmp = (RangeDouble) h.getParent(tmp);
                        if (tmp.equals(secondDouble))
                            return true;
                        if (tmp.compareTo(secondDouble) > 0)
                            return false;
                    }
                    return false;
                }
                tmp = secondDouble;
                while(tmp != null){
                    tmp = (RangeDouble) h.getParent(tmp);
                    if (tmp.equals(firstDouble))
                        return true;
                    if (tmp.compareTo(firstDouble) > 0)
                        return false;
                }
                return false;
            }
            else {
                RangeDate firstDate=null;
                RangeDate secondDate=null;
                if(first.getValue() instanceof RangeDate){
                    firstDate = (RangeDate) first.getValue();
                    try {
                        if((h.getDictionaryData().getIdToString(((Double)second.getValue()).intValue())).equals("NaN")){
                            secondDate = new RangeDate(null,null);
                        }
                        else{
                            secondDate = (RangeDate) h.getParent(new SimpleDateFormat("dd/MM/yyyy").parse(h.getDictionaryData().getIdToString(((Double)second.getValue()).intValue())));
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(MixedCombinations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    try {
                        if((h.getDictionaryData().getIdToString(((Double)first.getValue()).intValue())).equals("NaN")){
                            firstDate = new RangeDate(null,null);
                        }
                        else{
                            firstDate = (RangeDate) h.getParent(new SimpleDateFormat("dd/MM/yyyy").parse(h.getDictionaryData().getIdToString(((Double)first.getValue()).intValue())));
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(MixedCombinations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    secondDate = (RangeDate) second.getValue();
                }
                
                
                if(firstDate.equals(secondDate)){
                    return true;
                }
                RangeDate tmp = firstDate;
                if(firstDate.compareTo(secondDate) < 0){
                    while(tmp != null){
                        tmp = (RangeDate) h.getParent(tmp);
                        if (tmp.equals(secondDate))
                            return true;
                        if (tmp.compareTo(secondDate) > 0)
                            return false;
                    }
                    return false;
                }
                tmp = secondDate;
                while(tmp != null){
                    tmp = (RangeDate) h.getParent(tmp);
                    if (tmp.equals(firstDate))
                        return true;
                    if (tmp.compareTo(firstDate) > 0)
                        return false;
                }
                return false;
                
                
            }
        }
        return false;
    }
}
