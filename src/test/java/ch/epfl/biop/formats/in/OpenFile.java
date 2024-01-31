package ch.epfl.biop.formats.in;

import loci.formats.ImageReader;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;


public class OpenFile {
    static {
        LegacyInjector.preinit();
    }
    public static void main(String... args) throws Exception {
        // TODO: fix dataset
        //ij.Prefs.set("bioformats.enabled.ZeissCZI", false);
        //ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", true);
        ImageJ fiji = new ImageJ();
        fiji.ui().showUI();
    }
}
