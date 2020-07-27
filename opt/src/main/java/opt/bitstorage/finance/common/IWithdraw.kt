package opt.bitstorage.finance.common

interface IWithdraw{
    fun send(value: String, wallet: String)
}