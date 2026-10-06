# QED-Shared

> ⚠️ **Deprecated:** this repository is replaced by [QED-Api-Contract](https://github.com/AneVisser/QED-Api-Contract) and will be archived in a future release.

Shared URLPaths and data structures for QED, with the purpose of using these tested url's in kTor applications.

This repository is Deprecated. Please use the QED-Api-Contract repository instead. So replace:

```kotlin
import qed.testbaseclass.RequestType
```

with this:
```kotlin
import qed.contract.RequestType
```

Remove dependency on QED-Shared from your build.gradle.kts file

and replace the dependency on QED-Shared in your build.gradle.kts with:

```kotlin
  implementation("com.qed:QED-Api-Contract:1.0.0")
```

