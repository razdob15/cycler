package razdob.cycler.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import razdob.cycler.R;

/**
 * Created by Raz on 10/07/2018, for project: PlacePicker2
 */
public class NotRestaurantDialog extends Dialog {
    private static final String TAG = "NotRestaurantDialog";

    private Context mContext;
    private Button yesBtn, noBtn;
    private View.OnClickListener yesClick, noClick;

    // TODO(!): Get the place name and out it in the dialog's text.

    public NotRestaurantDialog(@NonNull Context context) {
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
        setContentView(R.layout.layout_alert_dialog_not_restaurant);
        setCancelable(false);
        yesBtn = findViewById(R.id.yes_btn);
        noBtn = findViewById(R.id.no_btn);
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