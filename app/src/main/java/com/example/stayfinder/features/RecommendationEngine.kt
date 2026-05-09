package com.example.stayfinder.features

import com.example.stayfinder.Property
import kotlin.collections.map

/**
 * Simple behavior-based suggestions: boost listings that match property types
 * the user already favorited.
 */
object RecommendationEngine {

    fun recommendationsForUser(
        allListings: List<Property>,
        favoriteListings: List<Property>,
        limit: Int = 8
    ): List<Property> {
        val favIds = favoriteListings.map { it.id }.toSet()
        val typeWeights = favoriteListings
            .groupingBy { it.propertyType.lowercase() }
            .eachCount()
        if (typeWeights.isEmpty()) {
            return allListings.filter { it.id !in favIds }.take(limit)
        }
        return allListings
            .filter { it.id !in favIds }
            .map { listing ->
                val typeScore = typeWeights[listing.propertyType.lowercase()] ?: 0
                val tagOverlap = listing.tags.count { tag ->
                    favoriteListings.any { f ->
                        f.tags.any { ft -> ft.equals(tag, ignoreCase = true) }
                    }
                }
                val ratingScore = listing.rating.toDoubleOrNull()?.toInt() ?: 0
                val score = typeScore * 3 + tagOverlap + ratingScore

                listing to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }
}
