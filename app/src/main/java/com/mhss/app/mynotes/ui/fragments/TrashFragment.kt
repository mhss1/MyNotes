package com.mhss.app.mynotes.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.databinding.FragmentTrashBinding
import com.mhss.app.mynotes.ui.recyclerview.NoteRecAdapter
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrashFragment : Fragment(R.layout.fragment_trash) {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var binding: FragmentTrashBinding
    private lateinit var emptyMenuItem: MenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrashBinding.bind(view)
        setHasOptionsMenu(true)

        val adapter = NoteRecAdapter{
            findNavController().navigate(TrashFragmentDirections.actionTrashFragmentToDetailsFragment(it),
                    FragmentNavigatorExtras(binding.trashRec to "details_fragment"))
        }
        binding.trashRec.adapter = adapter
        binding.trashRec.layoutManager = StaggeredGridLayoutManager(2, 1)

        viewModel.allDeletedNotes.observe(viewLifecycleOwner, { list ->
            binding.noTrashTv.visibility = if(list.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(list)
            emptyMenuItem.isVisible = list.isNotEmpty()
        })
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
                cancelEmptyTrashWorker()
            }
            .setNegativeButton(getString(R.string.cancel)){_,_ ->
                return@setNegativeButton
            }
            .show()
    }

    private fun cancelEmptyTrashWorker(){
        WorkManager.getInstance(requireActivity())
            .cancelUniqueWork(
            getString(R.string.empty_trash_worker_name)
        )
    }
}//END Fragment