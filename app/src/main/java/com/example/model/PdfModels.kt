package com.example.model

import android.net.Uri

enum class ImageFilterType(val displayName: String) {
    ORIGINAL("Original"),
    BW_DOCUMENT("Doc Scan (B&W)"),
    GRAYSCALE("Grayscale"),
    VIVID("Vivid Color"),
    HIGH_CONTRAST("High Contrast"),
    WARM("Warm Tone"),
    COOL("Cool Tone")
}

enum class CropMode(val displayName: String) {
    FIT_PAGE("Fit (Maintain Aspect)"),
    FILL_PAGE("Fill (Crop to Page)")
}

enum class PageSizeOption(val displayName: String, val widthPt: Int, val heightPt: Int) {
    A4("A4 Standard", 595, 842),
    LETTER("US Letter", 612, 792),
    LEGAL("US Legal", 612, 1008),
    EXECUTIVE("Executive", 522, 756),
    MATCH_IMAGE("Fit Picture Dimensions", 0, 0)
}

enum class PageOrientationOption(val displayName: String) {
    AUTO("Auto Orientation"),
    PORTRAIT("Portrait (Vertical)"),
    LANDSCAPE("Landscape (Horizontal)")
}

enum class PageMarginOption(val displayName: String, val marginPt: Int) {
    NONE("No Margin (0 pt)", 0),
    COMPACT("Compact (18 pt)", 18),
    STANDARD("Standard (36 pt)", 36),
    SPACIOUS("Spacious (54 pt)", 54)
}

enum class CompressionQuality(val displayName: String, val qualityPercent: Int, val scaleFactor: Float) {
    ORIGINAL("Maximum Quality (100%)", 100, 1.0f),
    HIGH("High Quality (85%)", 85, 0.9f),
    MEDIUM("Balanced (70%)", 70, 0.75f),
    LOW("Smallest File Size (50%)", 50, 0.6f)
}

enum class PageNumberFormat(val displayName: String) {
    NONE("None"),
    BOTTOM_CENTER("Bottom Center (Page X of Y)"),
    BOTTOM_RIGHT("Bottom Right (X/Y)"),
    TOP_RIGHT("Top Right (X)")
}

data class ImagePageItem(
    val id: String,
    val uri: Uri? = null,
    val sampleResId: Int? = null,
    val title: String = "Page",
    val rotationDegrees: Int = 0,
    val filter: ImageFilterType = ImageFilterType.ORIGINAL,
    val cropMode: CropMode = CropMode.FIT_PAGE
)

data class PdfConfig(
    val title: String = "",
    val author: String = "Pic to PDF for Android",
    val pageSize: PageSizeOption = PageSizeOption.A4,
    val orientation: PageOrientationOption = PageOrientationOption.AUTO,
    val margin: PageMarginOption = PageMarginOption.COMPACT,
    val quality: CompressionQuality = CompressionQuality.HIGH,
    val pageNumberFormat: PageNumberFormat = PageNumberFormat.BOTTOM_CENTER,
    val isGrayscale: Boolean = false
)

sealed interface ConversionState {
    data object Idle : ConversionState
    data class Converting(val progress: Float, val currentStep: String) : ConversionState
    data class Success(val filePath: String, val fileSizeBytes: Long, val pageCount: Int, val title: String) : ConversionState
    data class Error(val message: String) : ConversionState
}
