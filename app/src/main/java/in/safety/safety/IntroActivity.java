package in.safety.safety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.nightonke.wowoviewpager.Animation.ViewAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoAlphaAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoPathAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoScaleAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoTextViewTextAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoTranslation3DAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoTranslationAnimation;
import com.nightonke.wowoviewpager.Enum.Ease;
import com.nightonke.wowoviewpager.Enum.WoWoTypewriter;
import com.nightonke.wowoviewpager.WoWoPathView;
import com.nightonke.wowoviewpager.WoWoViewPager;
import com.nightonke.wowoviewpager.WoWoViewPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class IntroActivity extends AppCompatActivity {
    private WoWoViewPager wowo;
    private ArrayList<Integer> colors;
    private Button continueBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        getWindow().setStatusBarColor(getResources().getColor(R.color.introScreen));
        sharedPreferences = getSharedPreferences("AppStats", MODE_PRIVATE);
        continueBtn = findViewById(R.id.btn_continue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("firstRun", false).apply();
                startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
                finish();
            }
        });
        getSupportActionBar().hide();
        colors = new ArrayList<>();
        colors.add(R.color.introScreen);
        colors.add(R.color.introScreen);
        colors.add(R.color.introScreen);
        colors.add(R.color.introScreen);
        wowo = (WoWoViewPager)findViewById(R.id.wowo_viewpager);
        Display mdisp = getWindowManager().getDefaultDisplay();
        int maxX= mdisp.getWidth();
        int maxY= mdisp.getHeight();
//        getWindow().getDecorView().getWidth();
        wowo.setAdapter(WoWoViewPagerAdapter.builder()
                .fragmentManager(getSupportFragmentManager())
                .count(4)                       // Fragment Count
                .colorsRes(colors)                // Colors of fragments, with transparent as default
                .build());
        ViewAnimation viewAnimation1 = new ViewAnimation(findViewById(R.id.img1));
        viewAnimation1.setEase(Ease.OutQuint);
        viewAnimation1.add(WoWoTranslationAnimation.builder()
                .page(0)
                .fromXY(0)
                .toY(maxY)
                .toX(0)
                .build());
        ViewAnimation viewAnimation2 = new ViewAnimation(findViewById(R.id.txt1));
        viewAnimation2.setEase(Ease.OutQuint);
        viewAnimation2.add(WoWoTranslationAnimation.builder()
                .page(0)
                .fromXY(0)
                .toY(-maxY/2.0)
                .toX(0)
                .build());
        ViewAnimation viewAnimation3 = new ViewAnimation(findViewById(R.id.img2));
        viewAnimation3.setEase(Ease.OutQuint);
        viewAnimation3.add(WoWoTranslationAnimation.builder()
                .page(0)
                .fromX(0)
                .fromY(-maxY)
                .toXY(0)
                .build());
        viewAnimation3.add(WoWoTranslationAnimation.builder()
                .page(1)
                .fromXY(0)
                .toX(0)
                .toY(-maxY)
                .build());
        ViewAnimation viewAnimation4 = new ViewAnimation(findViewById(R.id.txt2));
        viewAnimation4.setEase(Ease.OutQuint);
        viewAnimation4.add(WoWoTranslationAnimation.builder()
                .page(0)
                .fromX(0)
                .fromY(maxY)
                .toXY(0)
                .build());
        viewAnimation4.add(WoWoTextViewTextAnimation.builder().page(1)
                .from("Sends live location, camera and audio updates to your friends and family when in emergency").to("Alerts people near you whenever you need help").typewriter(WoWoTypewriter.DeleteThenType).build());
        viewAnimation4.add(WoWoTextViewTextAnimation.builder().page(2)
                .from("Alerts people near you whenever you need help").to("Lets you file reports without revealing your identity").typewriter(WoWoTypewriter.DeleteThenType).build());
        ViewAnimation viewAnimation5 = new ViewAnimation(findViewById(R.id.img3));
        viewAnimation5.setEase(Ease.OutQuint);
        viewAnimation5.add(WoWoTranslationAnimation.builder()
                .page(0)
                .fromX(0)
                .fromY(-maxY)
                .toXY(-maxY)
                .build());
        viewAnimation5.add(WoWoTranslationAnimation.builder()
                .page(1)
                .fromX(0)
                .fromY(-maxY)
                .toXY(0)
                .build());
        viewAnimation5.add(WoWoTranslationAnimation.builder()
                .page(2)
                .fromXY(0)
                .toY(-maxY)
                .toX(0)
                .build());
        ViewAnimation viewAnimation6 = new ViewAnimation(findViewById(R.id.img4));
        viewAnimation6.setEase(Ease.OutQuint);
        viewAnimation6.add(WoWoTranslationAnimation.builder()
                .page(0)
                .fromX(0)
                .fromY(-maxY)
                .toXY(-maxY)
                .build());
        viewAnimation6.add(WoWoTranslationAnimation.builder()
                .page(2)
                .fromX(0)
                .fromY(-maxY)
                .toXY(0)
                .build());
        ViewAnimation viewAnimation7 = new ViewAnimation(continueBtn);
        viewAnimation7.add(WoWoAlphaAnimation.builder()
                .page(0)
                .from(0)
                .to(0)
                .build());
        viewAnimation7.add(WoWoAlphaAnimation.builder()
                .page(2)
                .from(0)
                .to(1)
                .start(0.35)
                .end(1)
                .build());
        wowo.addAnimation(viewAnimation1);
        wowo.addAnimation(viewAnimation2);
        wowo.addAnimation(viewAnimation3);
        wowo.addAnimation(viewAnimation4);
        wowo.addAnimation(viewAnimation5);
        wowo.addAnimation(viewAnimation6);
        wowo.addAnimation(viewAnimation7);
        wowo.ready();
    }
}