package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter.stringListToString
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter.dateToString
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import io.nightfish.lightnovelreader.api.book.UserReadingData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserReadingDataDao {
    @Query("replace into user_reading_data (id, last_read_time, total_read_time, reading_progress, last_read_chapter_id, last_read_chapter_title, last_read_chapter_progress, read_completed_chapter_ids) " +
            "values (:id, :lastReadTime, :totalReadTime, :readingProgress, :lastReadChapterId, :lastReadChapterTitle, :lastReadChapterProgress, :readCompletedChapterIds)")
    fun update(
        id: String,
        lastReadTime: String,
        totalReadTime: Int,
        readingProgress: Float,
        lastReadChapterId: String,
        lastReadChapterTitle: String,
        lastReadChapterProgress: Float,
        readCompletedChapterIds: String
    )

    @Transaction
    fun update(userReading: UserReadingData) {
        dateToString(userReading.lastReadTime)?.let {
            update(
                userReading.id,
                it,
                userReading.totalReadTime,
                userReading.readingProgress,
                userReading.lastReadChapterId,
                userReading.lastReadChapterTitle,
                userReading.lastReadChapterProgress,
                stringListToString(userReading.readCompletedChapterIds)
            )
        }
    }

    @Query("select * from user_reading_data where id = :id")
    fun getEntity(id: String): UserReadingDataEntity?

    @Query("select * from user_reading_data where id = :id")
    fun getEntityFlow(id: String): Flow<UserReadingDataEntity?>

    @Query("select * from user_reading_data")
    fun getAll(): List<UserReadingDataEntity>

    @Query("select * from user_reading_data where id = :id")
    fun getEntityWithoutFlow(id: String): UserReadingDataEntity?

    @Query("delete from user_reading_data")
    fun clear()
}