package ext.appinventor.ClientTCP.ClientTCP;

/**
 * Simple TCP Client to send `one-shot` commands to remote server and get its reply
 * @author aluis.rcastro@bol.com.br
 * @Date 2019.05.22
 *
 * Copyright (c) 2019 andre luis ramos de castro
 *
 *  ### This code is provided "as-is", which means no implicit or explicit warranty ###
 */
 
//
//   Required copyright notices:
//   --------------------------
//   Copyright 2009-2011 Google, All Rights reserved
//   Copyright 2011-2012 MIT, All rights reserved
//


import com.google.appinventor.components.runtime.*;
 
import android.os.Environment;
import android.os.AsyncTask;
import android.os.StrictMode;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import javax.swing.*;  
import javax.swing.JOptionPane;

import com.google.appinventor.components.runtime.util.RuntimeErrorAlert;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.SdkLevel;
 
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import java.io.*;
import java.io.ByteArrayOutputStream; 
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.InetAddress;

import java.lang.Enum;

import java.util.concurrent.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * General config parameters
 */
@DesignerComponent(version = 1,
 description = "Non-visible component that provides client tcp connectivity.",
 category = ComponentCategory.EXTENSION,
 nonVisible = true,
 iconName = "TCP.png")
@SimpleObject(external = true)
// @UsesLibraries(libraries = "")
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
        "android.permission.CHANGE_NETWORK_STATE," +
        "android.permission.ACCESS_WIFI_STATE," +
        "android.permission.ACCESS_NETWORK_STATE," +
        "android.permission.WRITE_EXTERNAL_STORAGE," +
        "android.permission.READ_EXTERNAL_STORAGE," +         
        "android.permission.WRITE_SETTINGS," +
        "android.permission.WRITE_SYNC_SETTINGS," +
        "android.permission.PERSISTENT_ACTIVITY," +
        "android.permission.CHANGE_CONFIGURATION," +
        "android.permission.READ_PHONE_STATE")        

public class ClientTCP extends AndroidNonvisibleComponent implements Component
{
    private static final 	String LOG_TAG 		= "ClientTCP";
    private String   		sReceivedMessage    = "";       // Stores the message just got from HTTP
    private String   		sDebugText       	= "";       // Auxiliary variable, useful whenever debugging issues
    public  boolean  		bNewDataFromHost    = false;      // Allows interaction between independent classes
	public  int				iConnectionStatus;
	public  String 			servermessage 		= "" ;
	
	public static final int iCommIDLE 			= 0;
	public static final int iCommConnecting		= 1;
	public static final int iCommConnected		= 2;
	public static final int iCommDisconnected	= 3;	
	public static final int iCommTimeout		= 4;	
	public static final int iCommError			= 5;	

    private final Activity 	activity;
    InputStream 			inputStream 		= null;
	private Thread 			thCurrentThread;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates a new Client SSH component.
     *
     * @param container the Form that this component is contained in.
     */
    public ClientTCP(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);  
    }

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Methods that return Communication statuses
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "IDLE state")
    public int IDLE() {
        return iCommIDLE;
    }
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Connecting state")
    public int CONNECTING() {
        return iCommConnecting;
    }
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Connected state")
    public int CONNECTED() {
        return iCommConnected;
    }
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Disconnected state")
    public int DISCONNECTED() {
        return iCommDisconnected;
    }
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Timeout state")
    public int TIMEOUT() {
        return iCommTimeout;
    }	
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Error state")
    public int ERROR() {
        return iCommError;
    }	
	
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Get state of the connection")
    public int GetConnectionStatus() {
        return iConnectionStatus;
    }
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Set state of the connection")
    public void SetConnectionStatus(int status) {
        iConnectionStatus = status;
    } 	
		
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Send cmd to the server
     */
    @SimpleFunction(description = "Send cmd to the server")
    public void SendData( final String sndIP , final int sndPort , final String sndValue, final int sndSocketTimeout  ) {
        if ( GetConnectionStatus() == CONNECTED() ) {
			NewStatus( CONNECTED() );
			}
		else {
			try { 
				SetConnectionStatus ( CONNECTING() );
				NewStatus( GetConnectionStatus() );
			
				AsynchUtil.runAsynchronously(new Runnable() {
					@Override
					public void run() {  
						try {    
						
								Socket socket 						= new Socket( sndIP, sndPort );
								socket.setSoTimeout( sndSocketTimeout*1000 );				
								InputStream inputstream 			= socket.getInputStream();
								InputStreamReader inputstreamreader = new InputStreamReader(inputstream); 
								BufferedReader bufferreader 		= new BufferedReader(inputstreamreader);
								OutputStream outputStream 			= socket.getOutputStream();
								
								PrintWriter printwriter 			= new PrintWriter(outputStream,true);                            
								printwriter.println( sndValue );
								printwriter.flush();
																								
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										SetConnectionStatus( CONNECTED() );
										NewStatus( GetConnectionStatus() );
										}
									} 
								);
								
								// TODO: replace blocking method 'readLine()' by 'read()' in order
								// to provide a means to control timeout fo incoming characters
								
								servermessage = bufferreader.readLine();
								SetReceivedMessage( servermessage );
								
								activity.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											NewIncomingMessage( GetReceivedMessage() );
											SetConnectionStatus ( DISCONNECTED() );
											NewStatus( GetConnectionStatus() );
											}
										} 	
									);
								SetConnectionStatus ( IDLE() );
								if (socket != null)
									socket.close();								
						   } 
						   catch ( Exception e ) {
							   	SetReceivedMessage ( e.getMessage() );
								Log.e(LOG_TAG, "S: Error", e);
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										SetConnectionStatus ( ERROR() );
										NewStatus( GetConnectionStatus() );
										NewIncomingMessage( GetReceivedMessage() );
										}
									} 
								);
								SetConnectionStatus ( IDLE() );
							}
					}
                });
            }
        catch(Exception err){
                SetReceivedMessage(err.getMessage());
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						SetConnectionStatus ( ERROR() );
						NewStatus( GetConnectionStatus() );
						NewIncomingMessage( GetReceivedMessage() );
						}
					} 
				);
		SetConnectionStatus ( IDLE() );
        }   
     }
} 

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Events indicating that there is available new text line and change of status
     *
     */
	@SimpleEvent
    public void NewIncomingMessage(String msgIn)
    {
        // invoke the application's "NewIncomingMessage" event handler.
        EventDispatcher.dispatchEvent(this, "NewIncomingMessage", msgIn);
    }
	
	@SimpleEvent
    public void NewStatus(int iStatus)
    {
        // invoke the application's "NewStatus" event handler.
        EventDispatcher.dispatchEvent(this, "NewStatus", iStatus);
    }
  
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method that returns Debug message Text
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Get Debug Text")
    public String GetDebugText() {
        return sDebugText;
    }
    
    /**
     * Method that set Debug message Text
     */
@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Set Debug Text") 
    public void SetDebugText(String text) {
        sDebugText = text;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method that returns the Text from remote server
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "read text from remote HTTP server")
    public String GetReceivedMessage() {
        return sReceivedMessage;
    }

    /**
     * Method that define Text to remote server
     */
    public void SetReceivedMessage(String text) {
        sReceivedMessage = text;
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method that returns status of reception
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "returns status of reception")
    public boolean NewDataAvailable() {
        return bNewDataFromHost;
    }

    /**
     * Method that define status of reception
     */
    public void DataAvailablility(boolean status) {
        bNewDataFromHost = status;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
  