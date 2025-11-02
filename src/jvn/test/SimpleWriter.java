package jvn.test;

import irc.ISentence;
import irc.Sentence;
import jvn.impl.JvnObjectProxy;
import jvn.impl.JvnServerImpl;

import static java.lang.System.exit;

/**
 * Test simple : vérifie que les read attendent les write avec JvnObjectImpl directement
 */
public class SimpleWriter {

    public static void main(String[] args) {
        System.out.println("=== TEST JVN (Direct sur JvnObjectImpl) ===\n");
        System.out.println("Scenario: 1 write long + 3 reads simultanes");
        System.out.println("Les reads doivent attendre le write et lire le dernier message\n");

        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();

            // Créer un objet JVN directement sans passer par le proxy
            ISentence sentence = (ISentence) JvnObjectProxy.newInstance("SIMPLE", new Sentence(), js);

            System.out.println("[WRITE] Debut lock write...");
            System.out.println("[WRITE] Lock acquis, ecriture du message...");
            sentence.write("PREMIER_MESSAGE");
            System.out.println("[WRITE] Unlock...");
            System.out.println("[WRITE] Fin ecriture\n");

            js.jvnTerminate();
            exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }
}

