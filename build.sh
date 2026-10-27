#!/usr/bin/env bash
# CORE//BREACH :: lokaler APK-Builder
#   ./build.sh            -> APK im Docker-Container bauen (kein Android SDK noetig)
#   ./build.sh native     -> APK mit lokal installiertem Android SDK bauen
#   ./build.sh desktop    -> Spiel am PC starten (Testen ohne Handy)
#   ./build.sh test       -> Level-Validator
#   ./build.sh install    -> APK per adb aufs verbundene Handy
#   ./build.sh clean
#
# Optional Release-Signing: KEYSTORE_FILE, KEYSTORE_PASS, KEY_ALIAS, KEY_PASS exportieren.
set -euo pipefail
cd "$(dirname "$0")"

MODE="${1:-docker}"
IMG="corebreach-builder:latest"
R='\033[38;2;218;41;28m'; W='\033[1;37m'; D='\033[0;90m'; N='\033[0m'

banner() {
  echo -e "${W}  ___ ___  ___ ___   ${R}//${W}  ___ ___ ___   _   ___ _  _ ${N}"
  echo -e "${W} / __/ _ \\| _ \\ __| ${R}//${W}  | _ ) _ \\ __| /_\\ / __| || |${N}"
  echo -e "${W}| (_| (_) |   / _|  ${R}//${W}  | _ \\   / _| / _ \\ (__| __ |${N}"
  echo -e "${W} \\___\\___/|_|_\\___|${R}//${W}   |___/_|_\\___/_/ \\_\\___|_||_|${N}"
  echo -e "${D}  apk builder :: mode=${MODE}${N}\n"
}

collect() {
  mkdir -p dist
  local apk
  apk=$(find android/build/outputs/apk/release -name '*.apk' 2>/dev/null | head -n 1 || true)
  if [[ -z "$apk" ]]; then echo -e "${R}>> keine APK gefunden${N}"; exit 1; fi
  cp "$apk" dist/CORE-BREACH.apk
  echo -e "\n${W}>> PAYLOAD READY:${N} dist/CORE-BREACH.apk ($(du -h dist/CORE-BREACH.apk | cut -f1))"
}

banner
chmod +x gradlew

case "$MODE" in
  docker)
    command -v docker >/dev/null || { echo -e "${R}>> docker fehlt. Nutze './build.sh native' oder GitHub Actions.${N}"; exit 1; }
    docker image inspect "$IMG" >/dev/null 2>&1 || docker build -t "$IMG" builder/
    KS_MOUNT=()
    if [[ -n "${KEYSTORE_FILE:-}" && -f "${KEYSTORE_FILE}" ]]; then
      KS_MOUNT=(-v "$(realpath "$KEYSTORE_FILE")":/keystore/release.jks:ro -e KEYSTORE_FILE=/keystore/release.jks)
    fi
    docker run --rm -t \
      -v "$PWD":/src \
      -v "$HOME/.gradle":/root/.gradle \
      ${KS_MOUNT[@]+"${KS_MOUNT[@]}"} \
      -e KEYSTORE_PASS -e KEY_ALIAS -e KEY_PASS \
      "$IMG" bash -c "./gradlew --no-daemon core:test android:assembleRelease"
    collect
    ;;
  native)
    ./gradlew core:test android:assembleRelease
    collect
    ;;
  desktop) ./gradlew desktop:run ;;
  test)    ./gradlew core:test ;;
  install)
    [[ -f dist/CORE-BREACH.apk ]] || { echo -e "${R}>> erst bauen${N}"; exit 1; }
    adb install -r dist/CORE-BREACH.apk
    ;;
  clean)   ./gradlew clean; rm -rf dist android/libs ;;
  *) echo "usage: $0 [docker|native|desktop|test|install|clean]"; exit 1 ;;
esac
