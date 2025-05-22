@echo off

.\winbin\naps2-7.3.0-win\App\NAPS2.Console.exe --force --deskew --device "fi-7280" --driver %1 --source %2 --dpi 200 --verbose --bitdepth color --output ".\tempimages\%3" >stdout.txt 2>stderr.txt
echo %errorlevel% > exit_code.txt
if %errorlevel% neq 0 (
    (type stdout.txt & echo --- Error Messages --- & type stderr.txt) > combined_output.txt
    cscript //nologo show_error.vbs < combined_output.txt

) else (
echo "Program exited normally"

)


