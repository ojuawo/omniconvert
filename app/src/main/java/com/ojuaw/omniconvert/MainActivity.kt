package com.ojuaw.omniconvert

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.ojuaw.omniconvert.converter.Category
import com.ojuaw.omniconvert.converter.ConversionEngine
import com.ojuaw.omniconvert.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentCategory = Category.LENGTH
    private var peopleCount = 2
    private var tipPercent = 15.0

    private val numberFormat = DecimalFormat("#,##0.######")
    private val currencyFormat = DecimalFormat("$#,##0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        setupConverter()
        setupTipCalculator()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position ?: 0) {
                    0 -> {
                        binding.layoutConverter.visibility = View.VISIBLE
                        binding.layoutTip.visibility = View.GONE
                        binding.layoutAbout.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutConverter.visibility = View.GONE
                        binding.layoutTip.visibility = View.VISIBLE
                        binding.layoutAbout.visibility = View.GONE
                        recalculateTip()
                    }
                    2 -> {
                        binding.layoutConverter.visibility = View.GONE
                        binding.layoutTip.visibility = View.GONE
                        binding.layoutAbout.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // --- CONVERTER LOGIC ---

    private fun setupConverter() {
        setupCategoryChips()
        updateSpinnersForCategory()

        binding.etConvertInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performConversion()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSwapUnits.setOnClickListener {
            val fromPos = binding.spinnerFromUnit.selectedItemPosition
            val toPos = binding.spinnerToUnit.selectedItemPosition
            binding.spinnerFromUnit.setSelection(toPos)
            binding.spinnerToUnit.setSelection(fromPos)
        }

        binding.btnCopyResult.setOnClickListener {
            val result = binding.tvConvertResult.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Conversion Result", result)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied: $result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
            currentCategory = when (checkedId) {
                R.id.chipLength -> Category.LENGTH
                R.id.chipWeight -> Category.WEIGHT
                R.id.chipTemp -> Category.TEMPERATURE
                R.id.chipSpeed -> Category.SPEED
                R.id.chipDigital -> Category.DIGITAL
                R.id.chipVolume -> Category.VOLUME
                R.id.chipArea -> Category.AREA
                else -> Category.LENGTH
            }
            updateSpinnersForCategory()
        }
    }

    private fun updateSpinnersForCategory() {
        val unitList = ConversionEngine.unitsByCategory[currentCategory]?.map { it.name } ?: listOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerFromUnit.adapter = adapter
        binding.spinnerToUnit.adapter = adapter

        if (unitList.size > 1) {
            binding.spinnerFromUnit.setSelection(0)
            binding.spinnerToUnit.setSelection(1)
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                performConversion()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerFromUnit.onItemSelectedListener = listener
        binding.spinnerToUnit.onItemSelectedListener = listener

        performConversion()
    }

    private fun performConversion() {
        val inputText = binding.etConvertInput.text?.toString()?.trim() ?: ""
        if (inputText.isEmpty() || inputText == "-" || inputText == ".") {
            binding.tvConvertResult.text = "0"
            binding.tvConvertFormula.text = ""
            return
        }

        val value = inputText.toDoubleOrNull() ?: 0.0
        val fromUnit = binding.spinnerFromUnit.selectedItem as? String ?: return
        val toUnit = binding.spinnerToUnit.selectedItem as? String ?: return

        val result = ConversionEngine.convert(currentCategory, value, fromUnit, toUnit)
        val formattedResult = numberFormat.format(result)

        binding.tvConvertResult.text = formattedResult
        binding.tvConvertFormula.text = "$value $fromUnit = $formattedResult $toUnit"
    }

    // --- TIP & BILL SPLIT LOGIC ---

    private fun setupTipCalculator() {
        binding.etBillAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalculateTip()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupTip.setOnCheckedChangeListener { _, checkedId ->
            tipPercent = when (checkedId) {
                R.id.chipTip10 -> 10.0
                R.id.chipTip15 -> 15.0
                R.id.chipTip18 -> 18.0
                R.id.chipTip20 -> 20.0
                else -> 15.0
            }
            recalculateTip()
        }

        binding.btnMinusPerson.setOnClickListener {
            if (peopleCount > 1) {
                peopleCount--
                binding.tvPeopleCount.text = "$peopleCount people"
                recalculateTip()
            }
        }

        binding.btnPlusPerson.setOnClickListener {
            if (peopleCount < 50) {
                peopleCount++
                binding.tvPeopleCount.text = "$peopleCount people"
                recalculateTip()
            }
        }

        recalculateTip()
    }

    private fun recalculateTip() {
        val billText = binding.etBillAmount.text?.toString()?.trim() ?: ""
        val bill = billText.toDoubleOrNull() ?: 0.0

        val totalTip = bill * (tipPercent / 100.0)
        val grandTotal = bill + totalTip

        val tipPerPerson = if (peopleCount > 0) totalTip / peopleCount else 0.0
        val totalPerPerson = if (peopleCount > 0) grandTotal / peopleCount else 0.0

        binding.tvTotalPerPerson.text = currencyFormat.format(totalPerPerson)
        binding.tvTipPerPerson.text = currencyFormat.format(tipPerPerson)
        binding.tvTipTotal.text = currencyFormat.format(totalTip)
        binding.tvGrandTotal.text = currencyFormat.format(grandTotal)
    }
}