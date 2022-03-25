/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.ranges;

import data.Data;
import data.DiskData;
import dictionary.DictionaryString;
import exceptions.LimitException;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.DemographicInfo;
import hierarchy.Hierarchy;
import hierarchy.NodeStats;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dimak
 */
public class HierarchyImplRangeDemographicAge implements Hierarchy<RangeDouble> {
    
    String inputFile = null;
    String hierarchyType = "range_demographic";
    String name = null;
    String nodesType = null;
    String country;
    int height = 3;
    int fanout = 2;
    RangeDouble root = null;
    
    
    Map<RangeDouble, List<RangeDouble>> children = new HashMap<>();
    Map<RangeDouble, NodeStats> stats = new HashMap<>();
    Map<Double,NodeStats> statsDistinct = new HashMap<>();
    Map<RangeDouble, RangeDouble> parents = new HashMap<>();
    Map<Integer,ArrayList<RangeDouble>> allParents = new HashMap<>();
    Map<RangeDouble,Integer> nodeToPopulation = new HashMap();
    
    DictionaryString dictData = null;
    
    public HierarchyImplRangeDemographicAge(String _inputFile){
        this.inputFile = _inputFile;
    }
    
    public HierarchyImplRangeDemographicAge(String _name, String _nodesType,String _country){
       
        this.name = _name;
        this.nodesType = _nodesType;
        this.country = _country;

        if(this.country.equals("USA")){
            this.height = 5;
        }
                   
    }
    
