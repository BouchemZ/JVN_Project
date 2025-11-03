package jvn.test;

import irc.ISentence;
import irc.Sentence;
import jvn.JvnObject;
import jvn.impl.JvnObjectProxy;
import jvn.impl.JvnServerImpl;

import java.util.Random;

import static java.lang.System.exit;

/**
 * Stress Test JVN : Test de cohérence avec opérations concurrentes aléatoires
 *
 * UTILISATION :
 * Lancez cette classe 3 fois en parallèle dans 3 terminaux différents :
 *   Terminal 1 : java jvn.test.StressTest
 *   Terminal 2 : java jvn.test.StressTest
 *   Terminal 3 : java jvn.test.StressTest
 *
 * Principe :
 * - Chaque processus effectue 20 itérations
 * - À chaque itération : choix aléatoire entre READ ou WRITE
 * - Délai aléatoire entre 0 et 500ms entre chaque opération
 * - Un compteur est incrémenté à chaque WRITE
 * - Validation : Regardez les logs pour vérifier la cohérence
 */
public class StressTest {

    public static void main(String[] args) {
        // Identifier ce processus avec un ID unique
        long processId = ProcessHandle.current().pid();

        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║         STRESS TEST JVN - Opérations Aléatoires          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Process ID    : " + processId);
        System.out.println("  Itérations    : 20");
        System.out.println("  Opérations    : READ ou WRITE (aléatoire)");
        System.out.println("  Délai         : 0-500ms (aléatoire)");
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println();

        Random random = new Random();
        int readCount = 0;
        int writeCount = 0;
        long startTime = System.currentTimeMillis();

        try {
            // Chaque processus obtient son propre serveur JVN (simule une machine différente)
            JvnServerImpl js = JvnServerImpl.jvnGetServer();

            ISentence sentence = (ISentence) JvnObjectProxy.newInstance("COUNTER", new Sentence(), js);

            System.out.println("✓ Connecté au coordinateur et objet COUNTER récupéré");
            System.out.println();

            // Effectuer 20 itérations avec opérations aléatoires
            for (int i = 1; i <= 500; i++) {
                // Délai aléatoire entre 0 et 500ms
                 int delay = 5 ; //random.nextInt(501);
                 Thread.sleep(delay);

                // Choix aléatoire entre READ (0) et WRITE (1)
                boolean isWrite = random.nextBoolean();

                if (isWrite) {
                    // ============ OPÉRATION WRITE ============
                    String s = String.format("%dW%04d", processId, i);
                    sentence.write(s);

                    System.out.printf("[PID:%d][Iter %04d] WRITE : %s%n",
                        processId, i, s);

                } else {
                    // ============ OPÉRATION READ ============
                    String s = sentence.read();

                    System.out.printf("[PID:%d][Iter %04d] READ  : %s%n",
                        processId, i, s);
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            // Afficher le résumé final
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();
            System.out.println("✓ Process " + processId + " terminé");
            System.out.println("  Durée totale  : " + duration + "ms");
            System.out.println();

            // Afficher la valeur finale
            String finalValue = sentence.read();

            System.out.println("  Valeur finale : " + finalValue);
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════");

            // Nettoyage
            js.jvnTerminate();
            exit(0);

        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
            exit(1);
        }
    }
}

