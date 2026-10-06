@echo off
rem Publishes the shared QED modules to mavenLocal in dependency order.
rem Each module depends on the one(s) before it, so the order matters:
rem publishing out of order leaves jars compiled against outdated dependencies.
rem Place this file in C:\QEDFramework (the parent folder of the modules).
setlocal
cd /d "%~dp0"

rem Modules to publish, in dependency order
set MODULES=QED-Api-Contract QED-Shared QED-Shared-DairyMax

for %%M in (%MODULES%) do (
    echo.
    echo === Publishing %%M ===
    pushd "%%M" || (set FAILED=%%M & goto :failed)
    call gradlew publishToMavenLocal
    if errorlevel 1 (
        popd
        set FAILED=%%M
        goto :failed
    )
    popd
)

echo.
echo All modules published to mavenLocal. Reload Gradle in the test suites.
pause
exit /b 0

:failed
echo.
echo *** PUBLISH FAILED for %FAILED% *** - later modules were not published.
pause
exit /b 1