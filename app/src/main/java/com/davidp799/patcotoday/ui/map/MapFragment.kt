package com.davidp799.patcotoday.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.davidp799.patcotoday.databinding.FragmentMapBinding
import com.davidp799.patcotoday.utils.EnableNestedScrolling
import com.google.android.material.transition.MaterialFadeThrough


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        enterTransition = MaterialFadeThrough()

        val mapViewModel =
            ViewModelProvider(this)[MapViewModel::class.java]
        val navController = findNavController()
        val root: View = binding.root

        // listView items
        val stationMap = root.findViewById<View>(com.davidp799.patcotoday.R.id.stationMap) as ListView
        EnableNestedScrolling.enable(stationMap)
        val stationMapGeneralAdapter = MapListAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            mapViewModel.stationList
        )
        stationMap.isTransitionGroup = true
        stationMap.adapter = stationMapGeneralAdapter
        stationMap.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val action = MapFragmentDirections
                    .actionNavigationMapToNavigationStationDetails(mapViewModel.stationList[position])
                navController.navigate(action)
            }
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}