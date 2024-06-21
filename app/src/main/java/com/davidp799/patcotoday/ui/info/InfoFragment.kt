package com.davidp799.patcotoday.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentInfoBinding
import com.davidp799.patcotoday.utils.EnableNestedScrolling
import com.google.android.material.transition.MaterialFadeThrough

class InfoFragment : Fragment() {
    // View Binding & Data Store
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!
    // ViewModel
    private val viewModel: InfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        enterTransition = MaterialFadeThrough()

        val navController = findNavController()
        val root: View = binding.root

        // listview items
        val infoListView = root.findViewById<View>(R.id.info_list_view) as ListView
        EnableNestedScrolling.enable(infoListView)
        val infoGeneralAdapter = InfoListAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            viewModel.infoItems
        )
        infoListView.adapter = infoGeneralAdapter
        infoListView.isTransitionGroup = true
        infoListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                try {
                    val activeItem = viewModel.infoItems[position]
                    if (activeItem == "Fares") {
                        val action = InfoFragmentDirections
                            .actionNavigationInfoToNavigationInfoDetails(activeItem)
                        navController.navigate(action)
                    } else {
                        val openLinksIntent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.infoLinks[position]))
                        requireContext().startActivity(openLinksIntent)
                    }
                } catch (e: Error) {
                    e.printStackTrace()
                }
            }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}