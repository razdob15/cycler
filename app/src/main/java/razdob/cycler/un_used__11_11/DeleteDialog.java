package razdob.cycler.un_used__11_11;

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
 * Created by Raz on 09/05/2018, for project: PlacePicker2
 */
public class DeleteDialog extends Dialog {
    private static final String TAG = "DeleteDialog";

    private Context mContext;
    private Button yesBtn, noBtn;
    private View.OnClickListener yesClick, noClick;
    private String text, title;
    private TextView textTV, titleTV;

    public DeleteDialog(@NonNull Context context,String dialogTitle, String dialogText) {
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
        this.text = dialogText;
        this.title = dialogTitle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_dialog);
        setCancelable(true);
        yesBtn = findViewById(R.id.yes_btn);
        noBtn = findViewById(R.id.no_btn);
        textTV = findViewById(R.id.dialog_text);
        titleTV = findViewById(R.id.dialog_title);

        yesBtn.setOnClickListener(yesClick);
        noBtn.setOnClickListener(noClick);
        textTV.setText(text);
        titleTV.setText(title);
    }

    /* --------------------------- Setters ------------------------------------- */
    public void setYesClick(View.OnClickListener yesClick) {
        this.yesClick = yesClick;
    }

    public void setNoClick(View.OnClickListener noClick) {
        this.noClick = noClick;
    }

    public void setText(String text) {
        textTV.setText(text);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

}