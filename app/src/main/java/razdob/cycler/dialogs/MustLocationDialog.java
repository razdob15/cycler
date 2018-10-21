package razdob.cycler.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import razdob.cycler.R;

/**
 * Created by Raz on 10/07/2018, for project: PlacePicker2
 */
public class MustLocationDialog extends Dialog {
    private static final String TAG = "MustLocationDialog";

    private Context mContext;
    private Button yesBtn, noBtn;
    private View.OnClickListener yesClick, noClick;

    public MustLocationDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
        this.yesClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: default YES click");
                dismiss();
            }
        };
        this.noClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: default NO click");
                dismiss();
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_custom_dialog_two_buttons);
        setCancelable(false);

        yesBtn = findViewById(R.id.yes_btn);
        noBtn = findViewById(R.id.no_btn);

        // Texts
        TextView titleTV = findViewById(R.id.dialog_title);
        TextView textTV = findViewById(R.id.dialog_text);
        titleTV.setText(mContext.getString(R.string.must_location_dialog_title));
        textTV.setText(mContext.getString(R.string.must_location_dialog_text));

        yesBtn.setText("Choose Now");
        noBtn.setText("OK");

        yesBtn.setOnClickListener(yesClick);
        noBtn.setOnClickListener(noClick);
    }


    /* --------------------------- Setters ------------------------------------- */
    public void setYesClick(View.OnClickListener yesClick) {
        this.yesClick = yesClick;
    }

    public void setNoClick(View.OnClickListener noClick) {
        this.noClick = noClick;
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }
}