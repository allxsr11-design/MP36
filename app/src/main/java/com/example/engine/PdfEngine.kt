package com.example.engine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.PdfDocumentEntity
import com.example.model.CompressionQuality
import com.example.model.CropMode
import com.example.model.ImageFilterType
import com.example.model.ImagePageItem
import com.example.model.PageMarginOption
import com.example.model.PageNumberFormat
import com.example.model.PageOrientationOption
import com.example.model.PageSizeOption
import com.example.model.PdfConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfEngine {

    suspend fun generatePdf(
        context: Context,
        pages: List<ImagePageItem>,
        config: PdfConfig,
        onProgress: (Float, String) -> Unit
    ): Result<PdfDocumentEntity> = withContext(Dispatchers.IO) {
        try {
            if (pages.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No images selected to convert."))
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val formattedDate = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date())
            val pdfTitle = if (config.title.isNotBlank()) config.title.trim() else "Document_$timestamp"
            val sanitizedFileName = pdfTitle.replace(Regex("[^a-zA-Z0-9_.-]"), "_") + ".pdf"

            val pdfDir = File(context.filesDir, "converted_pdfs").apply { if (!exists()) mkdirs() }
            val outputFile = File(pdfDir, sanitizedFileName)

            val thumbsDir = File(context.filesDir, "thumbnails").apply { if (!exists()) mkdirs() }
            val thumbFile = File(thumbsDir, "thumb_${sanitizedFileName.replace(".pdf", "")}.jpg")

            val pdfDocument = PdfDocument()
            var firstPageThumbnailBitmap: Bitmap? = null

            val total = pages.size
            for (index in pages.indices) {
                val pageItem = pages[index]
                val progressVal = (index.toFloat() / total.toFloat()) * 0.9f
                onProgress(progressVal, "Processing Page ${index + 1} of $total...")

                // 1. Decode bitmap
                val sourceBitmap = loadBitmap(context, pageItem)
                    ?: return@withContext Result.failure(Exception("Failed to decode image on page ${index + 1}"))

                // 2. Apply rotation and filter
                val processedBitmap = applyFilterAndRotation(sourceBitmap, pageItem, config.quality, config.isGrayscale)

                // 3. Determine page dimensions (in PostScript points: 72 points per inch)
                val (pageWidth, pageHeight) = calculatePageDimensions(processedBitmap, config)

                // 4. Create PDF Page
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Draw background white
                canvas.drawColor(Color.WHITE)

                // Compute destination bounds considering margins & cropMode
                val margin = config.margin.marginPt.toFloat()
                val availableWidth = (pageWidth - margin * 2).coerceAtLeast(10f)
                val availableHeight = (pageHeight - margin * 2).coerceAtLeast(10f)

                val destRect = calculateDestinationRect(
                    bitmapWidth = processedBitmap.width.toFloat(),
                    bitmapHeight = processedBitmap.height.toFloat(),
                    availWidth = availableWidth,
                    availHeight = availableHeight,
                    marginX = margin,
                    marginY = margin,
                    cropMode = pageItem.cropMode
                )

                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(processedBitmap, null, destRect, paint)

                // Draw Page Numbers if enabled
                drawPageNumber(
                    canvas = canvas,
                    pageIndex = index + 1,
                    totalPages = total,
                    pageWidth = pageWidth,
                    pageHeight = pageHeight,
                    format = config.pageNumberFormat
                )

                pdfDocument.finishPage(page)

                if (index == 0) {
                    firstPageThumbnailBitmap = createThumbnail(processedBitmap)
                }

                if (processedBitmap != sourceBitmap) {
                    processedBitmap.recycle()
                }
                sourceBitmap.recycle()
            }

            onProgress(0.95f, "Writing PDF document to disk...")

            // Write PDF
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Save Thumbnail
            firstPageThumbnailBitmap?.let { thumb ->
                FileOutputStream(thumbFile).use { out ->
                    thumb.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                thumb.recycle()
            }

            val fileSizeBytes = outputFile.length()
            val entity = PdfDocumentEntity(
                title = pdfTitle,
                filePath = outputFile.absolutePath,
                fileSizeBytes = fileSizeBytes,
                pageCount = total,
                createdAt = System.currentTimeMillis(),
                previewThumbnailPath = if (thumbFile.exists()) thumbFile.absolutePath else null,
                pageSizeLabel = config.pageSize.displayName,
                orientationLabel = config.orientation.displayName
            )

            onProgress(1.0f, "Completed successfully!")
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadBitmap(context: Context, pageItem: ImagePageItem): Bitmap? {
        return try {
            if (pageItem.sampleResId != null) {
                val drawable = ContextCompat.getDrawable(context, pageItem.sampleResId)
                when (drawable) {
                    is BitmapDrawable -> drawable.bitmap
                    is VectorDrawable -> {
                        val bitmap = Bitmap.createBitmap(
                            drawable.intrinsicWidth.coerceAtLeast(800),
                            drawable.intrinsicHeight.coerceAtLeast(1000),
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bitmap
                    }
                    else -> {
                        val bitmap = Bitmap.createBitmap(800, 1000, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        drawable?.setBounds(0, 0, 800, 1000)
                        drawable?.draw(canvas)
                        bitmap
                    }
                }
            } else if (pageItem.uri != null) {
                // Decode with sample size to prevent OOM
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(pageItem.uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                val maxDimension = 2400
                var sampleSize = 1
                while ((options.outWidth / sampleSize) > maxDimension || (options.outHeight / sampleSize) > maxDimension) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                context.contentResolver.openInputStream(pageItem.uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOptions)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun applyFilterAndRotation(
        src: Bitmap,
        pageItem: ImagePageItem,
        quality: CompressionQuality,
        forceGrayscale: Boolean
    ): Bitmap {
        // 1. Scale down if quality factor dictates
        val scaledWidth = (src.width * quality.scaleFactor).toInt().coerceAtLeast(100)
        val scaledHeight = (src.height * quality.scaleFactor).toInt().coerceAtLeast(100)

        val workingBitmap = if (scaledWidth != src.width || scaledHeight != src.height) {
            Bitmap.createScaledBitmap(src, scaledWidth, scaledHeight, true)
        } else {
            src
        }

        // 2. Prepare Matrix for rotation
        val matrix = Matrix()
        if (pageItem.rotationDegrees != 0) {
            matrix.postRotate(pageItem.rotationDegrees.toFloat())
        }

        val rotatedBitmap = if (pageItem.rotationDegrees != 0) {
            Bitmap.createBitmap(workingBitmap, 0, 0, workingBitmap.width, workingBitmap.height, matrix, true)
        } else {
            workingBitmap
        }

        // 3. Color Filter
        val activeFilter = if (forceGrayscale) ImageFilterType.GRAYSCALE else pageItem.filter
        if (activeFilter == ImageFilterType.ORIGINAL) {
            return rotatedBitmap
        }

        val resultBitmap = Bitmap.createBitmap(rotatedBitmap.width, rotatedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix()
        when (activeFilter) {
            ImageFilterType.BW_DOCUMENT -> {
                // High contrast document scanning filter
                colorMatrix.setSaturation(0f)
                val contrast = 1.8f
                val brightness = -40f
                val matrixArray = floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
                colorMatrix.postConcat(ColorMatrix(matrixArray))
            }
            ImageFilterType.GRAYSCALE -> {
                colorMatrix.setSaturation(0f)
            }
            ImageFilterType.VIVID -> {
                colorMatrix.setSaturation(1.6f)
            }
            ImageFilterType.HIGH_CONTRAST -> {
                val contrast = 1.5f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                val matrixArray = floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
                colorMatrix.set(matrixArray)
            }
            ImageFilterType.WARM -> {
                val matrixArray = floatArrayOf(
                    1.2f, 0f, 0f, 0f, 15f,
                    0f, 1.05f, 0f, 0f, 5f,
                    0f, 0f, 0.85f, 0f, -15f,
                    0f, 0f, 0f, 1f, 0f
                )
                colorMatrix.set(matrixArray)
            }
            ImageFilterType.COOL -> {
                val matrixArray = floatArrayOf(
                    0.85f, 0f, 0f, 0f, -15f,
                    0f, 1.05f, 0f, 0f, 5f,
                    0f, 0f, 1.25f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                )
                colorMatrix.set(matrixArray)
            }
            ImageFilterType.ORIGINAL -> {}
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(rotatedBitmap, 0f, 0f, paint)

        if (rotatedBitmap != src && rotatedBitmap != workingBitmap) {
            rotatedBitmap.recycle()
        }
        if (workingBitmap != src) {
            workingBitmap.recycle()
        }

        return resultBitmap
    }

    private fun calculatePageDimensions(bitmap: Bitmap, config: PdfConfig): Pair<Int, Int> {
        if (config.pageSize == PageSizeOption.MATCH_IMAGE) {
            // Scale bitmap to reasonable point size
            val maxPt = 1000f
            val maxDim = bitmap.width.coerceAtLeast(bitmap.height).toFloat()
            val scale = (maxPt / maxDim).coerceAtMost(1f)
            val w = (bitmap.width * scale).toInt().coerceAtLeast(200)
            val h = (bitmap.height * scale).toInt().coerceAtLeast(200)
            return Pair(w, h)
        }

        val baseWidth = config.pageSize.widthPt
        val baseHeight = config.pageSize.heightPt

        val isLandscape = when (config.orientation) {
            PageOrientationOption.PORTRAIT -> false
            PageOrientationOption.LANDSCAPE -> true
            PageOrientationOption.AUTO -> bitmap.width > bitmap.height
        }

        return if (isLandscape) {
            Pair(baseHeight.coerceAtLeast(baseWidth), baseWidth.coerceAtMost(baseHeight))
        } else {
            Pair(baseWidth.coerceAtMost(baseHeight), baseHeight.coerceAtLeast(baseWidth))
        }
    }

    private fun calculateDestinationRect(
        bitmapWidth: Float,
        bitmapHeight: Float,
        availWidth: Float,
        availHeight: Float,
        marginX: Float,
        marginY: Float,
        cropMode: CropMode
    ): RectF {
        val aspectBitmap = bitmapWidth / bitmapHeight
        val aspectAvail = availWidth / availHeight

        return when (cropMode) {
            CropMode.FIT_PAGE -> {
                val (drawW, drawH) = if (aspectBitmap > aspectAvail) {
                    val w = availWidth
                    val h = availWidth / aspectBitmap
                    Pair(w, h)
                } else {
                    val h = availHeight
                    val w = availHeight * aspectBitmap
                    Pair(w, h)
                }
                val left = marginX + (availWidth - drawW) / 2f
                val top = marginY + (availHeight - drawH) / 2f
                RectF(left, top, left + drawW, top + drawH)
            }
            CropMode.FILL_PAGE -> {
                RectF(marginX, marginY, marginX + availWidth, marginY + availHeight)
            }
        }
    }

    private fun drawPageNumber(
        canvas: Canvas,
        pageIndex: Int,
        totalPages: Int,
        pageWidth: Int,
        pageHeight: Int,
        format: PageNumberFormat
    ) {
        if (format == PageNumberFormat.NONE) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val text = when (format) {
            PageNumberFormat.BOTTOM_CENTER -> "Page $pageIndex of $totalPages"
            PageNumberFormat.BOTTOM_RIGHT -> "$pageIndex / $totalPages"
            PageNumberFormat.TOP_RIGHT -> "$pageIndex"
            PageNumberFormat.NONE -> ""
        }

        when (format) {
            PageNumberFormat.BOTTOM_CENTER -> {
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(text, pageWidth / 2f, pageHeight - 16f, paint)
            }
            PageNumberFormat.BOTTOM_RIGHT -> {
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(text, pageWidth - 24f, pageHeight - 16f, paint)
            }
            PageNumberFormat.TOP_RIGHT -> {
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(text, pageWidth - 24f, 24f, paint)
            }
            PageNumberFormat.NONE -> {}
        }
    }

    private fun createThumbnail(bitmap: Bitmap): Bitmap {
        val targetWidth = 320
        val targetHeight = (targetWidth.toFloat() * (bitmap.height.toFloat() / bitmap.width.toFloat())).toInt().coerceAtLeast(200)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun sharePdf(context: Context, filePath: String, title: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF via..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openPdfExternal(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printPdf(context: Context, filePath: String, docName: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
            val printAdapter = PdfPrintDocumentAdapter(file)
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build()

            printManager.print(docName, printAdapter, printAttributes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.getDefault(), "%.2f MB", mb)
    }
}
