package opt.bitstorage.finance.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import opt.bitstorage.finance.R
import opt.bitstorage.finance.common.IDeposit

class DepositDialog(val deposit: IDeposit) : DialogFragment() {

    private var currentCurrency: String = "BTC"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_Dialog_Alert)
        builder.setTitle(getString(R.string.opt_title_dialog_deposit))

        val inflater = requireActivity().layoutInflater
        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.opt_dialog_deposit, null)
        val valueTI = view.findViewById<TextInputLayout>(R.id.ti_amount)
        val currencySpinnerText = view.findViewById<TextView>(R.id.currency_spinner_text)
        val currencySpinner = view.findViewById<FrameLayout>(R.id.currency_spinner)
        currencySpinner.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.inflate(R.menu.opt_popup_select_currency)
            popup.setOnMenuItemClickListener { item ->

                when (item.itemId) {
                    R.id.opt_currency_btc -> {
                        currencySpinnerText.text = item.title
                        currentCurrency = item.title.toString()
                        true
                    }
                    R.id.opt_currency_usd -> {
                        currencySpinnerText.text = item.title
                        currentCurrency = item.title.toString()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }


        builder.setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    deposit.send(valueTI.editText!!.text.toString())
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
        return builder.create()
    }
}