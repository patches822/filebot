# One-stop local build script for this fork.
#
# Sets up JAVA_HOME/ANT_HOME/PATH for this session (works even if the persisted user-level
# environment variables haven't propagated to this terminal, e.g. inside an IDE's integrated
# terminal that was launched before the variables were set), then runs the Ant build.
#
# Usage:
#   .\build.ps1                  # ant resolve (if needed) + ant fatjar, with JavaFX auto-fetched
#   .\build.ps1 resolve          # just ant resolve
#   .\build.ps1 build            # just ant build
#   .\build.ps1 clean            # ant clean
#   .\build.ps1 <any ant target> # forwarded to ant as-is

param(
    [string]$Target = "fatjar"
)

$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Java\jdk-10.0.2"
$env:ANT_HOME = "C:\Java\apache-ant-1.10.17"
$env:Path = "$env:JAVA_HOME\bin;$env:ANT_HOME\bin;$env:Path"

if (-not (Test-Path $env:JAVA_HOME)) {
    throw "JDK not found at $env:JAVA_HOME. Update this script if you installed it elsewhere."
}
if (-not (Test-Path $env:ANT_HOME)) {
    throw "Ant not found at $env:ANT_HOME. Update this script if you installed it elsewhere."
}

Set-Location $PSScriptRoot

# First-time setup: resolve dependencies if lib\ivy hasn't been populated yet.
if (-not (Test-Path "lib\ivy\jar") -and $Target -ne "resolve") {
    Write-Host "Dependencies not resolved yet, running 'ant resolve' first..."
    ant resolve
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if ($Target -eq "build" -or $Target -eq "fatjar") {
    # Compile once so dist\lib exists, fetch JavaFX (this OpenJDK build doesn't bundle it),
    # then compile again so javac can see it. The first pass is expected to fail on javafx.* errors.
    if (-not (Test-Path "dist\lib\javafx-base.jar")) {
        Write-Host "JavaFX jars not present, running an initial 'ant build' to populate dist\lib (this will report errors, that's expected)..."
        ant build *> $null
        & "$PSScriptRoot\fetch-javafx.ps1"
    }
}

ant $Target
exit $LASTEXITCODE
