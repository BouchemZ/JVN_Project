/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn.impl;

import jvn.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.HashMap;


public class JvnServerImpl
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer {
	
  /**
	 * 
	 */
	// A JVN server is managed as a singleton  
	private static JvnServerImpl js = null;

    // reference to coord
    private final JvnRemoteCoord coordinator;

    // map id to object
    private HashMap<Integer, JvnObjectImpl> jvnObjects; // dict of all objects and their id(key)

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		// to be completed
        try
        {
            coordinator = (JvnRemoteCoord) Naming.lookup("COORD");
        } catch (Exception e){
            throw new JvnException("Cannot connect to coordinator: " + e.getMessage());
        }

        jvnObjects = new HashMap<>();
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() throws JvnException{
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				throw new JvnException("Failed to initialize JVN Server: " + e.getMessage());
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws JvnException {
    // to be completed
        try
        {
         coordinator.jvnTerminate(this);
        }catch (Exception e){
            throw new JvnException("Terminate failed: " + e.getMessage());
        }
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public JvnObject jvnCreateObject(Serializable o)
	throws JvnException {
		// to be completed
        try
        {
            int joi = coordinator.jvnGetObjectId();
            JvnObjectImpl jo =  new JvnObjectImpl(joi,o,this);
            jvnObjects.put(joi, jo);
            return jo;
        } catch (Exception e){
            throw new JvnException("CreateObject failed: "+ e.getMessage());
        }
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws JvnException {
		// to be completed
        try
        {
            coordinator.jvnRegisterObject(jon, jo, jo.jvnGetObjectId(),this);
        } catch (Exception e){
            throw new JvnException("RegisterObject failed: "+ e.getMessage());
        }
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws JvnException {
    // to be completed
        try
        {
            return coordinator.jvnLookupObject(jon,this);
        }catch (Exception e) {
            throw new JvnException("Lookup of jon: " + jon + "Failed." + e.getMessage());
        }
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
		// to be completed
       try
       {
           return coordinator.jvnLockRead(joi,this);
       }catch (Exception e){
           throw new JvnException("Failed acquiring read lock :" + e.getMessage());
       }
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		// to be completed 
       try
       {
           return coordinator.jvnLockWrite(joi,this);
       }catch (Exception e){
           throw new JvnException("Failed acquiring write lock :" + e.getMessage());
       }
    }

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,JvnException {
		// to be completed
      try
      {
          JvnObject jo = jvnObjects.get(joi);
          jo.jvnInvalidateReader();
      }catch (Exception e){
          throw new JvnException("Failed InvalidateReader :" + e.getMessage());
      }
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,JvnException {
		// to be completed 
      try
      {
          JvnObject jo = jvnObjects.get(joi);
          return jo.jvnInvalidateWriter();
      }catch (Exception e){
          throw new JvnException("Failed InvalidateWriter :" + e.getMessage());
      }
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,JvnException {
		// to be completed 
       try
       {
           JvnObject jo = jvnObjects.get(joi);
           return jo.jvnInvalidateWriterForReader();
       }catch (Exception e){
           throw new JvnException("Failed InvalidateWriterForReader :" + e.getMessage());
       }
	 };
}

 
