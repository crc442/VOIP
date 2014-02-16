/*
						Version Controlling
=========================================================================================
		Name: - Handler.java
-----------------------------------------------------------------------------------------
	Version		Date		Comments
-----------------------------------------------------------------------------------------
	1.0		25-Mar-2009	  Initial Issued Version

=========================================================================================
*/
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

//import java.sql.*;

public class Handler extends Thread {
    static Vector handlers = new Vector( 10 ); // vector that holds the thread
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    boolean alreadyclosed = false;
    final int bytesize = 1024+1;//1024*8+10; // the maximum size of each packet in bytes
    
    String nick = "";
    boolean IAmTalking = false;
    boolean IAmAdmin = false;
    boolean IAmMute = false;
    int ImTalkingCounter = 0;
    
    sendout s = null;

	byte[] breaker = new byte[5];  
	
	boolean keepGoing = false;
    
    //wordPlace wp = new wordPlace(
    public Handler(){}
    public Handler(Socket socket) throws IOException {
        try{
            this.socket = socket;
        }catch( Exception exp ){ System.out.println( "someone connecting with ftp?"); }
    }
    
    
    CommonSoundClass cs = new CommonSoundClass();
    
    
    boolean lastpacketrecieved = true;;
    
    public class pingClass extends Thread{
    	
    	
    	Handler ptrtoThis = null;
    	public pingClass( Handler ptrtoThis ){
    		this.ptrtoThis = ptrtoThis;
    	}
    	
    	public void run(){
    		
    		while( keepGoing ){
				ptrtoThis.cs.writebyte( ("PT$" + "KAERB" ).getBytes() );
				try{
					synchronized( this ) {
						wait(1000);
					}
				} catch( Exception mexp  ) {mexp.printStackTrace();}		
    		}
    			
    	}	
    }
    
    public class sendout extends Thread{ 
    
    	Handler fr;
        public sendout( Handler fr ){
            this.fr = fr;
        }
        
        public void stopit(){
        	keepGoing = false;
        }
        
	    public void run(){ 
			try{
				while( keepGoing ){
					if( true /*lastpacketrecieved*/ ){
						lastpacketrecieved = false;
						byte[] b = (byte[])cs.readbyte();
						//synchronized( handlers ){
								fr.out.write( b );
				            	fr.out.flush();
		            	//}
	            	}else{
	            		try{
	            			synchronized( this ) {
                            	wait(10);
                        	}
                        } catch( Exception mexp  ) {mexp.printStackTrace();}	
	            	}
				}
			}catch( Exception exp ){
				//exp.printStackTrace();
				System.out.println("nothing to worry about");
			}finally {
            try {
                synchronized(handlers) {
                    for( int i = 0; i < handlers.size(); i++){
                        Handler tmp = (Handler)(handlers.elementAt(i));
                        if( tmp != fr ){
                            // tell others that this user logged off
                            tmp.cs.writebyte( ("NC"+ fr.nick + "KAERB").getBytes()  );
                            //tmp.out.write( ("NC"+ this.nick).getBytes() );
	            			//tmp.out.flush();
                            
                        }
                    }
                    if( IAmTalking ){ // if i'm talking
                        for( int i = 0; i < handlers.size(); i++){
                            Handler tmp = (Handler)(handlers.elementAt(i));
                            if( tmp != fr ){
                                //tell others that i'm no longer talking
                                tmp.cs.writebyte( ("NT"+ fr.nick + "KAERB").getBytes() );
                                //tmp.out.write( ("NT"+ this.nick).getBytes() );
	            				//tmp.out.flush();
                            }
                        }
                    }
                    IAmTalking = false;
                    keepGoing = false;
                    if( s != null ){
                    	fr.s.stopit(); // stop the thread from sending any more data to me i'm already logged off
                	}
                    
                    if( ! alreadyclosed ){
                        alreadyclosed = true;
                        in.close();
                        out.close();
                        socket.close();
                    }
                }
            } catch(IOException ioe) {
            } finally {
                synchronized(handlers) {
                    if( fr.nick != null && fr.nick != ""){
                        System.out.println( fr.nick + " signed off");
                    }
                    keepGoing = false;
                    handlers.removeElement(fr);
                    
                }
            }
        }
	    }
    }
    
    public class timeout extends Thread{
        
