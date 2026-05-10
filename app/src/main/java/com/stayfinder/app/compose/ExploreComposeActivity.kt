package com.stayfinder.app.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ─────────────────────────────────────────────────────────
// Activity entry point
// ─────────────────────────────────────────────────────────
class ExploreComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userName = intent.getStringExtra("USER_NAME") ?: "Traveller"
        setContent {
            StayFinderTheme {
                ExploreScreen(
                    userName = userName,
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        fun launch(context: Context, userName: String) {
            context.startActivity(
                Intent(context, ExploreComposeActivity::class.java)
                    .putExtra("USER_NAME", userName)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// Theme
// ─────────────────────────────────────────────────────────
private val Primary = Color(0xFFFF385C)
private val Surface = Color(0xFFF8F8F8)
private val CardBg = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF222222)
private val TextSecondary = Color(0xFF717171)

@Composable
fun StayFinderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            background = Surface,
            surface = CardBg,
            onPrimary = Color.White,
            onBackground = TextPrimary,
        ),
        content = content
    )
}

// ─────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────
data class CategoryItem(val label: String, val icon: ImageVector)
data class PropertyCard(
    val name: String,
    val location: String,
    val price: Double,
    val rating: Double,
    val type: String,
    val badge: String = ""
)

val categories = listOf(
    CategoryItem("All", Icons.Default.Apps),
    CategoryItem("Entire Stay", Icons.Default.Home),
    CategoryItem("Private Room", Icons.Default.Bed),
    CategoryItem("Shared", Icons.Default.People),
    CategoryItem("Villa", Icons.Default.Villa),
    CategoryItem("Beach", Icons.Default.BeachAccess),
    CategoryItem("Mountain", Icons.Default.Terrain),
    CategoryItem("City", Icons.Default.LocationCity),
)

val sampleProperties = listOf(
    PropertyCard("Cozy Karachi Studio", "Karachi, Sindh", 4500.0, 4.8, "Entire Stay", "🔥 Hot"),
    PropertyCard("Lahore Heritage Haveli", "Lahore, Punjab", 7200.0, 4.9, "Entire Stay", "⭐ Top Rated"),
    PropertyCard("Islamabad B&B", "Islamabad, Capital", 3800.0, 4.6, "Private Room"),
    PropertyCard("Murree Mountain Retreat", "Murree, Punjab", 6500.0, 4.7, "Villa", "❄️ New"),
    PropertyCard("Hunza Valley Guesthouse", "Hunza, Gilgit-Baltistan", 5200.0, 4.9, "Shared"),
    PropertyCard("Peshawar Boutique Hotel", "Peshawar, KPK", 4100.0, 4.4, "Private Room"),
    PropertyCard("Swat Riverside Cottage", "Swat, KPK", 5800.0, 4.8, "Entire Stay", "🌊 Popular"),
    PropertyCard("Quetta Desert Lodge", "Quetta, Balochistan", 3500.0, 4.3, "Shared"),
)

// ─────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(userName: String, onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(selectedCategory, searchQuery) {
        sampleProperties.filter { property ->
            val matchesCategory = selectedCategory == "All" || property.type == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    property.name.contains(searchQuery, ignoreCase = true) ||
                    property.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = getGreeting(userName),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Explore Stays",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Search Bar ──────────────────────────────────
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            // ── Hero Banner ─────────────────────────────────
            item { HeroBanner() }

            // ── Category Chips ──────────────────────────────
            item {
                Text(
                    text = "Browse by Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                CategoryRow(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }

            // ── Results header ──────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filtered.size} stays found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    TextButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filter", color = Primary)
                    }
                }
            }

            // ── Property Cards ──────────────────────────────
            if (filtered.isEmpty()) {
                item { EmptyState() }
            } else {
                items(filtered) { property ->
                    PropertyCardItem(property = property)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Components
// ─────────────────────────────────────────────────────────

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Search by name or location…", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF385C), Color(0xFFFC642D))
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "✨ Weekend Deals",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Up to 30% off selected stays",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Explore Deals", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun CategoryRow(
    categories: List<CategoryItem>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { cat ->
            CategoryChip(
                item = cat,
                isSelected = selected == cat.label,
                onClick = { onSelect(cat.label) }
            )
        }
    }
}

@Composable
fun CategoryChip(item: CategoryItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Primary else Color.White,
        animationSpec = tween(200),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(200),
        label = "chip_text"
    )

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = bgColor,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(item.icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
            Text(item.label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PropertyCardItem(property: PropertyCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Placeholder image area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0E0E0),
                            Color(0xFFC8C8C8)
                        )
                    )
                )
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
            // Badge
            if (property.badge.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White
                ) {
                    Text(
                        text = property.badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            // Wishlist heart
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = "Save",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = property.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(property.rating.toString(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(3.dp))
                Text(property.location, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = property.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "PKR ${String.format("%,.0f", property.price)}/night",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("No stays found", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Try a different category or search term.", fontSize = 14.sp, color = TextSecondary)
    }
}

// ─────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────
private fun getGreeting(name: String): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    return "$greeting, $name 👋"
}
