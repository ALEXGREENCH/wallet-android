package opt.bitstorage.finance.net.model

data class OptBets(
        var currency: String = "USD",
        var c_currency: String = "BTC",
        var amount: String,
        var curname: String = "BTC",
        var bet_type: String
)