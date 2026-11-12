@echo off
rem ============================================================================
rem  Peluncur SIMRS Khanza RS Pelita Kasih - JDK 25 (64-bit)
rem  - Butuh Java 25 FULL (dengan JavaFX), mis. Liberica JDK 25 Full.
rem  - -Dfile.encoding=Cp1252 : charset tetap Windows-1252 seperti JDK 15
rem    (JDK 18+ bawaannya UTF-8), supaya file cetak/HTML/teks tidak berubah encoding.
rem  - --enable-native-access : menghilangkan peringatan JavaFX/WebView di JDK 25.
rem  - Urutan pencarian java: Liberica JDK 25 Full, JAVA_HOME, lalu javaw di PATH.
rem ============================================================================
cd /d "%~dp0"

set "JAVAW=javaw"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
if exist "%ProgramFiles%\BellSoft\LibericaJDK-25-Full\bin\javaw.exe" set "JAVAW=%ProgramFiles%\BellSoft\LibericaJDK-25-Full\bin\javaw.exe"

start "" "%JAVAW%" -Dfile.encoding=Cp1252 --enable-native-access=ALL-UNNAMED,javafx.graphics,javafx.web -XX:+UseG1GC -XX:+UseStringDeduplication -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar