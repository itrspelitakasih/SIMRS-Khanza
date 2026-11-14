@echo off
rem ============================================================================
rem  Peluncur SIMRS Khanza RS Pelita Kasih - JDK 25 (64-bit)
rem  - Butuh Java 25 FULL (dengan JavaFX), mis. Liberica JDK 25 Full.
rem  - -Dfile.encoding=Cp1252 : charset tetap Windows-1252 seperti JDK 15
rem    (JDK 18+ bawaannya UTF-8), supaya file cetak/HTML/teks tidak berubah encoding.
rem  - --enable-native-access : menghilangkan peringatan JavaFX/WebView di JDK 25.
rem  - Memori: -Xss2m -Xms32m -Xmx1024m. -XX:PermSize/MaxPermSize lama diganti
rem    -XX:MaxMetaspaceSize (PermSize sudah dihapus sejak Java 8, JDK 25 menolaknya).
rem  - Memakai java.exe (bukan javaw) supaya jendela terminal tetap terbuka untuk melihat log.
rem  - Urutan pencarian java: Liberica JDK 25 Full, JAVA_HOME, lalu java di PATH.
rem ============================================================================
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if exist "%ProgramFiles%\BellSoft\LibericaJDK-25-Full\bin\java.exe" set "JAVA=%ProgramFiles%\BellSoft\LibericaJDK-25-Full\bin\java.exe"

title SIMRS Khanza - log
echo Menjalankan: "%JAVA%"
"%JAVA%" -Xss2m -Xms32m -Xmx1024m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=Cp1252 --enable-native-access=ALL-UNNAMED,javafx.graphics,javafx.web -XX:+UseG1GC -XX:+UseStringDeduplication -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar
echo.
echo Aplikasi berhenti (kode keluar %ERRORLEVEL%). Log di atas bisa dicek.
pause