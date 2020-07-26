package opt.bitstorage.finance.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.opt_activity_support.*
import opt.bitstorage.finance.R

class SupportActivity: AppCompatActivity(R.layout.opt_activity_support) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent?.getStringExtra(EXTRA_ID_SUPPORT)
        user_id.text = getString(R.string.opt_title_user_id, id)

        btn_to_mail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    getString(R.string.opt_email_scheme), getString(R.string.opt_support_email), null))
            startActivity(Intent.createChooser(intent, getString(R.string.opt_title_send_email)))
        }

        btn_to_telegram.setOnClickListener {
            val telegram = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(getString(R.string.opt_link_telegram) + getString(R.string.opt_support_telegram_chat))
            }
            startActivity(telegram)
        }

        btn_to_fd.setOnClickListener {
            val fd = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(getString(R.string.opt_support_freshdesk))
            }
            startActivity(fd)
        }

        btn_to_tutorial.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
        }

        btn_to_back.setOnClickListener {
            onBackPressed()
        }
    }

    companion object{
        const val EXTRA_ID_SUPPORT = "ID"
    }
}