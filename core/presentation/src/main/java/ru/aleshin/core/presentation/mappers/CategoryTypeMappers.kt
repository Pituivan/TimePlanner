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
package ru.aleshin.core.presentation.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import ru.aleshin.core.domain.entities.categories.DefaultCategoryType
import ru.aleshin.timeplanner.core.ui.theme.TimePlannerRes
import ru.aleshin.timeplanner.core.ui.theme.tokens.TimePlannerIcons
import ru.aleshin.timeplanner.core.ui.theme.tokens.TimePlannerStrings

/**
 * @author Stanislav Aleshin on 24.02.2023.
 */
fun DefaultCategoryType.mapToIcon(icons: TimePlannerIcons): Int = when (this) {
    DefaultCategoryType.WORK -> icons.categoryWorkIcon
    DefaultCategoryType.REST -> icons.categoryRestIcon
    DefaultCategoryType.AFFAIRS -> icons.categoryAffairsIcon
    DefaultCategoryType.TRANSPORT -> icons.categoryTransportIcon
    DefaultCategoryType.STUDY -> icons.categoryStudyIcon
    DefaultCategoryType.EAT -> icons.categoryEatIcon
    DefaultCategoryType.ENTERTAINMENTS -> icons.categoryEntertainmentsIcon
    DefaultCategoryType.SPORT -> icons.categorySportIcon
    DefaultCategoryType.SLEEP -> icons.categorySleepIcon
    DefaultCategoryType.CULTURE -> icons.categoryCultureIcon
    DefaultCategoryType.OTHER -> icons.categoryOtherIcon
    DefaultCategoryType.EMPTY -> icons.categoryEmptyIcon
    DefaultCategoryType.HYGIENE -> icons.categoryHygiene
    DefaultCategoryType.HEALTH -> icons.categoryHealth
    DefaultCategoryType.SHOPPING -> icons.categoryShopping
}

fun DefaultCategoryType.mapToString(strings: TimePlannerStrings): String = when (this) {
    DefaultCategoryType.WORK -> strings.categoryWorkTitle
    DefaultCategoryType.REST -> strings.categoryRestTitle
    DefaultCategoryType.AFFAIRS -> strings.categoryChoresTitle
    DefaultCategoryType.TRANSPORT -> strings.categoryTransportTitle
    DefaultCategoryType.STUDY -> strings.categoryStudyTitle
    DefaultCategoryType.EAT -> strings.categoryEatTitle
    DefaultCategoryType.ENTERTAINMENTS -> strings.categoryEntertainmentsTitle
    DefaultCategoryType.SPORT -> strings.categorySportTitle
    DefaultCategoryType.SLEEP -> strings.categorySleepTitle
    DefaultCategoryType.CULTURE -> strings.categoryCultureTitle
    DefaultCategoryType.OTHER -> strings.categoryOtherTitle
    DefaultCategoryType.EMPTY -> strings.categoryEmptyTitle
    DefaultCategoryType.HYGIENE -> strings.categoryHygieneTitle
    DefaultCategoryType.HEALTH -> strings.categoryHealthTitle
    DefaultCategoryType.SHOPPING -> strings.categoryShoppingTitle
}

@Composable
fun DefaultCategoryType.mapToName() = mapToString(TimePlannerRes.strings)

@Composable
fun DefaultCategoryType.mapToIconPainter() = painterResource(id = mapToIcon(TimePlannerRes.icons))

fun String.mapToCategoryIcon(icons: TimePlannerIcons): Int? = when (this) {
    "work" -> icons.categoryWorkIcon
    "rest" -> icons.categoryRestIcon
    "affairs" -> icons.categoryAffairsIcon
    "transport" -> icons.categoryTransportIcon
    "study" -> icons.categoryStudyIcon
    "eat" -> icons.categoryEatIcon
    "entertainments" -> icons.categoryEntertainmentsIcon
    "sport" -> icons.categorySportIcon
    "sleep" -> icons.categorySleepIcon
    "culture" -> icons.categoryCultureIcon
    "other" -> icons.categoryOtherIcon
    "hygiene" -> icons.categoryHygiene
    "health" -> icons.categoryHealth
    "shopping" -> icons.categoryShopping
    "projects" -> icons.categoryCustomIconRocket
    "pets" -> icons.categoryCustomIconPets
    "call" -> icons.categoryCustomIconCall
    "meditate" -> icons.categoryCustomIconMeditate
    "people" -> icons.categoryCustomIconGroup
    "code" -> icons.categoryCustomIconCode
    "growth" -> icons.categoryCustomIconGrowth
    "event" -> icons.categoryCustomIconEvent
    "read" -> icons.categoryCustomIconBook
    "person" -> icons.categoryCustomIconUser
    "hiking" -> icons.categoryCustomIconHiking
    "art" -> icons.categoryCustomIconArt
    "music" -> icons.categoryCustomIconMusic
    else -> null
}

fun resolveMainCategoryIconKey(
    customIconKey: String?,
    defaultType: DefaultCategoryType?,
): String? = customIconKey ?: defaultType?.takeIf { it != DefaultCategoryType.EMPTY }?.name?.lowercase()

@Composable
fun resolveMainCategoryIconPainter(
    customIconKey: String?,
    defaultType: DefaultCategoryType?,
): Painter? = resolveMainCategoryIconKey(customIconKey, defaultType)?.mapToCategoryIconPainter()

@Composable
fun String.mapToCategoryIconPainter() = mapToCategoryIcon(TimePlannerRes.icons)?.let { painterResource(id = it) }

fun fetchMainCategoryIconKeys() = listOf(
    "work",
    "rest",
    "affairs",
    "transport",
    "study",
    "eat",
    "entertainments",
    "sport",
    "sleep",
    "culture",
    "other",
    "hygiene",
    "health",
    "shopping",
    "projects",
    "pets",
    "call",
    "meditate",
    "people",
    "code",
    "growth",
    "event",
    "read",
    "person",
    "hiking",
    "art",
    "music",
)
