@echo off
rem ============================================================================
rem  Peluncur SIMRS Khanza RS Pelita Kasih - JDK 15 (komputer client)
rem  - Butuh Java 15 FULL (dengan JavaFX), mis. Liberica JDK 15 Full.
rem  - Memori: -Xss2m -Xms32m -Xmx1024m. -XX:PermSize/MaxPermSize lama diganti
rem    -XX:MaxMetaspaceSize (PermSize sudah dihapus sejak Java 8, JDK 25 menolaknya).
rem  - Memakai java.exe (bukan javaw) supaya jendela terminal tetap terbuka untuk melihat log.
rem  - Urutan pencarian java: Liberica JDK 15 Full, JAVA_HOME, lalu java di PATH.
rem  Taruh file ini di folder yang sama dengan SIMRSKhanza.jar, buat shortcut ke file ini.
rem ============================================================================
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if exist "%ProgramFiles%\BellSoft\LibericaJDK-15-Full\bin\java.exe" set "JAVA=%ProgramFiles%\BellSoft\LibericaJDK-15-Full\bin\java.exe"

title SIMRS Khanza - log
echo Menjalankan: "%JAVA%"
"%JAVA%" -Xss2m -Xms32m -Xmx1024m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=Cp1252 -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar
echo.
echo Aplikasi berhenti (kode keluar %ERRORLEVEL%). Log di atas bisa dicek.
pause