@echo off
set JAVA_HOME=.\winbin\liberica-17.0.6
set SCANMETHOD=shell

:: Check if scan.log exists
if exist scan.log (
    :: Use PowerShell to get the last write time of the file
    for /f "usebackq tokens=*" %%i in (`PowerShell -Command "(Get-Item scan.log).LastWriteTime.ToString('yyyy-MM-dd')"`) do set fileDate=%%i

    :: Use PowerShell to get today's date
    for /f "usebackq tokens=*" %%i in (`PowerShell -Command "Get-Date -Format 'yyyy-MM-dd'"`) do set today=%%i

    :: Compare file date with today's date
    if not "%fileDate%"=="%today%" (
        :: If dates are not equal, rename the file with the last used date
        echo Moving scan.log to scan_%fileDate%.log
        move scan.log scan_%fileDate%.log

        :: Ensure the target directory exists
        if not exist .\tempimages\processedimages mkdir .\tempimages\processedimages

        :: Move all files from tempimages to tempimages/processedimages
        echo Moving files from tempimages to tempimages\processedimages
        move .\tempimages\*.* .\tempimages\processedimages\
    )
)

:: Continue with the main task
.\winbin\maven\bin\mvn javafx:run >> scan.log 2>>&1
