package jvn.impl;

import jvn.*;

import java.io.Serializable;
import java.util.Locale;

public class JvnObjectImpl implements JvnObject {
    private static final long serialVersionUID = 1L;
    private int jvnObjectId;
    private Serializable sharedObject;
    private transient JvnLocalServer localServer;
    private LockState currentLockState;

    public JvnObjectImpl(int id, Serializable obj, JvnLocalServer server, LockState initialState) {
        this.jvnObjectId = id;
        this.sharedObject = obj;
        this.localServer = server;
        this.currentLockState = initialState;
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
            case W:
            case RWC:
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
        synchronized (this) {
            switch (currentLockState) {
                case R:
                    currentLockState = LockState.RC;
                    break;
                case W:
                case RWC:
                    currentLockState = LockState.WC;
                    break;
                case WC:
                case NL:
                case RC:
                    break;
            }
            notifyAll();
        }
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
        synchronized (this) {
            while(currentLockState == LockState.R || currentLockState == LockState.RWC) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new JvnException("Interrupted while waiting to invalidate reader ");
                }
            }
            if(currentLockState == LockState.RC) currentLockState = LockState.NL;
        }
    }

    /**
     * Invalidate the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException
     **/
    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        synchronized (this) {
            while(currentLockState == LockState.W || currentLockState == LockState.RWC) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new JvnException("Interrupted while waiting to invalidate writer");
                }
            }
            if(currentLockState == LockState.WC) currentLockState = LockState.NL;
            return sharedObject;
        }
    }

    /**
     * Reduce the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException
     **/
    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        synchronized (this) {
            while(currentLockState == LockState.W || currentLockState == LockState.RWC) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new JvnException("Interrupted while waiting to invalidate writer");
                }
            }
            if(currentLockState == LockState.WC) currentLockState = LockState.RC;
            return sharedObject;
        }
    }
}
