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

class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_map, container, false)
        
        val btnShowNearby = view.findViewById<Button>(R.id.btnShowNearby)
        val etMapSearch = view.findViewById<EditText>(R.id.etMapSearch)

        btnShowNearby.setOnClickListener {
            val query = etMapSearch.text.toString().trim().ifEmpty { "hotels nearby" }
            openGoogleMaps(query)
        }
        
        return view
    }
    
    // Better: Open Google Maps using Intent
    private fun openGoogleMaps(query: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Fallback to browser if Maps app is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"))
            startActivity(browserIntent)
        }
    }
}
