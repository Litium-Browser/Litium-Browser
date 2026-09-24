# Chromium Android setup

Litium uses the official Chromium source tree. Do not copy Chromium into this
repository: a checkout and its build output are large, and Chromium is managed
with `depot_tools` rather than a normal Android Studio project.

## Requirements

- Linux x86-64 host with a case-sensitive filesystem
- At least 100 GB of free disk space; more is recommended for build output
- At least 16 GB RAM; Chromium builds are more comfortable with 32 GB+
- Git, Python 3, and the Android build dependencies from Chromium's documentation
- An Android device or emulator for installation

Use the current official requirements for the exact Chromium revision. Chromium
changes its supported toolchain over time.

## Fetch the source

Run these commands outside this repository, for example under `~/src`:

```bash
mkdir -p ~/src
cd ~/src
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
export PATH="$HOME/src/depot_tools:$PATH"
mkdir chromium && cd chromium
fetch android
cd src
gclient sync
```

For a persistent shell setup, add the `depot_tools` path to your shell profile.
The `fetch android` command is the official Chromium bootstrap and may take a
long time and several tens of gigabytes of storage.

## First build

From `~/src/chromium/src`:

```bash
./build/install-build-dep-android.sh
gn args out/Default
autoninja -C out/Default chrome_public_apk
adb install -r out/Default/apks/ChromePublic.apk
```

The generated APK name or output path can change with the Chromium revision;
inspect `out/Default/apks` if the final command differs.

## Litium workflow

1. Build the unmodified Chromium APK and confirm it runs on a device.
2. Pin the Chromium revision used by Litium and record it in this document.
3. Make one small product change at a time: application name, package identity,
   icons, then browser UI and privacy defaults.
4. Keep Chromium's upstream history separate from Litium commits.
5. Before distributing builds, include Chromium's licenses, notices, and source
   offer as required by the licenses of Chromium and its dependencies.

The repository also contains a small Android prototype in `app/`. It uses the
Chromium WebView supplied by Android, so it can be developed with much smaller
resources than a full Chromium checkout. The prototype is the current local
development target; a full Chromium checkout remains a later engine option.