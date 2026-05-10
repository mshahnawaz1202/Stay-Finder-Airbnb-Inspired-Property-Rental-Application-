package com.example.stayfinder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stayfinder.features.RecommendationEngine
import com.example.stayfinder.firebase.FirestoreFavoritesRepository
import com.example.stayfinder.firebase.FirestoreListingRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private val listingRepo by lazy { FirestoreListingRepository() }
    private val favoritesRepo by lazy { FirestoreFavoritesRepository() }

    private var listingsRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var favoritesRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private var latestListings: List<Property> = emptyList()
    private var latestFavoriteItems: List<com.example.stayfinder.firebase.FavoriteFirestoreItem> =
        emptyList()
    private var selectedTag: String = "all"

    private lateinit var rvListings: RecyclerView
    private lateinit var rvRecommended: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var etMaxPrice: EditText
    private lateinit var etMinRating: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rvListings = view.findViewById(R.id.rvListings)
        rvRecommended = view.findViewById(R.id.rvRecommended)
        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        etMaxPrice = view.findViewById(R.id.etMaxPrice)
        etMinRating = view.findViewById(R.id.etMinRating)

        rvListings.layoutManager = LinearLayoutManager(context)
        rvRecommended.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        view.findViewById<MaterialSwitch>(R.id.switchDarkMode).setOnCheckedChangeListener { _, checked ->
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        setupChips(view)
        view.findViewById<MaterialButton>(R.id.btnApplyFilters).setOnClickListener {
            applyFiltersAndRender()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFiltersAndRender()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    override fun onStart() {
        super.onStart()
        progressBar.visibility = View.VISIBLE
        listingsRegistration = listingRepo.listenAll { list ->
            latestListings = list
            progressBar.visibility = View.GONE
            applyFiltersAndRender()
            renderRecommendations()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            favoritesRegistration = favoritesRepo.listenUserFavorites(uid) { items ->
                latestFavoriteItems = items
                renderRecommendations()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        listingsRegistration?.remove()
        listingsRegistration = null
        favoritesRegistration?.remove()
        favoritesRegistration = null
    }

    private fun setupChips(view: View) {
        val chips = listOf(
            R.id.chipAll to "all",
            R.id.chipBeach to "beach",
            R.id.chipMountain to "mountain",
            R.id.chipCity to "city",
            R.id.chipVillage to "village"
        )
        chips.forEach { (id, tag) ->
            view.findViewById<Chip>(id).setOnClickListener {
                chips.forEach { (otherId, _) ->
                    view.findViewById<Chip>(otherId).isChecked = otherId == id
                }
                selectedTag = tag
                applyFiltersAndRender()
            }
        }
    }

    private fun applyFiltersAndRender() {
        val q = etSearch.text?.toString()?.trim().orEmpty()
        val maxPrice = etMaxPrice.text?.toString()?.toDoubleOrNull()
        val minRating = etMinRating.text?.toString()?.toDoubleOrNull()
        val filtered = latestListings.filter { prop ->
            val locOk = q.isEmpty() || prop.location.contains(q, ignoreCase = true) ||
                prop.title.contains(q, ignoreCase = true)
            val priceOk = maxPrice == null || prop.priceValue <= maxPrice
            val rateOk = minRating == null || (prop.rating.toDoubleOrNull()
                ?: 0.0) >= minRating
            val tagOk = selectedTag == "all" || prop.tags.any {
                it.contains(selectedTag, ignoreCase = true)
            } || prop.title.contains(selectedTag, ignoreCase = true)
            locOk && priceOk && rateOk && tagOk
        }
        rvListings.adapter = HomeAdapter(filtered) { openDetail(it) }
    }

    private fun renderRecommendations() {
        val favProps = latestFavoriteItems.map { item ->
            Property(
                id = item.listingId,
                title = item.title,
                location = item.location,
                price = item.price,
                rating = item.rating,
                imageUrl = item.imageUrl,
                propertyType = guessType(item.title)
            )
        }
        val recs = RecommendationEngine.recommendationsForUser(latestListings, favProps)
        rvRecommended.adapter = HomeAdapter(recs) { openDetail(it) }
    }

    private fun guessType(title: String): String {
        val t = title.lowercase()
        return when {
            t.contains("villa") -> "Villa"
            t.contains("apt") -> "Apt"
            else -> "House"
        }
    }

    private fun openDetail(selectedProperty: Property) {
        val detailFragment = DetailFragment()
        detailFragment.arguments = Bundle().apply {
            putSerializable("PROPERTY_DATA", selectedProperty)
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, detailFragment)
            .addToBackStack(null)
            .commit()
    }
}
