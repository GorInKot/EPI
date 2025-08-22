package com.example.epi.Fragments.Reports.SendReport

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.epi.App
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentSendReportBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class SendReportFragment : Fragment() {

    private var _binding : FragmentSendReportBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext
        )
    }

    companion object {
        private const val TAG = "Tagg-SendReport"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSendReportBinding.inflate(inflater, container, false)

        binding.SeRFrBtnInfo.setOnClickListener {
            Log.d(TAG, "Info-кнопка нажата")
            val data = sharedViewModel.showAllEnteredData()
            Log.d(TAG, "Data for dialog: $data")
            showInfoDialog(data)
            sharedViewModel.exportDatabase(requireContext())
            Log.d(TAG, "Показали AlertDialog с информацией")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.SeRFrBtnNext.setOnClickListener {
            Log.d(TAG, "Next-кнопка нажата")
            lifecycleScope.launch {
                try {
                    val reportId = sharedViewModel.saveOrUpdateReport()
                    if (reportId > 0) {
                        sharedViewModel.clearAllData()
                        findNavController().navigate(R.id.action_SendReportFragment_to_ReportsFragment, null, navOptions {
                            popUpTo(R.id.StartFragment) { inclusive = false }
                        })
                    } else {
                        // Обработка ошибки уже логируется в SharedViewModel
                    }
                } catch (e: Exception) {
                    // Логирование ошибки уже в SharedViewModel
                }
            }
        }

        binding.SeRFrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.action_SendReportFragment_to_FixVolumesFragment)
        }
    }

    private fun showInfoDialog(data: String) {
        // Создаем TextView для отображения данных
        val textView = TextView(requireContext()).apply {
            text = data
            setPadding(32) // padding в 16dp (примерно 32px в зависимости от плотности)
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }

        // Создаем ScrollView для прокрутки длинного текста
        val scrollView = ScrollView(requireContext()).apply {
            addView(textView)
        }

        // Создаем AlertDialog
        AlertDialog.Builder(requireContext())
            .setTitle("Информация об отчете")
            .setView(scrollView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}