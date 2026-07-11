# Software Disclaimer

[简体中文](DISCLAIMER_zh.md) | English

SerialPort is provided under the MIT License on an "as is" basis, without warranties or conditions of any kind. This document supplements the license with practical notices for hardware-facing use; it does not limit rights granted by the license.

## Device compatibility

Serial device paths, drivers, baud-rate support, firmware behavior, kernels, SELinux policies, and permission models vary by manufacturer and device. Compatibility with one device does not imply compatibility with another. Users are responsible for validating every supported hardware and software configuration.

## Privileged access

The library may attempt to invoke `su` and change a device node to mode `666` when ordinary read/write access is unavailable. Root access and world-writable device nodes can materially weaken system security. Users must review, restrict, or replace this behavior to meet their threat model and production security policy.

## Operational safety

Serial commands may control physical equipment. Invalid, duplicated, delayed, reordered, or incomplete commands can cause data loss, service interruption, property damage, or personal injury. Applications must implement protocol validation, framing, integrity checks, appropriate timeouts, fail-safe states, access controls, and independent safety mechanisms suitable for the connected equipment.

## User responsibility

Users are solely responsible for integration, testing, backup, monitoring, regulatory compliance, export-control compliance, and determining whether this software is suitable for a particular purpose. Test in an isolated environment before production use. Do not use the software as the sole safety control for medical, automotive, industrial, life-support, or other safety-critical systems.

## Third-party material

Some source files retain separate copyright and license notices. Those notices continue to apply to the corresponding material. Users are responsible for complying with all applicable third-party terms.
