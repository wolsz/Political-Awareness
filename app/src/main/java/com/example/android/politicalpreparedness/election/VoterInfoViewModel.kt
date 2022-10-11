package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoterInfoViewModel(app: Application) : AndroidViewModel(app) {


    private val _navigationAddress = MutableLiveData<NavDirections?>()
    val navigationAddress: LiveData<NavDirections?>
        get() = _navigationAddress

    private val _url = MutableLiveData<String>()
    val url: LiveData<String>
        get() = _url

    private val _isElectionFollowed = MutableLiveData<Boolean>()
    val isElectionFollowed: LiveData<Boolean>
        get() = _isElectionFollowed

    private val dataSource = ElectionDatabase.getInstance(app.applicationContext).electionDao
//    private val database = ElectionDatabase.getInstance(app.applicationContext)
    private val electionsRepository = ElectionsRepository(dataSource)


    //TODO: Add live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    //TODO: Add var and methods to populate voter info
//    init {
////        _isElectionFollowed.value = false
//    }

    fun fetchVoterInfo(electionId: Int, state: String) {
        viewModelScope.launch {
            Log.i("VoterInfoFragment -", "id = ${electionId}, state = ${state}")
            getVoterInfo(electionId, state)
        }
    }

    private suspend fun getVoterInfo(electionId: Int, state: String) {
        withContext(Dispatchers.IO) {
            try {
//                _voterInfo.value = CivicsApi.retrofitService.getVoterInfoResults(electionId, state)
                val response = CivicsApi.retrofitService.getVoterInfoResults(electionId, state)
                _voterInfo.postValue(response)
                Log.i("VoterInfoViewModel", "${response.state?.get(0)?.electionAdministrationBody?.electionInfoUrl}  ---")
                Log.i("VoterInfoViewModel", response.election.name)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("Error-- ","Some error here")
            }
        }
    }

    fun launchPage(url: String) {
        _url.value = url
    }

    fun setIfElectionIsFollowed(electionId: Int) {
        viewModelScope.launch {
            _isElectionFollowed.value = electionsRepository.isElectionSaved(electionId)
        }
    }

    fun takeCorrectFollowAction() {
        if (isElectionFollowed.value == true) {
            unFollowElection(voterInfo.value!!.election.id)
        } else {
            followElection(voterInfo.value!!.election.id)
        }
    }

    private fun followElection(id: Int) {
        viewModelScope.launch {
            electionsRepository.insertFollowedElection(id)
            _isElectionFollowed.value = true
        }
    }

    private fun unFollowElection(id: Int) {
        viewModelScope.launch {
            electionsRepository.deleteFollowedElection(id)
            _isElectionFollowed.value = false
        }
    }

}