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
package ru.aleshin.features.editor.impl.presentation.ui.categories.store

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.aleshin.core.domain.entities.categories.MainCategory
import ru.aleshin.core.domain.entities.categories.MainCategoryDetails
import ru.aleshin.core.domain.entities.categories.SubCategory
import ru.aleshin.core.utils.architecture.store.work.ActionResult
import ru.aleshin.core.utils.functional.DomainResult
import ru.aleshin.core.utils.functional.Either
import ru.aleshin.core.utils.functional.FlowDomainResult
import ru.aleshin.core.utils.functional.UnitDomainResult
import ru.aleshin.features.editor.impl.domain.entites.EditorFailures
import ru.aleshin.features.editor.impl.domain.interactors.MainCategoriesInteractor
import ru.aleshin.features.editor.impl.domain.interactors.SubCategoriesInteractor
import ru.aleshin.features.editor.impl.presentation.ui.categories.contract.CategoriesAction

class CategoriesWorkProcessorTest {

    @Test
    fun addMainCategory_propagatesCustomIconKeyToSavedAndSelectedCategory() = runBlocking {
        val categoriesInteractor = RecordingMainCategoriesInteractor(addedCategoryId = 77L)
        val processor = CategoriesWorkProcessor.Base(
            categoriesInteractor = categoriesInteractor,
            subCategoriesInteractor = UnusedSubCategoriesInteractor(),
        )

        val result = processor.work(
            CategoriesWorkCommand.AddMainCategory(name = "Learning", customIconKey = "study"),
        ).first()

        val action = (result as ActionResult).action as CategoriesAction.ChangeMainCategory
        assertEquals("study", categoriesInteractor.lastSavedCategory?.customIconKey)
        assertEquals("study", action.category.customIconKey)
        assertEquals(77L, action.category.id)
    }
}

private class RecordingMainCategoriesInteractor(
    private val addedCategoryId: Long,
) : MainCategoriesInteractor {

    var lastSavedCategory: MainCategory? = null

    override suspend fun fetchCategories(): FlowDomainResult<EditorFailures, List<MainCategoryDetails>> = unused()

    override suspend fun addOrUpdateMainCategory(mainCategory: MainCategory): DomainResult<EditorFailures, Long> {
        lastSavedCategory = mainCategory
        return Either.Right(addedCategoryId)
    }

    override suspend fun deleteMainCategoryById(mainCategoryId: Long): UnitDomainResult<EditorFailures> = unused()

    override suspend fun restoreDefaultCategories(): UnitDomainResult<EditorFailures> = unused()
}

private class UnusedSubCategoriesInteractor : SubCategoriesInteractor {

    override suspend fun addOrUpdateSubCategory(subCategory: SubCategory): DomainResult<EditorFailures, Long> = unused()

    override suspend fun deleteSubCategoryById(subCategoryId: Long): UnitDomainResult<EditorFailures> = unused()
}

private fun unused(): Nothing = error("Unused dependency")
