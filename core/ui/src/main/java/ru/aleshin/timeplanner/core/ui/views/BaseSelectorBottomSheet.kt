/*
 * Copyright 2026 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.timeplanner.core.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.aleshin.timeplanner.core.ui.theme.TimePlannerRes
import ru.aleshin.timeplanner.core.ui.theme.material.full

/**
 * @author Stanislav Aleshin on 04.09.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> BaseSelectorBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    confirmEnabled: Boolean = true,
    selected: T?,
    items: List<T>,
    itemKeys: ((T) -> Any)? = null,
    header: String,
    title: String?,
    itemView: @Composable LazyItemScope.(Int, T) -> Unit,
    notSelectedItem: @Composable (LazyItemScope.() -> Unit)? = null,
    addItemView: @Composable (LazyItemScope.() -> Unit)? = null,
    searchBar: @Composable (() -> Unit)? = null,
    filters: @Composable (RowScope.() -> Unit)? = null,
    reorderEnabled: Boolean = false,
    isItemReorderable: ((Int, T) -> Boolean)? = null,
    onItemsReordered: ((List<T>) -> Unit)? = null,
    itemsListState: LazyListState = rememberLazyListState(),
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    onDismissRequest: () -> Unit,
    onConfirm: (T?) -> Unit,
) {
    var reorderedItems by remember(items) { mutableStateOf(items) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggedItemOffset by remember { mutableStateOf(0f) }
    var draggedPointerViewportY by remember { mutableStateOf<Float?>(null) }
    var dragPointerDeltaY by remember { mutableStateOf(0f) }
    var dragOffsetAnchorY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val autoScrollEdgeDistance = with(density) { 56.dp.toPx() }
    val autoScrollMaxStep = with(density) { 18.dp.toPx() }
    val autoScrollSafeInset = with(density) { 16.dp.toPx() }

    fun resetDragState() {
        draggedIndex = null
        draggedItemOffset = 0f
        draggedPointerViewportY = null
        dragPointerDeltaY = 0f
        dragOffsetAnchorY = 0f
    }

    fun moveItem(from: Int, to: Int) {
        if (from == to || from !in reorderedItems.indices || to !in reorderedItems.indices) return
        val mutableItems = reorderedItems.toMutableList()
        val item = mutableItems.removeAt(from)
        mutableItems.add(to, item)
        reorderedItems = mutableItems
        onItemsReordered?.invoke(mutableItems)
    }

    fun consumeDragThresholds() {
        val currentIndex = draggedIndex ?: return
        val firstReorderableListIndex = if (notSelectedItem != null) 1 else 0
        val currentListIndex = currentIndex + firstReorderableListIndex
        val currentItemSize = itemsListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentListIndex }
            ?.size
            ?.toFloat()
            ?: return
        val threshold = currentItemSize / 2f
        var updatedIndex = currentIndex
        while (draggedItemOffset > threshold && updatedIndex < reorderedItems.lastIndex) {
            val targetIndex = updatedIndex + 1
            val targetItem = reorderedItems[targetIndex]
            val canMoveToTarget = isItemReorderable?.invoke(targetIndex, targetItem) ?: true
            if (!canMoveToTarget) {
                draggedItemOffset = threshold
                break
            }
            moveItem(updatedIndex, targetIndex)
            updatedIndex += 1
            draggedItemOffset -= currentItemSize
        }
        while (draggedItemOffset < -threshold && updatedIndex > 0) {
            val targetIndex = updatedIndex - 1
            val targetItem = reorderedItems[targetIndex]
            val canMoveToTarget = isItemReorderable?.invoke(targetIndex, targetItem) ?: true
            if (!canMoveToTarget) {
                draggedItemOffset = -threshold
                break
            }
            moveItem(updatedIndex, targetIndex)
            updatedIndex -= 1
            draggedItemOffset += currentItemSize
        }
        if (updatedIndex != currentIndex) {
            draggedIndex = updatedIndex
        }
    }

    LaunchedEffect(draggedIndex, reorderEnabled, onItemsReordered, isItemReorderable) {
        if (!reorderEnabled || onItemsReordered == null || draggedIndex == null) return@LaunchedEffect
        while (isActive && draggedIndex != null) {
            val layoutInfo = itemsListState.layoutInfo
            val pointerY = draggedPointerViewportY
            if (pointerY == null) {
                delay(16)
                continue
            }
            val viewportTop = layoutInfo.viewportStartOffset.toFloat()
            val viewportBottom = layoutInfo.viewportEndOffset.toFloat()
            val safeTop = viewportTop + autoScrollSafeInset
            val safeBottom = viewportBottom - autoScrollSafeInset
            val effectivePointerY = pointerY.coerceIn(safeTop, safeBottom)

            val topDistance = effectivePointerY - viewportTop
            val bottomDistance = viewportBottom - effectivePointerY
            val scrollDelta = when {
                topDistance < autoScrollEdgeDistance -> {
                    val intensity = ((autoScrollEdgeDistance - topDistance) / autoScrollEdgeDistance)
                        .coerceIn(0f, 1f)
                    -(autoScrollMaxStep * intensity)
                }

                bottomDistance < autoScrollEdgeDistance -> {
                    val intensity = ((autoScrollEdgeDistance - bottomDistance) / autoScrollEdgeDistance)
                        .coerceIn(0f, 1f)
                    autoScrollMaxStep * intensity
                }

                else -> 0f
            }

            if (scrollDelta != 0f) {
                val consumed = itemsListState.scrollBy(scrollDelta)
                if (consumed != 0f) {
                    draggedItemOffset += consumed
                    consumeDragThresholds()
                    dragOffsetAnchorY = dragPointerDeltaY - draggedItemOffset
                }
            }
            delay(16)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = containerColor,
        dragHandle = { MediumDragHandle() },
        properties = properties,
    ) {
        Column {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column {
                    Text(
                        text = header,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (title != null) {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                searchBar?.invoke()
                if (filters != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = filters,
                    )
                }
            }
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier
                    .height(350.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    .then(
                        if (reorderEnabled && onItemsReordered != null) {
                            Modifier.pointerInput(reorderEnabled, notSelectedItem) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { startOffset ->
                                        val layoutInfo = itemsListState.layoutInfo
                                        val touchedItemInfo = layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                                            startOffset.y.toInt() in itemInfo.offset..(itemInfo.offset + itemInfo.size)
                                        } ?: return@detectDragGesturesAfterLongPress
                                        val firstReorderableListIndex = if (notSelectedItem != null) 1 else 0
                                        val touchedReorderedIndex = touchedItemInfo.index - firstReorderableListIndex
                                        if (touchedReorderedIndex !in reorderedItems.indices) {
                                            return@detectDragGesturesAfterLongPress
                                        }
                                        val touchedItem = reorderedItems[touchedReorderedIndex]
                                        val canReorderItem = isItemReorderable
                                            ?.invoke(touchedReorderedIndex, touchedItem)
                                            ?: true
                                        if (!canReorderItem) {
                                            return@detectDragGesturesAfterLongPress
                                        }

                                        draggedIndex = touchedReorderedIndex
                                        draggedItemOffset = 0f
                                        dragPointerDeltaY = 0f
                                        dragOffsetAnchorY = 0f
                                        val viewportTop = layoutInfo.viewportStartOffset.toFloat()
                                        val viewportBottom = layoutInfo.viewportEndOffset.toFloat()
                                        val safeTop = viewportTop + autoScrollSafeInset
                                        val safeBottom = viewportBottom - autoScrollSafeInset
                                        draggedPointerViewportY = startOffset.y.coerceIn(safeTop, safeBottom)
                                    },
                                    onDrag = { change, dragAmount ->
                                        if (draggedIndex == null) return@detectDragGesturesAfterLongPress
                                        change.consume()
                                        dragPointerDeltaY += dragAmount.y
                                        val layoutInfo = itemsListState.layoutInfo
                                        val viewportTop = layoutInfo.viewportStartOffset.toFloat()
                                        val viewportBottom = layoutInfo.viewportEndOffset.toFloat()
                                        val safeTop = viewportTop + autoScrollSafeInset
                                        val safeBottom = viewportBottom - autoScrollSafeInset
                                        val nextPointerY = (draggedPointerViewportY ?: change.position.y) + dragAmount.y
                                        draggedPointerViewportY = nextPointerY.coerceIn(safeTop, safeBottom)
                                        draggedItemOffset = dragPointerDeltaY - dragOffsetAnchorY
                                        consumeDragThresholds()
                                        dragOffsetAnchorY = dragPointerDeltaY - draggedItemOffset
                                    },
                                    onDragEnd = { resetDragState() },
                                    onDragCancel = { resetDragState() },
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                state = itemsListState,
                userScrollEnabled = draggedIndex == null,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (notSelectedItem != null) {
                    item(content = notSelectedItem, key = "NotSelectedItem")
                }
                itemsIndexed(
                    items = reorderedItems,
                    key = if (itemKeys != null) { _, item -> itemKeys(item) } else null,
                ) { index, item ->
                    val isDragged = draggedIndex == index
                    Box(
                        modifier = Modifier.graphicsLayer {
                            translationY = if (isDragged) draggedItemOffset else 0f
                            if (isDragged) {
                                scaleX = 1.02f
                                scaleY = 1.02f
                                alpha = 0.96f
                                shadowElevation = 24.dp.toPx()
                            }
                        }
                    ) {
                        itemView(index, item)
                    }
                }
                if (addItemView != null) {
                    item(content = addItemView, key = "AddItem")
                }
            }
            Row(
                modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(text = TimePlannerRes.strings.cancelTitle)
                }
                Button(
                    onClick = { onConfirm(selected) },
                    modifier = Modifier.weight(1f),
                    enabled = confirmEnabled
                ) {
                    Text(text = TimePlannerRes.strings.confirmTitle)
                }
            }

            var isShowedFirstItem by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(true) {
                if (!isShowedFirstItem && selected != null) {
                    val itemIndex = items.indexOf(selected)
                    if (itemIndex != -1) itemsListState.animateScrollToItem(itemIndex)
                }
                isShowedFirstItem = true
            }
        }
    }
}

@Composable
fun MediumDragHandle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = modifier.padding(vertical = 16.dp),
            color = color,
            shape = MaterialTheme.shapes.full,
        ) {
            Box(Modifier.size(width = 32.dp, height = 4.dp))
        }
    }
}