# Stato dei miglioramenti

Aggiornato al 13 novembre 2026.

## Ripresa nella nuova chat dopo il cambio dei permessi

Ripresa completata su main a partire da f42f11b. Accesso in scrittura a .git
verificato creando e rimuovendo un file temporaneo, senza modificare le ACL.
I passi 1?4 sono separati nei quattro commit sotto, ciascuno verificato con
una build completa prima del commit. Nessun push eseguito.
Tutti i sette file di codice, manifest e checklist finali sono identici byte
per byte alla copia iniziale. Questo documento ? aggiornato e il suo testo
precedente ? conservato integralmente nel registro storico in fondo.

## Commit dei passi 1?4

| Passo | Commit effettivo | Verifica eseguita |
| --- | --- | --- |
| 1. Persistenza e base preesistente | `9cc95b2` ? test: cover portal persistence and establish tuner regression suite | build riuscita, 24/24 GameTest |
| 2. Chunk scaricati e scadenza ticket | `e67aa16` ? test: verify unloaded portal twins and arrival ticket expiry | build riuscita, 26/26 GameTest |
| 3. Tuner tra dimensioni | `7318be8` ? test: cover tuner interactions across dimensions | build riuscita, 29/29 GameTest |
| 4. Giocatori ed End | Questo commit ? test: verify player transfers and End travel | build riuscita, 35/35 GameTest |

Il quarto commit contiene questo aggiornamento: il suo hash non pu? essere
inserito nel proprio contenuto. Per risolvere l'identificativo effettivo anche
dopo altri commit, usare:

```powershell
git log -1 --format="%h %s" --fixed-strings --grep="test: verify player transfers and End travel"
```

Il passo 1 comprende i quattro test di persistenza, i nove test locali del
tuner, il ticket temporaneo vanilla e le correzioni delle fixture di arrivo.
Il passo 2 registra ChunkLifecycleGameTests e aggiunge il test di scadenza del
ticket nel file condiviso CrossDimensionGameTests. Il passo 3 aggiunge i tre
casi useOn tra dimensioni. Il passo 4 aggiunge due viaggi di entit? da/verso
l'End e quattro viaggi di ServerPlayer tra Overworld, Nether ed End.
Manifest e checklist sono aggiornati insieme alle rispettive coperture.

Tutti i controlli git diff --check e git diff --cached --check sono puliti.
La build finale supera 35/35 GameTest (34 della mod e uno Fabric). Nessun
asset modificato: runDatagen non necessario. Restano l'avviso ambientale per
build/resources/client assente e ritardi del server durante la preparazione
dei test; nessun errore di compilazione o test fallito. Gli helper con
connessione simulata mantengono la soppressione locale dell'avviso di rimozione.

## Limiti residui

- Il vero arresto e riavvio del mondo resta manuale: i test di persistenza
  esercitano le API reali di serializzazione, senza riavviare il server.
- La connessione dei giocatori ? simulata: feedback visivo, messaggi, suoni e
  conferma del cambio dimensione richiedono una verifica con client reale.
- Restano le prove manuali su entit? larghe/alte, ulteriori ostacoli e pericoli,
  permanenza sul portale oltre il cooldown e ritorno dopo essersi allontanati.
- Gli scaricamenti reali e la scadenza dei ticket sono coperti dal passo 2;
  il costo dei caricamenti sincroni non ? stato misurato.
- CI e allineamento dei riferimenti obsoleti Java/Yarn in AGENTS.md,
  PROJECT_DESIGN.md e README restano attivit? future; quei file sono invariati.

La checklist ? in [portal-regression-checks.md](docs/portal-regression-checks.md).

## Registro storico conservato

Il testo seguente ? quello della sessione precedente, conservato integralmente.
Le indicazioni su permessi bloccati, commit mancanti e attivit? ancora da
implementare dei passi 1?4 sono superate dallo stato corrente sopra.

# Stato dei miglioramenti

Aggiornato al 13 novembre 2026. Questo documento raccoglie le proposte emerse
dalla ricognizione del progetto e lo stato della loro implementazione.

