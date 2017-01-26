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
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;

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

public class ExplorerActivity extends AppCompatActivity implements MyRecyclerViewAdapter.OnItemClickListener {

    ImageView imgFoto;
    private RecyclerView mRecyclerView, myRecyclerView;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    MasonryAdapter adapter;
    MyRecyclerViewAdapter adapterFile;
    File parentFolder;//Guarda en directory general
    File currentFolder;//Guarda en directory general
    File nowFolder;
    Context context = this;
    TextView idCurrent;

    //Globales
    int Request_Camera = 1, Request_Storage = 2;
    final int PICTURE_RESULT = 0;
    Uri imageUri;
    String pathDestination; //String con el pathDestino
    String oldpath, newPath;
    String nameFol, renameFile;
    String parentpath, nameparent,folderCargar;
    private String currentpath;
    ArrayList fileArrayDialog = null, fileArrayDialogPath = null;
    String[] nameListFol, nameListFolPath;
    String imageurl, itemFile, itemFileName;
    boolean success;
    //load
    private static final int SELECT_PICTURE = 1;
    private static final int RESULT_LOAD_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  myPath = (TextView)findViewById(R.id.name_folder);

        //Appbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle values = getIntent().getExtras();
        parentpath = values.getString("parent");
        currentpath = values.getString("current");
        nameparent = values.getString("nameparent");

        idCurrent = (TextView) findViewById(R.id.idCurrent);

        if (parentpath != null && currentpath != null) {

            oldpath = currentpath;
            parentFolder = new File(parentpath);
            currentFolder = new File(currentpath);

            idCurrent.setText(currentFolder.getName());

            if (currentFolder.exists()) {
                reCreateVista();
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
        }

    }//oncreate

