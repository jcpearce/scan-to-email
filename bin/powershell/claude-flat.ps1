# Specify the path to save scanned image
$imagePath = "scan.png"

# Load the Fujitsu Scanning SDK assemblies
Add-Type -Path "C:\Program Files (x86)\Fujitsu\PaperStream IP Driver\FJSVfdm.dll"
Add-Type -Path "C:\Windows\assembly\GAC_MSIL\ScanLib\1.0.0.0__dd317afc787d4d13\ScanLib.dll"

# Create scanner and start session
$scanner = New-Object -ComObject ScanLib.DocumentScanner
$scanner.Startup()

# Set scanner properties
$scanner.Brightness = 50
$scanner.Contrast = 50
$scanner.Resolution = 200
$scanner.PageSize = [ScanLib.PageSize]::A4
$scanner.AutoDeskew = $true

# Scan flatbed
$image = $scanner.SaveAsImage($false)

try {
    # Save image file to disk
    [System.IO.File]::WriteAllBytes($imagePath, $image)
    Write-Host "Saved image to $imagePath"
}
catch {
    Write-Error "Error saving image: $_"
}

# Stop scanning
$scanner.Shutdown()