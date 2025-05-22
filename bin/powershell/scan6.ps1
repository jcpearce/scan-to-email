param (
    [string]$FileNamePrefix = "scan",
    [string]$ScanMode = "ADF", # Valid options are "ADF" or "Flatbed"
    [string]$Duplex = "Simplex" # Valid options are "Duplex" or "Simplex"
)

function PrintScannerSourceProperty($device) {
    Write-Host "Looking for ADF/Flatbed property..."
    foreach ($prop in $device.Properties) {
        # Assuming the property for scanner source has 'feeder' or 'flatbed' in its name
        if ($prop.Name -like "*feeder*" -or $prop.Name -like "*flatbed*") {
            Write-Host "Property Name: $($prop.Name)"
            Write-Host "Property ID: $($prop.PropertyID)"
            Write-Host "Possible Values: $($prop.SubTypeMin) to $($prop.SubTypeMax)"
            break
        }
    }
}

# Get the directory of the script
$scriptPath = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition

# Create a subdirectory 'scans' if it doesn't exist
$scanDirectory = Join-Path -Path $scriptPath -ChildPath "scans"
if (-not (Test-Path -Path $scanDirectory)) {
    New-Item -ItemType Directory -Path $scanDirectory
}

# Load the WIA Automation library
$deviceManager = New-Object -ComObject WIA.DeviceManager

# Select the first available Fujitsu scanner with 'fi-' in its name
$scannerDevice = $deviceManager.DeviceInfos | ? { $_.Type -eq 1 -and $_.Properties["Name"].Value -match "fi-" } | select -first 1

# Check if a Fujitsu scanner is found
if ($scannerDevice -eq $null) {
    Write-Host "No Fujitsu 'fi-' scanner found."
    exit
}

# Connect to the scanner
$device = $scannerDevice.Connect()

# Print the scanner source property (ADF/Flatbed)
PrintScannerSourceProperty $device

# ... [rest of your script remains the same]

Write-Host "All scans complete."

