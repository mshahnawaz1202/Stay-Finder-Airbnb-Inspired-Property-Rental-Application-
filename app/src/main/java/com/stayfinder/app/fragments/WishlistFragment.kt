package com.stayfinder.app.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stayfinder.app.R
import com.stayfinder.app.adapters.WishlistAdapter
import com.stayfinder.app.activities.database.DatabaseHelper
import com.stayfinder.app.databinding.FragmentWishlistBinding
import com.stayfinder.app.models.Property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: WishlistAdapter
    private var wishlistItems = listOf<Property>()
    private var userId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getLong("USER_ID") ?: -1L
        db = DatabaseHelper.getInstance(requireContext())

        setupRecyclerView()
        setupSearch()
        loadWishlist()
    }

    private fun setupRecyclerView() {
        adapter = WishlistAdapter(
            onItemClick = { property -> openDetail(property) },
            onItemLongClick = { property -> showDeleteDialog(property) }
        )
        binding.rvWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWishlist.adapter = adapter
    }

    private fun loadWishlist() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val list = db.getWishlistByUser(userId)
            wishlistItems = list
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                adapter.submitList(list)
                updateStats(list)
                binding.tvEmptyWishlist.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateStats(list: List<Property>) {
        binding.tvStatSavedCount.text = list.size.toString()
        val avg = if (list.isNotEmpty()) list.map { it.pricePerNight }.average() else 0.0
        binding.tvStatAvgPrice.text = String.format(Locale.getDefault(), "PKR %,.0f", avg)
        binding.tvStatTypesCount.text = list.map { it.type }.distinct().size.toString()
    }

    private fun setupSearch() {
        binding.etWishlistSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase(Locale.getDefault())
                val filtered = wishlistItems.filter {
                    it.name.lowercase(Locale.getDefault()).contains(query) || 
                    it.location.lowercase(Locale.getDefault()).contains(query)
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showDeleteDialog(property: Property) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove from wishlist?")
            .setMessage("Are you sure you want to remove ${property.name}?")
            .setPositiveButton("Remove") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    db.removeFromWishlistById(property.id)
                    loadWishlist()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            putBoolean("IS_IN_WISHLIST", true)
        }
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
