<div align="center">

<img src="app/src/main/ic_launcher-web.png" width="120" />

# WakeUpScreen

**Il tuo schermo, sveglio quando conta.**

Un'app Android open-source che accende delicatamente il display nel momento in cui arriva una notifica.
Mantenuta dal 2019, con oltre 10.000 utenti in 119 paesi e regioni.

[![Google Play](https://img.shields.io/badge/Google%20Play-Scarica-2dd4a8?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen)
[![GitHub](https://img.shields.io/badge/Codice-GitHub-6366f1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/riko2chen/WakeUpScreen)
[![Website](https://img.shields.io/badge/Sito-Visita-0ea5e9?style=for-the-badge&logo=googlechrome&logoColor=white)](https://riko2chen.github.io/WakeUpScreen/)
[![Changelog](https://img.shields.io/badge/Changelog-Vedi-a855f7?style=for-the-badge)](docs/CHANGELOG-it.md)
[![License](https://img.shields.io/badge/Licenza-GPLv3-a855f7?style=for-the-badge)](LICENSE)

[English](README.md) · [简体中文](README-zh.md) · [繁體中文](README-zh-TW.md) · [Italiano](README-it.md) · [日本語](README-ja.md) · [한국어](README-ko.md) · [ไทย](README-th.md)

</div>

---

## Funzionalità

| | Funzione | Descrizione |
|---|---|---|
| :bell: | **Attivazione Istantanea** | Lo schermo si illumina nel momento in cui arriva una notifica. |
| :sun_with_face: | **Modalità Tasca** | Rileva quando il telefono è in tasca o in borsa e lascia lo schermo spento. |
| :repeat: | **Promemoria Ripetuto** | Si riaccende ogni 5–60 minuti finché le notifiche restano non lette. |
| :hourglass_flowing_sand: | **Durata Schermo Personalizzata** | Tiene lo schermo acceso da 5 a 30 secondi invece di affidarsi al timeout di sistema. |
| :mag: | **Filtro App** | Scegli esattamente quali app possono attivare lo schermo. |
| :chart_with_upwards_trend: | **Statistiche di Attenzione** | Mostra quali app accendono lo schermo senza mai essere guardate, contate sul dispositivo negli ultimi 30 giorni. |
| :clipboard: | **Registro Notifiche** | Registra ogni notifica e quale regola l'ha fermata, con un diagramma dal vivo del processo di controllo. |
| :new_moon: | **Modalità Scura** | Un'interfaccia scura perfetta per qualsiasi display AMOLED. |
| :closed_lock_with_key: | **Nessun Internet** | Funziona interamente sul dispositivo, senza raccogliere dati né contattare server. |
| :zap: | **Leggera** | Impatto minimo e consumo di batteria trascurabile. |

## Come Funziona

```
1. Installa e concedi il permesso
   └─ Serve solo l'accesso alle notifiche.
      I tuoi dati non lasciano mai il dispositivo.

2. Scegli le tue app
   └─ Seleziona quali app possono attivare lo schermo. Lascia passare i messaggi
      importanti, filtra il rumore. Regola in qualsiasi momento.

3. Ecco fatto — vivi la tua vita
   └─ WakeUpScreen funziona silenziosamente in background. Quando arriva una notifica,
      lo schermo si accende. Quando il telefono è in tasca, resta spento. Semplice.
```

> **Sui permessi.** L'accesso alle notifiche è l'unico permesso necessario al funzionamento, e l'app
> non richiede alcun permesso di rete. L'unica eccezione è la Durata Schermo Personalizzata: su
> Android 9 e successivi solo un servizio di accessibilità può spegnere lo schermo in anticipo, quindi
> quella singola funzione chiede l'accesso all'accessibilità. È disattivata per impostazione
> predefinita, il servizio non legge alcun contenuto dello schermo e tutto il resto funziona senza.

## Altre Funzionalità

Tutte attivabili e disattivabili a piacere:

- **Modalità Sonno** — più finestre silenziose al giorno, precise al minuto, eventualmente limitate a certi giorni della settimana
- **Bagliore Notturno** — durante una finestra di sonno, un breve bagliore rosso tenue al posto del buio totale
- **Silenzio con Batteria Scarica** — sotto una soglia di batteria scelta e senza ricarica, lo schermo non si accende
- **Silenzio a Faccia in Giù** — mentre il telefono è appoggiato con lo schermo verso il basso, resta spento
- **Rilevamento Non Disturbare** — sospende l'attivazione mentre il DND di sistema è attivo
- **Solo in Ricarica** — accende lo schermo soltanto mentre il telefono è collegato alla corrente
- **Filtro Notifiche Persistenti** — ignora navigazione, musica e altre notifiche di lunga durata
- **Backup delle Impostazioni** — esporta e importa tutte le impostazioni come file JSON, senza permessi aggiuntivi
- **Test di Attivazione** — attiva lo schermo, o un singolo promemoria ripetuto, sul momento invece di aspettare una notifica

## Screenshot

<div align="center">
<img src="screenshots/main-en.png" width="720" />
</div>

## Compatibilità

- **Funziona su**: Android 6.0 e successivi
- **Ottimizzata per**: Android 16
- **Lingue**: Italiano, English, 简体中文, 繁體中文, 日本語, 한국어, ไทย

## Contribuire

I contributi sono benvenuti! Sentiti libero di aprire issue o inviare pull request.

## Licenza

Questo progetto è rilasciato sotto la [GNU General Public License v3.0](LICENSE) — sei libero di
usarlo, studiarlo, modificarlo e condividerlo, e tutto ciò che ne distribuisci deve restare
open source alle stesse condizioni.

---

<div align="center">

**WakeUpScreen** di [Riko Lab](mailto:symeonchen@gmail.com)

<sub>Precedentemente ospitata su <https://github.com/SymeonChen/WakeUpScreen> — quell'URL reindirizza ancora qui. Stesso account, username precedente.</sub>

</div>
