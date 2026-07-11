# SerialPort v1.0.0 Open Source Release Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish a professional bilingual MIT-licensed `v1.0.0` release with an automatically built JAR artifact.

**Architecture:** Keep the Android library module unchanged except for centralized semantic version metadata and the existing `Bytecode` optimization. Use tag-driven GitHub Actions to build the release AAR, extract its Java classes into a versioned JAR, and attach that artifact to a GitHub Release.

**Tech Stack:** Android Gradle Plugin 7.0.2, Gradle 7.2, Java 11 CI runtime, GitHub Actions, Markdown, PowerShell verification.

---

### Task 1: Version And Repository Hygiene

**Files:**
- Modify: `.gitignore`
- Modify: `gradle.properties`
- Modify: `app/build.gradle`
- Modify: `app/src/main/java/android/serial/port/api/Bytecode.java`

- [x] **Step 1: Define the project version**

Add `VERSION_NAME=1.0.0` to `gradle.properties` and set `versionName VERSION_NAME` in `app/build.gradle` so CI and Android metadata share one source of truth.

- [x] **Step 2: Keep IDE state out of Git**

Add `/.idea/` to `.gitignore`, preserving the existing ignore entries.

- [x] **Step 3: Review the existing Bytecode change**

Run `git diff -- app/src/main/java/android/serial/port/api/Bytecode.java` and confirm the only behavior change is reuse and reset of the instance `StringBuilder`.

- [ ] **Step 4: Verify Gradle sees the version**

Run `./gradlew.bat properties --no-daemon` and expect `VERSION_NAME: 1.0.0` with exit code 0.

### Task 2: Bilingual Project Documentation

**Files:**
- Modify: `README.md`
- Create: `README_zh.md`

- [x] **Step 1: Write the default English README**

Document the project overview, features, compatibility, JitPack and local JAR/native-library installation, quick start, listener lifecycle, proxy service, building, release model, security considerations, contributing, disclaimer, and license. Link to `README_zh.md` at the top.

- [x] **Step 2: Write the equivalent Chinese README**

Mirror all material sections accurately in Chinese and link back to `README.md` at the top.

- [x] **Step 3: Check local README links**

Extract relative Markdown targets from both READMEs and confirm each referenced local file exists.

### Task 3: License And Software Disclaimers

**Files:**
- Create: `LICENSE`
- Create: `DISCLAIMER.md`
- Create: `DISCLAIMER_zh.md`

- [x] **Step 1: Add the canonical MIT license**

Use the complete MIT license text with copyright `2026 RelinRan`.

- [x] **Step 2: Add the English disclaimer**

Cover no warranty, hardware and firmware differences, privileged device access, root and permission risks, data and equipment safety, testing responsibility, export and legal compliance, and third-party notices. Link to the Chinese disclaimer.

- [x] **Step 3: Add the Chinese disclaimer**

Provide an equivalent Chinese statement and link to the English disclaimer.

- [x] **Step 4: Validate license identity**

Confirm `LICENSE` contains `MIT License`, the permission grant, warranty disclaimer, and copyright line.

### Task 4: Tag-Driven JAR Release Workflow

**Files:**
- Create: `.github/workflows/release.yml`

- [x] **Step 1: Configure the release trigger and permissions**

Trigger on pushed tags matching `v*.*.*`, set `contents: write`, use Ubuntu, and configure JDK 11 with Gradle caching.

- [x] **Step 2: Validate tag and project versions**

Read `VERSION_NAME` from `gradle.properties`, remove the leading `v` from `GITHUB_REF_NAME`, fail when they differ, and expose the validated version through `GITHUB_OUTPUT`.

- [x] **Step 3: Build and package the JAR**

Run `./gradlew :app:assembleRelease --no-daemon`, extract `classes.jar` from `app/build/outputs/aar/app-release.aar`, rename it to `dist/serial-${version}.jar`, reject an empty artifact, and inspect it with `jar tf`.

- [x] **Step 4: Publish the GitHub Release**

Use `softprops/action-gh-release` pinned to a full commit SHA, enable generated release notes, and attach `dist/serial-${version}.jar`.

- [x] **Step 5: Validate workflow syntax and pinned actions**

Parse `.github/workflows/release.yml` with a YAML parser, confirm the tag trigger and `contents: write`, and ensure third-party actions do not use floating branch names.

### Task 5: Local Release Verification

**Files:**
- Verify: all changed release files

- [ ] **Step 1: Build the release AAR**

Run `./gradlew.bat clean :app:assembleRelease --no-daemon --stacktrace` and require exit code 0.

- [ ] **Step 2: Reproduce the CI JAR locally**

Extract `classes.jar` from `app/build/outputs/aar/app-release.aar` into `build/release-check/serial-1.0.0.jar`.

- [ ] **Step 3: Inspect expected classes**

Run `jar tf build/release-check/serial-1.0.0.jar` and require entries for `android/serial/port/api/Serial.class`, `SerialPort.class`, and `Bytecode.class`.

- [ ] **Step 4: Review repository integrity**

Run `git diff --check`, inspect `git diff`, verify `.idea/` is ignored, and ensure generated build output is not staged.

### Task 6: Commit, Push, Tag, And Monitor

**Files:**
- Commit: all intended source, documentation, and workflow files

- [ ] **Step 1: Commit the implementation**

Stage only `.gitignore`, `.github/workflows/release.yml`, `README.md`, `README_zh.md`, `LICENSE`, `DISCLAIMER.md`, `DISCLAIMER_zh.md`, `gradle.properties`, `app/build.gradle`, and `app/src/main/java/android/serial/port/api/Bytecode.java`; commit as `release: prepare v1.0.0`.

- [ ] **Step 2: Push the main branch**

Run `git push origin main` and require a successful update to `git@github.com:RelinRan/SerialPort.git`.

- [ ] **Step 3: Create and push the release tag**

Create annotated tag `v1.0.0` with message `SerialPort v1.0.0`, then run `git push origin v1.0.0`.

- [ ] **Step 4: Monitor release automation**

Use GitHub CLI or GitHub API to identify the workflow run for `v1.0.0`, wait until it completes, and inspect failure logs if its conclusion is not `success`.

- [ ] **Step 5: Verify the published release**

Confirm the `v1.0.0` GitHub Release exists and includes a non-empty `serial-1.0.0.jar` asset.
