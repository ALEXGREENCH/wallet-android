package opt.bitstorage.finance.net.model

import com.google.gson.annotations.SerializedName

data class OptBets(
        @SerializedName("currency")
        var currency: String = "USD",
        @SerializedName("c_currency")
        var cCurrency: String = "BTC",
        @SerializedName("amount")
        var amount: String,
        @SerializedName("curname")
        var curname: String = "BTC",
        @SerializedName("bet_type")
        var betType: String
)