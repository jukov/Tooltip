package com.github.jukov.tooltip

import com.github.jukov.tooltip.databinding.TestCaseBottomEndBinding
import com.github.jukov.tooltip.databinding.TestCaseBottomStartBinding
import com.github.jukov.tooltip.databinding.TestCaseCenterBinding
import com.github.jukov.tooltip.databinding.TestCaseTopEndBinding
import com.github.jukov.tooltip.databinding.TestCaseTopStartBinding

class CenterLargeHorizontalTestCaseFragment: TestCaseFragment<TestCaseCenterBinding>() {

    override val name: String = "Center targetView, large horizontal tooltips"

    override fun onCreateDataBinding(): TestCaseCenterBinding =
        TestCaseCenterBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.START)
        showTooltip(Tooltip.Position.END)
    }
}

class CenterLargeVerticalTestCaseFragment: TestCaseFragment<TestCaseCenterBinding>() {

    override val name: String = "Center targetView, large vertical tooltips"

    override fun onCreateDataBinding(): TestCaseCenterBinding =
        TestCaseCenterBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.TOP)
        showTooltip(Tooltip.Position.BOTTOM)
    }
}

class TopStartLargeBottomTestCaseFragment: TestCaseFragment<TestCaseTopStartBinding>() {

    override val name: String = "top|start targetView, large bottom tooltip"

    override fun onCreateDataBinding(): TestCaseTopStartBinding =
        TestCaseTopStartBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.BOTTOM)
    }
}

class TopStartLargeEndTestCaseFragment: TestCaseFragment<TestCaseTopStartBinding>() {

    override val name: String = "top|start targetView, large end tooltip"

    override fun onCreateDataBinding(): TestCaseTopStartBinding =
        TestCaseTopStartBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.END)
    }
}

class TopEndLargeStartTestCaseFragment: TestCaseFragment<TestCaseTopEndBinding>() {

    override val name: String = "top|end targetView, large start tooltip"

    override fun onCreateDataBinding(): TestCaseTopEndBinding =
        TestCaseTopEndBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.START)
    }
}

class TopEndLargeBottomTestCaseFragment: TestCaseFragment<TestCaseTopEndBinding>() {

    override val name: String = "top|end targetView, large bottom tooltip"

    override fun onCreateDataBinding(): TestCaseTopEndBinding =
        TestCaseTopEndBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.BOTTOM)
    }
}

class BottomStartLargeTopTestCaseFragment: TestCaseFragment<TestCaseBottomStartBinding>() {

    override val name: String = "bottom|start targetView, large top tooltip"

    override fun onCreateDataBinding(): TestCaseBottomStartBinding =
        TestCaseBottomStartBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.TOP)
    }
}

class BottomStartLargeEndTestCaseFragment: TestCaseFragment<TestCaseBottomStartBinding>() {

    override val name: String = "bottom|start targetView, large end tooltip"

    override fun onCreateDataBinding(): TestCaseBottomStartBinding =
        TestCaseBottomStartBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.END)
    }
}

class BottomEndLargeStartTestCaseFragment: TestCaseFragment<TestCaseBottomEndBinding>() {

    override val name: String = "bottom|start targetView, large start tooltip"

    override fun onCreateDataBinding(): TestCaseBottomEndBinding =
        TestCaseBottomEndBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.START)
    }
}

class BottomEndLargeTopTestCaseFragment: TestCaseFragment<TestCaseBottomEndBinding>() {

    override val name: String = "bottom|start targetView, large top tooltip"

    override fun onCreateDataBinding(): TestCaseBottomEndBinding =
        TestCaseBottomEndBinding.inflate(layoutInflater)

    override fun initTargetView() {
        targetView = binding.buttonTarget
    }

    override fun showTooltips() {
        showTooltip(Tooltip.Position.TOP)
    }
}