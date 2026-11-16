@echo off
chcp 65001 > nul
cd /d "%~dp0"
if exist out rmdir /s /q out
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -d out @fontes.txt
if errorlevel 1 (
    echo Falha na compilacao.
    del fontes.txt
    pause
    exit /b 1
)
del fontes.txt
java -Dfile.encoding=UTF-8 -cp out main.Main
pause
