set GRAALVM_OPTIONS=-Dprism.verbose=true -Djavafx.verbose=true
set GRAALVM_OPTIONS=-Dprism.order=sw

org.graalvm.buildtools:native-maven-plugin
      <groupId>org.graalvm.buildtools</groupId>
                        <artifactId>native-maven-plugin</artifactId>


up JAVA_TOOL_OPTIONS: -Dfile.encoding=IBM850
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by com.google.inject.internal.aop.HiddenClassDefiner (file:/C:/Users/Christophe/AppData/Roaming/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/lib/guice-5.1.0-classes.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.inject.internal.aop.HiddenClassDefiner
WARNING: sun.misc.Unsafe::staticFieldBase will be removed in a future release

set GRAALVM_OPTIONS=-Dprism.order=sw -Dprism.verbose=true -Djavafx.verbose=true
ebox.exe

mvn clean package -Pnativemini -DskipTests

jlink  --module-path "%JAVA_HOME%\jmods;path\to\javafx-jmods-25" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.web --output runtime-fluppy  --strip-debug --compress=2 --no-header-files  --no-man-pages


jpackage   --name Fluppy  --input target  --main-jar fluppy.jar   --main-class com.notamethod.fluppy.gui.GamesWall --runtime-image runtime-fluppy --icon path/to/icon.ico  --type exe  --win-console
