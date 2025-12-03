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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

@Composable
fun WorldClockScreen() {
    val viewModel: ClockViewModel = hiltViewModel()

    val selectedWorldClocks by viewModel.selectedWorldClocks.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    var showZoneSearch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "World Clock",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showZoneSearch = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add City",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
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
    }

    if (showZoneSearch) {
        ZoneSearchSheet(
            availableZones = viewModel.availableZones,
            onZoneSelected = {
                viewModel.addWorldClock(it)
                showZoneSearch = false
            },
            onDismiss = { showZoneSearch = false }
        )
    }
}

@Composable
fun WorldClockItem(
    data: WorldClockData,
    is24HourFormat: Boolean,
    onDelete: () -> Unit
) {
    val pattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
    val formatter = DateTimeFormatter.ofPattern(pattern)

    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = data.city,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "GMT${data.offset}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = data.time.format(formatter),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        enableDismissFromStartToEnd = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneSearchSheet(
    availableZones: List<String>,
    onZoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredZones = remember(searchQuery) {
        if (searchQuery.isEmpty()) availableZones
        else availableZones.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, null)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp)
            ) {
                items(filteredZones.size) { index ->
                    val zone = filteredZones[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onZoneSelected(zone) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = zone.split("/").last().replace("_", " "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
