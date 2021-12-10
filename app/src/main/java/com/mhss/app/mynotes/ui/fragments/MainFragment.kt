package com.mhss.app.mynotes.ui.fragments

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.database.DeleteNoteWorker
import com.mhss.app.mynotes.databinding.FragmentMainBinding
import com.mhss.app.mynotes.ui.recyclerview.NoteRecAdapter
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    private lateinit var noteAdapter: NoteRecAdapter

    private var currentView = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentMainBinding.inflate(inflater, container, false)
        sharedElementReturnTransition = MaterialContainerTransform()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noteSearchEdt.clearFocus()
        hideKeyboard()

        noteAdapter = NoteRecAdapter {note, card ->
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToDetailsFragment(note),
                FragmentNavigatorExtras(card to card.transitionName)
            )
        }
        binding.notesRec.adapter = noteAdapter
        readDataStore().observe(viewLifecycleOwner){
            binding.notesRec.layoutManager =
                if (it == 0) StaggeredGridLayoutManager(2, 1)
                else LinearLayoutManager(requireContext())

                currentView = it
                binding.notesRec.scheduleLayoutAnimation()
        }

        observeAllNotes()

        binding.addNoteFab.setOnClickListener {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToAddNoteFragment(),
                FragmentNavigatorExtras(it to "add_fragment")
            )
        }

        binding.noteSearchEdt.addTextChangedListener {
            if (it.toString().isBlank()) observeAllNotes()
            else viewModel.getNote(it.toString()) { list ->
                noteAdapter.submitList(list)
                binding.notesRec.scheduleLayoutAnimation()
            }
        }

        postponeEnterTransition()
        binding.notesRec.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }//End onViewCreated

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId ){
            R.id.delete_all -> showDeleteAllNotesDialog()
            R.id.view -> showViewDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteAllNotesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_all_notes_dialog_title))
            .setMessage(getString(R.string.delete_all_notes_dialog_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.moveAllNotesToTrash()
                viewModel.allNotes.value?.forEach {
                    enqueueDeleteNoteWorker(it.id!!)
                }
                toast(getString(R.string.all_notes_moved_to_trash))
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                return@setNegativeButton
            }
            .show()
    }

    private fun observeAllNotes() = viewModel.allNotes.observe(viewLifecycleOwner) { list ->
        noteAdapter.submitList(list)
        binding.noNotes.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        setHasOptionsMenu(list.isNotEmpty())
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

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

    private fun hideKeyboard(){
        activity?.currentFocus?.let { view ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun writeDatastore(view: Int){
        lifecycleScope.launch{
            val key = intPreferencesKey(getString(R.string.view))
            requireContext().dataStore.edit { settings ->
                settings[key] = view
            }
        }
    }

    private fun readDataStore() =
        requireContext().dataStore.data
            .map { preferences ->
                preferences[intPreferencesKey(getString(R.string.view))] ?: 0
            }.asLiveData()

    private fun showViewDialog(){
        val choices = arrayOf(getString(R.string.grid), getString(R.string.list))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.choose_view))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.save)) { dialog, which ->
                    writeDatastore((dialog as AlertDialog).listView.checkedItemPosition)
                    toast(getString(R.string.view_saved))
            }
            .setSingleChoiceItems(choices, currentView){_, _ -> }
            .show()
    }


}//END Fragment