package opt.bitstorage.finance.net.model.chart

import com.google.gson.annotations.SerializedName

data class Opt(
        @SerializedName("data")
        val data: List<List<Any>>
)