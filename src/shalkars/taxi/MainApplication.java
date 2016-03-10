package shalkars.taxi;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static MainApplication instance = new MainApplication();

    public MainApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "wYwjvSd8hs05UPISchXs1wEkIxJ7orGX43V1YaF0", "VSb0LZRRBp0lFU7Byd04F3YHHdArTdjgJr3a4iHV");
        PushService.setDefaultPushCallback(this, Main2Activity.class);
        //PushService.subscribe(this, "Barca", FabulaClient.class);
        //ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}