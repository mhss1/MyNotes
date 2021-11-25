package com.mhss.app.mynotes.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mhss.app.mynotes.R
import com.mhss.app.mynotes.databinding.FragmentAboutBinding

class AboutFragment : Fragment(R.layout.fragment_about) {

    private lateinit var binding: FragmentAboutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAboutBinding.bind(view)
        binding.privacyPolicy.setOnClickListener {
            val privacyPolicyIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://sites.google.com/view/mohamedx5/home"))
            startActivity(privacyPolicyIntent)

        }

        binding.appInfo.setOnClickListener {
            findNavController().navigate(AboutFragmentDirections.actionAboutFragmentToAppInfoFragment())
        }
        binding.rateUs.setOnClickListener {
            val storeIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}"))
            startActivity(storeIntent)

        }
    }
}