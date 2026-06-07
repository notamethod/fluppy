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

## video format
./ffmpeg.exe -vcodec zmbv -i ../../video/video0003.avi -c:v libx264 -crf 18 -pix_fmt yuv420p video0003.mp4