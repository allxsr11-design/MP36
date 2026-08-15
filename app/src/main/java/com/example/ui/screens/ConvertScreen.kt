package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.CompressionQuality
import com.example.model.ImagePageItem
import com.example.model.PageMarginOption
import com.example.model.PageNumberFormat
import com.example.model.PageOrientationOption
import com.example.model.PageSizeOption
import com.example.model.PdfConfig
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosGroupedSection
import com.example.ui.components.IosListRow
import com.example.ui.components.IosPillBadge
import com.example.ui.components.IosSegmentedControl
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.ApplePurple
import com.example.ui.theme.AppleRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(
    pages: List<ImagePageItem>,
    pdfConfig: PdfConfig,
    isConverting: Boolean,
    conversionProgress: Float,
    conversionStatusText: String,
    onAddUris: (List<Uri>) -> Unit,
    onAddSamples: () -> Unit,
    onRemovePage: (Int) -> Unit,
    onRotatePage: (Int) -> Unit,
    onClearAll: () -> Unit,
    onSelectPageForEdit: (Int) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdatePageSize: (PageSizeOption) -> Unit,
    onUpdateOrientation: (PageOrientationOption) -> Unit,
    onUpdateMargin: (PageMarginOption) -> Unit,
    onUpdateQuality: (CompressionQuality) -> Unit,
    onUpdatePageNumberFormat: (PageNumberFormat) -> Unit,
    onToggleGrayscale: (Boolean) -> Unit,
    onStartConvert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddUris(uris)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            // Apple Header Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AppleRed.copy(alpha = 0.9f),
                                AppleOrange.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Convert Pictures to PDF",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ultra high quality • Multi-page • Instant export",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Gallery Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("pick_gallery_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = AppleRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Select Photos",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = AppleRed
                                )
                            }
                        }

                        // Try Sample Presets Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { onAddSamples() }
                                .testTag("sample_images_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sample Photos",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Selected Images Section
            IosGroupedSection(
                title = if (pages.isEmpty()) "Selected Pictures" else "Selected Pictures (${pages.size})",
                footer = if (pages.isNotEmpty()) "Tap a picture to edit its filter, orientation, or crop." else null
            ) {
                if (pages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Pictures Added Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Select Photos' or 'Sample Photos' to start building your PDF.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${pages.size} Page${if (pages.size > 1) "s" else ""} ready to convert",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleRed
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onClearAll() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(pages, key = { _, item -> item.id }) { index, page ->
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .aspectRatio(0.72f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .clickable { onSelectPageForEdit(index) }
                                ) {
                                    val imageModel: Any? = page.uri ?: page.sampleResId
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = "Page ${index + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .rotate(page.rotationDegrees.toFloat()),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Page Number Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        )
                                    }

                                    // Rotate & Remove Action Pills
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RotateRight,
                                            contentDescription = "Rotate",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { onRotatePage(index) }
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = AppleRed,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { onRemovePage(index) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PDF Settings Grouped Section
            IosGroupedSection(
                title = "Document Configuration",
                footer = "Configure paper size, orientation, margins, and compression quality."
            ) {
                // PDF Title Input
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "Document Title",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = pdfConfig.title,
                        onValueChange = onUpdateTitle,
                        placeholder = { Text("e.g., Receipts_August_2026") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pdf_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (pdfConfig.title.isNotEmpty()) {
                                IconButton(onClick = { onUpdateTitle("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Page Size Segmented Control
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page Size",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IosPillBadge(text = pdfConfig.pageSize.displayName, color = AppleBlue)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val pageSizes = listOf(
                        PageSizeOption.A4,
                        PageSizeOption.LETTER,
                        PageSizeOption.LEGAL,
                        PageSizeOption.MATCH_IMAGE
                    )
                    val sizeLabels = listOf("A4", "US Letter", "Legal", "Fit Pic")
                    val selectedIndex = pageSizes.indexOf(pdfConfig.pageSize).coerceAtLeast(0)
                    IosSegmentedControl(
                        items = sizeLabels,
                        selectedIndex = selectedIndex,
                        onItemSelected = { idx -> onUpdatePageSize(pageSizes[idx]) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Orientation Segmented Control
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Page Orientation",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val orientations = listOf(
                        PageOrientationOption.AUTO,
                        PageOrientationOption.PORTRAIT,
                        PageOrientationOption.LANDSCAPE
                    )
                    val orientLabels = listOf("Auto", "Portrait", "Landscape")
                    val selectedIndex = orientations.indexOf(pdfConfig.orientation).coerceAtLeast(0)
                    IosSegmentedControl(
                        items = orientLabels,
                        selectedIndex = selectedIndex,
                        onItemSelected = { idx -> onUpdateOrientation(orientations[idx]) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Page Margin Segmented Control
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Page Margins",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val margins = listOf(
                        PageMarginOption.NONE,
                        PageMarginOption.COMPACT,
                        PageMarginOption.STANDARD,
                        PageMarginOption.SPACIOUS
                    )
                    val marginLabels = listOf("None (0)", "Compact", "Standard", "Wide")
                    val selectedIndex = margins.indexOf(pdfConfig.margin).coerceAtLeast(0)
                    IosSegmentedControl(
                        items = marginLabels,
                        selectedIndex = selectedIndex,
                        onItemSelected = { idx -> onUpdateMargin(margins[idx]) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Compression Quality
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Image Quality & Compression",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IosPillBadge(
                            text = "${pdfConfig.quality.qualityPercent}%",
                            color = ApplePurple,
                            backgroundColor = ApplePurple.copy(alpha = 0.12f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val qualities = listOf(
                        CompressionQuality.ORIGINAL,
                        CompressionQuality.HIGH,
                        CompressionQuality.MEDIUM,
                        CompressionQuality.LOW
                    )
                    val qualLabels = listOf("Max 100%", "High 85%", "Med 70%", "Low 50%")
                    val selectedIndex = qualities.indexOf(pdfConfig.quality).coerceAtLeast(0)
                    IosSegmentedControl(
                        items = qualLabels,
                        selectedIndex = selectedIndex,
                        onItemSelected = { idx -> onUpdateQuality(qualities[idx]) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Grayscale Switch
                IosListRow(
                    title = "Convert to Grayscale (B&W)",
                    subtitle = "Saves file size and optimizes for document printers",
                    showDivider = true,
                    trailingContent = {
                        Switch(
                            checked = pdfConfig.isGrayscale,
                            onCheckedChange = onToggleGrayscale,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleGreen
                            )
                        )
                    }
                )

                // Page Numbering Format Selector
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Page Numbering",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val formats = listOf(
                        PageNumberFormat.NONE,
                        PageNumberFormat.BOTTOM_CENTER,
                        PageNumberFormat.BOTTOM_RIGHT,
                        PageNumberFormat.TOP_RIGHT
                    )
                    val formatLabels = listOf("Off", "Page 1 of N", "1/N", "Top 1")
                    val selectedIndex = formats.indexOf(pdfConfig.pageNumberFormat).coerceAtLeast(0)
                    IosSegmentedControl(
                        items = formatLabels,
                        selectedIndex = selectedIndex,
                        onItemSelected = { idx -> onUpdatePageNumberFormat(formats[idx]) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Prominent Convert Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                IosButton(
                    text = if (pages.isEmpty()) "Add Pictures to Convert" else "Convert ${pages.size} Picture${if (pages.size > 1) "s" else ""} to PDF",
                    onClick = onStartConvert,
                    enabled = pages.isNotEmpty() && !isConverting,
                    isLoading = isConverting,
                    icon = Icons.Default.PictureAsPdf,
                    containerColor = AppleRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("convert_to_pdf_main_button")
                )
            }
        }

        // Progress Dialog when Converting
        if (isConverting) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AppleRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Converting PDF",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = conversionStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { conversionProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AppleRed,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
