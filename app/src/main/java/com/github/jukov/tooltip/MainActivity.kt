package com.github.jukov.tooltip

import android.os.Bundle
import android.widget.Toast
import com.github.jukov.tooltip.databinding.ActivityMainBinding

class MainActivity : BindingActivity<ActivityMainBinding>() {

    override fun onCreateDataBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.buttonShowcase.setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_LONG).show()
        }

        binding.buttonTestcases.setOnClickListener {
            TestCasesActivity.start(this)
        }
    }

}