package com.example.epi.Fragments.Transport

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentArrangmentBinding
import com.example.epi.databinding.FragmentTransportBinding


class TransportFragment : Fragment() {

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTransportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.TraFrNextBtn.setOnClickListener {
            findNavController().navigate(R.id.controlFragment)
        }
        binding.TraFrBackBtn.setOnClickListener {
            findNavController().navigate(R.id.arrangementFragment)
        }

        binding.checkBoxManualCustomer.setOnCheckedChangeListener { _, isChecked ->
            // Список всех TextInputLayout и TextInputEditText
            val allViews = listOf(
                binding.textInputLayoutEdCustomer,
                binding.edEdCustomer,
                binding.textInputLayoutEdContract,
                binding.edEdContract,
                binding.textInputLayoutEdExecutor,
                binding.edEdExecutor,
                binding.textInputLayoutEdContractTransort,
                binding.edEdContractTransort,
                binding.textInputLayoutStartDate,
                binding.textInputEditTextStartDate,
                binding.imbStartCalendar,
                binding.textInputLayoutStartDateHours,
                binding.textInputEditTextStartDateHours,
                binding.textInputLayoutStateNumber,
                binding.textInputEditTextStateNumber,
                binding.textInputLayoutEndDate,
                binding.textInputEditTextEndDate,
                binding.imbEndCalendar,
                binding.textInputLayoutEndDateHours,
                binding.textInputEditTextEndDateHours
            )

            allViews.forEach { view ->
                view.isEnabled = !isChecked
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}