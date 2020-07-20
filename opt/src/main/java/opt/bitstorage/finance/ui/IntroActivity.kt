package opt.bitstorage.finance.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import opt.bitstorage.finance.R

class IntroActivity : AppIntro() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(
                description = "Новая услуга Bistorage",
                imageDrawable = R.drawable.screenshot_2_1
        ))
        addSlide(AppIntroFragment.newInstance(
                description = "Торговая пара.",
                imageDrawable = R.drawable.screenshot_2_2
        ))
        addSlide(AppIntroFragment.newInstance(
                description = "График изменения цены.",
                imageDrawable = R.drawable.screenshot_2_3
        ))
        addSlide(AppIntroFragment.newInstance(
                description = "Ваш баланс на счёте бинарных опционов.",
                imageDrawable = R.drawable.screenshot_2_4
        ))
        addSlide(AppIntroFragment.newInstance(
                description = "Если вы считаете что цена пойдёт в верх укажите размер ставки и нажмите вверх. Если считаете что цена пойдёт вниз нажмите вниз.",
                imageDrawable = R.drawable.screenshot_2_5
        ))
        addSlide(AppIntroFragment.newInstance(
                description = "Для работы с бинарными опционами необходимо пополнить свой счёт. С которого можно вывести нажав Withdraw.",
                imageDrawable = R.drawable.screenshot_2_6
        ))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()

        complete()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        finish()

        complete()
    }

    @SuppressLint("ApplySharedPref")
    fun complete() {
        val sp = getSharedPreferences("BO_SPLASH_SP", Context.MODE_PRIVATE)
        sp.edit().putBoolean("bo_splash_complete", true).commit()
    }
}