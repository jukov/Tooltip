package com.github.jukov.tooltip.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class TestCasesActivity : AppCompatActivity() {

    private var mode: Mode = Mode.CONFIGURE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, TestCaseConfigureFragment(), CONFIGURE_FRAGMENT_TAG)
            .commit()

        initToolbar()
        handleBackPressed()
    }

    private fun handleBackPressed() {

    }

    private fun initToolbar(title: String = getString(R.string.app_name)) {
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close)
            actionBar.title = title //TODO text size
        }
    }

    private fun invalidateToolbar() {
        if (mode == Mode.DISPLAY) {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        } else {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (mode == Mode.DISPLAY) {
            mode = Mode.CONFIGURE
            supportFragmentManager.popBackStack()
            invalidateOptionsMenu()
            invalidateToolbar()
        } else {
            finish()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.button_done, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.done).isVisible = mode == Mode.CONFIGURE
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.done -> {
                showTest()
                invalidateOptionsMenu()
                invalidateToolbar()
                true
            }
            else -> {
                false
            }
        }

    private fun showTest() {
            (supportFragmentManager
                .findFragmentByTag(CONFIGURE_FRAGMENT_TAG) as? TestCaseConfigureFragment)
                ?.makeConfig()
                ?.let { config -> TestCaseFragment.newInstanse(config) }
                ?.let { fragmenr ->
                    supportFragmentManager
                        .beginTransaction()
                        .replace(android.R.id.content, fragmenr)
                        .addToBackStack(null)
                        .commit()
                }
                ?.also { mode = Mode.DISPLAY }
    }

    private enum class Mode {
        CONFIGURE,
        DISPLAY
    }

    companion object {

        private const val CONFIGURE_FRAGMENT_TAG = "CONFIGURE_FRAGMENT_TAG"
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