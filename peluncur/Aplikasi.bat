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
rem  - Bila java yang ditemukan versi lama (di bawah 19, mis. JDK 15 di komputer client),
rem    opsi khusus JDK 25 tidak dikenali: peluncur pindah ke "Jalankan SIMRS Khanza.bat" (JDK 15).
rem ============================================================================
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if exist "%ProgramFiles%\BellSoft\LibericaJDK-25-Full\bin\java.exe" set "JAVA=%ProgramFiles%\BellSoft\LibericaJDK-25-Full\bin\java.exe"

rem  Versi utama java (mis. "25.0.1" -> 25, "15.0.2" -> 15, "1.8.0_x" -> 1).
set "JMAJOR=0"
for /f "tokens=3" %%v in ('call "%JAVA%" -version 2^>^&1 ^| findstr /i "version"') do set "JVER=%%~v"
for /f "delims=." %%m in ("%JVER%") do set "JMAJOR=%%m"

if %JMAJOR% LSS 19 (
    echo Java yang ditemukan versi %JVER%, bukan JDK 25: memakai peluncur JDK 15.
    if exist "Jalankan SIMRS Khanza.bat" (
        call "Jalankan SIMRS Khanza.bat"
        exit /b
    )
    title SIMRS Khanza - log
    echo Menjalankan: "%JAVA%"
    "%JAVA%" -Xss2m -Xms32m -Xmx1024m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=Cp1252 -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar
    echo.
    echo Aplikasi berhenti (kode keluar %ERRORLEVEL%). Log di atas bisa dicek.
    pause
    exit /b
)

rem  Cache kelas (CDS) otomatis di cache\simrskhanza-jdk25.jsa; dibuat ulang sendiri bila jar berubah.
if not exist cache mkdir cache

title SIMRS Khanza - log
echo Menjalankan: "%JAVA%"
"%JAVA%" -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=cache\simrskhanza-jdk25.jsa -Xss2m -Xms32m -Xmx1024m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=Cp1252 --enable-native-access=ALL-UNNAMED,javafx.graphics,javafx.web -XX:+UseG1GC -XX:+UseStringDeduplication -Dsun.java2d.opengl=true -jar SIMRSKhanza.jar
echo.
echo Aplikasi berhenti (kode keluar %ERRORLEVEL%). Log di atas bisa dicek.
pause
