/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.demographics;

import algorithms.Algorithm;
import algorithms.flash.LatticeNode;
import data.Data;
import data.DiskData;
import data.Pair;
import dictionary.DictionaryString;
import graph.Graph;
import hierarchy.Hierarchy;
import hierarchy.ranges.RangeDouble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author dimak
 */
public class DemographicAlgorithm implements Algorithm {
    
    private Data data;
    private Map<Integer, Hierarchy> hierarchies;
    private int k;
    private Map<Integer,Map<Integer,Object>> resultSet;

    @Override
    public void setDataset(Data dataset) {
        this.data = dataset;
    }

    @Override
    public void setHierarchies(Map<Integer, Hierarchy> _hierarchies) {
        this.hierarchies = _hierarchies;
    }

    @Override
    public void setArguments(Map<String, Integer> arguments) {
        this.k = arguments.get("k");
    }

    @Override
    public void anonymize() {
        this.resultSet = new HashMap();
        if(this.data instanceof DiskData){
            System.out.println("demographic diskdata");
            DiskData diskdata = (DiskData) this.data;
            diskdata.cloneOriginalToAnonymize();
            for(Entry<Integer, Hierarchy> enrtyHier : this.hierarchies.entrySet()){
                
                List<Pair<Double[],List<Integer>>> problematicRecsVal = diskdata.getSmallRecordsClusters(k, new HashSet(Arrays.asList(enrtyHier.getKey())), false);
                for(Pair<Double[],List<Integer>> value : problematicRecsVal){
                    Double problematicValue = value.getKey()[enrtyHier.getKey()+1];
                    System.out.println("problematic "+problematicValue);
                    System.out.println("population "+enrtyHier.getValue().getPopulation(problematicValue.doubleValue()));
                    if(enrtyHier.getValue().getPopulation(problematicValue.doubleValue()) < this.k){
                        Object parent = (Object) enrtyHier.getValue().getParent(problematicValue, k);
                        if(parent instanceof RangeDouble){
                            System.out.println("Mpainei edw");
                            DictionaryString dict = enrtyHier.getValue().getDictionary();
                            int idRange;
                            if(dict == null){
                                dict = new DictionaryString();
                                enrtyHier.getValue().setDictionaryData(dict);
                            }

                            String strCommon = parent.toString();

                            if(dict.containsString(strCommon)){
                                idRange = dict.getStringToId(strCommon);
                            }
                            else{
                                idRange = dict.getMaxUsedId() + 1;
                                dict.putIdToString(idRange, strCommon);
                                dict.putStringToId(strCommon, idRange);
                            }
                            diskdata.setAnonymizedValue(enrtyHier.getKey(), idRange, new HashSet(value.getValue()));
                        }
                        else{
                            diskdata.setAnonymizedValue(enrtyHier.getKey(), (Double)parent, new HashSet(value.getValue()));
                        }
                        
                        
                        for(Integer row : value.getValue()){
                            
                            if(this.resultSet.containsKey(row)){
                                this.resultSet.get(row).put(enrtyHier.getKey(), parent);
                            }
                            else{
                                this.resultSet.put(row, new HashMap<Integer,Object>(){{put(enrtyHier.getKey(), parent);}});
                            }
                        }
                    }
                }
            }
        }
        else{
            double[][] dataset = this.data.getDataSet();
            for(Entry<Integer, Hierarchy> enrtyHier : this.hierarchies.entrySet()){
                Map<Double,List<Integer>> problematicVals = new HashMap();
                
                for(int row=0; row<dataset.length; row++){
                    if(problematicVals.containsKey(dataset[row][enrtyHier.getKey()])){
                        problematicVals.get(dataset[row][enrtyHier.getKey()]).add(row);
                    }
                    else{
                        List<Integer> rowsIds = new ArrayList<Integer>();
                        rowsIds.add(row);
                        problematicVals.put(dataset[row][enrtyHier.getKey()], rowsIds);
                    }
                }
                
                List<Entry<Double,List<Integer>>> problematicRecs = problematicVals.entrySet().stream()
                    .filter(entry -> entry.getValue().size() < this.k)
                    .collect(Collectors.toList());
                
                for(Entry<Double, List<Integer>> value : problematicRecs){
                    System.out.println("problematic "+value.getKey());
                    System.out.println("population "+enrtyHier.getValue().getPopulation(value.getKey().doubleValue()));
                    if(enrtyHier.getValue().getPopulation(value.getKey().doubleValue()) < this.k){
                        Object parent = (Object) enrtyHier.getValue().getParent(value.getKey(), k);
                        for(Integer rowId : value.getValue()){
                            if(this.resultSet.containsKey(rowId)){
                                this.resultSet.get(rowId).put(enrtyHier.getKey(), parent);
                            }
                            else{
                                this.resultSet.put(rowId, new HashMap<Integer,Object>(){{put(enrtyHier.getKey(), parent);}});
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object getResultSet() {
        return this.resultSet;
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
