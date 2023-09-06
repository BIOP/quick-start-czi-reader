# Explained metadata differences between ZeissQuickStartCZIReader and ZeissCZI Reader
## Rule #1  IMPROVEMENT 
The quick reader reads the czi stage label name, instead of iterating scene indices. This fits better with the original CZI file.

Affected images:
* (x 2) S=1_3x3_T=3_Z=4_CH=2.czi.autoStitch.true
* (x 1) S=1_CH=2.czi.autoStitch.true
* (x 1) Z=5_CH=2.czi.autoStitch.true
* (x 9) S=1_3x3_T=3_Z=4_CH=2.czi.autoStitch.false
* (x 1) T=2_Z=5_CH=2.czi.autoStitch.false
* (x 31) Demo LISH 4x8 15pct 647.czi.autoStitch.false
* (x 90) W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi.autoStitch.false
* (x 1) S=1_CH=2.czi.autoStitch.false
* (x 1) Plate1-Blue-A-12-Scene-3-P3-F2-03.czi.autoStitch.true
* (x 5) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.true
* (x 184) v.zanotelli_20190509_p165_031.czi.autoStitch.false
* (x 6) W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi.autoStitch.true
* (x 18) S=2_3x3_T=1_Z=4_CH=2.czi.autoStitch.false
* (x 5) 3Dexample.czi.autoStitch.false
* (x 4) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.true
* (x 1) MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.czi.autoStitch.false
* (x 1) T=3_CH=2.czi.autoStitch.false
* (x 1) T=3_CH=2.czi.autoStitch.true
* (x 35) test_gray.czi.autoStitch.false
* (x 99) test-plate.czi.autoStitch.true
* (x 4) S=2_3x3_T=1_Z=4_CH=2.czi.autoStitch.true
* (x 1) MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.czi.autoStitch.true
* (x 1) Z=5_CH=2.czi.autoStitch.false
* (x 15) v.zanotelli_20190509_p165_031.czi.autoStitch.true
* (x 2) S=2_T=3_CH=1.czi.autoStitch.false
* (x 2) S=2_T=3_CH=1.czi.autoStitch.true
* (x 18) S=2_3x3_T=3_CH=2.czi.autoStitch.false
* (x 1) T=1_CH=2.czi.autoStitch.true
* (x 8) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.false
* (x 1) T=3_Z=5_CH=2.czi.autoStitch.true
* (x 1) MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.czi.autoStitch.true
* (x 1) Plate1-Blue-A-12-Scene-3-P3-F2-03.czi.autoStitch.false
* (x 2) S=2_T=3_Z=5_CH=1.czi.autoStitch.true
* (x 45) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.false
* (x 6) W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi.autoStitch.true
* (x 2) S=2_T=3_Z=5_CH=1.czi.autoStitch.false
* (x 8) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.false
* (x 4) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.true
* (x 8) S=2_2x2_Z=4_CH=1.czi.autoStitch.false
* (x 8) S=2_2x2_T=3_CH=1.czi.autoStitch.false
* (x 3) MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.czi.autoStitch.false
* (x 4) S=2_2x2_Z=4_CH=1.czi.autoStitch.true
* (x 1) T=1_CH=2.czi.autoStitch.false
* (x 1) T=2_Z=5_CH=2.czi.autoStitch.true
* (x 90) W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi.autoStitch.false
* (x 3) S=3_CH=2.czi.autoStitch.false
* (x 1) MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.czi.autoStitch.false
* (x 3) Image_1_2023_08_18__14_32_31_964.czi.autoStitch.false
* (x 4) S=2_2x2_CH=1.czi.autoStitch.true
* (x 1) T=3_Z=5_CH=2.czi.autoStitch.false
* (x 8) S=2_2x2_CH=1.czi.autoStitch.false
* (x 3) S=3_CH=2.czi.autoStitch.true
* (x 4) S=2_2x2_T=3_CH=1.czi.autoStitch.true
* (x 18) S=2_3x3_T=3_Z=1_CH=2.czi.autoStitch.false
* (x 4) S=2_3x3_T=3_CH=2.czi.autoStitch.true
* (x 4) S=2_3x3_T=3_Z=1_CH=2.czi.autoStitch.true
* (x 99) test-plate.czi.autoStitch.false
## Rule #2  IMPROVEMENT 
The quick reader reads the Z location correctly, and not the original reader.

