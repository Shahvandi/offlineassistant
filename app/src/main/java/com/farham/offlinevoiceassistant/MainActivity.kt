package com.farham.offlinevoiceassistant

import android.app.AlertDialog
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var result: TextView
    private val aliases = linkedMapOf<String, String>()
    private var recognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAliases()
        buildUi()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10)
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 24, 28, 16) }
        val title = TextView(this).apply { text = "Offline Voice Assistant"; textSize = 26f; setPadding(0, 0, 0, 8) }
        val sub = TextView(this).apply { text = "دستیار صوتی • آماده برای فرمان شما"; textSize = 15f }
        status = TextView(this).apply { text = "وضعیت: آماده"; textSize = 14f; setPadding(0, 18, 0, 10) }
        result = TextView(this).apply { text = "فرمان تشخیص‌داده‌شده اینجا نمایش داده می‌شود."; textSize = 17f; setPadding(0, 12, 0, 18) }
        val speak = Button(this).apply { text = "🎙  صحبت کردن"; setOnClickListener { startListening() } }
        val aliasBtn = Button(this).apply { text = "⚙  مدیریت نام‌های مستعار"; setOnClickListener { aliasDialog() } }
        root.addView(title); root.addView(sub); root.addView(status); root.addView(result)
        root.addView(speak, LinearLayout.LayoutParams(-1, 58)); root.addView(aliasBtn, LinearLayout.LayoutParams(-1, 58))
        val space = Space(this); root.addView(space, LinearLayout.LayoutParams(1, 0, 1f))
        val card = TextView(this).apply { text = "دستیار در پایین صفحه آماده است\nفرمان نمونه: «یوتیوب را باز کن»"; textSize = 14f; gravity = Gravity.CENTER; setPadding(16, 20, 16, 20) }
        root.addView(card, LinearLayout.LayoutParams(-1, 110))
        setContentView(root)
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { status.text = "تشخیص گفتار روی این دستگاه در دسترس نیست."; return }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { r ->
            r.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) { status.text = "وضعیت: گوش می‌دهم…" }
                override fun onResults(b: Bundle?) { val s = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); handleCommand(s) }
                override fun onError(e: Int) { status.text = "وضعیت: آماده (کد خطا $e)" }
                override fun onBeginningOfSpeech() {}; override fun onRmsChanged(v: Float) {}; override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}; override fun onPartialResults(b: Bundle?) {}; override fun onEvent(t: Int, b: Bundle?) {}
            })
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR"); putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true); putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5) }
            r.startListening(i)
        }
    }

    private fun handleCommand(text: String) {
        result.text = if (text.isBlank()) "چیزی تشخیص داده نشد." else "فرمان: $text"
        status.text = "وضعیت: آماده"
        val lower = text.lowercase()
        val target = aliases.entries.firstOrNull { lower.contains(it.key.lowercase()) }?.value
        if (target != null) try { startActivity(packageManager.getLaunchIntentForPackage(target)); return } catch (_: Exception) {}
        if (lower.contains("تنظیم") || lower.contains("نام مستعار")) aliasDialog()
    }

    private fun aliasDialog() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 8, 32, 0) }
        val name = EditText(this).apply { hint = "نام مستعار، مثلا یوتیوب" }
        val pkg = EditText(this).apply { hint = "شناسه برنامه، مثلا com.google.android.youtube" }
        layout.addView(name); layout.addView(pkg)
        AlertDialog.Builder(this).setTitle("افزودن نام مستعار").setView(layout).setPositiveButton("ذخیره") { _, _ -> if (name.text.isNotBlank() && pkg.text.isNotBlank()) { aliases[name.text.toString()] = pkg.text.toString(); saveAliases(); Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show() } }.setNegativeButton("لغو", null).show()
    }

    private fun saveAliases() { getPreferences(MODE_PRIVATE).edit().putString("aliases", aliases.entries.joinToString("|") { "${it.key}=${it.value}" }).apply() }
    private fun loadAliases() { getPreferences(MODE_PRIVATE).getString("aliases", "")?.split("|")?.forEach { val p = it.split("=", limit = 2); if (p.size == 2) aliases[p[0]] = p[1] } }
    override fun onDestroy() { recognizer?.destroy(); super.onDestroy() }
}