    public boolean checkPermisosCamera() {
        if (Build.VERSION.SDK_INT >= 23) {
            //camera
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePic();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(ExplorerActivity.this, R.string.camera_need, Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.CAMERA}, Request_Camera);
            }
        } else {
            return true;
        }
        return false;
    }//checkRequest

    public boolean checkPermisosStorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            //storage
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(ExplorerActivity.this, R.string.store_need, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Request_Camera) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePic();
            } else {
                Toast.makeText(ExplorerActivity.this, R.string.no_camera, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == Request_Storage) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ;
            } else {
                Toast.makeText(ExplorerActivity.this, R.string.no_store, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public void clickCarpeta(View view) {

        try {
            if (currentFolder.exists()) {
                final EditText edittext = new EditText(ExplorerActivity.this);
                edittext.requestFocus();
                new AlertDialog.Builder(ExplorerActivity.this)
                        .setMessage(R.string.new_folder_instruction)
                        .setTitle(R.string.newfolder_title)
                        .setView(edittext)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                nameFol = String.valueOf(edittext.getText().toString());
                                if (nameFol.length() > 0) {

                                    String path = currentpath;
                                    try {
                                        File ruta_sd = new File(path);
                                        File folder = new File(ruta_sd.getAbsolutePath(), nameFol);
                                        boolean success = true;
                                        if (!folder.exists()) {
                                            success = folder.mkdir();
                                        }
                                        if (success) {
                                            Toast.makeText(ExplorerActivity.this, R.string.success_folder, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        Log.e("Carpetas", "Error al crear Carpeta a tarjeta SD");
                                    }
                                    mRecyclerView = null;
                                    myRecyclerView = null;
                                    reCreateVista();


                                } else {
                                    Toast toast = Toast.makeText(ExplorerActivity.this, R.string.fail_folder, Toast.LENGTH_SHORT);
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
            } else {
                Toast.makeText(ExplorerActivity.this, R.string.no_rootfolder, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }

    }

    public void clickFoto(View view) {
        if (checkPermisosCamera()) {
            takePic();
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

                nameListFol[nameListFol.length - 1] = nameparent;//current
                nameListFolPath[nameListFol.length - 1] = pathDestination;
            } else {
                nameListFol = new String[1];
                nameListFolPath = new String[1];
                nameListFol[0] = nameparent;//current
                nameListFolPath[0] = pathDestination;
            }

            if (nameListFolPath.length > 0) {
                if (nameListFol.length > 0) {
                    AlertDialog.Builder myDialog =
                            new AlertDialog.Builder(ExplorerActivity.this);
                    myDialog.setTitle(R.string.choose_folder);

                    myDialog.setItems(nameListFol, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
                            itemFile = String.valueOf(nameListFolPath[which]);
                            itemFileName = String.valueOf(nameListFol[which]);
                            File file = new File(itemFile + "/photo_" + timeStamp + ".png");//dnde guardo la foto
                            imageUri = Uri.fromFile(file);

                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, PICTURE_RESULT);

                            Toast.makeText(getApplicationContext(),
                                    "Save at: " + itemFileName, Toast.LENGTH_SHORT).show();
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
                            // imgFoto.setImageBitmap(thumbnail);
                            imageurl = getRealPathFromURI(imageUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mRecyclerView = null;
                        myRecyclerView = null;
                        //   listarDirectorio();
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
        pathDestination = currentpath;//pasar ubicación
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
                }
            }
        }
    }

    public void reCreateVista() {

        try {
            mRecyclerView = null;
            mRecyclerView = (RecyclerView) findViewById(R.id.masonry_grid);
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
            myRecyclerView = (RecyclerView) findViewById(R.id.myrecyclerview);

            myRecyclerViewAdapter = new MyRecyclerViewAdapter(ExplorerActivity.this);
            myRecyclerViewAdapter.setOnItemClickListener(this);
            myRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
            listarDirectorio();
            mRecyclerView.setAdapter(adapter);
            myRecyclerView.setAdapter(myRecyclerViewAdapter);

            ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    TextView nameFile = (TextView) v.findViewById(R.id.name_folder);
                    String nameFilePath = String.valueOf(nameFile.getText().toString());

                    newPath = oldpath + "/" + nameFilePath;
                    nowFolder = new File(newPath);
                    Intent i = new Intent(ExplorerActivity.this, ExplorerActivity.class);
                    i.putExtra("parent", oldpath);
                    i.putExtra("current", newPath);
                    i.putExtra("nameparent", nameFilePath);
                    startActivity(i);
                }
            });

            ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View v) {
                    //  Toast.makeText(MainActivity.this, "Presión larga", Toast.LENGTH_SHORT).show();
                    PopupMenu popup = new PopupMenu(ExplorerActivity.this, v);
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
                                    final EditText edittext = new EditText(ExplorerActivity.this);
                                    edittext.requestFocus();
                                    new AlertDialog.Builder(ExplorerActivity.this)
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
                                                        Toast toast = Toast.makeText(ExplorerActivity.this, R.string.fail_rename_file, Toast.LENGTH_SHORT);
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
                                    Toast.makeText(ExplorerActivity.this, R.string.moveFolder, Toast.LENGTH_SHORT).show();
                                    return true;

                                case R.id.itemload:
                                    folderCargar=oldpath+"/"+nameFolder;
                                    Intent i = new Intent(
                                            Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                                    return true;



                                case R.id.itemremove:
                                    final AlertDialog.Builder removeDialog = new AlertDialog.Builder(ExplorerActivity.this);
                                    removeDialog.setTitle(R.string.confirmRemove);

                                    removeDialog.setMessage(R.string.msgConfirmRemove);

                                    removeDialog.setNegativeButton(R.string.cancel, null);

                                    removeDialog.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int arg1) {

                                    try {
                                        File fileRemove = new File(oldpath + "/" + nameFolder);

                                        for(File tempFile : fileRemove.listFiles()) {
                                            success=  tempFile.delete();
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
                                    removeDialog.show();//


                                    return true;

                                case R.id.itemshare:
                                    Toast.makeText(ExplorerActivity.this, R.string.shareinvalid, Toast.LENGTH_SHORT).show();
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


            //file
            ItemClickSupport.addTo(myRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, final View v) {
                    //  Toast.makeText(MainActivity.this, "Presión larga", Toast.LENGTH_SHORT).show();
                    PopupMenu popup = new PopupMenu(ExplorerActivity.this, v);
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
                                    final EditText edittext = new EditText(ExplorerActivity.this);
                                    edittext.requestFocus();
                                    new AlertDialog.Builder(ExplorerActivity.this)
                                            .setMessage(R.string.rename_instruction_file)
                                            .setTitle(R.string.rename)
                                            .setView(edittext)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    renameFile = String.valueOf(edittext.getText().toString());
                                                    if (renameFile.length() > 0) {

                                                        try {
                                                            File oldFolder = new File(oldpath + "/" + nameFile);
                                                            newPath = oldpath + "/" + renameFile+".png";
                                                            File newFolder = new File(newPath);
                                                            boolean success = oldFolder.renameTo(newFolder);
                                                            if (success) {
                                                                Toast.makeText(context, R.string.rename, Toast.LENGTH_SHORT).show();
                                                            }

                                                        } catch (Exception ex) {
                                                            Log.e("Files", "Error to rename the File ");
                                                        }
                                                        mRecyclerView = null;
                                                        myRecyclerView = null;
                                                        reCreateVista();
                                                    } else {
                                                        Toast toast = Toast.makeText(ExplorerActivity.this, R.string.fail_rename_file, Toast.LENGTH_SHORT);
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
                                            nameListFol = new String[fileArrayDialog.size() + 1];
                                            nameListFolPath = new String[fileArrayDialogPath.size() + 1];

                                            for (int pos = 0; pos < nameListFol.length - 1; pos++) {
                                                nameListFol[pos] = String.valueOf(fileArrayDialog.get(pos));
                                                nameListFolPath[pos] = String.valueOf(fileArrayDialogPath.get(pos));
                                            }

                                            File parentP=new File(parentpath);
                                            String anterior=parentP.getName();
                                            nameListFol[nameListFol.length - 1] = anterior;//current
                                            nameListFolPath[nameListFol.length - 1] = parentpath;
                                        } else {
                                            nameListFol = new String[1];
                                            nameListFolPath = new String[1];

                                            File parentP=new File(parentpath);
                                            String anterior=parentP.getName();
                                            nameListFol[0] = anterior;//current parentpath
                                            nameListFolPath[0] = parentpath;
                                        }

                                        if (nameListFolPath.length > 0) {
                                            if (nameListFol.length > 0) {
                                                AlertDialog.Builder myDialog =
                                                        new AlertDialog.Builder(ExplorerActivity.this);
                                                myDialog.setTitle(R.string.choose_folder);

                                                myDialog.setItems(nameListFol, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        itemFile = String.valueOf(nameListFolPath[which]);
                                                        itemFileName = String.valueOf(nameListFol[which]);


                                                        File oldFolder = new File(oldpath + "/" + nameFile);
                                                        newPath = itemFile + "/" + nameFile;
                                                        File newFolder = new File(newPath);
                                                        boolean success = oldFolder.renameTo(newFolder);
                                                        oldFolder.delete();


                                                        if(success){
                                                            Toast.makeText(getApplicationContext(),
                                                                    "Move to: " + itemFileName, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ExplorerActivity.this, R.string.loadFile, Toast.LENGTH_SHORT).show();
                                    return true;


                                case R.id.itemremove:
                                    final AlertDialog.Builder removeDialog = new AlertDialog.Builder(ExplorerActivity.this);
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


                                    return true;

                                case R.id.itemshare:
                                   TextView nameFile2 = (TextView) v.findViewById(R.id.item_name_file);
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

            //File click Item
            ItemClickSupport.addTo(myRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    TextView nameFolder = (TextView) v.findViewById(R.id.item_name_file);
                    String nameCarpeta = String.valueOf(nameFolder.getText().toString());

                    newPath = oldpath + "/" + nameCarpeta;
                    nowFolder = new File(newPath);
                    Intent i = new Intent(ExplorerActivity.this, PictureViewActivity.class);
                    i.putExtra("parent", oldpath);
                    i.putExtra("current", newPath);
                    i.putExtra("vengo","explorer");
                    startActivity(i);
                }
            });
        } catch (Exception e) {

        }
    }

    @Override
    public void onItemClick(MyRecyclerViewAdapter.ItemHolder item, int position) {

        String stringitemUri = item.getItemUri();
        Toast.makeText(ExplorerActivity.this, stringitemUri, Toast.LENGTH_SHORT).show();
//oldpath + "/" +
        newPath = stringitemUri;
        nowFolder = new File(stringitemUri);
        Intent i = new Intent(ExplorerActivity.this, PictureViewActivity.class);
        i.putExtra("parent", oldpath);
        i.putExtra("current", newPath);
        startActivity(i);
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


    public void clickHelp(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.helpTitle);
        builder.setMessage(R.string.help);
        builder.setPositiveButton("OK",null);
        builder.create();
        builder.show();
    }

    public void onBackPressed() {
        finish();
    }

    public void clickRefresh(View view) {
        mRecyclerView = null;
        myRecyclerView = null;
        reCreateVista();
    }
}