$JPACKAGE_HOME/bin/jpackage \
--add-launcher run_min=assets/run_min_linux_and_macos.properties \
--module-path $JAVAFX_JMODS/:target/lib/ \
--add-modules $REQ_MODULES \
--input target/lib \
--main-jar server-$VERSION.jar \
--main-class $MAIN_CLASS \
--description "Stream-Pi Server" \
--vendor "Stream-Pi" \
--verbose \
--copyright "Copyright 2019-21 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)" \
--dest $INSTALL_DIR \
--name 'Stream-Pi Server' \
--java-options '-Dprism.verbose=true -Djavafx.verbose=true -Dprism.dirtyopts=false' \
--arguments 'Stream-Pi.startupRunnerFileName=run_min Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation=true' \
"$@"
