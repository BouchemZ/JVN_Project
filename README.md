# Comment exécuter et tester le projet Javanaise 

## Compilation du projet

Avant de lancer le projet, il faut compiler les sources :

```bash
javac -d bin -sourcepath src src\jvn\impl\*.java src\jvn\*.java src\irc\*.java
```

## Lancement du projet 

Pour lancer le coordinateur et trois machines IRC, nous avons créé un script bash qui démarre tout automatiquement. Le script est situé dans le dossier `bash/run.sh`.

```bash
bash bash/run.sh
```

Le script va :
1. Démarrer le coordinateur JVN
2. Attendre 5 secondes que le coordinateur soit prêt
3. Lancer 3 instances IRC l'une après l'autre
4. Vous permettre d'arrêter tous les processus avec Ctrl+C

## Lancement des tests

### Test de write et read avec Timeout

### Stress Test