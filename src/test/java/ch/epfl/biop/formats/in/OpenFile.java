package ch.epfl.biop.formats.in;

import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;


public class OpenFile {
    static {
        LegacyInjector.preinit();
    }
    public static void main(String... args) throws Exception {
        //ZeissQuickStartCZIReader reader = new ZeissQuickStartCZIReader();
        //reader.setId("C:/Users/nicol/Desktop/test_gray.czi");
        ij.Prefs.set("bioformats.enabled.ZeissCZI", true);
        ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", false);
        ImageJ fiji = new ImageJ();
        fiji.ui().showUI();
    }
}
