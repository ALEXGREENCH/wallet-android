package opt.bitstorage.finance.net

import okhttp3.ResponseBody
import opt.bitstorage.finance.net.model.Deposit
import opt.bitstorage.finance.net.model.EmptyObj
import opt.bitstorage.finance.net.model.OptBets
import opt.bitstorage.finance.net.model.Withdraw
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("chart")
    fun getChart(): Call<ResponseBody>

    @GET("balance")
    fun getBalance(): Call<ResponseBody>

    @GET("history")
    fun getHistory(): Call<ResponseBody>

    @GET("open-bets")
    fun getOpenBets(): Call<ResponseBody>

    @POST("bitid")
    fun postBitID(@Body emptyObj: EmptyObj): Call<ResponseBody>

    @POST("optbets")
    fun postOptBets(@Body optBets: OptBets): Call<ResponseBody>

    @POST("deposit")
    fun postDeposit(@Body deposit: Deposit): Call<ResponseBody>

    @POST("withdraw")
    fun postWithdraw(@Body withdraw: Withdraw): Call<ResponseBody>
}