package com.example.stayfinder.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.stayfinder.R
import com.example.stayfinder.features.offline.OfflineFavoriteMirror
import com.example.stayfinder.firebase.FavoriteFirestoreItem
import com.example.stayfinder.firebase.FirestoreFavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesComposeRoute() {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val repo = remember { FirestoreFavoritesRepository() }
    var items by remember { mutableStateOf<List<FavoriteFirestoreItem>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    DisposableEffect(uid) {
        if (uid == null) {
            items = emptyList()
            return@DisposableEffect onDispose { }
        }
        val reg = repo.listenUserFavorites(uid) { list ->
            items = list
            OfflineFavoriteMirror.persist(context, list.map { it.listingId })
        }
        onDispose { reg.remove() }
    }

    val filtered = remember(items, query) {
        if (query.isBlank()) items
        else items.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Favorites") })
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search saved stays") },
            singleLine = true
        )
        if (uid == null) {
            Text(
                "Sign in to sync favorites from Firestore.",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (filtered.isEmpty()) {
            Text(
                "No favorites yet. Heart a listing to see it here.",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.listingId }) { item ->
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            AsyncImage(
                                model = item.imageUrl.takeIf { it.isNotBlank() },
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_home),
                                error = painterResource(R.drawable.ic_home)
                            )
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${item.location} • ${item.price} • ★ ${item.rating}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        repo.removeFavorite(uid!!, item.listingId)
                                    }
                                }
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
