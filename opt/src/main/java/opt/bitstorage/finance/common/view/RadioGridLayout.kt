package opt.bitstorage.finance.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewStructure
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.CompoundButton
import android.widget.GridLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.IdRes
import androidx.core.view.updatePadding
import opt.bitstorage.finance.R

class RadioGridLayout : GridLayout {
    @get:IdRes
    var checkedRadioButtonId = -1
        private set

    private var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var mProtectFromCheckedChange = false
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private var mPassThroughListener: PassThroughHierarchyChangeListener? = null


    private fun setCheckedId(@IdRes id: Int) {
        checkedRadioButtonId = id
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener!!.onCheckedChanged(this, checkedRadioButtonId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val afm: AutofillManager? = context.getSystemService(AutofillManager::class.java)
            afm?.notifyValueChanged(this)
        }

    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        mOnCheckedChangeListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(group: RadioGridLayout?, @IdRes checkedId: Int)
    }

    private var mInitialCheckedId = View.NO_ID

    constructor(context: Context?) : super(context) {
        orientation = VERTICAL
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_AUTO) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
            }
        }
        val attributes: TypedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.RadioGridLayout,
                R.attr.radioButtonStyle, 0)
        val value = attributes.getResourceId(R.styleable.RadioGridLayout_checked, View.NO_ID)
        if (value != View.NO_ID) {
            checkedRadioButtonId = value
            mInitialCheckedId = value
        }
        attributes.recycle()
        init()
    }

    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        mPassThroughListener = PassThroughHierarchyChangeListener()
        super.setOnHierarchyChangeListener(mPassThroughListener)
    }

    override fun setOnHierarchyChangeListener(listener: OnHierarchyChangeListener) {
        mPassThroughListener!!.mOnHierarchyChangeListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (checkedRadioButtonId != -1) {
            mProtectFromCheckedChange = true
            setCheckedStateForView(checkedRadioButtonId, true)
            mProtectFromCheckedChange = false
            setCheckedId(checkedRadioButtonId)
        }
    }


    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is RadioButton) {
            if (child.isChecked) {
                mProtectFromCheckedChange = true
                if (checkedRadioButtonId != -1) {
                    setCheckedStateForView(checkedRadioButtonId, false)
                }
                mProtectFromCheckedChange = false
                setCheckedId(child.id)
            }
        }

        super.addView(child, index, params)
    }

    fun check(@IdRes id: Int) {
        if (id != -1 && id == checkedRadioButtonId) {
            return
        }
        if (checkedRadioButtonId != -1) {
            setCheckedStateForView(checkedRadioButtonId, false)
        }
        if (id != -1) {
            setCheckedStateForView(id, true)
        }
        setCheckedId(id)
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView = findViewById<View>(viewId)
        if (checkedView != null && checkedView is RadioButton) {
            checkedView.isChecked = checked
        }
    }

    fun clearCheck() {
        check(-1)
    }

    override fun generateLayoutParams(attrs: AttributeSet): GridLayout.LayoutParams {
        return GridLayout.LayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is RadioGroup.LayoutParams
    }

    override fun generateDefaultLayoutParams(): GridLayout.LayoutParams {
        return LayoutParams()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return RadioGroup::class.java.name
    }

    class LayoutParams : GridLayout.LayoutParams {
        constructor(rowSpec: Spec?, columnSpec: Spec?) : super(rowSpec, columnSpec)
        constructor() : super()
        constructor(params: ViewGroup.LayoutParams?) : super(params)
        constructor(params: MarginLayoutParams?) : super(params)
        constructor(source: GridLayout.LayoutParams?) : super(source)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

        override fun setBaseAttributes(a: TypedArray,
                                       widthAttr: Int, heightAttr: Int) {
            width = if (a.hasValue(widthAttr)) {
                a.getLayoutDimension(widthAttr, "layout_width")
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
            height = if (a.hasValue(heightAttr)) {
                a.getLayoutDimension(heightAttr, "layout_height")
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }

    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (mProtectFromCheckedChange) {
                return
            }
            mProtectFromCheckedChange = true
            if (checkedRadioButtonId != -1) {
                setCheckedStateForView(checkedRadioButtonId, false)
            }
            mProtectFromCheckedChange = false
            val id = buttonView.id
            setCheckedId(id)
        }
    }

    private inner class PassThroughHierarchyChangeListener : OnHierarchyChangeListener {
        var mOnHierarchyChangeListener: OnHierarchyChangeListener? = null
        override fun onChildViewAdded(parent: View, child: View) {
            if (parent === this@RadioGridLayout && child is RadioButton) {
                var id = child.getId()
                if (id == View.NO_ID) {
                    id = View.generateViewId()
                    child.setId(id)
                }
                child.setOnCheckedChangeListener(
                        mChildOnCheckedChangeListener)
            }
            mOnHierarchyChangeListener?.onChildViewAdded(parent, child)
        }

        override fun onChildViewRemoved(parent: View, child: View) {
            if (parent === this@RadioGridLayout && child is RadioButton) {
                child.setOnCheckedChangeListener(null)
            }
            mOnHierarchyChangeListener?.onChildViewRemoved(parent, child)
        }
    }

    override fun onProvideAutofillStructure(structure: ViewStructure, flags: Int) {
        super.onProvideAutofillStructure(structure, flags)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            structure.setDataIsSensitive(checkedRadioButtonId != mInitialCheckedId)
        }
    }

    override fun autofill(value: AutofillValue) {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!value.isList) {
                //Timber.w(value + " could not be autofilled into " + this);
                return
            }
        }
        var index = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            index = value.listValue
        }
        val child = getChildAt(index)
                ?: //Timber.w("RadioGroup.autoFill(): no child with index %s", index);
                return
        check(child.id)
    }

    @SuppressLint("InlinedApi")
    override fun getAutofillType(): Int {
        return if (isEnabled) View.AUTOFILL_TYPE_LIST else View.AUTOFILL_TYPE_NONE
    }

    override fun getAutofillValue(): AutofillValue? {
        if (!isEnabled) return null
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.id == checkedRadioButtonId) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return AutofillValue.forList(i)
                }
            }
        }
        return null
    }
}