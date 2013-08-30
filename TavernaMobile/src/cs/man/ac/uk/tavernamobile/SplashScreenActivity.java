package cs.man.ac.uk.tavernamobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGHT = 1500;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);

        /* New Handler to start the the Main activity 
         * and close this Splash-Screen after 1.5 seconds.*/
        new Handler().postDelayed(new Runnable(){
            public void run() {
            	// TODO: check whether saved password exists
            	
                Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                // Intent mainIntent = new Intent(SplashScreenActivity.this, LoginScreenActivity.class);
                //Intent mainIntent = new Intent(SplashScreenActivity.this, MyExperimentLogin.class);
                SplashScreenActivity.this.startActivity(mainIntent);
                SplashScreenActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }
}