        Handler fr;
        public boolean shuldclose = true;
        public timeout( Handler fr ){
            this.fr = fr;
        }
        
        public void run(){
            try{
                sleep(9000);
                if( shuldclose && !alreadyclosed ){
                    alreadyclosed = true;
                    if( fr.in != null ){
                        fr.in.close();
                    }
                    if( fr.out != null){
                        fr.out.close();
                    }
                    if( fr.socket != null ){
                        fr.socket.close();
                    }
                }
            }catch( Exception exp ){}
        }
    }
    
    public class vectorandsize implements Serializable{
    	public byte[] b;
    	public int size;
    	public vectorandsize( byte[] b, int size ){
    		this.b = b;
    		this.size = size;
    	}
    }
    
    Vector recievedByteVector  = new Vector();
    
    public class innerlogic extends Thread{
    	
    	Handler ptrtoThis = null;
    	
    	public innerlogic( Handler ptrtoThis ){
    		this.ptrtoThis = ptrtoThis;
    	}
    	
    	public void run(){

    	while( keepGoing ){
    	
    		if( recievedByteVector.size() > 0 ){
		    	
		    	vectorandsize vs = (vectorandsize)recievedByteVector.elementAt(0);
				recievedByteVector.removeElementAt(0);
				byte[] bytepassedObj = vs.b;
				int sizeread = vs.size;
				
    		synchronized(handlers) {
                
                String passedObj = "";
                if( sizeread < 100 && sizeread >= 0 ){
                    passedObj = new String(bytepassedObj,0, sizeread );
                }
                
                byte[] b = new byte[sizeread];
                for( int x = 0 ; x < sizeread; x++ ){
                	b[x] = bytepassedObj[x];
            	}
            	
                    if( (sizeread > 2 && sizeread < 100 && passedObj.length() >= 2 && passedObj.substring(0,2).equals("NN") ) && ptrtoThis.nick == ""  ){
                        
                        for( int i=0;i< handlers.size();i++){ // if someone is trying to log in with a nick that is already used reject their log in
                            Handler tmp = (Handler)(handlers.elementAt(i)); // this can be used for password authentication as well.
                            if( passedObj.substring(2,  passedObj.length() - 5 ).equals( tmp.nick ) ){
                                ptrtoThis.cs.writebyte( ("bye" + "KAERB" ).getBytes() );
                                
                                keepGoing = false;
                            }
                        }
                        
                        if( keepGoing ){
                        	
                        	pingClass pc = new pingClass( ptrtoThis );
							pc.start();
                        	
                        	
                        	if( passedObj.length() > 6 ){
                           		nick = passedObj.substring(2, passedObj.length() - 5 );
                        	}
                            
                            if( nick.equals("admin") ){ // if you log in with this nick name you will be admin
                                IAmAdmin = true;
                            }
                            
                            for( int i = 0; i < handlers.size(); i++){;
                            Handler tmp = (Handler)(handlers.elementAt(i));
                            if( tmp != ptrtoThis && tmp.nick != "" && ptrtoThis.nick != "" ){ // everyone elses nick name to me
                                ptrtoThis.cs.writebyte( ("NN" + tmp.nick + "KAERB").getBytes() );
                                
                            }
                            }
                            
                            for( int i = 0; i < handlers.size(); i++){;
                            Handler tmp = (Handler)(handlers.elementAt(i));
                            if( tmp != ptrtoThis && tmp.nick != "" && ptrtoThis.nick != "" ){ // ny nick name to everyone else
                                tmp.cs.writebyte( ("NN" + ptrtoThis.nick + "KAERB").getBytes() );
                                
                            }
                            }
                        }
                    }
                    else if( sizeread > 2 && sizeread < 100 && passedObj.length() >= 2 && ( passedObj.substring(0,2).equals("NT") || passedObj.substring(0,2).equals("#&") )  ){ //stopped talking
                        if( IAmTalking == true){
                            for( int i = 0; i < handlers.size(); i++){;
                            Handler tmp = (Handler)(handlers.elementAt(i));
                            tmp.cs.writebyte( ("NT" + ptrtoThis.nick + "KAERB").getBytes() );
                           
                            }
                            ImTalkingCounter = 0;//reset the talk counter so next time someone taks it could notify them that someone is talking
                            IAmTalking = false;
                        }
                        //notify other people that nobody is talking
                    }
                    else if( sizeread >= 2 && sizeread < 100 && passedObj.length() >= 2 && ( passedObj.substring(0,2).equals("PR") ) ){ //packet recieved send next one
                     	lastpacketrecieved = true;
                    }
                    else if( sizeread > 2 && sizeread < 100 && passedObj.length() > 4 && passedObj.substring(0,4).equals("MUTE")){ // Mute one of the users
                       if ( IAmAdmin ){
	                        for( int i = 0; i < handlers.size(); i++){;
	                        Handler tmp = (Handler)(handlers.elementAt(i));
	                        if( tmp.nick.equals( passedObj.substring(4, passedObj.length() - 5) ) ){
	                            //send the user a message that he is mute
	                            tmp.cs.writebyte( ("MUTE" + ptrtoThis.nick + "KAERB").getBytes() );
	                            //tmp.out.write( ("MUTE" + this.nick).getBytes() );
	            				//tmp.out.flush();
	                            
	                            tmp.IAmMute = true;
	                            if( tmp.IAmTalking == true){ // tell everyone that they can now talk
	                                for( int j = 0; j < handlers.size(); j++){;
	                                Handler tmp2 = (Handler)(handlers.elementAt(i));
	                                tmp2.cs.writebyte( ("NT" + ptrtoThis.nick + "KAERB").getBytes() );
	                                
	                                
	                                }
	                                ImTalkingCounter = 0;//reset the talk counter so next time someone taks it could notify them that someone is talking
	                                tmp.IAmTalking = false;
	                            }
	                        }
	                        }
	                     }
                    }
                    else if( sizeread > 2 && sizeread < 100 && passedObj.length() > 6 && passedObj.substring(0,6).equals("UNMUTE")){ // Mute one of the users
                        if ( IAmAdmin ){
	                        for( int i = 0; i < handlers.size(); i++){;
	                        Handler tmp = (Handler)(handlers.elementAt(i));
	                        if( tmp.nick.equals( passedObj.substring(6, passedObj.length() - 5 ) ) ){
	                            //send the user a message that he is no longer mute
	                            tmp.cs.writebyte( ("UNMUTE" + ptrtoThis.nick + "KAERB").getBytes() );
	                            tmp.IAmMute = false;
	                        }
	                        }
                    	}
                    }
                    
                    else if( sizeread > 2 && sizeread < 100 && passedObj.substring(0,3).equals("TXT") ){ //text talking or low audio size
                        for( int i = 0; i < handlers.size(); i++){;
                        Handler tmp = (Handler)(handlers.elementAt(i));
                        	if( tmp != ptrtoThis || passedObj.length() >= 3 && passedObj.substring(0,3).equals("TXT") ){
		                        tmp.cs.writebyte( b );
		                        
                    		}
                        }
                    }
                    else{
                    	
                        boolean someoneIsTalking = false;
                        String talkersNick = "";
                        for( int i = 0; i < handlers.size(); i++){
                            Handler tmp = (Handler)(handlers.elementAt(i));
                            if( tmp.IAmTalking == true ){
                                someoneIsTalking = true;
                                talkersNick = tmp.nick;
                            }
                        }
                        if( ( handlers.size() <= 2 || someoneIsTalking == false || IAmTalking == true && ptrtoThis.nick != "" )  && ( IAmMute == false ) ){
                            if( ImTalkingCounter % 8 == 0 /*|| true*/ ){ //make sure there are more then two people in the room
                                for( int i = 0; i < handlers.size(); i++){
                                    Handler tmp = (Handler)(handlers.elementAt(i));
                                    if( tmp.nick != "" ){
                                        if(handlers.size() > 2 ){
                                        	if( nick != ""){
	                                            // stop talking, i'm using the mic also notifies the talker
	                                            tmp.cs.writebyte( ("ST" + nick + "KAERB" ).getBytes()  );
	                                            	                                            
                                        	}
                                        }else{
                                        	
                                            // one on less  when one user is talking highlights his name
                                            tmp.cs.writebyte( ("PT" + nick + "KAERB").getBytes() );
                                            //tmp.out.write( ("PT" + nick).getBytes() );
	            							//tmp.out.flush();
                                            
                                        }
                                    }
                                }
                                ImTalkingCounter = 0;
                            }
                            IAmTalking = true;
                            ImTalkingCounter++; // used to highlight names for people who are talking
                            
                            for( int i = 0; i < handlers.size(); i++){
                                Handler tmp = (Handler)(handlers.elementAt(i));
                                if( tmp != ptrtoThis && tmp.nick != "" ){ // anyone whos is not logged in wont hear voice.
                                	tmp.cs.writebyte( b );
                                	
                                	
                                }
                            }
                        }else{
                            if( handlers.size() > 2 ){ // if there is more then two people in the room
                                if( talkersNick != ""){
	                                // stop talking, someone is using the mic
	                                ptrtoThis.cs.writebyte( ("ST" + talkersNick + "KAERB" ).getBytes() );
	                                
	                             }
                            }else if( IAmMute ){
                                // stop talking, someone is using the mic
                                ptrtoThis.cs.writebyte( ("ST|YourMute" + "KAERB").getBytes() );
                               
                            }
                        }
                    }
                }
                
                }else{
		            try{
	        			synchronized( this ) {
	                    	wait(10);
	                	}
	                } catch( Exception mexp  ) {mexp.printStackTrace();}	
	        	}
            }
    	}	
    }
    
