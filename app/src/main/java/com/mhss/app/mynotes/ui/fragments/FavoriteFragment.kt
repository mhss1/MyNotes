package com.mhss.app.mynotes.ui.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.databinding.FragmentFavoriteBinding
import com.mhss.app.mynotes.ui.recyclerview.NoteRecAdapter
import com.mhss.app.mynotes.ui.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private lateinit var binding: FragmentFavoriteBinding
    private val viewModel: NoteViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        )
        binding = FragmentFavoriteBinding.bind(view)

        binding.favoriteRec.layoutManager = StaggeredGridLayoutManager(2, 1)

        val adapter = NoteRecAdapter {note, card ->
            findNavController().navigate(
                FavoriteFragmentDirections.actionFavoriteFragmentToDetailsFragment(note),
                FragmentNavigatorExtras(card to card.transitionName)
            )
        }
        binding.favoriteRec.adapter = adapter
        viewModel.allFavoriteNotes.observe(viewLifecycleOwner, {
            binding.noFavoriteTv.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(it)
        })
        postponeEnterTransition()
        binding.favoriteRec.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

}