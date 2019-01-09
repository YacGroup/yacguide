package com.example.paetz.yacguide.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.paetz.yacguide.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FileChooser {

    private static final String _PARENT_DIR = "..";

    private final Context _context;
    private ListView _list;
    private Dialog _dialog;
    private File _currentPath;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public FileChooser setFileListener(FileSelectedListener fileListener) {
        _fileListener = fileListener;
        return this;
    }

    private FileSelectedListener _fileListener;

    public FileChooser(Context context, boolean external) {
        _context = context;
        _dialog = new Dialog(context);
        _dialog.setContentView(R.layout.choose_file_dialog);
        _list = (ListView) _dialog.findViewById(R.id.filesListView);
        _list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                final String fileChosen = (String) _list.getItemAtPosition(which);
                final File chosenFile = _getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    _refresh(chosenFile);
                } else {
                    if (_fileListener != null) {
                        _fileListener.fileSelected(chosenFile.getAbsoluteFile());
                    }
                    _dialog.dismiss();
                }
            }
        });
        _dialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_fileListener != null) {
                    _fileListener.fileSelected(_currentPath.getAbsoluteFile());
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
        if (external) {
            _refresh(_dialog.getContext().getExternalFilesDir(null));
        } else {
            _refresh(_dialog.getContext().getFilesDir());
        }
    }

    public void showDialog() {
        _dialog.show();
    }

    private void _refresh(File path) {
        _currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    return (file.canRead() && file.isDirectory());
                }
            });

            File[] files = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    return (file.canRead() && !file.isDirectory() && file.getName().endsWith(".json"));
                }
            });

            int i = 0;
            String[] fileList;
            if (path.getParentFile() == null || path.getParentFile().listFiles() == null) {
                fileList = new String[dirs.length + files.length];
            } else {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = _PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for (File dir : dirs) { fileList[i++] = dir.getName() +  "/"; }
            for (File file : files ) { fileList[i++] = file.getName(); }

            ((TextView) _dialog.findViewById(R.id.currentPathTextView)).setText(_currentPath.getPath());
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

    private File _getChosenFile(String fileChosen) {
        if (fileChosen.equals(_PARENT_DIR)) {
            return _currentPath.getParentFile();
        } else {
            return new File(_currentPath, fileChosen);
        }
    }
}
