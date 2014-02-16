/*
						Version Controlling
=========================================================================================
		Name: - Client.java
-----------------------------------------------------------------------------------------
	Version		Date		Comments
-----------------------------------------------------------------------------------------
	1.0		25-Mar-2009	  Initial Issued Version

=========================================================================================
*/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.net.URL;
import java.util.*;
import java.net.*;

public class Client extends JFrame implements Runnable, ActionListener{
    
    private Socket socket;
    private String connectAddr;
    private int connectPort = 6035;
    private InputStream in;
    private OutputStream out;
    
    // voice  java variables
    public CommonSoundClass cs;
    Recorder r;
    Playback playback;
    
    boolean imRunning = true;
    
    final int offset = 1;
    final int bytesize = 1024-5;//1024//*1024 * 8*/;//6144;//18432;//6144;
    int peacespersecond = 8; // number of peaces per second
    boolean recording = false;
    
    // compression variables
    int compression = 0; // level of compression
    int cmpressto = bytesize / 2; // sets the compression to half of the byte size
    int leftover = 0; // how much is left over to spair out of cmpressto value
    boolean useleftover = true;
    int MaximumCompression = 9; //maximum amout of compression we want
    
    byte reduceBy = 1;  // reduces the sample rate by N used with reduceSamplerate
    byte increaseBy = 1;// increases the sample rate by N retreaveSampleRate
    
    
    //min amd max of transmission used for stats
    int minval = bytesize * peacespersecond;
    int maxval = 0;
    int recievedMinval = bytesize * peacespersecond;
    int recievedMaxval = 0;
    
    int preveousnumber = 0;
    int alternatenumber = 9999;
    int packetnumber = 9999;
    
    
    double multiplyer = 3.0;
    
    //hands free  variables
    int spikesensitivity = 70; // how many noise spikes have to happen for it to think something was said
    boolean HandsFree = false;
    
    // text  varibles
    private boolean nickEntered = false;
    private String NickName = ""; // your own nick name
    private Vector NickNameVector = new Vector(); //list of nick names in room
    private String NicktoSend = ""; // private message in text .
    
    // debugging variables
    public boolean debug = false; // standard debugging boolean variable
    
    // stats variables
    int currentsize = 0; // sent size for this second in bytes
    int avgsize = 0; // everadge sent size
    int avgcounter = 0; // counter varible for send ++
    
    int recievecounter = 0; // counter variable for recieve ++
    int currentsecondSound = 0; // how much data i recieved this second in bytes
    
    //key down recording variables
    protected javax.swing.Timer SplashTimer = new javax.swing.Timer( 500, this );
    boolean keypressed = false; // monitors the ctrl key
    boolean timercheck = false; // check if key is pressed in the timer
    boolean connected = false;
    boolean canrecord = false;
    
    //admin and users variables
    boolean IAmMute = false;
    boolean IAmAdmin = false; // not implemented on the client side
    
    // threads variables
    MyThread mt = null;
    int size = 0;
    
    byte[] breaker = new byte[5];
    
    /** Creates new form JFrame */
    public Client() {
    	/**
    	*creates the Client
    	*/
    	//displays message frm client
		breaker[0] = (byte)'K';
		breaker[1] = (byte)'A';
		breaker[2] = (byte)'E';
		breaker[3] = (byte)'R';
		breaker[4] = (byte)'B';
    	
        initComponents();
        super.setTitle( " Client Version 1.0" );
        
        txtInput.setEnabled(false);
        btnTalk.setEnabled(false);
        setButtonTalkColor( btnExit.getBackground() );
        
        txtOutput.setText( "Enter an ip adress and click connect\n");
        txtInput.requestFocus();
        
        startRecording();
        stopRecording();
        
        this.show();
        
    }
    
