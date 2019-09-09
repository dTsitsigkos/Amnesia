/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy.ranges;

import anonymizeddataset.AnonymizedDataset;
import data.Data;
import dictionary.DictionaryString;
import graph.Edge;
import graph.Graph;
import graph.Node;
import hierarchy.Hierarchy;
import hierarchy.NodeStats;
import hierarchy.distinct.HierarchyImplDouble;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.util.StringUtils;

/**
 *
 * @author jimakos
 */
public class HierarchyImplRangesDate implements Hierarchy<RangeDate>{
    String inputFile = null;
    String name = null;
    String nodesType = "date";
    String hierarchyType = "range";
    int height = -1;
    BufferedReader br = null;
    RangeDate root = null;
    int months = -1;
    int days = -1;
    int levelFlash =-1;
    DictionaryString dictData = null;
    
    Map<RangeDate, List<RangeDate>> children = new HashMap<>();
    Map<RangeDate, NodeStats> stats = new HashMap<>();
    Map<RangeDate, RangeDate> parents = new HashMap<>();
//    Map<Range, List<Range>> siblings = new HashMap<>();
    Map<Integer,ArrayList<RangeDate>> allParents = new HashMap<>();
    Map<Double,NodeStats> statsDistinct = new HashMap<>();

    
    public HierarchyImplRangesDate(String inputFile){
        this.inputFile = inputFile;
    }
    
