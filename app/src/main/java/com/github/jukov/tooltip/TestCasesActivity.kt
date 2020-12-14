package com.github.jukov.tooltip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class TestCasesActivity : AppCompatActivity() {

    private val testCaseFragmentCreators = listOf(
        CenterLargeHorizontalTestCaseFragment(),
        CenterLargeVerticalTestCaseFragment(),
        TopStartLargeBottomTestCaseFragment(),
        TopStartLargeEndTestCaseFragment(),
        TopEndLargeStartTestCaseFragment(),
        TopEndLargeBottomTestCaseFragment(),
        BottomStartLargeEndTestCaseFragment(),
        BottomStartLargeTopTestCaseFragment(),
        BottomEndLargeStartTestCaseFragment(),
        BottomEndLargeTopTestCaseFragment()
    )

    private var currentCase: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialTestCase = testCaseFragmentCreators[currentCase]

        savedInstanceState ?: supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, initialTestCase)
            .commit()

        initToolbar(initialTestCase.name)
        handleBackPressed()
    }

    private fun handleBackPressed() {

    }

    private fun initToolbar(title: String) {
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close)
            actionBar.title = title //TODO text size
        }
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
        testCaseFragmentCreators[++currentCase]
            .let { fragment ->
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit()

                supportActionBar?.title = fragment.name
            }

    }

    private fun previousCase() {
        testCaseFragmentCreators[--currentCase]
            .let { fragment ->
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit()

                supportActionBar?.title = fragment.name
            }

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