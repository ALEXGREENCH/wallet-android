package com.mycelium.wallet.activity

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mycelium.wallet.R
import com.mycelium.wallet.activity.util.AddressLabel
import com.mycelium.wallet.activity.util.EthFeeFormatter
import com.mycelium.wapi.wallet.EthTransactionSummary
import com.mycelium.wapi.wallet.GenericTransactionSummary
import com.mycelium.wapi.wallet.eth.EthAccount
import kotlinx.android.synthetic.main.transaction_details_eth.*
import java.math.BigInteger
import kotlin.math.round

class EthDetailsFragment : GenericDetailsFragment() {
    private val tx: EthTransactionSummary by lazy {
        arguments!!.getSerializable("tx") as EthTransactionSummary
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.transaction_details_eth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUi()
    }

    private fun updateUi() {
        alignTables(specific_table)

        val fromAddress = AddressLabel(requireContext())
        fromAddress.address = tx.sender
        llFrom.addView(fromAddress)

        val toAddress = AddressLabel(requireContext())
        toAddress.address = tx.receiver
        llTo.addView(toAddress)

        llValue.addView(getValue(tx.value, null))
        llFee.addView(getValue(tx.fee!!, null))

        tvGasLimit.text = tx.gasLimit.toString()
        val percent = if (isWholeNumber(tx.gasUsed.toDouble() / tx.gasLimit.toDouble() * 100))
            "%.0f".format(tx.gasUsed.toDouble() / tx.gasLimit.toDouble() * 100) else (round(tx.gasUsed.toDouble() / tx.gasLimit.toDouble() * 10000) / 100).toString()
        tvGasUsed.text = "${tx.gasUsed} ($percent%)"
        val txFeeTotal = tx.fee!!.valueAsLong
        val txFeePerUnit = BigInteger.valueOf(txFeeTotal) / tx.gasUsed
        tvGasPrice.text = EthFeeFormatter().getFeePerUnit(txFeePerUnit.toLong())
        tvNonce.text = if (tx.nonce == null) {
            UpdateNonce().execute("0x" + tx.idHex)
            "?"
        } else {
            tx.nonce.toString()
        }
    }

    inner class UpdateNonce : AsyncTask<String, Void?, BigInteger?>() {
        override fun doInBackground(vararg txid: String): BigInteger? {
            val selectedAccount = mbwManager!!.selectedAccount
            return if (selectedAccount is EthAccount) {
                selectedAccount.fetchNonce(txid[0])
            } else {
                null
            }
        }

        override fun onPostExecute(nonce: BigInteger?) {
            if (nonce != null) {
                tvNonce.text = nonce.toString()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(tx: GenericTransactionSummary): EthDetailsFragment {
            val f = EthDetailsFragment()
            val args = Bundle()

            args.putSerializable("tx", tx)
            f.arguments = args
            return f
        }
    }

    private fun isWholeNumber(d: Double) = d % 1.0 == 0.0
}