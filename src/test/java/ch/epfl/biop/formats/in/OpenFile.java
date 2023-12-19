package ch.epfl.biop.formats.in;

import loci.formats.in.ZeissCZIReader;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;


public class OpenFile {
    static {
        LegacyInjector.preinit();
    }
    public static void main(String... args) throws Exception {
        /*ZeissQuickStartCZIReader reader = new ZeissQuickStartCZIReader();*/
        //ZeissQuickStartCZIReader.allowAutoStitch = false;
        // TODO: fix dataset
        /*reader.setId("N:\\temp-Nico\\lightsheet-cleared-brain-demo\\Demo LISH 4x8 15pct 647.czi");
        //reader.setId("C:/Users/nicol/Desktop/test_gray.czi");
        ij.Prefs.set("bioformats.enabled.ZeissCZI", false);
        ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", true);*/
        ImageJ fiji = new ImageJ();
        fiji.ui().showUI();
    }
}
