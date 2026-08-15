package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.PdfEngine
import com.example.ui.dialogs.PdfPreviewDialog
import com.example.ui.screens.ConvertScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PageEditorScreen
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.ApplePurple
import com.example.ui.theme.AppleRed
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    val pages by viewModel.selectedPages.collectAsStateWithLifecycle()
    val currentEditingIndex by viewModel.currentEditingIndex.collectAsStateWithLifecycle()
    val pdfConfig by viewModel.pdfConfig.collectAsStateWithLifecycle()
    val isConverting by viewModel.isConverting.collectAsStateWithLifecycle()
    val conversionProgress by viewModel.conversionProgress.collectAsStateWithLifecycle()
    val conversionStatusText by viewModel.conversionStatusText.collectAsStateWithLifecycle()
    val historyPdfs by viewModel.historyPdfs.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalPdfCount.collectAsStateWithLifecycle()
    val totalBytes by viewModel.totalPdfBytes.collectAsStateWithLifecycle()
    val selectedPdfForPreview by viewModel.selectedPdfForPreview.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToastMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                // iOS Styled Top Navigation Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Pic to PDF"
                                1 -> "Page Editor"
                                else -> "PDF Library"
                            },
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = when (selectedTab) {
                                0 -> "Create & configure PDF"
                                1 -> "Filter, rotate & arrange"
                                else -> "$totalCount document${if (totalCount != 1) "s" else ""} saved"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Top Action Indicator
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                when (selectedTab) {
                                    0 -> AppleRed.copy(alpha = 0.15f)
                                    1 -> ApplePurple.copy(alpha = 0.15f)
                                    else -> AppleBlue.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> if (pages.isEmpty()) "0 Pages" else "${pages.size} Pages"
                                1 -> "Edit Mode"
                                else -> "All Files"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (selectedTab) {
                                    0 -> AppleRed
                                    1 -> ApplePurple
                                    else -> AppleBlue
                                }
                            )
                        )
                    }
                }
            },
            bottomBar = {
                // Apple Floating Capsule Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(32.dp)
                            )
                            .shadow(12.dp, RoundedCornerShape(32.dp), clip = false)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 1: Convert / Create
                        IosTabItem(
                            title = "Convert",
                            icon = Icons.Default.PictureAsPdf,
                            isSelected = selectedTab == 0,
                            activeColor = AppleRed,
                            badgeCount = if (pages.isNotEmpty()) pages.size else null,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.testTag("tab_convert")
                        )

                        // Tab 2: Page Editor
                        IosTabItem(
                            title = "Pages",
                            icon = Icons.Default.Layers,
                            isSelected = selectedTab == 1,
                            activeColor = ApplePurple,
                            badgeCount = if (pages.isNotEmpty()) pages.size else null,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.testTag("tab_pages")
                        )

                        // Tab 3: History & Library
                        IosTabItem(
                            title = "Library",
                            icon = Icons.Default.FolderSpecial,
                            isSelected = selectedTab == 2,
                            activeColor = AppleBlue,
                            badgeCount = if (historyPdfs.isNotEmpty()) historyPdfs.size else null,
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.testTag("tab_library")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> {
                        ConvertScreen(
                            pages = pages,
                            pdfConfig = pdfConfig,
                            isConverting = isConverting,
                            conversionProgress = conversionProgress,
                            conversionStatusText = conversionStatusText,
                            onAddUris = { uris -> viewModel.addImageUris(uris) },
                            onAddSamples = { viewModel.addSampleImages() },
                            onRemovePage = { idx -> viewModel.removePage(idx) },
                            onRotatePage = { idx -> viewModel.rotatePage(idx) },
                            onClearAll = { viewModel.clearAllPages() },
                            onSelectPageForEdit = { idx ->
                                viewModel.setEditingIndex(idx)
                                selectedTab = 1
                            },
                            onUpdateTitle = { title -> viewModel.updatePdfTitle(title) },
                            onUpdatePageSize = { sz -> viewModel.updatePageSize(sz) },
                            onUpdateOrientation = { orient -> viewModel.updateOrientation(orient) },
                            onUpdateMargin = { mg -> viewModel.updateMargin(mg) },
                            onUpdateQuality = { q -> viewModel.updateQuality(q) },
                            onUpdatePageNumberFormat = { fmt -> viewModel.updatePageNumberFormat(fmt) },
                            onToggleGrayscale = { gr -> viewModel.toggleGrayscale(gr) },
                            onStartConvert = {
                                viewModel.startConversion { generated ->
                                    // Successfully generated PDF, dialog shows automatically
                                }
                            }
                        )
                    }
                    1 -> {
                        PageEditorScreen(
                            pages = pages,
                            editingIndex = currentEditingIndex,
                            onSelectIndex = { idx -> viewModel.setEditingIndex(idx) },
                            onRotateCurrent = { viewModel.rotatePage(currentEditingIndex) },
                            onRotateAll = { viewModel.rotateAllPages() },
                            onSetFilter = { filter -> viewModel.setPageFilter(currentEditingIndex, filter) },
                            onApplyFilterAll = { filter -> viewModel.applyFilterToAllPages(filter) },
                            onSetCropMode = { mode -> viewModel.setPageCropMode(currentEditingIndex, mode) },
                            onMoveLeft = {
                                if (currentEditingIndex > 0) {
                                    viewModel.movePage(currentEditingIndex, currentEditingIndex - 1)
                                }
                            },
                            onMoveRight = {
                                if (currentEditingIndex < pages.size - 1) {
                                    viewModel.movePage(currentEditingIndex, currentEditingIndex + 1)
                                }
                            },
                            onDeleteCurrent = { viewModel.removePage(currentEditingIndex) }
                        )
                    }
                    2 -> {
                        LibraryScreen(
                            pdfs = historyPdfs,
                            totalCount = totalCount,
                            totalBytes = totalBytes ?: 0L,
                            onSelectPdf = { pdf -> viewModel.setPreviewPdf(pdf) },
                            onSharePdf = { pdf -> PdfEngine.sharePdf(context, pdf.filePath, pdf.title) },
                            onPrintPdf = { pdf -> PdfEngine.printPdf(context, pdf.filePath, pdf.title) },
                            onDeletePdf = { pdf -> viewModel.deletePdf(pdf) },
                            onGoToConvert = { selectedTab = 0 }
                        )
                    }
                }
            }
        }

        // PDF Preview Sheet Modal
        selectedPdfForPreview?.let { pdf ->
            PdfPreviewDialog(
                pdf = pdf,
                onDismiss = { viewModel.setPreviewPdf(null) },
                onShare = { PdfEngine.sharePdf(context, pdf.filePath, pdf.title) },
                onPrint = { PdfEngine.printPdf(context, pdf.filePath, pdf.title) },
                onOpenExternal = { PdfEngine.openPdfExternal(context, pdf.filePath) }
            )
        }
    }
}

@Composable
fun IosTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    badgeCount: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount != null && badgeCount > 0) {
                        Badge(
                            containerColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "$badgeCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = activeColor
                )
            }
        }
    }
}
