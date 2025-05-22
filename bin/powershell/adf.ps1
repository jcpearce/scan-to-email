$wia = New-Object -ComObject WIA.DeviceManager
$scanner = $wia.DeviceInfos.Item(1)  # Replace 1 with the appropriate device index if needed
$scanner.Connect()

$imageItems = $scanner.Items
$imageItem = $imageItems.Item(1)  # Usually 1 for ADF, but you can check available items

# Set properties (adjust as needed)
$imageItem.Properties("4104").Value = 1  # Use document feeder
$imageItem.Properties("4105").Value = 1  # Duplex scanning (if available)
$imageItem.Properties("6146").Value = 200  # Set resolution to 200 dpi

# Scan the document
$imageFile = $imageItem.Transfer()
$filename = "scanned_document.jpg"  # Adjust filename and path as needed
$imageFile.SaveFile($filename)

# Disconnect from the scanner
$scanner.Disconnect()
