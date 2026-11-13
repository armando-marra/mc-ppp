param(
    [ValidateRange(1, 100)][int] $Samples = 30,
    [ValidateRange(1, 30)][int] $Burst = 10,
    [ValidateRange(1, 10)][int] $Repeats = 3,
    [ValidateRange(1, 100)][int] $Interval = 20,
    [ValidateRange(0, 10000)][int] $WarmupTicks = 1200,
    [string] $CellFilter = '.*'
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$runPath = Join-Path $repo ('build/benchmarks/' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
New-Item -ItemType Directory -Path $runPath | Out-Null
@('view-distance=5', 'simulation-distance=5', 'online-mode=false') | Set-Content (Join-Path $runPath 'server.properties')
'eula=true' | Set-Content (Join-Path $runPath 'eula.txt')
Push-Location $repo
try {
    $revision = git rev-parse HEAD
    if ($LASTEXITCODE -ne 0) { throw 'Cannot identify repository revision.' }
    $status = git status --short
    @(
        "revision=$revision"
        "utc=$([DateTime]::UtcNow.ToString('o'))"
        "samples=$Samples;burst=$Burst;repeats=$Repeats;interval=$Interval;warmupTicks=$WarmupTicks"
        'world=fresh run directory; vanilla GameTest seed 0; no OS disk-cache eviction'
        'runner=unpaced GameTest server, isolated from regression suites'
        "JAVA_TOOL_OPTIONS=$env:JAVA_TOOL_OPTIONS"
        'workingTree:'
        $status
    ) | Set-Content (Join-Path $runPath 'invocation.txt')
    git diff --binary HEAD | Set-Content (Join-Path $runPath 'working-tree.patch')
    git ls-files --cached --others --exclude-standard | ForEach-Object {
        if (Test-Path -LiteralPath $_ -PathType Leaf) {
            [pscustomobject]@{ Path = $_; SHA256 = (Get-FileHash -LiteralPath $_ -Algorithm SHA256).Hash }
        }
    } | Export-Csv (Join-Path $runPath 'source-hashes.csv') -NoTypeInformation
    Get-CimInstance Win32_Processor | Select-Object Name, NumberOfCores, NumberOfLogicalProcessors |
        Format-List | Out-String | Set-Content (Join-Path $runPath 'hardware.txt')
    Get-Volume | Where-Object DriveLetter | Select-Object DriveLetter, FileSystem, Size, SizeRemaining |
        Format-Table | Out-String | Add-Content (Join-Path $runPath 'hardware.txt')
    Write-Host "Benchmark output: $runPath"
    & .\gradlew.bat runGameTest --no-configuration-cache '-PportalBenchmark' "-PportalBenchmarkDir=$($runPath.Replace('\', '/'))" `
        "-PportalBenchmark.samples=$Samples" "-PportalBenchmark.burst=$Burst" "-PportalBenchmark.repeats=$Repeats" `
        "-PportalBenchmark.interval=$Interval" "-PportalBenchmark.warmupTicks=$WarmupTicks" "-PportalBenchmark.filter=$CellFilter" 2>&1 |
        Tee-Object -FilePath (Join-Path $runPath 'gradle.log')
    if ($LASTEXITCODE -ne 0) { throw "Benchmark failed; retain logs in $runPath" }
    & (Join-Path $PSScriptRoot 'Summarize-LoadingBenchmark.ps1') -RunDirectory $runPath
    Write-Host "Completed benchmark: $runPath"
} finally {
    Pop-Location
}
