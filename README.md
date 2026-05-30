# Fluppy

One day, this app will proudly be a glorified DOSBox front‑end


## APIs

### IGDB
https://api-docs.igdb.com/#account-creation

###
Add games based on last played games
tell if game is protected or not
add link to manual

## build from sources
generate native executable: mvn clean -Pnative package
## post installation:
### windows
run post-install.bat. 
Prerequesite: You need scoop:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression

update to GUI mode:
editbin /SUBSYSTEM:WINDOWS d:\fluppy.exe
<plugin>
<groupId>org.codehaus.mojo</groupId>
<artifactId>exec-maven-plugin</artifactId>
<executions>
<execution>
<id>editbin</id>
<phase>package</phase>
<goals>
<goal>exec</goal>
</goals>
<configuration>
<executable>editbin</executable>
<arguments>
<argument>/SUBSYSTEM:WINDOWS</argument>
<argument>${project.build.directory}/${project.artifactId}.exe</argument>
</arguments>
</configuration>
</execution>
</executions>
</plugin>