Affected images:
* (x 2499) RBC_full_time_series.czi.autoStitch.false
* (x 20) Plate1-Blue-A-12-Scene-3-P3-F2-03.czi.autoStitch.true
* (x 833) RBC_tiny.czi.autoStitch.false
* (x 833) RBC_full_one_timepoint.czi.autoStitch.false
* (x 4165) RBC_medium_LLSZ.czi.autoStitch.false
* (x 2499) RBC_full_time_series.czi.autoStitch.true
* (x 4165) RBC_medium_LLSZ.czi.autoStitch.true
* (x 833) RBC_tiny.czi.autoStitch.true
* (x 20) Plate1-Blue-A-12-Scene-3-P3-F2-03.czi.autoStitch.false
* (x 833) RBC_full_one_timepoint.czi.autoStitch.true
## Rule #3  IMPROVEMENT 
The quick reader reads correctly the physical pixel for the lower resolution levels, in contrast to the original reader,  which sets the same pixel size for all resolution levels (the one of the highest resolution).

Affected images:
* (x 4) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.true
* (x 46) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.false
* (x 8) W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi.autoStitch.true
* (x 4) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.true
* (x 4) S=2_2x2_T=3_CH=1.czi.autoStitch.true
* (x 8) W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi.autoStitch.true
* (x 4) S=2_3x3_T=1_Z=4_CH=2.czi.autoStitch.true
* (x 4) S=2_3x3_T=3_CH=2.czi.autoStitch.true
* (x 4) S=2_3x3_T=3_Z=1_CH=2.czi.autoStitch.true
* (x 4) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.true
* (x 4) S=2_2x2_Z=4_CH=1.czi.autoStitch.true
* (x 4) S=2_2x2_CH=1.czi.autoStitch.true
## Rule #4  IMPROVEMENT 
getPlaneDeltaT returns a wrong value (timestamp from first series) for the lower resolution levels in the original reader

Affected images:
* (x 21) S=1_3x3_T=3_Z=4_CH=2.czi.autoStitch.true
* (x 7) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.true
* (x 216) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.false
* (x 92) W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi.autoStitch.true
* (x 15) S=2_3x3_T=1_Z=4_CH=2.czi.autoStitch.true
* (x 21) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.true
* (x 1) S=2_2x2_CH=1.czi.autoStitch.true
* (x 22) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.true
* (x 3) S=2_2x2_T=3_CH=1.czi.autoStitch.true
* (x 2) W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi.autoStitch.true
* (x 9) S=2_3x3_T=3_CH=2.czi.autoStitch.true
* (x 9) S=2_3x3_T=3_Z=1_CH=2.czi.autoStitch.true
* (x 7) S=2_2x2_Z=4_CH=1.czi.autoStitch.true
## Rule #5  IDENTICAL 
Metadata not present for modulo planes in original reader

Affected images:
* (x 3255) 2021-02-25-tulip_unprocessed-Airyscan.czi.autoStitch.true
* (x 3255) 2021-02-25-tulip_unprocessed-Airyscan.czi.autoStitch.false
## Rule #6  IMPROVEMENT 
The stage label is null in the original reader.

Affected images:
* (x 31) Demo LISH 4x8 15pct 647.czi.autoStitch.false
* (x 3) Image_1_2023_08_18__14_32_31_964.czi.autoStitch.false
## Rule #7  IDENTICAL 
When the pixel size is not defined (label / overview), the stage and plane location is in reference frame in the quick reader, and in micrometer in the original reader.

