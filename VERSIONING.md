# Versioning

This library follows a three-part version scheme, `X.Y.Z`
(`Major.Minor.Patch`).

## What each part means

- **X — Major.** A significant rewrite or redesign of the library. Adopting a
  new major version may require effort on your part. Major versions are
  expected to be rare.
- **Y — Minor.** A moderate change that may need your attention, such as a
  significant feature addition, a change in default behavior, a change to
  supported platforms or Java versions, or the removal of previously deprecated
  APIs. Any change that may require action on your part is noted and explained
  in the release notes. Minor updates are typically low effort to adopt.
- **Z — Patch.** A small change, such as a bug fix, a new option or method on
  an existing API, or an update to the bundled native libraries. A patch
  release is not expected to break backwards compatibility and may warn of an
  upcoming change. Patch updates can be picked up automatically.

## Public API

This policy covers the classes in `software.amazon.awssdk.crt` and its
sub-packages. It does not cover:

- `software.amazon.awssdk.crt.internal` and anything documented as for
  internal use.
- The JNI layer and the bundled native libraries.
- APIs documented as experimental, which may change or be removed in any
  release.

## Deprecation

APIs planned for removal are first marked with `@Deprecated` and a
`@deprecated` Javadoc note naming the replacement, where one exists. Deprecated
APIs continue to work for a period of time and may be removed in a later minor
release. Removals are called out in the release notes.

## Bundled native libraries

This library bundles the AWS Common Runtime C libraries (`aws-c-*`,
`aws-checksums`, `aws-lc`, `s2n-tls`). Updates to these libraries ship as part
of this library's releases and use the version part that best reflects their
impact on you.

## Branch stability

Untagged branches (for example, `main`) are **not** subject to any API
stability policy. Stability guarantees apply only to tagged releases.

## What this means for you

We recommend running the latest release. Use the version parts as a guide to how
much effort an upgrade requires:

- **Major (X):** expect adoption effort; review the release notes.
- **Minor (Y):** low effort; review the release notes for changed defaults,
  removed APIs, and platform or Java version changes.
- **Patch (Z):** pick up automatically.

Addressing deprecation warnings as they appear keeps most minor upgrades to a
simple version bump.
