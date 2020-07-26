package opt.bitstorage.finance.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.opt_fragment_graph.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import opt.bitstorage.finance.R
import opt.bitstorage.finance.common.*
import opt.bitstorage.finance.net.ApiClient
import opt.bitstorage.finance.net.model.EmptyObj
import opt.bitstorage.finance.net.model.OptBets
import opt.bitstorage.finance.ui.dialog.BidInfoDialog
import opt.bitstorage.finance.ui.dialog.DepositDialog
import opt.bitstorage.finance.ui.dialog.WithdrawDialog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

open class GraphFragment(private val optBaseFragment: IOptBaseFragment) :
        Fragment(R.layout.opt_fragment_graph), IPreparationUserData, IDeposit, IWithdraw {

    private var timeLine = 600.0 * 1000
    private var userId: String? = null
    private var token: ByteArray? = null
    private var isTokenValid = false

    var parentJob: Job? = null
    private var coroutineScope: CoroutineScope? = null

    @SuppressLint("SimpleDateFormat")
    var sdf = SimpleDateFormat("HH:mm")

    private var maxY = 0.0
    private var x: DoubleArray? = null
    private var y: DoubleArray? = null

    private var series: LineGraphSeries<DataPoint?>? = LineGraphSeries()
    private var but = false

    private lateinit var depositDialogFragment: DialogFragment
    private lateinit var withdrawDialogFragment: DialogFragment
    private lateinit var bidInfoDialog: BidInfoDialog

    private val delayRequest = 15_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        depositDialogFragment = DepositDialog(this)
        withdrawDialogFragment = WithdrawDialog()
    }

    private fun getChart() {
        ApiClient.getInstance(token = String(token!!))!!.getService().getChart().enqueue(
                object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        //
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() != 200) return
                        val body = response.body()
                        val strJson = body!!.string()
                        Log.i("TAG", "strJson -> $strJson")

                        val obj = JSONObject(strJson)
                        val optArray = obj.getJSONArray("opt")
                        val dataObject = optArray.getJSONObject(0)
                        val dataArray = dataObject.getJSONArray("data")
                        x = DoubleArray(dataArray.length() - 1)
                        y = DoubleArray(dataArray.length() - 1)
                        for (i in 0 until dataArray.length() - 1) {
                            x!![i] = dataArray.getString(i).substring(1, 14).toDouble()
                            y!![i] = dataArray.getString(i).substring(15, dataArray.getString(i).length - 1).toDouble()
                        }


                        if (isVisible) {
                            updateUI()
                        }
                    }
                }
        )
    }

    private fun getBalance() {
        if (token == null) {
            Log.i("TAG", "TOKEN IS NULL")
            return
        }

        ApiClient.getInstance(token = String(token!!))!!.getService().getBalance().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() != 200) return
                val body = response.body()
                val strJson = body!!.string()
                Log.i("TAG", "strJson balance $strJson")
                try {
                    if (strJson.indexOf("[") == 0) {
                        val arr = JSONArray(strJson)
                        val obj = arr.getJSONObject(0)
                        val jsonBalance = obj.getDouble("balance")
                        userId = obj.getInt("userId").toString()
                        Log.i("TAG", "userId $userId")
                        balanceTxt = jsonBalance.toBigDecimal().toPlainString().toFloat()

                        // todo: надо ли?
                        if (isVisible) {
                            GlobalScope.launch(Dispatchers.Main) {
                                val v = balanceTxt.toBigDecimal()
                                val usd = toUSD(v)
                                balance.text = "${v.toPlainString()} BTC / ${usd.toPlainString()} USD"
                                if (v.compareTo(BigDecimal.ZERO) == 0){
                                    button_up.isEnabled = false
                                    button_down.isEnabled = false
                                    //currency_spinner.isClickable = false
                                }else{
                                    button_up.isEnabled = true
                                    button_down.isEnabled = true
                                    //currency_spinner.isClickable = true
                                }
                            }
                        }
                    } else {
                        isTokenValid = false
                        parentJob?.cancel()
                        setGetBitId()
                    }
                } catch (e: JSONException) {
                    //setGetBitId()
                }
            }

        })
    }

    private fun setGetBitId() {
        ApiClient.getInstance()!!.getService().postBitID(EmptyObj()).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() != 200) return
                val body = response.body()
                val strJson = body!!.string()
                Log.i("TAG", "strJson BitId -> $strJson")

                val obj = JSONObject(strJson)
                val data = obj.getString("bitid")

                optBaseFragment.executeBitIdAsyncTask(Uri.parse(data))
            }
        })
    }

    // POST запросы
    private fun sendPost(but: Boolean, amount: String) {
        if (isVisible) {
            GlobalScope.launch(Dispatchers.Main) {
                llProgressBar.visibility = View.VISIBLE
                root.isClickable = false
            }
        }

        val buttonMethod: String = if (but) "buy" else "sell"

        ApiClient.getInstance(token = String(token!!))!!.getService()
                .postOptBets(OptBets(amount = amount, bet_type = buttonMethod))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        if (isVisible) {
                            GlobalScope.launch(Dispatchers.Main) {
                                llProgressBar.visibility = View.GONE
                                root.isClickable = true
                                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                                optBaseFragment.showToast(t.localizedMessage, true)
                            }
                        }
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() != 200) return
                        if (isVisible) {
                            GlobalScope.launch(Dispatchers.Main) {
                                optBaseFragment.showToast("$buttonMethod Success", true)
                                llProgressBar.visibility = View.GONE
                                root.isClickable = true
                                getBalance()
                            }
                        }
                    }

                })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Кнопки
        button_up.setOnClickListener {
            but = true

            var amount = ti_amount.editText!!.text.toString()
            if (currentCurrency == "USD") {
                amount = getUSD(amount.toBigDecimal()).toString()
            }

            if (amount.isNotEmpty() && amount.toFloat() != 0f) {

                if (amount.toDouble() >= balanceTxt) {
                    // toast
                    return@setOnClickListener
                }


                bidInfoDialog = BidInfoDialog.getInstance(object : IBidConfirm {
                    override fun confirm() {
                        GlobalScope.launch {
                            sendPost(but, amount)
                        }
                    }
                }, but, amount)
                bidInfoDialog.show(fragmentManager?.beginTransaction()!!, "bid_info")
            } else {
                optBaseFragment.showToast("Input amount", true)
            }
        }
        button_down.setOnClickListener {

            but = false

            var amount = ti_amount.editText!!.text.toString()
            if (currentCurrency == "USD") {
                amount = getUSD(amount.toBigDecimal()).toString()
            }
            Log.i("TAG", "amount = $amount")

            if (amount.isNotEmpty() && amount.toFloat() != 0f) {

                if (amount.toDouble() >= balanceTxt) {
                    return@setOnClickListener
                }
                bidInfoDialog = BidInfoDialog.getInstance(object : IBidConfirm {
                    override fun confirm() {
                        GlobalScope.launch {
                            sendPost(but, amount)
                        }
                    }
                }, but, amount)

                // todo...
                bidInfoDialog.show(fragmentManager?.beginTransaction()!!, "bid_info")
            } else {
                optBaseFragment.showToast("Input amount", false)
            }
        }
        controlSize()


        graph?.apply {

            viewport.isXAxisBoundsManual = true
            gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX) {
                        sdf.format(Date(value.toLong()))
                    } else super.formatLabel(value, isValueX)
                }
            }

            changeGraphSize()

            // hide
            gridLabelRenderer.isVerticalLabelsVisible = false
        }


        currency_spinner_text.text = currentCurrency
        currency_spinner.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.inflate(R.menu.opt_popup_select_currency)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.opt_currency_btc -> {
                        currency_spinner_text.text = item.title
                        currentCurrency = item.title.toString()
                        true
                    }
                    R.id.opt_currency_usd -> {
                        currency_spinner_text.text = item.title
                        currentCurrency = item.title.toString()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
            //currencyDialog.show(fragmentManager!!.beginTransaction(), "currency")
        }
    }

    private fun controlSize() {
        to2m.setOnClickListener {
            cleanStateControl()
            it.setBackgroundResource(R.drawable.opt_bg_selected_time)
            timeLine = 120.0 * 1000
            changeGraphSize()
        }
        to5m.setOnClickListener {
            cleanStateControl()
            it.setBackgroundResource(R.drawable.opt_bg_selected_time)
            timeLine = 300.0 * 1000
            changeGraphSize()
        }
        to3h.setOnClickListener {
            cleanStateControl()
            it.setBackgroundResource(R.drawable.opt_bg_selected_time)
            timeLine = 3600.0 * 1000
            changeGraphSize()
        }
        to1d.setOnClickListener {
            cleanStateControl()
            it.setBackgroundResource(R.drawable.opt_bg_selected_time)
            timeLine = 86400.0 * 1000
            changeGraphSize()
        }
        to1mn.setOnClickListener {
            cleanStateControl()
            it.setBackgroundResource(R.drawable.opt_bg_selected_time)
            timeLine = 2592000.0 * 1000
            changeGraphSize()
        }
        to1y.setOnClickListener {
            cleanStateControl()
            it.setBackgroundResource(R.drawable.opt_bg_selected_time)
            timeLine = 31536000.0 * 1000
            changeGraphSize()
        }
        toAll.setOnClickListener {
            changeGraphSize()
        }
    }

    private fun cleanStateControl(){
        arrayListOf(
                to2m, to5m, to3h, to1d, to1mn, to1y, toAll
        ).forEach {
            it.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    android.R.color.transparent))
        }
    }

    private fun changeGraphSize(){
        val tsLong = System.currentTimeMillis()
        graph.viewport.setMinX(tsLong - timeLine)
        graph.viewport.setMaxX(tsLong.toDouble())
        graph.gridLabelRenderer.textSize = 18f
        graph.gridLabelRenderer.reloadStyles()
        graph.viewport.isScrollable = false // enables horizontal scrolling
        graphUpdate()
        // ???
        updateRightLabels()
    }

    private fun graphUpdate(){
        if (series == null){
            series = LineGraphSeries(generateData())
        }else{
            series?.resetData(generateData())
        }

        series?.color = Color.rgb(240, 240, 240)
        series?.setAnimated(true)
        series?.isDrawBackground = true
        //series?.backgroundColor = Color.argb(70,196, 196, 196)
        series?.thickness = 2

        graph.viewport.computeScroll()

        graph.clearSecondScale()
        updateRightLabels()
        graph.secondScale.addSeries(series)
        graph.gridLabelRenderer.isVerticalLabelsVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.optDeposit -> {
                val fTrans = fragmentManager?.beginTransaction()
                depositDialogFragment.show(fTrans!!, "deposit")
                return true
            }
            R.id.optWithdraw -> {
                val fTrans = fragmentManager?.beginTransaction()
                withdrawDialogFragment.show(fTrans!!, "withdraw")
                return true
            }
            R.id.optHistory -> {
                val fTrans = fragmentManager?.beginTransaction()
                val history = HistoryFragment(token!!)
                history.show(fTrans!!, "history")
            }
            R.id.optSupport -> {
                val intent = Intent(requireContext(), SupportActivity::class.java)
                intent.putExtra(SupportActivity.EXTRA_ID_SUPPORT, userId)
                requireActivity().startActivity(intent)
            }
            R.id.optHelp -> {
                requireActivity().startActivity(Intent(Intent(requireContext(), IntroActivity::class.java)))
            }
        }
        return false
    }
    
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        llProgressBar.visibility = View.VISIBLE
        root.isClickable = false

        //eventBus.register(this)

        // TODO: !!!!!!!!!
        loadToken()
        val v = balanceTxt.toBigDecimal()
        val usd = toUSD(v)
        balance.text = "${v.toPlainString()} BTC / ${usd.toPlainString()} USD"
        super.onResume()
    }

    fun toUSD(v: BigDecimal): BigDecimal {
        val c = optBaseFragment.getPriceBitcoin()
        return (v * c).setScale(2, RoundingMode.HALF_UP)
    }

    private fun getUSD(v: BigDecimal): BigDecimal {
        val c = optBaseFragment.getPriceBitcoin()
        return (v / c).setScale(8, RoundingMode.HALF_UP)
    }

    private fun updateDATA() {
        llProgressBar.visibility = View.GONE
        root.isClickable = true

        parentJob = Job()
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        coroutineScope = CoroutineScope(Dispatchers.IO + parentJob!! + handler)
        coroutineScope!!.launch {
            temp()
        }
    }

    private suspend fun temp() {
        try {
            // TODO!!!
            getBalance()
            getChart()

            if (isVisible) {
                updateUI()
            }

        } catch (e: Exception) { }

        delay(delayRequest)
        temp()
    }

    private fun updateUI() {
        GlobalScope.launch(Dispatchers.Main) {

            graph.apply {
                viewport.isXAxisBoundsManual = true
                gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                    override fun formatLabel(value: Double, isValueX: Boolean): String {
                        return if (isValueX) {
                            sdf.format(Date(value.toLong()))
                        } else super.formatLabel(value, isValueX)
                    }
                }
                gridLabelRenderer.textSize = 18f
                gridLabelRenderer.reloadStyles()
                viewport.isScrollable = true // enables horizontal scrolling
            }

            changeGraphSize()
        }
    }

    private fun updateRightLabels() {
        if (y == null) return

        val rounding = 100.0
        val mass2 = y!!.clone()
        Arrays.sort(mass2)
        var minY = mass2[0]
        val manY = mass2[mass2.size - 1]
        val dMin = ceil(minY / rounding) * rounding - rounding - rounding // taaak??
        val dMax = ceil(manY / rounding) * rounding + rounding - rounding
        if (minY == 0.0 || dMin < minY) {
            minY = dMin
        }
        if (maxY == 0.0 || dMax > maxY) {
            maxY = dMax
        }
        Log.i("TAG", "min $minY| max $maxY")
        graph.secondScale.setMinY(dMin)
        graph.secondScale.setMaxY(dMax)
    }

    override fun onPause() {
        //eventBus.unregister(this)
        parentJob?.cancel()
        super.onPause()
    }

    private fun generateData(): Array<DataPoint?> {
        val count = if (x == null) 0 else x!!.size
        val values = arrayOfNulls<DataPoint>(count)
        for (i in 0 until count) {
            val v = DataPoint(x!![i], y!![i])
            values[i] = v
        }
        return values
    }

    private fun loadToken() {
        setGetBitId()
    }

    override fun complete(token: ByteArray, userId: String) {
        this.token = token
        this.userId = userId
        updateDATA()
    }

    companion object {
        private var balanceTxt: Float = 0.0F
        val currencyList: ArrayList<String> = arrayListOf("BTC", "USD")
        private var currentCurrency: String = "BTC"
    }

    override fun deposit(amount: String) {
        optBaseFragment.deposit(amount)
    }

    override fun withdraw(value: String, wallet: String) {
        optBaseFragment.withdraw(value, wallet)
    }

}