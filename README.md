<div align="center">

# 🛸 Litium Browser

### Fast. Private. Open. Community.

An open-source web browser built for speed, simplicity, and freedom.

Built as an independent Chromium-based browser for Android.

![License](https://img.shields.io/badge/License-GPLv3-green.svg)

</div>

---

# About

Litium Browser is a free and open-source web browser focused on performance, low resource usage, privacy, and independence.

Our first target is Android. The browser UI and product code will live in this
repository, while the Chromium checkout will be kept separately because it is
very large and has its own source/dependency workflow.

We believe that the web should be open, fast, and accessible to everyone.

---

# Why Litium?

- 🚀 Extremely fast
- 💚 Lightweight and optimized
- 🌍 Free and Open Source
- 🌐 Chromium-based Android engine
- 👥 Community-driven
- 🔒 Privacy-focused
- 🚫 Independent from Google and large corporations
- ⚡ Built for the future

---

# Vision

We are building Litium Browser to become a modern browser that is:

- Fast
- Stable
- Lightweight
- Community-first
- Open Source forever

We want everyone to be able to contribute and help shape the future of the browser.

---

## Engine

Litium Browser is based on **Chromium**. We will keep upstream Chromium changes
separate from Litium-specific changes and document every pinned revision.

The official Chromium source is fetched with `depot_tools`; see
[CHROMIUM_ANDROID.md](CHROMIUM_ANDROID.md) before starting a checkout.

## Local Android prototype

The first prototype uses Android's Chromium-based `WebView`, so it can be
developed in this workspace without downloading the full Chromium source:

```bash
export ANDROID_HOME="$HOME/android-sdk"
gradle :app:assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

The prototype uses a privacy-focused search provider behind a Litium-owned
interface with a quiet Windows 8/Xbox home style and a landscape background. WebView third-party cookies are blocked, its
history is cleared when the app starts while normal WebView cache is retained
for speed, and local file/content access is disabled. These are privacy defaults, not a guarantee that every website
will be tracking-free.

The menu button opens local settings with counts for blocked ads, trackers, and
recent sites. Only hostnames and counters are stored for this screen; clearing
the statistics removes them from the device.

The settings screen also exposes Android's standard default-browser chooser only
after the user taps the action. The manifest includes `http` and `https` intent
filters so links opened from other apps can be handled by Litium.

All Litium Android source, resources, Gradle files, and the build instructions
are kept in this repository. The full Chromium checkout is intentionally kept
outside the repository because of its size.

---

# Platforms

### Initial release

- 📱 Android

### Later

- 🖥️ Desktop platforms
- 🍎 macOS
- 🍏 iOS (future)

---

# Open Source

Litium Browser will always be open source.

Everyone is welcome to:

- Report bugs
- Suggest ideas
- Improve the code
- Submit Pull Requests
- Join the community

---

# License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

This ensures Litium Browser and all improvements remain open for everyone.

---

# Roadmap

- [x] Android prototype with Chromium WebView
- [x] Private search shell and Windows 8/Xbox home style
- [x] Basic ad and tracker domain blocking
- [x] Privacy settings and per-site blocking statistics
- [x] Default-browser intent support
- [ ] Rename package and application branding
- [ ] Basic browser window polish
- [ ] Evaluate a full Chromium checkout
- [ ] Tabs
- [ ] Downloads
- [ ] Extensions support
- [ ] Settings
- [ ] Android release
- [ ] Desktop releases

---

# Founders

Created with ❤️ by the Litium Browser founders.

Together with our community, we are building the future of the open web.

---

## ⭐ Join us

If you like this project, give it a ⭐ and help us build the future of browsing.

**Fast. Free. Open. Community.**
