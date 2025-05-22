$wia = New-Object -ComObject WIA.DeviceManager
$scanner = $deviceManager.DeviceInfos | ? { $_.Type -eq 1 -and $_.Properties["Name"].Value -match "fi-" } | select -first 1
$scanner.Connect()

$imageItems = $scanner.Items
$imageItem = $imageItems.Item(1)  # Usually 1 for ADF, but check available items if unsure

# Enumerate properties and display their descriptions
Write-Host "Available Image Item Properties:"
foreach ($property in $imageItem.Properties) {
    Write-Host "- Name: $($property.Name)"
    Write-Host "  Description: $($property.Description)"
    Write-Host "  Value: $($property.Value)"
    Write-Host ""  # Add an empty line for readability
}


