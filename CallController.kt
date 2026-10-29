package com.example.jarvis.contacts

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CallLog
import androidx.core.content.ContextCompat

sealed class CallInitiationResult {
    data class DialOpened(val contactName: String, val number: String) : CallInitiationResult()
    data class DirectCalling(val contactName: String, val number: String) : CallInitiationResult()
    data class MultipleFound(val contacts: List<ContactItem>) : CallInitiationResult()
    data class NotFound(val name: String) : CallInitiationResult()
    data class PermissionRequired(val message: String) : CallInitiationResult()
}

class CallController(
    private val context: Context,
    private val contactsRepository: ContactsRepository
) {

    fun initiateCall(targetName: String, preferDirectCall: Boolean = false): CallInitiationResult {
        if (!contactsRepository.hasContactsPermission()) {
            return CallInitiationResult.PermissionRequired("Contacts permission is needed to find ${targetName}.")
        }

        val matches = contactsRepository.searchContacts(targetName)
        if (matches.isEmpty()) {
            return CallInitiationResult.NotFound(targetName)
        }

        if (matches.size > 1) {
            // Check if exact match
            val exact = matches.firstOrNull { it.name.equals(targetName, ignoreCase = true) }
            if (exact == null) {
                return CallInitiationResult.MultipleFound(matches)
            }
            return dialOrCall(exact, preferDirectCall)
        }

        return dialOrCall(matches.first(), preferDirectCall)
    }

    private fun dialOrCall(contact: ContactItem, preferDirectCall: Boolean): CallInitiationResult {
        val hasCallPhone = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val uri = Uri.parse("tel:${contact.phoneNumber.replace(" ", "")}")

        return if (preferDirectCall && hasCallPhone) {
            val intent = Intent(Intent.ACTION_CALL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CallInitiationResult.DirectCalling(contact.name, contact.phoneNumber)
        } else {
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CallInitiationResult.DialOpened(contact.name, contact.phoneNumber)
        }
    }

    fun openCallHistory(): String {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Opening call history."
        } catch (e: Exception) {
            "Unable to open call history: ${e.message}"
        }
    }
}
