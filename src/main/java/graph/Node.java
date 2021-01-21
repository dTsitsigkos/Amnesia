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
public class Node {
    private String label;
    private int level;
    private String id;
    private String color;
    private String title;
    
    
    public Node( String _id, String _label,int _level ){
        this.label = _label;
        this.level = _level;
        this.id = _id;
    }

    /*public Node( String _id, String _label,int _level, String color ){
        this.label = _label;
        this.level = _level;
        this.id = _id;
        this.color = color;
    }*/
    
    public Node( String _id, String _label,int _level, String _color, String _title ){
        this.label = _label;
        this.level = _level;
        this.id = _id;
        this.color = _color;
        this.title = _title;
    }
    
    
    public String getLabel() {
        return label;
    }
    
    public int getLevel() {
        return level;
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }
    
    

    public void setLabel(String label) {
        this.label = label;
    }

   
    public void setLevel(int level) {
        this.level = level;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    
    
    
}
