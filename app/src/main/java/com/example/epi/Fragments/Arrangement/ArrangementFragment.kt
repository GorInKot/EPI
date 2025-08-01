package com.example.epi.Fragments.Arrangement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.epi.App
import com.example.epi.DataBase.Entities.*
import com.example.epi.databinding.FragmentArrangementBinding

class ArrangementFragment : Fragment() {

    private var _binding: FragmentArrangementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArrangementViewModel by viewModels {
        ArrangementViewModelFactory((requireActivity().application as App).reportRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArrangementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAutoCompleteWorkType()
        setupAutoCompleteCustomer()
        setupAutoCompleteObject()
        setupAutoCompletePlot()
        setupAutoCompleteContractor()
    }

    private fun setupAutoCompleteWorkType() {
        viewModel.workTypes.observe(viewLifecycleOwner) { workTypes ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                workTypes.map { it.name })

            binding.autoCompleteWorkType.setAdapter(adapter)

            binding.autoCompleteWorkType.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectedWorkType.value = workTypes[position]
            }
        }
    }

    private fun setupAutoCompleteCustomer() {
        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                customers.map { it.name })

            binding.autoCompleteCustomer.setAdapter(adapter)

            binding.autoCompleteCustomer.setOnItemClickListener { _, _, position, _ ->
                val selected = customers[position]
                viewModel.selectedCustomer.value = selected

                // Загрузка зависимых сущностей
                viewModel.loadContractorsForCustomer(selected.id)
                viewModel.loadObjectsForCustomer(selected.id)

                // Очистка зависимых полей
                viewModel.selectedObject.value = null
                viewModel.selectedPlot.value = null
                viewModel.selectedContractor.value = null
                viewModel.selectedSubContractor.value = null
                viewModel.manualObject.value = null
                viewModel.manualPlot.value = null
                viewModel.manualContractor.value = null
                viewModel.manualSubContractor.value = null
            }
        }
    }

    private fun setupAutoCompleteObject() {
        viewModel.objects.observe(viewLifecycleOwner) { objects ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                objects.map { it.name })

            binding.autoCompleteObject.setAdapter(adapter)

            binding.autoCompleteObject.setOnItemClickListener { _, _, position, _ ->
                val selectedObject = objects[position]
                viewModel.selectedObject.value = selectedObject
                viewModel.loadPlotsForObject(selectedObject.id)

                // Очистка участка
                viewModel.selectedPlot.value = null
                viewModel.manualPlot.value = null
            }
        }
    }

    private fun setupAutoCompletePlot() {
        viewModel.plots.observe(viewLifecycleOwner) { plots ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                plots.map { it.name })

            binding.autoCompletePlot.setAdapter(adapter)

            binding.autoCompletePlot.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectedPlot.value = plots[position]
            }
        }
    }

    private fun setupAutoCompleteContractor() {
        viewModel.contractors.observe(viewLifecycleOwner) { contractors ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                contractors.map { it.name })

            binding.autoCompleteContractor.setAdapter(adapter)

            binding.autoCompleteContractor.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectedContractor.value = contractors[position]
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
