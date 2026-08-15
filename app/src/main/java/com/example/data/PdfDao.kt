package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_documents ORDER BY createdAt DESC")
    fun getAllPdfs(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id")
    fun getPdfById(id: Long): Flow<PdfDocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfDocumentEntity): Long

    @Update
    suspend fun updatePdf(pdf: PdfDocumentEntity)

    @Delete
    suspend fun deletePdf(pdf: PdfDocumentEntity)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deletePdfById(id: Long)

    @Query("SELECT COUNT(*) FROM pdf_documents")
    fun getCount(): Flow<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM pdf_documents")
    fun getTotalBytes(): Flow<Long?>
}
