# Load the TWAIN library
Add-Type -AssemblyName SpanComp.TwainLib

# Initialize manager
$twain = New-Object SpanComp.TwainLib.TwainSourceManager

# Get list of available sources
$sources = $twain.GetSources()

# Check if Fujitsu scanner found
$isFujitsu = $false
foreach ($source in $sources) {
    if ($source.Name -match "FUJITSU") {
        $isFujitsu = $true
        break
    }
}

if ($isFujitsu) {
    Write-Host "Fujitsu scanner detected via TWAIN"
}
else {
    Write-Warning "No Fujitsu scanner detected"
}