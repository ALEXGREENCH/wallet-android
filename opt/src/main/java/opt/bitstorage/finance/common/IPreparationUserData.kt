package opt.bitstorage.finance.common

interface IPreparationUserData{
    fun complete(token: ByteArray, userId: String)
}