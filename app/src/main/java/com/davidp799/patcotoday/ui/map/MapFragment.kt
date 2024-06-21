package com.davidp799.patcotoday.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.davidp799.patcotoday.databinding.FragmentMapBinding
import com.davidp799.patcotoday.ui.info.InfoViewModel
import com.davidp799.patcotoday.utils.EnableNestedScrolling
import com.google.android.material.transition.MaterialFadeThrough


class MapFragment : Fragment() {
    // View Binding & Data Store
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    // ViewModel
    private val viewModel: MapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        enterTransition = MaterialFadeThrough()

        val navController = findNavController()
        val root: View = binding.root

        // listView items
        val stationMap = root.findViewById<View>(com.davidp799.patcotoday.R.id.stationMap) as ListView
        EnableNestedScrolling.enable(stationMap)
        val stationMapGeneralAdapter = MapListAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            viewModel.stationList
        )
        stationMap.isTransitionGroup = true
        stationMap.adapter = stationMapGeneralAdapter
        stationMap.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val activeItem = viewModel.stationList[position]
                val action = MapFragmentDirections
                    .actionNavigationMapToNavigationStationDetails(activeItem)
                navController.navigate(action)
            }
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}