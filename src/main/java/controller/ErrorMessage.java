/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

/**
 *
 * @author jimakos
 */
public class ErrorMessage {
    private boolean success = false;
    private String problem = null;
    
    public ErrorMessage(){
    
    }

    public boolean isSuccess() {
        return success;
    }

    public String getProblem() {
        return problem;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    
    
    
    
}
