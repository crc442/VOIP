/*
						Version Controlling
=========================================================================================
		Name: - Queue.java
-----------------------------------------------------------------------------------------
	Version		Date		Comments
-----------------------------------------------------------------------------------------
	1.0		25-Mar-2009	  Initial Issued Version

=========================================================================================
*/
import java.util.*;

public class Queue {
    // Internal storage for the queue'd objects
    private Vector vec = new Vector();
    boolean prebuffer = true;
    
    synchronized public int numWaiting() {
    	// makes the client wait for more packets before starting to play them
    	// this takes care of the buffer
    	if( prebuffer && vec.size() < ( 1 * 12) ){
    		return 0;
    	}else{
    		prebuffer = false;
        	return vec.size();
    	}
    }
    
    synchronized public void put( Object o ) {
        // Add the element
        vec.addElement( o );
        // There might be threads waiting for the new object --
        notifyAll();
    }
    
    synchronized public Object get() {
        while (true) {
            if ( numWaiting() > 0 ) {
            	
            	// remove the bytes if its more then 1.5 seconds delay
            	while( vec.size() > (24) ){
            		vec.removeElementAt(0);
        		}
            	
                // There's an available object!
                Object o = vec.elementAt( 0 );
                
               vec.removeElementAt( 0 );
                
               
                if( vec.size() == 0 ){
                	prebuffer = true; // we have reached the last element in the stack we shuld buffer more data before playing
                }
                
                // Return the object
                return o;
                //return (Object)bigbyte;
            } else {
                // There aren't any objects available.  Do a wait(),
                // and when we wake up, check again to see if there
                // are any.
                try { wait(); } catch( InterruptedException ie ) {}
            }
        }
    }
}
