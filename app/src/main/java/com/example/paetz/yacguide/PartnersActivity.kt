package com.example.paetz.yacguide

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Partner
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

class PartnersActivity : AppCompatActivity() {

    private var _db: AppDatabase? = null
    private var _checkboxMap: MutableMap<Int, CheckBox>? = null
    private var _selectedPartnerIds: ArrayList<Int>? = null
    private var _partnerNamePart: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partners)

        _db = AppDatabase.getAppDatabase(this)
        _checkboxMap = HashMap()
        _selectedPartnerIds = intent.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS)
        _partnerNamePart = ""

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.onFocusChangeListener = View.OnFocusChangeListener { v, _ ->
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                _partnerNamePart = searchEditText.text.toString()
                displayContent()
            }
        })
        displayContent()
    }

    fun addPartner(v: View) {
        val dialog = Dialog(this)
        dialog.setTitle("Kletterpartner hinzufügen")
        dialog.setContentView(R.layout.add_partner_dialog)
        val okButton = dialog.findViewById<View>(R.id.okButton) as Button
        okButton.setOnClickListener {
            val newName = (dialog.findViewById<View>(R.id.addPartnerEditText) as EditText).text.toString().trim { it <= ' ' }
            updatePartner(dialog, newName, null)
        }
        val cancelButton = dialog.findViewById<View>(R.id.cancelButton) as Button
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    fun enter(v: View) {
        val selectedIds = ArrayList<Int>()
        for ((key, cb) in _checkboxMap!!) {
            if (cb.isChecked) {
                selectedIds.add(key)
            }
        }
        val resultIntent = Intent()
        resultIntent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, selectedIds)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun displayContent() {
        title = "Kletterpartner"
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        _checkboxMap!!.clear()
        val partners = _db!!.partnerDao().all
        val ascends = _db!!.ascendDao().all

        // We need to sort the partners according to the number of ascends you have done with them
        val ascendPartnerIds = ArrayList<Int>()
        for (ascend in ascends) {
            ascendPartnerIds.addAll(ascend.partnerIds!!)
        }
        val sortedPartners = ArrayList<Partner>()
        val partnerIdOccurences = ArrayList<Int>()
        for (i in partners.indices) {
            val occurenceCount = Collections.frequency(ascendPartnerIds, partners[i].id)
            var index = i
            for (j in 0 until i) {
                if (occurenceCount > partnerIdOccurences[j]) {
                    index = j
                    break
                }
            }
            sortedPartners.add(index, partners[i])
            partnerIdOccurences.add(index, occurenceCount)
        }

        for (partner in sortedPartners) {
            val partnerName = partner.name
            if (!partnerName!!.toLowerCase().contains(_partnerNamePart!!.toLowerCase())) {
                continue
            }
            val innerLayout = RelativeLayout(this)
            innerLayout.setBackgroundColor(Color.WHITE)

            val checkBox = CheckBox(this)
            checkBox.text = partnerName
            if (_selectedPartnerIds!!.contains(partner.id)) {
                checkBox.isChecked = true
            }
            _checkboxMap!![partner.id] = checkBox
            innerLayout.addView(checkBox)

            val editButton = ImageButton(this)
            editButton.id = View.generateViewId()
            editButton.setImageResource(android.R.drawable.ic_menu_edit)
            editButton.setOnClickListener {
                val dialog = Dialog(this@PartnersActivity)
                dialog.setContentView(R.layout.add_partner_dialog)
                (dialog.findViewById<View>(R.id.addPartnerEditText) as EditText).setText(partnerName)
                val okButton = dialog.findViewById<View>(R.id.okButton) as Button
                okButton.setOnClickListener {
                    val newName = (dialog.findViewById<View>(R.id.addPartnerEditText) as EditText).text.toString().trim { it <= ' ' }
                    updatePartner(dialog, newName, partner)
                }
                val cancelButton = dialog.findViewById<View>(R.id.cancelButton) as Button
                cancelButton.setOnClickListener { dialog.dismiss() }
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.show()
            }
            innerLayout.addView(editButton)

            val deleteButton = ImageButton(this)
            deleteButton.id = View.generateViewId()
            deleteButton.setImageResource(android.R.drawable.ic_menu_delete)
            deleteButton.setOnClickListener {
                val dialog = Dialog(this@PartnersActivity)
                dialog.setContentView(R.layout.dialog)
                dialog.findViewById<TextView>(R.id.dialogText).text = "Kletterpartner löschen?"
                val okButton = dialog.findViewById<View>(R.id.yesButton) as Button
                okButton.setOnClickListener {
                    _db!!.partnerDao().delete(partner)
                    dialog.dismiss()
                    displayContent()
                }
                val cancelButton = dialog.findViewById<View>(R.id.noButton) as Button
                cancelButton.setOnClickListener { dialog.dismiss() }
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.show()
            }
            innerLayout.addView(deleteButton)

            val buttonWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
            val paramsDelete = deleteButton.layoutParams as RelativeLayout.LayoutParams
            paramsDelete.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            paramsDelete.height = buttonWidthPx
            paramsDelete.width = paramsDelete.height

            val paramsEdit = editButton.layoutParams as RelativeLayout.LayoutParams
            paramsEdit.addRule(RelativeLayout.LEFT_OF, deleteButton.id)
            paramsEdit.height = buttonWidthPx
            paramsEdit.width = paramsEdit.height

            layout.addView(innerLayout)
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun updatePartner(dialog: Dialog, newName: String, partner: Partner?) {
        var result = partner
        when {
            newName == "" ->
                Toast.makeText(dialog.context, "Kein Name eingegeben", Toast.LENGTH_SHORT).show()
            _db!!.partnerDao().getId(newName) > 0 ->
                Toast.makeText(dialog.context, "Name bereits vergeben", Toast.LENGTH_SHORT).show()
            else -> {
                if (result == null) {
                    result = Partner()
                }
                result.name = newName
                _db!!.partnerDao().insert(result)
                dialog.dismiss()
                displayContent()
            }
        }
    }
}
