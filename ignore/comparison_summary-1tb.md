# Explained metadata differences between ZeissQuickStartCZIReader and ZeissCZI Reader
## Rule #1  IMPROVEMENT 
The quick reader reads the czi stage label name, instead of iterating scene indices. This fits better with the original CZI file.

Affected images:
## Rule #2  IMPROVEMENT 
The quick reader reads the Z location correctly, and not the original reader.

Affected images:
## Rule #3  IMPROVEMENT 
The quick reader reads correctly the physical pixel for the lower resolution levels, in contrast to the original reader,  which sets the same pixel size for all resolution levels (the one of the highest resolution).

Affected images:
## Rule #4  IMPROVEMENT 
getPlaneDeltaT returns a wrong value (timestamp from first series) for the lower resolution levels in the original reader

Affected images:
## Rule #5  IDENTICAL 
Metadata not present for modulo planes in original reader

Affected images:
## Rule #6  IMPROVEMENT 
The stage label is null in the original reader.

Affected images:
## Rule #7  IDENTICAL 
When the pixel size is not defined (label / overview), the stage and plane location is in reference frame in the quick reader, and in micrometer in the original reader.

Affected images:
## Rule #8  ISSUE 
An edge case when the pixel size in Z is not defined, so one has to add a 0 in reference frame unit and a micrometer value in the metadata. The new reader chooses to set this operation as null, while the original reader keeps the micrometer value only

Affected images:
## Rule #9  IDENTICAL 
The quick reader sets the plane location as 0 micrometer, while the  original reader sets it as 0 in reference frame unit.

Affected images:
## Rule #10  IDENTICAL 
A constant shift in XY (40 nm) that is below half a pixel (100 nm),

Affected images:
## Rule #11  IMPROVEMENT 
The original reader is not consistent when setting the plane positions: it returns the top left corner of all images, except when a plane contains many blocks (mosaic) and autostitching is set to true: in this case the original reader return the center location of the image. In comparison, the quick reader consistently returns the top left corner of the image, whether autostitch is true or false.

Affected images:

# Table
|OK |File Name|AutoStitch|#Diffs<br>(Critical)|#Diffs|#Diffs Ignored|#DiffsPixels|Mem Gain|Init Time Gain|Read Time Gain|
|---|---------|----------|--------------------|------|--------------|------------|--------|--------------|--------------|
| |[TL-03.czi](../compare/TL-03.flat_true.stitch_false.md)|false|1|2441953|0|51128|2.7|5.5|1.5|
| |[TL-03.czi](../compare/TL-03.flat_true.stitch_true.md)|true|1|2441953|0|51128|2.7|8.8|2.3|
