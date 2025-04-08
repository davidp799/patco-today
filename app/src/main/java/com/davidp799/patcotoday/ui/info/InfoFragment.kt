package com.davidp799.patcotoday.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        val navController = findNavController()

        // recyclerview items
        val infoRecyclerView = view.findViewById<RecyclerView>(R.id.info_recycler_view) // Update ID in your layout
        infoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(infoRecyclerView.context, LinearLayoutManager.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.drawable_divider) // Replace with your drawable
        dividerItemDecoration.setDrawable(dividerDrawable!!)
        infoRecyclerView.addItemDecoration(dividerItemDecoration)
        EnableNestedScrolling.enable(infoRecyclerView)

        val infoAdapter = InfoListAdapter(
            viewModel.infoItems
        ) { position, itemView ->
            try {
                val activeItem = viewModel.infoItems[position]
                if (activeItem == "Fares") {
                    val action = InfoFragmentDirections
                        .actionNavigationInfoToNavigationInfoDetails(activeItem)
                    val extras = FragmentNavigatorExtras(
                        itemView to "info_${activeItem}"
                    )
                    navController.navigate(action, extras)
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

        // Resume once layout is finished
        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}