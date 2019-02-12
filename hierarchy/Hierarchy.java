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
package hierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import data.Data;
import dictionary.DictionaryString;
import graph.Graph;
import java.util.Date;

/**
 * Interface of the hierarchy
 * @author jimakos
 * @param <T>
 */
public interface Hierarchy <T> {
    public int[][] getHierarchy();
    public void setHierarchy();
    public int getHierarchyLength();
    public void print();
    
    public void load();
    public List<T> getChildren(T parent);
    public Integer getLevel(T node);
    public Integer getLevel(double nodeId);
    
    public String getNodesType();
    public T getParent(T node);
//    public List<T> getSiblings(T node);
    public T getRoot();
    public String getName();
    public Map<Integer, ArrayList<T>> getAllParents();
    public void export(String file);
    public void findAllParents();
    public boolean contains(T o);

    public Integer getHeight();
//    public void setHierachyType(String type);
    public String getHierarchyType();
    
    public void add(T newObj, T parent);
    public void clear();
    public void edit(T oldValue, T newValue);
    public Map<Integer, Set<T>> remove(T obj);
    public Map<Integer,Set<T>> dragAndDrop(T firstObj,T lastObj);
    public Map<Integer,Set<T>> BFS(T firstnode,T lastNode);
    
    public void computeWeights(Data dataset, String column);
    public Integer getWeight(T node);
    public Integer getWeight(double nodeId);
    public int getLevelSize(int level);
    
    public void autogenerate();
    
    public T checkColumn(int column, Data dataset);
    public DictionaryString getDictionary();
    
    public boolean validCheck(String parsePoint);
    public void transformParents();
    public  Map<Integer,Integer> getParentsInteger();
    public T getParent(Double d);
    public T getParent(Date d); 
    public double getParentId(double d); 
    public Set<Double> getChildrenIds(double d);
    public void buildDictionary(DictionaryString dictionary);
    public List<Integer> getNodeIdsInLevel(int level);
    public void setNodesType(String nodesType);
    public int findAllChildren(T node, int sum);
    
    public String getInputFile();
    
    public Graph getGraph(String node,int level);
    
    public String checkHier();
    public int translateDateViaLevel(int level);
}
