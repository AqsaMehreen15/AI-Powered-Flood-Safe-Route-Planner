# 🌊 AI-Powered Flood-Safe Route Planner

> An offline-first Android application that predicts flood risk and computes the safest navigable route through flood-prone urban terrain — built as a Final Year Project focused on **Sialkot City, Pakistan**.

---

## 📖 Overview

Urban flooding regularly cuts off roads and strands commuters in cities like Sialkot, but existing navigation apps (Google Maps, Waze) have no concept of *flood risk* — they will happily route a driver straight into a waterlogged street. This project solves that gap with a **fully offline, AI-driven routing engine** that:

- Predicts flood likelihood and severity using historical and real-time environmental data
- Builds a road network where elevation and flood risk are encoded as edge weights
- Computes the safest path using a custom **Dijkstra/A\*** routing algorithm — not just the shortest one
- Runs entirely on-device, with no dependency on constant cloud connectivity
- Provides real-time weather and flood alerts to the user

The result is a navigation assistant designed for exactly the moment when connectivity and infrastructure are most likely to fail: during a flood.

---

## ✨ Features

- Real-time flood risk prediction using AI/ML models
- Dynamic safe-route generation with Dijkstra/A* algorithms
- Interactive maps with flood risk visualization
- Weather and flood alerts
- Offline support using TensorFlow Lite

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| IDE | Android Studio |
| ML Inference | TensorFlow Lite |
| Mapping | OpenStreetMap (OSM) |
| Weather Data | OpenWeather API |
| Backend | Firebase |
| Routing Algorithms | Dijkstra, A* |

---

## 🏗️ Design Priorities

- **Offline-first architecture** — core routing and prediction work with no internet connection
- **A risk-aware routing algorithm** over simple shortest-path navigation

These constraints reflect the real-world conditions of flood-affected areas — unreliable connectivity, limited device capability, and the need for fast, actionable guidance.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 17
- Min SDK 24

### Installation

```bash
git clone https://github.com/AqsaMehreen15/AI-Powered-Flood-Safe-Route-Planner.git
```

Open in Android Studio → Sync Gradle → Run on emulator/device.

---

## 👤 Author

Aqsa Mehreen — Final Year, BS Information Technology, University of Sargodha
Supervised by Dr. Bushra Jamil

---

## 📄 License

This project is developed as an academic Final Year Project. Feel free to explore the code for learning purposes. For reuse or collaboration inquiries, please reach out directly.