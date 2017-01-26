package dot7.livre.auxiliares;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import dot7.livre.MainActivity;
import dot7.livre.R;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ItemHolder>{

    private List<Uri> itemsUri;
    private LayoutInflater layoutInflater;
    private Context context;
    private OnItemClickListener onItemClickListener;
    MainActivity mainActivity;
    private ArrayList<String> mNamesFolder = new ArrayList<String>();


    public MyRecyclerViewAdapter(Context context){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        itemsUri = new ArrayList<Uri>();

        //this.mainActivity = mainActivity;
    }

    @Override
    public MyRecyclerViewAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView itemCardView = (CardView)layoutInflater.inflate(R.layout.file_item, parent, false);
        return new ItemHolder(itemCardView, this);
    }

    @Override
    public void onBindViewHolder(MyRecyclerViewAdapter.ItemHolder holder, int position) {
        Uri targetUri = itemsUri.get(position);
        holder.setItemUri(targetUri.getPath());
        holder.imageView.setImageResource(R.drawable.folder);
        holder.textView.setText(mNamesFolder.get(position));

        if (targetUri != null){

            try {
                //! CAUTION !
                //I'm not sure is it properly to load bitmap here!
                holder.setImageView(loadScaledBitmap(targetUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    reference:
    Load scaled bitmap
    http://android-er.blogspot.com/2013/08/load-scaled-bitmap.html
     */
    private Bitmap loadScaledBitmap(Uri src) throws FileNotFoundException {

        //display the file to be loadScaledBitmap(),
        //such that you can know how much work on it.
      //  mainActivity.textInfo.append(src.getLastPathSegment() + "\n");

        // required max width/height
        final int REQ_WIDTH = 150;
        final int REQ_HEIGHT = 150;

        Bitmap bm = null;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(src),
                null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, REQ_WIDTH,
                REQ_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(src), null, options);

        return bm;
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return itemsUri.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener(){
        return onItemClickListener;
    }

    public interface OnItemClickListener{
        public void onItemClick(ItemHolder item, int position);
    }

    public void add(int location, Uri iUri,String name){
        itemsUri.add(location, iUri);
        mNamesFolder.add(name);
        notifyDataSetChanged();
        notifyItemInserted(location);
    }

    public void clearAll(){
        int itemCount = itemsUri.size();

        if(itemCount>0){
            itemsUri.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }


    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private MyRecyclerViewAdapter parent;
        private CardView cardView;
        TextView textView;
        ImageView imageView;
        String itemUri;

        public ItemHolder(CardView cardView, MyRecyclerViewAdapter parent) {
            super(cardView);
            itemView.setOnClickListener(this);
            this.cardView = cardView;
            this.parent = parent;
            imageView = (ImageView) cardView.findViewById(R.id.item_file_image);
            textView=(TextView)cardView.findViewById(R.id.item_name_file);
        }

        public void setItemUri(String itemUri){
            this.itemUri = itemUri;
        }

        public String getItemUri(){
            return itemUri;
        }

        public void setImageView(Bitmap bitmap){
            imageView.setImageBitmap(bitmap);
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = parent.getOnItemClickListener();
            if(listener != null){
                listener.onItemClick(this, getLayoutPosition());
                //or use
                //listener.onItemClick(this, getAdapterPosition());
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



}
