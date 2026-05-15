# 🛒 ShopWise
### Offline AI Food Allergy Scanner — Powered by Gemma 4

[![Gemma 4](https://img.shields.io/badge/Powered%20by-Gemma%204-4285F4?style=flat-square&logo=google)](https://ai.google.dev/gemma)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Offline](https://img.shields.io/badge/Works-Offline-success?style=flat-square)]()
[![Hackathon](https://img.shields.io/badge/Gemma%204%20Good-Hackathon-orange?style=flat-square)](https://www.kaggle.com/competitions/gemma-4-good-hackathon)

---

## The Problem

Millions of people with food allergies face a daily challenge: decoding tiny ingredient lists on food packaging — often written in chemical names, under poor lighting, without internet access.

A single missed ingredient can trigger a serious allergic reaction. ShopWise eliminates that risk.

---

## What is ShopWise?

ShopWise is a **fully offline Android app** that uses **Gemma 4 on-device** to analyze food ingredient labels from a photo and instantly alert users to allergens based on their personal profile.

- No internet connection required
- No data sent to any server
- Works anywhere — markets, rural areas, travel

---

## Demo

> 📹 [Watch the demo video](https://youtu.be/dIJU0kzpA1M?si=dBAeVuEPgVmJyCQd)

---

## How It Works

```
User sets allergy profile
        ↓
Point camera at ingredient label
        ↓
Gemma 4 (on-device via LiteRT) analyzes composition
        ↓
Instant result: ✅ Safe  or  ⚠️ Contains allergen
```

---

## Features

| Feature | Description |
|---|---|
| 📵 100% Offline | Runs entirely on-device, no internet needed |
| 🤖 Gemma 4 on-device | Multimodal AI via Google LiteRT inference |
| 👤 Multiple profiles | Separate allergy profiles per family member |
| ⚡ Fast analysis | Results in seconds, no cloud latency |
| 🔒 Privacy-first | Zero data collection, zero cloud sync |
| 🌍 Accessible | Designed for mid-range Android devices |

---

## Tech Stack

```
Language        : Kotlin (Android Native)
AI Model        : Gemma 4 (quantized for mobile)
Inference       : Google LiteRT (formerly MediaPipe / TFLite)
Image pipeline  : CameraX + ML Kit OCR
Min SDK         : Android 8.0 (API 26)
Target SDK      : Android 14 (API 34)
```

---

## Architecture

```
┌─────────────────────────────────────┐
│             ShopWise App            │
├─────────────────────────────────────┤
│  UI Layer       │ Jetpack Compose   │
│  Camera         │ CameraX           │
│  OCR            │ ML Kit Text Recog │
│  AI Inference   │ LiteRT + Gemma 4  │
│  Local Storage  │ Room Database     │
└─────────────────────────────────────┘
         ↕ All on-device, no network
```

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android device / emulator with API 26+
- Minimum 4GB RAM recommended for on-device inference

### Installation

```bash
# Clone the repo
git clone https://github.com/mzhanif16/ShopWise.git

# Open in Android Studio
# File → Open → select the ShopWise folder

# Download the Gemma 4 model weights
# Place in: app/src/main/assets/gemma4/
```

### Model Setup

1. Download the quantized Gemma 4 model from [Kaggle Models](https://www.kaggle.com/models/google/gemma)
2. Place the `.tflite` file in `app/src/main/assets/`
3. Build and run

---

## Why Gemma 4?

Gemma 4's multimodal architecture allows ShopWise to:
- **Read** ingredient text from imperfect, real-world label photos
- **Understand** ingredient aliases and chemical names (e.g. "casein" = milk protein, "triticum" = wheat)
- **Reason** about complex compositions — all within a quantized model that runs on mobile hardware

No other model in this size class offers this combination of vision + language understanding optimized for edge deployment.

---

## Impact

> 250 million people worldwide live with food allergies.
> Many live in areas with unreliable internet access.
> ShopWise works for all of them.

---

## Hackathon

This project was built for the **[Gemma 4 Good Hackathon](https://www.kaggle.com/competitions/gemma-4-good-hackathon)** on Kaggle.

**Track:** Health & Wellbeing
**Kaggle Writeup:** [View writeup](https://www.kaggle.com/competitions/gemma-4-good-hackathon/writeups/new-writeup-17772260577695)

---

## License

```
MIT License — free to use, modify, and distribute.
```

---

<p align="center">Built with ❤️ · Powered by Gemma 4 · Gemma 4 Good Hackathon 2026</p>