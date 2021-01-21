/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

/**
 *
 * @author nikos
 */
public class LimitException extends Exception{
    private String message;
    private static final long serialVersionUID = -7806029002430564887L;
    private boolean clean;
     
    
    public LimitException(String msg) {
        super(msg);
        this.message = msg;
        this.clean = true;
    }
    
    public LimitException(String msg, boolean cl){
        super(msg);
        this.message = msg;
        this.clean = cl;
    }
    
    public String getErrorMessage() {
        return message;
    }
    
    public boolean isCleanSession(){
        return clean;
    }
    
    @Override
    public String toString() {
        return  this.getErrorMessage();
    }
}
