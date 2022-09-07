/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import jsoninterface.View;

/**
 *
 * @author jimakos
 */
public class Graph {
    ArrayList<Node> nodeList = null;
    ArrayList<Edge> edgeList = null;
    @JsonView(View.Graph.class)
    String type = null;
    
    
    public Graph(){
        nodeList = new ArrayList<>();
        edgeList = new ArrayList<>();
        type = "";
    }
    
    public Graph(String _type){
        nodeList = new ArrayList<>();
        edgeList = new ArrayList<>();
        type = _type;
    }

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }
    
    
    public void setNode( Node n){
        nodeList.add(n);
    }
    
    
    public ArrayList<Edge> getEdgeList() {
        return edgeList;
    }
    
    
    public void setEdge(Edge e){
        edgeList.add(e);
    }
    
    public void setNodeList(ArrayList<Node> list){
        nodeList = list;
    }
    
    public void setEdgeList(ArrayList<Edge> list){
        edgeList = list;
    }
    
    public void print(){
        int i = 0 ;
        for ( i = 0 ; i < nodeList.size() ; i ++){
            System.out.println(nodeList.get(i).getLabel());
        }
    }
}