Affected images:
* (x 4) v.zanotelli_20190509_p165_031_pt1.czi.autoStitch.true
* (x 4) v.zanotelli_20190509_p165_031_pt1.czi.autoStitch.false
## Rule #8  ISSUE 
An edge case when the pixel size in Z is not defined, so one has to add a 0 in reference frame unit and a micrometer value in the metadata. The new reader chooses to set this operation as null, while the original reader keeps the micrometer value only

Affected images:
* (x 2) v.zanotelli_20190509_p165_031_pt1.czi.autoStitch.true
* (x 2) v.zanotelli_20190509_p165_031_pt1.czi.autoStitch.false
## Rule #9  IDENTICAL 
The quick reader sets the plane location as 0 micrometer, while the  original reader sets it as 0 in reference frame unit.

Affected images:
* (x 4) v.zanotelli_20190509_p165_031_pt2.czi.autoStitch.false
* (x 4) v.zanotelli_20190509_p165_031_pt2.czi.autoStitch.true
## Rule #10  IDENTICAL 
A constant shift in XY (40 nm) that is below half a pixel (100 nm),

Affected images:
* (x 40) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.true
* (x 16) S=2_2x2_CH=1.czi.autoStitch.false
* (x 32) S=2_2x2_T=3_CH=1.czi.autoStitch.false
* (x 104) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.false
* (x 32) S=2_2x2_T=3_CH=1.czi.autoStitch.true
* (x 40) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.false
* (x 104) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.true
* (x 40) S=2_2x2_Z=4_CH=1.czi.autoStitch.false
* (x 40) S=2_2x2_Z=4_CH=1.czi.autoStitch.true
* (x 16) S=2_2x2_CH=1.czi.autoStitch.true
## Rule #11  IMPROVEMENT 
The original reader is not consistent when setting the plane positions: it returns the top left corner of all images, except when a plane contains many blocks (mosaic) and autostitching is set to true: in this case the original reader return the center location of the image. In comparison, the quick reader consistently returns the top left corner of the image, whether autostitch is true or false.

Affected images:
* (x 100) S=1_3x3_T=3_Z=4_CH=2.czi.autoStitch.true
* (x 40) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.true
* (x 671) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.false
* (x 300) W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi.autoStitch.true
* (x 166) MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.czi.autoStitch.false
* (x 72) S=2_3x3_T=1_Z=4_CH=2.czi.autoStitch.true
* (x 494) MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.czi.autoStitch.false
* (x 40) S=2_2x2_T=1_Z=4_CH=1.czi.autoStitch.false
* (x 104) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.true
* (x 494) MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.czi.autoStitch.true
* (x 40) S=2_2x2_Z=4_CH=1.czi.autoStitch.false
* (x 494) MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.czi.autoStitch.true
* (x 130) S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi.autoStitch.true
* (x 32) S=2_2x2_T=3_CH=1.czi.autoStitch.false
* (x 104) S=2_2x2_T=3_Z=4_CH=1.czi.autoStitch.false
* (x 1976) MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.czi.autoStitch.false
* (x 32) S=2_2x2_T=3_CH=1.czi.autoStitch.true
* (x 24) W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi.autoStitch.true
* (x 166) MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.czi.autoStitch.true
* (x 56) S=2_3x3_T=3_CH=2.czi.autoStitch.true
* (x 56) S=2_3x3_T=3_Z=1_CH=2.czi.autoStitch.true
* (x 40) S=2_2x2_Z=4_CH=1.czi.autoStitch.true

