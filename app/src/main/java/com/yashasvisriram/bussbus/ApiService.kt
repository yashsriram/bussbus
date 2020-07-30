package com.yashasvisriram.bussbus

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiService {
    @GET("NexTrip/{stop_id}?format=json")
    fun getStopDepartures(@Path("stop_id") stopId: String): Single<List<StopDeparture>?>?
}
