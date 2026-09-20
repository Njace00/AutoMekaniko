package com.example.automekaniko

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.automekaniko.databinding.FragmentHomeBinding

import androidx.core.content.ContextCompat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // ── "Auto" dark gray, "Mekaniko" red ──────────────────────────────────────
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.app_text_primary)
        val redColor = ContextCompat.getColor(requireContext(), R.color.theme_red)
        
        spannable.setSpan(ForegroundColorSpan(primaryColor), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(redColor), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannable

        // Cards
        binding.card3D.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_guidesFragment)
        }
        binding.cardLive.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_obdFragment)
        }

        // Hidden buttons (fallback)
        binding.viewbtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_guidesFragment)
        }
        binding.livebtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_obdFragment)
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
