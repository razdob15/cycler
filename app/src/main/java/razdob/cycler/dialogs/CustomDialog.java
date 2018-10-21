package razdob.cycler.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.dynamic.IFragmentWrapper;

import razdob.cycler.R;

/**
 * Created by Raz on 09/05/2018, for project: PlacePicker2
 */
public class CustomDialog extends Dialog {
    private static final String TAG = "CustomDialog";

    private LinearLayout mainLL;
    private Context mContext;
    private Button btn1, btn2;
    private View.OnClickListener click1, click2;
    private String dialogTitle, dialogText, text1, text2;
    private TextView titleTV, dialogTV;
    private int buttonsCount;

    /**
     *
     * @param context - Current Context.
     * @param dialogTitle - dialog's title.
     * @param dialogText - dialog's text.
     * @param buttonsCount - Number of buttons. Max is 2. (more than 2 - puts 2 buttons).
     * @param btn1Text - Buttons1's text.
     * @param btn2Text - Buttons2's text.
     */
    public CustomDialog(@NonNull Context context, String dialogTitle, String dialogText, int buttonsCount,
                        String btn1Text, String btn2Text) {
        super(context);
        this.mContext = context;
        this.click1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: default YES click");
                dismiss();
            }
        };
        this.click2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: default NO click");
                dismiss();
            }
        };
        this.dialogTitle = dialogTitle;
        this.dialogText = dialogText;
        this.text1 = btn1Text;
        this.text2 = btn2Text;
        this.buttonsCount = buttonsCount;
    }


    /**
     *
     * @param context - Current Context.
     * @param dialogText - dialog's text.
     * @param buttonsCount - Number of buttons. Max is 2. (more than 2 - puts 2 buttons).
     * @param btn1Text - Buttons1's text.
     * @param btn2Text - Buttons2's text.
     */
    public CustomDialog(@NonNull Context context, String dialogText, int buttonsCount,
                        String btn1Text, String btn2Text) {
        super(context);
        this.mContext = context;
        this.click1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: default YES click");
                dismiss();
            }
        };
        this.click2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: default NO click");
                dismiss();
            }
        };
        this.dialogText = dialogText;
        this.text1 = btn1Text;
        this.text2 = btn2Text;
        this.buttonsCount = buttonsCount;
        // TODO(!) Continue from here - add the dialog a title ! fix the calling to this method !
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_dialog);
        setCancelable(true);

        mainLL = findViewById(R.id.dialog_main);
        mainLL.setBackgroundResource(R.drawable.my_button);

        titleTV = findViewById(R.id.dialog_title);
        if (dialogTitle == null || dialogTitle.length() == 0)
            titleTV.setVisibility(View.GONE);
        else {
            titleTV.setText(dialogTitle);
            titleTV.setVisibility(View.VISIBLE);
        }

        btn1 = findViewById(R.id.yes_btn);
        btn2 = findViewById(R.id.no_btn);
        if (text1 != null) btn1.setText(text1);
        if (text2 != null) btn2.setText(text2);
        btn1.setOnClickListener(click1);
        btn2.setOnClickListener(click2);
        if (buttonsCount >= 2) {
            btn1.setVisibility(View.VISIBLE);
            btn2.setVisibility(View.VISIBLE);
        } else if (buttonsCount == 1){
            btn1.setVisibility(View.VISIBLE);
            btn2.setVisibility(View.GONE);
        } else {
            btn1.setVisibility(View.GONE);
            btn2.setVisibility(View.GONE);
        }
        dialogTV = findViewById(R.id.dialog_text);
        dialogTV.setText(dialogText);

    }

    /* --------------------------- Getters ------------------------------------- */
    public Button getBtn1() {
        return btn1;
    }

    public Button getBtn2() {
        return btn2;
    }

    /* --------------------------- Setters ------------------------------------- */
    public void setClick1(View.OnClickListener click1) {
        this.click1 = click1;
    }

    public void setClick2(View.OnClickListener click2) {
        this.click2 = click2;
    }

    /**
     * Sets the text1 attr.
     * Updates btn1's text.
     * @param text1 - new text for btn1.
     */
    public void setText1(String text1) {
        this.text1 = text1;
        if (btn1 != null) {
            if (text1 == null) btn1.setVisibility(View.GONE);
            else {
                btn1.setText(text1);
                btn1.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Sets the text2 attr.
     * Updates btn2's text.
     * @param text2 - new text for btn2.
     */
    public void setText2(String text2) {
        this.text2 = text2;
        if (btn2 != null) {
            if (text2 == null) btn2.setVisibility(View.GONE);
            else {
                btn2.setText(text2);
                btn2.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Sets the dialogText attr.
     * updates the dialogTV's text.
     * @param dialogText - new text.
     */
    public void setDialogText(String dialogText) {
        this.dialogText = dialogText;
        if (dialogTV != null) {
            dialogTV.setText(dialogText);
            dialogTV.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets the dialogTitle attr.
     * updates the titleTV's text.
     * @param dialogTitle - new title.
     */
    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        if (titleTV != null) {
            titleTV.setText(dialogTitle);
            titleTV.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

}