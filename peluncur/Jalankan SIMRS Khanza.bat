@echo off
rem ============================================================================
rem  Peluncur SIMRS Khanza RS Pelita Kasih - JDK 15 (komputer client)
rem  - Butuh Java 15 FULL (dengan JavaFX), mis. Liberica JDK 15 Full.
rem  - Urutan pencarian java: Liberica JDK 15 Full, JAVA_HOME, lalu javaw di PATH.
rem  Taruh file ini di folder yang sama dengan SIMRSKhanza.jar, buat shortcut ke file ini.
rem ============================================================================
cd /d "%~dp0"

set "JAVAW=javaw"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
if exist "%ProgramFiles%\BellSoft\LibericaJDK-15-Full\bin\javaw.exe" set "JAVAW=%ProgramFiles%\BellSoft\LibericaJDK-15-Full\bin\javaw.exe"

start "" "%JAVAW%" -Dfile.encoding=Cp1252 -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar