# Notes sur les points à revoir ou à confirmer

## Ajout de transient sur le localServer

Dans la classe JvnObjectImpl, l'attribut localServer est marqué comme transient. Cela signifie qu'il ne sera pas sérialisé lorsque l'objet JvnObjectImpl sera sérialisé. Il est important de vérifier que cela est intentionnel et que le comportement attendu est bien celui-ci. Si localServer doit être recréé ou réinitialisé après la désérialisation, il faut s'assurer que cela est bien géré dans le code.

À voir dans ce [fichier](./src/jvn/impl/JvnObjectImpl.java)

## Schema des interactions

```mermaid
sequenceDiagram
    participant App as readListener/writeListener
    participant Obj as JvnObjectImpl
    participant Srv as JvnServerImpl
    participant Coord as JvnCoordImpl
    
    %% Flux d'écriture
    Note over App,Coord: Flux d'écriture (writeListener)
    App->>Obj: jvnLockWrite()
    Obj->>Srv: jvnLockWrite(joi)
    Srv->>Coord: jvnLockWrite(joi, js)
    Note over Coord: Invalide tous les lecteurs
    Note over Coord: Invalide l'ancien writer
    Coord-->>Srv: Dernière version de l'objet
    Srv-->>Obj: Met à jour sharedObject + état W
    App->>Obj: jvnGetSharedObject()
    Obj-->>App: Retourne Sentence
    App->>Obj: write(newValue)
    App->>Obj: jvnUnLock()
    Note over Obj: Passe en état WC

    %% Flux de lecture
    Note over App,Coord: Flux de lecture (readListener)
    App->>Obj: jvnLockRead()
    Obj->>Srv: jvnLockRead(joi)
    Srv->>Coord: jvnLockRead(joi, js)
    Note over Coord: Si writer existe
    Coord->>Srv: jvnInvalidateWriterForReader
    Srv->>Obj: jvnInvalidateWriterForReader
    Note over Obj: Attend si W/RWC
    Note over Obj: WC->RC
    Obj-->>Srv: Dernière version
    Srv-->>Coord: Dernière version
    Note over Coord: Enregistre reader
    Coord-->>Srv: Dernière version
    Srv-->>Obj: Met à jour sharedObject + état R
    App->>Obj: jvnGetSharedObject()
    Obj-->>App: Retourne Sentence
    App->>Obj: read()
    App->>Obj: jvnUnLock()
    Note over Obj: Passe en état RC
```
