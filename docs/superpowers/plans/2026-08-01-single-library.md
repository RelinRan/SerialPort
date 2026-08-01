# Single Library Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Publish the complete Kotlin serial SDK as one Android AAR under `android.serial.port.api`.

**Architecture:** Move core, Android, proxy, native libraries, and tests into one `library` module. Keep source files focused and implementation visibility internal while exposing one cohesive package and facade.

**Tech Stack:** Kotlin 2.0, coroutines, StateFlow, Android Gradle Plugin 8.7, Gradle 8.9, JUnit.

---

### Task 1: Create The Unified Module

- [ ] Create `library/build.gradle.kts` with Android, Kotlin, coroutines, testing, publishing, consumer rules, and four ABI native libraries.
- [ ] Change `settings.gradle` to include only `:library`.
- [ ] Move all public models and session APIs to `android.serial.port.api`.

### Task 2: Integrate Platform And Proxy

- [ ] Move Android transport, session factory, device scanner, JNI bridge, and TCP proxy into the same package and artifact.
- [ ] Add a `SerialPort.create(context)` facade as the shortest supported entry point.
- [ ] Keep Compose out of dependencies while documenting StateFlow collection.

### Task 3: Remove Split Modules And Verify

- [ ] Remove the three split modules after migration.
- [ ] Run unit tests and build the unified release AAR.
- [ ] Inspect the AAR for public classes, consumer rules, and all four native ABIs.

### Task 4: Update Delivery

- [ ] Rewrite README installation and usage for one artifact and one package.
- [ ] Update GitHub Actions to publish only `serialport-2.0.0.aar`.
- [ ] Commit and push the verified consolidation to `main`.
