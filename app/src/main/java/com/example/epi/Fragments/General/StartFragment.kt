package com.example.epi.Fragments.General

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbSettings.setOnClickListener {
            findNavController().navigate(R.id.SettingFragment)
        }

        binding.btnArrangement.setOnClickListener {
            findNavController().navigate(R.id.arrangementFragment)
        }

        binding.btnAuth.setOnClickListener {
            findNavController().navigate(R.id.authFragment)
        }

        binding.btnReports.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }

        binding.btnLogOut.setOnClickListener {


            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}