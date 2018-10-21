package razdob.cycler.myUtils;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Raz on 27/06/2018, for project: PlacePicker2
 */
public interface RecyclerPlaceTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}