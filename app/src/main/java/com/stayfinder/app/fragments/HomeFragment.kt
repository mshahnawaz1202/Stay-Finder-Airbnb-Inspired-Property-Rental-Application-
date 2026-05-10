package com.stayfinder.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.stayfinder.app.R
import com.stayfinder.app.adapters.PropertyAdapter
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.databinding.FragmentHomeBinding
import com.stayfinder.app.models.Property
import com.stayfinder.app.repository.PropertyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var db: DatabaseHelper
    private lateinit var repository: PropertyRepository
    private lateinit var adapter: PropertyAdapter
    private var allProperties = listOf<Property>()
    private var userId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userId = arguments?.getLong("USER_ID") ?: -1L
        val userName = arguments?.getString("USER_NAME") ?: "User"
        
        db = DatabaseHelper.getInstance(requireContext())
        repository = PropertyRepository(db)
        
        setupGreeting(userName)
        setupRecyclerView()
        setupSearch()
        setupFilters()
        
        loadProperties()
    }

    private fun setupGreeting(name: String) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        binding.tvGreeting.text = "$greeting, $name \ud83d\udc4b"
    }

    private fun setupRecyclerView() {
        adapter = PropertyAdapter(
            onCardClick = { property -> openDetail(property) },
            onHeartClick = { property, position -> toggleWishlist(property, position) }
        )
        binding.rvProperties.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProperties.adapter = adapter
    }

    private fun loadProperties() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val properties = repository.fetchAndMapProperties(userId)
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                binding.progressBar.visibility = View.GONE
                if (properties.isNotEmpty()) {
                    allProperties = properties
                    adapter.submitList(properties)
                } else {
                    Snackbar.make(binding.root, R.string.api_error, Snackbar.LENGTH_LONG).show()
                    val fallback = db.getWishlistByUser(userId)
                    allProperties = fallback
                    adapter.submitList(fallback)
                }
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        if (userId != -1L) {
                            db.insertSearchHistory(userId, it)
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText ?: "")
                return true
            }
        })
    }

    private fun filterList(query: String) {
        val filtered = allProperties.filter { 
            it.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) || 
            it.location.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
        }
        adapter.submitList(filtered)
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener { updateFilter("All") }
        binding.chipEntire.setOnClickListener { updateFilter("Entire Stay") }
        binding.chipPrivate.setOnClickListener { updateFilter("Private Room") }
        binding.chipShared.setOnClickListener { updateFilter("Shared") }
    }

    private fun updateFilter(type: String) {
        val selectedColor = Color.WHITE
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.primary)

        binding.chipAll.setBackgroundResource(if (type == "All") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipAll.setTextColor(if (type == "All") selectedColor else unselectedColor)
        
        binding.chipEntire.setBackgroundResource(if (type == "Entire Stay") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipEntire.setTextColor(if (type == "Entire Stay") selectedColor else unselectedColor)
        
        binding.chipPrivate.setBackgroundResource(if (type == "Private Room") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipPrivate.setTextColor(if (type == "Private Room") selectedColor else unselectedColor)
        
        binding.chipShared.setBackgroundResource(if (type == "Shared") R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        binding.chipShared.setTextColor(if (type == "Shared") selectedColor else unselectedColor)

        val filtered = if (type == "All") allProperties else allProperties.filter { it.type == type }
        adapter.submitList(filtered)
    }

    private fun toggleWishlist(property: Property, position: Int) {
        if (userId == -1L) {
            Snackbar.make(binding.root, "Please login to save favorites", Snackbar.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (property.isInWishlist) {
                db.removeFromWishlist(userId, property.apiPostId)
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.format(Date())
                db.addToWishlist(userId, property.copy(dateSaved = date))
            }
            
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                property.isInWishlist = !property.isInWishlist
                adapter.notifyItemChanged(position, true)
                val msgRes = if (property.isInWishlist) R.string.added_to_wishlist else R.string.removed_from_wishlist
                Snackbar.make(binding.root, msgRes, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDetail(property: Property) {
        val fragment = PropertyDetailFragment()
        val bundle = Bundle().apply {
            putString("PROPERTY_NAME", property.name)
            putString("PROPERTY_LOCATION", property.location)
            putDouble("PROPERTY_PRICE", property.pricePerNight)
            putDouble("PROPERTY_RATING", property.rating)
            putString("PROPERTY_TYPE", property.type)
            putLong("USER_ID", userId)
            putInt("API_POST_ID", property.apiPostId)
            putBoolean("IS_IN_WISHLIST", property.isInWishlist)
        }
        fragment.arguments = bundle
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("detail")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
