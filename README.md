# Comment exécuter et tester le projet Javanaise

## Lancement du projet 

Pour lancer le coordinateur et trois machines IRC, nous avons créé un script bash qui compile puis exécute les fichiers automatiquement. Le script est situé dans le dossier `bash/run.sh`.

```bash
bash bash/run.sh
```

Le script va :
1. Démarrer le coordinateur JVN
2. Attendre 5 secondes que le coordinateur soit prêt
3. Lancer 3 instances IRC l'une après l'autre
4. Vous permettre d'arrêter tous les processus avec Ctrl+C

Pour terminer les clients proprement, vous pouvez appuyer sur la croix "x", cela fermera le client en appelant la fonction terminate.

## Lancement des tests

### Test de write et read avec Timeout

Nous avons créé un test simple qui lance un write puis attend 10 secondes avant de unlock, et entre temps nous lançons un read pour vérifier qu'il attend la fin du write pour lire le message.

Pour lancer ce test, il y a un script bash que vous pouvez lancer depuis la racine du projet :

```bash
bash bash/simpleTest.sh
```

### Stress Test

Pour le stress test, nous lançons trois clients simultanément qui font chacun 500 itérations dans lesquels ils vont aléatoirement faire soit un read soit un write. Pour lancer ce test, il y a un script bash :
```bash
bash bash/stressTest.sh
```

En suivant les logs sortis par ce test, on peut voir qu'il y a une cohérence sur le read et write qui se suivent, qu'il n'y en pas qui sont écrasés.

## Extensions

### Extension 1 : Gestion des pannes clients

Pour la gestion des pannes clients, nous avons simplement ajouté une récupération des erreurs sur les appels d'invalidate de lock clients. Nous vérifier qu'il renvoie bien un objet non null et si ce n'est pas le cas, on loop 10 fois pour bien vérifier qu'il est en panne.

Nous n'avons pas gérer l'enregistrement des clients morts auprès du coordinateur par manque de temps, mais nous gérons simplement que le système ne soit pas cassé si un client tombe en panne. 

Vous pouvez tester cette foncitonnalité avec le bouton kill que nous avons ajouté sur l'interface, ce bouton arrête le programme sans le terminer proprement.

### Extension 2 : Gestion des pannes Coordinateurs

Pour la gestion des pannes coordinateurs, lors de l'instanciation du coordinateur, celui-ci cherche l'existence du COORD.ser (un état serializé de celui-ci). Si il existe alors le coordinateur copie ce état, sinon il se lance avec un état "vierge".

Apres l'instanciation et le possible load, se lance une thread qui a pour but d'écrire toutes les 10 secondes l'état du cordinateur pour de futur recovery.

Cette gestion n'est pas cohérente. On trouvera dans le codes une ébauche de fonction qui cherche à sauvegarder/load chaque champs du coordinateur, la fonction de sauvegarde serait à appeler lors de chaque modification de données critique du coordinateur de manière à pouvoir plus tard faire une récupération cohérente.