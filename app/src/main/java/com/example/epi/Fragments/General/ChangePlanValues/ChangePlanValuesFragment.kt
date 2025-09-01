package com.example.epi.Fragments.General.ChangePlanValues

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.App
import com.example.epi.DataBase.ExtraDatabase.ExtraDatabaseHelper
import com.example.epi.Fragments.Control.ControlFragmentDirections
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentChangePlanValuesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class ChangePlanValuesFragment : Fragment() {
    private var _binding: FragmentChangePlanValuesBinding? = null
    private val binding get() = _binding!!

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

    private lateinit var adapter: PlanValueAdapter

    companion object {
        private val TAG = "Tagg-ChangePlanValuesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangePlanValuesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObjectDropdown()
        setupRecyclerView()
        observePlanValues()
        setupBackButton()

        binding.autoCompleteObjects.setText("", false)
        sharedViewModel.clearPlanValues()
    }

    private fun setupObjectDropdown() {
        lifecycleScope.launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val objects = withContext(Dispatchers.IO) {
                dbHelper.getObjects()
            }

            if (objects.isEmpty()) {
                Toast.makeText(requireContext(), "Список объектов пуст", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Список объектов пуст")
            }

            val objectListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                objects
            )
            binding.autoCompleteObjects.setAdapter(objectListAdapter)
            binding.autoCompleteObjects.inputType = InputType.TYPE_NULL
            binding.autoCompleteObjects.keyListener = null

            binding.autoCompleteObjects.setOnTouchListener { _, _ ->
                binding.autoCompleteObjects.showDropDown()
                false
            }

            binding.autoCompleteObjects.setOnItemClickListener { parent, _, position, _ ->
                val selectedObject = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedObject(selectedObject)

                // Загружаем плановые значения для выбранного объекта
                lifecycleScope.launch {
                    sharedViewModel.loadPlanValues(selectedObject)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PlanValueAdapter(
            onEditClicked = { planValue ->
                val action = ChangePlanValuesFragmentDirections
                    .actionChangePlanValuesFragmentToEditPlanValuesFragment(planValue)
                findNavController().navigate(action)
            },
            onDeleteClicked = { planValue ->
                lifecycleScope.launch {
                    sharedViewModel.deletePlanValue(planValue)
                }
            }
        )

        val recyclerView = binding.recyclerViewChangePlanValue
        recyclerView.adapter = adapter
    }

    private fun observePlanValues() {
        sharedViewModel.planValues.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "planValues size = ${list.size}")
            adapter.submitList(list)
        }
    }

    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            val action = ChangePlanValuesFragmentDirections.actionChangePlanValuesFragmentToSettingFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}