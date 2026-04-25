package com.example.stayfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.stayfinder.repository.PropertyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private val repository = PropertyRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val rvListings: RecyclerView = view.findViewById(R.id.rvListings)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        
        rvListings.layoutManager = LinearLayoutManager(context)

        // Fetch from API
        fetchProperties(rvListings, progressBar)

        return view
    }

    private fun fetchProperties(rvListings: RecyclerView, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val properties = repository.getProperties()
                
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (properties.isNotEmpty()) {
                        val adapter = HomeAdapter(properties) { selectedProperty ->
                            val detailFragment = DetailFragment()
                            val bundle = Bundle()
                            bundle.putSerializable("PROPERTY_DATA", selectedProperty)
                            detailFragment.arguments = bundle

                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.content_frame, detailFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                        rvListings.adapter = adapter
                    } else {
                        Toast.makeText(context, "No properties found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
