param([Parameter(Mandatory = $true)][string] $RunDirectory)

$ErrorActionPreference = 'Stop'
$operations = @(Import-Csv (Join-Path $RunDirectory 'benchmark-operations.csv'))
$ticks = @(Import-Csv (Join-Path $RunDirectory 'benchmark-ticks.csv'))
$settings = ConvertFrom-StringData (Get-Content (Join-Path $RunDirectory 'benchmark-runtime.txt') -Raw)
$expected = [int]$settings.cells * ([int]$settings.samples + [int]$settings.burst)
if ($operations.Count -ne $expected) { throw "Expected $expected verified operations, found $($operations.Count)." }
$lifecycle = @(Import-Csv (Join-Path $RunDirectory 'benchmark-lifecycle.csv'))
if ($lifecycle.Count -ne [int]$settings.cells -or @($lifecycle | Where-Object {
    $_.AllUnloaded -ne 'true' -or [int]$_.ReleaseCheckTicks -lt 350 -or [int]$_.ReleaseCheckTicks -gt 6000
}).Count -gt 0) { throw 'Incomplete or invalid target release checks.' }
foreach ($cell in ($operations | Group-Object Cell)) {
    if (@($cell.Group | Where-Object Mode -eq 'serial').Count -ne [int]$settings.samples -or
        @($cell.Group | Where-Object Mode -eq 'burst').Count -ne [int]$settings.burst) {
        throw "Incomplete sample counts in cell $($cell.Name)"
    }
}
$eventsJson = & (Join-Path $env:JAVA_HOME 'bin/jfr.exe') print --json --events pointportals.ChunkLoad (Join-Path $RunDirectory 'portal-gameTest.jfr')
if ($LASTEXITCODE -ne 0) { throw 'JFR export failed.' }
$events = (($eventsJson -join "`n") | ConvertFrom-Json).recording.events
$byChunk = @{}
foreach ($event in $events) {
    $v = $event.values
    $key = "$($v.dimension)/$($v.chunkX)/$($v.chunkZ)"
    if ($byChunk.ContainsKey($key)) { throw "Multiple explicit loads for measured target $key" }
    $byChunk[$key] = $v
}
$measurements = [System.Collections.Generic.List[object]]::new()
foreach ($op in $operations) {
    if ($op.Verified -ne 'true') { throw 'An operation was not verified.' }
    $expectedSuccess = $op.State -ne 'new'
    if (($op.Success -eq 'true') -ne $expectedSuccess) { throw 'Unexpected operation result.' }
    $key = "$($op.Dimension)/$($op.ChunkX)/$($op.ChunkZ)"
    if (-not $byChunk.ContainsKey($key)) { throw "Missing chunk event for $key" }
    $event = $byChunk[$key]
    $expectedOperation = if ($op.Operation -eq 'tuner') { 'tuner_source' } else { 'teleport_destination' }
    if ($event.operation -ne $expectedOperation -or -not $event.completed -or
        $event.loadedBefore -ne ($op.State -eq 'loaded')) { throw "Invalid chunk event for $key" }
    foreach ($metric in 'operation', 'chunk') {
        $ms = if ($metric -eq 'operation') { [double]$op.DurationNs / 1e6 } else {
            [System.Xml.XmlConvert]::ToTimeSpan($event.duration).TotalMilliseconds
        }
        $measurements.Add([pscustomobject]@{
            Metric = $metric; Operation = $op.Operation; State = $op.State
            Dimension = $op.Dimension; Mode = $op.Mode; Milliseconds = $ms
        })
    }
}
if ($operations.Count -ne $byChunk.Count) { throw 'Unexpected chunk event count; fixture/setup loads may have entered the sample set.' }
foreach ($tick in $ticks) {
    $measurements.Add([pscustomobject]@{
        Metric = 'tick'; Operation = $tick.Operation; State = $tick.State
        Dimension = $tick.Dimension; Mode = $tick.Mode; Milliseconds = [double]$tick.DurationNs / 1e6
    })
    if ([int]$tick.Operations -gt 0) {
        $measurements.Add([pscustomobject]@{
            Metric = 'active_tick'; Operation = $tick.Operation; State = $tick.State
            Dimension = $tick.Dimension; Mode = $tick.Mode; Milliseconds = [double]$tick.DurationNs / 1e6
        })
    }
}
$summary = @($measurements | Group-Object Metric, Operation, State, Dimension, Mode | ForEach-Object {
    $first = $_.Group[0]
    $values = @($_.Group.Milliseconds | Sort-Object)
    $stats = $values | Measure-Object -Average -Sum -Maximum
    $budget = if ($first.Metric -in 'tick', 'active_tick') { 50 } else { 5 }
    [pscustomobject]@{
        Metric = $first.Metric; Operation = $first.Operation; State = $first.State
        Dimension = $first.Dimension; Mode = $first.Mode; Count = $values.Count
        MeanMs = [Math]::Round($stats.Average, 4)
        P50Ms = $values[[Math]::Ceiling($values.Count * .50) - 1]
        P95Ms = $values[[Math]::Ceiling($values.Count * .95) - 1]
        P99Ms = $values[[Math]::Ceiling($values.Count * .99) - 1]
        MaxMs = $stats.Maximum; TotalMs = $stats.Sum; BudgetMs = $budget
        OverBudget = @($values | Where-Object { $_ -gt $budget }).Count
    }
})
$summary | Export-Csv (Join-Path $RunDirectory 'summary.csv') -NoTypeInformation
$summary | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $RunDirectory 'summary.json')
$summary | Where-Object { $_.Metric -in 'chunk', 'active_tick' } |
    Format-Table Metric, Operation, State, Dimension, Mode, Count, P95Ms, MaxMs, OverBudget -AutoSize
Write-Host "Validated $($operations.Count) operations and $($ticks.Count) completed ticks."
