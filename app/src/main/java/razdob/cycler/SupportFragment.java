package razdob.cycler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import razdob.cycler.myUtils.MyFonts;
import razdob.cycler.R;

/**
 * Created by Raz on 27/09/2018, for project: PlacePicker2
 */
public class SupportFragment extends Fragment {
    private static final String TAG = "SupportFragment";

    private Context mContext;
    private TextView emailTv;
    // ToolBar
    private TextView mainTitleTv, cyclerSticker;
    private ImageView checkMark, backArrow;

    // Vars
    private MyFonts mFonts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support, container, false);

        mContext = getActivity();
        emailTv = view.findViewById(R.id.support_email_tv);
        mainTitleTv = view.findViewById(R.id.profile_name_tv);
        checkMark = view.findViewById(R.id.save_changes);
        backArrow = view.findViewById(R.id.back_arrow);
        cyclerSticker = view.findViewById(R.id.cycler_sticker);
        mFonts = new MyFonts(mContext);

        checkMark.setVisibility(View.GONE);
        emailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: send email...");
                sendEmail();
            }
        });
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to InstProfileActivity");
                Objects.requireNonNull(getActivity()).finish();
            }
        });
        mainTitleTv.setText(R.string.support_title);
        cyclerSticker.setTypeface(mFonts.getBoldItalicFont());

        return view;
    }

    private void sendEmail() {
        Log.d(TAG, "sendEmail: called.");

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","email@email.com", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Your Subject");
        intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.support_email_hint));
        startActivity(Intent.createChooser(intent, "Choose an Email app:"));

    }
}
