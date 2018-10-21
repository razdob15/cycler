package razdob.cycler.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import razdob.cycler.R;

/**
 * Created by Raz on 09/05/2018, for project: PlacePicker2
 */
public class DeleteDialog extends Dialog {
    private static final String TAG = "DeleteDialog";

    private Context mContext;
    private Button yesBtn, noBtn;
    private View.OnClickListener yesClick, noClick;
    private String dialogText;
    private TextView dialogTV;

    public DeleteDialog(@NonNull Context context, String dialogText) {
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
        this.dialogText = dialogText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_dialog);
        setCancelable(true);
        yesBtn = findViewById(R.id.yes_btn);
        noBtn = findViewById(R.id.no_btn);
        dialogTV = findViewById(R.id.dialog_text);

        yesBtn.setOnClickListener(yesClick);
        noBtn.setOnClickListener(noClick);
        dialogTV.setText(dialogText);
    }

    /* --------------------------- Setters ------------------------------------- */
    public void setYesClick(View.OnClickListener yesClick) {
        this.yesClick = yesClick;
    }

    public void setNoClick(View.OnClickListener noClick) {
        this.noClick = noClick;
    }

    public void setDialogText(String text) {
        dialogTV.setText(text);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

}