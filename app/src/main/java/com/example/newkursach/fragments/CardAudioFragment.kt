package com.example.newkursach.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newkursach.AudioAdapter
import com.example.newkursach.R
import com.example.newkursach.data.AppDatabase
import com.example.newkursach.databinding.FragmentCardAudioBinding
import com.example.newkursach.secondary.OnItemClickListener
import com.example.newkursach.viewmodel.CardAudioViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior


class CardAudioFragment : Fragment() {
    private var _binding: FragmentCardAudioBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterAudioRecords: AudioAdapter
    private val audioDAO = AppDatabase.getInstance(requireContext()).audioRecordDao()
    private var allIsChecked = false

    private lateinit var toolbar: MaterialToolbar

    private val args by navArgs<AudioPlayerFragmentArgs>()
    private val fileName by lazy { args.filename }
    private val filePath by lazy { args.filename }
    private val viewModel: CardAudioViewModel by viewModels { CardAudioViewModel.Factory() }

    private lateinit var editBar: View
    private lateinit var butClose: ImageButton
    private lateinit var butSelectAll: ImageButton
    val grayColor = ContextCompat.getColor(requireContext(), R.color.grayRipple)
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var editBut: ImageButton
    private lateinit var deleteBut: ImageButton
    private lateinit var deleteText: TextView
    private lateinit var editText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentCardAudioBinding.inflate(layoutInflater, container, false)

        toolbar = binding.toolBar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        val action =
                            CardAudioFragmentDirections.actionCardAudioFragmentToMainFragment()
                        findNavController().navigate(action)
                    }
                })
        }

        editBut = binding.editbut
        deleteBut = binding.deletebut
        deleteText = binding.textdelete
        editText = binding.textedit

        editBar = binding.editBar
        butClose = binding.butClose
        butSelectAll = binding.butAll

        bottomSheet = binding.bottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val recyclerView: RecyclerView = binding.recid
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapterAudioRecords = AudioAdapter(listener).apply {
            viewModel.records.observe(viewLifecycleOwner) {
                setRecords(it)
            }
        }
        recyclerView.adapter = adapterAudioRecords

        butClose.setOnClickListener {
            exitEditMode()
        }

        butSelectAll.setOnClickListener {
            allIsChecked = !allIsChecked
            viewModel.records.observe(viewLifecycleOwner) {
                viewModel.setIsChecked(allIsChecked)
            }
            if (allIsChecked) {
                switchDeleteMode(true)
                switchEditMode(false)
            } else {
                switchDeleteMode(false)
                switchEditMode(false)
            }
        }
        deleteBut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Удалить запись(-и)?")
            builder.setPositiveButton("Удалить") { _, _ ->

                viewModel.records.observe(viewLifecycleOwner) {
                    val recordsToDelete = it.filter { it.isChecked }.toList()
                    viewModel.deleteRecords(recordsToDelete)
                }
                exitEditMode()
            }
            builder.setNegativeButton("Отменить") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }

        editBut.setOnClickListener {
            showRenameDialog()
        }
        return binding.root
    }

    private fun exitEditMode() {
        val actionBars = (requireActivity() as AppCompatActivity).supportActionBar
        actionBars?.setDisplayHomeAsUpEnabled(true)
        actionBars?.setDisplayShowHomeEnabled(true)
        editBar.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        viewModel.records.observe(viewLifecycleOwner) {
            it.map {
                it.isChecked = false
            }
        }
        adapterAudioRecords.setEditMode(false)

    }


    private fun switchDeleteMode(isClickable:Boolean){
        deleteText.isClickable = isClickable
        deleteText.setTextColor(grayColor)
        deleteText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }

    private fun switchEditMode(isClickable:Boolean){
        editText.isClickable = isClickable
        editText.setTextColor(grayColor)
        editText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }


    private fun showRenameDialog() {
        viewModel.records.observe(viewLifecycleOwner) {
            val selectedRecords = it.filter { it.isChecked }
            if (selectedRecords.size != 1) {
                Toast.makeText(
                    requireContext(),
                    "Можно выбрать только один файл для переименования",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            val record = selectedRecords[0]
            val input = EditText(requireContext())
            input.hint = "Введите название файла"
            input.setText(record.filename)
            AlertDialog.Builder(requireContext()).setTitle("Переименовать запись?").setView(input)
                .setPositiveButton("Сохранить") { _, _ ->
                    if (input.text.toString().isBlank()) {
                        Toast.makeText(requireContext(), "Требуется имя файла", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        viewModel.updateRecord(record, input.text.toString())
                        adapterAudioRecords.notifyItemChanged(it.indexOf(record))
                        exitEditMode()
                    }
                }.setNegativeButton("Отменить") { _, _ ->
                }.show()
        }

    }

    private val listener = object : OnItemClickListener {
        override fun onShareClickListener(position: Int) {
            viewModel.records.observe(viewLifecycleOwner) {
                val record = it[position]
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Поделиться файлом: ${record.filename}")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
            }
        }


        override fun onItemLongClickListener(position: Int) {
            val actionBars = (requireActivity() as AppCompatActivity).supportActionBar
            Toast.makeText(requireContext(), "Долгое нажатие", Toast.LENGTH_SHORT).show()
            adapterAudioRecords.setEditMode(true)
            viewModel.records.observe(viewLifecycleOwner) {
                it[position].isChecked = !it[position].isChecked
            }
            adapterAudioRecords.notifyItemChanged(position)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            if (adapterAudioRecords.isEditMode() && editBar.visibility == View.GONE) {
                actionBars?.setDisplayHomeAsUpEnabled(false)
                actionBars?.setDisplayShowHomeEnabled(false)
                editBar.visibility = View.VISIBLE
                switchDeleteMode(true)
                switchEditMode(true)
            }
        }

        override fun onItemClickListener(position: Int) {
            viewModel.records.observe(viewLifecycleOwner) {
                val audioRecord = it[position]
                if (adapterAudioRecords.isEditMode()) {
                    audioRecord.isChecked = !audioRecord.isChecked
                    adapterAudioRecords.notifyItemChanged(position)

                    when (it.count { it.isChecked }) {
                        0 -> {
                            switchDeleteMode(false)
                            switchEditMode(false)
                        }

                        1 -> {
                            switchDeleteMode(true)
                            switchEditMode(true)
                        }

                        else -> {
                            switchDeleteMode(true)
                            switchEditMode(false)
                        }
                    }
                } else {
                    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                        object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                val action =
                                    CardAudioFragmentDirections.actionCardAudioFragmentToAudioPlayerFragment(
                                        fileName, filePath
                                    )
                                findNavController().navigate(action)
                            }
                        })
                }
            }
        }
    }
}