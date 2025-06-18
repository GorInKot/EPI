package com.example.epi.Fragments.Control

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentControlBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
//        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.CtrFrTvDate.text = "Дата: $currentDate"


        binding.CtrFrrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.transportFragment)
        }

        binding.CtrFrBtnReport.setOnClickListener {
            findNavController().navigate(R.id.inspectionReportFragment)
        }

        binding.CtrFrBtnDocsReport.setOnClickListener {
            findNavController().navigate(R.id.commentsDocsFragment)
        }

        binding.CtrFrrBtnNext.setOnClickListener {
            findNavController().navigate(R.id.fixFragment)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}