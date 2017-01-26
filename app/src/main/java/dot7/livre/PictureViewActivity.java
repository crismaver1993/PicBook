package dot7.livre;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import dot7.livre.auxiliares.TouchImageView;

/**
 * Created by GTIM on 09/05/2016.
 */
public class PictureViewActivity extends AppCompatActivity {
    String pathDestination, renameFile; //String con el pathDestino
    String oldpath, newPath;
    String nameFol;
    int width, height;
    String parentpath;
    String vengo;
    private String currentpath;

    File parentFolder;//Guarda en directory general
    File currentFolder;//Guarda en directory general
    File nowFolder;
    Context context = this;
    TextView idCurrent;
    ImageView  imgFoto2;
    Display display;
    Uri imageUri;
    Intent i;

    //zoom
    private TouchImageView imgFoto;
    private TextView scrollPositionTextView;
    private TextView zoomedRectTextView;
    private TextView currentZoomTextView;
    private DecimalFormat df;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picturefianalview);

        //Appbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle values = getIntent().getExtras();
        parentpath = values.getString("parent");
        currentpath = values.getString("current");
        vengo = values.getString("vengo");
        idCurrent = (TextView) findViewById(R.id.idCurrent);
        imgFoto = (TouchImageView) findViewById(R.id.imgPictureView);
        if (parentpath != null && currentpath != null) {
            oldpath = currentpath;
            parentFolder = new File(parentpath);
            currentFolder = new File(currentpath);
            idCurrent.setText(currentFolder.getName());
            //   Toast.makeText(PictureViewActivity.this, currentpath, Toast.LENGTH_SHORT).show();


            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels; // ancho absoluto en pixels
            height = metrics.heightPixels; // alto absoluto en pixels


            String pathName = currentpath;
            // Bitmap bm = BitmapFactory.decodeFile(pathName);
            Bitmap bm = ShrinkBitmap(pathName, width, height);
            imgFoto.setImageBitmap(bm);

            imgFoto.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {

                @Override
                public void onMove() {
                    PointF point = imgFoto.getScrollPosition();
                    RectF rect = imgFoto.getZoomedRect();
                    float currentZoom = imgFoto.getCurrentZoom();
                    boolean isZoomed = imgFoto.isZoomed();

                }
            });

        }//if
    }//oncreate

    Bitmap ShrinkBitmap(String file, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }



}

    /*

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }





public void clickMenu (View view) {
    //
    PopupMenu popup = new PopupMenu(PictureViewActivity.this, view);
    setForceShowIcon(popup);
    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.popupmenu, popup.getMenu());
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.itemrename:
                    //
                    final EditText edittext = new EditText(PictureViewActivity.this);
                    edittext.requestFocus();
                    new AlertDialog.Builder(PictureViewActivity.this)
                            .setMessage(R.string.rename_instruction_file)
                            .setTitle(R.string.rename)
                            .setView(edittext)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    renameFile = String.valueOf(edittext.getText().toString());
                                    if (renameFile.length() > 0) {

                                        try {

                                            File oldFolder = new File(oldpath + "/" + currentpath);
                                            newPath = parentpath + "/" + renameFile + ".png";
                                            File newFolder = new File(newPath);
                                            boolean success = oldFolder.renameTo(newFolder);
                                            if (success) {
                                                Toast.makeText(context, R.string.success_renamed, Toast.LENGTH_SHORT).show();
                                            }

                                        } catch (Exception ex) {
                                            Log.e("Files", "Error to rename the File ");
                                        }

                                    } else {
                                        Toast toast = Toast.makeText(PictureViewActivity.this, R.string.fail_rename_file, Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                        toast.show();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                    //
                    return true;


                case R.id.itemremove:
                    try {
                        File fileRemove = new File(currentpath);

                        boolean success = fileRemove.delete();
                        if (success) {
                            Toast.makeText(context, R.string.delete, Toast.LENGTH_SHORT).show();

                            switch(vengo){
                                case "main":
                                     i=new Intent(PictureViewActivity.this,MainActivity.class);
                                    break;

                                case "explorer":
                                    i=new Intent(PictureViewActivity.this,ExplorerActivity.class);

                                    break;
                            }

                            startActivity(i);
                            finish();
                        }
                    } catch (Exception ex) {
                        Log.e("Files", "Error to delete the File ");
                    }

                    break;

                case R.id.itemshare:


                    newPath = oldpath + "/" + currentpath;


                    final ContentValues values = new ContentValues(2);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.DATA, newPath);
                    final Uri contentUriFile = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("image/jpg");
                    intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
                    startActivity(Intent.createChooser(intent, "title"));


                    break;

            }
            return false;
        }

    });
    popup.show();

}*/