    public static ArrayList<String> getCountries(){
        ArrayList<String> countries = new ArrayList(DemographicInfo.countryDistributionAge.keySet());
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
        this.dictData = dict;
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
            autogenerate();
        }
//        findAllParents();
    }

    @Override
    public List<RangeDouble> getChildren(RangeDouble parent) {
        return this.children.get(parent);
    }

    @Override
    public Integer getLevel(RangeDouble node) {
        return this.stats.get(node).level;
    }

    @Override
    public Integer getLevel(double nodeId) {
        return 0; 
        /// or return this.height
    }

    @Override
    public String getNodesType() {
        return this.nodesType;
    }

    @Override
    public RangeDouble getParent(RangeDouble node) {
        return this.parents.get(node);
    }

    @Override
    public void setLevel(int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RangeDouble getRoot() {
        return this.allParents.get(0).get(0);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<Integer, ArrayList<RangeDouble>> getAllParents() {
        return this.allParents;
    }

    @Override
    public void export(String file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void findAllParents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(RangeDouble o) {
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
    public void add(RangeDouble newObj, RangeDouble parent) throws LimitException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
        allParents = new HashMap<>();
        children.put(root, new ArrayList<RangeDouble>());
        stats.put(root, new NodeStats(0));
        this.nodeToPopulation = new HashMap();
       
        height = 1;
    }

    @Override
    public void clearAprioriStructures() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void edit(RangeDouble oldValue, RangeDouble newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<RangeDouble>> remove(RangeDouble obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<RangeDouble>> dragAndDrop(RangeDouble firstObj, RangeDouble lastObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<RangeDouble>> BFS(RangeDouble firstnode, RangeDouble lastNode) {
        Map<Integer,Set<RangeDouble>> bfsMap = new HashMap<Integer,Set<RangeDouble>>();
        LinkedList<RangeDouble> listNodes = new LinkedList<RangeDouble>();
        ArrayList childs1 = null;
        int counter = 1;
        int levelNode1;
        int levelNode2;
        Set s = new HashSet<RangeDouble>();
        
        
        s.add(firstnode);
        bfsMap.put(counter,s);
        listNodes.add(firstnode);
        counter ++;
        
        levelNode1 = this.getLevel(firstnode);
        
        while (!listNodes.isEmpty()){
            childs1 = (ArrayList) this.getChildren(listNodes.getFirst());
            if ( childs1 != null && childs1.size() > 0){// ean exei paidia
                levelNode2 = this.getLevel((RangeDouble) childs1.get(0));
                if (levelNode2 == levelNode1){// ean einai sto idio epipedo tote valta sto proigoumeno set
                    s.addAll(childs1);
                    if ( lastNode != null){
                        if (s.contains(lastNode)){
                            bfsMap = null;
                            break;
                        }
                    }
                    bfsMap.put(counter, s);
                }
                else{// ean den einai sto idio epipedo dimiourgise kainourgio set
                    s = new HashSet<RangeDouble>();
                    levelNode1 = levelNode2;
                    s.addAll(childs1);
                    if ( lastNode != null ){
                        if (s.contains(lastNode)){
                            bfsMap = null;
                            break;
                        }
                    }
                    bfsMap.put(counter, s);
                   
                }
                listNodes.addAll(childs1);//add ola stin linked list
                
                if (listNodes.size() > 1){
                    if ( this.stats.get(listNodes.getFirst()).level != this.stats.get(listNodes.get(1)).level ){//ean to epomeno stoixeio tis listas exei allo level tote auksise ton counter
                        counter ++;
                    }
                }
                else{//ean uparxei mono ena stoixeio stin lista auksise ton counter
                    counter ++;
                }
            }
            
            listNodes.removeFirst();//remove to prwto stoixeio tis linkedlist giati to exoume tsekarei
        }
      
        return bfsMap;
    }

    @Override
    public boolean checkExistance(Double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeWeights(Data dataset, String column) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getWeight(RangeDouble node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getWeight(double nodeId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLevelSize(int level) {
        return this.allParents.get(this.height - level - 1).size();
    }

    @Override
    public void autogenerate() throws LimitException {
        int curLevel = this.height - 1;
        System.out.println("Country: "+this.country);
        this.nodeToPopulation = new HashMap(DemographicInfo.countryDistributionAge.get(this.country));
        ArrayList<RangeDouble> lastChilds = new ArrayList<RangeDouble>(this.nodeToPopulation.keySet());
        Collections.sort(lastChilds, new Comparator<RangeDouble>(){
            @Override
            public int compare(RangeDouble d1, RangeDouble d2) {
//                    return s1.getTo().compareToIgnoreCase(s2.getTo());
                if(d1.upperBound.equals(d2.upperBound) && d1.lowerBound.equals(d2.lowerBound)){
                    return 0;
                }
                else if(d1.lowerBound < d2.lowerBound &&  d1.upperBound.equals(d2.upperBound)){
                    return 1;
                }
                else if(d1.upperBound > d2.upperBound){
                    return 1;
                }
                else{
                    return -1;
                }
            }
        });
        
        while(curLevel>=0){
            if(curLevel == 0){
                lastChilds = this.allParents.get(curLevel+1);
                root = new RangeDouble(Double.NaN,Double.NaN);
                List<RangeDouble> ch = new ArrayList<>();
                for(RangeDouble child : lastChilds){
                    if(child.lowerBound < root.lowerBound || root.lowerBound.equals(Double.NaN)){
                        root.lowerBound = child.lowerBound;
                    }

                    if(child.upperBound>root.upperBound || root.upperBound.equals(Double.NaN)){
                        root.upperBound = child.upperBound;
                    }
                    ch.add(child);
                }
                root.nodesType = nodesType;
                int newPopulation = 0;
                for(RangeDouble c : ch){
                    this.parents.put(c,root);
                    newPopulation += this.nodeToPopulation.get(c);
                }
                RangeDouble nanNode = new RangeDouble(Double.NaN,Double.NaN);
                ch.add(nanNode);
                this.nodeToPopulation.put(nanNode,0);
                this.parents.put(nanNode,root);
                this.stats.put(nanNode, new NodeStats(curLevel+1));
                this.allParents.get(curLevel+1).add(nanNode);
                
                this.nodeToPopulation.put(root, newPopulation);
                this.stats.put(root, new NodeStats(curLevel));
                this.children.put(root, ch);
                
                this.allParents.put(curLevel,new ArrayList<RangeDouble>(){{add(root);}});   
            }
            else if(curLevel == this.height-1){
                ArrayList<RangeDouble> curParents = new ArrayList();
                for(int i=0; i<lastChilds.size(); i+=fanout){
                    int counter = 0;
                    RangeDouble parent = new RangeDouble(Double.NaN,Double.NaN);
                    List<RangeDouble> ch = new ArrayList<>();
                    while(counter < fanout){
                        RangeDouble child = lastChilds.get(i+counter);
                        if(child.lowerBound < parent.lowerBound || parent.lowerBound.equals(Double.NaN)){
                            parent.lowerBound = child.lowerBound;
                        }
                        
                        if(child.upperBound>parent.upperBound || parent.upperBound.equals(Double.NaN)){
                            parent.upperBound = child.upperBound;
                        }
                        ch.add(child);
                        child.nodesType = nodesType;
                        this.stats.put(child, new NodeStats(curLevel));
                        counter++;
                    }
                    parent.nodesType = nodesType;
                    int newPopulation = 0;
                    for(RangeDouble c : ch){
                        this.parents.put(c,parent);
                        newPopulation += this.nodeToPopulation.get(c);
                    }
                    this.nodeToPopulation.put(parent, newPopulation);
                    this.stats.put(parent, new NodeStats(curLevel-1));
                    this.children.put(parent, ch);
                    curParents.add(parent);
                    
                }
                this.allParents.put(curLevel,lastChilds);
                this.allParents.put(curLevel-1,curParents);
                lastChilds = curParents;
                if(curParents.size() <= 3){
                   curLevel = 1; 
                }
                
            }
            else{
                ArrayList<RangeDouble> curParents = new ArrayList();
                int counter_ancestors = 0;
                for(int i=0; i<lastChilds.size(); i+=fanout){
                    int counter = 0;
                    RangeDouble parent = new RangeDouble(Double.NaN,Double.NaN);
                    List<RangeDouble> ch = new ArrayList<>();
                    if(lastChilds.size() % 2 == 0){
                        while(counter < fanout){
                            RangeDouble child = lastChilds.get(i+counter);
                            if(child.lowerBound < parent.lowerBound || parent.lowerBound.equals(Double.NaN)){
                                parent.lowerBound = child.lowerBound;
                            }

                            if(child.upperBound>parent.upperBound || parent.upperBound.equals(Double.NaN)){
                                parent.upperBound = child.upperBound;
                            }
                            ch.add(child);
                            counter++;
                        }
                    }
                    else{
                        counter_ancestors++;
                        
                        if(counter_ancestors == lastChilds.size()/2){
                            for(int j=i; j<lastChilds.size(); j++){
                                RangeDouble child = lastChilds.get(j);
                                if(child.lowerBound < parent.lowerBound || parent.lowerBound.equals(Double.NaN)){
                                    parent.lowerBound = child.lowerBound;
                                }

                                if(child.upperBound>parent.upperBound || parent.upperBound.equals(Double.NaN)){
                                    parent.upperBound = child.upperBound;
                                }
                                ch.add(child);
                            }
                            i=lastChilds.size();
                        }
                        else{
                            while(counter < fanout){
                                RangeDouble child = lastChilds.get(i+counter);
                                if(child.lowerBound < parent.lowerBound || parent.lowerBound.equals(Double.NaN)){
                                    parent.lowerBound = child.lowerBound;
                                }

                                if(child.upperBound>parent.upperBound || parent.upperBound.equals(Double.NaN)){
                                    parent.upperBound = child.upperBound;
                                }
                                ch.add(child);
                                counter++;
                            } 
                        }
                    }
                    parent.nodesType = nodesType;
                    int newPopulation = 0;
                    for(RangeDouble c : ch){
                        this.parents.put(c,parent);
                        newPopulation += this.nodeToPopulation.get(c);
                    }
                    this.nodeToPopulation.put(parent, newPopulation);
                    this.stats.put(parent, new NodeStats(curLevel-1));
                    this.children.put(parent, ch);
                    curParents.add(parent);
                }
                this.allParents.put(curLevel-1,curParents);
                lastChilds = curParents;
            }
            System.out.println("current level "+curLevel);
            curLevel--;
        }
    }

    @Override
    public RangeDouble checkColumn(int column, Data dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DictionaryString getDictionary() {
        return this.dictData;
    }

    @Override
    public DictionaryString getDictionaryData() {
        return this.dictData;
    }

    @Override
    public boolean validCheck(String nodeStr) {
        boolean valid = true;
        RangeDouble node = new RangeDouble();
        String[] parts = nodeStr.split("-");
        if(parts.length == 2){
            node.lowerBound = Double.parseDouble(parts[0]);
            node.upperBound = Double.parseDouble(parts[1]);
            node.nodesType = nodesType;
        }
        
        List<RangeDouble> chList = children.get(node);
        if(chList != null){
            for(RangeDouble c : chList){
                valid = !node.overlays(c);
            }
        }
        return valid;
    }

    @Override
    public void transformParents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Integer> getParentsInteger() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RangeDouble getParent(Double d) {
        if(d.isNaN() || d==2147483646.0){
            if(this.parents.containsKey(new RangeDouble(Double.NaN,Double.NaN))){
                return this.parents.get(new RangeDouble(Double.NaN,Double.NaN));
            }
        }
        List<RangeDouble> leafNodes = this.allParents.get(this.height-1);
        RangeDouble r = binarySearch(leafNodes, d);
        return r;
    }
    
    private RangeDouble binarySearch(List<RangeDouble> list, Double d){
        
        if(list.isEmpty()){
            return null;
        }
        
        
        int mid = (list.size()-1)/2;
        
        if((list.size()-1)%2 > 0){
            mid++;
        }
        if(list.size()-1 == 1){
            return list.get(0);
        }
        RangeDouble r = list.get(mid);
        
        
        if(d < r.lowerBound){
            for ( int i = 0 ; i < mid ; i ++ ){
                r = list.get(i);
                
                if ( r.contains2(d,false)){
                    return r;
                }
            }
        }
        else if (d > r.upperBound){
            for ( int i = mid ; i < list.size() ; i ++ ){
                r = list.get(i);
                if (list.get(list.size()-1).equals(new RangeDouble(Double.NaN,Double.NaN)) ){
                    if( i == list.size()-2){
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                    else{
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                }
                else{
                    if( i == list.size()-1){
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                    else{
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                }
            }
        }
        else{
            return r;
        }
        
        
        return null;
    }

    @Override
    public RangeDouble getParent(Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getParentId(double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Double> getChildrenIds(double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildDictionary(DictionaryString dictionary) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Integer> getNodeIdsInLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RangeDouble> getNodesInLevel(int level) {
        int curLevel = this.height - level - 1;
        List<RangeDouble> curLevelIds =  this.allParents.get(curLevel);
        return curLevelIds;
    }

    @Override
    public void setNodesType(String nodesType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findAllChildren(RangeDouble node, int sum,boolean onlyLeaves) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<RangeDouble>> getLeafNodesAndParents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getInputFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void syncDictionaries(Integer column, Data data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Graph getGraph(String nodeInput, int nodeLevel) {
        Graph graph = new Graph(this.hierarchyType);
        Node n = null;
        Edge e = null;
        ArrayList<String> nodes = null;
        boolean FLAG = false;
        String parent = null;
        List<RangeDouble> nodeChilds = null;
        RangeDouble nodeRange = null;
        String color = null;
        String label = null;
        
        RangeDouble node = null;
        //System.out.println("Get Graphhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        //System.out.println(" nodeInputtttt = " + nodeInput);
        //System.out.println(" nodeLevellllllll = " + nodeLevel);
        
        if (!nodeInput.equals("null") && !nodeInput.equals("(null)") ){
            //System.out.println("nodeInput11111 = " + nodeInput);
            if ( nodeInput.equals("0-0")){
                node  = new RangeDouble(Double.NaN,Double.NaN);
            }
            else{
                String []temp = null;
                temp = nodeInput.split("-");
                Double start=null,end=null;
                int count = StringUtils.countMatches(nodeInput, "-");
                if(count==1){
                    start = Double.parseDouble(temp[0]);
                    end = Double.parseDouble(temp[1]);
                }
                else if(count==2){
                    try{
                        start = Double.parseDouble("-"+temp[1]);
                        end = Double.parseDouble(temp[2]);
                        System.out.println("Count "+count+" start "+start+" end "+end);
                    }catch(Exception e1){
                        e1.printStackTrace();
                        
                        // TODO exception 
                    }
                }
                else if(count==3){
                    try{
                        start = Double.parseDouble("-"+temp[1]);
                        end = Double.parseDouble("-"+temp[3]);
                        System.out.println("Count "+count+" start "+start+" end "+end);
                    }catch(Exception e2){
                         e2.printStackTrace();
                        
                        // TODO exception 
                    }
                }
                else{
                    /// TODO exception 
                    
                    System.out.println("Count "+count);
                }
                node  = new RangeDouble(start,end);
            }
            
            if (nodesType.equals("double")){
                node.setNodesType("double");
            }
            else{
                node.setNodesType("int");
            }
        }
        
        int counter = 0;
        
        if (nodesType.equals("double")){
            if ( !nodeInput.equals("null") && !nodeInput.equals("(null)")  && !nodeInput.equals("") && nodeLevel != 0 ){

                if (height > nodeLevel + 1){
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                
                                if (nodeChilds.get(j).getLowerBound().isNaN() && nodeChilds.get(j).getUpperBound().isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                                graph.setNode(n);
                                e = new Edge(nodeRange+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                        
                    }
                    n = new Node(root+"",root+"",0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                    graph.setNode(n);

                }
                else{
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().isNaN() && nodeChilds.get(j).getUpperBound().isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                                graph.setNode(n);
                                e = new Edge(nodeRange+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root +"",root+"",0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                    graph.setNode(n);

                }

            }
            else{
                nodeRange = root;
                nodeLevel= 0;
                for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().isNaN() && nodeChilds.get(j).getUpperBound().isNaN()){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                               
                                n = new Node(nodeChilds.get(j)+"",label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                                graph.setNode(n);
                                e = new Edge(nodeRange+"",nodeChilds.get(j)+"");
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root+"",root+"",0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                    graph.setNode(n);


            }
        }
        else{
            if ( !nodeInput.equals("null") && !nodeInput.equals("(null)")  && !nodeInput.equals("") && nodeLevel != 0 ){
                //nodeDouble = Double.parseDouble(node);

                if (height > nodeLevel + 1){    
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().intValue() == 0 && nodeChilds.get(j).getUpperBound().intValue() == 0){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue(),label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                                graph.setNode(n);
                                e = new Edge(nodeRange.getLowerBound().intValue() + "-" + nodeRange.getUpperBound().intValue(),nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue());
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue(),root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue(),0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                    graph.setNode(n);

                }
                else{
                    nodeRange = node;
                    for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                } 
                                if (nodeChilds.get(j).getLowerBound().intValue() == 0 && nodeChilds.get(j).getUpperBound().intValue() == 0){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue(),label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                                graph.setNode(n);
                                e = new Edge(nodeRange.getLowerBound().intValue() + "-" + nodeRange.getUpperBound().intValue(),nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue());
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue(),root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue()+"",0,null, this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                    graph.setNode(n);

                }
            }
            else{
                nodeRange = root;
                nodeLevel= 0;
                
                for (int i = nodeLevel ; i >= 0 ; i --){
                        nodeChilds = this.children.get(nodeRange);
                        if ( nodeChilds != null){
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                if (nodeChilds.get(j).getLowerBound().intValue() == 0 && nodeChilds.get(j).getUpperBound().intValue() == 0){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j)+"";
                                }
                                
                                n = new Node(nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue(),label,i+1,color,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(nodeChilds.get(j)));  
                                graph.setNode(n);
                                e = new Edge(nodeRange.getLowerBound().intValue() + "-" + nodeRange.getUpperBound().intValue(),nodeChilds.get(j).getLowerBound().intValue() + "-" + nodeChilds.get(j).getUpperBound().intValue());
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren");
                        }
                        
                        nodeRange = this.parents.get(nodeRange);
                        
                    }
                    n = new Node(root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue() ,root.getLowerBound().intValue() + "-" + root.getUpperBound().intValue()+"",0,null,this.hierarchyType + ", " +this.nodesType+", population: "+this.nodeToPopulation.get(root));
                    graph.setNode(n);
            }
        }
        
        Collections.reverse(graph.getNodeList());
        Collections.reverse(graph.getEdgeList());
        
        return graph;
    }

    @Override
    public String checkHier(Data d, int col) {
        String str = null;
//        System.out.println("Check Hierarchies");
        
        
        ArrayList<RangeDouble> firstArr = allParents.get(0);
        ArrayList<RangeDouble> lastArr = allParents.get(allParents.size()-1);
        ArrayList<RangeDouble> preLastArr = allParents.get(allParents.size()-2);
        
        double lowerLimit = firstArr.get(0).lowerBound;
        double upperLimit = firstArr.get(0).upperBound;
        
        double lower;
        double upper;
        
        
        
        
        //ean uparxei mono to null san paidi
        if (allParents.size() == 2){
            if (lastArr.size() == 1){
                str = "Ok";
                return str;

            }
        }

        if (preLastArr.size() > lastArr.size()){
            str = "Hierarchy:" + this.name + "\nNumber of nodes in the last level must be greater than the previous level.";
            return str;
        }





        Collections.sort(lastArr, new Comparator<RangeDouble>(){
            @Override
            public int compare(RangeDouble o1, RangeDouble o2) {
                return o1.getLowerBound().compareTo(o2.getLowerBound());
            }
        });



        for (Map.Entry<Integer,ArrayList<RangeDouble>> entry : this.allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().size());

            if(entry.getKey() != 0){
                ArrayList <RangeDouble> tempArr = entry.getValue();
                Collections.sort(tempArr, new Comparator<RangeDouble>(){
                    @Override
                    public int compare(RangeDouble o1, RangeDouble o2) {
                        return o1.getLowerBound().compareTo(o2.getLowerBound());
                    }
                });



                for( int i = 0 ; i < tempArr.size()-1 ; i ++){
                    RangeDouble r = tempArr.get(i);
                    RangeDouble tempR = tempArr.get(i+1);
                    if(!tempR.lowerBound.isNaN() && !tempR.upperBound.isNaN()){
                        if( !r.upperBound.equals(tempR.lowerBound-1)){
                            str = "Hierarchy Name: " + this.name + "\nLevel: " + (entry.getKey()+1) +"\nNot continuous values between ranges:" + r.toString() + " and " + tempR.toString();
                            return str;
                            //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nNot continuous values between ranges:" + r.toString() + " and " + tempR.toString());
                        }
                    }

                    if ( i == 0){
                        if ( r.lowerBound != lowerLimit){
                            str = "Hierarchy Name: " + this.name + "\nLevel: " + (entry.getKey()+1) +"\nFirst node of the level must have the same lower bound as the root node. Problem in range:" + r.toString() + ". Root range is :" + (int)lowerLimit + "-" + (int)upperLimit;
                            return str;
                            //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nFirst node of the last level must have the same lower bound as the root node. Problem in range:" + r.toString());
                        }
                    }
                    else if ( i == tempArr.size()-2){
                        if(!tempR.lowerBound.isNaN() && !tempR.upperBound.isNaN()){ 
                            if(!tempR.upperBound.equals(upperLimit)){
                                str = "Hierarchy Name: " + this.name + "\nLevel: " + (entry.getKey()+1) +"\nLast node of the level must have the same upper bound as the root node. Problem in range:" + tempR.toString() + ". Root range is :" + (int)lowerLimit + "-" + (int)upperLimit;
                                return str;
                                //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString());
                            }
                        }
                        else{
                            if(!r.upperBound.equals(upperLimit)){
                                str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString() + ". Root range is :" + (int)lowerLimit + "-" + (int)upperLimit;
                                return str;
                                //System.out.println("Hierarchy Name:" + this.name + "\nLevel: " + i +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + r.toString());
                            }
                        }
                    }
                }
            }

        }


        if(d instanceof DiskData){
            DiskData diskData = (DiskData) d;
            List<Double> missingValues = diskData.checkRange(root.upperBound, root.lowerBound, col);
            if(!missingValues.isEmpty()){
                for(int i=0; i<missingValues.size(); i++){
                    if(Double.isNaN(missingValues.get(i)) || missingValues.get(i).equals(2147483646.0)){
                        if(this.getParent(new RangeDouble(Double.NaN,Double.NaN)) == null){
                           return "Node (null) for spaces values and non-Numeric values, is not defined in the hierarchy \""+this.name+"\"" ;
                        }
                        missingValues.remove(i);
                    }
                }
                if(missingValues.size() == 1){
                    if(d.getColNamesType().get(col).equals("int")){
                        return "Value \""+missingValues.get(0).intValue()+"\" are not defined in hierarchy \""+this.name+"\"";
                    }
                    else{
                        return "Value \""+missingValues.get(0)+"\" are not defined in hierarchy \""+this.name+"\"";
                    }
                }
                else if(!missingValues.isEmpty()){
                    if(d.getColNamesType().get(col).equals("int")){
                        return "Values \""+missingValues.toString().replace(".0", "").replace("[", "(").replace("]", ")")+"\" are not defined in hierarchy \""+this.name+"\"";
                    }
                    else{
                        return "Values \""+missingValues.toString().replace("[", "(").replace("]", ")")+"\" are not defined in hierarchy \""+this.name+"\"";
                    } 
                }
            }
        }
        else{
            double[][] dataset = d.getDataSet();
            for(int i=0; i<dataset.length; i++){
                if(!root.contains(dataset[i][col])){
                    if(Double.isNaN(dataset[i][col]) || dataset[i][col] == 2147483646.0){
                        if(this.getParent(new RangeDouble(Double.NaN,Double.NaN)) == null){
                           return "Node (null) for spaces values and non-Numeric values, is not defined in the hierarchy \""+this.name+"\"" ;
                        }
                    }
                    else{
                        Object value;
                        if(d.getColNamesType().get(col).equals("int")){
                            value = (int)dataset[i][col];
                        }
                        else{
                            value = dataset[i][col];
                        }
                        return "Value \""+value+"\" is not defined in the hierarchy \""+this.name+"\"";
                    }
                }
            }
        }

        
        return str;
    }

    @Override
    public int translateDateViaLevel(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findCommonHeight(Double n1, Double n2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double findCommon(Double n1, Double n2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RangeDouble getParent(Double v, Integer k) {
        RangeDouble parent = this.getParent(v);
        int population = this.nodeToPopulation.get(parent);
        while(population < k){
            if(parent == root){
                break;
            }
            parent = this.getParent(parent);
            population = this.nodeToPopulation.get(parent);
        }
        return parent;
    }

    @Override
    public Integer getPopulation(double v) {
        RangeDouble parent = this.getParent(v);
        System.out.println("parent "+parent);
        int population = this.nodeToPopulation.get(parent);
        if(population == 0){
            return 0;
        }
        double divident = parent.upperBound - parent.lowerBound ;
        population = (int) Math.ceil(population/ divident);
        return population;
    }

    @Override
    public Integer getPopulation(RangeDouble rd) {
         return this.nodeToPopulation.get(rd);
    }

    @Override
    public void setpLevel(int ti, int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RangeDouble getParent(RangeDouble node, int ti) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
