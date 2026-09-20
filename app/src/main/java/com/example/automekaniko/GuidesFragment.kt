package com.example.automekaniko

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.automekaniko.databinding.ActivityGuidesBinding

class GuidesFragment : Fragment() {

    private var _binding: ActivityGuidesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityGuidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFF222222.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannable

        binding.cardDtc.setOnClickListener {
            findNavController().navigate(R.id.action_guidesFragment_to_dtcFragment)
        }

        binding.cardMaintenance.setOnClickListener {
            findNavController().navigate(R.id.action_guidesFragment_to_maintenanceFragment)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.topBar.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
