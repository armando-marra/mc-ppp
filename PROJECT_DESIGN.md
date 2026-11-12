# Point-to-Point Portals (Minecraft Fabric Mod)

**Versione Target:** Minecraft Java Edition 1.21+  
**Mod Loader:** Fabric  
**Ambiente di Sviluppo:** Antigravity IDE (Gradle + Java 21)

---

## 1. Visione del Progetto
**Point-to-Point Portals** è una mod per Minecraft progettata per offrire un sistema di viaggio rapido bilanciato, immersivo e integrato con le meccaniche vanilla (Redstone ed esplorazione). 

A differenza dei tradizionali sistemi di teletrasporto basati su menu testuali o reti globali, questa mod introduce **portali fisici collegabili esclusivamente in coppie (1-a-1)**. Ogni portale funziona come un ponte dedicato tra due punti specifici della mappa (anche tra dimensioni diverse) e richiede energia Redstone per rimanere attivo.

---

## 2. Meccaniche di Gioco (Core Gameplay)

### 2.1 Il Blocco Portale (`TeleporterBlock`)
Il blocco centrale della mod è una pedana/monolite che attraversa **tre stati visivi e funzionali**:
1. **Inattivo (Non collegato):** Stato iniziale al momento del piazzamento. Non emette luce e non reagisce alla Redstone o ai giocatori.
2. **In Standby (Collegato, ma senza Redstone):** Il portale è associato a un gemello, ma è spento. Mostra un indicatore visivo tenue (es. cristalli illuminati a bassa intensità), ma il teletrasporto è bloccato.
3. **Attivo (Collegato + Alimentato da Redstone):** Quando il portale è associato a un gemello **E** riceve un segnale di Redstone (`POWERED = true`), si attiva completamente: emette luce, particelle e teletrasporta le entità che vi passano sopra.

> **Vincolo di Sicurezza:** Perché il viaggio avvenga, entrambi i portali devono essere validi; se il portale di destinazione viene distrutto, il portale di partenza torna immediatamente allo stato *Inattivo*.

### 2.2 Collegamento Esclusivo 1-a-1 (`TunerItem`)
Per associare due portali si utilizza uno strumento dedicato: il **Sintonizzatore di Fase**.
* **Selezione Primo Nodo:** `Click Destro` su un Portale libero salva le sue coordinate e la sua dimensione all'interno del Sintonizzatore.
* **Chiusura del Circuito:** `Click Destro` su un secondo Portale libero crea un **legame bidirezionale esclusivo** (`Portale A <-> Portale B`) e resetta lo strumento.
* **Protezione Anti-Sovrascrittura:** Tentare di collegare un portale già occupato restituisce un messaggio di errore al giocatore.
* **Scollegamento Manuale:** `Shift + Click Destro` su un portale collegato interrompe il legame su entrambi i capi, rendendo nuovamente disponibili i due blocchi.

### 2.3 Sistema Anti-Loop
Per evitare che un giocatore venga teletrasportato all'infinito avanti e indietro tra A e B al momento dell'arrivo:
* Viene applicato un **cooldown temporaneo** (es. 40 tick / 2 secondi) sull'entità appena teletrasportata, oppure viene richiesto che il giocatore si accovacci (`Sneak`) sulla pedana attiva per innescare il salto.

---

## 3. Progressione e Ricette di Crafting

Le ricette sono bilanciate per richiedere materiali di metà gioco (Nether ed esplorazione di geodi):

### 3.1 Blocco Portale (Produce 2 unità)
Permette al giocatore di ottenere subito una coppia completa con un singolo craft.
* **Angoli:** Ossidiana (`minecraft:obsidian`)
* **Lati:** Lingotti di Rame (`minecraft:copper_ingot`) o Polvere di Redstone (`minecraft:redstone`)
* **Centro:** Occhio di Ender (`minecraft:ender_eye`)

```text
[ Ossidiana ] [   Rame    ] [ Ossidiana ]
[ Redstone  ] [OcchioEnder] [ Redstone  ]
[ Ossidiana ] [   Rame    ] [ Ossidiana ]
```

### 3.2 Sintonizzatore di Fase (Strumento di Linking)
* **Centro:** Perla di Ender (`minecraft:ender_pearl`)
* **Sopra/Sotto:** Frammento di Ametista (`minecraft:amethyst_shard`)
* **Impugnatura:** Verga di Blaze (`minecraft:blaze_rod`)

```text
[     -     ] [ Ametista  ] [     -     ]
[     -     ] [Ender Pearl] [     -     ]
[     -     ] [Verga Blaze] [     -     ]
```

---

## 4. Architettura Tecnica (Fabric API)

Il codice sarà strutturato in moduli indipendenti all'interno del package principale `com.project.pointportals`:

* **`block/TeleporterBlock.java`**: Gestisce le proprietà `BlockState` (`LINKED`, `POWERED`), gli aggiornamenti dei vicini per rilevare la Redstone (`neighborUpdate`), la collisione con le entità (`onEntityCollision`) e la pulizia del gemello alla rottura (`onStateReplaced`).
* **`block/entity/TeleporterBlockEntity.java`**: Estende `BlockEntity`. Salva su disco (NBT) le coordinate `BlockPos` del portale gemello, la `RegistryKey<World>` della dimensione di destinazione e lo stato di accoppiamento.
* **`item/TunerItem.java`**: Gestisce la logica di accoppiamento utilizzando i moderni `DataComponents` di Minecraft 1.21+ per memorizzare temporaneamente la posizione del primo portale cliccato.
* **`registry/`**: Classi dedicate alla registrazione pulita di Blocchi, Oggetti, BlockEntities, DataComponents e Suoni/Particelle.

---

## 5. Setup Ambiente su Antigravity IDE

Per inizializzare lo spazio di lavoro e avviare lo sviluppo:

1. **Prerequisiti:**
   * **Java Development Kit (JDK) 21** installato e configurato nella variabile `JAVA_HOME`.
   * **Antigravity IDE** aggiornato.
2. **Creazione del Progetto Base:**
   * Generare il template dal sito ufficiale **Fabric Template Mod Generator** selezionando la versione target di Minecraft (es. `1.21.4`), **Yarn Mappings** e l'opzione **Data Generation**.
   * Estrarre la cartella e aprirla come workspace in **Antigravity IDE**.
3. **Configurazione Gradle & Sorgenti:**
   * Aprire il terminale integrato di Antigravity ed eseguire:
     ```bash
     ./gradlew genSources
     ```
   * Questo comando scarica le librerie Fabric e decompila il codice sorgente di Minecraft per l'autocompletamento.
4. **Test di Avvio:**
   * Avviare il client di test direttamente dal terminale o dai task Gradle dell'IDE:
     ```bash
     ./gradlew runClient
     ```

---

## 6. Roadmap di Sviluppo

- [ ] **Fase 1:** Inizializzazione progetto Fabric su Antigravity IDE e registrazione scheletro Mod.
- [ ] **Fase 2:** Implementazione `TeleporterBlock` con stati `LINKED` / `POWERED` e rilevamento segnale Redstone.
- [ ] **Fase 3:** Implementazione `TeleporterBlockEntity` per persistenza dati NBT e rottura sincronizzata.
- [ ] **Fase 4:** Creazione `TunerItem` con Data Components per il linking 1-a-1.
- [ ] **Fase 5:** Logica di teletrasporto cross-dimensionale e sistema anti-loop.
- [ ] **Fase 6:** Aggiunta texture, modelli JSON (3 stati del blocco), suoni, particelle e ricette di crafting.