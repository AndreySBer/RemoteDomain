![CI](https://github.com/AndreySBer/RemoteDomain/workflows/CI/badge.svg)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.4.10-blue.svg)](https://kotlinlang.org)
[![Compose Version](https://img.shields.io/badge/Jetpack%20Compose-1.0.0--alpha06-yellow)](https://developer.android.com/jetpack/compose)

# Remote Domain
Remote Domain is a Framework for rapid application development built as a part of a Master Graduation Work.
It provides multiplatform (currently Android) client library and Ktor server for real-time synchronization of data state. It replaces Repository, Database and Network layers in client.
Due to multiplatform code reuse it can be easily adopted for use with iOS target.

# Utilized technologies
- Kotlin Multiplatform
- Server
  - Ktor Server
    - HTTP
    - WebSockets
    - Authentication
- Client
  - Coroutines + Flow
  - SQLDelight
  - Ktor Client
- Android
  - [FlowReactiveNetwork](https://github.com/AndreySBer/FlowReactiveNetwork)
  - Moxy
  - AndroidX Navigation
  - Jetpack Compose

# How to use
### Integration
There is no maven artifact yet.
### [Use cases](docs/use-cases.md)
