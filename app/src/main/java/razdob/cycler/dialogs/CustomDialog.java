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

import razdob.cycler.R;

/**
 * Created by Raz on 09/05/2018, for project: PlacePicker2
 */
public class CustomDialog extends Dialog {
    private static final String TAG = "CustomDialog";

    private static final int MY_BUTTON_BACKGROUND = R.drawable.my_button;
    private static final int COLOR_WHITE = R.color.colorWhite;

    private LinearLayout mainLL;
    private Button btn1, btn2;
    private View.OnClickListener click1, click2;
    private String dialogTitle, dialogText, text1, text2;
    private TextView titleTV, dialogTV;
    private int buttonsCount;
    private int backgroundRes;

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
        this.backgroundRes = MY_BUTTON_BACKGROUND;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_dialog);
        setCancelable(true);



        mainLL = findViewById(R.id.dialog_main);
        mainLL.setBackgroundResource(backgroundRes);

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
        if (titleTV != null && dialogTitle != null) {
            titleTV.setText(dialogTitle);
            titleTV.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onBackPressed() {
        dismiss();
    }

    public void setBackgroundRes(int backgroundRes) {
        this.backgroundRes = backgroundRes;
    }

    /**
     * Creates delete dialog (btn1- 'YES', btn2 - 'NO').
     * @param context - App Context.
     * @param title - Dialog's title.
     * @param text - Dialog's text.
     * @return - new CustomDialog with 'YES' \ 'NO' Buttons.
     */
    public static CustomDialog createDeleteDialog(Context context, String title, String text) {
        CustomDialog deleteDialog = new CustomDialog(context, title, text, 2, "YES", "NO");
        deleteDialog.setBackgroundRes(COLOR_WHITE);
        return  deleteDialog;
    }

    public static CustomDialog createNotRestaurantDialog(Context context) {
        CustomDialog notRestDialog = new CustomDialog(context, context.getString(R.string.not_rest_dialog_title), context.getString(R.string.not_rest_dialog_text),
                2, "YES", "NO");
        notRestDialog.setBackgroundRes(MY_BUTTON_BACKGROUND);
        return  notRestDialog;
    }

    public static CustomDialog createMustLocationDialog(Context context) {
        CustomDialog notRestDialog = new CustomDialog(context, context.getString(R.string.must_location_dialog_title), context.getString(R.string.must_location_dialog_text),
                2, "Choose Now", "OK");
        notRestDialog.setBackgroundRes(MY_BUTTON_BACKGROUND);
        return notRestDialog;
    }

    public static CustomDialog createTwoButtonsDialog(Context context, String title,
                                                String text, String btn1text, String btn2text) {
        CustomDialog notRestDialog = new CustomDialog(context, title, text,
                2, btn1text, btn2text);
        notRestDialog.setBackgroundRes(MY_BUTTON_BACKGROUND);
        return notRestDialog;
    }

    public static CustomDialog createOneButtonDialog(Context context, String title,
                                                      String text, String btnText) {
        CustomDialog notRestDialog = new CustomDialog(context, title, text,
                1, btnText, null);
        notRestDialog.setBackgroundRes(MY_BUTTON_BACKGROUND);
        return notRestDialog;
    }



}