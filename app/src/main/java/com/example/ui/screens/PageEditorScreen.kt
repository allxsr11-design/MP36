package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CropMode
import com.example.model.ImageFilterType
import com.example.model.ImagePageItem
import com.example.ui.components.IosButton
import com.example.ui.components.IosCard
import com.example.ui.components.IosGroupedSection
import com.example.ui.components.IosPillBadge
import com.example.ui.components.IosSegmentedControl
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.ApplePurple
import com.example.ui.theme.AppleRed

@Composable
fun PageEditorScreen(
    pages: List<ImagePageItem>,
    editingIndex: Int,
    onSelectIndex: (Int) -> Unit,
    onRotateCurrent: () -> Unit,
    onRotateAll: () -> Unit,
    onSetFilter: (ImageFilterType) -> Unit,
    onApplyFilterAll: (ImageFilterType) -> Unit,
    onSetCropMode: (CropMode) -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onDeleteCurrent: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pages.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Pages to Edit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add photos from the Convert tab first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val safeIndex = editingIndex.coerceIn(0, pages.size - 1)
    val currentPage = pages[safeIndex]
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
    ) {
        // Thumbnail filmstrip pager
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page ${safeIndex + 1} of ${pages.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IosPillBadge(
                        text = currentPage.filter.displayName,
                        color = ApplePurple,
                        backgroundColor = ApplePurple.copy(alpha = 0.12f)
                    )
                    IosPillBadge(
                        text = "${currentPage.rotationDegrees}°",
                        color = AppleBlue,
                        backgroundColor = AppleBlue.copy(alpha = 0.12f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(pages, key = { _, item -> item.id }) { index, item ->
                    val isSelected = index == safeIndex
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) AppleBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectIndex(index) }
                    ) {
                        val imageModel: Any? = item.uri ?: item.sampleResId
                        AsyncImage(
                            model = imageModel,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(item.rotationDegrees.toFloat()),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(if (isSelected) AppleBlue else Color.Black.copy(alpha = 0.6f))
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large High-Fidelity Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(300.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageModel: Any? = currentPage.uri ?: currentPage.sampleResId
            AsyncImage(
                model = imageModel,
                contentDescription = "Current page preview",
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .rotate(currentPage.rotationDegrees.toFloat()),
                contentScale = if (currentPage.cropMode == CropMode.FIT_PAGE) ContentScale.Fit else ContentScale.Crop
            )

            // Page floating indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Page ${safeIndex + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reorder & Quick Action Tools
        IosGroupedSection(title = "Page Order & Layout") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Move Left
                IconButton(
                    onClick = onMoveLeft,
                    enabled = safeIndex > 0,
                    modifier = Modifier.testTag("move_left_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Move Left",
                        tint = if (safeIndex > 0) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }

                // Rotate 90°
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleBlue.copy(alpha = 0.12f))
                        .clickable { onRotateCurrent() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("rotate_page_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rotate 90°",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppleBlue
                            )
                        )
                    }
                }

                // Delete Page
                IconButton(
                    onClick = onDeleteCurrent,
                    modifier = Modifier.testTag("delete_page_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Page",
                        tint = AppleRed
                    )
                }

                // Move Right
                IconButton(
                    onClick = onMoveRight,
                    enabled = safeIndex < pages.size - 1,
                    modifier = Modifier.testTag("move_right_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Move Right",
                        tint = if (safeIndex < pages.size - 1) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Image Enhancement Filter Selection
        IosGroupedSection(
            title = "Enhance & Document Filters",
            footer = "Choose B&W scan mode for receipts & documents to increase sharpness."
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                val filters = ImageFilterType.values()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(filters) { _, filter ->
                        val isSelected = currentPage.filter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) AppleBlue else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onSetFilter(filter) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Apply to All Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ApplePurple.copy(alpha = 0.1f))
                        .clickable { onApplyFilterAll(currentPage.filter) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ApplePurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply '${currentPage.filter.displayName}' to All Pages",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ApplePurple
                        )
                    )
                }
            }
        }

        // Crop & Aspect Ratio Section
        IosGroupedSection(title = "Fitting Mode") {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                val cropModes = listOf(CropMode.FIT_PAGE, CropMode.FILL_PAGE)
                val cropLabels = listOf("Fit (Keep Ratio)", "Fill (Full Bleed)")
                val selectedIdx = cropModes.indexOf(currentPage.cropMode).coerceAtLeast(0)
                IosSegmentedControl(
                    items = cropLabels,
                    selectedIndex = selectedIdx,
                    onItemSelected = { idx -> onSetCropMode(cropModes[idx]) }
                )
            }
        }

        // Quick rotate all
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IosButton(
                text = "Rotate All ${pages.size} Pages (90°)",
                onClick = onRotateAll,
                isSecondary = true,
                icon = Icons.Default.RotateRight,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
