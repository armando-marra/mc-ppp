# Primo report dei caricamenti — 13 novembre 2026

La campagna automatizzata ha validato **1.440 operazioni**, con 960 esiti riusciti
e 480 rifiuti attesi su riferimenti obsoleti. Tutte le 36 celle hanno confermato
il rilascio dei target. Il riepilogo richiede un evento JFR completato per ogni
operazione, coerente con dimensione, coordinate e stato iniziale del chunk.

## Metodo e ambiente

- Minecraft 26.2, Fabric Loader 0.20.1, Fabric API 0.161.0+26.2; Java 25.0.5+8-LTS.
- Windows 11, Intel Core i7-1165G7 (4 core / 8 thread), heap massimo JVM 8.522.825.728 byte; volume D: NTFS. Tipo e velocità del disco fisico non misurati.
- Mondo GameTest nuovo, seed 0, strutture disabilitate, vista/simulazione configurate a 5. Overworld di test piatto; Nether del runner vanilla.
- 30 richieste singole, una per tick, poi una raffica di 10; tre ripetizioni, con nuovi target nel medesimo mondo. 1.200 tick iniziali di riscaldamento.
- Target distanti 32 chunk; partenza dei trasferimenti sempre in Overworld. I casi nuovi simulano riferimenti obsoleti e devono essere rifiutati.
- Il runner GameTest non garantisce cadenza wall-clock di 20 TPS. I risultati sono tempi di lavoro del banco locale, non uno SLA su un server dedicato.

Il run è `build/benchmarks/20261113-172415-3c7ea9ca`, avviato dalla base Git
`d7361d2` con modifiche locali registrate tramite patch e hash. I 24 file Java
del checkout coincidono con gli hash della campagna. La selezione delle risorse
del benchmark è stata verificata separatamente, mantenendo il manifest sorgente
valido e la selezione ordinaria delle suite di regressione.

## Confronto

Tutti i tempi nella tabella sono in **millisecondi**. Ogni p95 delle richieste
singole usa 90 campioni (30 × 3); il massimo delle raffiche usa tre tick, uno per
ripetizione. Il numero di raffiche è troppo piccolo per stimare una coda rara.
Le colonne JFR e operazione usano rilevazioni indipendenti (evento JFR e
`System.nanoTime`): nei dati compaiono piccoli scostamenti anche fra il tempo
della chiamata e quello totale. Trattare i valori come approssimativi e non
sottrarre le colonne per attribuire con precisione i costi residui.

| Percorso | Destinazione | Stato | getChunk p95, singole | Operazione p95, singole | Tick attivo p95, singole | Tick massimo, raffica | Tick p95 senza nuove richieste |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: |
| Tuner | Overworld | caricato | 0.005 | 0.343 | 77.147 | 61.015 | 61.516 |
| Tuner | Overworld | generato/scaricato | 17.804 | 17.910 | 106.541 | 160.775 | 210.861 |
| Tuner | Overworld | nuovo, riferimento obsoleto | 293.943 | 293.656 | 437.745 | 1807.062 | 58.758 |
| Teletrasporto | Overworld | caricato | 0.021 | 0.576 | 68.264 | 67.385 | 89.304 |
| Teletrasporto | Overworld | generato/scaricato | 209.924 | 210.102 | 269.550 | 1103.687 | 196.174 |
| Teletrasporto | Overworld | nuovo, riferimento obsoleto | 220.747 | 220.770 | 398.389 | 1677.623 | 69.146 |
| Tuner | Nether | caricato | 0.003 | 0.226 | 53.482 | 48.553 | 98.191 |
| Tuner | Nether | generato/scaricato | 9.740 | 9.834 | 96.897 | 155.229 | 128.805 |
| Tuner | Nether | nuovo, riferimento obsoleto | 726.513 | 725.825 | 854.488 | 4793.884 | 51.870 |
| Teletrasporto | Nether | caricato | 0.007 | 2.246 | 84.374 | 76.409 | 62.910 |
| Teletrasporto | Nether | generato/scaricato | 6034.455 | 6028.308 | 6096.137 | 31286.638 | 159.729 |
| Teletrasporto | Nether | nuovo, riferimento obsoleto | 1028.987 | 1027.876 | 1100.858 | 6923.739 | 83.265 |

