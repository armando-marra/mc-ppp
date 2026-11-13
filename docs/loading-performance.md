# Prestazioni dei caricamenti

Prima campagna automatizzata completata: **1.440 operazioni e 36 celle**.
Il [report](loading-performance-results.md) include risultati, controlli di
rilascio e limiti del riferimento senza nuove richieste; non dimostra uno SLA
su un server dedicato a 20 TPS.

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

## Benchmark automatizzato

Il runner dedicato seleziona `LoadingBenchmark` al posto delle suite ordinarie,
usa una directory nuova a ogni avvio e conserva log, dati grezzi, JFR, riepilogo,
revisione Git, patch dei file tracciati e hash SHA-256 dei sorgenti (inclusi i
file nuovi). Non cancella mondi o registrazioni precedenti.

```powershell
# Protocollo predefinito: 30 richieste singole ogni 20 tick, raffica da 10,
# tre ripetizioni, 1200 tick iniziali di riscaldamento.
.\scripts\Run-LoadingBenchmark.ps1

# Carico sostenuto: una richiesta per tick, stesso numero di campioni.
.\scripts\Run-LoadingBenchmark.ps1 -Interval 1

# Prova ridotta della strumentazione; non usare per conclusioni sui percentili.
.\scripts\Run-LoadingBenchmark.ps1 -Samples 2 -Burst 2 -Repeats 1 -Interval 2 -WarmupTicks 20

# Rigenerare il riepilogo senza riavviare Minecraft:
.\scripts\Summarize-LoadingBenchmark.ps1 -RunDirectory '<directory della campagna>'

# Riprodurre solo una combinazione per diagnosticare un errore:
.\scripts\Run-LoadingBenchmark.ps1 -CellFilter 'teleport:unloaded:minecraft:the_nether'
```

Le 12 combinazioni sono tuner/teletrasporto, Overworld/Nether e chunk
caricato/generato-scaricato/nuovo. Ogni combinazione usa target distinti e
distanziati di 32 chunk, anche tra richieste singole e raffiche. Le ripetizioni
usano nuovi target nello stesso mondo; ogni invocazione dello script crea un
mondo nuovo con **seed vanilla GameTest 0**. La posizione del banco di prova è
scelta dal runner vanilla ed è registrata nei log; le coordinate target sono
deterministiche e presenti nei CSV. Non è il protocollo su copie identiche del
mondo descritto più sotto: ordine fisso, cache del sistema e stato JVM possono
influenzare i risultati delle ripetizioni.

Il banco esegue i veri `TunerItem.useOn` e `teleportEntity`. Per i chunk validi
la selezione iniziale del tuner è ottenuta con un'interazione reale in fase di
preparazione; i target nuovi sono riferimenti obsoleti costruiti dalla fixture.
Gli entity transfer hanno partenza in Overworld e destinazione nella dimensione
della cella. I casi Nether misurano quindi Overworld → Nether, non il ritorno.
Le prove di ritorno rimangono nella suite ordinaria.

Per ogni cella:

1. Prepara i portali e, per la cella scaricata, salva i chunk e attende che tutti
   risultino effettivamente scaricati. Solo la cella caricata mantiene ticket
   temporanei di fixture, rimossi in pulizia.
2. Registra almeno 60 tick senza richieste come riferimento, poi esegue richieste
   singole e una raffica. Controlla lo stato caricato prima di ciascun campione.
3. A partire da due tick dopo l'operazione verifica esito, reciprocità, arrivo unico con UUID conservato,
   posizione, rotazione, cooldown e rifiuto del ritorno immediato. I riferimenti
   nuovi/mancanti devono essere rifiutati e ripuliti. Se le sezioni entità non
   espongono ancora l'arrivo, riprova fino a un massimo di 200 tick; registra il
   ritardo in `VerificationDelayTicks` e fallisce se l'arrivo non è osservabile.
4. Rimuove entità e portali, rilascia i ticket della fixture e attende almeno
   350 tick e lo scaricamento di tutti i target prima della cella successiva,
   entro un massimo diagnostico di 6000 tick. `benchmark-lifecycle.csv` registra
   la conferma del rilascio: non è la misura esatta della scadenza del ticket.
   In vanilla il timeout può attendere che il chunk sia pronto per il salvataggio;
   300 tick dalla richiesta non sono un limite assoluto di scaricamento sotto carico.

Le entità vengono create in preparazione, prima della fase senza richieste;
la partenza è mantenuta attiva con ticket di fixture per consentire il tracking.
I tempi dell'operazione escludono creazione dell'entità e verifiche successive;
i tempi dei tick includono anche il lavoro del banco eseguito in quel tick.
I tick vengono letti dal buffer vanilla `getTickTimesNanos` al tick successivo,
verificando il numero del tick. `active_tick` riporta solo tick con richieste;
`tick` include anche gli intervalli senza richieste nella stessa fase. Questo
evita che una raffica lenta venga nascosta dai tick inattivi nel percentile.

Il riepilogo associa ogni evento JFR al target univoco della fixture, richiede
un evento completato per ogni operazione e rifiuta conteggi o stati di caricamento
inattesi o conferme di rilascio mancanti. Produce `summary.csv` e `summary.json`,
aggregando le ripetizioni; i CSV grezzi conservano il campo `Repeat`.
Riporta conteggio, media, p50/p95/p99, massimo, somma
e superamenti di 5 ms per chiamata/operazione e 50 ms per tick. La soglia delle
operazioni complete è un confronto diagnostico; il budget iniziale di 5 ms era
definito per la sola chiamata esplicita getChunk.

**Limite del runner:** GameTest non garantisce la normale cadenza wall-clock di
20 TPS. Si misurano tempi di lavoro dei tick, non un SLA a 20 TPS su server
dedicato. Il riscaldamento è in tick, non in secondi. Il seed 0, le distanze
vista/simulazione 5 e il runtime effettivo sono registrati negli artefatti.
Il mondo GameTest dell'Overworld è una fixture di test: i risultati non vanno
generalizzati alla generazione di un Overworld di sopravvivenza.
Le strutture sono disabilitate dalle opzioni vanilla GameTest. La preparazione
genera il chunk del portale, senza pregenerare a FULL tutto il vicinato richiesto
dal ticket di arrivo. La cella `unloaded` non isola quindi la sola lettura da
disco: durante il trasporto può includere lavoro sul vicinato e sul caricamento
delle sezioni entità. Per attribuire il costo alla sola I/O occorre una campagna
aggiuntiva con area circostante interamente pregenerata.

## Protocollo di conferma su server dedicato

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

## Prima verifica della strumentazione (storico)

Il primo incremento verificava solo l'emissione degli eventi tramite la suite
di regressione; i risultati sotto non dimostrano il rispetto del budget.

Prima verifica del 13 novembre 2026: build con registrazione, 35/35 GameTest e
21 eventi completati, tutti con `loadedBefore=true`. Nove risoluzioni tuner
in Overworld; dodici risoluzioni di teletrasporto (6 Overworld, 4 Nether, 2 End).
Massimo osservato 0,0217 ms. Non sono presenti campioni di `link_verification`
né casi da disco/generazione: nessuna conclusione prestazionale su questi casi.
Artefatti locali ignorati da Git: `build/run/gameTest/portal-gameTest.jfr` e
`build/portal-chunk-loads.csv`; un successivo avvio con profilo li può sostituire.
