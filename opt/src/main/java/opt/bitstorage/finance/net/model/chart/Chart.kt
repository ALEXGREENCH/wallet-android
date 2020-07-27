package opt.bitstorage.finance.net.model.chart

import com.google.gson.annotations.SerializedName

data class Chart(
        @SerializedName("opt")
        val opt: List<Opt>,
        @SerializedName("lastprice")
        val lastPrice: Double
)