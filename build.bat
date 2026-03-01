@echo off
echo ==========================================
echo       Building Mall System
echo ==========================================

echo [1/2] Building Mall User...
cd mall-user
call npm run build
if %errorlevel% neq 0 exit /b %errorlevel%
cd ..

echo [2/2] Building Mall Admin...
cd mall-admin
call npm run build
if %errorlevel% neq 0 exit /b %errorlevel%
cd ..

echo ==========================================
echo       Build Complete!
echo ==========================================
echo Output directories:
echo   - mall-user/dist
echo   - mall-admin/dist
echo.
pause
