package com.example.stayfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        val rvFavorites = view.findViewById<RecyclerView>(R.id.rvFavorites)
        val tvEmptyFavorites = view.findViewById<TextView>(R.id.tvEmptyFavorites)

        rvFavorites.layoutManager = LinearLayoutManager(context)

        loadFavorites(rvFavorites, tvEmptyFavorites)
    }

    private fun loadFavorites(rv: RecyclerView, tvEmpty: TextView) {
        // JOIN query: favorites ⟵ listings (foreign key relationship)
        val pairs = dbHelper.getFavoritesWithListings()

        if (pairs.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE

            adapter = FavoritesAdapter(pairs.toMutableList()) { favoriteId ->
                // Remove from DB and refresh list
                dbHelper.deleteFavorite(favoriteId)
                loadFavorites(rv, tvEmpty)
            }
            rv.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
    }
}
