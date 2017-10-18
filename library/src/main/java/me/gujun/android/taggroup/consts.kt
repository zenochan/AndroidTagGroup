package me.gujun.android.taggroup

import android.view.View

/**
 * @author 陈治谋 (513500085@qq.com)
 * @since 2017/9/28
 */

private const val EMPTY_STRING = ""

fun String.Companion.empty() = EMPTY_STRING

/**
 * # 自定义 View 编辑模式执行闭包
 * TODO: add to zk lib
 */
fun View.editMode(action: () -> Unit) {
  if (isInEditMode) action()
}