## Ripresa nella nuova chat dopo il cambio dei permessi

Questa sezione è il punto di ripresa corrente e prevale sulle vecchie note di
riavvio più sotto. Il lavoro di implementazione e verifica automatica dei passi
1–4 è presente nel checkout `D:\Projects\mc-ppp`, branch `main`, HEAD `f42f11b`.
Ultima build completa riuscita: **35/35 GameTest**. Nessuna build lasciata in
esecuzione e nessun commit o push creato in questa sessione.

L'utente ha modificato la configurazione dei permessi e apre una nuova chat
per ricaricarla. L'ultimo tentativo nella vecchia sessione di `git add` è ancora
fallito con `Unable to create '.git/index.lock': Permission denied`.
`Test-Path .git/index.lock` ha restituito `False`: non è un lock residuo.
L'ACL della directory mostrava ancora divieti espliciti di scrittura. Verificare
i permessi effettivi nella nuova sessione, senza modificare o aggirare le ACL.

File locali da conservare integralmente:

- `src/main/java/com/project/pointportals/block/entity/TeleporterBlockEntity.java`;
- `src/gametest/java/com/project/pointportals/CrossDimensionGameTests.java`;
- `src/gametest/java/com/project/pointportals/TunerGameTests.java` (non tracciato);
- `src/gametest/java/com/project/pointportals/PersistenceGameTests.java` (non tracciato);
- `src/gametest/java/com/project/pointportals/ChunkLifecycleGameTests.java` (non tracciato);
- `src/gametest/resources/fabric.mod.json`;
- `docs/portal-regression-checks.md`;
- `MIGLIORAMENTI.md` (non tracciato).

Prossima azione autorizzata: creare **quattro commit separati**, uno per passo,
con staging selettivo dei file condivisi. Ogni commit deve avere suite registrate
coerenti e una build funzionante; non limitarsi a dividere i file arbitrariamente.
Il passo 1 può includere la base locale preesistente (nove test del tuner,
ticket di arrivo vanilla e correzioni delle fixture cross-dimensione), come già
comunicato all'utente. Ripartizione consigliata:

1. Persistenza e base preesistente: quattro nuovi casi; build attesa 24 test.
2. Chunk scaricati e scadenza dei ticket: due nuovi casi; build attesa 26 test.
3. Tuner tra dimensioni: tre nuovi casi; build attesa 29 test.
4. Giocatori ed End: sei nuovi casi; build attesa 35 test.

Le modifiche dei passi 2 e 4 condividono `CrossDimensionGameTests.java`; quelle
del passo 3 estendono `TunerGameTests.java`. Manifest e documentazione vanno
distribuiti coerentemente. Conservare il risultato finale mentre si prepara lo
staging, senza scartare il lavoro successivo. Aggiornare questo documento con
i commit effettivi e lo stato conclusivo. Nessun push richiesto.

Per Gradle, se la cache torna a risolversi in `C:\.gradle`, usare solo nel
processo PowerShell `$env:GRADLE_USER_HOME = 'C:\Users\armando.marra\.gradle'`
prima di `.\gradlew.bat build`. Non inserire percorsi personali nel repository.
Il vero riavvio del mondo e le verifiche dal client restano manuali e non sono
dimostrati dai GameTest: mantenerli espliciti nel resoconto.

Prompt da incollare nella nuova chat:

> Riprendi in D:\Projects\mc-ppp. Leggi AGENTS.md e MIGLIORAMENTI.md, iniziando
> dalla sezione «Ripresa nella nuova chat dopo il cambio dei permessi». Ho
> aggiornato i permessi per consentire i commit. Conserva tutte le modifiche
> locali: i passi 1–4 sono implementati e l'ultima build ha superato 35/35
> GameTest. Verifica l'accesso a .git e crea quattro commit separati e coerenti,
> uno per passo, con le verifiche indicate nel documento. Aggiorna
> MIGLIORAMENTI.md con i commit effettivi e gli eventuali limiti residui.
> Non fare push. Non richiedere nuovamente l'autorizzazione ai commit.

## Esecuzione autorizzata dei passi 1–4

