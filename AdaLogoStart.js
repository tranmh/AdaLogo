var fenster;

function AdaLogoStart()
{
fenster = window.open (
"",
"NeuesFenster", // Name des neuen Fensters
+"toolbar=1" // Toolbar
+",location=1" // Adress-Leiste
+",directories=1" // Zusatzleisten
+",status=1" // Statusleiste
+",menubar=1" // Menü
+",scrollbars=1" // Scrollbars
+",resizable=1" // Fenstergrösse veränderbar?
+",width=650" // Fensterbreite in Pixeln
+",height=400" // Fensterhöhe in Pixeln
);

fenster.moveTo(
150, // X-Koordinate
200 // Y-Koordinate
); // der linken oberen Ecke

fenster.location.href = "adalogo.html";
//Dateiname und Pfad der Datei, die angezeigt werden soll
}

function AdaLogoWebStart()
{
fenster = window.open (
"",
"NeuesFenster", // Name des neuen Fensters
+"toolbar=1" // Toolbar
+",location=1" // Adress-Leiste
+",directories=1" // Zusatzleisten
+",status=1" // Statusleiste
+",menubar=1" // Menü
+",scrollbars=1" // Scrollbars
+",resizable=1" // Fenstergrösse veränderbar?
+",width=650" // Fensterbreite in Pixeln
+",height=400" // Fensterhöhe in Pixeln
);

fenster.moveTo(
150, // X-Koordinate
200 // Y-Koordinate
); // der linken oberen Ecke

fenster.location.href = "adalogo.jnlp";
//Dateiname und Pfad der Datei, die angezeigt werden soll
}
