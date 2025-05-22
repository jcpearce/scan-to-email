# PowerShell Script to Scan Images Using a Fujitsu Scanner

# Load the WIA Automation library
$deviceManager = New-Object -ComObject WIA.DeviceManager

# Select the Fujitsu scanner
$scannerDevice = $deviceManager.DeviceInfos | ? { $_.Type -eq 1 -and $_.Properties["Name"].Value -match "Fujitsu" } | select -first 1

# Check if a Fujitsu scanner is found
if ($scannerDevice -eq $null) {
    Write-Host "No Fujitsu scanner found."
    exit
}

# Connect to the Fujitsu scanner
$device = $scannerDevice.Connect()

# Set the scanner to use ADF
$device.Properties["3088"].Value = 1 # 1 for Feeder, 2 for Flatbed

# Check if ADF is available and loaded
if ($device.Properties["3087"].Value -ne 1) {
    Write-Host "ADF is not loaded with documents."
    exit
}

# Prepare to scan multiple pages
$pageNumber = 1
$basePath = "C:\path\to\output\scan" # Base path for output files

do {
    # Use the scanner's Automatic Document Feeder (ADF)
    $feeder = $device.Items[1]

    # Set scan settings (adjust these as needed)
    $feeder.Properties["6146"].Value = 1  # Color mode: 1 for grayscale, 2 for color
    $feeder.Properties["6147"].Value = 300 # DPI horizontal
    $feeder.Properties["6148"].Value = 300 # DPI vertical

    # Perform the scan
    $image = $feeder.Transfer()

    # Save the image to a file
    $filePath = "$basePath$pageNumber.jpg"
    $image.SaveFile($filePath)

    Write-Host "Page $pageNumber scanned and saved to $filePath"

    $pageNumber++
} while ($device.Properties["3087"].Value -eq 1) # Check if more documents are in the feeder

Write-Host "All scans complete."