### Stato attuale dopo la verifica finale

| Passo | Implementazione e prova automatica | Limiti residui |
| --- | --- | --- |
| 1. Persistenza | Quattro nuovi GameTest sulle API reali di salvataggio/caricamento di BlockEntity e ItemStack; dati mancanti, malformati e dimensione inesistente. | Il vero arresto e riavvio del mondo resta una prova manuale distinta. |
| 2. Chunk scaricati | Scaricamento reale, conservazione del legame senza caricamenti, rottura con gemello scaricato, ricaricamento e riconciliazione; ticket dopo un vero trasferimento scaduto con scaricamento verificato. | Nessuna misura prestazionale richiesta in questo passo. |
| 3. Tuner tra dimensioni | Tre scenari con `useOn` reale: collegamento/scollegamento, destinazione occupata e sorgente distrutta. | Feedback client da verificare manualmente. |
| 4. Giocatori ed End | Due viaggi di entità da/verso l'End e quattro viaggi di ServerPlayer: Overworld/Nether e Overworld/End in entrambi i sensi. | Connessione simulata; interfaccia e conferma del cambio dimensione da client reale non verificate. |

**Verifica finale: `.\gradlew.bat build` riuscita, 35/35 GameTest** (34 della
mod e uno Fabric). I test di trasporto verificano posizione, rotazione, UUID,
assenza di duplicati alla partenza, cooldown, rifiuto del ritorno immediato e
reciprocità. Quelli dei giocatori controllano anche identità dell'oggetto,
dimensione attuale e presenza unica nella lista server. Le fixture rimuovono
il giocatore dalla lista e puliscono entrambi i livelli. Gli avvisi di rimozione
dell'helper con connessione simulata sono soppressi localmente con una nota
sul motivo; rimangono gli avvisi dell'ambiente e i ritardi durante la generazione
dei chunk di test. Nessun asset modificato; `runDatagen` non necessario.

**Commit ancora da creare:** la richiesta di un commit per passo resta valida,
ma `.git` è in sola lettura nell'ambiente. Le modifiche di tutti i passi sono
conservate nel checkout e il ramo resta a `f42f11b`; nessun push eseguito.
Per riprendere i commit serve una sessione con accesso in scrittura ai metadata
Git. Non considerare la consegna completa finché non sono stati creati i commit
richiesti. Le note sotto sono il registro cronologico degli incrementi e possono
descrivere stati precedenti.

Il 13 novembre è stata autorizzata l'esecuzione dei passi 1–4 con un commit
alla conclusione di ciascun passo. Le indicazioni successive di attendere
autorizzazione per implementare o creare commit sono quindi superate.

Passo 1, copertura di persistenza: aggiunta `PersistenceGameTests`, registrata
nella mod di test, con quattro scenari. Verifica salvataggio con metadata e
ricostruzione delle BlockEntity, ripristino dei riferimenti reciproci,
caricamento di dati assenti/malformati sopra uno stato precedentemente valido,
riconciliazione del BlockState, portale scollegato, dimensione inesistente e
serializzazione dell'intero ItemStack del tuner con il suo Data Component.
Build riuscita: **24/24 GameTest**. Questi test esercitano le API reali di
persistenza; non dimostrano un vero arresto e riavvio del server, che resta
una verifica manuale da eseguire. Nessuna modifica di produzione necessaria
per questi quattro nuovi casi. Durante la suite è comparso anche un avviso
temporaneo di ritardo del server, senza test falliti.

Il commit previsto del passo 1 deve includere le modifiche locali pregresse
descritte sotto, oltre alla nuova suite e alla documentazione. Il tentativo
di `git add` e `git commit` è fallito: l'ambiente concede sola lettura a `.git`
e impedisce la creazione di `.git/index.lock`. Nessun commit creato e nessuna
modifica scartata. I passi 2–4 restano in corso;
nessuna verifica client è stata eseguita. Le sezioni sul vecchio punto di
ripresa descrivono la situazione precedente a questa esecuzione.

