package opt.bitstorage.finance.common

import android.net.Uri
import java.math.BigDecimal

interface IOptBaseFragment {
    fun executeBitIdAsyncTask(data: Uri)
    fun getPriceBitcoin(): BigDecimal
    fun deposit(amount: String)
    fun withdraw(amount: String, wallet: String)
    fun showToast(msg: String, shortDuration: Boolean)
    fun showToast(resIdMsg: Int, shortDuration: Boolean)
}