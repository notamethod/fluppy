
set PROJECT_DIR=%~dp0..\..\
pushd %PROJECT_DIR%
set PROJECT_DIR=%CD%
popd
set EXE=%PROJECT_DIR%\target\fluppy.exe
where rcedit >nul 2>&1 || scoop install rcedit
rcedit "%EXE%"  --set-icon "%PROJECT_DIR%\src\main\resources\dosdog2.ico"
:: remove console mode
powershell -ExecutionPolicy Bypass -Command "$bytes=[IO.File]::ReadAllBytes('%EXE%'); $pe=[BitConverter]::ToInt32($bytes,0x3C); $bytes[$pe+0x5C]=2; [IO.File]::WriteAllBytes('%EXE%',$bytes)"
cp %EXE% E:/

