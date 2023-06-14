package ch.epfl.biop.formats.in;

import ch.epfl.biop.formats.in.ZeissQuickStartCZIReader;
import loci.common.services.ServiceFactory;
import loci.formats.in.ZeissCZIReader;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.meta.MetadataRetrieve;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class CompareMeta {

    public static void compareMeta(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2) {
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
                        () -> method.invoke(meta_2), method.getName());
            }
        }

        /*if (!allEqual) {
            System.out.println("Differences found in methods without args, skipping comparisons");
            return;
        }*/

        compareExperimenter(meta_1, meta_2, methods);

        compareInstrument(meta_1, meta_2, methods);

        compareImageMethods(meta_1, meta_2, methods);

        comparePlanes(meta_1, meta_2, methods);

        compareChannels(meta_1, meta_2, methods);

        comparePlate(meta_1, meta_2, methods);

        compareModulo(meta_1, meta_2, methods);

    }

    public static void compareModulo(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {

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
                        () -> methods.get(method).invoke(meta_2,i), method+"| Plate "+i+" > ");
            }
        }
    }

    public static void comparePlate(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {

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
                        () -> methods.get(method).invoke(meta_2,i), method+"| Plate "+i+" > ");
            }
        }
    }

    public static void compareInstrument(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {

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
                        () -> methods.get(method).invoke(meta_2,i), method+"| Instrument "+i+" > ");
            }
        }
    }


    private static void comparePlanes(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {
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
                    () -> meta_2.getPlaneCount(iImageFinal), "Different plane count found for image "+iImageFinal)) {
                for (int iPlane = 0; iPlane < meta_1.getPlaneCount(iImage); iPlane++) {
                    int iPlaneFinal = iPlane;
                    for (String method : methods_ImageIdx_PlaneIdx) {
                        isEqualExceptionAndNullSafe(
                                () -> methods.get(method).invoke(meta_1, iImageFinal, iPlaneFinal),
                                () -> methods.get(method).invoke(meta_2, iImageFinal, iPlaneFinal), method + "| Image " + iImageFinal + " Plane " + iPlaneFinal + " > ");
                    }
                }
            }
        }
    }

    private static void compareChannels(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {
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
                    () -> meta_2.getChannelCount(iImageFinal), "Different channel count found for image "+iImageFinal)) {
                for (int iChannel = 0; iChannel < meta_1.getChannelCount(iImage); iChannel++) {
                    int iChannelFinal = iChannel;
                    for (String method : methods_ImageIdx_ChannelIdx) {
                        isEqualExceptionAndNullSafe(
                                () -> methods.get(method).invoke(meta_1, iImageFinal, iChannelFinal),
                                () -> methods.get(method).invoke(meta_2, iImageFinal, iChannelFinal), method + "| Image " + iImageFinal + " Channel " + iChannelFinal + " > ");
                    }
                }
            }
        }
    }

    private static void compareImageMethods(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {
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
                        () -> methods.get(method).invoke(meta_2, i), method + "| Image " + i + " > ");
            }
        }
    }

    public static void compareExperimenter(OMEXMLMetadata meta_1, OMEXMLMetadata meta_2, Map<String, Method> methods) {
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
                        () -> methods.get(method).invoke(meta_2,i), method+"| Experimenter "+i+" > ");
            }
        }
    }

    private static boolean isEqualExceptionAndNullSafe(Callable<Object> o1Getter, Callable<Object> o2Getter, String message) {

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
            System.out.println( message + "\t 1: \t error: "+exception_1+" \t 2: \t error: "+exception_2);
            return false;
        } else {
            if ((o1!=null)&&(o2!=null)) {
                if (!o1.equals(o2)) {
                    if ((o1 instanceof Length) && (o2 instanceof Length)) {
                        Length l1 = (Length) o1;
                        Length l2 = (Length) o2;
                        double diff = Math.abs(l1.value(UNITS.MICROMETER).doubleValue() - l2.value(UNITS.MICROMETER).doubleValue());

                        if (diff<0.001) {
                            return true;
                        } else {
                            System.out.println( message +"l diff = "+diff+ "\t 1: \t "+o1+"\t 2: \t "+o2);
                        }
                    } else if ((o1 instanceof Time) && (o2 instanceof Time)) {
                        Time t1 = (Time) o1;
                        Time t2 = (Time) o2;
                        double diff = Math.abs(t1.value(UNITS.SECOND).doubleValue() - t2.value(UNITS.SECOND).doubleValue());
                        //System.out.println( message +"t diff = "+diff+ "\t 1: \t "+o1+"\t 2: \t "+o2);
                        if (diff<0.001) {
                            return true; // below 1 ms = equality, yep that's right.
                        } else {
                            System.out.println( message +"t diff = "+diff+ "\t 1: \t "+o1+"\t 2: \t "+o2);
                        }
                    } else {
                        System.out.println(message + "\t 1: \t " + o1 + "\t 2: \t " + o2);
                    }
                    return false;
                } else {
                    //System.out.println( message + "\t 1: \t "+o1+"\t 2: \t "+o2);
                    return true;
                }
            } else {
                if ((o1==null)&&(o2==null)) {
                    return true;
                } else {
                    System.out.println( message + "\t 1: \t "+(o1==null?"null":o1)+"\t 2: \t "+(o2==null?"null":o2));
                    return false;
                }
            }
        }
    }

    public static void main(String... args) throws Exception {
        String imagePath;
        //--------------------------
        // imagePath = "C:\\Users\\nicol\\Dropbox\\230316_stitched.czi";
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
        imagePath = "N:\\temp-Romain\\organoid\\GbD3P_RapiClear_40x_default_miniStack_guess.czi";

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);

        OMEXMLMetadata omeXML_1 = service.createOMEXMLMetadata();
        ZeissQuickStartCZIReader reader_1 = new ZeissQuickStartCZIReader();
        //reader_1.setFlattenedResolutions(false); // To test in bdv condition
        reader_1.setMetadataStore(omeXML_1);
        reader_1.setId(imagePath);

        OMEXMLMetadata omeXML_2 = service.createOMEXMLMetadata();
        ZeissCZIReader reader_2 = new ZeissCZIReader();
        //reader_2.setFlattenedResolutions(false); // To test in bdv condition
        reader_2.setMetadataStore(omeXML_2);
        reader_2.setId(imagePath);

        compareMeta(omeXML_1, omeXML_2);
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
        reader.setFlattenedResolutions(true);
        List<ImagePlus> images1 = ImageJOpen.openWithReader(reader, imagePath,0);

        ImagePlus imgReader1 = images1.get(0);
        imgReader1.setTitle("Reader ZeissCZIReader");
        imgReader1.show();


        ZeissQuickStartCZIReader quickreader = new ZeissQuickStartCZIReader();
        quickreader.setFlattenedResolutions(true);
        List<ImagePlus> images2 = ImageJOpen.openWithReader(quickreader, imagePath,0);
        ImagePlus imgReader2 = images2.get(0);
        imgReader2.setTitle("Reader ZeissQuickStartCZIReader");
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

}
