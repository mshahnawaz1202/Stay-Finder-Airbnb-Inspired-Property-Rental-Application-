package com.example.stayfinder

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class NotificationHistoryActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("notifications", Context.MODE_PRIVATE)
        val historyStr = prefs.getString("history", "") ?: ""
        
        val notifications = if (historyStr.isNotEmpty()) {
            historyStr.split("|end|").mapNotNull {
                val parts = it.split("|split|")
                if (parts.size == 3) NotificationItem(parts[0], parts[1], parts[2]) else null
            }
        } else {
            emptyList()
        }

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Notification History") }
                        )
                    }
                ) { padding ->
                    NotificationHistoryScreen(
                        notifications = notifications,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

data class NotificationItem(val title: String, val message: String, val date: String)

@Composable
fun NotificationHistoryScreen(notifications: List<NotificationItem>, modifier: Modifier = Modifier) {
    if (notifications.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No past notifications")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = notif.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = notif.message)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = notif.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