Passo 2, avanzamento: aggiunta `ChunkLifecycleGameTests`. Il test attende un
vero scaricamento del chunk remoto nel Nether, verifica per almeno 45 tick
che la verifica del legame non lo ricarichi, distrugge la sorgente senza
caricare il gemello, ricarica il chunk e controlla persistenza e riconciliazione
del riferimento obsoleto. **Build riuscita con 25/25 GameTest**. Resta da
aggiungere la verifica della scadenza del ticket dopo un trasferimento reale.
La build è terminata; nessun processo Gradle lasciato in esecuzione.

Prossime azioni: completare la copertura dei ticket del passo 2, implementare
il tuner tra dimensioni del passo 3 e giocatori/End del passo 4. Non considerare
concluso l'obiettivo complessivo. I commit per passo rimangono da creare quando
la directory `.git` sarà scrivibile; non tentare aggiramenti dei permessi.

Aggiornamento successivo: **passo 2 verificato**, incluso trasferimento reale,
visibilità dell'entità dopo due tick e scaricamento del chunk remoto dopo
350 tick, senza ricaricamenti causati dalla verifica del legame. Build 26/26.
La destinazione del test dei ticket è isolata dalle altre fixture; la pulizia
ricarica esplicitamente il chunk solo dopo le asserzioni sullo scaricamento.

**Passo 3 verificato**: tre scenari aggiunti a `TunerGameTests`, con vero
`useOn` in Overworld e Nether: collegamento e scollegamento, rifiuto di una
destinazione occupata preservando la coppia e la selezione, e sorgente distrutta
con cancellazione della selezione obsoleta. Il giocatore simulato raggiunge
il Nether tramite il trasferimento vanilla; la pulizia lo rimuove anche dalla
lista server. Reciprocità e alimentazione sono controllate per dimensione.
Build finale di questo incremento: **29/29 GameTest**, nessun processo rimasto
in esecuzione. Il test usa `makeMockServerPlayerInLevel`, attualmente deprecato
ma dotato della connessione simulata richiesta dal trasferimento; questo genera
un avviso di compilazione. Rimane il passo 4: trasporto attraverso il portale
di giocatori e viaggi da/verso l'End. I commit sono ancora impediti dai permessi
di `.git`; non sono stati creati né dichiarati completati.

## Completati e integrati

Le seguenti modifiche sono nel commit `f42f11b`, integrato in `main` e pubblicato
su `origin`.

| Miglioramento | Risultato |
| --- | --- |
| Build portabile | Rimosso il percorso personale `org.gradle.java.home`; dichiarata la toolchain Java 25 e aggiornate le istruzioni nel README. |
| Sicurezza dell'arrivo | Controllo del volume completo dell'entità, delle collisioni parziali, di fuoco e lava nel volume di arrivo e del confine del mondo. |
| Recupero degli stati incoerenti | Il tick ripristina un portale con `LINKED=true` ma senza destinazione e si interrompe quando la verifica invalida il collegamento. |
| Protezione dai riferimenti obsoleti | La rimozione di A scollega B solo se B punta ancora ad A; una coppia B–C valida viene preservata. |
| Scollegamento condiviso | Il tuner riutilizza il controllo di reciprocità impiegato alla rimozione del portale. |
| Distribuzione delle verifiche | I controlli restano ogni 40 tick, con una fase determinata dalla posizione per ridurre la concentrazione del lavoro. |
| Rotazione al teletrasporto | Gli angoli vengono passati come valori assoluti, evitando di sommarli una seconda volta. |
| Infrastruttura GameTest | Source set separato, mod di test e suite server eseguita anche da `build`; i test non entrano nel JAR distribuito. |

### Copertura automatica integrata

Dieci scenari della mod verificano:

- collegamenti reciproci, rimozione con riferimento obsoleto e rottura del gemello;
- recupero di `LINKED` senza destinazione;
- rifiuto dell'arrivo su una lastra, trasporto con cooldown e perdita di Redstone;
- Overworld → Nether e Nether → Overworld: posizione, rotazione, UUID,
  unicità alla partenza, cooldown e conservazione del collegamento;
- rifiuto di una destinazione ostruita nel Nether.