    /* This method is called from within the constructor to initialize the form.*/
    private void initComponents() {//initComponents
        /*Initialize all the components */
        
        mnuPopup = new javax.swing.JPopupMenu();
        mnuMute = new javax.swing.JMenuItem();
        mnuUnmute = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnExit = new javax.swing.JButton();
        txtPort = new javax.swing.JTextField();
        txtPortAdr = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextArea();
        nickPanHolder = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstNick = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        btnPan = new javax.swing.JPanel();
        btnTalk = new javax.swing.JButton();
        chkHandsFree = new javax.swing.JCheckBox();
        txtInput = new javax.swing.JTextField();
        lblStatus = new javax.swing.JLabel();

        mnuMute.setText("Mute");
        mnuMute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuMuteActionPerformed(evt);
            }
        });

        mnuPopup.add(mnuMute);

        mnuUnmute.setText("Unmute");
        mnuUnmute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuUnmuteActionPerformed(evt);
            }
        });

        mnuPopup.add(mnuUnmute);

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                formKeyTyped(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel2.setMinimumSize(new java.awt.Dimension(10, 35));
        jPanel2.setPreferredSize(new java.awt.Dimension(10, 35));
        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        jPanel2.add(btnExit);

        txtPort.setColumns(18);
        txtPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPortActionPerformed(evt);
            }
        });
        
        txtPort.setText("0.0.0.0" );
        txtPortAdr.setText("6035");
        txtPortAdr.setEditable(false);// disables a textbox
        txtPortAdr.setColumns(5);
        txtPortAdr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPortAdrActionPerformed(evt);
            }
        });
        
        jPanel2.add(txtPort);
        jPanel2.add(txtPortAdr);
        

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        jPanel2.add(btnConnect);

        jPanel3.add(jPanel2, java.awt.BorderLayout.NORTH);

        jScrollPane1.setAutoscrolls(true);
        txtOutput.setEditable(false);
        txtOutput.setFont(new java.awt.Font("Dialog", 0, 18));
        txtOutput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtOutputKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtOutputKeyReleased(evt);
            }
        });

        jScrollPane1.setViewportView(txtOutput);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        nickPanHolder.setLayout(new java.awt.BorderLayout());

        nickPanHolder.setMinimumSize(new java.awt.Dimension(100, 100));
        nickPanHolder.setPreferredSize(new java.awt.Dimension(100, 100));
        lstNick.setFont(new java.awt.Font("Dialog", 1, 12));
        lstNick.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstNickKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lstNickKeyReleased(evt);
            }
        });
        lstNick.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lstNickMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lstNickMouseReleased(evt);
            }
        });

        jScrollPane2.setViewportView(lstNick);

        nickPanHolder.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel3.add(nickPanHolder, java.awt.BorderLayout.EAST);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        jPanel1.setMinimumSize(new java.awt.Dimension(4, 80));
        jPanel1.setPreferredSize(new java.awt.Dimension(63, 80));
        btnPan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnTalk.setText("Talk");
        btnTalk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTalkActionPerformed(evt);
            }
        });
        btnTalk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnTalkKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                btnTalkKeyReleased(evt);
            }
        });

        btnPan.add(btnTalk);

        chkHandsFree.setSelected(false);
        chkHandsFree.setText("Hands Free");
        chkHandsFree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkHandsFreeActionPerformed(evt);
            }
        });
        chkHandsFree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                chkHandsFreeKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                chkHandsFreeKeyReleased(evt);
            }
        });

        btnPan.add(chkHandsFree);

        jPanel1.add(btnPan);

        txtInput.setFont(new java.awt.Font("Dialog", 0, 18));
        txtInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInputActionPerformed(evt);
            }
        });
        txtInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtInputKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtInputKeyReleased(evt);
            }
        });

        jPanel1.add(txtInput);

        jPanel3.add(jPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        lblStatus.setText("Status:");
        getContentPane().add(lblStatus, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-442)/2, (screenSize.height-316)/2, 442, 316);
    }//initComponents

    private void lstNickMouseReleased(java.awt.event.MouseEvent evt) {//event_lstNickMouseReleased
       
        if( evt.isPopupTrigger() && connected ){
            mnuPopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//event_lstNickMouseReleased
    
    private void mnuUnmuteActionPerformed(java.awt.event.ActionEvent evt) {//event_mnuUnmuteActionPerformed
       
        try{
            if(! getSelectedUsersName().equals("") ){
                out.write( ("UNMUTE" + getSelectedUsersName() ).getBytes() );
                out.flush();
                out.write( breaker );
           		out.flush();
            }
        }catch(  java.net.UnknownHostException uhkx  ){
            System.out.println("unknown host");
        }catch(  java.io.IOException iox  ){
            txtOutput.append( "\nNot Connected to the server." );
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }
    }//:event_mnuUnmuteActionPerformed
    
    private void mnuMuteActionPerformed(java.awt.event.ActionEvent evt) {//:event_mnuMuteActionPerformed
      
        try{
            out.write( ("MUTE" + getSelectedUsersName() ).getBytes() );
            out.flush();
            out.write( breaker );
            out.flush();
        }catch(  java.net.UnknownHostException uhkx  ){
            System.out.println("unknown host");
        }catch(  java.io.IOException iox  ){
            txtOutput.append( "\nNot Connected to the server." );
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }
    }//:event_mnuMuteActionPerformed
    
    private void lstNickKeyReleased(java.awt.event.KeyEvent evt) {//:event_lstNickKeyReleased
        
        KeyWasReleased( evt);
    }//:event_lstNickKeyReleased
    
    private void txtOutputKeyReleased(java.awt.event.KeyEvent evt) {//:event_txtOutputKeyReleased
       
        KeyWasReleased( evt);
    }//:event_txtOutputKeyReleased
    
    private void chkHandsFreeKeyReleased(java.awt.event.KeyEvent evt) {//:event_chkHandsFreeKeyReleased
      
        KeyWasReleased( evt);
    }//:event_chkHandsFreeKeyReleased
    
    private void btnTalkKeyReleased(java.awt.event.KeyEvent evt) {//:event_btnTalkKeyReleased
       
        KeyWasReleased( evt);
    }//:event_btnTalkKeyReleased
    
    private void txtInputKeyReleased(java.awt.event.KeyEvent evt) {//:event_txtInputKeyReleased
       
        KeyWasReleased( evt);
    }//:event_txtInputKeyReleased
    
    private void chkHandsFreeKeyPressed(java.awt.event.KeyEvent evt) {//:event_chkHandsFreeKeyPressed
       
        KeyWasPressed( evt.getKeyCode() );
    }//:event_chkHandsFreeKeyPressed
    
    private void lstNickKeyPressed(java.awt.event.KeyEvent evt) {//:event_lstNickKeyPressed

        KeyWasPressed( evt.getKeyCode() );
    }//:event_lstNickKeyPressed
    
    private void txtOutputKeyPressed(java.awt.event.KeyEvent evt) {//:event_txtOutputKeyPressed
      
        KeyWasPressed( evt.getKeyCode() );
    }//:event_txtOutputKeyPressed
    
    private void btnTalkKeyPressed(java.awt.event.KeyEvent evt) {//:event_btnTalkKeyPressed
      
        KeyWasPressed( evt.getKeyCode() );
    }//:event_btnTalkKeyPressed
    
    private void txtInputKeyPressed(java.awt.event.KeyEvent evt) {//:event_txtInputKeyPressed
       
        KeyWasPressed( evt.getKeyCode() );
    }//:event_txtInputKeyPressed
    
    private void formKeyTyped(java.awt.event.KeyEvent evt) {//:event_formKeyTyped
       
    }//:event_formKeyTyped
    
    private void chkHandsFreeActionPerformed(java.awt.event.ActionEvent evt) {//:event_chkHandsFreeActionPerformed
        
        HandsFree = chkHandsFree.isSelected();
        if( !HandsFree && recording ){
            setButtonTalkColor( Color.red );
        }
    }//:event_chkHandsFreeActionPerformed
    
    private void btnTalkActionPerformed(java.awt.event.ActionEvent evt) {//:event_btnTalkActionPerformed
        
        
        /*When Talk Button is pressed toggles the talk, dont talk*/
        
        if( recording ){
            stopRecording();
            btnTalk.setText("Talk");
            setButtonTalkColor( btnExit.getBackground() );
        }else{
            if( canrecord && IAmMute != true ){
                startRecording();
                btnTalk.setText("Stop Talk");
                setButtonTalkColor( Color.red );
            }else{
                recording = !recording;
            }
        }
        recording = !recording;
    }//:event_btnTalkActionPerformed
    
   private void txtPortActionPerformed(java.awt.event.ActionEvent evt) {//:event_txtPortActionPerformed
      
       btnConnectActionPerformed( evt );
   }//:event_txtPortActionPerformed
   
   private void txtPortAdrActionPerformed(java.awt.event.ActionEvent evt) {//:event_txtPortActionPerformed
   		btnConnectActionPerformed( evt );
   }
   
   private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//:event_btnExitActionPerformed
           
		/*
		*Exits Client
		*Stops the recodrer from running
		*And calls its the recorders onExit() function to unload its thread
		*/
		
		if( r != null ){
			r.onExit();
		}
		imRunning = false;
		System.exit(0);
		
   }//:event_btnExitActionPerformed
   
   private void lstNickMousePressed(java.awt.event.MouseEvent evt) {//:event_lstNickMousePressed
       // used to private messaging
       NicktoSend = new String(lstNick.getSelectedValue() + "");
       this.NicktoSend = NicktoSend;
      
   }//:event_lstNickMousePressed
   
   private void startRecording(){
   	    /*
    	*starts recording
    	*create a recorder class
    	*calls the Recorder clas  startRecording() if its already running
    	*/
    	
       if( cs == null || r == null){
           if( cs == null){
               cs = new CommonSoundClass();
           }
           r = new Recorder( cs, bytesize );
       }else{
           r.startRecording();
       }
       
       if( mt == null ){
           mt = new MyThread();
           mt.start();
       }
   }
   
   private void stopRecording(){
   	    /*
    	*stops recording by
    	*calling the recorders stopRecording();
    	*/
       packetnumber = 0; // resets the packet number for tracking.
       
       if( r != null){
           r.stopRecording();
           
       }
   }
   
   private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//:event_btnConnectActionPerformed
              
        /*
        *Tryes to connect to the server 
    	*Enables the text fields
    	*/
       if (! txtPort.getText().equals("")){
           try{
               connectAddr = txtPort.getText();
               try{
               connectPort = Integer.parseInt(txtPortAdr.getText() );
               }catch( Exception Exp ){
               		connectPort = 6035;	
               }
               socket = new Socket( connectAddr, connectPort );
               in = socket.getInputStream();
               out = socket.getOutputStream();
               out.flush();
               
               Thread t = new Thread( this, "socket listener" );
               t.start();
               
               connected = true;
               
               btnConnect.setEnabled(false);
               txtPort.setEditable(false);
               txtPortAdr.setEditable(false);
               txtInput.setEnabled(true);
               btnTalk.setEnabled(true);
               txtOutput.setText( "Enter a user name and press enter\n");
               
           }catch(  java.net.UnknownHostException uhkx  ){
               System.out.println("unknown host");
           }catch(  java.io.IOException iox  ){
               txtOutput.append( "\nUnable To Connect to the Server." );
               txtOutput.moveCaretPosition(txtOutput.getText().length() );
           }
       }
   }//:event_btnConnectActionPerformed
   
    private void txtInputActionPerformed(java.awt.event.ActionEvent evt) {//:event_txtInputActionPerformed
        
        
        /**
    	*Sends what ever you typed in the input field to the server
    	*Send the username to the server
    	*/
        
        if( txtInput.getText().equals("/help") ){
            txtOutput.append( "\nAvailable commands\n/setmax - maximum compression\n/compressto - size to compress it down to\n/leftover - use the leftover bytes\n/sensitivity - hands free sensitivity \n/amplify - increase volume upto 3x\n/stats - shows current settings\n/reset - resets the min and max sent and recieved values" );
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
            
        }else if( txtInput.getText().equals("/stats") ){
            txtOutput.append( "\ncompressto = " + getCompressto() + "\nsetmax = " + MaximumCompression + "\nsensitivity = " + spikesensitivity  );
            txtOutput.append( "\namplify = " + multiplyer + "\nleftover = " + useleftover + "\nreduceby = " + reduceBy + "\nincreaseby = " + increaseBy );
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }
        else if( txtInput.getText().length()  >= 9 &&  txtInput.getText().substring(0,9).equals("/reduceby") ){
            
            try{
                int i = Integer.parseInt( txtInput.getText().substring(9).trim() );
                if( i >= 0 ){
                    reduceBy = (byte)i;
                }
            }catch(Exception exp){}
            
            txtOutput.append( "\nSample rate is now reduced by: " + reduceBy );
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }
        else if( txtInput.getText().length()  >= 11 &&  txtInput.getText().substring(0,11).equals("/increaseby") ){
            
            try{
                int i = Integer.parseInt( txtInput.getText().substring(11).trim() );
                if( i >= 0 ){
                    increaseBy = (byte)i;
                }
            }catch(Exception exp){}
            
            txtOutput.append( "\nSample rate is now increased by: " + increaseBy);
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
            
        }
        else if( txtInput.getText().length()  >= 8 &&  txtInput.getText().substring(0,8).equals("/amplify") ){
            
            try{
                double i = Double.parseDouble( txtInput.getText().substring(8).trim() );
                if( i > 0.1 && i <= 3.0 ){
                    multiplyer = i;
                }
            }catch(Exception exp){}
            
            txtOutput.append( "\nAmplification is now: " + multiplyer);
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
            
        }else if( txtInput.getText().length()  >= 12 &&  txtInput.getText().substring(0,12).equals("/sensitivity") ){
            
            try{
                int i = Integer.parseInt( txtInput.getText().substring(12).trim() );
                if( i >= 0 ){
                    spikesensitivity = i;
                }
            }catch(Exception exp){}
            
            txtOutput.append( "\nSensitivity is now: " + spikesensitivity);
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
            
        }else if( txtInput.getText().equals("/reset") ){
            
            minval = bytesize * peacespersecond;
            maxval = 0;
            
            recievedMinval = bytesize * peacespersecond;
            recievedMaxval = 0;
            
            txtOutput.append( "\n" + "stats reset" );
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }else if( txtInput.getText().equals("/leftover") ){
            useleftover = !useleftover;
            if( useleftover ){
                txtOutput.append( "\n" + "leftover on" );
            }else{
                txtOutput.append( "\n" + "leftover off" );
            }
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }else if( txtInput.getText().length()  >= 7 &&  txtInput.getText().substring(0,7).equals("/setmax") ){
            
            try{
                int i = Integer.parseInt( txtInput.getText().substring(7).trim() );
                if( i >= 0 ){
                    MaximumCompression = i;
                }
            }catch(Exception exp){}
            
            
            txtOutput.append( "\nMaximum compression: " + MaximumCompression);
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }else if( txtInput.getText().length()  >= 11 &&  txtInput.getText().substring(0,11 ).equals("/compressto") ){
            
            try{
                int i = Integer.parseInt( txtInput.getText().substring(11).trim() );
                if( i > 0 ){
                    setCompressto( i );
                }
            }catch(Exception exp){}
            
            txtOutput.append( "\nWill try to compress down to: " + getCompressto() );
            txtInput.setText("");
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }
        
        if( nickEntered == false && !txtInput.getText().equals("")){
            NickName = txtInput.getText();
            nickEntered = true;
            NickNameVector.addElement( (Object)NickName );
            lstNick.setListData( NickNameVector );
            try{
                out.write( ("NN" + NickName + "KAERB" ).getBytes() );
                out.flush();
                canrecord = true; // cant record sound until you log in with a name.
            }catch(  java.net.UnknownHostException uhkx  ){
                System.out.println("unknown host");
            }catch(  java.io.IOException iox  ){
                txtOutput.append( "\nNot Connected to the server." );
                txtOutput.moveCaretPosition(txtOutput.getText().length() );
            }
        }else if( !txtInput.getText().equals("")){
            try{
                out.write( ("TXT" + NickName + ": "  + txtInput.getText() + "KAERB" ).getBytes() );
                out.flush();
            }catch(  java.net.UnknownHostException uhkx  ){
                System.out.println("unknown host");
            }catch(  java.io.IOException iox  ){
                txtOutput.append( "\nNot Connected to the server." );
                txtOutput.moveCaretPosition(txtOutput.getText().length() );
            }
        }

        txtInput.setText("");
    }//:event_txtInputActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//:event_exitForm
    
	/**
	*Exits Client
	*Stops the recodrer from running
	*And calls its the recorders onExit() function to unload its thread
	*/
        if( r != null ){
            r.onExit();
        }
        imRunning = false;
        System.exit(0);
    }//:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    
    public static void main( String args[] ) {
        Client app = new Client();
    }
    public void windowOpened(java.awt.event.WindowEvent windowEvent) {
    }
    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
    }
    public void windowDeiconified(java.awt.event.WindowEvent windowEvent) {
    }
    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    	/**
		*Exits Client
		*Stops the recodrer from running
		*And calls its the recorders onExit() function to unload its thread
		*/
    	
        if( r != null ){
            r.onExit();
        }
        imRunning = false;
        System.exit( 0 );
    }
    public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {
    }
    public void windowActivated(java.awt.event.WindowEvent windowEvent) {
    }
    public void windowIconified(java.awt.event.WindowEvent windowEvent) {
    }
    
    
    public class vectorandsize implements Serializable{
    	public byte[] b;
    	public int size;
    	public vectorandsize( byte[] b, int size ){
    		this.b = b;
    		this.size = size;
    	}
    }
    
    Vector recievedByteVector  = new Vector(); // common sound class queue for recieved data, so the in.read would not freeze up during calculations.
    public class innerlogic extends Thread{
    	    
	    synchronized public void run(){
	            try{
	            	while( true ){
	            	
	            	if( recievedByteVector.size() > 0 ){
	            		
		            	vectorandsize vs = (vectorandsize)recievedByteVector.elementAt(0);
		            	recievedByteVector.removeElementAt(0);
		            	byte[] bytepassedObj = vs.b;
		                int sizeread = vs.size;
		            	
		            	String passedObj = "";
		                if( sizeread < 100 && sizeread >= 2){
		                    passedObj = new String(bytepassedObj, 0, sizeread);
		                }
		                
		                if( sizeread < 100 && sizeread > 3 && passedObj.substring(0,3).equals("TXT") ){
		                	
		                	if( txtOutput.getText().length() > 1024){ // clear the text if its more then 1024
		                    	txtOutput.setText("");
		                	}
		                    txtOutput.append( "\n" + passedObj.substring(3) );
		                    txtOutput.moveCaretPosition(txtOutput.getText().length() );
		                }
		                else if( sizeread < 100 && passedObj.length() >= 2 && passedObj.substring(0,2).equals("NC")){
		                    for( int i = 0; i < NickNameVector.size(); i++){
		                        if( (NickNameVector.elementAt(i) + "" ).equals( passedObj.substring(2)) ){
		                            NickNameVector.removeElementAt(i);
		                            break;
		                        }
		                    }
		                    lstNick.setListData( NickNameVector );
		                }
		                else if( sizeread < 10 && passedObj.length() >= 3 && passedObj.substring(0,3).equals("bye")){
		                    imRunning = false;
		                }
		                else if( sizeread < 100 && passedObj.length() >= 2 && passedObj.substring(0,2).equals("NN")){
		                    NickNameVector.addElement( passedObj.substring(2) );
		                    lstNick.setListData( NickNameVector );
		                }
		                else if( sizeread < 100 && passedObj.length() >= 2 && passedObj.substring(0,2).equals("NT")){
		                    if( NickName != "" ){
		                        canrecord = true;
		                        lstNick.clearSelection();
		                        
		                        alternatenumber = 9999;
		                        preveousnumber = 9999; 
		                    }
		                }
		                else if( sizeread < 100 && passedObj.length() >= 2 && passedObj.substring(0,2).equals("ST")){ // more then two peole in  someone talking
		                    if( !passedObj.substring(2).equals(NickName) ){ // the the person that is talking is not you
		                        SomeoneIsTalking();
		                    }
		                    for( int i = 0; i < NickNameVector.size(); i++){
		                        if( (NickNameVector.elementAt(i) + "" ).equals( passedObj.substring(2)) ){
		                            lstNick.setSelectedIndex(i);
		                            break;
		                        }
		                    }
		                }
		                else if( sizeread < 100 && passedObj.length() >= 4 && passedObj.substring(0,4).equals("MUTE")){ // admin mutes you
		                    SomeoneIsTalking();
		                    IAmMute = true;
		                }
		                else if( sizeread < 100 && passedObj.length() >= 6 && passedObj.substring(0,6).equals("UNMUTE")){ // admin unmutes you 
		                    IAmMute = false;
		                    canrecord = true;
		                }
		                else if( sizeread < 100 && passedObj.length() >= 2 && passedObj.substring(0,2).equals("PT")){ // one on one  someone is talking
		                    for( int i = 0; i < NickNameVector.size(); i++){
		                        if( (NickNameVector.elementAt(i) + "" ).equals( passedObj.substring(2)) ){
		                            lstNick.setSelectedIndex(i);
		                            break;
		                        }
		                    }
		                }else{
		                	
		                	boolean palythispacket = false;
		                	int packetnumber =  bytepassedObj[sizeread-1];
		                	
		                	if( true || alternatenumber == 9999 || preveousnumber == 9999 || alternatenumber == packetnumber -1 ||  preveousnumber == packetnumber-1 || (packetnumber == -128 && preveousnumber == 127 ) || (packetnumber == -128 && alternatenumber == 127 ) ){
		                	
		                		preveousnumber = packetnumber;
		                		if( alternatenumber == 9999 ){
		                			alternatenumber = packetnumber;
		                		}
		                		palythispacket = true;
		                	}else{
		                		alternatenumber = packetnumber;
		                		palythispacket = false;
		                	
		                	}
		                	
							if( palythispacket ){
								if (playback == null){
								    
								    if( sizeread >= 1 ){
								        increaseBy = bytepassedObj[sizeread-1];
								        byte[] newbyte = new byte[sizeread-1];
								        for( int i = 0; i < sizeread-1; i++){
								        	newbyte[i] = bytepassedObj[i];
								        }
								        bytepassedObj = newbyte;
									}
								    
								    bytepassedObj = split( bytepassedObj );
								    bytepassedObj = decompress(bytepassedObj);// decompress the data
								    bytepassedObj = retreaveSampleRate( bytepassedObj, increaseBy );
								    
								    playback = new Playback( bytepassedObj );
								   
								    
								   
								}
								else{
									
									if( sizeread >= 1 ){
								    	increaseBy = bytepassedObj[sizeread-1];
								        byte[] newbyte = new byte[sizeread-1];
								        for( int i = 0; i < sizeread-1; i++){
								        	newbyte[i] = bytepassedObj[i];
								        }
								        bytepassedObj = newbyte;
								    }
									
								    bytepassedObj = split( bytepassedObj );
								    bytepassedObj = decompress(bytepassedObj);// decompress the data
								    bytepassedObj = retreaveSampleRate( bytepassedObj, increaseBy );
								    
								    playback.setSound( bytepassedObj );
								    
								    
								}
								
								recievecounter++;
								currentsecondSound += sizeread;
								if( recievecounter % peacespersecond == 0 ){
								    
								    recievedMinval = Math.min( currentsecondSound, recievedMinval);
								    recievedMaxval = Math.max( currentsecondSound, recievedMaxval);
								    
								    setStats( "recieved: " + currentsecondSound+ "| Max: " + recievedMaxval + " | Min: " + recievedMinval );
								    recievecounter = 0;
								    currentsecondSound = 0;
								}
							}
		                }
		                
		            	}else{
			            	try{
		            			synchronized( this ) {
	                            	wait(50);
	                        	}
	                        } catch( Exception mexp  ) {mexp.printStackTrace();}	
		            	}
	            	}
	            }catch( Exception exp ){}
	        }
    }
    
    public void run() {
    	
		/**
		*Listens for the data from the server
		*/
    	
        try{
        	
        	innerlogic il = new innerlogic();
        	il.start();
        	
        	byte[] mybyte = new byte[1024 * 3];
			int j = 0;
			int i = 0;
            
            while( imRunning ) {
                byte[] bytepassedObj = new byte[bytesize + offset];
                int sizeread = in.read( bytepassedObj, 0, bytesize + offset );

                i=0;
				for( ; i < sizeread; i++ ){	
					mybyte[j] = bytepassedObj[i];
					if( j == (1024 * 3 - 1 ) ||  (j >= 4 && mybyte[j-4] == breaker[0] && mybyte[j-3] == breaker[1] && mybyte[j-2] == breaker[2] && mybyte[j-1] == breaker[3] && mybyte[j] == breaker[4] ) ){
						if( j-4 != 0 ){
							recievedByteVector.addElement( ((Object)(new vectorandsize( mybyte, j-4 ))) );
						}
						
						j=-1;
						mybyte = new byte[1024 * 3];
					}
					j++;
				}
            }
            in.close();
            out.close();
            socket.close();
        }catch ( NullPointerException npx ){
            npx.printStackTrace();
        }catch(IOException ioe) {
            txtOutput.append( "\nLost Connection with the server." );
            txtOutput.moveCaretPosition(txtOutput.getText().length() );
        }catch(Exception exp) {
            exp.printStackTrace();
        }
        
    }
    
    public void setStats( String str){
    	/**
		*Updates the status bar
		*/
        lblStatus.setText( str );
    }
    
    // send the recorded sound to the server.
    private class MyThread extends Thread{
    	/**
		*Gets the data from the recording queue and sends it to the server
		*/
        public void run(){
            
        /**
		*Runs while CommonSoundClass is not null
		*/
            
            while( true ){
                while( cs != null ){
                    
                    byte[] b = (byte[])cs.readbyte();
                    
                    if( socket != null ){
                    
                    try{
                        if( b.length < 10 ){
                            out.write( b );
                            out.flush();
                            while( cs.vec.size() > 0 ){
                                b = (byte[])cs.readbyte();
                            }
                        }else{
                        	
                                                	
                            avgcounter++; // add one to the avgcounter
                            
                            int localcompression = 0;
                            byte[] localbyte = new byte[bytesize]; // create a byte
                            do{
                                setCompression(localcompression); //sets compression to N
                                for( int i = 0; i < localbyte.length && i < b.length; i++ ){ // copy the byte from the b
                                    localbyte[i] = b[i];
                                }
                                
                                setDataSize( bytesize );
                                localbyte = reduceSamplerate( localbyte, reduceBy  ); // reduce samples by 2 
                                localbyte = compress( localbyte ); // compression
                                localbyte = merge( localbyte ); // secont layer compression
                                localcompression++;
                            }while( (getDataSize() - leftover) > getCompressto() && getCompression() < MaximumCompression );
                            
                            if( debug == true ){
                                printbyte(localbyte);
                            }
                            
                            if( useleftover == false ){
                                leftover = 0;
                            }
                            
                            if( getCompressto() - getDataSize() > 0 ){
                                leftover += (getCompressto() - getDataSize()); // sets the left over value which is reset every second
                            }else{
                                leftover -= (getDataSize() - getCompressto());
                                if( leftover < 0){
                                    leftover = 0;
                                }
                            }
                            
                            if( avgcounter % peacespersecond == 0 ){
                                leftover = 0;
                            }
                            
                            byte[] b2 = null;
                            if( localbyte == null ){ // if i'm not talking and using hands free mode
                            	localbyte = ("NT|".getBytes() );
                            	setDataSize(localbyte.length);
                            }else{
	                            b2 = new byte[getDataSize()+1];
	                            for( int i = 0; i < getDataSize(); i++){
	                            	b2[i] = localbyte[i];
	                            }
	                            
	                            b2[getDataSize()] = reduceBy;
	                            localbyte = b2;
	                            setDataSize( getDataSize() + 1 );
                        	}
                        	
	                            out.write( localbyte, 0, getDataSize() );
	                            out.flush();
	                        try{
		            			synchronized( this ) {
	                            	//wait(1);
	                        	}
	                        } catch( Exception mexp  ) {mexp.printStackTrace();}	
	                            
                            //calculate avg bytes a second
                            avgsize += getDataSize();
                            currentsize += getDataSize();
                            
                            if( avgcounter % peacespersecond == 0 ){
                                
                                minval = Math.min( currentsize, minval);
                                maxval = Math.max( currentsize, maxval);
                                
                                setMyTttle( " Client compression " + getCompression() + " transfer rate: "
                                + currentsize + " avg: " + avgsize/(avgcounter/peacespersecond)
                                + "| Max: " + maxval + " | Min: " + minval );
                                
                                currentsize = 0;
                                if( avgcounter > 500){
                                    avgcounter = 0;
                                    avgsize = 0;
                                }
                            }
                        }
                        
                        out.write( breaker );
                        out.flush();
                        
                    }catch(  java.net.UnknownHostException uhkx  ){
                        System.out.println("unknown host");
                    }catch(  java.io.IOException iox  ){
                        if ( canrecord && IAmMute != true ){
                            SomeoneIsTalking(); // stop talking because your not connected to the server.
                            txtOutput.append( "\nLost Connection with the server." );
                            txtOutput.moveCaretPosition(txtOutput.getText().length() );
                        }
                    }
                }
            	
            	}
                try { wait(100); } catch( InterruptedException ie ) {}
            }
        }
    }
    
    
    public void setMyTttle( String title ){
        super.setTitle( title );
    }
    
    
    int totalcounter = 0;
    public byte[] removeNoise(byte[] b ){ // doesnt really remove noise i use this for hands free mode
    
        /**
		*Is used for hands free recording mode doesnt remove noise
		*/
        if( HandsFree || keypressed ){
            byte[] returnThis = new byte[b.length];
            
            int hit = 0;
            int counterhit = 0;
            
            for( int i = 0; i < b.length; i++){
                if( b[i] <= (byte) 3 && b[i] >= (byte) -3 ){ // proboble noise
                    hit++;
                }else if( b[i] >= (byte)4| b[i] <= (byte)-4){
                    counterhit++;
                }
            }
            
            if( counterhit > spikesensitivity || keypressed ){
                totalcounter=16;//record for 2 seconds of sound no matter what
                setButtonTalkColor( Color.red );
                btnTalk.setText( "Stop Talk " + counterhit );
                return b;
            }
            
            if( totalcounter > 0 ){
                totalcounter--;
                setButtonTalkColor( Color.red );
                return b;
            }
            
            for( int i = 0; i < b.length; i++){
                returnThis[i] = (byte)0;
            }
            returnThis[0] = 'N';
            returnThis[1] = 'T';
            
            setButtonTalkColor( Color.yellow );
            return null;
        }else{
            
            return b;
        }
    }
    
    // level one ultra low compression
    public byte[] compress( byte[] b ){
    	
    	/**
		*Compresses the data by representing a group of 
		*repeating numbers as a number followed by how many times it repeats
		*/
        b = removeNoise( b );
        
        if( b == null ){
			setDataSize(0);
			return b;
		}
        
        byte[] returnThis = new byte[b.length];
        int j = 0;
        for( int i = 0; b != null && i < b.length; i++){
            
            int same = areTheySame(b, i);
            if( (same > 3 && (b[i] >= 10 || ( b[i] > -6 && b[i] < 0 )  ) ) || ( same > 4 ) ){
                returnThis[j] = (byte)59;// keeps track of it by this number
                j++;
                returnThis[j] = b[i];// the actual value
                j++;
                returnThis[j] = (byte)same;// how many are repeating
                i+=(same - 1);
            }else{
                if( b[i] < (byte)59 && b[i] > -59 ){ // gets rid of numbers higher then 59
                    returnThis[j] = b[i];
                }else{
                    if( b[i] > -59 ){
                        returnThis[j] = (byte)((int)b[i] / 2.2);
                    }else{
                        returnThis[j] = (byte)((int)b[i] / 2.2);
                    }
                }
            }
            j++;
        }
        setDataSize( j );
        return returnThis;
    }
    
    // level one ultra low compression
    public int areTheySame( byte[] b, int start ){
    	/**
		*Checks if the next number is the same as the first one
		*called by the compress function
		*/
        
        for(int i=start+1; i < b.length; i++ ){
            
            if( getCompression() >= 1 && ( b[start] == b[i] + (byte)1 || b[start] == b[i] - (byte)1  ) ){
            }else if( getCompression() >= 2 && (b[start] == ( b[i] + (byte)2 ) || b[start] ==  b[i] - (byte)2 ) ){
            }else if( getCompression() >= 3 && (b[start] == ( b[i] + (byte)3 ) || b[start] ==  b[i] - (byte)3 ) ){
            }else if( getCompression() >= 4 && (b[start] == ( b[i] + (byte)4 ) || b[start] ==  b[i] - (byte)4 ) ){
            }else if( getCompression() >= 5 && (b[start] == ( b[i] + (byte)5 ) || b[start] ==  b[i] - (byte)5 ) ){
            }else if( getCompression() >= 6 && (b[start] == ( b[i] + (byte)6 ) || b[start] ==  b[i] - (byte)6 ) ){
            }else if( getCompression() >= 7 && (b[start] == ( b[i] + (byte)7 ) || b[start] ==  b[i] - (byte)7 ) ){
            }else if( getCompression() >= 8 && (b[start] == ( b[i] + (byte)8 ) || b[start] ==  b[i] - (byte)8 ) ){
            }else if( getCompression() >= 9 && (b[start] == ( b[i] + (byte)9 ) || b[start] ==  b[i] - (byte)9 ) ){
            }else if( b[start] != b[i] ){
                return (i - start );
            }
            
            if( i - start >= 59 ){
                return (i - start );
            }
        }
        return b.length - start;
    }
    
    public byte[] decompress( byte[] b ){
    	
    	/**
		*Decompresses the data
		*/
    	
        byte[] returnThis = new byte[bytesize];
        int j = 0;
        
        for( int i = 0; i < b.length && j < returnThis.length; i++){
            double myMultiplyer = multiplyer; //local multiplyer
            
            if( b[i] == 59 && (i+2) < (b.length)  ){
                
                if( Math.abs(b[i+1]) > 50 && myMultiplyer > 2.5){ // adjust the multiplyer so you can amplify upto 3 times
                    myMultiplyer = 2.1;
                }else if( Math.abs(b[i+1]) > 42 && myMultiplyer > 2.5){ // adjust the multiplyer so you can amplify upto 3 times
                    myMultiplyer = 2.5;
                }
                
                returnThis[j] = (byte)(b[i+1] * myMultiplyer); // multiplyer sets the amplification
                for( int x = 0; returnThis.length > j+1 && b.length > i+2 && x < b[i+2] -1; x++){
                    j++;
                    returnThis[j] = (byte)(b[i+1] * myMultiplyer); //multiplyer sets the amplification
                }
                i+=2;
            }else{
                if( Math.abs(b[i]) > 50 && myMultiplyer > 2.5){ // adjust the multiplyer so you can amplify upto 3 times
                    myMultiplyer = 2.1;
                }else if( Math.abs(b[i]) > 42 && myMultiplyer > 2.5){ // adjust the multiplyer so you can amplify upto 3 times
                    myMultiplyer = 2.5;
                }
                returnThis[j] = (byte)(b[i] * myMultiplyer); // multiplyer sets the amplification
            }
            j++;
        }
        return returnThis;
    }
    
    public byte[] merge( byte[] b ){
    	
    	/**
		*Merges numbrs between 0 and 9 and between -6 and -9
		*represents two bytes as one for example
		*(byte)2 and (byte)7 would be (byte)127
		*/
		
		if( b == null ){
			setDataSize(0);
			return null;	
		}
		
        byte[] returnThis = new byte[b.length];
        int j = 0;
        
        for( int i = 0; b != null && i < getDataSize() && j < returnThis.length; i++){
            if( b.length > i+1  ){
                if( b[i] < (byte)10 && b[i+1] < (byte)10 && b[i] >= (byte)0 && b[i+1] >= (byte)0  ){
                    if( b[i] > (byte)5 ){ 															// 6 - 9
                        returnThis[j] = (byte)(((int)b[i] * 10) + (int)b[i+1]);
                    }else if( b[i] < (byte)2 ){ 													//0 - 1
                        returnThis[j] = (byte)( 100 + ((int)b[i] * 10) + (int)b[i+1]);
                    }else if(b[i] == (byte)2 && b[i+1] <= 7 ){ 										//2 and 0 - 7
                        returnThis[j] = (byte)( 100 + ((int)b[i] * 10) + (int)b[i+1]);
                    }else if( b[i] == (byte)3 ){													//3 -100
                        returnThis[j] = (byte) (-1*(100 + (int)b[i+1]));
                    }else if( b[i] == (byte)4 ){													//4 -111
                        returnThis[j] = (byte) (-1*(110 + (int)b[i+1]));
                    }else if( b[i] == (byte)5 && b[i+1] <= 7 ){										//5 and 0 - 7 -122
                        returnThis[j] = (byte) (-1*(120 + (int)b[i+1]));
                    }else{																			//if cant compress
                        returnThis[j] = b[i];
                        i--;
                    }
                    i++; //increment the i to skip next record
                }else if( b[i] <= (byte)-6 && b[i+1] <= (byte)0 && b[i] >= (byte)-9 && b[i+1] >= (byte)-9 ){
                    returnThis[j] = (byte)( ((int)b[i]  * 10) + (int)b[i+1]);
                    i++; //increment the i to skip next record
                }else{
                    returnThis[j] = b[i];
                }
            }
            j++;
        }
        setDataSize( j );
        return returnThis;
        
    }
    
    public byte[] split( byte[] b ){ // reverses the merge
    
		/**
		*Splits the compressed numbers
		*Ex: 127 becomes 2 and 7
		*/
    
        byte[] returnThis = new byte[bytesize];
        int j = 0;
        
        for( int i = 0; i < b.length && j < returnThis.length; i++){
            if( b.length > i+1  ){
                if( (b[i] > (byte)59 || b[i] <= (byte)-100  ) && j+1 < returnThis.length ){
                    if( b[i] > (byte)59 && b[i] < 100 ){// 6 - 9
                        returnThis[j] = (byte)(b[i] / 10);
                        j++;
                        returnThis[j] = (byte)(b[i] % 10);
                    }else if( b[i] >= (byte)100 && b[i] <= (byte)127 ){	//0 - 1
                        returnThis[j] = (byte)( (b[i] - 100) / 10);
                        j++;
                        returnThis[j] = (byte)( (b[i] - 100) % 10);
                    }else if( b[i] <= (byte)-100 && b[i] >= (byte)-127 ){//3 - 5 -100
                        returnThis[j] = (byte)( ((b[i]*-1) - 70) / 10);
                        j++;
                        returnThis[j] = (byte)( ((b[i]*-1) - 70) % 10);
                    }else{												//if cant compress
                        returnThis[j] = b[i];
                    }
                }else if( b[i] <= -60 && b[i] >= (byte)-99 && j+1 < returnThis.length  ){
                    returnThis[j] = (byte)(b[i] / 10);
                    j++;
                    returnThis[j] = (byte)(b[i] % 10);
                }else{
                    returnThis[j] = b[i];
                }
            }
            j++;
        }
        return returnThis;
    }
    
    
    public byte[] reduceSamplerate( byte[] b , int LowerSampleRateBy ){
    	
    	if( LowerSampleRateBy <= 1 || LowerSampleRateBy >= 127 ){
    		return b;
    	}
    	
		byte returnThis[] = new byte[bytesize - (bytesize / LowerSampleRateBy )];
		
        int j = 0;
        
        if( b == null ){
			setDataSize(0);
			return b;
		}
        for(  int i = 0; j < returnThis.length && i < b.length ; i++ ){
    		returnThis[j] = (byte)(b[i] );
    		if( i % LowerSampleRateBy == 0 ){
    			i++;
    		}
    		j++;
        }
        
        return returnThis;
    }
    
    public byte[] retreaveSampleRate( byte[] b, int LowerSampleRateBy ){
    	
    	if( LowerSampleRateBy <= 1 || LowerSampleRateBy >= 127 ){
    		return b;	
    	}
    	
    	byte returnThis[] = new byte[bytesize];
    	int j = 0;
		for( int i = 0; i < returnThis.length; i++ ){
			returnThis[i] = (byte)(b[j] );
			if( i % LowerSampleRateBy == 0 ){
				i++;
				if( i < returnThis.length ){
					returnThis[i] = returnThis[i-1];
				}
			}
			j++;
        }
    	return returnThis;
    }
    
    
    public int getDataSize(){
    	/**
    	*Returns the data size after compression or a merge used for sending only the data and not the succeeding zeros
		*/
        return this.size;
    }
    
    public void setDataSize( int size ){
    	/**
    	*Sets the data size called form merge and compress functions
		*/
        this.size = size;
    }
    
    public void setCompressto( int number ){
    	/**
    	*Sets the maxumum compression that the program will try before reaching the desired file size
    	*more ocmpression = worse sound quality and a smaller file
		*/
        cmpressto = number;
    }
    public int getCompressto(){
    	/**
    	*Gets the maxumum compression that the program will try before reaching the desired file size
		*/
        return cmpressto;
    }
    
    public void setCompression( int number){
    	/**
    	*Called from inside Mythread's run() when it increments the compression
		*/
        compression = number;
    }
    
    public int getCompression(){
    	/**
    	*Called by Compress's areTheySame() function when it has to deside how much compression to use
		*/
        return compression;
    }
    
    public String getSelectedUsersName(){
        return NicktoSend;
    }
    
    public void setButtonTalkColor(Color c ){
        btnTalk.setBackground( c );
    }
    
    public void KeyWasPressed( int keycode ){
        
        if( canrecord && IAmMute != true ){
            if( keycode == KeyEvent.VK_CONTROL && keypressed == false && connected == true ){
                startRecording();
                keypressed = true;
                timercheck = true;
                recording = true;
                SplashTimer.start();
            }else if( keycode == KeyEvent.VK_CONTROL && keypressed && timercheck == false ){
                timercheck = true;
            }
        }
    }
    public void KeyWasReleased( java.awt.event.KeyEvent evt ){
    }
    
    public void printbyte( byte[] b ){
        for( int i = 0; i < b.length; i++ ){
            System.out.print( b[i] + " " );
        }
        System.out.println("\n\n");
        debug = true;
    }
    
    public void SomeoneIsTalking(){
        //stop talking
        /**
    	*Called from inside the rum() method when the server wants you to 
    	*stop talking or notifies you that its not listening to you or reboadcasting your signal
		*/
        
        canrecord = false;
        stopRecording();
        keypressed = false;
        SplashTimer.stop();
        btnTalk.setText("Talk");
        setButtonTalkColor( btnExit.getBackground() );
        recording = false;
    }
    
    public void actionPerformed(ActionEvent e) { //timers action
        /**
    	*Called by the SplashTimer every half a second 
    	*checks if the CTRL button is being pushed down
    	*if not it calls the stopRecording() method 
    	*/
        
        if( timercheck == false){
            stopRecording();
            keypressed = false;
            SplashTimer.stop();
            btnTalk.setText("Talk");
            setButtonTalkColor( btnExit.getBackground() );
            recording = false;
        }
        timercheck = false; // set the timercheck to false afterwards
    }
    
    // Variables declaration
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnExit;
    private javax.swing.JPanel btnPan;
    private javax.swing.JButton btnTalk;
    private javax.swing.JCheckBox chkHandsFree;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JList lstNick;
    private javax.swing.JMenuItem mnuMute;
    private javax.swing.JPopupMenu mnuPopup;
    private javax.swing.JMenuItem mnuUnmute;
    private javax.swing.JPanel nickPanHolder;
    private javax.swing.JTextField txtInput;
    private javax.swing.JTextArea txtOutput;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtPortAdr;
    // End of variables declaration
    
}
