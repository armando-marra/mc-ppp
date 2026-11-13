# Prestazioni dei caricamenti — primo incremento

## Strumentazione disponibile

Gli eventi JFR `pointportals.ChunkLoad` misurano esclusivamente il tempo trascorso
dentro le tre risoluzioni esplicite `getChunk` della mod, sul thread chiamante:

| Operazione | Punto misurato |
| --- | --- |
| `tuner_source` | Risoluzione del primo capo alla chiusura del collegamento |
| `link_verification` | Risoluzione di un gemello scaricato nelle interazioni con il tuner |
| `teleport_destination` | Risoluzione della destinazione prima dei controlli di arrivo |

Ogni evento contiene durata, dimensione, coordinate del chunk, `loadedBefore` e
`completed` (completamento di getChunk, non del collegamento o del viaggio).
Nessuna coda, ticket aggiuntivo o modifica delle regole di trasporto.
Senza una registrazione che abilita l'evento, il wrapper esegue solo la chiamata
originale dopo il controllo JFR. Le verifiche periodiche non caricano chunk.

`loadedBefore=false` non distingue disco e generazione. Questa distinzione va
stabilita tramite fixture e copie del mondo. Le letture di blocchi adiacenti,
collisioni, ticket vanilla e trasferimento possono provocare altro lavoro:
la somma degli eventi non rappresenta l'intero costo del viaggio o del tick.

## Avvio e riepilogo

Con JDK 25 in JAVA_HOME, da PowerShell:

```powershell
.\gradlew.bat runGameTest -PportalProfile
# Trova la registrazione nella directory di esecuzione Loom:
Get-ChildItem run,build -Filter 'portal-*.jfr' -Recurse
.\scripts\Summarize-PortalChunkLoads.ps1 -Recording '<percorso.jfr>' |
    Export-Csv build/portal-chunk-loads.csv -NoTypeInformation
```

L'opzione vale anche per `runServer` e `runClient`. Registra solo il processo
Minecraft, con profilo JFR e scaricamento alla chiusura normale. Copiare ogni
file prima della successiva esecuzione, che usa lo stesso nome. La suite è una
verifica della strumentazione e della regressione, non un benchmark controllato.
Il riepilogo usa percentili nearest-rank, raggruppati per operazione, dimensione,
stato iniziale e completamento; non unire scenari diversi nello stesso report.

## Campagna riproducibile da eseguire

Usare un server dedicato senza altri giocatori/mod, seed `20261113`, distanze
di vista/simulazione 5, tick rate 20, stessi JDK, heap, hardware e storage.
Annotare commit, configurazione, seed, coordinate, versione sistema e stato
della cache disco: chunk scaricato non significa cache del sistema vuota.
Attendere 60 secondi dopo l'avvio prima del carico. Eseguire tre ripetizioni
di ciascuna cella seguente, ripristinando la copia iniziale del mondo ogni volta.

| Percorso | Fixture caricata | Generata e scaricata | Chunk nuovo |
| --- | --- | --- | --- |
| Tuner | Primo capo già caricato | Selezionare il primo capo, allontanarsi e verificarne lo scaricamento prima del secondo click | Selezione obsoleta su coordinate mai generate, tramite Data Component di fixture |
| Teletrasporto | Coppia valida caricata | Coppia salvata, destinazione senza ticket o giocatori | Riferimento obsoleto verso coordinate mai generate, tramite dati BlockEntity di fixture |

Un portale valido non può trovarsi in un chunk mai generato. I casi nuovi
misurano quindi il recupero da dati obsoleti: devono rifiutare il viaggio o
il collegamento e ripulire il riferimento secondo le regole correnti.
Non presentarli come trasferimenti riusciti verso nuovi portali.

Per ogni cella preparare 30 coppie/target distinti, distanziati di almeno 32
chunk e lontani dallo spawn, ripetendo Overworld e Nether. Eseguire un'operazione
ogni 20 tick (30 campioni), poi un carico separato di 10 operazioni nello stesso
tick su fixture distinte. Le fixture devono ripristinare tuner/riferimenti per
ogni prova di rifiuto e non includere preparazione/generazione negli intervalli
misurati. Controllare `loadedBefore` prima di accettare un campione nella cella;
se un chunk non è scaricato, attendere o ripristinare la fixture.

Registrare almeno conteggio, media, p50/p95/p99, massimo, somma dei tempi e numero
di chiamate oltre 5 ms per cella. Affiancare una misura dei tick del server e una
registrazione senza carico per separare il costo di base. Il profilo JVM può
aiutare ad attribuire CPU, I/O e GC, ma i soli eventi della mod non misurano MSPT.

Budget iniziale proposto: p95 <= 5 ms per chiamata esplicita e p95 <= 50 ms per
tick complessivo sotto il carico concordato; riportare sempre anche massimo e
tick oltre 50 ms. Non sono garanzie né soglie di test automatico. Con 30 campioni
p99 coincide con il massimo: ampliare la campagna prima di decisioni definitive.
Valutare limiti, accodamento o asincronia solo dopo i risultati della campagna,
preservando reciprocità, sicurezza dell'arrivo e cooldown.

## Stato

Strumentazione e procedura iniziali; campagna controllata e misura MSPT ancora
da implementare/eseguire. I risultati della suite di regressione verificano
l'emissione degli eventi e non dimostrano il rispetto del budget.

Prima verifica del 13 novembre 2026: build con registrazione, 35/35 GameTest e
21 eventi completati, tutti con `loadedBefore=true`. Nove risoluzioni tuner
in Overworld; dodici risoluzioni di teletrasporto (6 Overworld, 4 Nether, 2 End).
Massimo osservato 0,0217 ms. Non sono presenti campioni di `link_verification`
né casi da disco/generazione: nessuna conclusione prestazionale su questi casi.
Artefatti locali ignorati da Git: `build/run/gameTest/portal-gameTest.jfr` e
`build/portal-chunk-loads.csv`; un successivo avvio con profilo li può sostituire.
