/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

/**
 *
 * @author jimakos
 */
public class Edge {
    //private int id ;
    private String from;
    private String to;
    
    
    
    public Edge(String _from, String _to){
        this.from = _from;
        this.to = _to;
        //this.id = _id;
    }

    /*public int getId() {
        return id;
    }*/
    
    public String getFrom() {
        return from;
    }

    
    public String getTo() {
        return to;
    }

    

}
