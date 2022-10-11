package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.location.R
import com.google.android.material.snackbar.Snackbar
import java.util.*


class RepresentativeFragment : Fragment() {

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 5
    }

    private lateinit var representativeAdapter: RepresentativeListAdapter

    private val viewModel: RepresentativeViewModel by lazy {
        ViewModelProvider(this).get(RepresentativeViewModel::class.java)
    }

    private val TAG = RepresentativeFragment::class.java.simpleName


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentRepresentativeBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.getTheRepresentatives()
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            getLocation()
        }

        viewModel.showSnackBarInt.observe(viewLifecycleOwner, Observer {resId ->
            Snackbar.make(this.requireView(), getString(resId), Snackbar.LENGTH_LONG).show()
        })

        //TODO: Define and assign Representative adapter
        representativeAdapter = RepresentativeListAdapter()
        binding.representativeList.adapter = representativeAdapter

        checkDeviceLocationSettings()
        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(requireContext(), "Permission granted in Request Permission, moving on....", Toast.LENGTH_LONG).show()
//                Log.d("onRequestPermissionsResult", "Permission granted in Request Permission, moving on....")
                locationGranted()
            } else {
                showDenialDialog()
                Log.d("onRequestPermissionsResult", "Showing snackbar to request permission.")
            }

        }
    }

    private fun checkLocationPermissions(permissionString: String, requestCode: Int) {
        when {
            ContextCompat.checkSelfPermission(requireContext(),
                    permissionString) == PackageManager.PERMISSION_GRANTED -> {
//                Log.d("checkLocationPermissions", "checkLocationPermissions: Permission granted in top check")
                Toast.makeText(requireContext(), "Permission granted in Check Location Permission: Permission granted in top check", Toast.LENGTH_LONG).show()
                locationGranted()
            }
            shouldShowRequestPermissionRationale(permissionString) -> showReasonDialog(permissionString, requestCode)
            else -> {
                Log.d("checkLocationPermissions", "checkLocationPermissions: Requesting permission in ")
                requestPermissions(arrayOf(permissionString), requestCode)}
        }
    }

    private fun locationGranted() {
        Log.d("locationGranted", "locationGranted: Yes granted... ")
        viewModel.setLocationEnabled()
    }

    private fun showReasonDialog(permissionString: String, requestCode: Int) {
        val alertBuilder = AlertDialog.Builder(requireContext())

        alertBuilder.apply {
            setMessage(R.string.permission_denied_explanation)
            setTitle("Permission required")
            setPositiveButton("OK") { _: DialogInterface, _: Int ->
                requestPermissions(arrayOf(permissionString), requestCode)
            }
        }
        val dialog = alertBuilder.create()
        dialog.show()
    }

    private fun showDenialDialog() {
        val alertBuilder = AlertDialog.Builder(requireContext())

        alertBuilder.apply {
            setMessage(R.string.permission_denied_explanation)
            setTitle("Location permission denied")
            setPositiveButton("OK") { _: DialogInterface, _: Int ->  }
        }
        val dialog = alertBuilder.create()
        dialog.show()
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationResult = fusedLocationClient.lastLocation
        locationResult.
        addOnSuccessListener { location ->
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
                    Address("${address.subThoroughfare} ${address.thoroughfare}", "", address.locality?:address.subLocality, address.adminArea, address.postalCode)
                }
                .first()
    }


    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    this.startIntentSenderForResult(
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
                Log.i(TAG, "checkDeviceLocationSettings: Snackbar")
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
                checkLocationPermissions(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                viewModel.setDeviceLocationOn()
                Log.i(TAG, "checkLocationPermissions: Device location setting is on")

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettings(false)
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}