param (
    [string]$FileNamePrefix = "scan",
    [string]$ScanMode = "ADF", # Valid options are "ADF" or "Flatbed"
    [string]$Duplex = "Simplex" # Valid options are "Duplex" or "Simplex"

)
Add-Type -AssemblyName System.Drawing

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
# ... (rest of the code for setting up the scanner)

# Prepare to scan
$pageNumber = 1

do {
    # Use the selected scanner feature (ADF or Flatbed)
    # ... (rest of the code for scanning settings)

    # Perform the scan
    $image = $item.Transfer()

    # Convert the scanned image to System.Drawing.Image
    $tempFilePath = [System.IO.Path]::GetTempFileName()
    $image.SaveFile($tempFilePath)
    $bitmap = [System.Drawing.Image]::FromFile($tempFilePath)
    [System.IO.File]::Delete($tempFilePath)

    # Save the image to a file as PNG
    $pngFilePath = Join-Path -Path $scanDirectory -ChildPath ("{0}{1}.png" -f $FileNamePrefix, $pageNumber)
    $bitmap.Save($pngFilePath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()

    Write-Host "Page $pageNumber scanned and saved to $pngFilePath"

    $pageNumber++
} while ($ScanMode -eq "ADF" -and $device.Properties["3087"].Value -eq 1) # Check if more documents are in the ADF

Write-Host "All scans complete."

