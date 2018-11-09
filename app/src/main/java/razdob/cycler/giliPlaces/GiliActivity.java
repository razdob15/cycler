package razdob.cycler.giliPlaces;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import razdob.cycler.R;

/**
 * Created by Raz on 09/11/2018, for project: Cycler
 */
public class GiliActivity extends AppCompatActivity {

    private RecyclerView recView1, recView2;
    private RelativeLayout rel1, rel2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gili);

        rel1 = findViewById(R.id.rel_layout1);
        rel2 = findViewById(R.id.rel_layout2);

        recView1 = rel1.findViewById(R.id.recycler_view);
        recView2 = rel2.findViewById(R.id.recycler_view);

        setupRecycler();
    }

    private void setupRecycler() {
        ArrayList<String> data1 = new ArrayList<>();
        data1.add("a");
        data1.add("b");
        data1.add("c");
        data1.add("d");

        ArrayList<String> data2 = new ArrayList<>();
        data1.add("1");
        data1.add("2");
        data1.add("3");
        data1.add("4");

        GilPlacesAdapter adapter1 = new GilPlacesAdapter(this, data1);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recView1.setLayoutManager(layoutManager1);
        recView1.setItemAnimator(new DefaultItemAnimator());
        recView1.setAdapter(adapter1);


        GilPlacesAdapter adapter2 = new GilPlacesAdapter(this, data2);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recView2.setLayoutManager(layoutManager2);
        recView2.setItemAnimator(new DefaultItemAnimator());
        recView2.setAdapter(adapter2);



    }


}
