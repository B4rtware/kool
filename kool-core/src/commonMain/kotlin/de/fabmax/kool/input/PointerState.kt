package de.fabmax.kool.input

import de.fabmax.kool.KoolContext

class PointerState {
    val pointers = Array(PointerInput.MAX_POINTERS) { Pointer() }

    private val compatGestureEvaluator = TouchGestureEvaluator()

    /**
     * The primary pointer. For mouse-input that's the mouse cursor, for touch-input it's the first finger
     * that touched the screen. Keep in mind that the returned [Pointer] might be invalid (i.e. [Pointer.isValid] is
     * false) if the cursor exited the window or no finger touches the screen.
     */
    val primaryPointer = pointers[0]

    fun getActivePointers(result: MutableList<Pointer>, consumedMask: Int = PointerInput.CONSUMED_ALL) {
        result.clear()
        // pointers.filter { it.isValid }.forEach { result.add(it) }
        for (i in pointers.indices) {
            if (pointers[i].isValid && !pointers[i].isConsumed(consumedMask)) {
                result.add(pointers[i])
            }
        }
    }

    internal fun onNewFrame(inputPointers: Array<BufferedPointerInput>, lastPtrInput: Double, ctx: KoolContext) {
        for (i in pointers.indices) {
            inputPointers[i].update(pointers[i], lastPtrInput)
        }

        if (PointerInput.isEvaluatingCompatGestures) {
            compatGestureEvaluator.evaluate(this, ctx)
            when (compatGestureEvaluator.currentGesture.type) {
                TouchGestureEvaluator.PINCH -> {
                    // set primary pointer deltaScroll for compatibility with mouse input
                    primaryPointer.consumptionMask = 0
                    primaryPointer.deltaScrollY = compatGestureEvaluator.currentGesture.dPinchAmount / 20.0
                    primaryPointer.x = compatGestureEvaluator.currentGesture.centerCurrent.x
                    primaryPointer.y = compatGestureEvaluator.currentGesture.centerCurrent.y
                    primaryPointer.deltaX = compatGestureEvaluator.currentGesture.dCenter.x
                    primaryPointer.deltaY = compatGestureEvaluator.currentGesture.dCenter.y
                }
                TouchGestureEvaluator.TWO_FINGER_DRAG -> {
                    // set primary pointer right button down for compatibility with mouse input
                    primaryPointer.consumptionMask = 0
                    primaryPointer.x = compatGestureEvaluator.currentGesture.centerCurrent.x
                    primaryPointer.y = compatGestureEvaluator.currentGesture.centerCurrent.y
                    primaryPointer.deltaX = compatGestureEvaluator.currentGesture.dCenter.x
                    primaryPointer.deltaY = compatGestureEvaluator.currentGesture.dCenter.y
                    if (primaryPointer.buttonMask == PointerInput.LEFT_BUTTON_MASK) {
                        primaryPointer.buttonMask = PointerInput.RIGHT_BUTTON_MASK
                        if (compatGestureEvaluator.currentGesture.numUpdates > 1){
                            primaryPointer.buttonEventMask = 0
                        }
                    }
                }
            }
        }
    }
}