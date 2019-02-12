/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import com.fasterxml.jackson.annotation.JsonView;
import jsoninterface.View;

/**
 *
 * @author jimakos
 */
public class DatasetsExistence {
    //@JsonView(View.DatasetsExists.class)
    private String anonExists = null;
   // @JsonView(View.DatasetsExists.class)
    private String originalExists = null;
    
    public DatasetsExistence(){
    
    }
    
    public DatasetsExistence(String _anonExists, String _originalExists){
        anonExists = _anonExists;
        originalExists = _originalExists;
    }

    public String getAnonExists() {
        return anonExists;
    }

    public void setAnonExists(String anonExists) {
        this.anonExists = anonExists;
    }

    public String getOriginalExists() {
        return originalExists;
    }

    public void setOriginalExists(String originalExists) {
        this.originalExists = originalExists;
    }

    
}
