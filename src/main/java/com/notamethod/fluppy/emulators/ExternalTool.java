package com.notamethod.fluppy.emulators;

public record ExternalTool(String name, String installUrlWindows, String installUrlLinux, String winExe,
                           String linuxApp) {
}
