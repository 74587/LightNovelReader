package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

@Dao
interface BookVolumesDao {
    @TypeConverters(ListConverter::class)
    @Query("replace into volume (book_id, volume_id, volume_title, chapter_id_list, volume_index)" +
            " values (:bookId, :volumeId, :volumeTitle, :chapterIds, :index)")
    fun update(bookId: String, volumeId: String, volumeTitle: String, chapterIds: String, index: Int)

    @Query("replace into chapter_information (id, title) values (:id, :title)")
    fun updateChapterInformation(id: String, title: String)

    @Query("select * from chapter_information where id = :id")
    suspend fun getChapterInformation(id: String): ChapterInformation?

    @Transaction
    fun update(bookId: String, volumes: BookVolumes) {
        volumes.volumes.forEachIndexed { index, volume ->
            update(bookId, volume.volumeId, volume.volumeTitle,
                volume.chapters.joinToString(",") { it.id }, index)
            volume.chapters.forEach {
                updateChapterInformation(it.id, it.title)
            }
        }
    }

    @Query("select * from volume where volume_id = :volumeId")
    suspend fun getVolumeEntity(volumeId: String): VolumeEntity?

    @Query("select * from volume where book_id = :bookId")
    suspend fun getVolumeEntitiesByBookId(bookId: String): List<VolumeEntity>

    @Transaction
    suspend fun getBookVolumes(bookId: String): BookVolumes? {
        return BookVolumes(
            bookId,
            getVolumeEntitiesByBookId(bookId)
            .sortedBy { it.index }
            .map { volumeEntity ->
                Volume(
                    volumeEntity.volumeId,
                    volumeEntity.volumeTitle,
                    volumeEntity.chapterIds.map {
                        getChapterInformation(it) ?: ChapterInformation("", "")
                    })
        })
    }

    @Query("delete from volume")
    fun clearVolumes()

    @Query("delete from chapter_information")
    fun clearChapterInformation()

    @Transaction
    fun clear() {
        clearVolumes()
        clearChapterInformation()
    }
}