# Table
|OK |File Name|AutoStitch|#Diffs<br>(Critical)|#Diffs|#Diffs Ignored|#DiffsPixels|Mem Gain|Init Time Gain|Read Time Gain|
|---|---------|----------|--------------------|------|--------------|------------|--------|--------------|--------------|
| |[Demo LISH 4x8 15pct 647.czi](compare/Demo LISH 4x8 15pct 647.flat_true.stitch_false.md)|false|0|5384|62|0|3.2|1.2|0.6|
| |[Demo LISH 4x8 15pct 647.czi](compare/Demo LISH 4x8 15pct 647.flat_true.stitch_true.md)|true|0|114|0|0|11.2|2.1|0.9|
|✓|[test_gray.czi](compare/test_gray.flat_true.stitch_false.md)|false|0|0|35|0|1.4|0.4|1.3|
|✓|[test_gray.czi](compare/test_gray.flat_true.stitch_true.md)|true|0|0|0|0|2.4|0.6|0.9|
| |[Image_1_2023_08_18__14_32_31_964.czi](compare/Image_1_2023_08_18__14_32_31_964.flat_true.stitch_false.md)|false|0|19|6|0|1.6|1.0|1.1|
| |[Image_1_2023_08_18__14_32_31_964.czi](compare/Image_1_2023_08_18__14_32_31_964.flat_true.stitch_true.md)|true|0|4|0|0|1.6|1.0|1.1|
| |[test-plate.czi](compare/test-plate.flat_true.stitch_false.md)|false|0|495|99|0|1.3|0.5|1.0|
| |[test-plate.czi](compare/test-plate.flat_true.stitch_true.md)|true|0|495|99|0|1.3|0.5|1.1|
|✓|[P1.czi](compare/P1.flat_true.stitch_false.md)|false|0|0|0|0|1.8|1.0|1.2|
|✓|[P1.czi](compare/P1.flat_true.stitch_true.md)|true|0|0|0|0|1.8|0.8|1.2|
|✓|[2021-02-25-tulip_Airyscan.czi](compare/2021-02-25-tulip_Airyscan.flat_true.stitch_false.md)|false|0|0|0|0|1.7|1.0|1.4|
|✓|[2021-02-25-tulip_Airyscan.czi](compare/2021-02-25-tulip_Airyscan.flat_true.stitch_true.md)|true|0|0|0|0|1.7|1.0|1.3|
|✓|[2021-02-25-tulip_unprocessed-Airyscan.czi](compare/2021-02-25-tulip_unprocessed-Airyscan.flat_true.stitch_false.md)|false|0|0|3255|0|2.1|0.9|0.9|
|✓|[2021-02-25-tulip_unprocessed-Airyscan.czi](compare/2021-02-25-tulip_unprocessed-Airyscan.flat_true.stitch_true.md)|true|0|0|3255|0|2.1|0.9|1.1|
|✓|[Airyscan Lines Pattern.czi](compare/Airyscan Lines Pattern.flat_true.stitch_false.md)|false|0|0|0|0|1.6|1.3|1.3|
|✓|[Airyscan Lines Pattern.czi](compare/Airyscan Lines Pattern.flat_true.stitch_true.md)|true|0|0|0|0|1.6|1.3|1.2|
|✓|[Confocal Lines Pattern.czi](compare/Confocal Lines Pattern.flat_true.stitch_false.md)|false|0|0|0|0|1.6|1.3|1.4|
|✓|[Confocal Lines Pattern.czi](compare/Confocal Lines Pattern.flat_true.stitch_true.md)|true|0|0|0|0|1.6|1.3|1.3|
|✓|[S=1_3x3_T=3_Z=4_CH=2.czi](compare/S=1_3x3_T=3_Z=4_CH=2.flat_true.stitch_false.md)|false|0|0|9|0|1.4|0.9|1.1|
| |[S=1_3x3_T=3_Z=4_CH=2.czi](compare/S=1_3x3_T=3_Z=4_CH=2.flat_true.stitch_true.md)|true|0|27|123|0|1.4|1.4|1.2|
|✓|[S=1_CH=2.czi](compare/S=1_CH=2.flat_true.stitch_false.md)|false|0|0|1|0|1.3|1.5|1.3|
|✓|[S=1_CH=2.czi](compare/S=1_CH=2.flat_true.stitch_true.md)|true|0|0|1|0|1.3|1.4|1.4|
|✓|[S=2_2x2_CH=1.czi](compare/S=2_2x2_CH=1.flat_true.stitch_false.md)|false|0|0|24|0|1.3|0.9|1.2|
| |[S=2_2x2_CH=1.czi](compare/S=2_2x2_CH=1.flat_true.stitch_true.md)|true|0|4|25|0|1.3|1.0|1.2|
|✓|[S=2_2x2_T=1_Z=4_CH=1.czi](compare/S=2_2x2_T=1_Z=4_CH=1.flat_true.stitch_false.md)|false|0|0|48|0|1.3|0.9|1.4|
| |[S=2_2x2_T=1_Z=4_CH=1.czi](compare/S=2_2x2_T=1_Z=4_CH=1.flat_true.stitch_true.md)|true|0|10|55|0|1.3|1.0|1.4|
|✓|[S=2_2x2_T=3_CH=1.czi](compare/S=2_2x2_T=3_CH=1.flat_true.stitch_false.md)|false|0|0|40|0|1.3|0.9|1.2|
| |[S=2_2x2_T=3_CH=1.czi](compare/S=2_2x2_T=3_CH=1.flat_true.stitch_true.md)|true|0|8|43|0|1.3|1.0|1.2|
|✓|[S=2_2x2_T=3_Z=4_CH=1.czi](compare/S=2_2x2_T=3_Z=4_CH=1.flat_true.stitch_false.md)|false|0|0|112|0|1.3|1.0|1.2|
| |[S=2_2x2_T=3_Z=4_CH=1.czi](compare/S=2_2x2_T=3_Z=4_CH=1.flat_true.stitch_true.md)|true|0|26|133|0|1.3|1.1|1.1|
|✓|[S=2_2x2_Z=4_CH=1.czi](compare/S=2_2x2_Z=4_CH=1.flat_true.stitch_false.md)|false|0|0|48|0|1.3|0.9|1.2|
| |[S=2_2x2_Z=4_CH=1.czi](compare/S=2_2x2_Z=4_CH=1.flat_true.stitch_true.md)|true|0|10|55|0|1.3|1.0|1.2|
|✓|[S=2_3x3_T=1_Z=4_CH=2.czi](compare/S=2_3x3_T=1_Z=4_CH=2.flat_true.stitch_false.md)|false|0|0|18|0|1.3|0.8|1.1|
| |[S=2_3x3_T=1_Z=4_CH=2.czi](compare/S=2_3x3_T=1_Z=4_CH=2.flat_true.stitch_true.md)|true|0|18|95|0|1.4|1.1|1.1|
|✓|[S=2_3x3_T=3_CH=2.czi](compare/S=2_3x3_T=3_CH=2.flat_true.stitch_false.md)|false|0|0|18|0|1.3|0.6|1.1|
| |[S=2_3x3_T=3_CH=2.czi](compare/S=2_3x3_T=3_CH=2.flat_true.stitch_true.md)|true|0|14|73|0|1.4|1.1|1.1|
|✓|[S=2_3x3_T=3_Z=1_CH=2.czi](compare/S=2_3x3_T=3_Z=1_CH=2.flat_true.stitch_false.md)|false|0|0|18|0|1.3|0.8|1.1|
| |[S=2_3x3_T=3_Z=1_CH=2.czi](compare/S=2_3x3_T=3_Z=1_CH=2.flat_true.stitch_true.md)|true|0|14|73|0|1.3|1.1|1.1|
|✓|[S=2_T=3_CH=1.czi](compare/S=2_T=3_CH=1.flat_true.stitch_false.md)|false|0|0|2|0|1.3|0.9|1.2|
|✓|[S=2_T=3_CH=1.czi](compare/S=2_T=3_CH=1.flat_true.stitch_true.md)|true|0|0|2|0|1.3|1.4|1.1|
|✓|[S=2_T=3_Z=5_CH=1.czi](compare/S=2_T=3_Z=5_CH=1.flat_true.stitch_false.md)|false|0|0|2|0|1.3|1.0|1.1|
|✓|[S=2_T=3_Z=5_CH=1.czi](compare/S=2_T=3_Z=5_CH=1.flat_true.stitch_true.md)|true|0|0|2|0|1.3|0.9|1.1|
| |[S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi](compare/S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.flat_true.stitch_false.md)|false|116|925|978|404999|1.5|0.7|1.8|
| |[S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.czi](compare/S=3_1Pos_2Mosaic_T=2=Z=3_CH=2.flat_true.stitch_true.md)|true|0|26|161|0|1.5|1.4|1.2|
|✓|[S=3_CH=2.czi](compare/S=3_CH=2.flat_true.stitch_false.md)|false|0|0|3|0|1.3|0.9|1.3|
|✓|[S=3_CH=2.czi](compare/S=3_CH=2.flat_true.stitch_true.md)|true|0|0|3|0|1.3|0.9|1.2|
|✓|[T=1_CH=2.czi](compare/T=1_CH=2.flat_true.stitch_false.md)|false|0|0|1|0|1.3|1.5|1.4|
|✓|[T=1_CH=2.czi](compare/T=1_CH=2.flat_true.stitch_true.md)|true|0|0|1|0|1.3|1.4|1.4|
|✓|[T=1_Z=5_CH=1.czi](compare/T=1_Z=5_CH=1.flat_true.stitch_false.md)|false|0|0|0|0|1.3|1.0|1.1|
|✓|[T=1_Z=5_CH=1.czi](compare/T=1_Z=5_CH=1.flat_true.stitch_true.md)|true|0|0|0|0|1.3|1.0|1.1|
|✓|[T=2_CH=1.czi](compare/T=2_CH=1.flat_true.stitch_false.md)|false|0|0|0|0|1.3|1.4|1.4|
|✓|[T=2_CH=1.czi](compare/T=2_CH=1.flat_true.stitch_true.md)|true|0|0|0|0|1.3|1.5|1.4|
|✓|[T=2_Z=5_CH=1.czi](compare/T=2_Z=5_CH=1.flat_true.stitch_false.md)|false|0|0|0|0|1.3|1.0|1.1|
|✓|[T=2_Z=5_CH=1.czi](compare/T=2_Z=5_CH=1.flat_true.stitch_true.md)|true|0|0|0|0|1.3|0.9|1.2|
|✓|[T=2_Z=5_CH=2.czi](compare/T=2_Z=5_CH=2.flat_true.stitch_false.md)|false|0|0|1|0|1.3|1.0|1.0|
|✓|[T=2_Z=5_CH=2.czi](compare/T=2_Z=5_CH=2.flat_true.stitch_true.md)|true|0|0|1|0|1.3|1.0|1.1|
|✓|[T=3_CH=2.czi](compare/T=3_CH=2.flat_true.stitch_false.md)|false|0|0|1|0|1.3|0.9|1.2|
|✓|[T=3_CH=2.czi](compare/T=3_CH=2.flat_true.stitch_true.md)|true|0|0|1|0|1.3|0.9|1.2|
|✓|[T=3_Z=5_CH=2.czi](compare/T=3_Z=5_CH=2.flat_true.stitch_false.md)|false|0|0|1|0|1.3|1.0|1.1|
|✓|[T=3_Z=5_CH=2.czi](compare/T=3_Z=5_CH=2.flat_true.stitch_true.md)|true|0|0|1|0|1.3|1.0|1.2|
|✓|[W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi](compare/W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.flat_true.stitch_false.md)|false|0|0|90|0|1.3|0.6|1.2|
| |[W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.czi](compare/W96_B2+B4_S=2_T=1=Z=1_C=1_Tile=5x9.flat_true.stitch_true.md)|true|0|8|40|0|1.4|0.9|1.1|
|✓|[W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi](compare/W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.flat_true.stitch_false.md)|false|0|0|90|0|1.9|0.7|1.1|
| |[W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.czi](compare/W96_B2+B4_S=2_T=2=Z=4_C=3_Tile=5x9.flat_true.stitch_true.md)|true|0|100|406|0|2.5|3.0|1.2|
|✓|[Z=5_CH=1.czi](compare/Z=5_CH=1.flat_true.stitch_false.md)|false|0|0|0|0|1.3|1.0|1.2|
|✓|[Z=5_CH=1.czi](compare/Z=5_CH=1.flat_true.stitch_true.md)|true|0|0|0|0|1.3|1.1|1.1|
|✓|[Z=5_CH=2.czi](compare/Z=5_CH=2.flat_true.stitch_false.md)|false|0|0|1|0|1.3|0.9|1.2|
|✓|[Z=5_CH=2.czi](compare/Z=5_CH=2.flat_true.stitch_true.md)|true|0|0|1|0|1.3|1.0|1.3|
|✓|[RBC_full_one_timepoint.czi](compare/RBC_full_one_timepoint.flat_true.stitch_false.md)|false|0|0|833|0|1.7|3.0|1.6|
|✓|[RBC_full_one_timepoint.czi](compare/RBC_full_one_timepoint.flat_true.stitch_true.md)|true|0|0|833|0|1.7|3.2|1.6|
|✓|[RBC_full_time_series.czi](compare/RBC_full_time_series.flat_true.stitch_false.md)|false|0|0|2499|0|2.0|5.2|2.0|
|✓|[RBC_full_time_series.czi](compare/RBC_full_time_series.flat_true.stitch_true.md)|true|0|0|2499|0|2.0|5.4|2.0|
|✓|[RBC_medium_LLSZ.czi](compare/RBC_medium_LLSZ.flat_true.stitch_false.md)|false|0|0|4165|0|2.2|6.4|2.0|
|✓|[RBC_medium_LLSZ.czi](compare/RBC_medium_LLSZ.flat_true.stitch_true.md)|true|0|0|4165|0|2.2|6.2|2.2|
|✓|[RBC_tiny.czi](compare/RBC_tiny.flat_true.stitch_false.md)|false|0|0|833|0|1.7|2.9|1.9|
|✓|[RBC_tiny.czi](compare/RBC_tiny.flat_true.stitch_true.md)|true|0|0|833|0|1.7|3.1|1.7|
|✓|[20221019_MixedGrain.czi](compare/20221019_MixedGrain.flat_true.stitch_false.md)|false|0|0|0|0|1.8|1.0|1.3|
|✓|[20221019_MixedGrain.czi](compare/20221019_MixedGrain.flat_true.stitch_true.md)|true|0|0|0|0|1.8|1.0|1.4|
|✓|[20221019_MixedGrain2.czi](compare/20221019_MixedGrain2.flat_true.stitch_false.md)|false|0|0|0|0|1.9|1.0|1.4|
|✓|[20221019_MixedGrain2.czi](compare/20221019_MixedGrain2.flat_true.stitch_true.md)|true|0|0|0|0|1.9|1.2|1.4|
|✓|[Ph488.czi](compare/Ph488.flat_true.stitch_false.md)|false|0|0|0|0|1.7|1.0|1.3|
|✓|[Ph488.czi](compare/Ph488.flat_true.stitch_true.md)|true|0|0|0|0|1.7|1.0|1.3|
| |[v.zanotelli_20190509_p165_031.czi](compare/v.zanotelli_20190509_p165_031.flat_true.stitch_false.md)|false|117|1864|184|1299741|1.1|0.8|1.4|
| |[v.zanotelli_20190509_p165_031.czi](compare/v.zanotelli_20190509_p165_031.flat_true.stitch_true.md)|true|0|179|15|127797|1.1|1.3|1.3|
|✓|[v.zanotelli_20190509_p165_031_pt1.czi](compare/v.zanotelli_20190509_p165_031_pt1.flat_true.stitch_false.md)|false|0|0|6|0|1.2|1.4|1.2|
|✓|[v.zanotelli_20190509_p165_031_pt1.czi](compare/v.zanotelli_20190509_p165_031_pt1.flat_true.stitch_true.md)|true|0|0|6|0|1.2|1.1|2.7|
|✓|[v.zanotelli_20190509_p165_031_pt2.czi](compare/v.zanotelli_20190509_p165_031_pt2.flat_true.stitch_false.md)|false|0|0|4|0|1.2|1.4|1.1|
|✓|[v.zanotelli_20190509_p165_031_pt2.czi](compare/v.zanotelli_20190509_p165_031_pt2.flat_true.stitch_true.md)|true|0|0|4|0|1.2|1.2|1.1|
|✓|[10.5 dpc vegfc gapdh Pecam wt 1.czi](compare/10.5 dpc vegfc gapdh Pecam wt 1.flat_true.stitch_false.md)|false|0|0|0|0|1.4|1.0|1.1|
|✓|[10.5 dpc vegfc gapdh Pecam wt 1.czi](compare/10.5 dpc vegfc gapdh Pecam wt 1.flat_true.stitch_true.md)|true|0|0|0|0|1.4|0.9|1.1|
|✓|[Plate1-Blue-A-12-Scene-3-P3-F2-03.czi](compare/Plate1-Blue-A-12-Scene-3-P3-F2-03.flat_true.stitch_false.md)|false|0|0|21|0|1.2|0.8|1.1|
|✓|[Plate1-Blue-A-12-Scene-3-P3-F2-03.czi](compare/Plate1-Blue-A-12-Scene-3-P3-F2-03.flat_true.stitch_true.md)|true|0|0|21|0|1.2|0.8|1.2|
|✓|[xt-scan-lsm980.czi](compare/xt-scan-lsm980.flat_true.stitch_false.md)|false|0|0|0|0|1.5|1.0|1.2|
|✓|[xt-scan-lsm980.czi](compare/xt-scan-lsm980.flat_true.stitch_true.md)|true|0|0|0|0|1.5|1.0|1.1|
| |[xz-scan-lsm980.czi](compare/xz-scan-lsm980.flat_true.stitch_false.md)|false|0|702|0|0|1.6|1.0|1.0|
| |[xz-scan-lsm980.czi](compare/xz-scan-lsm980.flat_true.stitch_true.md)|true|0|702|0|0|1.6|1.0|1.1|
| |[xzt-scan-lsm980.czi](compare/xzt-scan-lsm980.flat_true.stitch_false.md)|false|0|3510|0|0|2.1|0.9|1.0|
| |[xzt-scan-lsm980.czi](compare/xzt-scan-lsm980.flat_true.stitch_true.md)|true|0|3510|0|0|2.1|0.9|1.0|
| |[MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.czi](compare/MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.flat_true.stitch_false.md)|false|0|83|167|0|2.3|1.0|1.1|
| |[MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.czi](compare/MouseBrain_41Slices_1Tile_1Channel_2Illuminations_2Angles.flat_true.stitch_true.md)|true|0|83|167|0|2.3|1.0|1.1|
| |[MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.czi](compare/MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.flat_true.stitch_false.md)|false|0|247|495|0|2.4|1.1|1.1|
| |[MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.czi](compare/MouseBrain_41Slices_1Tile_3Channel_2Illuminations_2Angles.flat_true.stitch_true.md)|true|0|247|495|0|2.4|1.0|1.0|
| |[MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.czi](compare/MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.flat_true.stitch_false.md)|false|0|741|1979|0|2.7|1.1|1.0|
|✓|[MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.czi](compare/MouseBrain_41Slices_2x2Tiles_3Channels_2Illuminations_1Angle.flat_true.stitch_true.md)|true|0|0|494|0|4.7|1.4|1.1|
| |[3Dexample.czi](compare/3Dexample.flat_true.stitch_false.md)|false|0|153|5|0|1.7|1.0|1.1|
| |[3Dexample.czi](compare/3Dexample.flat_true.stitch_true.md)|true|0|18|0|0|1.9|1.3|1.2|
