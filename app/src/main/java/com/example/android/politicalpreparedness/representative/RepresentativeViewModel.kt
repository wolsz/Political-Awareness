package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.util.NetworkResult
import com.example.android.politicalpreparedness.util.SingleLiveEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.Response

class RepresentativeViewModel(application: Application) : AndroidViewModel(application) {

    val line1 = MutableLiveData("")

    val line2 = MutableLiveData("")

    val city = MutableLiveData("")

    val state = MutableLiveData("")

    val zip = MutableLiveData("")

    var networkStatus = false

    private val _deviceLocationOn = MutableLiveData(false)
    val deviceLocationOn: LiveData<Boolean>
        get() = _deviceLocationOn

    private val _locationEnabled = MutableLiveData(false)
    val locationEnabled: LiveData<Boolean>
        get() = _locationEnabled


    private val _representatives: MutableLiveData<List<Representative>> = MutableLiveData()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    var representativesResponse: MutableLiveData<NetworkResult<RepresentativeResponse>> =
        MutableLiveData()

    val locationAddress = MutableLiveData(Address("", "", "", "", ""))

    val msg: MutableLiveData<String?> = MutableLiveData("")

    private fun validateEnteredData(): Boolean {
        if (line1.value.isNullOrEmpty()) {
            msg.value = getApplication<Application>().resources.getString(R.string.err_enter_line1)
            return false
        }

        if (city.value.isNullOrEmpty()) {
            msg.value = getApplication<Application>().resources.getString(R.string.err_enter_city)
            return false
        }

        if (state.value.isNullOrEmpty()) {
            msg.value = getApplication<Application>().resources.getString(R.string.err_enter_state)
            return false
        }

        if (zip.value.isNullOrEmpty()) {
            msg.value = getApplication<Application>().resources.getString(R.string.err_enter_zip)
            return false
        }
        locationAddress.value = Address(
            line1 = line1.value!!,
            line2 = line2.value,
            city = city.value!!,
            state = state.value!!,
            zip = zip.value!!
        )
        return true
    }

    fun getTheRepresentatives() {



        val address = locationAddress.value!!
        representativesResponse.value = NetworkResult.Loading()
        Log.d("RepresentativeViewModel", "getTheRepresentatives: !!!!!!!!!!!!!")

        if (hasInternetConnection()) {
            viewModelScope.launch {
                try {
                    val response = CivicsApi.retrofitService.getRepresentativesResults(
                        address.toFormattedString()
                    )
                    representativesResponse.value = handleRepresentativeResponse(response)
                    val (offices, officials) =  (representativesResponse.value)!!.data as RepresentativeResponse
                    _representatives.value =
                        offices.flatMap { office -> office.getRepresentatives(officials) }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    representativesResponse.value = NetworkResult.Error("Bad Input")
                }
            }
        } else {
            representativesResponse.value = NetworkResult.Error("No Internet Connection")
        }

    }

    fun setLocationEnabled() {
        _locationEnabled.value = true
    }

    fun setDeviceLocationOn() {
        _deviceLocationOn.value = true
    }

    private fun handleRepresentativeResponse(response: Response<RepresentativeResponse>): NetworkResult<RepresentativeResponse> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.offices.isEmpty() -> {
                return NetworkResult.Error("Representatives not found.")
            }
            response.isSuccessful -> {
                val representatives = response.body()!!

                return NetworkResult.Success(representatives)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    fun hasInternetConnection(): Boolean {

        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            return isConnected(connectivityManager)
        }
    }

    @Suppress("DEPRECATION")
    private fun isConnected(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo ?: return false
        return when {
            (activeNetwork.type == ConnectivityManager.TYPE_WIFI) -> true
            (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) -> true
            (activeNetwork.type == ConnectivityManager.TYPE_ETHERNET) -> true
            else -> false
        }
    }

    fun setAddress(address: Address) {
        line1.value = address.line1
        line2.value = ""
        city.value = address.city
        state.value = address.state
        zip.value = address.zip
        locationAddress.value = address
    }

    fun validateAddress() {
        if (!validateEnteredData()) {
            Log.d("RepresentativeViewModel", "getTheRepresentatives: Not validated here")
            return
        }
    }

    fun onShowMsgComplete() {
        msg.value = ""
    }

    fun showNetworkStatus() {
        if(!networkStatus) {
            Toast.makeText(getApplication(), "No Internet Connection.", Toast.LENGTH_SHORT).show()
        }
    }
}
