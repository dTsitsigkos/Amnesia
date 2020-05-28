/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clusterbased;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
//    private Map<Integer,List<Integer>> deleteQueries;
    private int[][] deleteQueries;
    private int[] deleteClusterPainter;
//    private Map<Integer,Set<Pair<Integer,Double>>> insertQueries;
    private Pair<Integer,Double>[][] insertQueries;
    int[] insertClusterPointer;
    private int counterInserts;
    private int counterDeletions;
    private int anonymizedPointer;
    
    public Clusters(String urlDatabase, int maxclusters, int k){
        this.urlDb = urlDatabase;
        this.lastClusterId = 0;
        this.counterDeletions = 0;
        anonymizedPointer = 0;
        this.maxClusters = maxclusters;
        this.size = 2*k-1;
        this.currentSizeCl = new HashMap();
        this.k = k;
        this.insertQueries = new Pair[2*maxClusters+1][this.size+1];
        this.insertClusterPointer = new int[2*maxClusters+1];
        this.deleteQueries = new int [2*maxClusters+1][this.size+1];
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
        String sqlCreate = "";/*CREATE TABLE cluster"+clusterId+" (id_cl integer PRIMARY KEY, distance real,  FOREIGN KEY (id_cl) REFERENCES dataset (id))";*/
//        String sqlInsert = "INSERT INTO cluster"+clusterId+"(id_cl,distance) VALUES(?,?)";
        Statement stm = null;
        PreparedStatement pstm = null;
        
        try{
            conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
//            for(Integer clusterId : clusterids){
//                sqlCreate = "CREATE TABLE cluster"+clusterId+" (id_cl integer PRIMARY KEY, distance real,  FOREIGN KEY (id_cl) REFERENCES dataset (id))";
//                stm.executeUpdate("drop table if exists cluster"+clusterId+";");
//                stm.execute(sqlCreate);
//                this.lastClusterId = clusterId;
//            }
            
//            sqlCreate = "CREATE TABLE cluster"+clusterids.get(0)+" (id_cl integer PRIMARY KEY, distance real,  FOREIGN KEY (id_cl) REFERENCES dataset (id))";
            sqlCreate = "CREATE TABLE cluster (id_cl integer PRIMARY KEY, cluster_id integer , distance real,  FOREIGN KEY (id_cl) REFERENCES dataset (id))";
            stm.execute(sqlCreate);
            
//            for(int i=1; i<clusterids.size(); i++){
//                sqlCreate = "CREATE TABLE cluster"+clusterids.get(i)+" AS SELECT * FROM cluster1";
//                stm.execute(sqlCreate);
//            }
            
            
            conn.commit();
            
            
//            for(Integer r : records){
//                this.put(clusterId, r, 0.0, null);
////                pstm = this.conn.prepareStatement(sqlInsert);
////                pstm.setInt(1, r);
////                pstm.setDouble(2, 0.0);
////                pstm.executeUpdate();
//            }
//            this.currentSizeCl.put(clusterId, records.size());
            
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
        return Collections.max(this.currentSizeCl.keySet());
    }
    
    public void remove(int clusterId, int recordId){
//        String sqlDelete = "DELETE FROM cluster"+clusterId+" WHERE id_cl = "+recordId;
//        Statement stm = null;
        try{
//            stm = this.conn.createStatement();
//            stm.execute(sqlDelete);
            
            if(this.deleteQueries[clusterId][0]!=0){
                this.deleteQueries[clusterId][this.deleteClusterPainter[clusterId]] = recordId;
                this.deleteClusterPainter[clusterId]++;
            }
            else{
                this.deleteQueries[clusterId][0] = recordId;
                this.deleteClusterPainter[clusterId] = 1;
            }
            
            this.counterDeletions++;
            
            this.currentSizeCl.put(clusterId, this.currentSizeCl.get(clusterId)-1);
            
            
//            if(this.counterDeletions >= 8000){
//                this.executeDeleteBatch();
//                
//            }

            
        }catch(Exception e){
            System.err.println("Error: "+e.getMessage()+" remove from cluster");
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
           
            
            if(this.deleteQueries[clusterId][0]!=0){
                String sqlDelete = "DELETE FROM cluster";
                for(int j=0; j<this.deleteQueries[clusterId].length; j++){
                    if(this.deleteQueries[clusterId][j]!=0){
                        stm.executeUpdate(sqlDelete+" WHERE id_cl = "+this.deleteQueries[clusterId][j]+" AND cluster_id = "+clusterId);
                        counterDelete++;
                        executeDelete = true;
                    }
                    else{
                        break;
                    }
                }
            }
            
            
//            for(Entry<Integer,List<Integer>> entry : this.deleteQueries.entrySet()){
//                String sqlDelete = "DELETE FROM cluster"+entry.getKey();
//                for(Integer record : entry.getValue()){
//                    stm.executeUpdate(sqlDelete+" WHERE id_cl = "+record);
//                }
//            }
            
            if(executeDelete){
                this.conn.commit();
                this.deleteQueries[clusterId] = new int [this.size+1];
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
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
            for(int i=0; i<=this.lastClusterId; i++){
                if(this.deleteQueries[i][0]!=0){
                    String sqlDelete = "DELETE FROM cluster";
                    for(int j=0; j<this.deleteQueries[i].length; j++){
                        if(this.deleteQueries[i][j]!=0){
                            stm.executeUpdate(sqlDelete+" WHERE id_cl = "+this.deleteQueries[i][j]+" AND cluster_id = "+i);
                        }
                        else{
                            break;
                        }
                    }
                }
            }
            
//            for(Entry<Integer,List<Integer>> entry : this.deleteQueries.entrySet()){
//                String sqlDelete = "DELETE FROM cluster"+entry.getKey();
//                for(Integer record : entry.getValue()){
//                    stm.executeUpdate(sqlDelete+" WHERE id_cl = "+record);
//                }
//            }
            
            if(this.counterDeletions!=0){
                this.conn.commit();
                this.deleteQueries = new int [2*maxClusters+1][this.size+1];
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
//        String sqlInsert = "INSERT INTO cluster"+clusterId+"(id_cl,distance) VALUES("+recordId+","+distance+")";
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
            
            int counterUpdate = 0;
            if(this.insertQueries[clusterId][0]!=null){
                String sqlInsert = "INSERT INTO cluster(id_cl,cluster_id,distance) VALUES(";
                for(int j=0; j<this.insertQueries[clusterId].length; j++){
                    if(insertQueries[clusterId][j]!=null){
                        errMessage = sqlInsert+insertQueries[clusterId][j].getKey()+","+insertQueries[clusterId][j].getValue()+")";
                        stm.executeUpdate(sqlInsert+insertQueries[clusterId][j].getKey()+","+clusterId+","+insertQueries[clusterId][j].getValue()+")");
                        counterUpdate++;
                        executeUpdate=true;
                    }
                    else{
                        break;
                    }
                }
            }
            
            
//            for(Entry<Integer,Set<Pair<Integer,Double>>> entry : this.insertQueries.entrySet()){
//                counter++;
//                if(entry == null)
//                    System.out.println(counter+" SQl insert ");
//                String sqlInsert = "INSERT INTO cluster"+entry.getKey()+"(id_cl,distance) VALUES(";
////                stm.executeUpdate(insertQuery);
//                for(Pair<Integer,Double> recDist : entry.getValue()){
////                    System.out.println("Batch "+sqlInsert+recDist.getKey()+","+recDist.getValue()+")");
//                    errMessage = sqlInsert+recDist.getKey()+","+recDist.getValue()+")";
//                    stm.executeUpdate(sqlInsert+recDist.getKey()+","+recDist.getValue()+")");
//                }
//            }
            if(executeUpdate){
                this.conn.commit();
                this.insertQueries[clusterId] = new Pair[this.size+1];;
                counterInserts -= counterUpdate;
            }
//            if(!insertQueries.isEmpty()){
//                this.conn.commit();
//                this.insertQueries.clear();
//                counterInserts = 0;
//            }
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
    
    public void executeBatch(){
        this.executeDeleteBatch();
        System.out.println("Execute batch");
        Statement stm = null;
        int counter = 0;
        String errMessage="";
        boolean executeUpdate=false;
//        String sqlInsert = "INSERT INTO cluster"+clusterId+"(id_cl,distance) VALUES("+recordId+","+distance+")";
        try{
            this.conn.setAutoCommit(false);
            stm = this.conn.createStatement();
            
            
            for(int i=0; i<=this.lastClusterId; i++){
                if(this.insertQueries[i][0]!=null){
                    String sqlInsert = "INSERT INTO cluster(id_cl,cluster_id,distance) VALUES(";
                    for(int j=0; j<this.insertQueries[i].length; j++){
                        if(insertQueries[i][j]!=null){
                            errMessage = sqlInsert+insertQueries[i][j].getKey()+","+insertQueries[i][j].getValue()+")";
                            stm.executeUpdate(sqlInsert+insertQueries[i][j].getKey()+","+i+","+insertQueries[i][j].getValue()+")");
                            executeUpdate=true;
                        }
                        else{
                            break;
                        }
                    }
                }
            }
            
//            for(Entry<Integer,Set<Pair<Integer,Double>>> entry : this.insertQueries.entrySet()){
//                counter++;
//                if(entry == null)
//                    System.out.println(counter+" SQl insert ");
//                String sqlInsert = "INSERT INTO cluster"+entry.getKey()+"(id_cl,distance) VALUES(";
////                stm.executeUpdate(insertQuery);
//                for(Pair<Integer,Double> recDist : entry.getValue()){
////                    System.out.println("Batch "+sqlInsert+recDist.getKey()+","+recDist.getValue()+")");
//                    errMessage = sqlInsert+recDist.getKey()+","+recDist.getValue()+")";
//                    stm.executeUpdate(sqlInsert+recDist.getKey()+","+recDist.getValue()+")");
//                }
//            }
            if(executeUpdate){
                this.conn.commit();
                this.insertQueries = new Pair[2*maxClusters+1][this.size+1];;
                counterInserts = 0;
            }
//            if(!insertQueries.isEmpty()){
//                this.conn.commit();
//                this.insertQueries.clear();
//                counterInserts = 0;
//            }
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
    
    
    
    public int put(int clusterId, int recordId, double distance,Semaphore split){
//        String sqlInsert = "INSERT INTO cluster"+clusterId+"(id_cl,distance) VALUES("+recordId+","+distance+")";
        int newCluster = -1;
        try{
//            System.out.println("sql insert "+sqlInsert);
//            pstm = this.conn.prepareStatement(sqlInsert);
//            pstm.setInt(1, recordId);
//            pstm.setDouble(2, distance);
//            pstm.executeUpdate();
            if(split!=null){
                if(split.tryAcquire()){
                    split.release();
//                    this.insertQueries.add(sqlInsert);
                    if(this.insertQueries[clusterId][0]==null){
//                        Set<Pair<Integer,Double>> list = new HashSet<Pair<Integer,Double>>();
//                        list.add(new Pair(recordId,distance));
                        Pair<Integer,Double> temp = new Pair(recordId,distance);
                        this.insertQueries[clusterId][0] = temp;
                        this.insertClusterPointer[clusterId] = 1;
                    }
                    else{
                        this.insertQueries[clusterId][this.insertClusterPointer[clusterId]] = new Pair(recordId,distance);
                        this.insertClusterPointer[clusterId]++;
                    }
                }
                else{
                    split.acquire();
                    if(this.insertQueries[clusterId][0]==null){
//                        Set<Pair<Integer,Double>> list = new HashSet<Pair<Integer,Double>>();
                       this.insertQueries[clusterId][0] = new Pair(recordId,distance);
                       this.insertClusterPointer[clusterId] = 1;
                    }
                    else{
                        this.insertQueries[clusterId][this.insertClusterPointer[clusterId]] = new Pair(recordId,distance);
                        this.insertClusterPointer[clusterId]++;
                    }
                    split.release();
                }
            }
            else{
                if(this.insertQueries[clusterId][0]==null){
//                    Set<Pair<Integer,Double>> list = new HashSet<Pair<Integer,Double>>();
//                    list.add(new Pair(recordId,distance));
//                    this.insertQueries.put(clusterId, list);
                    this.insertQueries[clusterId][0] = new Pair(recordId,distance);
                    this.insertClusterPointer[clusterId] = 1;
                }
                else{
//                    this.insertQueries.get(clusterId).add(new Pair(recordId,distance));
                    this.insertQueries[clusterId][this.insertClusterPointer[clusterId]] = new Pair(recordId,distance);
                    this.insertClusterPointer[clusterId]++;
                }     
            }
            counterInserts++;
            if(this.currentSizeCl.get(clusterId)!=null){
                this.currentSizeCl.put(clusterId, this.currentSizeCl.get(clusterId)+1);

    //            if(sqlInsert==null){
    //                System.out.println("NULL cID "+clusterId+" recID "+recordId+" distance "+distance);
    //            }

                if(this.currentSizeCl.get(clusterId) == size+1){
                    if(split != null){
                        split.acquire();   
                    }
                    this.executeBatch(clusterId);
                    newCluster = split(clusterId);

                    if(split!=null)
                        split.release();

                }
                else if(counterInserts >= 8000){
                    if(split != null){
                        split.acquire();   
                    }
                    this.executeBatch();


                    if(split!=null)
                        split.release();
                }
            }
            else if(counterInserts >= 8000){
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
    
//    public void deleteTable(int clusterId){
//        String sqlDelete = "DROP TABLE IF EXISTS cluster;";
//        Statement stm = null;
//        try{
//            
//            stm = this.conn.createStatement();
//            stm.executeUpdate(sqlDelete);
//            
////            this.currentSizeCl.remove(clusterId);
//        }catch(Exception e){
//            e.printStackTrace();
//            System.err.println("Error : "+e.getMessage()+" delete table");
//        }finally {
//            if(stm!=null){
//                try {
//                    stm.close();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//        
//        
//    }
    
    private List<Integer> newSplit(int clusterId){
        return null;
    }
    
    private int split(int clusterId){
        System.out.println("Split "+clusterId+" "+(this.lastClusterId+1));
        
        int newCluster = this.lastClusterId +1;
        this.lastClusterId++;
//        String sqlCreate = "CREATE TABLE cluster"+(++this.lastClusterId)+" (id_cl integer PRIMARY KEY, distance real,  FOREIGN KEY (id_cl) REFERENCES dataset (id))";
        String sqlInsert = "INSERT INTO cluster (id_cl,cluster_id,distance) VALUES(?,?,?)";
        String sqlSelect = "SELECT * FROM cluster WHERE cluster_id="+clusterId+" ORDER BY distance DESC LIMIT "+this.k;
        String sqlDelete = "DELETE FROM cluster WHERE cluster_id="+clusterId+" AND id_cl = ?";
        Statement stm = null;
        PreparedStatement pstm = null,pstmDel = null;
        
        try{
            stm = this.conn.createStatement();
            
            
//            System.out.println("Select "+sqlSelect +" insert "+sqlInsert +" delete "+sqlDelete);
            

//            stm.executeUpdate("drop table if exists cluster"+this.lastClusterId+";");
//            stm.execute(sqlCreate);
            
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
                
//                System.out.println("Insert "+rs.getInt(1)+" "+rs.getDouble(2));
//                System.out.println("Delete "+rs.getInt(1));
                
                
                
                
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
//        String[] datesVals  = null;
//        int countDatesVal=0;
        
        try{
            Class.forName("org.sqlite.JDBC").newInstance();
            centrConn = DriverManager.getConnection(this.urlDb);
            stm = centrConn.createStatement();
            
//            for(Entry<Integer,Hierarchy> entryH : hiers.entrySet()){
//                if(entryH.getValue().getNodesType().equals("date")){
//                    countDatesVal++;
//                }
//            }

//            if(countDatesVal!=0){
//                datesVals = new String[countDatesVal];
//                countDatesVal=0;
//            }
            
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
//            System.out.println("Distance "+result.getString(2));
            pstm.setDouble(1, 0.0);
            pstm.setInt(2, firstRecord[0].intValue());
            pstm.setInt(3, clusterId);
            pstm.executeUpdate();
            
            while(result.next()){
                Double[] record = new Double[resultMeta.getColumnCount()-1];
                for(int i=2; i<=resultMeta.getColumnCount(); i++){
                    record[i-2] = result.getDouble(i);
//                    if(hiers.containsKey(i-2) && hiers.get(i-2).getNodesType().equals("date")){
//                        datesVals[countDatesVal] = hiers.get(i-2).getDictionary().getIdToString().get(record[i-2].intValue());
//                        countDatesVal++;
//                    }
                }
//                countDatesVal=0;
                double distance = newCentroid.computeDistance(record,false);
//                System.out.println("Id other: "+result.getInt(1)+" distrance "+distance);
                pstm.setDouble(1, distance);
                pstm.setInt(2, result.getInt(1));
                pstm.setInt(3, clusterId);
                pstm.executeUpdate();
                newCentroid.update(record,false);
            }
            centrConn.commit();
            
//            while(result.next()){
//                String sqlSelectDataTemp = sqlSelectData+result.getInt(1);
//                
//                stmData = this.conn.createStatement();
//                ResultSet resultData = stmData.executeQuery(sqlSelectDataTemp);
//                ResultSetMetaData metaData = resultData.getMetaData();
//                
//                if(resultData.next()){
//                    Double[] record = new Double[metaData.getColumnCount()-1];
//                    for(int i=2; i<=metaData.getColumnCount(); i++){
//                        record[i-2] = resultData.getDouble(i);
//                    }
//                    
//                    if(firstrecord){
//                        newCentroid = new Centroid(clusterId,hiers,record,false);
//                        System.out.println("Id first: "+result.getInt(1));
//                        System.out.println("Distance "+result.getString(2));
//                        if(result.getDouble(2) != 0.0){
//                            System.out.println("Mpike "+result.getString(2));
//                            pstm = conn.prepareStatement(sqlUpdate);
//                            pstm.setDouble(1, 0.0);
//                            pstm.setInt(2, result.getInt(1));
//                            pstm.executeUpdate();
//                        }
//                        firstrecord = false;
//                    }
//                    else{
//                        double distance = newCentroid.computeDistance(record,false);
//                        pstm = conn.prepareStatement(sqlUpdate);
//                        pstm.setDouble(1, distance);
//                        pstm.setInt(2, result.getInt(1));
//                        System.out.println("Id other: "+result.getInt(1)+" distrance "+distance);
//                        pstm.executeUpdate();
//                        newCentroid.update(record,false,s);
//                    }
//                    
//                }
//                
//                resultData.close();
//                
//            }
//            result.close();
            
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
//        Double[][] records = null;
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

        //            result.close();

    //                System.out.println("SQL SEl: "+sqlSelectDatabase);
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

    //                resultDatabase.close();
                    sqlSelectDatabase = "SELECT * from dataset WHERE id=";
                }
                
//                recordsClusters[l] = records;
//                records = null;
                
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
//        String sqlCount = "SELECT COUNT(*) FROM cluster"+cluster;
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

    //            result.close();

//                System.out.println("SQL SEl: "+sqlSelectDatabase);
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

//                resultDatabase.close();
                sqlSelectDatabase = "SELECT * from dataset WHERE id=";
            }
//            System.out.println("Cluster "+cluster+" num records "+numRecords+" counter "+recordsCounter);
            
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
    
//    public Map<Integer,Double[][]> removeAnonymized(){
//        Map<Integer,Double[][]> anonymizedClusters = new HashMap<Integer,Double[][]>();
//        
//        try{
//            for(Entry<Integer,Integer> entrySize : this.currentSizeCl.entrySet()){
//                if(entrySize.getValue() >= this.k){
//                    Double[][] records = this.getRecords(entrySize.getKey(),false);
//                    anonymizedClusters.put(entrySize.getKey(), records);
//                    this.deleteTable(entrySize.getKey());
//                }
//
//            }
//            
//            for(Integer cluster : anonymizedClusters.keySet()){
//                this.currentSizeCl.remove(cluster);
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//            System.err.println("Error: "+e.getMessage()+" remove anonymized data");
//        }
//        
//        return anonymizedClusters;
//        
//    }
    
    public void daleteClusterTable(){
        String sqlDelete = "DROP TABLE IF EXISTS cluster;";
        Statement stm = null;
        try{
            
            stm = this.conn.createStatement();
            stm.executeUpdate(sqlDelete);
            
//            this.currentSizeCl.remove(clusterId);
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
//            System.out.println("Before Cluster "+entryCluster.getKey()+" size "+entryCluster.getValue());
            if(entryCluster.getValue() == 0){
//                System.out.println("Cluster "+entryCluster.getKey()+" size "+entryCluster.getValue());
                emptyClusters.add(entryCluster.getKey());
//                this.deleteTable(entryCluster.getKey());
            }
        }
        
        for(Integer emptyCluster : emptyClusters){
            this.currentSizeCl.remove(emptyCluster);
        }
        
        return emptyClusters;
    }
    
   
    
    public List<Integer> getSmallClusters(){
//        String sqlCount = "SELECT COUNT(*) FROM cluster";
//        Statement stm = null;
//        List<Integer> smallClusters = new ArrayList<Integer>();
//        int computedSize=0;
//        
//        try{
//            stm = this.conn.createStatement();
//            for(int i=1; i<=this.numOfClusters; i++){
//                ResultSet count = stm.executeQuery(sqlCount+i);
//                count.next();
//                int size = count.getInt(1);
//                if(size < this.k){
//                    smallClusters.add(i);
//                    computedSize += size;
//                }
//            }
//        }catch(Exception e){
//           System.err.println("Error: "+e.getMessage()+" get clusters with size < k");
//        }finally{
//            if(stm!=null){
//                try {
//                    stm.close();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
        
        List<Integer> smallClusters = new ArrayList<Integer>();
        int computedSize=0;
        
        for(Entry<Integer,Integer> entrySize : this.currentSizeCl.entrySet()){
            if(entrySize.getValue() < this.k){
                smallClusters.add(entrySize.getKey());
                computedSize += entrySize.getValue();
            }
        }
        
        
        
        System.out.println("Total Size small "+computedSize+" list size "+smallClusters.size());
        return smallClusters;
    }
    
    public void removeSize(int clusterId){
        this.currentSizeCl.remove(clusterId);
    }
    
//    public boolean tableExists(int clusterId){
//        ResultSet rs = null;
//        try{
//            DatabaseMetaData md = conn.getMetaData();
//            rs = md.getTables(null, null, "cluster"+clusterId, null);
//            rs.last();
//            boolean result =  rs.getRow() > 0;
//            rs.close();
//            return result;
//        }catch(SQLException ex){
//            ex.printStackTrace();
//            System.err.println("Error: "+ex.getMessage()+" check table existance");
//            Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex);
//            
//            if(rs!=null){
//                try {
//                    rs.close();
//                } catch (SQLException ex1) {
//                    ex1.printStackTrace();
//                    Logger.getLogger(Clusters.class.getName()).log(Level.SEVERE, null, ex1);
//                }
//            }
//        }
//        return false;
//    }
    
    public int totalSize(){
        int totalSize=0;
        for(Entry<Integer,Integer> entry : this.currentSizeCl.entrySet()){
//            System.out.println("Cluster "+entry.getKey()+" size "+entry.getValue());
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
    
    public void anonymizeCluster(int clusterId, Centroid centroidCluster, DiskData data,Double[][][] clusterRecords,int i){
        Double [][] records = null;
        Double [][] anonymizedRecords = null;
        try{
            
//            synchronized(this.conn){
                records = this.getRecords(clusterId,true);
//            }
//            records = clusterRecords[i];
            
            anonymizedRecords = centroidCluster.anonymize(records);
            
//            synchronized(data){
//                data.fillAnonymizedRecords(anonymizedRecords);
//            }
            clusterRecords[i] = anonymizedRecords;
//            this.anonymizedPointer++;
            
//            clusterRecords[i] = centroidCluster.anonymize(clusterRecords[i]);
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: anonymize cluster "+clusterId+" "+e.getMessage());
        }
    }
    
    public void setAnonymizedPointer(int p){
        this.anonymizedPointer = p;
    }
}
