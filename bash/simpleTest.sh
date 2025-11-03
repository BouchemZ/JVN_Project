#!/bin/bash

# Fonction pour nettoyer les processus en arrière-plan à la fermeture
cleanup() {
    echo "Arrêt des processus..."
    kill $(jobs -p) 2>/dev/null
    exit
}

trap cleanup SIGINT SIGTERM

# Compilation du projet
echo "=========================================="
echo "Compilation du projet..."
echo "=========================================="

# Compiler tous les fichiers Java dans le bon ordre
# D'abord les interfaces et classes de base
javac -encoding UTF-8 -d bin -cp bin src/jvn/*.java
# Puis irc
javac -encoding UTF-8 -d bin -cp bin src/irc/*.java
# Puis les implémentations
javac -encoding UTF-8 -d bin -cp bin src/jvn/impl/*.java
# Enfin les tests qui dépendent de tout le reste
javac -encoding UTF-8 -d bin -cp bin src/jvn/test/*.java

if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation"
    exit 1
fi

echo "Compilation réussie!"
echo ""

# Lancement du coordinateur
echo "Démarrage du coordinateur..."
java -cp ./bin jvn.impl.JvnCoordImpl &
COORD_PID=$!

# Attendre que le coordinateur soit prêt
echo "Attente du démarrage du coordinateur..."
sleep 5

# Vérifier que le coordinateur est toujours en cours d'exécution
if ! ps -p $COORD_PID > /dev/null 2>&1; then
    echo "Erreur: Le coordinateur n'a pas démarré correctement"
    exit 1
fi

echo "Coordinateur démarré (PID: $COORD_PID)"

# Lancement des trois machines IRC
echo "Démarrage du write..."
java -cp ./bin jvn.test.SimpleWriter &

sleep 2

echo "Démarrage du read..."
java -cp ./bin jvn.test.SimpleReader &

# Attendre indéfiniment
wait