Prima del commit la suite ha superato 11 GameTest: dieci della mod e un controllo
Fabric. Il JDK 25 installato nel sistema è stato verificato con una build riuscita.

## Implementati localmente: incremento corrente

- Nove GameTest del tuner: selezione con Data Component, collegamento e stato
  Redstone, auto-collegamento, destinazione occupata, sorgente occupata nel
  frattempo, sorgente distrutta, cancellazione della selezione, scollegamento
  reciproco e interazione con blocchi ordinari.
- Uso di un giocatore simulato in sopravvivenza e chiamata al vero `useOn` dello
  strumento. Verifica dei risultati, dei componenti e dei BlockState.
- Ricerca dell'entità nella sola dimensione di arrivo nei test tra dimensioni,
  invece della ricerca globale per UUID; pulizia esplicita delle entità di test
  nelle due dimensioni.
- Trasferimento tramite `TeleportTransition.PLACE_PORTAL_TICKET`: il ticket
  vanilla mantiene temporaneamente caricata e attiva la destinazione, anche
  senza giocatori, e scade automaticamente dopo 300 tick (15 secondi).
- Controlli di arrivo differiti di due tick, con la coppia di portali mantenuta
  fino alla verifica. Pulizia centralizzata di entità, portali e alimentazione
  anche se un'asserzione fallisce, sia durante la preparazione sia nel callback.
- Aggiornamento della guida alle verifiche in `docs/portal-regression-checks.md`.

### Diagnosi e verifica del trasferimento Overworld → Nether

Riprodotto il guasto iniziale: 19 GameTest riusciti e ricerca dell'entità nel
Nether fallita al tick 0. Il solo rinvio della query al tick 2 fallisce ancora.
L'indagine sul codice della versione Minecraft 26.2 usata dalla build ha
mostrato che `getChunk(...)` carica i blocchi con un ticket `UNKNOWN`, di breve
durata e senza simulazione, mentre `Entity.teleportTo(...)` usa una transizione
con callback `DO_NOTHING`. Manca quindi il ticket di arrivo che mantiene attiva
una destinazione senza giocatori; l'entità trasferita può restare esclusa dalle
query delle sezioni accessibili.

La correzione usa `Entity.teleport(new TeleportTransition(...))` con il callback
vanilla `PLACE_PORTAL_TICKET`, senza caricamenti forzati permanenti. Il test
attende due tick per interrogare la destinazione dopo l'aggiornamento del server,
mantenendo i controlli su posizione esatta, rotazione, UUID, assenza di duplicati
alla partenza, cooldown minimo di 40 tick, rifiuto del ritorno e reciprocità.

Verifiche del 13 novembre 2026:

- `.\gradlew.bat runGameTest`: 20/20 GameTest riusciti dopo la correzione.
- `.\gradlew.bat build`: **BUILD SUCCESSFUL**, suite completa 20/20 riuscita
  (19 test della mod, inclusi i nove del tuner, e un controllo Fabric).
- `git diff --check`: nessun errore di whitespace.
- Nessun asset modificato: `runDatagen` non necessario per questo incremento.

Restano gli avvisi di avvio dell'ambiente di test sul percorso delle risorse
client assente e sulle mappings non presenti; non ci sono errori di compilazione.
Le modifiche locali precedenti sono conservate e nessun commit è stato creato.

### Punto di ripresa dopo il riavvio

Indagine ripresa e conclusa dopo il riavvio: il difetto di arrivo nel Nether è
corretto e la build completa è verde. Nessuna build è in esecuzione.
Checkout: `D:\Projects\mc-ppp`, branch `main`, ultimo commit `f42f11b` già
pubblicato. Il vecchio worktree è stato rimosso.

Modifiche locali da conservare:

- `src/gametest/java/com/project/pointportals/TunerGameTests.java`: nove nuovi test;
- `src/gametest/resources/fabric.mod.json`: registrazione della nuova suite;
- `src/gametest/java/com/project/pointportals/CrossDimensionGameTests.java`:
  ricerca nella destinazione dopo due tick e gestione della pulizia della coppia;
