package com.example.keyboard.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.KeyAction
import com.example.keyboard.emoji.EmojiCategory
import com.example.keyboard.emoji.EmojiManager
import com.example.keyboard.ui.components.RepeatingBackspaceKey
import com.example.keyboard.ui.components.SpecialKeyButton

/**
 * Emoji Keyboard View with automatic device glyph compatibility filtering.
 */
@Composable
fun EmojiKeyboardLayout(
    imeActionLabel: String?,
    imeActionIconType: String,
    onAction: (KeyAction) -> Unit,
    onTriggerHaptic: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) } // 0: Recientes, 1..6: Categories
    val categories = EmojiCategory.values()

    val totalSupported = remember { EmojiManager.getTotalSupportedCount() }

    val currentEmojis = remember(selectedCategoryIndex) {
        if (selectedCategoryIndex == 0) {
            EmojiManager.getRecentEmojis()
        } else {
            val cat = categories[selectedCategoryIndex - 1]
            EmojiManager.getSupportedEmojis(cat)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Categories & Compatibility Badge Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Chips (Recientes, Caras, Animales, Comida, etc.)
                    CategoryChip(
                        icon = "🕒",
                        isSelected = selectedCategoryIndex == 0,
                        onClick = { selectedCategoryIndex = 0 }
                    )
                    categories.forEachIndexed { idx, cat ->
                        CategoryChip(
                            icon = cat.icon,
                            isSelected = selectedCategoryIndex == idx + 1,
                            onClick = { selectedCategoryIndex = idx + 1 }
                        )
                    }
                }

                // Supported Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$totalSupported compatibles",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }
        }

        // Emoji Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            if (currentEmojis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sin emojis recientes aún",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(currentEmojis, key = { it }) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onAction(KeyAction.InsertEmoji(emoji))
                                }
                                .testTag("emoji_item_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom navigation row for Emoji keyboard (ABC, ?123, Space, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpecialKeyButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = Color(0xFFE2E8F0),
                onClick = { onAction(KeyAction.SwitchToLetters) }
            ) {
                Text(
                    text = "ABC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
            }

            SpecialKeyButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = Color(0xFFE2E8F0),
                onClick = { onAction(KeyAction.SwitchToSymbols) }
            ) {
                Text(
                    text = "?123",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
            }

            SpecialKeyButton(
                modifier = Modifier.weight(3.5f),
                backgroundColor = Color.White,
                onClick = { onAction(KeyAction.Space) }
            ) {
                Text(
                    text = "espacio",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            RepeatingBackspaceKey(
                modifier = Modifier.weight(1.35f),
                onDelete = { onAction(KeyAction.Backspace) },
                onTriggerHaptic = onTriggerHaptic
            )
        }
    }
}

@Composable
fun CategoryChip(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 16.sp)
    }
}
