package jvn.impl; /***
 * JAVANAISE Implementation
 * jvn.impl.JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */ 


import jvn.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord {
	

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
  public void jvnRegisterObject(String jon, JvnObject jo, int joi, JvnRemoteServer js)
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
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
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
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{

       JvnRemoteServer writer = this.lockWriters.get(joi);
       if(writer != null) {
           this.shareObjects.put(joi, writer.jvnInvalidateWriterForReader(joi));
           this.lockWriters.remove(joi);
           this.lockReaders.get(joi).add(writer);
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
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{

       HashSet<JvnRemoteServer> readers = this.lockReaders.get(joi);
       if (readers != null) {
           for (JvnRemoteServer reader : readers) {
               reader.jvnInvalidateReader(joi);
           }
           readers.clear();
       }

       if (this.lockWriters.get(joi) != null) {
           this.shareObjects.put(joi, this.lockWriters.get(joi).jvnInvalidateWriter(joi));
       }

       this.lockWriters.put(joi, js);

       return this.shareObjects.get(joi);
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException,JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 // to be completed
        for (Integer joi : this.lockWriters.keySet()){
            if (this.lockWriters.get(joi) == js){
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

            JvnCoordImpl coord = new JvnCoordImpl();

            Naming.rebind("COORD", coord);

            System.out.println("Coordinateur lancé");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


