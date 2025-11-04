package com.notamethod.ebox.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class FileWizard {


    public List<File> getRunners(File file) {
        log.debug("searching exe files");
        List<File> runnableFile = new ArrayList<>();
        File[] dirfiles = file.listFiles(pathname -> !pathname.isHidden());
        assert  (Objects.requireNonNull(dirfiles).length==18);
        boolean found=false;
        while (!found) {
            //  if ((dirfiles.length == 1 && dirfiles[0].isDirectory())|| (dirfiles.length == 2 && (dirfiles[0].isDirectory() || dirfiles[1].isDirectory()))) {
            if (dirfiles.length == 1 && dirfiles[0].isDirectory()) {
                dirfiles = dirfiles[0].listFiles();
            } else {
                if (dirfiles.length == 2 && (dirfiles[0].isDirectory() || dirfiles[1].isDirectory())) {

                    if (dirfiles[0].toString().toLowerCase().endsWith(".txt")){
                        dirfiles = dirfiles[1].listFiles();
                        found=true;
                    }else  if (dirfiles[1].toString().toLowerCase().endsWith(".txt")){
                        dirfiles = dirfiles[0].listFiles();
                        found=true;
                    }
//                   if (found){
//                       break;
//                   }
                    break;
                } else {
                    break;
                }
            }
        }
        if (!found){
         //   return runnableFile;
        }


            getRunnerInFolder(runnableFile, dirfiles);
            if (runnableFile.isEmpty() && dirfiles.length>0){
                for (File f : dirfiles) {
                    if (f.isDirectory() && f.getName().equalsIgnoreCase("vga")){
                        getRunnerInFolder(runnableFile, dirfiles);
                    }
                }
            }
            return runnableFile;

    }

    private void getRunnerInFolder(List<File> runnableFile, File[] dirfiles) {
        int count = 0;

        for (File f : dirfiles) {
            if (f.toString().toLowerCase().indexOf("setup") == -1 && f.toString().toLowerCase().indexOf("install") == -1 && (f.toString().toLowerCase().endsWith("pif") || f.toString().toLowerCase().endsWith("exe") || f.toString().toLowerCase().endsWith("com") || f.toString().toLowerCase().endsWith("bat"))) {
                count++;
                runnableFile.add(f);
            }
        }
    }

}