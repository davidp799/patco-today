package com.davidp799.patcotoday.ui.info.infoItemDetails

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentInfoDetailsBinding
import com.google.android.material.transition.MaterialContainerTransform

class InfoItemDetailsFragment : Fragment() {

    private var _binding: FragmentInfoDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 400
            scrimColor = Color.TRANSPARENT
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            duration = 400
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val itemSelected = arguments?.getString("itemSelected")
        val infoDetailsViewModel =
            ViewModelProvider(this)[InfoItemDetailsViewModel::class.java]
        _binding = FragmentInfoDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        val details = infoDetailsViewModel.infoDetailsList[itemSelected]
        val packageManager = requireContext().packageManager

        val title = root.findViewById<TextView>(R.id.titleTextView)
        title.text = details?.get("title").toString()
        val subTitle = root.findViewById<TextView>(R.id.subTitleTextView)
        subTitle.text = details?.get("subTitle").toString()
        val description = root.findViewById<TextView>(R.id.descriptionTextView)
        description.text = details?.get("description").toString()

        val reducedUrl = root.findViewById<TextView>(R.id.faresTextViewReducedUrl)
        reducedUrl.setOnClickListener {
            val url = details?.get("reducedFareProgramUrl")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "No app found to handle this link",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val optionsUrl = root.findViewById<TextView>(R.id.faresTextViewOptionsUrl)
        optionsUrl.setOnClickListener {
            val url = details?.get("farePaymentOptionsUrl")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "No app found to handle this link",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return root
    }
}
