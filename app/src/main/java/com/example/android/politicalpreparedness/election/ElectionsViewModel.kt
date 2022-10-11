package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import kotlinx.coroutines.launch

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(app: Application) : AndroidViewModel(app) {


    private val _navigationAddress = MutableLiveData<NavDirections?>()
    val navigationAddress: LiveData<NavDirections?>
        get() = _navigationAddress

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    private val dataSource = ElectionDatabase.getInstance(app.applicationContext).electionDao
    private val electionsRepository = ElectionsRepository(dataSource)

     val upcomingElections = electionsRepository.currentElections

    init {
        refreshElections()
    }

    private fun refreshElections() {
        viewModelScope.launch {
            electionsRepository.refreshElections()
        }
    }

    fun upcomingElectionSelected(election: Election) {
        _navigationAddress.value = ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id, election.division)
    }

    fun savedElectionSelected(election: Election) {
        _navigationAddress.value = ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id, election.division)
    }

    fun doneNavigating() {
        _navigationAddress.value = null
    }

    fun refreshFollowedElection() {
        viewModelScope.launch {
            _savedElections.value = electionsRepository.getFollowedElections()
        }
    }

}