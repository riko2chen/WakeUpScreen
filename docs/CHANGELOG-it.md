# Changelog

Tutte le modifiche rilevanti di WakeUpScreen sono documentate qui, organizzate per versione.

---

## [4.0.0]

### Aggiunte
- La pagina iniziale mostra quando lo schermo si è acceso con successo l'ultima volta ("adesso", minuti, ore o giorni fa), letto direttamente dal log delle notifiche; toccandolo si apre il log
- Le finestre di sonno possono valere solo per i giorni della settimana scelti, con i preset Ogni giorno / Giorni feriali / Fine settimana. Una finestra che scavalca la mezzanotte appartiene alla sera in cui inizia, quindi venerdì 23:00 - 07:00 copre comunque il sabato mattina. Le finestre esistenti continuano a valere ogni giorno
- Nuova regola "Silenzio con batteria scarica": sotto un livello di batteria a scelta (5-50%) e senza carica in corso, le notifiche non accendono più lo schermo. In carica passano sempre
- Nuova regola "Silenzio a faccia in giù": mentre il telefono è appoggiato con lo schermo verso il basso — rilevato dall'accelerometro, lo stesso schema che la modalità tasca usa con il sensore di prossimità — le notifiche non accendono lo schermo
- "Bagliore notturno": durante le finestre di sonno, un debole bagliore rosso opzionale (luminosità minima, sfondo nero) appare per un attimo invece del buio totale, per chi è reperibile o ha un neonato. Rosso perché è il colore che disturba meno gli occhi abituati al buio; registrato nel log con un suo stato dedicato
- Statistiche di attenzione: conteggiate interamente sul dispositivo a partire da due soli eventi — l'accensione dello schermo e lo sblocco del dispositivo entro mezzo minuto — l'app mostra quali app accendono lo schermo senza mai essere guardate negli ultimi 30 giorni, e offre l'aggiunta alla blacklist con un tocco per le peggiori. Il contenuto delle notifiche non viene mai letto
- Esportazione e importazione delle impostazioni come file JSON, tramite il selettore di file di sistema, senza nuovi permessi. Il formato del file include un numero di versione: le impostazioni più recenti vengono semplicemente ignorate dalle build più vecchie, e un file in un formato più nuovo viene rifiutato con un messaggio chiaro invece di essere applicato a metà
- Changelog nell'app: dopo un aggiornamento, il primo avvio mostra un breve riepilogo di cosa è cambiato — coprendo ogni versione saltata, non solo l'ultima. Si può riaprire in qualsiasi momento da Impostazioni → Info → Cronologia versioni, e le voci delle impostazioni aggiunte da un aggiornamento portano un puntino finché non vengono visitate

### Modifiche
- Entrambe le nuove regole compaiono nel diagramma del processo di verifica delle notifiche e nel log delle notifiche, come ogni altra regola
- La durata schermo acceso personalizzata non spegne più lo schermo quando il blocco schermo è già stato tolto: se hai sbloccato durante la finestra (o il tuo blocco schermo è impostato su "Nessuno"), la finestra termina senza bloccare. In precedenza un broadcast di sblocco perso poteva bloccare il telefono mentre lo si stava usando
- La durata minima dello schermo acceso personalizzata è ora di 5 secondi (era 3). Il controllo del blocco schermo descritto sopra ha bisogno che passino i primi istanti dopo un'accensione prima di essere affidabile su alcuni dispositivi; le impostazioni esistenti più brevi vengono portate a 5 automaticamente
- Durata schermo acceso personalizzata: il permesso di accessibilità mancante è ora segnalato con un avviso rosso ben visibile, e uscendo dalla pagina con l'interruttore attivo ma senza permesso compare una finestra che propone di attivarlo o di uscire comunque

### Note
- Nessun nuovo permesso
- Le finestre di sonno salvate da questa versione senza restrizioni sui giorni della settimana restano leggibili dalla 3.2.0; le finestre limitate a certi giorni vengono scartate dalle versioni più vecchie invece di essere lette male

---

## [3.2.0]

