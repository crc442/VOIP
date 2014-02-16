/*
						Version Controlling
=========================================================================================
		Name: - Recorder.java
-----------------------------------------------------------------------------------------
	Version		Date		Comments
-----------------------------------------------------------------------------------------
	1.0		25-Mar-2009	  Initial Issued Version

=========================================================================================
*/
import	java.io.IOException;
import	java.io.File;
import java.io.ByteArrayOutputStream;

import	javax.sound.sampled.DataLine;
import	javax.sound.sampled.TargetDataLine;
import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.AudioSystem;
import	javax.sound.sampled.AudioInputStream;
import	javax.sound.sampled.LineUnavailableException;
import	javax.sound.sampled.AudioFileFormat;
import  javax.swing.*;
import  java.awt.*;

public class Recorder
extends		Thread {
    private TargetDataLine	m_line;
    private AudioFileFormat.Type m_targetType;
    private AudioInputStream m_audioInputStream;
    private boolean	m_bRecording;
    private boolean	m_bQuitting;
    
    final int bytesize;
    public byte bs[];
    public static CommonSoundClass cs;
    
    boolean onlyonce = false;
    
    public Recorder( CommonSoundClass csPtr, int bytesize ){
        this.cs = csPtr;
        this.bytesize = bytesize;
        
        boolean gotrecordingline = true;
        
        ByteArrayOutputStream outputFile = new ByteArrayOutputStream();
        AudioFormat	audioFormat = null;
        // 8 kHz, 8 bit, mono
        audioFormat =  new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 8, 1, 1, 8000.0F, false);
        // 44.1 kHz, 16 bit, stereo

        DataLine.Info	info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine	targetDataLine = null;
        
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
        }
        catch (LineUnavailableException e) {
            System.out.println("unable to get a recording line");
            gotrecordingline = false;
           
        }
        
        if( gotrecordingline ){        
	        AudioFileFormat.Type	targetType = AudioFileFormat.Type.AU;
	        Recorder recorder = null;
	        RecorderInit(targetDataLine, targetType);
	        m_bRecording = true;
	        m_bQuitting = false;
	        this.start();
    	}
    }
    
    
    
    public void RecorderInit( TargetDataLine line, AudioFileFormat.Type targetType ) {
        m_line = line;
        m_audioInputStream = new AudioInputStream(line);
        m_targetType = targetType;
    }
    
    
    /**	Starts the recording.
    *thread is started.
     */
    public void start() {
        m_bRecording = true;
        m_line.start();
        super.start();
    }
    
    public void startRecording(){
    	m_bRecording = true;
    }
    
    public void stopRecording() {
        m_bRecording = false;
        onlyonce = true;
    }
    
    
    synchronized public void run() {
        
        int i = 0;
        
        while( !m_bQuitting ){
        	byte bs[] = new byte[bytesize];
            m_line.read( bs , 0 , bytesize );
        	if( m_bRecording ){
        		cs.writebyte( bs );
        	}else if( onlyonce ){
        		cs.writebyte( ("NT|").getBytes() );
        		onlyonce = false;	
        	}
        }
        
        m_line.stop();
        m_line.close();	
    }
    public void onExit(){
    	m_bQuitting = true;
    }
}

