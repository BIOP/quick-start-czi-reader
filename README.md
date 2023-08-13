[![Build Status](https://github.com/BIOP/quick-start-czi-reader/actions/workflows/build.yml/badge.svg)](https://github.com/BIOP/quick-start-czi-reader/actions/workflows/build.yml)

# Quick start Bio-Formats CZI reader

A revamped version of the original ZeissCZIReader which parses faster the metadata. This is critical to open multi-TB files in Fiji in a reasonable amount of time.

This new reader is tested against a [list of publicly available CZI files](comparison_summary.md).

For more information see:
- the [original issue](https://github.com/ome/bioformats/issues/3839)
- a [draft PR on Bio-Formats](https://github.com/ome/bioformats/pull/4009)
- go to the compare folder of this repository to see how this readers differs from the bio-formats reader for a set of publicly available czi files

To use this reader instead of the original one, you will need to activate the Fiji update site [Zeiss Quick Start Reader](https://imagej.net/plugins/zeiss-quick-start-reader) and by enabling the Zeiss(CZI) Quick Start reader in the Bio-Formats plugins configuration.

Special thanks to Zeiss for [opening its file format](https://github.com/ZEISS/libczi) and allowing open source software to improve!
