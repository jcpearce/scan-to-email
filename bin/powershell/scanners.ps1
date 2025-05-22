# Load the WIA Automation library
$wia = New-Object -ComObject WIA.Automation

# Find devices of type Scanner (device type 1)
$scanners = $wia.DeviceInfos | Where-Object { $_.Type -eq 1 }

# List the scanners
foreach ($scanner in $scanners) {
    $scanner.Properties.Item("Name").Value
}

