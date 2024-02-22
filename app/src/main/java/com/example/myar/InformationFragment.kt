package com.example.myar

import android.app.Dialog
import android.os.Bundle
import com.example.myar.databinding.FragmentInformationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class InformationFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        _binding = FragmentInformationBinding.inflate(layoutInflater)
        bottomSheet.setContentView(binding.root)
        return bottomSheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}