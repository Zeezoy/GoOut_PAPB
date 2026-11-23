package com.example.goout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.goout.fakes.FakeAuthRepository
import com.example.goout.fakes.TestData
import com.example.goout.feature.auth.AuthViewModel
import com.example.goout.feature.auth.LoginScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the login screen: validation and guest-mode entry. */
@RunWith(AndroidJUnit4::class)
class AuthUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun launchLogin(
        auth: FakeAuthRepository = FakeAuthRepository(),
        onSuccess: () -> Unit = {},
        onGuest: () -> Unit = {},
    ) {
        composeRule.setContent {
            LoginScreen(
                onSuccess = onSuccess,
                onRegister = {},
                onGuest = onGuest,
                viewModel = AuthViewModel(auth),
            )
        }
    }

    @Test
    fun login_withEmptyFields_showsValidationError() {
        launchLogin()
        composeRule.onNodeWithText("Masuk").performClick()
        composeRule.onNodeWithText("Email dan kata sandi wajib diisi.").assertIsDisplayed()
    }

    @Test
    fun login_withOnlyEmail_showsValidationError() {
        launchLogin()
        composeRule.onNodeWithText("Email").performTextInput("a@b.com")
        composeRule.onNodeWithText("Masuk").performClick()
        composeRule.onNodeWithText("Email dan kata sandi wajib diisi.").assertIsDisplayed()
    }

    @Test
    fun login_withValidCredentials_trimsEmailAndNavigates() {
        val auth = FakeAuthRepository().apply { loginResult = Result.success(TestData.user) }
        var navigated = false
        launchLogin(auth = auth, onSuccess = { navigated = true })

        composeRule.onNodeWithText("Email").performTextInput(" penguji@example.com ")
        composeRule.onNodeWithText("Kata sandi").performTextInput("rahasia123")
        composeRule.onNodeWithText("Masuk").performClick()

        composeRule.waitUntil(5_000) { navigated }
        assertEquals("penguji@example.com", auth.lastLogin?.first)
    }

    @Test
    fun login_failure_showsRepositoryError() {
        val auth = FakeAuthRepository().apply { loginResult = Result.failure(Exception("Kata sandi salah.")) }
        launchLogin(auth = auth)

        composeRule.onNodeWithText("Email").performTextInput("a@b.com")
        composeRule.onNodeWithText("Kata sandi").performTextInput("salah")
        composeRule.onNodeWithText("Masuk").performClick()

        composeRule.onNodeWithText("Kata sandi salah.").assertIsDisplayed()
    }

    @Test
    fun guestMode_tappingGuestButton_entersGuestFlow() {
        var guest = false
        launchLogin(onGuest = { guest = true })
        composeRule.onNodeWithText("Lanjut sebagai tamu (hanya lihat peta)").performClick()
        assertTrue(guest)
    }
}
