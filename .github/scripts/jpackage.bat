set /p REQ_MODULES=<req_modules.txt
set /p VERSION=<version.txt

%JPACKAGE_HOME%\bin\jpackage ^
--module-path %JAVAFX_JMODS%;target/lib/ ^
--add-modules %REQ_MODULES% ^
--name "Stream-Pi Server" ^
--description "Stream-Pi Server" ^
--copyright "Copyright 2019-21 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)" ^
--input target/lib ^
--main-jar server-%VERSION%.jar ^
--type msi ^
--java-options "-Dprism.verbose=true -Djavafx.verbose=true -Dprism.dirtyopts=false" ^
--main-class %MAIN_CLASS% ^
--arguments -DStream-Pi.startupRunnerFileName=run_min.exe ^
--add-launcher run_min=assets/run_min_win.properties ^
--icon assets/windows-icon.ico ^
--dest %INSTALL_DIR% ^
--win-dir-chooser ^
--win-menu ^
--win-menu-group "Stream-Pi" ^
--license-file LICENSE.txt ^
--vendor "Stream-Pi"

echo Done now renaming ..
cd %INSTALL_DIR%
echo run dir
dir
ren *.msi stream-pi-server-windows-%ARCH%-%VERSION%-installer.msi
dir