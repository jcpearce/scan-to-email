@echo off
for /f "tokens=2" %%i in ('tasklist ^| findstr /i "java.exe javaw.exe"') do (
    echo Process ID: %%i
    wmic process where "ProcessId=%%i" get ExecutablePath
    echo -------------------------------------
)
