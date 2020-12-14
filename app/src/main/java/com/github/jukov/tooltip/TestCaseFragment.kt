package com.github.jukov.tooltip

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.github.jukov.tooltip.databinding.TestCaseBottomEndBinding
import com.github.jukov.tooltip.databinding.TestCaseBottomStartBinding
import com.github.jukov.tooltip.databinding.TestCaseCenterBinding
import com.github.jukov.tooltip.databinding.TestCaseTopEndBinding
import com.github.jukov.tooltip.databinding.TestCaseTopStartBinding

class CenterTestCaseFragment: TestCaseFragment<TestCaseCenterBinding>() {

    override fun onCreateDataBinding(): TestCaseCenterBinding =
        TestCaseCenterBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }
}

class TopStartTestCaseFragment: TestCaseFragment<TestCaseTopStartBinding>() {

    override fun onCreateDataBinding(): TestCaseTopStartBinding =
        TestCaseTopStartBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }
}

class TopEndTestCaseFragment: TestCaseFragment<TestCaseTopEndBinding>() {

    override fun onCreateDataBinding(): TestCaseTopEndBinding =
        TestCaseTopEndBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }
}

class BottomStartTestCaseFragment: TestCaseFragment<TestCaseBottomStartBinding>() {

    override fun onCreateDataBinding(): TestCaseBottomStartBinding =
        TestCaseBottomStartBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }
}

class BottomEndTestCaseFragment: TestCaseFragment<TestCaseBottomEndBinding>() {

    override fun onCreateDataBinding(): TestCaseBottomEndBinding =
        TestCaseBottomEndBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }
}

abstract class TestCaseFragment<VB: ViewBinding>: BaseFragment<VB>() {

    protected lateinit var targetView: View

    abstract fun initTargetView()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initTargetView()

        showTooltips()
    }

    private fun showTooltips() {

    }
}