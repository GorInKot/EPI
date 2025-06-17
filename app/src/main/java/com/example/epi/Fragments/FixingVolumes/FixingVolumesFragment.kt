package com.example.epi.Fragments.FixingVolumes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentFixingVolumesBinding


class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFixingVolumesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.FixFrBtnNext.setOnClickListener {
            findNavController().navigate(R.id.sendReportFragment)
        }

        binding.FixFrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.controlFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


}