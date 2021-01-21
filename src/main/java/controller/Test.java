/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author jimakos
 */
public class Test {
    String str ="xaxa";
    @JsonIgnore
    String x1 = "lsls";
    
    public Test(){
    }

    public String getStr() {
        return str;
    }

    public String getX1() {
        return x1;
    }
    
    

    public void setStr(String str) {
        this.str = str;
    }
    
    
}
