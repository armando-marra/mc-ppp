# Point-to-Point Portals — progetto e stato

**Target:** Minecraft Java Edition 26.2+, Fabric Loader >= 0.20.1.
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
aspetti: inattivo senza luce, standby con luce 3 e attivo con luce 12.
POWERED segue sempre la Redstone, anche senza collegamento. Il trasporto
richiede LINKED e POWERED alla partenza e un gemello valido e reciproco.
La rimozione del segnale interrompe l'attività del portale.

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
`FabricDataGenerator` genera modelli per tutte le combinazioni di BlockState,
modelli item, ricette, avanzamenti, loot, tag e localizzazione inglese.
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
- [ ] Prestazioni: strumentazione iniziale e [protocollo](docs/loading-performance.md);
  campagna comparativa da eseguire prima di scegliere accodamento, limiti o asincronia.

Restano distinti i [controlli manuali](docs/portal-regression-checks.md): riavvio
reale del mondo, client reale e ulteriori casi di ingombro e cooldown.
Lo stato di sviluppo corrente e lo storico sono in [MIGLIORAMENTI.md](MIGLIORAMENTI.md).
