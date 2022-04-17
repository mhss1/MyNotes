package com.mhss.app.mynotes.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.databinding.FragmentTrashBinding
import com.mhss.app.mynotes.ui.recyclerview.NoteRecAdapter
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrashFragment : Fragment(R.layout.fragment_trash) {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var binding: FragmentTrashBinding
    private lateinit var emptyMenuItem: MenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrashBinding.bind(view)
        setHasOptionsMenu(true)

        val adapter = NoteRecAdapter{it, card ->
            findNavController().navigate(TrashFragmentDirections.actionTrashFragmentToDetailsFragment(it),
                    FragmentNavigatorExtras(card to card.transitionName))
        }
        binding.trashRec.adapter = adapter
        readDataStore().observe(viewLifecycleOwner){
            binding.trashRec.layoutManager =
                if (it == 0) StaggeredGridLayoutManager(2, 1)
                else LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allDeletedNotes.collect { list ->
                    binding.noTrashTv.visibility = if(list.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(list)
                    emptyMenuItem.isVisible = list.isNotEmpty()
                }
            }
        }

        postponeEnterTransition()
        binding.trashRec.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trash_fragment_menu, menu)
        emptyMenuItem = menu.findItem(R.id.empty_trash)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId ) {
            R.id.empty_trash -> {
                showEmptyTrashDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showEmptyTrashDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.empty_trash))
            .setPositiveButton(getString(R.string.yes)){_,_ ->
                viewModel.deleteNotesInTrash()
                cancelAllDeleteWorkers()
            }
            .setNegativeButton(getString(R.string.cancel)){_,_ ->
                return@setNegativeButton
            }
            .show()
    }

    private fun cancelAllDeleteWorkers(){
        WorkManager.getInstance(requireActivity())
            .cancelAllWork()
    }

    private fun readDataStore() =
        requireContext().dataStore.data
            .map { preferences ->
                preferences[intPreferencesKey(getString(R.string.view))] ?: 0
            }.asLiveData()
}//END Fragment