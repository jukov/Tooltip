# Tooltip

Tooltip is inspired by [ViewTooltip](https://github.com/florent37/ViewTooltip) lib for showing
tooltips in Android app.

* Automatically adjusted by viewport borders
* Fully customisable: position, padding, margin, colors, corner radius, and more.
* Can be anchored to views in scrollable containers:
    * ScrollView,
    * NestedScrollView
    * HorizontalScrollView
    * RecyclerView
* Minimum Android SDK: **17**

## Setup

### Gradle

**build.gradle** (project)

```gradle
buildscript {
    ...
}

allprojects {
    repositories {
    
        ...
        
        // Add jitpack repository
        maven {
            url "https://jitpack.io"  
        }
    }
}
```

**build.gradle** (app)

```gradle

android {
    ...
}
  
dependencies {
    implementation 'com.github.jukov:tooltip:1'
}
```

## Usage

**styles.xml**

```xml

<!-- Override Tooltip theme -->
<style name="MyTooltipTheme" parent="Tooltip">
    <item name="arrowWidth">16dp</item>
    <item name="arrowHeight">8dp</item>
    <item name="arrowSourceMargin">0dp</item>
    <item name="arrowTargetMargin">0dp</item>
    <item name="cornerRadius">8dp</item>
    <item name="shadowPadding">2dp</item>
    <item name="shadowWidth">4dp</item>
    <item name="tooltipTargetViewMargin">2dp</item>
    <item name="tooltipViewPortMargin">8dp</item>
    <item name="paddingStart">8dp</item>
    <item name="paddingTop">8dp</item>
    <item name="paddingEnd">8dp</item>
    <item name="paddingBottom">8dp</item>
    <item name="backgroundColor">@color/background_default</item>
    <item name="shadowColor">@color/shadow_default</item>
    <item name="borderEnabled">false</item>
    <item name="borderColor">@android:color/transparent</item>
    <item name="borderWidth">0dp</item>
    <item name="cancelable">true</item>
    <item name="clickToHide">true</item>
    <item name="autoHide">false</item>
    <item name="autoHideAfterMillis">0</item>
    <item name="dimEnabled">false</item>
    <item name="dimColor">@android:color/transparent</item>
    <item name="dimTargetViewCornerRadius">8dp</item>
    <item name="dimTargetViewPadding">1dp</item>
</style>
```

**Code**

```kotlin

//Init tooltip for Fragment
tooltip = TooltipBuilder(
    fragment = this,
    targetView = targetView,
    tooltipLayoutRes = R.layout.tooltip_layout, // Any layout can be shown in Tooltip,
)
    .setTheme(R.style.MyTooltipTheme)
    .setPosition(Tooltip.Position.TOP)
    .setTooltipAnimation(FadeTooltipAnimation())
    .setOnDisplayListener { ... }
    .setOnHideListener { ... }
    .show()

//Init tooltip for Activity
tooltip = TooltipBuilder(
    activity = this,
    targetView = targetView,
    tooltipLayoutRes = R.layout.tooltip_layout, // Any layout can be shown in Tooltip,
)
    .setTheme(R.style.MyTooltipTheme)
    .setPosition(Tooltip.Position.TOP)
    .setTooltipAnimation(FadeTooltipAnimation())
    .setOnDisplayListener { ... }
    .setOnHideListener { ... }
    .show()

```

## License

```txt
Copyright 2023 Alexandr Zhukov 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```