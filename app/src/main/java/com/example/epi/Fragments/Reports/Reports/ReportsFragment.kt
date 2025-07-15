package com.example.epi.Fragments.Reports.Reports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.epi.R
import com.example.epi.ViewModel.SharedViewModel
import com.example.epi.databinding.FragmentReportsBinding


class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExpandableAdapter

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReportsBinding.inflate(inflater, container, false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupRecyclerView()

        binding.RepFrMainMenuBtn.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

//    private fun prepareExpandableData(): MutableList<Any> {
//        val parentItem = ParentItem(
//            date = viewModel.currentDate.value ?: "не указано",
//            obj = viewModel.selectedObject.value ?: "не указано",
//            children = listOf(
//                ChildItem(
//                    workType = viewModel.selectedWorkType.value ?: "—",
//                    customer = viewModel.selectedCustomer.value ?: "—",
//                    contractor = viewModel.selectedContractor.value ?: "—",
//                    transportCustomer = viewModel.customerName.value ?: "—"
//                )
//            )
//        )
//
//        val list = mutableListOf<Any>()
//        list.add(parentItem)
//        return list
//    }

//    private fun setupRecyclerView() {
//        // Здесь можно подготовить данные из viewModel
//        val data = prepareExpandableData()
//
//        adapter = ExpandableAdapter(data)
//        binding.recyclerView.adapter = adapter
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}