### Aggiunte
- La modalità sonno accetta più finestre, aggiunte e rimosse una per una, così una finestra notturna e un riposino pomeridiano possono coesistere
- Le finestre di sonno si impostano al minuto invece che all'ora piena
- La pagina degli orari di sonno mostra la giornata come un anello di 24 ore, con le finestre di sonno in grigio, il momento attuale evidenziato e tacche a 0, 6, 12 e 18
- L'aggiunta di una finestra viene verificata rispetto a quelle esistenti, indicando per nome la finestra con cui entrerebbe in conflitto invece di accettare in silenzio una sovrapposizione

### Modifiche
- I colori sono ricostruiti su Material 3. Invece di scrivere ogni valore a mano, intere gamme tonali vengono generate dai colori del brand e i temi chiaro e scuro leggono ciascuno i toni prescritti da M3, così i due non possono più divergere
- Le barre superiori abbandonano il gradiente per un colore piatto. Il colore della barra di stato e la luminosità dei suoi simboli seguono la pagina corrente: la sezione principale della home mantiene il suo gradiente profondo, tutto il resto segue il tema chiaro o scuro
- Le card, il campo di ricerca e la barra superiore usano ora i livelli di superficie di M3, così restano distinguibili dallo sfondo in modalità scura
- L'anello delle 24 ore etichetta ogni ora da 0 a 23, dove prima segnava solo 0, 6, 12 e 18
- Una finestra di sonno va dal suo inizio fino alla fine esclusa, quindi 02:00 - 04:00 termina alle 03:59. Due finestre possono perciò toccarsi, una che inizia esattamente dove l'altra finisce
- Una singola finestra esistente viene convertita automaticamente all'aggiornamento e continua a comportarsi allo stesso modo
- "Optimize Ongoing Notify" è ora "Blocca notifiche in corso" e "Radical Ongoing Detect" è ora "Blocca notifiche non cancellabili". Entrambe hanno un pulsante "?" che spiega cosa ferma ciascuna e in cosa differiscono
- Le impostazioni sono riorganizzate in Generali, Avanzate, Diagnostica e Info. Generali contiene la lingua e la nuova modalità scura; Avanzate contiene il processo di verifica delle notifiche e la pagina delle regole di accensione; Diagnostica contiene il test di accensione e un accesso diretto al log
- Nuova impostazione della modalità scura con tre opzioni: segui il sistema, chiaro, scuro. Il valore predefinito è seguire il sistema, quindi l'aspetto non cambia all'aggiornamento
- La durata schermo acceso e il filtro app (modalità, whitelist, blacklist) sono passati dalla pagina principale delle impostazioni alla pagina delle regole di accensione, che ora raccoglie tutto ciò che decide se lo schermo si accende
- Diverse voci sono state rinominate per dire cosa fanno davvero: "Advanced Setting" è ora "Regole di accensione", "Block Chain" è ora "Processo di verifica delle notifiche", "Function Test" è ora "Test di accensione" e "Current Mode" è ora "Modalità filtro". Sono cambiate solo le etichette, ogni impostazione conserva il suo valore

### Correzioni
- Corretto "Radical Ongoing Detect" (Impostazioni → Regole di accensione) che scriveva sulla chiave di preferenza sbagliata: attivarlo o disattivarlo cambiava in silenzio l'impostazione semplice "Ongoing Detect", e l'interruttore radicale stesso non veniva mai salvato — tornava attivo ogni volta che si riapriva la pagina. Ora i due interruttori sono indipendenti e vengono salvati correttamente

### Note
- Radical Ongoing Detect era di fatto sempre attivo prima di questa correzione e resta attivo per impostazione predefinita, quindi il filtraggio delle notifiche non cambia all'aggiornamento
- Se hai mai toccato l'interruttore radicale, l'impostazione semplice "Ongoing Detect" potrebbe essere stata cambiata a tua insaputa. Vale la pena dare a entrambi gli interruttori una rapida occhiata in Impostazioni → Regole di accensione

---

## [3.1.1]

### Modifiche
- L'app ora ha come target Android 16 (API 36). Verificato su un'immagine di sistema Android 16: la durata schermo acceso precisa, l'azione di blocco schermo tramite accessibilità e il fallback senza permessi si comportano tutti esattamente come prima
- Le pagine secondarie usano ora l'animazione di ritorno predittivo di Android invece della transizione a scorrimento dell'app. Deriva dal target Android 16 ed è un cambiamento solo visivo — la navigazione indietro in sé è invariata

