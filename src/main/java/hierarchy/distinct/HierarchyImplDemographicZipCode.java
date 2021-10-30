/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.distinct;

import controller.AppCon;
import data.Data;
import dictionary.DictionaryString;
import exceptions.LimitException;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.DemographicInfo;
import hierarchy.Hierarchy;
import static hierarchy.Hierarchy.online_limit;
import static hierarchy.Hierarchy.online_version;
import hierarchy.NodeStats;
import static hierarchy.distinct.HierarchyImplString.dict;
import hierarchy.ranges.HierarchyImplRangeDemographicAge;
import hierarchy.ranges.RangeDouble;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dimak
 */
public class HierarchyImplDemographicZipCode extends HierarchyImplString {
    
    String country;
    Map<Double,Integer> nodeToPopulation = new HashMap();
    Map<String,Integer> nodeToPopulationStr = new HashMap();
    
    
    public HierarchyImplDemographicZipCode(String inputFile, DictionaryString dictData) {
        super(inputFile, dictData);
    }
    
    public HierarchyImplDemographicZipCode(String _name, String _nodesType,String _country, DictionaryString dictData){
        super(_name, _nodesType, dictData);
        this.country = _country;
        this.hierarchyType = "distinct_demographic";
                   
    }
    
    public static ArrayList<String> getCountries(){
        if(DemographicInfo.countryDistributionZip == null){
            try {
                DemographicInfo.ReadObject();
            } catch (IOException ex) {
                Logger.getLogger(HierarchyImplRangeDemographicAge.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(HierarchyImplRangeDemographicAge.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(HierarchyImplDemographicZipCode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ArrayList<String> countries = new ArrayList(DemographicInfo.countryDistributionZip.keySet());
        Collections.sort(countries);
        return countries;
    }

    @Override
    public int[][] getHierarchy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setHierarchy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDictionaryData(DictionaryString dict) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getHierarchyLength() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load() throws LimitException {
       if(this.inputFile == null){
           this.autogenerate();
       }
       this.findAllParents();
    }

    @Override
    public List<Double> getChildren(Double parent) {
        return this.children.get(parent);
    }

    @Override
    public Integer getLevel(Double node) {
        NodeStats nodeStats = this.stats.get(node);
        if(nodeStats == null){
            return null;
        }
        
        return nodeStats.getLevel();
    }

    @Override
    public Integer getLevel(double nodeId) {
        if(this.getLevel((Double)nodeId) == null){
            return null;
        }
        return (this.height - this.getLevel((Double)nodeId) - 1) ;
    }

    @Override
    public String getNodesType() {
        return nodesType;
    }

    @Override
    public Double getParent(Double node) {
        return this.parents.get(node);
    }

    @Override
    public void setLevel(int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<Integer, ArrayList<Double>> getAllParents() {
        return allParents;
    }

    @Override
    public void export(String file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void findAllParents() {
        List<Double> tempChild = null;
        int i = 0;
        int level = 0;
        ArrayList<Double> tempArr1 = new ArrayList<>();
        ArrayList<Double> tempArr2 = new ArrayList<>();
        
        tempArr1.add(root);
        allParents.put(level, tempArr1);
        tempArr2 = (ArrayList<Double>) tempArr1.clone();
        level ++;
        
        while (level <= height - 1 ){
            tempArr1 = new ArrayList<>();
            for (Double x : tempArr2) {
                tempChild = children.get(x);
                if ( tempChild != null){
                    for ( i = 0 ; i < tempChild.size() ; i ++ ){
                        tempArr1.add(tempChild.get(i));
                    }
                }
            }
            allParents.put(level, tempArr1);
            tempArr2 = (ArrayList<Double>) tempArr1.clone();
            level ++;
        }
    }

    @Override
    public boolean contains(Double o) {
       return stats.get(o) != null;
    }

    @Override
    public Integer getHeight() {
        return this.height;
    }

    @Override
    public String getHierarchyType() {
        return this.hierarchyType;
    }

    @Override
    public void add(Double newObj, Double parent) throws LimitException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
//        siblings = new HashMap<>();
        allParents = new HashMap<>();
        this.nodeToPopulation = new HashMap();
        this.nodeToPopulationStr = new HashMap();
        
        DemographicInfo.countryDistributionZip = null;
    }

    @Override
    public void clearAprioriStructures() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void edit(Double oldValue, Double newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<Double>> remove(Double obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<Double>> dragAndDrop(Double firstObj, Double lastObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public boolean checkExistance(Double d) {
        if(d.doubleValue() != root.doubleValue()){
            return this.parents.get(d) != null;
        }
        else{
            return true;
        }
    }



    @Override
    public int getLevelSize(int level) {
        return this.allParents.get(this.height - level - 1).size();
    }

    @Override
    public void autogenerate() throws LimitException {
        int strCount;
        if(dictData.isEmpty() && dict.isEmpty()){
            System.out.println("Both empty");
            strCount = 1;
        }
        else if(!dictData.isEmpty() && !dict.isEmpty()){
            System.out.println("Both have values");
            if(dictData.getMaxUsedId() > dict.getMaxUsedId()){
                strCount = dictData.getMaxUsedId()+1;
            }
            else{
                strCount = dict.getMaxUsedId()+1;
            }
        }
        else if(dictData.isEmpty()){
            System.out.println("Dict data empty");
            strCount = dict.getMaxUsedId()+1;
        }
        else{
            System.out.println("Dict hier empty");
            strCount = dictData.getMaxUsedId()+1;
        }
        
        
        this.nodeToPopulationStr = new HashMap(DemographicInfo.countryDistributionZip.get(this.country));
        ArrayList<String> lastChilds = new ArrayList<String>(this.nodeToPopulationStr.keySet());
        this.counterNodes += lastChilds.size();
        if(AppCon.os.equals(online_version) && counterNodes > online_limit){
            throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
        }
        this.height = lastChilds.get(0).length()+1;
        System.out.println("zip "+lastChilds.get(0));
        int curLevel = this.height - 1;
        while(curLevel>=1){
            if(curLevel == 1){
                String rootStr = fillStars(lastChilds.get(0).substring(0,curLevel-1),this.height-1);
                
                if(dictData.containsString(rootStr)){
                    root = dictData.getStringToId(rootStr).doubleValue();

                    dict.putIdToString(root.intValue(), rootStr);
                    dict.putStringToId(rootStr, root.intValue());
                }
                else{

                    if(!dict.containsString(rootStr)){
                        dict.putIdToString(strCount, rootStr);
                        dict.putStringToId(rootStr, strCount++);


                    }
                    root = dict.getStringToId(rootStr).doubleValue();
                }
                
                this.stats.put(root, new NodeStats(0));
                this.children.put(root, new ArrayList<Double>());
                this.nodeToPopulation.put(root,0);
                for(String child : lastChilds){
                    Double childId = dict.getStringToId(child).doubleValue();
                    this.parents.put(childId, root);
                    this.children.get(root).add(childId);
                    this.nodeToPopulation.put(root,this.nodeToPopulation.get(root)+this.nodeToPopulation.get(childId));
                }
            }
            else{
                Set<String> nextLevel = new TreeSet<String>();
                for(String child : lastChilds){
                    String parent = fillStars(child.substring(0,curLevel-1),this.height-1);
                    Double parentId,childId;
                    
                    
                    if(dictData.containsString(parent)){
                        parentId = dictData.getStringToId(parent).doubleValue();

                        dict.putIdToString(parentId.intValue(), parent);
                        dict.putStringToId(parent, parentId.intValue());
                    }
                    else{

                        if(!dict.containsString(parent)){
                            dict.putIdToString(strCount, parent);
                            dict.putStringToId(parent, strCount++);
                            

                        }
                        parentId = dict.getStringToId(parent).doubleValue();
                    }
                    
                    if(dictData.containsString(child)){
                        childId = dictData.getStringToId(child).doubleValue();

                        dict.putIdToString(childId.intValue(), child);
                        dict.putStringToId(child, childId.intValue());
                    }
                    else{

                        if(!dict.containsString(child)){
                            dict.putIdToString(strCount, child);
                            dict.putStringToId(child, strCount++);
                            

                        }
                        childId = dict.getStringToId(child).doubleValue();
                    }
                    
                    if(curLevel == this.height-1){
                        this.stats.put(childId, new NodeStats(curLevel));
                        this.nodeToPopulation.put(childId, this.nodeToPopulationStr.get(child));
                    }
                    if(!this.stats.containsKey(parentId)){
                        this.stats.put(parentId, new NodeStats(curLevel-1));
                    }
                    if(!this.children.containsKey(parentId)){
                        this.children.put(parentId, new ArrayList<Double>(){{add(childId);}});
                        this.nodeToPopulation.put(parentId, this.nodeToPopulation.get(childId));
                    }
                    else{
                        this.children.get(parentId).add(childId);
                        this.nodeToPopulation.put(parentId, this.nodeToPopulation.get(parentId)+this.nodeToPopulation.get(childId));
                    }
                    this.parents.put(childId,parentId);
                    nextLevel.add(parent);
                }
                lastChilds = new ArrayList(nextLevel);
                this.counterNodes += lastChilds.size();
                if(AppCon.os.equals(online_version) && counterNodes > online_limit){
                    throw new LimitException("Hierarchy is too large, the limit is "+online_limit+" nodes, please download desktop version, the online version is only for simple execution.");
                }
            }
            curLevel--;
        }
    }
    
     private String fillStars(String str,int size){
        if(str.length()==size){
            return str;
        }
        else{
            while(str.length()!=size){
                str += "*";
            }
            return str;
        }
    }



    @Override
    public boolean validCheck(String parsePoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Double getParent(Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getParentId(double d) {
        Double parentStringValue = this.parents.get(d);
        if(parentStringValue == null){
            return -1;
        }
        else{
            return parentStringValue;
        }
    }

    @Override
    public Set<Double> getChildrenIds(double d) {
        List<Double> childrenIds = this.children.get(d);
        Set<Double> chs = null;
        
        if(childrenIds != null){
            chs = new HashSet<>(childrenIds);
        }
        return chs;
    }


    @Override
    public int findAllChildren(Double node, int sum) {
        int result = 0;
       
        List<Double> child = this.getChildren(node);
       
        if ( child == null){
            return 1;
        }

        for (int i =0 ; i < child.size() ; i ++){
            result = findAllChildren(child.get(i),sum) + result;
        }

        return result;
    }


    @Override
    public Graph getGraph(String node, int nodeLevel) {
        Graph graph = new Graph(this.hierarchyType);
        Node n = null;
        Edge e = null;
        ArrayList<String> nodes = null;
        boolean FLAG = false;
        String parent = null;
        List<Double> nodeChilds = null;
        int counter = 0;
        int counterNode = 0;
        String color = null;
        String label = null;
        Double nodeId;
        String nodeChild;
        
        System.out.println("roottttttttttttttttttttttttttt = " + root);
        System.out.println("nodeeeeeeeeeeeeeeeeeeeeee = " + node);
        
      
        
        if ( !node.equals("(null)") && !node.equals("") && nodeLevel != 0 ){
            
            if(dict.containsString(node)){
                nodeId = (double) dict.getStringToId(node);
            }
            else{
                nodeId = (double) dictData.getStringToId(node);
            }
            
            System.out.println("i am here"+nodeId);
            counter = 1;
            if (height > nodeLevel + 1){
                for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(nodeId);
                    //System.out.println(node);
                    counterNode = counter;
                    counter ++;                
                    //Collections.sort(nodeChilds);
                    if ( nodeChilds != null){
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){
                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                                nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                    nodeChild = dict.getIdToString(nodeChilds.get(j).intValue()); 
                                }
                            }
                            else{
                                color = null;
                                nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                  nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());  
                                }
                            }
                            
                            if (nodeChild.equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChild;
                            }

//                            n = new Node(nodeChilds.get(j),label,i+1,color,this.hierarchyType + "," +this.nodesType); 
                            
                            n = new Node(nodeChild,label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));
                            graph.setNode(n);
                            e = new Edge(node,nodeChild);
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    nodeId = this.parents.get(nodeId);
                    System.out.println("ParentId: "+nodeId);
                    if(nodeId!=null){
                        node = dict.getIdToString(nodeId.intValue());
                        if(node == null){
                            node = dictData.getIdToString(nodeId.intValue());
                        }
                    }
                    //System.out.println("node = " + node);
                }
                n = new Node(dict.getIdToString(root.intValue()),dict.getIdToString(root.intValue()),0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                graph.setNode(n);
                Collections.reverse(graph.getNodeList());
                Collections.reverse(graph.getEdgeList());
                
            }
            else{
                System.out.println("i am here222");
                for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(nodeId);
                   
                    System.out.println("ola kala");
                    //Collections.sort(nodeChilds);
                    if ( nodeChilds != null){
                        counterNode = counter;
                        counter ++;
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){
                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                                nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                    nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                }
                            }
                            else{
                                color = null;
                                nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                  nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());  
                                }
                            }
                            
                            if (nodeChild.equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChild;
                            }
                            n = new Node(nodeChild,label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                            graph.setNode(n);
                            System.out.println("Node "+node+" child "+nodeChild);
                            e = new Edge(node ,nodeChild);
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    nodeId = this.parents.get(nodeId);
                    if(nodeId!=null){
                        node = dict.getIdToString(nodeId.intValue());
                        if(node == null){
                            node = dictData.getIdToString(nodeId.intValue());
                        }
                    }
                    //System.out.println("node = " + node);
                }
                n = new Node(dict.getIdToString(root.intValue()),dict.getIdToString(root.intValue()),0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                graph.setNode(n);
            
            }
            
        }
        else{
            nodeId = root;
            nodeLevel= 0;
            for (int i = nodeLevel ; i >= 0 ; i --){
                    nodeChilds = this.children.get(nodeId);
                    //Collections.sort(nodeChilds);
                    counterNode = counter;
                    counter ++;
                    if ( nodeChilds != null){
                        Collections.sort(nodeChilds);
                        for (int j = 0 ; j < nodeChilds.size() ; j ++){

                            if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                color = "red";
                                nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                    nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                }
                            }
                            else{
                                color = null;
                                nodeChild = dict.getIdToString(nodeChilds.get(j).intValue());
                                if(nodeChild == null){
                                  nodeChild = dictData.getIdToString(nodeChilds.get(j).intValue());  
                                }
                            }
                            
                            if (nodeChild.equals("NaN")){
                                label = "(null)";
                            }
                            else{
                                label = nodeChild;
                            }
                            n = new Node(nodeChild,label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                            graph.setNode(n);
                            e = new Edge(dict.getIdToString(root.intValue()),nodeChild);
                            graph.setEdge(e);
                            counter ++;
                        }
                    }
                    else{
                        System.out.println("noChildren");
                    }
                    nodeId = this.parents.get(nodeId);
                    
                    //System.out.println("nodeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee = " + node);
                }
                System.out.println("Root "+root+" dictionary ");
                n = new Node(dict.getIdToString(root.intValue()),dict.getIdToString(root.intValue()),0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                graph.setNode(n);
            
            
        }
        
        //Collections.reverse(graph.getNodeList());
        //Collections.reverse(graph.getEdgeList());

        /*for ( int i = 0 ; i < graph.getNodeList().size() ; i ++){
            System.out.println(graph.getNodeList().get(i).getId() + "\t level = " + graph.getNodeList().get(i).getLevel() + "\tlabel = "+ graph.getNodeList().get(i).getLabel() );
        }
        
        
        for ( int i = 0 ; i < graph.getEdgeList().size() ; i ++){
            System.out.println("From = " + graph.getEdgeList().get(i).getFrom() + "\t to = " + graph.getEdgeList().get(i).getTo() );
            
        }*/
        ArrayList<Edge> list = graph.getEdgeList();
        Collections.sort(list, new Comparator<Edge>() {
            @Override
            public int compare(Edge s1, Edge s2) {
                return s1.getTo().compareToIgnoreCase(s2.getTo());
            }
        });
        
        graph.setEdgeList(list);
        
        
        return graph;
    }


    @Override
    public Double getParent(Double v, Integer k) {
        Double parent = this.getParent(v);
        int population = this.nodeToPopulation.get(parent);
        while(population < k){
            if(parent.equals(root)){
                break;
            }
            parent = this.getParent(parent);
            population = this.nodeToPopulation.get(parent);
        }
        return parent;
    }

    @Override
    public Integer getPopulation(Double v) {
        return this.nodeToPopulation.get(v);
    }

    @Override
    public Integer getPopulation(double rd) {
        return this.nodeToPopulation.get(rd);
    }
    
}
