package com.example.android.politicalpreparedness.representative

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.repository.Result
import com.example.android.politicalpreparedness.util.SingleLiveEvent
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    //TODO: Establish live data for representatives and address

    val line1 = MutableLiveData("")

    val line2 = MutableLiveData("")

    val city = MutableLiveData("")

    val state = MutableLiveData("")

    val zip = MutableLiveData("")

    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()

    private val _deviceLocationOn = MutableLiveData(false)
    val deviceLocationOn: LiveData<Boolean>
        get() = _deviceLocationOn

    private val _locationEnabled = MutableLiveData(false)
    val locationEnabled :LiveData<Boolean>
        get() = _locationEnabled


    private val _representatives: MutableLiveData<List<Representative>> = MutableLiveData()
    val representatives: LiveData<List<Representative>>
        get() = _representatives


    fun validateEnteredData(): Boolean {
        if (line1.value.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_line1
            return false
        }

        if (city.value.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_city
            return false
        }

        if (state.value.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_state
            return false
        }

        if (zip.value.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_zip
            return false
        }
        return true
    }

    fun getTheRepresentatives() {

        if (!validateEnteredData()) {
            return
        }

        val address = Address(
                line1 = line1.value!!,
                line2 = line2.value,
                city = city.value!!,
                state = state.value!!,
                zip = zip.value!!
        )

        viewModelScope.launch {
            try {
                val (offices, officials) = CivicsApi.retrofitService.getRepresentativesResults(address.toFormattedString())
                _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }

    }

    fun setLocationEnabled() {
        _locationEnabled.value = true
    }

    fun setDeviceLocationOn() {
        _deviceLocationOn.value = true
    }

    fun getRepresentativesByLocation(address: Address) {
        line1.value = address.line1
        line2.value = ""
        city.value = address.city
        state.value = address.state
        zip.value = address.zip
        getTheRepresentatives()
    }

}
