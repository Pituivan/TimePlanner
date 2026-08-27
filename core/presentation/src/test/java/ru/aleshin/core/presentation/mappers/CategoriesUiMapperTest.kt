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

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.aleshin.core.domain.entities.categories.DefaultCategoryType
import ru.aleshin.core.domain.entities.categories.MainCategory
import ru.aleshin.core.presentation.models.categories.MainCategoryUi

class CategoriesUiMapperTest {

    @Test
    fun mapMainCategoryUiToDomainAndBack_preservesCustomIconKey() {
        val model = MainCategoryUi(
            id = 5L,
            customName = "Study",
            customIconKey = "health",
            defaultType = DefaultCategoryType.STUDY,
        )

        val mappedDomain = model.mapToDomain()
        val mappedUi = mappedDomain.mapToUi()

        assertEquals(model, mappedUi)
    }

    @Test
    fun mapMainCategoryDomainToUiAndBack_preservesNullCustomIconKey() {
        val category = MainCategory(
            id = 6L,
            customName = "Rest",
            customIconKey = null,
            default = DefaultCategoryType.REST,
        )

        val mappedUi = category.mapToUi()
        val mappedDomain = mappedUi.mapToDomain()

        assertEquals(category, mappedDomain)
    }
}
