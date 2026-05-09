package com.example.stayfinder

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFavoriteDialog(
    private val favoriteId: Long,
    private val currentNote: String,
    private val onNoteUpdated: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input = EditText(requireContext())
        input.setText(currentNote)
        
        // Add some padding to the input for better UI
        input.setPadding(32, 32, 32, 32)

        return AlertDialog.Builder(requireContext())
            .setTitle("Edit Note")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newNote = input.text.toString()
                updateNote(newNote)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun updateNote(note: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbHelper = DatabaseHelper(requireContext())
                dbHelper.updateFavoriteNote(favoriteId, note)
                dbHelper.close()
                withContext(Dispatchers.Main) {
                    onNoteUpdated()
                }
            } catch (e: Exception) {
                // Silently handle if DB operation fails
            }
        }
    }
}
