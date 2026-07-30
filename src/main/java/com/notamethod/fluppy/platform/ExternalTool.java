package com.notamethod.fluppy.platform;

public record ExternalTool(String name, String installUrlWindows, String installUrlLinux, String winExe,
                           String linuxApp) {
}
