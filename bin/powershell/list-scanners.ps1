# PowerShell Script to List All Attached Scanners

# Load the WIA Automation library
$deviceManager = New-Object -ComObject WIA.DeviceManager

# Filter for scanner devices (Type 1)
$scanners = $deviceManager.DeviceInfos | Where-Object { $_.Type -eq 1 }

# Check if scanners are found
if ($scanners -eq $null) {
    Write-Host "No scanners found."
} else {
    Write-Host "Attached Scanners:"
    foreach ($scanner in $scanners) {
        Write-Host "Name: $($scanner.Properties.Item("Name").Value)"
        Write-Host "Description: $($scanner.Properties.Item("Description").Value)"
        Write-Host "Device ID: $($scanner.DeviceID)"
        Write-Host "--------------------"
    }
}

