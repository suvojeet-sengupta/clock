package com.suvojeet.clock.ui.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

import androidx.navigation.NavController
import com.suvojeet.clock.ui.navigation.Screen
import com.suvojeet.clock.util.HapticFeedback

@Composable
fun WorldClockScreen(navController: NavController) {
    val viewModel: ClockViewModel = hiltViewModel()

    val selectedWorldClocks by viewModel.selectedWorldClocks.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    Scaffold(
        containerColor = Color.Black // Dark background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Digital Clock Header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val pattern = if (is24HourFormat) "HH:mm" else "h:mm a"
                val formatter = DateTimeFormatter.ofPattern(pattern)
                Text(
                    text = currentTime.format(formatter),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "World Clock",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(selectedWorldClocks.size) { index ->
                    val clock = selectedWorldClocks[index]
                    WorldClockItem(
                        data = clock,
                        is24HourFormat = is24HourFormat,
                        onDelete = { viewModel.removeWorldClock(clock.zoneId) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add New Locations Section
            Text(
                text = "Add New Locations",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddLocationCard(
                    text = "Add City",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate(Screen.AddLocation) },
                    modifier = Modifier.weight(1f)
                )
                AddLocationCard(
                    text = "London",
                    onClick = { viewModel.addWorldClock("Europe/London") },
                    modifier = Modifier.weight(1f)
                )
                AddLocationCard(
                    text = "Tokyo",
                    onClick = { viewModel.addWorldClock("Asia/Tokyo") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddLocationCard(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)) // Dark gray
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(text, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockItem(
    data: WorldClockData,
    is24HourFormat: Boolean,
    onDelete: () -> Unit
) {
    val pattern = if (is24HourFormat) "HH:mm" else "h:mm a"
    val formatter = DateTimeFormatter.ofPattern(pattern)
    val view = LocalView.current
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()
    
    // Format time difference
    val timeDiffText = when {
        data.timeDifferenceHours == 0 -> "Same time"
        data.timeDifferenceHours > 0 -> "+${data.timeDifferenceHours}h"
        else -> "${data.timeDifferenceHours}h"
    }
    val timeDiffColor = when {
        data.timeDifferenceHours == 0 -> Color.Gray
        data.timeDifferenceHours > 0 -> Color(0xFF81C784) // Green for ahead
        else -> Color(0xFFE57373) // Red for behind
    }
    
    // Handle swipe to delete
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            showDeleteConfirmation = true
            dismissState.reset()
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove World Clock") },
            text = { 
                Text("Are you sure you want to remove ${data.zoneId.split("/").last().replace("_", " ")}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticFeedback.performConfirm(view)
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Red)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)), // Dark gray
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = data.zoneId.split("/").last().replace("_", " "),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Today, ${data.offset}HRS",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "• $timeDiffText",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = timeDiffColor
                            )
                        }
                    }
                    Text(
                        text = data.time.format(formatter),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    )
}
