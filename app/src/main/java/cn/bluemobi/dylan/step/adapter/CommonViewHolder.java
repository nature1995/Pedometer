package cn.bluemobi.dylan.step.adapter;


import android.util.SparseArray;
import android.view.View;

/**
 *
 * common ViewHolder
 *
 * @author
 */
public class CommonViewHolder {
    /**
     * @param view root View for all views
     * @param id   view id
     * @return
     */
    public static <T extends View> T get(View view, int id) {

        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        //if root view without any view
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);//get related with root View
        }
        View chidlView = viewHolder.get(id);//get the child of root View
        if (chidlView == null) {//no child
            //get the child
            chidlView = view.findViewById(id);
            viewHolder.put(id, chidlView);//store the views
        }
        return (T) chidlView;
    }
}
