package com.example.stayfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class HostFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_host, container, false)

        dbHelper = DatabaseHelper(requireContext())

        val etTitle       = view.findViewById<TextInputEditText>(R.id.etPropertyTitle)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etPropertyDescription)
        val etPrice       = view.findViewById<TextInputEditText>(R.id.etPropertyPrice)
        val rgPropertyType = view.findViewById<RadioGroup>(R.id.rgPropertyType)
        val btnSubmit     = view.findViewById<Button>(R.id.btnSubmitHost)

        var selectedType = "House"
        rgPropertyType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.rbHouse     -> "House"
                R.id.rbApartment -> "Apt"
                R.id.rbVilla     -> "Villa"
                else             -> "House"
            }
        }

        btnSubmit.setOnClickListener {
            val title       = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val price       = etPrice.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()) {

                // ── Save listing to SQLite DB ────────────────────────
                val listing = ListingEntity(
                    title     = "$selectedType: $title",
                    price     = "\$$price/night",
                    amenities = description,   // reuse description as amenities text
                    imageUrl  = ""
                )
                val newId = dbHelper.insertListing(listing)

                if (newId != -1L) {
                    Toast.makeText(
                        context,
                        "'$title' listed successfully! (ID $newId)",
                        Toast.LENGTH_LONG
                    ).show()

                    // Clear fields after successful insert
                    etTitle.text?.clear()
                    etDescription.text?.clear()
                    etPrice.text?.clear()
                    rgPropertyType.check(R.id.rbHouse)
                } else {
                    Toast.makeText(context, " Failed to save listing", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
    }
}
