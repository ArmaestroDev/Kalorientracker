# Kalorientracker für Android

Ein nativer und einfach zu bedienender Kalorientracker für Android. Diese App wurde entwickelt, um die tägliche Nahrungsaufnahme unkompliziert zu protokollieren und Fitnessziele zu unterstützen. Das Projekt ist in Android Studio entwickelt und nutzt [Haupt-Programmiersprache, z.B. Kotlin oder Java].

<img src="https://github.com/user-attachments/assets/e2c36eda-d5c5-457c-8380-2fe6071a2222" alt="App Screenshot" width="350"/>
---

## Inhaltsverzeichnis

- [Über das Projekt](#über-das-projekt)
  - [Funktionen](#funktionen)
  - [Technologie-Stack](#technologie-stack)
- [Erste Schritte](#erste-schritte)
  - [Voraussetzungen](#voraussetzungen)
  - [Installation & Ausführung](#installation--ausführung)
- [Verwendung](#verwendung)
- [Zukünftige Entwicklungen](#zukünftige-entwicklungen)
- [Mitwirkung](#mitwirkung)
- [Kontakt](#kontakt)

---

## Über das Projekt

Dieses Projekt ist eine native Android-Anwendung, die es Benutzern ermöglicht, ihre tägliche Kalorien-, Protein-, Kohlenhydrat- und Fettaufnahme, sowie ihr Aktivitätslevel zu verfolgen. Ziel war es, eine minimalistische und performante Alternative zu überladenen Fitness-Apps direkt für das Android-Betriebssystem zu schaffen.

### Funktionen

-   ✔️ **Tagesprotokoll:** Erfasse Mahlzeiten und Snacks für den aktuellen Tag.
-   ✔️ **Nährwertübersicht:** Automatische Berechnung der Gesamt-Makronährstoffe und Kalorien.
-   ✔️ **Persistente Datenspeicherung:** Einträge werden lokal auf dem Gerät gespeichert.
-   ✔️ **Natives Design:** Eine saubere, an Android-Designrichtlinien orientierte Benutzeroberfläche.

### Technologie-Stack

Dieses Projekt wurde mit den folgenden Technologien umgesetzt:

-   **Sprache:** Kotlin
-   **UI:** Jetpack Compose
-   **Kernkomponenten:** Android SDK, Room Database, Gemini API, OpenFoodFacts-API (siehe [https://github.com/openfoodfacts/openfoodfacts-server](https://github.com/openfoodfacts/openfoodfacts-server))

---

## Erste Schritte

Folge diesen Schritten, um das Projekt lokal auf deinem Rechner einzurichten und auf einem Emulator oder einem physischen Gerät auszuführen.

### Voraussetzungen

Stelle sicher, dass die folgende Software auf deinem System installiert ist:

-   **Android Studio:** Die offizielle IDE für die Android-Entwicklung. (Neueste stabile Version empfohlen)
-   **Android SDK:** Mindestens API-Level [z.B. 21] oder höher. (Wird normalerweise mit Android Studio installiert)

### Installation & Ausführung

1.  **Klone das Repository:**
    ```sh
    git clone https://github.com/ArmaestroDev/Kalorientracker.git
    ```
2.  **Öffne das Projekt in Android Studio:**
    -   Starte Android Studio.
    -   Wähle **"Open an existing project"**.
    -   Navigiere zum geklonten `Kalorientracker`-Ordner und wähle ihn aus.
3.  **Abhängigkeiten synchronisieren (Gradle Sync):**
    -   Android Studio wird automatisch versuchen, alle notwendigen Abhängigkeiten (Libraries) herunterzuladen. Dieser Prozess heißt "Gradle Sync". Warte, bis er abgeschlossen ist. Du siehst den Fortschritt in der unteren Statusleiste.
4.  **Die App ausführen:**
    -   Wähle ein verfügbares Gerät aus der Dropdown-Liste oben aus (entweder einen konfigurierten Emulator oder ein per USB verbundenes Android-Gerät).
    -   Klicke auf den grünen **"Run 'app'"**-Button (das Dreieck-Symbol).

Die App wird nun auf dem ausgewählten Gerät oder Emulator installiert und gestartet.

### Alternative

Installiere die APK bei den Releases auf einem Android Gerät oder Emulator.


---

## Verwendung

Nachdem die App gestartet ist, kannst du beginnen, deine Mahlzeiten hinzuzufügen. Zuvor musst du deine Profildaten eingeben, sowie auch deinen Gemini API Key, den sich jeder kostenlos besorgen kann: [https://ai.google.dev/gemini-api/docs/api-key](https://ai.google.dev/gemini-api/docs/api-key)

1.  **Mahlzeit hinzufügen:** Tippe auf den entsprechenden Button, um eine neue Mahlzeit einzugeben. Oder scanne das Produkt mit dem Barcode-Scanner und gebe die Grammmenge ein!
2.  **Aktivitäten hinzufügen:** Tippe auf den entsprechenden Button, um eine neue Aktivität einzugeben.
3.  **Werte eintragen:** Gib den Namen des Lebensmittels und die Nährwerte in die dafür vorgesehenen Felder ein.
4.  **Speichern:** Bestätige die Eingabe. Die Hauptansicht aktualisiert sich automatisch und zeigt die neuen Gesamtwerte für den Tag an.

---

## Zukünftige Entwicklungen

Ideen für zukünftige Versionen und Verbesserungen:

-   [ ] Grafische Darstellung des Verlaufs mit Diagrammen (z.B. mit der MPAndroidChart-Bibliothek).
-   [ ] Setzen von individuellen Tageszielen in den Einstellungen.
-   [ ] Benutzung von generativer KI für Tagesübersichten, sowie Ratschläge und Empfehlungen zum Erreichen deiner Ziele.

---

## Mitwirkung

Beiträge sind das, was die Open-Source-Community zu einem so großartigen Ort zum Lernen, Inspirieren und Gestalten macht. Jeder Beitrag, den du leistest, wird **sehr geschätzt**.

Wenn du einen Vorschlag hast, wie dieses Projekt verbessert werden könnte, erstelle bitte einen Fork des Repos und einen Pull Request.

1.  Forke das Projekt
2.  Erstelle deinen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3.  Commite deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4.  Pushe zum Branch (`git push origin feature/AmazingFeature`)
5.  Öffne einen Pull Request

---

## Kontakt

ArmaestroDev - Finde mich auf GitHub

Projekt-Link: [https://github.com/ArmaestroDev/Kalorientracker](https://github.com/ArmaestroDev/Kalorientracker)
