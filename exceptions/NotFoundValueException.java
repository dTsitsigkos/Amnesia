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
public class NotFoundValueException extends Exception {
    private String message;
    private static final long serialVersionUID = -7806029002430564887L;
    
    public NotFoundValueException(String msg) {
        super(msg);
        this.message = msg;
    }
    
    public String getErrorMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return  this.getErrorMessage();
    }
}
