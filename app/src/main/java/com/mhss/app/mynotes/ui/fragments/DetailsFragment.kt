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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.database.EmptyTrashWorker
import com.mhss.app.mynotes.database.Note
import com.mhss.app.mynotes.databinding.FragmentDetailsBinding
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

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
        viewModel.setNoteFavorite(note.favorite)

        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = note.title

        return binding.root
    }// END onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleEdt.setText(note.title)
        binding.noteEdt.setText(note.note)
        binding.colorGroup.check(colorToButtonId(note.color))

        binding.colorGroup.setOnCheckedChangeListener { _, item ->
            handleColorChanged(buttonIdToColor(item))
            viewModel.updateNote(note.copy(color = buttonIdToColor(item)))
        }

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
            .setPositiveButton(getString(R.string.yes)){_,_ ->
                viewModel.updateNote(note.copy(
                    // Also updates the note title and content because they might have been changed
                    note = binding.noteEdt.text.toString(),
                    title = binding.titleEdt.text.toString(),
                    deleted = true
                    )
                )
                enqueueEmptyTrashWorker()
                toast(getString(R.string.note_moved_to_trash))
                deleted = true
                findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToMainFragment())
            }
            .setNegativeButton(getString(R.string.cancel)){_,_ ->
                return@setNegativeButton
            }
            .show()
    }

    private fun showDeleteForeverDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_note))
            .setMessage(R.string.delete_note_forever_message)
            .setPositiveButton(getString(R.string.yes)){_,_ ->
                viewModel.deleteNote(note)
                toast(getString(R.string.note_deleted))
                findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToMainFragment())
            }
            .setNegativeButton(getString(R.string.cancel)){_,_ ->
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
                findNavController().navigate(DetailsFragmentDirections.actionDetailsFragmentToMainFragment())
            }
        }
    }

    private fun handleFavoriteClicked() {
        if (viewModel.isNoteFavorite) {
            viewModel.updateNote(note.copy(favorite = false))
            favoriteMenuItem.setIcon(R.drawable.star_border)
            viewModel.setNoteFavorite(false)
        } else {
            viewModel.updateNote(note.copy(favorite = true))
            favoriteMenuItem.setIcon(R.drawable.star_fill)
            viewModel.setNoteFavorite(true)
        }
    }


    private fun setupFavoriteButton() {
        favoriteMenuItem.apply {
            if (note.favorite) setIcon(R.drawable.star_fill)
            else setIcon(R.drawable.star_border)
        }
    }

    private fun toast(message: String)
            = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

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

    override fun onStop() {
        if (!deleted) {
            val currentTitle = binding.titleEdt.text.toString()
            val currentNoteContent = binding.noteEdt.text.toString()
            if (isNoteEdited(currentTitle, currentNoteContent))
                viewModel.updateNote(
                    note.copy(
                        title = currentTitle,
                        note = currentNoteContent,
                        date = System.currentTimeMillis()
                    )
                )
        }
        super.onStop()
    }

    private fun isNoteEdited(newTitle: String, CurrentNoteContent: String)
            = newTitle != note.title || CurrentNoteContent != note.note

    private fun enqueueEmptyTrashWorker(){
        val deleteRequest = OneTimeWorkRequestBuilder<EmptyTrashWorker>()
            .setInitialDelay(14, TimeUnit.DAYS)
            .build()

        WorkManager
            .getInstance(requireActivity())
            .enqueueUniqueWork(getString(R.string.empty_trash_worker_name), ExistingWorkPolicy.REPLACE, deleteRequest)
    }
}//END Fragment