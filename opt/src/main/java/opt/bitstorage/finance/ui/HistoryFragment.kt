package opt.bitstorage.finance.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.opt_fragment_history.*
import opt.bitstorage.finance.R
import opt.bitstorage.finance.net.ApiClient
import opt.bitstorage.finance.net.model.history.History
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment(private val bytes: ByteArray) : DialogFragment() {

    private var data: ArrayList<String>? = null

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.opt_fragment_history, container, false)
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        data = ArrayList(mutableListOf("Amount", "Type", "Status", "Pair"))
        getHistory()
    }

    private fun getHistory(){
        val token = String(bytes)
        ApiClient.getInstance(token).getService().getHistory().enqueue(object : Callback<ArrayList<History>>{
            override fun onFailure(call: Call<ArrayList<History>>, t: Throwable) {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                Log.i("TAG3", t.localizedMessage)
            }

            override fun onResponse(call: Call<ArrayList<History>>, response: Response<ArrayList<History>>) {
                if (response.code() != 200) return
                val history = response.body()

                for (item in history!!){
                    Log.i("TAG3", item.bet_status)
                    data!!.add(item.bet_amount.toBigDecimal().toPlainString())
                    val betType = when(item.bet_type){
                        "sell" -> {
                            "down"
                        }
                        "buy" -> {
                            "up"
                        }
                        else -> {
                            item.bet_type
                        }
                    }
                    data!!.add(betType)
                    data!!.add(item.bet_status)
                    var betPair = item.bet_pair
                    if (betPair == "BTC_USD"){
                        betPair = "BTC / USD"
                    }
                    data!!.add(betPair)
                }

                //gridview.isExpanded = true
                val adapter = ArrayAdapter(requireContext(), R.layout.opt_item_table_simple, data!!)
                gridview.adapter = adapter
            }
        })
    }
}