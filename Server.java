/*
						Version Controlling
=========================================================================================
		Name: - Server.java
-----------------------------------------------------------------------------------------
	Version		Date		Comments
-----------------------------------------------------------------------------------------
	1.0		25-Mar-2009	  Initial Issued Version

=========================================================================================
*/
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.*;
import java.awt.*;


public class Server extends Thread{
        
    public static final int DEFAULT_PORT = 6035;
    public static void main(String[] args) {
        
        int port = DEFAULT_PORT;
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            if(args.length > 0)
                port = Integer.parseInt(args[0]);
        } catch(NumberFormatException nfe) {
            System.err.println("Usage: java Server [port]");
            System.err.println("Where options include:");
            System.err.println("\tport the port on which to listen.");
            System.exit(0);
        }
        try {
            serverSocket = new ServerSocket(port);
            
            while(true) {
                socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch(IOException ioe) {
        	System.out.println("\n\nServer is already running\n\n\n\n\n\n\n\n\n");
            ioe.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}