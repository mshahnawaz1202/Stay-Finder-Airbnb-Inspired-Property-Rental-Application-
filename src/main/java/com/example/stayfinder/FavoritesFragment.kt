package com.example.stayfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.SearchView
import android.widget.RadioGroup

class FavoritesFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: FavoritesAdapter
    private var currentQuery: String = ""
    private var isAscending: Boolean = true

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
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val rgSort = view.findViewById<RadioGroup>(R.id.rgSort)

        rvFavorites.layoutManager = LinearLayoutManager(context)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query ?: ""
                searchFavorites(rvFavorites, tvEmptyFavorites)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                searchFavorites(rvFavorites, tvEmptyFavorites)
                return true
            }
        })

        rgSort.setOnCheckedChangeListener { _, checkedId ->
            isAscending = checkedId == R.id.rbSortAsc
            searchFavorites(rvFavorites, tvEmptyFavorites)
        }

        // Initialize with default
        rgSort.check(R.id.rbSortAsc)
        searchFavorites(rvFavorites, tvEmptyFavorites)
    }

    private fun searchFavorites(rv: RecyclerView, tvEmpty: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pairs = dbHelper.searchFavoritesWithListings(currentQuery, isAscending)
            
            withContext(Dispatchers.Main) {
                if (pairs.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rv.visibility = View.VISIBLE

                    adapter = FavoritesAdapter(
                        pairs.toMutableList(),
                        onRemove = { favoriteId ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                dbHelper.deleteFavorite(favoriteId)
                                searchFavorites(rv, tvEmpty)
                            }
                        },
                        onEditNote = { favEntity ->
                            val dialog = EditFavoriteDialog(favEntity.id, favEntity.note) {
                                // Refresh list after update
                                searchFavorites(rv, tvEmpty)
                            }
                            dialog.show(parentFragmentManager, "EditFavoriteDialog")
                        }
                    )
                    rv.adapter = adapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
    }
}
