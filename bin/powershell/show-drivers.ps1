# Load the WIA Automation library
$deviceManager = New-Object -ComObject WIA.DeviceManager

# Enumerate all devices (scanners, cameras, etc.)
$deviceInfos = $deviceManager.DeviceInfos

# Header for the output
Write-Host "Installed Scanners and their Drivers:"
Write-Host "-------------------------------------"

# Loop through each device
foreach ($deviceInfo in $deviceInfos) {
    # Filter to include only scanners (Type == 1)
    if ($deviceInfo.Type -eq 1) {
        $deviceName = $deviceInfo.Properties.Item("Name").Value
        $deviceDescription = $deviceInfo.Properties.Item("Description").Value
        $deviceDriverVersion = $deviceInfo.Properties.Item("Driver Version").Value

        # Output the device information
        Write-Host "Scanner Name: $deviceName"
        Write-Host "Description: $deviceDescription"
        Write-Host "Driver Version: $deviceDriverVersion"
        Write-Host ""
    }
}

