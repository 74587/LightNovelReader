package indi.dmzz_yyhyy.lightnovelreader.data.local.cbor

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import kotlinx.serialization.Serializable

@Serializable
data class AppLocalData(
    val version: Int = 0,
    val localDataList: List<LocalData>,
    val globalUserDataEntity: List<UserDataEntity>
)