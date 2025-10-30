package jvn.test;

import irc.Sentence;
import jvn.JvnObject;
import jvn.LockState;
import jvn.impl.JvnObjectImpl;
import jvn.impl.JvnServerImpl;

/**
 * Test simple : vérifie que les read attendent les write avec JvnObjectImpl directement
 */
public class SimpleTest {

    public static void main(String[] args) {
        System.out.println("=== TEST JVN (Direct sur JvnObjectImpl) ===\n");
        System.out.println("Scenario: 1 write long + 3 reads simultanes");
        System.out.println("Les reads doivent attendre le write et lire le dernier message\n");

        try {
            JvnServerImpl jsMain = JvnServerImpl.jvnGetServer();

            // Créer un objet JVN directement sans passer par le proxy
            Sentence sentenceObj = new Sentence();
            JvnObject mainObject = jsMain.jvnCreateObject(sentenceObj);
            jsMain.jvnRegisterObject("IRC", mainObject);

            final boolean[] writeFinished = new boolean[1];
            final int[] successCount = new int[1];

            // 1 Writer qui ecrit (le verrou garde 2 secondes grace au delai dans jvnLockWrite)
            Thread writer = new Thread(() -> {
                try {
                    JvnServerImpl js = JvnServerImpl.jvnGetServer();
                    JvnObject jvnObject = js.jvnLookupObject("IRC");

                    System.out.println("[WRITE] Debut lock write...");
                    jvnObject.jvnLockWrite(); // Le delai de 2 sec est ici

                    System.out.println("[WRITE] Lock acquis, ecriture du message...");
                    ((Sentence) jvnObject.jvnGetSharedObject()).write("DERNIER_MESSAGE");

                    Thread.sleep(2000);
                    System.out.println("[WRITE] Unlock...");
                    jvnObject.jvnUnLock();

                    writeFinished[0] = true;
                    System.out.println("[WRITE] Fin ecriture\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 3 Readers qui tentent de lire pendant le write
            Thread[] readers = new Thread[3];
            for (int i = 0; i < 3; i++) {
                final int id = i;
                readers[i] = new Thread(() -> {
                    try {
                        JvnServerImpl js = JvnServerImpl.jvnGetServer();
                        JvnObject jvnObject = js.jvnLookupObject("IRC");

                        Thread.sleep(500); // Laisse le write demarrer
                        System.out.println("[READ-" + id + "] Tentative de lecture...");
                        long start = System.currentTimeMillis();

                        jvnObject.jvnLockRead();
                        String text = ((Sentence) jvnObject.jvnGetSharedObject()).read();
                        jvnObject.jvnUnLock();

                        long duration = System.currentTimeMillis() - start;

                        System.out.println("[READ-" + id + "] Lu: '" + text + "' (attente: " + duration + "ms)");

                        if (writeFinished[0] && "DERNIER_MESSAGE".equals(text)) {
                            successCount[0]++;
                        } else {
                            System.err.println("[READ-" + id + "] ERREUR!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            writer.start();
            for (Thread r : readers) r.start();

            writer.join();
            for (Thread r : readers) r.join();

            System.out.println("\n=== RESULTAT ===");
            if (successCount[0] == 3) {
                System.out.println("✓✓✓ TEST REUSSI ✓✓✓");
                System.out.println("Les 3 reads ont attendu le write et lu le bon message!");
            } else {
                System.err.println("✗ TEST ECHOUE ✗");
                System.err.println("Seulement " + successCount[0] + "/3 reads corrects");
            }

            jsMain.jvnTerminate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

