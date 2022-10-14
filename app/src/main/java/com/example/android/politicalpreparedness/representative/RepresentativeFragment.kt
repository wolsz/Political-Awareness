package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import java.util.*


class RepresentativeFragment : Fragment() {

    companion object {
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 5
        private val TAG = RepresentativeFragment::class.java.simpleName
    }

    private var _binding: FragmentRepresentativeBinding? = null
    private val binding get() = _binding!!

    private lateinit var layout: View


    private lateinit var representativeAdapter: RepresentativeListAdapter

    private val viewModel: RepresentativeViewModel by lazy {
        ViewModelProvider(this)[RepresentativeViewModel::class.java]
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepresentativeBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        layout = binding.constraintLayout!!

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.getTheRepresentatives()
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            getLocation()
        }

        representativeAdapter = RepresentativeListAdapter()
        binding.representativeList.adapter = representativeAdapter

        checkDeviceLocationSettings()
        return binding.root
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
                doToast("Permission granted as requested")
                locationGranted()
            } else {
                doToast("Permission denied at this time")
                Log.i("Permission: ", "Denied")
            }
        }

    private fun doToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun onClickRequestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                locationGranted()

                doToast("Permission already granted.")
             }

            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                layout.showSnackBar(
                    view,
                    "Location access is required to access and display the representatives.",
//                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    "Ok"
//                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }


    private fun locationGranted() {
        Log.d("locationGranted", "locationGranted: Yes granted... ")
        viewModel.setLocationEnabled()
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationResult = fusedLocationClient.lastLocation
        locationResult.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("getLocation", "getLocation: $location")
                val address = geoCodeLocation(location)
                viewModel.getRepresentativesByLocation(address)
            }
        }
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    "${address.subThoroughfare} ${address.thoroughfare}",
                    "",
                    address.locality ?: address.subLocality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }


    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    ActivityCompat.startIntentSenderForResult(
                        requireActivity(),
                        exception.resolution.intentSender, REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        TAG,
                        "checkDeviceLocationSettings: Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Log.i(TAG, "checkDeviceLocationSettings: Snack bar")
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                onClickRequestPermission(requireView())
                viewModel.setDeviceLocationOn()
                Log.i(TAG, "checkLocationPermissions: Device location setting is on")

            }
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettings(false)
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun View.showSnackBar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackBar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackBar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackBar.show()
        }
    }


}