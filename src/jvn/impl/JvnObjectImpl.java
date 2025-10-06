package jvn.impl;

import jvn.*;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
    private static final long serialVersionUID = 1L;
    private int jvnObjectId;
    private Serializable sharedObject;
    private JvnLocalServer localServer;

    private LockState currentLockState = LockState.NL;

    public JvnObjectImpl(int id, Serializable obj, JvnLocalServer server) {
        this.jvnObjectId = id;
        this.sharedObject = obj;
        this.localServer = server;
    }


    /**
     * Get a Read lock on the shared object
     *
     * @throws JvnException
     **/
    @Override
    public void jvnLockRead() throws JvnException {
        switch (currentLockState) {
            case NL:
                // No lock - need to acquire read lock from coordinator
                sharedObject = localServer.jvnLockRead(jvnObjectId);
                currentLockState = LockState.R;
                break;

            case RC:
                // Have cached read lock - just transition to taken
                currentLockState = LockState.R;
                break;

            case WC:
                // Have cached write lock - transition to read with write cached
                currentLockState = LockState.RWC;
                break;

            case R:
                // error maybe ?
            case W:
                // error maybe
            case RWC:
                // Already have the lock we need
                break;
        }
    }

    /**
     * Get a Write lock on the object
     *
     * @throws JvnException
     **/
    @Override
    public void jvnLockWrite() throws JvnException {
        switch (currentLockState) {
            case NL:
            case RC:
                // No lock - need to acquire read lock from coordinator
                sharedObject = localServer.jvnLockWrite(jvnObjectId);
                currentLockState = LockState.W;
                break;

            case WC:
                // Have cached write lock - transition to read with write cached
                currentLockState = LockState.W;
                break;

            case W:
            case R:
            case RWC:
                // Already have the lock we need
                break;
        }
    }

    /**
     * Unlock the object
     *
     * @throws JvnException
     **/
    @Override
    public void jvnUnLock() throws JvnException {
        switch (currentLockState) {
            case R:
                // release read lock - keep cached
                currentLockState = LockState.RC;
            case W:
                currentLockState = LockState.WC;
            case RWC:
            case WC:
            case NL:
            case RC:
                // Already have the lock we need
                break;
        }
        notifyAll();
    }

    /**
     * Get the object identification
     *
     * @throws JvnException
     **/
    @Override
    public int jvnGetObjectId() throws JvnException {
        return jvnObjectId;
    }

    /**
     * Get the shared object associated with this JvnObject
     *
     * @throws JvnException
     **/
    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return sharedObject;
    }

    /**
     * Invalidate the Read lock of the JVN object
     *
     * @throws JvnException
     **/
    @Override
    public void jvnInvalidateReader() throws JvnException {
        while(currentLockState == LockState.R || currentLockState == LockState.RWC)
        {
            try {
                wait();
            }catch (InterruptedException e) {
                throw new JvnException("Interrupted while waiting to invalidate reader ");
            }
        }
        if(currentLockState == LockState.RC) currentLockState = LockState.NL;
    }

    /**
     * Invalidate the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException
     **/
    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        while(currentLockState == LockState.W || currentLockState == LockState.RWC)
        {
            try {
                wait();
            }catch (InterruptedException e) {
                throw new JvnException("Interrupted while waiting to invalidate writer");
            }
        }
        if(currentLockState == LockState.WC) currentLockState = LockState.NL;
        return sharedObject;
    }

    /**
     * Reduce the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException
     **/
    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        while(currentLockState == LockState.W || currentLockState == LockState.RWC)
        {
            try {
                wait();
            }catch (InterruptedException e) {
                throw new JvnException("Interrupted while waiting to invalidate writer");
            }
        }
        if(currentLockState == LockState.WC) currentLockState = LockState.RC;
        return sharedObject;
    }
}