- `src/main/java/com/project/pointportals/block/entity/TeleporterBlockEntity.java`:
  transizione con ticket temporaneo vanilla;
- `docs/portal-regression-checks.md`: copertura aggiornata;
- questo documento, `MIGLIORAMENTI.md`.

Nessuna correzione pendente per il guasto al tick 0. Il prossimo incremento va
scelto tra le attività proposte sotto. Creare un commit solo quando richiesto;
non scartare le modifiche locali.

Prompt suggerito per una nuova sessione:

> Riprendi il lavoro in D:\Projects\mc-ppp. Leggi AGENTS.md e MIGLIORAMENTI.md,
> soprattutto il punto di ripresa dopo il riavvio. Conserva le modifiche locali.
> Il difetto Overworld → Nether è stato corretto con il ticket temporaneo vanilla
> e i controlli di arrivo dopo due tick. La build completa ha superato tutti i
> 20 GameTest. Nessun commit creato: attendi indicazioni sul prossimo incremento.

## Proposta operativa dopo la ricognizione del 13 novembre 2026

Richiesta corrente: leggere le istruzioni, proporre i prossimi passi e aggiornare
questo documento. Nessuna nuova funzionalità implementata in questa ricognizione;
le modifiche locali dell'incremento precedente vanno conservate, senza creare
commit finché non richiesto.

Il controllo del checkout conferma `main` al commit `f42f11b` e i file locali
elencati sopra. `MIGLIORAMENTI.md` e `TunerGameTests.java` sono ancora non
tracciati. La verifica della pubblicazione su `origin` non è stata ripetuta.

### Ordine consigliato degli incrementi

1. **Persistenza: primo incremento consigliato.** Aggiungere una suite dedicata
   in `src/gametest/java/com/project/pointportals/` e registrarla nella mod di
   test. Esercitare il salvataggio e caricamento effettivi della BlockEntity
   (`saveAdditional`/`loadAdditional`) e dell'ItemStack con il Data Component,
   senza limitarsi al codec isolato. Verificare coppia reciproca, coordinate e
   dimensione, portale non collegato, `twin_target` assente o malformato e
   dimensione formalmente valida ma inesistente. Controllare la riconciliazione
   del BlockState dopo il caricamento. Toccare `TeleporterBlockEntity`,
   `PortalTarget` o i componenti solo se un test evidenzia un difetto.
   Completamento: nuovi casi e suite esistente verdi; il semplice ciclo di
   serializzazione non sostituisce la prova di chiusura e riapertura del mondo,
   che resta manuale finché non viene automatizzato un vero riavvio.
2. **Chunk scaricati e riconciliazione.** Preparare test con chunk realmente
   scaricati, dimostrando la precondizione. Verificare che il controllo periodico
   con `verifyAndSyncLink(false)` conservi il legame senza caricare il gemello;
   dopo la rottura con l'altro capo scaricato, verificare lo scollegamento quando
   il gemello mancante torna ispezionabile. Controllare anche che i ticket
   temporanei di arrivo scadano e non mantengano caricamenti permanenti. Non
   simulare lo scaricamento rimuovendo soltanto una BlockEntity.
3. **Tuner tra dimensioni.** Estendere `TunerGameTests` usando il vero `useOn`:
   selezione in Overworld, collegamento nel Nether, reciprocità, stato Redstone,
   consumo della selezione e scollegamento. Includere riferimenti obsoleti e
   portali già occupati; riusare la pulizia delle fixture tra dimensioni.
4. **Giocatori ed End.** Separare in incrementi distinti il trasporto di un
   giocatore e i viaggi da/verso l'End. Verificare posizione, rotazione, UUID,
   cooldown e assenza di duplicati; mantenere una verifica client per il cambio
   dimensione e i messaggi, che il solo giocatore simulato non dimostra.
5. **Costo dei caricamenti.** Misurare le chiamate sincrone a `getChunk(...)`
   presenti nel teletrasporto e nel tuner, con destinazioni già generate e
   nuove, prima di scegliere limiti, accodamento o gestione asincrona. Definire
   carico e budget di tick riproducibili; evitare una riscrittura preventiva.
