package com.example.newkursach

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newkursach.data.AppDatabase
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.databinding.CardAudioBinding
import com.example.newkursach.secondary.OnItemClickListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CardAudioActivity : AppCompatActivity(), OnItemClickListener {
    lateinit var binding: CardAudioBinding
    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var mAdapter: AudioAdapter
    private val audioDAO = AppDatabase.getInstance(this).audioRecordDao()
    private var allChecked = false

    private lateinit var toolbar: MaterialToolbar

    private lateinit var editBar: View
    private lateinit var butClose: ImageButton
    private lateinit var butSelectAll: ImageButton
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var editBut: ImageButton
    private lateinit var deleteBut: ImageButton
    private lateinit var deleteText: TextView
    private lateinit var editText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.toolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
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


        records = ArrayList()


        mAdapter = AudioAdapter(records, this)
        binding.recid.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }
        fetchAll()
        butClose.setOnClickListener {
            editModeLeft()
        }

        butSelectAll.setOnClickListener {
            allChecked = !allChecked
            records.map { it.isChecked = allChecked }
            mAdapter.notifyDataSetChanged()
            if (allChecked) {
                editDis()
                deleteEn()
            } else {
                editDis()
                deleteDis()
            }
        }
        deleteBut.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Удалить запись?")
            val delRecords = records.count { it.isChecked }
            builder.setMessage("Удалить $delRecords запись(и)?")
            builder.setPositiveButton("Удалить") { _, _ ->
                val toDel = records.filter { it.isChecked }.toTypedArray()
                GlobalScope.launch {
                    audioDAO.delete(toDel)
                    runOnUiThread {
                        records.removeAll(toDel)
                        mAdapter.notifyDataSetChanged()
                        editModeLeft()
                    }
                }
            }
            builder.setNegativeButton("Отменить") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }

        editBut.setOnClickListener {
            showRenameDialog()
        }


    }

    private fun editModeLeft() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        editBar.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        records.map {
            it.isChecked = false
        }
        mAdapter.setEditMode(false)

    }

    private fun editDis() {
        editText.isClickable = false
        val grayColor = ContextCompat.getColor(this, R.color.grayRipple)
        editText.setTextColor(grayColor)
        editText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }

    private fun deleteDis() {
        deleteText.isClickable = false
        val grayColor = ContextCompat.getColor(this, R.color.grayRipple)
        deleteText.setTextColor(grayColor)
        deleteText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }

    private fun deleteEn() {
        deleteText.isClickable = true
        val grayColor = ContextCompat.getColor(this, R.color.grayRipple)
        deleteText.setTextColor(grayColor)
        deleteText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }

    private fun editEn() {
        editText.isClickable = true
        val grayColor = ContextCompat.getColor(this, R.color.grayRipple)
        editText.setTextColor(grayColor)
        editText.backgroundTintList = ColorStateList.valueOf(grayColor)
    }


    private fun fetchAll() {
        GlobalScope.launch {
            records.clear()
            val queryResult = audioDAO.getAll()
            records.addAll(queryResult)

            runOnUiThread {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClickListener(position: Int) {
        val audioRecord = records[position]

        if (mAdapter.isEditMode()) {
            records[position].isChecked = !records[position].isChecked
            mAdapter.notifyItemChanged(position)

            val select = records.count { it.isChecked }
            when (select) {
                0 -> {
                    editDis()
                    deleteDis()
                }

                1 -> {
                    editEn()
                    deleteEn()
                }

                else -> {
                    editDis()
                    deleteEn()
                }
            }
        } else {
            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("filepath", audioRecord.filepath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        }
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(this, "Долгое нажатие", Toast.LENGTH_SHORT).show()
        mAdapter.setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        mAdapter.notifyItemChanged(position)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (mAdapter.isEditMode() && editBar.visibility == View.GONE) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
            editBar.visibility = View.VISIBLE
            deleteEn()
            editEn()
        }
    }

    override fun onShareClickListener(position: Int) {
        val record = records[position]
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Поделиться файлом: ${record.filename}")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
    }


    private fun showRenameDialog() {
        val selectedRecords = records.filter { it.isChecked }

        if (selectedRecords.size == 1) {
            val record = selectedRecords[0]
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Переименовать запись?")

            val input = EditText(this)
            input.hint = "Введите название файла"
            input.setText(record.filename)

            alertDialogBuilder.setView(input)
            alertDialogBuilder.setPositiveButton("Сохранить") { _, _ ->
                val inputName = input.text.toString()
                if (inputName.isEmpty()) {
                    Toast.makeText(this, "Требуется имя файла", Toast.LENGTH_SHORT).show()
                } else {
                    GlobalScope.launch {
                        audioDAO.update(AudioRecord(record.id,inputName,record.filepath,record.timestamp,record.duration,record.wavesPath))
                        runOnUiThread {
                            mAdapter.notifyItemChanged(records.indexOf(record))
                            editModeLeft()
                        }
                    }
                }
            }
            alertDialogBuilder.setNegativeButton("Отменить") { _, _ ->
            }

            alertDialogBuilder.show()
        } else {
            Toast.makeText(
                this,
                "Можно выбрать только один файл для переименования",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}