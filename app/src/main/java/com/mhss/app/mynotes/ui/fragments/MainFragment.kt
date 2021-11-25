package com.mhss.app.mynotes.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.database.EmptyTrashWorker
import com.mhss.app.mynotes.databinding.FragmentMainBinding
import com.mhss.app.mynotes.ui.recyclerview.NoteRecAdapter
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    private lateinit var noteAdapter: NoteRecAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noteSearchEdt.clearFocus()

        noteAdapter = NoteRecAdapter {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToDetailsFragment(it)
            )
        }
        binding.notesRec.layoutManager = StaggeredGridLayoutManager(2, 1)

        observeAllNotes()

        binding.notesRec.adapter = noteAdapter
        binding.addNoteFab.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToAddNoteFragment())
        }

        binding.noteSearchEdt.addTextChangedListener {
            if (it.toString().isBlank()) observeAllNotes()
            else viewModel.getNote(it.toString()){ list ->
                noteAdapter.submitList(list)
                binding.notesRec.scheduleLayoutAnimation()
            }
        }

    }//End onViewCreated

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_all)
            showDeleteAllNotesDialog()

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteAllNotesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_all_notes_dialog_title))
            .setMessage(getString(R.string.delete_all_notes_dialog_message))
            .setPositiveButton(getString(R.string.yes)){_,_ ->
                viewModel.moveAllNotesToTrash()
                enqueueEmptyTrashWorker()
                toast(getString(R.string.all_notes_moved_to_trash))
            }
            .setNegativeButton(getString(R.string.cancel)){_,_ ->
                return@setNegativeButton
            }
            .show()
    }

    private fun observeAllNotes() = viewModel.allNotes.observe(viewLifecycleOwner){ list ->
        noteAdapter.submitList(list)
        binding.noNotes.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        setHasOptionsMenu(list.isNotEmpty())
    }
    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private fun enqueueEmptyTrashWorker(){
        val deleteRequest = OneTimeWorkRequestBuilder<EmptyTrashWorker>()
            .setInitialDelay(14, TimeUnit.DAYS)
            .build()

        WorkManager
            .getInstance(requireActivity())
            .enqueueUniqueWork(getString(R.string.empty_trash_worker_name), ExistingWorkPolicy.REPLACE, deleteRequest)
    }

}//END Fragment