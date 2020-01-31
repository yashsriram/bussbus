package com.yashasvisriram.bussbus

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class StopDeparture {
    @Expose
    @SerializedName("Actual")
    val actual = false
    @Expose
    @SerializedName("BlockNumber")
    val blockNumber = 0f
    @Expose
    @SerializedName("DepartureText")
    val departureText: String? = null
    @Expose
    @SerializedName("DepartureTime")
    val departureTime: String? = null
    @Expose
    @SerializedName("Description")
    val description: String? = null
    @Expose
    @SerializedName("Gate")
    val gate: String? = null
    @Expose
    @SerializedName("Route")
    val route: String? = null
    @Expose
    @SerializedName("RouteDirection")
    val routeDirection: String? = null
    @Expose
    @SerializedName("Terminal")
    val terminal: String? = null
    @Expose
    @SerializedName("VehicleHeading")
    val vehicleHeading = 0f
    @Expose
    @SerializedName("VehicleLatitude")
    val vehicleLatitude = 0f
    @Expose
    @SerializedName("VehicleLongitude")
    val vehicleLongitude = 0f
}
