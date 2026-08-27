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
package ru.aleshin.core.data.mappers.categories

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.aleshin.core.data.models.categories.MainCategoryEntity
import ru.aleshin.core.domain.entities.categories.DefaultCategoryType
import ru.aleshin.core.domain.entities.categories.MainCategory

class CategoriesDataMappersTest {

    @Test
    fun mapMainCategoryToDataAndBack_preservesCustomIconKey() {
        val category = MainCategory(
            id = 17L,
            orderPosition = 5L,
            customName = "My category",
            customIconKey = "study",
            default = DefaultCategoryType.WORK,
        )

        val mappedData = category.mapToData()
        val mappedDomain = mappedData.mapToDomain()

        assertEquals(category, mappedDomain)
    }

    @Test
    fun mapMainCategoryEntityToDomainAndBack_preservesNullableCustomIconKey() {
        val entity = MainCategoryEntity(
            id = 11L,
            orderPosition = 3L,
            customName = "No icon",
            customIconKey = null,
            defaultType = DefaultCategoryType.REST,
        )

        val mappedDomain = entity.mapToDomain()
        val mappedData = mappedDomain.mapToData()

        assertEquals(entity, mappedData)
    }
}