    public HierarchyImplRangesDate(String _name, String _nodesType){
        this.name = _name;
        this.nodesType = _nodesType;
//        this.hierarchyType = "range";
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
    public int getHierarchyLength() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public DictionaryString getDictionaryData(){
        return this.dictData;
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load() {
        try {
            br = new BufferedReader(new FileReader(this.inputFile));
            processingMetadata();
            loadHierarchy();
            findAllParents();
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void processingMetadata() throws IOException{

        String line;
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            if(line.trim().isEmpty())
                break;

            //System.out.println("Metadata: " + line);
            String[] tokens = line.split(" ");
            if(tokens[0].equalsIgnoreCase("name")){
                this.name = tokens[1];
            }
            else if(tokens[0].equalsIgnoreCase("type")){
                this.nodesType = tokens[1];
            }
            else if(tokens[0].equalsIgnoreCase("height")){
                this.height = Integer.parseInt(tokens[1]);
            }
        }   
    }
    
    private void loadHierarchy() throws IOException, ParseException{
        String line;
        int curLevel = this.height - 1;
        
        while ((line = br.readLine()) != null) {
            String tokens[] = line.split(" ");
            if(line.trim().isEmpty()){
                curLevel--;
                continue;
            }
            
            
            //split parent
            RangeDate pDate = new RangeDate();
            String bounds[] = tokens[0].split(",");
            pDate.lowerBound = this.getDateFromString(bounds[0], true);
            pDate.upperBound = this.getDateFromString(bounds[1], false);
            //pDate.nodesType = nodesType;
            
            boolean isChild = false;
            List<RangeDate> ch = new ArrayList<>();
            for (String token : tokens){
                if(token.equals("has")){ 
                    isChild = true;
                    continue;
                }
                RangeDate newDate = null;
                bounds = token.split(",");
                if (!bounds[0].equals("null")){
                    newDate = new RangeDate();
                    newDate.lowerBound = this.getDateFromString(bounds[0], true);
                    newDate.upperBound = this.getDateFromString(bounds[1], false);
                }
                else{
                    newDate = new RangeDate(null,null);
                }
                
               // newRange.nodesType = nodesType;
                
                if(isChild){
                    ch.add(newDate);
                    
                    
                    this.stats.put(newDate, new NodeStats(curLevel));
                    
                    this.parents.put(newDate, pDate);  
                }
                else{
                    this.stats.put(newDate, new NodeStats(curLevel-1));
                    
                    if(curLevel - 1 == 0){
                        root = pDate;
                    }
                }
            }
            
            this.children.put(pDate, ch);
            
//            for (Range child : ch) {
//                List<Range> sib = new ArrayList<>(ch);
//                sib.remove(child);
//                this.siblings.put(child, sib);
//            }
            
        }
        
        
        int mb = 1024*1024;
    }
    
    @Override
    public void findAllParents() {
        List<RangeDate> tempChild = null;
        int i = 0;
        int level = 0;
        ArrayList<RangeDate> tempArr1 = new ArrayList<>();
        ArrayList<RangeDate> tempArr2 = new ArrayList<>();
        
        tempArr1.add(root);
        allParents.put(level, tempArr1);
        tempArr2 = (ArrayList<RangeDate>) tempArr1.clone();
        level ++;
        
        while (level <= height - 1 ){
            tempArr1 = new ArrayList<>();
            for (RangeDate x : tempArr2) {
                tempChild = children.get(x);
                if ( tempChild != null){
                    for ( i = 0 ; i < tempChild.size() ; i ++ ){
                        tempArr1.add(tempChild.get(i));
                    }
                }
            }           
            allParents.put(level, tempArr1);
            tempArr2 = (ArrayList<RangeDate>) tempArr1.clone();
            level ++;  
        }
    }

    public Date getDateFromString(String tmstmp, boolean startDate) throws ParseException{
        Date d = null;
        SimpleDateFormat sf = null;
        Calendar calendar = Calendar.getInstance() ;
        
        if ( startDate == true){
            if (tmstmp.contains("/")){
                int num = StringUtils.countOccurrencesOf(tmstmp, "/");;
                if ( num == 1){
                    sf = new SimpleDateFormat("MM/yyyy");
                    d = sf.parse(tmstmp);
                }
                else{
                    sf = new SimpleDateFormat("dd/MM/yyyy");
                    d = sf.parse(tmstmp);
                }
            }
            else{
                sf = new SimpleDateFormat("yyyy");
                d = sf.parse(tmstmp);
            }
        }
        else{
            if (tmstmp.contains("/")){
                int num = StringUtils.countOccurrencesOf(tmstmp, "/");;
                if ( num == 1){
                    sf = new SimpleDateFormat("MM/yyyy");
                    d = sf.parse(tmstmp);
                    
                    calendar.setTime(d);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    d = calendar.getTime();
                }
                else{
                    sf = new SimpleDateFormat("dd/MM/yyyy");
                    d = sf.parse(tmstmp);
                }
            }
            else{
                sf = new SimpleDateFormat("yyyy");
                d = sf.parse(tmstmp);
                
                
                calendar.setTime(d);
                calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                d = calendar.getTime();
            }
        
        }
        
       /* if (curLevel == 2){
            sf = new SimpleDateFormat("dd/MM/yyyy");
            d = sf.parse(tmstmp);
            
            sf = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
            tmstmp = sf.format( d );
            
            d = sf.parse(tmstmp);
            
            System.out.println("edwwwwwwwwwwwwww = " + d);
            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
            //System.out.println("ddddddddddddddd = " + d);
            //d = formatter.parse(d.toString());
            
            //d = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
        }*/
        
        return d;
    }
    
    @Override
    public List<RangeDate> getChildren(RangeDate parent) {
       return this.children.get(parent);
    }

    @Override
    public Integer getLevel(RangeDate node) {
        return this.stats.get(node).level;
    }

    @Override
    public Integer getLevel(double nodeId) {
        return 0;
    }

    @Override
    public String getNodesType() {
        return this.nodesType;
    }

    @Override
    public RangeDate getParent(RangeDate node) {
         //System.out.println("GET PARENT Range = " +node.toString());
        if(this.levelFlash == -1){
            RangeDate r = new RangeDate(null,null);
            //r.nodesType = "int";
            //System.out.println("rrrrrrrrrrrrrrrrrrrrrrrrrrrrr = "+ r.toString());
            for (Map.Entry<RangeDate, RangeDate> entry : parents.entrySet()) {
                //System.out.println(entry.getKey()+" : "+entry.getValue());
                RangeDate r1 = entry.getKey();
               // System.out.println("r1 = " + r1);
                //System.out.println("r = " + r);
                //System.out.println("dfsdfsdfsd :" + parents.get(r1));
                //System.out.println("dfsdfsdfsd :" + parents.get(node));
                if (r1.equals(r)){
                    //System.out.println("i am hereeeeeeeeeeeeeeeeeeeeeedsfgsfewfe");
                }
            }

            return this.parents.get(node);
        }
        else{
            RangeDate rd = this.parents.get(node);
            int currentLevel = this.height - this.getLevel(node)  ;
//            System.out.println("flash level Date "+levelFlash+" anon level "+currentLevel+" anon Value "+rd);
            if(levelFlash == currentLevel){
               return rd;
            }
            else{
                return node;
            }
        }
    }

    @Override
    public RangeDate getRoot() {
        return this.allParents.get(0).get(0);
    }

    @Override
    public String getName() {
       return this.name;
    }

    @Override
    public Map<Integer, ArrayList<RangeDate>> getAllParents() {
        return this.allParents;
    }

    @Override
    public void export(String file) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("range");
            writer.println("name " + this.name);
            writer.println("type " + this.nodesType);
            writer.println("height " + this.height);
            writer.println();
            
            int counter = 1;

            //write parents - childen to file
            for(int curLevel = height - 2; curLevel >= 0; curLevel--){
                System.out.println("curLevel = " + curLevel);
                for (RangeDate curParent : this.allParents.get(curLevel)){
                    StringBuilder sb = new StringBuilder();
                    if (this.getChildren(curParent) != null){
                        for (RangeDate child : this.getChildren(curParent)){
                            sb.append(((RangeDate)child).dateToExportHierString(translateDateViaLevel(curLevel + 1)));
                            sb.append(" ");
                        } 
                        writer.println(((RangeDate)curParent).dateToExportHierString(translateDateViaLevel(curLevel)) + " has " + sb.toString());
                    }
                    
                }
                
                counter ++;
                writer.println();
                
            }
            writer.close(); 
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(HierarchyImplDouble.class.getName()).log(Level.SEVERE, null, ex);
        }
   
    }


    @Override
    public boolean contains(RangeDate o) {
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
    public void add(RangeDate newObj, RangeDate parent) {
        System.out.println("add new "+newObj.toString()+"root "+root+" parent "+parent+"\n List "+this.stats.toString());
        
        if(parent!=null){
            this.stats.put(newObj, new NodeStats(this.stats.get(parent).getLevel()+1));
            
            parents.put(newObj, parent);
            this.children.put(newObj, new ArrayList<RangeDate>());
            List<RangeDate> childrenList = children.get(parent);
            if(childrenList!=null){
                childrenList.add(newObj);
            }
            else{
                childrenList = new ArrayList();
                childrenList.add(newObj);
                children.put(parent,childrenList);
            }
            
            if(allParents.size()-1 == stats.get(parent).getLevel()){
                ArrayList<RangeDate> newParentList = new ArrayList();
                newParentList.add(newObj);
                allParents.put(this.stats.get(newObj).getLevel(),  newParentList);
                height++;
            }
            else{
                allParents.get(this.stats.get(newObj).getLevel()).add(newObj);
            }
        }
        else{
            System.out.println("Error: parent is null!");
        }
        
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        children = new HashMap<>();
        stats = new HashMap<>();
        parents = new HashMap<>();
        allParents = new HashMap<>();
        children.put(root, new ArrayList<RangeDate>());
        stats.put(root, new NodeStats(0));
    }

    @Override
    public void edit(RangeDate oldValue, RangeDate newValue) {
        RangeDate parent = null;
        ArrayList parentsList = null;
        List<RangeDate> childrenListNew = null,parentChildren=null;
        boolean changeRoot = false;
        
        System.out.println("before");
            for (Map.Entry<Integer, ArrayList<RangeDate>> entry : this.allParents.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("Old Value"+oldValue);
        childrenListNew = this.children.get(oldValue);
        if(childrenListNew!=null){
            if(this.allParents.get(0).get(0).equals(oldValue)){ //root
                  this.children.put(newValue,childrenListNew);
                  this.children.remove(oldValue);
                  
                  changeRoot = true;
                  
                  for(RangeDate child : childrenListNew){
                      this.parents.remove(child);
                      this.parents.put(child, newValue);
                  }
                  
                  parentsList = allParents.get(0);
                  parentsList.remove(oldValue);
                  parentsList.add(newValue);
                  this.allParents.put(0, parentsList);
                  this.stats.remove(root);
                  root.setLowerBound(newValue.lowerBound);
                  root.setUpperBound(newValue.upperBound);
                  this.stats.put(root, new NodeStats(0));
            }
            else{ // node with parent and children 
                this.children.put(newValue,childrenListNew);
                this.children.remove(oldValue);
                
             
                parent = this.parents.get(oldValue);
                parentChildren = this.children.get(parent);
                for(int i=0; i<parentChildren.size(); i++ ){
                    if(parentChildren.get(i).equals(oldValue)){
                        parentChildren.remove(i);
                        parentChildren.add(newValue);
                        break;
                    }
                }
                
                parents.remove(oldValue);
                parents.put(newValue,parent);
                
                for ( int i = 0; i < childrenListNew.size() ; i ++ ){
                    this.parents.remove(childrenListNew.get(i));
                    this.parents.put(childrenListNew.get(i), newValue);
                }
                
                parentsList = this.allParents.get(stats.get(oldValue).getLevel());
                for(int i=0; i<parentsList.size(); i++){
                    if(parentsList.get(i).equals(oldValue)){
                        parentsList.remove(i);
                        parentsList.add(newValue);
                        this.allParents.put(this.stats.get(oldValue).getLevel(), parentsList);
                        break;
                    }
                }
                
                
//                throw new UnsupportedOperationException("Not supported yet for nodes with parents and children.");
            }
        } // leaf
        else{
            
            parent = this.parents.get(oldValue);
            parentChildren = this.children.get(parent);
            for(int i=0; i<parentChildren.size(); i++){
                if(parentChildren.get(i).equals(oldValue)){
                    parentChildren.remove(i);
                    parentChildren.add(newValue);
                    break;
                }
            }
            
            this.parents.put(newValue,parent);
            this.parents.remove(oldValue);
            
            parentsList = this.allParents.get(allParents.size()-1);
            for(int i=0; i<parentsList.size(); i++){
                if(parentsList.get(i).equals(oldValue)){
                    parentsList.remove(i);
                    parentsList.add(newValue);
                    allParents.put(allParents.size()-1,parentsList);
                }
            }
            
//            System.out.println("Edwwww leaf range date");
//            throw new UnsupportedOperationException("Not supported yet for leaves nodes.");
        }
        
        if(!changeRoot){
            this.stats.put(newValue, this.stats.get(oldValue));
            this.stats.remove(oldValue);
        }
         //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<RangeDate>> remove(RangeDate obj) {
        List<RangeDate> parentsList;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Map<Integer,Set<RangeDate>> nodeMap = BFS(obj,null);
        for(int i=nodeMap.keySet().size(); i>0; i--){
            for(RangeDate itemToDelete : nodeMap.get(i)){
                
                if(root.equals(itemToDelete)){
                    System.out.println("Cannot remove root");
                    return null;
                }
                
                children.remove(itemToDelete);
                children.get(getParent(itemToDelete)).remove(itemToDelete);
                this.parents.remove(itemToDelete);
                
                parentsList = this.allParents.get(this.stats.get(itemToDelete).getLevel());
                parentsList.remove(itemToDelete);
                if(parentsList.isEmpty()){
                    allParents.remove(stats.get(itemToDelete).level);
                    height--;
                }
                stats.remove(itemToDelete);
            }
        }
        return nodeMap;
    }

    @Override
    public Map<Integer, Set<RangeDate>> dragAndDrop(RangeDate firstObj, RangeDate lastObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<RangeDate>> BFS(RangeDate firstnode, RangeDate lastNode) {
        Map<Integer,Set<RangeDate>> bfsMap = new HashMap<Integer,Set<RangeDate>>();
        LinkedList<RangeDate> listNodes = new LinkedList<RangeDate>();
        ArrayList childs1 = null;
        int counter = 1;
        int levelNode1;
        int levelNode2;
        Set s = new HashSet<RangeDate>();
        
        
        s.add(firstnode);
        bfsMap.put(counter,s);
        listNodes.add(firstnode);
        counter ++;
        
        levelNode1 = this.getLevel(firstnode);
        
        while (!listNodes.isEmpty()){
            childs1 = (ArrayList) this.getChildren(listNodes.getFirst());
            if ( childs1 != null && childs1.size() > 0){// ean exei paidia
                levelNode2 = this.getLevel((RangeDate) childs1.get(0));
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
                    s = new HashSet<RangeDate>();
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
    public void computeWeights(Data dataset, String column) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        for(RangeDate node : this.stats.keySet()){
            NodeStats s = this.stats.get(node);
            s.weight = 0;
        }
        
        Integer c;
        double[][] data = dataset.getDataSet();
        for(c  = 0 ; c < dataset.getColNamesPosition().keySet().size() ; c++){
            if(dataset.getColNamesPosition().get(c).equals(column)){
                break;
            }
        }
        
        for (double[] columnData : data) {
            System.out.println("columnData[c] = " + columnData[c]+" original value "+dataset.getDictionary().getIdToString((int)columnData[c]));
            compute(getRoot(), columnData[c],true,true,dataset);
            if(this.statsDistinct.get(columnData[c])!=null){
                this.statsDistinct.get(columnData[c]).weight++;
            }
            else{
               NodeStats nodestat = new NodeStats(0) ;
               nodestat.setWeight(1);
               this.statsDistinct.put(columnData[c], nodestat);
            }
        }   
    }
    
    public void compute(RangeDate r, Double value, boolean whichContain, boolean isRoot,Data dataset){
        if (isRoot == true){
            try {
                if(r.contains(dataset.getDictionary().getIdToString(value.intValue()))){
//                System.out.println("Compute");
//                System.out.println("value = " + value.toString());
//                System.out.println(r.toString());
                    this.stats.get(r).weight++;
                }
            } catch (ParseException ex) {
                Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {             
                if(r.contains2(dataset.getDictionary().getIdToString(value.intValue()),whichContain)){
//                System.out.println("Compute111111");
//                System.out.println("value = " + value.toString());
//                System.out.println(whichContain);
//                System.out.println(r.toString());
                    this.stats.get(r).weight++;
                }
            } catch (ParseException ex) {
                Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        List<RangeDate> ch = this.children.get(r);
        /*for ( int i = 0; i < ch.size() ; i ++ ){
            System.out.println("ch = " + ch.get(i).toString() + "\t i = " + i +"\tsize = " + ch.size());
            
        }*/
        if(ch != null){
            //List<Range> ch1 = this.children.get(ch.get(0));
            //if ( ch1 != null ){
                //System.out.println("ch = " + ch.toString());
                int counter = 1;
                for(RangeDate c : ch){
//                    System.out.println("cccccccccccccccc = " + c.toString());
                    if (value == 2147483646.0 || value.equals(Double.NaN)){
                       if (c.toString().equals("NaN - NaN")){
                           this.stats.get(c).weight++;
                           this.stats.get(getRoot()).weight++;
//                            System.out.println("value = " + value.toString());
//                            System.out.println(c.toString());
                       }
                       else if(c.toString().equals("0 - 0")){
                           this.stats.get(c).weight++;
                           this.stats.get(getRoot()).weight++;
//                            System.out.println("value = " + value.toString());
//                            System.out.println(c.toString());
                       }
                    }
                    else if (isRoot == true){
                        if(!c.toString().equals("0 - 0") && !c.toString().equals("NaN - NaN") ){
                            if (counter == ch.size()-1){
                                try {
                                    if(c.contains2(dataset.getDictionary().getIdToString(value.intValue()),false)){
//                                    System.out.println("Compute22222");
//                                    System.out.println("value = " + value.toString());
//                                    System.out.println(c.toString());
                                        compute(c, value,false,false,dataset);
                                    }
                                } catch (ParseException ex) {
                                    Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            else{
                                try {
                                    if(c.contains2(dataset.getDictionary().getIdToString(value.intValue()),true)){
//                                    System.out.println("Compute3333");
//                                     System.out.println("value = " + value.toString());
//                                    System.out.println(c.toString());
                                        compute(c, value,true,false,dataset);
                                    }
                                } catch (ParseException ex) {
                                    Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                    else{
                        //if(!c.toString().equals("0 - 0") && !c.toString().equals("NaN - NaN") ){
                       
                            if (counter == ch.size()){
                                try {
                                    if(c.contains2(dataset.getDictionary().getIdToString(value.intValue()),false)){
//                                    System.out.println("Compute44444");
//                                    System.out.println("value = " + value.toString());
//                                    System.out.println(c.toString());
                                        compute(c, value,false,false,dataset);
                                    }
                                } catch (ParseException ex) {
                                    Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            else{
                                try {
                                    if(c.contains2(dataset.getDictionary().getIdToString(value.intValue()),true)){
//                                    System.out.println("Compute555555");
//                                     System.out.println("value = " + value.toString());
//                                    System.out.println(c.toString());
                                        compute(c, value,true,false,dataset);
                                    }
                                } catch (ParseException ex) {
                                    Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        //}
                    }
                    counter++;
                }
            //}
           // else{
            
            //}
            
        }
    }

    @Override
    public Integer getWeight(RangeDate node) {
        return this.stats.get(node).weight;
    }

    @Override
    public Integer getWeight(double nodeId) {
        return this.statsDistinct.get(nodeId).weight;
    }

    @Override
    public int getLevelSize(int level) {
       return this.allParents.get(this.height - level - 1).size();
    }

    @Override
    public void autogenerate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RangeDate checkColumn(int column, Data dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DictionaryString getDictionary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean validCheck(String parsePoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public RangeDate getParent(Double d) {
       
        String value = this.dictData.getIdToString(d.intValue());
        RangeDate parent = null;
        try {
            parent = this.getParent(AnonymizedDataset.getDateFromString(value));
        } catch (ParseException ex) {
            Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return parent;
        
    }
    
    @Override
    public RangeDate getParent(Date d) {
        if(d==null){
            RangeDate nullDate = new RangeDate(null,null);
            return this.parents.get(nullDate);
        }
        List<RangeDate> leafNodes = this.allParents.get(this.height-1);
       // System.out.println("parentssize = " + this.allParents.size());
        //System.out.println("Arrr get parent = " + leafNodes.toString() +"\t d = " + d);
        RangeDate r = binarySearch(leafNodes, d);
        
        //System.out.println("Find parent of " + d + " is " + r.toString());
        return r;
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
    public void setNodesType(String nodesType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findAllChildren(RangeDate node, int sum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getInputFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public int translateDateViaLevel(int level){
    
        if ( months == 0 && days == 0){
            return 0;
        }
        
        if ( days == 0){
            if (level == height - 1){
                return 1;
            }
        }
        else{
            if (level == height - 1){
                return 2;
            }
        }
        
        if ( months != 0  && level == height-2){
            return 1;
        }
        
        
        return 0;
    }
    
    
    @Override
    public Graph getGraph(String nodeInput, int nodeLevel) {
        System.out.println("months = " + months + "\t days = " + days + "\theight = " + height);
        Graph graph = new Graph();
        Node n = null;
        Edge e = null;
        ArrayList<String> nodes = null;
        boolean FLAG = false;
        String parent = null;
        List<RangeDate> nodeChilds = null;
        RangeDate nodeRange = null;
        String color = null;
        String label = null;
        int levelHier = 0;
        
        
        /*for (Map.Entry<RangeDate, NodeStats> entry : stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().level);
        }*/
        
        
        
        /*System.out.println("all Parents");
        for (Map.Entry<Integer,ArrayList<RangeDate>> entry : allParents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("Stats");
        for (Map.Entry<RangeDate, NodeStats> entry : stats.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("parents");
        for (Map.Entry<RangeDate, RangeDate> entry : parents.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");
        
        System.out.println("children");
        for (Map.Entry<RangeDate, List<RangeDate>> entry : children.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
        System.out.println("/////////////////////////////////////");*/
        
        
        
        
        RangeDate node = null;
        //RangeDate node1 = null;
       // System.out.println("Get Graphhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println(" nodeInputtttt = " + nodeInput);
        //System.out.println(" nodeLevellllllll = " + nodeLevel);
        
        if ( !nodeInput.equals("null") ){
            //System.out.println("nodeInput11111 = " + nodeInput);

            String []temp = null;
            temp = nodeInput.split("-");
            try {        
                if (!nodeInput.equals("(null)")){
                    node  = new RangeDate( this.getDateFromString(temp[0],true), this.getDateFromString(temp[1],false));
                }
                else{
                    node = new RangeDate(null,null);
                }
                
            } catch (ParseException ex) {
                Logger.getLogger(HierarchyImplRangesDate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        int counter = 0;
        
        //if (nodesType.equals("double")){
            if (  !nodeInput.equals("null") && !nodeInput.equals("") && nodeLevel != 0 ){

                if (height > nodeLevel + 1){
                    nodeRange = node;

                    for (int i = nodeLevel ; i >= 0 ; i --){
                        levelHier = 1;
                        nodeChilds = this.children.get(nodeRange);
                     
                        if ( nodeChilds != null){
                           
                            for (int j = 0 ; j < nodeChilds.size() ; j ++){                                
                                
                                if (this.getLevel(nodeChilds.get(j)) == this.height -1){
                                    color = "red";
                                }
                                else{
                                    color = null;
                                }
                                
                                if (nodeChilds.get(j).getLowerBound() == null && nodeChilds.get(j).getUpperBound() == null){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j).dateToString(translateDateViaLevel(i+1));
                                }
                                
                                
                                n = new Node(label,label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange.dateToString(translateDateViaLevel(i)),nodeChilds.get(j).dateToString(translateDateViaLevel(i+1)));
                                graph.setEdge(e);
                                counter ++;
                            }
                            levelHier ++;
                        }
                        else{
                            System.out.println("noChildren111");
                        }
                        

                        nodeRange = this.parents.get(nodeRange);
                        System.out.println("PArent "+nodeRange);
                        
                    }
                    n = new Node(root.dateToString(0),root.dateToString(0),0,null,this.hierarchyType + "," +this.nodesType);
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
                                if (nodeChilds.get(j).getLowerBound() == null && nodeChilds.get(j).getUpperBound() == null){
                                    label = "(null)";
                                }
                                else{
                                    label = nodeChilds.get(j).dateToString(i+1);
                                }
                                                               
                                n = new Node(label,label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange.dateToString(i),nodeChilds.get(j).dateToString(i+1));
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren2222");
                        }
                        nodeRange = this.parents.get(nodeRange);
                        //System.out.println("node = " + node);
                    }
                    n = new Node(root.dateToString(0),root.dateToString(0),0,null,this.hierarchyType + "," +this.nodesType);
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
                                if (nodeChilds.get(j).getLowerBound() == null && nodeChilds.get(j).getUpperBound() == null){
                                    label = "(null)";
                                }
                                else{
                                    //label = nodeChilds.get(j)+"";
                                    label = nodeChilds.get(j).dateToString(translateDateViaLevel(1))+"";
                                }
                                
                                n = new Node(label,label,i+1,color,this.hierarchyType + "," +this.nodesType);  
                                graph.setNode(n);
                                e = new Edge(nodeRange.dateToString(translateDateViaLevel(0)),nodeChilds.get(j).dateToString(translateDateViaLevel(1)));
                                graph.setEdge(e);
                                counter ++;
                            }
                        }
                        else{
                            System.out.println("noChildren3333");
                        }
                        nodeRange = this.parents.get(nodeRange);
                    }
                    n = new Node(root.dateToString(0),root.dateToString(0),0,null,this.hierarchyType + "," +this.nodesType);
                    graph.setNode(n);


            }
        //}        
        
        Collections.reverse(graph.getNodeList());
        Collections.reverse(graph.getEdgeList());

        
        //graph.print();
        
        /*for ( int i = 0 ; i < graph.getNodeList().size() ; i ++){
            System.out.println("\tid = " +graph.getNodeList().get(i).getLabel() );
        }
        
        
        for ( int i = 0 ; i < graph.getEdgeList().size() ; i ++){
            System.out.println("From = " + graph.getEdgeList().get(i).getFrom() + "\t to = " + graph.getEdgeList().get(i).getTo() +"\t id = " + graph.getEdgeList().get(i).getId() );
            
        }*/
        return graph;
    }

    @Override
    public String checkHier() {
        String str = null;
        
//        System.out.println("Check Hierarchies RangeDate");
        
        List<RangeDate> firstLabel = allParents.get(0);
        List<RangeDate> lastLabel = allParents.get(allParents.size()-1);
        List<RangeDate> preLastLabel = allParents.get(allParents.size()-2);
        
        Date upperLimit = firstLabel.get(0).upperBound;
        Date lowerLimit = firstLabel.get(0).lowerBound;
       
        
        if(allParents.size()-1 == 2){
            if(lastLabel.size()==1){
                return "Ok";
            }
        }
        
        if(preLastLabel.size() > lastLabel.size()){
            return "Hierarchy:" + this.name + "\nNumber of nodes in the last level must be greater than the previous level.";
        }
        
//        System.out.println("before");
//        for( int i = 0 ; i < preLastLabel.size() ; i ++){
//            System.out.println("xaxa = " + preLastLabel.get(i));
//        }
        
//        Collections.sort(preLastLabel, new Comparator<RangeDate>(){
//            @Override
//            public int compare(RangeDate o1, RangeDate o2) {
//                    return o1.getLowerBound().compareTo(o2.getLowerBound());
//            }
//        });
        
//        System.out.println("after");
//        for( int i = 0 ; i < preLastLabel.size() ; i ++){
//            System.out.println("xaxa = " + preLastLabel.get(i));
//        }
        
        for(Map.Entry<Integer,ArrayList<RangeDate>> entry : allParents.entrySet()){
            
            if(entry.getKey()!=0){
                ArrayList<RangeDate> tempArr = entry.getValue();
                Collections.sort(tempArr, new Comparator<RangeDate>(){
                    @Override
                    public int compare(RangeDate o1, RangeDate o2){
                        if(o1.getLowerBound()==null || o2.getLowerBound()==null){
                            return -1;
                        }
                        return o1.getLowerBound().compareTo(o2.getLowerBound());
                    }
                });
                
                
                for(int i=0; i<tempArr.size()-1; i++){
                    RangeDate current = tempArr.get(i);
                    RangeDate next = tempArr.get(i+1);
                    
                    if(current.lowerBound==null && current.upperBound==null){
                        continue;
                    }
                    
                    Calendar c = Calendar.getInstance();
                    c.setTime(current.upperBound);
                    c.add(Calendar.DATE, 1);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    Date plusOneDay = c.getTime();
                    
//                    System.out.println("current="+current.upperBound+" plusOneDAy="+plusOneDay+" next="+next.lowerBound);
                    if((current.lowerBound==null && current.upperBound==null) || (next.upperBound==null && next.lowerBound==null)){
                        continue;
                    }
                    
                    if(!plusOneDay.equals(next.lowerBound)){
                        str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nNot continuous values between ranges: " + current.toString() + " and " + next.toString();
                        return str;
                    }
                    
                    if(i==0){
                        if(!current.lowerBound.equals(lowerLimit)){
                           str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nFirst node of the last level must have the same lower bound as the root node. Problem in range:" + current.toString() + ". Root range is :" + lowerLimit + "-" + upperLimit;
                                return str;
                        }
                    }
                    else if(i == tempArr.size()-2){
                        if(!next.upperBound.equals(upperLimit)){
                             str = "Hierarchy Name: " + this.name + "\nLevel: " + entry.getKey() +"\nLast node of the last level must have the same upper bound as the root node. Problem in range:" + next.toString() + ". Root range is :" + lowerLimit + "-" + upperLimit;
                             return str;
                        }
                    }
                }
            }
        }

        return str;
    }
    
    private RangeDate binarySearch(List<RangeDate> list, Date d){
//        System.out.println("binary Search...");
        
        if(list.isEmpty()){
            return null;
        }
        
        //System.out.println("d = " + d);
        
        //System.out.println("binary Searchhhhhhhhhhhhhhhhhhhhhhh = " + list.get(0));
        
        int mid = (list.size()-1)/2;
        
        //System.out.println("mid = " + mid);
        if((list.size()-1)%2 > 0){
            mid++;
            //System.out.println("Mid of " + list.size() + " is " + mid);
        }
      // System.out.println("Mid of " + list.size() + " is " + mid);
        if(list.size()-1 == 1){
            //System.out.println("Return = " + list.get(0));
            return list.get(0);
        }
        RangeDate r = list.get(mid);
        
        //System.out.println("d = " + d + "\t mid = " + r.toString());
        
        //System.out.println("rrrrrrrrrrrrrrrr = " + r.toString());
        
        if(d.before(r.lowerBound)){
            //System.out.println("gia pameeeeeeeeeeeeeeee");
            //return binarySearch(list.subList(0, mid-1), d);
            for ( int i = 0 ; i < mid ; i ++ ){
                r = list.get(i);
                
               
                
                //System.out.println("lower = " + r.toString());
                if ( r.contains2(d,false)){
                    return r;
                }
            }
        }
        else if (d.after(r.upperBound)){
            //System.out.println("gia pameeeeeeeeeeeeeeeeeee 2");
            for ( int i = mid ; i < list.size() ; i ++ ){
                //System.out.println("upper = " + r.toString());
                r = list.get(i);
                
                //System.out.println("i = " + i + " \t listsize = " + list.size() + "\tstring = " + r.toString());
                
                /*if (list.get(list.size()-1).equals(new RangeDouble(Double.NaN,Double.NaN)) ){
                    if( i == list.size()-2){
                        if ( r.contains2(d,false)){
                            return r;
                        }
                    }
                    else{
                        if ( r.contains2(d,true)){
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
                        if ( r.contains2(d,true)){
                            return r;
                        }
                    }
                }*/
                if ( r.contains2(d,false)){
                    return r;
                }
                        
            }
           //return binarySearch(list.subList(mid+1, list.size()-1), d);
        }
        else{
            
            //System.out.println("gia pameeeeeeeeeeeeeee3");
            return r;
        }
        
        /*if(d < r.lowerBound){
            return binarySearch(list.subList(0, mid-1), d);
        }
        else if (d > r.upperBound){
            
           return binarySearch(list.subList(mid+1, list.size()-1), d);
        }
        else{
            return r;
        }*/
        
        return null;
    }

    
    
    public String dateToString(int level, Date lowerBound){
        String str = null;
        Calendar calendar = Calendar.getInstance() ;
        
        calendar.setTime(lowerBound);
        
        switch (level) {
            case 0:
                str = calendar.get(Calendar.YEAR) +"";
                break;
            case 1:
                str = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
                break;
            default:
                str = calendar.get(Calendar.DAY_OF_MONTH) + "/" + ( calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
                break;
        }
        
        return str;
    }

    @Override
    public List<RangeDate> getNodesInLevel(int level) {
        int curLevel = this.height - level - 1;
        List<RangeDate> curLevelIds =  this.allParents.get(curLevel);
        return curLevelIds;
    }

    @Override
    public void setDictionaryData(DictionaryString dict) {
        dictData = dict;
    }

    @Override
    public void setLevel(int l) {
        this.levelFlash = l;
    }

    @Override
    public Map<Integer, Set<RangeDate>> getLeafNodesAndParents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void syncDictionaries(Integer column, Data data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
