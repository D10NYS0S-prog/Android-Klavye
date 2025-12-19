package com.turkceklavyem

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.graphics.Color

/**
 * Uzun basış popup menüsü yöneticisi
 * Tuşlara uzun basıldığında özel karakterler gösterir
 */
class LongPressPopupManager(private val context: Context) {
    
    private var currentPopup: PopupWindow? = null
    
    companion object {
        private const val POPUP_VERTICAL_OFFSET_DP = 150
    }
    
    // Tuş ID'lerine göre popup karakterleri
    private val popupCharacters = mapOf(
        // T9 tuşları için
        "key_2" to listOf("a", "b", "c", "ç", "2"),
        "key_3" to listOf("d", "e", "f", "3"),
        "key_4" to listOf("g", "ğ", "h", "ı", "i", "4"),
        "key_5" to listOf("j", "k", "l", "5"),
        "key_6" to listOf("m", "n", "o", "ö", "6"),
        "key_7" to listOf("p", "q", "r", "s", "ş", "7"),
        "key_8" to listOf("t", "u", "ü", "v", "8"),
        "key_9" to listOf("w", "x", "y", "z", "9"),
        "key_0" to listOf(" ", "0"),
        "key_1" to listOf(".", ",", "?", "!", "1", ";", ":", "'", "\""),
        
        // T12 tuşları için
        "key_qw" to listOf("q", "w", "Q", "W"),
        "key_er" to listOf("e", "r", "E", "R"),
        "key_ty" to listOf("t", "y", "T", "Y"),
        "key_ui" to listOf("u", "ü", "ı", "i", "U", "Ü", "I", "İ"),
        "key_op" to listOf("o", "ö", "p", "O", "Ö", "P"),
        "key_as" to listOf("a", "s", "A", "S"),
        "key_df" to listOf("d", "f", "D", "F"),
        "key_gh" to listOf("g", "ğ", "h", "G", "Ğ", "H"),
        "key_jk" to listOf("j", "k", "J", "K"),
        "key_l" to listOf("l", "-", "_", "L"),
        "key_zx" to listOf("z", "x", "Z", "X"),
        "key_cv" to listOf("c", "ç", "v", "C", "Ç", "V"),
        "key_bn" to listOf("b", "n", "B", "N"),
        "key_m" to listOf("m", "'", "M", "\""),
        "key_comma" to listOf(",", ";", "<"),
        "key_dot" to listOf(".", "!", "?", ">")
    )
    
    fun showPopup(anchorView: View, keyId: String, onCharSelected: (String) -> Unit) {
        dismissPopup()
        
        val characters = popupCharacters[keyId] ?: return
        
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_characters, null)
        val container = popupView.findViewById<LinearLayout>(R.id.popup_container)
        
        // Her karakter için buton oluştur
        characters.forEach { char ->
            val button = Button(context).apply {
                text = if (char == " ") "␣" else char
                textSize = 20f
                setPadding(20, 20, 20, 20)
                setBackgroundColor(Color.parseColor("#F5F5F5"))
                setTextColor(Color.BLACK)
                
                setOnClickListener {
                    onCharSelected(char)
                    dismissPopup()
                }
            }
            container.addView(button)
        }
        
        // PopupWindow oluştur
        currentPopup = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 10f
            // Popup'ı tuşun üstünde göster (dp to px conversion)
            val offsetPx = (POPUP_VERTICAL_OFFSET_DP * context.resources.displayMetrics.density).toInt()
            showAsDropDown(anchorView, 0, -(anchorView.height + offsetPx), Gravity.CENTER)
        }
    }
    
    fun dismissPopup() {
        currentPopup?.dismiss()
        currentPopup = null
    }
    
    fun isShowing(): Boolean {
        return currentPopup?.isShowing == true
    }
}
