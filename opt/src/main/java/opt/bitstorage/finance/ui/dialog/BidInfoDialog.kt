package opt.bitstorage.finance.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import opt.bitstorage.finance.common.IBid
import opt.bitstorage.finance.R

class BidInfoDialog(val bidConfirm: IBid, val bid: Boolean, val amount: String) : DialogFragment() {

    companion object{

        private var mInstance: BidInfoDialog? = null

        fun getInstance(bidConfirm: IBid, bid: Boolean, amount: String): BidInfoDialog {
            if (mInstance == null) {
                mInstance = BidInfoDialog(bidConfirm, bid, amount)
            }
            return mInstance!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
     val view = inflater.inflate(R.layout.opt_dialog_bid_confirm, container, false)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
        val txt = view.findViewById<TextView>(R.id.txt)
        if (!bid){
            txt.text = "If the price decreases by at least 0.01USD, you will receive + ${amount.toDouble() * 0.6} btc to your deposit."
        }else{
            txt.text = "If the price increases by at least 0.01USD, you will receive + ${amount.toDouble() * 0.6} btc to your deposit."
        }
        btnConfirm.setOnClickListener {
            bidConfirm.confirm()
            dismiss()
        }
        return view
    }
}