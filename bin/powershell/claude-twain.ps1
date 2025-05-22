# Specify TWAIN data source name
$scannerTwainId = "FUJITSU TwainDS"

# Folder to save scanned images
$outputFolder = "C:\temp"

# Load assemblies
Add-Type -AssemblyName System.Drawing.Common
Add-Type -AssemblyName SpanComp.TwainLib

# Open scanner TWAIN source
$twain = New-Object SpanComp.TwainLib.TwainSourceManager($scannerTwainId)
$twain.OpenSource()

# Enable feeder
$feeder = $twain.CurrentSource.Capability[4]
$feeder.SetCapability(2, 2) # Enable ADF

# Scan loop
$pageNum = 1
while ($true) {

    # Scan page
    $image = $twain.CurrentSource.TransferPictures()

    # Save image
    $bitmap = New-Object System.Drawing.Bitmap($image)
    $outPath = Join-Path $outputFolder ("page_{0}.jpg" -f $pageNum)
    $bitmap.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Jpeg)

    # Increment counter
    $pageNum++

    # Check if more pages
    if (!$twain.CurrentSource.Capability[5].GetCurrentValue()) {
        break
    }
}

# Disable feeder
$feeder.SetCapability(2, 0) # Disable ADF

# Close source
$twain.CloseSource()

Write-Host "Scanning complete, images saved to $outputFolder"