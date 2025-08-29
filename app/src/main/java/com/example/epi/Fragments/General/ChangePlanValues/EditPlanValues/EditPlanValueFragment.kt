package com.example.epi.Fragments.General.ChangePlanValues.EditPlanValues

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.util.copy
import com.example.epi.App
// import com.example.epi.R // R is usually automatically imported if package is correct
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentEditPlanValueBinding
import kotlinx.coroutines.launch

class EditPlanValueFragment : Fragment() {

    private var _binding: FragmentEditPlanValueBinding? = null
    private val binding get() = _binding!!

    private val args: EditPlanValueFragmentArgs by navArgs()

    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext,
            (requireActivity().application as App).planValueRepository,
            (requireActivity().application as App).orderNumberRepository,
            (requireActivity().application as App).factValueRepository
        )
    }

    companion object {
        private val TAG = "Tagg-EditPlanValuesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPlanValueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val planValue = args.planValue

        // Заполняем поля
        binding.autoCompleteDialogIdObject.setText(planValue.objectId)
        binding.autoCompleteDialogComplexOfWork.setText(planValue.complexWork)
        binding.autoCompleteDialogWorkProject.setText(planValue.typeOfWork)
        binding.autoCompleteDialogMeasures.setText(planValue.measures)
        binding.autoCompleteDialogPlan.setText(planValue.planValue.toString())

        // Кнопка "Сохранить"
        binding.btnEPVSave.setOnClickListener {
            val updated = planValue.copy(
                objectId = binding.autoCompleteDialogIdObject.text.toString(),
                complexWork = binding.autoCompleteDialogComplexOfWork.text.toString(),
                typeOfWork = binding.autoCompleteDialogWorkProject.text.toString(),
                measures = binding.autoCompleteDialogMeasures.text.toString(),
                planValue = binding.autoCompleteDialogPlan.text.toString().toDoubleOrNull() ?: 0.0
            )

            lifecycleScope.launch {
                sharedViewModel.updatePlanValue(updated)
                findNavController().navigateUp() // возвращаемся назад
            }
        }

        // Кнопка "Отмена"
        binding.btnEPVCancel.setOnClickListener {
            findNavController().navigateUp()
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
