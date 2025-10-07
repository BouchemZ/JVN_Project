# Notes sur les points à revoir ou à confirmer

## Ajout de transient sur le localServer

Dans la classe JvnObjectImpl, l'attribut localServer est marqué comme transient. Cela signifie qu'il ne sera pas sérialisé lorsque l'objet JvnObjectImpl sera sérialisé. Il est important de vérifier que cela est intentionnel et que le comportement attendu est bien celui-ci. Si localServer doit être recréé ou réinitialisé après la désérialisation, il faut s'assurer que cela est bien géré dans le code.

À voir dans ce [fichier](./src/jvn/impl/JvnObjectImpl.java)

## Schema des interactions

```mermaid
sequenceDiagram
    participant App as Application (Sentence)
    participant Obj as JvnObjectImpl
    participant Srv as JvnServerImpl
    participant Coord as JvnCoordImpl

    App->>Obj: appel méthode métier (ex: read/write)
    Obj->>Srv: envoie la Sentence pour demande d'accès (lockRead/lockWrite)
    Srv->>Coord: transmet la Sentence pour requête de verrou (lockRead/lockWrite)
    Coord-->>Srv: accorde le verrou (peut inclure la Sentence mise à jour)
    Srv-->>Obj: retourne le résultat (objet ou verrou, éventuellement la Sentence)
    Obj-->>App: retourne le résultat métier (Sentence)
```
