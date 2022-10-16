package de.fabmax.kool.demo.uidemo

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MdColor

class BasicUiWindow(val uiDemo: UiDemo) : UiDemo.DemoWindow {

    private val windowState = WindowState().apply { setWindowBounds(Dp(200f), Dp(200f), Dp(500f), Dp(700f)) }

    private val clickCnt = mutableStateOf(0)
    private val scrollState = ScrollState()
    private val listState = LazyListState()
    private val listItems = mutableStateListOf<String>().apply { for (i in 1..500) { add("Item $i") } }
    private val hoveredListItem = mutableStateOf<String?>(null)

    private val radioButtonState = mutableStateOf(false)
    private val checkboxState = mutableStateOf(false)
    private val switchState = mutableStateOf(false)
    private val sliderValue = mutableStateOf(50f)
    private val checkboxTooltipState = MutableTooltipState()

    private val comboBoxItems = mutableStateListOf<String>().apply { for (i in 1..8) { add("Item $i") } }
    private val comboBoxSelection = mutableStateOf(1)

    private val text1 = mutableStateOf("")
    private val text2 = mutableStateOf("")

    override val windowSurface: UiSurface = Window(windowState, name = "Demo Window") {
        surface.sizes = uiDemo.selectedUiSize.use()
        surface.colors = uiDemo.selectedColors.use()

        TitleBar(onCloseAction = { uiDemo.closeWindow(this@BasicUiWindow, it.ctx) })
        WindowContent(listItems)
    }
    override val windowScope: WindowScope = windowSurface.windowScope!!

    fun UiScope.WindowContent(listItems: MutableList<String>) = Column(Grow.Std, Grow.Std) {
        modifier
            .padding(horizontal = sizes.gap, vertical = sizes.largeGap)

        Row {
            modifier.margin(bottom = sizes.smallGap)
            Button("A regular button: clicked ${clickCnt.use()} times") {
                modifier
                    .onClick { clickCnt.value += 1 }
            }
        }

        Row {
            modifier.margin(bottom = sizes.smallGap)
            Text("Checkbox") { modifier.alignY(AlignmentY.Center) }
            Checkbox(checkboxState.use()) {
                modifier.margin(sizes.gap).onToggle { checkboxState.set(it) }
                Tooltip(checkboxTooltipState, "A simple checkbox")
            }

            Text("Radio Button") { modifier.alignY(AlignmentY.Center).margin(start = sizes.largeGap) }
            RadioButton(radioButtonState.use()) {
                modifier.margin(sizes.gap).onToggle { radioButtonState.set(it) }
            }

            Text("Switch") { modifier.alignY(AlignmentY.Center).margin(start = sizes.largeGap) }
            Switch(switchState.use()) {
                modifier.margin(sizes.gap).onToggle { switchState.set(it) }
            }
        }

        Row {
            modifier.margin(bottom = sizes.smallGap)
            Text("Text fields") { modifier.alignY(AlignmentY.Center).margin(end = sizes.gap) }
            TextField(text1.use()) {
                modifier
                    .width(150.dp)
                    .hint("Ctrl+C to copy")
                    .onChange { text1.set(it) }
            }
            TextField(text2.use()) {
                modifier
                    .width(150.dp)
                    .hint("Ctrl+V to paste")
                    .margin(start = sizes.largeGap)
                    .onChange { text2.set(it) }
            }
        }

        Row {
            modifier.margin(bottom = sizes.smallGap)
            Text("Slider") { modifier.alignY(AlignmentY.Center) }
            Slider(sliderValue.use(), 0f, 100f) {
                modifier
                    .margin(sizes.gap)
                    .width(sizes.largeGap * 6f)
                    .onChange { sliderValue.set(it) }
            }
            Text("${sliderValue.use().toInt()}") { modifier.alignY(AlignmentY.Center).margin(start = sizes.gap) }
        }

        Row {
            Text("Combo-box") { modifier.alignY(AlignmentY.Center) }
            ComboBox {
                modifier
                    .margin(start = sizes.gap)
                    .width(sizes.largeGap * 6f)
                    .items(comboBoxItems)
                    .selectedIndex(comboBoxSelection.use())
                    .onItemSelected { comboBoxSelection.set(it) }
            }
        }

        divider(colors.secondaryVariant.withAlpha(0.5f), marginTop = sizes.largeGap, marginBottom = sizes.gap)
        Text("A scroll area:") {
            modifier.margin(bottom = sizes.gap)
        }
        ScrollArea(
            scrollState,
            vScrollbarModifier = {
                it.colors(
                    trackColor = colors.secondaryVariant.withAlpha(0.1f),
                    trackHoverColor = colors.secondaryVariant.withAlpha(0.2f)
                )
            },
            hScrollbarModifier = {
                it.colors(
                    trackColor = colors.secondaryVariant.withAlpha(0.1f),
                    trackHoverColor = colors.secondaryVariant.withAlpha(0.2f)
                )
            }
        ) {
            Column {
                Text("Text with two lines in a slightly larger font:\nThe second line is a tiny bit longer than the first one.") {
                    modifier
                        .margin(sizes.smallGap)
                        .font(sizes.largeText)
                }
                Row {
                    for (tint in listOf(Color.WHITE, MdColor.RED, MdColor.GREEN, MdColor.BLUE)) {
                        Image {
                            modifier
                                .padding(sizes.smallGap)
                                .image(uiDemo.exampleImage)
                                .tint(tint)
                                .imageScale(0.2f)
                        }
                    }
                }
                Row {
                    for (r in TextRotation.values()) {
                        Text("Another text with rotation: \"$r\"") {
                            modifier
                                .margin(sizes.smallGap)
                                .padding(sizes.largeGap)
                                .textRotation(r)
                                .border(RoundRectBorder(colors.primaryVariant, sizes.gap, 2.dp, 6.dp))
                        }
                    }
                }
            }
        }

        divider(colors.secondaryVariant.withAlpha(0.5f), marginTop = sizes.largeGap, marginBottom = sizes.gap)
        Text("A longer list, click items to delete them:") {
            modifier.margin(bottom = sizes.gap)
        }
        LazyList(
            listState,
            vScrollbarModifier = {
                it.colors(
                    trackColor = colors.secondaryVariant.withAlpha(0.1f),
                    trackHoverColor = colors.secondaryVariant.withAlpha(0.2f)
                )
            }
        ) {
            itemsIndexed(listItems) { i, item ->
                val isHovered = item == hoveredListItem.use()
                val bgColor = if (isHovered) {
                    colors.secondary.withAlpha(0.5f)
                } else if (i % 2 == 0) {
                    val bg = if (colors.isLight) Color.BLACK else colors.secondary
                    bg.withAlpha(0.05f)
                } else {
                    null
                }
                val textColor = if (isHovered) colors.onPrimary else colors.onBackground
                val isLarge = (i / 10) % 2 != 0
                val txt = if (isLarge) "$item [large]" else item

                Text(txt) {
                    modifier
                        .textColor(textColor)
                        .textAlignY(AlignmentY.Center)
                        .padding(sizes.smallGap)
                        .width(Grow.Std)
                        .height(if (isLarge) 64.dp else FitContent)
                        .backgroundColor(bgColor)
                        .onHover { hoveredListItem.set(item) }
                        .onExit { hoveredListItem.set(null) }
                        .onClick {
                            listItems.remove(item)
                        }
                }
            }
        }
    }
}