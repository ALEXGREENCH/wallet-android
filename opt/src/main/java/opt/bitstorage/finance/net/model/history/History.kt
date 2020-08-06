package opt.bitstorage.finance.net.model.history

import com.google.gson.annotations.SerializedName

data class History (
		@SerializedName("id") val id : Int,
		@SerializedName("user_id") val user_id : Int,
		@SerializedName("bet_amount") val bet_amount : Double,
		@SerializedName("bet_type") val bet_type : String,
		@SerializedName("bet_status") val bet_status : String,
		@SerializedName("current_price") val current_price : Double,
		@SerializedName("bet_pair") val bet_pair : String,
		@SerializedName("finish_price") val finish_price : Double,
		@SerializedName("finished_at") val finished_at : String,
		@SerializedName("pending_at") val pending_at : String,
		@SerializedName("bet_ccur") val bet_ccur : String,
		@SerializedName("bet_cur") val bet_cur : String,
		@SerializedName("currency") val currency : String,
		@SerializedName("createdAt") val createdAt : String,
		@SerializedName("updatedAt") val updatedAt : String
)