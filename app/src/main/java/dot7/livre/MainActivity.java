package dot7.livre;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dot7.livre.auxiliares.ItemClickSupport;
import dot7.livre.auxiliares.MasonryAdapter;
import dot7.livre.auxiliares.MyRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {
    /*Variables Gráficas*/
    ImageView imgFoto;
    Uri imageUri;
    private RecyclerView mRecyclerView, myRecyclerView;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    MasonryAdapter adapter;
    MyRecyclerViewAdapter adapterFile;
    private RelativeLayout drawerContainer;
    private DrawerLayout drawerLayout;
    TextView idCurrent, nameFile2;
    FloatingActionButton btnFoto, btnCarpeta;

    /*Variables globales*/
    String nameFol, renameFile;
    final int PICTURE_RESULT = 0;
    int Request_Camera = 1, Request_Storage = 2;
    File genFolderLivre = new File(Environment.getExternalStorageDirectory() + "/PicBook");//Guarda en directory general
    File defaultFolder = new File(Environment.getExternalStorageDirectory() + "/PicBook/default");//Guarda en directory general
    String pathDestination = String.valueOf(Environment.getExternalStorageDirectory()) + "/PicBook"; //String con el pathDestino
    String oldpath = pathDestination, newPath;
    String imageurl, item,itemMove, itemName,folderCargar;
    File nowFolder;
    Context context = this;
    ArrayList fileArrayDialog = null, fileArrayDialogPath = null;
    String[] nameListFol, nameListFolPath;
    boolean success;
    //load
    private static final int SELECT_PICTURE = 1;
    private static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Appbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        idCurrent = (TextView) findViewById(R.id.idCurrent);
        idCurrent.setText("PicBook");

        if (checkPermisosStorage()) {
            createLivreFolder();
            mRecyclerView = null;
            myRecyclerView = null;
            reCreateVista();
        } else {
            if (genFolderLivre.exists()) {
                mRecyclerView = null;
                myRecyclerView = null;
                reCreateVista();
            }
        }

        FloatingActionsMenu floatingMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        ((FloatingActionsMenu) floatingMenu).setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                if (checkPermisosStorage()) ;

            }

            @Override
            public void onMenuCollapsed() {
            }
        });

    }//oncreate

    public boolean checkPermisosStorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            //storage
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (!genFolderLivre.exists()) {
                    createLivreFolder();
                }

            } else {
                Toast.makeText(MainActivity.this, R.string.store_need, Toast.LENGTH_SHORT).show();
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //   Toast.makeText(MainActivity.this, "Storage permission is needed to create a new Folder", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Request_Storage);
            }

        } else {
            return true;
        }
        return false;
    }//checkRequest

    public boolean checkPermisosCamera() {
        if (Build.VERSION.SDK_INT >= 23) {
            //camera
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (defaultFolder.exists()) {
                    takePic();
                } else {
                    createLivreFolder();
                }

            } else {
                Toast.makeText(MainActivity.this, R.string.camera_need, Toast.LENGTH_SHORT).show();
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {

                }
                requestPermissions(new String[]{Manifest.permission.CAMERA}, Request_Camera);
            }
        } else {
            return true;
        }
        return false;
    }//checkRequest

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == Request_Storage) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!genFolderLivre.exists()) {
                    createLivreFolder();
                    mRecyclerView = null;
                    myRecyclerView = null;
                    reCreateVista();
                } else {
                    mRecyclerView = null;
                    myRecyclerView = null;
                    reCreateVista();
                }

            } else {
                Toast.makeText(MainActivity.this, R.string.no_store, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == Request_Camera) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (defaultFolder.exists()) {
                    takePic();
                } else {
                    createLivreFolder();
                }

            } else {
                Toast.makeText(MainActivity.this, R.string.no_camera, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void createLivreFolder() {
        //cuarda en directory
        if (!genFolderLivre.exists()) {
            genFolderLivre.mkdir();
            if (genFolderLivre.exists()) {
                defaultFolder.mkdir();
                mRecyclerView = null;
                myRecyclerView = null;
                reCreateVista();

            }
        } else {
            if (genFolderLivre.exists() && !defaultFolder.exists()) {
                defaultFolder.mkdir();
                mRecyclerView = null;
                myRecyclerView = null;
                reCreateVista();
            }
        }
    }

    public void clickCarpeta(View view) {
        //  if(checkPermisosStorage()){
        createFolder();
        // }
    }

    public void clickFoto(View view) {
        if (genFolderLivre.exists()) {
            if (checkPermisosCamera()) {
                takePic();
            }
        } else {
            if (checkPermisosStorage()) {
                createLivreFolder();
            }
        }
    }

    private void createFolder() {
        try {
            if (genFolderLivre.exists()) {
                final EditText edittext = new EditText(MainActivity.this);
                edittext.requestFocus();

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.new_folder_instruction)
                        .setTitle(R.string.newfolder_title)
                        .setView(edittext)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                nameFol = String.valueOf(edittext.getText().toString());
                                if (nameFol.length() > 0) {

                                    String path = String.valueOf(Environment.getExternalStorageDirectory()) + "/PicBook";
                                    try {
                                        File ruta_sd = new File(path);
                                        File folder = new File(ruta_sd.getAbsolutePath(), nameFol);
                                        boolean success;
                                        if (!folder.exists()) {
                                            success = folder.mkdir();
                                            if (success) {
                                                Toast.makeText(MainActivity.this, R.string.success_folder, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (Exception ex) {
                                        Log.e("Carpetas", "Error al crear Carpeta a tarjeta SD");
                                    }
                                    mRecyclerView = null;
                                    myRecyclerView = null;
                                    reCreateVista();
                                } else {
                                    Toast toast = Toast.makeText(MainActivity.this, R.string.fail_folder, Toast.LENGTH_SHORT);
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
            }
        } catch (Exception e) {

        }
    }

    private void takePic() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");

        try {
            fileArrayDialog = new ArrayList();
            fileArrayDialogPath = new ArrayList();
            File f = new File(pathDestination);
            File[] files = f.listFiles();
            if (files.length > 0) {
                for (File inFile : files) {
                    if (inFile.isDirectory()) {
                        fileArrayDialog.add(inFile.getName());
                        fileArrayDialogPath.add(inFile.getPath());
                    }
                }
            }
            if (fileArrayDialog.size() > 0 && fileArrayDialogPath.size() > 0) {
                nameListFol = new String[fileArrayDialog.size() + 1];
                nameListFolPath = new String[fileArrayDialogPath.size() + 1];

                for (int pos = 0; pos < nameListFol.length - 1; pos++) {
                    nameListFol[pos] = String.valueOf(fileArrayDialog.get(pos));
                    nameListFolPath[pos] = String.valueOf(fileArrayDialogPath.get(pos));
                }

                nameListFol[nameListFol.length - 1] = "PicBook";
                nameListFolPath[nameListFol.length - 1] = pathDestination;
            } else {
                nameListFol = new String[1];
                nameListFolPath = new String[1];
                nameListFol[0] = "PicBook";
                nameListFolPath[0] = pathDestination;
            }

            if (nameListFolPath.length > 0) {

                if (nameListFol.length > 0) {
                    AlertDialog.Builder myDialog =
                            new AlertDialog.Builder(MainActivity.this);
                    myDialog.setTitle(R.string.choose_folder);

                    myDialog.setItems(nameListFol, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
                            item = String.valueOf(nameListFolPath[which]);
                            itemName = String.valueOf(nameListFol[which]);
                            File file = new File(item + "/img_" + timeStamp + ".png");//dnde guardo la foto
                            imageUri = Uri.fromFile(file);

                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, PICTURE_RESULT);

                            Toast.makeText(getApplicationContext(),
                                    "Save at: " + itemName, Toast.LENGTH_SHORT).show();
                        }
                    });
                    myDialog.setNegativeButton(R.string.cancel, null);
                    myDialog.show();
                }
            }
        } catch (Exception e) {

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case PICTURE_RESULT:
                if (requestCode == PICTURE_RESULT)
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), imageUri);
                            //  imgFoto.setImageBitmap(thumbnail);
                            imageurl = getRealPathFromURI(imageUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mRecyclerView = null;
                        myRecyclerView = null;
                        reCreateVista();
                    }

            case SELECT_PICTURE:
                if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    File nameNewFolder=new File(folderCargar);
                    File oldFolder = new File(picturePath);
                    newPath = folderCargar + "/" + oldFolder.getName();
                    File newFolder = new File(newPath);
                    boolean success = oldFolder.renameTo(newFolder);
                    oldFolder.delete();

                    if(success){
                        Toast.makeText(getApplicationContext(),
                                "Load at: " + nameNewFolder.getName(), Toast.LENGTH_SHORT).show();
                        mRecyclerView = null;
                        myRecyclerView = null;
                        reCreateVista();
                    }

                }

        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void listarDirectorio() {

        try {
            pathDestination = String.valueOf(Environment.getExternalStorageDirectory()) + "/PicBook";//pasar ubicación
            File f = new File(pathDestination);
            File[] files = f.listFiles();
            adapter = new MasonryAdapter(context);
            adapterFile = new MyRecyclerViewAdapter(context);

            if (files.length > 0) {
                for (File inFile : files) {
                    if (inFile.isDirectory()) {
                        adapter.addItem(inFile.getName());
                    }
                    if (inFile.isFile()) {
                        Uri uri = Uri.fromFile(inFile);
                        myRecyclerViewAdapter.add(
                                myRecyclerViewAdapter.getItemCount(),
                                uri, inFile.getName());

                        //  Log.v("xxx", "" + inFile);
                        //  Log.v("xxx", "" + inFile.getName());
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public void reCreateVista() {

        try {
            mRecyclerView = null;
            mRecyclerView = (RecyclerView) findViewById(R.id.masonry_grid);
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
            myRecyclerView = (RecyclerView) findViewById(R.id.myrecyclerview);

            myRecyclerViewAdapter = new MyRecyclerViewAdapter(this);
            // myRecyclerViewAdapter.setOnItemClickListener(MainActivity.this);
            myRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

            listarDirectorio();
            mRecyclerView.setAdapter(adapter);
            myRecyclerView.setAdapter(myRecyclerViewAdapter);

//folder navegar
            ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                    TextView nameFolder = (TextView) v.findViewById(R.id.name_folder);
                    String nameCarpeta = String.valueOf(nameFolder.getText().toString());

                    newPath = oldpath + "/" + nameCarpeta;
                    nowFolder = new File(newPath);
                    Intent i = new Intent(MainActivity.this, ExplorerActivity.class);
                    i.putExtra("parent", oldpath);
                    i.putExtra("current", newPath);
                    i.putExtra("nameparent", nameCarpeta);
                    startActivity(i);
                }
            });

            //
            ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View v) {
                    //  Toast.makeText(MainActivity.this, "Presión larga", Toast.LENGTH_SHORT).show();
                    PopupMenu popup = new PopupMenu(MainActivity.this, v);
                    setForceShowIcon(popup);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.popupmenu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            TextView txtFolder = (TextView) v.findViewById(R.id.name_folder);
                            final String nameFolder = String.valueOf(txtFolder.getText().toString());

                            switch (item.getItemId()) {
                                case R.id.itemrename:
                                    //
                                    final EditText edittext = new EditText(MainActivity.this);
                                    edittext.requestFocus();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage(R.string.folderrename_instruction)
                                            .setTitle(R.string.rename)
                                            .setView(edittext)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    renameFile = String.valueOf(edittext.getText().toString());
                                                    if (renameFile.length() > 0) {

                                                        try {
                                                            File oldFolder = new File(oldpath + "/" + nameFolder);
                                                            newPath = oldpath + "/" + renameFile;
                                                            File newFolder = new File(newPath);
                                                            boolean success = oldFolder.renameTo(newFolder);
                                                            if (success) {
                                                                Toast.makeText(context, R.string.success_renamed, Toast.LENGTH_SHORT).show();
                                                            }

                                                        } catch (Exception ex) {
                                                            Log.e("Files", "Error to rename the Folder ");
                                                        }
                                                        mRecyclerView = null;
                                                        myRecyclerView = null;
                                                        reCreateVista();
                                                    } else {
                                                        Toast toast = Toast.makeText(MainActivity.this, R.string.fail_rename_folder, Toast.LENGTH_SHORT);
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


                                case R.id.itemmove:
                                    Toast.makeText(MainActivity.this, R.string.moveFolder, Toast.LENGTH_SHORT).show();
                                    return true;

                                case R.id.itemload:
                                    folderCargar=oldpath+"/"+nameFolder;
                                    Intent i = new Intent(
                                            Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                                    return true;


                                case R.id.itemremove:
                                    final AlertDialog.Builder removeDialog = new AlertDialog.Builder(MainActivity.this);

                                    removeDialog.setTitle(R.string.confirmRemove);

                                    removeDialog.setMessage(R.string.msgConfirmRemove);

                                    removeDialog.setNegativeButton(R.string.cancel, null);

                                    removeDialog.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int arg1) {

                                            try {
                                                File fileRemove = new File(oldpath + "/" + nameFolder);

                                                for (File tempFile : fileRemove.listFiles()) {
                                                    success = tempFile.delete();
                                                }

                                                success = fileRemove.delete();

                                                if (success) {
                                                    Toast.makeText(context, R.string.delete, Toast.LENGTH_SHORT).show();
                                                    mRecyclerView = null;
                                                    myRecyclerView = null;
                                                    reCreateVista();
                                                }
                                            } catch (Exception ex) {
                                                Log.e("Files", "Error to delete the Folder ");
                                            }

                                        }

                                    });
                                    removeDialog.show();
                                    return true;

                                case R.id.itemshare:
                                    Toast.makeText(MainActivity.this, R.string.shareinvalid, Toast.LENGTH_SHORT).show();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popup.show();
                    return false;
                }
            });

            //file Long CLick
            ItemClickSupport.addTo(myRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View v) {
                    //  Toast.makeText(MainActivity.this, "Presión larga", Toast.LENGTH_SHORT).show();
                    PopupMenu popup = new PopupMenu(MainActivity.this, v);
                    setForceShowIcon(popup);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.popupmenu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            TextView txtFile = (TextView) v.findViewById(R.id.item_name_file);
                            final String nameFile = String.valueOf(txtFile.getText().toString());

                            switch (item.getItemId()) {
                                case R.id.itemrename:
                                    //
                                    final EditText edittext = new EditText(MainActivity.this);
                                    edittext.requestFocus();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage(R.string.rename_instruction_file)
                                            .setTitle(R.string.rename)
                                            .setView(edittext)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    renameFile = String.valueOf(edittext.getText().toString());
                                                    if (renameFile.length() > 0) {
                                                        try {
                                                            File oldFolder = new File(oldpath + "/" + nameFile);
                                                            newPath = oldpath + "/" + renameFile + ".png";
                                                            File newFolder = new File(newPath);
                                                            boolean success = oldFolder.renameTo(newFolder);
                                                            if (success) {
                                                                Toast.makeText(context, R.string.success_renamed, Toast.LENGTH_SHORT).show();
                                                            }

                                                        } catch (Exception ex) {
                                                            Log.e("Files", "Error to rename the File ");
                                                        }
                                                        mRecyclerView = null;
                                                        myRecyclerView = null;
                                                        reCreateVista();
                                                    } else {
                                                        Toast toast = Toast.makeText(MainActivity.this, R.string.fail_rename_file, Toast.LENGTH_SHORT);
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

                                case R.id.itemmove:


                                    try {
                                        fileArrayDialog = new ArrayList();
                                        fileArrayDialogPath = new ArrayList();
                                        File f = new File(pathDestination);
                                        File[] files = f.listFiles();
                                        if (files.length > 0) {
                                            for (File inFile : files) {
                                                if (inFile.isDirectory()) {
                                                    fileArrayDialog.add(inFile.getName());
                                                    fileArrayDialogPath.add(inFile.getPath());
                                                }
                                            }
                                        }
                                        if (fileArrayDialog.size() > 0 && fileArrayDialogPath.size() > 0) {
                                            nameListFol = new String[fileArrayDialog.size() ];
                                            nameListFolPath = new String[fileArrayDialogPath.size() ];

                                            for (int pos = 0; pos <= nameListFol.length - 1; pos++) {
                                                nameListFol[pos] = String.valueOf(fileArrayDialog.get(pos));
                                                nameListFolPath[pos] = String.valueOf(fileArrayDialogPath.get(pos));
                                            }


                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.nomove, Toast.LENGTH_SHORT).show();
                                        }

                                        if (nameListFolPath.length > 0) {

                                            if (nameListFol.length > 0) {
                                                AlertDialog.Builder myDialog =
                                                        new AlertDialog.Builder(MainActivity.this);
                                                myDialog.setTitle(R.string.choose_folder);

                                                myDialog.setItems(nameListFol, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        itemMove = String.valueOf(nameListFolPath[which]);
                                                        itemName = String.valueOf(nameListFol[which]);

                                                        File oldFolder = new File(oldpath + "/" + nameFile);
                                                        newPath = itemMove + "/" + nameFile;
                                                        File newFolder = new File(newPath);
                                                        boolean success = oldFolder.renameTo(newFolder);
                                                        oldFolder.delete();

                                                        if(success){
                                                            Toast.makeText(getApplicationContext(),
                                                                    "Move to: " + itemName, Toast.LENGTH_SHORT).show();
                                                            mRecyclerView = null;
                                                            myRecyclerView = null;
                                                            reCreateVista();
                                                        }
                                                    }
                                                });
                                                myDialog.setNegativeButton(R.string.cancel, null);
                                                myDialog.show();
                                            }
                                        }
                                    } catch (Exception e) {

                                    }

                                    return true;

                                case R.id.itemload:
                                    Toast.makeText(MainActivity.this, R.string.loadFile, Toast.LENGTH_SHORT).show();
                                    return true;


                                case R.id.itemremove:
                                    final AlertDialog.Builder removeDialog = new AlertDialog.Builder(MainActivity.this);

                                    removeDialog.setTitle(R.string.confirmRemove);

                                    removeDialog.setMessage(R.string.msgConfirmRemove);

                                    removeDialog.setNegativeButton(R.string.cancel, null);

                                    removeDialog.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int arg1) {
                                            try {
                                                File fileRemove = new File(oldpath + "/" + nameFile);

                                                boolean success = fileRemove.delete();
                                                if (success) {
                                                    Toast.makeText(context, R.string.delete, Toast.LENGTH_SHORT).show();
                                                    mRecyclerView = null;
                                                    myRecyclerView = null;
                                                    reCreateVista();
                                                }
                                            } catch (Exception ex) {
                                                Log.e("Files", "Error to delete the File ");
                                            }
                                        }

                                    });
                                    removeDialog.show();//
                                    mRecyclerView = null;
                                    myRecyclerView = null;
                                    reCreateVista();
                                    return true;

                                case R.id.itemshare:
                                    nameFile2 = (TextView) v.findViewById(R.id.item_name_file);
                                    String newNameFile = String.valueOf(nameFile2.getText().toString());

                                    newPath = oldpath + "/" + newNameFile;

                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("image/jpg");
                                    final File photoFile = new File(newPath);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
                                    startActivity(Intent.createChooser(shareIntent, "Share image using"));

                                    return true;

                                default:
                                    return false;
                            }
                        }
                    });
                    popup.show();
                    return false;
                }
            });

            //click normal
            ItemClickSupport.addTo(myRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                    nameFile2 = (TextView) v.findViewById(R.id.item_name_file);
                    String newNameFile = String.valueOf(nameFile2.getText().toString());

                    newPath = oldpath + "/" + newNameFile;
                    nowFolder = new File(newPath);
                    Intent i = new Intent(MainActivity.this, PictureViewActivity.class);
                    i.putExtra("parent", oldpath);
                    i.putExtra("current", newPath);
                    i.putExtra("vengo", "main");
                    startActivity(i);
                }
            });

        } catch (Exception e) {

        }

    }

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

    public void clickHelp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.helpTitle);
        builder.setMessage(R.string.help);
        builder.setPositiveButton("OK", null);
        builder.create();
        builder.show();
    }

    public void clickRefresh(View view) {
        mRecyclerView = null;
        myRecyclerView = null;
        reCreateVista();
    }
}