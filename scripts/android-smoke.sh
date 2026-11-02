#!/usr/bin/env bash
set -euo pipefail
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb install -r android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w cn.weiping.niu.test/cn.weiping.funddiary.SmokeTest | tee release/device-test.txt
adb shell screencap -p /sdcard/niu-smoke.png
adb pull /sdcard/niu-smoke.png release/niu-smoke.png
adb logcat -d -t 1000 > release/emulator-log.txt
grep -q 'NIU_SMOKE_PASS' release/device-test.txt
adb pull /sdcard/Android/data/cn.weiping.niu/files/screenshots/niu-android.png release/niu-android.png
