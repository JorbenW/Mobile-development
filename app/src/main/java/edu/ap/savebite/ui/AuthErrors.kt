package edu.ap.savebite.ui

import androidx.annotation.StringRes
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.ap.savebite.R

/** Turns a Firebase error into a clear message for the user (A3). */
@StringRes
fun authErrorMessage(e: Exception, @StringRes fallback: Int): Int = when {
    e is FirebaseNetworkException -> R.string.error_no_internet
    e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
        R.string.error_no_internet
    e is FirebaseAuthUserCollisionException -> R.string.error_email_in_use
    // Must come before InvalidCredentials, it is a subclass of it
    e is FirebaseAuthWeakPasswordException -> R.string.error_password_short
    e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_INVALID_EMAIL" ->
        R.string.error_email_invalid
    // Firebase gives the same error for a wrong password and an unknown e-mail,
    // so nobody can find out which e-mails have an account
    e is FirebaseAuthInvalidCredentialsException || e is FirebaseAuthInvalidUserException ->
        R.string.error_wrong_credentials
    else -> fallback
}
