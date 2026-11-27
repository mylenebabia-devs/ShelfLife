#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
# Supply a local org.json jar (e.g. org.json:json:20240303).
json_jar=${1:?Pass the absolute path to an org.json jar}
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
java -m jdk.compiler/com.sun.tools.javac.Main -cp "$json_jar" -d "$out" app/src/main/java/com/example/shelflife/BarcodeLookup.java app/src/main/java/com/example/shelflife/Measurement.java app/src/main/java/com/example/shelflife/LookupMetadata.java app/src/main/java/com/example/shelflife/CategorySuggestions.java app/src/test/java/com/example/shelflife/BarcodeResponseChecks.java
java -cp "$out:$json_jar" com.example.shelflife.BarcodeResponseChecks
