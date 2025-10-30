package jvn.test;

import irc.Sentence;
import jvn.JvnObject;
import jvn.impl.JvnServerImpl;

import java.util.Random;

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

            JvnObject jvnObject = js.jvnLookupObject("COUNTER");

            if (jvnObject == null) {
                System.out.println("Looked up is not enough");
                Sentence sentenceObj = new Sentence();
                sentenceObj.write("0");
                jvnObject = js.jvnCreateObject(sentenceObj);
                js.jvnRegisterObject("COUNTER", jvnObject);
                // after creation, I have a write lock on the object
                jvnObject.jvnUnLock();
            }

            System.out.println("✓ Connecté au coordinateur et objet COUNTER récupéré");
            System.out.println();

            // Effectuer 20 itérations avec opérations aléatoires
            for (int i = 1; i <= 20; i++) {
                // Délai aléatoire entre 0 et 500ms
                int delay = random.nextInt(501);
                Thread.sleep(delay);

                // Choix aléatoire entre READ (0) et WRITE (1)
                boolean isWrite = random.nextBoolean();

                if (isWrite) {
                    // ============ OPÉRATION WRITE ============
                    int currentVal, newVal;
                    jvnObject.jvnLockWrite();

                    try {
                        // Lecture de la valeur actuelle directement depuis l'objet partagé
                        String currentStr = ((Sentence) jvnObject.jvnGetSharedObject()).read();
                        currentVal = Integer.parseInt(currentStr);
                        newVal = currentVal + 1;

                        // Écriture directement sur l'objet partagé
                        ((Sentence) jvnObject.jvnGetSharedObject()).write(String.valueOf(newVal));

                        writeCount++;
                    } finally {
                        jvnObject.jvnUnLock();
                    }

                    System.out.println(String.format("[PID:%d][Iter %2d] WRITE : %d → %d (délai: %dms)",
                        processId, i, currentVal, newVal, delay));

                } else {
                    // ============ OPÉRATION READ ============
                    jvnObject.jvnLockRead();

                    String value;
                    try {
                        // Lecture directement depuis l'objet partagé
                        value = ((Sentence) jvnObject.jvnGetSharedObject()).read();
                        readCount++;
                    } finally {
                        jvnObject.jvnUnLock();
                    }

                    System.out.println(String.format("[PID:%d][Iter %2d] READ  : %s (délai: %dms)",
                        processId, i, value, delay));
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            // Afficher le résumé final
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();
            System.out.println("✓ Process " + processId + " terminé");
            System.out.println("  Durée totale  : " + duration + "ms");
            System.out.println("  READs         : " + readCount);
            System.out.println("  WRITEs        : " + writeCount);
            System.out.println();

            // Afficher la valeur finale
            jvnObject.jvnLockRead();
            String finalValue = ((Sentence) jvnObject.jvnGetSharedObject()).read();
            jvnObject.jvnUnLock();

            System.out.println("  Valeur finale : " + finalValue);
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════");

            // Nettoyage
            js.jvnTerminate();

        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