## Cosa mostrano i dati

- Le chiamate esplicite sui chunk già caricati sono brevi: nessuna supera il budget proposto di 5 ms. Questo non garantisce un tick breve: il lavoro successivo al caricamento resta visibile nelle colonne dei tick.
- Le chiamate su chunk scaricati e nuovi superano spesso il budget: complessivamente **917 chiamate su 1.440** sono oltre 5 ms. Il p95 delle chiamate per teletrasporti verso chunk scaricati nel Nether è **6034,455 ms**, con massimo **8281,920 ms**.
- La raffica peggiore, sempre per teletrasporti verso chunk scaricati nel Nether, produce un tick di **31286,638 ms**. Tra i soli tick con richieste, **866 su 1.116** superano 50 ms.
- La correttezza è verificata separatamente: coppie reciproche, rifiuto e pulizia dei riferimenti mancanti, UUID/arrivo unico, coordinate e rotazione, cooldown e rifiuto del ritorno immediato. Il controllo degli arrivi avviene dopo 2–5 tick in questa campagna.
- Tutte le celle confermano i target scaricati dopo la pulizia. Il controllo si chiude tra 350 e 1856 tick; per le tre celle di teletrasporto con destinazioni caricate nel Nether i valori sono 1809, 1856 e 1779. Si tratta del tempo fino al controllo riuscito, non del timeout esatto del ticket.

## Limiti di attribuzione e prossimo esperimento

**Anche il riferimento senza nuove richieste supera 50 ms al p95 in tutte le
celle.** Non è una misura di server inattivo e stabilizzato: viene registrato
dopo la preparazione delle fixture, con chunk/portali mantenuti dal banco,
possibile lavoro di generazione/salvataggio ancora pendente e carico del sistema
locale. Non si possono attribuire alla sola mod tutti gli sforamenti dei tick,
né sottrarre semplicemente due percentili per ricavarne il costo incrementale.

Il chunk del portale generato non implica vicinato interamente pregenerato a
FULL: i ticket di arrivo possono richiedere altro lavoro. Le durate getChunk
comprendono l'attesa e il lavoro gestito dal server durante quella chiamata,
non la sola lettura da disco. Cache del sistema non svuotata, ordine fisso e
ripetizioni nello stesso processo limitano ulteriormente la generalizzazione.

Il prossimo esperimento deve usare un server dedicato a 20 TPS, area di arrivo
interamente pregenerata, attesa esplicita della quiescenza dopo generazione e
salvataggi, riferimento stabile senza richieste e copie identiche del mondo.
Confrontare prima il carico a intervallo di 20 tick con quello sostenuto a un
tick. Solo allora dimensionare limiti, accodamento o gestione asincrona; questo
incremento aggiunge misure e verifiche, senza cambiare la politica di trasporto.

## Artefatti e riproduzione

- [Riepilogo completo CSV](benchmarks/2026-11-13-loading-summary.csv): conteggi, media, p50/p95/p99, massimo, somma e superamenti del budget.
- [Conferme di rilascio CSV](benchmarks/2026-11-13-loading-lifecycle.csv).
- Nel run locale: `benchmark-operations.csv`, `benchmark-ticks.csv`, `benchmark-lifecycle.csv`, `summary.json`, JFR (58.417.057 byte), log, runtime, hardware, revisione/patch e hash dei sorgenti. Gli artefatti voluminosi restano esclusi da Git.
- Comando della campagna: `scripts/Run-LoadingBenchmark.ps1 -Samples 30 -Burst 10 -Repeats 3 -Interval 1 -WarmupTicks 1200`.
- [Protocollo e comandi](loading-performance.md). I primi run di diagnosi interrotti non sono inclusi nelle statistiche sopra.
- Build ordinaria finale riuscita: **35/35 GameTest**. Selezione isolata del benchmark verificata sulle risorse generate e classi di test/benchmark assenti dal JAR distribuito. Nessun asset modificato; datagen non necessario.
