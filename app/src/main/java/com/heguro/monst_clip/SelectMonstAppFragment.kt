package com.heguro.monst_clip

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class SelectMonstAppFragment : DialogFragment() {
    internal lateinit var listener: SelectIntentListener

    interface SelectIntentListener {
        fun onSelectIntent(indexes: MutableList<Int>?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SelectIntentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement SelectIntentListener"))
        }
    }

    fun newInstance(selectionLabels: Array<String>): SelectMonstAppFragment? {
        val fragment = SelectMonstAppFragment()
        val args = Bundle()
        args.putStringArray("selectionLabels", selectionLabels)
        fragment.arguments = args
        return fragment
    }

    private var selectionLabels: Array<String>? = null
    private val selectedIndexes = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        selectionLabels = args!!.getStringArray("selectionLabels")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val hoge = selectionLabels ?: throw IllegalStateException("selectionLabels is null")
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.ui_message_select_app_to_join))
                .setMultiChoiceItems(hoge, null) { _, id, checked ->
                    when (checked) {
                        true -> selectedIndexes.add(id)
                        else -> selectedIndexes.remove(id)
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    listener.onSelectIntent(selectedIndexes)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.onSelectIntent(null)
    }

}
