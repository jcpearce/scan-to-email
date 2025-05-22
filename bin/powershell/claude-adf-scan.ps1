# Specify the path to save scanned images
$imagePath = "C:\Temp"

# Load the Fujitsu Scanning SDK assemblies
# C:\Program Files (x86)\PaperStream IP\CommonCOM\ScanLLD.dll
#
Add-Type -Path "C:\Program Files (x86)\PaperStream IP\CommonCOM\ScanLLD.dll"
#Add-Type -Path "C:\Windows\assembly\GAC_MSIL\ScanLib\1.0.0.0__dd317afc787d4d13\ScanLib.dll"

# Create scanner and start session
$scanner = New-Object -ComObject ScanLib.DocumentScanner
$scanner.Startup()

# Set scanner properties
$scanner.Brightness = 50
$scanner.Contrast = 50
$scanner.Resolution = 200
$scanner.PageSize = [ScanLib.PageSize]::A4
$scanner.AutoDeskew = $true

# Start scanning document feeder
$scanner.ADF($true)

# Scan each page and save image
$page = 1
while ($scanner.PageReady) {

    $image = $scanner.SaveAsImage($true)
    $filename = Join-Path -Path $imagePath -ChildPath ("page_{0}.png" -f $page)

    try {
        # Save image file to disk
        [System.IO.File]::WriteAllBytes($filename, $image)
        Write-Host "Saved page $page"
    }
    catch {
        Write-Error "Error saving image: $_"
    }

    $page++

    # Get next page
    $scanner.TransferImage($true)
}

# Stop scanning
$scanner.ADF($false)
$scanner.Shutdown()