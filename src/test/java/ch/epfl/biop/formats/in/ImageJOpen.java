package ch.epfl.biop.formats.in;

import ij.ImagePlus;
import loci.common.DebugTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.MetadataStore;
import loci.plugins.config.LociConfig;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImportStep;
import loci.plugins.in.ImporterOptions;
import loci.plugins.util.LociPrefs;
import net.imagej.ImageJ;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ImageJOpen {

    public static void main(String... arg) throws Exception {

        String pathczi;
        //pathczi = "C:\\Users\\nicol\\Dropbox\\Romain-Experiment-10-Airyscan Processing-05.czi";
        //pathczi = "N:\\temp-Nico\\TL-03.czi";
        //pathczi = "N:\\temp-Nico\\2023-Clearing\\LocalData\\2023-02-10\\LSM980\\20220628-ClearedOrganoid.czi";
        //pathczi = "N:\\temp-Nico\\2023-Clearing\\LocalData\\2023-02-10\\LSM980\\AKS-40x-LSM980-ImmersolG-Fine.czi";
        //pathczi = "N:\\temp-Nico\\PCB03\\Pos-100x1800-0p010-60-170-Lattice Lightsheet-Deskew.czi";
        //pathczi = "N:\\temp-Romain\\2022_Spirochrome\\LSM980\\20220121\\Experiment-08-Airyscan Processing-04.czi";
        //pathczi = "N:\\temp-Romain\\2022_Spirochrome\\LSM980\\20220121\\Experiment-08.czi"; // FAIL! <- Airyscan non processed
        //pathczi = "Z:\\Data\\Marine\\2023-03-17\\Cfra_LS2-01.czi";
        //pathczi = "Z:\\Data\\Marine\\2023-03-16\\Cfra_LS2-03.czi";
        //pathczi = "Z:\\Data\\Marine\\2023-03-16\\Cfra_LS2-03_MIP.czi";
        //pathczi = "F:\\230321_21008.czi";
        //pathczi = "F:\\230316_stitched.czi";
        //pathczi = "F:\\Experiment-488.czi";
        //pathczi = "C:\\Users\\nicol\\Dropbox\\Experiment-08.czi";
        //pathczi = "C:\\Users\\nicol\\Dropbox\\230321_21008.czi";
        //pathczi = "Z:\\Data\\Omaya Dudin\\2022-11-22\\HighRes\\FM4-64-03.czi";
        //pathczi = "Z:\\Data\\2023-02-16_Marine\\2023-02-16\\New-02.czi";
        //pathczi = "Z:\\Data\\2023-02-16_Marine\\2023-02-16\\New-02-Lattice Lightsheet-03.czi";
        //pathczi = "Z:\\Data\\2023-02-16_Marine\\2023-02-16\\New-02_MIP.czi";
        //pathczi = "Z:\\Data\\2022-11-07-Omaya\\2022-11-08\\New-05-Lattice Lightsheet-06.czi"; // Multiresolution after LLS processing
        //pathczi = "C:\\Users\\nicol\\Dropbox\\230316_stitched.czi";
        //pathczi = "E:\\Pos-100x1800-0p000-60-170.czi";
        //pathczi = "E:\\wetransfer_lsm-980-tiles-fuse-bug_2022-12-01_1054\\Experiment-111-Stitching-no_fuse.czi";
        //pathczi = "E:\\v.zanotelli_20190509_p161_016.czi";
        //pathczi = "C:\\Users\\nicol\\Dropbox\\Experiment-08.czi";
        //pathczi = "C:\\Users\\nicol\\Dropbox\\230316_stitched.czi";

        /*final ImageJ ij = new ImageJ();
        DebugTools.enableLogging("DEBUG");
        ij.setVisible(true);

        tic();
        LociPrefs.makeImageReader();
        toc("czi");*/

        //ImageJ ij = new ImageJ();
        //ij.ui().showUI();

        ImageReader ir;

        (new LociConfig()).run((String)null);


    }
    static long start;
    public static void tic() {
        start = System.currentTimeMillis();
    }

    public static void toc(String message) {
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println(message +": "+(timeElapsed/1000.0)+"s");
    }

    public static void toc() {
        toc("");
    }

    /**
     * An helper function to open an ImagePlus object with a specified reader
     * @param reader the reader to use
     * @param id file path
     * @param series the series to open, to set to -1 to open all series
     * @return
     * @throws Exception
     */
    public static List<ImagePlus> openWithReader(IFormatReader reader, String id, int series) throws Exception {
        ImporterOptions options = new ImporterOptions();
        options.setId(id);
        if (series>0) {
            options.setOpenAllSeries(false);
            options.setSeriesOn(series, true);
        }

        ImportProcess process = new ImportProcess(options);

        Method step = ImportProcess.class.getDeclaredMethod("step", ImportStep.class);
        step.setAccessible(true);
        step.invoke(process, ImportStep.READER);    //process.step(ImportStep.READER);

        Method initializeReader = ImportProcess.class.getDeclaredMethod("initializeReader");
        initializeReader.setAccessible(true);
        initializeReader.invoke(process);

        Field f = ImportProcess.class.getDeclaredField("baseReader");
        f.setAccessible(true);
        Field meta = ImportProcess.class.getDeclaredField("meta");
        meta.setAccessible(true);
        reader.setMetadataStore((MetadataStore) meta.get(process));
        f.set(process, reader);
        step.invoke(process, ImportStep.FILE);
        Method initializeFile = ImportProcess.class.getDeclaredMethod("initializeFile");
        initializeFile.setAccessible(true);
        initializeFile.invoke(process);

        step.invoke(process, ImportStep.STACK);
        Method initializeStack = ImportProcess.class.getDeclaredMethod("initializeStack");
        initializeStack.setAccessible(true);
        initializeStack.invoke(process);

        step.invoke(process, ImportStep.SERIES);
        Method initializeSeries = ImportProcess.class.getDeclaredMethod("initializeSeries");
        initializeSeries.setAccessible(true);
        initializeSeries.invoke(process);

        step.invoke(process, ImportStep.DIM_ORDER);
        Method initializeDimOrder = ImportProcess.class.getDeclaredMethod("initializeDimOrder");
        initializeDimOrder.setAccessible(true);
        initializeDimOrder.invoke(process);

        step.invoke(process, ImportStep.RANGE);
        Method initializeRange = ImportProcess.class.getDeclaredMethod("initializeRange");
        initializeRange.setAccessible(true);
        initializeRange.invoke(process);

        step.invoke(process, ImportStep.CROP);
        Method initializeCrop = ImportProcess.class.getDeclaredMethod("initializeCrop");
        initializeCrop.setAccessible(true);
        initializeCrop.invoke(process);

        step.invoke(process, ImportStep.COLORS);
        Method initializeColors = ImportProcess.class.getDeclaredMethod("initializeColors");
        initializeColors.setAccessible(true);
        initializeColors.invoke(process);

        step.invoke(process, ImportStep.METADATA);
        Method initializeMetadata = ImportProcess.class.getDeclaredMethod("initializeMetadata");
        initializeMetadata.setAccessible(true);
        initializeMetadata.invoke(process);

        step.invoke(process, ImportStep.COMPLETE);

        ImagePlusReader ipr = new ImagePlusReader(process);

        int sCount = reader.getSeriesCount();
        Method m = ImagePlusReader.class.getDeclaredMethod("readImage", int.class, boolean.class);
        m.setAccessible(true);
        List<ImagePlus> images = new ArrayList<>();
        for (int s = 0; s<sCount;s++) {
            ImagePlus imp = (ImagePlus) m.invoke(ipr,s,false);
            images.add(imp);
        }
        return images;

    }

}

