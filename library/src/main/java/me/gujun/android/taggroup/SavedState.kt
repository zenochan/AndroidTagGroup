package me.gujun.android.taggroup

/**
 * @author 陈治谋 (513500085@qq.com)
 * @since 2017/9/28
 */

import android.os.Parcel
import android.os.Parcelable
import android.view.View

/**
 * For [TagGroup] save and restore state.
 */
internal class SavedState : View.BaseSavedState {
  lateinit var tags: Array<String>
  var checkedPosition: Int = 0
  var input: String? = null

  constructor(source: Parcel) : super(source) {
    tags = source.let {
      val tagCount = it.readInt()
      val target = Array(tagCount) { String.empty() }
      it.readStringArray(target)
      target
    }
    checkedPosition = source.readInt()
    input = source.readString()
  }

  constructor(superState: Parcelable) : super(superState)

  override fun writeToParcel(dest: Parcel, flags: Int) {
    super.writeToParcel(dest, flags)
    dest.writeInt(tags.size)
    dest.writeStringArray(tags)
    dest.writeStringList(tags.toList())
    dest.writeInt(checkedPosition)
    dest.writeString(input)
  }

  companion object {
    @JvmStatic
    val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
      override fun createFromParcel(`in`: Parcel): SavedState = SavedState(`in`)
      override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
    }
  }
}

