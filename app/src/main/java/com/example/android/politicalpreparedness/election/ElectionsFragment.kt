package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    private val viewModel : ElectionsViewModel by lazy {
        ViewModelProvider(this)[ElectionsViewModel::class.java]
    }

    private var _binding: FragmentElectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var upcomingElectionListAdapter: ElectionListAdapter
    private lateinit var savedElectionListAdapter: ElectionListAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentElectionBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        upcomingElectionListAdapter = ElectionListAdapter(ElectionListener {
            viewModel.upcomingElectionSelected(it)
        })
        binding.upcomingElectionsList.adapter = upcomingElectionListAdapter

        savedElectionListAdapter = ElectionListAdapter(ElectionListener {
            viewModel.savedElectionSelected(it)
        })
        binding.savedElectionsList.adapter = savedElectionListAdapter

        viewModel.navigationAddress.observe(viewLifecycleOwner) { directions ->
            directions?.let {
                this.findNavController().navigate(directions)
                viewModel.doneNavigating()
            }
        }

        Log.v("Another Fragment", "Creating the view again")
        viewModel.refreshFollowedElection()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}