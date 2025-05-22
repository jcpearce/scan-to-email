param (
    [string]$FileNamePrefix = "scan",
    [string]$ScanMode = "ADF", # Valid options are "ADF" or "Flatbed"
    [string]$Duplex = "Simplex" # Valid options are "Duplex" or "Simplex"
)

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
$imageItems = $device.Items
# Set the scanner to use ADF and configure duplex settings
if ($ScanMode -eq "ADF") {
    $device.Properties["3088"].Value = 1 # 1 for Feeder
    # Check if ADF is available and loaded
    if ($device.Properties["3087"].Value -ne 1) {
        Write-Host "ADF is not loaded with documents."
        exit
    }
    # Set duplex mode if selected
    if ($Duplex -eq "Duplex") {
        # Duplex mode property varies by scanner model. 
        # Check your scanner documentation for the correct property ID.
        $device.Properties["xxxx"].Value = $true # Replace 'xxxx' with duplex mode property ID
    }
} elseif ($ScanMode -eq "Flatbed") {
    $device.Properties["3088"].Value = 2 # 2 for Flatbed
} else {
    Write-Host "Invalid scan mode. Use 'ADF' or 'Flatbed'."
    exit
}

# Prepare to scan
$pageNumber = 1

do {
    # Use the selected scanner feature (ADF or Flatbed)
    $item = $device.Items[1]

    # Set scan settings (adjust these as needed)
    $item.Properties["6146"].Value = 1  # Color mode: 1 for grayscale, 2 for color
    $item.Properties["6147"].Value = 300 # DPI horizontal
    $item.Properties["6148"].Value = 300 # DPI vertical

    # Perform the scan
    $image = $item.Transfer()

    # Save the image to a file with page number suffix
    $filePath = Join-Path -Path $scanDirectory -ChildPath ("{0}{1}.jpg" -f $FileNamePrefix, $pageNumber)
    $image.SaveFile($filePath)

    Write-Host "Page $pageNumber scanned and saved to $filePath"

    $pageNumber++
} while ($ScanMode -eq "ADF" -and $device.Properties["3087"].Value -eq 1) # Check if more documents are in the ADF

Write-Host "All scans complete."

