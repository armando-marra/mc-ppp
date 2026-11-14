# Point-to-Point Portals — progetto e stato

**Versione:** 0.1.2.

**Target:** Minecraft Java Edition 26.2+, Fabric Loader >= 0.19.5, Fabric API >= 0.159.0 per Minecraft 26.2.
**Toolchain:** Java 25, Gradle 9.x, Fabric Loom 1.19+, mappings ufficiali Mojang.
Le versioni effettive sono in `gradle.properties` e nel wrapper Gradle.

## Meccaniche implementate

I portali formano coppie reciproche esclusive: A punta a B e B punta ad A.
Non esistono reti, hub o collegamenti multipli. Il tuner conserva posizione
e dimensione del primo capo usando `DataComponentType<PortalTarget>`.
Un secondo click completa il legame; i capi occupati non vengono sovrascritti.
Sneak + click cancella prima una selezione presente nel tuner; con tuner vuoto
scollega una coppia. Il gemello scaricato viene riconciliato successivamente.

Il blocco ha le proprietà LINKED e POWERED, con quattro combinazioni e tre
aspetti: inattivo senza luce, standby e attivo con luce 12. Lo standby conserva
texture, particelle e suono del precedente stato attivo, ma non mostra l'ovale.
POWERED segue sempre la Redstone, anche senza collegamento. Il trasporto
richiede LINKED e POWERED alla partenza e un gemello valido e reciproco.
La rimozione del segnale interrompe l'attività del portale.

Dalla versione 0.1.2 un renderer esclusivamente client mostra
un ovale verticale largo circa 1,3 blocchi e alto 2,2 sopra la pedana attiva:
la texture animata vanilla `minecraft:block/nether_portal`, campionata dall'atlante
blocchi con colori e alpha originali, senza spirale procedurale. Un doppio bordo
viola/ciano delimita la sagoma; particelle vanilla REVERSE_PORTAL circondano
il volume dell'ovale, emesse da animateTick solo nello stato attivo e senza
dipendenza dal frame rate. L'ovale si orienta verso la camera lungo l'asse verticale, resta
visibile entro 32 blocchi e rispetta l'occlusione dei blocchi opachi. Non mostra
la destinazione, non aggiunge collisioni o luce dinamica/bloom e non modifica
il trasporto. Estrazione dello stato dal BlockState sincronizzato e disegno
separati, senza accesso al mondo nella fase di submission. Lo spegnimento o
lo scollegamento disabilitano subito il renderer quando il client riceve lo stato.

La rimozione di un portale scollega subito il gemello caricato e reciproco.
I controlli periodici, distribuiti su 40 tick, non caricano chunk remoti:
un gemello scaricato conserva il riferimento fino alla riconciliazione.
Interazioni con il tuner e tentativi di trasporto possono caricare chunk
sincronicamente e scartano riferimenti mancanti o non reciproci.

Il trasporto supporta dimensioni vanilla e custom esistenti, verifica il volume
completo di arrivo (collisioni, bordo, fuoco e lava) e rifiuta passeggeri/veicoli.
Usa il ticket vanilla temporaneo di arrivo, senza forzature permanenti dei chunk.
Il cooldown è di 60 tick dopo il trasferimento; la permanenza sulla pedana lo
rinnova a 40 tick. Occorre allontanarsi e attenderne la scadenza per tornare.
Non esiste una modalità configurabile di attivazione tramite sneak.

## Progressione e asset

La ricetta produce due portali con quattro ossidiane agli angoli, due lingotti
di rame sopra/sotto e due polveri di Redstone ai lati dell'occhio di Ender.
Il tuner usa una colonna di ametista, perla di Ender e verga di Blaze.
Il Teleporter usa nove texture originali 32×32 (alto, lato e fondo per ciascuno
dei tre stati): cornice di rame, corpo di ossidiana, rune scure da scollegato,
il nucleo ciano/viola del precedente attivo sia in standby sia da attivo.
L'item in inventario usa il modello inattivo; le quattro varianti LINKED/POWERED
mantengono la mappatura ai tre stati visivi.
Il Phase Tuner ha una texture originale trasparente 32×32: impugnatura di
ossidiana, montatura di rame, nucleo viola e inserto redstone. Il modello item
generato usa `pointportals:item/tuner`; la ricetta resta invariata.
`FabricDataGenerator` genera modelli per tutte le combinazioni di BlockState,
modelli item, ricette, avanzamenti, loot, tag e localizzazioni inglese (`en_us`),
italiana (`it_it`), spagnola (`es_es`), francese (`fr_fr`) e tedesca (`de_de`).
Nomi, suggerimenti e messaggi seguono la lingua selezionata dal client.
Suoni e particelle usano quelli vanilla.

## Architettura e sviluppo

Il package base è `com.project.pointportals` e il mod ID è `pointportals`.

- `TeleporterBlock`: stati, Redstone (`neighborChanged`, `onPlace`), attivazione
  (`stepOn`), particelle, comparatore e ispezione.
- `TeleporterBlockEntity`: reciprocità, riconciliazione, trasporto e cooldown;
  persistenza tramite `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`;
  pulizia alla rimozione tramite `preRemoveSideEffects`, senza scollegare allo scaricamento.
- `TunerItem`: interazioni e selezione tramite Data Components, senza NBT legacy degli item.
- `registry/`: registri con `Registry.register` e `BuiltInRegistries`.
- `diagnostics/PortalChunkLoads`: eventi JFR delle risoluzioni sincrone esplicite.

Usare JDK 25 in JAVA_HOME e importare il repository come progetto Gradle nell'IDE
scelto. `./gradlew genSources` prepara i sorgenti; `./gradlew build` compila ed
esegue i GameTest server; `./gradlew runClient` avvia il client.
Per gli asset eseguire anche `./gradlew runDatagen`. Non aggiungere Yarn.

## Roadmap attuale

- [x] Registri, blocchi LINKED/POWERED e Redstone.
- [x] Persistenza, tuner e coppie esclusive.
- [x] Trasporto cross-dimensione, sicurezza dell'arrivo e cooldown.
- [x] Datagen di modelli, ricette, loot, tag e lingua.
- [x] GameTest su persistenza, tuner, scaricamenti reali, ticket, giocatori ed End.
- [ ] CI con Java 25 e verifica da cache vuota.
- [x] Strumentazione JFR e banco automatizzato per confrontare chunk caricati,
  scaricati e nuovi, con verifica delle invarianti e dei tempi dei tick. Prima campagna: 1.440 operazioni; [report](docs/loading-performance-results.md).
- [ ] Conferma su server dedicato a 20 TPS secondo il
  [protocollo](docs/loading-performance.md), prima di scegliere una soluzione
  definitiva per accodamento, limiti o asincronia.

Restano distinti i [controlli manuali](docs/portal-regression-checks.md): riavvio
reale del mondo, client reale e ulteriori casi di ingombro e cooldown.
Lo stato di sviluppo corrente e lo storico sono in [MIGLIORAMENTI.md](MIGLIORAMENTI.md).
