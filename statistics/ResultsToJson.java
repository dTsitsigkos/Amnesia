/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

/**
 *
 * @author jimakos
 */
public class ResultsToJson {
    private String y = null;
    private String a = null;
    
    public ResultsToJson(String _y, String _a){
        y = _y;
        a = _a;
    }

    
    public String getY() {
        return y;
    }

    public String getA() {
        return a;
    }

    public void setY(String y) {
        this.y = y;
    }

    public void setA(String a) {
        this.a = a;
    }
    
    
    
    
}
