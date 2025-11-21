package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import io.nightfish.lightnovelreader.api.userdata.BooleanUserData
import io.nightfish.lightnovelreader.api.userdata.ColorUserData
import io.nightfish.lightnovelreader.api.userdata.FloatUserData
import io.nightfish.lightnovelreader.api.userdata.IntListUserData
import io.nightfish.lightnovelreader.api.userdata.IntUserData
import io.nightfish.lightnovelreader.api.userdata.StringListUserData
import io.nightfish.lightnovelreader.api.userdata.StringUserData
import io.nightfish.lightnovelreader.api.userdata.UriUserData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val userDataDao: UserDataDao
): io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi {
    override fun stringUserData(path: String) = StringUserData(path, userDataDao)
    override fun floatUserData(path: String) = FloatUserData(path, userDataDao)
    override fun intUserData(path: String) = IntUserData(path, userDataDao)
    override fun booleanUserData(path: String) = BooleanUserData(path, userDataDao)
    override fun intListUserData(path: String) = IntListUserData(path, userDataDao)
    override fun stringListUserData(path: String) = StringListUserData(path, userDataDao)
    override fun colorUserData(path: String) = ColorUserData(path, userDataDao)
    override fun uriUserData(path: String) = UriUserData(path, userDataDao)

    override fun remove(path: String) = userDataDao.remove(path)

    fun importUserData(data: AppUserDataContent): Boolean {
        val userDataList = data.userData ?: return false
        userDataList.forEach {
            userDataDao.update(
                path = it.path,
                group = it.group,
                type = it.type,
                value = it.value
            )
        }
        return true
    }
}