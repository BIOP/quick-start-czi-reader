package ch.epfl.biop.formats.in;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.plugin.Scaler;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.in.DynamicMetadataOptions;
import loci.formats.in.ZeissCZIReader;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.meta.MetadataRetrieve;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CompareMeta {

    public static void compareFileMeta(String imagePath, boolean flattenResolutions, boolean autoStitch, Consumer<String> logger) throws DependencyException, ServiceException, IOException, FormatException {

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);

        IFormatReader reader_1 = CompareMeta.builder().quickStart(true).autoStitch(autoStitch).flattenResolutions(flattenResolutions).get();

        OMEXMLMetadata omeXML_1 = service.createOMEXMLMetadata();
        reader_1.setMetadataStore(omeXML_1);
        reader_1.setId(imagePath);

        IFormatReader reader_2 = CompareMeta.builder().quickStart(false).autoStitch(autoStitch).flattenResolutions(flattenResolutions).get();

        OMEXMLMetadata omeXML_2 = service.createOMEXMLMetadata();
        reader_2.setMetadataStore(omeXML_2);
        reader_2.setId(imagePath);

        logger.accept(" Method            | Parameters       | Quick Start Reader | Original Reader | Delta ");
        logger.accept("-------------------|------------------|--------------------|-----------------|-------");

        compareMeta(omeXML_1, omeXML_2, logger);
    }

    public static void compareMeta(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Consumer<String> logger) {
        // Methods without arguments
        boolean allEqual = true;
        Map<String, Method> methods = new TreeMap<>();

        for (Method method: MetadataRetrieve.class.getMethods()) {
            methods.put(method.getName(), method);
        }

        for (Method method: methods.values()) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                // There's no method returning void apparently
                allEqual = allEqual && isEqualExceptionAndNullSafe(() -> method.invoke(meta_1),
                        () -> method.invoke(meta_2), method.getName()+"|(No args)|", logger);
            }
        }

        compareExperimenter(meta_1, meta_2, methods, logger);

        compareInstrument(meta_1, meta_2, methods, logger);

        compareImageMethods(meta_1, meta_2, methods, logger);

        comparePlanes(meta_1, meta_2, methods, logger);

        compareChannels(meta_1, meta_2, methods, logger);

        comparePlate(meta_1, meta_2, methods, logger);

        //compareModulo(meta_1, meta_2, methods, logger); Not implemented yet

    }

    public static void compareModulo(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {
        // TODO
        /*String[] methods_plateIdx = new String[] {
                "getPlateID",
                "getPlateAnnotationRefCount",
                "getPlateColumnNamingConvention",
                "getPlateColumns",
                "getPlateDescription",
                "getPlateAcquisitionCount",
                "getPlateExternalIdentifier",
                "getPlateName",
                "getPlateRows",
                "getPlateFieldIndex",
                "getPlateRowNamingConvention",
                "getPlateWellOriginX",
                "getPlateWellOriginY",
                "getPlateStatus"
        };

        // Plate
        for (int iPlate = 0; iPlate<meta_1.getPlateCount(); iPlate++) {
            int i = iPlate;
            for (String method:methods_plateIdx) {
                isEqualExceptionAndNullSafe(
                        () -> methods.get(method).invoke(meta_1,i),
                        () -> methods.get(method).invoke(meta_2,i), method+"| Plate "+i+" | ", logger);
            }
        }*/
    }

    public static void comparePlate(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {

        String[] methods_plateIdx = new String[] {
                "getPlateID",
                "getPlateAnnotationRefCount",
                "getPlateColumnNamingConvention",
                "getPlateColumns",
                "getPlateDescription",
                "getPlateAcquisitionCount",
                "getPlateExternalIdentifier",
                "getPlateName",
                "getPlateRows",
                "getPlateFieldIndex",
                "getPlateRowNamingConvention",
                "getPlateWellOriginX",
                "getPlateWellOriginY",
                "getPlateStatus"
        };

        int nPlates = Math.max(meta_1.getPlateCount(), meta_2.getPlateCount());

        // Plate
        for (int iPlate = 0; iPlate<nPlates; iPlate++) {
            int i = iPlate;
            for (String method:methods_plateIdx) {
                isEqualExceptionAndNullSafe(
                        () -> methods.get(method).invoke(meta_1,i),
                        () -> methods.get(method).invoke(meta_2,i), method+"| Plate "+i+" | ", logger);
            }
        }
    }

    public static void compareInstrument(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {

        String[] methods_instrIdx = new String[] {
                "getInstrumentID",
                "getInstrumentAnnotationRefCount"
        };

        int nInstruments = Math.max(meta_1.getInstrumentCount(), meta_2.getInstrumentCount());

        // Experimenter
        for (int iInstrument = 0; iInstrument<nInstruments; iInstrument++) {
            int i = iInstrument;
            for (String method:methods_instrIdx) {
                isEqualExceptionAndNullSafe(
                        () -> methods.get(method).invoke(meta_1,i),
                        () -> methods.get(method).invoke(meta_2,i), method+"| Instrument "+i+" | ", logger);
            }
        }
    }

    private static void comparePlanes(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {
        // Per image
        String[] methods_ImageIdx_PlaneIdx = new String[] {
                "getPlaneAnnotationRefCount",
                "getPlaneDeltaT",
                "getPlaneExposureTime",
                "getPlaneHashSHA1",
                "getPlanePositionX","getPlanePositionY",
                "getPlanePositionZ",
                "getPlaneTheC",
                "getPlaneTheT",
                "getPlaneTheZ"
        };

        int nImages = Math.max(meta_1.getImageCount(), meta_2.getImageCount());

        for (int iImage = 0; iImage<nImages; iImage++) {
            int iImageFinal = iImage;
            if (isEqualExceptionAndNullSafe(
                    () -> meta_1.getPlaneCount(iImageFinal),
                    () -> meta_2.getPlaneCount(iImageFinal), "Different plane count found for image "+iImageFinal, logger)) {
                for (int iPlane = 0; iPlane < meta_1.getPlaneCount(iImage); iPlane++) {
                    int iPlaneFinal = iPlane;
                    for (String method : methods_ImageIdx_PlaneIdx) {
                        isEqualExceptionAndNullSafe(
                                () -> methods.get(method).invoke(meta_1, iImageFinal, iPlaneFinal),
                                () -> methods.get(method).invoke(meta_2, iImageFinal, iPlaneFinal), method + "| Image " + iImageFinal + " Plane " + iPlaneFinal + " | ", logger);
                    }
                }
            }
        }
    }

    private static void compareChannels(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {
        String[] methods_ImageIdx_ChannelIdx = new String[] {
                "getChannelAcquisitionMode",
                "getChannelAnnotationRefCount",
                "getChannelColor",
                "getChannelFluor",
                "getChannelExcitationWavelength",
                "getChannelEmissionWavelength",
                "getChannelContrastMethod",
                "getChannelIlluminationType",
                "getChannelFilterSetRef",
                "getChannelID",
                "getChannelLightSourceSettingsAttenuation",
                "getChannelLightSourceSettingsID",
                "getChannelLightSourceSettingsWavelength",
                "getChannelName",
                "getChannelNDFilter",
                "getChannelPockelCellSetting",
                "getChannelSamplesPerPixel"
        };

        int nImages = Math.max(meta_1.getImageCount(), meta_2.getImageCount());

        for (int iImage = 0; iImage<nImages; iImage++) {
            int iImageFinal = iImage;
            if (isEqualExceptionAndNullSafe(
                    () -> meta_1.getChannelCount(iImageFinal),
                    () -> meta_2.getChannelCount(iImageFinal), "Different channel count found for image "+iImageFinal, logger)) {
                for (int iChannel = 0; iChannel < meta_1.getChannelCount(iImage); iChannel++) {
                    int iChannelFinal = iChannel;
                    for (String method : methods_ImageIdx_ChannelIdx) {
                        isEqualExceptionAndNullSafe(
                                () -> methods.get(method).invoke(meta_1, iImageFinal, iChannelFinal),
                                () -> methods.get(method).invoke(meta_2, iImageFinal, iChannelFinal), method + "| Image " + iImageFinal + " Channel " + iChannelFinal + " | ", logger);
                    }
                }
            }
        }
    }

    private static void compareImageMethods(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {
        String[] methods_ImageIdx = new String[] {
                "getImageName",
                "getImageAcquisitionDate",
                "getImageAnnotationRefCount",
                "getImageDescription",
                "getImageExperimenterGroupRef",
                "getImageExperimenterRef",
                "getImageInstrumentRef",
                "getImageID",
                "getImageROIRefCount",
                "getPlaneCount",
                "getChannelCount",
                "getImageROIRefCount",
                "getPixelsBinDataCount",
                "getMicrobeamManipulationRefCount",
                "getTiffDataCount",
                "getStageLabelName",
                "getStageLabelX",
                "getStageLabelY",
                "getStageLabelZ",
                "getPixelsSizeX",
                "getPixelsSizeY",
                "getPixelsSizeZ",
                "getPixelsSizeC",
                "getPixelsSizeT",
                "getPixelsPhysicalSizeX",
                "getPixelsPhysicalSizeY",
                "getPixelsPhysicalSizeZ",
                "getPixelsInterleaved",
                "getPixelsDimensionOrder",
                "getPixelsBigEndian",
                "getPixelsID"

        };


        int nImages = Math.max(meta_1.getImageCount(), meta_2.getImageCount());

        for (int iImage = 0; iImage < nImages; iImage++) {
            int i = iImage;
            for (String method : methods_ImageIdx) {
                isEqualExceptionAndNullSafe(
                        () -> methods.get(method).invoke(meta_1, i),
                        () -> methods.get(method).invoke(meta_2, i), method + "| Image " + i + " | ", logger);
            }
        }
    }

    public static void compareExperimenter(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {
        String[] methods_expIdx = new String[] {
                "getExperimenterEmail",
                "getExperimenterID",
                "getExperimenterAnnotationRefCount",
                "getExperimenterInstitution",
                "getExperimenterFirstName",
                "getExperimenterLastName",
                "getExperimenterUserName",
                "getExperimenterMiddleName"
        };
        int nExperimenters = Math.max(meta_1.getExperimenterCount(), meta_2.getExperimenterCount());
        // Experimenter
        for (int iExperimenter = 0; iExperimenter<nExperimenters; iExperimenter++) {
            int i = iExperimenter;
            for (String method:methods_expIdx) {
                isEqualExceptionAndNullSafe(
                        () -> methods.get(method).invoke(meta_1,i),
                        () -> methods.get(method).invoke(meta_2,i), method+"| Experimenter "+i+" | ", logger);
            }
        }
    }

    private static boolean isEqualExceptionAndNullSafe(Callable<Object> o1Getter, Callable<Object> o2Getter, String message, Consumer<String> logger) {

        DecimalFormat df = new DecimalFormat("0.000");
        Object o1 = null, o2 = null;
        boolean exception_1 = false, exception_2 = false;
        try {
            o1 = o1Getter.call();
        } catch (Exception e) {
            exception_1 = true;
        }

        try {
            o2 = o2Getter.call();
        } catch (Exception e) {
            exception_2 = true;
        }

        if (Boolean.logicalXor(exception_1, exception_2)) {
            logger.accept( message + " error: "+exception_1+" | error: "+exception_2+ "|");
            return false;
        } else {
            if ((o1!=null)&&(o2!=null)) {
                if (!o1.equals(o2)) {
                    if ((o1 instanceof Length) && (o2 instanceof Length) && (((Length) o1).value(UNITS.MICROMETER)!=null) && (((Length) o2).value(UNITS.MICROMETER)!=null)) {
                        Length l1 = (Length) o1;
                        Length l2 = (Length) o2;

                        l1.value(UNITS.MICROMETER);
                        l1.value(UNITS.MICROMETER).doubleValue();

                        l2.value(UNITS.MICROMETER);
                        l2.value(UNITS.MICROMETER).doubleValue();

                        double diff = Math.abs(l1.value(UNITS.MICROMETER).doubleValue() - l2.value(UNITS.MICROMETER).doubleValue());

                        if (diff<0.001) {
                            return true;
                        } else {
                            logger.accept( message + df.format(l1.value(UNITS.MICROMETER))+" um | "+df.format(l2.value(UNITS.MICROMETER))+" um | "+df.format(diff)+ " um");
                        }
                    } else if ((o1 instanceof Time) && (o2 instanceof Time)) {
                        Time t1 = (Time) o1;
                        Time t2 = (Time) o2;
                        double diff = Math.abs(t1.value(UNITS.SECOND).doubleValue() - t2.value(UNITS.SECOND).doubleValue());
                        //System.out.println( message +"t diff = "+diff+ "\t 1: \t "+o1+"\t 2: \t "+o2);
                        if (diff<0.001) {
                            return true; // below 1 ms = equality, yep that's right.
                        } else {
                            logger.accept( message +" "+df.format(t1.value(UNITS.SECOND))+" s |  "+df.format(t2.value(UNITS.SECOND))+" s | "+df.format(diff)+" s");
                        }
                    } else {
                        logger.accept(message + o1 + "| " + o2 + "|");
                    }
                    return false;
                } else {
                    //println( message + "\t 1: \t "+o1+"\t 2: \t "+o2);
                    return true;
                }
            } else {
                if ((o1==null)&&(o2==null)) {
                    return true;
                } else {
                    logger.accept( message + "| 1: "+(o1==null?"null":o1)+"| 2: "+(o2==null?"null":o2));
                    return false;
                }
            }
        }
    }

    static int MAX_LINES = 500;
    static double THUMB_SIZE = 150;

    public static void makeReport(String imageFolderPath, String imageName, boolean autoStitch, boolean flattenRes) {
        String imagePath = imageFolderPath + imageName;
        String imageNameNoExt = FilenameUtils.removeExtension(imageName);

        //boolean flattenRes = true;
        //boolean autoStitch = true;
        ZeissQuickStartCZIReader.allowAutoStitch = autoStitch;
        ij.Prefs.set("bioformats.zeissczi.allow.autostitch", autoStitch);

        String reportFolderPath = "compare"+File.separator;
        String reportImagePath = "compare"+File.separator+imageNameNoExt+File.separator;
        String reportFilePath = reportFolderPath+imageNameNoExt+".flat_"+flattenRes+".stitch_"+autoStitch+".md";

        if (!new File(reportFolderPath).exists()) {
            if (!new File(reportFolderPath).mkdirs()) {
                System.err.println("Couldn't create folder "+reportFolderPath+", exiting");
                return;
            }
        }

        if (!new File(reportImagePath).mkdirs()) {
            System.err.println("Couldn't create folder "+reportImagePath+", exiting");
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportFilePath))) {

            Consumer<String> logTo = (str) -> //System.out.println(str);
            {
                try {
                    bw.write(str+"\n");//System.out.println(str);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            // Collect images
            if (flattenRes==true) { // Skips images if flatten = false



                List<String> qImages = new ArrayList<>();
                List<String> images = new ArrayList<>();

                // Importer settings
                ImporterOptions options = new ImporterOptions();
                options.setAutoscale(true);
                options.setId(imagePath);
                options.setOpenAllSeries(true);
                options.setShowOMEXML(false);
                options.setVirtual(true);
                ImagePlus[] imps;

                // Quick Start
                ij.Prefs.set("bioformats.enabled.ZeissCZI", false);
                ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", true);
                imps = BF.openImagePlus(options);
                int nSeriesQStart = imps.length;
                for (int i = 0; i < nSeriesQStart; i++) {
                    ImagePlus temp = new ImagePlus("", imps[i].getProcessor());
                    double scaleFactor = temp.getWidth() > temp.getWidth() ? THUMB_SIZE / (double) temp.getWidth() : THUMB_SIZE / (double) temp.getHeight();
                    ImagePlus thumb = Scaler.resize(temp, (int) (temp.getWidth() * scaleFactor), (int) (temp.getHeight() * scaleFactor), 1, "");
                    thumb.setTitle(imps[i].getTitle());
                    new ContrastEnhancer().equalize(thumb);
                    qImages.add(imageNameNoExt + ".quick_true.flat_" + flattenRes + ".stitch_" + autoStitch + ".series_" + i + ".jpg");
                    new FileSaver(thumb).saveAsJpeg(reportImagePath + qImages.get(qImages.size()-1));
                }

                // Default reader
                ij.Prefs.set("bioformats.enabled.ZeissCZI", true);
                ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", false);
                imps = BF.openImagePlus(options);
                int nSeries = imps.length;
                for (int i = 0; i < nSeries; i++) {
                    ImagePlus temp = new ImagePlus("", imps[i].getProcessor());
                    double scaleFactor = temp.getWidth() > temp.getWidth() ? THUMB_SIZE / (double) temp.getWidth() : THUMB_SIZE / (double) temp.getHeight();
                    ImagePlus thumb = Scaler.resize(temp, (int) (temp.getWidth() * scaleFactor), (int) (temp.getHeight() * scaleFactor), 1, "");
                    thumb.setTitle(imps[i].getTitle());
                    new ContrastEnhancer().equalize(thumb);
                    images.add(imageNameNoExt + ".quick_false.flat_" + flattenRes + ".stitch_" + autoStitch + ".series_" + i + ".jpg");
                    new FileSaver(thumb).saveAsJpeg(reportImagePath + images.get(images.size()-1));
                }

                logTo.accept("| Series            | Quick Start Reader | Original Reader |");
                logTo.accept("|-------------------|--------------------|-----------------|");

                int nImages = Math.max(qImages.size(), images.size());

                for (int i = 0; i<nImages; i++) {
                    String imgQ = "";
                    if (i<qImages.size()) {
                        imgQ = "!["+qImages.get(i)+"]("+imageNameNoExt+"/"+qImages.get(i)+")";
                    }
                    String img = "";
                    if (i<images.size()) {
                        img = "!["+images.get(i)+"]("+imageNameNoExt+"/"+images.get(i)+")";
                    }
                    logTo.accept("|"+i+"|"+imgQ+"|"+img+"|");
                }


            }

            logTo.accept("");
            logTo.accept("");


            // Build the rest of the comparison
            AtomicInteger counter = new AtomicInteger();
            counter.set(0);
            compareFileMeta(imagePath, flattenRes, autoStitch, (str) -> {
                int count = counter.incrementAndGet();
                if (count<MAX_LINES) {
                    logTo.accept("| " + str + " |");
                } else if (count==MAX_LINES) {
                    logTo.accept("");
                    logTo.accept(" More than "+MAX_LINES+" differences.");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String... args) {

        makeReport("C:/Users/nicol/Downloads/", "S=2_2x2_T=1_Z=4_CH=1.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/S=1_CH=2.czi";
        makeReport("C:/Users/nicol/Downloads/", "S=1_CH=2.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/S=2_T=3_CH=1.czi";
        makeReport("C:/Users/nicol/Downloads/", "S=2_T=3_CH=1.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/Z=5_CH=2.czi";
        makeReport("C:/Users/nicol/Downloads/", "Z=5_CH=2.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/T=2_Z=5_CH=2.czi";
        makeReport("C:/Users/nicol/Downloads/", "T=2_Z=5_CH=2.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/T=2_Z=5_CH=1.czi";
        makeReport("C:/Users/nicol/Downloads/", "T=2_Z=5_CH=1.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi";
        makeReport("C:/Users/nicol/Downloads/", "W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi";
        makeReport("C:/Users/nicol/Downloads/", "S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi", true ,true);
        //imagePath = "C:/Users/nicol/Downloads/v.zanotelli_20190509_p165_031.czi";
        makeReport("C:/Users/nicol/Downloads/", "v.zanotelli_20190509_p165_031.czi", true ,true);
        //imagePath = "F:/czis/test-plate.czi";
        //imagePath = "F:/czis/v.zanotelli_20190509_p161_016.czi";
        //imagePath = "C:\\Users\\nicol\\Downloads\\test-plate.czi";
        makeReport("C:/Users/nicol/Downloads/", "test-plate.czi", true ,true);
        //imagePath = "N:\\temp-Romain\\organoid\\GbD3P_RapiClear_stitch_fullStack.czi";
        //imagePath = "N:\\temp-Romain\\organoid\\GbD3P_RapiClear_40x_default_miniStack_guess.czi";
        //String imageFolderPath = "C:\\Users\\nicol\\Dropbox\\";
        //String imageName = "230316_stitched.czi";
        makeReport("C:/Users/nicol/Dropbox/", "230316_stitched.czi", true ,true);
        makeReport("C:/Users/nicol/Dropbox/", "Experiment-08.czi", true ,true);
        makeReport("C:/Users/nicol/Dropbox/", "Romain-Experiment-10-Airyscan Processing-05.czi", true ,true);

        // Report on 230316_stitched.czi:
        // Difference in positions... To test
        //--------------------------
        //imagePath = "C:\\Users\\nicol\\Dropbox\\Experiment-08.czi";
        // Report on Experiment-08.czi:
        // More identical metadata is written on the extra channels (the airyscan channels)
        //--------------------------
        //imagePath = "C:\\Users\\nicol\\Dropbox\\Romain-Experiment-10-Airyscan Processing-05.czi";
        // Report on Romain-Experiment-10-Airyscan Processing-05.czi
        // Looks ok


        //String imageFolderPath = "C:\\Users\\nicol\\Dropbox\\";
        //String imageName = "230316_stitched.czi";







    }

    public static CZIReaderBuilder builder() {
        return new CZIReaderBuilder();
    }

    public static class CZIReaderBuilder {

        boolean quick = false;
        boolean autoStitch = true;
        boolean flattenRes = true;

        public CZIReaderBuilder quickStart(boolean flag) {
            this.quick = flag;
            return this;
        }

        public CZIReaderBuilder autoStitch(boolean flag) {
            this.autoStitch = flag;
            return this;
        }

        public CZIReaderBuilder flattenResolutions(boolean flag) {
            this.flattenRes = flag;
            return this;
        }

        public IFormatReader get() {
            IFormatReader reader;
            if (quick) {
                reader = new ZeissQuickStartCZIReader();
            } else {
                reader = new ZeissCZIReader();
            }
            DynamicMetadataOptions options = new DynamicMetadataOptions();
            options.setBoolean(ZeissCZIReader.ALLOW_AUTOSTITCHING_KEY, autoStitch); // Same key for both readers
            reader.setFlattenedResolutions(flattenRes); // To test in bdv condition
            reader.setMetadataOptions(options);
            return reader;

        }
    }

}