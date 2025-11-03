package jvn.test;

import irc.ISentence;
import irc.Sentence;
import jvn.JvnObject;
import jvn.impl.JvnObjectProxy;
import jvn.impl.JvnServerImpl;

import static java.lang.System.exit;

/**
 * Test simple : vérifie que les read attendent les write avec JvnObjectImpl directement
 */
public class SimpleWriter {

    public static void main(String[] args) {

        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            JvnObject jo = js.jvnLookupObject("SIMPLE");

            if (jo == null) {
                jo = js.jvnCreateObject(new Sentence());
                js.jvnRegisterObject("SIMPLE", jo);
                // after creation, I have a write lock on the object
                jo.jvnUnLock();
            }

            System.out.println("[WRITE] Debut lock write...");
            jo.jvnLockWrite();
            System.out.println("[WRITE] Lock acquis, ecriture du message...");
            ((Sentence) jo.jvnGetSharedObject()).write("INITIAL_MESSAGE");
            System.out.println("[WRITE] Wait 10 secondes");
            Thread.sleep(10000);
            System.out.println("[WRITE] Unlock...");
            jo.jvnUnLock();
            System.out.println("[WRITE] Fin ecriture\n");

            js.jvnTerminate();
            exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }
}

