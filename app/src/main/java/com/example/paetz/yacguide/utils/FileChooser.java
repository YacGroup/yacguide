package com.example.paetz.yacguide.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FileChooser {

    private final Context _context;
    private ListView _list;
    private Dialog _dialog;
    private File _storagePath;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public FileChooser setFileListener(FileSelectedListener fileListener) {
        _fileListener = fileListener;
        return this;
    }

    private FileSelectedListener _fileListener;

    public FileChooser(Context context, String defaultFileName) {
        _context = context;
        _storagePath = Environment.getExternalStoragePublicDirectory("YACguide");
        _dialog = new Dialog(context);
        _dialog.setContentView(R.layout.choose_file_dialog);
        _list = (ListView) _dialog.findViewById(R.id.filesListView);
        _list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                final String fileChosen = (String) _list.getItemAtPosition(which);
                ((EditText) _dialog.findViewById(R.id.fileNameEditText)).setText(fileChosen);
            }
        });
        _dialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final File chosenFilePath = new File(_storagePath, ((EditText) _dialog.findViewById(R.id.fileNameEditText)).getText().toString());
                if (_fileListener != null) {
                    _fileListener.fileSelected(chosenFilePath);
                }
                _dialog.dismiss();
            }
        });
        _dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _dialog.dismiss();
            }
        });
        ((TextView) _dialog.findViewById(R.id.currentPathTextView)).setText("Pfad: " + _storagePath.getAbsolutePath());
        ((EditText) _dialog.findViewById(R.id.fileNameEditText)).setText(defaultFileName);
    }

    public void showDialog() {
        if (!_storagePath.canWrite()) {
            Toast.makeText(_context, "SD Karte nicht beschreibbar.\nBitte Speicherberechtigung zulassen", Toast.LENGTH_SHORT).show();
            return;
        }
        _displayContent();
        _dialog.show();
    }

    private void _displayContent() {
        if (!_storagePath.exists()) {
            _storagePath.mkdirs();
        }
        File[] files = _storagePath.listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                return (file.canRead() && !file.isDirectory() && file.getName().endsWith(".json"));
            }
        });

        String[] fileList = new String[files.length];
        Arrays.sort(files);
        int i = 0;
        for (final File file : files ) {
            fileList[i++] = file.getName();
        }

        _list.setAdapter(new ArrayAdapter(_context,
                android.R.layout.simple_list_item_1, fileList) {
            @Override public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });
    }
}