    public void run() {
        
        keepGoing = false;
        try {
            out = socket.getOutputStream();
            out.flush();
            s = new sendout( this );
            timeout t = new timeout( this );
            t.start();
            s.start();
            in = socket.getInputStream();
            t.shuldclose = false;
            
            keepGoing = true;
        }catch( IOException exp ){}
        synchronized(handlers) {
            handlers.addElement(this);
        }
        try {
        	
			breaker[0] = (byte)'K';
			breaker[1] = (byte)'A';
			breaker[2] = (byte)'E';
			breaker[3] = (byte)'R';
			breaker[4] = (byte)'B';
        	
        	innerlogic il = new innerlogic(this);
        	il.start();
        	
			byte[] mybyte = new byte[1024 * 3];
			int j = 0;
			int i = 0;
			
            while( keepGoing ) {
                byte[] bytepassedObj = new byte[bytesize];
                int sizeread = in.read( bytepassedObj, 0, bytesize );
				i=0;
				for( ; i < sizeread; i++ ){	
					mybyte[j] = bytepassedObj[i];
					if( j == (1024 * 3 - 1 ) ||  (j >= 4 && mybyte[j-4] == breaker[0] && mybyte[j-3] == breaker[1] && mybyte[j-2] == breaker[2] && mybyte[j-1] == breaker[3] && mybyte[j] == breaker[4] ) ){
						recievedByteVector.addElement( ((Object)(new vectorandsize( mybyte, j+1 ))) );
						j=-1;
						mybyte = new byte[1024 * 3];
						
					}
					j++;
				}
            }
        }catch ( NullPointerException npx ){
            npx.printStackTrace();
        } catch(IOException ioe) {
        } catch(Exception exp) {
            exp.printStackTrace();
        }
        finally {
            try {
                synchronized(handlers) {
                    for( int i = 0; i < handlers.size(); i++){
                        Handler tmp = (Handler)(handlers.elementAt(i));
                        if( tmp != this ){
                            // tell others that this user logged off
                            tmp.cs.writebyte( ("NC"+ this.nick + "KAERB").getBytes()  );
                            
                        }
                    }
                    if( IAmTalking ){ // if i'm talking
                        for( int i = 0; i < handlers.size(); i++){
                            Handler tmp = (Handler)(handlers.elementAt(i));
                            if( tmp != this ){
                                //tell others that i'm no longer talking
                                tmp.cs.writebyte( ("NT"+ this.nick + "KAERB").getBytes() );
                                
                            }
                        }
                    }
                    IAmTalking = false;
                    if( s != null ){
                    	this.s.stopit(); // stop the thread from sending any more data to me i'm already logged off
                	}
                    
                    if( ! alreadyclosed ){
                        alreadyclosed = true;
                        in.close();
                        out.close();
                        socket.close();
                    }
                }
            } catch(IOException ioe) {
            } finally {
                synchronized(handlers) {
                    if( this.nick != null && this.nick != ""){
                        System.out.println( this.nick + " signed off");
                    }
                    keepGoing = false;
                    handlers.removeElement(this);
                    
                }
            }
        }
    }
}