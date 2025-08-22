package com.example.epi.Fragments.FixingVolumes.AddPlanValues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.DataBase.PlanValue.PlanValue
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentAddPlanValuesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue

class AddPlanValuesFragment : Fragment() {

    private var _binding: FragmentAddPlanValuesBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext,
            (requireActivity().application as App).planValueRepository,
        )
    }

    private val args by lazy {
        AddPlanValuesFragmentArgs.fromBundle(requireArguments())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlanValuesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceFix: Bundle?) {
        super.onViewCreated(view, savedInstanceFix)

        // Настройка выпадающего списка для ComplexOfWork
        sharedViewModel.controlsComplexOfWork.observe(viewLifecycleOwner) { complexOfWorks ->
            if (complexOfWorks.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, complexOfWorks)
                binding.autoCompleteComplexOfWorks.setAdapter(adapter)
                // Обновляем выбранный элемент, если есть значение
                sharedViewModel.selectedComplex.value?.let { selected ->
                    val position = complexOfWorks.indexOf(selected)
                    if (position >= 0) {
                        binding.autoCompleteComplexOfWorks.setText(complexOfWorks[position], false)
                    }
                }
                binding.autoCompleteComplexOfWorks.setOnItemClickListener { parent, view, position, id ->
                    val selectedComplex = parent.getItemAtPosition(position) as String
                    sharedViewModel.setSelectedComplex(selectedComplex) // Обновляем комплекс и виды работ
                }
                binding.autoCompleteComplexOfWorks.setOnClickListener { binding.autoCompleteComplexOfWorks.showDropDown() }
            }
        }

        // Настройка выпадающего списка для TypesOfWork
        sharedViewModel.controlTypesOfWork.observe(viewLifecycleOwner) { typesOfWork ->
            if (typesOfWork.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typesOfWork)
                binding.autoCompleteTypeOfWork.setAdapter(adapter)
                // Сбрасываем текст, если список обновился
                if (binding.autoCompleteTypeOfWork.text.isNullOrEmpty()) {
                    binding.autoCompleteTypeOfWork.setText("", false)
                }
                binding.autoCompleteTypeOfWork.setOnClickListener { binding.autoCompleteTypeOfWork.showDropDown() }
            } else {
                binding.autoCompleteTypeOfWork.setText("", false) // Очищаем, если нет видов работ
            }
        }

        sharedViewModel.fixMeasures.observe(viewLifecycleOwner) { measures ->
            if (measures.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, measures)
                binding.autoCompleteMeasures.setAdapter(adapter)
                binding.autoCompleteMeasures.setOnClickListener { binding.autoCompleteMeasures.showDropDown() }
            }
        }

        // Обработка кнопок
        binding.btnCloseDialogAppPlan.setOnClickListener {
            findNavController().navigateUp() // Используем navigateUp для возврата назад
        }

        binding.btnSaveAppPlan.setOnClickListener {
            val complexWork = binding.autoCompleteComplexOfWorks.text.toString().trim()
            val workType = binding.autoCompleteTypeOfWork.text.toString().trim()
            val planValueStr = binding.TextInputEditTextPlanValues.text.toString().trim()
            val objectId = args.objectId

            if (complexWork.isNotEmpty() && workType.isNotEmpty() && planValueStr.isNotEmpty()) {
                val planValue = planValueStr.toDoubleOrNull()
                if (planValue != null) {
                    val newPlanValue = PlanValue(
                        objectId = objectId,
                        complexWork = complexWork,
                        typeOfWork = workType,
                        planValue = planValue
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        sharedViewModel.addPlanValue(newPlanValue)
                        Toast.makeText(requireContext(), "Плановое значение добавлено", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                } else {
                    Toast.makeText(requireContext(), "Плановое значение должно быть числом", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}