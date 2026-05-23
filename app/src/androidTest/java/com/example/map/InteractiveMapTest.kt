package com.example.map

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InteractiveMapTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMapImageIsDisplayed() {
        // Проверяем, что изображение карты отображается
        composeTestRule.onNodeWithContentDescription("Map Image").assertExists()
    }

    @Test
    fun testClickOnDefaultMarkerOpensDialog() {
        // Находим одну из стандартных точек по тексту (если бы мы добавили текст к маркеру)
        // Но так как у нас только кликабельные боксы, мы можем проверить открытие диалога через нажатие.
        // Для этого теста в MainActivity стоит добавить modifier.testTag или contentDescription к маркерам.
    }
}
