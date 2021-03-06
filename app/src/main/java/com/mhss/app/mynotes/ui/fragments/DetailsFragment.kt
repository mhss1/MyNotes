package com.mhss.app.mynotes.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.database.DeleteNoteWorker
import com.mhss.app.mynotes.database.Note
import com.mhss.app.mynotes.databinding.FragmentDetailsBinding
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

const val NOTE_STATE_EDITED = 1
const val NOTE_STATE_FAVORITE_CHANGED = 2
const val NOTE_STATE_NOT_EDITED = 0

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding

    private val viewModel: NoteViewModel by viewModels()
    private val args: DetailsFragmentArgs by navArgs()

    private lateinit var note: Note

    private lateinit var deleteMenuItem: MenuItem
    private lateinit var favoriteMenuItem: MenuItem
    private lateinit var shareMenuItem: MenuItem

    private var deleted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        note = args.note
        if (viewModel.isNoteFavorite == null) {
            viewModel.setNoteFavorite(note.favorite)
        }
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = note.title

        sharedElementEnterTransition = MaterialContainerTransform()
        return binding.root
    }// END onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainDetailsContainer.transitionName = note.id.toString()

        binding.titleEdt.setText(note.title)
        binding.noteEdt.setText(note.note)

        binding.colorGroup.setOnCheckedChangeListener { _, item ->
            handleColorChanged(buttonIdToColor(item))
        }
        binding.colorGroup.check(colorToButtonId(note.color))

    }// END onViewCreated

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (note.deleted) {
            inflateDeletedNoteMenu(menu, inflater)
        } else {
            inflateMenu(menu, inflater)
            setupFavoriteButton()
        }

    }// END onCreateOptionsMenu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (note.deleted)
            handleOptionsNoteDeleted(item)
        else
            when (item.itemId) {
                R.id.delete -> {
                    showDeleteDialog()
                }
                R.id.favorite -> {
                    handleFavoriteClicked()
                }
                R.id.share -> {
                    handleShareClicked()
                }
            }

        return super.onOptionsItemSelected(item)
    }// END onOptionsItemSelected

    private fun handleShareClicked() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "${note.title} \n\n${note.note}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.send_to)))

    }

    private fun inflateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.details_fragment_menu, menu)
        deleteMenuItem = menu.findItem(R.id.delete)
        favoriteMenuItem = menu.findItem(R.id.favorite)
        shareMenuItem = menu.findItem(R.id.share)
    }

    private fun inflateDeletedNoteMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.details_fragment_deleted_menu, menu)
        binding.titleEdt.isEnabled = false
        binding.noteEdt.isEnabled = false
        binding.colorList.visibility = View.GONE
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.move_note_to_trash))
            .setMessage(getString(R.string.move_note_trash_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.updateNote(
                    note.copy(
                        // Also updates the note title and content because they might have been changed
                        note = binding.noteEdt.text.toString(),
                        title = binding.titleEdt.text.toString(),
                        deleted = true
                    )
                )
                enqueueDeleteNoteWorker(note.id!!)
                toast(getString(R.string.note_moved_to_trash))
                deleted = true
                findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToMainFragment())
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                return@setNegativeButton
            }
            .show()
    }

    private fun showDeleteForeverDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_note))
            .setMessage(R.string.delete_note_forever_message)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteNote(note)
                cancelWorker(note.id.toString())
                toast(getString(R.string.note_deleted))
                findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToMainFragment())
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                return@setNegativeButton
            }
            .show()
    }


    private fun handleColorChanged(color: Int) {
        binding.mainDetailsContainer.setBackgroundColor(
            ContextCompat.getColor(requireContext(), color)
        )
    }

    private fun handleOptionsNoteDeleted(item: MenuItem) {
        when (item.itemId) {
            R.id.delete_forever -> showDeleteForeverDialog()
            R.id.restore -> {
                viewModel.updateNote(note.copy(deleted = false))
                cancelWorker(note.id.toString())
                findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToMainFragment())
            }
        }
    }

    private fun handleFavoriteClicked() {
        if (viewModel.isNoteFavorite == true) {
            favoriteMenuItem.setIcon(R.drawable.star_border)
            viewModel.setNoteFavorite(false)
        } else {
            favoriteMenuItem.setIcon(R.drawable.star_fill)
            viewModel.setNoteFavorite(true)
        }
    }


    private fun setupFavoriteButton() {
        favoriteMenuItem.apply {
            if (viewModel.isNoteFavorite == true) setIcon(R.drawable.star_fill)
            else setIcon(R.drawable.star_border)
        }
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private fun colorToButtonId(color: Int) = when (color) {
        R.color.blue -> R.id.blue_button
        R.color.green -> R.id.green_button
        R.color.purple -> R.id.purple_button
        R.color.yellow -> R.id.yellow_button
        R.color.orange -> R.id.orange_button
        R.color.red -> R.id.red_button
        R.color.pink -> R.id.pink_button
        R.color.brown -> R.id.brown_button
        else -> R.id.gray_button
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

    override fun onDestroyView() {
        if (!deleted) {
            val currentTitle = binding.titleEdt.text.toString()
            val currentNoteContent = binding.noteEdt.text.toString()
            val currentColor = buttonIdToColor(binding.colorGroup.checkedRadioButtonId)
            val currentFavorite = viewModel.isNoteFavorite == true

            when (getNoteState(currentTitle, currentNoteContent, currentColor, currentFavorite)) {
                NOTE_STATE_EDITED -> viewModel.updateNote(
                    note.copy(
                        title = currentTitle,
                        note = currentNoteContent,
                        color = currentColor,
                        favorite = currentFavorite,
                        date = System.currentTimeMillis()
                    )
                )
                NOTE_STATE_FAVORITE_CHANGED -> viewModel.updateNote(note.copy(favorite = currentFavorite))
            }
        }
        super.onDestroyView()
    }

    private fun isNoteEdited(newTitle: String, CurrentNoteContent: String, currentColor: Int) =
        newTitle != note.title || CurrentNoteContent != note.note || currentColor != note.color

    private fun favoriteOnlyChanged(
        title: String,
        currentNote: String,
        color: Int,
        favorite: Boolean
    ) =
        title == note.title && currentNote == note.note && color == note.color && favorite != note.favorite

    private fun getNoteState(title: String, note: String, color: Int, favorite: Boolean): Int {
        return when {
            isNoteEdited(title, note, color) -> NOTE_STATE_EDITED
            favoriteOnlyChanged(title, note, color, favorite) -> NOTE_STATE_FAVORITE_CHANGED
            else -> NOTE_STATE_NOT_EDITED
        }
    }

    private fun enqueueDeleteNoteWorker(id: Int) {
        val deleteRequest = OneTimeWorkRequestBuilder<DeleteNoteWorker>()
            .setInputData(workDataOf("id" to id))
            .setInitialDelay(14, TimeUnit.DAYS)
            .build()

        WorkManager
            .getInstance(requireActivity())
            .enqueueUniqueWork(
                id.toString(),
                ExistingWorkPolicy.REPLACE,
                deleteRequest
            )
    }

    private fun cancelWorker(name: String){
        WorkManager.getInstance(requireActivity())
            .cancelUniqueWork(name)
    }
}//END Fragment