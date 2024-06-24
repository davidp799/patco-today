package com.davidp799.patcotoday.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // recyclerview items
        val infoRecyclerView = root.findViewById<RecyclerView>(R.id.info_recycler_view) // Update ID in your layout
        infoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        EnableNestedScrolling.enable(infoRecyclerView)

        val infoAdapter = InfoListAdapter(
            viewModel.infoItems
        ) { position ->
            try {
                val activeItem = viewModel.infoItems[position]
                if (activeItem == "Fares") {
                    val action = InfoFragmentDirections
                        .actionNavigationInfoToNavigationInfoDetails(activeItem)
                    navController.navigate(action)
                } else {
                    val openLinksIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.infoLinks[position]))
                    requireContext().startActivity(openLinksIntent)
                }
            } catch (e: Error) {
                e.printStackTrace()
            }
        }
        infoRecyclerView.adapter = infoAdapter
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}