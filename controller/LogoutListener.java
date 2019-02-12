/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
//import org.apache.log4j.Logger;


/**
 *
 * @author jimakos
 */
//@Component
/*public class LogoutListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent e) {
        System.out.println("edwwwwwwwwwwwwwwwwww");
    }
    //public class LogoutListener extends HttpSessionEventPublisher {

}*/

public class LogoutListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        
        System.out.println("Total sessions started = " + event.getSession().getId() );
    }

    // other methods

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("Total sessions ended") ;
        try {
            CleanUserData (se);
        } catch (IOException ex) {
            Logger.getLogger(LogoutListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void CleanUserData (HttpSessionEvent event ) throws IOException  {
        String rootPath = (String)event.getSession().getAttribute("inputpath");

        if ( rootPath != null){
            //File file = new File(rootPath);
            delete(rootPath);
        }
        System.out.println("all cleannnnnnnn root = " + rootPath);
    }
    
    public static void delete(String file)
    	throws IOException{

    	/*if(file.isDirectory()){

    		//directory is empty, then delete it
    		if(file.list().length==0){

    		   file.delete();
    		   System.out.println("Directory is deleted : "
                                                 + file.getAbsolutePath());

    		}else{

    		   //list all the directory contents
        	   String files[] = file.list();

        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(file, temp);

        	      //recursive delete
        	     delete(fileDelete);
        	   }

        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	     System.out.println("Directory is deleted : "
                                                  + file.getAbsolutePath());
        	   }
    		}

    	}else{
    		//if file, then delete it
    		file.delete();
    		System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
    }*/
        
        System.out.println("file = " + file);
        Path dirPath = Paths.get( file );
        Files.walk( dirPath )
             .map( Path::toFile )
             .sorted( Comparator.comparing( File::isDirectory ) ) 
             .forEach( File::delete );
        
        File directory = new File(file);
        directory.delete();
    }
    
}
