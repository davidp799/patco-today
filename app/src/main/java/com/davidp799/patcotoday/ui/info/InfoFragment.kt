package com.davidp799.patcotoday.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.SettingsActivity
import com.davidp799.patcotoday.databinding.FragmentInfoBinding
import com.davidp799.patcotoday.utils.EnableNestedScrolling
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.internal.CollapsingTextHelper
import com.google.android.material.transition.MaterialFadeThrough

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val infoViewModel =
            ViewModelProvider(this)[InfoViewModel::class.java]
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        enterTransition = MaterialFadeThrough()

        // listview items
        val infoListView = root.findViewById<View>(R.id.info_list_view) as ListView
        EnableNestedScrolling.enable(infoListView)
        val infoGeneralAdapter = InfoListAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            infoViewModel.infoItems
        )
        infoListView.adapter = infoGeneralAdapter
        infoListView.isTransitionGroup = true
        infoListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                try {
                    val openLinksIntent = Intent(Intent.ACTION_VIEW, Uri.parse(infoViewModel.infoLinks[position]))
                    requireContext().startActivity(openLinksIntent)
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