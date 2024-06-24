package com.davidp799.patcotoday.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidp799.patcotoday.databinding.FragmentMapBinding
import com.davidp799.patcotoday.utils.EnableNestedScrolling
import com.google.android.material.transition.MaterialFadeThrough


class MapFragment : Fragment() {
    // View Binding & Data Store
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    // ViewModel
    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val root: View = binding.root

        // recyclerview items
        val stationMap = root.findViewById<RecyclerView>(com.davidp799.patcotoday.R.id.stationMap)
        stationMap.layoutManager = LinearLayoutManager(requireContext()) // Or any other layout manager
        EnableNestedScrolling.enable(stationMap)

        val stationMapGeneralAdapter = MapListAdapter(
            viewModel.stationList
        ) { stationName, view -> // Item click listener
            val action = MapFragmentDirections
                .actionNavigationMapToNavigationStationDetails(stationName)
            val extras = FragmentNavigatorExtras(
                view to "station_${stationName}"
            )
            navController.navigate(action, extras)
        }
        stationMap.adapter = stationMapGeneralAdapter
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}