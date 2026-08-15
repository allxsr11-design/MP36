package com.example.data

import kotlinx.coroutines.flow.Flow
import java.io.File

class PdfRepository(private val pdfDao: PdfDao) {
    val allPdfs: Flow<List<PdfDocumentEntity>> = pdfDao.getAllPdfs()
    val totalCount: Flow<Int> = pdfDao.getCount()
    val totalBytes: Flow<Long?> = pdfDao.getTotalBytes()

    fun getPdfById(id: Long): Flow<PdfDocumentEntity?> = pdfDao.getPdfById(id)

    suspend fun insertPdf(pdf: PdfDocumentEntity): Long = pdfDao.insertPdf(pdf)

    suspend fun updatePdf(pdf: PdfDocumentEntity) = pdfDao.updatePdf(pdf)

    suspend fun deletePdf(pdf: PdfDocumentEntity) {
        try {
            val file = File(pdf.filePath)
            if (file.exists()) file.delete()
            pdf.previewThumbnailPath?.let {
                val thumbFile = File(it)
                if (thumbFile.exists()) thumbFile.delete()
            }
        } catch (_: Exception) {}
        pdfDao.deletePdf(pdf)
    }

    suspend fun deletePdfById(id: Long) = pdfDao.deletePdfById(id)
}
