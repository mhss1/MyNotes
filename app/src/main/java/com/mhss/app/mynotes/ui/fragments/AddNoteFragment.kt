package com.mhss.app.mynotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.database.Note
import com.mhss.app.mynotes.databinding.FragmentAddNoteBinding
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNoteFragment : Fragment() {

    private lateinit var binding: FragmentAddNoteBinding
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.colorGroup.check(R.id.gray_button)

        binding.addNoteBtn.setOnClickListener {
            val title = binding.titleEdt.text.toString()
            val note = binding.noteEdt.text.toString()
            val color = buttonIdToColor(binding.colorGroup.checkedRadioButtonId)

            if (title.isBlank() && note.isBlank()) {
                toast(getString(R.string.note_cannot_be_empty))
                return@setOnClickListener
            }
            viewModel.insertNote(Note(title, note, System.currentTimeMillis(), color = color))
            toast(getString(R.string.note_added))
            findNavController().navigateUp()
        }

        binding.colorGroup.setOnCheckedChangeListener { _, id ->
            handleColorChanged(buttonIdToColor(id))
        }
    }


    private fun handleColorChanged(color: Int) {
        binding.addFragmentContainer.setBackgroundColor(
            ContextCompat.getColor(requireContext(), color)
        )
    }

    private fun buttonIdToColor(buttonId: Int) = when (buttonId) {
        R.id.blue_button -> R.color.blue
        R.id.green_button -> R.color.green
        R.id.purple_button -> R.color.purple
        R.id.red_button -> R.color.red
        R.id.yellow_button -> R.color.yellow
        R.id.orange_button -> R.color.orange
        R.id.pink_button -> R.color.pink
        R.id.brown_button -> R.color.brown
        else -> R.color.dark_gray
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}