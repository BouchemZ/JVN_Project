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

### Stress Test

## Extensions

### Extension 1 : Gestion des pannes clients

Pour la gestion des pannes clients, nous avons simplement ajouté une récupération des erreurs sur les appels d'invalidate de lock clients. Nous vérifier qu'il renvoie bien un objet non null et si ce n'est pas le cas, on loop 10 fois pour bien vérifier qu'il est en panne.

Nous n'avons pas gérer l'enregistrement des clients morts auprès du coordinateur par manque de temps, mais nous gérons simplement que le système ne soit pas cassé si un client tombe en panne. 

Vous pouvez tester cette foncitonnalité avec le bouton kill que nous avons ajouté sur l'interface, ce bouton arrête le programme sans le terminer proprement.