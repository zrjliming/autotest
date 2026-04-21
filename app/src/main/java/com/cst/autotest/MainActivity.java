package com.cst.autotest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cst.autotest.utils.PackageUtils;

/**
 * Displays the CulebraTester2 helper status and the command used to start the server.
 */
public class MainActivity extends Activity {
    /**
     * Creates a small Java UI equivalent to the original CulebraTester2 status screen.
     *
     * @param savedInstanceState previous activity state, if Android is recreating the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(22);
        title.setText(getString(R.string.app_name));
        layout.addView(title);

        TextView message = new TextView(this);
        message.setGravity(Gravity.CENTER);
        message.setTextSize(16);
        message.setText(PackageUtils.isInstrumentationPresent(this) != null
                ? getString(R.string.msg_start_server)
                : getString(R.string.msg_instrumentation_not_installed));
        layout.addView(message);

        TextView command = new TextView(this);
        command.setGravity(Gravity.CENTER);
        command.setText(getString(R.string.msg_start_server_command));
        layout.addView(command);

        setContentView(layout);
    }
}
