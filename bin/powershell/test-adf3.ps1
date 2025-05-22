$wia = New-Object -ComObject WIA.DeviceManager
$scanners = $wia.DeviceInfos

# Header for the output
Write-Host "Searching for Scanners with 'fi-' in the description..."

# Variable to hold the selected scanner object
$selectedScanner = $null

# Iterate through each scanner and check its description
foreach ($scanner in $scanners) {
    $description = $scanner.Properties.Item("Description").Value

    # Check if the description contains 'fi-'
    if ($description -like "*fi-*") {
        $selectedScanner = $scanner
        Write-Host "Scanner selected: $description"
        break # Stop the loop after finding the first matching scanner
    }
}

# Check if a scanner was found
if ($selectedScanner -eq $null) {
    Write-Error "No scanner with 'fi-' in the description was found."
    exit
}

# Connect to the scanner
$device = $selectedScanner.Connect()
# Check ADF Status
$adfStatus = $device.Properties.Item("3087").Value
Write-Host "ADF Status: $adfStatus"

if ($adfStatus -ne 1) {
    Write-Host "ADF is not loaded with documents. Please load documents into the ADF."
#    exit
}
else
{
Write-Host "ADF has documents"
}

# Set the scanner to use ADF
$device.Properties.Item("3088").Value = 1 # 2 for Feeder

# Get the directory of the script
$scriptPath = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition

# Define the subdirectory for scans relative to the script's location
$scanSubdirectory = Join-Path -Path $scriptPath -ChildPath "scans"
if (-not (Test-Path -Path $scanSubdirectory)) {
    New-Item -ItemType Directory -Path $scanSubdirectory
}

# Scanning loop
$pageNumber = 1
do {
    # Perform the scan
    $item = $device.Items.Item(1) # The first item in the Items collection represents the scanning bed or ADF.

    # Set scan settings (adjust as needed)
    $item.Properties.Item("6146").Value = 1  # Color mode: 1 for grayscale, 2 for color
    $item.Properties.Item("6147").Value = 300 # DPI horizontal
    $item.Properties.Item("6148").Value = 300 # DPI vertical

    # Execute the scan
    $image = $item.Transfer()

    # Save the scanned image
    $imageFileName = "scan_page_$pageNumber.jpg"
    $imagePath = Join-Path -Path $scanSubdirectory -ChildPath $imageFileName
    $image.SaveFile($imagePath)

    Write-Host "Scanned document page $pageNumber saved to $imagePath"

    # Increment page number
    $pageNumber++
} while ($device.Properties.Item("3087").Value -eq 1) # Continue if more documents are in the ADF

Write-Host "All scans complete."
