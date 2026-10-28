package com.kshetrajna.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoRecordDao {
    @Query("SELECT * FROM photo_records ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoRecord>>

    @Query("SELECT * FROM photo_records WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: Long): PhotoRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(record: PhotoRecord): Long

    @Update
    suspend fun updatePhoto(record: PhotoRecord)

    @Query("UPDATE photo_records SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)

    @Delete
    suspend fun deletePhoto(record: PhotoRecord)

    @Query("DELETE FROM photo_records")
    suspend fun deleteAllPhotos()

    @Query("SELECT COUNT(*) FROM photo_records")
    suspend fun getPhotoCount(): Int
}
