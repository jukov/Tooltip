package com.github.jukov.tooltip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class TestCasesActivity : AppCompatActivity() {

    private val testCaseFragmentCreators = listOf(
        CenterTestCaseFragment(),
        TopStartTestCaseFragment(),
        TopEndTestCaseFragment(),
        BottomStartTestCaseFragment(),
        BottomEndTestCaseFragment()
    )

    private var currentCase: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, CenterTestCaseFragment())
            .commit()

        initToolbar()
        handleBackPressed()
    }

    private fun handleBackPressed() {

    }

    private fun initToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
    }

    private fun invalidateToolbar() {
        if (hasPreviousCase()) {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        } else {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (hasPreviousCase()) {
            previousCase()
            invalidateOptionsMenu()
            invalidateToolbar()
        } else {
            finish()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.button_next, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.forward).isEnabled = hasNextCase()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.forward -> {
                nextCase()
                invalidateOptionsMenu()
                invalidateToolbar()
                true
            }
            else -> {
                false
            }
        }

    private fun hasNextCase(): Boolean =
        testCaseFragmentCreators.getOrNull(currentCase + 1) != null

    private fun nextCase() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, testCaseFragmentCreators[++currentCase])
            .commit()
    }

    private fun previousCase() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, testCaseFragmentCreators[--currentCase])
            .commit()
    }

    private fun hasPreviousCase(): Boolean =
        testCaseFragmentCreators.getOrNull(currentCase - 1) != null

    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    TestCasesActivity::class.java
                )
            )
        }
    }
}