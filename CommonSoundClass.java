/*
						Version Controlling
=========================================================================================
		Name: - CommonSoundClass.java
-----------------------------------------------------------------------------------------
	Version		Date		Comments
-----------------------------------------------------------------------------------------
	1.0		25-Mar-2009	  Initial Issued Version

=========================================================================================
*/
import java.util.*;
import java.awt.*;


public class CommonSoundClass
{
   
 public Vector vec = new Vector();
 boolean lock = true;
 private byte b[];
 
 
  public CommonSoundClass(){}
  
   synchronized public Object readbyte(){

        try{
        while( vec.isEmpty() ){
            wait();
        }
        }
        catch( InterruptedException ie ) { 
        	System.err.println(" your out of luck my friend :) ");
        }
        
        if( ! vec.isEmpty() ){
        	b = (byte[]) vec.elementAt( 0 );
        	vec.removeElementAt(0);
        	return b;
    	}else{
    		byte[] b = new byte[5];
    		return b;
    	}
        
    }
    
    synchronized public void writebyte( Object e ){
        
        vec.addElement( e );
        
        lock = false;
        notifyAll();
    
    }

  }



 