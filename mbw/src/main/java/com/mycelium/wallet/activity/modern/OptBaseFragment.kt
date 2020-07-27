package com.mycelium.wallet.activity.modern

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import com.mrd.bitlib.crypto.InMemoryPrivateKey
import com.mrd.bitlib.model.Address
import com.mrd.bitlib.model.AddressType
import com.mycelium.wallet.MbwManager
import com.mycelium.wallet.R
import com.mycelium.wallet.activity.send.GetSpendingRecordActivity
import com.mycelium.wallet.activity.send.SendInitializationActivity
import com.mycelium.wallet.bitid.BitIDSignRequest
import com.mycelium.wallet.bitid.BitIdAsyncTask
import com.mycelium.wallet.bitid.BitIdAuthenticator
import com.mycelium.wallet.bitid.BitIdResponse
import com.mycelium.wapi.content.GenericAssetUri
import com.mycelium.wapi.wallet.coins.Value
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import opt.bitstorage.finance.ui.GraphFragment
import opt.bitstorage.finance.common.IOptBaseContract
import opt.bitstorage.finance.common.IPrepareData
import opt.bitstorage.finance.net.ApiClient
import opt.bitstorage.finance.net.model.Deposit
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal

open class OptBaseFragment : Fragment(R.layout.fragment_opt_base), IOptBaseContract {

    private var token: ByteArray? = null
    private var userId: String? = null

    private var toaster: Toaster? = null
    private var request: BitIDSignRequest? = null
    private var manager: MbwManager? = null
    private var key: InMemoryPrivateKey? = null
    private var address: Address? = null

    private val eventBus: Bus
        get() = MbwManager.getEventBus()

    private var prepareData: IPrepareData? = null
    var graphFragment: GraphFragment? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toaster = Toaster(this)

        graphFragment = GraphFragment(this)
        prepareData = graphFragment
        graphFragment?.let { fragment ->
            childFragmentManager.beginTransaction().add(R.id.opt_root_view, fragment).commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        for (item in menu){
            item.isVisible = false
        }
        inflater.inflate(R.menu.opt_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val miBackup = menu.findItem(R.id.miBackup)
        miBackup?.isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    override fun executeBitIdAsyncTask(data: Uri) {
        request = BitIDSignRequest.parse(data).get()
        request?.let { r ->
            manager = MbwManager.getInstance(requireContext())
            manager?.let { m ->
                key = m.getBitIdKeyForWebsite(r.idUri)
                key?.let { k ->
                    address = k.publicKey.toAddress(m.network, AddressType.P2PKH)
                    address?.let { a ->
                        BitIdAsyncTask(
                                BitIdAuthenticator(r, true, k, a), eventBus
                        ).execute()
                    }
                }
            }
        }
    }

    override fun showToast(msg: String, shortDuration: Boolean) {
        toaster?.toast(msg, shortDuration)
    }

    override fun showToast(resIdMsg: Int, shortDuration: Boolean) {
        toaster?.toast(resIdMsg, shortDuration)
    }

    override fun getPriceBitcoin(): BigDecimal {
        val mbwManager = MbwManager.getInstance(context)
        val account = mbwManager.selectedAccount

        val ac = account.coinType
        var value: Value? = null
        if (ac != null) {
            value = mbwManager.exchangeRateManager.get(
                    ac.oneCoin(),
                    mbwManager.getFiatCurrency(ac))
        }
        return value?.valueAsBigDecimal ?: BigDecimal(0)
    }

    override fun deposit(amount: String) {
        ApiClient.getInstance(String(token!!))!!.getService().postDeposit(Deposit(amount)).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() != 200) return
                val body = response.body()
                val strJson = body!!.string()
                Log.i("TAG", "strJson -> $strJson")
                //
                val obj = JSONObject(strJson)
                val rs = obj.getString("response")



                val oil = manager!!.contentResolver.resolveUri(rs!!)
                GlobalScope.launch(Dispatchers.Main) {
                    handlePaymentRequest(oil)
                }
            }
        })
    }

    override fun withdraw(amount: String, wallet: String) {

    }

    private fun handlePaymentRequest(bytes: GenericAssetUri?) {
        var spendingAccounts = manager?.getWalletManager(false)!!.getSpendingAccountsWithBalance()
        if (spendingAccounts.isEmpty()) {
            spendingAccounts = manager?.getWalletManager(false)!!.getSpendingAccounts()
        }
        if (spendingAccounts.size == 1) {
            SendInitializationActivity.callMeWithResult(requireActivity(), spendingAccounts[0].id, bytes, false, REQUEST_FROM_URI)
        } else {
            GetSpendingRecordActivity.callMeWithResult(requireActivity(), bytes, REQUEST_FROM_URI)
        }
    }

    override fun onResume() {
        super.onResume()
        eventBus.register(this)
    }

    override fun onPause() {
        eventBus.unregister(this)
        super.onPause()
    }

    @Subscribe
    open fun onTaskCompleted(response: BitIdResponse) {
        try {
            val obj = JSONObject(response.message)
            token = obj.getString("reason").toByteArray()
            userId = obj.getString("userid")
            if (token != null && userId != null) {
                prepareData?.complete(token!!, userId!!)
            }
        } catch (e: JSONException) {
        }
    }

    companion object {
        private const val REQUEST_FROM_URI = 2
    }
}