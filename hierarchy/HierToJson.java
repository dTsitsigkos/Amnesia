/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy;

/**
 *
 * @author jimakos
 */
public class HierToJson {
    private String id = null;
    private String text = null;
    private String sort = null;
    private String type = null;
    
    
    public HierToJson(String _id , String _text, String _sort, String _type){
        this.id = _id;
        this.text = _text;
        this.sort = _sort;
        this.type = _type;
    }
    
    public HierToJson(String _id , String _text, String _type){
        this.id = _id;
        this.text = _text;
        this.type = _type;
    }
    
    public HierToJson(String _id , String _text){
        this.id = _id;
        this.text = _text;
    }

    
    public String getId() {
        return id;
    }

    
    public String getText() {
        return text;
    }

    public String getSort() {
        return sort;
    }

    public String getType() {
        return type;
    }
    
    

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
    
}
