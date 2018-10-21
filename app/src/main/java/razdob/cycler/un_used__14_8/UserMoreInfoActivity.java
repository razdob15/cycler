package razdob.cycler.un_used__14_8;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import razdob.cycler.R;

public class UserMoreInfoActivity extends AppCompatActivity {

    // TODO(!) Continue this activity. build a User reference.

    private Button birthdayBtn;
    private DatePickerDialog.OnDateSetListener mOnDateSetListener;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_more_info);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.i("dateset", "year = " + year + "month = " + month + "day = "+ dayOfMonth);
            }
        };
        birthdayBtn = findViewById(R.id.birthday_btn);
        birthdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("bithday_click", "HERE1");
                databaseReference.child("persons").child("test111").setValue("YESS !");
                Log.i("bithday_click", "" + databaseReference.child("persons").getKey());

            }
        });


    }
}

