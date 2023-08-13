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
import org.openjdk.jol.info.GraphLayout;
import org.scijava.util.VersionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CompareMeta {
    public static Set<String> criticalMethods = new HashSet<>(Arrays.asList(
            "getPixelsSizeX",
            "getPixelsSizeY",
            "getPixelsSizeZ",
            "getPixelsSizeC",
            "getPixelsSizeT",
            "getImageCount",
            "getChannelSamplesPerPixel"));

    static long nanoStart;
    public static void tic() {
        nanoStart = System.nanoTime();
    }

    public static Duration toc() {
        long nanoEnd = System.nanoTime();
        long delta = nanoEnd-nanoStart;
        return Duration.ofNanos(delta);
    }

    public static String formatDuration(Duration period) {
        if (period.minusMillis(1).isNegative()) {
            return "<1 ms";
        } else {
            return period.toMillis()+" ms";
        }
    }

    public static void compareFileMeta(String imagePath, boolean flattenResolutions, boolean autoStitch, Consumer<String> logger, SummaryPerFile summary) throws DependencyException, ServiceException, IOException, FormatException {

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);

        IFormatReader reader_1 = CompareMeta.builder().quickStart(true).autoStitch(autoStitch).flattenResolutions(flattenResolutions).get();

        OMEXMLMetadata omeXML_1 = service.createOMEXMLMetadata();
        reader_1.setMetadataStore(omeXML_1);
        tic();
        reader_1.setId(imagePath);
        Duration readerIniTime1 = toc();
        String quickStartInit = formatDuration(readerIniTime1);

        IFormatReader reader_2 = CompareMeta.builder().quickStart(false).autoStitch(autoStitch).flattenResolutions(flattenResolutions).get();

        OMEXMLMetadata omeXML_2 = service.createOMEXMLMetadata();
        reader_2.setMetadataStore(omeXML_2);
        tic();
        reader_2.setId(imagePath);
        Duration readerIniTime2 = toc();
        String init = formatDuration(readerIniTime2);

        logger.accept(" Method            | Parameters       | Quick Start Reader | Original Reader | Delta ");
        logger.accept("-------------------|------------------|--------------------|-----------------|-------");
        logger.accept("Initialization     |                  |"+quickStartInit+  "|"+init+         "|       ");

        summary.ratioReaderInitialisationDuration = (double) (readerIniTime2.toMillis()) / (double) (readerIniTime1.toMillis());

        double reader1SizeInMb = (float) GraphLayout.parseInstance(reader_1).totalSize() / (1024.0*1024.0);
        double reader2SizeInMb = (float) GraphLayout.parseInstance(reader_2).totalSize() / (1024.0*1024.0);
        DecimalFormat df = new DecimalFormat("0.00");
        logger.accept("Reader Size (Mb)     |                  |"+df.format(reader1SizeInMb)+  "|"+df.format(reader2SizeInMb)+         "|       ");

        summary.ratioMem = reader2SizeInMb / reader1SizeInMb;

        compareMeta(omeXML_1, omeXML_2, logger, summary);
        reader_1.close();
        reader_2.close();
    }

    public static void compareMeta(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Consumer<String> logger, SummaryPerFile summary) {
        // Methods without arguments
        boolean allEqual = true;
        Map<String, Method> methods = new TreeMap<>();

        for (Method method: MetadataRetrieve.class.getMethods()) {
            methods.put(method.getName(), method);
        }

        for (Method method: methods.values()) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                allEqual = allEqual && isEqualExceptionAndNullSafe(() -> method.invoke(meta_1),
                        () -> method.invoke(meta_2), method.getName(),"|(No args)|", logger, summary);
            }
        }

        compareExperimenter(meta_1, meta_2, methods, logger, summary);

        compareInstrument(meta_1, meta_2, methods, logger, summary);

        compareImageMethods(meta_1, meta_2, methods, logger, summary);

        comparePlanes(meta_1, meta_2, methods, logger, summary);

        compareChannels(meta_1, meta_2, methods, logger, summary);

        comparePlate(meta_1, meta_2, methods, logger, summary);

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

    public static void comparePlate(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger, SummaryPerFile summary) {

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
                        () -> methods.get(method).invoke(meta_2,i), method,"| Plate "+i+" | ", logger, summary);
            }
        }
    }

    public static void compareInstrument(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger, SummaryPerFile summary) {

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
                        () -> methods.get(method).invoke(meta_2,i), method,"| Instrument "+i+" | ", logger, summary);
            }
        }
    }

    private static void comparePlanes(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger, SummaryPerFile summary) {
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
                    () -> meta_2.getPlaneCount(iImageFinal), "Different plane count found for image "+iImageFinal, "", logger, summary)) {
                for (int iPlane = 0; iPlane < meta_1.getPlaneCount(iImage); iPlane++) {
                    int iPlaneFinal = iPlane;
                    for (String method : methods_ImageIdx_PlaneIdx) {
                        isEqualExceptionAndNullSafe(
                                () -> methods.get(method).invoke(meta_1, iImageFinal, iPlaneFinal),
                                () -> methods.get(method).invoke(meta_2, iImageFinal, iPlaneFinal), method , "| Image " + iImageFinal + " Plane " + iPlaneFinal + " | ", logger, summary);
                    }
                }
            }
        }
    }

    private static void compareChannels(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger, SummaryPerFile summary) {
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
                    () -> meta_2.getChannelCount(iImageFinal), "Different channel count found for image "+iImageFinal, "", logger, summary)) {
                for (int iChannel = 0; iChannel < meta_1.getChannelCount(iImage); iChannel++) {
                    int iChannelFinal = iChannel;
                    for (String method : methods_ImageIdx_ChannelIdx) {
                        isEqualExceptionAndNullSafe(
                                () -> methods.get(method).invoke(meta_1, iImageFinal, iChannelFinal),
                                () -> methods.get(method).invoke(meta_2, iImageFinal, iChannelFinal), method, "| Image " + iImageFinal + " Channel " + iChannelFinal + " | ", logger, summary);
                    }
                }
            }
        }
    }

    private static void compareImageMethods(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger, SummaryPerFile summary) {
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
                        () -> methods.get(method).invoke(meta_2, i), method , "| Image " + i + " | ", logger, summary);
            }
        }
    }

    public static void compareExperimenter(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods, Consumer<String> logger, SummaryPerFile summary) {
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
                        () -> methods.get(method).invoke(meta_2,i), method,"| Experimenter "+i+" | ", logger, summary);
            }
        }
    }

    private static boolean isEqualExceptionAndNullSafe(Callable<Object> o1Getter, Callable<Object> o2Getter, String methodName, String arguments, Consumer<String> logger, SummaryPerFile summary) {
        String message = methodName+arguments;
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
            summary.numberOfDifferences++;
            if (criticalMethods.contains(methodName)) {
                summary.numberOfCriticalDifferences++;
            }
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
                    summary.numberOfDifferences++;
                    if (criticalMethods.contains(methodName)) {
                        summary.numberOfCriticalDifferences++;
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
                    summary.numberOfDifferences++;
                    if (criticalMethods.contains(methodName)) {
                        summary.numberOfCriticalDifferences++;
                    }
                    return false;
                }
            }
        }
    }

    static int MAX_LINES = 500;
    static double THUMB_SIZE = 150;

    public static void makeReport(String imageFolderPath, String imageName, boolean autoStitch, boolean flattenRes,
                                  String originalURL, SummaryPerFile summary) {

        summary.imageName = imageName;
        summary.urlImage = originalURL;
        summary.flattenRes = flattenRes;
        summary.autoStitch = autoStitch;

        String imagePath = imageFolderPath + imageName;
        String imageNameNoExt = FilenameUtils.removeExtension(imageName);

        ZeissQuickStartCZIReader.allowAutoStitch = autoStitch;
        ij.Prefs.set("bioformats.zeissczi.allow.autostitch", autoStitch);

        String reportFolderPath = "compare"+File.separator;
        String reportImagePath = "compare"+File.separator+imageNameNoExt+File.separator;
        String reportFilePath = reportFolderPath+imageNameNoExt+".flat_"+flattenRes+".stitch_"+autoStitch+".md";

        summary.urlFullReport = "compare/"+imageNameNoExt+".flat_"+flattenRes+".stitch_"+autoStitch+".md";;

        if (!new File(reportFolderPath).exists()) {
            if (!new File(reportFolderPath).mkdirs()) {
                System.err.println("Couldn't create folder "+reportFolderPath+", exiting");
                return;
            }
        }

        if (!new File(reportImagePath).exists()) {
            if (!new File(reportImagePath).mkdirs()) {
                System.err.println("Couldn't create folder "+reportImagePath+", exiting");
                return;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportFilePath))) {

            Consumer<String> logTo = (str) ->
            {
                try {
                    bw.write(str+"\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            logTo.accept("# ["+imageName+"]("+originalURL+") report" );
            logTo.accept(" - **Autostitch** = "+autoStitch);
            logTo.accept(" - ZeissCZIReader v"+ VersionUtils.getVersion(ZeissCZIReader.class));
            logTo.accept(" - ZeissQuickStartCZIReader v"+ VersionUtils.getVersion(ZeissQuickStartCZIReader.class));
            logTo.accept("");
            logTo.accept("# Images ");
            logTo.accept("");

            // Collect images
            if (flattenRes==true) { // Skips images if flatten = false

                List<String> qImages = new ArrayList<>();
                List<String> qImagesSize = new ArrayList<>();
                List<String> images = new ArrayList<>();
                List<String> imagesSize = new ArrayList<>();

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
                tic();
                imps = BF.openImagePlus(options);
                Duration readTimeQuickStartReader = toc();
                String readTimeQuick = formatDuration(readTimeQuickStartReader);//System.out.println("init quick = "+toc());
                int nSeriesQStart = imps.length;
                for (int i = 0; i < nSeriesQStart; i++) {
                    ImagePlus temp = new ImagePlus("", imps[i].getProcessor());
                    double scaleFactor = temp.getWidth() > temp.getWidth() ? THUMB_SIZE / (double) temp.getWidth() : THUMB_SIZE / (double) temp.getHeight();
                    ImagePlus thumb = Scaler.resize(temp, (int) (temp.getWidth() * scaleFactor), (int) (temp.getHeight() * scaleFactor), 1, "");
                    thumb.setTitle(imps[i].getTitle());
                    new ContrastEnhancer().equalize(thumb);
                    qImages.add(imageNameNoExt + ".quick_true.flat_" + flattenRes + ".stitch_" + autoStitch + ".series_" + i + ".jpg");
                    new FileSaver(thumb).saveAsJpeg(reportImagePath + qImages.get(qImages.size()-1));
                    qImagesSize.add("X:"+imps[i].getWidth()+"<br>"+
                                    "Y:"+imps[i].getHeight()+"<br>"+
                                    "C:"+imps[i].getNChannels()+"<br>"+
                                    "Z:"+imps[i].getNSlices()+"<br>"+
                                    "T:"+imps[i].getNFrames()
                            );
                    temp.close();
                    thumb.close();
                    imps[i].close();
                }

                // Default reader
                ij.Prefs.set("bioformats.enabled.ZeissCZI", true);
                ij.Prefs.set("bioformats.enabled.ZeissQuickStartCZI", false);
                tic();
                imps = BF.openImagePlus(options);
                Duration readTimeReader = toc();
                String readTime = formatDuration(readTimeReader);//System.out.println("init quick = "+toc());

                int nSeries = imps.length;
                for (int i = 0; i < nSeries; i++) {
                    ImagePlus temp = new ImagePlus("", imps[i].getProcessor());
                    double scaleFactor = temp.getWidth() > temp.getWidth() ? THUMB_SIZE / (double) temp.getWidth() : THUMB_SIZE / (double) temp.getHeight();
                    ImagePlus thumb = Scaler.resize(temp, (int) (temp.getWidth() * scaleFactor), (int) (temp.getHeight() * scaleFactor), 1, "");
                    thumb.setTitle(imps[i].getTitle());
                    new ContrastEnhancer().equalize(thumb);
                    images.add(imageNameNoExt + ".quick_false.flat_" + flattenRes + ".stitch_" + autoStitch + ".series_" + i + ".jpg");
                    new FileSaver(thumb).saveAsJpeg(reportImagePath + images.get(images.size()-1));
                    imagesSize.add("X:"+imps[i].getWidth()+"<br>"+
                            "Y:"+imps[i].getHeight()+"<br>"+
                            "C:"+imps[i].getNChannels()+"<br>"+
                            "Z:"+imps[i].getNSlices()+"<br>"+
                            "T:"+imps[i].getNFrames()
                    );
                    temp.close();
                    thumb.close();
                    imps[i].close();
                }

                logTo.accept("| Series            | Quick Start Reader | Size | Original Reader | Size |");
                logTo.accept("|-------------------|--------------------|------|-----------------|------|");
                logTo.accept("| Read time (all)   |"+readTimeQuick+   "|------|"+readTime     +"|------|");
                summary.ratioFirstPlaneReadingTime = (double) (readTimeReader.toMillis()) / (double) (readTimeQuickStartReader.toMillis());


                int nImages = Math.max(qImages.size(), images.size());

                for (int i = 0; i<nImages; i++) {
                    String imgQ;
                    if (i<qImages.size()) {
                        imgQ = "!["+qImages.get(i)+"]("+imageNameNoExt+"/"+qImages.get(i)+")"+"|"+qImagesSize.get(i);
                    } else imgQ = " | ";
                    String img;
                    if (i<images.size()) {
                        img = "!["+images.get(i)+"]("+imageNameNoExt+"/"+images.get(i)+")"+"|"+imagesSize.get(i);
                    } else img = " | ";
                    logTo.accept("|"+i+"|"+imgQ+"|"+img+"|");
                }
            }

            logTo.accept("");
            logTo.accept("# Metadata");
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
            }, summary);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        //DebugTools.setRootLevel("TRACE");
        String[] cziURLs ={
            // "https://zenodo.org/record/7147844/files/test-fullplate.czi", // 13.7 GB test full plate -> out of memory
            "https://zenodo.org/record/7129425/files/test-plate.czi", // test plate, 3.3 GB
            "https://zenodo.org/record/7254229/files/P1.czi", // Cytoskeleton stack image, 125 MB
            "https://zenodo.org/record/4662053/files/2021-02-25-tulip_Airyscan.czi", // Airyscan processed, 42.1 MB
            "https://zenodo.org/record/4662053/files/2021-02-25-tulip_unprocessed-Airyscan.czi", // Unprocessed Airyscan 2.9Gb
            "https://zenodo.org/record/6848342/files/Airyscan%20Lines%20Pattern.czi", //2 MB
            "https://zenodo.org/record/6848342/files/Confocal%20Lines%20Pattern.czi", //2.2 MB
            "https://zenodo.org/record/7015307/files/S%3D1_3x3_T%3D3_Z%3D4_CH%3D2.czi", // 107MB (demo camera images by Sebastien Rhode)
            "https://zenodo.org/record/7015307/files/S%3D1_CH%3D2.czi", // 1.6MB
            "https://zenodo.org/record/7015307/files/S%3D2_2x2_CH%3D1.czi", // 2.6MB
            "https://zenodo.org/record/7015307/files/S%3D2_2x2_T%3D1_Z%3D4_CH%3D1.czi", //6.5MB
            "https://zenodo.org/record/7015307/files/S%3D2_2x2_T%3D3_CH%3D1.czi", // 5.2MB
            "https://zenodo.org/record/7015307/files/S%3D2_2x2_T%3D3_Z%3D4_CH%3D1.czi", // 16.8 MB
            "https://zenodo.org/record/7015307/files/S%3D2_2x2_Z%3D4_CH%3D1.czi", // 6.5 MB
            "https://zenodo.org/record/7015307/files/S%3D2_3x3_T%3D1_Z%3D4_CH%3D2.czi", // 72.6MB
            "https://zenodo.org/record/7015307/files/S%3D2_3x3_T%3D3_CH%3D2.czi", // 55.3 MB
            "https://zenodo.org/record/7015307/files/S%3D2_3x3_T%3D3_Z%3D1_CH%3D2.czi", // 54.6 MB
            "https://zenodo.org/record/7015307/files/S%3D2_T%3D3_CH%3D1.czi", // 2.1 MB
            "https://zenodo.org/record/7015307/files/S%3D2_T%3D3_Z%3D5_CH%3D1.czi", // 5.3 MB
            "https://zenodo.org/record/7015307/files/S%3D3_1Pos_2Mosaic_T%3D2%3DZ%3D3_CH%3D2.czi", // 57.1 MB
            "https://zenodo.org/record/7015307/files/S%3D3_CH%3D2.czi", // 2.1 MB
            "https://zenodo.org/record/7015307/files/T%3D1_CH%3D2.czi", // 1.6 MB
            "https://zenodo.org/record/7015307/files/T%3D1_Z%3D5_CH%3D1.czi", // 2.0 MB
            "https://zenodo.org/record/7015307/files/T%3D2_CH%3D1.czi", // 1.6 MB
            "https://zenodo.org/record/7015307/files/T%3D2_Z%3D5_CH%3D1.czi", // 2.6 MB
            "https://zenodo.org/record/7015307/files/T%3D2_Z%3D5_CH%3D2.czi", // 4.0MB
            "https://zenodo.org/record/7015307/files/T%3D3_CH%3D2.czi", // 2.1 MB
            "https://zenodo.org/record/7015307/files/T%3D3_Z%3D5_CH%3D2.czi", // 5.3 MB
            "https://zenodo.org/record/7015307/files/W96_B2%2BB4_S%3D2_T%3D1%3DZ%3D1_C%3D1_Tile%3D5x9.czi", // 31.3 MB
            "https://zenodo.org/record/7015307/files/W96_B2%2BB4_S%3D2_T%3D2%3DZ%3D4_C%3D3_Tile%3D5x9.czi", // 737.7 MB
            "https://zenodo.org/record/7015307/files/Z%3D5_CH%3D1.czi", // 2.0 MB
            "https://zenodo.org/record/7015307/files/Z%3D5_CH%3D2.czi", // 2.6 MB
            "https://zenodo.org/record/7117784/files/RBC_full_one_timepoint.czi", // 1.0 GB RBC full one timepoint
            "https://zenodo.org/record/7117784/files/RBC_full_time_series.czi", // 3.1 GB RBC full time series
            "https://zenodo.org/record/7117784/files/RBC_medium_LLSZ.czi", // 700 MB RBC series LLSZ
            "https://zenodo.org/record/7117784/files/RBC_tiny.czi", // 48.9 MB RBC tiny
            "https://zenodo.org/record/7260610/files/20221019_MixedGrain.czi", // 113 MB Mixed Grain confocal
            "https://zenodo.org/record/7260610/files/20221019_MixedGrain2.czi", // 78.6 MB Mixed Grain2
            "https://zenodo.org/record/5101351/files/Ph488.czi", // 43.1 MB
            "https://zenodo.org/record/3991919/files/v.zanotelli_20190509_p165_031.czi", // 964 MB
            "https://zenodo.org/record/3991919/files/v.zanotelli_20190509_p165_031_pt1.czi", // 3 MB
            "https://zenodo.org/record/3991919/files/v.zanotelli_20190509_p165_031_pt2.czi", // 7.3 MB
            // There are many more of the same kind that I do not use

            "https://zenodo.org/record/7430767/files/10.5%20dpc%20vegfc%20gapdh%20Pecam%20wt%201.czi",
            // There are many more of the same kind
            "https://downloads.openmicroscopy.org/images/Zeiss-CZI/idr0011/Plate1-Blue-A_TS-Stinger/Plate1-Blue-A-12-Scene-3-P3-F2-03.czi",
            // There are many more of the same kind*/
        };

        List<SummaryPerFile> summaryList = new ArrayList<>();

        for (String url: cziURLs) {
            File f = DatasetHelper.getDataset(url, (str) -> {
                try {
                    return URLDecoder.decode(str, "UTF-8");
                } catch(Exception e){
                    e.printStackTrace();
                    return str;
                }
            });
            System.out.println("Analysis of file: "+f.getName());
            try {
                boolean autoStitch, flattenRes;

                flattenRes = true;
                autoStitch = false;
                SummaryPerFile summaryASFalse = new SummaryPerFile();
                makeReport(f.getParent() + File.separator, f.getName(), autoStitch, flattenRes, url, summaryASFalse);
                summaryList.add(summaryASFalse);

                flattenRes = true;
                autoStitch = true;
                SummaryPerFile summaryASTrue = new SummaryPerFile();
                makeReport(f.getParent() + File.separator, f.getName(), autoStitch, flattenRes, url, summaryASTrue);
                summaryList.add(summaryASTrue);

            } catch (Exception e) {
                System.err.println("Couldn't analyse file: "+e.getMessage());
            }
        }

        // Write summary table

        String reportSummaryFilePath = "comparison_summary.md";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportSummaryFilePath))) {
            Consumer<String> logTo = (str) ->
            {
                try {
                    bw.write(str);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            DecimalFormat df = new DecimalFormat("0.0");
            logTo.accept("|File Name|AutoStitch|#Diffs<br>(Critical)|#Diffs|Mem Gain|Init Time Gain|Read Time Gain|\n");
            logTo.accept("|---------|----------|--------------------|------|--------|--------------|--------------|\n");
            for (SummaryPerFile summary: summaryList) {
                logTo.accept("|["+summary.imageName+"]("+summary.urlFullReport+")|");
                logTo.accept(summary.autoStitch+"|");
                logTo.accept(summary.numberOfCriticalDifferences+"|");
                logTo.accept(summary.numberOfDifferences+"|");
                logTo.accept(df.format(summary.ratioMem)+"|");
                logTo.accept(df.format(summary.ratioReaderInitialisationDuration)+"|");
                logTo.accept(df.format(summary.ratioFirstPlaneReadingTime)+"|\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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


    public static class SummaryPerFile {
        String imageName;
        String urlFullReport;
        String urlImage;
        boolean flattenRes;
        boolean autoStitch;
        double ratioMem; // CZIReader/QuickStartCZIReader
        double ratioReaderInitialisationDuration; // CZIReader/QuickStartReader
        double ratioFirstPlaneReadingTime; // CZIReader/QuickStartReader
        int numberOfDifferences;
        int numberOfCriticalDifferences;
    }

}
