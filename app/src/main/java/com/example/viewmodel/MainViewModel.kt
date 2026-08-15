package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppDatabase
import com.example.data.PdfDocumentEntity
import com.example.data.PdfRepository
import com.example.engine.PdfEngine
import com.example.model.CompressionQuality
import com.example.model.CropMode
import com.example.model.ImageFilterType
import com.example.model.ImagePageItem
import com.example.model.PageMarginOption
import com.example.model.PageNumberFormat
import com.example.model.PageOrientationOption
import com.example.model.PageSizeOption
import com.example.model.PdfConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PdfRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PdfRepository(db.pdfDao())
    }

    val historyPdfs: StateFlow<List<PdfDocumentEntity>> = repository.allPdfs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPdfCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPdfBytes: StateFlow<Long?> = repository.totalBytes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Current Active Selected Images
    private val _selectedPages = MutableStateFlow<List<ImagePageItem>>(emptyList())
    val selectedPages: StateFlow<List<ImagePageItem>> = _selectedPages.asStateFlow()

    // Active selected page index for the page editor
    private val _currentEditingIndex = MutableStateFlow<Int>(0)
    val currentEditingIndex: StateFlow<Int> = _currentEditingIndex.asStateFlow()

    // PDF Configuration Options
    private val _pdfConfig = MutableStateFlow(PdfConfig())
    val pdfConfig: StateFlow<PdfConfig> = _pdfConfig.asStateFlow()

    // Conversion progress / state
    private val _conversionProgress = MutableStateFlow<Float>(0f)
    val conversionProgress: StateFlow<Float> = _conversionProgress.asStateFlow()

    private val _conversionStatusText = MutableStateFlow<String>("")
    val conversionStatusText: StateFlow<String> = _conversionStatusText.asStateFlow()

    private val _isConverting = MutableStateFlow<Boolean>(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<PdfDocumentEntity?>(null)
    val lastGeneratedPdf: StateFlow<PdfDocumentEntity?> = _lastGeneratedPdf.asStateFlow()

    private val _selectedPdfForPreview = MutableStateFlow<PdfDocumentEntity?>(null)
    val selectedPdfForPreview: StateFlow<PdfDocumentEntity?> = _selectedPdfForPreview.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun addImageUris(uris: List<Uri>) {
        val current = _selectedPages.value.toMutableList()
        val startingNum = current.size + 1
        uris.forEachIndexed { i, uri ->
            current.add(
                ImagePageItem(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    title = "Page ${startingNum + i}"
                )
            )
        }
        _selectedPages.value = current
    }

    fun addSampleImages() {
        val current = _selectedPages.value.toMutableList()
        current.add(
            ImagePageItem(
                id = UUID.randomUUID().toString(),
                sampleResId = R.drawable.sample_invoice,
                title = "Invoice Scan"
            )
        )
        current.add(
            ImagePageItem(
                id = UUID.randomUUID().toString(),
                sampleResId = R.drawable.sample_notes,
                title = "Meeting Notes"
            )
        )
        current.add(
            ImagePageItem(
                id = UUID.randomUUID().toString(),
                sampleResId = R.drawable.sample_photo,
                title = "Color Graphic"
            )
        )
        _selectedPages.value = current
        _toastMessage.value = "Added 3 sample pages to convert!"
    }

    fun removePage(index: Int) {
        val current = _selectedPages.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedPages.value = current
            if (_currentEditingIndex.value >= current.size) {
                _currentEditingIndex.value = (current.size - 1).coerceAtLeast(0)
            }
        }
    }

    fun clearAllPages() {
        _selectedPages.value = emptyList()
        _currentEditingIndex.value = 0
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        val current = _selectedPages.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selectedPages.value = current
            _currentEditingIndex.value = toIndex
        }
    }

    fun rotatePage(index: Int) {
        val current = _selectedPages.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            val newRotation = (item.rotationDegrees + 90) % 360
            current[index] = item.copy(rotationDegrees = newRotation)
            _selectedPages.value = current
        }
    }

    fun rotateAllPages() {
        val current = _selectedPages.value.map { item ->
            item.copy(rotationDegrees = (item.rotationDegrees + 90) % 360)
        }
        _selectedPages.value = current
    }

    fun setPageFilter(index: Int, filter: ImageFilterType) {
        val current = _selectedPages.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(filter = filter)
            _selectedPages.value = current
        }
    }

    fun applyFilterToAllPages(filter: ImageFilterType) {
        val current = _selectedPages.value.map { it.copy(filter = filter) }
        _selectedPages.value = current
        _toastMessage.value = "Applied ${filter.displayName} to all pages"
    }

    fun setPageCropMode(index: Int, cropMode: CropMode) {
        val current = _selectedPages.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(cropMode = cropMode)
            _selectedPages.value = current
        }
    }

    fun setEditingIndex(index: Int) {
        if (index in _selectedPages.value.indices) {
            _currentEditingIndex.value = index
        }
    }

    // PDF Configuration updates
    fun updatePdfTitle(title: String) {
        _pdfConfig.update { it.copy(title = title) }
    }

    fun updatePageSize(pageSize: PageSizeOption) {
        _pdfConfig.update { it.copy(pageSize = pageSize) }
    }

    fun updateOrientation(orientation: PageOrientationOption) {
        _pdfConfig.update { it.copy(orientation = orientation) }
    }

    fun updateMargin(margin: PageMarginOption) {
        _pdfConfig.update { it.copy(margin = margin) }
    }

    fun updateQuality(quality: CompressionQuality) {
        _pdfConfig.update { it.copy(quality = quality) }
    }

    fun updatePageNumberFormat(format: PageNumberFormat) {
        _pdfConfig.update { it.copy(pageNumberFormat = format) }
    }

    fun toggleGrayscale(enabled: Boolean) {
        _pdfConfig.update { it.copy(isGrayscale = enabled) }
    }

    fun startConversion(onSuccess: (PdfDocumentEntity) -> Unit) {
        val pages = _selectedPages.value
        if (pages.isEmpty()) {
            _toastMessage.value = "Please add at least one picture first!"
            return
        }

        viewModelScope.launch {
            _isConverting.value = true
            _conversionProgress.value = 0f
            _conversionStatusText.value = "Starting conversion..."

            val result = PdfEngine.generatePdf(
                context = getApplication(),
                pages = pages,
                config = _pdfConfig.value,
                onProgress = { progress, step ->
                    _conversionProgress.value = progress
                    _conversionStatusText.value = step
                }
            )

            _isConverting.value = false

            result.onSuccess { entity ->
                val id = repository.insertPdf(entity)
                val savedEntity = entity.copy(id = id)
                _lastGeneratedPdf.value = savedEntity
                _selectedPdfForPreview.value = savedEntity
                _toastMessage.value = "PDF created successfully! (${PdfEngine.formatFileSize(entity.fileSizeBytes)})"
                onSuccess(savedEntity)
            }.onFailure { error ->
                _toastMessage.value = "Error generating PDF: ${error.message}"
            }
        }
    }

    fun setPreviewPdf(pdf: PdfDocumentEntity?) {
        _selectedPdfForPreview.value = pdf
    }

    fun deletePdf(pdf: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.deletePdf(pdf)
            if (_selectedPdfForPreview.value?.id == pdf.id) {
                _selectedPdfForPreview.value = null
            }
            _toastMessage.value = "Deleted ${pdf.title}"
        }
    }

    fun renamePdf(pdf: PdfDocumentEntity, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updatePdf(pdf.copy(title = newTitle.trim()))
            _toastMessage.value = "Renamed to ${newTitle.trim()}"
        }
    }
}
