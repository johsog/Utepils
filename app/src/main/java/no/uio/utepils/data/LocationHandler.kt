package no.uio.utepils.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import no.uio.utepils.HomeScreenViewModel

// Denne klassen håndterer alt som har med lokasjon å gjøre.
class LocationHandler {
    companion object {
        // Denne metoden sjekker om lokasjons-tillatelser er gitt.
        fun locationPermitted(context: ComponentActivity): Boolean {
            return (
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }

        // Denne ber om tillattelse til å bruke telefonens lokasjon.
        fun requestLocationAccess(context: ComponentActivity, viewModel: HomeScreenViewModel) {
            context.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                    // Location access granted.
                    Log.d("requestAccess", "Fine Access Approved")
                    viewModel.fetchLocation(context)
                } else {
                    // No location access granted.
                    Log.d("requestAccess", "Access Denied")
                    viewModel.dataStatus.value = DataSource.Status.LOCATION_ACCESS_DENIED
                }
            }.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        // Denne bruker locationManager til å be om lokasjonsoppdateringer og legge med i en LocationListener,
        // som oppdaterer currentLocation i ViewModelen hver gang man flytter seg mer enn 500 meter.
        // Valgte denne verdien siden det ikke er så viktig å hente nye værdata ofte når man
        // befinner seg på omtrentlig samme sted.
        @SuppressLint("MissingPermission")
        fun followLocation(context: ComponentActivity, listener: LocationListener) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 500f, listener)
        }
    }
}