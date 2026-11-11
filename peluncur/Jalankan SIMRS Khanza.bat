@echo off
rem ============================================================================
rem  Peluncur SIMRS Khanza RS Pelita Kasih (JDK 25)
rem  - Butuh Java 25 FULL (dengan JavaFX), mis. Liberica JDK 25 Full, 64-bit.
rem  - -Dfile.encoding=COMPAT : charset default tetap Windows-1252 seperti JDK 15,
rem    supaya file cetak/HTML/teks yang ditulis aplikasi tidak berubah encoding.
rem  - --enable-native-access : menghilangkan peringatan JavaFX/WebView di JDK 25.
rem  Taruh file ini di folder yang sama dengan SIMRSKhanza.jar, buat shortcut ke file ini.
rem ============================================================================
cd /d "%~dp0"

set "JAVAW=javaw"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"

start "" "%JAVAW%" -Dfile.encoding=COMPAT --enable-native-access=ALL-UNNAMED,javafx.graphics,javafx.web -XX:+UseG1GC -XX:+UseStringDeduplication -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar
