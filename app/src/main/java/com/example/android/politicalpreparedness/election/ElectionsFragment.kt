package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    private val viewModel : ElectionsViewModel by lazy {
        val application = requireNotNull(this.activity).application
        val viewModelFactory = ElectionsViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)
    }

    private lateinit var upcomingElectionListAdapter: ElectionListAdapter
    private lateinit var savedElectionListAdapter: ElectionListAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentElectionBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        upcomingElectionListAdapter = ElectionListAdapter(ElectionListener {
            viewModel.upcomingElectionSelected(it)
        })
        binding.upcomingElectionsList.adapter = upcomingElectionListAdapter

        savedElectionListAdapter = ElectionListAdapter(ElectionListener {
            viewModel.savedElectionSelected(it)
        })
        binding.savedElectionsList.adapter = savedElectionListAdapter

        viewModel.navigationAddress.observe(viewLifecycleOwner, Observer {
            directions ->
            directions?.let {
                this.findNavController().navigate(directions)
                viewModel.doneNavigating()
            }
        })

        Log.v("Another Fragment", "Creating the view again")
        viewModel.refreshFollowedElection()

        return binding.root

    }

}