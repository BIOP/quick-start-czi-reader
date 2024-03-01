package ch.epfl.biop.formats.in;

import loci.common.DebugTools;
import loci.formats.in.ZeissCZIReader;
import net.imagej.ImageJ;

public class SimpleIJLaunch {
    public static void main(String... args) {
        //DebugTools.setRootLevel("OFF");
        //DebugTools.enableLogging("OFF");

        DebugTools.setRootLevel("OFF");
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        ZeissCZIReader r;
    }
}
