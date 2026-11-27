#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
java -m jdk.compiler/com.sun.tools.javac.Main -d "$out" app/src/main/java/com/example/shelflife/LookupMetadata.java app/src/test/java/com/example/shelflife/LookupChecks.java app/src/main/java/com/example/shelflife/CategorySuggestions.java app/src/test/java/com/example/shelflife/CategoryChecks.java app/src/main/java/com/example/shelflife/Measurement.java app/src/test/java/com/example/shelflife/MeasurementChecks.java app/src/main/java/com/example/shelflife/SkuGenerator.java app/src/test/java/com/example/shelflife/SkuChecks.java app/src/main/java/com/example/shelflife/Rules.java app/src/main/java/com/example/shelflife/Passwords.java app/src/main/java/com/example/shelflife/Csv.java app/src/test/java/com/example/shelflife/DomainChecks.java
java -cp "$out" com.example.shelflife.DomainChecks

java -cp "$out" com.example.shelflife.SkuChecks

java -cp "$out" com.example.shelflife.MeasurementChecks

java -cp "$out" com.example.shelflife.CategoryChecks

java -cp "$out" com.example.shelflife.LookupChecks