### Note
- Nessun nuovo permesso, nessuna modifica funzionale
- Verificato su un'immagine di dispositivo con la nuova dimensione di pagina di memoria da 16 KB

---

## [3.1.0]

### Funzionalità
- Aggiunto "Promemoria ripetuto" (Impostazioni → Regole di accensione): finché ci sono notifiche non lette, lo schermo si riaccende ogni 5–60 minuti e smette non appena vengono cancellate. L'intervallo e il numero massimo di promemoria per serie sono configurabili, e la modalità sonno, Non disturbare, la modalità tasca, la modalità solo in carica e il filtro app hanno comunque la precedenza. Le notifiche in corso non contano mai come non lette
- Le accensioni da promemoria vengono registrate nel log delle notifiche, e il test di accensione può eseguire subito un promemoria invece di aspettare l'intervallo
- Aggiunta "Durata schermo acceso personalizzata" (Impostazioni → Durata schermo acceso personalizzata): lo schermo viene tenuto acceso per 3, 5, 10, 15 o 30 secondi e poi spento, invece di essere lasciato al timeout schermo di sistema. Per spegnere lo schermo si usa l'azione di blocco schermo del sistema stesso tramite un servizio di accessibilità opzionale, che non legge alcun contenuto dello schermo

### Modifiche
- La durata schermo acceso personalizzata è disattivata per impostazione predefinita e non fa nulla finché è disattivata, quindi le installazioni esistenti si comportano esattamente come prima — la vecchia implementazione rilasciava subito il suo wake lock e non applicava mai il valore configurato

### Note
- Nessun nuovo permesso. I promemoria usano allarmi inesatti, quindi Doze può ritardarne uno di qualche minuto
- Una finestra di schermo acceso personalizzata viene annullata in anticipo se il dispositivo viene sbloccato o lo schermo si spegne, e non oscura mai il display durante una chiamata

---

## [3.0.6]

### Modifiche
- Rimosso il permesso `FOREGROUND_SERVICE_SPECIAL_USE`. L'accensione dello schermo si basa sul listener delle notifiche, che il sistema mantiene collegato automaticamente, quindi il servizio in primo piano a uso speciale non era in realtà necessario per il funzionamento di base.
- Rimossa l'impostazione opzionale "notifica persistente (mantieni il servizio attivo)", che era l'unica funzione a usare quel permesso. Sui dispositivi con una gestione aggressiva della batteria, mantenete invece l'app esente dall'ottimizzazione della batteria.

---

## [3.0.5]

### Funzionalità
- Aggiunta una pagina "Controlla aggiornamenti" (Impostazioni → Info) con le opzioni Google Play, F-Droid e GitHub, e la versione attualmente installata. Non serve alcun permesso internet — si limita ad aprire lo store o la pagina scelti.

### Correzioni
- Corretto il layout edge-to-edge / safe area su Android 15+: i pulsanti indietro non sono più irraggiungibili, i titoli non sono più nascosti dietro la barra di stato o il ritaglio della fotocamera, e la navigazione inferiore e gli elenchi ora restano liberi dalla barra dei gesti.

---

## [3.0.4]

### Correzioni
- Corretto il log delle notifiche che nelle build di rilascio mostrava sempre "Motivo sconosciuto" — il motivo del blocco viene ora risolto da un identificatore stabile invece che dal nome della classe offuscato, quindi il motivo reale (Non disturbare, carica, modalità sonno, ecc.) viene mostrato correttamente
- L'interruttore della notifica persistente ha ora effetto immediato, senza dover riavviare l'app o il servizio

### Miglioramenti
- Qualsiasi motivo di blocco non riconosciuto viene ora mostrato così com'è nel log invece di confluire in "Motivo sconosciuto", così nessuna informazione va persa durante la diagnosi dei problemi

---

## [3.0.3]

### Modifiche
- Aggiunti i metadati fastlane per F-Droid (descrizioni, screenshot, changelog in inglese, cinese e italiano)
- Spostati i file CHANGELOG nella directory `docs/`
- Aggiunta la pagina di consultazione del changelog sul sito web
- Corretto il README: Min SDK corretto ad Android 6.0 (API 23) per corrispondere al codice

---

## [3.0.2]

