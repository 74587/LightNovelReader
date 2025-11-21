package io.nightfish.lightnovelreader.api.userdata

interface UserDataRepositoryApi {
    fun stringUserData(path: String): StringUserData
    fun floatUserData(path: String): FloatUserData
    fun intUserData(path: String): IntUserData
    fun booleanUserData(path: String): BooleanUserData
    fun intListUserData(path: String): IntListUserData
    fun stringListUserData(path: String): StringListUserData
    fun colorUserData(path: String): ColorUserData
    fun uriUserData(path: String): UriUserData
    fun remove(path: String)
}