6. **Integrazione continua.** Riprodurre la build Java 25 e i GameTest in GitHub
   Actions, conservando i log in caso di errore. Verificare prima che il mondo
   di test e le fixture non dipendano dalla cache locale.

**Allineamento documentale da anticipare:** in un piccolo incremento dedicato,
correggere Java 21/Yarn 1.21.4 nel punto 3.2 di `AGENTS.md` e i riferimenti
obsoleti in `PROJECT_DESIGN.md`, seguendo Minecraft 26.2, Java 25 e i nomi
Mojang effettivamente usati dalla build. Aggiornare il README sulle opzioni
non implementate (attivazione configurabile con sneak) e sulle garanzie ancora
da verificare (assenza di blocchi durante il caricamento dei chunk). Precisare
che la riconciliazione della rottura può essere differita con chunk scaricati.
Questi file non sono stati modificati nella richiesta corrente.

Per ogni incremento: piano circoscritto, implementazione, `build`, revisione e
aggiornamento di questo documento; `runDatagen` solo quando cambiano asset o
dati. Concordare l'incremento successivo prima di avviarlo. Non introdurre in
un'unica modifica persistenza, caricamento chunk e nuova logica di trasporto.

### Verifica della ricognizione

Nuova esecuzione di `.\gradlew.bat build`: **BUILD SUCCESSFUL**, tutti i 20
GameTest superati (19 della mod e un controllo Fabric). Restano gli avvisi già
noti sulle risorse client assenti e sulle mappings non presenti. Nessun asset
modificato: `runDatagen` non necessario.

Il primo tentativo del wrapper si è fermato prima di avviare Gradle perché
cercava la cache in `C:\.gradle`, non scrivibile nell'ambiente corrente. Il
comando è riuscito impostando `GRADLE_USER_HOME=C:\Users\armando.marra\.gradle`
solo nel processo PowerShell, senza aggiungere percorsi personali alla
configurazione del repository. Nessuna build rimane in esecuzione.

## Proposti, ancora da iniziare

| Priorità | Attività | Criterio di completamento |
| --- | --- | --- |
| Alta | Persistenza e riavvio | Test di salvataggio e caricamento dei collegamenti, inclusi dati mancanti o invalidi. |
| Alta | Chunk scaricati | Verificare conservazione del legame, distruzione del gemello e successiva riconciliazione senza caricamenti continui. |
| Alta | Tuner tra dimensioni | Collegare e scollegare due portali in dimensioni diverse usando il vero strumento. |
| Alta | Trasporto di giocatori e viaggi nell'End | Estendere la copertura oltre i maiali e la coppia Overworld–Nether. |
| Media | Caricamenti sincroni dei chunk | Misurare l'impatto di `getChunk(...)` sui tick e progettare una gestione con limiti e rilascio delle risorse. |
| Media | Integrazione continua | Workflow GitHub Actions con Java 25, build, GameTest e conservazione dei log in caso di errore. |
| Media | Allineamento documentazione | Aggiornare `PROJECT_DESIGN.md` e risolvere i riferimenti discordanti a Java 21/Yarn in `AGENTS.md`; correggere nel README le funzionalità descritte ma non implementate. |

## Verifiche manuali ancora necessarie

- Feedback visivo, messaggi, suoni e interazioni dal client.
- Arrivo di entità larghe o alte, ulteriori ostacoli parziali e pericoli.
- Permanenza sul portale oltre il cooldown e successivo ritorno dopo essere usciti.
- Scenari di scaricamento dei chunk e riavvio del mondo finché non automatizzati.

La checklist dettagliata è in [portal-regression-checks.md](docs/portal-regression-checks.md).

## Comandi

```powershell
.\gradlew.bat build        # Compilazione e suite GameTest server
.\gradlew.bat runGameTest  # Solo suite GameTest server
.\gradlew.bat runClient    # Verifiche manuali dal client
```

Per i prossimi incrementi: implementare una modifica circoscritta, eseguire la
suite, aggiornare questo documento e creare il commit quando richiesto.
