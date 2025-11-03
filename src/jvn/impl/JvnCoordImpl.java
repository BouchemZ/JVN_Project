package jvn.impl; /***
 * JAVANAISE Implementation
 * jvn.impl.JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */ 


import jvn.*;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord, Serializable {


  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private int nextObjectId = 1;
    private HashMap<String, JvnObject> nameObjects; // map name to object
    private HashMap<Integer, Serializable> shareObjects; // map id to shared object
    private HashMap<Integer, HashSet<JvnRemoteServer>> lockReaders; // map id to servers with read lock
    private HashMap<Integer, JvnRemoteServer> lockWriters; // map id to server with write lock

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		// to be completed
        super();
        this.nameObjects = new HashMap<>();
        this.shareObjects = new HashMap<>();
        this.lockReaders = new HashMap<>();
        this.lockWriters = new HashMap<>();
	}

    /**
     * to be run on its own thread
     * saves the coord as .ser file in the data directory
     */
    public void saveCoord() throws IOException{
        try {
            FileOutputStream fout = new FileOutputStream("data/COORD.ser");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(this);
            out.close();
        } catch (IOException e) {
            System.err.println("Failed to save state of the coordinator: " + e.getMessage());
        }
    }

    /**
     * try retrieving a .ser file if there is one
     */
    public static JvnCoordImpl loadCoord() throws IOException{
        try {
            FileInputStream fin = new FileInputStream("data/COORD.ser");
            ObjectInputStream in = new ObjectInputStream(fin);
            JvnCoordImpl coord = (JvnCoordImpl) in.readObject();
            in.close();
            return coord;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load state of the coordinator: " + e.getMessage());
            return null;
        }
    }
  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public synchronized int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    // to be completed
      return nextObjectId++;  // Increment and return unique ID
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public synchronized void jvnRegisterObject(String jon, JvnObject jo, int joi, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
      this.nameObjects.put(jon, jo);
      this.shareObjects.put(joi, jo.jvnGetSharedObject());
      this.lockWriters.put(joi, js);
      this.lockReaders.put(joi,new HashSet<JvnRemoteServer>());
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
      return this.nameObjects.get(jon);
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException,JvnException
  **/
   public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{

       JvnRemoteServer writer = this.lockWriters.get(joi);
       if(writer != null) {
           Serializable newState = null;
           int counter = 0;
           while (newState == null && counter < 10) {
               try {
                   newState = writer.jvnInvalidateWriterForReader(joi);
                   this.lockReaders.get(joi).add(writer);
               } catch (RemoteException e) {
                   System.err.println("Writer unreachable (try " + (counter+1) + "/10) :" + e.getMessage());
               } catch (JvnException e) {
                   throw new JvnException(e.getMessage());
               }
               counter++;
           }
           if (newState != null) {
               this.shareObjects.put(joi, newState);
           } else {
               System.err.println("Writer definitely unreachable");
               this.lockWriters.remove(joi);
           }
           this.lockWriters.remove(joi);
       }

       this.lockReaders.get(joi).add(js);

       return this.shareObjects.get(joi);
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException,JvnException
  **/
   public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{

      HashSet<JvnRemoteServer> readers = this.lockReaders.get(joi);
      if (readers != null) {
          HashSet<JvnRemoteServer> deadReaders = new HashSet<>();
          for (JvnRemoteServer reader : readers) {
              int counter = 0;
              boolean success = false;
              while (!success && counter < 10) {
                  try {
                      reader.jvnInvalidateReader(joi);
                      success = true;
                  } catch (RemoteException e) {
                      System.err.println("Reader unreachable (try " + (counter+1) + "/10) : " + e.getMessage());
                      counter++;
                  }
              }
              if (!success) {
                  System.err.println("Reader definitely unreachable");
              }
          }
          readers.clear();
      }

      JvnRemoteServer currentWriter = this.lockWriters.get(joi);
      if (currentWriter != null) {
          Serializable newState = null;
          int counter = 0;
          while (newState == null && counter < 10) {
              try {
                  newState = currentWriter.jvnInvalidateWriter(joi);
              } catch (RemoteException e) {
                  System.err.println("Writer unreachable (try " + (counter+1) + "/10) : " + e.getMessage());
              } catch (JvnException e) {
                  throw new JvnException(e.getMessage());
              }
              counter++;
          }
          if (newState != null) {
              this.shareObjects.put(joi, newState);
          } else {
                System.err.println("Writer definitely unreachable");
          }
          this.lockWriters.remove(joi);
      }

      this.lockWriters.put(joi, js);

       return this.shareObjects.get(joi);
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException,JvnException
	**/
    public synchronized void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 // to be completed
        for (Integer joi : this.lockWriters.keySet()){

            if (js != null && js.equals(this.lockWriters.get(joi))){
                this.shareObjects.put(joi, js.jvnInvalidateWriter(joi));
                this.lockWriters.put(joi,null);
            }
        }

        for (Integer joi : this.lockReaders.keySet()){
            this.lockReaders.get(joi).remove(js);
        }
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);

            JvnCoordImpl coord = JvnCoordImpl.loadCoord();

            if(coord == null) coord = new JvnCoordImpl();

            Naming.rebind("COORD", coord);

            JvnCoordImpl finalCoord = coord;

            Thread saveThread = new Thread() {
                public void run() {
                    try {
                        while (true) {

                            finalCoord.saveCoord();

                            Thread.sleep(1000);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            saveThread.start();
            System.out.println("Coordinateur lancé");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


