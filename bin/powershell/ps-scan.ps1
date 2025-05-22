param (
    [string]$FileNamePrefix = "scan",
    [string]$ScanMode = "ADF" # Valid options are "ADF" or "Flatbed"
)
# execute with .\ps-scan.ps1 -FileNamePrefix "MyScan" -ScanMode "ADF"
# .\ps-scan.ps1 -FileNamePrefix "MyScan" -ScanMode "Flatbed"

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

# Set the scanner to use ADF or Flatbed
if ($ScanMode -eq "ADF") {
    $device.Properties["3088"].Value = 1 # 1 for Feeder
    # Check if ADF is available and loaded
    if ($device.Properties["3087"].Value -ne 1) {
        Write-Host "ADF is not loaded with documents."
        exit
    }
} elseif ($ScanMode -eq "Flatbed") {
    $device.Properties["3088"].Value = 2 # 2 for Flatbed
} else {
    Write-Host "Invalid scan mode. Use 'ADF' or 'Flatbed'."
    exit
}

# Prepare to scan
$pageNumber = 1
$basePath = Join-Path -Path $scanDirectory -ChildPath $FileNamePrefix # Base path for output files

do {
    # Use the selected scanner feature (ADF or Flatbed)
    $item = $device.Items[1]

    # Set scan settings (adjust these as needed)
    $item.Properties["6146"].Value = 2  # Color mode: 1 for grayscale, 2 for color
    $item.Properties["6147"].Value = 300 # DPI horizontal
    $item.Properties["6148"].Value = 300 # DPI vertical

    # Perform the scan
    $image = $item.Transfer()

    # Save the image to a file
    $filePath = "$basePath$pageNumber.jpg"
    $image.SaveFile($filePath)

    Write-Host "Page $pageNumber scanned and saved to $filePath"

    $pageNumber++
} while ($ScanMode -eq "ADF" -and $device.Properties["3087"].Value -eq 1) # Check if more documents are in the ADF

Write-Host "All scans complete."
