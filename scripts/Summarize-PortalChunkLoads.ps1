param(
    [Parameter(Mandatory = $true)]
    [string] $Recording
)

$ErrorActionPreference = 'Stop'
$jfr = Join-Path $env:JAVA_HOME 'bin/jfr.exe'
$json = & $jfr print --json --events pointportals.ChunkLoad $Recording
if ($LASTEXITCODE -ne 0) { throw 'JFR export failed.' }
$data = ($json -join "`n") | ConvertFrom-Json
$samples = @($data.recording.events | ForEach-Object {
    [pscustomobject]@{
        Operation = $_.values.operation
        Dimension = $_.values.dimension
        LoadedBefore = $_.values.loadedBefore
        Completed = $_.values.completed
        Milliseconds = [System.Xml.XmlConvert]::ToTimeSpan($_.values.duration).TotalMilliseconds
    }
})
if ($samples.Count -eq 0) { throw 'No portal chunk-load events in this recording.' }

$samples | Group-Object Operation, Dimension, LoadedBefore, Completed | ForEach-Object {
    $ordered = @($_.Group.Milliseconds | Sort-Object)
    $stats = $ordered | Measure-Object -Average -Sum -Maximum
    [pscustomobject]@{
        Operation = $_.Group[0].Operation
        Dimension = $_.Group[0].Dimension
        LoadedBefore = $_.Group[0].LoadedBefore
        Completed = $_.Group[0].Completed
        Count = $ordered.Count
        MeanMs = [Math]::Round($stats.Average, 4)
        P50Ms = $ordered[[Math]::Ceiling($ordered.Count * 0.50) - 1]
        P95Ms = $ordered[[Math]::Ceiling($ordered.Count * 0.95) - 1]
        P99Ms = $ordered[[Math]::Ceiling($ordered.Count * 0.99) - 1]
        MaxMs = $stats.Maximum
        TotalMs = $stats.Sum
        Over5Ms = @($ordered | Where-Object { $_ -gt 5 }).Count
    }
}
