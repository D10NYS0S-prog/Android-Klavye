package com.turkceklavyem

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.LayoutInflater
import android.widget.TextView

class T9KeyboardService : InputMethodService() {
    override fun onCreateInputView(): View {
        // Klavye görünümünü bağla
        val inflater = LayoutInflater.from(this)
        return inflater.inflate(R.layout.keyboard_layout, null)
    }
}