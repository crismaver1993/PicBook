package dot7.livre.auxiliares;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dot7.livre.MainActivity;
import dot7.livre.R;

/**
 * Created by GTIM on 20/04/2016.
 */
public class MasonryAdapter extends RecyclerView.Adapter<MasonryAdapter.MasonryView> {

    private List<Uri> itemsUri;
    private LayoutInflater layoutInflater;
    private ItemClickSupport.OnItemClickListener onItemClickListener;
    MainActivity mainActivity;

    //
    private Context context;
    private ArrayList<String> mImages = new ArrayList<String>();
    private ArrayList<String> mNamesFolder = new ArrayList<String>();

    public MasonryAdapter(Context context) {
        this.context = context;
        //
        layoutInflater = LayoutInflater.from(context);
        itemsUri = new ArrayList<Uri>();
    }

    public void addItem(String name) {
        mNamesFolder.add(name);
        notifyDataSetChanged();
    }

    public void addItemFile(String name2) {
        mImages.add(name2);
        notifyDataSetChanged();
    }

    public int getCount() {
        return mNamesFolder.size();
    }

    public String getItem(int position) {
        return mNamesFolder.get(position);
    }

    @Override
    public int getItemCount() {
        return mNamesFolder.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public MasonryView onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        MasonryView masonryView = new MasonryView(layoutView);
        return masonryView;
    }

    @Override
    public void onBindViewHolder(MasonryView holder, int position) {
        //holder.imageView.setImageResource(imgList[position]);
        holder.imageView.setImageResource(R.drawable.folder);
        holder.textView.setText(mNamesFolder.get(position));
    }

    class MasonryView extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public MasonryView(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_image);
            textView = (TextView) itemView.findViewById(R.id.name_folder);

        }
    }

    //Bitmap

}