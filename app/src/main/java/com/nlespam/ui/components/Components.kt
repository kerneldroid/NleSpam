package com.nlespam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nlespam.models.AdvertisementSet
import com.nlespam.ui.theme.CookieShape
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SpamCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    deviceCount: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isActive) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "containerColor"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    // Apply rotation to the entire bounds *before* clipping, so the shape physically spins
                    .graphicsLayer { rotationZ = rotationAngle }
                    // Custom 9-sided cookie shape
                    .clip(CookieShape)
                    .background(themeColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier
                        .size(28.dp)
                        // Counter-rotate the icon itself so it stays upright
                        .graphicsLayer { rotationZ = -rotationAngle },
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isActive) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(themeColor.copy(alpha = pulseAlpha))
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = themeColor.copy(alpha = 0.2f),
            ) {
                Text(
                    text = "$deviceCount",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun DeviceToggleItem(
    adSet: AdvertisementSet,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 24.dp
    val connectionRadius = 4.dp

    val shape = when {
        isFirst && isLast -> androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
        isFirst -> androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = connectionRadius,
            bottomEnd = connectionRadius
        )
        isLast -> androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = connectionRadius,
            topEnd = connectionRadius,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )
        else -> androidx.compose.foundation.shape.RoundedCornerShape(connectionRadius)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        onClick = { onToggle(index) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = adSet.isSelected,
                onCheckedChange = { onToggle(index) },
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = adSet.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Text(
                    text = adSet.target.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpamControlBar(
    isRunning: Boolean,
    packetsFlow: StateFlow<Long>,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, "Select All")
                }
                IconButton(onClick = onDeselectAll) {
                    Icon(Icons.Default.Deselect, "Deselect All")
                }
            }

            if (isRunning) {
                val packetsSent by packetsFlow.collectAsState()
                Text(
                    text = "$packetsSent pkts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // M3 Expressive ButtonShapes squish button:
            // shape morphs pill → slightly rounded rect on press (squish effect)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val squishScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else if (isRunning) 1.04f else 1f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "squish",
            )

            Button(
                onClick = if (isRunning) onStop else onStart,
                modifier = Modifier.scale(squishScale),
                interactionSource = interactionSource,
                shapes = ButtonDefaults.shapes(),  // M3 Expressive: pill → squircle on press
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(if (isRunning) "STOP" else "START", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeviceListWithControls(
    sets: List<AdvertisementSet>,
    isRunning: Boolean,
    packetsFlow: StateFlow<Long>,
    onToggle: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isControlBarExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredData = remember(sets, searchQuery) {
        if (searchQuery.isBlank()) {
            sets to sets.indices.toList()
        } else {
            val filtered = sets.filter { it.title.contains(searchQuery, ignoreCase = true) }
            val indices = sets.indices.filter { sets[it].title.contains(searchQuery, ignoreCase = true) }
            filtered to indices
        }
    }
    val filteredSets = filteredData.first
    val filteredIndices = filteredData.second

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar + expand/collapse button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search devices…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = {
                    onExpandChange(!isControlBarExpanded)
                },
            ) {
                Icon(
                    imageVector = if (isControlBarExpanded) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = if (isControlBarExpanded) "Collapse controls" else "Expand controls",
                )
            }
        }

        // Control bar (collapsible)
        androidx.compose.animation.AnimatedVisibility(visible = isControlBarExpanded) {
            SpamControlBar(
                isRunning = isRunning,
                packetsFlow = packetsFlow,
                onStart = onStart,
                onStop = onStop,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(
                items = filteredSets,
                key = { _, adSet -> adSet.title + adSet.type.name + (adSet.manufacturerData?.manufacturerId ?: 0) }
            ) { localIdx, adSet ->
                val realIndex = filteredIndices[localIdx]
                val isFirst = localIdx == 0
                val isLast = localIdx == filteredSets.lastIndex
                
                DeviceToggleItem(
                    adSet = adSet,
                    index = realIndex,
                    isFirst = isFirst,
                    isLast = isLast,
                    onToggle = onToggle,
                )
            }
        }
    }
}
