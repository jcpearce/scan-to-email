$wia = New-Object -ComObject WIA.DeviceManager
$scanners = $wia.DeviceInfos

# Header for the output
Write-Host "Attached Scanners:"

# Iterate through each scanner and display its information
foreach ($scanner in $scanners) {
    Write-Host "- Name: $($scanner.Name)"
    Write-Host "  Description: $($scanner.Properties("Description").Value)"
    Write-Host "  Type: $($scanner.Type)"
    Write-Host "  Port: $($scanner.Port)"
    Write-Host ""  # Add an empty line for readability
}
