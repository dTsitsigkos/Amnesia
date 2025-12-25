/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

/**
 *
 * @author dimak
 */

public class DicomMetadata {
    public static String[] columnNames = {
        // --- Original attributes ---
        "PatientID",
        "PatientName",
        "PatientAge",
        "Modality",
        "PatientSex",
        "PatientBirthDate",
        "PhotometricInterpretation",
        "BodyPartExamined",
        "PatientOrientation",
        "ViewPosition",
        "ConversionType",
        "SamplesPerPixel",

        // --- Patient ---
        "PatientSize",
        "PatientWeight",
        "EthnicGroup",
        "PregnancyStatus",
        "PatientComments",

        // --- Study ---
        "StudyInstanceUID",
        "StudyID",
        "StudyDate",
        "StudyTime",
        "AccessionNumber",
        "ReferringPhysicianName",
        "StudyDescription",
        "AdmittingDiagnosesDescription",

        // --- Series ---
        "SeriesInstanceUID",
        "SeriesNumber",
        "SeriesDescription",
        "SeriesDate",
        "SeriesTime",
        "FrameOfReferenceUID",
        "Laterality",
        "PositionReferenceIndicator",

        // --- Image / Instance ---
        "SOPClassUID",
        "SOPInstanceUID",
        "InstanceNumber",
        "ImageType",
        "ContentDate",
        "ContentTime",
        "AcquisitionDate",
        "AcquisitionTime",
        "AcquisitionNumber",
        "ImageComments",

        // --- Equipment ---
        "Manufacturer",
        "ManufacturerModelName",
        "DeviceSerialNumber",
        "StationName",
        "SoftwareVersions",
        "OperatorsName",
        "InstitutionName",
        "InstitutionAddress",

        // --- Image Geometry ---
        "ImagePositionPatient",
        "ImageOrientationPatient",
        "SliceThickness",
        "SliceLocation",
        "SpacingBetweenSlices",
        "PixelSpacing",
        "PixelAspectRatio",
        "ReconstructionDiameter",
        "FieldOfViewDimensions",
        "WindowCenter",
        "WindowWidth",
        "WindowCenterWidthExplanation",

        // --- Pixel Data ---
        "Rows",
        "Columns",
        "BitsAllocated",
        "BitsStored",
        "HighBit",
        "PixelRepresentation",
        "PlanarConfiguration",
        "LossyImageCompression",
        "LossyImageCompressionRatio",
        "LossyImageCompressionMethod",

        // --- CT Specific ---
        "KVP",
        "XRayTubeCurrent",
        "ExposureTime",
        "Exposure",
        "FilterType",
        "ConvolutionKernel",
        "GantryDetectorTilt",
        "ReconstructionAlgorithm",
        "CTDIvol",
        "ScanOptions",

        // --- MRI Specific ---
        "RepetitionTime",
        "EchoTime",
        "InversionTime",
        "FlipAngle",
        "MagneticFieldStrength",
        "EchoTrainLength",
        "SequenceName",
        "SequenceVariant",
        "ScanningSequence",
        "ImagingFrequency",

        // --- Multiframe / 4D ---
        "NumberOfFrames",
        "FrameTime",
        "FrameIncrementPointer",

        // --- Added (PixelMed-safe) ---
        "SpecificCharacterSet",
        "ImageLaterality",
        "BurnedInAnnotation",
        "ProtocolName"
    };
}
