/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import com.fasterxml.jackson.annotation.JsonView;
import data.Data;
import data.DiskData;
import jsoninterface.View;

/**
 *
 * @author jimakos
 */
public class DatasetsExistence {
//    @JsonView(View.DatasetsExists.class)
    private String anonExists = null;
   // @JsonView(View.DatasetsExists.class)
    private String originalExists = null;
    
    private String algorithm = null;
    
    private String isDiskData = null;
    
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
    
    public String getDiskData(){
        return this.isDiskData;
    }

    public String getOriginalExists() {
        return originalExists;
    }
    
    public String getAlgorithm(){
        return this.algorithm;
    }

    public void setOriginalExists(String originalExists) {
        this.originalExists = originalExists;
    }
    
    public void setAlgorithm(String algo){
        this.algorithm = algo;
    }
    
    public void setDiskData(Data data){
        this.isDiskData = data instanceof DiskData ? "true" : "false";
    }

    
}
