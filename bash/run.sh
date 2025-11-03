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
javac -d bin -cp bin src/jvn/*.java src/jvn/impl/*.java src/irc/*.java

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
echo "Démarrage de la machine IRC 1..."
java -cp ./bin irc.Irc &

sleep 2

echo "Démarrage de la machine IRC 2..."
java -cp ./bin irc.Irc &

sleep 2

echo "Démarrage de la machine IRC 3..."
java -cp ./bin irc.Irc &

echo ""
echo "=========================================="
echo "Toutes les instances sont lancées!"
echo "- Coordinateur (PID: $COORD_PID)"
echo "- 3 machines IRC"
echo "=========================================="
echo "Appuyez sur Ctrl+C pour arrêter tous les processus"
echo ""

# Attendre indéfiniment
wait