### Modifiche principali
- Interfaccia completamente rinnovata e ricostruita con Jetpack Compose
- Sistema visivo tutto nuovo (tema Material 3 Periwinkle Bloom)
- Nuove pagine: log delle notifiche, test di accensione, feedback
- Supporto multilingue ampliato
- Compatibilità con Android 14/15

### Interfaccia e design
- Grande ridisegno artistico dell'interfaccia — tema dark forest dawn
- Grande ridisegno artistico dell'interfaccia — tema M3 Periwinkle Bloom
- Riscritte le schermate Home e Impostazioni con Jetpack Compose
- Riscritte tutte le pagine secondarie con Jetpack Compose
- Finestre di dialogo migrate a Compose
- Migliorato SettingRow con icone finali personalizzabili e migliore gestione delle icone delle app
- Passata di revisione e ottimizzazione dell'interfaccia

### Funzionalità
- Aggiunto il filtro solo in carica alle impostazioni avanzate
- Aggiunta la pagina di feedback
- Aggiunta la pagina del test di accensione
- Aggiunta la pagina del log delle notifiche con le informazioni del canale (importanza, suono, vibrazione)
- Aggiunto il supporto multilingue

### Infrastruttura
- Aggiornati Gradle a 8.9, AGP a 8.7.3, Kotlin a 2.0.21
- Migrazione a JDK 17
- Migrate le API deprecate
- Aggiunta la compatibilità con Android 14/15
- Aggiornato MMKV a 2.4.0 (supporto alle pagine di memoria da 16 KB)
- Aggiunto il workflow CI/CD di GitHub Actions con firma degli APK e rilascio automatico
- Licenza aggiornata da MIT a GPLv3

---

## [2.2.2]

### Funzionalità
- Aggiunto il permesso runtime POST_NOTIFICATIONS per Android 13+

### Documentazione
- Aggiunto il documento sull'informativa sulla privacy

---

## [2.2.1]

### Refactoring
- Migrati tutti i moduli da synthetic a View Binding
- Aggiunte utilità di estensione per ViewBinding

### Altro
- Aggiornamenti delle dipendenze

---

## [2.2.0]

### Funzionalità
- Aggiunto il supporto alla modalità scura
- Aggiunte configurazioni più fini per la modalità scura

### Prestazioni
- Abilitata la riduzione del codice con R8
- Rimossa la libreria Play Core per ridurre la dimensione dell'APK

### Altro
- Aggiornamenti delle dipendenze

---

## [2.1.1]

### Modifiche
- Cambiato il valore predefinito dell'impostazione della notifica persistente

---

## [2.1.0]

### Funzionalità
- Aggiunta l'opzione per nascondere/chiudere la notifica persistente in primo piano
- Ottimizzato il punto di ripristino dell'attività principale

---

## [2.0.0]

### Modifiche principali
- Migrazione a un Foreground Service per impedire al sistema di terminare il listener

### Funzionalità
- Ricollegamento automatico quando il servizio listener delle notifiche si disconnette
- Rimosso il database Realm incorporato; livello dati semplificato
- Rifattorizzata la gestione delle stringhe multilingue in un unico punto centralizzato

---

## [1.9.0]

### Modifiche
- Aggiunto un tempo di attesa alla richiesta di recensione in-app per evitare richieste ripetute

---

## [1.8.0]

### Funzionalità
- Integrata l'API di recensione in-app di Google
- Spostato l'accesso alla valutazione in una posizione più visibile

### Altro
- Aggiornamenti delle dipendenze e pulizia del codice inutilizzato

---

## [1.7.0]

### Refactoring
- Rifattorizzate le regole di filtro condizionale per l'accensione dello schermo alla ricezione delle notifiche

---

## [Versioni precedenti]

### Funzionalità aggiunte nel tempo
- Whitelist / elenco filtro app: consenti solo ad app specifiche di accendere lo schermo
- Modalità tasca: impedisce l'accensione dello schermo quando il dispositivo è in tasca (sensore di prossimità)
- Modalità sonno: ore di silenzio configurabili
- Riconoscimento della modalità Non disturbare
- Guida all'ottimizzazione della batteria con accesso rapido
- Notifica persistente con servizio in primo piano
- Pagina di debug / log per diagnosticare gli eventi di notifica
- Supporto al cambio di lingua
- Struttura con navigazione inferiore
- Interruttori personalizzati
- Architettura ViewModel + LiveData
- Ottimizzazione del sensore di prossimità
