package opt.bitstorage.finance.net.model.balance

data class Balance(
        val id: Int,
        val currency: String,
        val userId: Int,
        val balance: Int,
        val createdAt: String,
        val updatedAt: String
)