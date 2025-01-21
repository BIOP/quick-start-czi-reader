package ch.epfl.biop.formats.in;

import loci.common.DebugTools;
import loci.formats.in.ZeissCZIReader;
import net.imagej.ImageJ;

public class SimpleIJLaunch {
    public static void main(String... args) throws Exception {
        //DebugTools.setRootLevel("OFF");
        //DebugTools.enableLogging("OFF");

        DebugTools.setRootLevel("DEBUG");
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        //ZeissCZIReader r;
        //ZeissQuickStartCZIReader ro = new ZeissQuickStartCZIReader();
        //ro.setId("C:\\Users\\chiarutt\\Dropbox\\BIOP\\data-need to fix with new czi reader\\data\\inputs\\DAPI_PGC.czi");
    }
}
