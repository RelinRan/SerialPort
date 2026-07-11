# Open Source Release Design

## Goal

Prepare SerialPort for a professional, bilingual open source release and publish version `v1.0.0` with a reproducible JAR artifact.

## Scope

- Replace the current README with a professional English default document.
- Add a complete Chinese README and reciprocal language links.
- Add the standard MIT license text.
- Add English and Chinese software disclaimers with reciprocal language links.
- Adopt Semantic Versioning, starting with `1.0.0`.
- Add a tag-driven GitHub Actions workflow that builds and publishes the JAR.
- Include the existing `Bytecode.java` `StringBuilder` reuse change.
- Keep IDE metadata out of version control.

## Documentation

`README.md` is the default English entry point and links to `README_zh.md`. Both documents describe features, supported Android and ABI targets, installation, basic usage, API behavior, local builds, release artifacts, safety considerations, contribution expectations, and licensing.

`LICENSE` contains the canonical English MIT license. `DISCLAIMER.md` and `DISCLAIMER_zh.md` explain that serial-port access depends on hardware, firmware, permissions, and device configuration; elevated permissions carry security risks; and users remain responsible for testing and deployment. The disclaimer documents link to each other.

## Versioning And Build

The library version is declared once as `1.0.0` in Gradle and reused for the Android library metadata and archive naming. Releases use Git tags in the form `vMAJOR.MINOR.PATCH`. The first formal release is `v1.0.0`.

The release JAR is derived from the release AAR's `classes.jar` and named `serial-1.0.0.jar`. Native `.so` files remain separate because a plain JAR cannot package Android JNI libraries in the layout expected by Android. The release documentation makes this limitation explicit.

## GitHub Actions

The release workflow runs only for tags matching `v*.*.*`. It:

1. Checks out the tagged source.
2. Sets up JDK 11 and Gradle caching.
3. Validates that the tag version matches the Gradle project version.
4. Runs the Gradle release build.
5. Extracts `classes.jar` from the release AAR.
6. Renames and validates the JAR.
7. Publishes a GitHub Release with the versioned JAR and generated release notes.

The workflow uses the repository-provided `GITHUB_TOKEN` with `contents: write`; no custom secret is required.

## Verification And Delivery

Before publishing, run the release build, inspect the JAR entries, validate Markdown links and workflow syntax, review the complete diff, and confirm the worktree contains no unintended files. Commit the release changes to `main`, push `main`, create annotated tag `v1.0.0`, push the tag, and monitor GitHub Actions until the release succeeds or a concrete failure is identified and fixed.

## Compatibility And Risk

No public API change is planned beyond the already-present `Bytecode` allocation optimization. Reusing a mutable `StringBuilder` means a shared `Bytecode` instance is not thread-safe; this behavior will be documented rather than expanded during this release-preparation task. Existing third-party Apache-2.0 source headers remain intact; the repository-level MIT license applies only to rights held by this project's copyright owner and does not replace those notices.
