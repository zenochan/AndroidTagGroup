package me.gujun.android.taggroup

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.ArrowKeyMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import me.gujun.android.taggroup.TagGroup.Companion.MODE_APPEND
import me.gujun.android.taggroup.TagGroup.Companion.MODE_DISPLAY
import me.gujun.android.taggroup.TagGroup.Companion.MODE_MULTI_CHOOSE
import me.gujun.android.taggroup.TagGroup.Companion.MODE_SINGLE_CHOOSE
import java.util.*

/**
 * A `TagGroup` is a special layout with a set of tags.
 * This group has two modes:
 *
 * #### [mode]
 *
 * 1. [MODE_DISPLAY]
 * 2. [MODE_APPEND]
 * 3. [MODE_SINGLE_CHOOSE]
 * 4. [MODE_MULTI_CHOOSE]
 *
 * Default is [MODE_DISPLAY]. When in [MODE_APPEND], the group is capable of input for append new tags
 * and delete tags.
 *
 *
 * When in [MODE_DISPLAY], the group is only contain NORMAL state tags, and the tags in group
 * is not focusable.
 *
 * ### API
 * 1. [setTags] set tags
 * 1. [getCheckedFirst] get first checked tag index, maybe [Unit]
 * 2. [getChecked] get all checked tag indexes
 *
 * @author Jun Gu (http://2dxgujun.com), ZenoChan (513500085@qq.com)
 * @version 2.0
 * @since 2015-2-3 14:16:32
 */
class TagGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.tagGroupStyle)
  : ViewGroup(context, attrs, defStyleAttr) {

  companion object {
    const val MODE_DISPLAY = 0
    const val MODE_APPEND = 1
    const val MODE_SINGLE_CHOOSE = 2
    const val MODE_MULTI_CHOOSE = 3


    const val TAG_VIEW_STATE_NORMAL = 1
    const val TAG_VIEW_STATE_INPUT = 2

    /** The offset to the text. */
    const val TAG_VIEW_CHECKED_MARKER_OFFSET = 3

    /** The stroke width of the checked marker */
    const val TAG_VIEW_CHECKED_MARKER_STROKE_WIDTH = 4
  }

  private val default_border_color = Color.rgb(0x49, 0xC1, 0x20)
  private val default_text_color = Color.rgb(0x49, 0xC1, 0x20)
  private val default_background_color = Color.WHITE
  private val default_dash_border_color = Color.rgb(0xAA, 0xAA, 0xAA)
  private val default_input_hint_color = Color.argb(0x80, 0x00, 0x00, 0x00)
  private val default_input_text_color = Color.argb(0xDE, 0x00, 0x00, 0x00)
  private val default_checked_border_color = Color.rgb(0x49, 0xC1, 0x20)
  private val default_checked_text_color = Color.WHITE
  private val default_checked_marker_color = Color.WHITE
  private val default_checked_background_color = Color.rgb(0x49, 0xC1, 0x20)
  private val default_pressed_background_color = Color.rgb(0xED, 0xED, 0xED)
  private val default_border_stroke_width: Float
  private val default_text_size: Float
  private val default_horizontal_spacing: Float
  private val default_vertical_spacing: Float
  private val default_horizontal_padding: Float
  private val default_vertical_padding: Float

  /** one of [MODE_DISPLAY], [MODE_APPEND], [MODE_SINGLE_CHOOSE], [MODE_MULTI_CHOOSE] */
  var mode: Int = 0

  /** The text to be displayed when the text of the INPUT tag is empty.  */
  private var inputHint: CharSequence? = null

  /** The tag outline border color.  */
  private var borderColor: Int = 0

  /** The tag text color.  */
  private var textColor: Int = 0

  /** The tag background color.  */
  private var mBackgroundColor: Int = 0

  /** The dash outline border color.  */
  private var dashBorderColor: Int = 0

  /** The  input tag hint text color.  */
  private var inputHintColor: Int = 0

  /** The input tag type text color.  */
  private var inputTextColor: Int = 0

  /** The checked tag outline border color.  */
  private var checkedBorderColor: Int = 0

  /** The check text color  */
  private var checkedTextColor: Int = 0

  /** The checked marker color.  */
  private var checkedMarkerColor: Int = 0

  /** The checked tag background color.  */
  private var checkedBackgroundColor: Int = 0

  /** The tag background color, when the tag is being pressed.  */
  private var pressedBackgroundColor: Int = 0

  /** The tag outline border stroke width, default is 0.5dp.  */
  private var borderStrokeWidth: Float = 0.toFloat()
  private var borderRadius = -1

  /** The tag text size, default is 13sp.  */
  private var textSize: Float = 0.toFloat()

  /** The horizontal tag spacing, default is 8.0dp.  */
  private var horizontalSpacing: Int = 0

  /** The vertical tag spacing, default is 4.0dp.  */
  private var verticalSpacing: Int = 0

  /** The horizontal tag padding, default is 12.0dp.  */
  private var horizontalPadding: Int = 0

  /** The vertical tag padding, default is 3.0dp.  */
  private var verticalPadding: Int = 0

  var onTagDelete: ((TagGroup, String) -> Unit)? = null

  var onTagAppend: ((TagGroup, String) -> Unit)? = null

  /** Listener used to dispatch tag click event.  */
  var onTagClick: ((String) -> Unit)? = null

  /** Listener used to handle tag click event.  */
  private val mInternalTagClickListener = InternalTagClickListener()

  /**
   * Returns the INPUT tag view in this group.
   *
   * @return the INPUT state tag view or null if not exists
   */
  protected val inputTag: TagView?
    get() = when (mode) {
      MODE_APPEND -> {
        val inputTagIndex = childCount - 1
        val inputTag = getTagAt(inputTagIndex)
        if (inputTag != null && inputTag.mState == TAG_VIEW_STATE_INPUT) {
          inputTag
        } else {
          null
        }
      }
      else -> null
    }


  /**
   * Returns the INPUT state tag in this group.
   *
   * @return the INPUT state tag view or null if not exists
   */
  val inputTagText: String?
    get() {
      val inputTagView = inputTag
      return inputTagView?.text?.toString()
    }

  /**
   * Return the last NORMAL state tag view in this group.
   *
   * @return the last NORMAL state tag view or null if not exists
   */
  protected val lastNormalTagView: TagView?
    get() {
      val lastNormalTagIndex = if (mode == MODE_APPEND) childCount - 2 else childCount - 1
      return getTagAt(lastNormalTagIndex)
    }

  /**
   * Returns the tag array in group, except the INPUT tag.
   *
   * @return the tag array.
   */
  val tags: Array<String>
    get() {
      val count = childCount
      val tagList = ArrayList<String>()
      for (i in 0 until count) {
        val tagView = getTagAt(i)
        if (tagView!!.mState == TAG_VIEW_STATE_NORMAL) {
          tagList.add(tagView.text.toString())
        }
      }

      return tagList.toTypedArray()
    }

  /**
   * Returns the checked tag view in the group.
   *
   * @return the checked tag view or null if not exists.
   */
  protected val checkedTag: TagView?
    get() {
      val checkedTagIndex = checkedTagIndex
      return if (checkedTagIndex != -1) {
        getTagAt(checkedTagIndex)
      } else null
    }

  /**
   * Return the checked tag index.
   *
   * @return the checked tag index, or -1 if not exists.
   */
  protected val checkedTagIndex: Int
    get() = (0 until childCount).firstOrNull { getTagAt(it)?.isChecked ?: false } ?: -1

  init {
    default_border_stroke_width = dp2px(0.5f)
    default_text_size = sp2px(13.0f)
    default_horizontal_spacing = dp2px(8.0f)
    default_vertical_spacing = dp2px(4.0f)
    default_horizontal_padding = dp2px(12.0f)
    default_vertical_padding = dp2px(3.0f)

    // Load styled attributes.
    val a = context.obtainStyledAttributes(attrs, R.styleable.TagGroup, defStyleAttr, R.style.TagGroup)
    with(a) {
      mode = getInt(R.styleable.TagGroup_atg_mode, 0)
      println("fuck--->$mode")
      inputHint = getText(R.styleable.TagGroup_atg_inputHint)
      borderColor = getColor(R.styleable.TagGroup_atg_borderColor, default_border_color)
      textColor = getColor(R.styleable.TagGroup_atg_textColor, default_text_color)
      mBackgroundColor = getColor(R.styleable.TagGroup_atg_backgroundColor, default_background_color)
      dashBorderColor = getColor(R.styleable.TagGroup_atg_dashBorderColor, default_dash_border_color)
      inputHintColor = getColor(R.styleable.TagGroup_atg_inputHintColor, default_input_hint_color)
      inputTextColor = getColor(R.styleable.TagGroup_atg_inputTextColor, default_input_text_color)
      checkedBorderColor = getColor(R.styleable.TagGroup_atg_checkedBorderColor, default_checked_border_color)
      checkedTextColor = getColor(R.styleable.TagGroup_atg_checkedTextColor, default_checked_text_color)
      checkedMarkerColor = getColor(R.styleable.TagGroup_atg_checkedMarkerColor, default_checked_marker_color)
      checkedBackgroundColor = getColor(R.styleable.TagGroup_atg_checkedBackgroundColor, default_checked_background_color)
      pressedBackgroundColor = getColor(R.styleable.TagGroup_atg_pressedBackgroundColor, default_pressed_background_color)
      borderStrokeWidth = getDimension(R.styleable.TagGroup_atg_borderStrokeWidth, default_border_stroke_width)
      textSize = getDimension(R.styleable.TagGroup_atg_textSize, default_text_size)
      horizontalSpacing = getDimension(R.styleable.TagGroup_atg_horizontalSpacing, default_horizontal_spacing).toInt()
      verticalSpacing = getDimension(R.styleable.TagGroup_atg_verticalSpacing, default_vertical_spacing).toInt()
      horizontalPadding = getDimension(R.styleable.TagGroup_atg_horizontalPadding, default_horizontal_padding).toInt()
      verticalPadding = getDimension(R.styleable.TagGroup_atg_verticalPadding, default_vertical_padding).toInt()
      borderRadius = getDimensionPixelOffset(R.styleable.TagGroup_atg_borderRadius, borderRadius)
    }

    a.recycle()

    if (mode == MODE_APPEND) {
      // Append the initial INPUT tag.
      appendInputTag()

      // Set the click listener to detect the end-input event.
      setOnClickListener { submitTag() }
    }

    editMode { setTags("There", "Are", "Some", "Test", "Tags") }
  }

  /**
   * Call this to submit the INPUT tag.
   */
  fun submitTag() {
    val inputTag = inputTag ?: return

    if (inputTag.isInputAvailable) {
      inputTag.endInput()
      onTagAppend?.invoke(this@TagGroup, inputTag.text.toString())
      appendInputTag()
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
    val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

    measureChildren(widthMeasureSpec, heightMeasureSpec)

    var width: Int
    var height = 0

    var row = 0 // The row counter.
    var rowWidth = 0 // Calc the current row width.
    var rowMaxHeight = 0 // Calc the max tag height, in current row.

    val count = childCount
    for (i in 0 until count) {
      val child = getChildAt(i)
      val childWidth = child.measuredWidth
      val childHeight = child.measuredHeight

      if (child.visibility != View.GONE) {
        rowWidth += childWidth
        if (rowWidth > widthSize) { // Next line.
          rowWidth = childWidth // The next row width.
          height += rowMaxHeight + verticalSpacing
          rowMaxHeight = childHeight // The next row max height.
          row++
        } else { // This line.
          rowMaxHeight = Math.max(rowMaxHeight, childHeight)
        }
        rowWidth += horizontalSpacing
      }
    }
    // Account for the last row height.
    height += rowMaxHeight

    // Account for the padding too.
    height += paddingTop + paddingBottom

    // If the tags grouped in one row, set the width to wrap the tags.
    if (row == 0) {
      width = rowWidth
      width += paddingLeft + paddingRight
    } else {// If the tags grouped exceed one line, set the width to match the parent.
      width = widthSize
    }

    setMeasuredDimension(if (widthMode == View.MeasureSpec.EXACTLY) widthSize else width,
        if (heightMode == View.MeasureSpec.EXACTLY) heightSize else height)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val parentLeft = paddingLeft
    val parentRight = r - l - paddingRight
    val parentTop = paddingTop
    val parentBottom = b - t - paddingBottom

    var childLeft = parentLeft
    var childTop = parentTop

    var rowMaxHeight = 0

    val count = childCount
    for (i in 0 until count) {
      val child = getChildAt(i)
      val width = child.measuredWidth
      val height = child.measuredHeight

      if (child.visibility != View.GONE) {
        if (childLeft + width > parentRight) { // Next line
          childLeft = parentLeft
          childTop += rowMaxHeight + verticalSpacing
          rowMaxHeight = height
        } else {
          rowMaxHeight = Math.max(rowMaxHeight, height)
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height)

        childLeft += width + horizontalSpacing
      }
    }
  }

  public override fun onSaveInstanceState(): Parcelable? {
    val superState = super.onSaveInstanceState()!!
    val ss = SavedState(superState)
    ss.tags = tags
    ss.checkedPosition = checkedTagIndex
    if (inputTag != null) {
      ss.input = inputTag!!.text.toString()
    }
    return ss
  }

  public override fun onRestoreInstanceState(state: Parcelable) {
    if (state !is SavedState) {
      super.onRestoreInstanceState(state)
      return
    }

    super.onRestoreInstanceState(state.superState)

    setTags(*state.tags)
    val checkedTagView = getTagAt(state.checkedPosition)
    checkedTagView?.isChecked = true
    inputTag?.text = state.input
  }

  /**
   * @see [setTags]
   */
  fun setTags(tagList: List<String>) = setTags(*tagList.toTypedArray())


  /**
   * Set the tags. It will remove all previous tags first.
   *
   * @param tags the tag list to set.
   */
  fun setTags(vararg tags: String) {
    removeAllViews()
    tags.forEach { appendTag(it) }

    if (mode == MODE_APPEND) {
      appendInputTag()
    }
  }

  fun setChecked(vararg index: Int) {
    (0 until childCount).forEach {
      val view = getChildAt(it) as TagView
      if (mode == MODE_SINGLE_CHOOSE) {
        view.isChecked = it == index[0]
      } else if (mode == MODE_MULTI_CHOOSE) {
        view.isChecked = it in index
      }
    }
  }

  fun getCheckedFirst(): Int? = (0 until childCount).firstOrNull { (getChildAt(it) as TagView).isChecked }
  fun getChecked(): List<Int> = (0 until childCount).filter { (getChildAt(it) as TagView).isChecked }

  /**
   * Returns the tag view at the specified position in the group.
   *
   * @param index the position at which to get the tag view from.
   * @return the tag view at the specified position or null if the position
   * does not exists within this group.
   */
  protected fun getTagAt(index: Int): TagView? {
    return getChildAt(index) as TagView?
  }

  /**
   * Append a INPUT tag to this group. It will throw an exception if there has a previous INPUT tag.
   *
   * @param tag the tag text.
   */
  protected fun appendInputTag(tag: String? = null) {
    val previousInputTag = inputTag
    if (previousInputTag != null) {
      throw IllegalStateException("Already has a INPUT tag in group.")
    }

    val newInputTag = TagView(context, TAG_VIEW_STATE_INPUT, tag)
    newInputTag.setOnClickListener(mInternalTagClickListener)
    addView(newInputTag)
  }

  /**
   * Append tag to this group.
   *
   * @param tag the tag to append.
   */
  protected fun appendTag(tag: CharSequence) {
    val newTag = TagView(context, TAG_VIEW_STATE_NORMAL, tag)
    newTag.setOnClickListener(mInternalTagClickListener)
    editMode {
      if (mode == MODE_MULTI_CHOOSE) {
        newTag.isChecked = Random().nextBoolean()
      } else if (mode == MODE_SINGLE_CHOOSE) {
        newTag.isChecked = childCount == 1
      }
    }
    addView(newTag)
  }

  fun dp2px(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        resources.displayMetrics)
  }

  fun sp2px(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
        resources.displayMetrics)
  }

  override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
    return TagGroup.LayoutParams(context, attrs)
  }

  protected fun deleteTag(tagView: TagView) {
    removeView(tagView)
    onTagDelete?.invoke(this@TagGroup, tagView.text.toString())
  }

  /**
   * Interface definition for a callback to be invoked when a tag group is changed.
   */
  interface OnTagChangeListener {
    /**
     * Called when a tag has been appended to the group.
     *
     * @param tag the appended tag.
     */
    fun onAppend(tagGroup: TagGroup, tag: String)

    /**
     * Called when a tag has been deleted from the the group.
     *
     * @param tag the deleted tag.
     */
    fun onDelete(tagGroup: TagGroup, tag: String)
  }

  /**
   * Per-child layout information for layouts.
   */
  class LayoutParams : ViewGroup.LayoutParams {
    constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {}

    constructor(width: Int, height: Int) : super(width, height) {}
  }


  /**
   * 内部点击监听
   * The tag view click listener for internal use.
   */
  internal inner class InternalTagClickListener : View.OnClickListener {
    override fun onClick(tag: View) = with(tag as TagView) {
      when (mode) {
        MODE_APPEND -> if (tag.mState == TAG_VIEW_STATE_INPUT) {
          // If the clicked tag is in INPUT state, uncheck the previous checked tag if exists.
          val checkedTag = checkedTag
          checkedTag?.isChecked = false
        } else {
          // If the clicked tag is currently checked, delete the tag.
          if (tag.isChecked) {
            deleteTag(tag)
          } else {
            // If the clicked tag is unchecked, uncheck the previous checked tag if exists,
            // then check the clicked tag.
            val checkedTag = checkedTag
            checkedTag?.isChecked = false
            tag.isChecked = true
          }
        }
        MODE_SINGLE_CHOOSE -> {
          (0 until this@TagGroup.childCount).forEach {
            val view = this@TagGroup.getChildAt(it) as TagView
            if (tag == view) {
              view.isChecked = !view.isChecked
            } else {
              view.isChecked = false
            }
          }
        }
        MODE_MULTI_CHOOSE -> {
          (0 until this@TagGroup.childCount).forEach {
            val view = this@TagGroup.getChildAt(it) as TagView
            if (tag == view) view.isChecked = !view.isChecked
          }
        }
        else -> onTagClick?.invoke(tag.text.toString())

      }

      Unit
    }
  }

  /**
   * The tag view which has two states can be either NORMAL or INPUT.
   * @param mState The current state
   */
  inner class TagView(context: Context, internal var mState: Int, text: CharSequence?) : TextView(context) {


    /**
     * Indicates the tag if checked.
     * Set whether this tag view is in the checked state.
     *
     * @param checked true is checked, false otherwise
     */
    internal var isChecked = false
      set(value) {
        field = value
        // Make the checked mark drawing region.
        val paddingR = if (isChecked && mode == MODE_APPEND)
          (horizontalPadding.toFloat() + height / 2.5f + TAG_VIEW_CHECKED_MARKER_OFFSET.toFloat()).toInt()
        else
          horizontalPadding
        setPadding(horizontalPadding, verticalPadding, paddingR, verticalPadding)
        invalidatePaint()
      }


    /** Indicates the tag if pressed.  */
    internal var isPressed = false

    private val mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mCheckedMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** The rect for the tag's left corner drawing.  */
    private val mLeftCornerRectF = RectF()
    private val cornersRect = RectF()

    /** The rect for the tag's right corner drawing.  */
    private val mRightCornerRectF = RectF()

    /** The rect for the tag's horizontal blank fill area.  */
    private val mHorizontalBlankFillRectF = RectF()

    /** The rect for the tag's vertical blank fill area.  */
    private val mVerticalBlankFillRectF = RectF()

    /** The rect for the checked mark draw bound.  */
    private val mCheckedMarkerBound = RectF()

    /** Used to detect the touch event.  */
    private val mOutRect = Rect()

    /** The path for draw the tag's outline border.  */
    private val mBorderPath = Path()

    /** The path effect provide draw the dash border.  */
    private val mPathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)

    /**
     * Indicates whether the input content is available.
     *
     * @return True if the input content is available, false otherwise.
     */
    val isInputAvailable: Boolean
      get() = text != null && text.length > 0

    init {
      mBorderPaint.style = Paint.Style.STROKE
      mBorderPaint.strokeWidth = borderStrokeWidth
      mBackgroundPaint.style = Paint.Style.FILL
      mCheckedMarkerPaint.style = Paint.Style.FILL
      mCheckedMarkerPaint.strokeWidth = TAG_VIEW_CHECKED_MARKER_STROKE_WIDTH.toFloat()
      mCheckedMarkerPaint.color = checkedMarkerColor
    }


    init {
      setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
      layoutParams = TagGroup.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT)

      gravity = Gravity.CENTER
      setText(text)
      setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

      isClickable = mode != MODE_DISPLAY
      isFocusable = mState == TAG_VIEW_STATE_INPUT
      isFocusableInTouchMode = mState == TAG_VIEW_STATE_INPUT
      hint = if (mState == TAG_VIEW_STATE_INPUT) inputHint else null
      movementMethod = if (mState == TAG_VIEW_STATE_INPUT) ArrowKeyMovementMethod.getInstance() else null

      // Interrupted long click event to avoid PAUSE popup.
      setOnLongClickListener { mState != TAG_VIEW_STATE_INPUT }

      if (mState == TAG_VIEW_STATE_INPUT) {
        requestFocus()

        // Handle the ENTER key down.
        setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
          if (actionId == EditorInfo.IME_NULL && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER
              && event.action == KeyEvent.ACTION_DOWN)) {
            if (isInputAvailable) {
              // If the input content is available, end the input and dispatch
              // the event, then append a new INPUT state tag.
              endInput()
              onTagAppend?.invoke(this@TagGroup, getText().toString())
              appendInputTag()
            }
            return@OnEditorActionListener true
          }
          false
        })

        // Handle the BACKSPACE key down.
        // 处理回车键
        setOnKeyListener { _, keyCode, event ->
          if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            // If the input content is empty, check or remove the last NORMAL state tag.
            if (TextUtils.isEmpty(getText().toString())) {
              val lastNormalTagView = lastNormalTagView
              if (lastNormalTagView != null) {
                if (lastNormalTagView.isChecked) {
                  removeView(lastNormalTagView)
                  onTagDelete?.invoke(this@TagGroup, lastNormalTagView.text.toString())
                } else {
                  val checkedTagView = checkedTag
                  checkedTagView?.isChecked = false
                  lastNormalTagView.isChecked = true
                }
                return@setOnKeyListener true
              }
            }
          }
          false
        }

        // Handle the INPUT tag content changed.
        addTextChangedListener(object : TextWatcher {
          override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // When the INPUT state tag changed, uncheck the checked tag if exists.
            val checkedTagView = checkedTag
            checkedTagView?.isChecked = false
          }

          override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

          override fun afterTextChanged(s: Editable) {}
        })
      }

      invalidatePaint()
    }

    /**
     * Call this method to end this tag's INPUT state.
     */
    fun endInput() {
      // Make the view not focusable.
      isFocusable = false
      isFocusableInTouchMode = false
      // Set the hint empty, make the TextView measure correctly.
      hint = null
      // Take away the cursor.
      movementMethod = null

      mState = TAG_VIEW_STATE_NORMAL
      invalidatePaint()
      requestLayout()
    }

    override fun getDefaultEditable(): Boolean {
      return true
    }

    private fun invalidatePaint() {
      when (mode) {
        MODE_DISPLAY -> {
          mBorderPaint.color = borderColor
          mBackgroundPaint.color = mBackgroundColor
          setTextColor(textColor)
        }
        else -> {
          if (mState == TAG_VIEW_STATE_INPUT) {
            mBorderPaint.color = dashBorderColor
            mBorderPaint.pathEffect = mPathEffect
            mBackgroundPaint.color = mBackgroundColor
            setHintTextColor(inputHintColor)
            setTextColor(inputTextColor)
          } else {
            mBorderPaint.pathEffect = null
            if (isChecked) {
              mBorderPaint.color = checkedBorderColor
              mBackgroundPaint.color = checkedBackgroundColor
              setTextColor(checkedTextColor)
            } else {
              mBorderPaint.color = borderColor
              mBackgroundPaint.color = mBackgroundColor
              setTextColor(textColor)
            }
          }

        }
      }

      if (isPressed) mBackgroundPaint.color = pressedBackgroundColor
    }

    override fun onDraw(canvas: Canvas) {

      if (borderRadius > 0) {
        val w = width
        val h = height
        val d = borderRadius * 2
        mBorderPath.reset()
        //lt
        cornersRect.set(1f, 1f, d.toFloat(), d.toFloat())
        mBorderPath.addArc(cornersRect, 180f, 90f)
        //t
        mBorderPath.lineTo((w - borderRadius).toFloat(), 1f)
        //rt
        cornersRect.set((w - d).toFloat(), 1f, (w - 1).toFloat(), d.toFloat())
        mBorderPath.addArc(cornersRect, -90f, 90f)
        //r
        mBorderPath.lineTo((w - 1).toFloat(), (h - borderRadius).toFloat())
        //rb
        cornersRect.set((w - d - 1).toFloat(), (h - d - 1).toFloat(), (w - 1).toFloat(), (h - 1).toFloat())
        mBorderPath.addArc(cornersRect, 0f, 90f)
        //b
        mBorderPath.lineTo(borderRadius.toFloat(), (h - 1).toFloat())
        //lb
        cornersRect.set(1f, (h - d).toFloat(), d.toFloat(), (h - 1).toFloat())
        mBorderPath.addArc(cornersRect, 90f, 90f)
        //l
        mBorderPath.lineTo(1f, borderRadius.toFloat())

        mBackgroundPaint.style = Paint.Style.FILL
        canvas.drawPath(mBorderPath, mBackgroundPaint)
      }
      canvas.drawArc(mLeftCornerRectF, -180f, 90f, true, mBackgroundPaint)
      canvas.drawArc(mLeftCornerRectF, -270f, 90f, true, mBackgroundPaint)
      canvas.drawArc(mRightCornerRectF, -90f, 90f, true, mBackgroundPaint)
      canvas.drawArc(mRightCornerRectF, 0f, 90f, true, mBackgroundPaint)
      canvas.drawRect(mHorizontalBlankFillRectF, mBackgroundPaint)
      canvas.drawRect(mVerticalBlankFillRectF, mBackgroundPaint)

      // 叉叉
      if (isChecked && mode == MODE_APPEND) {
        canvas.save()
        canvas.rotate(45f, mCheckedMarkerBound.centerX(), mCheckedMarkerBound.centerY())
        canvas.drawLine(mCheckedMarkerBound.left, mCheckedMarkerBound.centerY(),
            mCheckedMarkerBound.right, mCheckedMarkerBound.centerY(), mCheckedMarkerPaint)
        canvas.drawLine(mCheckedMarkerBound.centerX(), mCheckedMarkerBound.top,
            mCheckedMarkerBound.centerX(), mCheckedMarkerBound.bottom, mCheckedMarkerPaint)
        canvas.restore()
      }

      canvas.drawPath(mBorderPath, mBorderPaint)
      super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      var h = h
      super.onSizeChanged(w, h, oldw, oldh)
      val left = borderStrokeWidth.toInt()
      val top = borderStrokeWidth.toInt()
      val right = (left + w - borderStrokeWidth * 2).toInt()
      val bottom = (top + h - borderStrokeWidth * 2).toInt()

      val d = bottom - top

      mLeftCornerRectF.set(left.toFloat(), top.toFloat(), (left + d).toFloat(), (top + d).toFloat())
      mRightCornerRectF.set((right - d).toFloat(), top.toFloat(), right.toFloat(), (top + d).toFloat())


      mBorderPath.reset()
      mBorderPath.addArc(mLeftCornerRectF, -180f, 90f)
      mBorderPath.addArc(mLeftCornerRectF, -270f, 90f)
      mBorderPath.addArc(mRightCornerRectF, -90f, 90f)
      mBorderPath.addArc(mRightCornerRectF, 0f, 90f)

      val l = (d / 2.0f).toInt()
      mBorderPath.moveTo((left + l).toFloat(), top.toFloat())
      mBorderPath.lineTo((right - l).toFloat(), top.toFloat())

      mBorderPath.moveTo((left + l).toFloat(), bottom.toFloat())
      mBorderPath.lineTo((right - l).toFloat(), bottom.toFloat())

      mBorderPath.moveTo(left.toFloat(), (top + l).toFloat())
      mBorderPath.lineTo(left.toFloat(), (bottom - l).toFloat())

      mBorderPath.moveTo(right.toFloat(), (top + l).toFloat())
      mBorderPath.lineTo(right.toFloat(), (bottom - l).toFloat())

      mHorizontalBlankFillRectF.set(left.toFloat(), (top + l).toFloat(), right.toFloat(), (bottom - l).toFloat())
      mVerticalBlankFillRectF.set((left + l).toFloat(), top.toFloat(), (right - l).toFloat(), bottom.toFloat())

      val m = (h / 2.5f).toInt()
      h = bottom - top
      mCheckedMarkerBound.set(
          (right - m - horizontalPadding + TAG_VIEW_CHECKED_MARKER_OFFSET).toFloat(),
          (top + h / 2 - m / 2).toFloat(),
          (right - horizontalPadding + TAG_VIEW_CHECKED_MARKER_OFFSET).toFloat(),
          (bottom - h / 2 + m / 2).toFloat()
      )

      // Ensure the checked mark drawing region is correct across screen orientation changes.
      if (isChecked) {
        setPadding(horizontalPadding,
            verticalPadding,
            (horizontalPadding.toFloat() + h / 2.5f + TAG_VIEW_CHECKED_MARKER_OFFSET.toFloat()).toInt(),
            verticalPadding)
      }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
      if (mState == TAG_VIEW_STATE_INPUT) {
        // The INPUT tag doesn't change background color on the touch event.
        return super.onTouchEvent(event)
      }

      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          getDrawingRect(mOutRect)
          isPressed = true
          invalidatePaint()
          invalidate()
        }
        MotionEvent.ACTION_MOVE -> {
          if (!mOutRect.contains(event.x.toInt(), event.y.toInt())) {
            isPressed = false
            invalidatePaint()
            invalidate()
          }
        }
        MotionEvent.ACTION_UP -> {
          isPressed = false
          invalidatePaint()
          invalidate()
        }
      }
      return super.onTouchEvent(event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection =
        ZanyInputConnection(super.onCreateInputConnection(outAttrs), true)

  }
}
