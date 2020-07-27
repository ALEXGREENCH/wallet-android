package opt.bitstorage.finance.common

interface IPrepareData{
    fun complete(token: ByteArray, userId: String)
}