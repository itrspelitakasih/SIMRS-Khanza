#!/bin/bash
# ============================================================================
#  Peluncur SIMRS Khanza RS Pelita Kasih - Ubuntu (JDK 15 Full)
#  - Cache kelas (Class Data Sharing) di cache/simrskhanza-jdk15.jsa: dibuat saat
#    aplikasi ditutup pertama kali, dipakai saat dibuka berikutnya (muat kode jauh
#    lebih cepat). Dibuat ulang otomatis bila SIMRSKhanza.jar lebih baru.
#    Hapus file .jsa untuk reset.
#  - Urutan pencarian java: JAVA_HOME, JDK 15 di /usr/lib/jvm, lalu java di PATH.
#  Taruh file ini di folder yang sama dengan SIMRSKhanza.jar, lalu: chmod +x
# ============================================================================
cd "$(dirname "$(readlink -f "$0")")" || exit 1

JAVA=java
for d in /usr/lib/jvm/*15*; do
    [ -x "$d/bin/java" ] && JAVA="$d/bin/java" && break
done
[ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ] && JAVA="$JAVA_HOME/bin/java"

mkdir -p cache
JSA=cache/simrskhanza-jdk15.jsa
if [ -f "$JSA" ] && [ "$JSA" -nt SIMRSKhanza.jar ]; then
    CDS="-XX:SharedArchiveFile=$JSA"
else
    rm -f "$JSA"
    CDS="-XX:ArchiveClassesAtExit=$JSA"
fi

echo "Menjalankan: $JAVA"
echo "Cache kelas: $CDS"
"$JAVA" $CDS -Xss2m -Xms32m -Xmx1024m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=Cp1252 -jar SIMRSKhanza.jar
echo "Aplikasi berhenti (kode keluar $?)."
