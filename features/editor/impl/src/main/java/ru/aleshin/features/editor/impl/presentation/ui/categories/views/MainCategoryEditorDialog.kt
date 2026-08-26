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
package ru.aleshin.features.editor.impl.presentation.ui.categories.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.aleshin.core.presentation.mappers.fetchMainCategoryIconKeys
import ru.aleshin.core.presentation.mappers.mapToCategoryIconPainter
import ru.aleshin.core.presentation.mappers.mapToIconPainter
import ru.aleshin.core.presentation.mappers.resolveMainCategoryIconKey
import ru.aleshin.timeplanner.core.ui.theme.TimePlannerRes
import ru.aleshin.timeplanner.core.ui.views.CategoryIconMonogram
import ru.aleshin.timeplanner.core.ui.views.CategoryTextMonogram
import ru.aleshin.timeplanner.core.ui.views.DialogButtons
import ru.aleshin.core.utils.functional.Constants
import ru.aleshin.core.presentation.models.categories.MainCategoryUi
import ru.aleshin.features.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 16.04.2023.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MainCategoryEditorDialog(
    modifier: Modifier = Modifier,
    editCategory: MainCategoryUi? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, customIconKey: String?) -> Unit,
) {
    var isError by rememberSaveable { mutableStateOf(false) }
    var isIconPickerOpen by rememberSaveable { mutableStateOf(false) }
    val categoryName = editCategory?.fetchName()
    val textRange = remember { TextRange(categoryName?.length ?: 0) }
    var mainCategoryNameValue by remember {
        mutableStateOf(TextFieldValue(text = categoryName ?: "", selection = textRange))
    }
    var selectedIconKey by rememberSaveable { mutableStateOf(editCategory?.customIconKey) }
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.width(328.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column {
                MainCategoryEditorDialogHeader()
                HorizontalDivider()
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val customIcon = selectedIconKey?.mapToCategoryIconPainter()
                    val defaultIcon = editCategory?.defaultType?.mapToIconPainter()
                    val iconModifier = Modifier.clickable { isIconPickerOpen = true }
                    if (customIcon != null) {
                        CategoryIconMonogram(
                            modifier = iconModifier,
                            icon = customIcon,
                            iconDescription = editCategory?.customName,
                            iconColor = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    } else if (defaultIcon != null) {
                        CategoryIconMonogram(
                            modifier = iconModifier,
                            icon = defaultIcon,
                            iconDescription = editCategory?.customName,
                            iconColor = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    } else {
                        CategoryTextMonogram(
                            modifier = iconModifier,
                            text = mainCategoryNameValue.text.firstOrNull()?.toString() ?: "-",
                            textColor = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }
                    CategoryDialogField(
                        modifier = Modifier.fillMaxWidth(),
                        categoryNameValue = mainCategoryNameValue,
                        isError = isError,
                        onNameChange = { nameValue -> mainCategoryNameValue = nameValue },
                    )
                }
                DialogButtons(
                    confirmTitle = when (editCategory != null) {
                        true -> TimePlannerRes.strings.okConfirmTitle
                        false -> EditorThemeRes.strings.dialogCreateTitle
                    },
                    onConfirmClick = {
                        val text = mainCategoryNameValue.text
                        if (text.isNotEmpty() && text.length < Constants.Text.MAX_LENGTH) {
                            onConfirm(mainCategoryNameValue.text, selectedIconKey)
                        } else {
                            isError = true
                        }
                    },
                    onCancelClick = onDismiss,
                )
            }
        }
    }
    if (isIconPickerOpen) {
        MainCategoryIconPickerDialog(
            defaultIconKey = resolveMainCategoryIconKey(null, editCategory?.defaultType),
            categoryName = mainCategoryNameValue.text,
            selectedIconKey = selectedIconKey,
            onDismiss = { isIconPickerOpen = false },
            onSelect = { key ->
                selectedIconKey = key
                isIconPickerOpen = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainCategoryIconPickerDialog(
    defaultIconKey: String?,
    categoryName: String,
    selectedIconKey: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(328.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column {
                MainCategoryEditorDialogHeader()
                HorizontalDivider()
                LazyVerticalGrid(
                    modifier = Modifier.padding(16.dp),
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Surface(
                            color = if (selectedIconKey == null) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            shape = MaterialTheme.shapes.small,
                            onClick = { onSelect(null) },
                        ) {
                            val defaultIcon = defaultIconKey?.mapToCategoryIconPainter()
                            if (defaultIcon != null) {
                                CategoryIconMonogram(
                                    modifier = Modifier.padding(8.dp),
                                    icon = defaultIcon,
                                    iconDescription = categoryName,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    backgroundColor = Color.Transparent,
                                )
                            } else {
                                CategoryTextMonogram(
                                    modifier = Modifier.padding(8.dp),
                                    text = categoryName.firstOrNull()?.toString() ?: "-",
                                    textColor = MaterialTheme.colorScheme.primary,
                                    backgroundColor = Color.Transparent,
                                )
                            }
                        }
                    }
                    items(fetchMainCategoryIconKeys()) { iconKey ->
                        val icon = iconKey.mapToCategoryIconPainter()
                        if (icon != null) {
                            Surface(
                                color = if (selectedIconKey == iconKey) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                shape = MaterialTheme.shapes.small,
                                onClick = { onSelect(iconKey) },
                            ) {
                                CategoryIconMonogram(
                                    modifier = Modifier.padding(8.dp),
                                    icon = icon,
                                    iconDescription = null,
                                    iconColor = MaterialTheme.colorScheme.primary,
                                    backgroundColor = Color.Transparent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MainCategoryEditorDialogHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(top = 24.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = EditorThemeRes.strings.mainCategoryChooserTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

