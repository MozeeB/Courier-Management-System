package id.cikup.couriermanagementsystem.data.service

import id.cikup.couriermanagementsystem.data.model.MapsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Julsapargi Nursam on 12/31/20.
 */
interface MapsService {
    @GET("maps/api/directions/json")
    suspend fun getDirectionLocation(
        @Query("origin") origin: String?,
        @Query("destination") destination: String?,
        @Query("key") key: String?
    ): Response<MapsResponse>
}