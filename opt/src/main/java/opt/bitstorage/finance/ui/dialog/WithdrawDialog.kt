package opt.bitstorage.finance.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import okhttp3.OkHttpClient
import opt.bitstorage.finance.R
import opt.bitstorage.finance.common.IWithdraw

class WithdrawDialog : DialogFragment() {

    //companion object {
    //    const val KEY_TOKEN = "Token"
    //}

    private val httpClient = OkHttpClient()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //val bundle = arguments

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_Dialog_Alert)
        builder.setTitle("Withdraw")

        val inflater = requireActivity().layoutInflater
        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.opt_dialog_withdraw, null)
        val valueTI = view.findViewById<TextInputLayout>(R.id.ti_amount)
        val walletTI = view.findViewById<TextInputLayout>(R.id.ti_wallet)

        builder.setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->

                    // TODO: временно
                    /*
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    val token = String(bundle!!.getByteArray(KEY_TOKEN))

                    val json = StringBuilder()
                            .append("{")
                            .append("\"value\":\"" + valueTI.editText!!.text + "\",")
                            .append("\"wallet\":\"" + walletTI.editText!!.text + "\"")
                            .append("}").toString()

                    // json request body
                    val mediaType = MediaType.parse("application/json; charset=utf-8");
                    val body: RequestBody = RequestBody.create(mediaType, json)
                    val request = Request.Builder()
                            .url("https://opt.bitstorage.finance/wallet/withdraw")
                            .addHeader("x-access-token", token)
                            .post(body)
                            .build()
                    try {
                        GlobalScope.launch {
                            httpClient.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) {
                                    //throw IOException("Unexpected code $response")
                                }
                            }
                        }
                    } catch (e: IOException) {
                    }

                     */
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
        return builder.create()
    }
}