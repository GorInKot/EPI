package com.example.epi.Fragments.Reports.SendReport

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        SharedViewModelFactory((requireActivity().application as App).reportRepository)
    }

    companion object {
        private const val TAG = "SendReportFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSendReportBinding.inflate(inflater, container, false)

        binding.SeRFrBtnInfo.setOnClickListener {
            Log.d(TAG, "Info-кнопка нажата")
//            showAllEnteredData()
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
                        findNavController().navigate(R.id.action_sendReportFragment_to_reportsFragment, null, navOptions {
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
            findNavController().navigate(R.id.fixFragment)
        }
    }

    fun exportDatabase(context: Context) {
        val dbName = "app_database"
        val dbPath = context.getDatabasePath(dbName)

        val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "")
        if (!exportDir.exists()) exportDir.mkdirs()

        val outFile = File(exportDir, dbName)
        try {
            FileInputStream(dbPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("ExportDB", "БД экспортирована в: ${outFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("ExportDB", "Ошибка экспорта: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}