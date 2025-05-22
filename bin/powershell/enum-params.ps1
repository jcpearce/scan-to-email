param (
[string]$FileNamePrefix = "scan",
[string]$ScanMode = "ADF", # Valid options are "ADF" or "Flatbed"
[string]$Duplex = "Simplex" # Valid options are "Duplex" or "Simplex"
)

function EnumerateScannerProperties($device) {
    Write-Host "Enumerating scanner properties:"
    foreach ($prop in $device.Properties) {
        Write-Host "Property ID: $($prop.PropertyID)"
        Write-Host "Property Name: $($prop.Name)"
        Write-Host "Property Value: $($prop.Value)"
        if ($prop.IsReadOnly -eq $false) {
            Write-Host "Modifiable: Yes"
            Write-Host "SubType: $($prop.SubType)"
            Write-Host "SubType Default: $($prop.SubTypeDefault)"
            if ($prop.SubType -eq 3) { # 3 indicates a range or list
                Write-Host "SubType Min: $($prop.SubTypeMin)"
                Write-Host "SubType Max: $($prop.SubTypeMax)"
                Write-Host "SubType Step: $($prop.SubTypeStep)"
            } elseif ($prop.SubType -eq 2) { # 2 indicates a value list
                Write-Host "Possible Values: $($prop.SubTypeValues)"
            }
        } else {
            Write-Host "Modifiable: No"
        }
        Write-Host ""
    }
}

# Select the first available Fujitsu scanner with 'fi-' in its name
#$scannerDevice = $deviceManager.DeviceInfos | ? { $_.Type -eq 1 -and $_.Properties["Name"].Value -match "fi-" } | select -first 1
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

        Write-Host "ADF Status: $adfStatus"
        Write-Host "Scanner selected: $description"
        break # Stop the loop after finding the first matching scanner
    }
}

# Check if a scanner was found
if ($selectedScanner -eq $null) {
    Write-Error "No scanner with 'fi-' in the description was found."
} else {
    $device = $selectedScanner.Connect()
    $adfStatus = $device.Properties.Item("3087").Value
    Write-Host "ADF Status: $adfStatus"
    # Enumerate scanner properties
    EnumerateScannerProperties $device
}

