package jvn.test;

import irc.ISentence;
import irc.Sentence;
import jvn.impl.JvnObjectProxy;
import jvn.impl.JvnServerImpl;

import static java.lang.System.exit;

/**
 * Test simple : vérifie que les read attendent les write avec JvnObjectImpl directement
 */
public class SimpleReader {

    public static void main(String[] args) {

        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();

            // Créer un objet JVN directement sans passer par le proxy
            ISentence sentence = (ISentence) JvnObjectProxy.newInstance("SIMPLE", new Sentence(), js);
            System.out.println("[READ] Tentative de lecture...");

            long start = System.currentTimeMillis();
            String text = sentence.read();
            long duration = System.currentTimeMillis() - start;

            System.out.println("[READ-] Lu: '" + text + "' (attente: " + duration + "ms)");


            js.jvnTerminate();
            exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }
}

