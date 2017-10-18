package me.gujun.android.taggroup

import android.view.KeyEvent
import android.view.inputmethod.InputConnectionWrapper

/**
 * # Solve edit text delete(backspace) key detect
 * - [Android: Backspace in WebView/BaseInputConnection](http://stackoverflow.com/a/14561345/3790554)
 */
class ZanyInputConnection(target: android.view.inputmethod.InputConnection, mutable: Boolean) : InputConnectionWrapper(target, mutable) {

  override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
    // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
    return if (beforeLength == 1 && afterLength == 0) {
      // backspace
      val eventA = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
      val eventB = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)
      sendKeyEvent(eventA) && sendKeyEvent(eventB)
    } else super.deleteSurroundingText(beforeLength, afterLength)
  }
}
