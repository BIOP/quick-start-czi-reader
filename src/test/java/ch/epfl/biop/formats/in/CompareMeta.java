package ch.epfl.biop.formats.in;

import ij.ImagePlus;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.in.DynamicMetadataOptions;
import loci.formats.in.MetadataOptions;
import loci.formats.in.ZeissCZIReader;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.BF;
import loci.plugins.config.LociConfig;
import loci.plugins.in.ImporterOptions;
import loci.plugins.util.LociPrefs;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.meta.MetadataRetrieve;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
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

        /*if (!allEqual) {
            System.out.println("Differences found in methods without args, skipping comparisons");
            return;
        }*/

        compareExperimenter(meta_1, meta_2, methods, logger);

        compareInstrument(meta_1, meta_2, methods, logger);

        compareImageMethods(meta_1, meta_2, methods, logger);

        comparePlanes(meta_1, meta_2, methods, logger);

        compareChannels(meta_1, meta_2, methods, logger);

        comparePlate(meta_1, meta_2, methods, logger);

        compareModulo(meta_1, meta_2, methods, logger);

    }

    public static void compareModulo(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger) {

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

        // Plate
        for (int iPlate = 0; iPlate<meta_1.getPlateCount(); iPlate++) {
            int i = iPlate;
            for (String method:methods_plateIdx) {
                isEqualExceptionAndNullSafe(
                        () -> methods.get(method).invoke(meta_1,i),
                        () -> methods.get(method).invoke(meta_2,i), method+"| Plate "+i+" | ", logger);
            }
        }
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

        // Plate
        for (int iPlate = 0; iPlate<meta_1.getPlateCount(); iPlate++) {
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

        // Experimenter
        for (int iInstrument = 0; iInstrument<meta_1.getInstrumentCount(); iInstrument++) {
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

        for (int iImage = 0; iImage<meta_1.getImageCount(); iImage++) {
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

        for (int iImage = 0; iImage<meta_1.getImageCount(); iImage++) {
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


        for (int iImage = 0; iImage < meta_1.getImageCount(); iImage++) {
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

        // Experimenter
        for (int iExperimenter = 0; iExperimenter<meta_1.getExperimenterCount(); iExperimenter++) {
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

    public static void main(String... args) throws Exception {
        String imagePath;
        //--------------------------
        imagePath = "C:\\Users\\nicol\\Dropbox\\230316_stitched.czi"; // Works better with the quick start reader

        boolean flattenRes = false;
        boolean autoStitch = false;

        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        compareFileMeta(imagePath, flattenRes, autoStitch, (str) -> {
            int count = counter.incrementAndGet();
            if (count<MAX_LINES) {
                System.out.println("| " + str + " |");
            } else if (count==MAX_LINES) {
                System.out.println();
                System.out.println(" More than "+MAX_LINES+" differences.");
            }
        });
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

        // C:/Users/nicol/Downloads
        //imagePath = "C:/Users/nicol/Downloads/S=2_2x2_T=1_Z=4_CH=1.czi";
        //imagePath = "C:/Users/nicol/Downloads/S=1_CH=2.czi";
        //imagePath = "C:/Users/nicol/Downloads/S=2_T=3_CH=1.czi";
        //imagePath = "C:/Users/nicol/Downloads/Z=5_CH=2.czi";
        //imagePath = "C:/Users/nicol/Downloads/T=2_Z=5_CH=2.czi";
        //imagePath = "C:/Users/nicol/Downloads/T=2_Z=5_CH=1.czi";
        //imagePath = "C:/Users/nicol/Downloads/W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi";
        //imagePath = "C:/Users/nicol/Downloads/S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi";
        //imagePath = "C:/Users/nicol/Downloads/v.zanotelli_20190509_p165_031.czi";
        //imagePath = "F:/czis/test-plate.czi";
        //imagePath = "F:/czis/v.zanotelli_20190509_p161_016.czi";
        //imagePath = "C:\\Users\\nicol\\Downloads\\test-plate.czi";
        //imagePath = "N:\\temp-Romain\\organoid\\GbD3P_RapiClear_stitch_fullStack.czi";
        //imagePath = "N:\\temp-Romain\\organoid\\GbD3P_RapiClear_40x_default_miniStack_guess.czi";
        //new LociConfig().run(null);
        //ij.Prefs.set("bioformats.enabled.Alicona", false);
        //ij.Prefs.set("bioformats.enabled.ZeissCZI", true);

        ImporterOptions options = new ImporterOptions();
        options.setAutoscale(true);
        options.setId(imagePath);
        options.setOpenAllSeries(false);
        options.setSeriesOn(2, true);
        options.setSeriesOn(3, true);
        options.setSeriesOn(4, true);
        options.setSeriesOn(5, true);
        options.setShowOMEXML(false);
        options.setVirtual(true);
        ImagePlus[] imps;
        ZeissQuickStartCZIReader.allowAutoStitch = autoStitch;
        ij.Prefs.set("bioformats.zeissczi.allow.autostitch", autoStitch);

        ij.Prefs.set("bioformats.enabled.ZeissCZI", false);
        ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", true);

        imps = BF.openImagePlus(options);
        imps[0].show();
        imps[1].show();
        imps[2].show();
        imps[3].show();

        ij.Prefs.set("bioformats.enabled.ZeissCZI", true);
        ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", false);
        imps = BF.openImagePlus(options);
        imps[0].show();
        imps[1].show();
        imps[2].show();
        imps[3].show();

        /* TODO compareReader(reader_1, reader_2);
        reader_1.getModuloC();
                reader_1.getZCTModuloCoords()*/


        /*int iSerie = 0;
        reader_1.setSeries(iSerie);
        for (int rl = 0; rl<reader_1.getResolutionCount(); rl++) {
            reader_1.setResolution(rl);
            System.out.println(reader_1.getSizeX()+"\t"+reader_1.getSizeY());

        }*/

        //System.out.println(omeXML_1.dumpXML());

        /*System.out.println(omeXML_1.dumpXML().equals(omeXML_2.dumpXML()));//" ==";
        System.out.println(omeXML_1.dumpXML());
        System.out.println("--------------------------------");
        System.out.println(omeXML_2.dumpXML());*/
        // TODO : compare with different metadata options
        /*ZeissCZIReader reader = new ZeissCZIReader();
        reader.setFlattenedResolutions(true);*/



        /*CZIReaderBuilder builder = builder().autoStitch(autoStitch).flattenResolutions(flattenRes);

        List<ImagePlus> images1 = ImageJOpen.openWithReader(builder.quickStart(true).get(), imagePath,1);
        List<ImagePlus> images2 = ImageJOpen.openWithReader(builder.quickStart(false).get(), imagePath,1);

        ImagePlus imgReader1 = images1.get(0);
        imgReader1.setTitle("ZeissQuickStartCZIReader");
        imgReader1.show();

        ImagePlus imgReader2 = images2.get(0);
        imgReader2.setTitle("ZeissCZIReader");
        imgReader2.show();*/


        /*omeXML = service.getOMEXML(getOMEMetadata());
        ImporterOptions options = new ImporterOptions();
        options.setAutoscale(false);
        options.setId(pathczi);
        options.setOpenAllSeries(true);
        options.setShowOMEXML(true);
        //options.setStitchTiles(false);
        //ZeissCZIFastReader.ALLOW_AUTOSTITCHING_DEFAULT = true;
        //options.getShowOMEXMLInfo();
        options.setVirtual(false);
        System.out.println("BF Open... ");
        ImagePlus[] imps = BF.openImagePlus(options);*/

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
