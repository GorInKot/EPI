package com.example.epi.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        binding.startFragmentBtnRasstanovka.setOnClickListener {
//            Toast.makeText(requireContext(),
//                "Нажата кнопка Расстановки",
//                Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.arrangementFragment)
        }

        binding.startFragmentBtnControl.setOnClickListener {
//            Toast.makeText(requireContext(),
//                "Нажата кнопка Инспекционный контроль",
//                Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.controlFragment)
        }

        binding.startFragmentBtnFicsacia.setOnClickListener {
//            Toast.makeText(requireContext(),
//                "Нажата кнопка Фиксация объемов",
//                Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.fixFragment)
        }

        binding.startFragmentBtnFReports.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}