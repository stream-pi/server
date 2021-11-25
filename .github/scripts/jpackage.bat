::
:: Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macro Pad
:: Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
::
:: This program is free software: you can redistribute it and/or modify
:: it under the terms of the GNU General Public License as published by
:: the Free Software Foundation, either version 3 of the License, or
:: (at your option) any later version.
:: This program is distributed in the hope that it will be useful,
:: but WITHOUT ANY WARRANTY; without even the implied warranty of
:: MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
:: GNU General Public License for more details.
::

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
--arguments -DStream-Pi.startupRunnerFileName=run_min.exe ^
--main-class %MAIN_CLASS% ^
--add-launcher run_min=assets/run_min_win.properties ^
--icon assets/windows-icon.ico ^
--dest %INSTALL_DIR% ^
--win-dir-chooser ^
--win-menu ^
--win-menu-group "Stream-Pi" ^
--vendor "Stream-Pi"

echo Done now renaming ..
cd %INSTALL_DIR%
echo run dir
dir
ren *.msi stream-pi-server-windows-%ARCH%-%VERSION%-installer.msi
dir