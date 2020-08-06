package opt.bitstorage.finance.common

import opt.bitstorage.finance.ui.GraphFragment

interface IDeposit {
    fun send(amount: String, currency: GraphFragment.Companion.CURRENCY)
}