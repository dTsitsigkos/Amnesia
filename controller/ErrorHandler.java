/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import exceptions.DateParseException;
import exceptions.ErrorResponse;
import exceptions.LimitException;
import exceptions.NotFoundValueException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author nikos
 */
@ControllerAdvice
public class ErrorHandler {
    
    @ExceptionHandler(NotFoundValueException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse processValidationError(NotFoundValueException ex) {
        ErrorResponse rs = new ErrorResponse(ex.getMessage());
        return rs;
    }
    
    
    @ExceptionHandler(LimitException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse onlineVersionLimits(LimitException ex){
        ErrorResponse rs = new ErrorResponse(ex.getMessage());
        if(ex.isCleanSession()){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            HttpSession session = request.getSession(true);
            AppController.restart(session);
        }
        return rs;
    }
    
    @ExceptionHandler(DateParseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse multipleDateFormats(DateParseException ex){
        ErrorResponse rs = new ErrorResponse(ex.getMessage());
        if(ex.isCleanSession()){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            HttpSession session = request.getSession(true);
            AppController.restart(session);
        }
        return rs;
    }
}
