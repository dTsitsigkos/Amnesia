/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

import controller.AppCon;
import data.DiskData;
import hierarchy.Hierarchy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author nikos
 */
public class Clusters {
    private String urlDb;
    private String urlBbTemp;
    private int lastClusterId;
    private int maxClusters;
    private int size;
    private int k;
    private Map<Integer,Integer> currentSizeCl;
    private Connection conn;
    private Map<Integer,List<Integer>> deleteQueries;
    private Map<Integer,List<Pair<Integer,Double>>> updateQueries;
    private int[] deleteClusterPainter;
    private Map<Integer,Set<Pair<Integer,Double>>> insertQueries;
    private int counterUpdates;
    private int counterInserts;
    private int counterDeletions;
    private int threshold = 8000;
    private int computedSmallSize = 0;
    private boolean withSplits;
    private double recordsProportion = 0.1;
    
    public Clusters(String urlDatabase, int maxclusters, int k){
        this.urlDb = urlDatabase;
        this.lastClusterId = 0;
        this.counterDeletions = 0;
        this.maxClusters = maxclusters;
        this.size = 2*k-1;
        this.currentSizeCl = new Hashtable();
        this.k = k;
        this.withSplits = true;
        
        System.out.println("Thresehold "+this.threshold);
        int possible_thresehold = ((Double)((maxclusters*k)*recordsProportion)).intValue();
        if(possible_thresehold > this.threshold){
            this.threshold = possible_thresehold;
            System.out.println("Thresehold New"+this.threshold);
            long heapFreeSize = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
            long mapSize = 40 * this.threshold + this.threshold*(Integer.BYTES+Double.BYTES);
            System.out.println("Heap "+heapFreeSize+" Map "+mapSize);
            if(mapSize > heapFreeSize){
                long availableSize = ((Long)(heapFreeSize*5/100));
                this.threshold = ((Long)(availableSize/((Integer.BYTES+Double.BYTES) + 40))).intValue();
                System.out.println("New Thresehold "+this.threshold);
            }
        }
            
        
        this.insertQueries = new TreeMap();
        this.updateQueries = new TreeMap();
        this.counterUpdates = 0;
        this.deleteQueries = new TreeMap();
        this.deleteClusterPainter = new int [2*maxClusters+1];
        this.counterInserts = 0;
        
        try{
            Class.forName("org.sqlite.JDBC").newInstance();
            conn = DriverManager.getConnection(this.urlDb);
            if (conn != null) {
                System.out.println("Database connected with cluster class");
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error opening database in clusters!");
        }
        
    }
    
    public void setSplit(boolean ws){
        this.withSplits = ws;
    }
    
    private void reconnect(){
        try{
            Class.forName("org.sqlite.JDBC").newInstance();
            conn = DriverManager.getConnection(this.urlDb);
            if (conn != null) {
                System.out.println("Database connected with cluster class");
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error opening database in clusters!");
        }
    }
    
    
    
    
    public void createCluster(){
        String sqlCreate = "";
        Statement stm = null;
        PreparedStatement pstm = null;
        this.daleteClusterTable();
        
        try{
            conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            sqlCreate = "CREATE TABLE cluster (id_cl integer PRIMARY KEY, cluster_id integer , distance real,  FOREIGN KEY (id_cl) REFERENCES dataset (id))";
            stm.execute(sqlCreate);
            conn.commit();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" create table cluster");
        } finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(pstm!=null){
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DiskData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public int getLastClusterId(){
        return this.lastClusterId;
    }
    
    public void setLastClusterId(int clId){
        this.lastClusterId = clId;
    }
    
    public int getmaxIdCluster(){
        if(!currentSizeCl.isEmpty())
            return Collections.max(this.currentSizeCl.keySet());
        else
            return 0;
    }
    
    
    public Double[][] removeSmallClusters(Centroid[] centroids, List<Integer> smallClusters){
        String sqlUnique = "SELECT * FROM cluster,dataset WHERE cluster_id IN "+Arrays.toString(smallClusters.toArray()).replace("[", "(").replace("]", ")")+" AND id_cl=id";
        String delUnique = "DELETE FROM cluster WHERE cluster_id IN "+Arrays.toString(smallClusters.toArray()).replace("[", "(").replace("]", ")");
        Statement stm = null;
        ResultSet rs = null;
        Double[][] records = null;
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            rs = stm.executeQuery(sqlUnique);
            ResultSetMetaData resultMeta = rs.getMetaData();
            records = new Double[this.computedSmallSize][resultMeta.getColumnCount()-3];
            int i=0;
            while(rs.next()){
                for(int j=4; j<=resultMeta.getColumnCount(); j++){
                    records[i][j-4] = rs.getDouble(j);
                }
                i++;
            }
            
            
            for(Integer clusterId : smallClusters){
                this.currentSizeCl.remove(clusterId);
                centroids[clusterId] = null;
            }
            stm.executeUpdate(delUnique);

            
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: remove unique clusters "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return records;
    }
    
   
    
    public void remove(int clusterId, int recordId){
        try{
            
            if(this.deleteQueries.get(clusterId) == null){
                List<Integer> recIds = new ArrayList();
                recIds.add(recordId);
                this.deleteQueries.put(clusterId, recIds);
            }
            else{
                this.deleteQueries.get(clusterId).add(recordId);
            }
            
            this.counterDeletions++;
            
            this.currentSizeCl.put(clusterId, this.currentSizeCl.get(clusterId)-1);
            

            
        }catch(Exception e){
            System.err.println("Error: "+e.getMessage()+" remove from cluster "+clusterId);
            e.printStackTrace();
        }
    }
    
    public void executeDeleteBatch(int clusterId){
        Statement stm = null;
        int counterDelete = 0;
        boolean executeDelete=false;
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();

            List<Integer> removeIds = this.deleteQueries.get(clusterId);
            if(removeIds!=null){
                String sqlDelete = "DELETE FROM cluster";
                for(Integer record : removeIds){
                    stm.executeUpdate(sqlDelete+" WHERE id_cl = "+record+" AND cluster_id = "+clusterId);
                    executeDelete = true;
                    counterDelete++;
                }
            }
            
            if(executeDelete){
                this.conn.commit();
                System.out.println("Execute Delete "+clusterId);
                this.deleteQueries.put(clusterId, null);
                this.counterDeletions -= counterDelete;
            }
        }catch(Exception e){
            System.err.println("Error : cluster excute batch "+e.getMessage());
            e.printStackTrace();
            try {
                this.conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void executeDeleteBatch(){
        Statement stm = null;
        boolean executeDelete = false;
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
            for(Entry<Integer,List<Integer>> entry : this.deleteQueries.entrySet()){
                String sqlDelete = "DELETE FROM cluster";
                for(Integer record : entry.getValue()){
                    stm.executeUpdate(sqlDelete+" WHERE id_cl = "+record+" AND cluster_id = "+entry.getKey());
                    executeDelete = true;
                }
            }
            
            if(executeDelete){
                this.conn.commit();
                System.out.println("Execute Delete");
                this.deleteQueries.clear();
                this.counterDeletions = 0;
            }
        }catch(Exception e){
            System.err.println("Error : cluster excute batch "+e.getMessage());
            e.printStackTrace();
            try {
                this.conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void executeBatch(int clusterId){
        this.executeDeleteBatch();
        System.out.println("Execute batch cluster "+clusterId);
        Statement stm = null;
        int counter = 0;
        String errMessage="";
        boolean executeUpdate=false;
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
            int counterUpdate = 0;
            Set<Pair<Integer,Double>> insertClIds = this.insertQueries.get(clusterId);
            
            if(insertClIds!=null){
                counter++;
                String sqlInsert = "INSERT INTO cluster(id_cl,cluster_id,distance) VALUES(";
                for(Pair<Integer,Double> recDist : insertClIds){
                    errMessage = sqlInsert+recDist.getKey()+","+clusterId+","+recDist.getValue()+")";
                    stm.executeUpdate(sqlInsert+recDist.getKey()+","+clusterId+","+recDist.getValue()+")");
                    executeUpdate = true;
                }
            }
            if(executeUpdate){
                this.conn.commit();
                this.insertQueries.remove(clusterId);
                counterInserts -= counterUpdate;
            }
        }catch(Exception e){
            System.err.println("Error : cluster excute batch "+e.getMessage());
            System.err.println(errMessage);
            e.printStackTrace();
            try {
                this.conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void executeUpdateBatch(){
        String sqlUpdate = "UPDATE cluster SET distance = ? WHERE id_cl = ? AND cluster_id = ?";
        PreparedStatement pstm = null;
        boolean executeUpdate = false;
        try{
            pstm = this.conn.prepareStatement(sqlUpdate);
            this.conn.setAutoCommit(false);
            for(Entry<Integer,List<Pair<Integer,Double>>> entryUpdate : this.updateQueries.entrySet()){
                for(Pair<Integer,Double> entryRecUpdate : entryUpdate.getValue()){
                    pstm.setDouble(1, entryRecUpdate.getValue());
                    pstm.setInt(2, entryRecUpdate.getKey());
                    pstm.setInt(3, entryUpdate.getKey());
                    pstm.executeUpdate();
                    executeUpdate = true;
                }
            }
            
            if(executeUpdate){
                this.conn.commit();
                this.updateQueries.clear();
                this.counterUpdates = 0;
            }
            
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error execute update batch "+e.getMessage());
        }finally{
            if(pstm!=null){
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void executeBatch(){
        this.executeDeleteBatch();
        System.out.println("Execute batch");
        Statement stm = null;
        int counter = 0;
        String errMessage="";
        boolean executeUpdate=false;
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
            for(Entry<Integer,Set<Pair<Integer,Double>>> entry : this.insertQueries.entrySet()){
                counter++;
                if(entry == null)
                    System.out.println(counter+" SQl insert ");
                String sqlInsert = "INSERT INTO cluster(id_cl,cluster_id,distance) VALUES(";
                for(Pair<Integer,Double> recDist : entry.getValue()){
                    errMessage = sqlInsert+recDist.getKey()+","+entry.getKey()+","+recDist.getValue()+")";
                    stm.executeUpdate(sqlInsert+recDist.getKey()+","+entry.getKey()+","+recDist.getValue()+")");
                    executeUpdate=true;
                }
            }
            if(executeUpdate){
                this.conn.commit();
                this.insertQueries.clear();
                counterInserts = 0;
            }
        }catch(Exception e){
            System.err.println("Error : cluster excute batch "+e.getMessage());
            System.err.println(errMessage);
            e.printStackTrace();
            try {
                this.conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void update(int clusterId, int recordId , double distance){
        if(this.updateQueries.get(clusterId) == null){
            List<Pair<Integer,Double>> list = new ArrayList<Pair<Integer,Double>>();
            list.add(new Pair(recordId,distance));
            this.updateQueries.put(clusterId, list);
        }
        else{
            this.updateQueries.get(clusterId).add(new Pair(recordId,distance));
        }
        
        this.counterUpdates++;
    }
    public int put(int clusterId, int recordId, double distance,Semaphore split){
        int newCluster = -1;
        try{
            if(split!=null){
                if(split.tryAcquire()){
                    split.release();
                    
                    if(this.insertQueries.get(clusterId) == null){
                        Set<Pair<Integer,Double>> list = new HashSet<Pair<Integer,Double>>();
                        list.add(new Pair(recordId,distance));
                        this.insertQueries.put(clusterId, list);
                    }
                    else{
                        this.insertQueries.get(clusterId).add(new Pair(recordId,distance));
                    }
                }
                else{
                    split.acquire();
                    
                    if(this.insertQueries.get(clusterId) == null){
                        Set<Pair<Integer,Double>> list = new HashSet<Pair<Integer,Double>>();
                        list.add(new Pair(recordId,distance));
                        this.insertQueries.put(clusterId, list);
                    }
                    else{
                        this.insertQueries.get(clusterId).add(new Pair(recordId,distance));
                    }
                    split.release();
                }
            }
            else{
                
                
                if(this.insertQueries.get(clusterId) == null){
                    Set<Pair<Integer,Double>> list = new HashSet<Pair<Integer,Double>>();
                    list.add(new Pair(recordId,distance));
                    this.insertQueries.put(clusterId, list);
                }
                else{
                    this.insertQueries.get(clusterId).add(new Pair(recordId,distance));
                }
            }
            counterInserts++;
            if(this.currentSizeCl.get(clusterId)!=null){
                this.currentSizeCl.put(clusterId, this.currentSizeCl.get(clusterId)+1);
                if(this.withSplits && this.currentSizeCl.get(clusterId) == size+1){
                    if(split != null){
                        split.acquire();   
                    }
                    this.executeBatch(clusterId);
                    newCluster = split(clusterId);

                    if(split!=null)
                        split.release();

                }
                else if(counterInserts >= this.threshold){
                    if(split != null){
                        split.acquire();   
                    }
                    this.executeBatch();


                    if(split!=null)
                        split.release();
                }
            }
            else if(counterInserts >= this.threshold){
                if(split != null){
                    split.acquire();   
                }
                this.executeBatch();


                if(split!=null)
                    split.release();
            }
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage());
        }
        
        return newCluster;
        
    }
    
    
    public boolean hasRightSize(int clusterId){
        return this.currentSizeCl.get(clusterId) >= this.k;
    }
    
    public int getK(){
        return k;
    }
    
    
    
    public void split(Centroid[] centroids, Map<Integer,Hierarchy> hiers){
        List<Integer> bigClusters = this.getBigclusters();
        List<Integer> newClusters = new ArrayList();
        for(Integer cluster : bigClusters){
            newClusters.addAll(newSplit(cluster,hiers,centroids));
        }
        this.executeBatch();
        this.executeUpdateBatch();
        
    }
    
    public Map<Integer,Double[][]> getClusterDatasetRecs(List<Integer> clusters, boolean newConnection){
        String sqlSelect = "SELECT * FROM cluster,dataset WHERE cluster_id IN "+Arrays.toString(clusters.toArray()).replace("[", "(").replace("]", ")")+" AND id=id_cl ORDER BY cluster_id,distance";
        Statement stm = null;
        ResultSet rs = null;
        Map<Integer,Double[][]> clustersRecords = null;
        int cluster=-1;
        Connection newConn=null;
        try{
            if(newConnection){
                newConn = DriverManager.getConnection(this.urlBbTemp);
                stm = newConn.createStatement();
            }
            else{
                stm = this.conn.createStatement();
            }
            clustersRecords = new HashMap();
            rs = stm.executeQuery(sqlSelect);
            ResultSetMetaData resultMeta = rs.getMetaData();
            int i=-1;
            
            while(rs.next()){
                cluster = rs.getInt(2);
                
                if(clustersRecords.get(cluster) == null){
                    clustersRecords.put(cluster,new Double[this.currentSizeCl.get(cluster)][resultMeta.getColumnCount()-3]);
                    i=0;
                }
                for(int j=4; j<=resultMeta.getColumnCount(); j++){
                    clustersRecords.get(cluster)[i][j-4] = rs.getDouble(j);
                }
                
                i++;
                
            }
            
            
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error getClusterDatasetRecs : "+e.getMessage()+" clusterId "+cluster+" lenght "+this.currentSizeCl.get(cluster));
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(newConn != null){
                try {
                    newConn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                 
        }
        return clustersRecords;
    }
    private List<Integer> newSplit(int clusterId,Map<Integer,Hierarchy> hiers,Centroid[] centroids){
        String sqlSelect = "SELECT * FROM cluster,dataset WHERE cluster_id="+clusterId+" AND id=id_cl ORDER BY distance";
        Statement stm = null;
        List<Integer> newClusters = new ArrayList();
        int remainder = this.currentSizeCl.get(clusterId) % this.k;
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            ResultSet rs = stm.executeQuery(sqlSelect);
            ResultSetMetaData resultMeta = rs.getMetaData();
            Double[] record = new Double[resultMeta.getColumnCount()-3];
            int size_cluster=0,i=0; 
            this.lastClusterId++;
            this.currentSizeCl.put(lastClusterId, 0);
            Centroid newCentroid = null;
            while(i<this.k){
                rs.next();
                for(int j=4; j<=resultMeta.getColumnCount(); j++){
                    record[j-4] = rs.getDouble(j);
                }
                
                if(i==0){
                    newCentroid = new Centroid(clusterId,hiers,record,true);
                }
                else{
                    double distance = newCentroid.computeDistance(record,true);
                    this.update(clusterId, record[0].intValue(), distance);
                    newCentroid.update(record,true);
                }
                
                i++;
            }
            
            centroids[clusterId] = newCentroid;
            while(remainder!=0){
                rs.next();
                this.remove(clusterId, rs.getInt(1));
                this.put(this.lastClusterId, rs.getInt(1), rs.getDouble(3), null);
                size_cluster++;
                for(int j=4; i<=resultMeta.getColumnCount(); i++){
                    record[j-4] = rs.getDouble(j);
                }
                if(size_cluster == 1){
                    newCentroid = new Centroid(clusterId,hiers,record,true);
                }
                else{
                    double distance = newCentroid.computeDistance(record,true);
                    this.update(clusterId, record[0].intValue(), distance);
                    newCentroid.update(record,true); 
                }
                remainder--;
            }
            int counter = 0;
            while(rs.next()){
                for(int j=4; i<=resultMeta.getColumnCount(); i++){
                    record[j-4] = rs.getDouble(j);
                }
                
                if(counter % this.k == 0){
                    newClusters.add(this.lastClusterId);
                    centroids[lastClusterId] = newCentroid;
                    this.lastClusterId++;
                    this.currentSizeCl.put(lastClusterId, 0);
                    size_cluster=0;
                    counter = 0;
                }
                
                this.remove(clusterId, rs.getInt(1));
                this.put(this.lastClusterId, rs.getInt(1), rs.getDouble(3), null);
                counter++;
                size_cluster++;
                
                if(size_cluster == 1){
                    newCentroid = new Centroid(clusterId,hiers,record,true);
                }
                else{
                    double distance = newCentroid.computeDistance(record,true);
                    this.update(clusterId, record[0].intValue(), distance);
                    newCentroid.update(record,true); 
                }
            }
            newClusters.add(this.lastClusterId);
            centroids[lastClusterId] = newCentroid;
            rs.close();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error new split : "+e.getMessage());
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return newClusters;
    }
    
    private int split(int clusterId){
        System.out.println("Split "+clusterId+" "+(this.lastClusterId+1));
        
        int newCluster = this.lastClusterId +1;
        this.lastClusterId++;
        String sqlInsert = "INSERT INTO cluster (id_cl,cluster_id,distance) VALUES(?,?,?)";
        String sqlSelect = "SELECT * FROM cluster WHERE cluster_id="+clusterId+" ORDER BY distance DESC LIMIT "+this.k;
        String sqlDelete = "DELETE FROM cluster WHERE cluster_id="+clusterId+" AND id_cl = ?";
        Statement stm = null;
        PreparedStatement pstm = null,pstmDel = null;
        
        try{
            stm = this.conn.createStatement();
            ResultSet rs = stm.executeQuery(sqlSelect);
            ResultSetMetaData metaData = rs.getMetaData();
            this.conn.setAutoCommit(false);
            pstm = this.conn.prepareStatement(sqlInsert);
            pstmDel = this.conn.prepareStatement(sqlDelete);
            while(rs.next()){
                
                pstmDel.setInt(1, rs.getInt(1));
                pstmDel.executeUpdate();
                
                
                pstm.setInt(1, rs.getInt(1));
                pstm.setDouble(2, newCluster);
                pstm.setDouble(3, rs.getDouble(3));
                pstm.executeUpdate();
                             
            }
            this.conn.commit();
            this.currentSizeCl.put(this.lastClusterId, k);
            this.currentSizeCl.put(clusterId, k);
            
            rs.close();
           
           
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" create table cluster split");
        } finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(pstm!=null){
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                this.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return newCluster;
    }
    
    
    public Integer[] getClustersIds(){
        return this.currentSizeCl.keySet().toArray(new Integer[this.currentSizeCl.keySet().size()]);
    }
    
    
    public Centroid getCentroid(int clusterId, Map<Integer,Hierarchy> hiers){
        String sqlSelect = "SELECT id_cl FROM cluster WHERE cluster_id="+clusterId+" ORDER BY distance";
        String sqlSelectData = "SELECT * FROM dataset WHERE id";
        String sqlLimit = "LIMIT 1";
        String sqlUpdate = "UPDATE cluster SET distance = ? WHERE id_cl = ? AND cluster_id = ?";
        Statement stm = null,stmData = null;
        PreparedStatement pstm = null;
        Centroid newCentroid=null;
        Double[] firstRecord = null;
        ResultSet result=null;
        Connection centrConn = null;
        try{
            Class.forName("org.sqlite.JDBC").newInstance();
            centrConn = DriverManager.getConnection(this.urlDb);
            stm = centrConn.createStatement();
            
            
            result = stm.executeQuery(sqlSelectData+"=("+sqlSelect+" "+sqlLimit+")");
            ResultSetMetaData resultMeta = result.getMetaData();
            firstRecord = new Double[resultMeta.getColumnCount()];
            result.next();
            for(int i=1; i<=resultMeta.getColumnCount(); i++){
                firstRecord[i-1] = result.getDouble(i);
            }
            newCentroid = new Centroid(clusterId,hiers,firstRecord,true);
            
            sqlSelect = "SELECT id_cl FROM cluster WHERE id_cl != "+firstRecord[0].intValue()+" AND cluster_id = "+clusterId;
            result = stm.executeQuery(sqlSelectData+" IN ("+sqlSelect+")");
            resultMeta = result.getMetaData();
            
            centrConn.setAutoCommit(false);
            pstm = centrConn.prepareStatement(sqlUpdate);
            System.out.println("Id first: "+firstRecord[0].intValue());
            pstm.setDouble(1, 0.0);
            pstm.setInt(2, firstRecord[0].intValue());
            pstm.setInt(3, clusterId);
            pstm.executeUpdate();
            
            while(result.next()){
                Double[] record = new Double[resultMeta.getColumnCount()-1];
                for(int i=2; i<=resultMeta.getColumnCount(); i++){
                    record[i-2] = result.getDouble(i);
                }
                double distance = newCentroid.computeDistance(record,false);
                pstm.setDouble(1, distance);
                pstm.setInt(2, result.getInt(1));
                pstm.setInt(3, clusterId);
                pstm.executeUpdate();
                newCentroid.update(record,false);
            }
            centrConn.commit();
            
            
        }catch(Exception e){
            if(centrConn!=null)
                centrConn.rollback();
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" create new centroid");
        }finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(pstm!=null){
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(stmData!=null){
                try {
                    stmData.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(result!=null){
                try {
                    result.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                if(centrConn!=null){
                    centrConn.setAutoCommit(true);
                    centrConn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            return newCentroid;
        }
    }
    
    
    public Double[][][] getRecords(Integer[] clusters){
        String sqlSelect = "SELECT * FROM cluster";
        String sqlSelectDatabase = "SELECT * from dataset WHERE id=";
        String sqlSelectDatabaseMore = " OR id=";
        Double[][][] recordsClusters = new Double[clusters.length][][];
        ResultSet resultDatabase=null,resultCluster=null;
        Statement stm = null;
        Statement stm2 = null;
        int counterExpr;
        int recordsCounter;
        
        try{
            for(int l=0; l<clusters.length; l++){
                int cluster = clusters[l];
                boolean firstReputation = true;
                int numRecords = this.currentSizeCl.get(cluster);
                stm  = this.conn.createStatement();
                stm2 = this.conn.createStatement();
                resultCluster = stm.executeQuery(sqlSelect+cluster);
                counterExpr = 0;
                recordsCounter = 0;
                while(counterExpr < numRecords){
                    resultCluster.next();
                    sqlSelectDatabase += resultCluster.getInt(1);
                    counterExpr++;
                    while(resultCluster.next()){
                        sqlSelectDatabase += sqlSelectDatabaseMore+resultCluster.getInt(1);
                        counterExpr++;
                        if(counterExpr % 999 == 0){
                            break;
                        }
                    }
                    resultDatabase = stm2.executeQuery(sqlSelectDatabase);

                    if(firstReputation){
                        ResultSetMetaData resultMeta = resultDatabase.getMetaData();
                        recordsClusters[l] = new Double[numRecords][resultMeta.getColumnCount()];
                        firstReputation = false;
                    }

                    while(resultDatabase.next()){
                        for(int i=1; i<=recordsClusters[l][0].length; i++){
                            recordsClusters[l][recordsCounter][i-1] = resultDatabase.getDouble(i);
                        }
                        recordsCounter++;
                    }

                    sqlSelectDatabase = "SELECT * from dataset WHERE id=";
                }
            }
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" get Records from many clusters clusters ");
        } finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(stm2!=null){
                try {
                    stm2.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(resultDatabase!=null){
                try {
                    resultDatabase.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(resultCluster!=null){
                try {
                    resultCluster.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return recordsClusters;
    }
    
    public void setTempDb(String urlDbT){
        this.urlBbTemp = urlDbT;
    }
    
    public Double[][] getRecords(int cluster,boolean newConnection){
        String sqlSelect = "SELECT * FROM cluster WHERE cluster_id="+cluster;
        String sqlSelectDatabase = "SELECT * from dataset WHERE id=";
        String sqlSelectDatabaseMore = " OR id=";
        ResultSet resultDatabase=null,resultCluster=null;
        Statement stm = null;
        Statement stm2 = null;
        Double[][] records = null;
        int counterExpr = 0;
        int recordsCounter = 0;
        Connection newConn = null;
        
        try{
            int numRecords = this.currentSizeCl.get(cluster);
            if(newConnection){
                newConn = DriverManager.getConnection(this.urlBbTemp);
                stm = newConn.createStatement();
                stm2 = newConn.createStatement();
            }
            else{
                stm = this.conn.createStatement();
                stm2 = this.conn.createStatement();
            }
            resultCluster = stm.executeQuery(sqlSelect);
            
            while(counterExpr < numRecords){
                resultCluster.next();
                sqlSelectDatabase += resultCluster.getInt(1);
                counterExpr++;
                while(resultCluster.next()){
                    sqlSelectDatabase += sqlSelectDatabaseMore+resultCluster.getInt(1);
                    counterExpr++;
                    if(counterExpr % 999 == 0){
                        break;
                    }
                }
                resultDatabase = stm2.executeQuery(sqlSelectDatabase);
                
                if(records == null){
                    ResultSetMetaData resultMeta = resultDatabase.getMetaData();
                    records = new Double[numRecords][resultMeta.getColumnCount()];
                }

                while(resultDatabase.next()){
                    for(int i=1; i<=records[0].length; i++){
                        records[recordsCounter][i-1] = resultDatabase.getDouble(i);
                    }
                    recordsCounter++;
                }

                sqlSelectDatabase = "SELECT * from dataset WHERE id=";
            }
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: "+e.getMessage()+" get Records cluster "+cluster+" and records"+Arrays.toString(records[recordsCounter-1]));
        } finally{
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(stm2!=null){
                try {
                    stm2.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(resultDatabase!=null){
                try {
                    resultDatabase.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(resultCluster!=null){
                try {
                    resultCluster.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(newConn != null){
                try {
                    newConn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return records;
    }
    
    
    public void daleteClusterTable(){
        String sqlDelete = "DROP TABLE IF EXISTS cluster;";
        Statement stm = null;
        try{
            
            stm = this.conn.createStatement();
            stm.executeUpdate(sqlDelete);
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error : "+e.getMessage()+" delete table");
        }finally {
            if(stm!=null){
                try {
                    stm.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        this.currentSizeCl.clear();
    }
    
    public List<Integer> removeEmptyClusters(){
        List<Integer> emptyClusters = new ArrayList<Integer>();
        for(Entry<Integer,Integer> entryCluster : this.currentSizeCl.entrySet()){
            if(entryCluster.getValue() == 0){
                emptyClusters.add(entryCluster.getKey());
            }
        }
        
        for(Integer emptyCluster : emptyClusters){
            this.currentSizeCl.remove(emptyCluster);
        }
        
        return emptyClusters;
    }
    
    public List<Integer> getBigclusters(){
        List<Integer> bigClusters = new ArrayList<Integer>();
        int computedSize=0;
        
        for(Entry<Integer,Integer> entrySize : this.currentSizeCl.entrySet()){
            if(entrySize.getValue() > this.size){
                bigClusters.add(entrySize.getKey());
                computedSize += entrySize.getValue();
            }
        }
        
        
        
        System.out.println("Total Size big "+computedSize+" list size "+bigClusters.size());
        return bigClusters;
    }
    
    public List<Integer> getSmallClusters(){
        List<Integer> smallClusters = new ArrayList<Integer>();
        this.computedSmallSize=0;
        int computeOnes=0;
        
        for(Entry<Integer,Integer> entrySize : this.currentSizeCl.entrySet()){
            if(entrySize.getValue() < this.k){
                smallClusters.add(entrySize.getKey());
                this.computedSmallSize += entrySize.getValue();
            }
            
            if(entrySize.getValue() == 1){
                computeOnes++;
            }
        }
        
        
        
        System.out.println("Total Size small "+this.computedSmallSize+" list size "+smallClusters.size()+" ones "+computeOnes);
        return smallClusters;
    }
    
    public void removeSize(int clusterId){
        this.currentSizeCl.remove(clusterId);
    }
    
    
    public int totalSize(){
        int totalSize=0;
        for(Entry<Integer,Integer> entry : this.currentSizeCl.entrySet()){
            totalSize += entry.getValue();
        }
        return totalSize;
    }
    
    public int getSize(int clusterId){
        return this.currentSizeCl.get(clusterId);
    }
    
    public void setSize(int i, int s){
        this.currentSizeCl.put(i, s);
    }
    
    public int numOfClusters(){
        return this.currentSizeCl.size();
    }
    
    public void anonymizeCluster(int clusterId, Centroid centroidCluster, DiskData data,Double[][][] clusterRecords,Double[][] records,int i){
        Double [][] anonymizedRecords = null;
        try{
            
            anonymizedRecords = centroidCluster.anonymize(records);
            clusterRecords[i] = anonymizedRecords;
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: anonymize cluster "+clusterId+" "+e.getMessage());
        }
    }
}
