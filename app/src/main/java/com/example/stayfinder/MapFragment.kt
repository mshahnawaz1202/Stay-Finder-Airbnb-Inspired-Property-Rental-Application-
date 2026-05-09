package com.example.stayfinder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stayfinder.firebase.FirestoreListingRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ListenerRegistration

class MapFragment : Fragment() {

    private val listingRepo = FirestoreListingRepository()
    private var map: GoogleMap? = null
    private var listingsReg: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            map = googleMap
            googleMap.uiSettings.isZoomControlsEnabled = true
            attachListingsListener()
        } ?: run {
            Toast.makeText(context, "Map is loading…", Toast.LENGTH_SHORT).show()
        }

        val btnShowNearby = view.findViewById<Button>(R.id.btnShowNearby)
        val etMapSearch = view.findViewById<EditText>(R.id.etMapSearch)
        btnShowNearby.setOnClickListener {
            val query = etMapSearch.text.toString().trim().ifEmpty { "hotels nearby" }
            openGoogleMaps(query)
        }
    }

    private fun attachListingsListener() {
        val googleMap = map ?: return
        listingsReg?.remove()
        listingsReg = listingRepo.listenAll { list ->
            googleMap.clear()
            val withCoords = list.filter { it.latitude != 0.0 && it.longitude != 0.0 }
            for (p in withCoords) {
                val pos = LatLng(p.latitude, p.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(p.title)
                        .snippet("${p.price} • ${p.location}")
                )
            }
            if (withCoords.isNotEmpty()) {
                val first = withCoords.first()
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(first.latitude, first.longitude),
                        11f
                    )
                )
            } else {
                Toast.makeText(context, "Add listings with a location to see pins", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        listingsReg?.remove()
        listingsReg = null
        map = null
        super.onDestroyView()
    }

    private fun openGoogleMaps(query: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
            )
            startActivity(browserIntent)
        }
    }
}
