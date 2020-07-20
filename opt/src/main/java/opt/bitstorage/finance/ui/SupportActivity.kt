package opt.bitstorage.finance.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.opt_activity_support.*
import opt.bitstorage.finance.R

class SupportActivity: AppCompatActivity(R.layout.opt_activity_support) {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // todo: нужен свой фон?
        //window.setBackgroundDrawableResource(R.drawable.background_main)
        val id = intent?.getStringExtra("ID")
        user_id.text = "USER ID: $id"

        btn_to_back.setOnClickListener {
            onBackPressed()
        }

        btn_to_mail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "support@bitstorage.finance", null))
            startActivity(Intent.createChooser(intent, "Send email..."))
        }

        btn_to_telegram.setOnClickListener {
            val telegram = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://t.me/" + "bitstorage")
            }
            startActivity(telegram)
        }

        btn_to_fd.setOnClickListener {
            val fd = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://bitstorage.freshdesk.com/support/home")
            }
            startActivity(fd)
        }

        btn_to_tutorial.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }
}