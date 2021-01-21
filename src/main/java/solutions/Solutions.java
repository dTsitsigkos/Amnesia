/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solutions;

import com.fasterxml.jackson.annotation.JsonView;
import jsoninterface.View;

/**
 *
 * @author jimakos
 */
public class Solutions {
    @JsonView(View.Solutions.class)
    String label = null;
    @JsonView(View.Solutions.class)
    String data = null;
    @JsonView(View.Solutions.class)
    String color = null;
    
    public Solutions( String _label, String _data, String _color){
        this.data = _data;
        this.label= _label;
        this.color = _color;
    }

    public String getLabel() {
        return label;
    }

    public String getData() {
        return data;
    }

    public String getColor() {
        return color;
    }

    
    

    public void setLabel(String label) {
        this.label = label;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setColor(String color) {
        this.color = color;
    }


    
}
