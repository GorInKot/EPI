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

    private val PICK_IMAGE = 100
    private val STORAGE_PERMISSION_CODE = 101
    val MY_REQUEST_CODE1 = 100



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

        binding.CtrFrBackToMainMenuBtn.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }

        binding.edLocation.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: местоположение",
                Toast.LENGTH_LONG).show()
        }

        binding.edEquipmentName.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: наименование прибора / оборудования",
                Toast.LENGTH_LONG).show()
        }

        binding.edType.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: виды работ",
                Toast.LENGTH_LONG).show()
        }

        binding.edOperations.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: операции",
                Toast.LENGTH_LONG).show()
        }

        binding.edParameters.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: Параметры контроля",
                Toast.LENGTH_LONG).show()
        }

        binding.et123.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: какое-то странное поле))",
                Toast.LENGTH_LONG).show()
        }

        binding.etWorkReport.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: краткий отчет о работе",
                Toast.LENGTH_LONG).show()
        }

        binding.CtrFrAddPhotoBtn.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Выбрано: добавить фото",
                Toast.LENGTH_LONG
            ).show()
            selectImage()
        }

        binding.CtrFrIssieOderBtn.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: выдать предписание",
                Toast.LENGTH_LONG).show()
        }

        binding.CtrFrAddWorkBtn.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: добавить работы",
                Toast.LENGTH_LONG).show()
        }

        binding.CtrFrDeleteWorksBtn.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: удалить работы",
                Toast.LENGTH_LONG).show()
        }

        binding.CtrFrrBackBtn.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: назад",
                Toast.LENGTH_LONG).show()
        }

        binding.CtrFrrSignUpBtn.setOnClickListener {
            Toast.makeText(requireContext(),
                "Выбрано: Записать",
                Toast.LENGTH_LONG).show()
        }

    }
    private fun selectImage(){
        val intent = Intent(ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        startActivityForResult(intent, MY_REQUEST_CODE1);
    }
    // I override system function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == MY_REQUEST_CODE1) {

            val uriFilePath_to_gallery = data?.data
//            val name_photo = data?.
            Log.d("TAG", uriFilePath_to_gallery.toString())
//            show_URI_in_ImageView(uriFilePath_to_gallery)

        }
    }

    // my function for image view
//    fun show_URI_in_ImageView(uri: Uri?)
//    {
//        val myImageView = findViewById(R.id.imageView1) as ImageView
//        myImageView.setImageURI(uri)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}